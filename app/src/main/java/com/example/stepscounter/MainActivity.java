package com.example.stepscounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private int mStepCount = 0;
    private Sensor mAccelerometer;
    private TextView mStepSensorInfo;
    private StepDetector mDetector;
    private long startTime;
    private float[] prev = {0f,0f,0f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStepSensorInfo = findViewById(R.id.stepCounter_layout);

        mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mDetector = new StepDetector();

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
            ++mStepCount;
            mStepSensorInfo.setText(String.valueOf(mStepCount));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




}