package com.ashlikun.photo_hander.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author　　: 李坤
 * 创建时间: 2020/12/3 17:41
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：媒体实体类
 */

public class MediaFile implements Parcelable, Comparable<MediaFile> {
    /**
     * 可能是网络图
     */
    public String path;
    public String name;
    public String mime;
    public Integer folderId;
    public String folderName;
    public long duration;
    public long dateToken;

    public MediaFile(String path) {
        this.path = path;
    }

    public MediaFile(String path, String name, long dateToken) {
        this.path = path;
        this.name = name;
        this.dateToken = dateToken;
    }

    public boolean isVideo() {
        return duration > 0;
    }


    /**
     * 是否是网络数据
     *
     * @return
     */
    public boolean isHttp() {
        if (path != null && path.startsWith("http")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        try {
            MediaFile other = (MediaFile) o;
            return TextUtils.equals(this.path, other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    @Override
    public int compareTo(MediaFile o) {
        if (o.dateToken > dateToken) {
            return 1;
        } else if (o.dateToken < dateToken) {
            return -1;
        }
        return 0;
    }

    protected MediaFile(Parcel in) {
        path = in.readString();
        mime = in.readString();
        if (in.readByte() == 0) {
            folderId = null;
        } else {
            folderId = in.readInt();
        }
        folderName = in.readString();
        duration = in.readLong();
        dateToken = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(mime);
        if (folderId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(folderId);
        }
        dest.writeString(folderName);
        dest.writeLong(duration);
        dest.writeLong(dateToken);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaFile> CREATOR = new Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(Parcel in) {
            return new MediaFile(in);
        }

        @Override
        public MediaFile[] newArray(int size) {
            return new MediaFile[size];
        }
    };


}

