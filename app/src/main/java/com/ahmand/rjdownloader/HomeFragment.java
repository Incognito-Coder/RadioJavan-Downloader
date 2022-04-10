package com.ahmand.rjdownloader;

import static com.ahmand.rjdownloader.R.drawable;
import static com.ahmand.rjdownloader.R.id;
import static com.ahmand.rjdownloader.R.layout;
import static com.ahmand.rjdownloader.R.string;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    View view;
    TextInputLayout link_text;
    TextView title_text;
    ImageView thumbnail;
    String link;
    String mime;
    String shared;
    MediaPlayer player;
    StreamState mediaState = StreamState.STOP;
    TextView duration;
    TextView elapsed;
    Slider seek;
    FloatingActionButton control_button;
    Timer timer;
    boolean isDragging;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            HomeActivity activity = (HomeActivity) getActivity();
            assert activity != null;
            shared = activity.GetShared();
        } catch (Exception ignored) {

        }

        view = inflater.inflate(layout.fragment_home_linear, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View down = view.findViewById(id.download_button);
        View copy = view.findViewById(id.copy_link);
        View search = view.findViewById(id.search_button);
        control_button = view.findViewById(id.playBtn);
        link_text = view.findViewById(id.url_field);
        link_text.getEditText().setText(shared);
        title_text = view.findViewById(id.title_text);
        thumbnail = view.findViewById(id.thumb);
        duration = view.findViewById(id.durationTv);
        elapsed = view.findViewById(id.positionTv);
        seek = view.findViewById(id.musicSlider);
        search.setOnClickListener(v -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(link_text.getEditText().getText()).toString())) {
                Snackbar.make(getView(), string.empty, Snackbar.LENGTH_SHORT).show();
            } else if (Utils.isRadioJavan(link_text.getEditText().getText().toString())) {
                APIService apiService = new APIService(getContext());
                apiService.Fetch(link_text.getEditText().getText().toString(), thumbnail, () -> {
                    link = apiService.link;
                    title_text.setText(apiService.title);
                    mime = apiService.mime;
                    control_button.setEnabled(true);
                    playerIsReady();
                });
            } else {
                Snackbar.make(getView(), string.invalidUrl, Snackbar.LENGTH_SHORT).show();
            }
        });
        down.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(link)) {
                boolean download_with = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("use_android_download_manager", true);
                String quality = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("list_preference_quality", "256");
                DownloadService async = new DownloadService(getContext());
                if (quality.equals("256")) {
                    async.url = link;
                } else {
                    async.url = link.replace("256", "128");
                }
                if (!download_with) {
                    async.execute();
                } else {
                    Snackbar.make(getView(), string.download_started, Snackbar.LENGTH_SHORT).show();
                    async.defaultDownloadManager();
                }
            } else {
                Snackbar.make(getView(), string.empty_queue, Snackbar.LENGTH_SHORT).show();
            }
        });
        copy.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(link)) {
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("download link", link));
                Snackbar.make(getView(), string.copied_clip, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(getView(), string.empty_queue, Snackbar.LENGTH_SHORT).show();
            }
        });
        control_button.setOnClickListener(v -> {
            switch (mediaState) {
                case STOP:
                    player.start();
                    mediaState = StreamState.PLAYING;
                    control_button.setImageResource(drawable.ic_round_pause_24);
                    break;
                case PAUSE:
                case PLAYING:
                    player.pause();
                    mediaState = StreamState.STOP;
                    control_button.setImageResource(drawable.ic_round_play_arrow_24);
                    break;
            }
        });
        seek.addOnChangeListener((slider, value, fromUser) ->
                elapsed.setText(Utils.convertMillisToString((long) value))
        );
        seek.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                isDragging = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                isDragging = false;
                player.seekTo((int) slider.getValue());
            }
        });
    }


    public void playerIsReady() {
        player = MediaPlayer.create(getContext(), Uri.parse(link));
        player.setOnPreparedListener(mp -> {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(() -> {
                        if (!isDragging) {
                            elapsed.setText(Utils.convertMillisToString(player.getCurrentPosition()));
                            seek.setValue(player.getCurrentPosition());
                        }
                    });

                }
            }, 1000, 1000);
            duration.setText(Utils.convertMillisToString(player.getDuration()));
            seek.setEnabled(true);
            seek.setValueTo(player.getDuration());
            player.setOnCompletionListener(mp1 -> {
                timer.cancel();
                player.release();
                mediaState = StreamState.STOP;
                control_button.setImageResource(drawable.ic_round_play_arrow_24);
            });
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            timer.cancel();
            player.release();
            player = null;
        } catch (Exception e) {
            Log.d(TAG, "onDestroyView: " + e);
        }
    }
}