package com.ashlikun.photo_hander.compress.luban;

import java.io.IOException;
import java.io.InputStream;

/**
 * 通过此接口获取输入流，以兼容文件、FileProvider方式获取到的图片
 */
public interface InputStreamProvider {

    InputStream open() throws IOException;

    void close();

    String getPath();

    /**
     * 是否是网络图
     *
     * @return
     */
    boolean isHttpImg();

    /**
     * 希望的大小 kb
     *
     * @return
     */
    int expectSize();

    /**
     * 希望的大小 kb
     */
    void setExpectSize(int expectSize);
}
