package com.ashlikun.photo_hander;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaSelectData;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 16:28 Administrator
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：图片选择器
 */

public class PhotoHander {
    //启动视频播放器的回调,在Application里面初始化
    public final static OnPhotoHandlerListener onPhotoHandlerListener = null;

    public static interface OnPhotoHandlerListener {
        /**
         * 播放视频
         *
         * @param mediaFile
         */
        void onVideoPlay(MediaFile mediaFile);
    }


    public static abstract class OnPhotoHandlerListenerAdapter implements OnPhotoHandlerListener {
        @Override
        public void onVideoPlay(MediaFile mediaFile) {
        }
    }

    //拍照code
    public final static int REQUEST_CAMERA = 100;

    //读写存储卡和拍照权限code
    public static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 110;
    /**
     * 已经选择的数据
     */
    private ArrayList<MediaSelectData> mOriginData;
    /**
     * 额外添加到顶部的数据,一般是网络图
     */
    private ArrayList<String> mAddImages;
    /**
     * 配置参数
     */
    private PhotoOptionData optionData;


    private PhotoHander() {
        optionData = new PhotoOptionData();
    }

    public static PhotoHander create() {
        //返回一个实例
        return new PhotoHander();
    }

    /**
     * 是否显示摄像头
     *
     * @param mShowCamera
     * @return
     */
    public PhotoHander showCamera(boolean mShowCamera) {
        optionData.isShowCamera = mShowCamera;
        return this;
    }

    /**
     * 是否只能拍照
     *
     * @param isMustCamera
     * @return
     */
    public PhotoHander isMustCamera(boolean isMustCamera) {
        optionData.isMustCamera = isMustCamera;
        return this;
    }

    /**
     * 最大多少张
     *
     * @param count
     * @return
     */
    public PhotoHander count(int count) {
        optionData.mDefaultCount = count;
        return this;
    }

    /**
     * 单选
     *
     * @return
     */
    public PhotoHander single() {
        optionData.selectMode = PhotoOptionData.MODE_SINGLE;
        return this;
    }

    /**
     * 多选
     *
     * @return
     */
    public PhotoHander multi() {
        optionData.selectMode = PhotoOptionData.MODE_MULTI;
        return this;
    }

    /**
     * 已选
     *
     * @param images
     * @return
     */
    public PhotoHander origin(ArrayList<MediaSelectData> images) {
        mOriginData = images;
        return this;
    }

    /**
     * 额外添加到顶部的数据,一般是网络图
     */
    public PhotoHander addImage(ArrayList<String> addImages) {
        mAddImages = addImages;
        return this;
    }

    /**
     * 压缩
     *
     * @param isCompress
     * @return
     */
    public PhotoHander compress(boolean isCompress) {
        optionData.isCompress = isCompress;
        return this;
    }

    /**
     * 3级压缩,高，一般在100-400kb
     *
     * @return
     */
    public PhotoHander compressRankThird() {
        return this;
    }

    /**
     * 2级压缩,中，一般在200-1024kb
     *
     * @return
     */
    public PhotoHander compressRankDouble() {
        return this;
    }

    /**
     * 1级压缩,低,一般在60-文件大小/5
     *
     * @return
     */
    public PhotoHander compressRankFirst() {
        return this;
    }

    /**
     * 裁剪
     *
     * @param isCrop
     * @return
     */
    public PhotoHander crop(boolean isCrop) {
        optionData.mIsCrop = isCrop;
        return this;
    }

    /**
     * 裁剪框
     *
     * @param cropWidth
     * @param cropHeight
     * @return
     */
    public PhotoHander crop(int cropWidth, int cropHeight) {
        crop(true);
        optionData.cropWidth = cropWidth;
        optionData.cropHeight = cropHeight;
        return this;
    }

    /**
     * 裁剪框圆形
     *
     * @param showCircle
     * @return
     */
    public PhotoHander cropCircle(boolean showCircle) {
        crop(true);
        optionData.cropShowCircle = showCircle;
        return this;
    }

    /**
     * 裁剪框颜色
     *
     * @param color
     * @return
     */
    public PhotoHander cropColor(int color) {
        optionData.cropColor = color;
        return this;
    }

    /**
     * 是否过滤目录名称
     */
    public PhotoHander filterFolderNameNo() {
        optionData.isFilterFolder = false;
        return this;
    }

    /**
     * 能选择视频和图片
     */
    public PhotoHander selectVideo() {
        optionData.isSelectVideo = true;
        optionData.isVideoCompress = true;
        return this;
    }

    /**
     * 如果选择视频，那么能选的视频的时长，-1代表不限,单位秒
     *
     * @param videoMaxDuration
     * @return
     */
    public PhotoHander videoMaxDuration(long videoMaxDuration) {
        optionData.videoMaxDuration = videoMaxDuration;
        return this;
    }

    /**
     * 只能选择视频
     */
    public PhotoHander videoOnly() {
        optionData.isVideoOnly = true;
        optionData.isVideoCompress = true;
        return this;
    }

    /**
     * 是否压缩视频,可以自己实现OnPhotoHandlerListener，
     * * 如果不实现就会调用com.github.yellowcath:VideoProcessor:2.4.2库，内部会检测是否有库
     */
    public PhotoHander videoCompress() {
        optionData.isVideoCompress = true;
        return this;
    }

    /**
     * 视频压缩的Fps,默认30
     */
    public PhotoHander videoCompressFps(int videoCompressFps) {
        optionData.videoCompressFps = videoCompressFps;
        return this;
    }

    /**
     * 视频压缩时候的宽高不变
     */
    public PhotoHander videoCompressAspectRatio() {
        optionData.isVideoCompressAspectRatio = false;
        return this;
    }

    /**
     * 开启
     *
     * @param activity
     * @param requestCode
     */
    public void start(Activity activity, int requestCode) {
        final Context context = activity;
        activity.startActivityForResult(createIntent(context), requestCode);
    }

    /**
     * 开启
     *
     * @param fragment
     * @param requestCode
     */
    public void start(Fragment fragment, int requestCode) {
        final Context context = fragment.getContext();
        fragment.startActivityForResult(createIntent(context), requestCode);
    }

    /**
     * 开启
     */
    public ActivityResultLauncher start(ComponentActivity activity, final ActivityResultCallback<List<MediaSelectData>> callback) {
        ActivityResultLauncher launcher = PhotoHanderUtils.registerForActivityResultX(activity, new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    List<MediaSelectData> mSelectPath = PhotoHander.getIntentResult(result.getData());
                    callback.onActivityResult(mSelectPath);
                }
            }
        });
        launcher.launch(createIntent(activity));
        return launcher;
    }

    /**
     * 开启
     */
    public ActivityResultLauncher start(Fragment fragment, final ActivityResultCallback<List<MediaSelectData>> callback) {
        ActivityResultLauncher launcher = PhotoHanderUtils.registerForActivityResultXF(fragment, new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    List<MediaSelectData> mSelectPath = PhotoHander.getIntentResult(result.getData());
                    callback.onActivityResult(mSelectPath);
                }
            }
        });
        launcher.launch(createIntent(fragment.getContext()));
        return launcher;
    }

    private Intent createIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, PhotoHanderActivity.class);
        //只有多选才会有原始数据
        if (optionData.isModeMulti() && mOriginData != null) {
            intent.putParcelableArrayListExtra(IntentKey.EXTRA_DEFAULT_SELECTED_LIST, mOriginData);
        }
        if (mAddImages != null) {
            intent.putStringArrayListExtra(IntentKey.EXTRA_DEFAULT_ADD_IMAGES, mAddImages);
        }
        PhotoOptionData.setCurrentData(optionData);
        optionData = null;
        mOriginData = null;
        mAddImages = null;
        return intent;
    }

    /**
     * 获取照片选择后的地址
     *
     * @param data
     * @return
     */
    public static ArrayList<MediaSelectData> getIntentResult(Intent data) {
        if (data == null) {
            return new ArrayList<>();
        }
        ArrayList<MediaSelectData> result = data.getParcelableArrayListExtra(IntentKey.EXTRA_RESULT);
        if (result == null) return new ArrayList<>();
        return result;
    }

}
