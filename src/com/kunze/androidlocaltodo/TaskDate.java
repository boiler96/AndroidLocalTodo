package com.kunze.androidlocaltodo;

import java.util.Calendar;
import java.util.Date;

public class TaskDate {
	
	public TaskDate(int days) {
		mDays = days;
	}
	
	static public TaskDate CreateToday() {
		Date today = new Date();
		Calendar calendar = Calendar.getInstance();
		return new TaskDate(0);
	}
	
	
	private int mDays;		// Number of days from January 1, 1970, local time

}
