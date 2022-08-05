package com.ashlikun.photo_hander.compress.video;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;

import com.ashlikun.photo_hander.PhotoOptionData;
import com.ashlikun.photo_hander.bean.MediaSelectData;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.ashlikun.photo_hander.utils.PhotoThreadUtils;
import com.hw.videoprocessor.VideoProcessor;
import com.hw.videoprocessor.util.VideoProgressListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/12/8　11:32
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：视频压缩
 */
public class VideoCompress {
    public static final String DEFAULT_DISK_CACHE_DIR = "videocompress_disk_cache";
    /**
     * 缓存文件大于200M文件个自动删除
     */
    private static final int MAX_SAVE_FILS_SIZE = 200 * 1024 * 1024;
    Context context;
    boolean isStop = false;
    private List<MediaSelectData> dataList;
    private File mTargetDir;
    private PhotoOptionData optionData;

    //进度 1个文件100,2个文件200。。。
    private List<Integer> mProgress = new ArrayList<>();
    private SoftReference<PhotoHandleVideoProgressListener> mListener;

    public VideoCompress(Context context, List<MediaSelectData> data, PhotoOptionData optionData, PhotoHandleVideoProgressListener listener) {
        this.context = context.getApplicationContext();
        this.dataList = data;
        this.mListener = new SoftReference<>(listener);
        mTargetDir = getCacheDir(context);
        this.optionData = optionData;
    }

    public List<MediaSelectData> getDataList() {
        return dataList;
    }

    public void stop() {
        isStop = true;
    }

    public void start() {
        isStop = false;
        mProgress.clear();
        for (int i = 0; i < dataList.size(); i++) {
            MediaSelectData data = dataList.get(i);
            if (data.isVideo()) {
                mProgress.add(0);
            }
        }
        PhotoThreadUtils.get().execute(new MRunable());
    }

    /**
     * 获取文件夹中已经存在的文件
     */
    public String getTempFile(MediaSelectData input) {
        String tempPath = mTargetDir + File.separator +
                Math.abs(input.mediaFile.path.hashCode()) + input.mediaFile.mime.replace("video/", ".");
        File file = new File(tempPath);
        if (file.exists()) {
            return file.getPath();
        }
        return null;
    }

    /**
     * 创建缓存文件
     */
    public File createTempFile(MediaSelectData input) {
        deleteDir(mTargetDir);
        if (!mTargetDir.exists()) {
            mTargetDir = getCacheDir(context);
        }
        try {
            File file = new File(mTargetDir, Math.abs(input.originPath().hashCode()) + input.mediaFile.mime.replace("video/", "."));
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
                if (PhotoHanderUtils.getFileSizes(cacheDir) > MAX_SAVE_FILS_SIZE) {
                    cacheDir.delete();
                }
            }
        }
    }

    private Point getVideoWidthAspect(int originWidth, int originHeight) {
        if (!optionData.isVideoCompressAspectRatio) {
            return new Point(originWidth, originHeight);
        }
        int bili = 1;
        Point point = new Point();
        if (originHeight > originWidth) {
            if (originHeight >= 3840) {//4K
                bili = 4;
            } else if (originHeight > 320) {
                bili = 2;
            } else {
                bili = 1;
            }
            point.y = originHeight / bili;
            point.x = (int) (originWidth / (originHeight * 1f) * point.y);
        } else {
            if (originWidth >= 3840) {//4K
                bili = 4;
            } else if (originWidth > 320) {
                bili = 2;
            } else {
                bili = 1;
            }
            point.x = originWidth / bili;
            point.y = (int) (originHeight / (originWidth * 1f) * point.x);
        }
        return point;
    }

    private class MyVideoProgressListener implements VideoProgressListener {
        MediaSelectData data;
        int index;

        public MyVideoProgressListener(MediaSelectData data, int index) {
            this.data = data;
            this.index = index;
        }

        @Override
        public void onProgress(float progress) {
            int intProgress = (int) (progress * 100);
            mProgress.set(index, intProgress);
            int allPro = mProgress.size() * 100;
            int curr = 0;
            for (Integer pp : mProgress) {
                curr += pp;
            }
            final int zhenshiPro = (int) (curr / (allPro * (1f)) * 100);
            if (zhenshiPro < 100) {
                PhotoThreadUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener.get() != null) {
                            mListener.get().onProgress(zhenshiPro, VideoCompress.this);
                        }
                    }
                });
            }
        }
    }

    private class MRunable implements Runnable {
        @Override
        public void run() {
            int index = 0;
            for (int i = 0; i < dataList.size(); i++) {
                if (isStop) {
                    return;
                }
                MediaSelectData data = dataList.get(i);
                if (data.isVideo()) {
                    String videoOutCompressPath = "";
                    MyVideoProgressListener listener = new MyVideoProgressListener(data, index);
                    try {
                        String filePath = getTempFile(data);
                        if (filePath != null) {
                            //已经存在了
                            listener.onProgress(1);
                            break;
                        }
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(data.originPath());
                        int originWidth = PhotoHanderUtils.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                        int originHeight = PhotoHanderUtils.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                        int bitrate = PhotoHanderUtils.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
                        int frameRate = (int) PhotoHanderUtils.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));
                        Point aspect = getVideoWidthAspect(originWidth, originHeight);
                        videoOutCompressPath = createTempFile(data).getPath();
                        data.compressPath = videoOutCompressPath;
                        int frameRateO = (Math.min(frameRate <= 24 ? 30 : frameRate, optionData.videoCompressFps));
                        int bitrateO = aspect.x * aspect.y * ((frameRateO + 9) / 10);
                        VideoProcessor.processor(context)
                                .input(data.originPath())
                                .outWidth(aspect.x)
                                .bitrate(bitrateO)
                                .outHeight(aspect.y)
                                .frameRate(frameRateO)
                                .output(videoOutCompressPath)
                                .progressListener(listener)
                                .process();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (!videoOutCompressPath.isEmpty()) {
                            new File(videoOutCompressPath).delete();
                        }
                        listener.onProgress(1);
                    }
                }
                index++;
            }
            PhotoThreadUtils.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener.get() != null) {
                        mListener.get().onProgress(100, VideoCompress.this);
                    }
                }
            });
        }
    }

    public interface PhotoHandleVideoProgressListener {
        void onProgress(int progress, VideoCompress videoCompress);
    }

    /**
     * 获取luban的默认缓存目录
     */
    public static synchronized File getCacheDir(Context context) {
        return PhotoHanderUtils.getPhotoCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

}
