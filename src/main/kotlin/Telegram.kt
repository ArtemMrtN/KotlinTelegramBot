package org.example

fun main(args: Array<String>) {

    val botToken = args[0]

    val telegramBotService = TelegramBotService(botToken)

    var lastUpdateId = 0

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":(.+?)\"id\":(.+?),\"first_name\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(lastUpdateId)
        println(updates)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        lastUpdateId = updateId + 1

        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(2)?.value ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (text?.lowercase() == "/start" || data?.lowercase() == MENU_BUTTON) {
            telegramBotService.sendMenu(chatId)
        }
        if (data?.lowercase() == STATISTICS_TITLE) {
            val statistic = trainer.getStatistic()
            telegramBotService.sendMessage(chatId, "Выучено ${statistic.learnedWordList} из ${statistic.total} слов | ${statistic.percent}%\n")
            Thread.sleep(1000)
            telegramBotService.sendMenu(chatId)
        }
        if (data?.lowercase() == LEARN_WORDS_TITLE) {
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBotService.sendMessage(chatId, CORRECT_ANSWER)
                Thread.sleep(1000)
                checkNextQuestionAndSend(trainer, telegramBotService, chatId)
            } else {
                val correctAnswer = trainer.getCorrectAnswer()
                telegramBotService.sendMessage(chatId, "Неправильно! ${correctAnswer?.original} - это ${correctAnswer?.translate}")
                Thread.sleep(1000)
                checkNextQuestionAndSend(trainer, telegramBotService, chatId)
            }
        }
    }

}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: String
) {
    val question = trainer.getNextQuestion()

    if (question == null) {
        telegramBotService.sendMessage(chatId, "Все слова в словаре выучены")
    } else {
        telegramBotService.sendQuestion(chatId, question)
    }
}

const val URL = "https://api.telegram.org/bot"
const val CORRECT_ANSWER = "Правильно!"