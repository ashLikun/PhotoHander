package com.ashlikun.photo_hander;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.ashlikun.photo_hander.adapter.LookFragmentAdapter;
import com.ashlikun.photo_hander.adapter.MiniImageAdapter;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.util.ArrayList;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　13:08
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：查看照片的Fragment
 */
public class PhotoLookFragment extends Fragment implements ViewPager.OnPageChangeListener, MiniImageAdapter.OnItemClickListener {
    private ViewPager viewPager;
    private ImageView btnBack;
    private TextView titleView;
    private ImageView checkmark;
    private TextView selectText;
    private LinearLayout checkLL;
    private FrameLayout bottomFl;
    private RecyclerView recycleView;
    LookFragmentAdapter adapter;
    MiniImageAdapter miniImageAdapter;
    OnCallback onCallback;
    /**
     * 已经选择的数据,这个数据是要回掉的
     */
    private ArrayList<MediaFile> selectDatas;
    /**
     * 提交按钮
     */
    private TextView submitButton;
    PhotoOptionData optionData = PhotoOptionData.currentData;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ph_fragment_look_photo, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof OnCallback) {
            onCallback = (OnCallback) getActivity();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
    }

    private void initView(final View view) {
        //获取主题颜色
        TypedArray array = getActivity().getTheme().obtainStyledAttributes(new int[]{R.attr.phBottonColor, R.attr.phTitleColor});
        int phBottonColor = array.getColor(0, 0xffffffff);
        int phTitleColor = array.getColor(1, 0xffffffff);
        array.recycle();
        viewPager = view.findViewById(R.id.viewPager);
        btnBack = view.findViewById(R.id.btn_back);
        titleView = view.findViewById(R.id.titleView);
        selectText = view.findViewById(R.id.selectText);
        checkmark = view.findViewById(R.id.checkmark);
        checkLL = view.findViewById(R.id.checkLL);
        bottomFl = view.findViewById(R.id.bottomFl);
        submitButton = view.findViewById(R.id.commit);
        recycleView = view.findViewById(R.id.recycleView);
        if (bottomFl.getBackground() != null) {
            Drawable drawable = DrawableCompat.wrap(bottomFl.getBackground()).mutate();
            drawable.setAlpha(160);
            bottomFl.setBackground(drawable);
        }

        if (recycleView.getBackground() != null) {
            Drawable drawable = DrawableCompat.wrap(recycleView.getBackground()).mutate();
            drawable.setAlpha(160);
            recycleView.setBackground(drawable);
        }
        PhotoHanderUtils.setCheck(checkmark, false);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onCallback != null) {
                    onCallback.onLookPhotoCompleteSelect();
                }
            }
        });
        if (btnBack.getDrawable() == null) {
            Drawable drawable = getResources().getDrawable(R.drawable.material_back);
            drawable.mutate();
            DrawableCompat.setTint(drawable, phBottonColor);
            btnBack.setImageDrawable(drawable);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        titleView.setTextColor(phTitleColor);
        selectText.setTextColor(phBottonColor);
        //选择点击
        checkLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置选中判断
                select(viewPager.getCurrentItem());
            }
        });

        ArrayList<MediaFile> listDatas = getArguments().getParcelableArrayList(IntentKey.EXTRA_ADAPTER_SHOW_DATA);
        selectDatas = getArguments().getParcelableArrayList(IntentKey.EXTRA_DEFAULT_SELECTED_LIST);
        if (selectDatas == null) {
            selectDatas = new ArrayList<>();
        }
        //2个对象一样
        if (listDatas == selectDatas) {
            selectDatas = new ArrayList<>();
            selectDatas.addAll(listDatas);
        }
        int position = getArguments().getInt(IntentKey.EXTRA_ADAPTER_CLICK_POSITION);
        MediaFile positionData = getArguments().getParcelable(IntentKey.EXTRA_ADAPTER_CLICK_DATA);
        adapter = new LookFragmentAdapter(getActivity(), listDatas);

        //初始化RecycleView
        recycleView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recycleView.addItemDecoration(new NeibuItemDecoration.Builder(getActivity(), NeibuItemDecoration.HORIZONTAL)
                .setSizeRes(R.dimen.ph_mini_space_size)
                .setColor(0)
                .create());
        recycleView.setAdapter(miniImageAdapter = new MiniImageAdapter(getActivity(), selectDatas, this));
        if (selectDatas.isEmpty()) {
            recycleView.setVisibility(View.GONE);
        }
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        if (position < adapter.getCount()) {
            viewPager.setCurrentItem(position);
            if (position == 0) {
                //0的时候不会自动选中
                onPageSelected(0);
            }
        }
        updateDoneText(selectDatas);
    }

    @ColorInt
    public int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }

    /**
     * 更新完成按钮的文字
     */
    private void updateDoneText(ArrayList resultList) {
        int size = 0;
        if (resultList == null || resultList.size() <= 0) {
            submitButton.setText(R.string.photo_action_done);
            submitButton.setEnabled(false);
        } else {
            size = resultList.size();
            submitButton.setEnabled(true);
        }
        submitButton.setText(getString(R.string.photo_action_button_string,
                getString(R.string.photo_action_done), size, optionData.mDefaultCount));
    }

    public void changTitle(int position) {
        titleView.setText((position + 1) + "/" + adapter.getCount());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < viewPager.getChildCount(); i++) {
            adapter.resetView(viewPager.getChildAt(i));
        }
        changTitle(position);
        //设置选中判断
        MediaFile image = adapter.getItemData(position);
        int selectPosition = selectDatas.indexOf(image);
        boolean isCheck = selectDatas.contains(image);
        PhotoHanderUtils.setCheck(checkmark, isCheck);
        int old = miniImageAdapter.setSelectItem(selectPosition);
        if (old != -1) {
            miniImageAdapter.notifyItemChanged(old, MiniImageAdapter.PLYLOAD_SELECT);
        }
        if (selectPosition != -1) {
            miniImageAdapter.notifyItemChanged(selectPosition, MiniImageAdapter.PLYLOAD_SELECT);
        }
        recycleView.scrollToPosition(selectPosition);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    /**
     * 选择某个图片，改变选择状态
     */
    public void select(int position) {
        MediaFile image = adapter.getItemData(position);

        if (selectDatas.contains(image)) {
            selectDatas.remove(image);
            //通知底部
            if (selectDatas.isEmpty()) {
                miniImageAdapter.notifyDataSetChanged();
            } else {
                miniImageAdapter.notifyItemRemoved(miniImageAdapter.setSelectItem(-1));
            }
        } else {
            if (!PhotoHanderUtils.checkLimit(getActivity(), selectDatas, optionData, image)) {
                return;
            }
            selectDatas.add(image);
            int selectPosition = selectDatas.indexOf(image);
            miniImageAdapter.setSelectItem(selectPosition);
            if (selectDatas.isEmpty()) {
                miniImageAdapter.notifyDataSetChanged();
            } else {
                miniImageAdapter.notifyItemInserted(selectPosition);
            }
            recycleView.scrollToPosition(selectPosition);
        }
        recycleView.setVisibility(selectDatas.isEmpty() ? View.GONE : View.VISIBLE);
        PhotoHanderUtils.setCheck(checkmark, selectDatas.contains(image));
        updateDoneText(selectDatas);
    }

    /**
     * 底部小图标点击
     *
     * @param view
     * @param data
     * @param position
     */
    @Override
    public void onItemClick(View view, MediaFile data, int position) {
        int viewPagerPosition = adapter.indexOf(data);
        if (viewPagerPosition != -1) {
            viewPager.setCurrentItem(viewPagerPosition, false);
        }
    }

    public ArrayList<MediaFile> getSelectDatas() {
        return selectDatas;
    }

    public interface OnCallback {
        /**
         * 照片查看界面选择完成，发送
         */
        void onLookPhotoCompleteSelect();
    }
}
