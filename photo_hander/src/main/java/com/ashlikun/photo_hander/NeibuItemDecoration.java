package com.ashlikun.photo_hander;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/8/31　9:42
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：分割线
 */

class NeibuItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;
    private Drawable mDivider;

    private int mOrientation;


    private NeibuItemDecoration(Context context, int orientation, Drawable drawable) {
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
            size = context.getResources().getDimensionPixelSize(R.dimen.ph_space_size);
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

        public NeibuItemDecoration create() {
            if (drawable == null) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setSize(size, size);
                drawable.setColor(color);
                this.drawable = drawable;
            }
            return new NeibuItemDecoration(context, orientation, drawable);
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
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getLeft() - params.leftMargin;
            final int right = child.getRight() + params.rightMargin
                    + mDivider.getIntrinsicWidth();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getTop() - params.topMargin;
            final int bottom = child.getBottom() + params.bottomMargin;
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicWidth();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();
        if (mOrientation == VERTICAL) {
            if (position >= itemCount - getLastDividerOffset(parent)) {
                // 如果是最后一行，则不需要绘制底部
                outRect.set(0, 0, 0, 0);
            } else {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            }
        } else {
            int spanCount = getSpanCount(parent);
            //当前第几列
            int spanIndex = getIndexColum(parent, position, spanCount, itemCount);
            // 如果是最后一列，则不需要绘制右边
            if (spanIndex == spanCount) {
                outRect.set(mDivider.getIntrinsicWidth() / 2, 0, 0, 0);
                return;
            }
            //第一列不绘制左边
            else if (spanIndex == 1) {
                outRect.set(0, 0, mDivider.getIntrinsicWidth() / 2, 0);
            } else {//中间的左右都绘制
                outRect.set(Math.round(mDivider.getIntrinsicWidth() / 2f), 0, Math.round(mDivider.getIntrinsicWidth() / 2f), 0);
            }
        }
    }

    /**
     * 当前是第几列
     *
     * @param parent
     * @param pos
     * @param spanCount
     * @param childCount
     * @return
     */
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
                // 如果是最后一列，则不需要绘制右边
                if (pos >= childCount) {
                    return spanCount;
                }
            }
        }
        return spanCount;
    }

    /**
     * 一共多少列
     *
     * @param parent
     * @return
     */
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

    private int getLastDividerOffset(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
            GridLayoutManager.SpanSizeLookup spanSizeLookup = layoutManager.getSpanSizeLookup();
            int spanCount = layoutManager.getSpanCount();
            int itemCount = parent.getAdapter().getItemCount();
            for (int i = itemCount - 1; i >= 0; i--) {
                if (spanSizeLookup.getSpanIndex(i, spanCount) == 0) {
                    return itemCount - i;
                }
            }
        }
        return 1;
    }
}