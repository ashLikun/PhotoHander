package com.ashlikun.photo_hander;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashlikun.photo_hander.adapter.FolderAdapter;
import com.ashlikun.photo_hander.adapter.ImageGridAdapter;
import com.ashlikun.photo_hander.bean.Folder;
import com.ashlikun.photo_hander.bean.Image;
import com.ashlikun.photo_hander.bean.ImageSelectData;
import com.ashlikun.photo_hander.utils.PhotoHanderPermission;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

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
public class PhotoHanderFragment extends Fragment {

    private static final String KEY_TEMP_FILE = "key_temp_file";
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;

    /**
     * 已选的数据
     */
    private ArrayList<String> resultList = new ArrayList<>();

    /**
     * 配置参数
     */
    PhotoOptionData optionData;
    /**
     * 可选目录的列表数据
     */
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    private RecyclerView recyclerView;
    private Callback mCallback;

    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;

    private ListPopupWindow mFolderPopupWindow;

    private TextView mCategoryText;
    private TextView yulanTv;
    private View mPopupAnchorView;

    private boolean hasFolderGened = false;

    private File mTmpFile;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (Callback) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("The Activity must implement MultiImageSelectorFragment.Callback interface...");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ph_fragment_multi_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //配置属性
        optionData = getArguments().getParcelable(IntentKey.EXTRA_OPTION_DATA);

        if (optionData.isMustCamera) {
            view.setVisibility(View.GONE);
            mTmpFile = PhotoHanderUtils.showCameraAction(PhotoHanderFragment.this);
            return;
        }

        //已选数据
        ArrayList<ImageSelectData> resultListM = getArguments().getParcelableArrayList(IntentKey.EXTRA_DEFAULT_SELECTED_LIST);

        resultList = ImageSelectData.getOriginPaths(resultListM);
        mImageAdapter = new ImageGridAdapter(getActivity(), optionData.isShowCamera, 4);
        mImageAdapter.showSelectIndicator(optionData.isModeMulti());
        mImageAdapter.setAddList(getArguments().getStringArrayList(IntentKey.EXTRA_DEFAULT_ADD_IMAGES));
        mPopupAnchorView = view.findViewById(R.id.footer);

        mCategoryText = (TextView) view.findViewById(R.id.category_btn);
        yulanTv = (TextView) view.findViewById(R.id.yulanTv);
        mCategoryText.setText(R.string.ph_folder_all);
        mCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mFolderPopupWindow == null) {
                    createPopupFolderList();
                }

                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.show();
                    int index = mFolderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    mFolderPopupWindow.getListView().setSelection(index);
                }
            }
        });

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
             * @param view
             * @param data
             * @param position
             */
            @Override
            public void onItemCheckClick(View view, Image data, int position) {
                selectImageFromGrid(data);
            }

            /**
             * 整个item点击回掉
             * @param view
             * @param data
             * @param position
             */
            @Override
            public void onItemClick(View view, Image data, int position) {
                if (mImageAdapter.isShowCamera()) {
                    if (position == 0) {
                        if (optionData.mDefaultCount <= resultList.size()) {
                            Toast.makeText(getActivity(), getString(R.string.ph_msg_amount_limit, optionData.mDefaultCount), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mTmpFile = PhotoHanderUtils.showCameraAction(PhotoHanderFragment.this);
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
                        mCallback.onSingleImageSelected(data.path);
                    }
                }
            }
        });
        yulanTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (optionData.isModeMulti()) {
                    if (mCallback != null) {
                        ArrayList<Image> selectDatas = mImageAdapter.getSelectedImages();
                        if (selectDatas != null && !selectDatas.isEmpty()) {
                            mCallback.onLookPhoto(selectDatas, selectDatas, 0, selectDatas.get(0));
                        }
                    }
                }
            }
        });
        mFolderAdapter = new FolderAdapter(getActivity());

        yulanTv.setVisibility(optionData.isModeMulti() ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置预览的文字
     */
    public void setYulanText() {
        if (optionData.isModeMulti()) {
            if (resultList == null || resultList.isEmpty()) {
                yulanTv.setText(R.string.ph_action_yulan);
            } else {
                yulanTv.setText(getString(R.string.ph_action_yulan_button_string,
                        getString(R.string.ph_action_yulan), resultList.size()));
            }

        }
    }

    /**
     * Create popup ListView
     */
    private void createPopupFolderList() {
        Point point = PhotoHanderUtils.getScreenSize(getActivity());
        int width = point.x;
        int height = (int) (point.y * (4.5f / 8.0f));
        mFolderPopupWindow = new ListPopupWindow(getActivity());
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setContentWidth(width);
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height);
        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mFolderAdapter.setSelectIndex(i);

                final int index = i;
                final AdapterView v = adapterView;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();

                        if (index == 0) {
                            getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryText.setText(R.string.ph_folder_all);
                            if (optionData.isShowCamera) {
                                mImageAdapter.setShowCamera(true);
                            } else {
                                mImageAdapter.setShowCamera(false);
                            }
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(index);
                            if (null != folder) {
                                mImageAdapter.setData(folder.images);
                                mCategoryText.setText(folder.name);
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            mImageAdapter.setShowCamera(false);
                        }

                        recyclerView.smoothScrollToPosition(0);
                    }
                }, 100);

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_TEMP_FILE, mTmpFile);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mTmpFile = (File) savedInstanceState.getSerializable(KEY_TEMP_FILE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (optionData.isMustCamera) {
            return;
        }
        // load image data
        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoHander.REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    if (mCallback != null) {
                        mCallback.onCameraShot(mTmpFile);
                    }
                }
            } else {
                // delete tmp file
                while (mTmpFile != null && mTmpFile.exists()) {
                    boolean success = mTmpFile.delete();
                    if (success) {
                        mTmpFile = null;
                    }
                }
                if (optionData.isMustCamera) {
                    getActivity().finish();
                }
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mTmpFile = PhotoHanderPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        if (mTmpFile == null) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 点击图片的回掉
     *
     * @param image image data
     */
    private void selectImageFromGrid(Image image) {
        if (image != null) {
            if (optionData.isModeMulti()) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    setYulanText();
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.path);
                    }
                } else {
                    if (optionData.mDefaultCount <= resultList.size()) {
                        Toast.makeText(getActivity(), getString(R.string.ph_msg_amount_limit, optionData.mDefaultCount), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    resultList.add(image.path);
                    setYulanText();
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.path);
                    }
                }
                mImageAdapter.select(image);
            } else {
                if (mCallback != null) {
                    mCallback.onSingleImageSelected(image.path);
                }
            }
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = null;
            if (id == LOADER_ALL) {
                cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3] + "=? OR " + IMAGE_PROJECTION[3] + "=? ",
                        new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
            } else if (id == LOADER_CATEGORY) {
                cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'",
                        null, IMAGE_PROJECTION[2] + " DESC");
            }
            return cursorLoader;
        }

        private boolean fileExist(String path) {
            if (!TextUtils.isEmpty(path)) {
                return new File(path).exists();
            }
            return false;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (data.getCount() > 0) {
                    List<Image> images = new ArrayList<>();
                    data.getCount();
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        if (!fileExist(path)) {
                            continue;
                        }
                        Image image = null;
                        if (!TextUtils.isEmpty(name)) {
                            image = new Image(path, name, dateTime);
                            images.add(image);
                        }
                        if (!hasFolderGened) {
                            // get all folder data
                            File folderFile = new File(path).getParentFile();
                            if (folderFile != null && folderFile.exists()) {
                                String fp = folderFile.getAbsolutePath();
                                Folder f = getFolderByPath(fp);
                                if (f == null) {
                                    Folder folder = new Folder();
                                    folder.name = folderFile.getName();
                                    folder.path = fp;
                                    folder.cover = image;
                                    List<Image> imageList = new ArrayList<>();
                                    imageList.add(image);
                                    folder.images = imageList;
                                    mResultFolder.add(folder);
                                } else {
                                    f.images.add(image);
                                }
                            }
                        }

                    } while (data.moveToNext());

                    mImageAdapter.setData(images);
                    if (resultList != null && resultList.size() > 0) {
                        mImageAdapter.setDefaultSelected(resultList);
                    }
                    if (!hasFolderGened) {
                        mFolderAdapter.setData(mResultFolder);
                        hasFolderGened = true;
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private Folder getFolderByPath(String path) {
        if (mResultFolder != null) {
            for (Folder folder : mResultFolder) {
                if (TextUtils.equals(folder.path, path)) {
                    return folder;
                }
            }
        }
        return null;
    }

    public void setSelectDatas(ArrayList<Image> selectDatas) {
        resultList.clear();
        if (selectDatas != null) {
            for (Image d : selectDatas) {
                resultList.add(d.path);
            }
        }
        setYulanText();
        mImageAdapter.setSelectDatas(selectDatas);
    }

    /**
     * 回掉给Activity的事件
     */
    public interface Callback {

        /**
         * 当单图选择时候
         *
         * @param path
         */
        void onSingleImageSelected(String path);

        /**
         * 当单图选择时候
         *
         * @param path
         */
        void onImageSelected(String path);

        /**
         * 当把已经选择的去除
         *
         * @param path
         */
        void onImageUnselected(String path);

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
        void onLookPhoto(List<Image> imageList, List<Image> selectList, int position, Image currentData);
    }


}
