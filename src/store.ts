import { DEFAULT_SETTINGS, type AppSettings, type Exam, type ExamStatus, type Trigger, type TriggerType } from "./types";

const KEY_EXAMS = "examping.exams";
const KEY_TRIGGERS = "examping.triggers";
const KEY_SETTINGS = "examping.settings";
const KEY_QUOTA = "examping.quota";

export interface AppData {
  exams: Exam[];
  triggers: Trigger[];
  settings: AppSettings;
}

const listeners = new Set<() => void>();

function readKey<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

let cache: AppData | null = null;

export function loadData(): AppData {
  if (!cache) {
    cache = {
      exams: readKey<Exam[]>(KEY_EXAMS, []),
      triggers: readKey<Trigger[]>(KEY_TRIGGERS, []),
      settings: { ...DEFAULT_SETTINGS, ...readKey<Partial<AppSettings>>(KEY_SETTINGS, {}) },
    };
  }
  return cache;
}

export function subscribe(fn: () => void) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

function commit(next: AppData) {
  cache = next;
  localStorage.setItem(KEY_EXAMS, JSON.stringify(next.exams));
  localStorage.setItem(KEY_TRIGGERS, JSON.stringify(next.triggers));
  localStorage.setItem(KEY_SETTINGS, JSON.stringify(next.settings));
  listeners.forEach((l) => l());
}

export function newId() {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
}

export function toEpoch(tanggal: string, jam: string): number {
  const [y, m, d] = tanggal.split("-").map(Number);
  const [hh, mm] = jam.split(":").map(Number);
  return new Date(y, (m || 1) - 1, d || 1, hh || 0, mm || 0, 0, 0).getTime();
}

export function buildTriggers(exam: Exam, settings: AppSettings): Trigger[] {
  const mulai = toEpoch(exam.tanggal, exam.jam_mulai);
  const selesai = toEpoch(exam.tanggal, exam.jam_selesai);
  const defs: Array<{ tipe: TriggerType; waktu: number }> = [];
  
  if (settings.notifMulai) {
    defs.push({ tipe: "reminder_mulai", waktu: mulai - settings.offsetMulai * 60_000 });
  }
  defs.push({ tipe: "alarm_mulai", waktu: mulai });
  if (settings.notifSelesai) {
    defs.push({ tipe: "reminder_selesai", waktu: selesai - settings.offsetSelesai * 60_000 });
  }

  return defs.map((d) => ({
    id: newId(),
    exam_id: exam.id,
    tipe: d.tipe,
    waktu: d.waktu,
    sudah_bunyi: false,
  }));
}

export function computeStatus(exam: Exam, now = Date.now()): ExamStatus {
  if (exam.status === "missed" || exam.status === "completed") return exam.status;
  const mulai = toEpoch(exam.tanggal, exam.jam_mulai);
  const selesai = toEpoch(exam.tanggal, exam.jam_selesai);
  if (now >= selesai) return "completed";
  if (now >= mulai) return "ongoing";
  return "upcoming";
}

export function addExams(drafts: Omit<Exam, "id" | "status" | "created_at">[]) {
  const data = loadData();
  const created: Exam[] = drafts.map((d) => ({
    ...d,
    id: newId(),
    status: "upcoming" as ExamStatus,
    created_at: new Date().toISOString(),
  }));
  const triggers = created.flatMap((e) => buildTriggers(e, data.settings));
  commit({
    ...data,
    exams: [...data.exams, ...created],
    triggers: [...data.triggers, ...triggers],
  });
}

export function updateExam(id: string, patch: Partial<Exam>) {
  const data = loadData();
  const exams = data.exams.map((e) => (e.id === id ? { ...e, ...patch } : e));
  const target = exams.find((e) => e.id === id);
  if (!target) return;
  const triggers = [
    ...data.triggers.filter((t) => t.exam_id !== id),
    ...buildTriggers(target, data.settings),
  ];
  commit({ ...data, exams, triggers });
}

export function deleteExam(id: string) {
  const data = loadData();
  commit({
    ...data,
    exams: data.exams.filter((e) => e.id !== id),
    triggers: data.triggers.filter((t) => t.exam_id !== id),
  });
}

export function setExamStatus(id: string, status: ExamStatus) {
  const data = loadData();
  commit({
    ...data,
    exams: data.exams.map((e) => (e.id === id ? { ...e, status } : e)),
  });
}

export function saveSettings(patch: Partial<AppSettings>) {
  const data = loadData();
  const settings = { ...data.settings, ...patch };
  const triggers = data.exams.flatMap((exam) => {
    const old = data.triggers.filter((t) => t.exam_id === exam.id);
    const fresh = buildTriggers(exam, settings);
    return fresh.map((t) => {
      const prev = old.find((o) => o.tipe === t.tipe);
      return prev ? { ...t, id: prev.id, sudah_bunyi: prev.sudah_bunyi } : t;
    });
  });
  commit({ ...data, settings, triggers });
}

export function clearAll() {
  commit({ exams: [], triggers: [], settings: loadData().settings });
}

export function getAiQuota(): { tanggal: string; jumlah: number } {
  const today = new Date().toISOString().slice(0, 10);
  const q = readKey<{ tanggal: string; jumlah: number }>(KEY_QUOTA, { tanggal: today, jumlah: 0 });
  return q.tanggal === today ? q : { tanggal: today, jumlah: 0 };
}

export function bumpAiQuota() {
  const q = getAiQuota();
  localStorage.setItem(KEY_QUOTA, JSON.stringify({ ...q, jumlah: q.jumlah + 1 }));
}
