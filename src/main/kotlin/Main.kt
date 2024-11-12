package org.example

import java.io.File

fun main() {

    val wordsFile = File("words.txt")
    val allWords = wordsFile.readLines()

    allWords.forEach { println(it) }

}