package com.selfservit.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.File;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TimerService extends Service {
    public Timer setTimerIntervel;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TimerTask setTimerIntervelObj;
        // **** TimeReader Interval Timer ***** //
        setTimerIntervel = new Timer();
        setTimerIntervelObj = new TimerTask() {
            public void run() {
                try {
                    timeReader();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        setTimerIntervel.schedule(setTimerIntervelObj, 60000, 60000);
        return START_STICKY;
    }
    private void timeReader()throws Exception {
        String serverTimeObj;
        SimpleDateFormat simpleDateFormat;
        StringBuilder timeObj;
        String currentLine;
        BufferedReader readerObj;
        JSONObject serverDateObj;
        timeObj = new StringBuilder();
        BufferedWriter writerObj;
        File baseDirectory = Environment.getExternalStorageDirectory();
        readerObj = new BufferedReader(new FileReader(new File(baseDirectory, "mservice/time_profile.txt")));
        while ((currentLine = readerObj.readLine()) != null) {
            timeObj.append(currentLine);
        }
        readerObj.close();
        serverDateObj = new JSONObject(timeObj.toString());
        serverTimeObj = serverDateObj.optString("serverDate").toString();

        // ******SERVER TIME ******//
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        //String currentDateandTime = sdf.format(new Date());
        simpleDateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        Date date = simpleDateFormat.parse(serverTimeObj);
        long a = date.getTime() + 60000;
        date.setTime(a);
        serverTimeObj = simpleDateFormat.format(date);
        serverDateObj.put("serverDate", serverTimeObj);
        writerObj = new BufferedWriter(new FileWriter(new File(baseDirectory, "mservice/time_profile.txt")));
        writerObj.write(serverDateObj.toString());
        writerObj.flush();
        writerObj.close();
    }
    /* USER CLEARED APP FROM CACHE */
    @ Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (setTimerIntervel != null) {
            setTimerIntervel.cancel();
        }
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000, restartServicePI);
    }
    /* WHEN THE RAM IS LOW */
    @ Override
    public void onLowMemory() {
        super.onLowMemory();
        if (setTimerIntervel != null) {
            setTimerIntervel.cancel();
        }

        startService(new Intent(getApplicationContext(), TimerService.class));
    }
    /* USER CLICK FORCE STOP IN SETTINGS */
    @ Override
    public void onDestroy() {
        super.onDestroy();
        if (setTimerIntervel != null) {
            setTimerIntervel.cancel();
        }

        startService(new Intent(getApplicationContext(), TimerService.class));
    }
}
