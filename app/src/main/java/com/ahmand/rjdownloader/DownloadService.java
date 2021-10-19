package com.ahmand.rjdownloader;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadService extends AsyncTask<Void, Void, Void> {
    public Context context;
    public String url;
    DownloadManager downloadmanager;
    ProgressDialog pDialog;

    public DownloadService(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(context);
        pDialog.setTitle(context.getString(R.string.please_wait));
        pDialog.setMessage(context.getString(R.string.downloading));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            downloadFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        pDialog.dismiss();
    }

    void downloadFile() throws IOException {

        URL u = new URL(url);
        FileUtils.copyURLToFile(u, new File(Environment.getExternalStorageDirectory() + "/RadioJavan", url.substring(url.lastIndexOf('/') + 1)));
    }

    public void defaultDownloadManager() {
        downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(url.substring(url.lastIndexOf('/') + 1));
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/RadioJavan", url.substring(url.lastIndexOf('/') + 1))));
        downloadmanager.enqueue(request);
    }
}
