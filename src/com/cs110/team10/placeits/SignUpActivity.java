package com.cs110.team10.placeits;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity{
	private Rect rect;    // hold the bounds of the login button

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
        final EditText usernameText = (EditText) findViewById(R.id.yourUsername);
        usernameText.setTypeface(titleTypeface);
		
        
        // Password
        final EditText passwordText = (EditText) findViewById(R.id.yourPassword);
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
						Toast.makeText(SignUpActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(SignUpActivity.this, TestActivity.class);
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
        
	}

}
