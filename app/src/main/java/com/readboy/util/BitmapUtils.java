package com.readboy.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.readboy.bean.old.Location;
import com.readboy.log.LogUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class BitmapUtils {
    private static int WIDTH_BLOCK = 40;
    private static int HEIGHT_BLOCK = 40;

    public static Bitmap cropBitmap(List<Point> points, Bitmap bitmap){
        Point bitmapTopLeft = points.get(0);
        Point bitmapTopRight=points.get(1);
        Point bitmapBottomLeft=points.get(2);
        Point bitmapBottomRight=points.get(3);
        Rect cropRect = new Rect(
                Math.min((int)bitmapTopLeft.x, (int)bitmapBottomLeft.x),
                Math.min((int)bitmapTopLeft.y, (int)bitmapTopRight.y),
                Math.max((int)bitmapBottomRight.x, (int)bitmapTopRight.x),
                Math.max((int)bitmapBottomRight.y, (int)bitmapBottomLeft.y));

        if(cropRect.left <= 0 ||  cropRect.top <= 0 || cropRect.width() <=0 || cropRect.height() <= 0 ){
            LogUtils.e("cropBitmap == is error");
            return bitmap;

        }
        Bitmap cut = Bitmap.createBitmap(
                bitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
        );



        android.graphics.Point cutTopLeft = new android.graphics.Point();
        android.graphics.Point cutTopRight = new android.graphics.Point();
        android.graphics.Point cutBottomLeft = new android.graphics.Point();
        android.graphics.Point cutBottomRight = new android.graphics.Point();

        cutTopLeft.x = (int)(bitmapTopLeft.x > bitmapBottomLeft.x ? bitmapTopLeft.x - bitmapBottomLeft.x : 0);
        cutTopLeft.y = (int)( bitmapTopLeft.y > bitmapTopRight.y ? bitmapTopLeft.y - bitmapTopRight.y : 0);

        cutTopRight.x = (int)(bitmapTopRight.x > bitmapBottomRight.x ? cropRect.width() : cropRect.width() - Math.abs(bitmapBottomRight.x - bitmapTopRight.x));
        cutTopRight.y = (int)(bitmapTopLeft.y > bitmapTopRight.y ? 0 : Math.abs(bitmapTopLeft.y - bitmapTopRight.y));

        cutBottomLeft.x = (int)(bitmapTopLeft.x > bitmapBottomLeft.x ? 0 : Math.abs(bitmapTopLeft.x - bitmapBottomLeft.x));
        cutBottomLeft.y = (int)(bitmapBottomLeft.y > bitmapBottomRight.y ? cropRect.height() : cropRect.height() - Math.abs(bitmapBottomRight.y - bitmapBottomLeft.y));

        cutBottomRight.x = (int)(bitmapTopRight.x > bitmapBottomRight.x ? cropRect.width() - Math.abs(bitmapBottomRight.x - bitmapTopRight.x) : cropRect.width());
        cutBottomRight.y = (int)(bitmapBottomLeft.y > bitmapBottomRight.y ? cropRect.height() - Math.abs(bitmapBottomRight.y - bitmapBottomLeft.y) : cropRect.height());




        float width = cut.getWidth();
        float height = cut.getHeight();

        float[] src = new float[]{cutTopLeft.x, cutTopLeft.y, cutTopRight.x, cutTopRight.y, cutBottomRight.x, cutBottomRight.y, cutBottomLeft.x, cutBottomLeft.y};
        float[] dst = new float[]{0, 0, width, 0, width, height, 0, height};

        Matrix matrix = new Matrix();
        matrix.setPolyToPoly(src, 0, dst, 0, 4);
        Bitmap stretch = Bitmap.createBitmap(cut.getWidth(), cut.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas stretchCanvas = new Canvas(stretch);
        stretchCanvas.concat(matrix);
        stretchCanvas.drawBitmapMesh(cut, WIDTH_BLOCK, HEIGHT_BLOCK, generateVertices(cut.getWidth(), cut.getHeight()), 0, null, 0, null);
        return stretch;
    }

    private  static float[] generateVertices(int widthBitmap, int heightBitmap) {

        float[] vertices=new float[(WIDTH_BLOCK+1)*(HEIGHT_BLOCK+1)*2];

        float widthBlock = (float)widthBitmap/WIDTH_BLOCK;
        float heightBlock = (float)heightBitmap/HEIGHT_BLOCK;

        for(int i=0;i<=HEIGHT_BLOCK;i++)
            for(int j=0;j<=WIDTH_BLOCK;j++) {
                vertices[i * ((HEIGHT_BLOCK+1)*2) + (j*2)] = j * widthBlock;
                vertices[i * ((HEIGHT_BLOCK+1)*2) + (j*2)+1] = i * heightBlock;
            }
        return vertices;
    }


    public static List<Point> getImagePoint(MatOfPoint2f approxCurve){
        double[] temp_double=approxCurve.get(0,0);
        Point point1=new Point(temp_double[0],temp_double[1]);
        temp_double=approxCurve.get(1,0);
        Point point2=new Point(temp_double[0],temp_double[1]);
        temp_double=approxCurve.get(2,0);
        Point point3=new Point(temp_double[0],temp_double[1]);
        temp_double=approxCurve.get(3,0);
        Point point4=new Point(temp_double[0],temp_double[1]);

        List<Point> source=new ArrayList<>();
        source.add(point1);
        source.add(point2);
        source.add(point3);
        source.add(point4);
        //对4个点进行排序
        Point centerPoint=new Point(0,0);//质心
        for (Point corner:source){
            centerPoint.x+=corner.x;
            centerPoint.y+=corner.y;
        }
        centerPoint.x=centerPoint.x/source.size();
        centerPoint.y=centerPoint.y/source.size();
        Point lefttop=new Point();
        Point righttop=new Point();
        Point leftbottom=new Point();
        Point rightbottom=new Point();
        for (int i=0;i<source.size();i++){
            if (source.get(i).x<centerPoint.x&&source.get(i).y<centerPoint.y){
                lefttop=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y<centerPoint.y){
                righttop=source.get(i);
            }else if (source.get(i).x<centerPoint.x&& source.get(i).y>centerPoint.y){
                leftbottom=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y>centerPoint.y){
                rightbottom=source.get(i);
            }
        }
        source.clear();
        source.add(lefttop);
        source.add(righttop);
        source.add(leftbottom);
        source.add(rightbottom);
        return source;
    }

    public static String getFilePath(Context context,int i){
        String root = context.getExternalCacheDir().getAbsolutePath();
        String dirName = "erweima16";
        File appDir = new File(root , dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        //文件名为时间
        String fileName = "Image" + i + ".jpg";

        //获取文件
        File file = new File(appDir, fileName);
        return file.getPath();
    }

    public static void savePicAsBitmap(Mat src,Context context, int i){
        Bitmap bitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bitmap);
        BitmapUtils.saveImageToGallery(bitmap,context,i);
    }

    public static int saveImageToGallery(Bitmap bmp, Context context,int i) {
        //生成路径
        String root = context.getExternalCacheDir().getAbsolutePath();
        String dirName = "erweima16";
        File appDir = new File(root , dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        //文件名为时间
        String fileName = "Image" + i + ".jpg";

        //获取文件
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            //通知系统相册刷新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(file.getPath()))));
            return 2;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static Bitmap getRectangleBitmap(Drawable drawable, Location location){
        Bitmap tempBitmap = drawableToBitmap(drawable);
        Canvas canvas = new Canvas(tempBitmap);

        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(3);  //线的宽度
        canvas.drawRect(location.getTop_left().getX(), location.getTop_left().getY(), location.getRight_bottom().getX(), location.getRight_bottom().getY(), paint);
        return tempBitmap;

    }

    public static Bitmap getRectangleBitmap(Bitmap tempBitmap,com.readboy.bean.newexam.Location location){
        Canvas canvas = new Canvas(tempBitmap);

        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(3);  //线的宽度
        canvas.drawRect((int)location.getTop_left().x,(int) location.getTop_left().y, (int)location.getRight_bottom().x, (int)location.getRight_bottom().y, paint);
        return tempBitmap;

    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
