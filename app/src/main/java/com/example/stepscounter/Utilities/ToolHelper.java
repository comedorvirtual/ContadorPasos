package com.example.stepscounter.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.stepscounter.MainActivity;
import com.example.stepscounter.Modelo.IoTMessage;
import com.example.stepscounter.Modelo.MqttMessageWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class ToolHelper {
    public final static String PREF_PUBLISH_END = "publish_end";
    public final static String PREF_PUBLISH_BEGIN = "publish_begin";
    public final static String PREFS_NAME = "app_settings";

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static ArrayList<MqttMessageWrapper> getData(int size) {
        ArrayList<MqttMessageWrapper> lst = new ArrayList<>();
        try {

            IoTMessage loc;
            MqttMessageWrapper mqttMessageWrapper;
            for (int i = 0; i < size; i++) {
                loc = new IoTMessage();
                loc.setId(i);
                loc.setLatitude(-15.893045);
                loc.setLongitude(-75.054245);
                loc.setSpeed(34);
                loc.setStatus(1);

                mqttMessageWrapper = new MqttMessageWrapper();
                mqttMessageWrapper.setOrderSend(i);
                mqttMessageWrapper.setUid(MainActivity.CLIENT_ID);
                lst.add(mqttMessageWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lst;
    }

    public static void setPublishEnd(Context context, int msgCounter) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_PUBLISH_END, msgCounter);
        editor.commit();
    }

    public static int getPublishEnd(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_PUBLISH_END, -1);
    }

    public static void setPublishBegin(Context context, String datetime) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_PUBLISH_BEGIN, datetime);
        editor.commit();
    }

    public static String getPublishBegin(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PREF_PUBLISH_BEGIN, "");
    }

    public static String getDateTime(){

        long currentDateTime = System.currentTimeMillis();
        Date currentDate = new Date(currentDateTime);
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        return df.format(currentDate);
    }
}
