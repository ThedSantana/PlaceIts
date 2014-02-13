package com.cs110.team10.placeits;

import java.util.Calendar;

import com.google.android.gms.maps.model.LatLng;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TimeAlarm extends BroadcastReceiver{
    
    private String tempMessage;
    private double tempLatitude;
    private double tempLongitude;
    

    
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("TimeAlarm", "OnReceive");
		tempLatitude = intent.getDoubleExtra("latitude", 0);
		tempLongitude = intent.getDoubleExtra("longitude", 0);
		Intent completeIntent = new Intent(context, NotificationHandler.class);
    	completeIntent.putExtra("completed", true);
    	completeIntent.putExtra("latitude", tempLatitude);
    	completeIntent.putExtra("longitude", tempLongitude);
    	
    	Log.d("TimeAlarm", String.valueOf(intent.getDoubleExtra("latitude", 0)));
    	Log.d("TimeAlarm", intent.getStringExtra("message"));
    	
    	if(TestActivity.deleteMarker(new LatLng(tempLatitude, tempLongitude), intent.getStringExtra("message"))){
    		Log.d("TimeAlarm", "Marker deleted, adding new Marker");
    		TestActivity.addAMarker(new LatLng(tempLatitude, tempLongitude), intent.getStringExtra("message"), TestActivity.thisContext);
    	}

        
        
	}
	
	

		public void setLongitude(double longitude) {
			this.tempLongitude = longitude;
			Log.d("TimeAlarm", "Set Longitude" + String.valueOf(tempLongitude));
		}

		public void setLatitude(double latitude) {
			this.tempLatitude = latitude;
		}

		public void setMessage(String tempValue) {
			this.tempMessage = tempValue;
		}
}
