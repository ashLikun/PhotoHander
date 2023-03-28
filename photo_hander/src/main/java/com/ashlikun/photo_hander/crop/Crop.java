package com.ashlikun.photo_hander.crop;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 13:49
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：裁剪的辅助类，设置一些参数然后启动
 */

public class Crop {

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
     */
    public void start(Context context, ActivityResultLauncher launcher) {
        launcher.launch(getIntent(context));
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

    private static Intent getImagePicker() {
        return new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
    }
}
