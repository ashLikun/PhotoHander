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
    private boolean isNeedClean = false;
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

    public void clean() {
        if (isNeedClean) {
            isNeedClean = false;
            mFile.clear();
            mFolder.clear();
        }
    }

    public void loader() {
        isNeedClean = true;
        for (int i = 0; i < absMediaScanners.size(); i++) {
            PhotoThreadUtils.get().execute(absMediaScanners.get(i));
        }
    }

    public void destroyLoader() {
        for (int i = 0; i < absMediaScanners.size(); i++) {
//            absMediaScanners.get(i).(i + 1);
        }
    }


    protected MediaFolder getFolderById(int folderId) {
        if (mFolder != null) {
            for (MediaFolder folder : mFolder) {
                if (folder.folderId == folderId) {
                    return folder;
                }
            }
        }
        return null;
    }

    protected MediaFile getFileByPath(String path) {
        if (mFile != null) {
            for (MediaFile file : mFile) {
                if (file.path.equals(path)) {
                    return file;
                }
            }
        }
        return null;
    }

    protected MediaFile getFileByPath(List<MediaFile> files, String path) {
        if (files != null) {
            for (MediaFile file : files) {
                if (file.path.equals(path)) {
                    return file;
                }
            }
        }
        return null;
    }

    private void addFile(List<MediaFile> mediaFiles) {
        for (MediaFile f : mediaFiles) {
            MediaFile oldf = getFileByPath(f.path);
            if (oldf == null) {
                mFile.add(f);
            }
            if (f.isVideo()) {
                MediaFile oldVideo = getFileByPath(mVideoFile, f.path);
                if (oldVideo == null) {
                    mVideoFile.add(f);
                }
            }
        }
    }

    private void addFolder(List<MediaFolder> folderDatas) {
        for (MediaFolder f : folderDatas) {
            MediaFolder oldf = getFolderById(f.folderId);
            if (oldf != null) {
                for (MediaFile fff : f.mediaFileList) {
                    MediaFile oldFile = getFileByPath(oldf.mediaFileList, fff.path);
                    if (oldFile == null) {
                        oldf.mediaFileList.add(fff);
                    }
                }
                Collections.sort(oldf.mediaFileList);
            } else {
                mFolder.add(f);
            }
        }
    }

    private void initImageScanner() {
        ImageScanner imageScanner = new ImageScanner(fragmentActivity, new AbsMediaScanner.OnLoadFinished() {
            @Override
            public void onLoadFinished(List<MediaFile> datas, List<MediaFolder> folderDatas) {
                clean();
                addFile(datas);
                addFolder(folderDatas);
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
                clean();
                addFile(datas);
                addFolder(folderDatas);
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
        if(PhotoOptionData.currentData == null) return;
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

