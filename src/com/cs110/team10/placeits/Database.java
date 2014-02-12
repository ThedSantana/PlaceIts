package com.cs110.team10.placeits;


import java.util.HashMap;

import android.util.Log;


import com.google.android.gms.maps.model.Marker;

public class Database {
	private HashMap<Marker, HashMap<String, Boolean>>  daysPicked;
	private HashMap<Marker, String> markers;
	
	
	public Database(){
        markers = new HashMap<Marker, String>();
		daysPicked = new HashMap<Marker, HashMap<String, Boolean>>();
	}
	
	
	

	
	public void addDaysPicked(Marker marker, HashMap<String, Boolean> days){
		daysPicked.put(marker, days);
	}
	
	public void removeDaysPicked(Marker marker){
		if(daysPicked.containsKey(marker)){
			Log.d("Database", "Marker removed!");

			daysPicked.remove(marker);
		}else{
			Log.d("Database", "Marker failed to remove, not in daysPicked map");
			
		}
	}
	
	public void addMarker(Marker marker, String message){
		markers.put(marker, message);
	}
	
	public HashMap<Marker,String> getMarkerList(){
		return markers;
	}


	

}
