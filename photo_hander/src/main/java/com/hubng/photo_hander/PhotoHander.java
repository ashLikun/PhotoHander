package com.hubng.photo_hander;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.widget.Toast;

import com.hubng.photo_hander.compress.Luban;
import com.hubng.photo_hander.crop.Crop;

import java.util.ArrayList;

/**
 * 图片选择器
 * Created by nereo on 16/3/17.
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

    public PhotoHander showCamera(boolean mShowCamera) {
        intent.putExtra(PhotoHanderActivity.EXTRA_SHOW_CAMERA, mShowCamera);
        return sSelector;
    }

    public PhotoHander count(int count) {
        intent.putExtra(PhotoHanderActivity.EXTRA_SELECT_COUNT, count);
        return sSelector;
    }

    public PhotoHander single() {
        intent.putExtra(PhotoHanderActivity.EXTRA_SELECT_MODE, PhotoHanderActivity.MODE_SINGLE);
        return sSelector;
    }

    public PhotoHander multi() {
        intent.putExtra(PhotoHanderActivity.EXTRA_SELECT_MODE, PhotoHanderActivity.MODE_MULTI);
        return sSelector;
    }

    public PhotoHander origin(ArrayList<String> images) {
        mOriginData = images;
        return sSelector;
    }

    public PhotoHander compress(boolean isCompress) {
        intent.putExtra(PhotoHanderActivity.EXTRA_IS_COMPRESS, isCompress);
        return sSelector;
    }

    public PhotoHander compressRankThird() {
        intent.putExtra(PhotoHanderActivity.EXTRA_COMPRESS_RANK, Luban.THIRD_GEAR);
        return sSelector;
    }

    public PhotoHander compressRankFirst() {
        intent.putExtra(PhotoHanderActivity.EXTRA_COMPRESS_RANK, Luban.FIRST_GEAR);
        return sSelector;
    }

    public PhotoHander crop(boolean isCrop) {
        intent.putExtra(PhotoHanderActivity.EXTRA_IS_CROP, isCrop);
        return sSelector;
    }

    public PhotoHander crop(int cropWidth, int cropHeight) {
        crop(true);
        intent.putExtra(PhotoHanderActivity.EXTRA_CROP_WIDTH, cropWidth);
        intent.putExtra(PhotoHanderActivity.EXTRA_CROP_HEIGHT, cropHeight);
        return sSelector;
    }

    public PhotoHander color(int color) {
        intent.putExtra(Crop.Extra.COLOR, color);
        return sSelector;
    }

    public PhotoHander showCircle(boolean showCircle) {
        intent.putExtra(Crop.Extra.SHOW_CIRCLE, showCircle);
        return sSelector;
    }


    public void start(Activity activity, int requestCode) {
        final Context context = activity;
        if (hasPermission(context)) {
            activity.startActivityForResult(createIntent(context), requestCode);
        } else {
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
        intent = null;
    }

    public void start(Fragment fragment, int requestCode) {
        final Context context = fragment.getContext();
        if (hasPermission(context)) {
            fragment.startActivityForResult(createIntent(context), requestCode);
        } else {
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
        intent = null;
    }

    private boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Permission was added in API Level 16
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private Intent createIntent(Context context) {
        intent.setClass(context, PhotoHanderActivity.class);
        if (mOriginData != null) {
            intent.putStringArrayListExtra(PhotoHanderActivity.EXTRA_DEFAULT_SELECTED_LIST, mOriginData);
        }

        return intent;
    }



}
