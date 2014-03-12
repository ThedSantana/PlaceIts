package com.cs110.team10.placeits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	public static final String TAG = "SettingsActivity";

	private String tempUsername;  // Temp username used in Delete Account
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.xml.settings_layout);
	        Preference signoutPref = (Preference) findPreference("pref_key_signout");
	        Preference deleteAccountPref = (Preference)findPreference("pref_key_delete_account");
	        signoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	                     public boolean onPreferenceClick(Preference preference) {
	                    	 if (preference.getKey().equals("pref_key_signout")) {
	                			 AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
	                				alert.setTitle("Sign Out");
	                				alert.setMessage("Do you want to sign out of this account?");
	                				
	                				alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                					public void onClick(DialogInterface dialog, int whichButton) {
	                								// Check if user has logged in already
	                								SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	                								if (sharedPreferences.contains("Username")) {
	                									SharedPreferences.Editor editor = sharedPreferences.edit();
	                									editor.remove("Username");
	                									editor.commit();
	                									Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
	            										//LoginActivity.usernameText = null;
	                									startActivity(intent);
	                									setResult(RESULT_OK);
	                									finish();

	                								}else{
	                									Toast.makeText(SettingsActivity.this, "No user found!", Toast.LENGTH_SHORT).show();
	                								}
	                						}
	                				});
	                				
	                				alert.setNegativeButton("Cancel",
	                						new DialogInterface.OnClickListener() {
	                							public void onClick(DialogInterface dialog, int whichButton) {
	                								setResult(RESULT_CANCELED);
	                								finish();
	                							}
	                				});
	                				
	                				final AlertDialog alertDialog = alert.create();
	                				alertDialog.show();
	                				
	                    	 }
							return true;
	                     }
	        });
	        
		deleteAccountPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						if (preference.getKey().equals("pref_key_delete_account")) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
							alert.setTitle("Delete Account");
							alert.setMessage("Do you want to permanently of this account?");

							alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									// Check if user has logged in already
									SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
									// First Delete the sharedPreference
									if (sharedPreferences.contains("Username")) {
										// Store the temp username
										tempUsername = sharedPreferences.getString("Username", null);
										
										// Next delete the account from the server
										deleteAccount();
										// Next Delete the shared preference
										SharedPreferences.Editor editor = sharedPreferences.edit();
										editor.clear();
										//editor.remove("Username");
										editor.commit();

										Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
										// LoginActivity.usernameText = null; // Changed usernameText in loginactivity to private, test this out
    									startActivity(intent);
										setResult(RESULT_OK);
										finish();

									} else {
										Toast.makeText(SettingsActivity.this, "No user found!", Toast.LENGTH_SHORT).show();
									}
								}
							});

							alert.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
											setResult(RESULT_CANCELED);
											finish();
										}
							});

							final AlertDialog alertDialog = alert.create();
							alertDialog.show();

						}

						return true;

					}
				});
	        
	    }
	 private void deleteAccount(){
			final ProgressDialog dialog = ProgressDialog.show(this, "Posting Data...", "Please wait...", false);
			Thread t = new Thread() {
				

			public void run(){
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(Database.PRODUCT_URI);

			HttpPost delete = new HttpPost(Database.PRODUCT_URI);
			
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
						if(obj.get("name").toString().equals(tempUsername)){
							Log.d("SettingsActivity", "Removed " + tempUsername);
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
			dialog.dismiss();

	     }
			};
			t.start();
			dialog.show();



	 }



}
