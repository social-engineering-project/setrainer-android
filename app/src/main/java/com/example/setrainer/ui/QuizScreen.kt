package com.example.setrainer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.setrainer.viewmodel.QuizViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator


@Composable
fun QuizScreen(
    viewModel: QuizViewModel = viewModel()
) {
    when {
        viewModel.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        viewModel.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.errorMessage ?: "Ошибка",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        viewModel.quizCompleted -> {
            ResultsScreen(
                score = viewModel.score,
                totalQuestions = viewModel.questions.size,
                onRestart = { viewModel.restartQuiz() }
            )
        }
        else -> {
            QuizContent(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizContent(viewModel: QuizViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Тренажер защиты от социальной инженерии",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Прогресс
        Text(
            text = "Вопрос ${viewModel.currentQuestionIndex + 1} из ${viewModel.questions.size}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LinearProgressIndicator(
            progress = { viewModel.progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Вопрос
        viewModel.currentQuestion?.let { question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = question.question,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Варианты ответов
            question.options.forEachIndexed { index, option ->
                val isSelected = viewModel.selectedAnswer == index
                val isCorrect = index == question.correct
                val showResult = viewModel.showingExplanation

                val backgroundColor = when {
                    showResult && isCorrect -> Color(0xFF4CAF50)
                    showResult && isSelected && !isCorrect -> Color(0xFFFF5252)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    onClick = {
                        if (!viewModel.showingExplanation) {
                            viewModel.selectAnswer(index)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (!viewModel.showingExplanation) {
                                    viewModel.selectAnswer(index)
                                }
                            },
                            enabled = !viewModel.showingExplanation
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Индикатор правильности и объяснение
            if (viewModel.showingExplanation) {
                Spacer(modifier = Modifier.height(16.dp))

                val isCorrect = viewModel.selectedAnswer == question.correct
                Text(
                    text = if (isCorrect) "✓ Правильно!" else "✗ Неправильно",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFFF5252)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Объяснение:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = question.explanation,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка действия
            Button(
                onClick = {
                    if (!viewModel.showingExplanation) {
                        viewModel.checkAnswer()
                    } else {
                        viewModel.nextQuestion()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = viewModel.selectedAnswer != null || viewModel.showingExplanation
            ) {
                Text(
                    text = when {
                        !viewModel.showingExplanation -> "Проверить"
                        viewModel.currentQuestionIndex < viewModel.questions.size - 1 -> "Следующий вопрос"
                        else -> "Показать результаты"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ResultsScreen(
    score: Int,
    totalQuestions: Int,
    onRestart: () -> Unit
) {
    val percentage = (score.toFloat() / totalQuestions) * 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Результаты тестирования",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Процент
        Text(
            text = "${percentage.toInt()}%",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = getResultColor(percentage)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Правильных ответов: $score из $totalQuestions",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = getEvaluation(percentage),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                color = getResultColor(percentage)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(56.dp)
            ) {
                Text("Пройти заново", fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = { /* Выход из приложения */ },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(56.dp)
            ) {
                Text("Выход", fontSize = 16.sp)
            }
        }
    }
}

fun getResultColor(percentage: Float): Color {
    return when {
        percentage >= 80 -> Color(0xFF4CAF50)
        percentage >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5252)
    }
}

fun getEvaluation(percentage: Float): String {
    return when {
        percentage >= 80 -> "Отлично! Вы хорошо защищены от атак социальной инженерии. Продолжайте в том же духе!"
        percentage >= 60 -> "Хорошо! У вас есть базовые знания, но есть области для улучшения. Рекомендуем дополнительное обучение."
        else -> "Требуется серьезное улучшение навыков безопасности. Вы уязвимы для атак социальной инженерии."
    }
}