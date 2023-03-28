package com.ashlikun.photo_hander.utils;

import android.util.Pair;

import androidx.activity.result.ActivityResult;

import java.io.File;

/**
 * 作者　　: 李坤
 * 创建时间: 2023/3/28　17:20
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public interface ShowCameraActionCall {
    void call(Pair<File, ActivityResult> data);
}
