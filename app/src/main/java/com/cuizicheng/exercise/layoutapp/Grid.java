package com.cuizicheng.exercise.layoutapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuizicheng on 2017/2/28.
 * 对应拼图中的格子
 */

public class Grid {
    private final int id;
    private static final int LINE_INDEX_L = 0;
    private static final int LINE_INDEX_T = 1;
    private static final int LINE_INDEX_R = 2;
    private static final int LINE_INDEX_B = 3;
    private static final String[] lineDesc = new String[]{"Left", "Top", "Right", "Bottom"};
    private ControlLine[] mControlLines = new ControlLine[4];

    private final float[] initStartPoint;
    private final float[] initEndPoint;

    private boolean inited;
    private BmpInfo mBmpInfo;

    private Layout mLayout;
    private RectF mDisplayRect;

    private Paint mPaint;
    private int highLightColor = Color.GREEN;
    private boolean hlState;
    private int normalColor;


    private boolean stateDragging;

    public Grid(int id, float[] startP, float[] endP) {
        this.id = id;
        initStartPoint = startP;
        initEndPoint = endP;
    }

    public void setLayout(Layout layout) {
        mLayout = layout;
    }

    public void setPaint(Paint paint) {
        mPaint = new Paint(paint);
        normalColor = mPaint.getColor();
    }

    public int getId() {
        return id;
    }

    public String getLineDesc(int lineId) {
        if (lineId > lineDesc.length - 1 || lineId < 0)
            return "UnKnown";
        return lineDesc[lineId];
    }

    public ControlLine[] getAvaliableControllines() {
        List<ControlLine> result = new ArrayList<>();

        for (ControlLine l : mControlLines) {
            if (l != null && l.avaliable()) {
                result.add(l);
            }
        }

        if (result.isEmpty())
            return null;

        return result.toArray(new ControlLine[result.size()]);
    }

    public void setImgResId(int resId) {
        if (mBmpInfo == null) {
            mBmpInfo = new BmpInfo(resId, mLayout.getRes());
        }

        if (inited) {
            mBmpInfo.calcRectImpl(mDisplayRect);
        }

    }

    public void setBmpInfo(BmpInfo info) {
        mBmpInfo = info;
        if (inited && mBmpInfo != null)
            mBmpInfo.calcRectImpl(mDisplayRect);
    }

    public RectF getDisplayRect() {
        return mDisplayRect;
    }

    /**
     * 可控制边是否可以水平方向移动到 dstX
     *
     * @param line 当前移动的控制边
     * @param dstX 控制边新的 x 轴坐标
     * @return
     */
    public boolean canControlLineHorizontalMove(ControlLine line, float dstX) {
        int id = line.getId();
        //只考虑 Left,Right 两条控制边
        switch (id) {
            case LINE_INDEX_L:
                //宽度小于规定值，且 dstX 使得新的宽度比当前宽度更小，则不能再移动了
                return !((mDisplayRect.width() <= 100) && (mControlLines[LINE_INDEX_R].getStartPoint().x - dstX) < mDisplayRect.width());
            case LINE_INDEX_R:
                //宽度小于规定值，且 dstX 使得新的宽度比当前宽度更小，则不能再移动了
                return !((mDisplayRect.width() <= 100) && (dstX - mControlLines[LINE_INDEX_L].getStartPoint().x) < mDisplayRect.width());
        }

        return true;
    }

    /**
     * 可控制边是否可以垂直方向移动到 dstY
     *
     * @param dstY 可控制边将要移动到的 y 轴坐标
     * @return
     */
    public boolean canControlLineVerticalMove(ControlLine line, float dstY) {
        int id = line.getId();
        //只考虑 Top,Bottom 两条控制边
        switch (id) {
            case LINE_INDEX_T:
                //当前高度小于等于规定值，且 dstY 使得新的高度比当前高度更小，则不能继续移动
                return !((mDisplayRect.height() <= 100) && (mControlLines[LINE_INDEX_B].getStartPoint().y - dstY) < mDisplayRect.height());
            case LINE_INDEX_B:
                //当前高度小于等于规定值，且 dstY 使得新的高度比当前高度更小，则不能继续移动
                return !((mDisplayRect.height() <= 100) && (dstY - mControlLines[LINE_INDEX_T].getStartPoint().y) < mDisplayRect.height());
        }

        return true;
    }


    public void init() {
        if (inited)
            return;
//        Log.d("layout", "grid init call");
        //1、确定自己的大小
        float w = mLayout.getWidth() * (initEndPoint[0] - initStartPoint[0]);
        float h = mLayout.getHeight() * (initEndPoint[1] - initStartPoint[1]);
        float left = mLayout.getLeft() + mLayout.getWidth() * initStartPoint[0];
        float right = left + w;
        float top = mLayout.getTop() + mLayout.getHeight() * initStartPoint[1];
        float bottom = top + h;
        //去掉小数点后面的数
        mDisplayRect = new RectF((int) left, (int) top, (int) right, (int) bottom);
//        Log.d("layout", "grid size : " + mDisplayRect);


        //2、初始化控制线
        mControlLines[LINE_INDEX_L] = new ControlLine(mDisplayRect.left, mDisplayRect.top, mDisplayRect.left, mDisplayRect.bottom);
        mControlLines[LINE_INDEX_L].setId(LINE_INDEX_L);
        mControlLines[LINE_INDEX_T] = new ControlLine(mDisplayRect.left, mDisplayRect.top, mDisplayRect.right, mDisplayRect.top);
        mControlLines[LINE_INDEX_T].setId(LINE_INDEX_T);
        mControlLines[LINE_INDEX_R] = new ControlLine(mDisplayRect.right, mDisplayRect.top, mDisplayRect.right, mDisplayRect.bottom);
        mControlLines[LINE_INDEX_R].setId(LINE_INDEX_R);
        mControlLines[LINE_INDEX_B] = new ControlLine(mDisplayRect.left, mDisplayRect.bottom, mDisplayRect.right, mDisplayRect.bottom);
        mControlLines[LINE_INDEX_B].setId(LINE_INDEX_B);

        mControlLines[LINE_INDEX_L].setAvaliable(!(Float.compare(mLayout.getLeft(), mDisplayRect.left) == 0));
        mControlLines[LINE_INDEX_T].setAvaliable(!(Float.compare(mLayout.getTop(), mDisplayRect.top) == 0));
        mControlLines[LINE_INDEX_R].setAvaliable(!(Float.compare(mLayout.getRight(), mDisplayRect.right) == 0));
        mControlLines[LINE_INDEX_B].setAvaliable(!(Float.compare(mLayout.getBottom(), mDisplayRect.bottom) == 0));

        for (ControlLine l : mControlLines) {
            l.setGrid(this);
        }

        //3、
        inited = true;

        //4、
        mBmpInfo.calcRectImpl(mDisplayRect);
    }


    public void calcDisplayRect(ControlLine movedLine) {
        int i = movedLine.getId();

        switch (i) {
            case LINE_INDEX_L:
                mControlLines[LINE_INDEX_T].setStartPoint(movedLine.getStartPoint());
                mControlLines[LINE_INDEX_B].setStartPoint(movedLine.getEndPoint());
                break;
            case LINE_INDEX_R:
                mControlLines[LINE_INDEX_T].setEndPoint(movedLine.getStartPoint());
                mControlLines[LINE_INDEX_B].setEndPoint(movedLine.getEndPoint());
                break;
            case LINE_INDEX_T:
                mControlLines[LINE_INDEX_L].setStartPoint(movedLine.getStartPoint());
                mControlLines[LINE_INDEX_R].setStartPoint(movedLine.getEndPoint());
                break;
            case LINE_INDEX_B:
                mControlLines[LINE_INDEX_L].setEndPoint(movedLine.getStartPoint());
                mControlLines[LINE_INDEX_R].setEndPoint(movedLine.getEndPoint());
                break;
        }

        float[] lt = mControlLines[LINE_INDEX_L].getStartPointXY();
        float[] rb = mControlLines[LINE_INDEX_B].getEndPointXY();

        mDisplayRect.set(lt[0], lt[1], rb[0], rb[1]);
        if (mBmpInfo != null)
            mBmpInfo.calcRectImpl(mDisplayRect);
    }

    public void reDraw() {
        mLayout.reDraw();
    }


    public void testSharedControlLinePrint() {
        Log.d("layout", "#" + id + ":\n");
        for (ControlLine line : getAvaliableControllines()) {
            Log.d("layout", line.toString() + "\n");
        }
    }

    public void findSharedControlLine(Grid grid) {
        if (grid != null) {
            ControlLine[] mLines = getAvaliableControllines();
            ControlLine[] otherLines = grid.getAvaliableControllines();

            for (ControlLine l : mLines) {
                for (ControlLine ol : otherLines) {
                    if (l.samePointWidth(ol) || l.isConnectTo(ol)) {
                        l.setSharedControlLine(ol);
                    }
                }
            }
        }
    }


    public boolean touchIn(float x, float y) {
        return mDisplayRect.contains(x, y);
    }

    ControlLine currentTouchLine;

    void onTouchEventDown(float x, float y) {
        Log.d("layout", "grid touch down");
        currentTouchLine = findTouchOnLine(x, y);

    }

    //找到触摸坐标是否在某个边上
    ControlLine findTouchOnLine(float x, float y) {
        for (ControlLine line : mControlLines) {
            if (line.touchOn(x, y)) {
                return line;
            }
        }

        return null;
    }


    void onTouchEventMove(MotionEvent event) {
        if (currentTouchLine != null) {
            currentTouchLine.handleTouchEventMove(event);
        } else if (mBmpInfo != null) {
            mBmpInfo.onTouchMove(event.getX(), event.getY());
            mLayout.reDraw();
        }
    }


    void onTouchCancelOrUp(MotionEvent event) {
        if (mBmpInfo != null) {
            mBmpInfo.clearMoveInfo();
            mBmpInfo.resotrePosition(mDisplayRect);
            mLayout.reDraw();
        }
    }


    void onDraw(Canvas canvas) {
        canvas.drawRect(mDisplayRect, mPaint);
        drawBmp(mBmpInfo, canvas);
    }

    void drawBmp(BmpInfo bmpInfo, Canvas canvas) {
        if (bmpInfo == null || stateDragging)
            return;

        canvas.save();
        canvas.clipRect(mDisplayRect);
        canvas.drawBitmap(bmpInfo.bmp, null, bmpInfo.rectF, null);
        canvas.restore();

    }


    /**
     * 获得代表拖拽信息的对象，可能返回 null
     *
     * @return
     */
    Layout.DraggingInfo getDraggingInfo() {
        if (mBmpInfo == null || mBmpInfo.isMoving || currentTouchLine != null)
            return null;

        stateDragging = true;
        return mLayout.new DraggingInfo(mBmpInfo.originRectF, mDisplayRect, mBmpInfo.bmp);
    }

    /**
     * 取消拖拽状态
     */
    void cancelDraggingSate() {
        stateDragging = false;
        mBmpInfo.calcRectImpl(mDisplayRect);
        mLayout.reDraw();
    }


    boolean isDragging() {
        return stateDragging;
    }


    /**
     * 交换两个 Grid 的图片
     *
     * @param other
     */
    public void swapBmpInfo(Grid other) {
        Log.d("layout", "swap bmp info");

        BmpInfo tmp = mBmpInfo;

        setBmpInfo(other.mBmpInfo);
        other.setBmpInfo(tmp);

    }

    public void highLight(boolean highlight) {
        hlState = highlight;
        mPaint.setColor(hlState ? highLightColor : normalColor);
    }

    private static class BmpInfo {
        int imgPath;
        int originWidth;
        int originHeight;
        //缩放后的尺寸，撑满 Grid 的空间
        RectF rectF;
        //图片原始的尺寸
        RectF originRectF;
        Bitmap bmp;
        Matrix matrix;
        Resources res;
        boolean isMoving;

        public BmpInfo(int resId, Resources resources) {
            res = resources;
            imgPath = resId;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeResource(res, imgPath, options);

            originWidth = options.outWidth;
            originHeight = options.outHeight;

            options.inJustDecodeBounds = false;
            options.inSampleSize = 4;
            bmp = BitmapFactory.decodeResource(res, imgPath, options);

            matrix = new Matrix();
            originRectF = new RectF(0, 0, originWidth, originHeight);
            rectF = new RectF();
        }


        //得到最小的包含显示区域的矩形
        private void calcRectImpl(RectF displayRect) {
            rectF.set(originRectF);
            matrix.setRectToRect(originRectF, displayRect, Matrix.ScaleToFit.CENTER);
            matrix.mapRect(rectF);

            if (Math.abs(rectF.width() - displayRect.width()) < 1) {
                float ratio = displayRect.height() / rectF.height();
                matrix.postScale(ratio, ratio, rectF.centerX(), rectF.centerY());
            } else if (Math.abs(rectF.height() - displayRect.height()) < 1) {
                float ratio = displayRect.width() / rectF.width();
                matrix.postScale(ratio, ratio, rectF.centerX(), rectF.centerY());
            } else {
                Log.d("layout", "else condition");
            }

            rectF.set(originRectF);
            matrix.mapRect(rectF);
        }

        float lastX;
        float lastY;

        private void onTouchMove(float x, float y) {
            if (lastX == 0 && lastY == 0) {
                lastX = x;
                lastY = y;
                isMoving = true;
                return;
            }


            rectF.offset(x - lastX, y - lastY);
            lastX = x;
            lastY = y;
        }

        private void clearMoveInfo() {
            lastX = 0;
            lastY = 0;
            isMoving = false;
        }

        //
        private void resotrePosition(RectF displayRectF) {

            if (((int) displayRectF.width()) <= ((int) rectF.width()) && ((int) displayRectF.height()) <= ((int) rectF.height())) {
                float dx = 0;
                float dy = 0;

                if (rectF.left > displayRectF.left) {
                    dx = displayRectF.left - rectF.left;
                }

                if (rectF.right < displayRectF.right) {
                    dx = displayRectF.right - rectF.right;
                }

                if (rectF.top > displayRectF.top) {
                    dy = displayRectF.top - rectF.top;
                }

                if (rectF.bottom < displayRectF.bottom) {
                    dy = displayRectF.bottom - rectF.bottom;
                }

                rectF.offset(dx, dy);
            }


        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("init start [");
        sb.append(initStartPoint[0]).append(",").append(initStartPoint[1]).append("]");
        sb.append(",init end [").append(initEndPoint[0]).append(",").append(initEndPoint[1]).append("]");
        return sb.toString();
    }
}
