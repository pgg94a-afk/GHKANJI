package com.gg.ghkanji

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.gg.ghkanji.data.ExamProgress
import com.gg.ghkanji.data.ExamProgressManager
import com.gg.ghkanji.data.ExamResult
import com.gg.ghkanji.data.ExamResultManager
import com.gg.ghkanji.data.KanjiItem
import com.gg.ghkanji.data.KanjiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// 문제 상태 관리
sealed class QuizPhase {
    object UnInput : QuizPhase()
    data class UndokQuiz(val currentIndex: Int, val totalCount: Int) : QuizPhase()
    data class HoondokQuiz(val currentIndex: Int, val totalCount: Int) : QuizPhase()
    object Completed : QuizPhase()
}

// 각 문제의 점수 정보
data class QuestionScore(
    val unCorrect: Boolean = false,
    val undokCorrectCount: Int = 0,
    val undokTotalCount: Int = 0,
    val hoondokCorrectCount: Int = 0,
    val hoondokTotalCount: Int = 0
)

@Composable
fun ExamScreen(
    grade: Int,
    totalKanji: Int,
    onBackClick: () -> Unit = {},
    onExamFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val examResultManager = remember { ExamResultManager(context) }
    val examProgressManager = remember { ExamProgressManager(context) }

    var kanjiList by remember { mutableStateOf<List<KanjiItem>>(emptyList()) }
    var originalKanjiList by remember { mutableStateOf<List<KanjiItem>>(emptyList()) }  // 섞기 전 원본 리스트
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var currentPhase by remember { mutableStateOf<QuizPhase>(QuizPhase.UnInput) }
    var questionScores by remember { mutableStateOf<MutableMap<Int, QuestionScore>>(mutableMapOf()) }
    var isLoading by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }

    // 데이터 로드 및 진행 상태 복원
    LaunchedEffect(Unit) {
        scope.launch {
            KanjiRepository.loadKanjiData()
            val kanji = KanjiRepository.getKanjiByGrade(grade)
            kanji?.let {
                originalKanjiList = it.kanjiList

                // 저장된 진행 상태 확인
                val savedProgress = examProgressManager.getProgress(grade)
                if (savedProgress != null) {
                    // 저장된 상태 복원
                    kanjiList = savedProgress.kanjiIndices.map { index -> originalKanjiList[index] }
                    currentQuestionIndex = savedProgress.currentQuestionIndex
                    questionScores = savedProgress.getQuestionScoresAsIntMap().toMutableMap()

                    // QuizPhase 복원
                    currentPhase = when (savedProgress.currentPhaseType) {
                        ExamProgress.PHASE_UNDOK_QUIZ -> QuizPhase.UndokQuiz(
                            savedProgress.currentPhaseIndex,
                            savedProgress.currentPhaseTotalCount
                        )
                        ExamProgress.PHASE_HOONDOK_QUIZ -> QuizPhase.HoondokQuiz(
                            savedProgress.currentPhaseIndex,
                            savedProgress.currentPhaseTotalCount
                        )
                        else -> QuizPhase.UnInput
                    }
                } else {
                    // 새로운 시험 시작
                    kanjiList = it.kanjiList.shuffled()
                }
            }
            isLoading = false
        }
    }

    // 상태 변경 시 자동 저장
    LaunchedEffect(currentQuestionIndex, currentPhase, questionScores.size) {
        if (!isLoading && kanjiList.isNotEmpty()) {
            scope.launch {
                // 한자 인덱스 찾기
                val kanjiIndices = kanjiList.map { kanji ->
                    originalKanjiList.indexOf(kanji)
                }

                // QuizPhase 정보 추출
                val (phaseType, phaseIndex, phaseTotalCount) = when (currentPhase) {
                    is QuizPhase.UndokQuiz -> {
                        val phase = currentPhase as QuizPhase.UndokQuiz
                        Triple(ExamProgress.PHASE_UNDOK_QUIZ, phase.currentIndex, phase.totalCount)
                    }
                    is QuizPhase.HoondokQuiz -> {
                        val phase = currentPhase as QuizPhase.HoondokQuiz
                        Triple(ExamProgress.PHASE_HOONDOK_QUIZ, phase.currentIndex, phase.totalCount)
                    }
                    else -> Triple(ExamProgress.PHASE_UN_INPUT, 0, 0)
                }

                val progress = ExamProgress(
                    grade = grade,
                    kanjiIndices = kanjiIndices,
                    currentQuestionIndex = currentQuestionIndex,
                    currentPhaseType = phaseType,
                    currentPhaseIndex = phaseIndex,
                    currentPhaseTotalCount = phaseTotalCount,
                    questionScores = questionScores.mapKeys { it.key.toString() },
                    timestamp = System.currentTimeMillis()
                )

                examProgressManager.saveProgress(progress)
            }
        }
    }

    // 뒤로가기 처리
    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("시험 나가기") },
            text = { Text("시험을 나가시겠습니까? 진행 상황은 저장되어 나중에 이어서 볼 수 있습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBackClick()
                }) {
                    Text("나가기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("계속하기")
                }
            }
        )
    }

    if (isLoading || kanjiList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFDF5))
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFE97878))
        }
    } else {
        // 모든 문제 완료 시
        if (currentQuestionIndex >= kanjiList.size) {
            // 점수 계산 및 결과 저장
            LaunchedEffect(Unit) {
                val totalScore = calculateTotalScore(kanjiList, questionScores)
                val attemptNumber = examResultManager.getNextAttemptNumber(grade)

                val examResult = ExamResult.create(
                    grade = grade,
                    attemptNumber = attemptNumber,
                    totalQuestions = kanjiList.size,
                    correctAnswers = (totalScore * kanjiList.size / 100.0).toInt()
                )

                examResultManager.saveExamResult(examResult)

                // 시험 완료 시 진행 상태 삭제
                examProgressManager.clearProgress(grade)

                onExamFinished()
            }
            return
        }

        val currentKanji = kanjiList[currentQuestionIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFDF5))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 상단 바 (뒤로가기 + 제목) - 항상 표시
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF8B6F5C),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "${grade}-${grade} 한자시험",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4A42)
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 진행 상황 표시
                Text(
                    text = "${currentQuestionIndex + 1}/${kanjiList.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8B6F5C),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 진행 바
                LinearProgressIndicator(
                    progress = (currentQuestionIndex + 1) / kanjiList.size.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFE97878),
                    trackColor = Color(0xFFEDB4B4)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Un 입력 화면
                if (currentPhase is QuizPhase.UnInput) {
                    UnInputScreen(
                        kanji = currentKanji,
                        onAnswerSubmit = { isCorrect ->
                            val currentScore = questionScores[currentQuestionIndex] ?: QuestionScore()
                            questionScores[currentQuestionIndex] = currentScore.copy(unCorrect = isCorrect)

                            if (isCorrect) {
                                // 음독 퀴즈로 이동
                                val undokList = parseReadings(currentKanji.kanjiUndokHiragana)
                                if (undokList.isNotEmpty()) {
                                    currentPhase = QuizPhase.UndokQuiz(0, undokList.size)
                                } else {
                                    // 음독이 없으면 훈독으로
                                    val hoondokList = parseReadings(currentKanji.kanjiHoondokHiragana)
                                    if (hoondokList.isNotEmpty()) {
                                        currentPhase = QuizPhase.HoondokQuiz(0, hoondokList.size)
                                    } else {
                                        // 둘 다 없으면 다음 문제로
                                        currentQuestionIndex++
                                        currentPhase = QuizPhase.UnInput
                                    }
                                }
                            } else {
                                // 오답이면 다음 문제로
                                currentQuestionIndex++
                                currentPhase = QuizPhase.UnInput
                            }
                        }
                    )
                }
            }
        }

        // 음독 팝업
        if (currentPhase is QuizPhase.UndokQuiz) {
            val undokPhase = currentPhase as QuizPhase.UndokQuiz
            val undokList = parseReadings(currentKanji.kanjiUndokHiragana)

            if (undokPhase.currentIndex < undokList.size) {
                ReadingQuizDialog(
                    title = "음독 읽기",
                    question = "${undokPhase.currentIndex + 1}/${undokPhase.totalCount}",
                    correctAnswer = undokList[undokPhase.currentIndex],
                    allAnswers = undokList,
                    onAnswerSelected = { isCorrect ->
                        val currentScore = questionScores[currentQuestionIndex] ?: QuestionScore()
                        val newUndokCorrect = if (isCorrect) currentScore.undokCorrectCount + 1 else currentScore.undokCorrectCount
                        questionScores[currentQuestionIndex] = currentScore.copy(
                            undokCorrectCount = newUndokCorrect,
                            undokTotalCount = undokList.size
                        )

                        // 다음 음독 문제로
                        if (undokPhase.currentIndex + 1 < undokList.size) {
                            currentPhase = QuizPhase.UndokQuiz(undokPhase.currentIndex + 1, undokPhase.totalCount)
                        } else {
                            // 음독 완료, 훈독으로
                            val hoondokList = parseReadings(currentKanji.kanjiHoondokHiragana)
                            if (hoondokList.isNotEmpty()) {
                                currentPhase = QuizPhase.HoondokQuiz(0, hoondokList.size)
                            } else {
                                // 훈독이 없으면 다음 문제로
                                currentQuestionIndex++
                                currentPhase = QuizPhase.UnInput
                            }
                        }
                    }
                )
            }
        }

        // 훈독 팝업
        if (currentPhase is QuizPhase.HoondokQuiz) {
            val hoondokPhase = currentPhase as QuizPhase.HoondokQuiz
            val hoondokList = parseReadings(currentKanji.kanjiHoondokHiragana)

            if (hoondokPhase.currentIndex < hoondokList.size) {
                ReadingQuizDialog(
                    title = "훈독 읽기",
                    question = "${hoondokPhase.currentIndex + 1}/${hoondokPhase.totalCount}",
                    correctAnswer = hoondokList[hoondokPhase.currentIndex],
                    allAnswers = hoondokList,
                    onAnswerSelected = { isCorrect ->
                        val currentScore = questionScores[currentQuestionIndex] ?: QuestionScore()
                        val newHoondokCorrect = if (isCorrect) currentScore.hoondokCorrectCount + 1 else currentScore.hoondokCorrectCount
                        questionScores[currentQuestionIndex] = currentScore.copy(
                            hoondokCorrectCount = newHoondokCorrect,
                            hoondokTotalCount = hoondokList.size
                        )

                        // 다음 훈독 문제로
                        if (hoondokPhase.currentIndex + 1 < hoondokList.size) {
                            currentPhase = QuizPhase.HoondokQuiz(hoondokPhase.currentIndex + 1, hoondokPhase.totalCount)
                        } else {
                            // 훈독 완료, 다음 문제로
                            currentQuestionIndex++
                            currentPhase = QuizPhase.UnInput
                        }
                    }
                )
            }
        }
    }
}

// Un 입력 화면
@Composable
fun UnInputScreen(
    kanji: KanjiItem,
    onAnswerSubmit: (Boolean) -> Unit
) {
    var userInput by remember(kanji) { mutableStateOf(TextFieldValue("")) }
    var showFeedback by remember(kanji) { mutableStateOf(false) }
    var isCorrect by remember(kanji) { mutableStateOf(false) }
    var correctUn by remember(kanji) { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 한자가 바뀔 때마다 상태 초기화
    LaunchedEffect(kanji) {
        userInput = TextFieldValue("")
        showFeedback = false
        isCorrect = false
        correctUn = ""
    }

    // 제출 처리 함수
    val submitAnswer = {
        if (!showFeedback) {
            // Un (음) 추출 - kanjiHoonUn에서 마지막 단어
            correctUn = kanji.kanjiHoonUn.trim().split(" ").lastOrNull() ?: ""
            isCorrect = userInput.text.trim().equals(correctUn, ignoreCase = true)
            showFeedback = true

            scope.launch {
                delay(1500)
                if (showFeedback) {  // 여전히 피드백 표시 중인지 확인
                    showFeedback = false
                    userInput = TextFieldValue("")
                    onAnswerSubmit(isCorrect)
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 고정 부분 (한자 + 설명)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 한자 표시
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(3.dp, Color(0xFFE97878), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = kanji.kanjiWord,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A42)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 설명
            Text(
                text = "이 한자의 '음(音)'을 입력하세요",
                fontSize = 16.sp,
                color = Color(0xFF8B6F5C),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "예: '윗 상' → '상'",
                fontSize = 13.sp,
                color = Color(0xFFAA9988)
            )
        }

        // 하단 입력 부분
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 피드백 표시
            if (showFeedback) {
                Text(
                    text = if (isCorrect) "정답입니다!" else "오답입니다",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                )

                // 오답일 때 정답 표시
                if (!isCorrect) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "정답: ${kanji.kanjiHoonUn}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8B6F5C)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 입력 필드와 확인 버튼을 수평 배치
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 입력 필드
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("음을 입력하세요") },
                    enabled = !showFeedback,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE97878),
                        unfocusedBorderColor = Color(0xFFEDB4B4),
                        cursorColor = Color(0xFFE97878)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (userInput.text.isNotBlank() && !showFeedback) {
                                submitAnswer()
                            }
                        }
                    )
                )

                // 확인 버튼
                Button(
                    onClick = { submitAnswer },
                    enabled = userInput.text.isNotBlank() && !showFeedback,
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE97878),
                        disabledContainerColor = Color(0xFFEDB4B4)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 음독/훈독 퀴즈 다이얼로그
@Composable
fun ReadingQuizDialog(
    title: String,
    question: String,
    correctAnswer: String,
    allAnswers: List<String>,
    onAnswerSelected: (Boolean) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 오답 생성
    val wrongAnswers = remember(correctAnswer) {
        generateWrongAnswers(correctAnswer, allAnswers, 3)
    }

    // 정답과 오답을 섞어서 보기 생성
    val options = remember(correctAnswer, wrongAnswers) {
        (listOf(correctAnswer) + wrongAnswers).shuffled()
    }

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFFFDF5))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A42)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = question,
                    fontSize = 16.sp,
                    color = Color(0xFF8B6F5C)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 보기들
                options.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val isCorrectOption = option == correctAnswer
                    val backgroundColor = when {
                        showResult && isCorrectOption -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        showResult && isSelected && !isCorrectOption -> Color(0xFFF44336).copy(alpha = 0.2f)
                        isSelected -> Color(0xFFE97878).copy(alpha = 0.2f)
                        else -> Color.White
                    }

                    // 테두리 색상 결정
                    val borderColor = when {
                        showResult && isCorrectOption -> Color(0xFF4CAF50) // 정답은 초록색 테두리
                        showResult && isSelected && !isCorrectOption -> Color(0xFFF44336) // 오답은 빨간색 테두리
                        isSelected -> Color(0xFFE97878) // 선택 중일 때는 핑크색
                        else -> Color(0xFFEDB4B4) // 기본 테두리
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .border(
                                width = 2.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !showResult) {
                                selectedAnswer = option
                                showResult = true

                                scope.launch {
                                    delay(1500)
                                    onAnswerSelected(isCorrectOption)
                                    selectedAnswer = null
                                    showResult = false
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5A4A42)
                        )
                    }
                }
            }
        }
    }
}

// 읽기를 파싱 (쉼표로 구분)
fun parseReadings(reading: String): List<String> {
    if (reading.isBlank()) return emptyList()
    return reading.split("、", ",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

// 오답 생성기
fun generateWrongAnswers(
    correctAnswer: String,
    allCorrectAnswers: List<String>,
    count: Int
): List<String> {
    val wrongAnswers = mutableSetOf<String>()
    var attempts = 0
    val maxAttempts = 100

    while (wrongAnswers.size < count && attempts < maxAttempts) {
        attempts++
        val wrong = mutateJapaneseText(correctAnswer)
        if (wrong != correctAnswer && wrong !in allCorrectAnswers && wrong !in wrongAnswers) {
            wrongAnswers.add(wrong)
        }
    }

    // 충분한 오답이 생성되지 않았을 경우, 간단한 대체 오답 추가
    while (wrongAnswers.size < count) {
        wrongAnswers.add(correctAnswer + "ー")
    }

    return wrongAnswers.take(count)
}

// 히라가나/카타카나 문자 변형
fun mutateJapaneseText(text: String): String {
    if (text.isEmpty()) return text

    val chars = text.toCharArray()
    val randomIndex = Random.nextInt(chars.size)
    val charToMutate = chars[randomIndex]

    // 히라가나 또는 카타카나인 경우 변형
    val mutated = when {
        charToMutate in '\u3040'..'\u309F' -> mutateHiragana(charToMutate) // 히라가나
        charToMutate in '\u30A0'..'\u30FF' -> mutateKatakana(charToMutate) // 카타카나
        else -> charToMutate
    }

    chars[randomIndex] = mutated
    return String(chars)
}

// 히라가나 변형 (자음 또는 모음 변경)
fun mutateHiragana(char: Char): Char {
    // 히라가나 그리드 (행: 자음, 열: 모음 a/i/u/e/o)
    val hiraganaGrid = listOf(
        listOf('あ', 'い', 'う', 'え', 'お'), // ∅ (모음만)
        listOf('か', 'き', 'く', 'け', 'こ'), // k
        listOf('が', 'ぎ', 'ぐ', 'げ', 'ご'), // g
        listOf('さ', 'し', 'す', 'せ', 'そ'), // s
        listOf('ざ', 'じ', 'ず', 'ぜ', 'ぞ'), // z
        listOf('た', 'ち', 'つ', 'て', 'と'), // t
        listOf('だ', 'ぢ', 'づ', 'で', 'ど'), // d
        listOf('な', 'に', 'ぬ', 'ね', 'の'), // n
        listOf('は', 'ひ', 'ふ', 'へ', 'ほ'), // h
        listOf('ば', 'び', 'ぶ', 'べ', 'ぼ'), // b
        listOf('ぱ', 'ぴ', 'ぷ', 'ぺ', 'ぽ'), // p
        listOf('ま', 'み', 'む', 'め', 'も'), // m
        listOf('や', null, 'ゆ', null, 'よ'), // y
        listOf('ら', 'り', 'る', 'れ', 'ろ'), // r
        listOf('わ', null, null, null, 'を')  // w
    )

    // 현재 문자의 위치 찾기
    var currentRow = -1
    var currentCol = -1
    for (row in hiraganaGrid.indices) {
        for (col in hiraganaGrid[row].indices) {
            if (hiraganaGrid[row][col] == char) {
                currentRow = row
                currentCol = col
                break
            }
        }
        if (currentRow != -1) break
    }

    if (currentRow == -1) return char // 그리드에 없으면 그대로 반환

    // 50% 확률로 자음 또는 모음 변경
    val changeConsonant = Random.nextBoolean()

    return if (changeConsonant) {
        // 자음 변경: 같은 열(모음)에서 다른 행(자음) 선택
        val validRows = hiraganaGrid.indices.filter { row ->
            row != currentRow && hiraganaGrid[row][currentCol] != null
        }
        if (validRows.isEmpty()) char
        else hiraganaGrid[validRows.random()][currentCol]!!
    } else {
        // 모음 변경: 같은 행(자음)에서 다른 열(모음) 선택
        val validCols = hiraganaGrid[currentRow].indices.filter { col ->
            col != currentCol && hiraganaGrid[currentRow][col] != null
        }
        if (validCols.isEmpty()) char
        else hiraganaGrid[currentRow][validCols.random()]!!
    }
}

// 카타카나 변형
fun mutateKatakana(char: Char): Char {
    // 카타카나 그리드 (행: 자음, 열: 모음 a/i/u/e/o)
    val katakanaGrid = listOf(
        listOf('ア', 'イ', 'ウ', 'エ', 'オ'), // ∅ (모음만)
        listOf('カ', 'キ', 'ク', 'ケ', 'コ'), // k
        listOf('ガ', 'ギ', 'グ', 'ゲ', 'ゴ'), // g
        listOf('サ', 'シ', 'ス', 'セ', 'ソ'), // s
        listOf('ザ', 'ジ', 'ズ', 'ゼ', 'ゾ'), // z
        listOf('タ', 'チ', 'ツ', 'テ', 'ト'), // t
        listOf('ダ', 'ヂ', 'ヅ', 'デ', 'ド'), // d
        listOf('ナ', 'ニ', 'ヌ', 'ネ', 'ノ'), // n
        listOf('ハ', 'ヒ', 'フ', 'ヘ', 'ホ'), // h
        listOf('バ', 'ビ', 'ブ', 'ベ', 'ボ'), // b
        listOf('パ', 'ピ', 'プ', 'ペ', 'ポ'), // p
        listOf('マ', 'ミ', 'ム', 'メ', 'モ'), // m
        listOf('ヤ', null, 'ユ', null, 'ヨ'), // y
        listOf('ラ', 'リ', 'ル', 'レ', 'ロ'), // r
        listOf('ワ', null, null, null, 'ヲ')  // w
    )

    // 현재 문자의 위치 찾기
    var currentRow = -1
    var currentCol = -1
    for (row in katakanaGrid.indices) {
        for (col in katakanaGrid[row].indices) {
            if (katakanaGrid[row][col] == char) {
                currentRow = row
                currentCol = col
                break
            }
        }
        if (currentRow != -1) break
    }

    if (currentRow == -1) return char // 그리드에 없으면 그대로 반환

    // 50% 확률로 자음 또는 모음 변경
    val changeConsonant = Random.nextBoolean()

    return if (changeConsonant) {
        // 자음 변경: 같은 열(모음)에서 다른 행(자음) 선택
        val validRows = katakanaGrid.indices.filter { row ->
            row != currentRow && katakanaGrid[row][currentCol] != null
        }
        if (validRows.isEmpty()) char
        else katakanaGrid[validRows.random()][currentCol]!!
    } else {
        // 모음 변경: 같은 행(자음)에서 다른 열(모음) 선택
        val validCols = katakanaGrid[currentRow].indices.filter { col ->
            col != currentCol && katakanaGrid[currentRow][col] != null
        }
        if (validCols.isEmpty()) char
        else katakanaGrid[currentRow][validCols.random()]!!
    }
}

// 전체 점수 계산
fun calculateTotalScore(
    kanjiList: List<KanjiItem>,
    questionScores: Map<Int, QuestionScore>
): Double {
    var totalScore = 0.0

    kanjiList.forEachIndexed { index, kanji ->
        val score = questionScores[index] ?: QuestionScore()

        // Un 점수 (50%)
        val unScore = if (score.unCorrect) 0.5 else 0.0

        // 음독 점수 (25%)
        val undokScore = if (score.undokTotalCount > 0) {
            0.25 * (score.undokCorrectCount.toDouble() / score.undokTotalCount)
        } else 0.0

        // 훈독 점수 (25%)
        val hoondokScore = if (score.hoondokTotalCount > 0) {
            0.25 * (score.hoondokCorrectCount.toDouble() / score.hoondokTotalCount)
        } else 0.0

        totalScore += (unScore + undokScore + hoondokScore)
    }

    // 전체 점수를 백분율로 변환
    return (totalScore / kanjiList.size) * 100
}
