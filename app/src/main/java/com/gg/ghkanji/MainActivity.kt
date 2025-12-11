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

sealed class Screen {
    object Splash : Screen()
    object Main : Screen()
    object LearningGrade : Screen()
    data class LearningStage(val grade: Int, val totalKanji: Int) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 간단한 네비게이션 상태 관리 (실무에선 NavController 사용 권장)
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

            when (currentScreen) {
                Screen.Splash -> {
                    SplashScreen(
                        onSplashFinished = {
                            currentScreen = Screen.Main // 스플래시 종료, 메인 화면으로!
                        }
                    )
                }
                Screen.Main -> {
                    MainScreen(
                        onStudyClick = {
                            currentScreen = Screen.LearningGrade // 학년 선택 화면으로 이동
                        },
                        onTestClick = {
                            // TODO: 한자 시험 화면으로 이동
                        }
                    )
                }
                Screen.LearningGrade -> {
                    LearningGradeScreen(
                        onGradeClick = { grade, totalKanji ->
                            currentScreen = Screen.LearningStage(grade, totalKanji) // 선택한 학년의 스테이지 화면으로 이동
                        },
                        onBackClick = {
                            currentScreen = Screen.Main // 메인 화면으로 돌아가기
                        }
                    )
                }
                is Screen.LearningStage -> {
                    LearningStageScreen(
                        grade = currentScreen.grade,
                        totalKanjiCount = currentScreen.totalKanji,
                        onStageClick = { stage ->
                            // TODO: 선택한 스테이지 학습 화면으로 이동
                        },
                        onBackClick = {
                            currentScreen = Screen.LearningGrade // 학년 선택 화면으로 돌아가기
                        }
                    )
                }
            }
        }
    }
}