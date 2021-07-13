package com.example.stepscounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.stepscounter.Controller.MainActivityListener;
import com.example.stepscounter.Controller.MqttBroadcastReceiver;
import com.example.stepscounter.Controller.MqttHelper;
import com.example.stepscounter.Controller.MqttHelperService;
import com.example.stepscounter.Controller.MqttIntentService;
import com.example.stepscounter.Modelo.MqttMessageWrapper;
import com.example.stepscounter.Utilities.ToolHelper;
import com.example.stepscounter.Utilities.Util;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener, MainActivityListener {
    private final static String TAG = "MainActivity";
    public final static String CLIENT_ID = UUID.randomUUID().toString();
    public static Util datos;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextView mStepSensorInfo;
    private TextView mCalorias;
    private StepDetector mDetector;
    private MqttBroadcastReceiver mqttBroadcastReceiver;
    private int size = 600;
    private int delay=2;
    private int qos=0;
    private MqttHelper mqttHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttHelper = MqttHelper.getInstance();
        mStepSensorInfo = findViewById(R.id.stepCounter_layout);
        mCalorias = findViewById(R.id.calorias_layout);
        Button start = findViewById(R.id.button_play);
        Button stop = findViewById(R.id.button_stop);
        Button subir = findViewById(R.id.button_subir);
        mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mDetector = new StepDetector();

        start.setOnClickListener(v -> {
            iniciarEnvio();
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);

        });

        stop.setOnClickListener(v -> {
            stop();
            mSensorManager.unregisterListener(this,mAccelerometer);
        });

        subir.setOnClickListener(v -> {
            ArrayList<MqttMessageWrapper> data = ToolHelper.getData(size);
            mqttHelper.publishBatch(data, delay);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        long startTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this,mAccelerometer);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int step = mDetector.detect(event);
        int pasofuturo=1;
        if (step != 0) {
            pasofuturo = Util.pasos+1;
            ++Util.pasos;
            Util.calorias =getCaloria(Util.pasos);
            mStepSensorInfo.setText(String.valueOf(Util.pasos));
            mCalorias.setText(String.valueOf(Util.calorias));
        }
        if(Util.pasos == pasofuturo)
        {
            ArrayList<MqttMessageWrapper> data = ToolHelper.getData(size);
            mqttHelper.publishBatch(data, delay);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getCaloria(int mStepCount){
        return mStepCount/33;
    }

    private void initMqttService(String action) {
        String topic = "v1/devices/me/telemetry";
        int qos = 0;
        int delay = 2;
        int size = 600;

        Log.d(TAG, "initMqttService");
        Intent intent = new Intent(MainActivity.this, MqttHelperService.class);
        intent.putExtra(MqttIntentService.TOPIC, topic);
        intent.putExtra(MqttIntentService.QOS, qos);
        intent.putExtra(MqttIntentService.DELAY, delay);
        intent.putExtra(MqttIntentService.DATA, size);
        intent.setAction(action);
        Log.d(TAG, "Por enviar");
        startService(intent);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }*/
    }
    public void iniciarEnvio(){
        try {
            initMqttService(MqttIntentService.ACTION_START);
        } catch (Exception e) {
            Log.d(TAG, "error envio ");
            e.printStackTrace();
        }
    }
    public void publicar(){
        try {
            initMqttService(MqttIntentService.ACTION_PUBLISH);
            String datetime2 = ToolHelper.getDateTime();
            ToolHelper.setPublishBegin(getApplicationContext(), datetime2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(){
        try {
            initMqttService(MqttIntentService.ACTION_SAVE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stop(){
        try {
            initMqttService(MqttIntentService.ACTION_STOP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display(String data) {

    }
}