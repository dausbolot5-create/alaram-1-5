import React, { useState } from "react";
import { Download, Trash2, Share2, Bell } from "lucide-react";
import type { AppSettings, Exam } from "../types";
import { clearAll, saveSettings } from "../store";
import { buildIcs, downloadFile } from "../ics";

interface SettingsScreenProps {
  settings: AppSettings;
  exams: Exam[];
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ settings, exams }) => {
  const [showClearConfirm, setShowClearConfirm] = useState(false);

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
