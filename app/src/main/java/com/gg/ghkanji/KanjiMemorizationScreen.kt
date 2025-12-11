package com.gg.ghkanji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gg.ghkanji.data.KanjiItem
import com.gg.ghkanji.data.KanjiRepository

@Composable
fun KanjiMemorizationScreen(
    grade: Int,
    stage: Stage,
    onBackClick: () -> Unit = {}
) {
    // 해당 학년의 한자 데이터 가져오기
    val kanjiData = remember { KanjiRepository.getKanjiByGrade(grade) }

    // 스테이지에 해당하는 한자들을 랜덤으로 섞기
    val kanjiItems = remember {
        kanjiData?.kanjiList?.subList(stage.startIndex, stage.endIndex + 1)?.shuffled()
            ?: emptyList()
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
            // 상단 바 (뒤로가기 + 제목)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
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

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${grade}학년 ${stage.label} 한자 암기",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A42)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 한자 카드 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(kanjiItems) { index, item ->
                    KanjiCard(
                        kanjiItem = item,
                        cardNumber = index + 1
                    )
                }
            }
        }
    }
}

@Composable
fun KanjiCard(
    kanjiItem: KanjiItem,
    cardNumber: Int
) {
    // 가림막 상태 관리
    var isRevealed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 그림자 레이어 (입체감)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD4B5A0))
        ) {
            Spacer(modifier = Modifier.height(180.dp))
        }

        // 메인 카드
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF8E1))
                .clickable { isRevealed = !isRevealed }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: 한자
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFE4CC)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "#$cardNumber",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8B6F5C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = kanjiItem.kanjiWord,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4A42)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 오른쪽: 정보 (가림막 가능)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                ) {
                    if (isRevealed) {
                        // 정보 표시
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 훈음
                            InfoText(
                                label = "훈음",
                                value = kanjiItem.kanjiHoonUn,
                                color = Color(0xFFE8A87C)
                            )

                            // 뜻
                            InfoText(
                                label = "뜻",
                                value = kanjiItem.kanjiMean,
                                color = Color(0xFF9AC6E8)
                            )

                            // 음독 (더보기 기능)
                            InfoTextWithExpand(
                                label = "음독",
                                hiraganaValue = kanjiItem.kanjiUndokHiragana,
                                koreanValue = kanjiItem.kanjiUndok,
                                color = Color(0xFFB5A8D1)
                            )

                            // 훈독 (더보기 기능)
                            if (kanjiItem.kanjiHoondokHiragana.isNotEmpty()) {
                                InfoTextWithExpand(
                                    label = "훈독",
                                    hiraganaValue = kanjiItem.kanjiHoondokHiragana,
                                    koreanValue = kanjiItem.kanjiHoondok,
                                    color = Color(0xFFAED89E)
                                )
                            }

                            // 예시 단어
                            if (kanjiItem.exampleWord.isNotEmpty()) {
                                InfoText(
                                    label = "예시",
                                    value = kanjiItem.exampleWord,
                                    color = Color(0xFFFFB6B9)
                                )
                            }
                        }
                    } else {
                        // 가림막
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8A87C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "👆",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "터치하여 확인",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoText(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5A4A42),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InfoTextWithExpand(
    label: String,
    hiraganaValue: String,
    koreanValue: String,
    color: Color
) {
    // 쉼표, 공백, /, 、 등으로 구분하여 파싱
    val hiraganaList = hiraganaValue.split(Regex("[,\\s、/]+")).map { it.trim() }.filter { it.isNotEmpty() }
    val koreanList = koreanValue.split(Regex("[,\\s、/]+")).map { it.trim() }.filter { it.isNotEmpty() }

    val showExpand = hiraganaList.size >= 3 || koreanList.size >= 3
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (showExpand) {
                // 첫 번째만 표시
                Text(
                    text = "${hiraganaList.firstOrNull() ?: ""} (${koreanList.firstOrNull() ?: ""})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5A4A42),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(4.dp))

                // 더보기 버튼
                Text(
                    text = "(더보기)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            } else {
                // 3개 미만이면 전체 표시
                Text(
                    text = "$hiraganaValue ($koreanValue)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5A4A42),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 드롭다운 메뉴 (툴팁처럼 표시)
        if (showExpand) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "전체 $label",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    hiraganaList.zip(koreanList).forEach { (hira, kor) ->
                        Text(
                            text = "• $hira ($kor)",
                            fontSize = 12.sp,
                            color = Color(0xFF5A4A42),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
