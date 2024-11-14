package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun main() {

    val wordsFile = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line.getOrNull(2)?.toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    dictionary.forEach { println(it) }

}