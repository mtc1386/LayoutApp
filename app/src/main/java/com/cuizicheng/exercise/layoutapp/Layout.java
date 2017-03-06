package com.cuizicheng.exercise.layoutapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by cuizicheng on 2017/2/28.
 * 对应拼图的布局类
 */

public class Layout {
    private float initWidthF;
    private float initHeightF;

    private LayoutView mHost;
    private RectF mLayoutRect;
    private Grid[] mGrids;
    private boolean inited;
    private DraggingInfo mDraggingInfo;

    private Paint mPaint;
    private Paint mGridsPaint;

    private GestureDetector mLongPressDetector;

    RectF highLightRect;
    Paint highLightPaint;

    public Layout(float wRatio, float hRatio, Grid[] grids) {
        initWidthF = wRatio;
        initHeightF = hRatio;
        mGrids = grids;

        mLayoutRect = new RectF();
        highLightRect = new RectF();

        highLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highLightPaint.setStyle(Paint.Style.STROKE);
        highLightPaint.setStrokeWidth(6);
        highLightPaint.setColor(Color.YELLOW);

        mGridsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridsPaint.setStyle(Paint.Style.STROKE);
        mGridsPaint.setStrokeWidth(3);
        mGridsPaint.setColor(Color.RED);

        mPaint = new Paint(mGridsPaint);
        mPaint.setColor(Color.BLUE);

        for (Grid grid : mGrids) {
            grid.setLayout(this);
            grid.setPaint(mGridsPaint);
        }
    }


    public void setLayoutView(LayoutView view) {

        if (view == null)
            return;

        mHost = view;
        mLongPressDetector = new GestureDetector(mHost.getContext(), new OnLongPressListener());
    }

    Grid currentGrid;
    Grid swapGrid;

    public boolean onTouchEvent(MotionEvent event) {
        int code = event.getActionMasked();
        switch (code) {
            case MotionEvent.ACTION_DOWN:
                if (inited && mLayoutRect.contains(event.getX(), event.getY())) {
                    currentGrid = findTouchOnGrid(event.getX(), event.getY());
                    //1、坐标点在控制线上
                    if (currentGrid != null) {
                        currentGrid.onTouchEventDown(event.getX(), event.getY());

                    }

                    mLongPressDetector.onTouchEvent(event);

                    //2、坐标点在其他区域

                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (currentGrid != null) {
                    currentGrid.onTouchEventMove(event);

                    //如果处于拖拽模式，找出可能要交换的 Grid
                    if (mDraggingInfo != null) {
                        mDraggingInfo.onTouchMove(event.getX(), event.getY());
                        swapGrid = findTouchOnGrid(event.getX(), event.getY());
                        if (swapGrid.getId() == currentGrid.getId()) {
                            highLightRect.setEmpty();
                            swapGrid = null;
                        } else {
                            highLightRect.set(swapGrid.getDisplayRect());
                        }
                    }

                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (swapGrid != null) {
                    currentGrid.swapBmpInfo(swapGrid);
                    swapGrid = null;
                    highLightRect.setEmpty();

                }

                if (mDraggingInfo != null) {
                    mDraggingInfo.destory();
                    mDraggingInfo = null;
                    currentGrid.cancelDraggingSate();
                }


                if (currentGrid != null) {
                    currentGrid.onTouchCancelOrUp(event);
                    currentGrid = null;
                }
                break;
        }


        return true;
    }


    private class OnLongPressListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            Log.d("layout", "long press detected!");
            if (currentGrid != null && (mDraggingInfo = currentGrid.getDraggingInfo()) != null) {
                mDraggingInfo.updateRects();
                reDraw();
            }
        }
    }


    class DraggingInfo {
        //图片原始尺寸
        RectF originPictureSize;
        //图片缩放后的尺寸，正好包含显示区域矩形
        RectF scaledFitSize;
        //显示区域
        RectF displayArea;
        Bitmap mBmp;
        Matrix matrix;
        float lastX;
        float lastY;
        Paint alphPaint;

        public DraggingInfo(RectF origin, RectF initialDisplayArea, Bitmap bmp) {
            originPictureSize = new RectF(origin);
            scaledFitSize = new RectF(origin);
            displayArea = new RectF(initialDisplayArea);
            mBmp = bmp;

            //显示区域放大一点
            matrix = new Matrix();
            matrix.postScale(1.3f, 1.3f, displayArea.centerX(), displayArea.centerY());
            matrix.mapRect(displayArea);
            matrix.reset();

            alphPaint = new Paint();
            alphPaint.setAlpha(100);

        }

        public void destory() {
            originPictureSize = null;
            scaledFitSize = null;
            displayArea = null;
            matrix = null;
            mBmp = null;
        }

        public void onTouchMove(float x, float y) {
            if (lastX == 0 && lastY == 0) {
                lastX = x;
                lastY = y;
                return;
            }

            //1、更新 displayRect
            displayArea.offset(x - lastX, y - lastY);
            lastX = x;
            lastY = y;


            //2、更新绘制区域
            updateRects();


            //3、
            reDraw();

        }

        public void onDraw(Canvas canvas) {
            Log.d("layout", "dragging onDraw");
            canvas.save();
            canvas.clipRect(displayArea);
            canvas.drawBitmap(mBmp, null, scaledFitSize, alphPaint);
            canvas.restore();
        }

        public void updateRects() {
            scaledFitSize.set(originPictureSize);
            matrix.setRectToRect(originPictureSize, displayArea, Matrix.ScaleToFit.CENTER);
            matrix.mapRect(scaledFitSize);

            if (Math.abs(scaledFitSize.width() - displayArea.width()) < 1) {
                float ratio = displayArea.height() / scaledFitSize.height();
                matrix.postScale(ratio, ratio, scaledFitSize.centerX(), scaledFitSize.centerY());
            } else if (Math.abs(scaledFitSize.height() - scaledFitSize.height()) < 1) {
                float ratio = displayArea.width() / scaledFitSize.width();
                matrix.postScale(ratio, ratio, scaledFitSize.centerX(), scaledFitSize.centerY());
            } else {
                Log.d("layout", "else condition");
            }

            scaledFitSize.set(originPictureSize);
            matrix.mapRect(scaledFitSize);
        }

    }

    public Resources getRes() {
        return mHost == null ? null : mHost.getResources();
    }

    Grid findTouchOnGrid(float x, float y) {
        for (int i = 0; i < mGrids.length; i++) {
            if (mGrids[i].touchIn(x, y)) {
                return mGrids[i];
            }
        }
        return null;
    }

    public float getLeft() {
        return mLayoutRect.left;
    }

    public float getTop() {
        return mLayoutRect.top;
    }

    public float getRight() {
        return mLayoutRect.right;
    }

    public float getBottom() {
        return mLayoutRect.bottom;
    }

    public boolean isInited() {
        return inited;
    }

    float getWidth() {
        return mLayoutRect.width();
    }

    float getHeight() {
        return mLayoutRect.height();
    }

    public void init() {
//        Log.d("layout", "layout init call");
        if (inited)
            return;

        //1、确定自己的大小
        RectF hostRect = new RectF(0, 0, mHost.getMeasuredWidth(), mHost.getMeasuredHeight());
        RectF mRect = new RectF(0, 0, initWidthF, initHeightF);

        Matrix matrix = new Matrix();
        matrix.setRectToRect(mRect, hostRect, Matrix.ScaleToFit.CENTER);
        matrix.mapRect(mLayoutRect, mRect);

//        Log.d("layout", "layout size : " + mLayoutRect.toString());

        //2、确定 grid 大小
        for (int i = 0; i < mGrids.length; i++) {
            mGrids[i].init();
        }

        //3、找共享控制线
        findSharedControlLine();

//        for (Grid g : mGrids) {
//            g.testSharedControlLinePrint();
//        }

        inited = true;
    }


    public void setImgResourceIds(int[] ids) {
        if (mGrids != null && ids != null && ids.length > 0) {
            int id = ids[0];
            for (int i = 0; i < mGrids.length; i++) {
                if (i < ids.length) {
                    id = ids[i];
                }

                mGrids[i].setImgResId(id);
            }
        }
    }

    private void findSharedControlLine() {
        Grid s;
        Grid d;
        for (int i = 0; i < mGrids.length - 1; i++) {
            s = mGrids[i];
            for (int j = i + 1; j < mGrids.length; j++) {
                d = mGrids[j];
                s.findSharedControlLine(d);
            }
        }
    }

    public void reDraw() {
        mHost.invalidate();
    }

    public void onDraw(Canvas canvas) {
        canvas.drawRect(mLayoutRect, mPaint);
        for (Grid grid : mGrids) {
            grid.onDraw(canvas);
        }

        if (!highLightRect.isEmpty()) {
            canvas.drawRect(highLightRect, highLightPaint);
        }

        if (mDraggingInfo != null)
            mDraggingInfo.onDraw(canvas);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ratio:").append(initWidthF).append(":").append(initHeightF);
        sb.append(",grids number:").append(mGrids.length);
        return sb.toString();
    }
}
