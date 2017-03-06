package com.cuizicheng.exercise.layoutapp;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Created by cuizicheng on 2017/2/28.
 * 拼图中的控制线
 */

public class ControlLine {
    private int id;
    private int mType;
    private ControlLineManager mManager;
    private final PointF mStartPoint;
    private final PointF mEndPoint;

    private Grid mGrid;
    private boolean canMove;


    public ControlLine(PointF startP, PointF endP) {
        mStartPoint = startP;
        mEndPoint = endP;

        if (Float.compare(mStartPoint.x, mEndPoint.x) == 0) {
            mType = DirectType.vetical;
        } else if (Float.compare(mStartPoint.y, mEndPoint.y) == 0) {
            mType = DirectType.horizontal;
        }
    }

    public void setGrid(Grid grid) {
        mGrid = grid;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    interface DirectType {
        int vetical = 1;
        int horizontal = 2;
        String[] info = new String[]{"disable", "vertical", "horizontal"};
    }

    public ControlLine(float startX, float startY, float endX, float endY) {
        this(new PointF(startX, startY), new PointF(endX, endY));
    }

    public void setAvaliable(boolean b) {
        canMove = b;
    }


    //与另一个 ControlLine 方向是否相同。
    private boolean sameDirection(ControlLine line) {
        return (mType == line.mType) && (mType != 0);
    }

    /**
     * 和另一个 ControlLine 是否端点重合
     *
     * @param line
     * @return
     */
    public boolean samePointWidth(ControlLine line) {
        if (line != null && sameDirection(line)) {
            return length(mStartPoint, line.mStartPoint) == 0 && length(mEndPoint, line.mEndPoint) == 0;
//            return mStartPoint.equals(line.mStartPoint) && mEndPoint.equals(line.mEndPoint);
        }
        return false;
    }

    /**
     * 两个控制线是否有某段重合或者一个控制线的尾端点和另一个控制线的头端点重合
     *
     * @param line
     * @return
     */
    public boolean isConnectTo(ControlLine line) {
        if (line != null && sameDirection(line)) {
            //两个线段不重合的长度
            float totalLen = length(mStartPoint, mEndPoint) + length(line.mStartPoint, line.mEndPoint);

            //如果重合，两个线段任意一个的开始点到另一个终点的长度都是小于 totalLen 的
            boolean overlay = (length(mStartPoint, line.mEndPoint) <= totalLen && length(line.mStartPoint, mEndPoint) <= totalLen);
//            Log.d("layout", "overlay:" + overlay);
//            Log.d("layout", "m:" + "[" + mStartPoint + "," + mEndPoint + "]" + ";other:[" + line.mStartPoint + "," + line.mEndPoint + "]");

            //
            if (overlay) {
                if (mType == DirectType.vetical) {
                    return Float.compare(mStartPoint.x, line.mStartPoint.x) == 0;
                } else if (mType == DirectType.horizontal) {
                    return Float.compare(mStartPoint.y, line.mStartPoint.y) == 0;
                }
            }

        }
        return false;
    }

    private static float length(PointF a, PointF b) {
        return (float) Math.hypot(b.x - a.x, b.y - a.y);
    }


    /**
     * 设置与另一个 ControlLine 共享控制线
     *
     * @param line
     */
    public void setSharedControlLine(ControlLine line) {
        if (!sameDirection(line))
            return;

        if (mManager == null) {
            mManager = new ControlLineManager();
        }

        if (line.mManager != null) {
            line.mManager.remove(line);
        }

        line.mManager = mManager;

        mManager.add(line);
        mManager.add(this);
    }


    public void handleTouchEventMove(MotionEvent event) {
        if (mType == DirectType.horizontal && mManager.canSharedControlLineVerticalMove(event.getY())) {

            handleTouchEventUpAndDown(event.getY());

        } else if (mType == DirectType.vetical && mManager.canSharedControlLineHorizontalMove(event.getX())) {

            handleTouchEventLeftAndRight(event.getX());
        }
    }

    /**
     * 能否水平移动到 dstX
     *
     * @param dstX
     * @return
     */
    boolean canHorizontalMove(float dstX) {
        return mGrid.canControlLineHorizontalMove(this, dstX);
    }

    /**
     * 能否垂直移动到 dstY
     *
     * @param dstY
     * @return
     */
    boolean canVerticalMove(float dstY) {
        return mGrid.canControlLineVerticalMove(this, dstY);
    }

    public void handleTouchEventUpAndDown(float y) {
        mManager.notifyAllLineUpAndDownEvent(y);
        mGrid.reDraw();
    }

    public void handleTouchEventLeftAndRight(float x) {
        mManager.notifyAllLineLeftAndRight(x);
        mGrid.reDraw();
    }


    public void onTouchEventUpAndDown(float y) {
        mStartPoint.y = y;
        mEndPoint.y = y;

        mGrid.calcDisplayRect(this);
    }

    public void onTouchEventLeftAndRight(float x) {
        mStartPoint.x = x;
        mEndPoint.x = x;

        mGrid.calcDisplayRect(this);
    }


    public String getDescName() {
        StringBuilder sb = new StringBuilder();
        sb.append("name:").append(mGrid.getId()).append("#Grid__").append(mGrid.getLineDesc(id));
        sb.append("(")
                .append("[").append(mStartPoint.x).append(",").append(mStartPoint.y).append("]")
                .append("[").append(mEndPoint.x).append(",").append(mEndPoint.y).append("]")
                .append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDescName()).append(",");
        sb.append("type:").append(DirectType.info[mType]).append(",hasMgr:").append(mManager != null);
        if (mManager != null) {
            sb.append(",mgrInfo:").append(mManager.toString());
        }

        return sb.toString();
    }


    private static final int touchActiveDis = 20;

    public boolean touchOn(float x, float y) {
        if (canMove) {
            if (mType == DirectType.horizontal) {
                return (y >= mStartPoint.y - touchActiveDis && y <= mStartPoint.y + touchActiveDis) && (x >= mStartPoint.x && x <= mEndPoint.x);
            } else if (mType == DirectType.vetical) {
                return (x >= mStartPoint.x - touchActiveDis && x <= mStartPoint.x + touchActiveDis) && (y >= mStartPoint.y && y <= mEndPoint.y);
            }
        }

        return false;
    }


    public PointF getStartPoint() {
        return mStartPoint;
    }

    public PointF getEndPoint() {
        return mEndPoint;
    }

    public float[] getStartPointXY() {
        return new float[]{mStartPoint.x, mStartPoint.y};
    }

    public float[] getEndPointXY() {
        return new float[]{mEndPoint.x, mEndPoint.y};
    }


    public void setStartPoint(PointF p) {
        mStartPoint.set(p);
    }

    public void setEndPoint(PointF p) {
        mEndPoint.set(p);
    }


    public boolean avaliable() {
        return canMove;
    }
}
