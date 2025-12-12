package com.gg.ghkanji.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * 시험 진행 상태를 저장하고 조회하는 관리자 클래스
 */
class ExamProgressManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "exam_progress"
        private const val KEY_PREFIX = "progress_grade_"
    }

    /**
     * 특정 학년의 시험 진행 상태 저장
     */
    fun saveProgress(progress: ExamProgress) {
        val key = KEY_PREFIX + progress.grade
        val json = gson.toJson(progress)
        sharedPreferences.edit()
            .putString(key, json)
            .apply()
    }

    /**
     * 특정 학년의 시험 진행 상태 조회
     */
    fun getProgress(grade: Int): ExamProgress? {
        val key = KEY_PREFIX + grade
        val json = sharedPreferences.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, ExamProgress::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 특정 학년의 진행 중인 시험이 있는지 확인
     */
    fun hasProgress(grade: Int): Boolean {
        return getProgress(grade) != null
    }

    /**
     * 특정 학년의 시험 진행 상태 삭제
     */
    fun clearProgress(grade: Int) {
        val key = KEY_PREFIX + grade
        sharedPreferences.edit()
            .remove(key)
            .apply()
    }

    /**
     * 모든 시험 진행 상태 삭제
     */
    fun clearAllProgress() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}
