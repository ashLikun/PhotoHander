package com.ashlikun.photo_hander;

import android.os.Parcel;
import android.os.Parcelable;

import com.ashlikun.photo_hander.compress.Luban;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　13:15
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：启动照片选择的属性
 */
class PhotoOptionData implements Parcelable {
    /**
     * 默认的最大图片数量
     */
    private static final int DEFAULT_IMAGE_SIZE = 9;
    /**
     * 单选模式
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选模式
     */
    public static final int MODE_MULTI = 1;

    /**
     * 最大选中个数
     */
    public int mDefaultCount = DEFAULT_IMAGE_SIZE;
    /**
     * 裁剪框的宽度
     */
    public int cropWidth = 0;
    /**
     * 裁剪框的高度
     */
    public int cropHeight = 0;
    /**
     * 是否显示摄像头
     */
    public boolean isShowCamera = true;
    /**
     * 是否裁剪
     */
    public boolean mIsCrop = false;
    /**
     * 是否显示圆
     */
    public boolean cropShowCircle = false;
    /**
     * 裁剪的颜色
     */
    public int cropColor;
    /**
     * 是否压缩
     */
    public boolean isCompress = false;
    /**
     * 压缩等级
     */
    public int compressRank = Luban.THIRD_GEAR;
    /**
     * 选择的模式
     */
    public int selectMode = MODE_MULTI;

    public PhotoOptionData() {
    }
    /**
     * 是否是多选
     *
     * @return true：多选， false：单选
     */
    public boolean isModeMulti() {
        return selectMode == MODE_MULTI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDefaultCount);
        dest.writeInt(this.cropWidth);
        dest.writeInt(this.cropHeight);
        dest.writeByte(this.isShowCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mIsCrop ? (byte) 1 : (byte) 0);
        dest.writeByte(this.cropShowCircle ? (byte) 1 : (byte) 0);
        dest.writeInt(this.cropColor);
        dest.writeByte(this.isCompress ? (byte) 1 : (byte) 0);
        dest.writeInt(this.compressRank);
        dest.writeInt(this.selectMode);
    }

    protected PhotoOptionData(Parcel in) {
        this.mDefaultCount = in.readInt();
        this.cropWidth = in.readInt();
        this.cropHeight = in.readInt();
        this.isShowCamera = in.readByte() != 0;
        this.mIsCrop = in.readByte() != 0;
        this.cropShowCircle = in.readByte() != 0;
        this.cropColor = in.readInt();
        this.isCompress = in.readByte() != 0;
        this.compressRank = in.readInt();
        this.selectMode = in.readInt();
    }

    public static final Creator<PhotoOptionData> CREATOR = new Creator<PhotoOptionData>() {
        @Override
        public PhotoOptionData createFromParcel(Parcel source) {
            return new PhotoOptionData(source);
        }

        @Override
        public PhotoOptionData[] newArray(int size) {
            return new PhotoOptionData[size];
        }
    };
}
