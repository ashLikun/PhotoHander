package com.ashlikun.photo_hander.loader;

import androidx.fragment.app.FragmentActivity;

import com.ashlikun.photo_hander.PhotoOptionData;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaFolder;
import com.ashlikun.photo_hander.utils.PhotoThreadUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/12/4　16:06
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class MediaHandler {
    private List<AbsMediaScanner> absMediaScanners = new ArrayList<>();
    private FragmentActivity fragmentActivity;
    /**
     * 可选目录的列表数据
     */
    protected ArrayList<MediaFolder> mFolder = new ArrayList<>();
    /**
     * 文件集合
     */
    protected ArrayList<MediaFile> mFile = new ArrayList<>();
    protected ArrayList<MediaFile> mVideoFile = new ArrayList<>();
    private boolean isImageLoad = false;
    private boolean isVideoLoad = false;
    protected AbsMediaScanner.OnLoadFinished onLoadFinished;

    public MediaHandler(FragmentActivity fragmentActivity, AbsMediaScanner.OnLoadFinished onLoadFinished) {
        this.fragmentActivity = fragmentActivity;
        this.onLoadFinished = onLoadFinished;
        if (!PhotoOptionData.currentData.isVideoOnly) {
            initImageScanner();
        }
        if (PhotoOptionData.currentData.isCanVideo()) {
            initVideoScanner();
        }
    }

    public List<MediaFolder> getFolder() {
        return mFolder;
    }

    public List<MediaFile> getFiles() {
        return mFile;
    }

    public List<MediaFile> getVideoFiles() {
        return mVideoFile;
    }


    public void loader() {
        for (int i = 0; i < absMediaScanners.size(); i++) {
            PhotoThreadUtils.get().execute(absMediaScanners.get(i));
        }
    }

    public void destroyLoader() {
        for (int i = 0; i < absMediaScanners.size(); i++) {
//            absMediaScanners.get(i).(i + 1);
        }
    }


    private void initImageScanner() {
        ImageScanner imageScanner = new ImageScanner(fragmentActivity, new AbsMediaScanner.OnLoadFinished() {
            @Override
            public void onLoadFinished(List<MediaFile> datas, List<MediaFolder> folderDatas) {
                mFile = (ArrayList<MediaFile>) datas;
                mFolder = (ArrayList<MediaFolder>) folderDatas;
                sort();
                isImageLoad = true;
                onLoadFinishedCallback();
            }
        });
        absMediaScanners.add(imageScanner);
    }


    private void initVideoScanner() {
        VideoScanner videoScanner = new VideoScanner(fragmentActivity, new AbsMediaScanner.OnLoadFinished() {
            @Override
            public void onLoadFinished(List<MediaFile> datas, List<MediaFolder> folderDatas) {
                mFile = (ArrayList<MediaFile>) datas;
                mFolder = (ArrayList<MediaFolder>) folderDatas;
                sort();
                isVideoLoad = true;
                onLoadFinishedCallback();
            }

        });
        absMediaScanners.add(videoScanner);
    }

    private void sort() {
        Collections.sort(mFile);
        Collections.sort(mFolder);
    }

    private void onLoadFinishedCallback() {
        if (PhotoOptionData.currentData.isVideoOnly) {
            if (isVideoLoad) {
                PhotoThreadUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        onLoadFinished.onLoadFinished(mFile, mFolder);
                    }
                });
            }
        } else if (!PhotoOptionData.currentData.isSelectVideo) {
            if (isImageLoad) {
                PhotoThreadUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        onLoadFinished.onLoadFinished(mFile, mFolder);
                    }
                });
            }
        } else {
            if (isImageLoad && isVideoLoad) {
                PhotoThreadUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        onLoadFinished.onLoadFinished(mFile, mFolder);
                    }
                });
            }
        }
    }


}
