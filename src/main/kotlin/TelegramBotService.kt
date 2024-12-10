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
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .build()

        return try {
            val response = sendRequestWithRetries(request)
            if (response != null && response.statusCode() == 200) {
                response.body()
            } else {
                "Error: Failed to send message. Status: ${response?.statusCode() ?: "unknown"}, Body: ${response?.body() ?: "empty"}"
            }
        } catch (e: IOException) {
            "Error: Unable to send message due to network issues. ${e.message}"
        } catch (e: Exception) {
            "Error: Unexpected issue occurred while sending message. ${e.message}"
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
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .timeout(Duration.ofSeconds(15))
            .build()

        return try {
            val response = sendRequestWithRetries(request)
            if (response != null && response.statusCode() == 200) {
                response.body()
            } else {
                "Error: Failed to send message. Status: ${response?.statusCode() ?: "unknown"}, Body: ${response?.body() ?: "empty"}"
            }
        } catch (e: IOException) {
            "Error: Unable to send message due to network issues. ${e.message}"
        } catch (e: Exception) {
            "Error: Unexpected issue occurred while sending message. ${e.message}"
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
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .timeout(Duration.ofSeconds(15))
            .build()

        return try {
            val response = sendRequestWithRetries(request)
            if (response != null && response.statusCode() == 200) {
                response.body()
            } else {
                "Error: Failed to send message. Status: ${response?.statusCode() ?: "unknown"}, Body: ${response?.body() ?: "empty"}"
            }
        } catch (e: IOException) {
            "Error: Unable to send message due to network issues. ${e.message}"
        } catch (e: Exception) {
            "Error: Unexpected issue occurred while sending message. ${e.message}"
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
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .timeout(Duration.ofSeconds(15))
            .build()

        return try {
            val response = sendRequestWithRetries(request)
            if (response != null && response.statusCode() == 200) {
                response.body()
            } else {
                "Error: Failed to send message. Status: ${response?.statusCode() ?: "unknown"}, Body: ${response?.body() ?: "empty"}"
            }
        } catch (e: IOException) {
            "Error: Unable to send message due to network issues. ${e.message}"
        } catch (e: Exception) {
            "Error: Unexpected issue occurred while sending message. ${e.message}"
        }
    }

    private fun sendRequestWithRetries(request: HttpRequest, retries: Int = 3): HttpResponse<String>? {
        var attempt = 0
        while (attempt < retries) {
            try {
                println("Попытка ${attempt + 1}")
                return client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: IOException) {
                println("Ошибка при попытке ${attempt + 1}: ${e.message}")
                attempt++
                if (attempt == retries) {
                    println("Все попытки исчерпаны.")
                    throw e
                }
            }
        }
        return null
    }

}