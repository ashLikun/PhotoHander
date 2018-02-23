package com.ashlikun.photo_hander;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.ashlikun.photo_hander.compress.Luban;
import com.ashlikun.photo_hander.crop.Crop;

import java.util.ArrayList;

/**
 * 作者　　: 李坤
 * 创建时间: 16:28 Administrator
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：图片选择器
 */

public class PhotoHander {
    private Intent intent;
    public static final String EXTRA_RESULT = PhotoHanderActivity.EXTRA_RESULT;
    //未压缩的图片与原图的对应关系
    private SparseArray<String> mRelationMap = new SparseArray();
    private ArrayList<String> mOriginData;
    private static PhotoHander sSelector;


    private PhotoHander() {
    }

    public SparseArray<String> getmRelationMap() {
        return mRelationMap;
    }

    public static PhotoHander create() {
        if (sSelector == null) {
            sSelector = new PhotoHander();
        }
        sSelector.intent = new Intent();
        return sSelector;
    }

    //是否显示摄像头
    public PhotoHander showCamera(boolean mShowCamera) {
        intent.putExtra(PhotoHanderActivity.EXTRA_SHOW_CAMERA, mShowCamera);
        return sSelector;
    }

    //最大多少张
    public PhotoHander count(int count) {
        intent.putExtra(PhotoHanderActivity.EXTRA_SELECT_COUNT, count);
        return sSelector;
    }

    //单选
    public PhotoHander single() {
        intent.putExtra(PhotoHanderActivity.EXTRA_SELECT_MODE, PhotoHanderActivity.MODE_SINGLE);
        return sSelector;
    }

    //多选
    public PhotoHander multi() {
        intent.putExtra(PhotoHanderActivity.EXTRA_SELECT_MODE, PhotoHanderActivity.MODE_MULTI);
        return sSelector;
    }

    //已选
    public PhotoHander origin(ArrayList<String> images) {
        mOriginData = images;
        return sSelector;
    }

    //压缩
    public PhotoHander compress(boolean isCompress) {
        intent.putExtra(PhotoHanderActivity.EXTRA_IS_COMPRESS, isCompress);
        return sSelector;
    }

    //压缩等级  高
    public PhotoHander compressRankThird() {
        intent.putExtra(PhotoHanderActivity.EXTRA_COMPRESS_RANK, Luban.THIRD_GEAR);
        return sSelector;
    }

    //压缩等级 低
    public PhotoHander compressRankFirst() {
        intent.putExtra(PhotoHanderActivity.EXTRA_COMPRESS_RANK, Luban.FIRST_GEAR);
        return sSelector;
    }

    //裁剪
    public PhotoHander crop(boolean isCrop) {
        intent.putExtra(PhotoHanderActivity.EXTRA_IS_CROP, isCrop);
        return sSelector;
    }

    //裁剪
    public PhotoHander crop(int cropWidth, int cropHeight) {
        crop(true);
        intent.putExtra(PhotoHanderActivity.EXTRA_CROP_WIDTH, cropWidth);
        intent.putExtra(PhotoHanderActivity.EXTRA_CROP_HEIGHT, cropHeight);
        return sSelector;
    }

    //裁剪圆形
    public PhotoHander cropCircle(boolean showCircle) {
        intent.putExtra(Crop.Extra.CROP_CIRCLE, showCircle);
        return sSelector;
    }

    //裁剪框颜色
    public PhotoHander color(int color) {
        intent.putExtra(Crop.Extra.COLOR, color);
        return sSelector;
    }


    //开启
    public void start(Activity activity, int requestCode) {
        final Context context = activity;
        activity.startActivityForResult(createIntent(context), requestCode);
        intent = null;
    }

    public void start(Fragment fragment, int requestCode) {
        final Context context = fragment.getContext();
        fragment.startActivityForResult(createIntent(context), requestCode);
        intent = null;
    }

    private Intent createIntent(Context context) {
        intent.setClass(context, PhotoHanderActivity.class);
        if (mOriginData != null) {
            intent.putStringArrayListExtra(PhotoHanderActivity.EXTRA_DEFAULT_SELECTED_LIST, mOriginData);
        }

        return intent;
    }

    //获取照片选择后的地址
    public static ArrayList<String> getIntentResult(Intent data) {
        return data.getStringArrayListExtra(PhotoHander.EXTRA_RESULT);
    }

}