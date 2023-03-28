package com.ashlikun.photo_hander.utils;

import static android.os.Environment.MEDIA_MOUNTED;

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
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.ashlikun.photo_hander.IntentKey;
import com.ashlikun.photo_hander.PhotoHander;
import com.ashlikun.photo_hander.PhotoLookFragment;
import com.ashlikun.photo_hander.PhotoOptionData;
import com.ashlikun.photo_hander.R;
import com.ashlikun.photo_hander.bean.MediaFile;
import com.ashlikun.photo_hander.crop.Crop;
import com.ashlikun.photo_hander.provider.PhotoHandleProvider;
import com.ashlikun.photoview.PhotoView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 反射字段
     *
     * @param object    要反射的对象
     * @param fieldName 要反射的字段名称
     */
    public static Field setField(Object object, String fieldName, Object value) {
        if (object == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        try {
            Field field = getAllDeclaredField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(object, value);
                return field;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定的字段
     */
    public static Field getAllDeclaredField(Class<?> claxx, String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return null;
        }

        while (claxx != null && claxx != Object.class) {
            try {
                Field f = claxx.getDeclaredField(fieldName);
                if (f != null) {
                    return f;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            claxx = claxx.getSuperclass();
        }
        return null;
    }

    public static <I, O> ActivityResultLauncher<I> registerForActivityResultX(ComponentActivity activity,
                                                                              ActivityResultContract<I, O> contract,
                                                                              final ActivityResultCallback<O> callback
    ) {
        final ActivityResultLauncher<I>[] launcher = new ActivityResultLauncher[1];
        //这种注册需要自己unregister
        launcher[0] = activity.getActivityResultRegistry().register("PhotoForActivityResult" + new AtomicInteger().getAndIncrement(), contract,
                new ActivityResultCallback<O>() {
                    @Override
                    public void onActivityResult(O result) {
                        callback.onActivityResult(result);
                        //这里主动释放
                        if (launcher[0] != null) {
                            launcher[0].unregister();
                        }
                    }
                });
        return launcher[0];
    }

    public static <I, O> ActivityResultLauncher<I> registerForActivityResultXF(Fragment fragment,
                                                                               ActivityResultContract<I, O> contract,
                                                                               final ActivityResultCallback<O> callback
    ) {
        final ActivityResultLauncher<I>[] launcher = new ActivityResultLauncher[1];
        //这种注册需要自己unregister
        launcher[0] = fragment.getActivity().getActivityResultRegistry().register("PhotoForActivityResult" + new AtomicInteger().getAndIncrement(), contract,
                new ActivityResultCallback<O>() {
                    @Override
                    public void onActivityResult(O result) {
                        callback.onActivityResult(result);
                        //这里主动释放
                        launcher[0].unregister();
                    }
                });
        return launcher[0];
    }

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

    public static boolean checkLimit(Activity activity, List selectDatas, PhotoOptionData optionData, MediaFile data) {
        if (optionData.mDefaultCount <= selectDatas.size()) {
            Toast.makeText(activity, activity.getString(R.string.photo_msg_amount_limit, optionData.mDefaultCount), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (optionData.videoMaxDuration > 0 && data.duration / 1000 > optionData.videoMaxDuration) {
            if (optionData.videoMaxDuration < 60) {
                Toast.makeText(activity, activity.getString(R.string.photo_msg_video_duration_limit, optionData.videoMaxDuration + "秒"), Toast.LENGTH_SHORT).show();
            } else if (optionData.videoMaxDuration % 60 == 0) {
                Toast.makeText(activity, activity.getString(R.string.photo_msg_video_duration_limit, optionData.videoMaxDuration / 60 + "分钟"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, activity.getString(R.string.photo_msg_video_duration_limit, optionData.videoMaxDuration / 60 + "分" + optionData.videoMaxDuration % 60 + "秒"), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public static void startVideoPlay(Context context, MediaFile data) {
        if (PhotoHander.onPhotoHandlerListener != null) {
            PhotoHander.onPhotoHandlerListener.onVideoPlay(data);
        } else {
            //实现播放视频的跳转逻辑(调用原生视频播放器)
            //实现播放视频的跳转逻辑(调用原生视频播放器)
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(FileProvider.getUriForFile(context, PhotoHandleProvider.getFileProviderName(context), new File(data.path)), "video/*");
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(new File(data.path)), "video/*");
            }
            context.startActivity(intent);
        }
    }

    /**
     * 启动裁剪页面
     */
    public static void startCrop(Activity activity, MediaFile file, PhotoOptionData optionData) {
        Uri destination = null;
        Uri source = Uri.fromFile(new File(file.path));
        try {
            destination = Uri.fromFile(PhotoHanderUtils.createCacheTmpFile(activity, "crop"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (destination != null && source != null) {
            Crop.of(source, destination)
                    .withSize(optionData.cropWidth, optionData.cropHeight)
                    .showCircle(optionData.cropShowCircle)
                    .color(optionData.cropColor)
                    .start(activity);
        } else {
            Toast.makeText(activity, R.string.photo_error_image_not_exist, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动查看照片和视频界面
     */
    public static void startLook(FragmentActivity activity, List<MediaFile> imageList, List<MediaFile> selectList, int position, MediaFile currentData) {
        try {
            //检查是否有PhotoView库
            Class.forName(PhotoView.class.getName());
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(IntentKey.EXTRA_ADAPTER_SHOW_DATA, (ArrayList<? extends Parcelable>) imageList);
            bundle.putParcelableArrayList(IntentKey.EXTRA_DEFAULT_SELECTED_LIST, (ArrayList<? extends Parcelable>) selectList);
            bundle.putInt(IntentKey.EXTRA_ADAPTER_CLICK_POSITION, position);
            bundle.putParcelable(IntentKey.EXTRA_ADAPTER_CLICK_DATA, currentData);
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag("PhotoLookFragment");
            if (fragment != null) {
                ft.remove(fragment);
            }
            fragment = Fragment.instantiate(activity, PhotoLookFragment.class.getName(), bundle);
            ft.setCustomAnimations(R.anim.mis_anim_fragment_lookphotp_in, R.anim.mis_anim_fragment_lookphotp_out)
                    .add(android.R.id.content, fragment, "PhotoLookFragment")
                    .commitAllowingStateLoss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void setCheck(ImageView imageView, boolean isCheck) {
        if (isCheck) {
            // 设置选中状态
            imageView.setImageResource(R.drawable.ph_btn_selected);
            imageView.setColorFilter(imageView.getResources().getColor(R.color.ph_check_selected_color));
        } else {
            // 未选择
            imageView.setImageResource(R.drawable.ph_btn_unselected);
            imageView.setColorFilter(imageView.getResources().getColor(R.color.ph_check_unselected_color));
        }
    }

    /**
     * 获取视频时长（格式化）
     *
     * @param timestamp
     * @return
     */
    public static String getVideoDuration(long timestamp) {
        if (timestamp < 1000) {
            return "00:01";
        }
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(date);
    }

    /**
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
    public static long getFileSizes(File file) {
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
     * 获取拍照Intent
     *
     * @return null:失败
     */
    public static Pair<File, Intent> getImageCapterIntent(Activity activity) {
        boolean isSuccess = false;
        File tmpFile = null;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            tmpFile = PhotoHanderUtils.createTmpFile(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tmpFile != null && tmpFile.exists()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
            } else {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, tmpFile.getAbsolutePath());
                Uri uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
            isSuccess = true;
        } else {
            Toast.makeText(activity, R.string.photo_error_image_not_exist, Toast.LENGTH_SHORT).show();
        }
        return isSuccess ? new Pair<>(tmpFile, intent) : null;
    }

    public static void permissionStorage(ComponentActivity activity, final Runnable call) {
        String[] permission = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PhotoHanderPermission.requestPermission(activity, permission, activity.getString(R.string.photo_permission_rationale_camera), call);
    }

    /**
     * 启动拍照
     * 用的权限code
     */
    public static void showCameraAction(final ComponentActivity activity, final ShowCameraActionCall call) {
        permissionStorage(activity, new Runnable() {
            @Override
            public void run() {
                final Pair<File, Intent> intent = getImageCapterIntent(activity);
                if (intent != null) {
                    try {
                        activity.getActivityResultRegistry().register("ForActivityResult" + new AtomicInteger().getAndIncrement(), new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                call.call(new Pair(intent.first, result));
                            }
                        }).launch(intent.second);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

    public static int getFileCount(File files) {
        if (files == null || !files.exists()) {
            return 0;
        }
        int num = 0;
        File[] childs = files.listFiles();
        for (int j = 0; j < childs.length; j++) {
            if (childs[j].isDirectory()) {
                getFileCount(childs[j]);
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

    /**
     * 过滤目录名称
     */
    public static String getFolderName(Context context, String name) {

        if (PhotoOptionData.currentData != null && PhotoOptionData.currentData.isFilterFolder && name != null) {
            if (name.equalsIgnoreCase("camera")) {
                return context.getString(R.string.photo_folder_camera);
            } else if (name.equalsIgnoreCase("screenshots")) {
                return context.getString(R.string.photo_folder_screenshots);
            } else if (name.equalsIgnoreCase("weixin")) {
                return context.getString(R.string.photo_folder_weixin);
            }
        }
        return name;
    }

    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    public static float parseFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
