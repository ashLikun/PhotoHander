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
import android.os.Parcelable;
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
import androidx.fragment.app.FragmentTransaction;

import com.ashlikun.photo_hander.bean.Image;
import com.ashlikun.photo_hander.bean.ImageSelectData;
import com.ashlikun.photo_hander.compress.Luban;
import com.ashlikun.photo_hander.compress.OnCompressListener;
import com.ashlikun.photo_hander.crop.Crop;
import com.ashlikun.photo_hander.utils.PhotoHanderPermission;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;
import com.ashlikun.photoview.PhotoView;

import java.io.File;
import java.io.IOException;
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
    ArrayList<ImageSelectData> resultList;
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
    PhotoOptionData optionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ph_activity_default);
        //获取主题颜色
        TypedArray array = getTheme().obtainStyledAttributes(new int[]{R.attr.phTitleColor, android.R.attr.colorPrimary});
        int titleColor = array.getColor(0, 0xffffffff);
        int colorPrimary = array.getColor(0, 0xffffffff);
        array.recycle();
        PhotoHanderUtils.autoStatueTextColor(getWindow(), colorPrimary);
        TextView titleView = findViewById(R.id.titleView);
        titleView.setText(getTitle());
        titleView.setTextColor(titleColor);

        mSubmitButton = findViewById(R.id.commit);
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack.getDrawable() == null) {
            Drawable drawable = getResources().getDrawable(R.drawable.material_back);
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
        //配置属性
        optionData = intent.getParcelableExtra(IntentKey.EXTRA_OPTION_DATA);
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
        for (ImageSelectData d : resultList) {
            if (d.isHttpImg()) {
                if (d.originPath != null && d.compressPath == null) {
                    d.compressPath = d.originPath;
                } else if (d.compressPath != null && d.originPath == null) {
                    d.originPath = d.compressPath;
                }
            }
        }

        if (optionData == null) {
            Toast.makeText(this, "缺少参数，无法启动照片选择", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
                    getString(R.string.ph_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            Bundle bundle = new Bundle();
            bundle.putParcelable(IntentKey.EXTRA_OPTION_DATA, optionData);
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
    private void updateDoneText(ArrayList<ImageSelectData> resultList) {
        int size = 0;
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText(R.string.ph_action_done);
            mSubmitButton.setEnabled(false);
        } else {
            size = resultList.size();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setText(getString(R.string.ph_action_button_string,
                getString(R.string.ph_action_done), size, optionData.mDefaultCount));
    }

    @Override
    public void onSingleImageSelected(String path) {
        if (optionData.mIsCrop) {
            Uri destination = null;
            Uri source = Uri.fromFile(new File(path));
            try {
                destination = Uri.fromFile(PhotoHanderUtils.createCacheTmpFile(this, "crop"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (destination != null && source != null) {
                Crop.of(source, destination)
                        .withSize(optionData.cropWidth, optionData.cropHeight)
                        .showCircle(optionData.cropShowCircle)
                        .color(optionData.cropColor)
                        .start(this);
            } else {
                Toast.makeText(this, R.string.ph_error_image_not_exist, Toast.LENGTH_SHORT).show();
            }

        } else {
            resultList.add(new ImageSelectData(path));
            completeSelect();
        }
    }

    @Override
    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(new ImageSelectData(path));
        }
        updateDoneText(resultList);
    }

    @Override
    public void onImageUnselected(String path) {
        ImageSelectData delete = null;
        for (ImageSelectData d : resultList) {
            if (TextUtils.equals(path, d.originPath)) {
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
            // notify system the image has change
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
            onSingleImageSelected(imageFile.getPath());
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
    public void onLookPhoto(List<Image> imageList, List<Image> selectList, int position, Image currentData) {
        try {
            //检查是否有PhotoView库
            Class.forName(PhotoView.class.getName());
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(IntentKey.EXTRA_ADAPTER_SHOW_DATA, (ArrayList<? extends Parcelable>) imageList);
            bundle.putParcelableArrayList(IntentKey.EXTRA_DEFAULT_SELECTED_LIST, (ArrayList<? extends Parcelable>) selectList);
            bundle.putInt(IntentKey.EXTRA_ADAPTER_CLICK_POSITION, position);
            bundle.putParcelable(IntentKey.EXTRA_ADAPTER_CLICK_DATA, currentData);
            bundle.putParcelable(IntentKey.EXTRA_OPTION_DATA, optionData);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("PhotoLookFragment");
            if (fragment != null) {
                ft.remove(fragment);
            }
            fragment = Fragment.instantiate(this, PhotoLookFragment.class.getName(), bundle);
            ft.setCustomAnimations(R.anim.mis_anim_fragment_lookphotp_in, R.anim.mis_anim_fragment_lookphotp_out)
                    .add(android.R.id.content, fragment, "PhotoLookFragment")
                    .commitAllowingStateLoss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 图片选择完成, 还没压缩
     */
    void completeSelect() {

        if (optionData.isCompress) {
            //压缩
            ArrayList<String> resultStrList = ImageSelectData.getOriginPaths(resultList);
            Luban.get(this).load(resultStrList)
                    .putGear(optionData.compressRank)
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
                        public void onSuccess(ArrayList<ImageSelectData> files) {
                            compressDialog.dismiss();
                            resultList = files;
                            optionData.isCompress = false;
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
        } else {
            if (resultList != null && resultList.size() > 0) {
                if (optionData.mDefaultCount < resultList.size()) {
                    resultList = (ArrayList<ImageSelectData>) resultList.subList(0, optionData.mDefaultCount);
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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                optionData.mIsCrop = false;
                onSingleImageSelected(Crop.getOutput(data).getPath());
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
    }

    @Override
    public void onLowMemory() {
        if (compressDialog != null) {
            compressDialog.dismiss();
        }
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
        ArrayList<Image> images = ((PhotoLookFragment) fragment).getSelectDatas();
        resultList = new ArrayList<>();
        if (images != null) {
            for (Image img : images) {
                if (img != null) {
                    resultList.add(new ImageSelectData(img.path));
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
