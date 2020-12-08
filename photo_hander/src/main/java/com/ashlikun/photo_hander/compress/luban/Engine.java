package com.ashlikun.photo_hander.compress.luban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Responsible for starting compress and managing active and cached resources.
 */
class Engine {
    private InputStreamProvider srcImg;
    private int srcWidth;
    private int srcHeight;
    //希望的大小 kb
    private int expectSize;
    //压缩的比例
    private int inSampleSize;
    private boolean focusAlpha;

    Engine(InputStreamProvider srcImg, boolean focusAlpha) throws IOException {
        this.srcImg = srcImg;
        this.expectSize = srcImg.expectSize();
        this.focusAlpha = focusAlpha;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeStream(srcImg.open(), null, options);
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;
        inSampleSize = computeSize();
        srcImg.setExpectSize(expectSize);
    }


    private int computeSize() {
        //计算出来的缩放比例
        int size = 0;
        double expectSize = 0;
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                size = 1;
                expectSize = (srcWidth * srcHeight / (size * 2)) / Math.pow(1664, 2) * 100;
                //希望（60-100）kb
                expectSize = Math.max(60, expectSize);
            } else if (longSide < 4990) {
                size = 2;
                expectSize = (srcWidth * srcHeight / (size * 2)) / Math.pow(2495, 2) * 300;
                //希望（60-300）kb
                expectSize = Math.max(60, expectSize);
            } else if (longSide > 4990 && longSide < 10240) {
                size = 4;
                expectSize = (srcWidth * srcHeight / (size * 2)) / Math.pow(2560, 2) * 300;
                //希望（100-300）kb
                expectSize = Math.max(100, expectSize);
            } else {
                size = longSide / 1280 == 0 ? 1 : longSide / 1280;
                expectSize = (srcWidth * srcHeight / (size * 2)) / Math.pow(2560, 2) * 300;
                //希望（100-300）kb
                expectSize = Math.max(100, expectSize);

            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            size = longSide / 1280 == 0 ? 1 : longSide / 1280;
            expectSize = (srcWidth * srcHeight / (size * 2)) / (1440.0 * 2560.0) * 400;
            //希望（100-400）kb
            expectSize = Math.max(100, expectSize);
        } else {
            size = (int) Math.ceil(longSide / (1280.0 / scale));
            expectSize = ((srcWidth * srcHeight / (size * 2)) / (1280.0 * (1280 / scale))) * 500;
            //希望（100-400）kb
            expectSize = Math.max(100, expectSize);
        }
        if (this.expectSize <= 0) {
            this.expectSize = (int) expectSize;
        }
        return size;
    }

    private Bitmap rotatingImage(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    File compress(File tagImg) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (Checker.SINGLE.isJPG(srcImg.open())) {
            tagBitmap = rotatingImage(tagBitmap, Checker.SINGLE.getOrientation(srcImg.open()));
        }
        //图片质量
        int optionsSize = 100;
        //压缩图片(质量压缩)
        tagBitmap.compress(focusAlpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, optionsSize, stream);
        while (stream.toByteArray().length / 1024.0 > expectSize && optionsSize > 6) {
            stream.reset();
            optionsSize -= 6;
            tagBitmap.compress(focusAlpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, optionsSize, stream);
        }
        tagBitmap.recycle();

        FileOutputStream fos = new FileOutputStream(tagImg);
        fos.write(stream.toByteArray());
        fos.flush();
        fos.close();
        stream.close();
        return tagImg;
    }
}