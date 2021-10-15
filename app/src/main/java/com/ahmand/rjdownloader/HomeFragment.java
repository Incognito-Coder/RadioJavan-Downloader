package com.ahmand.rjdownloader;

import static com.ahmand.rjdownloader.R.id;
import static com.ahmand.rjdownloader.R.layout;
import static com.ahmand.rjdownloader.R.string;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    View view;
    DownloadManager downloadmanager;
    TextInputLayout link_text;
    TextView title_text;
    ImageView thumbnail;
    String link;
    String title;
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
        downloadmanager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
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
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Cant be empty", Snackbar.LENGTH_SHORT).show();
            } else {
                Fetch(link_text.getEditText().getText().toString());
            }
        });
        down.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(link)) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), string.download_started, Snackbar.LENGTH_SHORT).show();
                Uri uri = Uri.parse(link);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setTitle(title);
                request.setDescription("Downloading");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                Map<String, String> types = new HashMap<>();
                types.put("music", ".mp3");
                types.put("podcast", ".mp3");
                types.put("video", ".mp4");
                request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/RadioJavan", title + types.get(mime))));
                downloadmanager.enqueue(request);
            } else {
                Snackbar.make(getActivity().findViewById(android.R.id.content), string.empty_queue, Snackbar.LENGTH_SHORT).show();
            }

        });
        copy.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(link)) {
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("download link", link));
                Snackbar.make(getActivity().findViewById(android.R.id.content), string.copied_clip, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(getActivity().findViewById(android.R.id.content), string.empty_queue, Snackbar.LENGTH_SHORT).show();
            }
        });
        control_button.setOnClickListener(v -> {
            switch (mediaState) {
                case STOP:
                    player.start();
                    mediaState = StreamState.PLAYING;
                    control_button.setImageResource(R.drawable.ic_round_pause_24);
                    break;
                case PAUSE:
                case PLAYING:
                    player.pause();
                    mediaState = StreamState.STOP;
                    control_button.setImageResource(R.drawable.ic_round_play_arrow_24);
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
                control_button.setImageResource(R.drawable.ic_round_play_arrow_24);
            });
        });
    }

    public void Fetch(String rj) {
        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(getContext());
        pDialog.setTitle(getString(R.string.fetchtext));
        pDialog.setMessage(getString(R.string.searching));
        pDialog.setCancelable(false);
        pDialog.show();
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://mr-alireza.ir/RJ/rj.php?link=" + rj,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        link = json.getString("result");
                        title = json.getString("title");
                        mime = json.getString("type");
                        title_text.setText(title);
                        Glide.with(requireContext()).load(json.getString("photo")).into(thumbnail);
                        control_button.setEnabled(true);
                        playerIsReady();
                        pDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                Throwable::printStackTrace
        );

        queue.add(stringRequest);
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