import React from "react";
import { Clock, Calendar, AlertTriangle, Edit2, Trash2 } from "lucide-react";
import type { Exam } from "../types";
import { computeStatus } from "../store";
import { formatTanggalLong, hitungMundur } from "../format";
import { buildIcs, downloadFile } from "../ics";

interface ExamCardProps {
  exam: Exam;
  isBentrok?: boolean;
  now?: number;
  onEdit?: (exam: Exam) => void;
  onDelete?: (id: string) => void;
}

export const ExamCard: React.FC<ExamCardProps> = ({
  exam,
  isBentrok = false,
  now = Date.now(),
  onEdit,
  onDelete,
}) => {
  const status = computeStatus(exam, now);
  const startEpoch = new Date(`${exam.tanggal}T${exam.jam_mulai}`).getTime();

  const statusBadge = {
    upcoming: { label: "Akan Datang", cls: "bg-sky-500/15 text-sky-400 border-sky-500/30" },
    ongoing: { label: "Sedang Berlangsung", cls: "bg-yellow-500/15 text-yellow-400 border-yellow-500/30" },
    completed: { label: "Selesai", cls: "bg-emerald-500/15 text-emerald-400 border-emerald-500/30" },
    missed: { label: "Terlewat", cls: "bg-red-500/15 text-red-400 border-red-500/30" },
  }[status];

  return (
    <div className="rounded-2xl border border-[#333a52] bg-[#252b3e] p-4 text-slate-100 shadow-sm transition hover:border-slate-600">
      <div className="flex flex-wrap items-center gap-2 mb-2">
        <span className={`rounded-full border px-2.5 py-0.5 text-[11px] font-medium ${statusBadge.cls}`}>
          {statusBadge.label}
        </span>
        {exam.jenis_ujian && (
          <span className="rounded-full bg-[#1e2436] px-2.5 py-0.5 text-[11px] font-medium text-slate-300">
            {exam.jenis_ujian}
          </span>
        )}
        {isBentrok && (
          <span className="inline-flex items-center gap-1 rounded-full border border-red-500/40 bg-red-500/15 px-2.5 py-0.5 text-[11px] font-medium text-red-400">
            <AlertTriangle className="h-3 w-3" /> Bentrok
          </span>
        )}
      </div>

      <h3 className="text-base font-bold text-white line-clamp-2">{exam.nama_mk || "Mata Kuliah"}</h3>
      <p className="text-xs text-slate-400 mt-0.5">
        {[exam.kode_mk, exam.kelas && `Kelas ${exam.kelas}`].filter(Boolean).join(" · ") || "Tanpa kode MK"}
      </p>

      <div className="mt-3 rounded-xl bg-[#1e2436] p-3 text-xs grid grid-cols-2 gap-2">
        <div>
          <span className="text-slate-400 block text-[10px]">Tanggal</span>
          <span className="font-semibold text-slate-200">{formatTanggalLong(exam.tanggal)}</span>
        </div>
        <div>
          <span className="text-slate-400 block text-[10px]">Waktu</span>
          <span className="font-semibold text-slate-200">
            {exam.jam_mulai} – {exam.jam_selesai}
          </span>
        </div>
      </div>

      {status === "upcoming" && (
        <div className="mt-2.5 flex items-center gap-1.5 text-xs text-amber-400 font-medium">
          <Clock className="h-3.5 w-3.5" />
          <span>{hitungMundur(startEpoch, now)}</span>
        </div>
      )}

      <div className="mt-3.5 flex items-center justify-between border-t border-[#333a52] pt-3 text-xs">
        <button
          onClick={() => downloadFile(buildIcs([exam]), `${exam.nama_mk.replace(/\s+/g, "_")}.ics`, "text/calendar")}
          className="inline-flex items-center gap-1.5 rounded-lg border border-[#333a52] bg-[#1e2436] px-3 py-1.5 font-medium text-slate-200 hover:bg-slate-700 hover:text-white transition"
        >
          <Calendar className="h-3.5 w-3.5" /> Kalender HP
        </button>

        <div className="flex items-center gap-1">
          {onEdit && (
            <button
              onClick={() => onEdit(exam)}
              className="p-1.5 rounded-lg text-slate-400 hover:bg-slate-700 hover:text-white transition"
              title="Edit"
            >
              <Edit2 className="h-3.5 w-3.5" />
            </button>
          )}
          {onDelete && (
            <button
              onClick={() => onDelete(exam.id)}
              className="p-1.5 rounded-lg text-slate-400 hover:bg-red-500/20 hover:text-red-400 transition"
              title="Hapus"
            >
              <Trash2 className="h-3.5 w-3.5" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
