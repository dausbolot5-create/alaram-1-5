import { LocalNotifications, ScheduleOptions } from "@capacitor/local-notifications";
import { ExactAlarm } from "./exactAlarm";
import { AppData } from "./store";

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
      .filter((t) => !t.sudah_bunyi && t.waktu > now && t.tipe !== "alarm_mulai")
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
        }

        // generate numerical ID for capacitor
        const intId = parseInt(t.id.slice(0, 8), 36) % 2147483647; 

        return {
          id: intId || Math.floor(Math.random() * 1000000),
          title,
          body,
          schedule: { at: new Date(t.waktu), allowWhileIdle: true },
          smallIcon: "ic_stat_icon_config_sample", // default fallback
          sound: "beep", // default sound instead of silent
          channelId: "alarm_channel",
        };
      })
      .filter(Boolean) as ScheduleOptions["notifications"];

    // Ensure channel exists
    await LocalNotifications.createChannel({
      id: "alarm_channel",
      name: "Alarm Channel",
      importance: 5,
      visibility: 1,
      vibration: true
    });

    if (toSchedule.length > 0) {
      await LocalNotifications.schedule({ notifications: toSchedule });
    }
    // Schedule native exact alarms (AlarmManager.setAlarmClock + foreground service)
    // so they still fire when the app is killed / swiped from recent apps.
    try {
      const nowMs = Date.now();
      const alarmTriggers = data.triggers.filter((t) => !t.sudah_bunyi && t.waktu > nowMs && t.tipe === "alarm_mulai");

      // Reset native alarms first so removed/cancelled triggers don't ring.
      await ExactAlarm.cancelAll();

      for (const t of alarmTriggers) {
        const exam = data.exams.find((e) => e.id === t.exam_id);
        if (!exam) continue;

        await ExactAlarm.schedule({
          id: t.id,
          at: t.waktu,
          title: "Alarm Ujian!",
          body: `${exam.nama_mk} mulai sekarang.`,
        });
      }
    } catch (e) {
      console.warn("Failed to set exact alarms", e);
    }

  } catch (e) {
    console.error("Failed to sync notifications", e);
  }
}
