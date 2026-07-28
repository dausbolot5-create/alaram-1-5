package com.example.examping.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.examping.data.model.ExamEntity
import com.example.examping.data.model.ExamSource
import com.example.examping.data.model.ExamStatus
import com.example.examping.ui.components.ExamCard
import com.example.examping.ui.components.ExamFormDialog
import com.example.examping.ui.theme.DarkBorder
import com.example.examping.ui.theme.DarkCard
import com.example.examping.ui.theme.DarkSurface
import com.example.examping.ui.viewmodel.ExamViewModel
import com.example.examping.util.DateTimeUtils

@Composable
fun DashboardScreen(
    viewModel: ExamViewModel,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exams by viewModel.exams.collectAsState()

    var editingExam by remember { mutableStateOf<ExamEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    val activeExams = remember(exams) {
        exams.filter {
            val status = DateTimeUtils.computeStatus(it)
            status != ExamStatus.COMPLETED && status != ExamStatus.MISSED
        }
    }

    val bentrokIds = remember(activeExams) {
        DateTimeUtils.findBentrok(activeExams)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Title
            Text(
                text = "ExamPing AI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${activeExams.size} ujian mendatang",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Permission Banner
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Aktifkan Notifikasi & Alarm",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tanpa izin notifikasi, pengingat dan alarm tidak bisa muncul. Ketuk untuk mengizinkan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // List of Exams or Empty State
            if (activeExams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkCard.copy(alpha = 0.5f))
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada jadwal ujian",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload screenshot jadwal dari portal kampus, biar AI yang membaca tabelnya.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToUpload) {
                            Text("Upload Screenshot")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(activeExams, key = { it.id }) { exam ->
                        ExamCard(
                            exam = exam,
                            isBentrok = bentrokIds.contains(exam.id),
                            onEdit = {
                                editingExam = it
                                showFormDialog = true
                            },
                            onDelete = {
                                viewModel.deleteExam(it.id)
                            }
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Untuk jaminan, tambahkan juga tiap ujian ke kalender HP lewat tombol 'Kalender HP'.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to add manual exam
        FloatingActionButton(
            onClick = {
                editingExam = null
                showFormDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Jadwal Manual")
        }
    }

    ExamFormDialog(
        show = showFormDialog,
        editingExam = editingExam,
        onDismiss = { showFormDialog = false },
        onSubmit = { draft ->
            if (editingExam != null) {
                val updated = editingExam!!.copy(
                    namaMk = draft.namaMk,
                    kodeMk = draft.kodeMk,
                    jenisUjian = draft.jenisUjian,
                    kelas = draft.kelas,
                    tanggal = draft.tanggal,
                    jamMulai = draft.jamMulai,
                    jamSelesai = draft.jamSelesai
                )
                viewModel.updateExam(updated)
            } else {
                viewModel.addExamDrafts(listOf(draft), ExamSource.MANUAL)
            }
        }
    )
}
