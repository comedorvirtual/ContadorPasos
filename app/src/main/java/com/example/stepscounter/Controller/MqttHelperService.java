package com.example.stepscounter.Controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.stepscounter.MainActivity;
import com.example.stepscounter.Modelo.MqttMessageWrapper;
import com.example.stepscounter.R;
import com.example.stepscounter.Utilities.FileHelper;
import com.example.stepscounter.Utilities.ToolHelper;

import java.util.ArrayList;

import androidx.core.app.NotificationCompat;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MqttHelperService extends Service implements MqttHelperListener {
    private final static String TAG = "MqttService";
    private final static int NOTIFICATION_ID = 123456;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String TOPIC = "com.mqtt.topic";
    public static final String QOS = "com.mqtt.qos";
    public static final String DATA = "com.mqtt.data";
    public static final String DELAY = "com.mqtt.delay";
    public static final String ACTION_START = "com.mqtt.service.start";
    public static final String ACTION_STOP = "com.mqtt.service.stop";
    public static final String ACTION_PUBLISH = "com.mqtt.service.publish";
    public static final String ACTION_SAVE = "com.mqtt.service.save";

    private Thread workerThread = null;
    private MqttHelper mqttHelper = null;
    private Context context = null;
    private PowerManager.WakeLock wakeLock = null;

    @Override
    public void displayMessage(String data) {
        display(data);
    }

    @Override
    public void saveMessage(MqttMessageWrapper[] data, int size) {
        FileHelper fileHelper = new FileHelper(context);
        String msg = fileHelper.save(data, size);
        display(msg);
    }

    private void display(String result) {
        Intent intent = new Intent(MqttBroadcastReceiver.ACTION_MESSAGE);
        intent.putExtra(MqttBroadcastReceiver.DISPLAY_RESULT, result);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        context = getApplicationContext();
        ignoreBatteryOptimization();

        String action = intent.getAction();
        String topic = intent.getStringExtra(TOPIC);
        int qos = intent.getIntExtra(QOS, 0);
        final int delay = intent.getIntExtra(DELAY, 0);
        final int size = intent.getIntExtra(DATA, 0);

        if (ACTION_START.equals(action)) {
            MqttHelper.setInitParameters(topic, qos);
            MqttHelper.getInstance().setMqttHelperListener(this);
        } else if (ACTION_STOP.equals(action)) {
            //mqttHelper.close();
            stopForegroundService();
        } else if (ACTION_PUBLISH.equals(action)) {
            //startForegroundService();
            mqttHelper = MqttHelper.getInstance();

            // For each start request, send a message to start a job and deliver the
            // start ID so we know which request we're stopping when we finish the job
//            Message msg = serviceHandler.obtainMessage();
//            msg.arg1 = startId;
//            serviceHandler.sendMessage(msg);

            if (workerThread == null || !workerThread.isAlive()) {
                workerThread = new Thread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "workerThread: ");
                        ArrayList<MqttMessageWrapper> data = ToolHelper.getData(size);
                        mqttHelper.publishBatch(data, delay);
                        display("Finished");
                        String datetime2 = ToolHelper.getDateTime();
                        ToolHelper.setPublishBegin(getApplicationContext(), "Finish at: " + datetime2);
                    }
                });
                workerThread.start();
            }


        } else if (ACTION_SAVE.equals(action)) {
            mqttHelper = MqttHelper.getInstance();
            mqttHelper.save();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        stopForegroundService();
    }

    private void startForegroundService() {

        wakeLock.acquire();

        createNotificationChannel();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        builder.setContentTitle("MqttService is running")
                .setContentText("Touch for open the application")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSound(null)
                .build();

        Notification notification = builder.build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void stopForegroundService() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock2 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag2");
        if (wakeLock2.isHeld())
            wakeLock2.release();
        stopForeground(true);
        stopSelf();

    }


    private void ignoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + packageName));
                context.startActivity(intent);
            }
        }
    }


}
