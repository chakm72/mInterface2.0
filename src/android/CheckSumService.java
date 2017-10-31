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

public class CheckSumService extends Service {
    public Timer setChecksumTimerInterval;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TimerTask setChecksumTimerIntervalObj;

        // *** CheckSum value Indicator Timer *** //
        setChecksumTimerInterval = new Timer();
        setChecksumTimerIntervalObj = new TimerTask() {
            public void run() {
                try {
                    if (isConnected()) {
                        new CheckSumIndicatorResult().execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        setChecksumTimerInterval.schedule(setChecksumTimerIntervalObj, 180000, 180000);

		        return START_STICKY;
    }
    private class CheckSumIndicatorResult extends AsyncTask < String,
            Void,
            String > {
        @ Override
        protected String doInBackground(String...strings) {
            File baseDirectory,
                    checksumFile,
                    userProfileFile;

            BufferedReader checksumFileReader,
                    userProfileFileReader,
                    responseReader;

            String currentLine,
                    checksumFileData,
                    checksumValue,
                    refreshIndValue,
                    userProfileFileData,
                    serverResponseData;

            JSONObject checksumObj,
                    userProfileObj,
                    serverResponseObj;

            JSONArray serverResponseArray;

            URL requestPath;

            HttpURLConnection urlConObj;

            OutputStreamWriter oStreamObj;

            FileWriter writerObj;

            try {

                baseDirectory = Environment.getExternalStorageDirectory();
                checksumFileData = "";
                checksumValue = "";
                refreshIndValue = "";
                userProfileFileData = "";
                serverResponseData = "";

				/* READ THE CHECKSUM VALUE AND GET CHECKSUM AND REFRESH INDICATOR VALUE*/
                checksumFile = new File(baseDirectory, "/mservice/database/checksum_value.txt");
                if (checksumFile.exists()) {
                    checksumFileReader = new BufferedReader(new FileReader(checksumFile));
                    while ((currentLine = checksumFileReader.readLine()) != null) {
                        checksumFileData += currentLine;
                    }
                    checksumFileReader.close();
                    checksumObj = new JSONObject(checksumFileData);
                    checksumValue = checksumObj.optString("checksum_value").toString();
                    refreshIndValue = checksumObj.optString("refresh_ind").toString();
                }

                if (refreshIndValue.matches("") || refreshIndValue.matches("false")) {

					/* READ THE USER PROFILE VALUE */
                    userProfileFile = new File(baseDirectory, "/mservice/user_profile.txt");
                    if (userProfileFile.exists()) {
                        userProfileFileReader = new BufferedReader(new FileReader(userProfileFile));
                        while ((currentLine = userProfileFileReader.readLine()) != null) {
                            userProfileFileData += currentLine;
                        }
                        userProfileFileReader.close();
                        userProfileObj = new JSONObject(userProfileFileData);
                        userProfileObj = userProfileObj.optJSONObject("login_profile");

						/* SEND VALIDATE CHECKSUM REQUEST TO SERVER */
                        requestPath = new URL(userProfileObj.optString("protocol").toString() + "//" + userProfileObj.optString("domain_name").toString() + ":" + userProfileObj.optString("portno").toString() + "/JSONServiceEndpoint.aspx?appName=common_modules&serviceName=retrieve_listof_values_for_searchcondition&path=context/outputparam");
                        urlConObj = (HttpURLConnection)requestPath.openConnection();
                        urlConObj.setDoOutput(true);
                        urlConObj.setRequestMethod("POST");
                        urlConObj.setRequestProperty("CONTENT-TYPE", "application/json");
                        urlConObj.connect();

                        oStreamObj = new OutputStreamWriter(urlConObj.getOutputStream());
                        oStreamObj.write("{\"context\":{\"sessionId\":" + "\"" + userProfileObj.optString("guid_val").toString() + "\"" + ",\"userId\":" + "\"" + userProfileObj.optString("user_id").toString() + "\"" + ",\"client_id\":" + "\"" + userProfileObj.optString("client_id").toString() + "\"" + ",\"locale_id\":" + "\"" + userProfileObj.optString("locale_id").toString() + "\"" + ",\"country_code\":" + "\"" + userProfileObj.optString("country_code").toString() + "\"" + ",\"inputparam\":{\"p_inputparam_xml\":\"<inputparam><lov_code_type>VALIDATE_CHECKSUM</lov_code_type><search_field_1>" + checksumValue + "</search_field_1><search_field_2>" + userProfileObj.optString("emp_id").toString() + "</search_field_2><search_field_3>MOBILE</search_field_3></inputparam>\"}}}");
                        oStreamObj.flush();
                        oStreamObj.close();

						/* READ THE SERVER RESPONSE AND WRITE TO THE FILE */
                        responseReader = new BufferedReader(new InputStreamReader(urlConObj.getInputStream()));
                        while ((currentLine = responseReader.readLine()) != null) {
                            serverResponseData += currentLine;
                        }
                        responseReader.close();
                        urlConObj.disconnect();

                        serverResponseArray = new JSONArray(serverResponseData);
                        serverResponseObj = serverResponseArray.optJSONObject(0);
                        writerObj = new FileWriter(checksumFile);
                        writerObj.write(serverResponseObj.toString());
                        writerObj.flush();
                        writerObj.close();
                        String date,
                                hour,
                                minute;
                        date = serverResponseObj.optString("serverDate").toString();
                        hour = serverResponseObj.optString("serverHour").toString();
                        minute = serverResponseObj.optString("serverMinute").toString();
                        new mInterfaceUtil().refreshTimeProfile(date, hour, minute);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    /* USER CLEARED APP FROM CACHE */
    @ Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (setChecksumTimerInterval != null) {
            setChecksumTimerInterval.cancel();
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
        if (setChecksumTimerInterval != null) {
            setChecksumTimerInterval.cancel();
        }
        startService(new Intent(getApplicationContext(), CheckSumService.class));
    }
    /* USER CLICK FORCE STOP IN SETTINGS */
    @ Override
    public void onDestroy() {
        super.onDestroy();
        if (setChecksumTimerInterval != null) {
            setChecksumTimerInterval.cancel();
        }
        startService(new Intent(getApplicationContext(), CheckSumService.class));
    }
    public boolean isConnected() {
        ConnectivityManager online = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = online.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
