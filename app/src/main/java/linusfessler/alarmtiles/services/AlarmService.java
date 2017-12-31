package linusfessler.alarmtiles.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import linusfessler.alarmtiles.R;

public class AlarmService extends Service {

    public static final long[] DEFAULT_VIBRATION_PATTERN = { 500, 500 };

    private Ringtone alarmTone;
    private Vibrator vibrator;
    private boolean alarmIsActive;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmIsActive = true;

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String uriPath = preferences.getString(getString(R.string.pref_alarm_sound_key), "");
        if (!uriPath.equals("")) {
            Uri uri = Uri.parse(uriPath);
            alarmTone = RingtoneManager.getRingtone(this, uri);
            alarmTone.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
            alarmTone.play();
        }

        boolean vibrate = preferences.getBoolean(getString(R.string.pref_vibrate_key), false);
        if (vibrate) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                long vibrationPause = Long.parseLong(preferences.getString(getString(R.string.pref_pause_duration_key), String.valueOf(DEFAULT_VIBRATION_PATTERN[0])));
                long vibrationDuration = Long.parseLong(preferences.getString(getString(R.string.pref_vibration_duration_key), String.valueOf(DEFAULT_VIBRATION_PATTERN[1])));
                long[] vibrationPattern = new long[]{ vibrationPause, vibrationDuration };
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0));
                } else {
                    vibrator.vibrate(vibrationPattern, 0);
                }
            }
        }

        boolean increaseVolume = preferences.getBoolean(getString(R.string.pref_increase_volume_key), false);
        if (increaseVolume) {
            Executors.newFixedThreadPool(1).execute(new Runnable() {
                @Override
                public void run() {
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    if (audioManager != null) {
                        int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

                        int duration = 1000 * preferences.getInt(getString(R.string.pref_increase_volume_duration_key), 0);
                        long endTime = duration + System.currentTimeMillis();
                        int currentVolume = 0;
                        while (alarmIsActive && currentVolume < originalVolume) {
                            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, ++currentVolume, 0);
                            long timeLeft = endTime - System.currentTimeMillis();
                            long timeStep = Math.max(timeLeft, 0) / Math.max(originalVolume - currentVolume, 1);
                            try {
                                Thread.sleep(timeStep);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarmIsActive = false;

        if (alarmTone != null) {
            alarmTone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
