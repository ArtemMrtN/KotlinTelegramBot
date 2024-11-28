package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Statistics(
    val learnedWordList: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(private val learnedAnswerCount: Int = LEARNED_ANSWER_COUNT, private val countOfQuestionWords: Int = COUNT_OF_QUESTION_WORDS) {

    private var question: Question? = null

    private val dictionary = loadDictionary()

    fun getStatistic(): Statistics {
        val learnedWordList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.size
        val total = dictionary.size
        val percent = learnedWordList.toDouble() / total.toDouble() * 100

        return Statistics(learnedWordList, total, percent.toInt())

    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled().take(countOfQuestionWords) + learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()

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
        try {
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
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary(dictionary: List<Word>) {

        val wordsFile = File("words.txt")

        val lines = dictionary.map { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
        wordsFile.writeText(lines.joinToString("\n"))
    }
}

const val LEARNED_ANSWER_COUNT = 3
const val COUNT_OF_QUESTION_WORDS = 4