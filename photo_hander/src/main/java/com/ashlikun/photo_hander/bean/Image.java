package com.ashlikun.photo_hander.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 13:23
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：图片实体
 */

public class Image implements Parcelable {
    /**
     * 可能是网络图
     */
    public String path;
    public String name;
    public long time;

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

    public Image(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        try {
            Image other = (Image) o;
            return TextUtils.equals(this.path, other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeString(this.name);
        dest.writeLong(this.time);
    }

    protected Image(Parcel in) {
        this.path = in.readString();
        this.name = in.readString();
        this.time = in.readLong();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
