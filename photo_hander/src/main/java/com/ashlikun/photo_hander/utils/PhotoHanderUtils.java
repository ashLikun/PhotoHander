package com.ashlikun.photo_hander.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.ashlikun.photo_hander.PhotoHander;
import com.ashlikun.photo_hander.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　18:07
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class PhotoHanderUtils {
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    /**
     * 这张图片是否是网络图
     *
     * @return
     */
    public static boolean isHttpImg(String path) {
        if (path != null && path.startsWith("/")) {
            return false;
        }
        return true;
    }

    public static void setCheck(ImageView imageView, boolean isCheck) {
        if (isCheck) {
            // 设置选中状态
            imageView.setImageResource(R.drawable.ph_btn_selected);
            imageView.setColorFilter(imageView.getResources().getColor(R.color.ph_ok_text_color));
        } else {
            // 未选择
            imageView.setImageResource(R.drawable.ph_btn_unselected);
            imageView.setColorFilter(0xffffffff);
        }
    }


    /**
     * 作者　　: 李坤
     * 创建时间: 2017/6/28 11:25
     * <p>
     * 方法功能：将dip或dp值转换为px值，保证尺寸大小不变
     */

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point out = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(out);
        } else {
            int width = display.getWidth();
            int height = display.getHeight();
            out.set(width, height);
        }
        return out;
    }

    public static String timeFormat(long timeMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public static File createTmpFile(Context context) throws IOException {
        return createTmpFile(context, "Camera");
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     * @throws Exception
     */
    public static long getFileSizes(File file) throws Exception {
        long size = 0;
        try {
            if (!file.isDirectory()) {
                size = size + file.length();
            } else {
                File[] fileList = file.listFiles();
                for (File aFileList : fileList) {
                    if (aFileList.isDirectory()) {
                        size = size + getFileSizes(aFileList);
                    } else {
                        size = size + aFileList.length();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }


    /**
     * 启动拍照
     * 用的权限code
     *
     * @param activityOrfragment 只能是activity或者fragment
     */
    public static File showCameraAction(Object activityOrfragment) {
        Activity activity = null;
        Fragment fragment = null;
        if (activityOrfragment instanceof Fragment) {
            fragment = (Fragment) activityOrfragment;
            activity = fragment.getActivity();
        } else {
            activity = (Activity) activityOrfragment;
        }
        String[] permission = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!PhotoHanderPermission.checkSelfPermission(activity, permission)) {
            PhotoHanderPermission.requestPermission(activityOrfragment, permission, activity.getString(R.string.ph_permission_rationale_camera),
                    PhotoHander.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
        } else {
            File mTmpFile = null;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                try {
                    mTmpFile = PhotoHanderUtils.createTmpFile(activity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mTmpFile != null && mTmpFile.exists()) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
                        if (fragment != null) {
                            fragment.startActivityForResult(intent, PhotoHander.REQUEST_CAMERA);
                        } else {
                            activity.startActivityForResult(intent, PhotoHander.REQUEST_CAMERA);
                        }
                    } else {
                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(MediaStore.Images.Media.DATA, mTmpFile.getAbsolutePath());
                        Uri uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        if (fragment != null) {
                            fragment.startActivityForResult(intent, PhotoHander.REQUEST_CAMERA);
                        } else {
                            activity.startActivityForResult(intent, PhotoHander.REQUEST_CAMERA);
                        }
                    }
                } else {
                    Toast.makeText(activity, R.string.ph_error_image_not_exist, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, R.string.ph_msg_no_camera, Toast.LENGTH_SHORT).show();
            }
            return mTmpFile;
        }
        return null;
    }

    public static File createTmpFile(Context context, String dirStr) throws IOException {
        File dir = null;
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            if (!dir.exists()) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + File.separator + dirStr);
                if (!dir.exists()) {
                    dir = getCacheDirectory(context, true);
                }
            }
        } else {
            dir = getCacheDirectory(context, true);
        }
        return File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, dir);
    }

    public static File createCacheTmpFile(Context context, String dirName) throws IOException {
        return File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, getPhotoCacheDir(context, dirName));
    }


    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link android.content.Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link android.content.Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        } catch (IncompatibleClassChangeError e) { // (sh)it happens too (Issue #989)
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    public static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = getCacheDirectory(context, false);
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }

            File noMedia = new File(cacheDir + "/.nomedia");
            if (!noMedia.mkdirs() && (!noMedia.exists() || !noMedia.isDirectory())) {
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(appCacheDir, cacheDir);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = appCacheDir;
            }
        }
        return individualCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public static int getFiles(File files) {
        if (files == null || !files.exists()) {
            return 0;
        }
        int num = 0;
        File[] childs = files.listFiles();
        for (int j = 0; j < childs.length; j++) {
            if (childs[j].isDirectory()) {
                getFiles(childs[j]);
            } else {
                num++;
            }
        }
        return num;
    }

    /**
     * 根据颜色值自动设置状态栏字体颜色
     *
     * @param color
     */
    public static void autoStatueTextColor(Window window, int color) {
        if (isColorDrak(color)) {
            //浅色文字
            setStatusLightColor(window);
        } else {
            //深色文字
            setStatusDarkColor(window);
        }
    }

    /**
     * 设置状态栏字体颜色为深色
     */
    public static void setStatusDarkColor(Window window) {
        setStatusTextColor(window, true);
    }

    /**
     * 这个颜色是不是深色的
     *
     * @param color
     * @return
     */
    public static boolean isColorDrak(int color) {
        //int t = (color >> 24) & 0xFF;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return r * 0.299 + g * 0.578 + b * 0.114 <= 192;
    }

    /**
     * 设置状态栏字体浅色
     */
    public static void setStatusLightColor(Window window) {
        setStatusTextColor(window, false);
    }

    /**
     * 是否可以设置状态栏颜色
     *
     * @return
     */
    public static boolean isSetStatusTextColor() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void setStatusTextColor(Window window, boolean drak) {
        if (!isSetStatusTextColor()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //判断当前是不是6.0以上的系统
            if (window != null) {
                View view = window.getDecorView();
                if (view != null) {
                    if (drak) {
                        //黑色
                        view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        //白色,就是去除View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        if ((view.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0) {
                            view.setSystemUiVisibility(view.getSystemUiVisibility() ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        }
                    }
                }
            }
        }
    }
}
