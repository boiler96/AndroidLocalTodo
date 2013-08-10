package com.kunze.androidlocaltodo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        public Date       mDueDate;
        public Date       mCompletedDate;
        public RepeatUnit mRepeatUnit;
        public int        mRepeatTime;
        public long       mID;
        
    }

    private static final String DB_NAME		            = "TASK_DATABASE";
    private static final int 	DB_VERSION	            = 1;
    private static final String DB_TABLE_NAME           = "TASKS";
    private static final String DB_TASK_NAME            = "NAME";
    private static final String DB_TASK_DESCRIPTION     = "DESCRIPTION";
    private static final String DB_TASK_DUE_DATE        = "DUE_DATE";
    private static final String DB_TASK_COMPLETED_DATE  = "COMPLETED_DATE";
    private static final String DB_TASK_REPEAT_UNIT     = "REPEAT_UNIT";
    private static final String DB_TASK_REPEAT_TIME     = "REPEAT_TIME";
    private static final String DB_ID                   = "_id";

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private static final String DB_TABLE_CREATE =
            "CREATE TABLE " + DB_TABLE_NAME + " (" +
                    DB_TASK_NAME + " TEXT, " +
                    DB_TASK_DESCRIPTION + " TEXT, " +
                    DB_TASK_DUE_DATE + " TEXT, " +
                    DB_TASK_COMPLETED_DATE + " TEXT, " +
                    DB_TASK_REPEAT_UNIT + " TEXT, " +
                    DB_TASK_REPEAT_TIME + " INTEGER, " +
                    DB_ID + " AUTONUMBER);";
    private static final String DB_WHERE = DB_TASK_COMPLETED_DATE + "='" + DATE_FORMAT.format(new Date(0)) + "'"; 
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
        ContentValues vals = new ContentValues();
        vals.put(DB_TASK_NAME, task.mName);
        vals.put(DB_TASK_DESCRIPTION, task.mDescription);
        vals.put(DB_TASK_DUE_DATE, DATE_FORMAT.format(task.mDueDate));
        vals.put(DB_TASK_COMPLETED_DATE, DATE_FORMAT.format(task.mCompletedDate));
        vals.put(DB_TASK_REPEAT_UNIT, task.mRepeatUnit.toString());
        vals.put(DB_TASK_REPEAT_TIME, task.mRepeatTime);
        db.insert(DB_TABLE_NAME, null, vals);
        db.close();
    }

    public Cursor GetCursor()
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(DB_TABLE_NAME, null, DB_WHERE, null, null, null, DB_ORDER_BY);
    }

    public void Remove()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.close();
        SQLiteDatabase.deleteDatabase(new File(db.getPath()));
    }
}
