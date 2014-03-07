package com.cs110.team10.placeits;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity{
	private Rect rect;    // hold the bounds of the login button
	private int requestCode = 1; // Used to start SignUpActivity
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        

        // Set the typefaces
        Typeface titleTypeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        Typeface buttonTypeface = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Bold.ttf");

        // Title Text
        TextView title = (TextView)findViewById(R.id.title);
        title.setTypeface(titleTypeface);
        

        // Username
        final EditText usernameText = (EditText) findViewById(R.id.username_edit);
        usernameText.setTypeface(titleTypeface);
		
        
        // Password
        final EditText passwordText = (EditText) findViewById(R.id.password_edit);
        passwordText.setTypeface(titleTypeface);
        
        // Create account textView
        final TextView createAccount = (TextView) findViewById(R.id.createAccount);
        createAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
				startActivityForResult(intent, requestCode);
			}
		});
        
        // Login Button
        final Button loginButton = (Button)findViewById(R.id.btn_login);
        loginButton.setTypeface(buttonTypeface);
       
        
      loginButton.setOnTouchListener(new OnTouchListener() {
		
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
						Toast.makeText(LoginActivity.this, "Please enter your username.", Toast.LENGTH_SHORT).show();
						startMap = false;
					 }else if(passwordText.getText().length() == 0){
						Toast.makeText(LoginActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
						startMap = false;

					 }
						 
					// Start the map
					if (startMap) {
						Intent intent = new Intent(LoginActivity.this, TestActivity.class);
						startActivity(intent);
						finish();
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
        
        
        
        
        
        
    } // onCreate

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("LoginActivity", "onActivityResult");
		// If map is started, finish this activity.
		// This prevents pressing the 'back' button and LoginActivity will still be running
		if(resultCode == RESULT_OK){       
			finish();
		}
	}
	
	
	
}
