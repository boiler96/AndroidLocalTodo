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

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

// This class allows a ViewPager's swiping to be programmatically enabled and
// disabled
public class ProgrammaticViewPager extends ViewPager implements GestureDetector.OnGestureListener {
    public ProgrammaticViewPager(Context context) {
        super(context);
        mEnabled = true;
        mSwipeDetector = new GestureDetector(getContext(), this);
    }

    public ProgrammaticViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEnabled = true;
        mSwipeDetector = new GestureDetector(getContext(), this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mEnabled) {
            return super.onTouchEvent(ev);
        }
        else {
            mSwipeDetector.onTouchEvent(ev);
            return true;
        }
    }

    public void Enable() {
        mEnabled = true;
    }

    public void Disable(ProgrammaticViewPagerNotify notify) {
        // Create a GestureDetector to catch swipes that happen when they
        // shouldn't.
        mNotify = notify;
        mEnabled = false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!mEnabled) {
            mNotify.SwipedWhileDisabled();
        }
        return false;
    }

    public interface ProgrammaticViewPagerNotify {
        public void SwipedWhileDisabled();
    }

    private Boolean mEnabled;
    private ProgrammaticViewPagerNotify mNotify;
    private GestureDetector mSwipeDetector;
}
