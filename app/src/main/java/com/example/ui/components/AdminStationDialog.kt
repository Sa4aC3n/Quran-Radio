package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.StatusError
import java.util.UUID

@Composable
fun AdminStationDialog(
    isOpen: Boolean,
    initialStation: RadioStation? = null,
    onSave: (RadioStation) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val isEditing = initialStation != null

    var name by remember(initialStation) { mutableStateOf(initialStation?.name ?: "") }
    var country by remember(initialStation) { mutableStateOf(initialStation?.country ?: "مصر") }
    var countryCode by remember(initialStation) { mutableStateOf(initialStation?.countryCode ?: "EG") }
    var city by remember(initialStation) { mutableStateOf(initialStation?.city ?: "القاهرة") }
    var streamUrl by remember(initialStation) { mutableStateOf(initialStation?.streamUrl ?: "") }
    var fallbackUrl by remember(initialStation) { mutableStateOf(initialStation?.fallbackStreamUrl ?: "") }
    var reciter by remember(initialStation) { mutableStateOf(initialStation?.reciter ?: "") }
    var description by remember(initialStation) { mutableStateOf(initialStation?.description ?: "") }
    var bitrate by remember(initialStation) { mutableStateOf(initialStation?.bitrate ?: "128 kbps") }
    var isActive by remember(initialStation) { mutableStateOf(initialStation?.isActive ?: true) }
    var isFeatured by remember(initialStation) { mutableStateOf(initialStation?.isFeatured ?: false) }

    var validationError by remember { mutableStateOf<String?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SkyBluePrimary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = SkyBluePrimary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = SkyBluePrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                contentDescription = null,
                tint = SkyBluePrimary
            )
        },
        title = {
            Text(
                text = if (isEditing) "تعديل محطة البث" else "إضافة محطة إذاعية جديدة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = StatusError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text("اسم الإذاعة *") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it; validationError = null },
                    label = { Text("رابط البث المباشر (Stream URL) *") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fallbackUrl,
                    onValueChange = { fallbackUrl = it },
                    label = { Text("رابط بديل للطوارئ (اختياري)") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("الدولة") },
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { countryCode = it },
                        label = { Text("الرمز (SA, EG)") },
                        colors = textFieldColors,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reciter,
                    onValueChange = { reciter = it },
                    label = { Text("اسم القارئ (إذا كانت مخصصة لقارئ)") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف المحطة") },
                    maxLines = 2,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المحطة تعمل حاليًا (Active)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = SkyBluePrimary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محطة مميزة في الصفحة الرئيسية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = SkyBluePrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || streamUrl.isBlank()) {
                        validationError = "يرجى ملء اسم الإذاعة ورابط البث المباشر."
                        return@Button
                    }
                    val station = initialStation?.copy(
                        name = name.trim(),
                        streamUrl = streamUrl.trim(),
                        fallbackStreamUrl = fallbackUrl.trim().ifBlank { null },
                        country = country.trim(),
                        countryCode = countryCode.trim().uppercase(),
                        city = city.trim(),
                        reciter = reciter.trim().ifBlank { null },
                        description = description.trim(),
                        bitrate = bitrate,
                        isActive = isActive,
                        isFeatured = isFeatured
                    ) ?: RadioStation(
                        id = "custom_" + UUID.randomUUID().toString().take(8),
                        name = name.trim(),
                        streamUrl = streamUrl.trim(),
                        fallbackStreamUrl = fallbackUrl.trim().ifBlank { null },
                        country = country.trim(),
                        countryCode = countryCode.trim().uppercase(),
                        city = city.trim(),
                        reciter = reciter.trim().ifBlank { null },
                        description = description.trim(),
                        bitrate = bitrate,
                        isActive = isActive,
                        isFeatured = isFeatured,
                        category = if (reciter.isNotBlank()) StationCategory.RECITER else StationCategory.OFFICIAL
                    )
                    onSave(station)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary, contentColor = PureWhite),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isEditing) "حفظ التعديلات" else "إضافة المحطة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("إلغاء")
            }
        }
    )
}
