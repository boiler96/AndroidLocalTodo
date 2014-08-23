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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TaskActivity extends FragmentActivity {
    
    public class TaskActivityPagerAdapter extends FragmentStatePagerAdapter
    {
        public TaskActivityPagerAdapter(TaskDatabase db, FragmentManager fm) 
        {
            super(fm);
            mDB = db;
            mCursor = mDB.GetCursor();
        }

        @Override
        public Fragment getItem(int item) 
        {
            Fragment fragment = new TaskActivityFragment();
            Bundle args = new Bundle();
            mCursor.moveToPosition(item);
            args.putLong(TASK_ID, mDB.LoadTask(mCursor).mID);
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
        private Cursor mCursor;
    }

    public final static String TASK_ID = "com.kunze.androidlocaltodo.TASK_ID";

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
