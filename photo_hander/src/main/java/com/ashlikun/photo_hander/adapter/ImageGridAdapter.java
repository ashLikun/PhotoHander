package com.ashlikun.photo_hander.adapter;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.photo_hander.R;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 13:19
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：显示图片Adapter
 */
public class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder> {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    private Context mContext;

    private LayoutInflater mInflater;
    private boolean showCamera;
    private boolean showSelectIndicator = true;
    /**
     * 追加的数据
     */
    private ArrayList<MediaFile> addList = null;
    private List<MediaFile> mImages = new ArrayList<>();
    private List<MediaFile> mSelectedImages = new ArrayList<>();
    OnItemClickListener onItemClickListener;
    final int mGridWidth;

    public ImageGridAdapter(Context context, boolean showCamera, int column) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showCamera = showCamera;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            width = size.x;
        } else {
            width = wm.getDefaultDisplay().getWidth();
        }
        mGridWidth = width / column;
    }

    /**
     * 显示选择指示器
     *
     * @param b
     */
    public void showSelectIndicator(boolean b) {
        showSelectIndicator = b;
    }

    public void setShowCamera(boolean b) {
        if (showCamera == b) {
            return;
        }

        showCamera = b;
        notifyDataSetChanged();
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    /**
     * 选择某个图片，改变选择状态
     *
     * @param image
     */
    public void select(MediaFile image) {
        if (mSelectedImages.contains(image)) {
            mSelectedImages.remove(image);
        } else {
            mSelectedImages.add(image);
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     *
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<String> resultList) {
        for (String path : resultList) {
            MediaFile image = getImageByPath(path);
            if (image != null) {
                mSelectedImages.add(image);
            }
        }
        if (mSelectedImages.size() > 0) {
            notifyDataSetChanged();
        }
    }

    private MediaFile getImageByPath(String path) {
        if (mImages != null && mImages.size() > 0) {
            for (MediaFile image : mImages) {
                if (image.path.equalsIgnoreCase(path)) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * 设置数据集
     *
     * @param images
     */
    public void setData(List<MediaFile> images) {
        mSelectedImages.clear();
        if (images != null && images.size() > 0) {
            mImages = images;
        } else {
            mImages.clear();
        }
        if (addList != null && !addList.isEmpty()) {
            mImages.addAll(0, addList);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (viewType == TYPE_CAMERA) {
            viewHolder = new ViewHolder(mInflater.inflate(R.layout.ph_list_item_camera, parent, false));
        } else {
            viewHolder = new ViewHolder(mInflater.inflate(R.layout.ph_list_item_image, parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bindData(getItem(position));
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, getItem(position), position);
                }
            });
            if (holder.indicator != null) {
                holder.indicator.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemCheckClick(v, getItem(position), position);
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera && position == 0) {
            return TYPE_CAMERA;
        }
        MediaFile file = getItem(position);
        if (file.isVideo()) {
            return TYPE_VIDEO;
        } else {
            return TYPE_IMAGE;
        }
    }


    public MediaFile getItem(int i) {
        if (showCamera) {
            if (i == 0) {
                return null;
            }
            return mImages.get(i - 1);
        } else {
            return mImages.get(i);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return showCamera ? mImages.size() + 1 : mImages.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setSelectDatas(ArrayList<MediaFile> selectDatas) {
        this.mSelectedImages = selectDatas;
        if (mSelectedImages == null) {
            mSelectedImages = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public void setAddList(ArrayList<String> addList) {
        if (addList != null) {
            this.addList = new ArrayList<>();
            for (String s : addList) {
                this.addList.add(new MediaFile(s, "http", 0));
            }
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView ph_videoDuration;
        LinearLayout videoLl;
        ImageView indicator;
        View mask;

        ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            indicator = (ImageView) view.findViewById(R.id.checkmark);
            mask = view.findViewById(R.id.mask);
            videoLl = view.findViewById(R.id.ph_videoLl);
            ph_videoDuration = view.findViewById(R.id.ph_videoDuration);
        }

        void bindData(final MediaFile data) {
            if (data == null) {
                return;
            }

            // 处理单选和多选状态
            if (showSelectIndicator) {
                indicator.setVisibility(View.VISIBLE);
                PhotoHanderUtils.setCheck(indicator, mSelectedImages.contains(data));
                mask.setVisibility(mSelectedImages.contains(data) ? View.VISIBLE : View.GONE);
            } else {
                indicator.setVisibility(View.GONE);
            }
            if (data.isHttp()) {
                // 显示网络图片
                Glide.with(mContext)
                        .load(data.path)
                        .apply(new RequestOptions().placeholder(R.drawable.ph_default_error)
                                .error(R.drawable.ph_default_error)
                                .override(mGridWidth, mGridWidth)
                                .centerCrop())
                        .into(image);
            } else {
                // 显示本地图片
                File imageFile = new File(data.path);
                if (imageFile.exists()) {

                    Glide.with(mContext)
                            .load(imageFile)
                            .apply(new RequestOptions().placeholder(R.drawable.ph_default_error)
                                    .error(R.drawable.ph_default_error)
                                    .override(mGridWidth, mGridWidth)
                                    .centerCrop())
                            .into(image);
                } else {
                    image.setImageResource(R.drawable.ph_default_error);
                }
            }
            if (data.isVideo()) {
                videoLl.setVisibility(View.VISIBLE);
                ph_videoDuration.setText(PhotoHanderUtils.getVideoDuration(data.duration));
            } else {
                videoLl.setVisibility(View.GONE);
            }
        }
    }

    public ArrayList<MediaFile> getImages() {
        return (ArrayList<MediaFile>) mImages;
    }

    public ArrayList<MediaFile> getSelectedImages() {
        return (ArrayList<MediaFile>) mSelectedImages;
    }

    public interface OnItemClickListener {
        /**
         * Check点击回掉
         *
         * @param view
         * @param data
         * @param position
         */
        void onItemCheckClick(View view, MediaFile data, int position);

        /**
         * 整个Item点击回掉，用于查看图片
         *
         * @param view
         * @param data
         * @param position
         */
        void onItemClick(View view, MediaFile data, int position);
    }
}
