package com.cs110.team10.placeits;

import java.util.HashMap;

public class Database {
	private HashMap<String, Boolean> daysPicked;
	
	
	public static void main(String[] args) {
		
	}
	
	public void addDaysPicked(HashMap<String, Boolean> days){
		this.daysPicked = days;		
	}

}
