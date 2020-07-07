package com.readboy.myopencvcamera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "TAG_CameraActivity";
    private int WIDTH_BLOCK = 40;
    private int HEIGHT_BLOCK = 40;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private static final int VIEW_MODE_RGBA = 0;
    private static final int VIEW_MODE_GRAY = 1;
    private static final int VIEW_MODE_CANNY = 2;
    private static final int VIEW_MODE_CLICK = 3;
    private static final int VIEW_MODE_FEATURES = 5;

    private int mViewMode;
    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private boolean hasSaved;
    private ImageView ivShow;

    /**
     * 第一次创建时调用
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "called onCreate");
        //权限检查
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    2233);

        }
        //将窗口变亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javaCameraView);
        ivShow = (ImageView) findViewById(R.id.iv_show);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     *       *创建菜单
     *  
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     *      *选择菜单项的处理
     *  
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.grayItem:
                ivShow.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mViewMode = VIEW_MODE_GRAY;
                break;
            case R.id.rgbItem:
                ivShow.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mViewMode = VIEW_MODE_RGBA;
                break;
            case R.id.cannyItem:
                ivShow.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mViewMode = VIEW_MODE_CANNY;
                break;
            case R.id.exitItem:
                finish();
                break;
            case R.id.clickItem:
                 //savePicture(mRgba);
                //showCropPicture(mRgba);
                showAutoCropPicture(mRgba);
                ivShow.setVisibility(View.VISIBLE);
                mOpenCvCameraView.setVisibility(View.GONE);
                mViewMode = VIEW_MODE_CLICK;
              break;
        }
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        switch (viewMode) {
            case VIEW_MODE_GRAY:
                hasSaved = false;
                Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_RGBA:

                hasSaved = false;
                mRgba = inputFrame.rgba();
                break;
            case VIEW_MODE_CANNY:
                hasSaved = false;
                mRgba = inputFrame.rgba();
                Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_CLICK:

                break;
        }

        return mRgba;
    }

    private Mat processImage( Mat gray ) {
        Mat b = new Mat();
        //高斯模糊效果较好，size里的参数只能为奇数
        Imgproc.GaussianBlur(gray,b, new Size(7,7),0);
       // Imgproc.medianBlur( gray, b, 3);
        Mat t = new Mat();
        Imgproc.threshold(b, t, 125, 300, THRESH_BINARY);
        return t;
    }


    public static List<Point> getCornersByContour(Mat imgsource){
            List<MatOfPoint> contours=new ArrayList<>();
            //轮廓检测
            Imgproc.findContours(imgsource,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
            Log.d(TAG,"findContours size = " + contours.size());
            double maxArea=-1;
            int maxAreaIdx=-1;
            MatOfPoint temp_contour=contours.get(0);//假设最大的轮廓在index=0处
            MatOfPoint2f approxCurve=new MatOfPoint2f();
            for (int idx=0;idx<contours.size();idx++){
                temp_contour=contours.get(idx);
                double contourarea=Imgproc.contourArea(temp_contour);
                Log.d(TAG,"findContours area = " + contourarea);

                //当前轮廓面积比最大的区域面积大就检测是否为四边形
                if (contourarea > maxArea){
                    //检测contour是否是四边形
                    MatOfPoint2f new_mat=new MatOfPoint2f(temp_contour.toArray());
                    int contourSize= (int) temp_contour.total();
                    MatOfPoint2f approxCurve_temp=new MatOfPoint2f();
                    //对图像轮廓点进行多边形拟合
                    Imgproc.approxPolyDP(new_mat,approxCurve_temp,contourSize*0.07,true);
                    if (approxCurve_temp.total()==4){
                        maxArea=contourarea;
                        maxAreaIdx=idx;
                        approxCurve=approxCurve_temp;
                        Log.d(TAG,"findContours22222 area = " + contourarea);

                    }
                }
            }
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

    private void savePicture(Mat frame){
        Mat frameData = processImage(frame);
        List<Mat> mats = new ArrayList<>();
        //多通道分离出单通道
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        String name = System.currentTimeMillis() + "output_image.jpg";
        String pathResult = getExternalFilesDir("Pictures").getPath() + "/" + name;
        String fileName = pathResult + ".jpg";
        Imgcodecs.imwrite(fileName, frameData);
        //ivShow.setImageBitmap(bitmap);
        Mat edge=new Mat();
        Imgproc.Canny(frameData,edge,90,270,5,true);
        List<Point> points = getCornersByContour(edge);
        for (Point point : points) {
            Log.d(TAG,"point ======" + point.toString() + ",width = " +edge.width() + ",height = " + edge.height());
        }
        Point leftTop = points.get(0);
        Point righttop=points.get(1);
        Point leftbottom=points.get(2);
        Point rightbottom=points.get(3);
        int startX = (int) Math.min(leftTop.x,leftbottom.x);
        int startY = (int) Math.min(leftTop.y,righttop.y);
        int maxWidth = edge.width() - startX;
        int maxHeight = edge.height() - startY;
        Utils.matToBitmap(frame, bitmap);
        Bitmap rectBitmap = Bitmap.createBitmap(bitmap,startX,startY ,
                Math.min((int) Math.abs(Math.max((righttop.x - leftTop.x),rightbottom.x - leftbottom.x)),maxWidth),
                Math.min((int)Math.abs( Math.max((leftbottom.y - leftTop.y),rightbottom.y - righttop.y)),maxHeight));
        ivShow.setImageBitmap(rectBitmap);
    }

    //将二值化后抓取的边框轮廓图片用不同颜色显示出来
    private void showCropPicture(Mat frame){
        Mat frameData = processImage(frame);
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        String name = System.currentTimeMillis() + "output_image.jpg";
        String pathResult = getExternalFilesDir("Pictures").getPath() + "/" + name;
        String fileName = pathResult + ".jpg";
        Imgcodecs.imwrite(fileName, frameData);
        //ivShow.setImageBitmap(bitmap);
        Mat edge=new Mat();
        Mat contours=new Mat();
        Imgproc.Canny(frameData,edge,90,270,5,true);
        List<MatOfPoint> contourList=new ArrayList<>();
        contours.create(edge.rows(), edge.cols(), CvType.CV_8UC3);
        Imgproc.findContours(edge,contourList,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contourList.size(); i++) {
            Random r = new Random();
            Imgproc.drawContours(
                    contours,
                    contourList,
                    i,
                    new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255)),
                    -1
            );
        }
        Utils.matToBitmap(contours, bitmap);
        ivShow.setImageBitmap(bitmap);
    }

    //自动按边框裁剪后拉伸
    private void showAutoCropPicture(Mat frame){
        Mat frameData = processImage(frame);
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        Mat edge=new Mat();
        Imgproc.Canny(frameData,edge,90,270,5,true);
        List<Point> points = getCornersByContour(edge);
        for (Point point : points) {
            Log.d(TAG,"point ======" + point.toString() + ",width = " +edge.width() + ",height = " + edge.height());
        }
        Point bitmapTopLeft = points.get(0);
        Point bitmapTopRight=points.get(1);
        Point bitmapBottomLeft=points.get(2);
        Point bitmapBottomRight=points.get(3);

        Utils.matToBitmap(frame, bitmap);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        // 1. draw path
        Path path = new Path();
        path.moveTo((float) bitmapTopLeft.x, (float) bitmapTopLeft.y);
        path.lineTo((float) bitmapTopRight.x, (float) bitmapTopRight.y);
        path.lineTo((float) bitmapBottomRight.x, (float) bitmapBottomRight.y);
        path.lineTo((float)bitmapBottomLeft.x,(float) bitmapBottomLeft.y);
        path.moveTo((float)bitmapTopLeft.x, (float)bitmapTopLeft.y);

        path.close();
        canvas.drawPath(path, paint);

        // 2. draw original bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //canvas.drawBitmap(bitmap, 0, 0, paint);
        Rect cropRect = new Rect(
                Math.min((int)bitmapTopLeft.x, (int)bitmapBottomLeft.x),
                Math.min((int)bitmapTopLeft.y, (int)bitmapTopRight.y),
                Math.max((int)bitmapBottomRight.x, (int)bitmapTopRight.x),
                Math.max((int)bitmapBottomRight.y, (int)bitmapBottomLeft.y));

        Bitmap cut = Bitmap.createBitmap(
                bitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
        );

        /*android.graphics.Point cutTopLeft = new android.graphics.Point();
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

        Log.e("stk", cut.getWidth() + "x" + cut.getHeight());

        Log.e("stk", "cutPoints="
                + cutTopLeft.toString() + " "
                + cutTopRight.toString() + " "
                + cutBottomRight.toString() + " "
                + cutBottomLeft.toString() + " ");

        float width = cut.getWidth();
        float height = cut.getHeight();

        float[] src = new float[]{cutTopLeft.x, cutTopLeft.y, cutTopRight.x, cutTopRight.y, cutBottomRight.x, cutBottomRight.y, cutBottomLeft.x, cutBottomLeft.y};
        float[] dst = new float[]{0, 0, width, 0, width, height, 0, height};

        Matrix matrix = new Matrix();
        matrix.setPolyToPoly(src, 0, dst, 0, 4);
        Bitmap stretch = Bitmap.createBitmap(cut.getWidth(), cut.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas stretchCanvas = new Canvas(stretch);
//            stretchCanvas.drawBitmap(cut, matrix, null);
        stretchCanvas.concat(matrix);
        stretchCanvas.drawBitmapMesh(cut, WIDTH_BLOCK, HEIGHT_BLOCK, generateVertices(cut.getWidth(), cut.getHeight()), 0, null, 0, null);*/

        ivShow.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        ivShow.setImageBitmap(cut);
    }


    private float[] generateVertices(int widthBitmap, int heightBitmap) {

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

}



