package com.ahmand.rjdownloader;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;

public class DownloadsFragment extends Fragment {
    ListView lv_files;

    public DownloadsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_downloads, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lv_files = view.findViewById(R.id.lv_files);
        try {
            ArrayList<String> filesinfolder;
            Utils files = new Utils();
            filesinfolder = files.GetFiles(Environment.getExternalStorageDirectory() + "/RadioJavan");
            ArrayAdapter<String> adapter
                    = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_list_item_1,
                    filesinfolder);
            lv_files.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lv_files.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            AlertDialog.Builder Dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);
            Dialog.setTitle(R.string.delete);
            Dialog.setMessage(getString(R.string.ask_del, selectedItem));
            Dialog.setPositiveButton(R.string.yes, (dialog, which) -> {
                File file = new File(Environment.getExternalStorageDirectory() + "/RadioJavan/" + selectedItem);
                if (file.delete()) {
                    Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                }
            });
            Dialog.setNegativeButton(R.string.no, null);
            Dialog.show();
        });
    }

}