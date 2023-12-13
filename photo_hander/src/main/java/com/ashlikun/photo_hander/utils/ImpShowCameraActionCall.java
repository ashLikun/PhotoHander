package com.ashlikun.photo_hander.utils;

import android.app.Activity;
import android.util.Pair;

import androidx.activity.result.ActivityResult;

import com.ashlikun.photo_hander.PhotoHanderFragment;
import com.ashlikun.photo_hander.PhotoOptionData;
import com.ashlikun.photo_hander.bean.MediaFile;

import java.io.File;

/**
 * 作者　　: 李坤
 * 创建时间: 2023/3/28　17:20
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class ImpShowCameraActionCall implements ShowCameraActionCall {
    PhotoHanderFragment.Callback mCallback;
    Activity activity;

    public ImpShowCameraActionCall(Activity activity, PhotoHanderFragment.Callback mCallback) {
        this.mCallback = mCallback;
        this.activity = activity;
    }

    @Override
    public void call(Pair<File, ActivityResult> data) {
        File tmpFile = data.first;
        if (data.second.getResultCode() == Activity.RESULT_OK) {
            if (tmpFile != null) {
                if (mCallback != null) {
                    if (PhotoOptionData.currentData.isVideoOnly && PhotoOptionData.currentData.isShowCamera) {
                        final File finalTmpFile = tmpFile;
                        PhotoThreadUtils.get().execute(new Runnable() {
                            @Override
                            public void run() {
                                final MediaFile mediaFile = PhotoHanderUtils.getVideoInfo(finalTmpFile, 3);
                                PhotoThreadUtils.get().posts(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.onCameraShot(mediaFile);
                                    }
                                });
                            }
                        });
                    } else {
                        mCallback.onCameraShot(new MediaFile(tmpFile.getPath(), 1));
                    }

                }
            }
        } else {
            // delete tmp file
            while (tmpFile != null && tmpFile.exists()) {
                boolean success = tmpFile.delete();
                if (success) {
                    tmpFile = null;
                }
            }
            if (PhotoOptionData.currentData.isMustCamera) {
                activity.finish();
            }
        }
    }
}
