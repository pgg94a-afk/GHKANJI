package com.gg.ghkanji

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gg.ghkanji.data.ExamResult
import com.gg.ghkanji.data.ExamResultManager
import com.gg.ghkanji.data.KanjiItem
import com.gg.ghkanji.data.KanjiRepository
import kotlinx.coroutines.launch

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
    var userAnswers by remember { mutableStateOf<MutableMap<Int, String>>(mutableMapOf()) }
    var isLoading by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        scope.launch {
            KanjiRepository.loadKanjiData()
            val kanji = KanjiRepository.getKanjiByGrade(grade)
            kanji?.let {
                kanjiList = it.kanjiList.shuffled() // 문제 순서 랜덤화
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
        val currentKanji = kanjiList[currentQuestionIndex]
        var currentAnswer by remember(currentQuestionIndex) {
            mutableStateOf(TextFieldValue(userAnswers[currentQuestionIndex] ?: ""))
        }

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
                // 상단 바 (뒤로가기 + 진행 상황)
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
                        text = "${currentQuestionIndex + 1} / ${kanjiList.size}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4A42)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

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

                Spacer(modifier = Modifier.height(60.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 제목
                    Text(
                        text = "${grade}학년 졸업시험",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4A42)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

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
                            text = currentKanji.kanjiWord,
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4A42)
                        )
                    }

                    Spacer(modifier = Modifier.height(60.dp))

                    // 문제 설명
                    Text(
                        text = "이 한자의 훈음을 입력하세요",
                        fontSize = 18.sp,
                        color = Color(0xFF8B6F5C),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 답안 입력
                    OutlinedTextField(
                        value = currentAnswer,
                        onValueChange = {
                            currentAnswer = it
                            userAnswers[currentQuestionIndex] = it.text
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        placeholder = { Text("예: 한 일") },
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

                    Spacer(modifier = Modifier.height(40.dp))
                }

                // 하단 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 이전 버튼
                    if (currentQuestionIndex > 0) {
                        Button(
                            onClick = { currentQuestionIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEDB4B4)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Text(
                                text = "이전",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE97878)
                            )
                        }
                    }

                    // 다음/제출 버튼
                    Button(
                        onClick = {
                            if (currentQuestionIndex < kanjiList.size - 1) {
                                currentQuestionIndex++
                            } else {
                                // 시험 제출
                                scope.launch {
                                    val correctAnswers = calculateScore(kanjiList, userAnswers)
                                    val attemptNumber = examResultManager.getNextAttemptNumber(grade)

                                    val examResult = ExamResult.create(
                                        grade = grade,
                                        attemptNumber = attemptNumber,
                                        totalQuestions = kanjiList.size,
                                        correctAnswers = correctAnswers
                                    )

                                    examResultManager.saveExamResult(examResult)
                                    onExamFinished()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(if (currentQuestionIndex > 0) 1f else 1f)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE97878)
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Text(
                            text = if (currentQuestionIndex < kanjiList.size - 1) "다음" else "제출",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 점수 계산 함수
 */
private fun calculateScore(
    kanjiList: List<KanjiItem>,
    userAnswers: Map<Int, String>
): Int {
    var correctCount = 0
    kanjiList.forEachIndexed { index, kanji ->
        val userAnswer = userAnswers[index]?.trim() ?: ""
        // 정답: kanjiHoonUn (예: "한 일")
        // 공백 제거 후 비교
        val correctAnswer = kanji.kanjiHoonUn.replace(" ", "")
        val normalizedUserAnswer = userAnswer.replace(" ", "")

        if (normalizedUserAnswer.equals(correctAnswer, ignoreCase = true)) {
            correctCount++
        }
    }
    return correctCount
}
