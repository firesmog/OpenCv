package com.readboy.myopencvcamera;

import org.apache.commons.codec.binary.Base64;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.utils.Converters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.readboy.bean.newexam.Answer;
import com.readboy.bean.newexam.Children;
import com.readboy.bean.newexam.ChildrenQuestion;
import com.readboy.bean.newexam.ExamBean;
import com.readboy.bean.newexam.RectangleInfo;
import com.readboy.bean.old.Block;
import com.readboy.bean.old.Data;
import com.readboy.bean.old.Location;
import com.readboy.log.LogUtils;
import com.readboy.net.HttpUtil;
import com.readboy.net.NetUtil;
import com.readboy.net.bean.BaseResponse;
import com.readboy.net.bean.Line;
import com.readboy.net.bean.Word;
import com.readboy.util.AnimatorUtil;
import com.readboy.util.BinaryUtils;
import com.readboy.util.BitmapUtils;
import com.readboy.util.DeviceUtil;
import com.readboy.util.GrayUtils;
import com.readboy.util.GsonUtil;
import com.readboy.util.HandleImgUtils;
import com.readboy.util.PhoneTypeUtil;
import com.readboy.util.PhotoUtil;
import com.readboy.util.ShowToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.readboy.net.NetUtil.WEBOCR_URL;
import static java.lang.Math.abs;
import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_CCOMP;
import static org.opencv.imgproc.Imgproc.Sobel;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.threshold;

@SuppressLint("NewApi")

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "TAG_CameraActivity";
    private int WIDTH_BLOCK = 40;
    private int HEIGHT_BLOCK = 40;
    private JavaCameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private static final int VIEW_MODE_RGBA = 0;
    private static final int VIEW_MODE_GRAY = 1;
    private static final int VIEW_MODE_CANNY = 2;
    private static final int VIEW_MODE_CLICK = 3;
    private static final int VIEW_MODE_FEATURES = 5;

    private static  final int TAKE_PHOTO_REQUEST = 1130;
    private static final int OPEN_CANMER = 1122;
    private String[] answer = {"Read", "boy", "girl", "child", "home", "Car", "bike", "book", "good","bad","tiger","apple","android","target","parent"
            ,"bus","cow","horse","house","water","fire","smog","at","where","when","how","what","why","ask","which"};
    private List<String> results = new ArrayList<>();

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
    private double lastAreaHere;
    private float nndrRatio = 0.7f;//这里设置既定值为0.7，该值可自行调整

    private int matchesPointCount = 0;

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
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        mOpenCvCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenCvCameraView.setAutoFocus();
            }
        });
        llShow = (RelativeLayout) findViewById(R.id.ll_show);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        llWidth = outMetrics.widthPixels;
        if(PhoneTypeUtil.SYS_EMUI.equals(PhoneTypeUtil.getSystem())){
            llHeight = outMetrics.heightPixels ;
        }else {
            llHeight = outMetrics.heightPixels + 72;
        }

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
            LogUtils.d( "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            LogUtils.d( "OpenCV library found inside package. Using it!");
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
                mOpenCvCameraView.cancelAutoFocus();
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                // dealRectangleCorrect(mRgba);
                //showAutoCropPicture(mRgba);
                //savePicture(mRgba);
                //getCorner(mRgba);
                //savePictureAccordExam(mRgba);
                //getMaxRectangle(mRgba);
                //getBookRectangle(mRgba);
                //getCorner(mRgba);
                getMaxRectangle(mRgba);
                llShow.setVisibility(View.VISIBLE);
                mOpenCvCameraView.setVisibility(View.GONE);
                mViewMode = VIEW_MODE_CLICK;
                break;
        }
        return true;
    }

    private void otherGetCorner(Mat frame){
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);


        Mat gray = new Mat();


        Imgproc.cvtColor(frame,gray,Imgproc.COLOR_BGR2GRAY);

//角点发现
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(gray,corners,15,0.5,10,new Mat(),3,false,0.04);

        Point[] points = corners.toArray();
        for (Point p :points) {
            Imgproc.circle(frame,p,5,new Scalar(0,0,255));
        }
        llShow.setVisibility(View.VISIBLE);
        final Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame,bitmap);
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));
    }

    private void getCorner(Mat frame) {
        Mat srcImage = new Mat();
        Mat src = new Mat();
        Imgproc.cvtColor(frame, src, Imgproc.COLOR_RGBA2RGB);
        srcImage = processImage(src);
        Mat dstImage = srcImage.clone();
        Imgproc.Canny(srcImage,dstImage,90,250,7,true);
        BitmapUtils.savePicAsBitmap(dstImage,MainActivity.this,300);
        Mat storage = new Mat();
        Imgproc.HoughLinesP(dstImage, storage, 1, Math.PI / 180, 100, 150, 10);
        for (int x = 0; x < storage.rows(); x++)
        {
            double[] vec = storage.get(x, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Imgproc.line(src, start, end, new Scalar(0,255), 2, Imgproc.LINE_AA, 0);
        }
        llShow.setVisibility(View.VISIBLE);
        final Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        BitmapUtils.savePicAsBitmap(src,MainActivity.this,301);

        Utils.matToBitmap(src,bitmap);
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));

    }

    private void getBookRectangle(final Mat gray){
        Observable.create(new ObservableOnSubscribe<Mat>() {
            // 1. 创建被观察者 & 生产事件
            @Override
            public void subscribe(ObservableEmitter<Mat> emitter) {
                Mat frame = new Mat();
                Imgproc.cvtColor(gray,frame,Imgproc. COLOR_RGBA2RGB);
                //Imgproc.pyrMeanShiftFiltering(frame,frame, 25, 10);


                Mat src = GrayUtils.grayNative(frame);
                BitmapUtils.savePicAsBitmap(src,MainActivity.this,100);
                src = BinaryUtils.binaryNativeMain(src,0,0);
                BitmapUtils.savePicAsBitmap(src,MainActivity.this,101);
                emitter.onNext(src);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Mat>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(final Mat info) {
                        LogUtils.d("savePictureAccordExam onComplete ");
                        llShow.setVisibility(View.VISIBLE);
                        final Bitmap bitmap = Bitmap.createBitmap(info.width(), info.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(info,bitmap);
                        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));

                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.d("savePictureAccordExam error = " + e.getMessage());

                    }

                    @Override
                    public void onComplete() {
                        LogUtils.d("savePictureAccordExam onComplete ");

                    }
                });

    }


    //convexHull 第二个参数：在第一种情况下，外壳元素是原始数组中凸包点的从0开始的索引 （因为凸包点集是原始点集的子集） 所以需要转换为真实的坐标地址
    public static MatOfPoint matOfIntToPoints(MatOfPoint contour, MatOfInt indexes) {
        int[] arrIndex = indexes.toArray();
        Point[] arrContour = contour.toArray();
        Point[] arrPoints = new Point[arrIndex.length];

        for (int i=0;i<arrIndex.length;i++) {
            arrPoints[i] = arrContour[arrIndex[i]];
        }

        MatOfPoint hull = new MatOfPoint();
        hull.fromArray(arrPoints);
        return hull;
    }



    public  Mat processImage(Mat gray) {
        Mat frame = new Mat();
        Imgproc.cvtColor(gray,frame,Imgproc. COLOR_RGBA2RGB);
        //Imgproc.pyrMeanShiftFiltering(frame,frame, 25, 10);


        Mat src = GrayUtils.grayNative(frame);
        BitmapUtils.savePicAsBitmap(src,MainActivity.this,100);
        src = BinaryUtils.binaryNativeMain(src,0,0);
        BitmapUtils.savePicAsBitmap(src,MainActivity.this,101);


        // size 越小，腐蚀的单位越小，图片越接近原图（凸边检测size使用3，3）
        Mat structImage1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4.2, 4.2));
        Mat structImage2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4));
        Imgproc.erode(src, frame, structImage1, new Point(-1, -1), 2);
        BitmapUtils.savePicAsBitmap(frame,MainActivity.this,102);
        Imgproc.dilate(src, frame, structImage2 , new Point(-1, -1), 2);
        BitmapUtils.savePicAsBitmap(frame,MainActivity.this,103);


        return frame;
    }

    private void findLineInPic(Mat gray){
        Mat frame = new Mat();
        Imgproc.cvtColor(gray,frame,Imgproc. COLOR_RGBA2RGB);
        Mat org = GrayUtils.grayNative(frame);

        Mat src =  processImage(gray);
        //3.获取结构元素 详细注释：https://blog.csdn.net/ren365880/article/details/103886484
        Mat hline = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(org.cols() / 90, 4), new Point(-1, -1));
        Mat vline = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, org.rows() / 70), new Point(-1, -1));

        //水平线
        Mat dst1 = new Mat(org.size(),org.type());
        // 腐蚀 详细注释  https://blog.csdn.net/ren365880/article/details/103886484
        Imgproc.erode(src, dst1, hline);
        //膨胀 详细注释  https://blog.csdn.net/ren365880/article/details/103886484
        Imgproc.dilate(src, dst1, hline);

       BitmapUtils.savePicAsBitmap(dst1,this,200);

        //垂直线
        Mat dst2 = new Mat(org.size(), org.type());
        Imgproc.erode(src, dst2, vline);
        Imgproc.dilate(src, dst2, vline);
        BitmapUtils.savePicAsBitmap(dst2,this,201);


    }

    public List<Point> getCornersByContour(Mat imgsource){
        List<MatOfPoint> contours=new ArrayList<>();
        //轮廓检测
        Imgproc.findContours(imgsource,contours,new Mat(),Imgproc.RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        LogUtils.d("findContours size = " + contours.size());
        double maxArea= 20;
        MatOfPoint temp_contour= contours.get(0);//假设最大的轮廓在index=0处
        MatOfPoint2f approxCurve=new MatOfPoint2f();
        for (int idx=0;idx<contours.size();idx++){
            temp_contour=contours.get(idx);
            double contourarea=Imgproc.contourArea(temp_contour);
            if(  contourarea < 6000){
                continue;
            }
            LogUtils.d("findContours area = " + contourarea);
            //当前轮廓面积比最大的区域面积大就检测是否为四边形
            if (contourarea > maxArea){
                //检测contour是否是四边形
                MatOfPoint2f new_mat=new MatOfPoint2f(temp_contour.toArray());
                MatOfPoint2f approxCurve_temp=new MatOfPoint2f();
                //对图像轮廓点进行多边形拟合
                Imgproc.approxPolyDP(new_mat,approxCurve_temp,0.01 * Imgproc.arcLength(new_mat, true),true);
                LogUtils.d("findContours22222 area = " + approxCurve_temp.total()  +", length = " +  0.01 * Imgproc.arcLength(new_mat, true));

                if (approxCurve_temp.total() == 4 ){
                    maxArea=contourarea;
                    approxCurve=approxCurve_temp;
                    LogUtils.d("findContours22222 area = " + contourarea);
                }
            }
        }
        LogUtils.d("findContours area max  = " + maxArea);
        return BitmapUtils.getImagePoint(approxCurve);
    }



    private Mat preprocess(Mat gray) {
        //1.Sobel算子，x方向求梯度
        Mat sobel = new Mat();
        Mat src = GrayUtils.grayNative(gray);
        Sobel(src, sobel, CV_8U, 1, 0, 3);

        //2.二值化
        Mat binary = new Mat();
        Imgproc.cvtColor(sobel, sobel, Imgproc.COLOR_BGR2GRAY);
        threshold(sobel, binary, 0, 255, THRESH_OTSU + THRESH_BINARY);

        //3.膨胀和腐蚀操作核设定
        Mat element1 = Imgproc.getStructuringElement(MORPH_RECT, new Size(30, 9));
        //控制高度设置可以控制上下行的膨胀程度，例如3比4的区分能力更强,但也会造成漏检
        Mat element2 = Imgproc.getStructuringElement(MORPH_RECT, new Size(24, 4));

        //4.膨胀一次，让轮廓突出
        Mat dilate1 = new Mat();
        dilate(binary, dilate1, element2);

        //5.腐蚀一次，去掉细节，表格线等。这里去掉的是竖直的线
        Mat erode1 = new Mat();
        erode(dilate1, erode1, element1);

        //6.再次膨胀，让轮廓明显一些
        Mat dilate2 = new Mat();
        dilate(erode1, dilate2, element2);

        //7.存储中间图片
        BitmapUtils.savePicAsBitmap(binary,this,1011);
        BitmapUtils.savePicAsBitmap(dilate1,this,1012);
        BitmapUtils.savePicAsBitmap(erode1,this,1013);
        BitmapUtils.savePicAsBitmap(dilate2,this,1014);


        return dilate2;
    }


   

    private List<RotatedRect> findTextRegion(Mat img) {
        List<RotatedRect> rects = new ArrayList<>();
        List<MatOfPoint> contours=new ArrayList<>();
        //轮廓检测
        Imgproc.findContours(img,contours,new Mat(),Imgproc.RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        LogUtils.d("findContours size = " + contours.size());
        MatOfPoint temp_contour= contours.get(0);//假设最大的轮廓在index=0处
        MatOfPoint2f approxCurve=new MatOfPoint2f();
        for (int idx = 0; idx < contours.size(); idx++){
            temp_contour=contours.get(idx);
            double contourarea=Imgproc.contourArea(temp_contour);
            if(  contourarea < 600){
                continue;
            }
            //检测contour是否是四边形
            MatOfPoint2f new_mat=new MatOfPoint2f(temp_contour.toArray());
            MatOfPoint2f approxCurve_temp=new MatOfPoint2f();
            //对图像轮廓点进行多边形拟合
            Imgproc.approxPolyDP(new_mat,approxCurve_temp,0.01 * Imgproc.arcLength(new_mat, true),true);
            LogUtils.d("findContours22222 area = " + approxCurve_temp.total()  +", length = " +  0.01 * Imgproc.arcLength(new_mat, true));
            RotatedRect rect = Imgproc.minAreaRect(new_mat);
            //计算高和宽
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;

            //筛选那些太细的矩形，留下扁的
            if (m_height > m_width * 1.2)
                continue;
            rects.add(rect);

        }
        return rects;
    }







    private  void findLongestLine(Mat frame){
        final Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame,bitmap);
        Mat src = new Mat();
        Mat canny = new Mat();
        Utils.bitmapToMat(bitmap,src);//将Bitmap对象转换为Mat对象
        Imgproc.Canny(src,canny,100,200);//边缘提取
        BitmapUtils.savePicAsBitmap(src,this,998866);
        Mat lines = new Mat();//存储线的容器
        Imgproc.HoughLinesP(canny,lines,1,Math.PI/180,3,0,8);//霍夫直线检测

        Mat dst = new Mat(src.size(),src.type());
        for(int i = 0;i<lines.rows();i++){
            int[] line = new int[4];
            lines.get(i,0,line);//将线对应的极点坐标存到line数组中
            Imgproc.line(dst,new Point(line[0],line[1]),new Point(line[2],line[3]),new Scalar(255, 0, 0),10,Imgproc.LINE_AA);

        }
        Utils.matToBitmap(dst,bitmap);//将Mat对象转换为Bitmap对象
        BitmapUtils.savePicAsBitmap(dst,this,99999);
        BitmapUtils.saveImageToGallery(bitmap,this,998877);
        llShow.setVisibility(View.VISIBLE);
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));
    }

    @SuppressLint("NewApi")
    private void savePicture(Mat frame){
        Mat frameData = processImage(frame);
        Mat edge=new Mat();

        Imgproc.Canny(frameData,edge,90,270,5,true);
        //List<Point> points = getCornersByContour(edge);
        List<MatOfPoint> contours=new ArrayList<>();
        //轮廓检测
        Imgproc.findContours(edge,contours,new Mat(),Imgproc.RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        for (MatOfPoint contour : contours) {
            if(null  != contour){
                MatOfPoint2f new_mat=new MatOfPoint2f(contour.toArray());
                Rect rect = Imgproc.boundingRect(new_mat);
                //如果高度不足，或者长宽比太小，认为是无效数据，否则把矩形画到原图上
                if(rect.height > 20 && (rect.width * 1.0 / rect.height) > 0.2){
                    Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 5);
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frame, bitmap);
            llShow.setBackground(new BitmapDrawable(getResources(),bitmap));
        }
    }

    private void getMaxRectangle(final Mat frame){
        Observable.create(new ObservableOnSubscribe<Mat>() {
            // 1. 创建被观察者 & 生产事件
            @Override
            public void subscribe(ObservableEmitter<Mat> emitter) {
                Mat frameData = processImage(frame);
                Mat dst = new Mat();
                List<MatOfPoint> list = new ArrayList<MatOfPoint>();
                Mat hierarchy = new Mat();
                /*Imgproc.Canny(dst,dst,90,270,5,true);
                BitmapUtils.savePicAsBitmap(dst,MainActivity.this,400);*/
                Imgproc.Canny(frameData,dst,40,120,5,true);
                Imgproc.findContours(dst, list, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE,new Point(0, 0));
                BitmapUtils.savePicAsBitmap(dst,MainActivity.this,10003);
                Mat drawing = Mat.zeros(dst.size(), CvType.CV_8UC3);

                for(int i=0,size = list.size();i<size;i++) {
                    ;
                    Scalar color = new Scalar(0 ,250,100);
                    MatOfPoint temp_contour=list.get(i);
                    double contourarea=Imgproc.contourArea(temp_contour);
                    if(  contourarea < 2000){
                        continue;
                    }

                    MatOfPoint2f new_mat=new MatOfPoint2f(temp_contour.toArray());
                    MatOfPoint2f approxCurve_temp=new MatOfPoint2f();
                    //对图像轮廓点进行多边形拟合
                    Imgproc.approxPolyDP(new_mat,approxCurve_temp,0.01 * Imgproc.arcLength(new_mat, true),true);

                    approxCurve = approxCurve_temp;
                    LogUtils.d("approxCurve data = " + approxCurve.rows() + ",findContours22222 area = " + approxCurve_temp.total() );
                    for(int j = 0; j < approxCurve.rows(); j++){
                        double[] temp_double=approxCurve.get(j,0);
                        LogUtils.d("j === " + j + ",x = " + temp_double[0] + ",y = " + temp_double[1]);
                    }

                    /*double[] temp_double=approxCurve.get(0,0);
                    Point point1=new Point(temp_double[0],temp_double[1]);
                    temp_double=approxCurve.get(1,0);
                    Point point2=new Point(temp_double[0],temp_double[1]);
                    temp_double=approxCurve.get(2,0);
                    Point point3=new Point(temp_double[0],temp_double[1]);
                    temp_double=approxCurve.get(3,0);
                    Point point4=new Point(temp_double[0],temp_double[1]);*/

                    Imgproc.drawContours(drawing, list, i, color, 2, Imgproc.LINE_AA,new Mat(),0,new Point());
                }
                emitter.onNext(drawing);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Mat>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(final Mat info) {
                        final Bitmap bitmap = Bitmap.createBitmap(info.width(), info.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(info,bitmap);//将Mat对象转换为Bitmap对象
                        BitmapUtils.savePicAsBitmap(info,MainActivity.this,10002);
                        llShow.setVisibility(View.VISIBLE);
                        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));

                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.d("savePictureAccordExam error = " + e.getMessage());

                        //finish();
                    }

                    @Override
                    public void onComplete() {
                        LogUtils.d("savePictureAccordExam onComplete ");

                    }
                });
    }

}



