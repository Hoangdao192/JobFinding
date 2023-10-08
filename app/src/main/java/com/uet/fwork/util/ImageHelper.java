package com.uet.fwork.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageHelper {
    public static final int MAX_IMAGE_WIDTH = 1920;
    public static final int MAX_IMAGE_HEIGHT = 1080;

    /**
     * Giảm độ phân giải của bức ảnh nếu nó vượt quá 1920 x 1080
     * Phục vụ cho việc giảm tải việc lưu trữ ảnh trên Firebase và giảm thời gian load ảnh
     * @param source
     * @return
     */
    public static Bitmap reduceImageSize(Bitmap source) {
        int bitmapWidth = source.getWidth();
        int bitmapHeight = source.getHeight();

        //  Chỉ có chiều rộng vượt ngưỡng cho phép
        if (bitmapWidth > MAX_IMAGE_WIDTH && bitmapHeight <= MAX_IMAGE_HEIGHT) {
            bitmapHeight = MAX_IMAGE_WIDTH * bitmapHeight / bitmapWidth;
            bitmapWidth = MAX_IMAGE_WIDTH;
            System.out.println("FIRST");
        }
        //  Chỉ có chiều dài vượt ngưỡng cho phép
        else if (bitmapHeight > MAX_IMAGE_HEIGHT && bitmapWidth <= MAX_IMAGE_WIDTH) {
            bitmapWidth = MAX_IMAGE_HEIGHT * bitmapWidth / bitmapHeight;
            bitmapHeight = MAX_IMAGE_HEIGHT;
            System.out.println("SECOND");
        }
        //  Cả hai chiều đều vượt mức cho phép
        else if (bitmapHeight > MAX_IMAGE_HEIGHT && bitmapWidth > MAX_IMAGE_WIDTH) {
            //  Scale theo chiều rộng
            if (bitmapWidth > bitmapHeight) {
                bitmapHeight = MAX_IMAGE_WIDTH * bitmapHeight / bitmapWidth;
                bitmapWidth = MAX_IMAGE_WIDTH;
                System.out.println("3");
            }
            //  Scale theo chiều dài
            else {
                bitmapWidth = MAX_IMAGE_HEIGHT * bitmapWidth / bitmapHeight;
                bitmapHeight = MAX_IMAGE_HEIGHT;
                System.out.println("4");
            }
        }

        System.out.println(bitmapWidth + " " + bitmapHeight);
        Bitmap reduce = Bitmap.createScaledBitmap(source, bitmapWidth, bitmapHeight, false);
        return reduce;
    }

    public static Bitmap reduceImageSize(Bitmap source, int maxWidth, int maxHeight) {
        int bitmapWidth = source.getWidth();
        int bitmapHeight = source.getHeight();

        //  Chỉ có chiều rộng vượt ngưỡng cho phép
        if (bitmapWidth > maxWidth && bitmapHeight <= maxHeight) {
            bitmapHeight = maxWidth * bitmapHeight / bitmapWidth;
            bitmapWidth = maxWidth;
            System.out.println("FIRST");
        }
        //  Chỉ có chiều dài vượt ngưỡng cho phép
        else if (bitmapHeight > maxHeight && bitmapWidth <= maxWidth) {
            bitmapWidth = maxHeight * bitmapWidth / bitmapHeight;
            bitmapHeight = maxHeight;
            System.out.println("SECOND");
        }
        //  Cả hai chiều đều vượt mức cho phép
        else if (bitmapHeight > maxHeight && bitmapWidth > maxWidth) {
            //  Scale theo chiều rộng
            if (bitmapWidth > bitmapHeight) {
                bitmapHeight = maxWidth * bitmapHeight / bitmapWidth;
                bitmapWidth = maxWidth;
                System.out.println("3");
            }
            //  Scale theo chiều dài
            else {
                bitmapWidth = maxHeight * bitmapWidth / bitmapHeight;
                bitmapHeight = maxHeight;
                System.out.println("4");
            }
        }
        Bitmap reduce = Bitmap.createScaledBitmap(source, bitmapWidth, bitmapHeight, true);
        return reduce;
    }

    public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static Bitmap loadBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
