package com.example.examping.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class ExamStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    MISSED;

    val label: String
        get() = when (this) {
            UPCOMING -> "Mendatang"
            ONGOING -> "Berlangsung"
            COMPLETED -> "Selesai"
            MISSED -> "Terlewat"
        }
}

enum class ExamSource {
    OCR_AI,
    MANUAL
}

enum class TriggerType {
    REMINDER_MULAI,
    ALARM_MULAI,
    REMINDER_SELESAI
}

@Serializable
@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val namaMk: String,
    val kodeMk: String = "",
    val jenisUjian: String = "",
    val kelas: String = "",
    /** ISO date: YYYY-MM-DD */
    val tanggal: String,
    /** HH:mm */
    val jamMulai: String,
    /** HH:mm */
    val jamSelesai: String,
    val statusRaw: String = ExamStatus.UPCOMING.name,
    val sourceRaw: String = ExamSource.MANUAL.name,
    val createdAt: Long = System.currentTimeMillis()
) {
    val status: ExamStatus
        get() = try { ExamStatus.valueOf(statusRaw) } catch (_: Exception) { ExamStatus.UPCOMING }

    val source: ExamSource
        get() = try { ExamSource.valueOf(sourceRaw) } catch (_: Exception) { ExamSource.MANUAL }
}

@Entity(tableName = "triggers")
data class TriggerEntity(
    @PrimaryKey val id: String,
    val examId: String,
    val tipeRaw: String,
    val waktu: Long,
    val sudahBunyi: Boolean = false
) {
    val tipe: TriggerType
        get() = try { TriggerType.valueOf(tipeRaw) } catch (_: Exception) { TriggerType.ALARM_MULAI }
}

@Serializable
data class ExamDraft(
    val namaMk: String = "",
    val kodeMk: String = "",
    val jenisUjian: String = "",
    val kelas: String = "",
    val tanggal: String = "",
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val confidence: Float = 1.0f
)

@Serializable
data class AppSettings(
    val offsetMulai: Int = 30,
    val offsetSelesai: Int = 30,
    val suaraAlarm: Boolean = true
)

val JENIS_UJIAN_LIST = listOf("UTS", "UAS", "Kuis", "Praktikum", "Responsi", "Lainnya")
