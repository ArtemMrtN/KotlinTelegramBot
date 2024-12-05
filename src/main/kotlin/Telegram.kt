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
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, telegramBotService, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1

    }

}

fun handleUpdate(update: Update, json: Json, telegramBotService: TelegramBotService, trainers: HashMap<Long, LearnWordsTrainer>) {

    val text = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) {LearnWordsTrainer("$chatId")}

    if ((text?.lowercase() == "/start" || data?.lowercase() == MENU_BUTTON)) {
        telegramBotService.sendMenu(json, chatId)
    }
    if (data?.lowercase() == STATISTICS_TITLE) {
        val statistic = trainer.getStatistic()
        telegramBotService.sendMessage(
            json,
            chatId,
            "Выучено ${statistic.learnedWordList} из ${statistic.total} слов | ${statistic.percent}%\n"
        )
        Thread.sleep(1000)
        telegramBotService.sendMenu(json, chatId)
    }
    if (data?.lowercase() == LEARN_WORDS_TITLE) {
        checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
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
    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        telegramBotService.sendMessage(json, chatId, "Прогресс сброшен")
        telegramBotService.sendMenu(json, chatId)
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