package com.cs110.team10.placeits;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TestActivity extends Activity implements OnMapClickListener, OnMarkerClickListener, CancelableCallback{
	 private static GoogleMap googleMap;
	 private Map<Marker, String> markerList;
	 private static boolean addMarker = false;
	 private Marker added;                   // Temp Marker used for onActivityResult()
	 private LatLng tempPos;				// Temp position used for onActivityResult()
	 private String tempValue;              // Temp string used for onActvityResult()

		
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		// Set up marker variables
		markerList = new HashMap<Marker, String>();
		
		// Loading map
		try {
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		// Enable my location button
	    googleMap.setMyLocationEnabled(true);	

	    
	    // Set on click listener for "Add Note"
	    googleMap.setOnMapClickListener(this);
	    googleMap.setOnMarkerClickListener((OnMarkerClickListener) this);
	}
	
	
    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
        case R.id.action_add_note:
        	Toast.makeText(TestActivity.this, "Tap on a spot to add a note", Toast.LENGTH_SHORT).show();
        	addMarker = true;
            return true;
        case R.id.action_location_found:
            // location found
            return true;
        case R.id.action_refresh:
            // refresh
            return true;
        case R.id.action_help:
            // help action
            return true;
        case R.id.action_check_updates:
            // check for updates action
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
	@Override
	public void onMapClick(LatLng position) {

		// Check if action bar "add button" was enabled
		if(!addMarker)
			return;
		else
			addMarker = false; // Disables onMapClick feature
		

		final LatLng pos = position;
		tempPos = pos;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("New Marker");
		alert.setMessage("Please enter a Marker Title:");
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing, this just overrides the code so that
				// when OK is clicked with an EMPTY text field, the dialog doesn't
				// automatically close.
				
				}
		});
		
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Toast.makeText(TestActivity.this, "Nothing added!", Toast.LENGTH_SHORT).show();
					}
		});
		
		final AlertDialog alertDialog = alert.create();
		alertDialog.show();
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View v) {
				String value = input.getText().toString();
				tempValue = value;
				// Checks if the users inputs an empty text field when adding a note
				if(value.isEmpty()){
					Toast.makeText(TestActivity.this, "Please add some text", Toast.LENGTH_SHORT).show();
				}else{
					Intent intent = new Intent(TestActivity.this, DayChooser.class);
					startActivityForResult(intent, 1);
					
					alertDialog.dismiss();
				}
			}
		});
		
	}
	
	@Override
	public boolean onMarkerClick(final Marker marker) {
		marker.showInfoWindow();
		Log.d("TestActivity", "Marker clicked");
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(markerList.get(marker));
		// Set an EditText view to get user input
		alert.setPositiveButton("Remove Note", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				marker.remove();
				Toast.makeText(TestActivity.this, "Note removed!", Toast.LENGTH_SHORT).show();

			}
		});
		
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing because the user clicked cancel
					}
		});
		
		alert.show();
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.test_actionbar, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initializeMap();

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
        		added = googleMap.addMarker(new MarkerOptions().position(tempPos).title(tempValue));
				markerList.put(added, tempValue);
            }
        }
    }

	 
	 private static void addAllMarkers(Map<Marker, String> map) {
		 for (Entry<Marker, String> entry : map.entrySet()) {
			    Marker key = entry.getKey();
			    String value = entry.getValue();
			    googleMap.addMarker(new MarkerOptions().position(key.getPosition()).title(value));
			}
		}
	 /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    } // initializeMap()

    /*
     * Used to re-add all markers when orientation is changed. 
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    	Log.d("TestActivity", "wee woo");

        
        if(!markerList.isEmpty()){
        	addAllMarkers(markerList);
        }
        
    }

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}



}
