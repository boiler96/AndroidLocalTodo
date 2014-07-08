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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class TaskActivity extends FragmentActivity {
    
    public class TaskActivityPagerAdapter extends FragmentStatePagerAdapter
    {
        public TaskActivityPagerAdapter(TaskDatabase db, FragmentManager fm) 
        {
            super(fm);
            mDB = db;
        }

        @Override
        public Fragment getItem(int item) 
        {
            Fragment fragment = new TaskActivityFragment();
            Bundle args = new Bundle();
            args.putInt(CURSOR_POS, item);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() 
        {
            Cursor cursor = mDB.GetCursor();
            int count = cursor.getCount();
            cursor.close();
            return count;
        }
        
        TaskDatabase mDB;
    }
    
    public static class TaskActivityFragment extends Fragment
            implements ProgrammaticViewPager.ProgrammaticViewPagerNotify
    {
        @Override
        public void onSaveInstanceState (Bundle outState)
        {
            Bundle args = getArguments();
            int pos = args.getInt(CURSOR_POS, 0);
            Log.v("TaskActivityFragment", "onSaveInstanceState " + Integer.toString(pos));
            mProgrammaticChange = true;
            outState.putSerializable("mNewTask", mNewTask);
            super.onSaveInstanceState(outState);
        }
        
        @Override
        public void onActivityCreated (Bundle savedInstanceState)
        {
            Bundle args = getArguments();
            int pos = args.getInt(CURSOR_POS, 0);
            Log.v("TaskActivityFragment", "onActivityCreated " + Integer.toString(pos));
            super.onActivityCreated(savedInstanceState);
        }
        
        @Override
        public void onStart()
        {
            Bundle args = getArguments();
            int pos = args.getInt(CURSOR_POS, 0);
            Log.v("TaskActivityFragment", "onStart " + Integer.toString(pos));
            super.onStart();
        }
        
        @Override
        public void onResume()
        {
            Bundle args = getArguments();
            int pos = args.getInt(CURSOR_POS, 0);
            Log.v("TaskActivityFragment", "onResume " + Integer.toString(pos));
            super.onResume();
            mProgrammaticChange = false;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) 
        {
            mProgrammaticChange = true;
            TaskDatabase db = new TaskDatabase(getActivity());
            View rootView = inflater.inflate(
                    R.layout.activity_task, container, false);
            Bundle args = getArguments();
            int pos = args.getInt(CURSOR_POS, 0);
            Log.v("TaskActivityFragment", "onCreateView " + Integer.toString(pos));
            Cursor cursor = db.GetCursor();
            cursor.moveToPosition(pos);
            
            TaskDatabase.Task task = db.LoadTask(cursor);
            mOriginalTask = task;
            if (savedInstanceState != null)
            {
                mNewTask = (TaskDatabase.Task)savedInstanceState.getSerializable("mNewTask");
                if (!mOriginalTask.equals(mNewTask))
                {
                    mProgrammaticChange = false;
                    TaskChanged(rootView);
                    mProgrammaticChange = true;
                }
            }
            else
            {
                mNewTask = new TaskDatabase.Task(task);
            }
            
            InitializeView(rootView);
            InitializeViewFields(rootView, task);
            return rootView;
        }
        private void InitializeView(View rootView)
        {
            final View activityView = rootView;
            TextWatcher textWatcher = new TextWatcher()
            {
                @Override
                public void afterTextChanged(Editable s) {}
                @Override
                public void beforeTextChanged(CharSequence s, int start,
                        int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start,
                        int before, int count) {
                    TaskChanged(activityView);
                }
            };
            final EditText nameEdit = (EditText)rootView.findViewById(R.id.task_name_edit);
            final TextView nameStatic = (TextView)rootView.findViewById(R.id.task_name_static);
            nameStatic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nameEdit.setVisibility(View.VISIBLE);
                    nameStatic.setVisibility(View.INVISIBLE);
                }
            });
            nameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus)
                    {
                        nameEdit.setVisibility(View.INVISIBLE);
                        nameStatic.setVisibility(View.VISIBLE);
                    }
                }
            });
            nameEdit.addTextChangedListener(textWatcher);
            final TextView descriptionEdit = (TextView)rootView.findViewById(R.id.task_description_edit);
            descriptionEdit.addTextChangedListener(textWatcher);
            
            final TextView dueDateView = (TextView)rootView.findViewById(R.id.due_date_text);
            final DatePicker datePicker = (DatePicker)rootView.findViewById(R.id.due_date_calendar);
            datePicker.init(0, 0, 0, 
                    new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear,
                                int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                            SetFriendlyDueDateText(dueDateView, calendar);
                            TaskChanged(activityView);
                        }
                    });

            Button todayButton = (Button)rootView.findViewById(R.id.today_button);
            todayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar now = Calendar.getInstance();
                    datePicker.updateDate(now.get(Calendar.YEAR),
                                          now.get(Calendar.MONTH),
                                          now.get(Calendar.DAY_OF_MONTH));
                    SetFriendlyDueDateText(dueDateView, now);
                }
            });
            Button plusDayButton = (Button)rootView.findViewById(R.id.plus_day_button);
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
                    SetFriendlyDueDateText(dueDateView, calendar);
                }
            });
            Button thisWeekendButton = (Button)rootView.findViewById(R.id.this_weekend_button);
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
                    SetFriendlyDueDateText(dueDateView, weekend);
                }
            });
            Button plusWeekButton = (Button)rootView.findViewById(R.id.plus_week_button);
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
                    SetFriendlyDueDateText(dueDateView, calendar);
                }
            });

            final CheckBox repeatCheck = (CheckBox)rootView.findViewById(R.id.repeat);
            repeatCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    View dialog = (View)buttonView.getParent();
                    SetRepeatVisibility(dialog, isChecked);
                    TaskChanged(activityView);
                }
            });

            final EditText repeatTimeEdit = (EditText)rootView.findViewById(R.id.repeat_time);
            repeatTimeEdit.addTextChangedListener(textWatcher);
            
            final Spinner repeatUnitSpinner = (Spinner)rootView.findViewById(R.id.repeat_unit);
            String[] repeatUnits = { "Days", "Weeks", "Months", "Years" };
            repeatUnitSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, repeatUnits));
            repeatUnitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                        int position, long id) {
                    if (mRow != -1 && mRow != id)
                    {
                        TaskChanged(activityView);
                    }
                    mRow = id;
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    TaskChanged(activityView);
                }
                private long mRow = -1;
            });
            
            final RadioButton repeatFromComplete = (RadioButton)rootView.findViewById(R.id.repeat_from_complete);
            final RadioButton repeatFromDue = (RadioButton)rootView.findViewById(R.id.repeat_from_due);
            Button revertButton = (Button)rootView.findViewById(R.id.revert_button);
            Button acceptButton = (Button)rootView.findViewById(R.id.accept_button);
            Button doneButton = (Button)rootView.findViewById(R.id.done_button);
            Button deleteButton = (Button)rootView.findViewById(R.id.delete_button);
            revertButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNewTask = new TaskDatabase.Task(mOriginalTask);
                    mProgrammaticChange = true;
                    InitializeViewFields(activityView, mOriginalTask);
                    mProgrammaticChange = false;
                    TaskReverted(activityView);
                }
            });
        }
        
        private void InitializeViewFields(View rootView, TaskDatabase.Task task)
        {
            final EditText nameEdit = (EditText)rootView.findViewById(R.id.task_name_edit);
            final TextView nameStatic = (TextView)rootView.findViewById(R.id.task_name_static);
            nameStatic.setText(task.mName);
            nameEdit.setText(task.mName);
            final TextView descriptionEdit = (TextView)rootView.findViewById(R.id.task_description_edit);
            descriptionEdit.setText(task.mDescription);
            final TextView dueDateView = (TextView)rootView.findViewById(R.id.due_date_text);
            SetFriendlyDueDateText(dueDateView, task.mDueDate);
            final Calendar dueDate = task.mDueDate;
            final DatePicker datePicker = (DatePicker)rootView.findViewById(R.id.due_date_calendar);
            datePicker.updateDate(dueDate.get(Calendar.YEAR), 
                    dueDate.get(Calendar.MONTH), 
                    dueDate.get(Calendar.DAY_OF_MONTH));
            final CheckBox repeatCheck = (CheckBox)rootView.findViewById(R.id.repeat);
            Boolean repeat = task.mRepeatUnit != TaskDatabase.Task.RepeatUnit.NONE;
            repeatCheck.setChecked(repeat);
            SetRepeatVisibility(rootView, repeat);
            final EditText repeatTimeEdit = (EditText)rootView.findViewById(R.id.repeat_time);
            repeatTimeEdit.setText(Integer.toString(task.mRepeatTime));
            final Spinner repeatUnitSpinner = (Spinner)rootView.findViewById(R.id.repeat_unit);
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
            final RadioButton repeatFromComplete = (RadioButton)rootView.findViewById(R.id.repeat_from_complete);
            final RadioButton repeatFromDue = (RadioButton)rootView.findViewById(R.id.repeat_from_due);
            if (task.mRepeatFromComplete)
            {
                repeatFromComplete.setChecked(true);
            }
            else
            {
                repeatFromDue.setChecked(true);
            }
        }

        private void TaskChanged(View rootView)
        {
            if (!mProgrammaticChange)
            {
                Button revertButton = (Button)rootView.findViewById(R.id.revert_button);
                Button acceptButton = (Button)rootView.findViewById(R.id.accept_button);
                Button doneButton = (Button)rootView.findViewById(R.id.done_button);
                Button deleteButton = (Button)rootView.findViewById(R.id.delete_button);
                doneButton.setVisibility(Button.INVISIBLE);
                deleteButton.setVisibility(Button.INVISIBLE);
                revertButton.setVisibility(Button.VISIBLE);
                acceptButton.setVisibility(Button.VISIBLE);
                ProgrammaticViewPager pager = (ProgrammaticViewPager)getActivity().findViewById(R.id.activity_task_pager);
                pager.Disable(this);
            }
        }
        
        private void TaskReverted(View rootView)
        {
            Button revertButton = (Button)rootView.findViewById(R.id.revert_button);
            Button acceptButton = (Button)rootView.findViewById(R.id.accept_button);
            Button doneButton = (Button)rootView.findViewById(R.id.done_button);
            Button deleteButton = (Button)rootView.findViewById(R.id.delete_button);
            doneButton.setVisibility(Button.VISIBLE);
            deleteButton.setVisibility(Button.VISIBLE);
            revertButton.setVisibility(Button.INVISIBLE);
            acceptButton.setVisibility(Button.INVISIBLE);
            ProgrammaticViewPager pager = (ProgrammaticViewPager)getActivity().findViewById(R.id.activity_task_pager);
            pager.Enable();
        }


        @Override
        public void SwipedWhileDisabled() {
            AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(50);
            animation.setRepeatCount(5);
            animation.setRepeatMode(Animation.REVERSE);
            Button revertButton = (Button)getView().findViewById(R.id.revert_button);
            Button acceptButton = (Button)getView().findViewById(R.id.accept_button);
            revertButton.startAnimation(animation);
            acceptButton.startAnimation(animation);
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
        private TaskDatabase.Task mOriginalTask;
        private TaskDatabase.Task mNewTask;
        private Boolean mProgrammaticChange;
    }
    public final static String CURSOR_POS = "com.kunze.androidlocaltodo.CURSOR_POS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_pager);
        mAdapter = new TaskActivityPagerAdapter(mDB, getSupportFragmentManager());
        mViewPager = (ProgrammaticViewPager)findViewById(R.id.activity_task_pager);
        mViewPager.setAdapter(mAdapter);
    }
    
    private TaskActivityPagerAdapter mAdapter;
    private ProgrammaticViewPager mViewPager;
    private TaskDatabase mDB = new TaskDatabase(this);
}
