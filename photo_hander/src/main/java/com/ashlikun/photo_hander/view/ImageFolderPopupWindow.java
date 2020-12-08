package com.ashlikun.photo_hander.view;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.photo_hander.R;
import com.ashlikun.photo_hander.adapter.FolderAdapter;
import com.ashlikun.photo_hander.loader.MediaHandler;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

/**
 * @author　　: 李坤
 * 创建时间: 2020/12/4 15:22
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：图片文件夹列表窗口
 */

public class ImageFolderPopupWindow extends PopupWindow {


    private Activity mActivity;

    private RecyclerView mRecyclerView;
    private FolderAdapter mFolderAdapter;
    /**
     * 数据来源
     */
    private MediaHandler mediaHandler;

    public ImageFolderPopupWindow(Activity activity, MediaHandler mediaHandler) {
        this.mActivity = activity;
        this.mediaHandler = mediaHandler;
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.ph_window_image_folders, null);
        mRecyclerView = view.findViewById(R.id.rv_main_imageFolders);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mFolderAdapter = new FolderAdapter(mActivity, mediaHandler);
        mRecyclerView.setAdapter(mFolderAdapter);
        initPopupWindow(view);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        setLightMode(false);
    }


    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        setLightMode(true);
    }

    /**
     * 设置屏幕的亮度模式
     */
    private void setLightMode(boolean isOpen) {
        WindowManager.LayoutParams layoutParams = mActivity.getWindow().getAttributes();
        if (isOpen) {
            layoutParams.alpha = 0.7f;
        } else {
            layoutParams.alpha = 1f;
        }
        mActivity.getWindow().setAttributes(layoutParams);
    }

    /**
     * 初始化PopupWindow的一些属性
     */
    private void initPopupWindow(View view) {
        Point point = PhotoHanderUtils.getScreenSize(mActivity);
        setContentView(view);
        view.measure(View.MeasureSpec.makeMeasureSpec(point.x, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(point.y, View.MeasureSpec.EXACTLY));
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(view.getMeasuredHeight());
        setBackgroundDrawable(new ColorDrawable());
        setOutsideTouchable(true);
        setFocusable(true);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                }
                return false;
            }
        });
    }

    public FolderAdapter getAdapter() {
        return mFolderAdapter;
    }

}
