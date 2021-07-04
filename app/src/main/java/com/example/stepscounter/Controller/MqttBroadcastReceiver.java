package com.example.stepscounter.Controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MqttBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_MESSAGE = "com.mqtt.message";
    public static final String DISPLAY_RESULT = "com.mqtt.result";
    private final MainActivityListener mainActivityListener;

    public MqttBroadcastReceiver(MainActivityListener mainActivityListener) {
        this.mainActivityListener = mainActivityListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final String result = intent.getStringExtra(DISPLAY_RESULT);
        if (action.equals(ACTION_MESSAGE)) {
            mainActivityListener.display(result);
        }

    }
}
