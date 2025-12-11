package com.gg.ghkanji

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gg.ghkanji.ui.theme.PretendardFontFamily

@Composable
fun MainScreen(
    onStudyClick: () -> Unit = {},
    onTestClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF5)) // 배경색
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 상단 여백
            Spacer(modifier = Modifier.weight(0.5f))

            // 제목 "우마우마"
            Text(
                text = "우마우마",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PretendardFontFamily,
                color = Color(0xFF8B6F5C),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            // 캐릭터 아이콘 (원형 배경)
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE97878)),
                contentAlignment = Alignment.Center
            ) {
                // 여기에 실제 캐릭터 이미지가 들어갈 수 있습니다
                // 현재는 이모지로 대체
                Text(
                    text = "🐮",
                    fontSize = 80.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // "한자 학습" 버튼
            Button(
                onClick = onStudyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE97878)
                ),
                shape = RoundedCornerShape(35.dp)
            ) {
                Text(
                    text = "한자 학습",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PretendardFontFamily,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "한자 시험" 버튼
            Button(
                onClick = onTestClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDB4B4)
                ),
                shape = RoundedCornerShape(35.dp)
            ) {
                Text(
                    text = "한자 시험",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PretendardFontFamily,
                    color = Color(0xFFE97878)
                )
            }

            // 하단 여백
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}
