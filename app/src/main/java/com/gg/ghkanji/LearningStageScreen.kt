package com.gg.ghkanji

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable
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
    // 시스템 뒤로가기 버튼 처리
    BackHandler {
        onBackClick()
    }

    // 20개씩 스테이지 나누기
    val stages = createStages(grade, totalKanjiCount)

    // 스크롤 상태 저장 (화면 전환 시에도 유지)
    val scrollState = rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) {
        androidx.compose.foundation.ScrollState(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
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
                    .height((stages.size * 200 + 100).dp)
            ) {
                // 배경 경로 그리기
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pathColor = Color(0xFFFFE4CC)
                    val strokeWidth = 20.dp.toPx()
                    val buttonRadius = 50.dp.toPx() // 버튼 반지름 (100dp / 2)
                    val buttonMargin = 30.dp.toPx() // 버튼의 좌우 마진
                    val verticalSpacing = 200.dp.toPx() // 버튼 간 세로 간격

                    stages.forEachIndexed { index, _ ->
                        val isLeftAlign = index % 2 == 0
                        val nextIsLeftAlign = (index + 1) % 2 == 0

                        // 현재 버튼의 중심 좌표
                        val currentCenterX = if (isLeftAlign) {
                            buttonMargin + buttonRadius // 왼쪽 버튼 중심
                        } else {
                            size.width - buttonMargin - buttonRadius // 오른쪽 버튼 중심
                        }
                        val currentCenterY = index * verticalSpacing + 30.dp.toPx() + buttonRadius

                        // 연결 곡선 (다음 스테이지로)
                        if (index < stages.size - 1) {
                            // 다음 버튼의 중심 좌표
                            val nextCenterX = if (nextIsLeftAlign) {
                                buttonMargin + buttonRadius
                            } else {
                                size.width - buttonMargin - buttonRadius
                            }
                            val nextCenterY = (index + 1) * verticalSpacing + 30.dp.toPx() + buttonRadius

                            val curvePath = Path().apply {
                                // 현재 버튼의 하단 가장자리에서 시작
                                moveTo(currentCenterX, currentCenterY + buttonRadius)

                                // 부드러운 S자 곡선 생성
                                val controlPoint1Y = currentCenterY + verticalSpacing * 0.35f
                                val controlPoint2Y = nextCenterY - verticalSpacing * 0.35f

                                cubicTo(
                                    currentCenterX, controlPoint1Y,
                                    nextCenterX, controlPoint2Y,
                                    nextCenterX, nextCenterY - buttonRadius // 다음 버튼의 상단 가장자리에서 종료
                                )
                            }

                            // 경로 그림자 (입체감)
                            drawPath(
                                path = curvePath,
                                color = Color(0xFFE8C9A8),
                                style = Stroke(width = strokeWidth + 4.dp.toPx())
                            )

                            // 메인 경로
                            drawPath(
                                path = curvePath,
                                color = pathColor,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                }

                // 스테이지 버튼들
                stages.forEachIndexed { index, stage ->
                    val isLeftAlign = index % 2 == 0
                    val yOffset = (index * 200).dp + 30.dp

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
                            isLastStage = index == stages.size - 1, // 마지막 스테이지에는 학사모 표시
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
    isLastStage: Boolean = false,
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
                    color = when {
                        showCharacter -> Color(0xFF9F5A5A) // 더 어두운 빨강
                        isLastStage -> Color(0xFF7B6FA3) // 더 어두운 보라색 (졸업)
                        else -> Color(0xFFCA8B5F) // 더 어두운 오렌지
                    }
                )
        )

        // 메인 버튼
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    color = when {
                        showCharacter -> Color(0xFFC97474) // 연한 빨강
                        isLastStage -> Color(0xFF9D8FC7) // 연한 보라색 (졸업)
                        else -> Color(0xFFE8A87C) // 연한 오렌지
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            when {
                showCharacter -> {
                    // 곰 캐릭터
                    Text(
                        text = "🐻",
                        fontSize = 48.sp
                    )
                }
                isLastStage -> {
                    // 학사모 (졸업시험)
                    Text(
                        text = "🎓",
                        fontSize = 48.sp
                    )
                }
                else -> {
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

    // 마지막 2개 그룹의 합이 20~35개 사이면 절반으로 분할
    if (stages.size >= 2) {
        val lastStage = stages[stages.size - 1]
        val secondLastStage = stages[stages.size - 2]

        val lastCount = lastStage.endIndex - lastStage.startIndex + 1
        val secondLastCount = secondLastStage.endIndex - secondLastStage.startIndex + 1
        val totalLastTwo = lastCount + secondLastCount

        if (totalLastTwo in 20..35) {
            // 마지막 2개 그룹을 절반으로 재분할
            val halfCount = totalLastTwo / 2
            val newSecondLastEndIndex = secondLastStage.startIndex + halfCount - 1
            val newLastStartIndex = newSecondLastEndIndex + 1

            stages[stages.size - 2] = Stage(
                id = secondLastStage.id,
                label = "$grade-${secondLastStage.id}",
                startIndex = secondLastStage.startIndex,
                endIndex = newSecondLastEndIndex
            )

            stages[stages.size - 1] = Stage(
                id = lastStage.id,
                label = "$grade-${lastStage.id}",
                startIndex = newLastStartIndex,
                endIndex = lastStage.endIndex
            )
        }
    }

    // 졸업시험 스테이지 추가 (모든 한자를 대상으로)
    stages.add(
        Stage(
            id = stageNumber,
            label = "졸업시험",
            startIndex = 0,
            endIndex = totalCount - 1
        )
    )

    return stages
}
