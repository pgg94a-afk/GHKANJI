package com.gg.ghkanji.data

import com.gg.ghkanji.QuestionScore
import com.google.gson.annotations.SerializedName

/**
 * 시험 진행 상태를 저장하는 데이터 클래스
 */
data class ExamProgress(
    @SerializedName("grade")
    val grade: Int,

    @SerializedName("kanjiIndices")
    val kanjiIndices: List<Int>,          // 섞인 한자의 원본 인덱스들

    @SerializedName("currentQuestionIndex")
    val currentQuestionIndex: Int,         // 현재 문제 번호

    @SerializedName("currentPhaseType")
    val currentPhaseType: String,          // "UnInput", "UndokQuiz", "HoondokQuiz"

    @SerializedName("currentPhaseIndex")
    val currentPhaseIndex: Int,            // 음독/훈독 퀴즈의 현재 인덱스

    @SerializedName("currentPhaseTotalCount")
    val currentPhaseTotalCount: Int,       // 음독/훈독 퀴즈의 총 개수

    @SerializedName("questionScores")
    val questionScores: Map<String, QuestionScore>,  // Int를 String으로 변환해서 저장

    @SerializedName("timestamp")
    val timestamp: Long                    // 저장된 시각 (밀리초)
) {
    companion object {
        const val PHASE_UN_INPUT = "UnInput"
        const val PHASE_UNDOK_QUIZ = "UndokQuiz"
        const val PHASE_HOONDOK_QUIZ = "HoondokQuiz"
    }

    /**
     * questionScores를 Int 키로 변환
     */
    fun getQuestionScoresAsIntMap(): Map<Int, QuestionScore> {
        return questionScores.mapKeys { it.key.toIntOrNull() ?: 0 }
    }
}
