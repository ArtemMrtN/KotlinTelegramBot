package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun main() {

    val wordsFile = File("words.txt")

    val dictionary = loadDictionary(wordsFile)

    while (true) {
        println(
            "Меню: \n" +
                    "1 – Учить слова\n" +
                    "2 – Статистика\n" +
                    "0 – Выход\n" +
                    "Введите число 1, 2 или 0"
        )

        val answer = readln().toIntOrNull()
        when (answer) {
            1 -> learnWord(dictionary, wordsFile)
            2 -> showStatistics(dictionary)
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

fun loadDictionary(wordsFile: File): MutableList<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    val lines: List<String> = wordsFile.readLines()

    for (line in lines) {
        val line = line.split("|")
        val word =
            Word(original = line[0], translate = line[1], correctAnswersCount = line.getOrNull(2)?.toIntOrNull() ?: 0)
        dictionary.add(word)
    }
    return dictionary
}

fun showStatistics(mutableList: MutableList<Word>) {
    val totalCount = mutableList.size

    val learnedWordList = mutableList.filter { it.correctAnswersCount >= 3 }
    val learnedCount = learnedWordList.size

    val percent = learnedCount.toDouble() / totalCount.toDouble() * 100

    println("Выучено $learnedCount из $totalCount слов | ${percent.toInt()}%\n")
}

fun learnWord(dictionary: List<Word>, wordsFile: File) {

    while (true) {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }

        if (notLearnedList.isNotEmpty()) {
            val questionWords = notLearnedList.shuffled().take(4)
            val correctAnswer = questionWords.random()
            val correctAnswerId = questionWords.indexOf(correctAnswer) + 1

            println()
            println(correctAnswer.original)

            val answerOptions = questionWords.mapIndexed { index, word -> "${index + 1} - ${word.translate}" }
            println(answerOptions.joinToString("\n", "", "\n----------\n0 - меню"))
            val userAnswerInput = readln().toIntOrNull()

            if (userAnswerInput == correctAnswerId) {
                println("Правильно!")
                correctAnswer.correctAnswersCount++
                saveDictionary(dictionary, wordsFile)
            } else {
                println("Неправильно! ${correctAnswer.original} – это ${correctAnswer.translate}")
            }

        } else {
            println("Все слова в словаре выучены")
            return
        }
    }
}

fun saveDictionary(dictionary: List<Word>, file: File) {
    val lines = dictionary.map { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
    file.writeText(lines.joinToString("\n"))
}