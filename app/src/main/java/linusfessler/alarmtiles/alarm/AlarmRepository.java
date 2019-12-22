package linusfessler.alarmtiles.alarm;

import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import linusfessler.alarmtiles.AppDatabase;
import lombok.AccessLevel;
import lombok.Getter;

class AlarmRepository {

    private final ExecutorService writeExecutor;
    private final AlarmDao alarmDao;

    @Getter(AccessLevel.PACKAGE)
    private final LiveData<Alarm> alarm;

    @Inject
    AlarmRepository(final AppDatabase appDatabase) {
        this.writeExecutor = appDatabase.getWriteExecutor();
        this.alarmDao = appDatabase.alarmDao();

        this.alarm = this.alarmDao.select();
    }

    void updateAlarm(final Alarm alarm) {
        this.writeExecutor.execute(() -> this.alarmDao.update(alarm));
    }
}
