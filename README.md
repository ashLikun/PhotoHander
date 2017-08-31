# PhotoHander
# 图片选择  图片裁剪 图片压缩
## 权限
    - READ_EXTERNAL_STORAGE
    - WRITE_EXTERNAL_STORAGE
    - CAMERA
## 鲁班压缩算法
### 判断图片比例值，是否处于以下区间内；
    [1, 0.5625) 即图片处于 [1:1 ~ 9:16) 比例范围内
    [0.5625, 0.5) 即图片处于 [9:16 ~ 1:2) 比例范围内
    [0.5, 0) 即图片处于 [1:2 ~ 1:∞) 比例范围内

### 判断图片最长边是否过边界值；
    [1, 0.5625) 边界值为：1664 * n（n=1）, 4990 * n（n=2）, 1280 * pow(2, n-1)（n≥3）
    [0.5625, 0.5) 边界值为：1280 * pow(2, n-1)（n≥1）
    [0.5, 0) 边界值为：1280 * pow(2, n-1)（n≥1）

### 计算压缩图片实际边长值
    以第2步计算结果为准，超过某个边界值则：width / pow(2, n-1)，height/pow(2, n-1)

### 计算压缩图片的实际文件大小，
    以第2、3步结果为准，图片比例越大则文件越大。
    size = (newW * newH) / (width * height) * m；

    [1, 0.5625) 则 width & height 对应 1664，4990，1280 * n（n≥3），m 对应 150，300，300；
    [0.5625, 0.5) 则 width = 1440，height = 2560, m = 200；
    [0.5, 0) 则 width = 1280，height = 1280 / scale，m = 500；注：scale为比例值

### 判断第4步的size是否过小

    [1, 0.5625) 则最小 size 对应 60，60，100
    [0.5625, 0.5) 则最小 size 都为 100
    [0.5, 0) 则最小 size 都为 100

    将前面求到的值压缩图片 width, height, size 传入压缩流程，压缩图片直到满足以上数值

### 效果与对比
|       方式      |      原图      |    Luban     |    Wechat   |
|-----------------|---------------|---------------|-------------|
|截屏 720P|720*1280,390k|720*1280,87k|720*1280,56k|
|截屏 1080P|1080*1920,2.21M|1080*1920,104k|1080*1920,112k|
|拍照 13M(4:3)|3096*4128,3.12M|1548*2064,141k|1548*2064,147k|
|拍照 9.6M(16:9)|4128*2322,4.64M|1032*581,97k|1032*581,74k|
|滚动截屏 1080*6433|1080*6433,1.56M|1080*6433,351k|1080*6433,482k|

## 截图
![Example1](art/example_1.png) ![Select1](art/select_1.png) ![Select2](art/select_2.png) ![Select3](art/select_3.png)

## 使用
####    权限
            <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
            <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            <uses-permission android:name="android.permission.CAMERA" />
####    引用activity
            <activity
                android:name="com.hubng.photo_hander.PhotoHanderActivity"
                android:configChanges="orientation|screenSize"
                android:label="图片选择" />
            <activity
                android:name="com.hubng.photo_hander.crop.CropImageActivity"
                android:configChanges="orientation|screenSize"
                android:label="图片裁剪" />
####    Java代码
            PhotoHander selector = PhotoHander.create();
            selector.showCamera(showCamera);
            selector.count(maxNum);
            if (mChoiceMode.getCheckedRadioButtonId() == R.id.single) {
                selector.single();
                selector.crop(true);
            } else {
                selector.multi();
            }
            selector.compress(true);

            selector.origin(mSelectPath);
            selector.start(MainActivity.this, REQUEST_IMAGE);

          //返回
          @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == REQUEST_IMAGE) {
                    if (resultCode == RESULT_OK) {
                       ArrayList<String> mSelectPath =  PhotoHander.getIntentResult(data);
                    }
                }
            }
####    依赖
            compile 'com.github.bumptech.glide:glide:3.7.0'
            //rxjava
            compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
            compile 'io.reactivex.rxjava2:rxjava:2.y.z'
            compile 'com.github.ashLikun:PhotoHander:1.2.6'

