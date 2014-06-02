/* Copyright (c) 2014 Aaron Kunze (boilerpdx@gmail.com)
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class TaskActivity extends Activity {
    
    public final static String CURSOR_POS = "com.kunze.androidlocaltodo.CURSOR_POS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        InitializeView();
    }
    
    private void InitializeView()
    {
        Intent intent = getIntent();
        int pos = intent.getIntExtra(CURSOR_POS, 0);
        mCursor = mDB.GetCursor();
        mCursor.moveToPosition(pos);
        
        TaskDatabase.Task task = mDB.LoadTask(mCursor);
        
        this.setTitle(task.mName);
        
        final TextView nameEdit = (TextView)findViewById(R.id.task_name_edit);
        nameEdit.setText(task.mName);
        final TextView descriptionEdit = (TextView)findViewById(R.id.task_description_edit);
        descriptionEdit.setText(task.mDescription);
        final TextView dueDateView = (TextView)findViewById(R.id.task_due_date);
        SetFriendlyDueDateText(dueDateView, task.mDueDate);
        Button dueDateButton = (Button)findViewById(R.id.task_due_date_choose);
        final Calendar dueDate = task.mDueDate;
        final TaskDatabase.Task thisTask = task;
        
        final CheckBox repeatCheck = (CheckBox)findViewById(R.id.repeat);
        repeatCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                View dialog = (View)buttonView.getParent();
                SetRepeatVisibility(dialog, isChecked);
            }
        });
        
        Boolean repeat = task.mRepeatUnit != TaskDatabase.Task.RepeatUnit.NONE;
        repeatCheck.setChecked(repeat);
        SetRepeatVisibility(findViewById(android.R.id.content), repeat);

        final EditText repeatTimeEdit = (EditText)findViewById(R.id.repeat_time);
        repeatTimeEdit.setText(Integer.toString(task.mRepeatTime));
        
        final Spinner repeatUnitSpinner = (Spinner)findViewById(R.id.repeat_unit);
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
        
        final RadioButton repeatFromComplete = (RadioButton)findViewById(R.id.repeat_from_complete);
        final RadioButton repeatFromDue = (RadioButton)findViewById(R.id.repeat_from_due);
        if (task.mRepeatFromComplete)
        {
            repeatFromComplete.setChecked(true);
        }
        else
        {
            repeatFromDue.setChecked(true);
        }
        
    }
    
    private static void SetFriendlyDueDateText(TextView dueDateView, Calendar dueDate) {
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

    private static void SetRepeatVisibility(View view, Boolean visible) {
        View[] repeatViews = {
            view.findViewById(R.id.repeat_label),
            view.findViewById(R.id.repeat_time),
            view.findViewById(R.id.repeat_unit),
            view.findViewById(R.id.repeat_from_label),
            view.findViewById(R.id.repeat_from),
            view.findViewById(R.id.repeat_from_complete),
            view.findViewById(R.id.repeat_from_due),
            view.findViewById(R.id.repeat_top_divider),
            view.findViewById(R.id.repeat_bottom_divider),
        };
        if (visible) {
            for (View repeatView : repeatViews) {
                repeatView.setVisibility(View.VISIBLE);
            }
        }
        else {
            for (View repeatView : repeatViews) {
                repeatView.setVisibility(View.GONE);
            }
        }
    }
    private TaskDatabase mDB = new TaskDatabase(this);
    private Cursor mCursor;
}
