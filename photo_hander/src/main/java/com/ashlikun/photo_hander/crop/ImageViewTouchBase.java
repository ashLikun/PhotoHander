package com.ashlikun.photo_hander.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ImageView;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 16:16
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：裁剪view父类
 */

abstract class ImageViewTouchBase extends ImageView {

    private static final float SCALE_RATE = 1.25F;

    /**
     * 这是基本的转换，用于显示最初的图像。当前的计算显示了完整的图像，需要的字母。
     * 你可以选择将图片显示为裁剪。当我们从缩略图到全尺寸图像时，这个矩阵被重新计算。
     */
    protected Matrix baseMatrix = new Matrix();

    /**
     * 这是一个补充变换反映了什么
     * 用户已经完成了缩放和平移操作。
     * 当我们从缩略图图像开始时这个矩阵仍然是一样的
     * 全尺寸的图像。
     */
    protected Matrix suppMatrix = new Matrix();

    /**
     * 这是最终的矩阵它被计算为共结
     * 基本矩阵和补充矩阵的基础。
     */
    private final Matrix displayMatrix = new Matrix();

    /**
     * 用于从矩阵中获取值的临时缓冲区。
     */
    private final float[] matrixValues = new float[9];

    /**
     * 当前位图显示。
     */
    protected final RotateBitmap bitmapDisplayed = new RotateBitmap(null, 0);

    int thisWidth = -1;
    int thisHeight = -1;

    float maxZoom;

    private Runnable onLayoutRunnable;

    protected Handler handler = new Handler();

    /**
     * ImageViewTouchBase会将位图传递给回收者，如果它已经完成了
     * 它对位图的使用
     */
    public interface Recycler {
        public void recycle(Bitmap b);
    }

    private Recycler recycler;

    public ImageViewTouchBase(Context context) {
        super(context);
        init();
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewTouchBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setRecycler(Recycler recycler) {
        this.recycler = recycler;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        thisWidth = right - left;
        thisHeight = bottom - top;
        Runnable r = onLayoutRunnable;
        if (r != null) {
            onLayoutRunnable = null;
            r.run();
        }
        if (bitmapDisplayed.getBitmap() != null) {
            getProperBaseMatrix(bitmapDisplayed, baseMatrix, true);
            setImageMatrix(getImageViewMatrix());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
            if (getScale() > 1.0f) {
                // 如果我们放大，按下跳来显示整个图像，否则返回给用户
                zoomTo(1.0f);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap, 0);
    }

    private void setImageBitmap(Bitmap bitmap, int rotation) {
        super.setImageBitmap(bitmap);
        Drawable d = getDrawable();
        if (d != null) {
            d.setDither(true);
        }

        Bitmap old = bitmapDisplayed.getBitmap();
        bitmapDisplayed.setBitmap(bitmap);
        bitmapDisplayed.setRotation(rotation);

        if (old != null && old != bitmap && recycler != null) {
            recycler.recycle(old);
        }
    }

    public void clear() {
        setImageBitmapResetBase(null, true);
    }


    /**
     * 这个函数改变位图，根据大小重置基本矩阵
     * 位图，并可选地重置补充矩阵
     *
     * @param bitmap
     * @param resetSupp
     */
    public void setImageBitmapResetBase(final Bitmap bitmap, final boolean resetSupp) {
        setImageRotateBitmapResetBase(new RotateBitmap(bitmap, 0), resetSupp);
    }

    public void setImageRotateBitmapResetBase(final RotateBitmap bitmap, final boolean resetSupp) {
        final int viewWidth = getWidth();

        if (viewWidth <= 0) {
            onLayoutRunnable = new Runnable() {
                @Override
                public void run() {
                    setImageRotateBitmapResetBase(bitmap, resetSupp);
                }
            };
            return;
        }

        if (bitmap.getBitmap() != null) {
            getProperBaseMatrix(bitmap, baseMatrix, true);
            setImageBitmap(bitmap.getBitmap(), bitmap.getRotation());
        } else {
            baseMatrix.reset();
            setImageBitmap(null);
        }

        if (resetSupp) {
            suppMatrix.reset();
        }
        setImageMatrix(getImageViewMatrix());
        maxZoom = calculateMaxZoom();
    }

    /**
     * 在一个或两个轴上尽可能多的中心。如果图像在视图的维度下缩放，那么中心就会被定义为如下所示。
     * 如果图像比视图放大，并被翻译成视图，然后将其转换回视图。
     */
    protected void center() {
        final Bitmap bitmap = bitmapDisplayed.getBitmap();
        if (bitmap == null) {
            return;
        }
        Matrix m = getImageViewMatrix();

        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        m.mapRect(rect);

        float height = rect.height();
        float width = rect.width();

        float deltaX = 0, deltaY = 0;

        deltaY = centerVertical(rect, height, deltaY);
        deltaX = centerHorizontal(rect, width, deltaX);

        postTranslate(deltaX, deltaY);
        setImageMatrix(getImageViewMatrix());
    }

    private float centerVertical(RectF rect, float height, float deltaY) {
        int viewHeight = getHeight();
        if (height < viewHeight) {
            deltaY = (viewHeight - height) / 2 - rect.top;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = getHeight() - rect.bottom;
        }
        return deltaY;
    }

    private float centerHorizontal(RectF rect, float width, float deltaX) {
        int viewWidth = getWidth();
        if (width < viewWidth) {
            deltaX = (viewWidth - width) / 2 - rect.left;
        } else if (rect.left > 0) {
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
        }
        return deltaX;
    }

    private void init() {
        setScaleType(ImageView.ScaleType.MATRIX);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(matrixValues);
        return matrixValues[whichValue];
    }

    /**
     * 从矩阵中得到比例因子。
     *
     * @param matrix
     * @return
     */
    protected float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    protected float getScale() {
        return getScale(suppMatrix);
    }

    /**
     * 设置基本矩阵，使图像居中并适当缩放。
     *
     * @param bitmap
     * @param matrix
     * @param includeRotation
     */
    private void getProperBaseMatrix(RotateBitmap bitmap, Matrix matrix, boolean includeRotation) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();
        matrix.reset();

        /**
         * 我们将缩放比例限制为3x否则结果可能看起来很糟糕如果它是一个小图标
         */
        float widthScale = Math.min(viewWidth / w, 3.0f);
        float heightScale = Math.min(viewHeight / h, 3.0f);
        float scale = Math.min(widthScale, heightScale);

        if (includeRotation) {
            matrix.postConcat(bitmap.getRotateMatrix());
        }
        matrix.postScale(scale, scale);
        matrix.postTranslate((viewWidth - w * scale) / 2F, (viewHeight - h * scale) / 2F);
    }

    /**
     * 将基本矩阵和supp矩阵组合成最终的矩阵
     *
     * @return
     */
    protected Matrix getImageViewMatrix() {
        //最后的矩阵是根据基矩阵的表示来计算的
        //和补充矩阵
        displayMatrix.set(baseMatrix);
        displayMatrix.postConcat(suppMatrix);
        return displayMatrix;
    }

    public Matrix getUnrotatedMatrix() {
        Matrix unrotated = new Matrix();
        getProperBaseMatrix(bitmapDisplayed, unrotated, false);
        unrotated.postConcat(suppMatrix);
        return unrotated;
    }

    protected float calculateMaxZoom() {
        if (bitmapDisplayed.getBitmap() == null) {
            return 1F;
        }

        float fw = (float) bitmapDisplayed.getWidth() / (float) thisWidth;
        float fh = (float) bitmapDisplayed.getHeight() / (float) thisHeight;
        // 400%
        return Math.max(fw, fh) * 4;
    }

    protected void zoomTo(float scale, float centerX, float centerY) {
        if (scale > maxZoom) {
            scale = maxZoom;
        }

        float oldScale = getScale();
        float deltaScale = scale / oldScale;

        suppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
        center();
    }

    protected void zoomTo(final float scale, final float centerX,
                          final float centerY, final float durationMs) {
        final float incrementPerMs = (scale - getScale()) / durationMs;
        final float oldScale = getScale();
        final long startTime = System.currentTimeMillis();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                float currentMs = Math.min(durationMs, now - startTime);
                float target = oldScale + (incrementPerMs * currentMs);
                zoomTo(target, centerX, centerY);

                if (currentMs < durationMs) {
                    handler.post(this);
                }
            }
        });
    }

    protected void zoomTo(float scale) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;
        zoomTo(scale, cx, cy);
    }

    protected void zoomIn() {
        zoomIn(SCALE_RATE);
    }

    protected void zoomOut() {
        zoomOut(SCALE_RATE);
    }

    protected void zoomIn(float rate) {
        if (getScale() >= maxZoom) {
            // 不要让用户放大到分子水平
            return;
        }
        if (bitmapDisplayed.getBitmap() == null) {
            return;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        suppMatrix.postScale(rate, rate, cx, cy);
        setImageMatrix(getImageViewMatrix());
    }

    protected void zoomOut(float rate) {
        if (bitmapDisplayed.getBitmap() == null) {
            return;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        // 放大到最多1倍
        Matrix tmp = new Matrix(suppMatrix);
        tmp.postScale(1F / rate, 1F / rate, cx, cy);

        if (getScale(tmp) < 1F) {
            suppMatrix.setScale(1F, 1F, cx, cy);
        } else {
            suppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
        }
        setImageMatrix(getImageViewMatrix());
        center();
    }

    protected void postTranslate(float dx, float dy) {
        suppMatrix.postTranslate(dx, dy);
    }

    protected void panBy(float dx, float dy) {
        postTranslate(dx, dy);
        setImageMatrix(getImageViewMatrix());
    }
}
