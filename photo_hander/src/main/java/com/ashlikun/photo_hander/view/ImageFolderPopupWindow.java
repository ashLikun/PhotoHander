package com.ashlikun.photo_hander.view;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
    View backgoundView;

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
        handleBackgroundAlpha(false);
    }


    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        handleBackgroundAlpha(true);
    }

    /**
     * 设置屏幕的亮度模式
     */
    private void setLightMode(boolean isOpen) {
        WindowManager.LayoutParams layoutParams = mActivity.getWindow().getAttributes();
        if (isOpen) {
            layoutParams.alpha = 0.3f;
        } else {
            layoutParams.alpha = 1f;
        }
        mActivity.getWindow().setAttributes(layoutParams);
    }

    /**
     * 处理背景变暗
     *
     * @param isShow 是否是弹出时候调用的
     */
    private void handleBackgroundAlpha(boolean isShow) {
        Activity activity = mActivity;
        if (activity != null) {
            View decorView = activity.getWindow().getDecorView();
            FrameLayout rootView = (FrameLayout) decorView.findViewById(android.R.id.content);
            if (isShow) {
                backgoundView = new View(activity);
                //设置宽高为全屏
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                backgoundView.setLayoutParams(layoutParams);
                //设置背景颜色为黑色，加上透明度，就会有半透明的黑色蒙版效果
                backgoundView.setBackgroundColor(0xff000000);
                //1.0f 不透明/0.0f 透明
                backgoundView.setAlpha(0.3f);
                rootView.addView(backgoundView);
            } else {
                rootView.removeView(backgoundView);
            }
//                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
//                if (isShow && mWindowAlphaOrgin == DEFAULT_WINDOW_ALPHA_ORGIN) {
//                    mWindowAlphaOrgin = lp.alpha;
//                }
////                lp.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//                if (isShow) {
//                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                } else {
//                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                }
//                lp.alpha = isShow ? mBackgroundAlpha : mWindowAlphaOrgin;
//                activity.getWindow().setAttributes(lp);
        }
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
