package com.example.stepscounter.Controller;

import com.example.stepscounter.Modelo.MqttMessageWrapper;

public interface MqttHelperListener {
    void displayMessage(String data);
    void saveMessage(MqttMessageWrapper[] data, int size);
}
