package linusfessler.alarmtiles.sleeptimer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import linusfessler.alarmtiles.App;
import linusfessler.alarmtiles.R;
import linusfessler.alarmtiles.databinding.FragmentSleepTimerConfigBinding;

public class SleepTimerConfigFragment extends Fragment {

    @Inject
    SleepTimerViewModelFactory sleepTimerViewModelFactory;

    private SleepTimerViewModel viewModel;
    private FragmentSleepTimerConfigBinding binding;
    private SleepTimer sleepTimer;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((App) this.requireActivity().getApplicationContext()).getAppComponent().inject(this);
        this.viewModel = new ViewModelProvider(this, this.sleepTimerViewModelFactory).get(SleepTimerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sleep_timer_config, container, false);

        this.disposable.add(this.viewModel.getSleepTimer().firstElement().subscribe(newSleepTimer -> {
            this.initializeView(newSleepTimer);
            this.viewModel.setUnavailable(newSleepTimer, true);
        }));

        this.disposable.add(this.viewModel.getSleepTimer().subscribe(newSleepTimer -> {
            this.registerSleepTimerListeners(newSleepTimer);
            this.sleepTimer = newSleepTimer;
        }));

        this.disposable.add(this.viewModel.isResettingVolumeEnabled().subscribe(this.binding.resettingVolume::setEnabled));

        return this.binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.sleepTimer != null) {
            if (this.sleepTimer.isEnabled()) {
                final NavController navController = NavHostFragment.findNavController(this);
                navController.navigateUp();
            } else {
                this.viewModel.setUnavailable(this.sleepTimer, true);
            }
        }
    }

    @Override
    public void onStop() {
        if (this.sleepTimer != null) {
            this.viewModel.setUnavailable(this.sleepTimer, false);
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        this.unregisterSleepTimerListeners();
        this.disposable.clear();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        this.disposable.dispose();
        super.onDestroy();
    }

    private void initializeView(final SleepTimer sleepTimer) {
        final SleepTimerConfig config = sleepTimer.getConfig();
        this.binding.duration.updateTime(config.getDuration());
        this.binding.fading.setChecked(config.isFading());
        this.binding.resettingVolume.setChecked(config.isResettingVolume());
    }

    private void registerSleepTimerListeners(final SleepTimer sleepTimer) {
        this.binding.duration.setOnTimeChangedListener((hours, minutes) ->
                this.viewModel.setDuration(sleepTimer, hours, minutes));

        this.binding.fading.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.viewModel.setFading(sleepTimer, isChecked));

        this.binding.resettingVolume.setOnCheckedChangeListener((buttonView, isChecked) ->
                this.viewModel.setResettingVolume(sleepTimer, isChecked));
    }

    private void unregisterSleepTimerListeners() {
        this.binding.duration.setOnTimeChangedListener(null);
        this.binding.fading.setOnCheckedChangeListener(null);
        this.binding.resettingVolume.setOnCheckedChangeListener(null);
    }
}
