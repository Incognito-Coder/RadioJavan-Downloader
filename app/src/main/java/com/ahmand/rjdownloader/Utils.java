package com.ahmand.rjdownloader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.io.File;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Utils {
    String[] urls = {"rjplay.co", "rj.app", "rjapp.app", "rjvan.me", "radiojavan.com"};
    public static String RJ_RESULT;

    public static String convertMillisToString(long durationInMillis) {
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;

        return String.format(Locale.US, "%02d:%02d", minute, second);
    }

    public static boolean isVpnConnectionActive() {
        List<String> networks = new ArrayList<>();

        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp()) {
                    networks.add(networkInterface.getName());
                }
            }
        } catch (Exception ignored) {
        }

        return networks.contains("tun0") || networks.contains("ppp") || networks.contains("pptp");
    }

    public ArrayList<String> GetFiles(String directorypath) {
        ArrayList<String> Myfiles = new ArrayList<>();
        File f = new File(directorypath);
        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length != 0) {
            for (File file : files) Myfiles.add(file.getName());
        }
        return Myfiles;
    }

    public boolean matchClipData(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboardManager.getPrimaryClip();
        CharSequence paste = clip.getItemAt(0).getText();
        if (paste != null) {
            String find = (String) paste;
            String[] match = find.split("https://");
            String[] last = match[1].split("/");
            if (Arrays.asList(urls).contains(last[0])) {
                RJ_RESULT = paste.toString();
                return true;
            }
        }
        return false;
    }
}
