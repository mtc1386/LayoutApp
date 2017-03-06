package com.cuizicheng.exercise.layoutapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuizicheng on 2017/2/28.
 */

public class ControlLineManager {
    List<ControlLine> mLines = new ArrayList<>();

    public void add(ControlLine line) {
        if (!mLines.contains(line)) {
            mLines.add(line);
        }
    }

    public void remove(ControlLine line) {
        if (mLines.contains(line)) {
            mLines.remove(line);
        }
    }

    public void notifyAllLineUpAndDownEvent(float y) {
        for (ControlLine l : mLines) {
            l.onTouchEventUpAndDown(y);
        }
    }

    public void notifyAllLineLeftAndRight(float x) {
        for (ControlLine l : mLines) {
            l.onTouchEventLeftAndRight(x);
        }
    }


    /**
     * 共享控制边的所有成员是否都可以移动
     *
     * @param dstX 控制边的新的 x 轴坐标
     * @return
     */
    boolean canSharedControlLineHorizontalMove(float dstX) {
        for (ControlLine l : mLines) {
            if (!l.canHorizontalMove(dstX)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 共享控制边的所有成员是否都可以移动
     * @param dstY
     * @return
     */
    boolean canSharedControlLineVerticalMove(float dstY) {
        for (ControlLine l : mLines) {
            if (!l.canVerticalMove(dstY)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (ControlLine l : mLines) {
            sb.append(l.getDescName()).append(";\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
