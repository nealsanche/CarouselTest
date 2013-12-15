package com.robotsandpencils.carousel;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.nineoldandroids.animation.TimeAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import hugo.weaving.DebugLog;

/**
 * Created by neal on 12/14/2013.
 */
public class Carousel extends FrameLayout {

    private Camera mCamera = new Camera();
    private float rotation = 0.0f;
    private boolean stopped = false;

    private HashMap<View, Integer> childOrder = new HashMap<View, Integer>();
    private TimeAnimator animator;

    SparseIntArray drawingOrder = new SparseIntArray();
    private float tiltAngleY = 0.0f;
    private float tiltAngleZ = 0.0f;
    private float stretchZ = 1.0f;
    private float stretchX = 1.0f;
    private Scroller scroller;
    private GestureDetector detector;
    private boolean mDetectedFling;

    public Carousel(Context context) {
        super(context);
        createAnimationLoop();
    }

    public Carousel(Context context, AttributeSet attrs) {
        super(context, attrs);
        createAnimationLoop();
    }

    public Carousel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createAnimationLoop();
    }

    private void createAnimationLoop() {
        scroller = new Scroller(getContext());

        detector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @DebugLog
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @DebugLog
            @Override
            public void onShowPress(MotionEvent e) {

            }

            @DebugLog
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @DebugLog
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //scroller.startScroll((int)rotation,0,(int)distanceX,0);
                //scroller.setFinalX((int)distanceX);
                //scroller.extendDuration(20000);
                rotation += distanceX / getWidth() * 5;
                return true;
            }

            @DebugLog
            @Override
            public void onLongPress(MotionEvent e) {

            }

            @DebugLog
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //scroller.fling(0,0,(int)velocityX,0,0,Integer.MAX_VALUE,0,0);
                //scroller.extendDuration(1000);
                //mDetectedFling = true;
                return true;
            }
        });
        setStaticTransformationsEnabled(true);
        setChildrenDrawingOrderEnabled(true);
        setChildrenDrawingCacheEnabled(true);

        animator = new TimeAnimator();
        animator.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator timeAnimator, long l, long l2) {

                if (mDetectedFling && scroller.computeScrollOffset()) {
                    rotation += -1 * scroller.getCurrX() / 100000.0;
                } else {
                    if (mDetectedFling) {
                        mDetectedFling = false;
                        animator.end();
                    }
                }

                if (animator.isRunning()) {
                    for (int i = 0; i < getChildCount(); i++) {
                        getChildAt(i).invalidate();
                    }

                    requestLayout();
                }
            }
        });
        TimeAnimator.setFrameDelay((long) (1000.0 / 120.0));

        ViewCompat.postInvalidateOnAnimation(this);

        //animator.start();
    }

    public float getTiltAngleY() {
        return tiltAngleY;
    }

    public void setTiltAngleY(float tiltAngleY) {
        this.tiltAngleY = tiltAngleY;
    }

    public float getTiltAngleZ() {
        return tiltAngleZ;
    }

    public void setTiltAngleZ(float tiltAngleZ) {
        this.tiltAngleZ = tiltAngleZ;
    }

    public float getStretchZ() {
        return stretchZ;
    }

    public void setStretchZ(float stretchZ) {
        this.stretchZ = stretchZ;
    }

    public float getStretchX() {
        return stretchX;
    }

    public void setStretchX(float stretchX) {
        this.stretchX = stretchX;
    }

    private static class ItemIndex {
        int index;
        float z;
    }

    ArrayList<ItemIndex> items = new ArrayList<ItemIndex>();
    private Comparator<ItemIndex> comparator = new Comparator<ItemIndex>() {
        @Override
        public int compare(ItemIndex lhs, ItemIndex rhs) {
            return new Float(rhs.z).compareTo(lhs.z);
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (items.size() != getChildCount()) {
            items.clear();
            for (int i = 0; i < getChildCount(); i++)
                items.add(new ItemIndex());
        }
        for (int i = 0; i < getChildCount(); i++) {
            items.get(i).index = i;
            items.get(i).z = ((CarouselItem) getChildAt(i)).getZ();
        }

        Collections.sort(items, comparator);

        for (int i = 0; i < getChildCount(); i++) {
            drawingOrder.put(i, items.get(i).index);
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return drawingOrder.get(i, super.getChildDrawingOrder(childCount, i));
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation transformation) {

        // Center of the view
        float centerX = (float) getWidth() / 2, centerY = (float) getHeight() / 2;

        transformation.setTransformationType(Transformation.TYPE_MATRIX);

        mCamera.save();

        // Translate the item to it's coordinates
        final Matrix matrix = transformation.getMatrix();

        int childIndex = childOrder.get(child);

        float angleOffset = ((360f / getChildCount()) * childIndex) * (float) (Math.PI / 180.0f) + rotation;
        float angleZ = getTiltAngleZ() * (float) (Math.PI / 180.0f);

        float diameter = getWidth();

        float x = -(diameter / 2) * (android.util.FloatMath.sin(angleOffset) + diameter / 2 - child.getWidth() / 2) * getStretchX();
        float z = diameter / 2 * (1.0f - android.util.FloatMath.cos(angleOffset)) * getStretchZ();
        float y = z * android.util.FloatMath.sin(angleZ);

        float radians = (float) (getTiltAngleY() * Math.PI / 180.0f);
        y = x * FloatMath.sin(radians) + y * FloatMath.sin(radians);

        mCamera.translate(x, y, z);

        ((CarouselItem) child).setZ(z);
        ((CarouselItem) child).setMaxZ(diameter * getStretchZ());

        mCamera.getMatrix(matrix);

        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);

        mCamera.restore();

        return true;
    }

    @Override
    public void addView(View child) {
        super.addView(child);

        childOrder.put(child, getChildCount());

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean retval = detector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP && scroller.isFinished())
            animator.end();
        else if (event.getAction() == MotionEvent.ACTION_DOWN)
            animator.start();
        /*
        stopped = !stopped;
        if (stopped) {
            animator.end();
            requestLayout();
        } else {
            animator.start();
        }
        return super.onTouchEvent(event);
        */

        return retval;
    }

}
