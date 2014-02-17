/*
 * This is an example test project created in Eclipse to test NotePad which is a sample 
 * project located in AndroidSDK/samples/android-11/NotePad
 * 
 * 
 * You can run these test cases either on the emulator or on device. Right click
 * the test project and select Run As --> Run As Android JUnit Test
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

package com.cs110.team10.placeits;

import com.robotium.solo.Solo;
import com.cs110.team10.placeits.TestActivity;

import android.test.ActivityInstrumentationTestCase2;



public class BDDTester extends ActivityInstrumentationTestCase2<TestActivity>{

	private Solo solo;
	public BDDTester() {
		super(TestActivity.class);

	}

	@Override
	public void setUp() throws Exception {
		//setUp() is run before a test case is started. 
		//This is where the solo object is created.
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		//tearDown() is run after a test case has finished. 
		//finishOpenedActivities() will finish all the activities that have been opened during the test execution.
		solo.finishOpenedActivities();
	}

	public void testAllBDD() throws Exception {
		//Given Add Note and the user clicks on the note
		
		solo.clickOnActionBarItem(R.id.action_add_note);
		//And Map is clicked 
		solo.clickOnText("Tap on a spot to add a note");
		//when they Click on menu item
		solo.clickOnButton("Cancel");
		boolean notesFound = solo.searchText("Cancel") && solo.searchText("Ok");
		//Assert that Note 1 & Note 2 are found
		assertFalse("The prompt disappeared", notesFound); 


		//Given Add Note and the user clicks on the note
		solo.clickOnActionBarItem(R.id.action_add_note);
		//And Map is clicked 
		solo.clickOnText("Tap on a spot to add a note");
		//when they Click on ok
		solo.clickOnButton("Ok");
		//message appears
		boolean search =solo.searchText("Please add some text");
		assertTrue("Please add text", search);
		solo.clickOnButton("Cancel");	
		


		//Given Add Note and the user clicks on the note
		solo.clickOnActionBarItem(R.id.action_add_note);
		//And Map is clicked 
		solo.clickOnText("Tap on a spot to add a note");
		//when they enter text and click ok
		solo.enterText(0, "First Note");
		solo.clickOnButton("Ok");
		//then interval thing appears
		boolean find = solo.searchText("Weekly");
		assertTrue("Reminder window poped up", find);
		solo.clickOnButton("Cancel");
		
		
		//Given Add Note and the user clicks on the note
		solo.clickOnActionBarItem(R.id.action_add_note);
		//And Map is clicked 
		solo.clickOnText("Tap on a spot to add a note");
		//when they enter text and click ok
		solo.enterText(0, "Second Note");
		solo.clickOnButton("Ok");
		//then 
		solo.clickOnButton("Confirm");
		solo.takeScreenshot();
		solo.clickOnText("Notes added!");
		//when they enter text and click complete task
		solo.clickOnButton("Complete task");
		solo.takeScreenshot();
		
		solo.drag(500, 600, 500, 600, 1);
		
		//Given Add Note and the user clicks on the note
		solo.clickOnActionBarItem(R.id.action_add_note);
		//And Map is clicked 
		solo.clickOnText("Tap on a spot to add a note");
		//when they enter text and click ok
		solo.enterText(0, "Third Note");
		solo.clickOnButton("Ok");
		//then click on radio button
		solo.clickOnRadioButton(0);
		solo.clickOnButton("Confirm");
		//solo.takeScreenshot();
		
		solo.drag(400, 400, 400, 400, 3);
 		//solo.drag(450, 400, 450, 400, 3);
		
		solo.clickOnActionBarItem(R.id.action_add_note);
		solo.clickOnText("Tap on a spot to add a note");
		//when they enter text and click remove alarm
		solo.clickOnButton("Remove Alarm");
		solo.takeScreenshot();
		
		
		solo.drag(300, 400, 300, 400, 1);
		
		//Given Add Note and the user clicks on the note
		solo.clickOnActionBarItem(R.id.action_add_note);
		//And Map is clicked 
		solo.clickOnText("Tap on a spot to add a note");
		//when they enter text and click ok
		solo.enterText(0, "Fourth Note");
		solo.clickOnButton("Ok");
		//then click on radio button
		solo.clickOnRadioButton(1);
		solo.clickOnButton("Confirm");
		solo.takeScreenshot();
		solo.drag(400, 400, 400, 400, 3);
		
		solo.clickOnActionBarItem(R.id.action_add_note);
		solo.clickOnText("Tap on a spot to add a note");
		//when they enter text and click snooze
		solo.clickOnButton("Snooze");
		solo.takeScreenshot();
		
		
		//Given that click on active items 
		solo.clickOnActionBarItem(R.id.action_search);
		solo.clickOnText("Fourth Note");
		
		 solo.drag(400, 400, 400, 400, 3);
		
		//click on active items 
		solo.clickOnActionBarItem(R.id.action_search);
		solo.clickOnButton("Okay");
		solo.takeScreenshot();
		
		
		
		
		
		
		
		
	}
}
