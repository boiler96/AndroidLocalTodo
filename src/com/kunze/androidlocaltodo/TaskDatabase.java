package com.kunze.androidlocaltodo;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDatabase extends SQLiteOpenHelper {
	
	private static final String DB_NAME		  = "TASK_DATABASE";
	private static final int 	DB_VERSION	  = 1;
	private static final String DB_TABLE_NAME = "TASKS";
	private static final String DB_TASK_NAME  = "NAME";
	
    private static final String DB_TABLE_CREATE =
            "CREATE TABLE " + DB_TABLE_NAME + " (" +
            DB_TASK_NAME + " TEXT, " +
            "_id AUTONUMBER);";


	public TaskDatabase(Context context) 
	{
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(DB_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// TODO Auto-generated method stub

	}
	
	public void AddTask(String task)
	{
		SQLiteDatabase db = getWritableDatabase();
		ContentValues vals = new ContentValues();
		vals.put(DB_TASK_NAME, task);
		db.insert(DB_TABLE_NAME, null, vals);
		db.close();
	}

	public Cursor GetCursor()
	{
		SQLiteDatabase db = getReadableDatabase();
		return db.query(DB_TABLE_NAME, null, null, null, null, null, null);
	}
	
	public void Remove()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.close();
		SQLiteDatabase.deleteDatabase(new File(db.getPath()));
	}
}
