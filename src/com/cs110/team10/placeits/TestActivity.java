package com.cs110.team10.placeits;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Criteria;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
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

public class TestActivity extends Activity implements OnMapClickListener, OnMarkerClickListener, 
			com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks, CancelableCallback, 
			com.google.android.gms.location.LocationListener, LocationListener, OnConnectionFailedListener{
	
	static GoogleMap googleMap;
	private static boolean addMarker = false;
	private final static double radiusSize = 804;      // Radius of notification marker in meters
	

	private Marker added; // Temp Marker used for onActivityResult()
	private LatLng tempPos; // Temp position used for onActivityResult()
	private String tempValue; // Temp string used for onActvityResult()

	private  static LocationManager locationManager;
	private static LocationManager alarmLocationManager;
	private  static PendingIntent pendingIntent; // Used for display notifications
	private static SharedPreferences sharedPreferences; // Used for saving markers, and reminders
	
	
	// Used for storing Data
	public static Database database;
	private int ID = 0;           // Used to give each marker an ID for storing them in data
	
	private static HashMap<Marker, Circle> circleMap;
	private HashMap<Marker, String> timeMode;
	private static String tempTime;    // Temporary time used for onActivityResult()
	private AlarmManager alarm;
	private PendingIntent alarmIntent; 
	
	static Context thisContext;
	
	// Used for Mocking
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private boolean tracking = false;
	
	private static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		thisContext = getApplicationContext();
		alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		// Loading map
		try {
            initializeMap();
        } catch (Exception e) {
            Log.d("TestActivity", e.getMessage());
        }
		


		// Enable my location button
	    googleMap.setMyLocationEnabled(true);	

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
        
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        locationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        
        locationClient = new LocationClient(this, this, this);
        
		// Create a criteria object to retrieve provider
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, true);
		Location myLocation = locationManager.getLastKnownLocation(provider);
		if (myLocation != null) {
			// Get latitude of the current location
			double myLatitude = myLocation.getLatitude();
			// Get longitude of the current location
			double myLongitude = myLocation.getLongitude();
			// Create a LatLng object for the current location
			LatLng latLng = new LatLng(myLatitude, myLongitude);
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		}
        
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
        	
        	 // Get Alarm for marker if it exists
            if(sharedPreferences.contains("markerAlarm_m" + i)){
            	Log.d("TestActivity", "SharedPreferences adding an alarm");
            	Marker mark = addAMarker(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), 
            		message, thisContext);
            	database.addTime(mark, sharedPreferences.getString("markerAlarm_m" + i, "0"));
            	

            } else if(!message.equals("0")){     // Check if marker exists

                // Drawing marker on the map
                Marker mark = googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), 
                		Double.parseDouble(longitude))).title(message));
                
                // Drawing circle on the map
                circleMap.put(mark, drawCircle(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))));
                
                
                database.addMarker(mark, message);
                
               
        		
        	}
        	
        }
        
        // Moves camera from NotificationHandler LatLng, if user clicks on "Complete"
		if(getIntent().getBooleanExtra("moveCamera", false)){	    	
			moveCamera(getIntent().getDoubleExtra("latitude", 0), getIntent().getDoubleExtra("longitude", 0), 4000);
		}
	    
		// Checks if device has been rebooted and needs to start alarm again
		if(getIntent().getBooleanExtra("minute", false)){
			Toast.makeText(TestActivity.this, "FUCKING MINUTE", Toast.LENGTH_SHORT).show();
			//setMinuteAlarm(TestActivity.this, marker, s, ID)
		}else if(getIntent().getBooleanExtra("weekly", false)){
			Toast.makeText(TestActivity.this, "FUCKING Week", Toast.LENGTH_SHORT).show();
			//setWeeklyAlarm(TestActivity.this, marker, s, ID)
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
            
        case R.id.action_search:
        	Intent aL = new Intent(TestActivity.this, activeList.class);
     		startActivity(aL);
     		return true;
            
        case R.id.action_about:
            // about us action
        	Intent i = new Intent(TestActivity.this, AboutUs.class);
        	startActivity(i);      	
            return true;
            
        case R.id.continuous_track:
        	if(tracking == false){
	        	tracking = true;
	        	Toast.makeText(TestActivity.this, "Continuous Tracking Enabled!", Toast.LENGTH_SHORT).show();
        	}else if(tracking == true){
        		tracking = false;
	        	Toast.makeText(TestActivity.this, "Continuous Tracking Disabled!", Toast.LENGTH_SHORT).show();
        	}
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
					Intent intent = new Intent(TestActivity.this, TimeChooser.class);
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
				database.removeDaysPicked(marker);           // Removes daysPicked for that marker
                editor.remove("markerLong_" + marker.getId());
                editor.remove("markerLat_" + marker.getId());
                editor.remove("markerMessage_" + marker.getId());
                
                // Remove Alarm if set
                if(sharedPreferences.contains("markerAlarm_" + marker.getId()));
                	editor.remove("markerAlarm_" + marker.getId());
                	
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
		
		alert.setNeutralButton("Remove Alarm", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("TestActivity", "Alarm Cancelled!");
				if(database.timePicked.remove(marker) == null){
					Toast.makeText(TestActivity.this, "No alarm set!", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(TestActivity.this, "Alarm cancelled!", Toast.LENGTH_SHORT).show();

				}
				Log.d("Remove Alarm", "Trying to remove " + marker.getId());
				alarm.cancel(alarmIntent);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				if(sharedPreferences.contains("markerAlarm_" + marker.getId())){
					Log.d("TestActivity", "Alarm preference cancelled");
					editor.remove("markerAlarm_" + marker.getId());
				}
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
                
                if(tempTime != null){
                	Log.d("testActivity", tempTime);
                }else{
                	Log.d("testActivity", "No Alarm mode was set");
                }
                // Setting Alarm notifications
                if(tempTime != null && tempTime.equals("weekly")){  // Weekly reminder
                	setWeeklyAlarm(TestActivity.this, added, tempValue, added.getId());
                	
                	
                	database.addTime(added, "weekly" );
                    editor.putString("markerAlarm_" + added.getId(), "weekly");
                    
                }else if(tempTime != null && tempTime.equals("minute")){ // Reminder every minute
                	setMinuteAlarm(TestActivity.this, added, tempValue, added.getId());
                	Log.d("TestActivity", "Saving markerAlarm_" + added.getId());
                	
                	database.addTime(added, "minute");
                    editor.putString("markerAlarm_" + added.getId(), "minute");

                }

                
                editor.commit();
            }
        } 
       
    }

	public void setWeeklyAlarm(Context context, Marker marker, String s, String ID){

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());


		Intent downloader = new Intent(context, TimeAlarm.class);
		downloader.putExtra("message", s);
		downloader.putExtra("longitude", marker.getPosition().longitude);
		downloader.putExtra("latitude", marker.getPosition().latitude);
		downloader.putExtra("id", ID);

		 alarmIntent = PendingIntent.getBroadcast(context, 7, downloader,
				PendingIntent.FLAG_CANCEL_CURRENT);

		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7,
				alarmIntent);
		
	}

	public void setMinuteAlarm(Context context, Marker marker, String s, String ID) {
		Intent downloader = new Intent(context, TimeAlarm.class);
		downloader.putExtra("message", s);
		downloader.putExtra("longitude", marker.getPosition().longitude);
		downloader.putExtra("latitude", marker.getPosition().latitude);
		downloader.putExtra("id", ID);

		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());


		 alarmIntent = PendingIntent.getBroadcast(context, 1, downloader,
				PendingIntent.FLAG_CANCEL_CURRENT);
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15,
				AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15, alarmIntent);
		     
	}
	    
	    public void cancelAlarm(Context context) {
	        // If the alarm has been set, cancel it.
	        if (alarm != null) {
	            alarm.cancel(alarmIntent);
	        }else{
	        	Toast.makeText(TestActivity.this, "Alarm has not been set!", Toast.LENGTH_SHORT).show();
	        }
	    }

	 
	 public static Marker addAMarker(LatLng position, String message, Context context){

		    Marker m = googleMap.addMarker(new MarkerOptions().position(position).title(message));
		    Log.d("addAMarker", "Adding marker " + m.getId());
		    database.markers.put(m, message);
		    database.timePicked.put(m, "minute");
		    circleMap.put(m, drawCircle(position));
		    
		    Intent enteredRegionIntent = new Intent(context, GeoAlert.class);
            
            // Adding message for the notification system
            enteredRegionIntent.putExtra("message", message);
            enteredRegionIntent.putExtra("latitude", position.latitude);
            enteredRegionIntent.putExtra("longitude", position.longitude);

            // Location manager checks this pendingIntent when user enters region. Goes to GeoAlert class if entered. 
            pendingIntent = PendingIntent.getBroadcast(context, 0, enteredRegionIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

            // Adding a proximity alert on the marker based on radiusSize. Expiration is -1, so it will never expire. 
            locationManager.addProximityAlert(position.latitude, position.longitude, (long)radiusSize, -1, pendingIntent);
            
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // Adding Marker data
            editor.putString("ID", m.getId());
            editor.putString("markerMessage_" + m.getId(), message);
            editor.putString("markerLat_" + m.getId(), String.valueOf(position.latitude));
            editor.putString("markerLong_" + m.getId(), String.valueOf(position.longitude));
            Log.d("AddAMarker", "adding markerMessage_" + m.getId());
            
            
            Log.d("AddAMarker Alarm mode", tempTime);
            // Setting Alarm notifications
            if(tempTime != null && tempTime.equals("weekly")){  // Weekly reminder
                editor.putString("markerAlarm_" + m.getId(), "weekly");
                
            }else if(tempTime != null && tempTime.equals("minute")){ // Reminder every minute
            	Log.d("TestActivity", "Saving markerAlarm_" + m.getId());
            	
                editor.putString("markerAlarm_" + m.getId(), "minute");
            }
            tempTime = null;    // Reset tempTime to avoid errors when adding a marker with no specified Alarm 
            editor.commit();
            
            return m;
         
	 }
	 
	 public static boolean deleteMarker(LatLng position, String message){
		 Log.d("TestActivity", "In Deleting Markers");
		 
		 if(database == null){
			 return false;
		 }
		 
		 for(Entry<Marker, String> entry : database.markers.entrySet()){
			 Marker marker = entry.getKey();
			 
			 if(marker.getPosition().latitude == position.latitude
					 && marker.getPosition().longitude == position.longitude){
				 Log.d("TestAcivity", "Deleting Marker");
				 return false;
			 }
			 
		 }
		 if(database.markers.isEmpty()){
			 Log.d("TestActivity", "fuuuck");
			 addAMarker(position, message,  TestActivity.thisContext);
			 
		 }
		 
		 return false;
	 }
	 
	 

	 
	 /*
	  *  Checks if map is loaded
	  */
    private void initializeMap() {
    	Log.d("TestActivity", "InitializeMap");
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap != null) {
            }
        }
    } // initializeMap()


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    	Log.d("TestActivity", "Configuration Changed");

        
    }

	@Override
	public void onCancel() {
		
	}


	@Override
	public void onFinish() {
	}


	@Override
	public void onBackPressed(){
		super.onBackPressed();
		googleMap = null;
		
	}
	/*
	 * Draws a circle around the markers of the map
	 */
    private static Circle drawCircle(LatLng position){
    	
        CircleOptions circle = new CircleOptions();
 
        circle.center(position);
 
        // Circle properties
        circle.radius(radiusSize);
        circle.strokeWidth(1);
        circle.strokeColor(Color.MAGENTA);
        circle.fillColor(0x30ff0000);
 
        return googleMap.addCircle(circle);
        
    }
    
    public static void storeTime(String s){
    	tempTime = s;
    }
    
    /*
     * Moves camera to location. Used when user clicks on 'complete' in notification bar
     */
    public static void moveCamera(double latitude, double longitude, int time){
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude))
				.zoom(12) 
				.bearing(60) // Sets the orientation of the camera to east
				.tilt(30) // Sets the tilt of the camera to 30 degrees		
				.build(); // Creates a CameraPosition from the builder
			
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), time, null);
    }
    
   

    
    // These below methods are for LocationListener
	@Override
	public void onLocationChanged(Location location) {
		if(googleMap != null && tracking == true){
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), 14));		
		}
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


	@Override
	public void onConnected(Bundle bundle) {
		locationClient.requestLocationUpdates(locationRequest, this);		
	}


	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        locationClient.connect();
    }
    
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        locationClient.disconnect();
        super.onStop();
    }





}
