import React, { useState } from "react";
import { X } from "lucide-react";
import { JENIS_UJIAN, type Exam, type ExamDraft } from "../types";

interface ExamFormDialogProps {
  show: boolean;
  editingExam?: Exam | null;
  onDismiss: () => void;
  onSubmit: (draft: ExamDraft) => void;
}

export const ExamFormDialog: React.FC<ExamFormDialogProps> = ({
  show,
  editingExam,
  onDismiss,
  onSubmit,
}) => {
  if (!show) return null;

  const [draft, setDraft] = useState<ExamDraft>({
    nama_mk: editingExam?.nama_mk || "",
    kode_mk: editingExam?.kode_mk || "",
    jenis_ujian: editingExam?.jenis_ujian || "UTS",
    kelas: editingExam?.kelas || "",
    tanggal: editingExam?.tanggal || new Date().toISOString().slice(0, 10),
    jam_mulai: editingExam?.jam_mulai || "08:00",
    jam_selesai: editingExam?.jam_selesai || "10:00",
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const validate = () => {
    const errs: Record<string, string> = {};
    if (!draft.nama_mk.trim()) errs.nama_mk = "Nama mata kuliah wajib diisi";
    if (!draft.jenis_ujian.trim()) errs.jenis_ujian = "Jenis ujian wajib diisi";
    if (!/^\d{4}-\d{2}-\d{2}$/.test(draft.tanggal)) errs.tanggal = "Format YYYY-MM-DD";
    if (!/^\d{2}:\d{2}$/.test(draft.jam_mulai)) errs.jam_mulai = "Format HH:mm";
    if (!/^\d{2}:\d{2}$/.test(draft.jam_selesai)) errs.jam_selesai = "Format HH:mm";
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validate()) {
      onSubmit(draft);
      onDismiss();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 p-4">
      <div className="w-full max-w-md rounded-2xl bg-[#252b3e] border border-[#333a52] p-5 text-slate-100 shadow-xl">
        <div className="flex items-center justify-between border-b border-[#333a52] pb-3 mb-4">
          <h3 className="text-lg font-bold">{editingExam ? "Edit Jadwal Ujian" : "Tambah Jadwal Manual"}</h3>
          <button onClick={onDismiss} className="rounded-lg p-1 text-slate-400 hover:bg-slate-700">
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          <div>
            <label className="block text-slate-300 font-medium mb-1">Nama Mata Kuliah *</label>
            <input
              type="text"
              value={draft.nama_mk}
              onChange={(e) => setDraft({ ...draft, nama_mk: e.target.value })}
              className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
              placeholder="Algoritma & Pemrograman"
            />
            {errors.nama_mk && <p className="text-red-400 mt-1">{errors.nama_mk}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-300 font-medium mb-1">Kode MK</label>
              <input
                type="text"
                value={draft.kode_mk}
                onChange={(e) => setDraft({ ...draft, kode_mk: e.target.value })}
                className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
                placeholder="IF102"
              />
            </div>
            <div>
              <label className="block text-slate-300 font-medium mb-1">Kelas</label>
              <input
                type="text"
                value={draft.kelas}
                onChange={(e) => setDraft({ ...draft, kelas: e.target.value })}
                className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
                placeholder="A1"
              />
            </div>
          </div>

          <div>
            <label className="block text-slate-300 font-medium mb-1">Jenis Ujian *</label>
            <select
              value={draft.jenis_ujian}
              onChange={(e) => setDraft({ ...draft, jenis_ujian: e.target.value })}
              className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
            >
              {JENIS_UJIAN.map((j) => (
                <option key={j} value={j}>
                  {j}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-slate-300 font-medium mb-1">Tanggal (YYYY-MM-DD) *</label>
            <input
              type="date"
              value={draft.tanggal}
              onChange={(e) => setDraft({ ...draft, tanggal: e.target.value })}
              className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
            />
            {errors.tanggal && <p className="text-red-400 mt-1">{errors.tanggal}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-300 font-medium mb-1">Jam Mulai (HH:mm) *</label>
              <input
                type="text"
                inputMode="numeric"
                pattern="\d{2}:\d{2}"
                maxLength={5}
                value={draft.jam_mulai}
                onChange={(e) => setDraft({ ...draft, jam_mulai: e.target.value })}
                className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
                placeholder="08:00"
              />
              {errors.jam_mulai && <p className="text-red-400 mt-1">{errors.jam_mulai}</p>}
            </div>
            <div>
              <label className="block text-slate-300 font-medium mb-1">Jam Selesai (HH:mm) *</label>
              <input
                type="text"
                inputMode="numeric"
                pattern="\d{2}:\d{2}"
                maxLength={5}
                value={draft.jam_selesai}
                onChange={(e) => setDraft({ ...draft, jam_selesai: e.target.value })}
                className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
                placeholder="10:00"
              />
              {errors.jam_selesai && <p className="text-red-400 mt-1">{errors.jam_selesai}</p>}
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-[#333a52]">
            <button
              type="button"
              onClick={onDismiss}
              className="rounded-xl px-4 py-2 font-medium text-slate-300 hover:bg-slate-700"
            >
              Batal
            </button>
            <button
              type="submit"
              className="rounded-xl bg-amber-500 px-5 py-2 font-bold text-slate-950 hover:bg-amber-400"
            >
              Simpan
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
