package com.ashlikun.photo_hander.crop;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　15:14
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：裁剪页面需要的参数
 */
class CropOptionData implements Parcelable{
    /**
     * 原始图
     */
    public Uri source;
    /**
     * 保存的图片地址
     */
    public Uri saveUri;
    /**
     * 裁剪框的宽高
     */
    public int cropWidth;
    public int cropHeight;
    /**
     * 输出的图片信息
     */
    public int outMaxWidth;
    public int outMaxHeight;

    /**
     * png格式保存
     */
    public boolean outAsPng;
    /**
     * 裁剪框是圆形的
     */
    public boolean showCircle;
    /**
     * 裁剪框的颜色
     */
    public int color;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.source, flags);
        dest.writeParcelable(this.saveUri, flags);
        dest.writeInt(this.cropWidth);
        dest.writeInt(this.cropHeight);
        dest.writeInt(this.outMaxWidth);
        dest.writeInt(this.outMaxHeight);
        dest.writeByte(this.outAsPng ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showCircle ? (byte) 1 : (byte) 0);
        dest.writeInt(this.color);
    }

    public CropOptionData() {
    }

    protected CropOptionData(Parcel in) {
        this.source = in.readParcelable(Uri.class.getClassLoader());
        this.saveUri = in.readParcelable(Uri.class.getClassLoader());
        this.cropWidth = in.readInt();
        this.cropHeight = in.readInt();
        this.outMaxWidth = in.readInt();
        this.outMaxHeight = in.readInt();
        this.outAsPng = in.readByte() != 0;
        this.showCircle = in.readByte() != 0;
        this.color = in.readInt();
    }

    public static final Creator<CropOptionData> CREATOR = new Creator<CropOptionData>() {
        @Override
        public CropOptionData createFromParcel(Parcel source) {
            return new CropOptionData(source);
        }

        @Override
        public CropOptionData[] newArray(int size) {
            return new CropOptionData[size];
        }
    };
}
