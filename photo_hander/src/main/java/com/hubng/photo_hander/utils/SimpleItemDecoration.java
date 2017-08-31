package com.hubng.photo_hander.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.LinearLayout;

import com.hubng.photo_hander.R;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/8/31　9:42
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class SimpleItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;
    private Drawable mDivider;

    private int mOrientation;

    private final Rect mBounds = new Rect();


    private SimpleItemDecoration(Context context, int orientation, Drawable drawable) {
        mDivider = drawable;
        setOrientation(orientation);
    }

    public static class Builder {
        Context context;
        int orientation;
        int color;
        int size;
        Drawable drawable;

        public Builder(Context context, int orientation) {
            this.context = context;
            this.orientation = orientation;
            size = context.getResources().getDimensionPixelSize(R.dimen.mis_space_size);
            color = 0xffaaaaaa;
        }

        public Builder setColorRes(@ColorRes int colorRes) {
            color = context.getResources().getColor(colorRes);
            return this;
        }

        public Builder setColor(int color) {
            this.color = color;
            return this;
        }

        public Builder setSizeRes(@DimenRes int sizeRes) {
            size = context.getResources().getDimensionPixelOffset(sizeRes);
            return this;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public void setDrawableRes(@DrawableRes int drawableRes) {
            this.drawable = context.getResources().getDrawable(drawableRes);
        }

        public SimpleItemDecoration create() {
            if (drawable == null) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setSize(size, size);
                drawable.setColor(color);
                this.drawable = drawable;
            }
            return new SimpleItemDecoration(context, orientation, drawable);
        }

    }

    /**
     * Sets the orientation for this divider. This should be called if
     * {@link RecyclerView.LayoutManager} changes orientation.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
    }

    /**
     * Sets the {@link Drawable} for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    public void setDrawable(@NonNull Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable cannot be null.");
        }
        mDivider = drawable;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }

        int itemCount = parent.getAdapter().getItemCount();
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent, itemCount);
        } else {
            drawHorizontal(c, parent, itemCount);
        }
    }

    @SuppressLint("NewApi")
    private void drawVertical(Canvas canvas, RecyclerView parent, int itemCount) {
        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (isLastRaw(parent, position, getSpanCount(parent), itemCount))// 如果是最后一行，则不需要绘制底部
            {
                break;
            }

           // parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom + Math.round(ViewCompat.getTranslationY(child));
            final int top = bottom - mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    @SuppressLint("NewApi")
    private void drawHorizontal(Canvas canvas, RecyclerView parent, int itemCount) {

        canvas.save();
        final int top;
        final int bottom;
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            int spanCount = getSpanCount(parent);
            int spanIndex = getIndexColum(parent, position, spanCount, itemCount);//当前第几列
            if (spanIndex == spanCount) {
                return;
            }
          //  parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int right = mBounds.right + Math.round(ViewCompat.getTranslationX(child));
            final int left = right - mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();
        if (mOrientation == VERTICAL) {
            if (isLastRaw(parent, position, getSpanCount(parent), itemCount))// 如果是最后一行，则不需要绘制底部
            {
                outRect.set(0, 0, 0, 0);
            } else {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            }
        } else {
            int spanCount = getSpanCount(parent);
            int spanIndex = getIndexColum(parent, position, spanCount, itemCount);//当前第几列
            if (spanIndex == spanCount) {
                return;
            }
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }

    //当前是第几列
    protected int getIndexColum(RecyclerView parent, int pos, int spanCount,
                                int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int posSpan = (pos + 1) % spanCount;
        if (layoutManager instanceof GridLayoutManager) {

            return posSpan == 0 ? spanCount : posSpan;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                return posSpan == 0 ? spanCount : posSpan;
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                    return spanCount;
            }
        }
        return spanCount;
    }

    //一共多少列
    protected int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {

            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager)
                    .getSpanCount();
        }
        return spanCount;
    }

    //是否是最后一行
    protected boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                                int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            childCount = childCount - childCount % spanCount;
            if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                return true;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount)
                    return true;
            } else
            // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}