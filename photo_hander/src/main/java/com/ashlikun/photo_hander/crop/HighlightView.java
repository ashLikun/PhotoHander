package com.ashlikun.photo_hander.crop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import com.ashlikun.photo_hander.R;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 16:16
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：裁剪边框的一些view抽离的代码
 */

class HighlightView {

    public static final int GROW_NONE = (1 << 0);
    public static final int GROW_LEFT_EDGE = (1 << 1);
    public static final int GROW_RIGHT_EDGE = (1 << 2);
    public static final int GROW_TOP_EDGE = (1 << 3);
    public static final int GROW_BOTTOM_EDGE = (1 << 4);
    public static final int MOVE = (1 << 5);

    private static final int DEFAULT_HIGHLIGHT_COLOR = 0xFF33B5E5;
    private static final float HANDLE_RADIUS_DP = 12f;
    private static final float OUTLINE_DP = 2f;

    enum ModifyMode {None, Move, Grow}

    enum HandleMode {Changing, Always, Never}

    /**
     * 裁剪大小
     */
    RectF cropRect;
    /**
     * 绘制的区域大小
     */
    Rect drawRect;
    Matrix matrix;
    /**
     * 图像大小
     */
    private RectF imageRect;

    private final Paint outsidePaint = new Paint();
    private final Paint outlinePaint = new Paint();
    private final Paint handlePaint = new Paint();

    private View viewContext;
    private boolean showThirds;
    private boolean showCircle;
    private int highlightColor;

    private ModifyMode modifyMode = ModifyMode.None;
    private HandleMode handleMode = HandleMode.Changing;
    private boolean maintainAspectRatio;
    private float initialAspectRatio;
    private float handleRadius;
    private float outlineWidth;
    private boolean isFocused;

    public HighlightView(View context) {
        viewContext = context;
        initStyles(context.getContext());
    }

    private void initStyles(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.cropImageStyle, outValue, true);
        TypedArray attributes = context.obtainStyledAttributes(outValue.resourceId, R.styleable.CropImageView);
        try {
            showThirds = attributes.getBoolean(R.styleable.CropImageView_showThirds, false);
            highlightColor = attributes.getColor(R.styleable.CropImageView_highlightColor,
                    DEFAULT_HIGHLIGHT_COLOR);
            handleMode = HandleMode.values()[attributes.getInt(R.styleable.CropImageView_showHandles, 0)];
        } finally {
            attributes.recycle();
        }
    }

    public void setShowCircle(boolean showCircle) {
        this.showCircle = showCircle;
    }

    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void setup(Matrix m, Rect imageRect, RectF cropRect, boolean maintainAspectRatio) {
        matrix = new Matrix(m);

        this.cropRect = cropRect;
        this.imageRect = new RectF(imageRect);
        this.maintainAspectRatio = maintainAspectRatio;

        initialAspectRatio = this.cropRect.width() / this.cropRect.height();
        drawRect = computeLayout();

        outsidePaint.setARGB(125, 50, 50, 50);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setAntiAlias(true);
        outlineWidth = dpToPx(OUTLINE_DP);

        handlePaint.setColor(highlightColor);
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setAntiAlias(true);
        handleRadius = dpToPx(HANDLE_RADIUS_DP);

        modifyMode = ModifyMode.None;
    }

    private float dpToPx(float dp) {
        return dp * viewContext.getResources().getDisplayMetrics().density;
    }

    protected void draw(Canvas canvas) {
        canvas.save();
        Path path = new Path();
        outlinePaint.setStrokeWidth(outlineWidth);
        if (!hasFocus()) {
            outlinePaint.setColor(Color.BLACK);
            canvas.drawRect(drawRect, outlinePaint);
        } else {
            Rect viewDrawingRect = new Rect();
            viewContext.getDrawingRect(viewDrawingRect);

            path.addRect(new RectF(drawRect), Path.Direction.CW);
            outlinePaint.setColor(highlightColor);

            if (isClipPathSupported(canvas)) {
                canvas.clipPath(path, Region.Op.DIFFERENCE);
                canvas.drawRect(viewDrawingRect, outsidePaint);
            } else {
                drawOutsideFallback(canvas);
            }

            canvas.restore();
            canvas.drawPath(path, outlinePaint);

            if (showThirds) {
                drawThirds(canvas);
            }

            if (showCircle) {
                drawCircle(canvas);
            }

            if (handleMode == HandleMode.Always ||
                    (handleMode == HandleMode.Changing && modifyMode == ModifyMode.Grow)) {
                drawHandles(canvas);
            }
        }
    }

    /**
     * 退回到幼稚的方法来让作物区域变暗
     */
    private void drawOutsideFallback(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), drawRect.top, outsidePaint);
        canvas.drawRect(0, drawRect.bottom, canvas.getWidth(), canvas.getHeight(), outsidePaint);
        canvas.drawRect(0, drawRect.top, drawRect.left, drawRect.bottom, outsidePaint);
        canvas.drawRect(drawRect.right, drawRect.top, canvas.getWidth(), drawRect.bottom, outsidePaint);
    }

    /**
     * 与硬件加速有关
     */
    @SuppressLint("NewApi")
    private boolean isClipPathSupported(Canvas canvas) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        } else if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                || Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return true;
        } else {
            return !canvas.isHardwareAccelerated();
        }
    }

    private void drawHandles(Canvas canvas) {
        int xMiddle = drawRect.left + ((drawRect.right - drawRect.left) / 2);
        int yMiddle = drawRect.top + ((drawRect.bottom - drawRect.top) / 2);

        canvas.drawCircle(drawRect.left, yMiddle, handleRadius, handlePaint);
        canvas.drawCircle(xMiddle, drawRect.top, handleRadius, handlePaint);
        canvas.drawCircle(drawRect.right, yMiddle, handleRadius, handlePaint);
        canvas.drawCircle(xMiddle, drawRect.bottom, handleRadius, handlePaint);
    }

    private void drawThirds(Canvas canvas) {
        outlinePaint.setStrokeWidth(1);
        float xThird = (drawRect.right - drawRect.left) / 3;
        float yThird = (drawRect.bottom - drawRect.top) / 3;

        canvas.drawLine(drawRect.left + xThird, drawRect.top,
                drawRect.left + xThird, drawRect.bottom, outlinePaint);
        canvas.drawLine(drawRect.left + xThird * 2, drawRect.top,
                drawRect.left + xThird * 2, drawRect.bottom, outlinePaint);
        canvas.drawLine(drawRect.left, drawRect.top + yThird,
                drawRect.right, drawRect.top + yThird, outlinePaint);
        canvas.drawLine(drawRect.left, drawRect.top + yThird * 2,
                drawRect.right, drawRect.top + yThird * 2, outlinePaint);
    }

    private void drawCircle(Canvas canvas) {
        outlinePaint.setStrokeWidth(outlineWidth / 2.0f);
        canvas.drawOval(new RectF(drawRect), outlinePaint);
    }

    public void setMode(ModifyMode mode) {
        if (mode != modifyMode) {
            modifyMode = mode;
            viewContext.invalidate();
        }
    }

    /**
     * 这个触摸点是否在边框上
     *
     * @param x
     * @param y
     * @return
     */
    public int getHit(float x, float y) {
        Rect r = computeLayout();
        final float hysteresis = 20F;
        int retval = GROW_NONE;

        // verticalCheck确保位置位于顶部和底部之间（具有一定的公差）。horizCheck相似。
        boolean verticalCheck = (y >= r.top - hysteresis)
                && (y < r.bottom + hysteresis);
        boolean horizCheck = (x >= r.left - hysteresis)
                && (x < r.right + hysteresis);

        // 检查这个位置是否接近某个边缘（s）
        if ((Math.abs(r.left - x) < hysteresis) && verticalCheck) {
            retval |= GROW_LEFT_EDGE;
        }
        if ((Math.abs(r.right - x) < hysteresis) && verticalCheck) {
            retval |= GROW_RIGHT_EDGE;
        }
        if ((Math.abs(r.top - y) < hysteresis) && horizCheck) {
            retval |= GROW_TOP_EDGE;
        }
        if ((Math.abs(r.bottom - y) < hysteresis) && horizCheck) {
            retval |= GROW_BOTTOM_EDGE;
        }

        // 不是在任何边缘，而是在矩形内部：移动
        if (retval == GROW_NONE && r.contains((int) x, (int) y)) {
            retval = MOVE;
        }
        return retval;
    }

    /**
     * 在屏幕空间中处理动作（dx，dy）。“edge”参数指定用户拖动的边。
     *
     * @param edge
     * @param dx
     * @param dy
     */
    void handleMotion(int edge, float dx, float dy) {
        Rect r = computeLayout();
        if (edge == MOVE) {
            // 在发送到moveBy（）之前转换成图像空间
            moveBy(dx * (cropRect.width() / r.width()),
                    dy * (cropRect.height() / r.height()));
        } else {
            if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & edge) == 0) {
                dx = 0;
            }

            if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & edge) == 0) {
                dy = 0;
            }

            // 在发送到growBy（）之前，转换成图像空间
            float xDelta = dx * (cropRect.width() / r.width());
            float yDelta = dy * (cropRect.height() / r.height());
            growBy((((edge & GROW_LEFT_EDGE) != 0) ? -1 : 1) * xDelta,
                    (((edge & GROW_TOP_EDGE) != 0) ? -1 : 1) * yDelta);
        }
    }

    /**
     * 在图像空间中增加（dx，dy）的裁剪矩形
     *
     * @param dx
     * @param dy
     */
    void moveBy(float dx, float dy) {
        Rect invalRect = new Rect(drawRect);

        cropRect.offset(dx, dy);

        // 将裁剪矩形放入图像矩形中
        cropRect.offset(
                Math.max(0, imageRect.left - cropRect.left),
                Math.max(0, imageRect.top - cropRect.top));

        cropRect.offset(
                Math.min(0, imageRect.right - cropRect.right),
                Math.min(0, imageRect.bottom - cropRect.bottom));

        drawRect = computeLayout();
        invalRect.union(drawRect);
        invalRect.inset(-(int) handleRadius, -(int) handleRadius);
        viewContext.invalidate(invalRect);
    }

    /**
     * 在图像空间中增加（dx，dy）的裁剪矩形。
     *
     * @param dx
     * @param dy
     */
    void growBy(float dx, float dy) {
        if (maintainAspectRatio) {
            if (dx != 0) {
                dy = dx / initialAspectRatio;
            } else if (dy != 0) {
                dx = dy * initialAspectRatio;
            }
        }

        // 不要让种植的长方形长得太快。
        //在图像矩形和图像矩形之间的差异的一半
        //裁剪矩形。
        RectF r = new RectF(cropRect);
        if (dx > 0F && r.width() + 2 * dx > imageRect.width()) {
            dx = (imageRect.width() - r.width()) / 2F;
            if (maintainAspectRatio) {
                dy = dx / initialAspectRatio;
            }
        }
        if (dy > 0F && r.height() + 2 * dy > imageRect.height()) {
            dy = (imageRect.height() - r.height()) / 2F;
            if (maintainAspectRatio) {
                dx = dy * initialAspectRatio;
            }
        }

        r.inset(-dx, -dy);

        // 不要让裁剪的矩形收缩得太快
        final float widthCap = 25F;
        if (r.width() < widthCap) {
            r.inset(-(widthCap - r.width()) / 2F, 0F);
        }
        float heightCap = maintainAspectRatio
                ? (widthCap / initialAspectRatio)
                : widthCap;
        if (r.height() < heightCap) {
            r.inset(0F, -(heightCap - r.height()) / 2F);
        }

        //把裁剪的矩形放在图像矩形中
        if (r.left < imageRect.left) {
            r.offset(imageRect.left - r.left, 0F);
        } else if (r.right > imageRect.right) {
            r.offset(-(r.right - imageRect.right), 0F);
        }
        if (r.top < imageRect.top) {
            r.offset(0F, imageRect.top - r.top);
        } else if (r.bottom > imageRect.bottom) {
            r.offset(0F, -(r.bottom - imageRect.bottom));
        }

        cropRect.set(r);
        drawRect = computeLayout();
        viewContext.invalidate();
    }

    /**
     * 在图像空间中以指定的比例返回裁剪矩形
     *
     * @param scale
     * @return
     */
    public Rect getScaledCropRect(float scale) {
        return new Rect((int) (cropRect.left * scale), (int) (cropRect.top * scale),
                (int) (cropRect.right * scale), (int) (cropRect.bottom * scale));
    }

    /**
     * 将裁剪矩形从图像空间映射到屏幕空间
     *
     * @return
     */
    private Rect computeLayout() {
        RectF r = new RectF(cropRect.left, cropRect.top,
                cropRect.right, cropRect.bottom);
        matrix.mapRect(r);
        return new Rect(Math.round(r.left), Math.round(r.top),
                Math.round(r.right), Math.round(r.bottom));
    }

    public void invalidate() {
        drawRect = computeLayout();
    }

    public boolean hasFocus() {
        return isFocused;
    }

    public void setFocus(boolean isFocused) {
        this.isFocused = isFocused;
    }

}
