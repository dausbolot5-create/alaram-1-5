import React, { useState } from "react";
import { Download, Trash2, Share2, Bell, Sparkles, Loader2, CheckCircle2, AlertCircle, BatteryCharging, ShieldCheck, ChevronDown } from "lucide-react";
import type { AppSettings, Exam } from "../types";
import { clearAll, saveSettings } from "../store";
import { buildIcs, downloadFile } from "../ics";
import { ExactAlarm } from "../exactAlarm";

interface SettingsScreenProps {
  settings: AppSettings;
  exams: Exam[];
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ settings, exams }) => {
  const [showClearConfirm, setShowClearConfirm] = useState(false);
  const [showOemGuide, setShowOemGuide] = useState(false);
  const [testStatus, setTestStatus] = useState<"idle" | "testing" | "success" | "error">("idle");
  const [testMessage, setTestMessage] = useState("");
  const [alarmCheck, setAlarmCheck] = useState<{ battery: boolean; exact: boolean } | null>(null);

  const handleTestConnection = async () => {
    const key = settings.apiKey || (import.meta as any).env?.VITE_GEMINI_API_KEY || (import.meta as any).env?.GEMINI_API_KEY || "";
    if (!key) {
      setTestStatus("error");
      setTestMessage("API Key kosong.");
      return;
    }
    
    setTestStatus("testing");
    // Use list models endpoint — doesn't consume generateContent quota
    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models?key=${key}`;
    
    try {
      const res = await fetch(endpoint);
      
      if (!res.ok) {
        if (res.status === 400) throw new Error("API Key tidak valid atau format salah.");
        if (res.status === 403) throw new Error("API Key tidak memiliki izin. Periksa di Google AI Studio.");
        throw new Error(`HTTP ${res.status}`);
      }
      const data = await res.json();
      const count = data.models?.length || 0;
      setTestStatus("success");
      setTestMessage(`Koneksi berhasil! ${count} model tersedia.`);
    } catch (err: any) {
      setTestStatus("error");
      setTestMessage(err.message || "Gagal menghubungi server.");
    }
  };

  return (
    <div className="space-y-4 pb-20 text-xs">
      <div>
        <h1 className="text-2xl font-black tracking-tight text-white">Pengaturan</h1>
        <p className="text-xs text-slate-400 mt-0.5">Semua data tersimpan lokal di HP ini</p>
      </div>

      {/* Notifications Section */}
      <div className="rounded-2xl border border-[#333a52] bg-[#252b3e] p-4 space-y-4">
        <h3 className="font-bold text-white text-sm flex items-center gap-2">
          <Bell className="h-4 w-4 text-amber-400" /> Notifikasi & Alarm
        </h3>

        <div className="flex items-center justify-between">
          <div>
            <p className="font-semibold text-slate-200">Suara Alarm</p>
            <p className="text-[11px] text-slate-400">Bunyi + getar saat ujian dimulai</p>
          </div>
          <input
            type="checkbox"
            checked={settings.suaraAlarm}
            onChange={(e) => saveSettings({ suaraAlarm: e.target.checked })}
            className="h-5 w-5 accent-amber-500 rounded"
          />
        </div>

        <div className="grid grid-cols-2 gap-3 pt-2 border-t border-[#333a52]">
          <div>
            <label className="block text-slate-300 font-medium mb-1">Reminder Sebelum Mulai (mnt)</label>
            <input
              type="number"
              value={settings.offsetMulai}
              onChange={(e) => saveSettings({ offsetMulai: parseInt(e.target.value, 10) || 30 })}
              className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
            />
          </div>
          <div>
            <label className="block text-slate-300 font-medium mb-1">Reminder Sebelum Selesai (mnt)</label>
            <input
              type="number"
              value={settings.offsetSelesai}
              onChange={(e) => saveSettings({ offsetSelesai: parseInt(e.target.value, 10) || 30 })}
              className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
            />
          </div>
        </div>
      </div>

      {/* Alarm Reliability Section */}
      <div className="rounded-2xl border border-[#333a52] bg-[#252b3e] p-4 space-y-3">
        <h3 className="font-bold text-white text-sm flex items-center gap-2">
          <BatteryCharging className="h-4 w-4 text-amber-400" /> Keandalan Alarm
        </h3>
        <p className="text-[11px] text-slate-400">
          Agar alarm tetap berbunyi walau aplikasi di-swipe / ditutup dari Recent Apps, pastikan aplikasi
          tidak dioptimasi baterai dan diizinkan jalan di latar belakang.
        </p>

        {alarmCheck && (
          <div className="space-y-1 text-[11px]">
            <p className={alarmCheck.battery ? "text-green-400" : "text-red-400"}>
              {alarmCheck.battery ? "✓" : "✗"} Pengecualian optimasi baterai {alarmCheck.battery ? "aktif" : "belum aktif"}
            </p>
            <p className={alarmCheck.exact ? "text-green-400" : "text-red-400"}>
              {alarmCheck.exact ? "✓" : "✗"} Izin alarm tepat waktu {alarmCheck.exact ? "aktif" : "belum aktif"}
            </p>
          </div>
        )}

        <button
          onClick={async () => {
            try {
              const [battery, exact] = await Promise.all([
                ExactAlarm.hasIgnoreBatteryOptimizations(),
                ExactAlarm.isExactAlarmAllowed(),
              ]);
              setAlarmCheck({ battery: battery.value, exact: exact.value });
            } catch {
              setAlarmCheck(null);
            }
          }}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition"
        >
          <ShieldCheck className="h-4 w-4" /> Periksa Keandalan Alarm
        </button>

        <button
          onClick={() => ExactAlarm.requestIgnoreBatteryOptimizations().catch(() => {})}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition"
        >
          <BatteryCharging className="h-4 w-4" /> Kecualikan dari Optimasi Baterai
        </button>

        <button
          onClick={() => ExactAlarm.openBatteryOptimizationSettings().catch(() => {})}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition"
        >
          Buka Pengaturan Baterai
        </button>

        <button
          onClick={() => ExactAlarm.requestExactAlarmPermission().catch(() => {})}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition"
        >
          Izinkan Alarm Tepat Waktu
        </button>

        <button
          onClick={() => ExactAlarm.openAppSettings().catch(() => {})}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition"
        >
          Buka Pengaturan Aplikasi
        </button>

        <button
          onClick={() => setShowOemGuide((v) => !v)}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition"
        >
          Panduan Autostart Per Merek HP <ChevronDown className={`h-4 w-4 transition ${showOemGuide ? "rotate-180" : ""}`} />
        </button>

        {showOemGuide && (
          <div className="space-y-3 text-[11px] text-slate-300">
            <div>
              <p className="font-bold text-amber-400 mb-0.5">Xiaomi / Redmi / POCO (MIUI/HyperOS)</p>
              <p className="text-slate-400">
                Pengaturan → Aplikasi → Kelola aplikasi → pilih Examping → Autostart: aktifkan. Lalu
                Baterai → Tanpa batasan.
              </p>
            </div>
            <div>
              <p className="font-bold text-amber-400 mb-0.5">Oppo / Realme (ColorOS)</p>
              <p className="text-slate-400">
                Pengaturan → Baterai → Aplikasi latar belakang → pilih Examping → izinkan. Lalu
                Pengaturan → Aplikasi → izin → Autostart.
              </p>
            </div>
            <div>
              <p className="font-bold text-amber-400 mb-0.5">Vivo (FuntouchOS/OriginOS)</p>
              <p className="text-slate-400">
                iManager → Pengelola Aplikasi → Autostart → aktifkan Examping. Lalu Pengaturan →
                Baterai → Konsumsi daya latar belakang → izinkan.
              </p>
            </div>
            <div>
              <p className="font-bold text-amber-400 mb-0.5">Samsung (One UI)</p>
              <p className="text-slate-400">
                Pengaturan → Aplikasi → Examping → Baterai → Tidak dibatasi. Lalu Aplikasi yang tidak
                pernah tidur → tambahkan Examping.
              </p>
            </div>
            <div>
              <p className="font-bold text-amber-400 mb-0.5">Huawei / Honor (EMUI/HarmonyOS)</p>
              <p className="text-slate-400">
                AppGallery → Optimasi → Izin start otomatis → aktifkan Examping. Lalu Pengaturan →
                Baterai → Peluncuran aplikasi → kelola manual.
              </p>
            </div>
          </div>
        )}
      </div>

      {/* AI Settings Section */}
      <div className="rounded-2xl border border-[#333a52] bg-[#252b3e] p-4 space-y-4">
        <h3 className="font-bold text-white text-sm flex items-center gap-2">
          <Sparkles className="h-4 w-4 text-amber-400" /> AI Vision (Gemini)
        </h3>
        
        <div>
          <label className="block text-slate-300 font-medium mb-1">API Key (Gemini)</label>
          <input
            type="password"
            value={settings.apiKey || ""}
            onChange={(e) => saveSettings({ apiKey: e.target.value })}
            className="w-full rounded-xl bg-[#1e2436] border border-[#333a52] px-3 py-2 text-white outline-none focus:border-amber-500"
            placeholder="AIzaSy..."
          />
          <p className="text-[10px] text-slate-500 mt-1">Kosongkan jika menggunakan key bawaan aplikasi.</p>
        </div>

        <button
          onClick={handleTestConnection}
          disabled={testStatus === "testing"}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition disabled:opacity-50"
        >
          {testStatus === "testing" ? <Loader2 className="h-4 w-4 animate-spin" /> : "Test Koneksi AI"}
        </button>

        {testStatus !== "idle" && (
          <div className={`flex items-start gap-2 p-3 rounded-xl border text-[11px] ${
            testStatus === "success" 
              ? "bg-green-500/10 border-green-500/30 text-green-400" 
              : "bg-red-500/10 border-red-500/30 text-red-400"
          }`}>
            {testStatus === "success" ? <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" /> : <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />}
            <span className="break-all">{testMessage}</span>
          </div>
        )}
      </div>

      {/* Data & Backup Section */}
      <div className="rounded-2xl border border-[#333a52] bg-[#252b3e] p-4 space-y-3">
        <h3 className="font-bold text-white text-sm">Data & Cadangan</h3>
        <p className="text-slate-400 text-[11px]">{exams.length} jadwal tersimpan di perangkat ini.</p>

        <button
          onClick={() => downloadFile(buildIcs(exams), "jadwal_ujian.ics", "text/calendar")}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-[#333a52] bg-[#1e2436] py-2.5 font-bold text-slate-200 hover:bg-slate-700 transition"
        >
          <Share2 className="h-4 w-4" /> Export semua ke kalender (.ics)
        </button>

        <button
          onClick={() => setShowClearConfirm(true)}
          className="w-full flex items-center justify-center gap-2 rounded-xl border border-red-500/30 bg-red-500/10 py-2.5 font-bold text-red-400 hover:bg-red-500/20 transition"
        >
          <Trash2 className="h-4 w-4" /> Hapus semua data
        </button>
      </div>

      {showClearConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 p-4">
          <div className="w-full max-w-xs rounded-2xl bg-[#252b3e] border border-[#333a52] p-5 text-center space-y-3">
            <h4 className="font-bold text-white text-base">Hapus Semua Data?</h4>
            <p className="text-xs text-slate-400">
              Seluruh jadwal ujian dan alarm di HP ini akan dihapus permanen.
            </p>
            <div className="flex gap-2 pt-2">
              <button
                onClick={() => setShowClearConfirm(false)}
                className="flex-1 rounded-xl bg-[#1e2436] py-2 font-medium text-slate-300"
              >
                Batal
              </button>
              <button
                onClick={() => {
                  clearAll();
                  setShowClearConfirm(false);
                }}
                className="flex-1 rounded-xl bg-red-500 py-2 font-bold text-white"
              >
                Ya, Hapus
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
