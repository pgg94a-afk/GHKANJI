package com.gg.ghkanji.data


// 전체 JSON을 감싸는 클래스
data class Kanji(
    val grade: Int,
    val kanjiString: String,
    val kanjiList: List<KanjiItem>
)

data class KanjiItem(
    val kanjiWord: String,            // 한자
    val kanjiHoonUn: String,          // 훈 음 (한국식 대표 훈음)
    val kanjiMean: String,            // 한자 뜻 (일본어 뉘앙스)
    val kanjiHoondok: String,         // 훈독 (한글)
    val kanjiHoondokHiragana: String, // 훈독 (히라가나)
    val kanjiUndok: String,           // 음독 (한글)
    val kanjiUndokHiragana: String,   // 음독 (가타카나)
    val exampleWord: String           // 대표 단어 (단어 : 뜻)
)