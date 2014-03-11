package com.cs110.team10.placeits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class activeList extends Activity{
	
    private ListView listView;		//For list view
    private Marker marker;
    
    List<Double> lati = new ArrayList<Double>();
    List<Double> longi = new ArrayList<Double>();
    

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activelist);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);
        super.onCreate(savedInstanceState);
        
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);
        // Defined Array values to show in ListView
        List<String> values = new ArrayList<String>();
        values.addAll(getData());
        
        /*
        for(Entry<Marker, String> entry : TestActivity.database.markers.entrySet())
        {
			 marker = entry.getKey();
			 String name = TestActivity.database.getMarkerName(marker);	
		     values.add(name);
		 }
		 */

        // Define a new Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
           R.layout.activemarkers, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {                   
            	  double lat;
                  double lon;
               // ListView Clicked item index
               for(int k = 0; k < lati.size(); k++)
               {

                   lat = lati.get(k);
                   lon = longi.get(k);
                   TestActivity.googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
                   Log.d("position", String.valueOf(lat));
                   Log.d("position", String.valueOf(lon));
            	   if(k == position)
            	   {
            		   break;
            	   }
               }
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
	
	private List<String> getData()
	{
		//Makes a GET HTTP request like 
		// AddItemActivity
		List<String> info;
		try {
			info = new UpdateTask().execute(Database.ITEM_URI).get();
			return info;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private class UpdateTask extends AsyncTask<String, Void, List<String>> {
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
							if(LoginActivity.usernameText.getText().toString().equals(obj.get("product").toString())){
								list.add(obj.get("name").toString());
								lati.add(Double.parseDouble(obj.get("lat").toString()));
								longi.add(Double.parseDouble(obj.get("long").toString()));
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
	 }


}
