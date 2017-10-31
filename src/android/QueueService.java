package com.selfservit.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class QueueService extends Service {
    public Timer setQueueNewInterval;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        TimerTask setQueueNewIntervalObj;
        Intent serviceIntent = new Intent(getApplicationContext(), mInterfaceReceiver.class);
        serviceIntent.setAction("com.example.checkservice");
        final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                300000, pIntent);
        setQueueNewInterval = new Timer();
        setQueueNewIntervalObj = new TimerTask() {
            public void run() {
                if (isConnected()) {
                    new DespatchQueueNew().execute();
                }
            }
        };
        setQueueNewInterval.schedule(setQueueNewIntervalObj, 2000, 2000);
        return START_STICKY;
    }
    private class DespatchQueueNew extends AsyncTask< String,
            Void,
            String > {
        @ Override
        protected String doInBackground(String...params) {
            File baseDirectory = Environment.getExternalStorageDirectory();
            File processDir = new File(baseDirectory.getAbsolutePath() + "/mservice/database/queue/");
            String currentLine,
                    sendData,
                    fileType,
                    sendFileName,
                    requestFilepath,
                    sendFileBasePath,
                    method,
                    keyValue,
                    subKeyValue,
                    requesturl = "",
                    receiveData = "";
            StringBuilder serverResponseObj,
                    backupFileData;
            BufferedReader readerObj;
            BufferedWriter writerObj;
            File backUpFilePath;
            FileInputStream fileInputStream;
            int bytesRead,
                    bytesAvailable,
                    bufferSize;
            byte[]buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            DataOutputStream dos;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            JSONObject queueObject,
                    backupDataObj;
            URL requestPath;
            HttpURLConnection urlConObj;
            OutputStreamWriter oStreamObj;
            StringBuilder queueObj = new StringBuilder();
            try {
                if (processDir.exists()) {
                    File[]fileList = processDir.listFiles();
                    if (fileList != null && fileList.length >= 1) {
                        Arrays.sort(fileList);
                        File fileIndex = fileList[0];
                        readerObj = new BufferedReader(new FileReader(fileIndex));
                        while ((currentLine = readerObj.readLine()) != null) {
                            queueObj.append(currentLine);
                        }
                        readerObj.close();
                        if (isConnected()) {
							/* FROM REQUEST OBJECT */
                            queueObject = new JSONObject(queueObj.toString());
                            requesturl = queueObject.optString("url").toString();
                            sendData = queueObject.optString("input").toString();
                            fileType = queueObject.optString("type").toString();
                            sendFileBasePath = queueObject.optString("filepath").toString();
                            sendFileName = queueObject.optString("filename").toString();
                            method = queueObject.optString("method").toString();
                            keyValue = queueObject.optString("key").toString();
                            subKeyValue = queueObject.optString("subkey").toString();

							/* UPLOAD FILE TO SERVER */
                            if (method.equals("read")) {
                                backUpFilePath = new File(baseDirectory.getAbsolutePath() + "/mservice/database/" + "bckp_" + keyValue + ".txt");
                                requestPath = new URL(requesturl);
                                urlConObj = (HttpURLConnection)requestPath.openConnection();
                                urlConObj.setDoOutput(true);
                                urlConObj.setRequestMethod("POST");
                                urlConObj.setRequestProperty("CONTENT-TYPE", "application/json");
                                urlConObj.connect();
                                oStreamObj = new OutputStreamWriter(urlConObj.getOutputStream());
                                oStreamObj.write(sendData);
                                oStreamObj.flush();
                                oStreamObj.close();

								/* GET RESPONSE FROM SERVER*/
                                serverResponseObj = new StringBuilder();
                                readerObj = new BufferedReader(new InputStreamReader(urlConObj.getInputStream()));
                                while ((currentLine = readerObj.readLine()) != null) {
                                    serverResponseObj.append(currentLine);
                                }
                                readerObj.close();
                                urlConObj.disconnect();
                                backupFileData = new StringBuilder();
                                try {
                                    if (backUpFilePath.exists()) {
                                        readerObj = new BufferedReader(new FileReader(backUpFilePath));
                                        while ((currentLine = readerObj.readLine()) != null) {
                                            backupFileData.append(currentLine + "\n");
                                        }
                                        readerObj.close();
                                        backupDataObj = new JSONObject(backupFileData.toString());
                                    } else {
                                        backUpFilePath.createNewFile();
                                        backupDataObj = new JSONObject();
                                    }
                                    backupDataObj.put(subKeyValue, new JSONArray(serverResponseObj.toString()));
                                    writerObj = new BufferedWriter(new FileWriter(backUpFilePath));
                                    writerObj.write(backupDataObj.toString());
                                    writerObj.flush();
                                    writerObj.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (fileType.equals("file")) {
                                    requestFilepath = baseDirectory + "/" + sendFileBasePath + "/" + sendFileName;
                                    if (new File(requestFilepath).exists()) {
                                        fileInputStream = new FileInputStream(new File(requestFilepath));
                                        requestPath = new URL((requesturl + "&filename=" + sendFileName).replaceAll(" ", "%20"));
                                        urlConObj = (HttpURLConnection)requestPath.openConnection();
                                        urlConObj.setDoInput(true); // Allow Inputs
                                        urlConObj.setDoOutput(true); // Allow Outputs
                                        urlConObj.setUseCaches(false); // Don't use a Cached Copy
                                        urlConObj.setRequestMethod("POST");
                                        urlConObj.setRequestProperty("Connection", "Keep-Alive");
                                        urlConObj.setRequestProperty("ENCTYPE", "multipart/form-data");
                                        urlConObj.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                                        urlConObj.setRequestProperty("uploaded_file", sendFileName);

                                        dos = new DataOutputStream(urlConObj.getOutputStream());
                                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                                + sendFileName + "\"" + lineEnd);
                                        dos.writeBytes(lineEnd);
                                        // create a buffer of  maximum size
                                        bytesAvailable = fileInputStream.available();

                                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                        buffer = new byte[bufferSize];

                                        // read file and write it into form...
                                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                                        while (bytesRead > 0) {
                                            dos.write(buffer, 0, bufferSize);
                                            bytesAvailable = fileInputStream.available();
                                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                                        }
                                        // send multipart form data necesssary after file data...
                                        dos.writeBytes(lineEnd);
                                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                                        urlConObj.getResponseCode();
                                        //close the streams //
                                        fileInputStream.close();
                                        dos.flush();
                                        dos.close();
                                        serverResponseObj = new StringBuilder();
                                        readerObj = new BufferedReader(new InputStreamReader(urlConObj.getInputStream()));
                                        while ((currentLine = readerObj.readLine()) != null) {
                                            serverResponseObj.append(currentLine + "\n");
                                        }
                                        readerObj.close();
                                        receiveData += "Time:" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\n";
                                        receiveData += "url:" + requestPath + "\n";
                                        receiveData += "------------------\n";
                                        urlConObj.disconnect();
                                    }
                                } else {
									/* SEND JSON DATA TO SERVER*/
                                    requestPath = new URL(requesturl);
                                    urlConObj = (HttpURLConnection)requestPath.openConnection();
                                    urlConObj.setDoOutput(true);
                                    urlConObj.setRequestMethod("POST");
                                    urlConObj.setRequestProperty("CONTENT-TYPE", "application/json");
                                    urlConObj.connect();
                                    oStreamObj = new OutputStreamWriter(urlConObj.getOutputStream());
                                    oStreamObj.write(sendData);
                                    oStreamObj.flush();
                                    oStreamObj.close();

									/* GET RESPONSE FROM SERVER*/
                                    serverResponseObj = new StringBuilder();
                                    readerObj = new BufferedReader(new InputStreamReader(urlConObj.getInputStream()));
                                    while ((currentLine = readerObj.readLine()) != null) {
                                        serverResponseObj.append(currentLine + "\n");
                                    }
                                    readerObj.close();
                                    receiveData += "Time:" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\n";
                                    receiveData += "url:" + requesturl + "\n";
                                    receiveData += "data:" + sendData + "\n";
                                    receiveData += "response:" + serverResponseObj.toString() + "\n";
                                    receiveData += "------------------\n";
                                    urlConObj.disconnect();
                                }
                            }
                            new mInterfaceUtil().logData("mservice/database/process/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt", receiveData);
                            fileIndex.delete ();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    @ Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (setQueueNewInterval != null) {
            setQueueNewInterval.cancel();
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

        if (setQueueNewInterval != null) {
            setQueueNewInterval.cancel();
        }
        startService(new Intent(getApplicationContext(), QueueService.class));
    }
    /* USER CLICK FORCE STOP IN SETTINGS */
    @ Override
    public void onDestroy() {
        super.onDestroy();
        if (setQueueNewInterval != null) {
            setQueueNewInterval.cancel();
        }
        startService(new Intent(getApplicationContext(), QueueService.class));
    }

    private boolean isConnected() {
        ConnectivityManager online = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = online.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

}
