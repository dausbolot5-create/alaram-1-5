import React, { useState } from "react";
import { Sparkles, CalendarDays, Plus } from "lucide-react";
import type { Exam, ExamDraft } from "../types";
import { addExams, computeStatus, deleteExam, updateExam } from "../store";
import { findBentrok } from "../format";
import { ExamCard } from "../components/ExamCard";
import { ExamFormDialog } from "../components/ExamFormDialog";

interface DashboardScreenProps {
  exams: Exam[];
  onNavigateToUpload: () => void;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({ exams, onNavigateToUpload }) => {
  const [editingExam, setEditingExam] = useState<Exam | null>(null);
  const [showForm, setShowForm] = useState(false);

  const activeExams = exams.filter((e) => {
    const s = computeStatus(e);
    return s !== "completed" && s !== "missed";
  });

  const bentrokIds = findBentrok(activeExams);

  const handleSaveDraft = (draft: ExamDraft) => {
    if (editingExam) {
      updateExam(editingExam.id, {
        nama_mk: draft.nama_mk,
        kode_mk: draft.kode_mk,
        jenis_ujian: draft.jenis_ujian,
        kelas: draft.kelas,
        tanggal: draft.tanggal,
        jam_mulai: draft.jam_mulai,
        jam_selesai: draft.jam_selesai,
      });
    } else {
      addExams([
        {
          nama_mk: draft.nama_mk,
          kode_mk: draft.kode_mk,
          jenis_ujian: draft.jenis_ujian,
          kelas: draft.kelas,
          tanggal: draft.tanggal,
          jam_mulai: draft.jam_mulai,
          jam_selesai: draft.jam_selesai,
          source: "manual",
        },
      ]);
    }
  };

  return (
    <div className="space-y-4 pb-20">
      <div>
        <h1 className="text-2xl font-black tracking-tight text-white">ExamPing AI</h1>
        <p className="text-xs text-slate-400 mt-0.5">{activeExams.length} ujian mendatang</p>
      </div>

      {activeExams.length === 0 ? (
        <div className="rounded-2xl border border-[#333a52] bg-[#252b3e]/50 p-6 text-center space-y-3">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-500/15 text-amber-400">
            <Sparkles className="h-6 w-6" />
          </div>
          <h2 className="text-base font-bold text-white">Belum ada jadwal ujian</h2>
          <p className="text-xs text-slate-400 max-w-xs mx-auto">
            Upload screenshot jadwal dari portal kampus, biar AI yang membaca tabelnya.
          </p>
          <button
            onClick={onNavigateToUpload}
            className="rounded-xl bg-amber-500 px-5 py-2.5 text-xs font-bold text-slate-950 hover:bg-amber-400 transition"
          >
            Upload Screenshot
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {activeExams.map((e) => (
            <ExamCard
              key={e.id}
              exam={e}
              isBentrok={bentrokIds.has(e.id)}
              onEdit={(exam) => {
                setEditingExam(exam);
                setShowForm(true);
              }}
              onDelete={(id) => deleteExam(id)}
            />
          ))}

          <div className="rounded-xl bg-[#1e2436] p-3 border border-[#333a52] text-xs text-slate-400 flex items-start gap-2">
            <CalendarDays className="h-4 w-4 text-slate-300 shrink-0 mt-0.5" />
            <p>Untuk jaminan, tambahkan juga tiap ujian ke kalender HP lewat tombol "Kalender HP".</p>
          </div>
        </div>
      )}

      {/* FAB Button */}
      <button
        onClick={() => {
          setEditingExam(null);
          setShowForm(true);
        }}
        className="fixed bottom-20 right-4 z-30 flex h-14 w-14 items-center justify-center rounded-2xl bg-amber-500 text-slate-950 shadow-lg hover:bg-amber-400 active:scale-95 transition"
        title="Tambah Jadwal Manual"
      >
        <Plus className="h-7 w-7" />
      </button>

      <ExamFormDialog
        show={showForm}
        editingExam={editingExam}
        onDismiss={() => setShowForm(false)}
        onSubmit={handleSaveDraft}
      />
    </div>
  );
};
