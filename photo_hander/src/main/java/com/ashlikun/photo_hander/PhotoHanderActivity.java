package com.ashlikun.photo_hander;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.bean.MediaSelectData;
import com.ashlikun.photo_hander.compress.luban.CompressResult;
import com.ashlikun.photo_hander.compress.luban.Luban;
import com.ashlikun.photo_hander.compress.luban.OnCompressListener;
import com.ashlikun.photo_hander.compress.video.VideoCompress;
import com.ashlikun.photo_hander.crop.Crop;
import com.ashlikun.photo_hander.utils.PhotoHanderPermission;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.ashlikun.photo_hander.utils.PhotoThreadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 16:32 Administrator
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：图片选择主界面
 */

public class PhotoHanderActivity extends AppCompatActivity
        implements PhotoHanderFragment.Callback, PhotoLookFragment.OnCallback {
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;

    /**
     * 已选的数据
     */
    ArrayList<MediaSelectData> resultList;
    /**
     * 追加的数据
     */
    ArrayList<String> addList;

    /**
     * 提交按钮
     */
    private TextView mSubmitButton;
    ProgressDialog compressDialog;
    /**
     * 配置参数
     */
    PhotoOptionData optionData = PhotoOptionData.currentData;
    private boolean isVideoCompressOk = !optionData.isVideoCompress;
    private boolean isCompressOk = !optionData.isCompress;
    private VideoCompress videoCompress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (optionData == null) {
            Toast.makeText(this, "缺少参数，无法启动照片选择", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setTitle(optionData.isVideoOnly ? R.string.photo_title_all_video : optionData.isCanVideo() ? R.string.photo_title_all_image_and_video : R.string.photo_title_image);
        setContentView(R.layout.ph_activity_default);
        //获取主题颜色
        TypedArray array = getTheme().obtainStyledAttributes(new int[]{R.attr.phTitleColor, android.R.attr.colorPrimary});
        int titleColor = array.getColor(0, 0xffffffff);
        int colorPrimary = array.getColor(1, 0xffffffff);
        array.recycle();
        PhotoHanderUtils.autoStatueTextColor(getWindow(), colorPrimary);
        TextView titleView = findViewById(R.id.titleView);
        titleView.setText(getTitle());
        titleView.setTextColor(titleColor);

        mSubmitButton = findViewById(R.id.commit);
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack.getDrawable() == null) {
            Drawable drawable = getDrawable(R.drawable.material_back);
            drawable.mutate();
            DrawableCompat.setTint(drawable, titleColor);
            btnBack.setImageDrawable(drawable);
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        //已选数据
        if (!optionData.isMustCamera) {
            //只拍照清空已选数据
            resultList = intent.getParcelableArrayListExtra(IntentKey.EXTRA_DEFAULT_SELECTED_LIST);
        }
        addList = intent.getStringArrayListExtra(IntentKey.EXTRA_DEFAULT_ADD_IMAGES);
        if (resultList == null) {
            resultList = new ArrayList<>();
        }

        //处理数据
        for (MediaSelectData d : resultList) {
            if (d.isHttpImg()) {
                if (d.originPath() != null && d.compressPath == null) {
                    d.compressPath = d.originPath();
                } else if (d.compressPath != null && d.originPath() == null) {
                    d.mediaFile = new MediaFile(d.compressPath, d.mediaFile.type);
                }
            }
        }
        if (optionData.isModeMulti()) {
            updateDoneText(resultList);
            mSubmitButton.setVisibility(View.VISIBLE);
            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    completeSelect();
                }
            });
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }


        if (savedInstanceState == null) {
            addFragment();
        }
        if (optionData.isMustCamera) {
            findViewById(R.id.phRootView).setVisibility(View.GONE);
        }
    }

    public void addFragment() {
        String[] permission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //请求读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && !PhotoHanderPermission.checkSelfPermission(this, permission)) {
            PhotoHanderPermission.requestPermission(this, permission,
                    getString(R.string.photo_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(IntentKey.EXTRA_DEFAULT_SELECTED_LIST, resultList);
            bundle.putStringArrayList(IntentKey.EXTRA_DEFAULT_ADD_IMAGES, addList);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.image_grid, Fragment.instantiate(this, PhotoHanderFragment.class.getName(), bundle), "PhotoHanderFragment")
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 更新完成按钮的文字
     */
    private void updateDoneText(ArrayList<MediaSelectData> resultList) {
        int size = 0;
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText(R.string.photo_action_done);
            mSubmitButton.setEnabled(false);
        } else {
            size = resultList.size();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setText(getString(R.string.photo_action_button_string,
                getString(R.string.photo_action_done), size, optionData.mDefaultCount));
    }

    @Override
    public void onSingleImageSelected(MediaFile file) {
        MediaSelectData mediaSelectData = new MediaSelectData(file);
        resultList.add(mediaSelectData);
        if (optionData.mIsCrop && !mediaSelectData.isVideo()) {
            PhotoHanderUtils.startCrop(this, file, optionData);
        } else {
            completeSelect();
        }
    }

    @Override
    public void onImageSelected(MediaFile file) {
        MediaSelectData data = new MediaSelectData(file);
        if (!resultList.contains(data)) {
            resultList.add(data);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onImageUnselected(MediaFile file) {
        MediaSelectData delete = null;
        for (MediaSelectData d : resultList) {
            if (TextUtils.equals(file.path, d.originPath())) {
                delete = d;
                break;
            }
        }
        resultList.remove(delete);
        updateDoneText(resultList);
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
            onSingleImageSelected(new MediaFile(imageFile.getPath(), 1));
        }
    }

    /**
     * 查看照片
     *
     * @param imageList   整个数据集合
     * @param selectList  已经选择的数据集合
     * @param position    点击的位置
     * @param currentData 点击的数据
     */
    @Override
    public void onLookPhoto(List<MediaFile> imageList, List<MediaFile> selectList, int position, MediaFile currentData) {
        PhotoHanderUtils.startLook(this, imageList, selectList, position, currentData);
    }


    /**
     * 图片选择完成, 还没压缩
     */
    void completeSelect() {
        if (!isCompressOk) {
            //压缩
            ArrayList<String> resultStrList = MediaSelectData.getCompressImagePaths(resultList);
            if (resultStrList == null || resultStrList.isEmpty()) {
                isCompressOk = true;
                completeSelect();
                return;
            }
            Luban.with(this)
                    .load(resultStrList)
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            if (compressDialog == null) {
                                compressDialog = new ProgressDialog(PhotoHanderActivity.this);
                                compressDialog.setTitle("图片处理中");
                                compressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                compressDialog.setCanceledOnTouchOutside(false);
                                compressDialog.setMax(100);
                            }
                            compressDialog.show();
                        }

                        @Override
                        public void onSuccess(ArrayList<CompressResult> files) {
                            for (MediaSelectData resultDd : resultList) {
                                for (CompressResult result : files) {
                                    String org = result.provider.getPath();
                                    if (TextUtils.equals(resultDd.originPath(), org) || TextUtils.equals(resultDd.cropPath, org)) {
                                        resultDd.compressPath = result.compressPath;
                                        resultDd.isCompress = result.isCompress;
                                        resultDd.isComparessError = result.isComparessError;
                                    }
                                }
                            }
                            compressDialog.dismiss();
                            isCompressOk = true;
                            completeSelect();
                        }

                        @Override
                        public void onError(Throwable e) {
                            compressDialog.dismiss();
                            Toast.makeText(PhotoHanderActivity.this, "图片处理出错", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoading(int progress, long total) {
                            compressDialog.setProgress((int) (progress / (total * 1.0f) * 100));
                        }
                    }).launch();
        } else if (!isVideoCompressOk) {
            try {
                //视频压缩
                ArrayList<MediaSelectData> resultStrList = MediaSelectData.getCompressVideoPaths(resultList);
                if (resultStrList == null || resultStrList.isEmpty()) {
                    isVideoCompressOk = true;
                    completeSelect();
                    return;
                }
                if (compressDialog == null) {
                    compressDialog = new ProgressDialog(PhotoHanderActivity.this);
                    compressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    compressDialog.setCanceledOnTouchOutside(false);
                    compressDialog.setMax(100);
                }
                compressDialog.setProgress(0);
                compressDialog.setTitle("视频压缩中");
                compressDialog.show();
                videoCompress = new VideoCompress(this, resultStrList, optionData, new VideoCompress.PhotoHandleVideoProgressListener() {
                    @Override
                    public void onProgress(int progress, VideoCompress videoCompress) {
                        compressDialog.setProgress(progress);
                        if (progress >= 100) {
                            compressDialog.dismiss();
                            isVideoCompressOk = true;
                            completeSelect();
                        }
                    }
                });
                videoCompress.start();
            } catch (Exception e) {
                if (compressDialog != null) {
                    compressDialog.dismiss();
                }
                isVideoCompressOk = true;
                completeSelect();
            }
        }
        if (!isCompressOk || !isVideoCompressOk) {
            return;
        }
        if (resultList != null && resultList.size() >= 0) {
            if (optionData.mDefaultCount < resultList.size()) {
                resultList = (ArrayList<MediaSelectData>) resultList.subList(0, optionData.mDefaultCount);
            }
            //回调
            Intent data = new Intent();
            data.putParcelableArrayListExtra(IntentKey.EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                optionData.mIsCrop = false;
                for (MediaSelectData dd : resultList) {
                    if (TextUtils.equals(dd.originPath(), Crop.getOrginOutput(data).getPath())) {
                        dd.cropPath = Crop.getOutput(data).getPath();
                        break;
                    }
                }
                completeSelect();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compressDialog != null) {
            compressDialog.dismiss();
        }
        if (videoCompress != null) {
            videoCompress.stop();
        }
        PhotoOptionData.setCurrentData(null);
        PhotoThreadUtils.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if (compressDialog != null) {
            compressDialog.dismiss();
        }
        if (videoCompress != null) {
            videoCompress.stop();
        }
        PhotoOptionData.setCurrentData(null);
        PhotoThreadUtils.onDestroy();
    }

    private boolean isEquals(String actual, String expected) {
        return actual == expected
                || (actual == null ? expected == null : actual.equals(expected));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addFragment();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("PhotoLookFragment");
        if (fragment != null) {
            finishPhotoLookFragment(false);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 照片查看完成
     *
     * @param isSelectOk
     */
    private void finishPhotoLookFragment(boolean isSelectOk) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("PhotoLookFragment");
        //更新顶部已选几个
        ArrayList<MediaFile> images = ((PhotoLookFragment) fragment).getSelectDatas();
        resultList = new ArrayList<>();
        if (images != null) {
            for (MediaFile img : images) {
                if (img != null) {
                    resultList.add(new MediaSelectData(img));
                }
            }
        }
        updateDoneText(resultList);
        //有PhotoHanderFragment
        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag("PhotoHanderFragment");
        if (fragment2 != null) {
            //把选择的数据告诉PhotoHanderFragment
            ((PhotoHanderFragment) fragment2).setSelectDatas(images);
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.mis_anim_fragment_lookphotp_in, R.anim.mis_anim_fragment_lookphotp_out)
                .remove(fragment)
                .commitAllowingStateLoss();
        if (isSelectOk) {
            //完成选择
            completeSelect();
        }
    }

    /**
     * 照片查看界面选择完成，发送
     */
    @Override
    public void onLookPhotoCompleteSelect() {
        finishPhotoLookFragment(true);
    }
}
