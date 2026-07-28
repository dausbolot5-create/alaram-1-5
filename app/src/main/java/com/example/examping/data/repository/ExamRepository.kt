package com.example.examping.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.examping.alarm.AlarmScheduler
import com.example.examping.data.db.ExamDao
import com.example.examping.data.model.AppSettings
import com.example.examping.data.model.ExamDraft
import com.example.examping.data.model.ExamEntity
import com.example.examping.data.model.ExamSource
import com.example.examping.data.model.ExamStatus
import com.example.examping.data.model.TriggerEntity
import com.example.examping.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExamRepository(
    private val context: Context,
    private val examDao: ExamDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("examping_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<AppSettings> = _settings

    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()
    val allTriggers: Flow<List<TriggerEntity>> = examDao.getAllTriggers()

    private fun loadSettings(): AppSettings {
        val raw = prefs.getString("settings_json", null) ?: return AppSettings()
        return try {
            Json.decodeFromString<AppSettings>(raw)
        } catch (_: Exception) {
            AppSettings()
        }
    }

    suspend fun updateSettings(newSettings: AppSettings) {
        prefs.edit().putString("settings_json", Json.encodeToString(newSettings)).apply()
        _settings.value = newSettings
    }

    fun getAiQuotaToday(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val savedDate = prefs.getString("quota_date", "")
        return if (savedDate == today) {
            prefs.getInt("quota_count", 0)
        } else {
            0
        }
    }

    fun bumpAiQuotaToday() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentQuota = getAiQuotaToday()
        prefs.edit()
            .putString("quota_date", today)
            .putInt("quota_count", currentQuota + 1)
            .apply()
    }

    suspend fun addExamsFromDrafts(drafts: List<ExamDraft>, source: ExamSource) {
        val currentSettings = _settings.value
        val newExams = mutableListOf<ExamEntity>()
        val newTriggers = mutableListOf<TriggerEntity>()

        for (d in drafts) {
            val id = DateTimeUtils.generateId()
            val exam = ExamEntity(
                id = id,
                namaMk = d.namaMk,
                kodeMk = d.kodeMk,
                jenisUjian = d.jenisUjian,
                kelas = d.kelas,
                tanggal = d.tanggal,
                jamMulai = d.jamMulai,
                jamSelesai = d.jamSelesai,
                statusRaw = ExamStatus.UPCOMING.name,
                sourceRaw = source.name,
                createdAt = System.currentTimeMillis()
            )
            newExams.add(exam)

            val triggers = DateTimeUtils.buildTriggers(exam, currentSettings)
            newTriggers.addAll(triggers)

            // Schedule System Alarms
            for (t in triggers) {
                val label = when (t.tipe) {
                    com.example.examping.data.model.TriggerType.REMINDER_MULAI -> "Pengingat Mulai"
                    com.example.examping.data.model.TriggerType.ALARM_MULAI -> "Alarm Mulai Ujian"
                    com.example.examping.data.model.TriggerType.REMINDER_SELESAI -> "Pengingat Selesai"
                }
                AlarmScheduler.scheduleTrigger(context, t, exam.namaMk, label)
            }
        }

        examDao.insertExams(newExams)
        examDao.insertTriggers(newTriggers)
    }

    suspend fun updateExam(exam: ExamEntity) {
        examDao.updateExam(exam)
        examDao.deleteTriggersForExam(exam.id)

        val freshTriggers = DateTimeUtils.buildTriggers(exam, _settings.value)
        examDao.insertTriggers(freshTriggers)

        for (t in freshTriggers) {
            AlarmScheduler.scheduleTrigger(context, t, exam.namaMk, "Alarm Ujian")
        }
    }

    suspend fun updateExamStatus(id: String, status: ExamStatus) {
        val exam = examDao.getExamById(id) ?: return
        val updated = exam.copy(statusRaw = status.name)
        examDao.updateExam(updated)
    }

    suspend fun deleteExam(id: String) {
        val triggers = examDao.getTriggersForExam(id)
        for (t in triggers) {
            AlarmScheduler.cancelTrigger(context, t.id)
        }
        examDao.deleteTriggersForExam(id)
        examDao.deleteExamById(id)
    }

    suspend fun clearAllData() {
        examDao.deleteAllTriggers()
        examDao.deleteAllExams()
    }
}
