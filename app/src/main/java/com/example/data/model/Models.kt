package com.example.data.model

data class ExampleSentence(
    val hz: String,
    val py: String,
    val es: String
)

data class Word(
    val hanzi: String,
    val pinyin: String,
    val es: String,
    val hsk: Int,
    val cat: String,
    val rad: String? = null,
    val mnemo: String? = null,
    val fr: ExampleSentence? = null
)

data class UnitInfo(
    val id: String,
    val emoji: String,
    val nombre: String,
    val desc: String,
    val cats: List<String> = emptyList(),
    val hsk: Int? = null
)

data class ToneOption(
    val hz: String,
    val py: String,
    val t: Int,
    val es: String
)

data class TonePair(
    val sil: String,
    val opts: List<ToneOption>
)

data class PoemLine(
    val hz: String,
    val py: String,
    val es: String
)

data class Poem(
    val id: String,
    val emoji: String,
    val titulo: String,
    val pinyinT: String,
    val tituloEs: String,
    val poeta: String,
    val poetaP: String,
    val dinastia: String,
    val nota: String,
    val lineas: List<PoemLine>
)

data class CulturalItem(
    val emoji: String,
    val zh: String,
    val py: String,
    val es: String,
    val texto: String
)

data class Achievement(
    val id: String,
    val emoji: String,
    val nombre: String,
    val desc: String
)

data class ShopItem(
    val id: String,
    val emoji: String,
    val nombre: String,
    val costo: Int,
    val desc: String
)

data class WordProgress(
    var ok: Int = 0,
    var fail: Int = 0,
    var last: Long = 0L,
    var box: Int = 1,
    var due: Long = 0L
)

data class GameStats(
    var best: Int = 0,
    var played: Int = 0,
    var ok: Int = 0
)

data class CategoryItem(
    val id: String,
    val emoji: String,
    val name: String,
    val desc: String
)

data class UnitProgress(
    var doneLevel: Int = 0, // 0 to 3
    var stars: Map<Int, Int> = emptyMap() // level -> stars
)
