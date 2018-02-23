package com.ashlikun.photo_hander.compress;

import java.util.ArrayList;

public abstract class OnCompressListener {

    /**
     * Fired when the compression is started, override to handle in your own code
     */
    public void onStart() {

    }

    /**
     * Fired when a compression returns successfully, override to handle in your own code
     */
    public abstract void onSuccess(ArrayList<String> files);

    /**
     * Fired when a compression fails to complete, override to handle in your own code
     */
    public void onError(Throwable e) {

    }

    public void onLoading(int progress, long total) {

    }
}
