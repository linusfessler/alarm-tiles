package linusfessler.alarmtiles.timer;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.TimeUnit;

import linusfessler.alarmtiles.shared.TimeFormatter;

public class TimerViewModel extends ViewModel {

    private final TimerRepository repository;
    private final String tileLabel;
    private final TimeFormatter timeFormatter;

    private final LiveData<Timer> timerLiveData;
    private final LiveData<String> tileLabelLiveData;

    private CountDownTimer countdown;

    public TimerViewModel(final TimerRepository repository, final String tileLabel, final TimeFormatter timeFormatter) {
        this.repository = repository;
        this.tileLabel = tileLabel;
        this.timeFormatter = timeFormatter;

        timerLiveData = this.repository.getTimer();

        tileLabelLiveData = Transformations.switchMap(timerLiveData, timer -> {
            final MutableLiveData<String> tileLabelMutableLiveData = new MutableLiveData<>();

            if (timer == null) {
                return tileLabelMutableLiveData;
            }

            if (!timer.isEnabled()) {
                if (countdown != null) {
                    countdown.cancel();
                    tileLabelMutableLiveData.postValue(tileLabel);
                }
                return tileLabelMutableLiveData;
            }

            final long millisElapsed = System.currentTimeMillis() - timer.getStartTimeStamp();
            final long millisLeft = timer.getDuration() - millisElapsed;
            countdown = new CountDownTimer(millisLeft, 1000) {
                @Override
                public void onTick(final long millisUntilFinished) {
                    final String formattedTimeLeft = timeFormatter.format(millisUntilFinished, TimeUnit.SECONDS);
                    tileLabelMutableLiveData.postValue(tileLabel + "\n" + formattedTimeLeft);
                }

                @Override
                public void onFinish() {
                    tileLabelMutableLiveData.postValue(tileLabel);

                    timer.disable();
                    TimerViewModel.this.repository.update(timer);

                    // TODO: Trigger alarm
                }
            };
            countdown.start();

            return tileLabelMutableLiveData;
        });
    }

    public LiveData<Timer> getTimer() {
        return timerLiveData;
    }

    public LiveData<String> getTileLabel() {
        return tileLabelLiveData;
    }

    public void toggle(final Timer timer) {
        timer.toggle();
        repository.update(timer);
    }
}