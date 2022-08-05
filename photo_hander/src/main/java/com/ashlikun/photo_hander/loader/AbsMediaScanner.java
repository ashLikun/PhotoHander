package com.ashlikun.photo_hander.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.ashlikun.photo_hander.PhotoOptionData;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author　　: 李坤
 * 创建时间: 2020/12/3 17:54
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：媒体库查询任务基类
 */

public abstract class AbsMediaScanner<T, F> implements Runnable {

    /**
     * 查询URI
     *
     * @return
     */
    protected abstract Uri getScanUri();

    /**
     * 查询列名
     *
     * @return
     */
    protected abstract String[] getProjection();

    /**
     * 查询条件
     *
     * @return
     */
    protected abstract String getSelection();

    /**
     * 查询条件值
     *
     * @return
     */
    protected abstract String[] getSelectionArgs();

    /**
     * 查询排序
     *
     * @return
     */
    protected abstract String getOrder();

    /**
     * 对外暴露游标，让开发者灵活构建对象
     *
     * @param cursor
     * @return
     */
    protected abstract T parse(Cursor cursor);

    protected abstract F parseFolder(T t);

    private Context mContext;
    public boolean hasFolderGened = false;
    protected ArrayList<T> mDatas = new ArrayList<>();
    /**
     * 可选目录的列表数据
     */
    protected ArrayList<F> mResultFolder = new ArrayList<>();

    protected OnLoadFinished onLoadFinished;


    public AbsMediaScanner(FragmentActivity context, OnLoadFinished onLoadFinished) {
        this.mContext = context;
        this.onLoadFinished = onLoadFinished;

    }

    public interface OnLoadFinished {
        void onLoadFinished(List<MediaFile> datas, List<MediaFolder> folderDatas);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void run() {
        if (PhotoOptionData.currentData != null) {
            queryMedia();
        }
    }


    /**
     * 根据查询条件进行媒体库查询，隐藏查询细节，让开发者更专注业务
     *
     * @return
     */
    public void queryMedia() {

        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor data = contentResolver.query(getScanUri(), getProjection(), getSelection(), getSelectionArgs(), getOrder());
        if (data != null && data.getCount() > 0) {
            mDatas.clear();
            while (data.moveToNext()) {
                T t = parse(data);
                if (t != null) {
                    mDatas.add(t);
                    F f = parseFolder(t);
                    if (f != null) {
                        mResultFolder.add(f);
                    }
                }
            }
            hasFolderGened = true;
        }
    }

}
