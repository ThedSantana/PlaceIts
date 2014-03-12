package com.cs110.team10.placeits;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class CategoryChooser extends Activity {
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spinner_layout);
		setTitle("Choose 3 Categories");
		
		
		final Spinner spinner = (Spinner) findViewById(R.id.spinner_textos);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.places, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
		
		final Spinner spinner2 = (Spinner) findViewById(R.id.spinner_textos_carpetas);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
		        R.array.places, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner2.setAdapter(adapter);
		
		final Spinner spinner3 = (Spinner) findViewById(R.id.spinner_titulo_carpetas);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
		        R.array.places, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner3.setAdapter(adapter);
		

		
		    Button confirmOption = (Button) this.findViewById(R.id.chooseCategory);
			Button cancelOption = (Button) this.findViewById(R.id.chooseCatCancel);
			//builder.show();
			
			confirmOption.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d("CategoryChooser","CLICKED");
			    	Toast.makeText(CategoryChooser.this, "Notes added!", Toast.LENGTH_SHORT).show();
			    	
			        String value1 = spinner.getSelectedItem().toString();
			        String value2 = spinner2.getSelectedItem().toString();
			        String value3 = spinner3.getSelectedItem().toString();
			        Log.d("CategoryChooser","CAT is " + value1);
			    	Intent returnIntent = new Intent();
			    	 returnIntent.putExtra("result1",value1);
			    	 returnIntent.putExtra("result2",value2);
			    	 returnIntent.putExtra("result3",value3);
			    	 setResult(RESULT_OK,returnIntent);     
			    	 finish();
			    	
		          
					
				}
			});
			
			cancelOption.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(CategoryChooser.this, "Nothing added!", Toast.LENGTH_SHORT).show();
					setResult(RESULT_CANCELED);
			    	finish();
				}
			});

		
		
		
		
		
	}

	
}
