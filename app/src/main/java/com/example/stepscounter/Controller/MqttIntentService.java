package com.example.stepscounter.Controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.stepscounter.Modelo.MqttMessageWrapper;
import com.example.stepscounter.Utilities.FileHelper;
import com.example.stepscounter.Utilities.ToolHelper;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class MqttIntentService extends IntentService
        implements MqttHelperListener {

    public static final String TAG = "MqttIntentService";
    public static final String TOPIC = "com.mqtt.topic";
    public static final String QOS = "com.mqtt.qos";
    public static final String DATA = "com.mqtt.data";
    public static final String DELAY = "com.mqtt.delay";

    public static final String ACTION_START = "com.mqtt.service.start";
    public static final String ACTION_STOP = "com.mqtt.service.stop";
    public static final String ACTION_PUBLISH = "com.mqtt.service.publish";
    public static final String ACTION_SAVE = "com.mqtt.service.save";
    public static final String ACTION_STATE = "com.mqtt.service.state";

    private MqttHelper mqttHelper;
    private Context context;

    public MqttIntentService(){
        super("MqttIntentService");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Saco Accion");
        String topic = intent.getStringExtra(TOPIC);
        int qos = intent.getIntExtra(QOS, 0);
        int delay = intent.getIntExtra(DELAY, 0);
        int size = intent.getIntExtra(DATA, 0);

        if (ACTION_START.endsWith(action)) {
            Log.d(TAG, "START");
            MqttHelper.setInitParameters(topic, qos);
            MqttHelper.getInstance().setMqttHelperListener(this);
        } else if (ACTION_STOP.equals(action)) {
            mqttHelper.close();
        } else if (ACTION_PUBLISH.equals(action)) {
            mqttHelper = MqttHelper.getInstance();
            //  ArrayList<MqttMessageWrapper> data = ToolHelper.getData(size);
            // mqttHelper.publishBatch(data, delay , stepCount, calorias);
            Log.d(TAG, "PUBLISH");
            ArrayList<MqttMessageWrapper> data = ToolHelper.getData(size);
            mqttHelper.publishBatch(data, delay);

        } else if (ACTION_SAVE.equals(action)) {
            mqttHelper = MqttHelper.getInstance();
            mqttHelper.save();
        } else if (ACTION_STATE.equals(action)) {
            Log.d(TAG, "Message: ");
            mqttHelper = MqttHelper.getInstance();

        } else {
            Log.d(TAG, "Message: else ");
        }
    }

    @Override
    public void displayMessage(String data) {
        display(data);
    }

    @Override
    public void saveMessage(MqttMessageWrapper[] data, int size) {
        FileHelper fileHelper = new FileHelper(context);
        String msg = fileHelper.save(data, size);
        display(msg);
    }

    private void display(String result) {
        Intent intent = new Intent(MqttBroadcastReceiver.ACTION_MESSAGE);
        intent.putExtra(MqttBroadcastReceiver.DISPLAY_RESULT, result);
        sendBroadcast(intent);
    }

}
