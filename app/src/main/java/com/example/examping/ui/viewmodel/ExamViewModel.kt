package com.example.examping.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.examping.data.db.AppDatabase
import com.example.examping.data.model.AppSettings
import com.example.examping.data.model.ExamDraft
import com.example.examping.data.model.ExamEntity
import com.example.examping.data.model.ExamSource
import com.example.examping.data.model.ExamStatus
import com.example.examping.data.model.TriggerEntity
import com.example.examping.data.remote.GeminiClient
import com.example.examping.data.repository.ExamRepository
import com.example.examping.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class ActiveAlarm(
    val examId: String,
    val title: String,
    val body: String
)

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExamRepository
    val exams: StateFlow<List<ExamEntity>>
    val triggers: StateFlow<List<TriggerEntity>>
    val settings: StateFlow<AppSettings>

    private val _aiQuota = MutableStateFlow(0)
    val aiQuota: StateFlow<Int> = _aiQuota.asStateFlow()

    private val _ocrLoading = MutableStateFlow(false)
    val ocrLoading: StateFlow<Boolean> = _ocrLoading.asStateFlow()

    private val _ocrError = MutableStateFlow<String?>(null)
    val ocrError: StateFlow<String?> = _ocrError.asStateFlow()

    private val _parsedDrafts = MutableStateFlow<List<ExamDraft>?>(null)
    val parsedDrafts: StateFlow<List<ExamDraft>?> = _parsedDrafts.asStateFlow()

    private val _activeAlarm = MutableStateFlow<ActiveAlarm?>(null)
    val activeAlarm: StateFlow<ActiveAlarm?> = _activeAlarm.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExamRepository(application, database.examDao())

        exams = repository.allExams.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        triggers = repository.allTriggers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        settings = repository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppSettings()
        )

        _aiQuota.value = repository.getAiQuotaToday()
        checkDueAlarms()
    }

    fun checkDueAlarms() {
        val currentExams = exams.value
        val currentTriggers = triggers.value
        val now = System.currentTimeMillis()

        for (t in currentTriggers) {
            if (!t.sudahBunyi && now >= t.waktu && (now - t.waktu) < 30 * 60 * 1000) {
                val exam = currentExams.find { it.id == t.examId }
                if (exam != null) {
                    _activeAlarm.value = ActiveAlarm(
                        examId = exam.id,
                        title = "Alarm Ujian: ${exam.namaMk}",
                        body = "Jadwal ${exam.jenisUjian} ${exam.namaMk} dimulai (${exam.jamMulai} - ${exam.jamSelesai})"
                    )
                    break
                }
            }
        }
    }

    fun dismissAlarm() {
        val current = _activeAlarm.value
        if (current != null) {
            viewModelScope.launch {
                repository.updateExamStatus(current.examId, ExamStatus.ONGOING)
                _activeAlarm.value = null
            }
        }
    }

    fun parseImage(uri: Uri) {
        val maxQuota = 20
        if (_aiQuota.value >= maxQuota) {
            _ocrError.value = "Batas 20 pemindaian AI per hari tercapai. Gunakan input manual."
            _parsedDrafts.value = listOf(ExamDraft())
            return
        }

        viewModelScope.launch {
            _ocrLoading.value = true
            _ocrError.value = null
            try {
                val contentResolver = getApplication<Application>().contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    _ocrError.value = "Gagal memuat gambar."
                    _parsedDrafts.value = listOf(ExamDraft())
                    _ocrLoading.value = false
                    return@launch
                }

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val base64String = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                repository.bumpAiQuotaToday()
                _aiQuota.value = repository.getAiQuotaToday()

                val result = GeminiClient.parseScheduleFromBase64Image(base64String)
                result.fold(
                    onSuccess = { rows ->
                        if (rows.isEmpty()) {
                            _ocrError.value = "Tidak ada baris jadwal terbaca. Isi manual di bawah."
                            _parsedDrafts.value = listOf(ExamDraft())
                        } else {
                            _parsedDrafts.value = rows
                        }
                    },
                    onFailure = { err ->
                        _ocrError.value = err.localizedMessage
                        _parsedDrafts.value = listOf(ExamDraft())
                    }
                )
            } catch (e: Exception) {
                _ocrError.value = "Kesalahan membaca file: ${e.localizedMessage}"
                _parsedDrafts.value = listOf(ExamDraft())
            } finally {
                _ocrLoading.value = false
            }
        }
    }

    fun clearOcrDrafts() {
        _parsedDrafts.value = null
        _ocrError.value = null
    }

    fun setManualDraft() {
        _parsedDrafts.value = listOf(ExamDraft())
        _ocrError.value = null
    }

    fun addExamDrafts(drafts: List<ExamDraft>, source: ExamSource) {
        viewModelScope.launch {
            repository.addExamsFromDrafts(drafts, source)
            clearOcrDrafts()
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.updateExam(exam)
        }
    }

    fun deleteExam(id: String) {
        viewModelScope.launch {
            repository.deleteExam(id)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
