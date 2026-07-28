package com.example.examping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.examping.data.model.ExamDraft
import com.example.examping.data.model.ExamEntity
import com.example.examping.data.model.JENIS_UJIAN_LIST
import com.example.examping.ui.theme.DarkBorder
import com.example.examping.ui.theme.DarkCard
import com.example.examping.ui.theme.StatusMissed
import com.example.examping.ui.theme.StatusToday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamFormFields(
    draft: ExamDraft,
    errors: Map<String, String>,
    onChange: (ExamDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedJenis by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (draft.confidence < 0.7f) {
            Text(
                text = "AI kurang yakin pada baris ini — periksa kembali sebelum menyimpan.",
                color = StatusToday,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StatusToday.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }

        // Nama MK
        OutlinedTextField(
            value = draft.namaMk,
            onValueChange = { onChange(draft.copy(namaMk = it)) },
            label = { Text("Nama Mata Kuliah *") },
            isError = errors.containsKey("namaMk"),
            supportingText = errors["namaMk"]?.let { { Text(it, color = StatusMissed) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = DarkBorder
            )
        )

        // Kode MK & Kelas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = draft.kodeMk,
                onValueChange = { onChange(draft.copy(kodeMk = it)) },
                label = { Text("Kode MK") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DarkBorder
                )
            )

            OutlinedTextField(
                value = draft.kelas,
                onValueChange = { onChange(draft.copy(kelas = it)) },
                label = { Text("Kelas") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DarkBorder
                )
            )
        }

        // Jenis Ujian
        ExposedDropdownMenuBox(
            expanded = expandedJenis,
            onExpandedChange = { expandedJenis = !expandedJenis },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = draft.jenisUjian,
                onValueChange = { onChange(draft.copy(jenisUjian = it)) },
                label = { Text("Jenis Ujian *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJenis) },
                isError = errors.containsKey("jenisUjian"),
                supportingText = errors["jenisUjian"]?.let { { Text(it, color = StatusMissed) } },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DarkBorder
                )
            )

            ExposedDropdownMenu(
                expanded = expandedJenis,
                onDismissRequest = { expandedJenis = false }
            ) {
                JENIS_UJIAN_LIST.forEach { jenis ->
                    DropdownMenuItem(
                        text = { Text(jenis) },
                        onClick = {
                            onChange(draft.copy(jenisUjian = jenis))
                            expandedJenis = false
                        }
                    )
                }
            }
        }

        // Tanggal ISO (YYYY-MM-DD)
        OutlinedTextField(
            value = draft.tanggal,
            onValueChange = { onChange(draft.copy(tanggal = it)) },
            label = { Text("Tanggal (YYYY-MM-DD) *") },
            placeholder = { Text("2026-08-15") },
            isError = errors.containsKey("tanggal"),
            supportingText = errors["tanggal"]?.let { { Text(it, color = StatusMissed) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = DarkBorder
            )
        )

        // Jam Mulai & Jam Selesai
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = draft.jamMulai,
                onValueChange = { onChange(draft.copy(jamMulai = it)) },
                label = { Text("Jam Mulai (HH:mm) *") },
                placeholder = { Text("08:00") },
                isError = errors.containsKey("jamMulai"),
                supportingText = errors["jamMulai"]?.let { { Text(it, color = StatusMissed) } },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DarkBorder
                )
            )

            OutlinedTextField(
                value = draft.jamSelesai,
                onValueChange = { onChange(draft.copy(jamSelesai = it)) },
                label = { Text("Jam Selesai (HH:mm) *") },
                placeholder = { Text("10:00") },
                isError = errors.containsKey("jamSelesai"),
                supportingText = errors["jamSelesai"]?.let { { Text(it, color = StatusMissed) } },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DarkBorder
                )
            )
        }
    }
}

fun validateDraft(d: ExamDraft): Map<String, String> {
    val errs = mutableMapOf<String, String>()
    if (d.namaMk.isBlank()) errs["namaMk"] = "Nama mata kuliah wajib diisi"
    if (d.jenisUjian.isBlank()) errs["jenisUjian"] = "Jenis ujian wajib diisi"
    if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(d.tanggal)) errs["tanggal"] = "Format tanggal YYYY-MM-DD"
    if (!Regex("^\\d{2}:\\d{2}$").matches(d.jamMulai)) errs["jamMulai"] = "Format jam HH:mm"
    if (!Regex("^\\d{2}:\\d{2}$").matches(d.jamSelesai)) errs["jamSelesai"] = "Format jam HH:mm"
    return errs
}

@Composable
fun ExamFormDialog(
    show: Boolean,
    editingExam: ExamEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (ExamDraft) -> Unit
) {
    if (!show) return

    var draft by remember(editingExam) {
        mutableStateOf(
            if (editingExam != null) {
                ExamDraft(
                    namaMk = editingExam.namaMk,
                    kodeMk = editingExam.kodeMk,
                    jenisUjian = editingExam.jenisUjian,
                    kelas = editingExam.kelas,
                    tanggal = editingExam.tanggal,
                    jamMulai = editingExam.jamMulai,
                    jamSelesai = editingExam.jamSelesai
                )
            } else {
                ExamDraft()
            }
        )
    }

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text(
                text = if (editingExam != null) "Edit Jadwal Ujian" else "Tambah Jadwal Manual",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            ExamFormFields(
                draft = draft,
                errors = errors,
                onChange = { draft = it }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val errMap = validateDraft(draft)
                    errors = errMap
                    if (errMap.isEmpty()) {
                        onSubmit(draft)
                        onDismiss()
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
