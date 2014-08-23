package com.kunze.androidlocaltodo;

/* Copyright (c) 2013-2014 Aaron Kunze (boilerpdx@gmail.com)
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

import java.io.Serializable;
import java.util.Calendar;

public class Task implements Serializable
{
    public static enum RepeatUnit { NONE, DAYS, WEEKS, MONTHS, YEARS };

    public String     mName;
    public String     mDescription;
    public Calendar mDueDate;
    public Calendar   mCompletedDate;
    public RepeatUnit mRepeatUnit;
    public int        mRepeatTime;
    public Boolean    mRepeatFromComplete;
    public long       mID;

    public Task() {
        mName = "";
        mDescription = "";
        mDueDate = Calendar.getInstance();
        mCompletedDate = Calendar.getInstance();
        mCompletedDate.setTimeInMillis(0);
        mRepeatUnit = RepeatUnit.NONE;
        mRepeatTime = 1;
        mRepeatFromComplete = true;
        mID = 0;
    }

    public Task(Task other)
    {
        mName = new String(other.mName);
        mDescription = new String(other.mDescription);
        mDueDate = (Calendar)other.mDueDate.clone();
        mCompletedDate = (Calendar)other.mCompletedDate.clone();
        mRepeatUnit = other.mRepeatUnit;
        mRepeatTime = other.mRepeatTime;
        mRepeatFromComplete = other.mRepeatFromComplete.booleanValue();
        mID = other.mID;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other == null)
        {
            return false;
        }
        else if (other instanceof Task)
        {
            Task otherTask = (Task)other;
            if (mName.equals(otherTask.mName) &&
                mDescription.equals(otherTask.mDescription) &&
                mDueDate.equals(otherTask.mDueDate) &&
                mCompletedDate.equals(otherTask.mCompletedDate) &&
                mRepeatUnit.equals(otherTask.mRepeatUnit) &&
                mRepeatTime == otherTask.mRepeatTime &&
                mRepeatFromComplete.equals(otherTask.mRepeatFromComplete) &&
                mID == otherTask.mID)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return mName.hashCode() + (int)mID;
    }

    public void Done() {
        if (mRepeatUnit == Task.RepeatUnit.NONE) {
            mCompletedDate = Calendar.getInstance();
        }
        else {
            Calendar repeatBase = Calendar.getInstance();
            if (!mRepeatFromComplete) {
                repeatBase = mDueDate;
            }
            int calUnit;
            int calNumber = 1;
            switch (mRepeatUnit) {
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
            calNumber *= mRepeatTime;
            mDueDate = repeatBase;
            mDueDate.add(calUnit, calNumber);
        }
    }
}
