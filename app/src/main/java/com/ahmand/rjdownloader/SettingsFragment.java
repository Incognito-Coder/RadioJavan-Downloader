package com.ahmand.rjdownloader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends  PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs,rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        switch (key){
            case "pref_about":
                MaterialAlertDialogBuilder Dialog = new MaterialAlertDialogBuilder(getContext());
                Dialog.setTitle(R.string.about_title);
                Dialog.setMessage(R.string.about_text);
                Dialog.setNeutralButton("Shayanify", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/Shayanify")));
                    } catch (Exception e) {
                        Snackbar.make(getView(), R.string.telegram_not, Snackbar.LENGTH_SHORT).show();
                    }
                });
                Dialog.setNegativeButton("IC Mods", ((dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/ic_mods")));
                    } catch (Exception e) {
                        Snackbar.make(getView(), R.string.telegram_not, Snackbar.LENGTH_SHORT).show();
                    }
                }));
                Dialog.setCancelable(true);
                Dialog.show();
                break;
            case "pref_help":
                Intent intent = new Intent(getContext(), HelpActivity.class);
                this.startActivity(intent);
                break;
            case "dark_mode":
                Snackbar.make(getView(),R.string.restart,Snackbar.LENGTH_SHORT).setAction("RESTART", v -> {
restart();
                }).show();
                break;
            default:
                break;
        }
        return true;
    }
    public void restart(){
        Intent intent = new Intent(getContext(), SplashActivity.class);
        getContext().startActivity(intent);
        getActivity().finishAffinity();
    }
}
