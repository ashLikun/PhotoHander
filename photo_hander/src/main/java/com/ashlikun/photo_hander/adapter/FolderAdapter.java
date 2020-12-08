package com.ashlikun.photo_hander.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.photo_hander.PhotoOptionData;
import com.ashlikun.photo_hander.R;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaFolder;
import com.ashlikun.photo_hander.loader.MediaHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 13:23
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：文件夹Adapter
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    /**
     * 数据来源
     */
    private MediaHandler mediaHandler;
    int mImageSize;

    int lastSelected = 0;

    public FolderAdapter(Context context, MediaHandler mediaHandler) {
        mContext = context;
        this.mediaHandler = mediaHandler;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.ph_folder_cover_size);
    }

    public List<MediaFolder> getFolders() {
        return mediaHandler.getFolder();
    }

    @Override
    public int getItemCount() {
        if (PhotoOptionData.currentData.isSelectVideoAndImg()) {
            return mediaHandler.getFolder().size() + 2;
        } else {
            return mediaHandler.getFolder().size() + 1;
        }
    }

    public MediaFolder getItem(int i) {
        if (i == 0) {
            return null;
        }
        if (PhotoOptionData.currentData.isSelectVideoAndImg()) {
            return mediaHandler.getFolder().get(i - 2);
        }
        return mediaHandler.getFolder().get(i - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.ph_list_item_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (position == 0) {
            if (PhotoOptionData.currentData.isVideoOnly) {
                holder.name.setText(R.string.ph_folder_all_video);
            } else {
                holder.name.setText(PhotoOptionData.currentData.isCanVideo() ? R.string.ph_folder_all_image_and_video : R.string.ph_folder_all);
            }
            holder.size.setText(String.format("(%d)",
                    mediaHandler.getFiles().size()));
            if (getFolders().size() > 0) {
                MediaFolder f = getFolders().get(0);
                if (f != null) {
                    Glide.with((Activity) mContext)
                            .load(new File(f.folderCover.path))
                            .apply(new RequestOptions().placeholder(R.drawable.ph_default_error)
                                    .override(mImageSize, mImageSize)
                                    .centerCrop())
                            .into(holder.cover);
                } else {
                    holder.cover.setImageResource(R.drawable.ph_default_error);
                }
            }
        } else if (PhotoOptionData.currentData.isSelectVideoAndImg() && position == 1) {
            //全部视频
            holder.name.setText(R.string.ph_folder_all_video);
            holder.size.setText(String.format("(%d)",
                    mediaHandler.getVideoFiles().size()));
            if (mediaHandler.getVideoFiles().size() > 0) {
                MediaFile f = mediaHandler.getVideoFiles().get(0);
                if (f != null) {
                    Glide.with((Activity) mContext)
                            .load(new File(f.path))
                            .apply(new RequestOptions().placeholder(R.drawable.ph_default_error)
                                    .override(mImageSize, mImageSize)
                                    .centerCrop())
                            .into(holder.cover);
                } else {
                    holder.cover.setImageResource(R.drawable.ph_default_error);
                }
            }
        } else {
            holder.bindData(getItem(position));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectIndex(position);
                notifyDataSetChanged();
                String text = holder.name.getText().toString();
                boolean showCamera = false;
                List<MediaFile> mediaFileList = null;
                if (position == 0) {
                    mediaHandler.loader();
                    showCamera = PhotoOptionData.currentData.isShowCamera;
                } else if (PhotoOptionData.currentData.isSelectVideoAndImg() && position == 1) {
                    mediaHandler.loader();
                    showCamera = false;
                } else {
                    MediaFolder folder = getItem(position);
                    if (null != folder) {
                        text = folder.folderName;
                        mediaFileList = folder.mediaFileList;
                    }
                    showCamera = false;
                }
                mImageFolderChangeListener.onImageFolderChange(position, text, showCamera, mediaFileList);
            }
        });
        if (lastSelected == position) {
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    public void setSelectIndex(int i) {
        if (lastSelected == i) {
            return;
        }

        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    public boolean isVideoMode() {
        return PhotoOptionData.currentData.isSelectVideo ? lastSelected == 1 : false;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView name;
        TextView size;
        ImageView indicator;

        ViewHolder(View view) {
            super(view);
            cover = (ImageView) view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(MediaFolder data) {
            if (data == null) {
                return;
            }
            name.setText(data.folderName);
            if (data.mediaFileList != null) {
                size.setText(String.format("(%d)", data.mediaFileList.size()));
            } else {
                size.setText("(*)");
            }

            if (data.folderCover != null) {
                File imageFile = new File(data.folderCover.path);
                if (imageFile.exists()) {
                    // 显示图片
                    Glide.with((Activity) mContext)
                            .load(imageFile)
                            .apply(new RequestOptions().placeholder(R.drawable.ph_default_error)
                                    .override(mImageSize, mImageSize)
                                    .centerCrop())
                            .into(cover);
                } else {
                    cover.setImageResource(R.drawable.ph_default_error);
                }
            } else {
                cover.setImageResource(R.drawable.ph_default_error);
            }
        }
    }

    /**
     * 接口回调，Item点击事件
     */
    private FolderAdapter.OnImageFolderChangeListener mImageFolderChangeListener;

    public void setOnImageFolderChangeListener(FolderAdapter.OnImageFolderChangeListener onItemClickListener) {
        this.mImageFolderChangeListener = onItemClickListener;
    }

    public interface OnImageFolderChangeListener {

        void onImageFolderChange(int position, String text, boolean showCamera, List<MediaFile> mediaFileList);
    }
}
