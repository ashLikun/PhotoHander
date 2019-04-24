package com.ashlikun.photo_hander.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ashlikun.photo_hander.R;
import com.ashlikun.photo_hander.bean.Image;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/17　16:43
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：查看照片底部的小图片
 */
public class MiniImageAdapter extends RecyclerView.Adapter<MiniImageAdapter.ViewHolder> {
    public static final String PLYLOAD_SELECT = "payload_select";
    private List<Image> listDatas;
    private Context context;
    private int size = 60;
    private int strokeSize = 1;
    GradientDrawable selectDrawable;
    /**
     * 当前选中的位置
     */
    private int selectPosition = -1;
    OnItemClickListener onItemClickListener;

    public MiniImageAdapter(Context context, List<Image> listDatas, OnItemClickListener onItemClickListener) {
        this.listDatas = listDatas;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        size = PhotoHanderUtils.dip2px(context, size);
        strokeSize = PhotoHanderUtils.dip2px(context, strokeSize);

        selectDrawable = new GradientDrawable();
        selectDrawable.setSize(size, size);
        selectDrawable.setStroke(strokeSize, context.getResources().getColor(R.color.ph_yulam_mini_stroke_color));
        selectDrawable.setColor(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(size, size);
        imageView.setLayoutParams(params);
        imageView.setPadding(strokeSize, strokeSize, strokeSize, strokeSize);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(position, listDatas.get(position), false);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads != null && payloads.contains(PLYLOAD_SELECT)) {
            holder.bindData(position, listDatas.get(position), true);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return listDatas == null ? 0 : listDatas.size();
    }

    /**
     * 设置item选中，不会主动更新
     *
     * @param selectItem
     * @return 返回之前选中的
     */
    public int setSelectItem(int selectItem) {
        int oldSelectPosition = selectPosition;
        this.selectPosition = selectItem;
        return oldSelectPosition;
    }

    public int getSelectItem() {
        return selectPosition;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        ViewHolder(View view) {
            super(view);
            image = (ImageView) view;
        }

        void bindData(final int position, final Image data, boolean isPayloads) {
            if (data == null) {
                return;
            }
            if (!isPayloads) {
                if (data.isHttp()) {
                    // 显示网络图片
                    Glide.with(context)
                            .load(data.path)
                            .apply(new RequestOptions().placeholder(R.drawable.ph_default_error)
                                    .override(size, size)
                                    .centerCrop())
                            .into(image);
                } else {
                    // 显示本地图片
                    File imageFile = new File(data.path);
                    if (imageFile.exists()) {
                        Glide.with(context)
                                .load(imageFile)
                                .apply(new RequestOptions().placeholder(R.drawable.ph_default_error)
                                        .override(size, size)
                                        .centerCrop())
                                .into(image);
                    } else {
                        image.setImageResource(R.drawable.ph_default_error);
                    }
                }
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(itemView, data, position);
                        }
                    }
                });
            }
            //设置选中
            if (position == selectPosition) {
                image.setBackground(selectDrawable);
            } else {
                image.setBackground(null);
            }
        }
    }

    public interface OnItemClickListener {

        /**
         * 整个Item点击回掉
         *
         * @param view
         * @param data
         * @param position
         */
        void onItemClick(View view, Image data, int position);
    }
}
