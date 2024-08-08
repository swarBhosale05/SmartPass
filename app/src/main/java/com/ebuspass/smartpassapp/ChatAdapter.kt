package com.ebuspass.smartpassapp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
class ChatAdapter(private val messages: MutableList<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == MessageType.USER.ordinal) {
            R.layout.item_message_user
        } else {
            R.layout.item_message_bot
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }
    override fun getItemCount(): Int = messages.size
    override fun getItemViewType(position: Int): Int {
        return messages[position].type.ordinal
    }
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        fun bind(message: Message) {
            messageTextView.text = message.text
            val context = itemView.context
            val backgroundColor = if (message.type == MessageType.USER) {
                context.getColor(R.color.colorUserMessageBackground)
            } else {
                context.getColor(R.color.colorBotMessageBackground)
            }
            cardView.setCardBackgroundColor(backgroundColor)
        }
    }
}
