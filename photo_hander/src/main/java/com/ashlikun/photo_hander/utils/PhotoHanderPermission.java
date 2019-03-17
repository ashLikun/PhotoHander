package com.ashlikun.photo_hander.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.ashlikun.photo_hander.PhotoHander;
import com.ashlikun.photo_hander.R;

import java.io.File;

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
     * 作者　　: 李坤
     * 创建时间: 2017/8/30 0030 22:52
     * <p>
     * 方法功能：请求权限
     */

    public static void requestPermission(final Object activityOrfragment, final String[] permission, String rationale, final int requestCode) {
        if (shouldShowRequestPermissionRationale(activityOrfragment, permission)) {
            new AlertDialog.Builder((activityOrfragment instanceof Fragment) ?
                    ((Fragment) activityOrfragment).getContext() : ((Activity) activityOrfragment))
                    .setTitle(R.string.ph_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.ph_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (activityOrfragment instanceof Fragment) {
                                ((Fragment) activityOrfragment).requestPermissions(permission, requestCode);
                            } else {
                                ActivityCompat.requestPermissions((Activity) activityOrfragment, permission, requestCode);
                            }
                        }
                    })
                    .setNegativeButton(R.string.ph_permission_dialog_cancel, null)
                    .create().show();
        } else {
            if (activityOrfragment instanceof Fragment) {
                ((Fragment) activityOrfragment).requestPermissions(permission, requestCode);
            } else {
                ActivityCompat.requestPermissions((Activity) activityOrfragment, permission, requestCode);
            }
        }
    }

    /**
     * 启动拍照后的权限返回
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param activityOrfragment 只能是activity或者fragment
     */
    public static File onRequestPermissionsResult(Object activityOrfragment, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PhotoHander.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION) {
            boolean result = true;
            for (int p : grantResults) {
                if (p != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                }
            }
            if (result) {
                return PhotoHanderUtils.showCameraAction(activityOrfragment);
            }
            return new File("");
        }
        return null;
    }
}
