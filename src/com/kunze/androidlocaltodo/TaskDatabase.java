package com.kunze.androidlocaltodo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDatabase extends SQLiteOpenHelper {
    
    static public class Task
    {
        public static enum RepeatUnit { NONE, DAYS, WEEKS, MONTHS, YEARS };

        public String     mName;
        public String     mDescription;
        public Calendar   mDueDate;
        public Calendar   mCompletedDate;
        public RepeatUnit mRepeatUnit;
        public int        mRepeatTime;
        public Boolean    mRepeatFromComplete;
        public long       mID;
        
    }

    private static final String DB_NAME		                 = "TASK_DATABASE";
    private static final int 	DB_VERSION	                 = 1;
    private static final String DB_TABLE_NAME                = "TASKS";
    private static final String DB_TASK_NAME                 = "NAME";
    private static final String DB_TASK_DESCRIPTION          = "DESCRIPTION";
    private static final String DB_TASK_DUE_DATE             = "DUE_DATE";
    private static final String DB_TASK_COMPLETED_DATE       = "COMPLETED_DATE";
    private static final String DB_TASK_REPEAT_UNIT          = "REPEAT_UNIT";
    private static final String DB_TASK_REPEAT_TIME          = "REPEAT_TIME";
    private static final String DB_TASK_REPEAT_FROM_COMPLETE = "REPEAT_FROM_COMPLETE";
    private static final String DB_ID                        = "_id";

    private static final String DB_TABLE_CREATE =
            "CREATE TABLE " + DB_TABLE_NAME + " (" +
                    DB_TASK_NAME + " TEXT, " +
                    DB_TASK_DESCRIPTION + " TEXT, " +
                    DB_TASK_DUE_DATE + " INTEGER, " +
                    DB_TASK_COMPLETED_DATE + " INTEGER, " +
                    DB_TASK_REPEAT_UNIT + " TEXT, " +
                    DB_TASK_REPEAT_TIME + " INTEGER, " +
                    DB_TASK_REPEAT_FROM_COMPLETE + " INTEGER, " +
                    DB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT);";
    private static final String DB_WHERE = DB_TASK_COMPLETED_DATE + "= 0"; 
    private static final String DB_ORDER_BY = DB_TASK_DUE_DATE + " ASC";             

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

    public void AddTask(Task task)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues vals = GetContentValues(task);
        db.insert(DB_TABLE_NAME, null, vals);
        db.close();
    }

    public Cursor GetCursor()
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(DB_TABLE_NAME, null, DB_WHERE, null, null, null, DB_ORDER_BY);
    }
    
    public Task LoadTask(Cursor cursor)
    {
        Task task = new Task();
        try
        {
            task.mName = cursor.getString(0);
            task.mDescription = cursor.getString(1);
            task.mDueDate = ConvertIntToDate(cursor.getInt(2));
            task.mCompletedDate = ConvertIntToDate(cursor.getInt(3));
            task.mRepeatUnit = Task.RepeatUnit.valueOf(cursor.getString(4));
            task.mRepeatTime = cursor.getInt(5);
            task.mRepeatFromComplete = cursor.getInt(6) == 1;
            task.mID = cursor.getLong(7);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return task;
    }
    
    public void SaveTask(Task task)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues vals = GetContentValues(task);
        db.update(DB_TABLE_NAME, vals, DB_ID + "=" + task.mID, null);
        db.close();
    }

    public void Remove()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.close();
        SQLiteDatabase.deleteDatabase(new File(db.getPath()));
    }
    
    private ContentValues GetContentValues(Task task)
    {
        ContentValues vals = new ContentValues();
        vals.put(DB_TASK_NAME, task.mName);
        vals.put(DB_TASK_DESCRIPTION, task.mDescription);
        vals.put(DB_TASK_DUE_DATE, ConvertDateToInt(task.mDueDate));
        vals.put(DB_TASK_COMPLETED_DATE, ConvertDateToInt(task.mCompletedDate));
        vals.put(DB_TASK_REPEAT_UNIT, task.mRepeatUnit.toString());
        vals.put(DB_TASK_REPEAT_TIME, task.mRepeatTime);
        vals.put(DB_TASK_REPEAT_FROM_COMPLETE, task.mRepeatFromComplete ? 1 : 0);
        return vals;
    }
    
    public static Calendar ConvertIntToDate(int day)
    {
    	long ms = (long)day * 24 * 60 * 60 * 1000;
    	Calendar date = Calendar.getInstance();
    	date.setTimeInMillis(ms - date.get(Calendar.ZONE_OFFSET));
    	return date;
    }
    
    public static int ConvertDateToInt(Calendar date) {
    	long ms = date.getTimeInMillis() + date.get(Calendar.ZONE_OFFSET);
    	int day = (int)(ms / 1000 / 60 / 60 / 24);
    	return day;
    }
}
