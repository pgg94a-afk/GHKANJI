package com.gg.ghkanji.data

import com.google.gson.Gson

fun main() {


    val gson = Gson()
    val jsonDataArray = listOf(
        KanjiData.Kanji1GradeJS,
        KanjiData.Kanji2GradeJS,
        KanjiData.Kanji3GradeJS,
        KanjiData.Kanji4GradeJS,
        KanjiData.Kanji5GradeJS,
        KanjiData.Kanji6GradeJS,
    )

    jsonDataArray.forEachIndexed { idx, data ->
        val data = gson.fromJson(data, Kanji::class.java)
        println("${data.grade}학년 데이터 수: ${data.kanjiList.size}")

        val dicData = KanjiData.grade1
        val dic2Data = KanjiData.grade2
        val dic3Data = KanjiData.grade3
        val dic4Data = KanjiData.grade4
        val dic5Data = KanjiData.grade5
        val dic6Data = KanjiData.grade6

        var d: List<String> = listOf()
        if (idx == 0) d = dicData.split(" ")
        else if (idx == 1) d = dic2Data.split(" ")
        else if (idx == 2) d = dic3Data.split(" ")
        else if (idx == 3) d = dic4Data.split(" ")
        else if (idx == 4) d = dic5Data.split(" ")
        else if (idx == 5) d = dic6Data.split(" ")

        var kd = mutableListOf<String>()
        data.kanjiList.forEach {
            kd.add(it.kanjiWord)
        }
        d.forEach { it ->
            if (it !in kd) {
                println("$it 이 존재하지 않습니다. 추가해주세요.")
            }
        }

        kd.forEach { it ->
            if (it !in d) {
                println("$it 이 존재하지 않습니다. 삭제해주세요.")
            }
        }
    }

}