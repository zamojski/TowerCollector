/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import info.zamojski.soft.towercollector.views.MainActivityPagerAdapter;
import timber.log.Timber;

public class NonSwipeableViewPager extends ViewPager {

    private boolean swipingEnabled = true;

    public NonSwipeableViewPager(@NonNull Context context) {
        super(context);
    }

    public NonSwipeableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipingEnabled && super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return swipingEnabled && super.onTouchEvent(event);
        } catch (IllegalArgumentException ex) {
            // it sometimes happens that ViewPager.onTouchEvent is unable to obtain MotionEvent.getX and MotionEvent.nativeGetAxisValue
            Timber.w(ex, "onTouchEvent(): Failed to handle event on system level.");
            return false;
        }
    }

    @Override
    public void setCurrentItem(int item) {
        refreshSwipingEnabled(item);
        super.setCurrentItem(item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        refreshSwipingEnabled(item);
        super.setCurrentItem(item, smoothScroll);
    }

    private void refreshSwipingEnabled(int item) {
        ISwipingController adapter = (ISwipingController) getAdapter();
        if (adapter != null) {
            setSwipingEnabled(adapter.shouldEnableSwiping(item));
        }
    }

    private void setSwipingEnabled(boolean enabled) {
        this.swipingEnabled = enabled;
    }
}
