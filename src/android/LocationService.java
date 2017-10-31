package com.selfservit.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.os.SystemClock;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class LocationService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @TargetApi(Build.VERSION_CODES.M)
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationService.MyLocationListener(locationManager);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 200, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 200, locationListener);
        return START_STICKY;
    }
    private class MyLocationListener implements LocationListener {
        public MyLocationListener(LocationManager locationManager) {}

        @ Override
        public void onLocationChanged(Location location) {
            new LocationService.UpdateLocation(Double.toString(location.getLatitude()), Double.toString(location.getLongitude())).execute("");
            if (isConnected()) {
                new LocationService.SendLocation().execute();
            }
        }
        @ Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @ Override
        public void onProviderEnabled(String provider) {}

        @ Override
        public void onProviderDisabled(String provider) {}
    }
    private class SendLocation extends AsyncTask < String,
            Void,
            String > {
        @ Override
        protected String doInBackground(String...urls) {
            String clientID,
                    countryCode,
                    deviceID;
            DocumentBuilderFactory dbfObj;
            DocumentBuilder dbObj;
            Document docObj;
            OutputStreamWriter oStreamObj;
            StringBuilder locationData,
                    userData;
            BufferedReader readerObj;
            BufferedWriter writerObj;
            String currentLine,
                    serverData,
                    requesturl;
            JSONObject userObj;
            HttpURLConnection urlConObj;
            URL requestPath;

            File baseDirectory = Environment.getExternalStorageDirectory();

            try {
				/* GETTING THE LOCATION POINTS TO BE SENT TO THE SERVER */
                locationData = new StringBuilder();
                readerObj = new BufferedReader(new FileReader(new File(baseDirectory, "mservice/MyLocation.txt")));
                while ((currentLine = readerObj.readLine()) != null) {
                    locationData.append(currentLine + "\n");
                    //lastKnownLocation = currentLine + "\n";
                }
                readerObj.close();
                serverData = locationData.toString();
				/* CLEARING THE LOCATION POINTS */
                if (serverData != "") {
                    writerObj = new BufferedWriter(new FileWriter(new File(baseDirectory, "mservice/MyLocation.txt")));
                    writerObj.write("");
                    writerObj.flush();
                    writerObj.close();
                }
				/* GETTING THE USER INFO */
                userData = new StringBuilder();
                readerObj = new BufferedReader(new FileReader(new File(baseDirectory, "mservice/user.txt")));
                while ((currentLine = readerObj.readLine()) != null) {
                    userData.append(currentLine);
                }
                readerObj.close();
                userObj = new JSONObject(userData.toString());
                clientID = userObj.optString("client_id").toString();
                countryCode = userObj.optString("country_code").toString();
                deviceID = userObj.optString("device_id").toString();
                readerObj = new BufferedReader(new FileReader(new File(baseDirectory, "mservice/client_functional_access_package" + "/" + clientID + "/" + countryCode + "/client_functional_access.xml")));
                dbfObj = DocumentBuilderFactory.newInstance();
                dbObj = dbfObj.newDocumentBuilder();
                docObj = dbObj.parse(new InputSource(readerObj));
                docObj.getDocumentElement().normalize();
                requesturl = docObj.getElementsByTagName("protocol_type").item(0).getTextContent() + "//" + docObj.getElementsByTagName("domain_name").item(0).getTextContent() + ":" + docObj.getElementsByTagName("port_no").item(0).getTextContent() + "/common/components/GeoLocation/update_device_location_offline.aspx";

				/* SEND LOCATION  */
                requestPath = new URL(requesturl);
                urlConObj = (HttpURLConnection)requestPath.openConnection();
                urlConObj.setDoOutput(true);
                urlConObj.setRequestMethod("POST");
                urlConObj.setRequestProperty("CONTENT-TYPE", "text/xml");
                urlConObj.connect();
                oStreamObj = new OutputStreamWriter(urlConObj.getOutputStream());
                oStreamObj.write("<location_xml><client_id>" + clientID + "</client_id><country_code>" + countryCode + "</country_code><device_id>" + deviceID + "</device_id><location>" + serverData + "</location></location_xml>");
                oStreamObj.flush();
                oStreamObj.close();
                urlConObj.getResponseCode();
                urlConObj.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class UpdateLocation extends AsyncTask < String,
            Void,
            String > {
        private String objLat,
                objLon;

        public UpdateLocation(String lat, String lon) {
            this.objLat = lat;
            this.objLon = lon;
        }

        @ Override
        protected String doInBackground(String...urls) {
            File appDirectory,
                    baseDirectory = Environment.getExternalStorageDirectory();
            FileWriter fileWriterObj;
            appDirectory = new File(baseDirectory.getAbsolutePath() + "/mservice");
            if (appDirectory.exists()) {
                try {
                    fileWriterObj = new FileWriter(new File(baseDirectory, "mservice/MyLocation.txt"), true);
                    fileWriterObj.write(this.objLat + "," + this.objLon + "," + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "\n");
                    fileWriterObj.flush();
                    fileWriterObj.close();

                    fileWriterObj = new FileWriter(new File(baseDirectory, "mservice/LastKnownLocation.txt"), false);
                    fileWriterObj.write(this.objLat + "," + this.objLon + "," + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                    fileWriterObj.flush();
                    fileWriterObj.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    @ Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
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
        startService(new Intent(getApplicationContext(), LocationService.class));
    }
    /* USER CLICK FORCE STOP IN SETTINGS */
    @ Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(getApplicationContext(), LocationService.class));
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
