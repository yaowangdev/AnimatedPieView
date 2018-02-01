package com.razerdp.widget.animatedpieview.render;

import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;

import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.IPieInfo;
import com.razerdp.widget.animatedpieview.utils.DegreeUtil;
import com.razerdp.widget.animatedpieview.utils.PLog;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 大灯泡 on 2018/2/1.
 */
final class PieInfoWrapper implements Serializable {
    private final String id;
    private final IPieInfo mPieInfo;

    //============= 绘制设置 =============
    private Paint mDrawPaint;
    private Paint mTouchPaint;
    private Path mDrawPath;

    //============= 参数 =============
    private float fromDegree;
    private float sweepDegree;
    private float toDegree;
    private boolean autoDesc;
    private String desc;


    PieInfoWrapper(IPieInfo pieInfo) {
        if (pieInfo == null) {
            throw new NullPointerException("pieinfo must not be null");
        }
        id = generateId();
        mPieInfo = pieInfo;
    }

    String getId() {
        return id;
    }


    PieInfoWrapper prepare(AnimatedPieViewConfig config) {
        if (mDrawPaint == null) mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        if (mTouchPaint == null) mTouchPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        if (mDrawPath == null) mDrawPath = new Path();

        mDrawPaint.setStyle(config.isStrokeMode() ? Paint.Style.STROKE : Paint.Style.FILL);
        mDrawPaint.setStrokeWidth(config.getStrokeWidth());
        mDrawPaint.setColor(mPieInfo.getColor());
        mTouchPaint.set(mDrawPaint);
        mDrawPath.reset();
        return this;
    }

    public IPieInfo getPieInfo() {
        return mPieInfo;
    }

    public Paint getDrawPaint() {
        return mDrawPaint;
    }

    public Paint getTouchPaint() {
        return mTouchPaint;
    }

    public Path getDrawPath() {
        mDrawPath.rewind();
        return mDrawPath;
    }

    public float getFromDegree() {
        return fromDegree;
    }

    public float getMiddleAngle() {
        return fromDegree + sweepDegree / 2;
    }

    public float getToDegree() {
        return toDegree;
    }

    public float getSweepAngle() {
        return sweepDegree;
    }

    public boolean isAutoDesc() {
        return autoDesc;
    }

    public void setAutoDesc(boolean autoDesc) {
        this.autoDesc = autoDesc;
    }

    public String getDesc() {
        return desc;
    }

    public float calculateDegree(float lastPieDegree, double sum, AnimatedPieViewConfig config) {
        fromDegree = lastPieDegree;
        sweepDegree = (float) (360f * (Math.abs(mPieInfo.getValue()) / sum));
        toDegree = fromDegree + sweepDegree;
        if (autoDesc) {
            //自动填充描述auto
            desc = String.format(config.getAutoDescStringFormat(), AnimatedPieViewConfig.sFormateRate.format((mPieInfo.getValue() / sum) * 100));
        } else {
            desc = mPieInfo.getDesc();
        }
        PLog.d("【calculate】 " + "{ \n" + "id = " + id + "\nfromDegree = " + fromDegree + "\nsweepDegree = " + sweepDegree + "\ntoDegree = " + toDegree + "\n desc = " + desc + "\n  }");
        return toDegree;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PieInfoWrapper)) {
            return false;
        } else {
            PieInfoWrapper from = (PieInfoWrapper) obj;
            return obj == this || TextUtils.equals(from.getId(), id);
        }
    }

    //=============================================================generate id
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private String generateId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();

            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1;//Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return Integer.toString(result);
            }
        }
    }

    //=============================================================tools
    boolean contains(float angle) {
        return angle >= fromDegree && angle <= toDegree;
    }

    boolean containsTouch(float angle) {
        //所有点击的角度都需要收归到0~360的范围，兼容任意角度
        final float tAngle = DegreeUtil.limitDegreeInTo360(angle);
        float tStart = DegreeUtil.limitDegreeInTo360(fromDegree);
        float tEnd = DegreeUtil.limitDegreeInTo360(toDegree);
        PLog.d("containsTouch  >>  tStart： " + tStart + "   tEnd： " + tEnd + "   tAngle： " + tAngle);
        boolean result;
        if (tEnd < tStart) {
            if (tAngle > 180) {
                //已经过界
                result = tAngle >= tStart && (360 - tAngle) <= tEnd;
            } else {
                result = tAngle + 360 >= tStart && tAngle <= tEnd;
            }
        } else {
            result = tAngle >= tStart && tAngle <= tEnd;
        }
        if (result) {
            PLog.i("find touch point  >>  " + toString());
        }
        return result;
    }

    @Override
    public String toString() {
        return "{ \nid = " + id + '\n'
                + "value =  " + getPieInfo().getValue() + '\n'
                + "fromDegree = " + fromDegree + '\n'
                + "toDegree = " + toDegree + "\n  }";
    }
}
