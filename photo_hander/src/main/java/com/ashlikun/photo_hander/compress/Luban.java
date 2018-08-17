package com.ashlikun.photo_hander.compress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;

import com.ashlikun.photo_hander.bean.ImageSelectData;
import com.ashlikun.photo_hander.utils.PhotoHanderUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * 作者　　: 李坤
 * 创建时间: 2017/4/19 0019 16:52
 * <p>
 * 方法功能：
 * 判断图片比例值，是否处于以下区间内；
 * <p>
 * [1, 0.5625) 即图片处于 [1:1 ~ 9:16) 比例范围内
 * [0.5625, 0.5) 即图片处于 [9:16 ~ 1:2) 比例范围内
 * [0.5, 0) 即图片处于 [1:2 ~ 1:∞) 比例范围内
 * 判断图片最长边是否过边界值；
 * <p>
 * [1, 0.5625) 边界值为：1664 * n（n=1）, 4990 * n（n=2）, 1280 * pow(2, n-1)（n≥3）
 * [0.5625, 0.5) 边界值为：1280 * pow(2, n-1)（n≥1）
 * [0.5, 0) 边界值为：1280 * pow(2, n-1)（n≥1）
 * 计算压缩图片实际边长值，以第2步计算结果为准，超过某个边界值则：width / pow(2, n-1)，height/pow(2, n-1)
 * <p>
 * 计算压缩图片的实际文件大小，以第2、3步结果为准，图片比例越大则文件越大。
 * <p>
 * size = (newW * newH) / (width * height) * m；
 * <p>
 * [1, 0.5625) 则 width & height 对应 1664，4990，1280 * n（n≥3），m 对应 150，300，300；
 * [0.5625, 0.5) 则 width = 1440，height = 2560, m = 200；
 * [0.5, 0) 则 width = 1280，height = 1280 / scale，m = 500；注：scale为比例值
 * 判断第4步的size是否过小
 * <p>
 * [1, 0.5625) 则最小 size 对应 60，60，100
 * [0.5625, 0.5) 则最小 size 都为 100
 * [0.5, 0) 则最小 size 都为 100
 * 将前面求到的值压缩图片 width, height, size 传入压缩流程，压缩图片直到满足以上数值
 * 效果与对比
 * 内容	原图	Luban	Wechat
 * 截屏 720P	720*1280,390k	720*1280,87k	720*1280,56k
 * 截屏 1080P	1080*1920,2.21M	1080*1920,104k	1080*1920,112k
 * 拍照 13M(4:3)	3096*4128,3.12M	1548*2064,141k	1548*2064,147k
 * 拍照 9.6M(16:9)	4128*2322,4.64M	1032*581,97k	1032*581,74k
 * 滚动截屏	1080*6433,1.56M	1080*6433,351k	1080*6433,482k
 */

public class Luban {

    public static final int FIRST_GEAR = 1;//1级压缩
    public static final int THIRD_GEAR = 3;//3级压缩
    /**
     * 缓存文件大于100个自动删除
     */
    public static final int MAX_SAVE_FILS = 100;

    private static final String TAG = "Luban";
    private static String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private static volatile Luban INSTANCE;

    private final File mCacheDir;

    private OnCompressListener compressListener;
    private int gear = THIRD_GEAR;
    private ArrayList<String> mFiles = new ArrayList<>();
    private ArrayList<ImageSelectData> compressFiles = new ArrayList<ImageSelectData>();

    private Luban(File cacheDir) {
        mCacheDir = cacheDir;
    }


    public static void deleteDir(Context context) {
        File cacheDir = getPhotoCacheDir(context);
        deleteDir(cacheDir);
    }

    public static void deleteDir(File cacheDir) {
        if (cacheDir != null) {
            if (cacheDir.exists()) {
                if (getFiles(cacheDir) > MAX_SAVE_FILS) {
                    cacheDir.delete();
                }
            }
        }
    }


    private static synchronized File getPhotoCacheDir(Context context) {
        return PhotoHanderUtils.getPhotoCacheDir(context, Luban.DEFAULT_DISK_CACHE_DIR);
    }


    public static Luban get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Luban(Luban.getPhotoCacheDir(context));
        }
        return INSTANCE;
    }

    public Luban launch() {

        if (compressListener != null) {
            compressListener.onStart();
        }

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                deleteDir(mCacheDir);
                compressFiles.clear();
                for (String f : mFiles) {
                    File ff = compress(f);
                    if (ff != null && ff.exists()) {
                        compressFiles.add(new ImageSelectData(f, ff.getPath()));
                        int progress = mFiles.indexOf(f) + 1;
                        e.onNext(progress);
                    }
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (compressListener != null) {
                            compressListener.onError(throwable);
                        }
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (compressListener != null) {
                            compressListener.onSuccess(compressFiles);
                        }
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer progress) {
                        if (compressListener != null) {
                            compressListener.onLoading(progress, mFiles.size());
                        }
                    }
                });


        return this;
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2016/9/8 10:05
     * <p>
     * 方法功能：放入待压缩的图片文件集合
     */
    public Luban load(List<String> file) {
        mFiles.clear();
        mFiles.addAll(file);
        return this;
    }


    public Luban setCompressListener(OnCompressListener listener) {
        compressListener = listener;
        return this;
    }

    public Luban putGear(int gear) {
        this.gear = gear;
        return this;
    }


    private File compress(@NonNull String file) {
        try {
            //是否存在缓存
            File tempFile = getTempFile(file);
            if (tempFile != null) {
                return tempFile;
            }
            File ff = new File(file);
            if (gear == Luban.THIRD_GEAR) {

                ff = thirdCompress(ff);

            } else if (gear == Luban.FIRST_GEAR) {
                ff = firstCompress(ff);
            } else {
                return null;
            }
            if (ff == null) {
                return null;
            }
            return ff;
        } catch (Exception e) {
            return null;
        }

    }

    private File thirdCompress(@NonNull File file) {
        double size;//期望大小  kb
        String filePath = file.getAbsolutePath();
        int[] imgSize = getImageSize(filePath);
        int angle = getImageSpinAngle(filePath);
        int width = imgSize[0];
        int height = imgSize[1];
        int thumbW = width % 2 == 1 ? width + 1 : width;
        int thumbH = height % 2 == 1 ? height + 1 : height;

        //一直保证width比height小，   那么除出来的就是  （0,1）
        width = thumbW > thumbH ? thumbH : thumbW;
        height = thumbW > thumbH ? thumbW : thumbH;

        //（1，0）
        double scale = ((double) width / height);
        //即图片处于 [1:1 ~ 9:16) 比例范围内
        if (scale <= 1 && scale > 0.5625) {
            if (height < 1664) {
                if (file.length() / 1024 < 100) {
                    //文件大小小于100kb  就不压缩
                    return file;
                }

                size = (width * height) / Math.pow(1664, 2) * 100;
                //希望（60-100）kb
                size = size < 60 ? 60 : size;
            } else if (height < 4990) {
                thumbW = width / 2;
                thumbH = height / 2;
                size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
                //希望（60-300）kb
                size = size < 60 ? 60 : size;
            } else if (height < 10240) {
                thumbW = width / 4;
                thumbH = height / 4;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                //希望（100-300）kb
                size = size < 100 ? 100 : size;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbW = width / multiple;
                thumbH = height / multiple;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                //希望（100-300）kb
                size = size < 100 ? 100 : size;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            //即图片处于 [9:16 ~ 1:2) 比例范围内
            if (height < 1280 && file.length() / 1024 < 100) {
                //文件大小小于100kb  就不压缩
                return file;
            }
            int multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (thumbW * thumbH) / (1440.0 * 2560.0) * 400;
            //希望（100-400）kb
            size = size < 100 ? 100 : size;
        } else {
            //即图片处于 [1:2 ~ 1:∞) 比例范围内
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
            //希望（100-500）kb
            size = size < 100 ? 100 : size;
        }

        return compress(filePath, createTempFile(filePath), thumbW, thumbH, angle, (long) size);
    }

    private File firstCompress(@NonNull File file) {
        int minSize = 60;
        int longSide = 720;
        int shortSide = 1280;

        String filePath = file.getAbsolutePath();

        long size = 0;
        long maxSize = file.length() / 5;

        int angle = getImageSpinAngle(filePath);
        int[] imgSize = getImageSize(filePath);
        int width = 0, height = 0;
        if (imgSize[0] <= imgSize[1]) {
            double scale = (double) imgSize[0] / (double) imgSize[1];
            if (scale <= 1.0 && scale > 0.5625) {
                width = imgSize[0] > shortSide ? shortSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = minSize;
            } else if (scale <= 0.5625) {
                height = imgSize[1] > longSide ? longSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = maxSize;
            }
        } else {
            double scale = (double) imgSize[1] / (double) imgSize[0];
            if (scale <= 1.0 && scale > 0.5625) {
                height = imgSize[1] > shortSide ? shortSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = minSize;
            } else if (scale <= 0.5625) {
                width = imgSize[0] > longSide ? longSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = maxSize;
            }
        }

        return compress(filePath, createTempFile(filePath), width, height, angle, size);
    }

    /**
     * 获取图片的宽高
     */
    public int[] getImageSize(String imagePath) {
        int[] res = new int[2];

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);

        res[0] = options.outWidth;
        res[1] = options.outHeight;

        return res;
    }

    /**
     * 按照指定的宽高压缩
     *
     * @param imagePath 目标图片
     * @param width     压缩宽度
     * @param height    压缩高度
     * @return {@link Bitmap}
     */
    private Bitmap compress(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;

        if (outH > height || outW > width) {
            int halfH = outH / 2;
            int halfW = outW / 2;

            while ((halfH / inSampleSize) > height && (halfW / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;

        int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    /**
     * 获得图像旋转角度
     *
     * @param path 目标文件路径
     */
    private int getImageSpinAngle(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 指定参数压缩图片
     *
     * @param largeImagePath the big image path
     * @param thumbFilePath  the thumbnail path
     * @param width          width of thumbnail
     * @param height         height of thumbnail
     * @param angle          rotation angle of thumbnail
     * @param size           the file size of image
     */
    private File compress(String largeImagePath, String thumbFilePath, int width, int height, int angle, long size) {
        Bitmap thbBitmap = compress(largeImagePath, width, height);

        thbBitmap = rotatingImage(angle, thbBitmap);

        return saveImage(thumbFilePath, thbBitmap, size);
    }

    /**
     * 旋转图片
     * rotate the image with specified angle
     *
     * @param angle  the angle will be rotating 旋转的角度
     * @param bitmap target image               目标图片
     */
    private static Bitmap rotatingImage(int angle, Bitmap bitmap) {
        //rotate image
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        //create a new image
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 保存图片到指定路径
     * Save image with specified size
     *
     * @param filePath the image file save path 储存路径
     * @param bitmap   the image what be save   目标图片
     * @param size     the file size of image   期望大小
     */
    private File saveImage(String filePath, Bitmap bitmap, long size) {
        if (filePath == null || bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //图片质量
        int options = 100;
        //压缩图片
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
        while (stream.toByteArray().length / 1024.0 > size && options > 6) {
            stream.reset();
            options -= 6;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
        }

        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(filePath);
    }


    public String createTempFile(String orginFile) {
        try {
            File file = new File(mCacheDir, Math.abs(orginFile.hashCode()) + PhotoHanderUtils.getFileSuffix(orginFile));
            file.createNewFile();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件夹中已经存在的文件
     */

    public File getTempFile(String orginFile) {
        String tempPath = mCacheDir.getAbsolutePath() + File.separator +
                Math.abs(orginFile.hashCode()) + PhotoHanderUtils.getFileSuffix(orginFile);
        File file = new File(tempPath);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static int getFiles(File mCacheDir) {
        if (mCacheDir == null || !mCacheDir.exists()) {
            return 0;
        }
        int num = 0;
        File[] childs = mCacheDir.listFiles();
        for (int j = 0; j < childs.length; j++) {
            if (childs[j].isDirectory()) {
                getFiles(childs[j]);
            } else {
                num++;
            }
        }
        return num;
    }
}