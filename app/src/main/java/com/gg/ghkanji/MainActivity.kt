package com.gg.ghkanji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 간단한 네비게이션 상태 관리 (실무에선 NavController 사용 권장)
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen(
                    onSplashFinished = {
                        showSplash = false // 스플래시 종료, 메인 화면으로!
                    }
                )
            } else {
                // 메인 화면
                MainScreen(
                    onStudyClick = {
                        // TODO: 한자 학습 화면으로 이동
                    },
                    onTestClick = {
                        // TODO: 한자 시험 화면으로 이동
                    }
                )
            }
        }
    }
}