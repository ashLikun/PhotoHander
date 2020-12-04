package com.ashlikun.photo_hander.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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


    private Context mContext;

    private RecyclerView mRecyclerView;
    private FolderAdapter mFolderAdapter;
    /**
     * 数据来源
     */
    private MediaHandler mediaHandler;
    public ImageFolderPopupWindow(Context context,MediaHandler mediaHandler) {
        this.mContext = context;
        this.mediaHandler = mediaHandler;
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ph_window_image_folders, null);
        mRecyclerView = view.findViewById(R.id.rv_main_imageFolders);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFolderAdapter = new FolderAdapter(mContext,mediaHandler);
        mRecyclerView.setAdapter(mFolderAdapter);
        initPopupWindow(view);
    }

    /**
     * 初始化PopupWindow的一些属性
     */
    private void initPopupWindow(View view) {
        setContentView(view);
        Point screenSize = PhotoHanderUtils.getScreenSize(mContext);
        setWidth(screenSize.x);
        setHeight((int) (screenSize.y * 0.6));
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
