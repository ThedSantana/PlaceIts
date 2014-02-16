package com.cs110.team10.placeits;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.Map.Entry;



import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class activeList extends Activity{
	
    private ListView listView;		//For list view
    private Marker marker;
    

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activelist);
        
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);
        // Defined Array values to show in ListView
        ArrayList<String> values = new ArrayList<String>();

        for(Entry<Marker, String> entry : TestActivity.database.markers.entrySet())
        {
			 marker = entry.getKey();
			 String name = TestActivity.database.getMarkerName(marker);	
		     values.add(name);
		 }

        // Define a new Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
           R.layout.activemarkers, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {                   
               // ListView Clicked item index
               int itemPosition = 0; 
               // ListView Clicked item value
               for(Entry<Marker, String> entry : TestActivity.database.markers.entrySet())
               {
            	   marker = entry.getKey();
            	   if(itemPosition == position)
            	   {
            	 	   break;
            	   }
            	   itemPosition++;
               }
               double lat = marker.getPosition().latitude;
               double lon = marker.getPosition().longitude;
               TestActivity.googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
   			   TestActivity.googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
   			   finish();
              }    
         }); 
        
        Button button = (Button) findViewById(R.id.okay);
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    }

}
