import React, { useState } from "react";
import { ImagePlus, Loader2, Edit3, AlertCircle, Plus, Trash2 } from "lucide-react";
import type { ExamDraft } from "../types";
import { addExams, bumpAiQuota, getAiQuota } from "../store";
import { parseScheduleFromImage } from "../ai";
import { ExamFormDialog } from "../components/ExamFormDialog";

interface UploadScreenProps {
  onNavigateHome: () => void;
}

export const UploadScreen: React.FC<UploadScreenProps> = ({ onNavigateHome }) => {
  const [imageUri, setImageUri] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [drafts, setDrafts] = useState<ExamDraft[] | null>(null);
  const [showManualForm, setShowManualForm] = useState(false);

  const quota = getAiQuota();

  const handleImageSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (quota.jumlah >= 20) {
      setError("Batas 20 pemindaian AI per hari tercapai. Gunakan input manual.");
      setDrafts([emptyDraft()]);
      return;
    }

    const reader = new FileReader();
    reader.onload = async () => {
      const dataUrl = reader.result as string;
      setImageUri(dataUrl);
      const base64 = dataUrl.split(",")[1];
      const mime = file.type || "image/jpeg";

      setLoading(true);
      setError(null);

      try {
        bumpAiQuota();
        const result = await parseScheduleFromImage(base64, mime);
        if (!result.length) {
          setError("Tidak ada baris jadwal terbaca. Silakan isi manual.");
          setDrafts([emptyDraft()]);
        } else {
          setDrafts(result);
        }
      } catch (err: any) {
        setError(err.message || "Gagal membaca gambar.");
        setDrafts([emptyDraft()]);
      } finally {
        setLoading(false);
      }
    };
    reader.readAsDataURL(file);
  };

  const emptyDraft = (): ExamDraft => ({
    nama_mk: "",
    kode_mk: "",
    jenis_ujian: "UTS",
    kelas: "",
    tanggal: new Date().toISOString().slice(0, 10),
    jam_mulai: "08:00",
    jam_selesai: "10:00",
  });

  const handleSaveAll = () => {
    if (!drafts) return;
    const valid = drafts.filter((d) => d.nama_mk.trim() !== "");
    if (!valid.length) {
      setError("Minimal isi satu nama mata kuliah.");
      return;
    }

    addExams(
      valid.map((d) => ({
        nama_mk: d.nama_mk,
        kode_mk: d.kode_mk,
        jenis_ujian: d.jenis_ujian || "UTS",
        kelas: d.kelas,
        tanggal: d.tanggal,
        jam_mulai: d.jam_mulai,
        jam_selesai: d.jam_selesai,
        source: imageUri ? "ocr_ai" : "manual",
      }))
    );
    onNavigateHome();
  };

  return (
    <div className="space-y-4 pb-20">
      <div>
        <h1 className="text-2xl font-black tracking-tight text-white">Upload Jadwal</h1>
        <p className="text-xs text-slate-400 mt-0.5">Screenshot tabel jadwal ujian dari portal kampus</p>
      </div>

      {!drafts ? (
        <div className="space-y-4">
          <label className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-amber-500/30 bg-amber-500/5 p-8 text-center cursor-pointer hover:border-amber-500/60 transition">
            {loading ? (
              <div className="space-y-2">
                <Loader2 className="h-8 w-8 animate-spin text-amber-400 mx-auto" />
                <p className="text-sm font-bold text-white">AI sedang membaca jadwal…</p>
              </div>
            ) : (
              <div className="space-y-2">
                <ImagePlus className="h-10 w-10 text-amber-400 mx-auto" />
                <p className="text-sm font-bold text-white">Pilih screenshot jadwal</p>
                <p className="text-xs text-slate-400">JPG atau PNG, maksimal 10MB</p>
              </div>
            )}
            <input type="file" accept="image/*" onChange={handleImageSelect} disabled={loading} className="hidden" />
          </label>

          {error && <p className="text-xs font-medium text-red-400">{error}</p>}

          <button
            onClick={() => setShowManualForm(true)}
            className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#252b3e] py-3 text-xs font-bold text-slate-200 hover:bg-slate-700 transition"
          >
            <Edit3 className="h-4 w-4" /> Isi manual tanpa screenshot
          </button>

          <p className="text-[11px] text-slate-400">
            Sisa pemindaian AI hari ini: {20 - quota.jumlah} / 20. Data jadwal tersimpan hanya di HP ini.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {imageUri && (
            <img src={imageUri} alt="Preview" className="h-40 w-full rounded-2xl border border-[#333a52] object-contain bg-[#131726]" />
          )}

          <p className="text-xs text-slate-400">Periksa hasil deteksi. Field bertanda bintang (*) wajib diisi.</p>

          {drafts.map((draft, idx) => (
            <div key={idx} className="rounded-2xl border border-[#333a52] bg-[#252b3e] p-4 space-y-3 text-xs">
              <div className="flex items-center justify-between border-b border-[#333a52] pb-2">
                <span className="font-bold text-white">Jadwal {idx + 1}</span>
                {drafts.length > 1 && (
                  <button
                    onClick={() => setDrafts(drafts.filter((_, i) => i !== idx))}
                    className="text-red-400 hover:text-red-300"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                )}
              </div>

              {draft.confidence !== undefined && draft.confidence < 0.7 && (
                <div className="flex items-center gap-1.5 rounded-lg bg-yellow-500/10 p-2 text-yellow-400 text-[11px]">
                  <AlertCircle className="h-3.5 w-3.5 shrink-0" />
                  <span>AI kurang yakin pada baris ini — periksa kembali.</span>
                </div>
              )}

              <div>
                <label className="block text-slate-300 font-medium mb-1">Nama Mata Kuliah *</label>
                <input
                  type="text"
                  value={draft.nama_mk}
                  onChange={(e) =>
                    setDrafts(drafts.map((d, i) => (i === idx ? { ...d, nama_mk: e.target.value } : d)))
                  }
                  className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Kode MK</label>
                  <input
                    type="text"
                    value={draft.kode_mk}
                    onChange={(e) =>
                      setDrafts(drafts.map((d, i) => (i === idx ? { ...d, kode_mk: e.target.value } : d)))
                    }
                    className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Kelas</label>
                  <input
                    type="text"
                    value={draft.kelas}
                    onChange={(e) =>
                      setDrafts(drafts.map((d, i) => (i === idx ? { ...d, kelas: e.target.value } : d)))
                    }
                    className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Tanggal *</label>
                  <input
                    type="date"
                    value={draft.tanggal}
                    onChange={(e) =>
                      setDrafts(drafts.map((d, i) => (i === idx ? { ...d, tanggal: e.target.value } : d)))
                    }
                    className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-2 py-2 text-white outline-none focus:border-amber-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Mulai *</label>
                  <input
                    type="text"
                    inputMode="numeric"
                    pattern="\d{2}:\d{2}"
                    maxLength={5}
                    placeholder="08:00"
                    value={draft.jam_mulai}
                    onChange={(e) =>
                      setDrafts(drafts.map((d, i) => (i === idx ? { ...d, jam_mulai: e.target.value } : d)))
                    }
                    className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-2 py-2 text-white outline-none focus:border-amber-500"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 font-medium mb-1">Selesai *</label>
                  <input
                    type="text"
                    inputMode="numeric"
                    pattern="\d{2}:\d{2}"
                    maxLength={5}
                    placeholder="10:00"
                    value={draft.jam_selesai}
                    onChange={(e) =>
                      setDrafts(drafts.map((d, i) => (i === idx ? { ...d, jam_selesai: e.target.value } : d)))
                    }
                    className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-2 py-2 text-white outline-none focus:border-amber-500"
                  />
                </div>
              </div>
            </div>
          ))}

          <button
            onClick={() => setDrafts([...drafts, emptyDraft()])}
            className="w-full flex items-center justify-center gap-1.5 rounded-xl border border-[#333a52] py-2 text-xs font-medium text-slate-300 hover:bg-slate-800"
          >
            <Plus className="h-4 w-4" /> Tambah Baris
          </button>

          <div className="flex gap-2 pt-2">
            <button
              onClick={() => {
                setDrafts(null);
                setImageUri(null);
              }}
              className="flex-1 rounded-xl bg-[#252b3e] py-2.5 text-xs font-bold text-slate-300 hover:bg-slate-700"
            >
              Batal
            </button>
            <button
              onClick={handleSaveAll}
              className="flex-1 rounded-xl bg-amber-500 py-2.5 text-xs font-bold text-slate-950 hover:bg-amber-400"
            >
              Simpan & Pasang Alarm
            </button>
          </div>
        </div>
      )}
      
      <ExamFormDialog
        show={showManualForm}
        onDismiss={() => setShowManualForm(false)}
        onSubmit={(draft) => {
          addExams([{
            ...draft,
            source: "manual",
          }]);
          onNavigateHome();
        }}
      />
    </div>
  );
};
