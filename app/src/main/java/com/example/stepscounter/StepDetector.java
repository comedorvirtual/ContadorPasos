package com.example.stepscounter;

import android.hardware.SensorEvent;

public class StepDetector {
    private ProcessAccelerometer mAccelProcessing;
    private double[] mAccelResult;

    public enum AccelerometerSignals {
        MAGNITUDE,
        MOV_AVERAGE;

        public static final int count = StepDetector.AccelerometerSignals.values().length;
    }

    public StepDetector() {
        mAccelProcessing = ProcessAccelerometer.getInstance();
        mAccelResult = new double[AccelerometerSignals.count];
    }

    public int detect(SensorEvent event) {
        mAccelProcessing.setEvent(event);
        mAccelResult[0] = mAccelProcessing.calcMagnitudeVector(0);
        mAccelResult[0] = mAccelProcessing.calcExpMovAvg(0);
        mAccelResult[1] = mAccelProcessing.calcMagnitudeVector(1);
        if (mAccelProcessing.stepDetected(1)) {
            return 1;
        }
        return 0;
    }
}
