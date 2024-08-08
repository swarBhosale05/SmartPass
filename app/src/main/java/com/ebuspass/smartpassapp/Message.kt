package com.ebuspass.smartpassapp
data class Message(val text: String, val type: MessageType)
enum class MessageType {
    USER,
    BOT
}
