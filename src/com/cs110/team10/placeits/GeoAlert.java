package com.cs110.team10.placeits;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class GeoAlert extends BroadcastReceiver {
		String notificationTitle;             // Title of the notification
	    String notificationContent;			  // Message that the notification has under the title
	    String tickerMessage;                // Pop up message
	    
	    boolean enteredMarker = false;         // Checks if user has enter the marker location
	 
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	
	    	Intent completeIntent = new Intent(context, NotificationHandler.class);
	    	completeIntent.putExtra("completed", true);
	    	completeIntent.putExtra("latitude", intent.getDoubleExtra("latitude", 0));
	    	completeIntent.putExtra("longitude", intent.getDoubleExtra("longitude", 0));

	    	
	    	PendingIntent complete = PendingIntent.getActivity(context, 20, completeIntent, 0);
	    	
	    	Intent dismissIntent = new Intent(context, NotificationHandler.class);
	    	PendingIntent dismiss = PendingIntent.getActivity(context, 40, dismissIntent , 0);

	    	
	    	
	        // Check if user has entered Marker
	        enteredMarker = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
	 
	        if(enteredMarker){  
	        	notificationTitle = "New task!";
	            notificationContent = intent.getStringExtra("message");
	            tickerMessage = notificationContent;
	        
		 
		        // Create the notification message
		        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		 
		        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
        				.setPriority(Notification.PRIORITY_HIGH)
	            		.setContentTitle(notificationTitle)
		                .setContentText(notificationContent)
		                .setTicker(tickerMessage)
		        		.setSmallIcon(R.drawable.ic_launcher)
		        		.setAutoCancel(true)
		        		.addAction(R.drawable.ic_action_accept, "Complete task", complete)
        				.addAction(R.drawable.ic_action_cancel, "Dismiss", dismiss);

		        // This is used to dismiss the notification upon click
		        PendingIntent notifyIntent = PendingIntent.getActivity(context, 30, new Intent(), 0);     
		        notificationBuilder.setContentIntent(notifyIntent);
		        
		        // Builds notification
		        Notification notification = notificationBuilder.build();
		        // Sends a notification. Uses system time to create a new ID for the notification
		        notificationManager.notify(0, notification);
	        }
	    }
	    

	}
