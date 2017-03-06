package com.cuizicheng.exercise.layoutapp;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by cuizicheng on 2017/2/28.
 */

public class LayoutView extends View {
    private Layout mLayout;
    private int mWidth, mHeight;
    private boolean haveSize;

    public LayoutView(Context context) {
        super(context);
    }

    public LayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LayoutView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setLayout(Layout layout) {
        if (layout == null)
            return;

        mLayout = layout;
        mLayout.setLayoutView(this);

        if (!mLayout.isInited() && haveSize) {
            mLayout.init();
            invalidate();
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        haveSize = true;
        mWidth = w;
        mHeight = h;

        if (mLayout != null && !mLayout.isInited()) {
            mLayout.init();
        }


    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mLayout != null && mLayout.isInited()) {
            mLayout.onDraw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLayout != null) {
            return mLayout.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
}
