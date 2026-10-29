package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LessonFeedback
import com.example.ui.theme.CaucasusEmerald
import com.example.ui.theme.GeorgianError

@Composable
fun BottomFeedbackSheet(
    feedback: LessonFeedback?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = feedback != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (feedback == null) return@AnimatedVisibility

        val isSuccess = feedback is LessonFeedback.Correct
        val bgColor = if (isSuccess) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
        val iconColor = if (isSuccess) CaucasusEmerald else GeorgianError
        val btnColor = if (isSuccess) CaucasusEmerald else GeorgianError

        Surface(
            color = bgColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.HighlightOff,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (isSuccess) "Великолепно! • ყოჩაღ!" else "Не совсем так...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (feedback is LessonFeedback.Incorrect) {
                    Text(
                        text = "Правильный ответ:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7F1D1D)
                    )
                    Text(
                        text = feedback.correctAnswer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                val explanation = when (feedback) {
                    is LessonFeedback.Correct -> feedback.explanation
                    is LessonFeedback.Incorrect -> feedback.explanation
                }

                if (explanation.isNotEmpty()) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSuccess) Color(0xFF047857) else Color(0xFF991B1B)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("feedback_continue_button")
                ) {
                    Text(
                        text = if (isSuccess) "ПРОДОЛЖИТЬ" else "ПОНЯТНО",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
