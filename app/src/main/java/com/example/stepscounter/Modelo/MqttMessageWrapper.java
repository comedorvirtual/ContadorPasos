package com.example.stepscounter.Modelo;

import java.io.Serializable;

public class MqttMessageWrapper implements Serializable {
    private int orderSend;
    private int orderArrive;
    private Long timeInit;
    private Long timeEnd;
    private String uid;
    private byte[] payload;

    public Long getTimeInit() {
        return timeInit;
    }

    public void setTimeInit(Long timeInit) {
        this.timeInit = timeInit;
    }

    public Long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getOrderSend() {
        return orderSend;
    }

    public void setOrderSend(int orderSend) {
        this.orderSend = orderSend;
    }

    public int getOrderArrive() {
        return orderArrive;
    }

    public void setOrderArrive(int orderArrive) {
        this.orderArrive = orderArrive;
    }
}
