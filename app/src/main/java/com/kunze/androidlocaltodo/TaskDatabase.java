/* Copyright (c) 2013 Aaron Kunze (boilerpdx@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kunze.androidlocaltodo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDatabase extends SQLiteOpenHelper {

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
    private static final String DB_WHERE_NOT_COMPLETE = DB_TASK_COMPLETED_DATE + "= 0";
    private static final String DB_WHERE_ID_EQUALS = DB_ID + "=";
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
    
    public void BackupDatabase(String path) throws java.io.IOException
    {
        SQLiteDatabase db = getWritableDatabase();
        db.close();
        File fromFile = new File(db.getPath());
        File toFile = new File(path);
        FileInputStream reader = new FileInputStream(fromFile);
        FileOutputStream writer = new FileOutputStream(toFile);
        
        final int bufSize = 256;
        byte[] buf = new byte[bufSize]; 
        int bytesCopied = 0;
        do
        {
            bytesCopied = reader.read(buf);
            if (bytesCopied == -1)
            {
                break;
            }
            writer.write(buf, 0, bytesCopied);
        } while(true);
        
        reader.close();
        writer.flush();
        writer.close();
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
        return db.query(DB_TABLE_NAME, null, DB_WHERE_NOT_COMPLETE, null, null, null, DB_ORDER_BY);
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

    public Task LoadTask(Long id)
    {
        Task task = new Task();
        SQLiteDatabase db = getReadableDatabase();
        String where = DB_WHERE_ID_EQUALS + Long.toString(id);
        Cursor cursor = db.query(DB_TABLE_NAME, null, where,
                                 null, null, null, null);
        try
        {
            cursor.moveToFirst();
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
