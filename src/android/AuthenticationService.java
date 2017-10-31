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

public class AuthenticationService extends Service {
	public Timer setProcessInterval;


	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TimerTask setProcessIntervalObj;
		/* AUTHENTICATION PROCESS */
		setProcessInterval = new Timer();
		setProcessIntervalObj = new TimerTask() {
			public void run() {
				if (isConnected()) {
					try {
						if (new File(Environment.getExternalStorageDirectory(), "mservice/auth_indication.txt").exists()) {
							String currentLine;
							BufferedReader authFileReader = new BufferedReader(new FileReader(new File(Environment.getExternalStorageDirectory(), "/mservice/auth_indication.txt")));
							StringBuilder authFileBuilder = new StringBuilder();
							while ((currentLine = authFileReader.readLine()) != null) {
								authFileBuilder.append(currentLine);
							}
							authFileReader.close();
							JSONObject authFileObj = new JSONObject(authFileBuilder.toString());
							long lastModifiedTime = new File(Environment.getExternalStorageDirectory(), "mservice/auth_indication.txt").lastModified();
							Date date = new Date();
							date.setTime(lastModifiedTime);
							String lastModifiedDate = new SimpleDateFormat("yyyyMMdd").format(date);
							if (authFileObj.optString("validDevice").toString().equals("nostatus") || !lastModifiedDate.equals(new SimpleDateFormat("yyyyMMdd").format(new Date()))) {
								new AuthenticationRefresh().execute();
							}
						} else {
							new AuthenticationRefresh().execute();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		setProcessInterval.schedule(setProcessIntervalObj, 600000, 600000);
		return START_STICKY;
	}
	/* AUTHENICATION PROCESS */
	private class AuthenticationRefresh extends AsyncTask < String,
	Void,
	String > {

		 @ Override
		protected String doInBackground(String...strings) {
			BufferedReader userProfileFileReader,
			responseReaderObj,
			readerObj;
			JSONObject userProfileObj,
			responseDataObj,
			userDataObj;
			String currentLine,
			requestPathURL;
			File baseDirectory = Environment.getExternalStorageDirectory();
			StringBuilder userProfileFileData,
			serverResponseObj,
			userData;
			URL requestAuthenticationPath,
			requestValidateDevicePath;
			HttpURLConnection urlConObj;
			OutputStreamWriter oStreamObj;
			BufferedWriter writerObj;
			try {
				if (new File(baseDirectory, "mservice/user.txt").exists()) {
					userData = new StringBuilder();
					readerObj = new BufferedReader(new FileReader(new File(baseDirectory, "mservice/user.txt")));
					while ((currentLine = readerObj.readLine()) != null) {
						userData.append(currentLine);
					}
					readerObj.close();
					userDataObj = new JSONObject(userData.toString());
					if (new File(baseDirectory, "/mservice/user_profile.txt").exists()) {
						userProfileFileReader = new BufferedReader(new FileReader(new File(baseDirectory, "/mservice/user_profile.txt")));
						userProfileFileData = new StringBuilder();
						while ((currentLine = userProfileFileReader.readLine()) != null) {
							userProfileFileData.append(currentLine);
						}
						userProfileFileReader.close();
						userProfileObj = new JSONObject(userProfileFileData.toString());
						userProfileObj = userProfileObj.optJSONObject("login_profile");
						if (userProfileObj.optString("client_id").toString().equals("dev")) {
							requestPathURL = "http://203.124.121.207:83/get_auth_indication.aspx";
						} else {
							requestPathURL = "http://203.124.120.196:83/get_auth_indication.aspx";
						}
						/* AUTHENTICATION DATA REQUEST */
						requestAuthenticationPath = new URL(requestPathURL);
						urlConObj = (HttpURLConnection)requestAuthenticationPath.openConnection();
						urlConObj.setDoOutput(true);
						urlConObj.setRequestMethod("POST");
						urlConObj.setRequestProperty("CONTENT-TYPE", "application/json");
						urlConObj.connect();

						oStreamObj = new OutputStreamWriter(urlConObj.getOutputStream());
						oStreamObj.write("<inputparam><context><sessionId>" + userProfileObj.optString("guid_val").toString() + "</sessionId><userId>" + userProfileObj.optString("user_id").toString() + "</userId><client_id>" + userProfileObj.optString("client_id").toString() + "</client_id><locale_id>" + userProfileObj.optString("locale_id").toString() + "</locale_id><country_code>" + userProfileObj.optString("country_code").toString() + "</country_code></context></inputparam>");
						oStreamObj.flush();
						oStreamObj.close();
						serverResponseObj = new StringBuilder();
						responseReaderObj = new BufferedReader(new InputStreamReader(urlConObj.getInputStream()));
						while ((currentLine = responseReaderObj.readLine()) != null) {
							serverResponseObj.append(currentLine);
						}
						responseReaderObj.close();
						urlConObj.disconnect();
						responseDataObj = new JSONObject(serverResponseObj.toString());

						/* VALIDATE DEVICE REQUEST */
						try {
							requestValidateDevicePath = new URL(userProfileObj.optString("protocol").toString() + "//" + userProfileObj.optString("domain_name").toString() + ":" + userProfileObj.optString("portno").toString() + "/security/validate_device.aspx");
							urlConObj = (HttpURLConnection)requestValidateDevicePath.openConnection();
							urlConObj.setDoOutput(true);
							urlConObj.setRequestMethod("POST");
							urlConObj.setRequestProperty("CONTENT-TYPE", "application/json");
							urlConObj.connect();
							oStreamObj = new OutputStreamWriter(urlConObj.getOutputStream());
							oStreamObj.write("<document><context><sessionId>" + userProfileObj.optString("guid_val").toString() + "</sessionId><userId>" + userProfileObj.optString("user_id").toString() + "</userId><client_id>" + userProfileObj.optString("client_id").toString() + "</client_id><locale_id>" + userProfileObj.optString("locale_id").toString() + "</locale_id><country_code>" + userProfileObj.optString("country_code").toString() + "</country_code><inputparam><p_device_id>" + userDataObj.optString("device_id").toString() + "</p_device_id><p_company_id>" + userProfileObj.optString("client_id").toString() + "</p_company_id><p_country_code>" + userProfileObj.optString("country_code").toString() + "</p_country_code></inputparam></context></document>");
							oStreamObj.flush();
							oStreamObj.close();
							serverResponseObj = new StringBuilder();
							responseReaderObj = new BufferedReader(new InputStreamReader(urlConObj.getInputStream()));
							while ((currentLine = responseReaderObj.readLine()) != null) {
								serverResponseObj.append(currentLine);
							}
							responseReaderObj.close();
							urlConObj.disconnect();
							Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(new InputSource(new StringReader(serverResponseObj.toString())));
							responseDataObj = responseDataObj.put("validDevice", doc.getElementsByTagName("p_valid_device_ind").item(0).getTextContent());

							/* RESPONSE FOR AUTHENTICATION & VALIDATE DEVICE REQUESTS */
							writerObj = new BufferedWriter(new FileWriter(new File(baseDirectory, "mservice/auth_indication.txt")));
							writerObj.write(responseDataObj.toString());
							writerObj.flush();
							writerObj.close();
						} catch (Exception ex) {
							responseDataObj = responseDataObj.put("validDevice", "nostatus");
							writerObj = new BufferedWriter(new FileWriter(new File(baseDirectory, "mservice/auth_indication.txt")));
							writerObj.write(responseDataObj.toString());
							writerObj.flush();
							writerObj.close();
						}
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
		if (setProcessInterval != null) {
			setProcessInterval.cancel();
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
		 if (setProcessInterval != null) {
			 setProcessInterval.cancel();
		 }

		startService(new Intent(getApplicationContext(), AuthenticationService.class));
	}
	/* USER CLICK FORCE STOP IN SETTINGS */
	 @ Override
	public void onDestroy() {
		super.onDestroy();
		 if (setProcessInterval != null) {
			 setProcessInterval.cancel();
		 }

		startService(new Intent(getApplicationContext(), AuthenticationService.class));
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
