package com.ashlikun.photo_hander.crop;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.ashlikun.photo_hander.R;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 13:49
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：裁剪的辅助类，设置一些参数然后启动
 */

public class Crop {

    public static final int REQUEST_CROP = 6709;
    public static final int REQUEST_PICK = 9162;
    public static final int RESULT_ERROR = 404;
    public final static String CROP_ORGIN_OUTPUT = "CROP_ORGIN_OUTPUT";

    CropOptionData optionData;

    /**
     * 图像uri创建Crop
     *
     * @param source  原始图片
     * @param saveUri 生成的图片
     */
    public static Crop of(Uri source, Uri saveUri) {
        return new Crop(source, saveUri);
    }

    private Crop(Uri source, Uri saveUri) {
        optionData = new CropOptionData();
        optionData.source = source;
        optionData.saveUri = saveUri;
    }

    /**
     * 裁剪框的宽高
     *
     * @param cropWidth  宽
     * @param cropHeight 高
     */
    public Crop withSize(int cropWidth, int cropHeight) {
        optionData.cropWidth = cropWidth;
        optionData.cropHeight = cropHeight;
        return this;
    }

    /**
     * 裁剪框的宽高1:1,就是正方形
     */
    public Crop asSquare() {
        withSize(1, 1);
        return this;
    }

    /**
     * 设置生成的图片最大大小
     *
     * @param width  Max width
     * @param height Max height
     */
    public Crop withMaxSize(int width, int height) {
        optionData.outMaxWidth = width;
        optionData.outMaxHeight = height;
        return this;
    }

    /**
     * 生成png
     *
     * @param asPng
     */
    public Crop asPng(boolean asPng) {
        optionData.outAsPng = asPng;
        return this;
    }

    /**
     * 边框颜色
     *
     * @param color
     * @return
     */
    public Crop color(int color) {
        optionData.color = color;
        return this;
    }

    /**
     * 圆形裁剪
     *
     * @param showCircle
     * @return
     */
    public Crop showCircle(boolean showCircle) {
        optionData.showCircle = showCircle;
        return this;
    }

    /**
     * 开始启动裁剪界面
     *
     * @param activity
     */
    public void start(Activity activity) {

        start(activity, REQUEST_CROP);
    }

    /**
     * 开始启动裁剪界面
     *
     * @param activity
     * @param requestCode requestCode for result
     */
    public void start(Activity activity, int requestCode) {
        activity.startActivityForResult(getIntent(activity), requestCode);
    }

    /**
     * 开始启动裁剪界面
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public void start(Context context, Fragment fragment) {
        start(context, fragment, REQUEST_CROP);
    }

    /**
     * 开始启动裁剪界面
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public void start(Context context, androidx.fragment.app.Fragment fragment) {
        start(context, fragment, REQUEST_CROP);
    }

    /**
     * 开始启动裁剪界面
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    public void start(Context context, Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getIntent(context), requestCode);
    }

    /**
     * 开始启动裁剪界面
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    public void start(Context context, androidx.fragment.app.Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getIntent(context), requestCode);
    }

    /**
     * 获取启动裁剪页面的意图
     *
     * @param context Context
     * @return Intent for CropImageActivity
     */
    public Intent getIntent(Context context) {
        Intent cropIntent = new Intent(context, CropImageActivity.class);
        CropOptionData.setCurrentData(optionData);
        return cropIntent;
    }

    /**
     * 获取返回的图片url
     *
     * @param result Output Image URI
     */
    public static Uri getOutput(Intent result) {
        return result.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
    }

    /**
     * 获取原图路径
     *
     * @param result Output Image URI
     */
    public static Uri getOrginOutput(Intent result) {
        return result.getParcelableExtra(Crop.CROP_ORGIN_OUTPUT);
    }

    /**
     * 提供给其他界面获取错误信息
     *
     * @param result Result Intent
     * @return Throwable handled in CropImageActivity
     */
    public static Throwable getError(Intent result) {
        return (Throwable) result.getSerializableExtra(IntentKey.EXTRA_ERROR);
    }

    /**
     * 从图库里面选择图片
     *
     * @param activity Activity to receive result
     */
    public static void pickImage(Activity activity) {
        pickImage(activity, REQUEST_PICK);
    }

    /**
     * 从图库里面选择图片
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public static void pickImage(Context context, Fragment fragment) {
        pickImage(context, fragment, REQUEST_PICK);
    }

    /**
     * 从图库里面选择图片
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    public static void pickImage(Context context, androidx.fragment.app.Fragment fragment) {
        pickImage(context, fragment, REQUEST_PICK);
    }

    /**
     * 从图库里面选择图片
     *
     * @param activity    Activity to receive result
     * @param requestCode requestCode for result
     */
    public static void pickImage(Activity activity, int requestCode) {
        try {
            activity.startActivityForResult(getImagePicker(), requestCode);
        } catch (ActivityNotFoundException e) {
            showImagePickerError(activity);
        }
    }

    /**
     * 从图库里面选择图片
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    public static void pickImage(Context context, Fragment fragment, int requestCode) {
        try {
            fragment.startActivityForResult(getImagePicker(), requestCode);
        } catch (ActivityNotFoundException e) {
            showImagePickerError(context);
        }
    }

    /**
     * 从图库里面选择图片
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    public static void pickImage(Context context, androidx.fragment.app.Fragment fragment, int requestCode) {
        try {
            fragment.startActivityForResult(getImagePicker(), requestCode);
        } catch (ActivityNotFoundException e) {
            showImagePickerError(context);
        }
    }

    private static Intent getImagePicker() {
        return new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
    }

    private static void showImagePickerError(Context context) {
        Toast.makeText(context.getApplicationContext(), R.string.photo_crop_pick_error, Toast.LENGTH_SHORT).show();
    }

}
