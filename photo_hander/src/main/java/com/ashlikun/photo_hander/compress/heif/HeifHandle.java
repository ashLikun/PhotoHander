package com.ashlikun.photo_hander.compress.heif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ashlikun.photo_hander.PhotoHanderConst;
import com.ashlikun.photo_hander.bean.MediaSelectData;
import com.ashlikun.photo_hander.compress.luban.InputStreamAdapter;
import com.ashlikun.photo_hander.compress.luban.InputStreamProvider;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.ashlikun.photo_hander.utils.PhotoThreadUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2023/10/9　13:15
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：Heif 格式转成jpg格式
 */
public class HeifHandle {
    /**
     * 缓存文件大于100个自动删除
     */
    private static final int MAX_SAVE_FILS = 100;
    private static final String DEFAULT_DISK_CACHE_DIR = "heif_disk_cache";
    private SoftReference<PhotoHandleHeifProgressListener> mListener;
    private List<MediaSelectData> dataList;
    private Context context;

    private File mTargetDir = null;

    public HeifHandle(Context context, List<MediaSelectData> dataList, PhotoHandleHeifProgressListener listener) {
        this.context = context;
        this.mListener = new SoftReference<>(listener);
        this.dataList = dataList;
        mTargetDir = PhotoHanderUtils.getPhotoCacheDir(context, HeifHandle.DEFAULT_DISK_CACHE_DIR);
    }

    public void start() {
        if (dataList.isEmpty()) {
            mListener.get().onProgress(100, HeifHandle.this);
            return;
        }

        PhotoThreadUtils.get().execute(new MRunable());
    }

    public List<MediaSelectData> getDataList() {
        return dataList;
    }

    /**
     * 转码一个图片
     */
    private void transcoding(InputStreamProvider srcImg, File tagImg) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //转码图片
            tagBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            tagBitmap.recycle();
            FileOutputStream fos = new FileOutputStream(tagImg);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String extSuffix(InputStreamProvider input) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input.open(), null, options);
            if (PhotoHanderConst.MIME_IMAGE_HEIF.equalsIgnoreCase(options.outMimeType) || PhotoHanderConst.MIME_IMAGE_HEIC.equalsIgnoreCase(options.outMimeType)) {
                return PhotoHanderConst.JPG;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取文件夹中已经存在的文件
     */
    private File getTempFile(MediaSelectData mediaSelectData) {
        String tempPath = mTargetDir + File.separator +
                Math.abs(mediaSelectData.mediaFile.path.hashCode()) + PhotoHanderConst.JPG;
        File file = new File(tempPath);
        if (file.exists() && file.length() > 0) {
            return file;
        }
        return null;
    }

    /**
     * 创建缓存文件
     */
    public File createTempFile(MediaSelectData mediaSelectData) {
        deleteDir(mTargetDir);
        if (!mTargetDir.exists()) {
            mTargetDir = PhotoHanderUtils.getPhotoCacheDir(context, HeifHandle.DEFAULT_DISK_CACHE_DIR);
        }
        try {
            File file = new File(mTargetDir, Math.abs(mediaSelectData.mediaFile.path.hashCode()) + PhotoHanderConst.JPG);
            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    private class MRunable implements Runnable {

        @Override
        public void run() {
            int count = 0;
            for (MediaSelectData mediaSelectData : dataList) {
                count++;
                InputStreamProvider inputStreamProvider = new InputStreamAdapter.InputStreamStringAdapter(mediaSelectData.mediaFile.path);
                String extSuffix = extSuffix(inputStreamProvider);
                if (extSuffix == null || extSuffix.isEmpty()) {
                    //非heif格式
                    continue;
                }
                //是否存在缓存
                File tempFile = getTempFile(mediaSelectData);
                if (tempFile != null) {
                    //使用缓存
                    mediaSelectData.convertPath = tempFile.getPath();
                } else {
                    //创建对应的新文件
                    File newFile = createTempFile(mediaSelectData);
                    //写入数据
                    transcoding(inputStreamProvider, newFile);
                    //设置字段
                    mediaSelectData.convertPath = newFile.getPath();
                }
                //执行完成，主线程回调
                final int finalCount = count;
                PhotoThreadUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener.get() != null) {
                            mListener.get().onProgress((int) ((((float) finalCount) / dataList.size()) * 100), HeifHandle.this);
                        }
                    }
                });
            }

            //执行完成，主线程回调
            PhotoThreadUtils.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener.get() != null) {
                        mListener.get().onEnd(HeifHandle.this);
                    }
                }
            });
        }
    }

    public interface PhotoHandleHeifProgressListener {
        void onProgress(int progress, HeifHandle heifHandle);

        void onEnd(HeifHandle heifHandle);
    }
}
