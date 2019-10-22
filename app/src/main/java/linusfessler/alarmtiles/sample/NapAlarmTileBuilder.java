package linusfessler.alarmtiles.sample;

import android.content.Context;

import linusfessler.alarmtiles.R;
import linusfessler.alarmtiles.model.AlarmTile;
import linusfessler.alarmtiles.model.FallAsleepSettings;
import linusfessler.alarmtiles.model.GeneralSettings;
import linusfessler.alarmtiles.model.SleepSettings;
import linusfessler.alarmtiles.model.SnoozeSettings;
import linusfessler.alarmtiles.model.WakeUpSettings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NapAlarmTileBuilder {

    @NonNull
    private final Context context;

    public AlarmTile build() {
        final GeneralSettings generalSettings = GeneralSettings.builder()
                .name(context.getResources().getString(R.string.sample_nap_name))
                .iconResourceId(R.drawable.ic_snooze_24px)
                .showingNotification(true)
                .graduallyIncreasingVolume(false)
                .vibrating(false)
                .turningOnFlashlight(false)
                .build();

        final FallAsleepSettings fallAsleepSettings = FallAsleepSettings.builder()
                .timerEnabled(false)
                .timerHours(0)
                .timerMinutes(0)
                .slowlyFadingMusicOut(false)
                .build();

        final SleepSettings sleepSettings = SleepSettings.builder()
                .timerEnabled(true)
                .timerHours(1)
                .timerMinutes(0)
                .enteringDoNotDisturb(false)
                .allowingPriorityNotifications(false)
                .build();

        final WakeUpSettings wakeUpSettings = WakeUpSettings.builder()
                .alarmEnabled(false)
                .alarmHour(0)
                .alarmMinute(0)
                .build();

        final SnoozeSettings snoozeSettings = SnoozeSettings.builder()
                .snoozeEnabled(false)
                .snoozeHours(0)
                .snoozeMinutes(0)
                .build();

        return new AlarmTile(generalSettings, fallAsleepSettings, sleepSettings, wakeUpSettings, snoozeSettings);
    }

}
