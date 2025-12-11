package com.gg.ghkanji.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gg.ghkanji.data.KanjiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Splash 화면의 UI 상태
 */
data class SplashUiState(
    val isLoading: Boolean = true,
    val isDataLoaded: Boolean = false,
    val error: String? = null,
    val shouldNavigateToMain: Boolean = false
)

/**
 * Splash 화면의 ViewModel
 * - 데이터 로딩 로직 담당
 * - UI 상태 관리
 * - Configuration changes에도 데이터 유지
 */
class SplashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // 데이터 로딩 시작
                _uiState.value = _uiState.value.copy(isLoading = true)

                // JSON 데이터 파싱 및 로딩
                KanjiRepository.loadKanjiData()

                // 최소 4초는 스플래시 화면 표시
                delay(4000)

                // 로딩 완료
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDataLoaded = true,
                    shouldNavigateToMain = true
                )
            } catch (e: Exception) {
                // 에러 발생 시
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "데이터 로딩 중 오류가 발생했습니다."
                )
            }
        }
    }

    /**
     * 네비게이션 완료 후 플래그 리셋
     */
    fun onNavigationComplete() {
        _uiState.value = _uiState.value.copy(shouldNavigateToMain = false)
    }
}
