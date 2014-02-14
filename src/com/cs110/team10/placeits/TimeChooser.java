package com.cs110.team10.placeits;




import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;


public class TimeChooser extends Activity {
	private HashMap<String, Boolean> timePicked;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_chooser);
		
		setTitle("Set up a reminder");
		
		timePicked = new HashMap<String, Boolean>();
		
    	
    	timePicked.put("minute", false);
    	timePicked.put("weekly", false);


		
		Button confirmOption = (Button) this.findViewById(R.id.chooseDaysConfirm);
		Button cancelOption = (Button) this.findViewById(R.id.chooseDaysCancel);
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.d("TimeChooser", "minute Selected");
				if (checkedId == R.id.one_min) {
					timePicked.put("minute", true);
					timePicked.put("weekly", false);
		        }else if (checkedId == R.id.weekly) {
		        	Log.d("TimeChooser", "Weekly Selected");
		        	timePicked.put("minute", false);
					timePicked.put("weekly", true);
		        }else{
		        	timePicked.put("minute", false);
					timePicked.put("weekly", false);
		        }				
			}
		});
		
		
		confirmOption.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("ConfirmationActivity", "Confirm Picked");
				Log.d("TimeChooser", String.valueOf(timePicked.get("minute")));
		    	Toast.makeText(TimeChooser.this, "Notes added!", Toast.LENGTH_SHORT).show();
		    	if(timePicked.get("minute")){
			    	TestActivity.storeTime("minute");
		    	}else if(timePicked.get("weekly")){
		    		TestActivity.storeTime("weekly");
		    	}else{
		    		TestActivity.storeTime(null);
		    	}
		    	setResult(RESULT_OK);
	            finish();
				
			}
		});
		
		cancelOption.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("DayChooser", "Cancel Picked");
				Toast.makeText(TimeChooser.this, "Nothing added!", Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED);
		    	finish();
			}
		});
		
	}
	


}
