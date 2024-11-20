package org.example

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index, word -> "${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.original + "\n" + variants + "\n----------\n0 - выйти в меню"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

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
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Все слова в словаре выучены")
                        break
                    } else {
                        println(question.asConsoleString())

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!\n")
                        } else {
                            println("Неправильно! ${question.correctAnswer.original} - это ${question.correctAnswer.translate}\n")
                        }
                    }
                }
            }
            2 -> {
                val statistics = trainer.getStatistic()
                println("Выучено ${statistics.learnedWordList} из ${statistics.total} слов | ${statistics.percent}%\n")
            }
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}