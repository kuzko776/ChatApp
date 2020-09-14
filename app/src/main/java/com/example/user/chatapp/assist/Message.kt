package com.example.user.chatapp.assist

data class Message (
    val sender : String ="",
    val receiver : String="",
    val message : String="",
    val status: String = "",
    val date: String = ""
)