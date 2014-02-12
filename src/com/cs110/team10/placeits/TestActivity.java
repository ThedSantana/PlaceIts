package com.cs110.team10.placeits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TestActivity extends Activity implements OnMapClickListener, OnMarkerClickListener, CancelableCallback, LocationListener{
	private static GoogleMap googleMap;
	private static boolean addMarker = false;
	private final static double radiusSize = 804;      // Radius of notification marker in meters
	
	private static HashMap<String, Boolean> daysPicked;

	private Marker added; // Temp Marker used for onActivityResult()
	private LatLng tempPos; // Temp position used for onActivityResult()
	private String tempValue; // Temp string used for onActvityResult()

	private LocationManager locationManager;
	private PendingIntent pendingIntent; // Used for display notifications
	private SharedPreferences sharedPreferences; // Used for saving markers, and reminders
	
	
	// Used for storing Data
	Database database;
	private int ID = 0;           // Used to give each marker an ID for storing them in data
	
	private HashMap<Marker, Circle> circleMap;

		
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		
		 
		
		// Loading map
		try {
            initializeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
		

		
		// Enable my location button
	    googleMap.setMyLocationEnabled(true);	

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
        
        circleMap = new HashMap<Marker, Circle>();
        
        // Set up the database for storing data
        database = new Database();
        
        // Saves all data in sharedPreferences. Mode is set to MODE_PRIVATE for creating a new file to write data
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        

        // Getting stored latitude if exists else return 0
        String storedLat = sharedPreferences.getString("myLat", "0");

        // Getting stored longitude if exists else return 0
        String storedLong = sharedPreferences.getString("myLong", "0");

        // Getting stored zoom level if exists else return 0
        String storedZoom = sharedPreferences.getString("myZoom", "0");

        // If coordinates are stored earlier
        if(!storedLat.equals("0")){
        	// Moving CameraPosition to previously clicked position
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(storedLat), Double.parseDouble(storedLong))));

            // Setting the zoom level in the map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(storedZoom)));
        }
        
        // Getting ID count
        if(! sharedPreferences.getString("ID", "noID").equals("noID")){
        	try{
        		String s = sharedPreferences.getString("ID", "noID");
        		s = s.substring(1);                 // Marker IDS begin as : m_       // Where _ is a number, so I'm taking out the m
        		ID = Integer.parseInt(s);
        	}catch(NumberFormatException e){
        		Toast.makeText(TestActivity.this, "Markers could not be loaded! Sorry!", Toast.LENGTH_SHORT).show();
        	}
        }
        
        
        Log.d("TestActivity", "Marker ID is " + ID);
        // Now add all the markers on the map
        for(int i=0; i <= ID; i++){
        	String longitude = sharedPreferences.getString("markerLong_m" + i, "0");
        	String latitude =  sharedPreferences.getString("markerLat_m" + i, "0");
        	String message = sharedPreferences.getString("markerMessage_m" + i, "0");
        	
        	// Check if marker exists
        	if(!message.equals("0")){

                // Drawing marker on the map
                Marker mark = googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), 
                		Double.parseDouble(longitude))).title(message));
                
                // Drawing circle on the map
                circleMap.put(mark, drawCircle(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))));
                
                
                database.addMarker(mark, message);
                database.addDaysPicked(mark, null);
        		
        	}
        	
        }
        
        // Moves camera from NotificationHandler LatLng, if user clicks on "Complete"
		if(getIntent().getBooleanExtra("moveCamera", false)){	    	
			moveCamera(getIntent().getDoubleExtra("latitude", 0), getIntent().getDoubleExtra("longitude", 0), 4000);
		}
	    
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
		Log.d("TestActivity", "REMOVING MARKER " + marker.getId());

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(database.getMarkerList().get(marker));
        final SharedPreferences.Editor editor = sharedPreferences.edit();
		// Set an EditText view to get user input
		alert.setPositiveButton("Complete task", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				database.getMarkerList().remove(marker);      // Removes marker from markerList
				database.removeDaysPicked(marker); // Removes daysPicked for that marker
                editor.remove("markerLong_" + marker.getId());
                editor.remove("markerLat_" + marker.getId());
                editor.remove("markerMessage_" + marker.getId());
                String s = sharedPreferences.getString("ID", "noID");
                s = s.substring(1);
                Log.d("TestActivity", "Before is " + s);
                int i = 0;
                try{
                	i = Integer.parseInt(s) - 1;
                    editor.putString("ID", "m" + String.valueOf(i));
                   

                }catch(NumberFormatException e){
                	Log.d("TestActivity", "Marker could not be removed " + marker.getId());
            		Toast.makeText(TestActivity.this, "Markers could not be removed! ", Toast.LENGTH_SHORT).show();
                }

                // Removes marker from map
				marker.remove();
				
				// Remove the marker's proximity alert
				Intent intent = new Intent(TestActivity.this, GeoAlert.class);
				pendingIntent = PendingIntent.getBroadcast(TestActivity.this, 0, intent,Intent.FLAG_ACTIVITY_NEW_TASK);
				locationManager.removeProximityAlert(pendingIntent);
				
				// Clear the circle
				circleMap.get(marker).remove();
				circleMap.remove(marker);
				
				
				Toast.makeText(TestActivity.this, "Note completed!", Toast.LENGTH_SHORT).show();
				editor.commit();
				
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
		Log.d("TestActivity", "onActivityResult");
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
        		added = googleMap.addMarker(new MarkerOptions().position(tempPos).title(tempValue));
        		circleMap.put(added, drawCircle(tempPos));            // For debug purposes
				database.addMarker(added, tempValue);           // Adds marker to database
				database.addDaysPicked(added, daysPicked);      // Stores marker and daysPicked data
				
				
                Intent enteredRegionIntent = new Intent(TestActivity.this, GeoAlert.class);
                
                // Adding message for the notification system
                enteredRegionIntent.putExtra("message", tempValue);
                enteredRegionIntent.putExtra("latitude", tempPos.latitude);
                enteredRegionIntent.putExtra("longitude", tempPos.longitude);

                // Location manager checks this pendingIntent when user enters region. Goes to GeoAlert class if entered. 
                pendingIntent = PendingIntent.getBroadcast(TestActivity.this, 0, enteredRegionIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

                // Adding a proximity alert on the marker based on radiusSize. Expiration is -1, so it will never expire. 
                locationManager.addProximityAlert(tempPos.latitude, tempPos.longitude, (long)radiusSize, -1, pendingIntent);
                
                
                // Save the data of the position of marker, the string message, and position of the camera
                SharedPreferences.Editor editor = sharedPreferences.edit();
                
                // Storing my location
                editor.putString("myLat", Double.toString(tempPos.latitude));
                editor.putString("myLong", Double.toString(tempPos.longitude));
                editor.putString("myZoom", Float.toString(googleMap.getCameraPosition().zoom));
                
                // Adding Marker data
                editor.putString("ID", added.getId());
                editor.putString("markerMessage_" + added.getId(), tempValue);
                editor.putString("markerLat_" + added.getId(), String.valueOf(tempPos.latitude));
                editor.putString("markerLong_" + added.getId(), String.valueOf(tempPos.longitude));
                Log.d("TestActivity", "adding markerMessage_" + added.getId());

                
                editor.commit();
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

        
        if(!database.getMarkerList().isEmpty()){
        	addAllMarkers(database.getMarkerList());
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

	/**
	 * Used to draw circles around the marker of the map
	 */
    private Circle drawCircle(LatLng position){
    	
        CircleOptions circle = new CircleOptions();
 
        circle.center(position);
 
        // Circle properties
        circle.radius(radiusSize);
        circle.strokeWidth(1);
        circle.strokeColor(Color.MAGENTA);
        circle.fillColor(0x30ff0000);
 
        return googleMap.addCircle(circle);
        
    }
    
    public static void storeMap(HashMap<String, Boolean> map){
    	daysPicked = map;
    }
    
    /*
     * Moves camera to location. Used when user clicks on 'complete' in notification bar
     */
    public static void moveCamera(double latitude, double longitude, int time){
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude))
				.zoom(15) 
				.bearing(90) // Sets the orientation of the camera to east
				.tilt(30) // Sets the tilt of the camera to 30 degrees		
				.build(); // Creates a CameraPosition from the builder
			
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), time, null);
    }

    
    // These below methods are for LocationListener
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
    // These above methods are for LocationListener




}
