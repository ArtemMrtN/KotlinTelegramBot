package org.example

import java.io.File

data class Statistics(
    val learnedWordList: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {

    private var question: Question? = null

    private val dictionary = loadDictionary()

    fun getStatistic(): Statistics {
        val learnedWordList = dictionary.filter { it.correctAnswersCount >= 3 }.size
        val total = dictionary.size
        val percent = learnedWordList.toDouble() / total.toDouble() * 100

        return Statistics(learnedWordList, total, percent.toInt())

    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.take(4).shuffled()
        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswersId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswersId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): MutableList<Word> {
        val dictionary: MutableList<Word> = mutableListOf()
        val wordsFile = File("words.txt")
        val lines: List<String> = wordsFile.readLines()

        for (line in lines) {
            val line = line.split("|")
            val word =
                Word(
                    original = line[0],
                    translate = line[1],
                    correctAnswersCount = line.getOrNull(2)?.toIntOrNull() ?: 0
                )
            dictionary.add(word)
        }
        return dictionary
    }

    private fun saveDictionary(dictionary: List<Word>) {

        val wordsFile = File("words.txt")

        val lines = dictionary.map { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
        wordsFile.writeText(lines.joinToString("\n"))
    }
}