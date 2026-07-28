import { toEpoch } from "./store";
import type { Exam } from "./types";

function formatUtc(epoch: number) {
  return new Date(epoch).toISOString().replace(/[-:]/g, "").split(".")[0] + "Z";
}

function escapeIcs(s: string) {
  return s.replace(/\\/g, "\\\\").replace(/;/g, "\\;").replace(/,/g, "\\,").replace(/\n/g, "\\n");
}

export function buildIcs(exams: Exam[]): string {
  const lines = [
    "BEGIN:VCALENDAR",
    "VERSION:2.0",
    "PRODID:-//ExamPing AI//ID",
    "CALSCALE:GREGORIAN",
  ];

  for (const e of exams) {
    const start = toEpoch(e.tanggal, e.jam_mulai);
    const end = toEpoch(e.tanggal, e.jam_selesai);
    const summary = escapeIcs(`${e.jenis_ujian || "Ujian"} ${e.nama_mk}`);
    const desc = escapeIcs([e.kode_mk && `Kode: ${e.kode_mk}`, e.kelas && `Kelas: ${e.kelas}`].filter(Boolean).join(" · "));

    lines.push(
      "BEGIN:VEVENT",
      `UID:${e.id}@examping`,
      `DTSTAMP:${formatUtc(Date.now())}`,
      `DTSTART:${formatUtc(start)}`,
      `DTEND:${formatUtc(end)}`,
      `SUMMARY:${summary}`,
      `DESCRIPTION:${desc}`,
      "BEGIN:VALARM",
      "TRIGGER:-PT30M",
      "ACTION:DISPLAY",
      `DESCRIPTION:${escapeIcs(`Ujian ${e.nama_mk} 30 menit lagi`)}`,
      "END:VALARM",
      "END:VEVENT"
    );
  }

  lines.push("END:VCALENDAR");
  return lines.join("\r\n");
}

export function downloadFile(content: string, filename: string, mime: string) {
  const blob = new Blob([content], { type: mime });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
