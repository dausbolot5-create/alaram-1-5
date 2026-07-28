import { LocalNotifications, ScheduleOptions } from "@capacitor/local-notifications";
import { AppData } from "./store";
import { toEpoch } from "./store";

export async function syncNotifications(data: AppData) {
  try {
    const hasPerm = await LocalNotifications.checkPermissions();
    if (hasPerm.display !== "granted") {
      const req = await LocalNotifications.requestPermissions();
      if (req.display !== "granted") return; // abort if not granted
    }

    // Cancel all existing to keep it simple and in sync
    const pending = await LocalNotifications.getPending();
    if (pending.notifications.length > 0) {
      await LocalNotifications.cancel({ notifications: pending.notifications });
    }

    const now = Date.now();
    const toSchedule = data.triggers
      .filter((t) => !t.sudah_bunyi && t.waktu > now)
      .map((t) => {
        const exam = data.exams.find((e) => e.id === t.exam_id);
        if (!exam) return null;

        let title = "Alarm Ujian!";
        let body = exam.nama_mk;

        if (t.tipe === "reminder_mulai") {
          title = "Ujian Segera Mulai";
          body = `${exam.nama_mk} mulai dalam ${data.settings.offsetMulai} menit.`;
        } else if (t.tipe === "reminder_selesai") {
          title = "Waktu Hampir Habis";
          body = `${exam.nama_mk} selesai dalam ${data.settings.offsetSelesai} menit.`;
        } else if (t.tipe === "alarm_mulai") {
          title = "Ujian Dimulai!";
          body = `${exam.nama_mk} mulai sekarang.`;
        }

        // generate numerical ID for capacitor
        const intId = parseInt(t.id.slice(0, 8), 36) % 2147483647; 

        return {
          id: intId || Math.floor(Math.random() * 1000000),
          title,
          body,
          schedule: { at: new Date(t.waktu), allowWhileIdle: true },
          smallIcon: "ic_stat_icon_config_sample", // default fallback
        };
      })
      .filter(Boolean) as ScheduleOptions["notifications"];

    if (toSchedule.length > 0) {
      await LocalNotifications.schedule({ notifications: toSchedule });
    }
  } catch (e) {
    console.error("Failed to sync notifications", e);
  }
}
