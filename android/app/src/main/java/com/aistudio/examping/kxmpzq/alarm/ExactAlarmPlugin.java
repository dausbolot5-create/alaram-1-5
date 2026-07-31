package com.aistudio.examping.kxmpzq.alarm;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Capacitor plugin exposing exact alarms (setAlarmClock), the alarm
 * foreground service and battery-optimization helpers to the webview.
 */
@CapacitorPlugin(name = "ExactAlarm")
public class ExactAlarmPlugin extends Plugin {

    @PluginMethod
    public void schedule(PluginCall call) {
        String id = call.getString("id");
        Double at = call.getDouble("at");
        if (id == null || at == null) {
            call.reject("id and at are required");
            return;
        }
        String title = call.getString("title", "");
        String body = call.getString("body", "");

        AlarmScheduler.schedule(getContext(), id, at.longValue(), title, body);
        call.resolve();
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        String id = call.getString("id");
        if (id == null) {
            call.reject("id is required");
            return;
        }
        AlarmScheduler.cancel(getContext(), id);
        call.resolve();
    }

    @PluginMethod
    public void cancelAll(PluginCall call) {
        AlarmScheduler.cancelAll(getContext());
        call.resolve();
    }

    @PluginMethod
    public void stopAlarm(PluginCall call) {
        Context context = getContext();
        Intent stopIntent = new Intent(context, AlarmService.class);
        stopIntent.setAction(AlarmService.ACTION_STOP);
        context.stopService(stopIntent);
        call.resolve();
    }

    @PluginMethod
    public void isExactAlarmAllowed(PluginCall call) {
        JSObject result = new JSObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            result.put("value", alarmManager != null && alarmManager.canScheduleExactAlarms());
        } else {
            result.put("value", true);
        }
        call.resolve(result);
    }

    @PluginMethod
    public void requestExactAlarmPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null || alarmManager.canScheduleExactAlarms()) {
                call.resolve();
                return;
            }
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            try {
                startActivity(intent);
                call.resolve();
            } catch (Exception e) {
                call.reject("No settings screen available", e);
            }
        } else {
            call.resolve();
        }
    }

    @PluginMethod
    public void hasIgnoreBatteryOptimizations(PluginCall call) {
        JSObject result = new JSObject();
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        boolean value = false;
        if (powerManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                value = powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName());
            } else {
                value = true;
            }
        }
        result.put("value", value);
        call.resolve(result);
    }

    @PluginMethod
    public void requestIgnoreBatteryOptimizations(PluginCall call) {
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            try {
                startActivity(intent);
            } catch (Exception ignored) {
                // Fall back to generic settings list
                openBatteryOptimizationSettings(call);
                return;
            }
        }
        call.resolve();
    }

    @PluginMethod
    public void openBatteryOptimizationSettings(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            call.reject("No battery settings screen available", e);
            return;
        }
        call.resolve();
    }

    @PluginMethod
    public void openAppSettings(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            call.reject("No app settings screen available", e);
            return;
        }
        call.resolve();
    }

    private void startActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }
}
