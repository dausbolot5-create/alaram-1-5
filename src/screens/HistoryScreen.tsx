import React from "react";
import type { Exam } from "../types";
import { computeStatus, deleteExam } from "../store";
import { ExamCard } from "../components/ExamCard";

interface HistoryScreenProps {
  exams: Exam[];
}

export const HistoryScreen: React.FC<HistoryScreenProps> = ({ exams }) => {
  const pastExams = exams
    .filter((e) => {
      const s = computeStatus(e);
      return s === "completed" || s === "missed";
    })
    .sort((a, b) => new Date(`${b.tanggal}T${b.jam_mulai}`).getTime() - new Date(`${a.tanggal}T${a.jam_mulai}`).getTime());

  return (
    <div className="space-y-4 pb-20">
      <div>
        <h1 className="text-2xl font-black tracking-tight text-white">Riwayat Ujian</h1>
        <p className="text-xs text-slate-400 mt-0.5">{pastExams.length} ujian sudah lewat</p>
      </div>

      {pastExams.length === 0 ? (
        <div className="rounded-2xl border border-[#333a52] bg-[#252b3e]/50 p-6 text-center text-xs text-slate-400">
          Belum ada ujian yang lewat. Riwayat akan muncul otomatis setelah jadwal berakhir.
        </div>
      ) : (
        <div className="space-y-3">
          {pastExams.map((e) => (
            <ExamCard key={e.id} exam={e} onDelete={(id) => deleteExam(id)} />
          ))}
        </div>
      )}
    </div>
  );
};
