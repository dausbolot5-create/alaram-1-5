package com.example.examping.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.examping.data.model.ExamEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object IcsExporter {

    private fun formatUtc(epochMs: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(epochMs))
    }

    private fun escape(s: String): String {
        return s.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }

    fun buildIcs(exams: List<ExamEntity>): String {
        val lines = mutableListOf(
            "BEGIN:VCALENDAR",
            "VERSION:2.0",
            "PRODID:-//ExamPing AI//ID",
            "CALSCALE:GREGORIAN"
        )

        for (e in exams) {
            val start = DateTimeUtils.toEpochMs(e.tanggal, e.jamMulai)
            val end = DateTimeUtils.toEpochMs(e.tanggal, e.jamSelesai)

            val summary = escape("${if (e.jenisUjian.isNotBlank()) e.jenisUjian else "Ujian"} ${e.namaMk}")
            val descriptionList = listOfNotNull(
                if (e.kodeMk.isNotBlank()) "Kode: ${e.kodeMk}" else null,
                if (e.kelas.isNotBlank()) "Kelas: ${e.kelas}" else null
            )
            val description = escape(descriptionList.joinToString(" · "))

            lines.add("BEGIN:VEVENT")
            lines.add("UID:${e.id}@examping")
            lines.add("DTSTAMP:${formatUtc(System.currentTimeMillis())}")
            lines.add("DTSTART:${formatUtc(start)}")
            lines.add("DTEND:${formatUtc(end)}")
            lines.add("SUMMARY:$summary")
            lines.add("DESCRIPTION:$description")
            lines.add("BEGIN:VALARM")
            lines.add("TRIGGER:-PT30M")
            lines.add("ACTION:DISPLAY")
            lines.add("DESCRIPTION:${escape("Ujian ${e.namaMk} 30 menit lagi")}")
            lines.add("END:VALARM")
            lines.add("END:VEVENT")
        }

        lines.add("END:VCALENDAR")
        return lines.joinToString("\r\n")
    }

    fun shareIcs(context: Context, exams: List<ExamEntity>) {
        try {
            val csContent = buildIcs(exams)
            val file = File(context.cacheDir, "jadwal-ujian.ics")
            file.writeText(csContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Kalender Ujian"))
        } catch (_: Exception) {}
    }
}
