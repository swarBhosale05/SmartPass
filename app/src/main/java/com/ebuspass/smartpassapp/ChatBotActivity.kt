package com.ebuspass.smartpassapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class ChatBotActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages: MutableList<Message> = mutableListOf()
    private lateinit var chatbotManager: ChatbotManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatBotActivity)
            adapter = chatAdapter
        }
        chatbotManager = ChatbotManager(applicationContext)
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(messageText, MessageType.USER)
                addMessage(message)
                sendMessage(messageText)
                messageEditText.text.clear()
            }
        }
    }
    private fun sendMessage(message: String) {
        chatbotManager.sendMessage(message) { response ->
            val message = Message(response!!, MessageType.BOT)
            addMessage(message)
        }
    }
    private fun addMessage(message: Message) {
        chatMessages.add(message)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        chatRecyclerView.scrollToPosition(chatMessages.size - 1)
    }
}
