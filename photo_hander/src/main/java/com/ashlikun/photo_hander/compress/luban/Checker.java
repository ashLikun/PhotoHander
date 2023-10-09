package com.ashlikun.photo_hander.compress.luban;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.ashlikun.photo_hander.PhotoHanderConst;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class Checker {
    private static final String TAG = "Luban";

    private static final byte[] JPEG_SIGNATURE = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    /**
     * 确定是否为JPG。
     */
    static boolean isJPG(InputStream is) {
        return isJPG(to3ByteArray(is));
    }

    /**
     * 返回顺时针方向的度数。值为0、90、180或270。
     */
    static int getOrientation(InputStream is) {
        return getOrientation(toByteArray(is));
    }

    static private boolean isJPG(byte[] data) {
        if (data == null || data.length < 3) {
            return false;
        }
        return Arrays.equals(JPEG_SIGNATURE, data);
    }

    private static int getOrientation(byte[] jpeg) {
        if (jpeg == null) {
            return 0;
        }

        int offset = 0;
        int length = 0;

        // ISO/IEC 10918-1:1993(E)
        while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
            int marker = jpeg[offset] & 0xFF;

            // Check if the marker is a padding.
            if (marker == 0xFF) {
                continue;
            }
            offset++;

            // 检查标记是SOI还是TEM。
            if (marker == 0xD8 || marker == 0x01) {
                continue;
            }
            // 检查标记是否为EOI或SOS。
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }

            // 获取长度并检查其是否合理。
            length = pack(jpeg, offset, 2, false);
            if (length < 2 || offset + length > jpeg.length) {
                Log.e(TAG, "Invalid length");
                return 0;
            }

            // 如果标记在APP1中为EXIF，则中断。
            if (marker == 0xE1 && length >= 8
                    && pack(jpeg, offset + 2, 4, false) == 0x45786966
                    && pack(jpeg, offset + 6, 2, false) == 0) {
                offset += 8;
                length -= 8;
                break;
            }

            // 跳过其他标记。
            offset += length;
            length = 0;
        }

        // JEITA CP-3451 Exif Version 2.2
        if (length > 8) {
            // Identify the byte order.
            int tag = pack(jpeg, offset, 4, false);
            if (tag != 0x49492A00 && tag != 0x4D4D002A) {
                Log.e(TAG, "Invalid byte order");
                return 0;
            }
            boolean littleEndian = (tag == 0x49492A00);

            // 获取偏移量并检查其是否合理。
            int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
            if (count < 10 || count > length) {
                Log.e(TAG, "Invalid offset");
                return 0;
            }
            offset += count;
            length -= count;

            // 计算并浏览所有元素。
            count = pack(jpeg, offset - 2, 2, littleEndian);
            while (count-- > 0 && length >= 12) {
                // Get the tag and check if it is orientation.
                tag = pack(jpeg, offset, 2, littleEndian);
                if (tag == 0x0112) {
                    int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                    switch (orientation) {
                        case 1:
                            return 0;
                        case 3:
                            return 180;
                        case 6:
                            return 90;
                        case 8:
                            return 270;
                    }
                    Log.e(TAG, "Unsupported orientation");
                    return 0;
                }
                offset += 12;
                length -= 12;
            }
        }

        Log.e(TAG, "Orientation not found");
        return 0;
    }

    static String extSuffix(InputStreamProvider input) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input.open(), null, options);
            String res = options.outMimeType.replace(PhotoHanderConst.MIME_IMAGE_START, ".");
            if (PhotoHanderConst.JPEG.equalsIgnoreCase(res) || PhotoHanderConst.HEIC.equalsIgnoreCase(res) || PhotoHanderConst.HEIF.equalsIgnoreCase(res)) {
                return PhotoHanderConst.JPG;
            }
            return res;
        } catch (Exception e) {
            return PhotoHanderConst.JPG;
        }
    }

    static boolean needCompress(int leastCompressSize, String path) {
        if (leastCompressSize > 0) {
            File source = new File(path);
            return source.exists() && source.length() / 1024.0f > leastCompressSize;
        }
        return true;
    }

    private static int pack(byte[] bytes, int offset, int length, boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }

        int value = 0;
        while (length-- > 0) {
            value = (value << 8) | (bytes[offset] & 0xFF);
            offset += step;
        }
        return value;
    }

    private static byte[] toByteArray(InputStream is) {
        if (is == null) {
            return new byte[0];
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int read;
        byte[] data = new byte[4096];

        try {
            while ((read = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
        } catch (Exception ignored) {
            return new byte[0];
        } finally {
            try {
                buffer.close();
            } catch (IOException ignored) {
            }
        }

        return buffer.toByteArray();
    }

    private static byte[] to3ByteArray(InputStream is) {
        if (is == null) {
            return new byte[0];
        }


        byte[] data = new byte[3];

        try {
            is.read(data, 0, data.length);
        } catch (Exception ignored) {
            return new byte[0];
        } finally {

        }
        return data;
    }
}
