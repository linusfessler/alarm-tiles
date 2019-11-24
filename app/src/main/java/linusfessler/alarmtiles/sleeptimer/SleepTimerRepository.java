package linusfessler.alarmtiles.sleeptimer;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutorService;

import linusfessler.alarmtiles.AppDatabase;
import lombok.AccessLevel;
import lombok.Getter;

class SleepTimerRepository {

    private final ExecutorService writeExecutor;
    private final SleepTimerDao sleepTimerDao;

    @Getter(AccessLevel.PACKAGE)
    private final LiveData<SleepTimer> sleepTimer;

    SleepTimerRepository(final Application application) {
        final AppDatabase db = AppDatabase.getInstance(application);
        this.writeExecutor = db.getWriteExecutor();
        this.sleepTimerDao = db.sleepTimerDao();
        this.sleepTimer = this.sleepTimerDao.select();
    }

    void setSleepTimer(final SleepTimer sleepTimer) {
        this.writeExecutor.execute(() -> {
            if (this.sleepTimerDao.count() == 0) {
                this.sleepTimerDao.insert(sleepTimer);
            } else {
                this.sleepTimerDao.update(sleepTimer);
            }
        });
    }
}
