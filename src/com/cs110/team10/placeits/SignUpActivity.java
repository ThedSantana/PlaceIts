package com.cs110.team10.placeits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity{
	private Rect rect;    // hold the bounds of the login button
	
	private EditText usernameText;
	private EditText passwordText;
		
	private static final String TAG = "SignUpActivity";


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup_layout);
		
		
		 // Set the typefaces
        Typeface titleTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        Typeface buttonTypeface = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");
        

        // Title Text
        TextView title = (TextView)findViewById(R.id.signUpTextView);
        title.setTypeface(titleTypeface);
        

        // Username
        usernameText = (EditText) findViewById(R.id.yourUsername);
        usernameText.setTypeface(titleTypeface);
		
        
        // Password
        passwordText = (EditText) findViewById(R.id.yourPassword);
        passwordText.setTypeface(titleTypeface);
        
     // Create account button Button
        final Button createAccountButton = (Button)findViewById(R.id.btn_create_account);
        createAccountButton.setTypeface(buttonTypeface);
       
        
      createAccountButton.setOnTouchListener(new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			switch(event.getAction()) {
			
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				boolean startMap = true;
				v.setBackground(getResources().getDrawable(R.drawable.buttonbackground));
				
				// Checks if user's finger is lifted up while on the button
				 if(rect.contains(rect.left + (int)event.getX(), rect.top + (int)event.getY())){
					// Check if Username/password fields are filled in
					 if(usernameText.getText().length() == 0){
						Toast.makeText(SignUpActivity.this, "Please enter your username.", Toast.LENGTH_SHORT).show();
						startMap = false;
					 }else if(passwordText.getText().length() == 0){
						Toast.makeText(SignUpActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
						startMap = false;

					 }
						 
					// Start the map
					if (startMap) {
						isUsernameInDatabase();
					}
				}
				return true;
				
			case MotionEvent.ACTION_DOWN:
				v.setBackground(getResources().getDrawable(R.drawable.buttonselected));
		        rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
			    return true;
			
			default:
				return false;
			        
			}
		}
	});
        
	} // onCreate()
	
	/*
	 * Sends username and password to /product page
	 */
	private void sendData(){
		final ProgressDialog dialog = ProgressDialog.show(this, "Posting Data...", "Please wait...", false);

		Thread t = new Thread() {

			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Database.PRODUCT_URI);

			    try {
			    	
			      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			      nameValuePairs.add(new BasicNameValuePair("name", usernameText.getText().toString()));
			      nameValuePairs.add(new BasicNameValuePair("description", passwordText.getText().toString()));
			      nameValuePairs.add(new BasicNameValuePair("action", "put"));
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
	 * Checks if username currently is in database
	 */
	private void isUsernameInDatabase(){
		final ProgressDialog dialog = ProgressDialog.show(this, "Posting Data...", "Please wait...", false);
		Thread t = new Thread() {
			
		 public void run(){
			 HttpClient client = new DefaultHttpClient();
			 HttpGet request = new HttpGet(Database.PRODUCT_URI);
				try {
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();
					String data = EntityUtils.toString(entity);
					Log.d(TAG, data);
					JSONObject myjson;
					
					try {
						myjson = new JSONObject(data);
						JSONArray array = myjson.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject obj = array.getJSONObject(i);
							// Checks if userNameText editBox is the same as database username
							if(usernameText.getText().toString().equals( obj.get("name").toString() )){
								dialog.dismiss();
								// The below line makes Toasts work in Threads
								SignUpActivity.this.runOnUiThread(new Runnable(){
						            public void run(){
										Toast.makeText(SignUpActivity.this, "Username is not available. Try a new username.", Toast.LENGTH_SHORT).show();
						            }
						        });
								return;
							}
						}
						
					} catch (JSONException e) {
	
				    	Log.d(TAG, "Error in parsing JSON");
					}
					
				} catch (ClientProtocolException e) {
	
			    	Log.d(TAG, "ClientProtocolException while trying to connect to GAE");
				} catch (IOException e) {
	
					Log.d(TAG, "IOException while trying to connect to GAE");
				}
				
				// This is a unique username so, create the username and start the map
				dialog.dismiss();
				// The below line makes Toasts work in Threads
				SignUpActivity.this.runOnUiThread(new Runnable(){
		            public void run(){
						sendData();                  // Sends username and password to /product page
						Toast.makeText(SignUpActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
		            }
		        });
				Intent intent = new Intent(SignUpActivity.this, TestActivity.class);
				startActivity(intent);	
				setResult(RESULT_OK);
				finish();

		 	} // run()
		}; // new Thread()
		t.start();
		dialog.show();
	}

}
