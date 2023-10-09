package com.ashlikun.photo_hander;

import java.io.Serializable;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　13:15
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：启动照片选择的属性
 */
public class PhotoOptionData implements Serializable {
    //保存
    public static PhotoOptionData currentData;

    public static void setCurrentData(PhotoOptionData data) {
        currentData = data;
    }

    /**
     * 默认的最大图片数量
     */
    private static final int DEFAULT_IMAGE_SIZE = 9;
    /**
     * 单选模式
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选模式
     */
    public static final int MODE_MULTI = 1;

    /**
     * 最大选中个数
     */
    public int mDefaultCount = DEFAULT_IMAGE_SIZE;
    /**
     * 裁剪框的宽度
     */
    public int cropWidth = 0;
    /**
     * 裁剪框的高度
     */
    public int cropHeight = 0;
    /**
     * 是否显示摄像头
     */
    public boolean isShowCamera = true;
    /**
     * 是否只能拍照
     * 注意在主题里面设置  <item name="android:windowIsTranslucent">true</item>
     */
    public boolean isMustCamera = false;
    /**
     * 拍照是否插入相册
     * true:拍照会存放在/DCIM 目录
     * false:拍照会存放在/data/data/pack/cache 目录
     */
    public boolean isInsetPhoto = false;
    /**
     * 是否裁剪
     */
    public boolean mIsCrop = false;
    /**
     * 是否显示圆
     */
    public boolean cropShowCircle = false;
    /**
     * 裁剪的颜色
     */
    public int cropColor;
    /**
     * 是否压缩
     */
    public boolean isCompress = false;
    /**
     * 选择的模式
     */
    public int selectMode = MODE_MULTI;
    /**
     * 是否可以选择视频
     */
    public boolean isSelectVideo = false;
    /**
     * 如果选择视频，那么能选的视频的时长，-1代表不限,单位秒
     */
    public long videoMaxDuration = -1;
    /**
     * 是否只能选择视频
     */
    public boolean isVideoOnly = false;
    /**
     * 是否压缩视频,可以自己实现OnPhotoHandlerListener，
     * 如果不实现就会调用com.github.yellowcath:VideoProcessor:2.4.2库，内部会检测是否有库
     */
    public boolean isVideoCompress = false;
    /**
     * 是否转码HEIF文件为JPG
     */
    public boolean isHeifToJpg = false;
    /**
     * 视频压缩的Fps,默认30
     */
    public int videoCompressFps = 30;
    /**
     * 视频的宽高比是否改变，true:动态计算，false：不变
     */
    public boolean isVideoCompressAspectRatio = true;
    /**
     * 是否过滤目录名称
     */
    public boolean isFilterFolder = true;
    /**
     * 是否可以选择0个文件
     */
    public boolean isNoSelect = false;

    public PhotoOptionData() {
    }

    /**
     * 是否是多选
     *
     * @return true：多选， false：单选
     */
    public boolean isModeMulti() {
        return selectMode == MODE_MULTI;
    }

    public boolean isFilterGif() {
        return true;
    }

    /**
     * 是否能选择视频
     *
     * @return
     */
    public boolean isCanVideo() {
        return isVideoOnly || isSelectVideo;
    }

    /**
     * 是否能选择视频和图片
     *
     * @return
     */
    public boolean isSelectVideoAndImg() {
        return !isVideoOnly && isSelectVideo;
    }

}
