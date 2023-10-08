package com.ashlikun.photo_hander.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ashlikun.photo_hander.R;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作者　　: 李坤
 * 创建时间: 2019/3/17　13:12
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：权限相关
 */
public class PhotoHanderPermission {
    /**
     * 是否有权限
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkSelfPermission(Context context, String[] permission) {
        if (permission == null) {
            return true;
        }
        for (String p : permission) {
            if (ActivityCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否拒绝过一次权限
     */
    public static boolean shouldShowRequestPermissionRationale(Object activityOrfragment, String[] permissions) {
        if (permissions == null) {
            return true;
        }
        for (String p : permissions) {
            if (activityOrfragment instanceof Fragment) {
                if (((Fragment) activityOrfragment).shouldShowRequestPermissionRationale(p)) {
                    return true;
                }
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) activityOrfragment, p)) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * 请求权限
     */
    public static void requestPermission(final ComponentActivity activity, final String[] permission, String rationale, final Runnable call) {
        if (permission.length == 0) {
            call.run();
            return;
        }
        if (!PhotoHanderPermission.checkSelfPermission(activity, permission)) {
            if (shouldShowRequestPermissionRationale(activity, permission)) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.photo_permission_dialog_title)
                        .setMessage(rationale)
                        .setPositiveButton(R.string.photo_permission_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissionRegister(activity, permission, call);
                            }
                        })
                        .setNegativeButton(R.string.photo_permission_dialog_cancel, null)
                        .create().show();
            } else {
                requestPermissionRegister(activity, permission, call);
            }
        } else {
            call.run();
        }
    }

    /**
     * 获取请求权限
     */
    public static void requestPermissionRegister(final ComponentActivity activity, final String[] permission, final Runnable call) {
        registerRequestPermissionX(activity, new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result != null) {
                    if (!result.values().contains(false)) {
                        call.run();
                    }
                }
            }
        }).launch(permission);
    }

    public static ActivityResultLauncher<String[]> registerRequestPermissionX(ComponentActivity activity, final ActivityResultCallback<Map<String, Boolean>> callback) {
        final ActivityResultLauncher<String[]>[] launcher = new ActivityResultLauncher[1];
        //这种注册需要自己unregister
        launcher[0] = activity.getActivityResultRegistry().register("PhotoForActivityResult" + new AtomicInteger().getAndIncrement(), new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        //这里主动释放
                        if (launcher[0] != null) {
                            launcher[0].unregister();
                        }
                        callback.onActivityResult(result);
                    }
                });
        return launcher[0];
    }

    public static ActivityResultLauncher<String[]> registerForActivityResultX(Fragment fragment, final ActivityResultCallback<Map<String, Boolean>> callback) {
        return registerRequestPermissionX(fragment.getActivity(), callback);
    }
}
