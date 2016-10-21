package com.hubng.photo_hander;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.hubng.photo_hander.compress.Luban;
import com.hubng.photo_hander.crop.Crop;

import java.util.ArrayList;

/**
 * 图片选择器
 * Created by nereo on 16/3/17.
 */
public class MultiImageSelector {
    private Intent intent;
    public static final String EXTRA_RESULT = MultiImageSelectorActivity.EXTRA_RESULT;

    private ArrayList<String> mOriginData;
    private static MultiImageSelector sSelector;


    private MultiImageSelector() {
    }


    public static MultiImageSelector create() {
        if (sSelector == null) {
            sSelector = new MultiImageSelector();
        }
        sSelector.intent = new Intent();
        return sSelector;
    }

    public MultiImageSelector showCamera(boolean mShowCamera) {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, mShowCamera);
        return sSelector;
    }

    public MultiImageSelector count(int count) {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, count);
        return sSelector;
    }

    public MultiImageSelector single() {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        return sSelector;
    }

    public MultiImageSelector multi() {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        return sSelector;
    }

    public MultiImageSelector origin(ArrayList<String> images) {
        mOriginData = images;
        return sSelector;
    }

    public MultiImageSelector compress(boolean isCompress) {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_IS_COMPRESS, isCompress);
        return sSelector;
    }

    public MultiImageSelector compressRankThird() {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_COMPRESS_RANK, Luban.THIRD_GEAR);
        return sSelector;
    }

    public MultiImageSelector compressRankFirst() {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_COMPRESS_RANK, Luban.FIRST_GEAR);
        return sSelector;
    }

    public MultiImageSelector crop(boolean isCrop) {
        intent.putExtra(MultiImageSelectorActivity.EXTRA_IS_CROP, isCrop);
        return sSelector;
    }

    public MultiImageSelector crop(int cropWidth, int cropHeight) {
        crop(true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_CROP_WIDTH, cropWidth);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_CROP_HEIGHT, cropHeight);
        return sSelector;
    }

    public MultiImageSelector color(int color) {
        intent.putExtra(Crop.Extra.COLOR, color);
        return sSelector;
    }

    public MultiImageSelector showCircle(boolean showCircle) {
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
        intent.setClass(context, MultiImageSelectorActivity.class);
        if (mOriginData != null) {
            intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mOriginData);
        }

        return intent;
    }
}
