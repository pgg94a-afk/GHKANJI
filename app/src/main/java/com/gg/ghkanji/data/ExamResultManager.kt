package com.gg.ghkanji.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 시험 결과를 저장하고 조회하는 관리자 클래스
 */
class ExamResultManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "exam_results"
        private const val KEY_RESULTS = "results"
    }

    /**
     * 시험 결과 저장
     */
    fun saveExamResult(result: ExamResult) {
        val results = getAllResults().toMutableList()
        results.add(result)

        val json = gson.toJson(results)
        sharedPreferences.edit()
            .putString(KEY_RESULTS, json)
            .apply()
    }

    /**
     * 모든 시험 결과 조회
     */
    fun getAllResults(): List<ExamResult> {
        val json = sharedPreferences.getString(KEY_RESULTS, null) ?: return emptyList()
        val type = object : TypeToken<List<ExamResult>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 특정 학년의 시험 결과 조회
     */
    fun getResultsByGrade(grade: Int): List<ExamResult> {
        return getAllResults()
            .filter { it.grade == grade }
            .sortedBy { it.attemptNumber }
    }

    /**
     * 특정 학년의 다음 시도 회차 번호 가져오기
     */
    fun getNextAttemptNumber(grade: Int): Int {
        val results = getResultsByGrade(grade)
        return if (results.isEmpty()) {
            1
        } else {
            (results.maxOfOrNull { it.attemptNumber } ?: 0) + 1
        }
    }

    /**
     * 특정 학년의 최고 성적 조회
     */
    fun getBestResult(grade: Int): ExamResult? {
        return getResultsByGrade(grade)
            .maxByOrNull { it.percentage }
    }

    /**
     * 특정 학년의 합격 여부 확인
     */
    fun hasPassed(grade: Int): Boolean {
        return getResultsByGrade(grade)
            .any { it.passed }
    }

    /**
     * 모든 시험 결과 삭제
     */
    fun clearAllResults() {
        sharedPreferences.edit()
            .remove(KEY_RESULTS)
            .apply()
    }

    /**
     * 특정 학년의 시험 결과 삭제
     */
    fun clearResultsByGrade(grade: Int) {
        val results = getAllResults()
            .filter { it.grade != grade }

        val json = gson.toJson(results)
        sharedPreferences.edit()
            .putString(KEY_RESULTS, json)
            .apply()
    }
}
