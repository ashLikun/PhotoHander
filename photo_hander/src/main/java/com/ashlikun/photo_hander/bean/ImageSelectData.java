package com.ashlikun.photo_hander.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　14:37
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：照片选择的数据
 * 包含原始图，和压缩图片
 */
public class ImageSelectData implements Parcelable {
    public String originPath;
    public String compressPath;

    /**
     * 是否压缩失败
     */
    public boolean isComparessError = false;
    //是否压缩（不是网络图，缓存没有，文件太小）
    public boolean isCompress;

    /**
     * 这张图片是否是网络图
     *
     * @return
     */
    public boolean isHttpImg() {
        if (PhotoHanderUtils.isHttpImg(originPath)) {
            return true;
        }
        if (PhotoHanderUtils.isHttpImg(compressPath)) {
            return true;
        }
        return false;
    }

    /**
     * 这张图片是否经过压缩
     * 1：网络图不压缩
     * 2：开启压缩
     * 3：太小图不压缩
     *
     * @return
     */
    public boolean isCompress() {
        if (isHttpImg()) {
            return false;
        } else if (originPath != null && !TextUtils.equals(originPath, compressPath)) {
            return true;
        }

        return false;
    }

    public ImageSelectData(String originImage, String compressImage) {
        this.originPath = originImage;
        this.compressPath = compressImage;
    }

    public ImageSelectData(String originPath, String compressPath, boolean isCompress) {
        this.originPath = originPath;
        this.compressPath = compressPath;
        this.isCompress = isCompress;
    }


    public ImageSelectData(String originImage) {
        this.originPath = originImage;
        if (isHttpImg()) {
            this.compressPath = originImage;
        }
    }

    public ImageSelectData(String originPath, String compressPath, boolean isCompress, boolean isComparessError) {
        this.originPath = originPath;
        this.compressPath = compressPath;
        this.isComparessError = isComparessError;
        this.isCompress = isCompress;
    }

    @Override
    public String toString() {
        long sizeOrigin = 0;
        long sizeCompress = 0;
        try {
            sizeOrigin = PhotoHanderUtils.getFileSizes(new File(originPath));
            sizeCompress = PhotoHanderUtils.getFileSizes(new File(compressPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "原图：" + originPath + "  size = " + sizeOrigin + "       压缩图:" + compressPath + "  size = " + sizeCompress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.originPath);
        dest.writeString(this.compressPath);
    }

    public ImageSelectData() {
    }

    protected ImageSelectData(Parcel in) {
        this.originPath = in.readString();
        this.compressPath = in.readString();
    }

    public static final Creator<ImageSelectData> CREATOR = new Creator<ImageSelectData>() {
        @Override
        public ImageSelectData createFromParcel(Parcel source) {
            return new ImageSelectData(source);
        }

        @Override
        public ImageSelectData[] newArray(int size) {
            return new ImageSelectData[size];
        }
    };

    public static ArrayList<String> getOriginPaths(ArrayList<ImageSelectData> resultList) {
        ArrayList<String> strings = new ArrayList<>();
        if (resultList != null) {
            for (ImageSelectData d : resultList) {
                strings.add(d.originPath);
            }
        }
        return strings;
    }
}
