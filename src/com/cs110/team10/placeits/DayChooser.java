package com.cs110.team10.placeits;




import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

public class DayChooser extends Activity {
	private HashMap<String, Boolean> daysPicked;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.day_picker_layout);
		
		setTitle("Set up a weekly reminder");
		
		daysPicked = new HashMap<String, Boolean>();
		
    	daysPicked.put("Sunday", false);
    	daysPicked.put("Monday", false);
    	daysPicked.put("Tuesday", false);
    	daysPicked.put("Wednesday", false);
    	daysPicked.put("Thursday", false);
    	daysPicked.put("Friday", false);
    	daysPicked.put("Saturday", false);


		
		Button confirmOption = (Button) this.findViewById(R.id.chooseDaysConfirm);
		Button cancelOption = (Button) this.findViewById(R.id.chooseDaysCancel);
		
		confirmOption.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("ConfirmationActivity", "Confirm Picked");
		    	Toast.makeText(DayChooser.this, "Notes added!", Toast.LENGTH_SHORT).show();
		    	TestActivity.storeMap(daysPicked);
		    	setResult(RESULT_OK);
	            finish();
				
			}
		});
		
		cancelOption.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("DayChooser", "Cancel Picked");
				Toast.makeText(DayChooser.this, "Nothing added!", Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED);
		    	finish();
			}
		});
		
	}

	public void onCheckBoxClicked(View view) {
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    // Check which checkbox was clicked
	    switch(view.getId()) {
	        case R.id.Sunday:
	            if (checked){
	            	daysPicked.put("Sunday", true);
	            }else{
	            	daysPicked.put("Sunday", false);
	            }
	            break;
	        case R.id.Monday:
	            if (checked){
	            	daysPicked.put("Monday", true);

	            }else{
	            	daysPicked.put("Monday", false);
	            }
	            break;
	        case R.id.Tuesday:
	            if (checked){
	            	daysPicked.put("Tuesday", true);

	            }else{
	            	daysPicked.put("Tuesday", false);
	            }
	            break;
	        case R.id.Wednesday:
	            if (checked){
	            	daysPicked.put("Wednesday", true);

	            }else{
	            	daysPicked.put("Wednesday", false);
	            }
	            break;
	        case R.id.Thursday:
	            if (checked){
	            	daysPicked.put("Thursday", true);

	            }
	            break;
	        case R.id.Friday:
	            if (checked){
	            	daysPicked.put("Friday", true);

	            }else{
	            	daysPicked.put("Friday", false);
	            }
	            break;
	        case R.id.Saturday:
	            if (checked){
	            	daysPicked.put("Saturday", true);

	            }else{
	            	daysPicked.put("Saturday", false);
	            }
	            break;
	            
	    }
	}
	

	

}
