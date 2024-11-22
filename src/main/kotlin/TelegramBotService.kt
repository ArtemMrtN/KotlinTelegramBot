package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(args: Array<String>) {

    private val botToken = args[0]
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {

        val urlUpdates = "$URL$botToken/getUpdates?offset=$updateId"

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()

    }

    fun sendMessage(chatId: String?, text: String): String {

        val urlUpdates = "$URL$botToken/sendMessage?chat_id=$chatId&text=$text"

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()

    }
}