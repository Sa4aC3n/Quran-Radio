package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.SkyBluePrimary

@Composable
fun SleepTimerDialog(
    isOpen: Boolean,
    isTimerActive: Boolean,
    remainingSeconds: Long,
    onSelectMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val options = listOf(
        15 to "15 دقيقة",
        30 to "30 دقيقة",
        45 to "45 دقيقة",
        60 to "ساعة واحدة (60 دقيقة)",
        90 to "ساعة ونصف (90 دقيقة)",
        120 to "ساعتان (120 دقيقة)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.AvTimer,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "مؤقت النوم الإيقافي",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isTimerActive && remainingSeconds > 0) {
                    val minutesLeft = remainingSeconds / 60
                    val secondsLeft = remainingSeconds % 60
                    Text(
                        text = "المؤقت نشط حاليًا: يتبقى %02d:%02d".format(minutesLeft, secondsLeft),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    Text(
                        text = "حدد مدة الاستماع لإيقاف تشغيل البث تلقائيًا عند انتهاء الوقت:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                options.forEach { (minutes, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectMinutes(minutes)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = false,
                            onClick = {
                                onSelectMinutes(minutes)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SkyBluePrimary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isTimerActive) {
                TextButton(
                    onClick = {
                        onCancelTimer()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = LiveCoral)
                ) {
                    Text("إلغاء المؤقت", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("إغلاق")
            }
        }
    )
}
