package com.kunze.androidlocaltodo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class TaskListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        InitializeView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_load_astrid)
        {
            LoadAstrid();
        }
        else if(item.getItemId() == R.id.action_delete_database)
        {
            DeleteDatabase();
        }
        return true;
    }

    private void RefreshView()
    {
        // Get a cursor to populate the UI
        Cursor cursor = mDB.GetCursor();
        mAdapter.changeCursor(cursor);
    }

    private void InitializeView()
    {
        // Get a cursor to populate the UI
        Cursor cursor = mDB.GetCursor();
        String from[] = { "NAME", "DUE_DATE" };
        int to[] = { R.id.task_name, R.id.task_due_date };

        mAdapter = new SimpleCursorAdapter(this, R.layout.task_list_element, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        
        // Set up a ViewBinder to convert the due date to something more human-friendly
        mAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                try
                {
                    if (columnIndex == 2)
                    {
                        String dueDateString = cursor.getString(columnIndex);
                        SimpleDateFormat dateFormatDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                        SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
                        Date dueDate = dateFormatDB.parse(dueDateString);
                        TextView dueDateView = (TextView)view;
                        Date now = new Date();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dueDate);
                        long dayDiff = (now.getTime() - dueDate.getTime()) / 1000 / 60 / 60 / 24;
                        if (dayDiff == 0)
                        {
                            dueDateView.setText("Today");
                            dueDateView.setTextColor(Color.RED);
                        }
                        else if (dueDate.before(now))
                        {
                            dueDateView.setText("+ " + dayDiff + " days!");
                            dueDateView.setTextColor(Color.RED);
                        }
                        else if (dayDiff > -7)
                        {
                            dueDateView.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
                            dueDateView.setTextColor(Color.BLACK);
                        }
                        else if (dayDiff > -14)
                        {
                            dueDateView.setText("Next " + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
                            dueDateView.setTextColor(Color.BLACK);
                        }
                        else
                        {
                            dueDateView.setText(dateFormatDisplay.format(dueDate));
                            dueDateView.setTextColor(Color.BLACK);
                        }
                        return true;
                    }
                } catch (Exception e)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this); 
                    AlertDialog dlg = builder.setTitle("Database error!").setMessage(e.getMessage()).create();
                    dlg.show();
                }
                return false;
            }
        });
        ListView listView = (ListView)findViewById(R.id.task_list);
        listView.setAdapter(mAdapter);
    }

    private void DeleteDatabase()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("WARNING!");
        builder.setMessage("This will delete all of your data!  Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
        {

            @Override
            public void onClick(DialogInterface dialog, int which) 
            {
                mDB.Remove();
                RefreshView();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() 
        {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Nothing
            }
        });
        builder.show();
    }

    private void LoadAstrid()
    {
        // Look for a zip file in the proper location
        String loc = Environment.getExternalStorageDirectory() + "/AstridImport/";
        AlertDialog.Builder errorBuilder = new AlertDialog.Builder(this);
        errorBuilder.setTitle("Error reading Astrid Import!");
        ZipInputStream zipStream = null;
        InputStream stream = null;
        try
        {
            // Try to safely open the file
            String errText = "Cannot find Astrid file in " + loc;
            File astridDir = new File(loc);
            if (!astridDir.isDirectory())
            {
                throw new Exception(errText);
            }

            File[] files = astridDir.listFiles();
            if (files.length != 1)
            {
                throw new Exception(errText);
            }

            File astridFile = files[0];
            if (!astridFile.isFile())
            {
                throw new Exception(errText);
            }

            // Try to unzip the file and unpack the tasks
            errText = "Could not unzip file " + astridFile.getAbsolutePath();
            stream = new FileInputStream(astridFile);
            zipStream = new ZipInputStream(stream);

            ZipEntry tasksEntry = null;
            while ((tasksEntry = zipStream.getNextEntry()) != null)
            {
                if (tasksEntry.getName().equals("tasks.csv"))
                {
                    break;
                }
            }
            if (tasksEntry == null)
            {
                throw new Exception(errText);
            }

            int size = (int)tasksEntry.getSize();
            byte tasksContent[] = new byte[size];
            int offset = 0;
            while (size != 0)
            {
                int read = zipStream.read(tasksContent, offset, size);
                if (read == 0)
                {
                    throw new Exception(errText);
                }
                offset += read;
                size -= read;
            }

            String tasksString = new String(tasksContent, "UTF-8");

            // Parse the tasks in the task list
            errText = "Could not parse task list";
            List<String> taskLines = new LinkedList<String>(Arrays.asList(tasksString.split("\n")));
            // Remove the header row
            taskLines.remove(0);
            // Some tasks have newlines in quotes, so we have to adjust for that.
            ListIterator<String> it = taskLines.listIterator();
            while (it.hasNext())
            {
                String task = it.next();
                while (CountQuotes(task) % 2 == 1)
                {
                    it.remove();
                    task += it.next();
                }
                it.set(task);
            } 
            for (String taskLine : taskLines) {
                List<String> taskFields = new LinkedList<String>(Arrays.asList(taskLine.split(",", -1)));
                // Some tasks have commas in quotes, so we have to adjust for that.
                it = taskFields.listIterator();
                while (it.hasNext())
                {
                    String field = it.next();
                    while (CountQuotes(field) % 2 == 1)
                    {
                        it.remove();
                        field += it.next();
                    }
                    it.set(field);
                }
                
                TaskDatabase.Task taskElement = new TaskDatabase.Task();
                taskElement.mName = taskFields.get(0);
                taskElement.mDescription = taskFields.get(8);
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                taskElement.mDueDate = dateFormat.parse(taskFields.get(4));
                String completedString = taskFields.get(9); 
                if (completedString.equals(""))
                {
                    taskElement.mCompletedDate = new Date(0);
                }
                else
                {
                    taskElement.mCompletedDate = dateFormat.parse(completedString);
                }
                taskElement.mRepeatUnit = TaskDatabase.Task.RepeatUnit.NONE;
                taskElement.mRepeatTime = 0;
                String repeatString = taskFields.get(6);
                String repeatFields[] = repeatString.split(":");
                if (repeatFields[0].equals("RRULE"))
                {
                    repeatFields = repeatFields[1].split(";");
                    String freqFields[] = repeatFields[0].split("=");
                    if (freqFields[0].equals("FREQ"))
                    {
                        if (freqFields[1].equals("YEARLY"))
                        {
                            taskElement.mRepeatUnit = TaskDatabase.Task.RepeatUnit.YEARS;
                        }
                        else if (freqFields[1].equals("MONTHLY"))
                        {
                            taskElement.mRepeatUnit = TaskDatabase.Task.RepeatUnit.MONTHS;
                        }
                        else if (freqFields[1].equals("WEEKLY"))
                        {
                            taskElement.mRepeatUnit = TaskDatabase.Task.RepeatUnit.WEEKS;
                        }
                        else if (freqFields[1].equals("DAILY"))
                        {
                            taskElement.mRepeatUnit = TaskDatabase.Task.RepeatUnit.DAYS;
                        }
                    }
                    freqFields = repeatFields[1].split("=");
                    if (freqFields[0].equals("INTERVAL"))
                    {
                        taskElement.mRepeatTime = Integer.valueOf(freqFields[1]);
                    }
                }
                mDB.AddTask(taskElement);
            }

            RefreshView();
        }
        catch (Exception e)
        {
            AlertDialog dlg = errorBuilder.setMessage(e.getMessage()).create();
            dlg.show();
        }
        finally
        {

            try
            {
                if (zipStream != null)
                {
                    zipStream.close();
                }
                if (stream != null)
                {
                    stream.close();
                }
            }
            catch (Exception e)
            {
                AlertDialog dlg = errorBuilder.setMessage(e.getMessage()).create();
                dlg.show();
            }
        }
    }

    static private int CountQuotes(String string)
    {
        int count = 0;
        for (int i = 0; i < string.length(); ++i) 
        {
            switch (string.charAt(i))
            {
            case '\\':
                ++i;
                break;
            case '\"':
                ++count;
                break;
            default:
                // Nothing
            }
        }
        return count;
    }

    private TaskDatabase mDB = new TaskDatabase(this);
    private SimpleCursorAdapter mAdapter = null;
}
