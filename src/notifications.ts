import { LocalNotifications, ScheduleOptions } from "@capacitor/local-notifications";
import { Alarm } from "@capawesome/capacitor-alarm";
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
    // Schedule native alarms
    try {
      const perms = await Alarm.checkPermissions();
      if (perms.alarm !== 'granted') {
        await Alarm.requestPermissions();
      }
      
      // we only care about alarm_mulai for full system alarms
      const alarmTriggers = data.triggers.filter((t) => !t.sudah_bunyi && t.waktu > now && t.tipe === "alarm_mulai");
      
      for (const t of alarmTriggers) {
         const exam = data.exams.find((e) => e.id === t.exam_id);
         if (!exam) continue;
         
         const alarmTime = new Date(t.waktu);
         
         await Alarm.setAlarm({
           hour: alarmTime.getHours(),
           minute: alarmTime.getMinutes(),
           message: exam.nama_mk,
           skipUi: true,
           days: [] // One-time alarm
         });
      }
    } catch (e) {
      console.warn("Failed to set system alarms", e);
    }
    
  } catch (e) {
    console.error("Failed to sync notifications", e);
  }
}
