package linusfessler.alarmtiles.sleeptimer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import linusfessler.alarmtiles.App;
import linusfessler.alarmtiles.R;
import linusfessler.alarmtiles.dialogs.TimeInputDialogBuilder;
import linusfessler.alarmtiles.views.TimeInput;

@Singleton
public class SleepTimerTileService extends TileService {

    @Inject
    SleepTimerViewModelFactory viewModelFactory;

    private SleepTimerViewModel viewModel;
    private String tileLabel;
    private AlertDialog alertDialog;

    private final PublishSubject<Boolean> clickSubject = PublishSubject.create();
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        ((App) this.getApplicationContext()).getAppComponent().inject(this);

        this.viewModel = this.viewModelFactory.create(SleepTimerViewModel.class);
        this.tileLabel = this.getString(R.string.sleep_timer);

        // Wrap context for compatibility between material components and tile service
        final Context context = new ContextThemeWrapper(this, R.style.AppTheme);
        this.alertDialog = new TimeInputDialogBuilder(context)
                .setTitle(R.string.sleep_timer_dialog_title)
                .create();
    }

    @Override
    public void onClick() {
        super.onClick();
        this.clickSubject.onNext(true);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        this.disposable.add(this.viewModel.getSleepTimer().subscribe(sleepTimer -> {
            final int state;
            if (sleepTimer.isEnabled()) {
                state = Tile.STATE_ACTIVE;
            } else {
                state = Tile.STATE_INACTIVE;
            }

            final Tile tile = this.getQsTile();
            tile.setState(state);
            tile.updateTile();
        }));

        this.disposable.add(this.viewModel.getTimeLeft().subscribe(newTimeLeft -> {
            final Tile tile = this.getQsTile();
            this.setSubtitle(tile, this.tileLabel, newTimeLeft);
            tile.updateTile();
        }));

        final Observable<SleepTimer> clickObservable = this.clickSubject.withLatestFrom(this.viewModel.getSleepTimer(), (click, sleepTimer) -> sleepTimer);
        this.disposable.add(clickObservable.subscribe(sleepTimer -> {
            if (sleepTimer.isEnabled()) {
                this.viewModel.cancel();
            } else {
                // TODO: Create class instead of duplicating the following code
                this.alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.dialog_ok), (dialog, which) -> {
                    final TimeInput timeInput = this.alertDialog.findViewById(R.id.time_input);
                    if (timeInput == null) {
                        throw new IllegalArgumentException("A time input is required as the view of the alert dialog.");
                    }

                    timeInput.getMillis().firstElement().subscribe(millis -> this.viewModel.start(millis)).dispose();
                });

                this.showDialog(this.alertDialog);
            }
        }));
    }


    @Override
    public void onStopListening() {
        this.disposable.clear();
        super.onStopListening();
    }

    @Override
    public void onDestroy() {
        this.disposable.dispose();
        super.onDestroy();
    }

    private void setSubtitle(final Tile tile, final String label, final String subtitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.setSubtitle(subtitle);
            return;
        }

        if (subtitle == null || subtitle.equals("")) {
            tile.setLabel(label);
        } else {
            tile.setLabel(label + "\n" + subtitle);
        }
    }
}
