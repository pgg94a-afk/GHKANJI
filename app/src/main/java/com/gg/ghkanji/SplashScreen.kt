package com.gg.ghkanji

import android.view.MotionEvent
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.systemBarsPadding
import com.gg.ghkanji.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit, // 애니메이션 끝난 후 실행할 함수 (네비게이션 등)
    viewModel: SplashViewModel = viewModel()
) {
    // ViewModel의 UI 상태 구독
    val uiState by viewModel.uiState.collectAsState()

    // 배경색: HTML의 배경색(#FFFDF5)과 일치시켜서 로딩 시 흰색 깜빡임 방지
    val backgroundColor = Color(0xFFFFFDF5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // 1. 기본 설정
                    settings.javaScriptEnabled = true // 애니메이션 실행을 위해 필수
                    settings.domStorageEnabled = true

                    // 뷰포트 및 스케일 설정
                    settings.loadWithOverviewMode = true

                    // 줌 완전히 비활성화
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false

                    // 2. UI 깔끔하게 (스크롤바 제거, 배경 투명)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    setBackgroundColor(0x00000000) // WebView 자체 배경 투명 처리

                    // 3. 터치 방지 (스플래시 화면이므로 스크롤/확대 막기)
                    setOnTouchListener { _, event ->
                        event.action == MotionEvent.ACTION_MOVE
                    }

                    // 4. 로컬 자산 로드
                    loadUrl("file:///android_asset/splash.html")
                }
            }
        )
    }

    // UI 상태 변화에 따라 네비게이션 처리
    LaunchedEffect(uiState.shouldNavigateToMain) {
        if (uiState.shouldNavigateToMain) {
            onSplashFinished()
            viewModel.onNavigationComplete()
        }
    }

    // TODO: 에러 처리 - 필요시 에러 메시지 표시
    // if (uiState.error != null) { ... }
}