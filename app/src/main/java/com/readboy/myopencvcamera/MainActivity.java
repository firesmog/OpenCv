package com.readboy.myopencvcamera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.readboy.bean.Block;
import com.readboy.bean.Data;
import com.readboy.bean.Location;
import com.readboy.log.LogUtils;
import com.readboy.util.BitmapUtils;
import com.readboy.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;


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
    private RelativeLayout llShow;
    private MatOfPoint2f approxCurve;
    private double lastArea;
    private int llWidth;
    private int llHeight;
    private ActionBar actionBar;
    private Mat mRgbaOrigin;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javaCameraView);
        llShow = (RelativeLayout) findViewById(R.id.iv_show);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        llWidth = outMetrics.widthPixels;
        llHeight = outMetrics.heightPixels + 72 ;
        LogUtils.i( "widthPixels = " + llWidth  + ",heightPixels = " + llHeight);



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
                llShow.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mViewMode = VIEW_MODE_GRAY;
                break;
            case R.id.rgbItem:
                llShow.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mViewMode = VIEW_MODE_RGBA;
                break;
            case R.id.cannyItem:
                llShow.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mViewMode = VIEW_MODE_CANNY;
                break;
            case R.id.exitItem:
                finish();
                break;
            case R.id.clickItem:
                //showDifferentColorImage(mRgba);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                //showAutoCropPicture(mRgba);
                savePicture(mRgba);
                llShow.setVisibility(View.VISIBLE);
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
                mRgbaOrigin = inputFrame.rgba();
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

    //todo 参数最好可以设置为动态变化，第一次识别失败后就修改参数
    private Mat processImage( Mat gray ) {
        Mat b = new Mat();

        //高斯模糊效果较好，size里的参数只能为奇数
        Imgproc.GaussianBlur(gray,b, new Size(5,5),0);
        Mat t = new Mat();
        Imgproc.threshold(b, t, 125, 300, THRESH_BINARY);
        return t;
    }


    public List<Point> getCornersByContour(Mat imgsource){
        List<MatOfPoint> contours=new ArrayList<>();
        //轮廓检测
        Imgproc.findContours(imgsource,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d(TAG,"findContours size = " + contours.size());
        double maxArea= 20;
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
        Log.d(TAG,"findContours area max  = " + maxArea);
        return BitmapUtils.getImagePoint(approxCurve);
    }


    //将二值化后抓取的边框轮廓图片用不同颜色显示出来
    private void showDifferentColorImage(Mat frame){
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
        for (int i = 0; i < contourList.size() ; i++) {
            double curArea = Imgproc.contourArea(contourList.get(i));
            if(  curArea < 3000){
                continue;
            }

            if(Math.abs(curArea - lastArea)  < 10){
                continue;
            }
            lastArea = curArea;

            //
            MatOfPoint2f curve = new MatOfPoint2f(contourList.get(i).toArray());
            approxCurve = new MatOfPoint2f();
            double epsilon = 15;
            Imgproc.approxPolyDP(curve, approxCurve, epsilon,true );

            if(approxCurve.total() != 4){
                continue;
            }

            Log.d(TAG,"findContours area max  = " + Imgproc.contourArea(contourList.get(i))  +  ",length = " + Imgproc.arcLength(approxCurve,true));


            List<Point> points = BitmapUtils.getImagePoint(approxCurve);
            Bitmap stretch = BitmapUtils.cropBitmap(points,bitmap);
            BitmapUtils.saveImageToGallery(stretch,MainActivity.this,i);
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
        llShow.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    //自动按边框裁剪后拉伸（test pass）
    private void showAutoCropPicture(Mat frame){
        Mat frameData = processImage(frame);
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        Mat edge=new Mat();
        Imgproc.Canny(frameData,edge,90,270,5,true);
        Utils.matToBitmap(frame, bitmap);
        List<Point> points = getCornersByContour(edge);
        for (Point point : points) {
            Log.d(TAG,"point ======" + point.toString() + ",width = " +edge.width() + ",height = " + edge.height());
        }

        Bitmap stretch = BitmapUtils.cropBitmap(points,bitmap);
        llShow.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        llShow.setBackground(new BitmapDrawable(getResources(), stretch));
        double ratioHeight  = llHeight*1.0d / 800;
        double ratioWidth = llWidth *1.0d/ 600;
        LogUtils.d("ratioHeight = " + ratioHeight + " , ratioWidth = " + ratioWidth );
        addView(ratioWidth,ratioHeight);

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
        Utils.matToBitmap(mRgbaOrigin, bitmap);


        //add by lzy for class exam demo
        //试卷宽高分别为600 和 720 px ,需要先计算拍照得宽高和真实试卷宽高得比例才好定位
        //这里要考虑到展示到设备上得时候，图片可能已经拉伸或压缩了，所以不推荐使用图片比例计算
        double height = Math.min(leftbottom.y - leftTop.y , rightbottom.y - righttop.y);
        double width = Math.min(rightbottom.x - leftbottom.x, righttop.x - leftTop.x);
        double ratioHeight  = height / 800;
        double ratioWidth = width / 600;


        Data data = GsonUtil.gsonToBean(getString(R.string.json_string_c),Data.class);
        Block[] blocks = data.getBlock();

        //修改各个答案显示在照片里的坐标值
        for(int j = 0 ; j< blocks.length ; j++ ){
            // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
            Block block = blocks[j];
            Location location = block.getLine().getLocation();
            //获取中位点
            location.setTop_left(new com.readboy.bean.Point((int)(leftTop.x + location.getTop_left().getX()*ratioWidth),(int)(leftTop.y + location.getTop_left().getY()*ratioHeight)));
            location.setRight_bottom(new com.readboy.bean.Point((int)(rightbottom.x + location.getRight_bottom().getX()*ratioWidth),(int)(rightbottom.y + location.getRight_bottom().getY()*ratioHeight)));
        }
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));
        LogUtils.d("ratioWidth = " + llWidth/width + " , ratioHeight = " + llHeight/height + ",block size = " + blocks.length );

        //addView(llWidth/600,llHeight/800);
        addViewForWholeTest(llWidth/width,llHeight/height,blocks);

    }


    public void addViewForWholeTest(double ratioWidth , double ratioHeight,Block[] blocks ) {
        for(int j = 0 ; j< blocks.length ; j++ ){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
            Block block = blocks[j];
            Location location = block.getLine().getLocation();
            //获取中位点
            double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/2 + location.getTop_left().getX();
            double midY = /*1.0d*(location.getRight_bottom().getY() - location.getTop_left().getY() )/2 +*/ location.getTop_left().getY();
            TextView child = new TextView(this);
            child.setTextSize(20);
            String result = "占位符" + (j + 1);
            child.setText(result);
            LogUtils.d("midX  = " + midX  + ",midY === " + midY + ",location" + location.toString());


            if(j % 2 == 0){
                child.setTextColor(getResources().getColor(R.color.green));

            }else {
                child.setTextColor(getResources().getColor(R.color.red));
            }
            params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight) ,0,0);
            child.setLayoutParams(params);
            // 调用一个参数的addView方法
            llShow.addView(child,params);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public void addView(double ratioWidth , double ratioHeight) {
        Data data = GsonUtil.gsonToBean(getString(R.string.json_string_c),Data.class);
        Block[] blocks = data.getBlock();
        for(int j = 0 ; j< blocks.length ; j++ ){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
            Block block = blocks[j];
            Location location = block.getLine().getLocation();
            //获取中位点
            double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/2 + location.getTop_left().getX();
            double midY = /*1.0d*(location.getRight_bottom().getY() - location.getTop_left().getY() )/2 +*/ location.getTop_left().getY();
            TextView child = new TextView(this);
            child.setTextSize(20);
            String result = "占位符" + (j + 1);
            child.setText(result);
            LogUtils.d("midX  = " + midX  + ",midY === " + midY + ",location" + location.toString());


            if(j % 2 == 0){
                child.setTextColor(getResources().getColor(R.color.green));

            }else {
                child.setTextColor(getResources().getColor(R.color.red));
            }
            params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight) ,0,0);
            child.setLayoutParams(params);
            // 调用一个参数的addView方法
            llShow.addView(child,params);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}



