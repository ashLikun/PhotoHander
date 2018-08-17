package com.ashlikun.photo_hander.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ashlikun.photo_hander.R;
import com.ashlikun.photo_hander.bean.Image;
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
    private ArrayList<Image> listDatas;

    DisplayMetrics displayMetrics = new DisplayMetrics();
    RequestOptions options;

    public LookFragmentAdapter(Context context, ArrayList<Image> listDatas) {
        this.listDatas = listDatas;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        options = new RequestOptions().placeholder(R.drawable.mis_default_error)
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
        PhotoView imageView = new PhotoView(container.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        container.addView(imageView);
        upDataImageView(imageView, position);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    /**
     * 填充数据
     *
     * @param imageView
     * @param position
     */
    private void upDataImageView(final PhotoView imageView, int position) {
        // 显示图片
        Glide.with(imageView.getContext())
                .load(listDatas.get(position).path)
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


    public Image getItemData(int position) {
        if (position < listDatas.size()) {
            return listDatas.get(position);
        }
        return null;
    }


    public int indexOf(Image data) {
        return listDatas.indexOf(data);
    }
}
