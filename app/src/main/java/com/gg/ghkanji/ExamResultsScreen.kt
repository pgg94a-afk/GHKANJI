package com.gg.ghkanji

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gg.ghkanji.data.ExamResult
import com.gg.ghkanji.data.ExamResultManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExamResultsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val examResultManager = remember { ExamResultManager(context) }

    var selectedGrade by remember { mutableStateOf(1) }
    var results by remember { mutableStateOf<List<ExamResult>>(emptyList()) }

    // 선택된 학년의 결과 로드
    LaunchedEffect(selectedGrade) {
        results = examResultManager.getResultsByGrade(selectedGrade)
    }

    // 뒤로가기 처리
    BackHandler {
        onBackClick()
    }

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
            // 상단 바 (뒤로가기 + 제목)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF8B6F5C),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "성적 조회",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A42)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 학년 선택 탭
            ScrollableTabRow(
                selectedTabIndex = selectedGrade - 1,
                containerColor = Color.Transparent,
                contentColor = Color(0xFFE97878),
                indicator = { tabPositions ->
                    if (selectedGrade - 1 < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedGrade - 1]),
                            color = Color(0xFFE97878)
                        )
                    }
                },
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                (1..6).forEach { grade ->
                    Tab(
                        selected = selectedGrade == grade,
                        onClick = { selectedGrade = grade },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "${grade}학년",
                            fontSize = 18.sp,
                            fontWeight = if (selectedGrade == grade) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedGrade == grade) Color(0xFFE97878) else Color(0xFF8B6F5C),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 성적 리스트
            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${selectedGrade}학년 시험 기록이 없습니다",
                            fontSize = 18.sp,
                            color = Color(0xFF8B6F5C),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    results.forEach { result ->
                        ExamResultCard(result)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ExamResultCard(result: ExamResult) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREAN)
    val dateStr = dateFormat.format(Date(result.timestamp))

    // 등급에 따른 색상
    val gradeColor = when (result.letterGrade) {
        "A+", "A" -> Color(0xFF4CAF50)
        "B+", "B" -> Color(0xFF2196F3)
        "C+", "C" -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 그림자 레이어
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .offset(y = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE0E0E0))
        )

        // 메인 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(2.dp, gradeColor, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 상단: 회차 + 등급
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${result.attemptNumber}회차",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4A42)
                    )

                    // 등급 뱃지
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(gradeColor)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = result.letterGrade,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 점수 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "정답률",
                            fontSize = 14.sp,
                            color = Color(0xFF8B6F5C)
                        )
                        Text(
                            text = "${String.format("%.1f", result.percentage)}%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = gradeColor
                        )
                    }

                    Column {
                        Text(
                            text = "정답/총 문제",
                            fontSize = 14.sp,
                            color = Color(0xFF8B6F5C)
                        )
                        Text(
                            text = "${result.correctAnswers} / ${result.totalQuestions}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4A42)
                        )
                    }

                    Column {
                        Text(
                            text = "합격 여부",
                            fontSize = 14.sp,
                            color = Color(0xFF8B6F5C)
                        )
                        Text(
                            text = if (result.passed) "합격" else "불합격",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (result.passed) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 일시
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}
