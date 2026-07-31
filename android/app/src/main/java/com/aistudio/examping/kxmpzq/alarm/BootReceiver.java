package com.aistudio.examping.kxmpzq.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Re-registers all pending exact alarms after a reboot.
 * directBootAware + LOCKED_BOOT_COMPLETED so alarms survive even before the
 * user unlocks the device for the first time after reboot.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            AlarmScheduler.rescheduleAll(context);
        }
    }
}
