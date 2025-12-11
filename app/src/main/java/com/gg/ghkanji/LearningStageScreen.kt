package com.gg.ghkanji

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Stage(
    val id: Int,
    val label: String,
    val startIndex: Int,
    val endIndex: Int
)

@Composable
fun LearningStageScreen(
    grade: Int = 1,
    totalKanjiCount: Int = 80,
    onStageClick: (Stage) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // 20개씩 스테이지 나누기
    val stages = createStages(grade, totalKanjiCount)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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

            Spacer(modifier = Modifier.height(16.dp))

            // 상단 헤더 (학교가방 아이콘 + "1학년")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFE8A87C)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 20.dp)
                ) {
                    Text(
                        text = "🎒",
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${grade}학년",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 스테이지 경로 (뱀 모양)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((stages.size * 180 + 100).dp)
            ) {
                // 배경 경로 그리기
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pathColor = Color(0xFFFFCDD2)
                    val pathWidth = size.width * 0.7f
                    val pathHeight = 150.dp.toPx()
                    val startX = size.width * 0.15f

                    stages.forEachIndexed { index, _ ->
                        val y = index * pathHeight + 50.dp.toPx()
                        val isLeftAlign = index % 2 == 0

                        val path = Path().apply {
                            if (isLeftAlign) {
                                // 왼쪽 정렬 경로
                                moveTo(startX, y)
                                lineTo(startX + pathWidth, y)
                            } else {
                                // 오른쪽 정렬 경로
                                moveTo(size.width - startX - pathWidth, y)
                                lineTo(size.width - startX, y)
                            }
                        }

                        // 경로 그리기 (둥근 사각형 느낌)
                        drawRoundRect(
                            color = pathColor,
                            topLeft = if (isLeftAlign)
                                Offset(startX, y - 30.dp.toPx())
                            else
                                Offset(size.width - startX - pathWidth, y - 30.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(pathWidth, 60.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(30.dp.toPx())
                        )

                        // 연결 곡선 (다음 스테이지로)
                        if (index < stages.size - 1) {
                            val nextY = (index + 1) * pathHeight + 50.dp.toPx()
                            val curveX = if (isLeftAlign) startX + pathWidth else size.width - startX - pathWidth

                            val curvePath = Path().apply {
                                moveTo(curveX, y)
                                cubicTo(
                                    curveX, y + pathHeight * 0.3f,
                                    if (isLeftAlign) size.width - startX - pathWidth else startX + pathWidth, nextY - pathHeight * 0.3f,
                                    if (isLeftAlign) size.width - startX - pathWidth else startX + pathWidth, nextY
                                )
                            }

                            drawPath(
                                path = curvePath,
                                color = pathColor,
                                style = Stroke(width = 60.dp.toPx())
                            )
                        }
                    }
                }

                // 스테이지 버튼들
                stages.forEachIndexed { index, stage ->
                    val isLeftAlign = index % 2 == 0
                    val yOffset = (index * 180).dp + 20.dp

                    Box(
                        modifier = Modifier
                            .offset(
                                x = if (isLeftAlign) 30.dp else 0.dp,
                                y = yOffset
                            )
                            .align(if (isLeftAlign) Alignment.TopStart else Alignment.TopEnd)
                            .padding(end = if (isLeftAlign) 0.dp else 30.dp)
                    ) {
                        StageButton(
                            stage = stage,
                            showCharacter = index == 0, // 첫 번째 스테이지에만 곰 표시
                            onClick = { onStageClick(stage) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StageButton(
    stage: Stage,
    showCharacter: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp)
    ) {
        // 입체감을 위한 그림자 레이어 (아래쪽)
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(y = 4.dp)
                .clip(CircleShape)
                .background(
                    color = if (showCharacter)
                        Color(0xFF9F5A5A) // 더 어두운 빨강
                    else
                        Color(0xFFCA8B5F) // 더 어두운 오렌지
                )
        )

        // 메인 버튼
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    color = if (showCharacter)
                        Color(0xFFC97474) // 연한 빨강
                    else
                        Color(0xFFE8A87C) // 연한 오렌지
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (showCharacter) {
                // 곰 캐릭터
                Text(
                    text = "🐻",
                    fontSize = 48.sp
                )
            } else {
                // 스테이지 라벨
                Text(
                    text = stage.label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A42),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 스테이지 생성 함수 (20개씩 묶기)
fun createStages(grade: Int, totalCount: Int): List<Stage> {
    val stageSize = 20
    val stages = mutableListOf<Stage>()
    var stageNumber = 1

    var currentIndex = 0
    while (currentIndex < totalCount) {
        val startIndex = currentIndex
        val endIndex = minOf(currentIndex + stageSize - 1, totalCount - 1)

        stages.add(
            Stage(
                id = stageNumber,
                label = "$grade-$stageNumber",
                startIndex = startIndex,
                endIndex = endIndex
            )
        )

        currentIndex += stageSize
        stageNumber++
    }

    return stages
}
