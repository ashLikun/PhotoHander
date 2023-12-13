package com.ashlikun.multiimageselector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ashlikun.multiimageselector.simple.R;
import com.ashlikun.photo_hander.PhotoHander;
import com.ashlikun.photo_hander.bean.MediaSelectData;
import com.bumptech.glide.Glide;
import com.hw.videoprocessor.util.CL;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    private TextView mResultText;
    private ImageView imageView;
    private RadioGroup mChoiceMode, mShowCamera, cropRg;
    private EditText mRequestNum, cropWidthEt, cropHeightEt;

    private ArrayList<MediaSelectData> mSelectPath;
    private ArrayList<String> addHttpImage = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CL.setLogEnable(true);
        setContentView(R.layout.activity_main);

//        addHttpImage.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1553075915539&di=ec1b6b518f6a26aa998505d80e0ede33&imgtype=0&src=http%3A%2F%2Fs9.knowsky.com%2Fbizhi%2Fl%2F20090606%2F200906186%2520%25281%2529.jpg");
//        addHttpImage.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1553075915645&di=163f3070e7b24cf924e8340504b7f189&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Fd043ad4bd11373f0e263de4bae0f4bfbfaed0481.jpg");
//        addHttpImage.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1553075915644&di=4e6734ad58513cf6e17b23a359f4dc24&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Fcdbf6c81800a19d8a1af34d139fa828ba71e46b1.jpg");
//        addHttpImage.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1553075915644&di=8f38098ae98e3f4b914a3342ce1bd2ce&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Ffaf2b2119313b07e1fcce2dc06d7912396dd8cf5.jpg");

        mResultText = (TextView) findViewById(R.id.result);
        imageView = (ImageView) findViewById(R.id.imageView);
        mChoiceMode = (RadioGroup) findViewById(R.id.choice_mode);
        mShowCamera = (RadioGroup) findViewById(R.id.show_camera);
        cropRg = (RadioGroup) findViewById(R.id.cropRg);
        mRequestNum = (EditText) findViewById(R.id.request_num);
        cropWidthEt = (EditText) findViewById(R.id.cropWidthEt);
        cropHeightEt = (EditText) findViewById(R.id.cropHeightEt);

        mChoiceMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.multi) {
                    mRequestNum.setEnabled(true);

                } else {
                    mRequestNum.setEnabled(false);
                    mRequestNum.setText("");
                }
            }
        });

        View button = findViewById(R.id.button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pickImage();
                }
            });
        }
        setResultText();
    }

    private void pickImage() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
//                    getString(R.string.ph_permission_rationale),
//                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
//        } else {
        boolean showCamera = mShowCamera.getCheckedRadioButtonId() == R.id.show;
        int maxNum = 9;

        if (!TextUtils.isEmpty(mRequestNum.getText())) {
            try {
                maxNum = Integer.valueOf(mRequestNum.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        PhotoHander selector = PhotoHander.create();
        selector.showCamera(showCamera);
        selector.count(maxNum);
        if (mChoiceMode.getCheckedRadioButtonId() == R.id.single) {
            selector.single();
        } else {
            selector.multi();
        }
//        selector.videoOnly();
//        selector.isMustCamera(true);
        selector.heifToJpg();
        selector.isNoSelect();
        selector.addImage(addHttpImage);
        selector.compress(true);
//            selector.isMustCamera(true);
        selector.crop(cropRg.getCheckedRadioButtonId() == R.id.crop);
//            selector.cropCircle(true);
        if (!TextUtils.isEmpty(cropWidthEt.getText()) && !TextUtils.isEmpty(cropHeightEt.getText())) {
            try {
                selector.crop(Integer.valueOf(cropWidthEt.getText().toString()), Integer.valueOf(cropHeightEt.getText().toString()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        selector.selectVideo();
        selector.origin(mSelectPath);
        selector.start(MainActivity.this, result -> {
            mSelectPath = (ArrayList<MediaSelectData>) result;
            setResultText();
        });
//        }
    }

    private void setResultText() {
        if (mSelectPath == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (mSelectPath.size() == 1) {
            Glide.with(this).load(mSelectPath.get(0).compressPath)
                    .into(imageView);
        } else {
            imageView.setImageDrawable(null);
        }
        for (MediaSelectData p : mSelectPath) {
            sb.append(p);
            if (!p.isHttpImg()) {
                try {
                    sb.append("\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            sb.append("\n\n");
        }
        Log.e("结果", mSelectPath.toString());
        mResultText.setText(sb.toString());
    }

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.photo_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.photo_permission_dialog_ok, (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode))
                    .setNegativeButton(R.string.photo_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
