package com.gg.ghkanji.data

import com.google.gson.annotations.SerializedName

/**
 * 시험 결과 데이터 클래스
 */
data class ExamResult(
    @SerializedName("grade")
    val grade: Int,                    // 학년 (1~6)

    @SerializedName("attemptNumber")
    val attemptNumber: Int,             // 시도 회차 (1, 2, 3, ...)

    @SerializedName("totalQuestions")
    val totalQuestions: Int,            // 총 문제 수

    @SerializedName("correctAnswers")
    val correctAnswers: Int,            // 맞은 개수

    @SerializedName("percentage")
    val percentage: Double,             // 정답률 (0.0 ~ 100.0)

    @SerializedName("letterGrade")
    val letterGrade: String,            // 등급 (A+, A, B+, B, C+, C, F)

    @SerializedName("passed")
    val passed: Boolean,                // 합격 여부 (70% 이상)

    @SerializedName("timestamp")
    val timestamp: Long                 // 시험 본 시각 (밀리초)
) {
    companion object {
        /**
         * 정답률에 따라 등급 계산
         */
        fun calculateGrade(percentage: Double): String {
            return when {
                percentage >= 100.0 -> "A+"
                percentage >= 90.0 -> "A"
                percentage >= 85.0 -> "B+"
                percentage >= 80.0 -> "B"
                percentage >= 75.0 -> "C+"
                percentage >= 70.0 -> "C"
                else -> "F"
            }
        }

        /**
         * 합격 여부 확인
         */
        fun isPassed(percentage: Double): Boolean {
            return percentage >= 70.0
        }

        /**
         * 시험 결과 생성 헬퍼 함수
         */
        fun create(
            grade: Int,
            attemptNumber: Int,
            totalQuestions: Int,
            correctAnswers: Int
        ): ExamResult {
            val percentage = (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100.0
            val letterGrade = calculateGrade(percentage)
            val passed = isPassed(percentage)

            return ExamResult(
                grade = grade,
                attemptNumber = attemptNumber,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                percentage = percentage,
                letterGrade = letterGrade,
                passed = passed,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
