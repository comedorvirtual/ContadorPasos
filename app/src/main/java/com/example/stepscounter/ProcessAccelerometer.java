package com.example.stepscounter;

import android.hardware.SensorEvent;

public class ProcessAccelerometer {
    final float ALPHA = 0.1f;
    private static ProcessAccelerometer instancia = null;
    private static final int INACTIVE_PERIODS = 12;
    public static final float THRESH_INIT_VALUE = 12.72f;
    private int mInactiveCounter = 0;
    public boolean isActiveCounter = true;
    private static double mThresholdValue = THRESH_INIT_VALUE;
    private double[] mAccelValues = new double[StepDetector.AccelerometerSignals.count];
    private double[] mAccelLastValues = new double[StepDetector.AccelerometerSignals.count];

    public static ProcessAccelerometer getInstance(){
        if(instancia == null) return new ProcessAccelerometer();
        return instancia;
    }
    private SensorEvent mEvent;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];

    public void setEvent(SensorEvent e) {
        mEvent = e;
    }

    public double calcMagnitudeVector(int i) {
        linear_acceleration[0] = mEvent.values[0] - gravity[0];
        linear_acceleration[1] = mEvent.values[1] - gravity[1];
        linear_acceleration[2] = mEvent.values[2] - gravity[2];

        mAccelValues[i] = Math.sqrt(
                linear_acceleration[0] * linear_acceleration[0] +
                        linear_acceleration[1] * linear_acceleration[1] +
                        linear_acceleration[2] * linear_acceleration[2]);
        return mAccelValues[i];
    }

    public double calcExpMovAvg(int i) {
        final double alpha = 0.1;
        mAccelValues[i] = alpha * mAccelValues[i] + (1 - alpha) * mAccelLastValues[i];
        mAccelLastValues[i] = mAccelValues[i];
        return mAccelValues[i];
    }

    public boolean stepDetected(int i) {
        if (mInactiveCounter == INACTIVE_PERIODS) {
            mInactiveCounter = 0;
            if (!isActiveCounter)
                isActiveCounter = true;
        }
        if (mAccelValues[i] > mThresholdValue) {
            if (isActiveCounter) {
                mInactiveCounter = 0;
                isActiveCounter = false;
                return true;
            }
        }
        ++mInactiveCounter;
        return false;
    }


    private float[] highPass(float x, float y, float z)
    {
        float[] filteredValues = new float[3];
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;
        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];
        return filteredValues;
    }


    private float[] lowPassFilter(float[] input, float[] prev) {

        if(input == null || prev == null) {
            return null;
        }
        for (int i=0; i< input.length; i++) {
            prev[i] = prev[i] + ALPHA * (input[i] - prev[i]);
        }
        return prev;
    }

}
