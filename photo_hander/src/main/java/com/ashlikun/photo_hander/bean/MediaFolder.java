package com.ashlikun.photo_hander.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件夹
 */
public class MediaFolder implements Comparable<MediaFolder> {

    public int folderId;
    public String folderName;
    public MediaFile folderCover;
    public ArrayList<MediaFile> mediaFileList;

    public MediaFolder(int folderId, String folderName, MediaFile folderCover, ArrayList<MediaFile> mediaFileList) {
        this.folderId = folderId;
        this.folderName = folderName;
        this.folderCover = folderCover;
        this.mediaFileList = mediaFileList;
    }

    @Override
    public boolean equals(Object o) {
        try {
            MediaFolder other = (MediaFolder) o;
            return other.folderId == folderId;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    @Override
    public int compareTo(MediaFolder o) {
        if (o.folderCover.dateToken > folderCover.dateToken) {
            return 1;
        } else if (o.folderCover.dateToken < folderCover.dateToken) {
            return -1;
        }
        return 0;
    }

    public static MediaFolder getFolderById(List<MediaFolder> folders, int folderId) {
        if (folders != null) {
            for (MediaFolder folder : folders) {
                if (folder.folderId == folderId) {
                    return folder;
                }
            }
        }
        return null;
    }

    public static MediaFolder getFolderByName(List<MediaFolder> folders, String folderName) {
        if (folders != null) {
            for (MediaFolder folder : folders) {
                if (TextUtils.equals(folder.folderName, folderName)) {
                    return folder;
                }
            }
        }
        return null;
    }
}
