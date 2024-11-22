package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService(args)

    var updateId = 0

    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":(.+?)\"id\":(.+?),\"first_name\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val startUpdateId = updates.lastIndexOf("update_id")
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")
        if (startUpdateId == -1 || endUpdateId == -1) continue
        val updateIdString = updates.substring(startUpdateId + 11, endUpdateId)

        updateId = updateIdString.toInt() + 1

        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)

        val matchResultChatId: MatchResult? = chatIdRegex.find(updates)
        val groupsChatId = matchResultChatId?.groups
        val chatId = groupsChatId?.get(2)?.value

        val message: String = telegramBotService.sendMessage(chatId, text.toString())
        println(message)
    }

}

const val URL = "https://api.telegram.org/bot"