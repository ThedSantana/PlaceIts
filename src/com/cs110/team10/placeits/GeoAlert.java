package com.cs110.team10.placeits;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class GeoAlert extends Activity {
		String notificationTitle;             // Title of the notification
	    String notificationContent;			  // Message that the notification has under the title
	    String tickerMessage;                // Pop up message
	    
	    boolean enteredMarker = false;         // Checks if user has enter the marker location
	 
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	 
	        // Check if user has entered Marker
	        enteredMarker = getIntent().getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
	 
	        if(enteredMarker){  
	        	notificationTitle = "New task!";
	            notificationContent = getIntent().getStringExtra("message");
	            tickerMessage = notificationContent;
	        
		 
		        // Create the notification message
		        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		 
		        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
	            		.setContentTitle(notificationTitle)
		                .setContentText(notificationContent)
		                .setTicker(tickerMessage)
		        		.setSmallIcon(R.drawable.ic_launcher);
	
		 
		        // Sends a notification. Uses system time to create a new ID for the notification
		        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
	        }
	        finish();
	    }
	}
