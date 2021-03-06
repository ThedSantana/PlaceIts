package com.cs110.team10.placeits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TestActivity extends Activity implements OnMapClickListener, OnMarkerClickListener, 
			com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks, CancelableCallback, 
			com.google.android.gms.location.LocationListener, LocationListener, OnConnectionFailedListener{
	
	public static final String TAG = "TestActivity";

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
	private static String tempTime;    // Temporary time used for onActivityResult()
	private AlarmManager alarm;
	private PendingIntent alarmIntent; 
	
	static Context thisContext;
	
	// Used for Mocking
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private boolean tracking = false;
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
	
	//TODO: Here are the variables I'm adding for Places
		private String[] places;	
		private Location markerLoc;
		
		
		private Location loc;	
		
		//End new variables

	private int settings_requestcode = 5;
	
	// Used for async and update task methods. These help add the markers in onCreate
	List<Double> latitude;
	List<Double> longtitude;
	List<String> categoryHeadList;
	List<String> isCategoryList;

	private double tempMarkerLat; // Used for deleting and adding a marker in Async methods
	private double tempMarkerLong; // Used for deleting and adding a marker in Async methods
	private String tempMarkerDescription; // Used for deleting and adding a marker in Async methods
	private String tempCategoryHead; // Used for deleting and adding a marker in Async methods, category holder
	private String tempIsCategory; // Used for deleting and adding a marker in Async methods, blue marker
	

	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		Log.d("TestActivity", "onCreate");
		
		googleMap = null;
		
		latitude = new ArrayList<Double>();
		longtitude = new ArrayList<Double>();
		categoryHeadList = new ArrayList<String>();
		isCategoryList = new ArrayList<String>();
		
		// TODO:check added code
		initCompo();
		places = getResources().getStringArray(R.array.places);
		currentLocation();
				
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
			Log.d("Test Activity", "Location is not null");
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
        /*
        // If coordinates are stored earlier
        if(!storedLat.equals("0")){
        	// Moving CameraPosition to previously clicked position
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(storedLat), Double.parseDouble(storedLong))));

            // Setting the zoom level in the map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(storedZoom)));
        }
        */
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
        /*
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
        } */
        
		List<String> info;
		try {
			info = new UpdateTask().execute(Database.ITEM_URI).get();
			for (int i = 0; i < info.size(); i++) {
				double lat = latitude.get(i);
				double lon = longtitude.get(i);
				String message = info.get(i);
				
				// Check if marker is a category place it
				String category = isCategoryList.get(i);
				Marker mark;
				// Drawing marker on the map
				if(category.equals("true")){
					mark = googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
							.title(message)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
							);

				}else{
					mark = googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(message));
				}
				// Drawing circle on the map
				addPendingIntent(message, lat, lon);
				circleMap.put(mark, drawCircle(new LatLng(lat, lon)));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

        // Moves camera from NotificationHandler LatLng, if user clicks on "Complete"
		if(getIntent().getBooleanExtra("moveCamera", false)){	    	
			moveCamera(getIntent().getDoubleExtra("latitude", 0), getIntent().getDoubleExtra("longitude", 0), 4000);
		}
	    
		// Checks if device has been rebooted and needs to start alarm again
		if(getIntent().getBooleanExtra("minute", false)){
			Toast.makeText(TestActivity.this, " MINUTE", Toast.LENGTH_SHORT).show();
			//setMinuteAlarm(TestActivity.this, marker, s, ID)
		}else if(getIntent().getBooleanExtra("weekly", false)){
			Toast.makeText(TestActivity.this, " Week", Toast.LENGTH_SHORT).show();
			//setWeeklyAlarm(TestActivity.this, marker, s, ID)
		}
		
	    // Set on click listener for "Add Note"
	    googleMap.setOnMapClickListener(this);
	    googleMap.setOnMarkerClickListener((OnMarkerClickListener) this);
	}
	
	//TODO: Check if below code works
		private void initCompo() {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		}
				

		private void currentLocation() {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Log.d("TestActivity","Enters here5");	
			String provider = locationManager
					.getBestProvider(new Criteria(), false);

			Location location = locationManager.getLastKnownLocation(provider);
	        
			if (location == null) {
				Log.d("TestActivity","Enters here4");	
				
				locationManager.requestLocationUpdates(provider, 0, 0, listener);
			} else {
				Log.d("TestActivity","Enters here2");	
				loc = location;
				markerLoc =loc;
				//new GetPlaces(TestActivity.this, places[0].toLowerCase().replace(
					//	"-", "_")).execute();
				Log.e(TAG, "location : " + location);
			}

		}	

		
		private LocationListener listener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onLocationChanged(Location location) {
				Log.e(TAG, "location update : " + location);
				loc = location;
				locationManager.removeUpdates(listener);
			}
		};	
		
		private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {
			private ProgressDialog dialog;
			private Context context;
			private String places;
			private Marker marker;

			public GetPlaces(Context context, String places, Marker added) {
				Log.d("TestActivity","PLACES PLZ " + places);
				this.context = context;
				this.places = places;
				this.marker = added;
			}

			@Override
			protected void onPostExecute(ArrayList<Place> result) {
				if(result.size()==0)
					return;
				super.onPostExecute(result);
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				for (int i = 0; i < result.size(); i++) {
					Log.d(result.get(i).getName(), result.get(i).getVicinity());
					String s = added.getTitle() + " - " + result.get(i).getName();
					Marker m = googleMap.addMarker(new MarkerOptions()
							.title(s)
							.position(new LatLng(result.get(i).getLatitude(),
									result.get(i).getLongitude()))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
							.snippet(result.get(i).getVicinity()));
					// .snippet(result.get(i).getFormattedAddress()));
					
				// Draw notification radius
					circleMap.put(m, drawCircle(new LatLng(result.get(i).getLatitude(), result.get(i).getLongitude())));

					
				// Add the pending intent
				addPendingIntent(s, result.get(i).getLatitude(), result.get(i).getLongitude());
				tempMarkerLat = m.getPosition().latitude;
				tempMarkerLong = m.getPosition().longitude;
				tempMarkerDescription = s;
				tempCategoryHead = "false";
				tempIsCategory = "true";
				// Sync with server
				// addMarkerToServer
				Log.d("GetPLACES", "ADDING MARKER NOW");
				addServerMarker();
				Log.d("GetPLACES", "ADDING MARKER DONE");

			}
				
				
				
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(result.get(0).getLatitude(), result
								.get(0).getLongitude())) // Sets the center of the map to
												// Mountain View
						.zoom(14) // Sets the zoom
						.tilt(30) // Sets the tilt of the camera to 30 degrees
						.build(); // Creates a CameraPosition from the builder
				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog = new ProgressDialog(context);

			}

			@Override
			protected ArrayList<Place> doInBackground(Void... arg0) {
				PlacesService service = new PlacesService("AIzaSyCJ5Lqx_DqMPmNiGn35lil0AsV81vkAzkA");
				ArrayList<Place> findPlaces = service.findPlaces(markerLoc.getLatitude(), // 28.632808
						markerLoc.getLongitude(), places); // 77.218276
				Log.d("TestActivity", "Logging this Shit");
	            if(findPlaces != null){
					for (int i = 0; i < findPlaces.size(); i++) {
		
						Place placeDetail = findPlaces.get(i);
						Log.e(TAG, "places : " + placeDetail.getName());
					}
					Log.d("TestActivity", "SIZE =" +Integer.toString(findPlaces.size()));
	            }	
				return findPlaces;
			}

		}		
			
	    //End of added code for Google places API	
	
	
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
        	
        case R.id.action_settings:
        	Intent intent = new Intent(TestActivity.this, SettingsActivity.class);
        	startActivityForResult(intent, settings_requestcode);
        	return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
	
	@Override
	public void onMapClick(LatLng position) {
		/*
		Toast.makeText(thisContext,
				"Lat: " + String.valueOf(position.latitude) + " Long: " + String.valueOf(position.longitude),
				Toast.LENGTH_SHORT).show();
				*/
			

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
		
		// Saving marker data
		tempMarkerLat = marker.getPosition().latitude;
		
		

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(marker.getTitle());
        final SharedPreferences.Editor editor = sharedPreferences.edit();
		// Set an EditText view to get user input
		alert.setPositiveButton("Complete task", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				tempMarkerDescription = marker.getTitle();  // Used to check which marker to delete inside deleteAMarker()
				
				// Deleting marker from database
				new DeleteAMarker().execute();
				
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
		
		alert.setNegativeButton("Snooze",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Snoozes alarm for 10 seconds because the user clicked
						marker.setVisible(false);
						Log.d("TestActivity", "Alarm Snoozed!");
						Toast.makeText(TestActivity.this, "Alarm snoozed!",
								Toast.LENGTH_SHORT).show();

						final Handler handler = new Handler();
						Timer t = new Timer();
						t.schedule(new TimerTask() {
							public void run() {
								handler.post(new Runnable() {
									public void run() {
										Log.d("TestActivity", "Enters Here!");
										marker.setVisible(true);
									}

								});
							}
						}, 10000);
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
		Log.d("TestActivity", "onResume");
		initializeMap();

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("TestActivity", "onActivityResult");
		Log.d("onActivityResult", String.valueOf(resultCode));
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
        		added = googleMap.addMarker(new MarkerOptions().position(tempPos).title(tempValue));
        		circleMap.put(added, drawCircle(tempPos));            // For debug purposes
				database.addMarker(added, tempValue);           // Adds marker to database
				
				String value1=data.getStringExtra("result1");
		        String value2=data.getStringExtra("result2");
		        String value3=data.getStringExtra("result3");
		        Log.d("TestActivity","VALUE1 is " + value1);
		        
				// Storing data to server
				tempMarkerLat = added.getPosition().latitude;
				tempMarkerLong = added.getPosition().longitude;
				tempMarkerDescription = tempValue;
				if(data.getBooleanExtra("categoryhead", false)){
					Log.d("onActivityResult", "wooo");
					tempCategoryHead = "true";
				}else{
					Log.d("onActivityResult", "boo");
					tempCategoryHead = "false";
				}
				tempIsCategory = "false";
				
				new AddAMarkerToServer().execute();
				
				// Setting up proximity alert
                Intent enteredRegionIntent = new Intent(TestActivity.this, GeoAlert.class);
                
                // Adding message for the notification system
                enteredRegionIntent.putExtra("message", tempValue);
                enteredRegionIntent.putExtra("latitude", tempPos.latitude);
                enteredRegionIntent.putExtra("longitude", tempPos.longitude);
                
                if(markerLoc != null && loc != null){
	                markerLoc = new Location(loc);
	                markerLoc.setLatitude(tempPos.latitude);
	                markerLoc.setLongitude(tempPos.longitude);
	                new GetPlaces(TestActivity.this,value1, added).execute();
	                new GetPlaces(TestActivity.this,value2, added).execute();
	                new GetPlaces(TestActivity.this,value3, added).execute();

	               
                }else{
                	Log.d("TestActivity-onActivityResult","GPS not turned on");
                	Toast.makeText(TestActivity.this, 
                			"Category Place It could not be added. Please turn on your GPS.", 
                			Toast.LENGTH_SHORT).show();
                }
                
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
		} else if (requestCode == settings_requestcode) {
			if (resultCode == RESULT_OK) {
				Log.d("onActivityResult", "Closing Activity");
				finish();
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
		} else {
			Toast.makeText(TestActivity.this, "Alarm has not been set!",
					Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * Add pendingIntent helper use in adding blue markers in GetPlaces()
	 */
	public void addPendingIntent(String message, double latitude,
			double longitude) {
		Intent enteredRegionIntent = new Intent(thisContext, GeoAlert.class);

		// Adding message for the notification system
		enteredRegionIntent.putExtra("message", message);
		enteredRegionIntent.putExtra("latitude", latitude);
		enteredRegionIntent.putExtra("longitude", longitude);

		// Location manager checks this pendingIntent when user enters
		// region. Goes to GeoAlert class if entered.
		pendingIntent = PendingIntent.getBroadcast(thisContext, 0,
				enteredRegionIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		// Adding a proximity alert on the marker based on radiusSize.
		// Expiration is -1, so it will never expire.
		locationManager.addProximityAlert(latitude, longitude,
				(long) radiusSize, -1, pendingIntent);
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
            
            if(tempTime != null){
            	Log.d("AddAMarker Alarm mode", tempTime);
            }else{
            	Log.d("AddAMarker Alarm Mode", "Alarm Mode not set!");
            }
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
			 Log.d("TestActivity", "addingmarker from deleteMarker()");
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
        	Log.d("InitializeMap", "Creating Map");
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap != null) {
            }
        }else if(googleMap != null){
        	Log.d("InitializeMap", "Map already created");
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
			Log.d(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
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
		if(locationClient != null){
			locationClient.requestLocationUpdates(locationRequest, this);	
		}
	}


	@Override
	public void onDisconnected() {
		// Display the connection status 
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();		
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			Toast.makeText(this, "FAILURE!", Toast.LENGTH_LONG).show();
		}
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

    private class UpdateTask extends AsyncTask<String, Void, List<String>> {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		 @Override
	     protected List<String> doInBackground(String... url) {
	    	    HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url[0]);
				List<String> list = new ArrayList<String>();
				try {
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();
					String data = EntityUtils.toString(entity);
					Log.d("TAG", data);
					JSONObject myjson;
					
					try {
						myjson = new JSONObject(data);
						JSONArray array = myjson.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject obj = array.getJSONObject(i);
							if(preferences.getString("Username", null).equals(obj.get("product").toString())){
								list.add(obj.get("name").toString());
								latitude.add(Double.parseDouble(obj.get("lat").toString()));
								longtitude.add(Double.parseDouble(obj.get("long").toString()));
								categoryHeadList.add(obj.get("categoryhead").toString());
								isCategoryList.add(obj.get("iscategory").toString());
							}
						}
					} catch (JSONException e) {

				    	Log.d("TAG", "Error in parsing JSON");
					}
					
				} catch (ClientProtocolException e) {

			    	Log.d("TAG", "ClientProtocolException while trying to connect to GAE");
				} catch (IOException e) {

					Log.d("TAG", "IOException while trying to connect to GAE");
				}
	         return list;
	     }
		 
    } // UpdateTask class
    
    /*
     * Add a marker to server
     */
    private class AddAMarkerToServer extends AsyncTask<Void, Void, Void>{
    	ProgressDialog dialog;
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
    	protected void onPreExecute() {
			dialog = new ProgressDialog(TestActivity.this);
			dialog.setMessage("Posting data, please wait...");
			dialog.show();

		}
    	
    	@Override
		protected Void doInBackground(Void... params) {
    	    Log.d("addMarkerServer", preferences.getString("Username", null));
    	    HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(Database.ITEM_URI);

		    try {


		      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
		      nameValuePairs.add(new BasicNameValuePair("name", tempMarkerDescription));
		      nameValuePairs.add(new BasicNameValuePair("product", preferences.getString("Username", null)));
		      nameValuePairs.add(new BasicNameValuePair("long", String.valueOf(tempMarkerLong)));
		      nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(tempMarkerLat)));
		      nameValuePairs.add(new BasicNameValuePair("categoryhead", tempCategoryHead));
		      nameValuePairs.add(new BasicNameValuePair("iscategory", tempIsCategory));
		      nameValuePairs.add(new BasicNameValuePair("action",  "put"));
		      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		      HttpResponse response = client.execute(post);
		      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		      String line = "";
		      while ((line = rd.readLine()) != null) {
		        Log.d(TAG, line);
		      }

		    } catch (IOException e) {
		    	Log.d(TAG, "IOException while trying to conect to GAE");
		    }
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void v) {
			dialog.dismiss();

		}	

	} // End of AddAMarker class
    /*
     * Add a marker to server
     */
    private void addServerMarker(){
    	final ProgressDialog dialog = ProgressDialog.show(this, "Posting Data...", "Please wait...", false);
    	final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
	    Log.d("addMarkerServer", preferences.getString("Username", null));
	    
		Thread t = new Thread() {
			
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Database.ITEM_URI);
 
			    try {


			      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
			      nameValuePairs.add(new BasicNameValuePair("name", tempMarkerDescription));
			      nameValuePairs.add(new BasicNameValuePair("product", preferences.getString("Username", null)));
			      nameValuePairs.add(new BasicNameValuePair("long", String.valueOf(tempMarkerLong)));
			      nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(tempMarkerLat)));
			      nameValuePairs.add(new BasicNameValuePair("categoryhead", tempCategoryHead));
			      nameValuePairs.add(new BasicNameValuePair("iscategory", tempIsCategory));
			      nameValuePairs.add(new BasicNameValuePair("action",  "put"));
			      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			      HttpResponse response = client.execute(post);
			      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			      String line = "";
			      while ((line = rd.readLine()) != null) {
			        Log.d(TAG, line);
			      }

			    } catch (IOException e) {
			    	Log.d(TAG, "IOException while trying to conect to GAE");
			    }
				dialog.dismiss();
			}
		};

		t.start();
		dialog.show();
	}

    
    /*
     * Deletes marker data from server
     */
    private class DeleteAMarker extends AsyncTask<Void, Void, Boolean>{
    	boolean categoryChecker = false;
    	private ProgressDialog dialog;
    	private  SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


		protected void onPreExecute() {
			dialog = new ProgressDialog(TestActivity.this);
			dialog.setMessage("Posting data, please wait...");
			dialog.show();

		}
    	
		@Override
		protected Boolean doInBackground(Void... params) {

			// If they are deleted we want to refresh the activity, otherwise we don't
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(Database.ITEM_URI);

			HttpPost delete = new HttpPost(Database.ITEM_URI);
			
			try {
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				String data = EntityUtils.toString(entity);
				Log.d(TAG, data);
				JSONObject myjson;
				
				try {
					myjson = new JSONObject(data);
					JSONArray array = myjson.getJSONArray("data");
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						// First delete references of the marker
						if(obj.get("product").toString().equals(preferences.getString("Username", null)) &&
								obj.get("name").toString().equals(tempMarkerDescription)){
								
								Log.d("DeleteAMarker()", "Removed from " + obj.get("product"));
								List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
							    nameValuePairs.add(new BasicNameValuePair("id", obj.get("name").toString()));
							    nameValuePairs.add(new BasicNameValuePair("action", "delete"));
							    
							    delete.setEntity(new UrlEncodedFormEntity(nameValuePairs));
							    client.execute(delete);
							    // Next check if the marker is a category
							}else if(obj.get("iscategory").equals("true")){ 
								// Set categoryChecker to true
								categoryChecker = true;
								// Format the string, get rid of the name of the place
								// so that only the message remains
								int index = obj.get("name").toString().indexOf(" - ");
								String temp = obj.get("name").toString().substring(0, index);
								Log.d("deleting category", temp);
								if(temp.equals(tempMarkerDescription)){
									Log.d("DeleteAMarker()", "Removed from " + obj.get("product"));
									List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
								    nameValuePairs.add(new BasicNameValuePair("id", obj.get("name").toString()));
								    nameValuePairs.add(new BasicNameValuePair("action", "delete"));
								    
								    delete.setEntity(new UrlEncodedFormEntity(nameValuePairs));
								    client.execute(delete);
								}
								// Finally check if it is just a normal marker being deleted
								} else if (obj.get("categoryhead").toString().equals("false") &&
										obj.get("name").toString().equals(tempMarkerDescription)) {
									
									Log.d("DeleteAMarker()", "Removed from " + obj.get("product"));
									List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
								    nameValuePairs.add(new BasicNameValuePair("id", obj.get("name").toString()));
								    nameValuePairs.add(new BasicNameValuePair("action", "delete"));
								    
								    delete.setEntity(new UrlEncodedFormEntity(nameValuePairs));
								    client.execute(delete);
								    break;

								}
					}

				} catch (JSONException e) {

					Log.d(TAG, "Error in parsing JSON");
				}

			} catch (ClientProtocolException e) {

				Log.d(TAG,
						"ClientProtocolException while trying to connect to GAE");
			} catch (IOException e) {

				Log.d(TAG, "IOException while trying to connect to GAE");
			}

		return categoryChecker;
		
	}
		@Override
		protected void onPostExecute(Boolean b) {
	            dialog.dismiss();
	        
			if (b) {
				Log.d("PostExecute", "Refreshing activity");
				refreshThisActivity();
			}
		}
		
    } // Delete a marker class
    
    /*
     * Refreshes the activity if any category place its deleted
     */
    public void refreshThisActivity(){
    	recreate();
    }


   


}
