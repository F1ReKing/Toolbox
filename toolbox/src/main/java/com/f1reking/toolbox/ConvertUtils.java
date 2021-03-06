package com.f1reking.toolbox;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;

/**
 * 转换工具类
 * Created by F1ReKing on 2016/1/2.
 */
public class ConvertUtils {

    private ConvertUtils() {
        throw new Error("Do not need instantiate!");
    }

    /**
     * bitmap 转换成 ByteArray
     */
    public static byte[] bitmap2ByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * byteArray 转换成 Bitmap
     */
    public static Bitmap byteArray2Bitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    /**
     * iamgeUri网络图片 转换成 路径
     */
    public static String imageUri2Path(Context context, Uri contentUri) {
        Cursor c = context.getContentResolver().query(contentUri, null, null, null, null);
        c.moveToFirst();
        String mediaFilePath = c.getString(c.getColumnCount());
        c.close();
        return mediaFilePath;
    }
}
