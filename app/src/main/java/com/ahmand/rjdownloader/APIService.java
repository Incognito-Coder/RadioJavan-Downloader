package com.ahmand.rjdownloader;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

public class APIService {
    public Context context;
    private ResultListener ResultListener;
    public  String link;
    public  String title;
    public  String mime;

    public APIService(Context context) {
        this.context = context;

    }

    public void Fetch(String rj, ImageView thumbnail,ResultListener ResultListener) {
        this.ResultListener =ResultListener;
        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(context);
        pDialog.setTitle(context.getString(R.string.fetchtext));
        pDialog.setMessage(context.getString(R.string.searching));
        pDialog.setCancelable(false);
        pDialog.show();
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://mr-alireza.ir/RJ/rj.php?link=" + rj,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        link = json.getString("result");
                        title = json.getString("title");
                        mime = json.getString("type");
                        Glide.with(context).load(json.getString("photo")).into(thumbnail);
                        pDialog.dismiss();
                        ResultListener.onOK();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                Throwable::printStackTrace
        );

        queue.add(stringRequest);
    }

    public interface ResultListener {
        void onOK();
    }
}

