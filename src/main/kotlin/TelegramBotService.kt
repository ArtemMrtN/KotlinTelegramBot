package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {

        val urlUpdates = "$URL$botToken/getUpdates?offset=$updateId"

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()

    }

    fun sendMessage(chatId: String, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)

        val urlUpdates = "$URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()

    }

    fun sendMenu(chatId: String): String {

        val urlUpdates = "$URL$botToken/sendMessage"

        val sendMenuBody = """
        {
            "chat_id": $chatId,
            "text": "Основное меню",
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {
                            "text": "Изучить слова",
                            "callback_data": "$LEARN_WORDS_TITLE"
                        },
                        {
                            "text": "Статистика",
                            "callback_data": "$STATISTICS_TITLE"
                        }
                    ]
                ]
            }
        }
    """.trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()

    }

    fun sendQuestion(chatId: String, question: Question): String {

        val urlUpdates = "$URL$botToken/sendMessage"

        val text = question.correctAnswer.original
        val variants = question.variants
            .mapIndexed { index, word ->
                """[{"text": "${index + 1} - ${word.translate}", "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX$index"}]"""
            }.joinToString(",")
        val sendQuestionBody = """
        {
            "chat_id": $chatId,
            "text": "$text",
            "reply_markup": {
                "inline_keyboard": [
                    
                    $variants,
                    
                    [
                        {
                            "text": "В меню",
                            "callback_data": "$MENU_BUTTON"
                        }
                    ]                    
                ]
            }
        }
        """.trimIndent()

        println("Тело запроса: $sendQuestionBody")

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Ответ от Telegram API: ${response.body()}")
        return response.body()
    }


}

const val LEARN_WORDS_TITLE = "learn_words_clicked"
const val STATISTICS_TITLE = "statistics_clicked"
const val MENU_BUTTON = "menu_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"