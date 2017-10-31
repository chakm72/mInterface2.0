package com.selfservit.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class mInterfaceUtil {
	LocationManager locationManager;
	LocationListener locationListener;
	/*FOR GETTING CURRENT LOCATION */
	public String getLocation(Context context) {
		String latitude="",
				longitude="";
		
				String currentLine,lastKnownLocation="";
				try {
					BufferedReader readerObj = new BufferedReader(new FileReader(new File(Environment.getExternalStorageDirectory(), "mservice/LastKnownLocation.txt")));
					while ((currentLine = readerObj.readLine()) != null) {
						lastKnownLocation = currentLine;
					}
					readerObj.close();
					String[] splitStr = lastKnownLocation.split(",");
					latitude = splitStr[0];
					longitude = splitStr[1];
				}catch(Exception e){
					e.printStackTrace();
				}
				
				//*** SUBSTRING LATITUDE,LONGITUDE IF GREATER THAN 10 POINTS ***//
				if (latitude.length() > 10) {
					latitude = latitude.substring(0, 10);
				}
				if (longitude.length() > 10) {
					longitude = longitude.substring(0, 10);
				}
		//*** RETURN LATITUDE,LONGITUDE VALUE IN JSON OBJECT FORMAT ***//
		return "{\"lat\":" + "\"" + latitude + "\"" + ",\"lon\":" + "\"" + longitude + "\"}" ;
	}

	/*FOR CHECK LOCATION SERVICE SETTING IS ENABLED OR NOT */
	public String checkLocation(Context context) {
		Boolean isEnabled = false;
	
		//*** REGISTER THE LOCATION MANAGER *** //
		locationManager = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
		//*** CHECK LOCATION SETTING ENABLED OR NOT ***//
		isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isEnabled) {
			isEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		
		return String.valueOf(isEnabled);
	}
	
	/*FOR CHECK QUEUE IS PROCESSING OR NOT */
	public String getSyncIndicator(Context context) {
		File queueFile, queueFolder;
		Boolean syncIndication = false;
		try{
			queueFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mservice/database/queue_mgr.txt");
			queueFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mservice/database/queue");
			if(queueFile.exists()){
				if(queueFile.length() == 0){
					syncIndication = true;
				}
			}else{
				syncIndication = true;
			}
			if(syncIndication){
				if(queueFolder.exists()){
					if(queueFolder.listFiles().length != 0){
						syncIndication = false;
					}
				}
			}
			return String.valueOf(syncIndication);
		}catch(Exception e){
		   return String.valueOf(e);
		}

	}

	/* COPY A FILE FROM ONE LOCATION TO ANOTHER LOCATION */
	public void copyFile(CallbackContext callbackContext, JSONArray arguments) {
		JSONObject inputFileObj;
		String srcFilePath,
				srcFileName,
				desFilePath,
				desFileName;
		InputStream srcFileReader;
		OutputStream desFileWriter;
		int srcFileSize;
		byte[] buf ;
		File sourceFileObj,desFileObj;
		try {
			//*** GET USER INPUT FOR SOURCE FILE AND DESTINATION PATH ***//
			inputFileObj = arguments.getJSONObject(0);
			srcFilePath = inputFileObj.optString("srcPath").toString();
			srcFileName = inputFileObj.optString("srcFile").toString();
			desFilePath = "/sdcard/" + inputFileObj.optString("desPath").toString();
			desFileName = inputFileObj.optString("desFile").toString();
			sourceFileObj = new File(srcFilePath + "/" + srcFileName);
			desFileObj = new File(desFilePath + "/" + desFileName);
			//*** CHECK DESTINATION FILE PATH EXITS OR NOT ***//
				if (!new File(desFilePath).exists()) {
					new File(desFilePath).mkdirs();
				}
				//*** CHECK SOURCE FILE EXITS OR NOT ***//
				if (sourceFileObj.exists()) {
					srcFileReader = new FileInputStream(sourceFileObj);
					desFileWriter = new FileOutputStream(desFileObj);
					buf = new byte[1024];
					//*** WRITE SOURCE FILE INTO DESTINATION PATH ***//
					while ((srcFileSize = srcFileReader.read(buf)) > 0) {
						desFileWriter.write(buf, 0, srcFileSize);
					}
					srcFileReader.close();
					desFileWriter.close();
					callbackContext.success("success");
				} else {
					callbackContext.error("failure");
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*GETTING A FILEPATH OF API LEVEL 19 AND ABOVE */
	public static String getDataColumn(Context context, Uri uri, String selection, String[]selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[]projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		}
		finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	@ TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getPathToNonPrimaryVolume(Context context, String tag) {
		File[]volumes = context.getExternalCacheDirs();
		if (volumes != null) {
			for (File volume: volumes) {
				if (volume != null) {
					String path = volume.getAbsolutePath();
					if (path != null) {
						int index = path.indexOf(tag);
						if (index != -1) {
							return path.substring(0, index) + tag;
						}
					}

				}
			}
		}
		return null;
	}
	public String refreshTimeProfile(String date,String hour,String minute){
		try {
			String serverResponseDate,deviceDate,serverDate;
			Date serverDateObj, todayDateobj;
			SimpleDateFormat serverDateformat,deviceDateFormat,serverDateString;
			BufferedWriter writerObj;
			serverResponseDate = date + " " + hour + ":" + minute+":00";
			serverDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			serverDateObj = serverDateformat.parse(serverResponseDate);
			serverDateString = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
			serverDate= serverDateString.format(serverDateObj);
			deviceDateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
			todayDateobj = new Date();
			deviceDate = deviceDateFormat.format(todayDateobj);
			JSONObject dateObj = new JSONObject();
			dateObj.put("serverDate",serverDate);
			dateObj.put("initServerDate",serverDate);
			dateObj.put("initDeviceDate",deviceDate);
			writerObj = new BufferedWriter(new FileWriter(new File(Environment.getExternalStorageDirectory(), "mservice/time_profile.txt")));
			writerObj.write(dateObj.toString());
			writerObj.flush();
			writerObj.close();
			return "true";
		}catch (Exception e){
			return "false";
		}
	}
	public void getNewDate(CallbackContext callbackContext){
		String currentLine;
		StringBuilder timeObj= new StringBuilder();
		try {
			Date mobileDate = new Date();
			File baseDirectory = Environment.getExternalStorageDirectory();
			BufferedReader readerObj = new BufferedReader(new FileReader(new File(baseDirectory, "mservice/time_profile.txt")));
			while ((currentLine = readerObj.readLine()) != null) {
				timeObj.append(currentLine);
			}
			readerObj.close();
			JSONObject serverDateObj = new JSONObject(timeObj.toString());
			String serverTimeObj = serverDateObj.optString("serverDate").toString();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
			Date serverDate = simpleDateFormat.parse(serverTimeObj);
			long timeDiff = mobileDate.getTime()-serverDate.getTime();
			serverDate.setTime(serverDate.getTime()+timeDiff);
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
			String serverDate1 = simpleDateFormat1.format(serverDate);
			callbackContext.success(serverDate1);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public void logData(String fileName, String data){
		File appDirectory;
		FileWriter fileWriterObj;

		try {

			/* CHECKING THE DIRECTORY EXISTS OR NOT AND CREATING THE DIRECTORY */
			appDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName.substring(0, fileName.lastIndexOf("/")));
			if (!appDirectory.exists()) {
				appDirectory.mkdirs();
			}


			/* WRITING THE DATA TO THE FILE */
			fileWriterObj = new FileWriter(new File(appDirectory,fileName.substring(fileName.lastIndexOf("/") + 1)), true);
			fileWriterObj.write(data);
			fileWriterObj.flush();
			fileWriterObj.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
