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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    var kanjiList by remember { mutableStateOf<List<KanjiItem>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var currentPhase by remember { mutableStateOf<QuizPhase>(QuizPhase.UnInput) }
    var questionScores by remember { mutableStateOf<MutableMap<Int, QuestionScore>>(mutableMapOf()) }
    var isLoading by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        scope.launch {
            KanjiRepository.loadKanjiData()
            val kanji = KanjiRepository.getKanjiByGrade(grade)
            kanji?.let {
                kanjiList = it.kanjiList.shuffled()
            }
            isLoading = false
        }
    }

    // 뒤로가기 처리
    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("시험 종료") },
            text = { Text("시험을 종료하시겠습니까? 현재까지의 답안은 저장되지 않습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBackClick()
                }) {
                    Text("종료")
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
                .background(Color(0xFFFFFDF5)),
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
                onExamFinished()
            }
            return
        }

        val currentKanji = kanjiList[currentQuestionIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFDF5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 상단 바 (뒤로가기 + 제목)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF8B6F5C),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "${grade}-${grade} 한자시험",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4A42)
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 진행 상황 표시
                Text(
                    text = "${currentQuestionIndex + 1}/${kanjiList.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8B6F5C),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(40.dp))

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
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 한자 표시
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(3.dp, Color(0xFFE97878), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = kanji.kanjiWord,
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4A42)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 설명
        Text(
            text = "이 한자의 '음(音)'을 입력하세요",
            fontSize = 18.sp,
            color = Color(0xFF8B6F5C),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "예: '윗 상' → '상'",
            fontSize = 14.sp,
            color = Color(0xFFAA9988)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 입력 필드
        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
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
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 제출 버튼
        Button(
            onClick = {
                // Un (음) 추출 - kanjiHoonUn에서 마지막 단어
                val correctUn = kanji.kanjiHoonUn.trim().split(" ").lastOrNull() ?: ""
                isCorrect = userInput.text.trim().equals(correctUn, ignoreCase = true)
                showFeedback = true

                scope.launch {
                    delay(1500)
                    showFeedback = false
                    userInput = TextFieldValue("")
                    onAnswerSubmit(isCorrect)
                }
            },
            enabled = userInput.text.isNotBlank() && !showFeedback,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),
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

        // 피드백 표시
        if (showFeedback) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isCorrect) "정답입니다!" else "오답입니다",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) Color(0xFFE97878) else Color(0xFFEDB4B4),
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
    val hiragana = "あいうえおかきくけこがぎぐげごさしすせそざじずぜぞたちつてとだぢづでどなにぬねのはひふへほばびぶべぼぱぴぷぺぽまみむめもやゆよらりるれろわをん"
    val index = hiragana.indexOf(char)
    if (index == -1) return char

    // 랜덤하게 다른 히라가나로 변경
    val newIndex = Random.nextInt(hiragana.length)
    return hiragana[newIndex]
}

// 카타카나 변형
fun mutateKatakana(char: Char): Char {
    val katakana = "アイウエオカキクケコガギグゲゴサシスセソザジズゼゾタチツテトダヂヅデドナニヌネノハヒフヘホバビブベボパピプペポマミムメモヤユヨラリルレロワヲン"
    val index = katakana.indexOf(char)
    if (index == -1) return char

    // 랜덤하게 다른 카타카나로 변경
    val newIndex = Random.nextInt(katakana.length)
    return katakana[newIndex]
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
