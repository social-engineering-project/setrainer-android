package com.example.setrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.setrainer.model.Question
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.IOException

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    var questions by mutableStateOf<List<Question>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentQuestionIndex by mutableStateOf(0)
    var score by mutableStateOf(0)
    var selectedAnswer by mutableStateOf<Int?>(null)
    var showingExplanation by mutableStateOf(false)
    var quizCompleted by mutableStateOf(false)

    private val gson = Gson()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                val jsonString = getApplication<Application>().assets
                    .open("questions.json")
                    .bufferedReader()
                    .use { it.readText() }

                // Парсим напрямую как массив Question
                val questionList = gson.fromJson(jsonString, Array<Question>::class.java)
                questions = questionList.toList()
                isLoading = false
            } catch (e: IOException) {
                errorMessage = "Ошибка загрузки вопросов: ${e.message}"
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Неизвестная ошибка: ${e.message}"
                isLoading = false
            }
        }
    }
    val currentQuestion: Question?
        get() = if (currentQuestionIndex < questions.size) {
            questions[currentQuestionIndex]
        } else null

    val progress: Float
        get() = if (questions.isNotEmpty()) {
            (currentQuestionIndex + 1).toFloat() / questions.size
        } else 0f

    fun selectAnswer(index: Int) {
        if (!showingExplanation) {
            selectedAnswer = index
        }
    }

    fun checkAnswer() {
        if (selectedAnswer == null || showingExplanation) return

        currentQuestion?.let { question ->
            if (selectedAnswer == question.correct) {
                score++
            }
            showingExplanation = true
        }
    }

    fun nextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            selectedAnswer = null
            showingExplanation = false
        } else {
            quizCompleted = true
        }
    }

    fun restartQuiz() {
        currentQuestionIndex = 0
        score = 0
        selectedAnswer = null
        showingExplanation = false
        quizCompleted = false
    }
}