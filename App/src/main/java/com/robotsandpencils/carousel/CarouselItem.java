package com.robotsandpencils.carousel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by neal on 12/14/2013.
 */
public class CarouselItem extends FrameLayout {

    private float z;
    private ImageView mImage;
    private ImageView mBlurImage;
    private float scale = 1.0f;
    private float maxZ = 100;
    private BlurMode blurMode = BlurMode.TO_BACK;
    private float imageRotation;

    public CarouselItem(Context context) {
        super(context);
    }

    public CarouselItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CarouselItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addImage(ImageView image) {
        ViewHelper.setScaleX(image, getScale());
        ViewHelper.setScaleY(image, getScale());
        ViewHelper.setRotation(image, getImageRotation());
        mImage = image;

        image.setDrawingCacheEnabled(true);

        mImage.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        mImage.layout(0, 0, mImage.getMeasuredWidth(), mImage.getMeasuredHeight());

        mImage.buildDrawingCache();

        mBlurImage = new ImageView(getContext());

        Bitmap b = mImage.getDrawingCache();
        mImage.setImageBitmap(b);

        // Calculate the blurred version of the bitmap too
        mBlurImage.setImageBitmap(Blur.fastblur(getContext(), b, 25));

        mBlurImage.setDrawingCacheEnabled(true);

        ViewHelper.setRotation(mBlurImage, getImageRotation());

        addView(mImage);
        addView(mBlurImage);
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        float fadeFactor = 0.5f;
        float maxZ = getMaxZ();

        float blurAlpha = 0.0f;
        float imageAlpha = 1.0f;

        if (getBlurMode() == BlurMode.TO_BACK) {
            blurAlpha = (maxZ - (maxZ - getZ())) / (maxZ / fadeFactor);
            imageAlpha = (maxZ - getZ()) / maxZ;
        } else if (getBlurMode() == BlurMode.FLAT) {

            float halfZ = maxZ / 2.0f;
            float eighthZ = maxZ / 8.0f;

            if (getZ() > halfZ) {
                // Hidden in the back half of the swing
                blurAlpha = 0.0f;
                imageAlpha = 0.0f;
            } else {
                // Blur is a gaussian centered at eighthZ with an amplitude of 1
                float width = eighthZ;
                float x = getZ();
                float max = eighthZ;
                blurAlpha = (float) Math.exp(-1 * ((x - max) * (x - max)) / (2 * (width * width)));

                // Subtract a gaussian situated at 0 to fade the blur out
                float fade = (float) Math.exp(-1 * (x * x) / (2 * (width / 8) * (width / 8)));
                blurAlpha = Math.max(0, blurAlpha - fade);

                imageAlpha = (eighthZ - getZ()) / eighthZ;
            }
        }

        ViewHelper.setAlpha(mImage, imageAlpha);
        ViewHelper.setAlpha(mBlurImage, blurAlpha);

        ViewHelper.setScaleX(mImage, getScale());
        ViewHelper.setScaleY(mImage, getScale());
        ViewHelper.setScaleX(mBlurImage, getScale());
        ViewHelper.setScaleY(mBlurImage, getScale());

        super.dispatchDraw(canvas);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(float maxZ) {
        this.maxZ = maxZ;
    }

    public BlurMode getBlurMode() {
        return blurMode;
    }

    public void setBlurMode(BlurMode blurMode) {
        this.blurMode = blurMode;
    }

    public float getImageRotation() {
        return imageRotation;
    }

    public void setImageRotation(float imageRotation) {
        this.imageRotation = imageRotation;
    }
}
