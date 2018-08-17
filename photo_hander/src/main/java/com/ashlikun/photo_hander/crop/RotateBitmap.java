/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ashlikun.photo_hander.crop;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 16:17
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：旋转图片用的
 */

class RotateBitmap {

    private Bitmap bitmap;
    private int rotation;

    public RotateBitmap(Bitmap bitmap, int rotation) {
        this.bitmap = bitmap;
        this.rotation = rotation % 360;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Matrix getRotateMatrix() {
        // 默认情况下，这是一个单位矩阵
        Matrix matrix = new Matrix();
        if (bitmap != null && rotation != 0) {
            // 我们想要在原点处进行旋转，但是从边界开始
            //矩形会在旋转后改变，所以delta值
            //分别基于旧的和新的高度/高度。
            int cx = bitmap.getWidth() / 2;
            int cy = bitmap.getHeight() / 2;
            matrix.preTranslate(-cx, -cy);
            matrix.postRotate(rotation);
            matrix.postTranslate(getWidth() / 2, getHeight() / 2);
        }
        return matrix;
    }

    public boolean isOrientationChanged() {
        return (rotation / 90) % 2 != 0;
    }

    public int getHeight() {
        if (bitmap == null) {
            return 0;
        }
        if (isOrientationChanged()) {
            return bitmap.getWidth();
        } else {
            return bitmap.getHeight();
        }
    }

    public int getWidth() {
        if (bitmap == null) {
            return 0;
        }
        if (isOrientationChanged()) {
            return bitmap.getHeight();
        } else {
            return bitmap.getWidth();
        }
    }

    public void recycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}

