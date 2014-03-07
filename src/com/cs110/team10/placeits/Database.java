package com.cs110.team10.placeits;


import java.util.HashMap;

import android.util.Log;


import com.google.android.gms.maps.model.Marker;

public class Database {
	
	public static final String PRODUCT_URI = "http://mobiletestlab1.appspot.com/product";
	public static final String ITEM_URI = "http://mobiletestlab1.appspot.com/item";
	
	public  HashMap<Marker, String>  timePicked;
	public  HashMap<Marker, String> markers;
	
	
	public Database(){
        markers = new HashMap<Marker, String>();
		timePicked = new HashMap<Marker, String>();
	}
	
	
	

	public String getMarkerName(Marker marker) {
		return (String) markers.get(marker);
	}
	
	public void removeDaysPicked(Marker marker){
		if(timePicked.containsKey(marker)){
			Log.d("Database", "Marker removed!");

			timePicked.remove(marker);
		}else{
			Log.d("Database", "Marker failed to remove, not in timePicked map");
			
		}
	}
	
	public void addMarker(Marker marker, String message){
		markers.put(marker, message);
	}
	
	public HashMap<Marker,String> getMarkerList(){
		return markers;
	}
	

	public void addTime(Marker marker, String s){
		timePicked.put(marker,s);
	}
	

}
