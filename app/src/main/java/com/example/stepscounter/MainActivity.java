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

import com.example.stepscounter.Controller.MqttBroadcastReceiver;
import com.example.stepscounter.Controller.MqttHelper;
import com.example.stepscounter.Controller.MqttHelperService;
import com.example.stepscounter.Controller.MqttIntentService;
import com.example.stepscounter.Modelo.MqttMessageWrapper;
import com.example.stepscounter.Utilities.ToolHelper;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final static String TAG = "MainActivity";
    public final static String CLIENT_ID = UUID.randomUUID().toString();
    private SensorManager mSensorManager;
    private int stepCount = 0;
    private int calorias = 0;
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
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Conectado");
            iniciarEnvio();
            publicar();
            ArrayList<MqttMessageWrapper> data = ToolHelper.getData(size);
            mqttHelper.publishBatch(data, delay , stepCount, calorias);


        });

        stop.setOnClickListener(v -> {
            stop();
            mSensorManager.unregisterListener(this,mAccelerometer);
        });

        subir.setOnClickListener(v -> {


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
        if (step != 0) {
            ++stepCount;
            calorias =getCaloria(stepCount);
            mStepSensorInfo.setText(String.valueOf(stepCount));
            mCalorias.setText(String.valueOf(calorias));
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

        Intent intent = new Intent(MainActivity.this, MqttHelperService.class);
        intent.putExtra(MqttIntentService.TOPIC, topic);
        intent.putExtra(MqttIntentService.QOS, qos);
        intent.putExtra(MqttIntentService.DELAY, delay);
        intent.putExtra(MqttIntentService.DATA, size);
        intent.putExtra(MqttIntentService.CALORIAS, calorias);
        intent.putExtra(MqttIntentService.STEPCOUNT, stepCount);
        intent.setAction(action);
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
}