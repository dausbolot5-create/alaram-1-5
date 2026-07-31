package com.aistudio.examping.kxmpzq.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Schedules exact alarms using AlarmManager.setAlarmClock().
 *
 * setAlarmClock() is the highest-priority alarm API on Android:
 * - It is exempt from the SCHEDULE_EXACT_ALARM permission.
 * - It shows an alarm icon in the status bar.
 * - It is almost never killed by OEM aggressive task killing.
 *
 * Alarm metadata is persisted in SharedPreferences so the BootReceiver can
 * re-register alarms after a reboot even if the app was never opened again.
 */
public final class AlarmScheduler {

    private static final String PREF_NAME = "exact_alarms";
    private static final String KEY_PREFIX = "alarm_";
    public static final String EXTRA_ALARM_ID = "alarmId";
    public static final String EXTRA_ALARM_TITLE = "alarmTitle";
    public static final String EXTRA_ALARM_BODY = "alarmBody";

    private AlarmScheduler() {}

    public static void schedule(Context context, String alarmId, long atMillis, String title, String body) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int requestCode = requestCodeFor(alarmId);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(EXTRA_ALARM_ID, alarmId);
        alarmIntent.putExtra(EXTRA_ALARM_TITLE, title);
        alarmIntent.putExtra(EXTRA_ALARM_BODY, body);
        PendingIntent operation = PendingIntent.getBroadcast(
                context,
                requestCode,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent showIntent = new Intent(context, com.aistudio.examping.kxmpzq.MainActivity.class);
        PendingIntent showOperation = PendingIntent.getActivity(
                context,
                requestCode,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(atMillis, showOperation),
                    operation
            );
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, operation);
        }

        persist(context, alarmId, atMillis, title, body);
    }

    public static void cancel(Context context, String alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent operation = PendingIntent.getBroadcast(
                context,
                requestCodeFor(alarmId),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(operation);
        operation.cancel();

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_PREFIX + alarmId).apply();
    }

    public static void cancelAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_PREFIX)) {
                String alarmId = key.substring(KEY_PREFIX.length());
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                    PendingIntent operation = PendingIntent.getBroadcast(
                            context,
                            requestCodeFor(alarmId),
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    alarmManager.cancel(operation);
                    operation.cancel();
                }
                editor.remove(key);
            }
        }
        editor.apply();
    }

    public static void rescheduleAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        SharedPreferences.Editor editor = prefs.edit();

        for (String key : prefs.getAll().keySet()) {
            if (!key.startsWith(KEY_PREFIX)) continue;
            String alarmId = key.substring(KEY_PREFIX.length());
            String raw = prefs.getString(key, null);
            if (raw == null) continue;

            try {
                JSONObject meta = new JSONObject(raw);
                long at = meta.optLong("at", 0);
                if (at > now) {
                    schedule(
                            context,
                            alarmId,
                            at,
                            meta.optString("title", ""),
                            meta.optString("body", "")
                    );
                } else {
                    editor.remove(key);
                }
            } catch (JSONException ignored) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    private static void persist(Context context, String alarmId, long atMillis, String title, String body) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONObject meta = new JSONObject();
        try {
            meta.put("at", atMillis);
            meta.put("title", title == null ? "" : title);
            meta.put("body", body == null ? "" : body);
        } catch (JSONException ignored) {
        }
        prefs.edit().putString(KEY_PREFIX + alarmId, meta.toString()).apply();
    }

    public static int requestCodeFor(String alarmId) {
        return Math.abs(alarmId.hashCode()) & 0x7fffffff;
    }
}
