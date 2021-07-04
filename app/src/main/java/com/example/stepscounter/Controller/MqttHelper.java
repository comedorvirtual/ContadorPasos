package com.example.stepscounter.Controller;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.stepscounter.Modelo.MqttMessageWrapper;
import com.example.stepscounter.Utilities.ToolHelper;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

public class MqttHelper implements MqttCallback {

    private MqttHelperListener mqttHelperListener;
    private final static String TAG = "MqttHelper";
    private final static String HOST = "tcp://demo.thingsboard.io:1883";
    private final static String USERNAME = "probando123";
    //private final static String PASSWORD = "probando";
    private MqttAsyncClient mqttAndroidClient;
    private static MqttHelper instance;
    private static String TOPIC = "v1/devices/me/telemetry";
    private static int QOS = -1;

    private final int MAX_SIZE = 1000000;
    private MqttMessageWrapper[] mqttMessageWrapperArray = new MqttMessageWrapper[MAX_SIZE];
    private static int COUNTER = 0;
    //{clientId:"allison",userName:"davidjose123456"}

    public static MqttHelper getInstance() {
        if (instance == null) {
            instance = new MqttHelper();
            Log.d(TAG, "starting MqttHelper ... ");
        }
        return instance;
    }

    private MqttHelper() {
        if (TOPIC.trim() == "") try {
            throw new Exception("Topic was not defined");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (QOS < 0) try {
            throw new Exception("QOS was not defined");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String clientId = MqttAsyncClient.generateClientId();
            mqttAndroidClient = new MqttAsyncClient(HOST, clientId, new MemoryPersistence());
            connect();
            mqttAndroidClient.setCallback(this);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public static void setInitParameters(String topic, int qos) {
        TOPIC = topic;
        QOS = qos;
    }

    public void setMqttHelperListener(MqttHelperListener mqttHelperListener) {
        this.mqttHelperListener = mqttHelperListener;
    }

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {

            MqttMessageWrapper msg = (MqttMessageWrapper) ToolHelper.deserialize(message.getPayload());
            msg.setTimeEnd(System.currentTimeMillis());
            msg.setOrderArrive(COUNTER);
            mqttMessageWrapperArray[COUNTER] = msg;
            String msg2 = COUNTER + ":" + msg.getOrderSend() + ":" + msg.getTimeInit() + ":" + msg.getTimeEnd();// + ":" + (new String(message.getPayload()));
            mqttHelperListener.displayMessage(msg2);
            COUNTER++;
            Log.d(TAG, msg2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    private void connect() {
        try {

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(USERNAME);
            //mqttConnectOptions.setPassword(PASSWORD.toCharArray());
            mqttConnectOptions.setMaxInflight(10);

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "connect to: onSuccess");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    //subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect to: " + exception.toString());
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }

    }

    public void publishBatch(List<MqttMessageWrapper> messages, int delay, int pasos, int calorias) {

        try {
            mqttMessageWrapperArray = new MqttMessageWrapper[MAX_SIZE];
            int _delay = delay * 1000;
            byte[] messageByte;
            MqttMessage mqttMessage;
            COUNTER = 0;
            int MESSAGE_ID2 = 0;

            while (MESSAGE_ID2 < messages.size()) {

                if (mqttAndroidClient.isConnected()) {

                    long timestamp = System.currentTimeMillis();
                    MqttMessageWrapper msg = messages.get(MESSAGE_ID2);
                    msg.setTimeInit(timestamp);

                    Random rand = new Random();
                    int temp = rand.nextInt(100);

                    String jsonString = "{\"N° Pasos\":"+pasos+"}";
                    String jsonString2 = "{\"Calorias\":"+calorias+"}";
                    JSONObject json = new JSONObject(jsonString);
                    JSONObject json2 = new JSONObject(jsonString2);
                    byte[] objAsBytes = json.toString().getBytes("UTF-8");
                    byte[] objAsBytes2 = json2.toString().getBytes("UTF-8");
                   // "v1/devices/me/telemetry"
                    mqttAndroidClient.publish(TOPIC,objAsBytes,1,true);
                    mqttAndroidClient.publish(TOPIC,objAsBytes2,1,true);
                    mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(objAsBytes);
                    mqttMessage.setQos(QOS);
                    MESSAGE_ID2++;

                    Log.d(TAG, "Temperatura:"+temp);
                    //Toast.makeText(,"Temperatura:"+temp,Toast.LENGTH_SHORT).show();

                }else {
                    mqttAndroidClient.connect();
                    Log.d(TAG, " is running:");
                }

                Thread.sleep(delay);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    public void publishBatch(List<MqttMessageWrapper> messages, int delay) {

        try {
            mqttMessageWrapperArray = new MqttMessageWrapper[MAX_SIZE];
            int _delay = delay * 1000;
            byte[] messageByte;
            MqttMessage mqttMessage;
            COUNTER = 0;
            int MESSAGE_ID2 = 0;

            while (MESSAGE_ID2 < messages.size()) {


                if (mqttAndroidClient.isConnected()) {

                    long timestamp = System.currentTimeMillis();
                    MqttMessageWrapper msg = messages.get(MESSAGE_ID2);
                    msg.setTimeInit(timestamp);

                    Random rand = new Random();
                    int temp = rand.nextInt(100);

                    String jsonString = "{\"N° Pasos\":"+temp+"}";
                    JSONObject json = new JSONObject(jsonString);
                    byte[] objAsBytes = json.toString().getBytes("UTF-8");
                    // "v1/devices/me/telemetry"
                    mqttAndroidClient.publish(TOPIC,objAsBytes,1,true);
                    mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(objAsBytes);
                    mqttMessage.setQos(QOS);
                    MESSAGE_ID2++;

                    Log.d(TAG, "Temperatura:"+temp);
                    //Toast.makeText(,"Temperatura:"+temp,Toast.LENGTH_SHORT).show();

                }else {
                    mqttAndroidClient.connect();
                    Log.d(TAG, " is running:");
                }

                Thread.sleep(_delay);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    private final IMqttActionListener publisher_IMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "Publisher_IMqttActionListener: onSuccess");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "Publisher_IMqttActionListener: onFailure");
        }
    };

    private final IMqttActionListener subscriber_IMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "Subscriber_IMqttActionListener: onSuccess");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "Subscriber_IMqttActionListener: onFailure");
        }
    };

    public void close() {
        try {
            mqttAndroidClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            mqttHelperListener.saveMessage(mqttMessageWrapperArray, COUNTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
