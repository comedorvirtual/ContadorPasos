package com.example.stepscounter.Controller;

import android.os.AsyncTask;
import android.util.Log;

public class MqttAsyncTask extends AsyncTask<Integer, String, String> {
    @Override
    protected String doInBackground(Integer... voids) {
        for (int n = 0; n < 2000; n++) {

            Log.d("", "N:" + n);
        }
        return null;
    }


}
