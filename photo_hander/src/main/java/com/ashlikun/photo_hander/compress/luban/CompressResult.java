package com.ashlikun.photo_hander.compress.luban;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/4/24　14:51
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：压缩后的结果
 */
public class CompressResult {
    //压缩之前的参数
    public InputStreamProvider provider;
    //压缩后的文件
    public String compressPath;
    //是否压缩（不是网络图，缓存没有，文件太小）
    public boolean isCompress;
    //是否压缩失败
    public boolean isComparessError = false;

    public CompressResult(InputStreamProvider provider, String compressPath) {
        this.provider = provider;
        this.compressPath = compressPath;
    }

    public CompressResult(InputStreamProvider provider, String compressPath, boolean isCompress) {
        this.provider = provider;
        this.compressPath = compressPath;
        this.isCompress = isCompress;
    }

    public CompressResult(InputStreamProvider provider, String compressPath, boolean isCompress, boolean isComparessError) {
        this.provider = provider;
        this.compressPath = compressPath;
        this.isCompress = isCompress;
        this.isComparessError = isComparessError;
    }
}
