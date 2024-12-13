package org.example

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    val json = Json {
        ignoreUnknownKeys = true
    }

    fun getUpdates(updateId: Long): String {

        val urlUpdates = "$URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .build()
        return getResponseValue(request)

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
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return getResponseValue(request)

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
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return getResponseValue(request)

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
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return getResponseValue(request)

    }

    private fun sendRequestWithRetries(request: HttpRequest, retries: Int = HTTP_REQUEST_RETRIES): HttpResponse<String>? {
        var attempt = 0
        while (attempt < retries) {
            try {
                println("$TEXT_TRYING ${attempt + 1}")
                return client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: IOException) {
                println("$TEXT_ERROR_TRYING ${attempt + 1}: ${e.message}")
                attempt++
                if (attempt == retries) {
                    println(TEXT_TRYING_EXHAUSTED)
                    throw e
                }
            }
        }
        return null
    }

    private fun getResponseValue(request: HttpRequest): String {
        return try {
            val response = sendRequestWithRetries(request)
            if (response != null && response.statusCode() == RESPONSE_STATUS_CODE) {
                response.body()
            } else {
                "$TEXT_ERROR_FAILED_SEND: ${response?.statusCode() ?: TEXT_UNKNOWN}"
            }
        } catch (e: IOException) {
            "$TEXT_ERROR_UNABLE_SEND. ${e.message}"
        } catch (e: Exception) {
            "$TEXT_ERROR_UNEXPECTED. ${e.message}"
        }
    }
}