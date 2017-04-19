package com.hubng.photo_hander;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hubng.photo_hander.compress.Luban;
import com.hubng.photo_hander.compress.OnCompressListener;
import com.hubng.photo_hander.crop.Crop;
import com.hubng.photo_hander.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.hubng.photo_hander.crop.Crop.Extra.SHOW_CIRCLE;

/**
 * 作者　　: 李坤
 * 创建时间: 16:32 Administrator
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：图片选择主界面
 */

public class PhotoHanderActivity extends AppCompatActivity
        implements PhotoHanderFragment.Callback {

    // Single choice
    public static final int MODE_SINGLE = 0;
    // Multi choice
    public static final int MODE_MULTI = 1;

    /**
     * Max image size，int，{@link #DEFAULT_IMAGE_SIZE} by default
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * Select mode，{@link #MODE_MULTI} by default
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * Whether show camera，true by default
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * Result data set，ArrayList&lt;String&gt;
     */
    public static final String EXTRA_RESULT = "select_result";
    /**
     * Original data set
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";

    public static final String EXTRA_CROP_WIDTH = "crop_width";
    public static final String EXTRA_CROP_HEIGHT = "crop_height";
    public static final String EXTRA_IS_CROP = "is_crop";
    public static final String EXTRA_IS_COMPRESS = "is_compress";
    public static final String EXTRA_COMPRESS_RANK = "is_compress";
    // Default image size
    private static final int DEFAULT_IMAGE_SIZE = 9;


    private ArrayList<String> resultList = new ArrayList<>();
    private Button mSubmitButton;
    private int mDefaultCount = DEFAULT_IMAGE_SIZE;
    private int cropWidth = 0;
    private int cropHeight = 0;
    private boolean mIsCrop = false;//是否裁剪
    private boolean cropShowCircle = false;//是否显示圆
    private int cropColor;//裁剪的颜色
    private boolean isCompress = false;//是否压缩
    private int compressRank = Luban.THIRD_GEAR;//压缩等级
    ProgressDialog compressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MIS_NO_ACTIONBAR);
        setContentView(R.layout.mis_activity_default);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final Intent intent = getIntent();
        mDefaultCount = intent.getIntExtra(EXTRA_SELECT_COUNT, DEFAULT_IMAGE_SIZE);

        final int mode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);
        final boolean isShow = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);

        if (mode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
            for (int i = 0; i < resultList.size(); i++) {
                if (PhotoHander.create().getmRelationMap().get(resultList.get(i).hashCode()) != null) {//是加密图片
                    resultList.set(i, PhotoHander.create().getmRelationMap().get(resultList.get(i).hashCode()));
                }
            }
        }

        cropWidth = intent.getIntExtra(EXTRA_CROP_WIDTH, cropWidth);
        cropHeight = intent.getIntExtra(EXTRA_CROP_HEIGHT, cropHeight);
        mIsCrop = intent.getBooleanExtra(EXTRA_IS_CROP, mIsCrop);
        cropShowCircle = intent.getBooleanExtra(SHOW_CIRCLE, cropShowCircle);
        cropColor = intent.getIntExtra(Crop.Extra.COLOR, cropColor);
        isCompress = intent.getBooleanExtra(EXTRA_IS_COMPRESS, isCompress);
        compressRank = intent.getIntExtra(EXTRA_COMPRESS_RANK, compressRank);

        mSubmitButton = (Button) findViewById(R.id.commit);
        if (mode == MODE_MULTI) {
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
            Bundle bundle = new Bundle();
            bundle.putInt(PhotoHanderFragment.EXTRA_SELECT_COUNT, mDefaultCount);
            bundle.putInt(PhotoHanderFragment.EXTRA_SELECT_MODE, mode);
            bundle.putBoolean(PhotoHanderFragment.EXTRA_SHOW_CAMERA, isShow);
            bundle.putStringArrayList(PhotoHanderFragment.EXTRA_DEFAULT_SELECTED_LIST, resultList);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.image_grid, Fragment.instantiate(this, PhotoHanderFragment.class.getName(), bundle))
                    .commit();
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
     * Update done button by select image data
     *
     * @param resultList selected image data
     */
    private void updateDoneText(ArrayList<String> resultList) {
        int size = 0;
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText(R.string.mis_action_done);
            mSubmitButton.setEnabled(false);
        } else {
            size = resultList.size();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setText(getString(R.string.mis_action_button_string,
                getString(R.string.mis_action_done), size, mDefaultCount));
    }

    @Override
    public void onSingleImageSelected(String path) {
        if (mIsCrop) {
            Uri destination = null;
            Uri source = Uri.fromFile(new File(path));
            try {
                destination = Uri.fromFile(FileUtils.createCacheTmpFile(this, "crop"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (destination != null && source != null) {
                Crop.of(source, destination)
                        .withAspect(cropWidth, cropHeight)
                        .showCircle(cropShowCircle)
                        .color(cropColor)
                        .start(this);
            } else {
                Toast.makeText(this, R.string.mis_error_image_not_exist, Toast.LENGTH_SHORT).show();
            }

        } else {
            resultList.add(path);
            completeSelect();
        }
    }

    @Override
    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            // notify system the image has change
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
            resultList.add(imageFile.getAbsolutePath());
            completeSelect();
        }
    }

    /**
     * 图片选择完成, 还没压缩
     */
    private void completeSelect() {

        if (isCompress) {

            Luban.get(this).load(resultList)
                    .putGear(compressRank)
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
                        public void onSuccess(ArrayList<String> files) {
                            compressDialog.dismiss();
                            PhotoHander.create().getmRelationMap().clear();
                            for (int i = 0; i < files.size(); i++) {
                                if (!isEquals(files.get(i), resultList.get(i))) {//是加密图片
                                    //保存关系
                                    PhotoHander.create().getmRelationMap().put(files.get(i).hashCode(),
                                            resultList.get(i));
                                }
                            }
                            resultList = files;
                            isCompress = false;
                            completeSelect();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(PhotoHanderActivity.this, "图片处理出错", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoading(int progress, long total) {
                            compressDialog.setProgress((int) (progress / (total * 1.0f) * 100));
                        }
                    }).launch();
        } else {
            if (resultList != null && resultList.size() > 0) {
                // Notify success
                Intent data = new Intent();
                data.putStringArrayListExtra(EXTRA_RESULT, resultList);
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
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            mIsCrop = false;
            onSingleImageSelected(Crop.getOutput(data).getPath());
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

    /*
      * 比较两个字符串
      */
    private boolean isEquals(String actual, String expected) {
        return actual == expected
                || (actual == null ? expected == null : actual.equals(expected));
    }
}
