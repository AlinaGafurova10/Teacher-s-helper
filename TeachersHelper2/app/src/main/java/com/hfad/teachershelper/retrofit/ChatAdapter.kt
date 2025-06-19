package com.hfad.teachershelper.retrofit

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter: RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = TextView(parent.context)
        view.setTextSize(20f)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    class ChatViewHolder(private val textView: TextView) :
        RecyclerView.ViewHolder(textView) {
        fun bind(chatMessage: ChatMessage) {
            textView.text = chatMessage.text
            textView.gravity = if (chatMessage.isUser) Gravity.END else Gravity.START
        }
    }
}