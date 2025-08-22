package com.example.setrainer.model

data class Question(
    val question: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String
)