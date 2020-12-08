package com.ashlikun.photo_hander.compress.luban;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class Luban implements Handler.Callback {
    private static final String TAG = "Luban";
    private static final String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";
    /**
     * 缓存文件大于500个自动删除
     */
    private static final int MAX_SAVE_FILS = 500;
    private static final int MSG_COMPRESS_SUCCESS = 0;
    private static final int MSG_COMPRESS_START = 1;
    private static final int MSG_COMPRESS_ERROR = 2;
    private static final int MSG_COMPRESS_LOADDING = 3;

    private File mTargetDir;
    private boolean focusAlpha;
    private OnRenameListener mRenameListener;
    private OnCompressListener mCompressListener;
    private CompressionPredicate mCompressionPredicate;
    private List<InputStreamProvider> mStreamProviders;

    private Handler mHandler;

    private Luban(Builder builder) {
        this.mTargetDir = builder.mTargetDir == null ? getCacheDir(builder.context) :
                new File(builder.mTargetDir);
        this.mRenameListener = builder.mRenameListener;
        this.mStreamProviders = builder.mStreamProviders;
        this.mCompressListener = builder.mCompressListener;
        this.mCompressionPredicate = builder.mCompressionPredicate;
        for (InputStreamProvider pp : mStreamProviders) {
            pp.setExpectSize(builder.expectSize);
        }
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    /**
     * 获取luban的默认缓存目录
     */
    public static synchronized File getCacheDir(Context context) {
        return PhotoHanderUtils.getPhotoCacheDir(context, Luban.DEFAULT_DISK_CACHE_DIR);
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }


    public static void deleteDir(File cacheDir) {
        if (cacheDir != null) {
            if (cacheDir.exists()) {
                if (PhotoHanderUtils.getFileCount(cacheDir) > MAX_SAVE_FILS) {
                    cacheDir.delete();
                }
            }
        }
    }

    /**
     * 创建缓存文件
     */
    public File createTempFile(Context context, InputStreamProvider path) {
        deleteDir(mTargetDir);
        if (!mTargetDir.exists()) {
            mTargetDir = getCacheDir(context);
        }
        try {
            File file = new File(mTargetDir, Math.abs(path.getPath().hashCode()) + Checker.SINGLE.extSuffix(path));
            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File createTempCustomFile(Context context, String filename) {
        deleteDir(mTargetDir);
        if (!mTargetDir.exists()) {
            mTargetDir = getCacheDir(context);
        }
        String cacheBuilder = mTargetDir + "/" + filename;
        return new File(cacheBuilder);
    }

    /**
     * 获取文件夹中已经存在的文件
     */
    public File getTempFile(InputStreamProvider input) {
        String tempPath = mTargetDir + File.separator +
                Math.abs(input.getPath().hashCode()) + Checker.SINGLE.extSuffix(input);
        File file = new File(tempPath);
        if (file.exists()) {
            return file;
        }
        return null;
    }



    /**
     * start asynchronous compress thread
     */
    private void launch(final Context context) {
        if (mStreamProviders == null || mStreamProviders.size() == 0 && mCompressListener != null) {
            mCompressListener.onError(new NullPointerException("image file cannot be null"));
        }
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<CompressResult> compressFiles = new ArrayList();
                final Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();
                int position = 0;
                int size = mStreamProviders.size();
                while (iterator.hasNext()) {
                    position++;
                    final InputStreamProvider path = iterator.next();
                    if (!PhotoHanderUtils.isHttpImg(path.getPath())) {
                        try {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));
                            File result = compress(context, path);
                            compressFiles.add(new CompressResult(path, result.getAbsolutePath(), true, false));
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_LOADDING, new Pair(position, size)));
                        } catch (IOException e) {
                            //错误
                            compressFiles.add(new CompressResult(path, path.getPath(), false, true));
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e));
                        }
                    } else {
                        //如果是网络图直接跳过
                        compressFiles.add(new CompressResult(path, path.getPath(), false, false));
                    }
                }
                mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, compressFiles));

            }
        });
    }

    /**
     * start compress and return the file
     */
    private File get(InputStreamProvider input, Context context) throws IOException {
        try {
            return new Engine(input, focusAlpha).compress(createTempFile(context, input));
        } finally {
            input.close();
        }
    }

    private List<File> get(Context context) throws IOException {
        List<File> results = new ArrayList<>();
        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();

        while (iterator.hasNext()) {
            results.add(compress(context, iterator.next()));
            iterator.remove();
        }

        return results;
    }

    private File compress(Context context, InputStreamProvider path) throws IOException {
        try {
            return compressReal(context, path);
        } finally {
            path.close();
        }
    }

    private File compressReal(Context context, InputStreamProvider path) throws IOException {
        //是否存在缓存
        File tempFile = getTempFile(path);
        if (tempFile != null) {
            return tempFile;
        }
        //必须先执行计算
        Engine engine = new Engine(path, focusAlpha);
        //是否忽略
        boolean isNeedCompress = mCompressionPredicate != null ? mCompressionPredicate.apply(path.getPath()) && Checker.SINGLE.needCompress(path.expectSize(), path.getPath()) :
                Checker.SINGLE.needCompress(path.expectSize(), path.getPath());

        File result;
        if (isNeedCompress) {
            File outFile = createTempFile(context, path);

            if (mRenameListener != null) {
                String filename = mRenameListener.rename(path.getPath());
                outFile = createTempCustomFile(context, filename);
            }
            result = engine.compress(outFile);
        } else {
            result = new File(path.getPath());
        }
        return result;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (mCompressListener == null) {
            return false;
        }
        switch (msg.what) {
            case MSG_COMPRESS_START:
                mCompressListener.onStart();
                break;
            case MSG_COMPRESS_SUCCESS:
                mCompressListener.onSuccess((ArrayList<CompressResult>) msg.obj);
                break;
            case MSG_COMPRESS_ERROR:
                mCompressListener.onError((Throwable) msg.obj);
                break;
            case MSG_COMPRESS_LOADDING:
                Pair<Integer, Integer> obj = (Pair<Integer, Integer>) msg.obj;
                mCompressListener.onLoading(obj.first, obj.second);
                break;
        }
        return false;
    }

    public static class Builder {
        private Context context;
        private String mTargetDir;
        private boolean focusAlpha;
        //期望的大小KB
        private int expectSize;
        private OnRenameListener mRenameListener;
        private OnCompressListener mCompressListener;
        private CompressionPredicate mCompressionPredicate;
        private List<InputStreamProvider> mStreamProviders;

        Builder(Context context) {
            this.context = context;
            this.mStreamProviders = new ArrayList<>();
        }

        private Luban build() {
            return new Luban(this);
        }

        public Builder load(InputStreamProvider inputStreamProvider) {
            mStreamProviders.add(inputStreamProvider);
            return this;
        }

        public Builder load(final File file) {
            mStreamProviders.add(new InputStreamAdapter.InputStreamFileAdapter(file));
            return this;
        }

        public Builder load(final String string) {
            mStreamProviders.add(new InputStreamAdapter.InputStreamStringAdapter(string));
            return this;
        }

        public <T> Builder load(List<T> list) {
            for (T src : list) {
                if (src instanceof String) {
                    load((String) src);
                } else if (src instanceof File) {
                    load((File) src);
                } else if (src instanceof Uri) {
                    load((Uri) src);
                } else {
                    throw new IllegalArgumentException("Incoming data type exception, it must be String, File, Uri or Bitmap");
                }
            }
            return this;
        }

        public Builder load(final Uri uri) {
            mStreamProviders.add(new InputStreamAdapter.InputStreamUriAdapter(context, uri));
            return this;
        }

        public Builder setRenameListener(OnRenameListener listener) {
            this.mRenameListener = listener;
            return this;
        }

        public Builder setCompressListener(OnCompressListener listener) {
            this.mCompressListener = listener;
            return this;
        }

        public Builder setTargetDir(String targetDir) {
            this.mTargetDir = targetDir;
            return this;
        }

        /**
         * Do I need to keep the image's alpha channel
         *
         * @param focusAlpha <p> true - to keep alpha channel, the compress speed will be slow. </p>
         *                   <p> false - don't keep alpha channel, it might have a black background.</p>
         */
        public Builder setFocusAlpha(boolean focusAlpha) {
            this.focusAlpha = focusAlpha;
            return this;
        }

        /**
         * 期望压缩到多少
         *
         * @param size 默认动态计算
         */
        public Builder expectSize(int size) {
            this.expectSize = size;
            return this;
        }

        /**
         * do compress image when return value was true, otherwise, do not compress the image file
         *
         * @param compressionPredicate A predicate callback that returns true or false for the given input path should be compressed.
         */
        public Builder filter(CompressionPredicate compressionPredicate) {
            this.mCompressionPredicate = compressionPredicate;
            return this;
        }


        /**
         * begin compress image with asynchronous
         */
        public void launch() {
            build().launch(context);
        }

        public File get(final String path) throws IOException {
            return build().get(new InputStreamAdapter.InputStreamStringAdapter(path), context);
        }

        /**
         * begin compress image with synchronize
         *
         * @return the thumb image file list
         */
        public List<File> get() throws IOException {
            return build().get(context);
        }
    }
}