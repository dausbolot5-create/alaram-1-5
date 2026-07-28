import React from "react";
import { BellRing, Check } from "lucide-react";
import type { Exam } from "../types";

interface AlarmHostProps {
  activeAlarmExam: Exam | null;
  onDismiss: () => void;
}

export const AlarmHost: React.FC<AlarmHostProps> = ({ activeAlarmExam, onDismiss }) => {
  if (!activeAlarmExam) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/90 backdrop-blur-md p-4">
      <div className="w-full max-w-sm rounded-3xl bg-[#1e2436] border border-amber-500/40 p-6 text-center shadow-2xl animate-in fade-in zoom-in-95 duration-200">
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-amber-500/15 text-amber-400 mb-4 animate-bounce">
          <BellRing className="h-8 w-8" />
        </div>
        <h2 className="text-xl font-bold text-white mb-1">Alarm Ujian Dimulai!</h2>
        <p className="text-base font-semibold text-amber-400 mb-2">{activeAlarmExam.nama_mk}</p>
        <p className="text-xs text-slate-400 mb-6">
          Waktu: {activeAlarmExam.jam_mulai} – {activeAlarmExam.jam_selesai} · {activeAlarmExam.jenis_ujian || "Ujian"}
        </p>

        <button
          onClick={onDismiss}
          className="w-full flex items-center justify-center gap-2 rounded-xl bg-amber-500 py-3 text-sm font-bold text-slate-950 hover:bg-amber-400 active:scale-98 transition"
        >
          <Check className="h-4 w-4" /> Matikan Alarm
        </button>
      </div>
    </div>
  );
};
