package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {

    val botToken = args[0]

    val telegramBotService = TelegramBotService(botToken)

    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val text = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if ((text?.lowercase() == "/start" || data?.lowercase() == MENU_BUTTON) && chatId != null) {
            telegramBotService.sendMenu(json, chatId)
        }
        if (data?.lowercase() == STATISTICS_TITLE && chatId != null) {
            val statistic = trainer.getStatistic()
            telegramBotService.sendMessage(
                json,
                chatId,
                "Выучено ${statistic.learnedWordList} из ${statistic.total} слов | ${statistic.percent}%\n"
            )
            Thread.sleep(1000)
            telegramBotService.sendMenu(json, chatId)
        }
        if (data?.lowercase() == LEARN_WORDS_TITLE && chatId != null) {
            checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && chatId != null) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBotService.sendMessage(json, chatId, CORRECT_ANSWER)
                checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
            } else {
                val correctAnswer = trainer.getCorrectAnswer()
                telegramBotService.sendMessage(
                    json,
                    chatId,
                    "Неправильно! ${correctAnswer?.original} - это ${correctAnswer?.translate}"
                )
                checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
            }
        }
    }

}

fun checkNextQuestionAndSend(
    json: Json,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long
) {
    val question = trainer.getNextQuestion()

    if (question == null) {
        telegramBotService.sendMessage(json, chatId, "Все слова в словаре выучены")
    } else {
        telegramBotService.sendQuestion(json, chatId, question)
    }
}

const val URL = "https://api.telegram.org/bot"
const val CORRECT_ANSWER = "Правильно!"