package com.ahmand.rjdownloader;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ahmand.rjdownloader.R.id;
import static com.ahmand.rjdownloader.R.layout;
import static com.ahmand.rjdownloader.R.string;
import static com.ahmand.rjdownloader.R.style;


public class HomeFragment extends Fragment {
    View view;
    DownloadManager downloadmanager;
    AppCompatEditText link_text;
    TextView title_text;
    ImageView thumbnail;
    String link;
    String title;
    String mime;
    String shared;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        downloadmanager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            HomeActivity activity = (HomeActivity) getActivity();
            assert activity != null;
            shared = activity.GetShared();
        } catch (Exception ignored) {

        }

        view = inflater.inflate(layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SwitchCompat switcher = view.findViewById(id.Switch_autodl);
        AppCompatButton down = view.findViewById(id.download_button);
        Button search = view.findViewById(id.search_button);
        link_text = view.findViewById(id.url_field);
        link_text.setText(shared);
        title_text = view.findViewById(id.title_text);
        thumbnail = view.findViewById(id.thumb);
        switcher.setOnClickListener(v -> Toast.makeText(getContext(), string.soon_text, Toast.LENGTH_SHORT).show());
        search.setOnClickListener(v -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(link_text.getText()).toString())) {
                Toast.makeText(getContext(), "Cant be empty", Toast.LENGTH_SHORT).show();
            } else {
                Fetch(link_text.getText().toString());
            }
        });
        down.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(link)) {
                Toast.makeText(getContext(), string.download_started, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), string.empty_queue, Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void Fetch(String rj) {
        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(getContext(), style.Theme_AppCompat_DayNight_Dialog_Alert);
        pDialog.setTitle(getString(string.fetchtext));
        pDialog.setMessage(getString(string.searching));
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
                        pDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                Throwable::printStackTrace
        );

        queue.add(stringRequest);
    }
}