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
public class MediaSelectData implements Parcelable {
    public MediaFile mediaFile;
    public String compressPath;
    //裁剪的图片
    public String cropPath;

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
        if (mediaFile != null && PhotoHanderUtils.isHttpImg(mediaFile.path)) {
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
        } else if (mediaFile != null && mediaFile.path != null && !TextUtils.equals(mediaFile.path, compressPath)) {
            return true;
        }

        return false;
    }


    public MediaSelectData(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
        if (isHttpImg()) {
            this.compressPath = mediaFile.path;
        }
    }

    public boolean isVideo() {
        return mediaFile.isVideo();
    }

    public MediaSelectData(MediaFile mediaFile, String compressPath, boolean isCompress, boolean isComparessError) {
        this.mediaFile = mediaFile;
        this.compressPath = compressPath;
        this.isComparessError = isComparessError;
        this.isCompress = isCompress;
    }

    public String originPath() {
        if (mediaFile != null) {
            return mediaFile.path;
        }
        return "";
    }

    public static ArrayList<String> getOriginPaths(ArrayList<MediaSelectData> resultList) {
        ArrayList<String> strings = new ArrayList<>();
        if (resultList != null) {
            for (MediaSelectData d : resultList) {
                //如果有裁剪的图片，优先使用裁剪的图片
                if (!TextUtils.isEmpty(d.cropPath)) {
                    strings.add(d.cropPath);
                } else {
                    strings.add(d.mediaFile.path);
                }

            }
        }
        return strings;
    }

    public static ArrayList<String> getCompressImagePaths(ArrayList<MediaSelectData> resultList) {
        ArrayList<String> strings = new ArrayList<>();
        if (resultList != null) {
            for (MediaSelectData d : resultList) {
                if (d.isVideo()) {
                    continue;
                }
                //如果有裁剪的图片，优先使用裁剪的图片
                if (!TextUtils.isEmpty(d.cropPath)) {
                    strings.add(d.cropPath);
                } else {
                    strings.add(d.mediaFile.path);
                }

            }
        }
        return strings;
    }

    public static ArrayList<MediaSelectData> getCompressVideoPaths(ArrayList<MediaSelectData> resultList) {
        ArrayList<MediaSelectData> strings = new ArrayList<>();
        if (resultList != null) {
            for (MediaSelectData d : resultList) {
                if (!d.isVideo()) {
                    continue;
                }
                strings.add(d);
            }
        }
        return strings;
    }

    @Override
    public String toString() {
        long sizeOrigin = 0;
        long sizeCompress = 0;
        try {
            sizeOrigin = PhotoHanderUtils.getFileSizes(new File(mediaFile.path));
            sizeCompress = PhotoHanderUtils.getFileSizes(new File(compressPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mediaFile != null && mediaFile.isVideo()) {
            return "视频：" + mediaFile.path + "  size = " + sizeOrigin + "       压缩视频:" + compressPath + "  size = " + sizeCompress;
        }
        return "原图：" + mediaFile.path + "  size = " + sizeOrigin + "       压缩图:" + compressPath + "  size = " + sizeCompress + "       裁剪图:" + cropPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    protected MediaSelectData(Parcel in) {
        mediaFile = in.readParcelable(MediaFile.class.getClassLoader());
        compressPath = in.readString();
        cropPath = in.readString();
        isComparessError = in.readByte() != 0;
        isCompress = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mediaFile, flags);
        dest.writeString(compressPath);
        dest.writeString(cropPath);
        dest.writeByte((byte) (isComparessError ? 1 : 0));
        dest.writeByte((byte) (isCompress ? 1 : 0));
    }

    public static final Creator<MediaSelectData> CREATOR = new Creator<MediaSelectData>() {
        @Override
        public MediaSelectData createFromParcel(Parcel in) {
            return new MediaSelectData(in);
        }

        @Override
        public MediaSelectData[] newArray(int size) {
            return new MediaSelectData[size];
        }
    };


}
