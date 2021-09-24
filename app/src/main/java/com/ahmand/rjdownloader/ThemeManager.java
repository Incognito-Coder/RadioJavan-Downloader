package com.ahmand.rjdownloader;

import android.content.Context;
import android.preference.PreferenceManager;

public class ThemeManager {

    public void ApplyTheme(Context context) {
        String theme = PreferenceManager.getDefaultSharedPreferences(context).getString("theme", "");
        context.setTheme(GetTheme(theme));
    }

    public int GetTheme(String theme) {
        if (theme.equals("black")) return R.style.Theme_RJDownloader_Dark;
        if (theme.equals("white")) return R.style.Theme_RJDownloader;
        return 0;
    }

    public String GetCurrent(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("theme", "");

    }
}
