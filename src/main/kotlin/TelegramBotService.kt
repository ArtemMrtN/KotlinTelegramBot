package org.example

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    val json = Json {
        ignoreUnknownKeys = true
    }

    fun getUpdates(updateId: Long): String {

        val urlUpdates = "$URL$botToken/getUpdates?offset=$updateId"

        return retry {

            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
                .build()
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        }

    }

    fun sendMessage(
        chatId: Long,
        text: String
    ): String {
        val urlUpdates = "$URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )

        val requestBodyString = json.encodeToString(requestBody)

        return retry {

            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()

        }

    }

    fun sendMenu(chatId: Long): String {

        val urlUpdates = "$URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = MENU,
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = LEARN_WORDS, callbackData = LEARN_WORDS_TITLE),
                        InlineKeyboard(text = STATISTICS, callbackData = STATISTICS_TITLE)
                    ),
                    listOf(
                        InlineKeyboard(text = RESET, callbackData = RESET_CLICKED)
                    )
                )
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        return retry {

            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()

        }

    }

    fun sendQuestion(
        chatId: Long,
        question: Question
    ): String {

        val urlUpdates = "$URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { index, word ->
                    listOf(
                        InlineKeyboard(
                            text = "${index + 1} - ${word.translate}",
                            callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                        )
                    )
                }).flatten() +
                        listOf(
                            listOf(
                                InlineKeyboard(
                                    text = ON_MENU,
                                    callbackData = MENU_BUTTON
                                )
                            )
                        )
            )
        )
        println(requestBody)

        val requestBodyString = json.encodeToString(requestBody)

        return retry {

            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()

        }
    }

    fun <T> retry(
        maxAttempts: Int = 3,
        delayMillis: Long = 2000,
        action: () -> T
    ): T {
        var currentAttempt = 0
        while (true) {
            try {
                return action()
            } catch (e: IOException) {
                currentAttempt++
                if (currentAttempt >= maxAttempts) {
                    throw e // Если превышено количество попыток, выбрасываем исключение
                }
                println("Retrying... ($currentAttempt/$maxAttempts)")
                Thread.sleep(delayMillis) // Задержка между попытками
            }
        }
    }

}