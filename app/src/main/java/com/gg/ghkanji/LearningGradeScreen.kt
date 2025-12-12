package com.gg.ghkanji

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class GradeInfo(
    val grade: Int,
    val icon: String,
    val totalKanji: Int,
    val color: Color,
    val shadowColor: Color
)

@Composable
fun LearningGradeScreen(
    onGradeClick: (Int, Int) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    // 시스템 뒤로가기 버튼 처리
    BackHandler {
        onBackClick()
    }

    val grades = listOf(
        GradeInfo(
            grade = 1,
            icon = "🎒",
            totalKanji = 80,
            color = Color(0xFFE8A87C),
            shadowColor = Color(0xFFCA8B5F)
        ),
        GradeInfo(
            grade = 2,
            icon = "📚",
            totalKanji = 160,
            color = Color(0xFF9AC6E8),
            shadowColor = Color(0xFF7BA8CC)
        ),
        GradeInfo(
            grade = 3,
            icon = "📖",
            totalKanji = 200,
            color = Color(0xFF85C88A),
            shadowColor = Color(0xFF6AAA6F)
        ),
        GradeInfo(
            grade = 4,
            icon = "✏️",
            totalKanji = 202,
            color = Color(0xFFB19CD9),
            shadowColor = Color(0xFF9580BD)
        ),
        GradeInfo(
            grade = 5,
            icon = "📝",
            totalKanji = 193,
            color = Color(0xFFE89AAC),
            shadowColor = Color(0xFFCC7E8F)
        ),
        GradeInfo(
            grade = 6,
            icon = "🎓",
            totalKanji = 191,
            color = Color(0xFF7B9FD3),
            shadowColor = Color(0xFF6283B7)
        )
    )

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
            // 뒤로가기 버튼
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF8B6F5C),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 제목
            Text(
                text = "학년을 선택하세요",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4A42),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // 학년 선택 버튼들
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                grades.forEach { gradeInfo ->
                    GradeButton(
                        gradeInfo = gradeInfo,
                        onClick = { onGradeClick(gradeInfo.grade, gradeInfo.totalKanji) }
                    )
                }
            }
        }
    }
}

@Composable
fun GradeButton(
    gradeInfo: GradeInfo,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 그림자 레이어
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .offset(y = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(gradeInfo.shadowColor)
        )

        // 메인 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(gradeInfo.color)
                .clickable(onClick = onClick)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 아이콘과 학년 텍스트
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = gradeInfo.icon,
                        fontSize = 48.sp
                    )
                    Column {
                        Text(
                            text = "${gradeInfo.grade}학년",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "한자 ${gradeInfo.totalKanji}개",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // 화살표
                Text(
                    text = "▶",
                    fontSize = 32.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
