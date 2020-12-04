package com.ashlikun.photo_hander.loader;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentActivity;

import com.ashlikun.photo_hander.PhotoOptionData;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaFolder;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.util.ArrayList;

/**
 * @author　　: 李坤
 * 创建时间: 2020/12/3 17:53
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍： 媒体库扫描类(图片)
 */

public class ImageScanner extends AbsMediaScanner<MediaFile, MediaFolder> {
    public ImageScanner(FragmentActivity context, OnLoadFinished onLoadFinished) {
        super(context, onLoadFinished);
    }

    @Override
    protected Uri getScanUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String[] getProjection() {
        return new String[]{
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
        };
    }

    @Override
    protected String getSelection() {
        if (PhotoOptionData.currentData.isFilterGif()) {
            //过滤GIF
            return MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";
        }
        return MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?" + " or " + MediaStore.Images.Media.MIME_TYPE + "=?";
    }

    @Override
    protected String[] getSelectionArgs() {
        if (PhotoOptionData.currentData.isFilterGif()) {
            //过滤GIF
            return new String[]{"image/jpeg", "image/png"};
        }
        return new String[]{"image/jpeg", "image/png", "image/gif"};
    }

    @Override
    protected String getOrder() {
        return MediaStore.Images.Media.DATE_ADDED + " desc";
    }

    /**
     * 构建媒体对象
     *
     * @param cursor
     * @return
     */
    @Override
    protected MediaFile parse(Cursor cursor) {
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
        String mime = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
        Integer folderId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
        String folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
        long dateToken = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));

        MediaFile mediaFile = new MediaFile(path);
        mediaFile.name = name;
        mediaFile.mime = mime;
        mediaFile.folderId = folderId;
        mediaFile.folderName = PhotoHanderUtils.getFolderName(folderName);
        mediaFile.dateToken = dateToken;
        return mediaFile;
    }


    @Override
    public void queryMedia() {
        super.queryMedia();
        onLoadFinished.onLoadFinished(mDatas, mResultFolder);
    }

    @Override
    protected MediaFolder parseFolder(MediaFile mediaFile) {
        if (!hasFolderGened) {
            // 获取目录数据
            MediaFolder f = MediaFolder.getFolderByName(mResultFolder, mediaFile.folderName);
            if (f == null) {
                MediaFolder folder = new MediaFolder(mediaFile.folderId, mediaFile.folderName, mediaFile, new ArrayList<MediaFile>());
                folder.mediaFileList.add(mediaFile);
                return folder;
            } else {
                f.mediaFileList.add(mediaFile);
            }
        }
        return null;
    }

}
