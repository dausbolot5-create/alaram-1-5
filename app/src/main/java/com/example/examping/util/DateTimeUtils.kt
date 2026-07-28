package com.example.examping.util

import com.example.examping.data.model.AppSettings
import com.example.examping.data.model.ExamEntity
import com.example.examping.data.model.ExamStatus
import com.example.examping.data.model.TriggerEntity
import com.example.examping.data.model.TriggerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

object DateTimeUtils {

    fun generateId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Converts YYYY-MM-DD + HH:mm into Epoch milliseconds local time.
     */
    fun toEpochMs(tanggal: String, jam: String): Long {
        return try {
            val partsDate = tanggal.split("-").map { it.toIntOrNull() ?: 1 }
            val partsTime = jam.split(":").map { it.toIntOrNull() ?: 0 }

            val year = partsDate.getOrElse(0) { 2026 }
            val month = (partsDate.getOrElse(1) { 1 } - 1).coerceIn(0, 11)
            val day = partsDate.getOrElse(2) { 1 }.coerceIn(1, 31)

            val hour = partsTime.getOrElse(0) { 0 }.coerceIn(0, 23)
            val min = partsTime.getOrElse(1) { 0 }.coerceIn(0, 59)

            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun computeStatus(exam: ExamEntity, now: Long = System.currentTimeMillis()): ExamStatus {
        if (exam.status == ExamStatus.MISSED || exam.status == ExamStatus.COMPLETED) {
            return exam.status
        }
        val mulai = toEpochMs(exam.tanggal, exam.jamMulai)
        val selesai = toEpochMs(exam.tanggal, exam.jamSelesai)
        return when {
            now >= selesai -> ExamStatus.COMPLETED
            now >= mulai -> ExamStatus.ONGOING
            else -> ExamStatus.UPCOMING
        }
    }

    fun buildTriggers(exam: ExamEntity, settings: AppSettings): List<TriggerEntity> {
        val mulai = toEpochMs(exam.tanggal, exam.jamMulai)
        val selesai = toEpochMs(exam.tanggal, exam.jamSelesai)

        val triggerList = mutableListOf<TriggerEntity>()

        // 1. Reminder sebelum mulai
        val reminderMulaiWaktu = mulai - (settings.offsetMulai * 60_000L)
        triggerList.add(
            TriggerEntity(
                id = generateId(),
                examId = exam.id,
                tipeRaw = TriggerType.REMINDER_MULAI.name,
                waktu = reminderMulaiWaktu,
                sudahBunyi = false
            )
        )

        // 2. Alarm saat mulai
        triggerList.add(
            TriggerEntity(
                id = generateId(),
                examId = exam.id,
                tipeRaw = TriggerType.ALARM_MULAI.name,
                waktu = mulai,
                sudahBunyi = false
            )
        )

        // 3. Reminder sebelum selesai
        val reminderSelesaiWaktu = selesai - (settings.offsetSelesai * 60_000L)
        triggerList.add(
            TriggerEntity(
                id = generateId(),
                examId = exam.id,
                tipeRaw = TriggerType.REMINDER_SELESAI.name,
                waktu = reminderSelesaiWaktu,
                sudahBunyi = false
            )
        )

        return triggerList
    }

    fun formatTanggal(iso: String): String {
        return try {
            val parts = iso.split("-").map { it.toInt() }
            val cal = Calendar.getInstance()
            cal.set(parts[0], parts[1] - 1, parts[2])
            val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            sdf.format(cal.time)
        } catch (_: Exception) {
            iso
        }
    }

    fun hitungMundur(target: Long, now: Long = System.currentTimeMillis()): String {
        var diff = target - now
        if (diff <= 0) return "sedang berlangsung"
        val hari = diff / 86_400_000L
        diff %= 86_400_000L
        val jam = diff / 3_600_000L
        diff %= 3_600_000L
        val menit = diff / 60_000L

        return when {
            hari > 0 -> "$hari hari $jam jam lagi"
            jam > 0 -> "$jam jam $menit menit lagi"
            else -> "$menit menit lagi"
        }
    }

    fun findBentrok(exams: List<ExamEntity>): Set<String> {
        val bentrokIds = mutableSetOf<String>()
        for (i in exams.indices) {
            for (j in i + 1 until exams.size) {
                val a = exams[i]
                val b = exams[j]
                val aStart = toEpochMs(a.tanggal, a.jamMulai)
                val aEnd = toEpochMs(a.tanggal, a.jamSelesai)
                val bStart = toEpochMs(b.tanggal, b.jamMulai)
                val bEnd = toEpochMs(b.tanggal, b.jamSelesai)

                if (aStart < bEnd && bStart < aEnd) {
                    bentrokIds.add(a.id)
                    bentrokIds.add(b.id)
                }
            }
        }
        return bentrokIds
    }
}
