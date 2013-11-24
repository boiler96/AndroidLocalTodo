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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class TaskListActivity 
			extends Activity 
			implements OnItemClickListener,
			OnItemLongClickListener {

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
        else if(item.getItemId() == R.id.action_new_task)
        {
        	final TaskDatabase.Task task = new TaskDatabase.Task();
        	ShowTaskDialog(task, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mDB.AddTask(task);
					RefreshView();
				}
			});
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
        // Set up event handlers for the task list
        ListView listView = (ListView)findViewById(R.id.task_list);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setLongClickable(true);
        
        // Get a cursor to populate the UI
        Cursor cursor = mDB.GetCursor();
        String from[] = { "NAME", "DUE_DATE" };
        int to[] = { R.id.task_name_edit, R.id.task_due_date };

        mAdapter = new SimpleCursorAdapter(this, R.layout.task_list_element, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        
        // Set up a ViewBinder to convert the due date to something more human-friendly
        mAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                try
                {
                    if (columnIndex == 2)
                    {
                    	Calendar dueDate = TaskDatabase.ConvertIntToDate(cursor.getInt(columnIndex));
                        TextView dueDateView = (TextView)view;
                        SetFriendlyDueDateText(dueDateView, dueDate);
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

                taskElement.mDueDate = Calendar.getInstance(); 
                taskElement.mDueDate.setTime(dateFormat.parse(taskFields.get(4)));
                String completedString = taskFields.get(9);
                taskElement.mCompletedDate = Calendar.getInstance();
                if (completedString.equals(""))
                {
                    taskElement.mCompletedDate.setTimeInMillis(0);
                }
                else
                {
                    taskElement.mCompletedDate.setTime(dateFormat.parse(completedString));
                }
                taskElement.mRepeatUnit = TaskDatabase.Task.RepeatUnit.NONE;
                taskElement.mRepeatTime = 0;
                taskElement.mRepeatFromComplete = false;
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
                    if (repeatFields.length > 2 && repeatFields[2] != null)
                    {
                        freqFields = repeatFields[2].split("=");
                        if (freqFields[0].equals("FROM") && freqFields[1].equals("COMPLETION"))
                        {
                            taskElement.mRepeatFromComplete = true;
                        }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor)parent.getItemAtPosition(position);
        final TaskDatabase.Task task = mDB.LoadTask(cursor);
        ShowTaskDialog(task, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        mDB.SaveTask(task);
		        RefreshView();
			}
		});
    }
    
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
        Cursor cursor = (Cursor)parent.getItemAtPosition(position);
        final TaskDatabase.Task task = mDB.LoadTask(cursor);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Done?");
        builder.setMessage("Mark task \"" + task.mName + "\" as done?");
        builder.setNegativeButton("No", null);
        builder.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (task.mRepeatUnit == TaskDatabase.Task.RepeatUnit.NONE) {
					task.mCompletedDate = Calendar.getInstance();
				}
				else {
					Calendar repeatBase = Calendar.getInstance();
					if (!task.mRepeatFromComplete) {
						repeatBase = task.mDueDate;
					}
					int calUnit;
					int calNumber = 1;
					switch (task.mRepeatUnit) {
					case DAYS:
						calUnit = Calendar.DATE;
						break;
					case WEEKS:
						calUnit = Calendar.DATE;
						calNumber = 7;
						break;
					case MONTHS:
						calUnit = Calendar.MONTH;
						break;
					case YEARS:
						calUnit = Calendar.YEAR;
						break;
					default:
						calUnit = Calendar.DATE;
						calNumber = 0;
						break;
					}
					calNumber *= task.mRepeatTime;
					task.mDueDate = repeatBase;
					task.mDueDate.add(calUnit, calNumber);
				}
				mDB.SaveTask(task);
				RefreshView();
			}
		});
        builder.show();
		return false;
	}

	private void ShowTaskDialog(TaskDatabase.Task task, OnClickListener okListener)
    {
        LayoutInflater inflater = this.getLayoutInflater();
        View dlgView = inflater.inflate(R.layout.dialog_task, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dlgView);
        builder.setTitle("Task");
        
        final TextView nameEdit = (TextView)dlgView.findViewById(R.id.task_name_edit);
        nameEdit.setText(task.mName);
        final TextView descriptionEdit = (TextView)dlgView.findViewById(R.id.task_description_edit);
        descriptionEdit.setText(task.mDescription);
        final TextView dueDateView = (TextView)dlgView.findViewById(R.id.task_due_date);
        SetFriendlyDueDateText(dueDateView, task.mDueDate);
        Button dueDateButton = (Button)dlgView.findViewById(R.id.task_due_date_choose);
        final Calendar dueDate = task.mDueDate;
        final TaskDatabase.Task thisTask = task;
        dueDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowDueDateDialog(dueDate, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int id) {
						Dialog dlg = (Dialog)dialogInterface;
				        DatePicker datePicker = (DatePicker)dlg.findViewById(R.id.due_date_calendar);
				        Calendar calendar = Calendar.getInstance();
				        calendar.set(datePicker.getYear(), datePicker.getMonth(),
				        			 datePicker.getDayOfMonth());
				        thisTask.mDueDate = calendar;
				        SetFriendlyDueDateText(dueDateView, thisTask.mDueDate);
					}
				});
			}
		});
        
        final CheckBox repeatCheck = (CheckBox)dlgView.findViewById(R.id.repeat);
        repeatCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				View dialog = (View)buttonView.getParent();
				SetRepeatVisibility(dialog, isChecked);
			}
		});
        
        Boolean repeat = task.mRepeatUnit != TaskDatabase.Task.RepeatUnit.NONE;
        repeatCheck.setChecked(repeat);
        SetRepeatVisibility(dlgView, repeat);

        final EditText repeatTimeEdit = (EditText)dlgView.findViewById(R.id.repeat_time);
        repeatTimeEdit.setText(Integer.toString(task.mRepeatTime));
        
        final Spinner repeatUnitSpinner = (Spinner)dlgView.findViewById(R.id.repeat_unit);
        String[] repeatUnits = { "Days", "Weeks", "Months", "Years" };
        repeatUnitSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, repeatUnits));
        int repeatUnitPos = 0;
        switch (task.mRepeatUnit)
        {
        case DAYS:
        	repeatUnitPos = 0;
        	break;
        case WEEKS:
        	repeatUnitPos = 1;
        	break;
        case MONTHS:
        	repeatUnitPos = 2;
        	break;
        case YEARS:
        	repeatUnitPos = 3;
        	break;
        case NONE:
        	repeatUnitPos = 0;	
        }
        repeatUnitSpinner.setSelection(repeatUnitPos);
        
        final RadioButton repeatFromComplete = (RadioButton)dlgView.findViewById(R.id.repeat_from_complete);
        final RadioButton repeatFromDue = (RadioButton)dlgView.findViewById(R.id.repeat_from_due);
        if (task.mRepeatFromComplete)
        {
        	repeatFromComplete.setChecked(true);
        }
        else
        {
        	repeatFromDue.setChecked(true);
        }
        
        // Here's a trick:  We cascade the OnClick listeners so we can extract
        // the dialog contents into the task before calling the second listener.
        final OnClickListener userListener = okListener;
        final TaskDatabase.Task myTask = task;
        OnClickListener cascadedListener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				myTask.mName = nameEdit.getText().toString();
				myTask.mDescription = descriptionEdit.getText().toString();
				
				myTask.mRepeatUnit = TaskDatabase.Task.RepeatUnit.NONE;
				if (repeatCheck.isChecked())
				{
					switch (repeatUnitSpinner.getSelectedItemPosition())
					{
					case 0:
						myTask.mRepeatUnit = TaskDatabase.Task.RepeatUnit.DAYS;
						break;
					case 1:
						myTask.mRepeatUnit = TaskDatabase.Task.RepeatUnit.WEEKS;
						break;
					case 2:
						myTask.mRepeatUnit = TaskDatabase.Task.RepeatUnit.MONTHS;
						break;
					case 3:
						myTask.mRepeatUnit = TaskDatabase.Task.RepeatUnit.YEARS;
						break;
					}
					
					myTask.mRepeatTime = Integer.parseInt(repeatTimeEdit.getText().toString());
					myTask.mRepeatFromComplete = repeatFromComplete.isChecked();
				}
				
				userListener.onClick(dialog, which);
			}
        };

        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", cascadedListener);

        builder.show();
    }
    
    private void ShowDueDateDialog(Calendar dueDate, OnClickListener okListener)
    {
        LayoutInflater inflater = this.getLayoutInflater();
        View dlgView = inflater.inflate(R.layout.dialog_due_date, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dlgView);
        builder.setTitle("Due Date");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("OK", okListener);
        
        final TextView dateText = (TextView)dlgView.findViewById(R.id.due_date_text);
        SetFriendlyDueDateText(dateText, dueDate);
        
        final DatePicker datePicker = (DatePicker)dlgView.findViewById(R.id.due_date_calendar);
        //datePicker.setMinDate(Math.min(now.getTimeInMillis(), dueDate.getTimeInMillis()));
        datePicker.init(dueDate.get(Calendar.YEAR), 
        				dueDate.get(Calendar.MONTH), 
        				dueDate.get(Calendar.DAY_OF_MONTH), 
        				new DatePicker.OnDateChangedListener() {
							@Override
							public void onDateChanged(DatePicker view, int year, int monthOfYear,
									int dayOfMonth) {
						        Calendar calendar = Calendar.getInstance();
						        calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
						        SetFriendlyDueDateText(dateText, calendar);
								
							}
						});
        datePicker.setCalendarViewShown(false);
        
        Button todayButton = (Button)dlgView.findViewById(R.id.today_button);
        todayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        Calendar now = Calendar.getInstance();
				datePicker.updateDate(now.get(Calendar.YEAR),
						              now.get(Calendar.MONTH),
						              now.get(Calendar.DAY_OF_MONTH));
		        SetFriendlyDueDateText(dateText, now);
			}
		});
        Button plusDayButton = (Button)dlgView.findViewById(R.id.plus_day_button);
        plusDayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(),
                             datePicker.getDayOfMonth());
                calendar.add(Calendar.DATE, 1);
				datePicker.updateDate(calendar.get(Calendar.YEAR),
				                      calendar.get(Calendar.MONTH),
				                      calendar.get(Calendar.DAY_OF_MONTH));
		        SetFriendlyDueDateText(dateText, calendar);
			}
		});
        Button thisWeekendButton = (Button)dlgView.findViewById(R.id.this_weekend_button);
        thisWeekendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        Calendar weekend = Calendar.getInstance();
		        while (weekend.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
		        	weekend.add(Calendar.DATE, 1);
		        }
				datePicker.updateDate(weekend.get(Calendar.YEAR),
									  weekend.get(Calendar.MONTH),
									  weekend.get(Calendar.DAY_OF_MONTH));
		        SetFriendlyDueDateText(dateText, weekend);
			}
		});
        Button plusWeekButton = (Button)dlgView.findViewById(R.id.plus_week_button);
        plusWeekButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(),
                             datePicker.getDayOfMonth());
                calendar.add(Calendar.DATE, 7);
                datePicker.updateDate(calendar.get(Calendar.YEAR),
                                      calendar.get(Calendar.MONTH),
                                      calendar.get(Calendar.DAY_OF_MONTH));
                SetFriendlyDueDateText(dateText, calendar);
			}
		});
        
        builder.show();
    }
    
    private void SetRepeatVisibility(View dialog, Boolean visible) {
		View[] repeatViews = {
			dialog.findViewById(R.id.repeat_label),
			dialog.findViewById(R.id.repeat_time),
			dialog.findViewById(R.id.repeat_unit),
			dialog.findViewById(R.id.repeat_from_label),
			dialog.findViewById(R.id.repeat_from),
			dialog.findViewById(R.id.repeat_from_complete),
			dialog.findViewById(R.id.repeat_from_due),
			dialog.findViewById(R.id.repeat_top_divider),
			dialog.findViewById(R.id.repeat_bottom_divider),
		};
		if (visible) {
			for (View view : repeatViews) {
				view.setVisibility(View.VISIBLE);
			}
		}
		else {
			for (View view : repeatViews) {
				view.setVisibility(View.GONE);
			}
		}
	}
    
    private void SetFriendlyDueDateText(TextView dueDateView, Calendar dueDate) {
        SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        Calendar now = Calendar.getInstance();
        int nowDay = TaskDatabase.ConvertDateToInt(now);
        int dueDay = TaskDatabase.ConvertDateToInt(dueDate);
        int dayDiff = nowDay - dueDay;
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
            dueDateView.setText(dueDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
            dueDateView.setTextColor(Color.BLACK);
        }
        else if (dayDiff > -14)
        {
            dueDateView.setText("Next " + dueDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
            dueDateView.setTextColor(Color.BLACK);
        }
        else
        {
            dueDateView.setText(dateFormatDisplay.format(dueDate.getTime()));
            dueDateView.setTextColor(Color.BLACK);
        }
    }

    private TaskDatabase mDB = new TaskDatabase(this);
    private SimpleCursorAdapter mAdapter = null;
}
