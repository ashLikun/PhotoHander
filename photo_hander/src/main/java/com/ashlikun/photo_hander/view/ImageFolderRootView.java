package com.ashlikun.photo_hander.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

/**
 * @author　　: 李坤
 * 创建时间: 2020/12/4 15:22
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：图片文件夹列表窗口
 */

public class ImageFolderRootView  extends FrameLayout {
    /**
     * 优先级低
     * 是屏幕高度的多少
     */
    public float mMaxRatio = 0.6f;
    /**
     * 优先级高
     * 最大高度
     */
    public float mMaxHeight = 0;

    public ImageFolderRootView(@NonNull Context context) {
        this(context, null);
    }

    public ImageFolderRootView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageFolderRootView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        init();
    }


    private void init() {
        if (mMaxHeight <= 0) {
            mMaxHeight = mMaxRatio * (float) getScreenHeight(getContext());
        } else if (mMaxRatio > 0) {
            mMaxHeight = Math.min(mMaxHeight, mMaxRatio * (float) getScreenHeight(getContext()));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //如果没有设置高度
        if (mMaxHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = heightSize <= mMaxHeight ? heightSize
                    : (int) mMaxHeight;
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = heightSize <= mMaxHeight ? heightSize
                    : (int) mMaxHeight;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = heightSize <= mMaxHeight ? heightSize
                    : (int) mMaxHeight;
        }
        int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,
                heightMode);
        super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec);
    }

    public void setMaxRatio(float mMaxRatio) {
        this.mMaxRatio = mMaxRatio;
        init();
        requestLayout();
    }

    public void setMaxHeight(float mMaxHeight) {
        this.mMaxHeight = mMaxHeight;
        init();
        requestLayout();
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     */
    private int getScreenHeight(Context context) {
        return PhotoHanderUtils.getScreenSize(context).y;
    }
}
