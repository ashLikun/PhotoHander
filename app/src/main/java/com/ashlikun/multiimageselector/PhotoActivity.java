package com.ashlikun.multiimageselector;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ashlikun.glideutils.GlideUtils;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/25　15:47
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：http://p1.pstatp.com/w439/4d8600040238b0064b38.webp
 * http://p9.pstatp.com/w640/4ea2000aca3f308d6583.webp
 */

public class PhotoActivity extends AppCompatActivity {
    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setMinimumScale(0.2f);
        GlideUtils.init(new GlideUtils.OnNeedListener() {
            @Override
            public Application getApplication() {
                return PhotoActivity.this.getApplication();
            }

            @Override
            public boolean isDebug() {
                return BuildConfig.DEBUG;
            }

            @Override
            public String getBaseUrl() {
                return "";
            }
        });
        GlideUtils.show(photoView, "http://p9.pstatp.com/w640/4d8600040238b0064b38.webp");
    }
}
