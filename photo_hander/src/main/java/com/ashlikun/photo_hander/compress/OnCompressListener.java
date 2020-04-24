package com.ashlikun.photo_hander.compress;

import java.util.ArrayList;

public interface OnCompressListener {

    /**
     * Fired when the compression is started, override to handle in your own code
     */
    void onStart();

    /**
     * Fired when a compression returns successfully, override to handle in your own code
     */
    void onSuccess(ArrayList<CompressResult> files);

    void onLoading(int progress, long total);

    /**
     * Fired when a compression fails to complete, override to handle in your own code
     */
    void onError(Throwable e);
}
