package com.selfservit.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova. * ;
import org.json.JSONObject;

import java.io.File;

public class mInterface extends CordovaPlugin {
	private static final int PICK_FILE_REQUEST = 1;
	CallbackContext callback;
	private static JSONArray arguments = null;
	/* START SERVICE WHEN USER COMES FROM INACTIVE TO ACTIVE STATE IF SERVICE NOT RUNNING */
	 @ Override
	public void onResume(boolean multitasking) {
		super.onResume(multitasking);
		 if (!isServiceRunning(AuthenticationService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), AuthenticationService.class));
		 }
		 if (!isServiceRunning(QueueService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), QueueService.class));
		 }
		 if (!isServiceRunning(LocationService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), LocationService.class));
		 }
		 if (!isServiceRunning(TimerService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), TimerService.class));
		 }
		 if (!isServiceRunning(CheckSumService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), CheckSumService.class));
		 }
	}
	 @ Override
	public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext)throws JSONException {
		/* START BACKGROUND SERVICE IF NOT RUNNING ALREADY */
		if (action.equals("StartService")) {
			if (!isServiceRunning(AuthenticationService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), AuthenticationService.class));
		 }
		 if (!isServiceRunning(QueueService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), QueueService.class));
		 }
		 if (!isServiceRunning(LocationService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), LocationService.class));
		 }
		 if (!isServiceRunning(TimerService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), TimerService.class));
		 }
		 if (!isServiceRunning(CheckSumService.class) || !isProcessRunning()) {
			 cordova.getActivity().startService(new Intent(cordova.getActivity().getApplicationContext(), CheckSumService.class));
		 }
			callbackContext.success("Success");
		} else if (action.equals("GetLocation")) {
			/* SEND CURRENT LOCATION VIA PLUGIN CALLBACK */
			callbackContext.success(new mInterfaceUtil().getLocation(cordova.getActivity().getApplicationContext()));
		} else if (action.equals("CheckLocation")) {
			/* SEND CURRENT LOCATION SETTING STATUS VIA PLUGIN CALLBACK */
			callbackContext.success(new mInterfaceUtil().checkLocation(cordova.getActivity().getApplicationContext()));
		}else if (action.equals("GetSyncIndicator")) {
			/* SEND QUEUE PROCESSING STATUS VIA PLUGIN CALLBACK */
			callbackContext.success(new mInterfaceUtil().getSyncIndicator(cordova.getActivity().getApplicationContext()));
		} else if (action.equals("FileChooser")) {
			chooseFile(callbackContext);
			return true;
		} else if (action.equals("CopyFile")) {
			arguments = args;
			new mInterfaceUtil().copyFile(callbackContext, arguments);
		}else if(action.equals("GetNewDate")){
			new mInterfaceUtil().getNewDate(callbackContext);
		} else if(action.equals("RefreshTimeProfile")) {
			JSONObject timeObj = args.getJSONObject(0);
			String date,hour,minute;
			date = timeObj.optString("serverDate").toString();
			hour = timeObj.optString("serverHour").toString();
			minute = timeObj.optString("serverMinute").toString();
			String returnStatus=new mInterfaceUtil().refreshTimeProfile(date,hour,minute);
			if(returnStatus.equals("true")){
				callbackContext.success("success");
			}else {
				callbackContext.error("failure");
			}
		}else if (action.equals("UpdateChoice")) {
			/* OPEN THE PLAYSTORE MSERVICE APP */
			String appVersion,softwareProductVersion,softwareProductSubVersion;
			JSONObject versionObj;
			AlertDialog.Builder builder= new AlertDialog.Builder(cordova.getActivity(),AlertDialog.THEME_HOLO_DARK);
			AlertDialog alertDialog;
			versionObj = args.getJSONObject(0);
			appVersion = versionObj.optString("appVersion").toString();
			softwareProductVersion= versionObj.optString("softwareProductVersion").toString();
			softwareProductSubVersion = versionObj.optString("softwareProductSubVersion").toString();
			builder.setTitle("New Update");
			builder.setMessage("Your mservice version is " + appVersion + ". There is an updated version  " + softwareProductVersion + "." + softwareProductSubVersion + " available. Please update.");
			builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cordova.getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +cordova.getActivity().getApplicationContext().getPackageName())));
					callbackContext.error("failure");
				}
			});
			builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					callbackContext.success("Success");
				}
			});
			alertDialog= builder.create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
			alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dialog.dismiss();
						callbackContext.error("failure");
					}
					return true;
				}

			});
		}else if (action.equals("UpdateConfirm")) {
			/* OPEN THE PLAYSTORE MSERVICE APP */
			String appVersion,softwareProductVersion,softwareProductSubVersion;
			JSONObject versionObj;
			AlertDialog alertDialog;
			AlertDialog.Builder builder= new AlertDialog.Builder(cordova.getActivity(),AlertDialog.THEME_HOLO_DARK);
			versionObj = args.getJSONObject(0);
			appVersion = versionObj.optString("appVersion").toString();
			softwareProductVersion= versionObj.optString("softwareProductVersion").toString();
			softwareProductSubVersion = versionObj.optString("softwareProductSubVersion").toString();
			builder.setTitle("New Update");
			builder.setMessage("Your mservice version is " + appVersion + ". Please upgrade the app to " + softwareProductVersion + "." + softwareProductSubVersion + " to proceed.");
			builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cordova.getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +cordova.getActivity().getApplicationContext().getPackageName())));
					callbackContext.error("failure");
				}
			});
			alertDialog= builder.create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
			alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if(keyCode == KeyEvent.KEYCODE_BACK){
						dialog.dismiss();
						callbackContext.error("failure");
					}
					return true;
				}
			});
		}
		return true;
	}
	public void chooseFile(CallbackContext callbackContext) {
		Intent openFile,
		selectFile;
		PluginResult pluginResult;
		callback = callbackContext;
		if (Build.MANUFACTURER.equals("samsung")) {
			/* SELECT FILE FOR SAMSUNG DEVICES */
			openFile = new Intent("com.sec.android.app.myfiles.PICK_DATA");
			openFile.putExtra("CONTENT_TYPE", "*/*");
		} else {
			/* SELECT FILE FOR EXCEPT SAMSUNG DEVICES */
			openFile = new Intent(Intent.ACTION_GET_CONTENT);
			openFile.setType("*/*");
			openFile.addCategory(Intent.CATEGORY_OPENABLE);
			openFile.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		}
		selectFile = Intent.createChooser(openFile, "Select File");
		cordova.startActivityForResult(this, selectFile, PICK_FILE_REQUEST);
		pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
		pluginResult.setKeepCallback(true);
		callbackContext.sendPluginResult(pluginResult);
	}
	 @ Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_FILE_REQUEST && callback != null) {
			if (resultCode == Activity.RESULT_OK) {
				String fileName = null,
				getfilePath = null,
				filePath = null,
				fileExtension,
				fileType,
				fileSize = null,
				storageDefinition; ;
				final String docId,
				type,
				id;
				final String[]split;
				final String tag,
				path;
				final int splitIndex;
				Cursor cursor;
				long getSize;
				JSONObject fileProperities = null;
				Uri uri = data.getData(); //get uri for selected file
				fileType = this.cordova.getActivity().getContentResolver().getType(uri); // get mimeType for selected file
				File getFileName,
				file;
				try {
					/*API LEVEL 19 AND ABOVE (TO GET FILEPATH)*/
					if (Build.VERSION.SDK_INT >= 19 && !Build.MANUFACTURER.equals("samsung") && DocumentsContract.isDocumentUri(cordova.getActivity(), uri)) {
						if ("com.android.externalstorage.documents".equals(uri.getAuthority())) { // ExternalStorageProvider
							docId = DocumentsContract.getDocumentId(uri);
							split = docId.split(":");
							type = split[0];
							if ("primary".equalsIgnoreCase(type)) {
								getfilePath = Environment.getExternalStorageDirectory() + "/" + split[1];
							} else {
								if (Environment.isExternalStorageRemovable()) {
									storageDefinition = System.getenv("EXTERNAL_STORAGE");
									getfilePath = storageDefinition;
								} else {
									splitIndex = docId.indexOf(':', 1);
									tag = docId.substring(0, splitIndex);
									path = docId.substring(splitIndex + 1);
									if (new mInterfaceUtil().getPathToNonPrimaryVolume(cordova.getActivity(), tag) != null) {
										storageDefinition = new mInterfaceUtil().getPathToNonPrimaryVolume(cordova.getActivity(), tag) + "/" + path;
										file = new File(storageDefinition);
										if (file.exists() && file.canRead()) {
											getfilePath = storageDefinition;
										}
									}
								}
							}
						} else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) { // DownloadsProvider

							id = DocumentsContract.getDocumentId(uri);
							final Uri contentUri = ContentUris.withAppendedId(
									Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

							getfilePath = new mInterfaceUtil().getDataColumn(cordova.getActivity(), contentUri, null, null);

						} else if ("com.android.providers.media.documents".equals(uri.getAuthority())) { // MediaProvider
							docId = DocumentsContract.getDocumentId(uri);
							split = docId.split(":");
							type = split[0];

							Uri contentUri = null;
							if ("image".equals(type)) {
								contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
							} else if ("video".equals(type)) {
								contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
							} else if ("audio".equals(type)) {
								contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
							}

							final String selection = "_id=?";
							final String[]selectionArgs = new String[]{
								split[1]
							};
							getfilePath = new mInterfaceUtil().getDataColumn(cordova.getActivity(), contentUri, selection, selectionArgs);
						}
						// *** API Level 19 and Above (To Get FileName,FileExtension and FileSize)**** //
						cursor = cordova.getActivity().getContentResolver().query(uri, null, null, null, null, null);
						try {
							if (cursor != null && cursor.moveToFirst()) {
								fileName = cursor.getString(
										cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
								if (!cursor.isNull(cursor.getColumnIndex(OpenableColumns.SIZE))) {
									fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
								}
							}
						}
						finally {
							cursor.close();
						}

						// *** API Level 19 below and SAMSUNG (To Get FileProperties of MediaFiles)**** //
					} else {
						if (fileType != null) {
							String[]projection = {
								MediaStore.Images.Media.DATA,
								MediaStore.Images.Media.SIZE,
								MediaStore.Images.Media.DISPLAY_NAME
							};
							cursor = cordova.getActivity().getContentResolver().query(uri, projection, null, null, null);
							if (cursor.moveToFirst()) {
								getfilePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
								fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
								fileSize = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
							}
							cursor.close();

							/* API LEVEL 19 BELOW AND SAMSUNG (TO GET FILEPROPERTIES OF NORMAL FILES(EX:txt,.pdf,etc..,))*/
						} else {
							getfilePath = data.getData().getPath();
							filePath = getfilePath.substring(0, getfilePath.lastIndexOf(File.separator));
							getFileName = new File(getfilePath);
							fileName = getFileName.getName();
							getSize = getFileName.length();
							fileSize = String.valueOf(getSize);
						}
					}
					filePath = getfilePath.substring(0, getfilePath.lastIndexOf(File.separator));
					fileExtension = "." + getfilePath.substring(getfilePath.lastIndexOf(".") + 1);
					/* SEND BELOW JSON OBJECT TO PLUGIN CALLBACK*/
					fileProperities = new JSONObject();
					fileProperities.put("filePath", filePath);
					fileProperities.put("fileName", fileName);
					fileProperities.put("fileSize", fileSize);
					fileProperities.put("fileExtension", fileExtension);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (filePath != null) {
					callback.success(fileProperities);
				} else {
					callback.error("File path was null");
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// TODO NO_RESULT or error callback?
				callback.error("failure");
			} else {
				callback.error(resultCode);
			}
		} else {
			callback.error("failure");
		}
	}
	/* CHECKING THE BACKGROUND SERVICE RUNNING OR NOT */
	private boolean isServiceRunning(Class <  ?  > serviceClass) {
		ActivityManager manager = (ActivityManager)cordova.getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	private boolean isProcessRunning() {
        ActivityManager manager1 = (ActivityManager)cordova.getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager1.getRunningAppProcesses()) {
            if (processInfo.processName.equals(cordova.getActivity().getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
