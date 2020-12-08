package com.ashlikun.photo_hander.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.ashlikun.photo_hander.R;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.ashlikun.photoview.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　16:55
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class LookFragmentAdapter extends PagerAdapter {
    /**
     * 原数据
     */
    private ArrayList<MediaFile> listDatas;
    private LayoutInflater mInflater;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    RequestOptions options;

    public LookFragmentAdapter(Context context, ArrayList<MediaFile> listDatas) {
        this.listDatas = listDatas;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        options = new RequestOptions().placeholder(R.drawable.ph_default_error)
                .override(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    @Override
    public int getCount() {
        return listDatas == null ? 0 : listDatas.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        MediaFile data = getItemData(position);
        View frameLayout = mInflater.inflate(R.layout.ph_list_item_lock_image, null);
        container.addView(frameLayout);
        upDataImageView(frameLayout, data);
        return frameLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    /**
     * 填充数据
     */
    private void upDataImageView(View view, final MediaFile data) {
        final PhotoView imageView = view.findViewById(R.id.ph_photoView);
        final ImageView ph_videoPlay = view.findViewById(R.id.ph_videoPlay);
        if (data.isVideo()) {
            ph_videoPlay.setVisibility(View.VISIBLE);
            ph_videoPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PhotoHanderUtils.startVideoPlay(v.getContext(), data);
                }
            });
        } else {
            ph_videoPlay.setVisibility(View.GONE);
        }
        // 显示图片
        Glide.with(imageView.getContext())
                .load(data.path)
                .apply(options)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition transition) {
                        if (resource.getIntrinsicHeight() > displayMetrics.heightPixels) {
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        imageView.setImageDrawable(resource);
                    }
                });
    }


    public MediaFile getItemData(int position) {
        if (position >= 0 && position < listDatas.size()) {
            return listDatas.get(position);
        }
        return null;
    }


    public int indexOf(MediaFile data) {
        return listDatas.indexOf(data);
    }

    public void resetView(View childAt) {
        if (childAt != null && childAt instanceof ViewGroup) {
            View view = childAt.findViewWithTag(1);
            if (view != null && view instanceof PhotoView) {
                ((PhotoView) view).update();
            }
        }
    }
}
