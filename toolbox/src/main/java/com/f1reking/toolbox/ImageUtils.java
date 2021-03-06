package com.f1reking.toolbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by F1ReKing on 2016/1/2.
 */
public class ImageUtils {

    private ImageUtils() {
        throw new Error("Do not need instantiate!");
    }

    public static File getPhotoDir(Context context) {
        String path = SDCardUtils.getRootPath(context);
        if (path == null) {
            Toast.makeText(context, "无存储设备", Toast.LENGTH_SHORT).show();
            return null;
        }
        path = path + File.separator + "photo" + File.separator;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.exists()) {
            Toast.makeText(context, "无法创建文件", Toast.LENGTH_SHORT).show();
            return null;
        }
        return dir;
    }

    // 对一张图片进行宽高比例的压缩
    public static Bitmap getImage(String srcPath, int scaleWidth, int scaleHeigth, String waterMark) {
        try {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            // 设置为true时，BitmapFactory.decodeFile(String pathName, Options
            // opts)并不会返回真正的bitmap给我们。
            // 而只会填充了这张图片的属性值返回给我们。
            newOpts.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 这里的bitmap其实是null
            int imageWidth = newOpts.outWidth;// 但我们可以取到属性值
            int imageHeight = newOpts.outHeight;// 因为填充了属性值
            // --------------------------
            int scaleFactor = Math.min(imageWidth / scaleWidth, imageHeight / scaleHeigth);
            if (scaleFactor < 0) {
                scaleFactor = 1;
            }
            newOpts.inSampleSize = scaleFactor;// 设置缩放比例
            newOpts.inJustDecodeBounds = false;
            newOpts.inPurgeable = true;
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
            // 图片方向---------------------------
            ExifInterface exif = new ExifInterface(srcPath);
            if (exif != null) { // 读取图片中相机方向信息
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                int digree = 0;
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        digree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        digree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        digree = 270;
                        break;
                }
                if (digree != 0) {
                    Matrix m = new Matrix();
                    m.postRotate(digree);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                }
            }
            // 打水印---------------------
            if (!TextUtils.isEmpty(waterMark)) {
                return bitmap = mark(bitmap, waterMark);
            } else {
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 添加水印
    public static Bitmap mark(Bitmap src, String watermark) {
        try {
            SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(src, 0, 0, null);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setShadowLayer(1, 1, 1, Color.BLACK);
            paint.setAlpha(100);
            paint.setTextSize(20);
            paint.setAntiAlias(true);
            paint.setUnderlineText(false);
            canvas.drawText(sdFormatter.format(new Date()) + " " + watermark, 20, h - 60, paint);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * 方法名：compressImage
     * 功 能：图片压缩（质量压缩）,取得压缩后的路径
     * 参 数：@param image
     * 参 数：@return
	 * 返回值：Bitmap
	 */
    public static String getCompressImagePath(Context context, String srcPath, int scaleWidth, int scaleHeigth,
        String waterMark, int condense) {

        if (srcPath == null || srcPath.length() <= 0) {
            return "";
        }
        Bitmap image = getImage(srcPath, scaleWidth, scaleHeigth, waterMark);
        if (image == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        Log.i("", "压缩前质量大小======前====" + baos.toByteArray().length / 1024);
        while (baos.toByteArray().length / 1024 > condense) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            options -= 10;// 每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            Log.i("", "压缩" + options + "%后质量大小======后====" + baos.toByteArray().length / 1024);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName = "image" + sdFormatter.format(new Date()) + ".jpg";
        File file = new File(getPhotoDir(context), fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = isBm.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                out.flush();
            }
            baos.close();
            isBm.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out = null;
            }
        }
        if (!image.isRecycled()) {
            image.recycle();
        }
        return file.getAbsolutePath();
    }
}
