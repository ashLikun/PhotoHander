package com.ashlikun.photo_hander;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.photo_hander.adapter.FolderAdapter;
import com.ashlikun.photo_hander.adapter.ImageGridAdapter;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaFolder;
import com.ashlikun.photo_hander.bean.MediaSelectData;
import com.ashlikun.photo_hander.loader.AbsMediaScanner;
import com.ashlikun.photo_hander.loader.MediaHandler;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.ashlikun.photo_hander.utils.ShowCameraActionCall;
import com.ashlikun.photo_hander.view.ImageFolderPopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author　　: 李坤
 * 创建时间: 2018/8/15 16:19
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：图片选择的fragment
 */
public class PhotoHanderFragment extends Fragment implements AbsMediaScanner.OnLoadFinished {

    private static final String KEY_TEMP_FILE = "key_temp_file";


    /**
     * 已选的数据
     */
    private ArrayList<String> resultList = new ArrayList<>();

    /**
     * 配置参数
     */
    PhotoOptionData optionData = PhotoOptionData.currentData;
    /**
     * 数据来源
     */
    private MediaHandler mediaHandler;

    private RecyclerView recyclerView;
    private Callback mCallback;

    private ImageGridAdapter mImageAdapter;

    private ImageFolderPopupWindow mFolderPopupWindow;

    private TextView yulanTv;
    private View mPopupAnchorView;
    FragmentActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (Callback) getActivity();
            activity = getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("The Activity must implement MultiImageSelectorFragment.Callback interface...");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ph_fragment_multi_image, container, false);
    }

    ShowCameraActionCall ShowCameraActionCall = new ShowCameraActionCall() {
        @Override
        public void call(Pair<File, ActivityResult> data) {
            File tmpFile = data.first;
            if (data.second.getResultCode() == Activity.RESULT_OK) {
                if (tmpFile != null) {
                    if (mCallback != null) {
                        mCallback.onCameraShot(tmpFile);
                    }
                }
            } else {
                // delete tmp file
                while (tmpFile != null && tmpFile.exists()) {
                    boolean success = tmpFile.delete();
                    if (success) {
                        tmpFile = null;
                    }
                }
                if (optionData.isMustCamera) {
                    getActivity().finish();
                }
            }
        }
    };


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //获取主题颜色
        TypedArray array = getActivity().getTheme().obtainStyledAttributes(new int[]{R.attr.phBottonColor, R.attr.phTitleColor});
        int phBottonColor = array.getColor(0, 0xffffffff);
        array.recycle();

        if (optionData.isMustCamera) {
            view.setVisibility(View.GONE);
            PhotoHanderUtils.showCameraAction(activity, ShowCameraActionCall);
            return;
        }

        //已选数据
        ArrayList<MediaSelectData> resultListM = getArguments().getParcelableArrayList(IntentKey.EXTRA_DEFAULT_SELECTED_LIST);

        resultList = MediaSelectData.getOriginPaths(resultListM);
        mImageAdapter = new ImageGridAdapter(getActivity(), optionData.isShowCamera, 4);
        mImageAdapter.showSelectIndicator(optionData.isModeMulti());
        mImageAdapter.setAddList(getArguments().getStringArrayList(IntentKey.EXTRA_DEFAULT_ADD_IMAGES));
        mPopupAnchorView = view.findViewById(R.id.mPopupAnchorView);

        yulanTv = view.findViewById(R.id.yulanTv);

        yulanTv.setTextColor(phBottonColor);


        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.addItemDecoration(new NeibuItemDecoration.Builder(getContext(), NeibuItemDecoration.HORIZONTAL)
                .setColorRes(R.color.ph_space_color)
                .setSizeRes(R.dimen.ph_space_size)
                .create());
        recyclerView.addItemDecoration(new NeibuItemDecoration.Builder(getContext(), NeibuItemDecoration.VERTICAL)
                .setColorRes(R.color.ph_space_color)
                .setSizeRes(R.dimen.ph_space_size)
                .create());
        recyclerView.setAdapter(mImageAdapter);
        mImageAdapter.setOnItemClickListener(new ImageGridAdapter.OnItemClickListener() {
            /**
             * check点击回掉
             */
            @Override
            public void onItemCheckClick(View view, MediaFile data, int position) {
                selectImageFromGrid(data);
            }

            /**
             * 整个item点击回掉
             */
            @Override
            public void onItemClick(View view, MediaFile data, int position) {
                if (mImageAdapter.isShowCamera()) {
                    if (position == 0) {
                        if (optionData.mDefaultCount <= resultList.size()) {
                            Toast.makeText(getActivity(), getString(R.string.photo_msg_amount_limit, optionData.mDefaultCount), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        PhotoHanderUtils.showCameraAction(activity, ShowCameraActionCall);
                        return;
                    } else {
                        //减去拍照
                        position = position - 1;
                    }
                }
                if (optionData.isModeMulti()) {
                    if (mCallback != null) {
                        mCallback.onLookPhoto(mImageAdapter.getImages(), mImageAdapter.getSelectedImages(), position, data);
                    }
                } else {
                    if (mCallback != null) {
                        mCallback.onSingleImageSelected(data);
                    }
                }
            }
        });
        yulanTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (optionData.isModeMulti()) {
                    if (mCallback != null) {
                        ArrayList<MediaFile> selectDatas = mImageAdapter.getSelectedImages();
                        if (selectDatas != null && !selectDatas.isEmpty()) {
                            mCallback.onLookPhoto(selectDatas, selectDatas, 0, selectDatas.get(0));
                        }
                    }
                }
            }
        });
        view.findViewById(R.id.footer).setVisibility(optionData.isModeMulti() ? View.VISIBLE : View.GONE);
        setYulanText();
    }

    /**
     * 设置预览的文字
     */
    public void setYulanText() {
        if (optionData.isModeMulti()) {
            if (resultList == null || resultList.isEmpty()) {
                yulanTv.setText(R.string.photo_action_yulan);
            } else {
                yulanTv.setText(getString(R.string.photo_action_yulan_button_string,
                        getString(R.string.photo_action_yulan), resultList.size()));
            }

        }
    }

    public void onFolderClick() {
        createPopupFolderList();
        if (mFolderPopupWindow.isShowing()) {
            mFolderPopupWindow.dismiss();
        } else {
            mFolderPopupWindow.showAsDropDown(mPopupAnchorView);
            mFolderPopupWindow.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * 选择分类的Popup
     */
    private void createPopupFolderList() {
        if (mFolderPopupWindow != null) {
            return;
        }
        mFolderPopupWindow = new ImageFolderPopupWindow(getActivity(), getView(), mediaHandler);
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFolderPopupWindow.setAnimationStyle(R.style.PhotoHandle_imageFolderAnimator);
        mFolderPopupWindow.getAdapter().setOnImageFolderChangeListener(new FolderAdapter.OnImageFolderChangeListener() {
            @Override
            public void onImageFolderChange(int position, String text, boolean showCamera, List<MediaFile> mediaFileList) {
                mFolderPopupWindow.dismiss();
                mCallback.onFolderChang(text);

                mImageAdapter.setShowCamera(showCamera);
                if (mediaFileList != null) {
                    mImageAdapter.setData(mediaFileList);
                    if (resultList != null && resultList.size() > 0) {
                        mImageAdapter.setDefaultSelected(resultList);
                    }
                }
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (optionData.isMustCamera) {
            return;
        }
        mediaHandler = new MediaHandler(getActivity(), this);
        mediaHandler.loader();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }
        }
        super.onConfigurationChanged(newConfig);
    }


    /**
     * 点击图片的回掉
     *
     * @param data image data
     */
    private void selectImageFromGrid(MediaFile data) {
        if (data != null) {
            if (optionData.isModeMulti()) {
                if (resultList.contains(data.path)) {
                    resultList.remove(data.path);
                    setYulanText();
                    if (mCallback != null) {
                        mCallback.onImageUnselected(data);
                    }
                } else {
                    if (!PhotoHanderUtils.checkLimit(getActivity(), resultList, optionData, data)) {
                        return;
                    }
                    resultList.add(data.path);
                    setYulanText();
                    if (mCallback != null) {
                        mCallback.onImageSelected(data);
                    }
                }
                mImageAdapter.select(data);
            } else {
                if (mCallback != null) {
                    mCallback.onSingleImageSelected(data);
                }
            }
        }
    }


    public void setSelectDatas(ArrayList<MediaFile> selectDatas) {
        resultList.clear();
        if (selectDatas != null) {
            for (MediaFile d : selectDatas) {
                resultList.add(d.path);
            }
        }
        setYulanText();
        mImageAdapter.setSelectDatas(selectDatas);
    }

    @Override
    public void onLoadFinished(List<MediaFile> datas, List<MediaFolder> folderDatas) {
        if (mFolderPopupWindow != null && mFolderPopupWindow.getAdapter() != null && mFolderPopupWindow.getAdapter().isVideoMode()) {
            mImageAdapter.setData(mediaHandler.getVideoFiles());
        } else {
            mImageAdapter.setData(datas);
        }
        if (resultList != null && resultList.size() > 0) {
            mImageAdapter.setDefaultSelected(resultList);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaHandler != null) {
            mediaHandler.destroyLoader();
        }
    }

    /**
     * 回掉给Activity的事件
     */
    public interface Callback {

        /**
         * 当单图选择时候
         */
        void onSingleImageSelected(MediaFile mediaFile);

        /**
         * 当单图选择时候
         */
        void onImageSelected(MediaFile mediaFile);

        /**
         * 当把已经选择的去除
         */
        void onImageUnselected(MediaFile mediaFile);

        /**
         * 当拍照完成时候
         *
         * @param imageFile
         */
        void onCameraShot(File imageFile);

        /**
         * 查看照片
         *
         * @param imageList   整个数据集合
         * @param selectList  已经选择的数据集合
         * @param position    点击的位置
         * @param currentData 点击的数据
         */
        void onLookPhoto(List<MediaFile> imageList, List<MediaFile> selectList, int position, MediaFile currentData);

        /**
         * 选择的目录改变
         *
         * @param text
         */
        void onFolderChang(String text);
    }


}
