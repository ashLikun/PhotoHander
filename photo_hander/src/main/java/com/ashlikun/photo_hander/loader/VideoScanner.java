package com.ashlikun.photo_hander.loader;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentActivity;

import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaFolder;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.util.ArrayList;

/**
 * @author　　: 李坤
 * 创建时间: 2020/12/3 18:04
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：媒体库扫描类(视频)
 */

public class VideoScanner extends AbsMediaScanner<MediaFile, MediaFolder> {

    public static final int ALL_IMAGES_FOLDER = -1;//全部视频


    public VideoScanner(FragmentActivity context, OnLoadFinished onLoadFinished) {
        super(context, onLoadFinished);
    }

    @Override
    protected Uri getScanUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String[] getProjection() {
        return new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED
        };
    }

    @Override
    protected String getSelection() {
        return null;
    }

    @Override
    protected String[] getSelectionArgs() {
        return null;
    }

    @Override
    protected String getOrder() {
        return MediaStore.Video.Media.DATE_ADDED + " desc";
    }

    /**
     * 构建媒体对象
     */
    @Override
    protected MediaFile parse(Cursor cursor) {
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        String mime = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
        Integer folderId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
        String folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
        long dateToken = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));

        MediaFile mediaFile = new MediaFile(path);
        mediaFile.mime = mime;
        mediaFile.folderId = folderId;
        mediaFile.folderName = PhotoHanderUtils.getFolderName(folderName);
        mediaFile.duration = duration;
        mediaFile.dateToken = dateToken;
        return mediaFile;
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


    @Override
    public void queryMedia() {
        super.queryMedia();
        onLoadFinished.onLoadFinished(mDatas, mResultFolder);
    }
}
