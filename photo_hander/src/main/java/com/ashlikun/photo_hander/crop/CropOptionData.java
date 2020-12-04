package com.ashlikun.photo_hander.crop;

import android.net.Uri;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　15:14
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：裁剪页面需要的参数
 */
class CropOptionData {
    //保存
    public static CropOptionData currentData;

    public static void setCurrentData(CropOptionData data) {
        currentData = data;
    }

    /**
     * 原始图
     */
    public Uri source;
    /**
     * 保存的图片地址
     */
    public Uri saveUri;
    /**
     * 裁剪框的宽高
     */
    public int cropWidth;
    public int cropHeight;
    /**
     * 输出的图片信息
     */
    public int outMaxWidth;
    public int outMaxHeight;

    /**
     * png格式保存
     */
    public boolean outAsPng;
    /**
     * 裁剪框是圆形的
     */
    public boolean showCircle;
    /**
     * 裁剪框的颜色
     */
    public int color;


}
