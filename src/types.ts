export type ExamStatus = "upcoming" | "ongoing" | "completed" | "missed";
export type ExamSource = "ocr_ai" | "manual";
export type TriggerType = "reminder_mulai" | "alarm_mulai" | "reminder_selesai";

export interface Exam {
  id: string;
  nama_mk: string;
  kode_mk: string;
  jenis_ujian: string;
  kelas: string;
  tanggal: string; // YYYY-MM-DD
  jam_mulai: string; // HH:mm
  jam_selesai: string; // HH:mm
  status: ExamStatus;
  source: ExamSource;
  created_at: string;
}

export interface Trigger {
  id: string;
  exam_id: string;
  tipe: TriggerType;
  waktu: number; // epoch ms
  sudah_bunyi: boolean;
}

export interface ExamDraft {
  nama_mk: string;
  kode_mk: string;
  jenis_ujian: string;
  kelas: string;
  tanggal: string;
  jam_mulai: string;
  jam_selesai: string;
  confidence?: number;
}

export interface AppSettings {
  offsetMulai: number;
  offsetSelesai: number;
  suaraAlarm: boolean;
  apiKey: string;
}

export const DEFAULT_SETTINGS: AppSettings = {
  offsetMulai: 30,
  offsetSelesai: 30,
  suaraAlarm: true,
  apiKey: "",
};

export const JENIS_UJIAN = ["UTS", "UAS", "Kuis", "Praktikum", "Responsi", "Lainnya"];
