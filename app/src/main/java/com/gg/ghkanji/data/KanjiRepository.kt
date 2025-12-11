package com.gg.ghkanji.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object KanjiRepository {
    private var kanjiDataList: List<Kanji>? = null

    suspend fun loadKanjiData(): List<Kanji> = withContext(Dispatchers.IO) {
        if (kanjiDataList != null) {
            return@withContext kanjiDataList!!
        }

        val gson = Gson()
        val jsonDataArray = listOf(
            KanjiData.Kanji1GradeJS,
            KanjiData.Kanji2GradeJS,
            KanjiData.Kanji3GradeJS,
            KanjiData.Kanji4GradeJS,
            KanjiData.Kanji5GradeJS,
            KanjiData.Kanji6GradeJS,
        )

        kanjiDataList = jsonDataArray.map { jsonData ->
            gson.fromJson(jsonData, Kanji::class.java)
        }

        kanjiDataList!!
    }

    fun getKanjiByGrade(grade: Int): Kanji? {
        return kanjiDataList?.find { it.grade == grade }
    }

    fun getAllKanji(): List<Kanji>? {
        return kanjiDataList
    }

    fun isDataLoaded(): Boolean {
        return kanjiDataList != null
    }
}
