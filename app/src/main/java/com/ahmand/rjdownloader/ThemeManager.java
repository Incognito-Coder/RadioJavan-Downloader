package com.ahmand.rjdownloader;

import android.content.Context;
import android.preference.PreferenceManager;

public class ThemeManager {

    public void ApplyTheme(Context context) {
        Boolean theme = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_mode",false);
        context.setTheme(SetDark(theme));
    }

    public int SetDark(Boolean dark) {
        if (dark) return R.style.Theme_RJDownloader_Dark;
        return R.style.Theme_RJDownloader;
    }
}
