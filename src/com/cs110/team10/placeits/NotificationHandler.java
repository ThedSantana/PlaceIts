package com.cs110.team10.placeits;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class NotificationHandler extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		if(getIntent().getBooleanExtra("completed", false)){
			Intent intent = new Intent(NotificationHandler.this, TestActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra("moveCamera", true);
			intent.putExtra("latitude", getIntent().getDoubleExtra("latitude", 0));
			intent.putExtra("longitude", getIntent().getDoubleExtra("longitude", 0));
			
			try{
				TestActivity.moveCamera(getIntent().getDoubleExtra("latitude", 0), getIntent().getDoubleExtra("longitude", 0), 2000);
			}catch(NullPointerException e){	
				startActivityIfNeeded(intent, 10);
			}
			
			

		}else{
			Log.d("NotificationHandler", "Dismiss was selected");
		}
		
		Log.d("NotificationHandler", "Deleting notification");
        NotificationManager manager = (NotificationManager) NotificationHandler.this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);          // Remove notification from status bar

        
        
        
        setResult(RESULT_OK);
        finish();
        
	}
	

}
