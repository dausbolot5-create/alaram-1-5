package com.aistudio.examping.kxmpzq.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Foreground service that plays a looping alarm sound and shows a full-screen
 * alarm notification. Because it is a foreground service it survives process
 * death and is exempt from most OEM task killing (with the caveats noted in
 * the settings screen).
 */
public class AlarmService extends Service {

    private static final String CHANNEL_ID = "exact_alarm_channel";
    private static final int NOTIFICATION_ID = 9001;
    public static final String ACTION_STOP = "com.aistudio.examping.kxmpzq.STOP_ALARM";

    private MediaPlayer player;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID);
        String title = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE);
        String body = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_BODY);

        if (alarmId == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, buildNotification(title, body));
        startRinging();
        startVibration();

        return START_STICKY;
    }

    private Notification buildNotification(String title, String body) {
        Intent stopIntent = new Intent(this, AlarmService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent activityIntent = new Intent(this, com.aistudio.examping.kxmpzq.MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fullScreen = PendingIntent.getActivity(
                this,
                1,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title != null ? title : "Alarm Ujian!")
                .setContentText(body != null ? body : "Waktunya ujian dimulai.")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreen, true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Matikan", stopPending)
                .setContentIntent(fullScreen)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Ujian",
                    NotificationManager.IMPORTANCE_MAX
            );
            channel.setDescription("Alarm berdering hingga dimatikan");
            channel.enableVibration(true);
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(channel);
        }
    }

    private void startRinging() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setDataSource(this, uri);
            player.setLooping(true);
            player.setWakeMode(this, android.os.PowerManager.PARTIAL_WAKE_LOCK);
            player.setOnErrorListener((mp, what, extra) -> {
                stopSelf();
                return true;
            });
            player.prepare();
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
            }
            player.start();
        } catch (Exception e) {
            stopSelf();
        }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;
        long[] pattern = {0, 600, 400, 600, 400};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            vibrator.vibrate(pattern, 0);
        }
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            try {
                player.stop();
            } catch (IllegalStateException ignored) {
            }
            player.release();
            player = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
