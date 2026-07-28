import { toEpoch, type Exam } from "./store";

export function formatTanggalLong(iso: string) {
  try {
    const [y, m, d] = iso.split("-").map(Number);
    const date = new Date(y, (m || 1) - 1, d || 1);
    return date.toLocaleDateString("id-ID", {
      weekday: "long",
      day: "numeric",
      month: "long",
      year: "numeric",
    });
  } catch {
    return iso;
  }
}

export function hitungMundur(targetEpoch: number, now = Date.now()) {
  let diff = targetEpoch - now;
  if (diff <= 0) return "sedang berlangsung";
  const days = Math.floor(diff / (24 * 3600 * 1000));
  diff %= 24 * 3600 * 1000;
  const hours = Math.floor(diff / (3600 * 1000));
  diff %= 3600 * 1000;
  const mins = Math.floor(diff / (60 * 1000));

  if (days > 0) return `${days} hari ${hours} jam lagi`;
  if (hours > 0) return `${hours} jam ${mins} menit lagi`;
  return `${mins} menit lagi`;
}

export function findBentrok(exams: Exam[]): Set<string> {
  const set = new Set<string>();
  for (let i = 0; i < exams.length; i++) {
    for (let j = i + 1; j < exams.length; j++) {
      const a = exams[i];
      const b = exams[j];
      const aStart = toEpoch(a.tanggal, a.jam_mulai);
      const aEnd = toEpoch(a.tanggal, a.jam_selesai);
      const bStart = toEpoch(b.tanggal, b.jam_mulai);
      const bEnd = toEpoch(b.tanggal, b.jam_selesai);

      if (aStart < bEnd && bStart < aEnd) {
        set.add(a.id);
        set.add(b.id);
      }
    }
  }
  return set;
}
