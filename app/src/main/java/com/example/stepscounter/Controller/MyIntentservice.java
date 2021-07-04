package com.example.stepscounter.Controller;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class MyIntentservice extends IntentService {

    public MyIntentservice(){
        super("MyIntentservice");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
