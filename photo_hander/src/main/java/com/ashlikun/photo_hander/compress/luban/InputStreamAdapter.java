package com.ashlikun.photo_hander.compress.luban;

import android.content.Context;
import android.net.Uri;

import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Automatically close the previous InputStream when opening a new InputStream,
 * and finally need to manually call {@link #close()} to release the resource.
 */
public abstract class InputStreamAdapter implements InputStreamProvider {

    private InputStream inputStream;
    int expectSize = 0;

    @Override
    public InputStream open() throws IOException {
        close();
        inputStream = openInternal();
        return inputStream;
    }

    @Override
    public int expectSize() {
        return expectSize;
    }

    @Override
    public void setExpectSize(int expectSize) {
        this.expectSize = expectSize;
    }

    public abstract InputStream openInternal() throws IOException;

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignore) {
            } finally {
                inputStream = null;
            }
        }
    }

    public static class InputStreamStringAdapter extends InputStreamAdapter {

        String filePath;

        public InputStreamStringAdapter(String filePath) {
            this.filePath = filePath;
        }

        public InputStreamStringAdapter(String filePath, int expectSize) {
            this.filePath = filePath;
            this.expectSize = expectSize;
        }

        @Override
        public InputStream openInternal() throws IOException {
            return new FileInputStream(filePath);
        }

        @Override
        public String getPath() {
            return filePath;
        }

        @Override
        public boolean isHttpImg() {
            return PhotoHanderUtils.isHttpImg(getPath());
        }


    }

    public static class InputStreamFileAdapter extends InputStreamAdapter {

        File filePath;


        public InputStreamFileAdapter(File filePath) {
            this.filePath = filePath;
        }

        public InputStreamFileAdapter(File filePath, int expectSize) {
            this.filePath = filePath;
            this.expectSize = expectSize;
        }

        @Override
        public InputStream openInternal() throws IOException {
            return new FileInputStream(filePath);
        }

        @Override
        public String getPath() {
            return filePath.getAbsolutePath();
        }

        @Override
        public boolean isHttpImg() {
            return PhotoHanderUtils.isHttpImg(getPath());
        }


    }

    public static class InputStreamUriAdapter extends InputStreamAdapter {
        Context context;
        Uri uri;

        public InputStreamUriAdapter(Context context, Uri uri) {
            this.context = context;
            this.uri = uri;
        }

        public InputStreamUriAdapter(Context context, Uri uri, int expectSize) {
            this.context = context;
            this.uri = uri;
            this.expectSize = expectSize;
        }

        @Override
        public InputStream openInternal() throws IOException {
            return context.getContentResolver().openInputStream(uri);
        }

        @Override
        public String getPath() {
            return uri.getPath();
        }

        @Override
        public boolean isHttpImg() {
            return PhotoHanderUtils.isHttpImg(getPath());
        }

    }
}

