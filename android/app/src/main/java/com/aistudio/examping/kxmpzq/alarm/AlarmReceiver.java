package com.aistudio.examping.kxmpzq.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

/**
 * Receives the exact alarm broadcast and starts the foreground AlarmService.
 * Registered in AndroidManifest so it keeps working even when the app process
 * was killed (e.g. swiped from recent apps).
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "examping:AlarmReceiver");
            wakeLock.acquire(10000);
        }

        try {
            Intent serviceIntent = new Intent(context, AlarmService.class);
            serviceIntent.putExtra(AlarmScheduler.EXTRA_ALARM_ID,
                    intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID));
            serviceIntent.putExtra(AlarmScheduler.EXTRA_ALARM_TITLE,
                    intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE));
            serviceIntent.putExtra(AlarmScheduler.EXTRA_ALARM_BODY,
                    intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_BODY));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        } finally {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
}
