package com.hfad.teachershelper.retrofit


data class StepOneResponse(val step1_token: String)
data class AuthResponse(val auth_token: String, val success: String)
data class MessageRequest(
    val message: String,
    val is_sync: Boolean = true
)

data class MessageResponse(
    val response: String
)
data class ChatMessage(val text: String, val isUser: Boolean)
