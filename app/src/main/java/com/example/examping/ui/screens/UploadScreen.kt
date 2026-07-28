package com.example.examping.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CircularProgressIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.examping.data.model.ExamDraft
import com.example.examping.data.model.ExamSource
import com.example.examping.ui.components.ExamFormFields
import com.example.examping.ui.components.validateDraft
import com.example.examping.ui.theme.DarkBorder
import com.example.examping.ui.theme.DarkCard
import com.example.examping.ui.theme.StatusMissed
import com.example.examping.ui.viewmodel.ExamViewModel

@Composable
fun UploadScreen(
    viewModel: ExamViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ocrLoading by viewModel.ocrLoading.collectAsState()
    val ocrError by viewModel.ocrError.collectAsState()
    val parsedDrafts by viewModel.parsedDrafts.collectAsState()
    val aiQuota by viewModel.aiQuota.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var draftList by remember(parsedDrafts) { mutableStateOf(parsedDrafts ?: emptyList()) }
    var errorsMap by remember { mutableStateOf<Map<Int, Map<String, String>>>(emptyMap()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            viewModel.parseImage(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Upload Jadwal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Screenshot tabel jadwal ujian dari portal kampus",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (parsedDrafts == null) {
            // Upload picker state
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .clickable(enabled = !ocrLoading) {
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(vertical = 40.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (ocrLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "AI sedang membaca jadwal…",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Pilih atau foto screenshot",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "JPG atau PNG, maksimal 10MB",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (ocrError != null) {
                    Text(
                        text = ocrError!!,
                        color = StatusMissed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.setManualDraft() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text("Isi manual tanpa screenshot")
                }

                Text(
                    text = "Sisa pemindaian AI hari ini: ${20 - aiQuota} / 20. Data jadwal tersimpan hanya di HP ini.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Review drafts state
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (selectedImageUri != null) {
                    item {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Preview screenshot",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                item {
                    Text(
                        text = "Periksa hasil deteksi. Field bertanda bintang (*) wajib diisi.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                itemsIndexed(draftList) { index, draft ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                        color = DarkCard
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jadwal ${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (draftList.size > 1) {
                                    TextButton(onClick = {
                                        draftList = draftList.filterIndexed { i, _ -> i != index }
                                    }) {
                                        Text("Hapus baris", color = StatusMissed, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            ExamFormFields(
                                draft = draft,
                                errors = errorsMap[index] ?: emptyMap(),
                                onChange = { updated ->
                                    draftList = draftList.mapIndexed { i, d -> if (i == index) updated else d }
                                }
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { draftList = draftList + ExamDraft() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tambah baris")
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.clearOcrDrafts()
                                selectedImageUri = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                val errs = mutableMapOf<Int, Map<String, String>>()
                                draftList.forEachIndexed { i, d ->
                                    val map = validateDraft(d)
                                    if (map.isNotEmpty()) errs[i] = map
                                }
                                errorsMap = errs
                                if (errs.isEmpty()) {
                                    viewModel.addExamDrafts(
                                        draftList,
                                        if (selectedImageUri != null) ExamSource.OCR_AI else ExamSource.MANUAL
                                    )
                                    onNavigateHome()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simpan & pasang alarm")
                        }
                    }
                }
            }
        }
    }
}
