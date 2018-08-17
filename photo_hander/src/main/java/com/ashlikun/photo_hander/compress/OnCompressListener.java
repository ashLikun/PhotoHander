package com.ashlikun.photo_hander.compress;

import com.ashlikun.photo_hander.bean.ImageSelectData;

import java.util.ArrayList;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 13:24
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：压缩监听
 */

public abstract class OnCompressListener {

    public void onStart() {
    }

    public abstract void onSuccess(ArrayList<ImageSelectData> files);

    public void onError(Throwable e) {
    }

    public void onLoading(int progress, long total) {
    }
}
