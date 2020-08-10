package com.readboy.myopencvcamera;

import org.apache.commons.codec.binary.Base64;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.readboy.net.RxDisposeManager;
import com.readboy.net.bean.BaseResponse;
import com.readboy.net.bean.Line;
import com.readboy.net.bean.Word;
import com.readboy.util.BinaryUtils;
import com.readboy.util.BitmapUtils;
import com.readboy.util.DeviceUtil;
import com.readboy.util.GrayUtils;
import com.readboy.util.GsonUtil;
import com.readboy.util.PhotoUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.readboy.net.NetUtil.WEBOCR_URL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

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
        llShow = (RelativeLayout) findViewById(R.id.iv_show);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        llWidth = outMetrics.widthPixels;
        llHeight = outMetrics.heightPixels + 72 ;


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
                savePictureAccordExam(mRgba);
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
        Mat frame = new Mat();
        Imgproc.cvtColor(gray,frame,Imgproc. COLOR_RGBA2RGB);
        Mat src = GrayUtils.grayColByAdapThreshold(frame);
        //testParameters(src);
        Imgproc.GaussianBlur(src,src, new Size(3,3),0);//高斯滤波去除小噪点
        src = BinaryUtils.binaryNative(src,0,0);
        BitmapUtils.savePicAsBitmap(src,this,100);
        return src;
    }



    private void testParameters(Mat src){
        Mat src1 = new Mat();
        Mat src2 = new Mat();
        Mat src3 = new Mat();
        Mat src4 = new Mat();
        Mat src5 = new Mat();
        Imgproc.GaussianBlur(src,src1, new Size(3,3),0);//高斯滤波去除小噪点
        Imgproc.GaussianBlur(src,src2, new Size(5,5),0);//高斯滤波去除小噪点
        Imgproc.GaussianBlur(src,src3, new Size(7,7),0);//高斯滤波去除小噪点
        Imgproc.medianBlur(src,src4,3);
        Imgproc.equalizeHist(src,src5);
        savePic(src1,1111);
        savePic(src2,2222);
        savePic(src3,3333);
        savePic(src4,4444);
        savePic(src5,5555);
    }

    private void savePic(Mat src,int i){
        src = BinaryUtils.binaryNative(src);
        //Imgproc.equalizeHist(src,src);
        Bitmap bitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bitmap);
        BitmapUtils.saveImageToGallery(bitmap,this,i);
    }

    //todo 参数最好可以设置为动态变化，第一次识别失败后就修改参数
    /*private Mat processImage( Mat gray ) {
        Mat b = new Mat();
        Mat s = new Mat();

        //高斯模糊效果较好，size里的参数只能为奇数
        Imgproc.GaussianBlur(gray,b, new Size(1,1),0);
        //Imgproc.Laplacian(gray,s,-1,3);//Laplace边缘提取
        Mat src = new Mat();
        Imgproc.threshold(b, src, 125, 255, THRESH_BINARY);
        Bitmap bitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bitmap);
        BitmapUtils.saveImageToGallery(bitmap,this,9999);
        return src;
    }*/


    public List<Point> getCornersByContour(Mat imgsource){
        List<MatOfPoint> contours=new ArrayList<>();
        //轮廓检测
        Imgproc.findContours(imgsource,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
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
        Imgproc.Canny(frameData,edge,90,225,5,true);
        List<MatOfPoint> contourList=new ArrayList<>();
        contours.create(edge.rows(), edge.cols(), CvType.CV_8UC3);
        Imgproc.findContours(edge,contourList,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        LogUtils.d("findContours area max11111  = " +contourList.size());
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
            LogUtils.d("findContours area max333333  = " + approxCurve.total() );

            if(approxCurve.total() !=4){
                continue;
            }

            LogUtils.d("findContours area max  = " + Imgproc.contourArea(contourList.get(i))  +  ",length = " + Imgproc.arcLength(approxCurve,true));


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
            LogUtils.d("point ======" + point.toString() + ",width = " +edge.width() + ",height = " + edge.height());
        }

        Bitmap stretch = BitmapUtils.cropBitmap(points,bitmap);
        llShow.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        llShow.setBackground(new BitmapDrawable(getResources(), stretch));
        double ratioHeight  = llHeight*1.0d / 800;
        double ratioWidth = llWidth *1.0d/ 600;
        LogUtils.d("stretch ratioHeight = " + stretch.getHeight() + " , ratioWidth = " + stretch.getWidth() );
        //addView(ratioWidth,ratioHeight);

    }


    //处理矩形矫正（test pass）
    private RectangleInfo dealRectangleCorrect(Mat frame){
        Mat frameData = processImage(frame);
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        Mat edge=new Mat();
        Imgproc.Canny(frameData,edge,90,270,5,true);
        Utils.matToBitmap(edge, bitmap);
        BitmapUtils.saveImageToGallery(bitmap,this,7777);
        List<Point> points = getCornersByContour(edge);
        for (Point point : points) {
            LogUtils.d("point ======" + point.toString() + ",width = " +edge.width() + ",height = " + edge.height());
        }

        Mat srcPoints = Converters.vector_Point_to_Mat(points, CvType.CV_32F);
        final Point leftTop = points.get(0);
        Point righttop=points.get(1);
        Point leftbottom=points.get(2);
        Point rightbottom=points.get(3);

        List<Point> dst = new ArrayList<>();
        double MinX =  Math.min(leftTop.x, leftbottom.x);
        double MaxX =  Math.max(righttop.x, rightbottom.x);
        double MinY =  Math.min(leftTop.y, righttop.y);
        double MaxY =  Math.max(leftbottom.y, rightbottom.y);
        dst.add(new Point(MinX,MinY));
        dst.add(new Point(MaxX,MinY));
        dst.add(new Point(MinX,MaxY));
        dst.add(new Point(MaxX,MaxY));
        Mat dstPoints = Converters.vector_Point_to_Mat(dst, CvType.CV_32F);
        Mat perspectiveMat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        Mat result = new Mat();
        Imgproc.warpPerspective(frame, result, perspectiveMat, frame.size(),Imgproc.INTER_LANCZOS4 );
        Utils.matToBitmap(result,bitmap);
        RectangleInfo info = new RectangleInfo();
        info.setBitmap(bitmap);
        info.setPoints(dst);
        //Bitmap stretch = BitmapUtils.cropBitmap(dst,bitmap);

        //llShow.setBackground(new BitmapDrawable(getResources(),stretch));
        return info;
    }



    @SuppressLint("NewApi")
    private void savePictureAccordExam(Mat frame){
       /* Mat frameData = processImage(frame);
        //多通道分离出单通道
        final Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameData, bitmap);
        BitmapUtils.saveImageToGallery(bitmap,this,303);
        Mat edge=new Mat();
        Imgproc.Canny(frameData,edge,90,270,5,true);
        Utils.matToBitmap(edge, bitmap);

        List<Point> points = getCornersByContour(edge);
        for (Point point : points) {
            LogUtils.d("point ======" + point.toString() + ",width = " +edge.width() + ",height = " + edge.height());
        }
        final Point leftTop = points.get(0);
        Point righttop=points.get(1);
        Point leftbottom=points.get(2);
        Point rightbottom=points.get(3);
        Utils.matToBitmap(frame, bitmap);*/
        RectangleInfo info = dealRectangleCorrect(frame);
        List<Point> points = info.getPoints();
        final Point leftTop = points.get(0);
        Point righttop=points.get(1);
        Point leftbottom=points.get(2);
        Point rightbottom=points.get(3);
        final Bitmap bitmap = info.getBitmap();
        //add by lzy for class exam demo
        //试卷宽高分别为600 和 720 px ,需要先计算拍照得宽高和真实试卷宽高得比例才好定位
        //这里要考虑到展示到设备上得时候，图片可能已经拉伸或压缩了，所以不推荐使用图片比例计算
        double leftHeight = leftbottom.y - leftTop.y;
        double rightHeight = rightbottom.y - righttop.y;
        double topWidth =  righttop.x - leftTop.x;
        double bottomWidth = rightbottom.x - leftbottom.x;

        double height = Math.min(leftHeight, rightHeight);
        double height2 = Math.max(leftHeight, rightHeight);
        double width = Math.min(bottomWidth, topWidth);
        double width2 = Math.max(bottomWidth, topWidth);

        ExamBean data = DeviceUtil.getExamData(this);
        int examHeight = data.getHeight();
        int examWidth = data.getWidth();
        double gapWidth = width2 - width;
        //final double ratioHeight  = height/examHeight ;
        final double ratioHeight  = height/examHeight ;
        //final double ratioWidth = width/examWidth;
        final double ratioWidth = width/examWidth;
        LogUtils.d("ratioWidth = " + ratioWidth + " , ratioHeight = " + ratioHeight   );
        LogUtils.d("ratioWidth leftHeight = " + leftHeight + " , rightHeight = " + rightHeight  +  ",topWidth = " + topWidth + ",bottomWidth = " + bottomWidth  );
        LogUtils.d("ratioWidth  width = " +  width  + " , ratioHeight00 = " + height  + ",maxWidth =" + width2 + ",maxHeight = " + height2  + ",gapWidth = " + gapWidth);
        List<Children> bigQuestion = data.getChildren();
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));


        for (final Children children : bigQuestion) {
            switch (children.getType()){
                case 10001:
                    //1.处理选择题
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dealChooseQuestion(children,leftTop,ratioWidth,ratioHeight,bitmap);
                        }
                    }).start();

                    break;
                case 10006:
                    //2.处理主观填空题
                    //dealListenQuestion(children,leftTop,ratioWidth,ratioHeight,bitmap);
                    dealFillInQuestion(children,leftTop,ratioWidth,ratioHeight,bitmap);


                    break;
                case 10023:
                    //3.处理听力填空题
                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           dealListenQuestion(children,leftTop,ratioWidth,ratioHeight,bitmap);
                       }
                   }).start();
                    break;
            }
        }


    }


    private void dealFillInQuestion(final Children children, Point leftTop, double ratioWidth , double ratioHeight, final Bitmap bitmap){
        com.readboy.bean.old.Point parentLeftTop = new com.readboy.bean.old.Point(children.getLeftTopX(),children.getLeftTopY());
        List<ChildrenQuestion> childQuestion = children.getChildren();
        for(int j = 1; j< childQuestion.size(); j++){
            ChildrenQuestion realQuestion = childQuestion.get(j);
            LogUtils.d("realQuestion == " + realQuestion.toString() + ",parentLT = "  + parentLeftTop.toString());
            //计算答案坐标，封装到location
            for(int i = 0 ; i < realQuestion.getAnswer().size() ; i++){
                Answer answer = realQuestion.getAnswer().get(i);
                com.readboy.bean.old.Point answerPoint1 =  new com.readboy.bean.old.Point((int)(leftTop.x + (answer.getLeftTopX() + parentLeftTop.getX())*ratioWidth),(int)(leftTop.y + (answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                com.readboy.bean.old.Point answerPoint2 =  new com.readboy.bean.old.Point((int)(leftTop.x + (answer.getRightBottomX() + parentLeftTop.getX())*ratioWidth),(int)(leftTop.y + (answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                final Location location = new Location(answerPoint1,answerPoint2);
                //填空题得每个答案单独处理
                final List<Point> pointScrop = new ArrayList<>();
                int marginMore = 6;
                pointScrop.add(new Point(Math.max(((leftTop.x + (answer.getLeftTopX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)-  marginMore),1) ,Math.max((leftTop.y + (answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight)-1.0*marginMore ,1)));
                pointScrop.add(new Point(Math.min(((leftTop.x + (answer.getRightBottomX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)  +  marginMore ),bitmap.getWidth()),Math.max((leftTop.y + (answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight)- 1.0*marginMore,1)));
                pointScrop.add(new Point(Math.max((leftTop.x + (answer.getLeftTopX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)  -marginMore,1),Math.min(leftTop.y + (answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight + marginMore ,bitmap.getHeight())));
                pointScrop.add(new Point(Math.min((leftTop.x + (answer.getRightBottomX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth ) +   marginMore ,bitmap.getWidth()),Math.min(leftTop.y + (answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight + marginMore,bitmap.getHeight())));
                final Bitmap stretch = BitmapUtils.cropBitmap(pointScrop,bitmap);
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       LogUtils.d("realQuestion == doNetRequest");
                       doNetRequest(stretch,null,1.0d*llWidth/bitmap.getWidth(),1.0d*llHeight/bitmap.getHeight(),children.getType(),location);
                   }
               }).start();
            }

        }
    }

    private void dealChooseQuestion(Children children, Point leftTop, double ratioWidth , double ratioHeight, final Bitmap bitmap){
        com.readboy.bean.old.Point parentLeftTop = new com.readboy.bean.old.Point(children.getLeftTopX(),children.getLeftTopY());
        List<ChildrenQuestion> childQuestion = children.getChildren();
        for(int j = 1; j< childQuestion.size(); j++){
            ChildrenQuestion realQuestion = childQuestion.get(j);
            LogUtils.d("realQuestion == " + realQuestion.toString() + ",parentLT = "  + parentLeftTop.toString());
            com.readboy.bean.old.Point realQuestionLeftTop = new com.readboy.bean.old.Point(realQuestion.getLeftTopX() + parentLeftTop.getX(),realQuestion.getLeftTopY() + parentLeftTop.getY());
            com.readboy.bean.old.Point realQuestionRightBottom = new com.readboy.bean.old.Point(realQuestion.getRightBottomX() + parentLeftTop.getX(),realQuestion.getRightBottomY() + parentLeftTop.getY());
            //计算答案坐标，封装到location
            Location[] locations = new Location[realQuestion.getAnswer().size()];
            for(int i = 0 ; i < locations.length ; i++){
                Answer answer = realQuestion.getAnswer().get(i);
                com.readboy.bean.old.Point answerPoint1 =  new com.readboy.bean.old.Point((int)(leftTop.x + (answer.getLeftTopX() + parentLeftTop.getX())*ratioWidth),(int)(leftTop.y + (answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                com.readboy.bean.old.Point answerPoint2 =  new com.readboy.bean.old.Point((int)(leftTop.x + (answer.getRightBottomX() + parentLeftTop.getX())*ratioWidth),(int)(leftTop.y + (answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                locations[i] = new Location(answerPoint1,answerPoint2);
            }
            List<Point> pointScrop = new ArrayList<>();
            int marginMore = 10;
            pointScrop.add(new Point(Math.max(((leftTop.x  + realQuestionLeftTop.getX() *ratioWidth )- 2.2* marginMore),1) ,Math.max((leftTop.y + realQuestionLeftTop.getY()*ratioHeight)- 1.2*marginMore ,1)));
            pointScrop.add(new Point(Math.min(((leftTop.x  + realQuestionRightBottom.getX()*ratioWidth )  + 2.2* marginMore ),bitmap.getWidth()),Math.max((leftTop.y + realQuestionLeftTop.getY()*ratioHeight) - 1.2*marginMore,1)));
            pointScrop.add(new Point(Math.max((leftTop.x  + realQuestionLeftTop.getX()*ratioWidth)  -2.2*marginMore,1),Math.min(leftTop.y + realQuestionRightBottom.getY()*ratioHeight  + 1.2*marginMore ,bitmap.getHeight())));
            pointScrop.add(new Point(Math.min((leftTop.x  + realQuestionRightBottom.getX()*ratioWidth ) + 2.2* marginMore ,bitmap.getWidth()),Math.min(leftTop.y + realQuestionRightBottom.getY()*ratioHeight   + 1.2*marginMore ,bitmap.getHeight())));
            Bitmap stretch = BitmapUtils.cropBitmap(pointScrop,bitmap);
            BitmapUtils.saveImageToGallery(stretch,this,9527 +  j);
            doNetRequest(stretch,locations,1.0d*llWidth/bitmap.getWidth(),1.0d*llHeight/bitmap.getHeight(),children.getType(),null);
        }
    }

    private void dealListenQuestion(Children children, Point leftTop, double ratioWidth , double ratioHeight, final Bitmap bitmap){
        try {
            com.readboy.bean.old.Point parentLeftTop = new com.readboy.bean.old.Point(children.getLeftTopX(),children.getLeftTopY());
            List<ChildrenQuestion> childQuestion = children.getChildren();
            ChildrenQuestion realQuestion = childQuestion.get(1);
            LogUtils.d("realQuestion == " + realQuestion.toString() + ",parentLT = "  + parentLeftTop.toString());
            com.readboy.bean.old.Point realQuestionLeftTop = new com.readboy.bean.old.Point(realQuestion.getLeftTopX() + parentLeftTop.getX(),realQuestion.getLeftTopY() + parentLeftTop.getY());
            com.readboy.bean.old.Point realQuestionRightBottom = new com.readboy.bean.old.Point(realQuestion.getRightBottomX() + parentLeftTop.getX(),realQuestion.getRightBottomY() + parentLeftTop.getY());
            //计算答案坐标，封装到location
            Location[] locations = new Location[realQuestion.getAnswer().size()];
            for(int i = 0 ; i < locations.length ; i++){
                Answer answer = realQuestion.getAnswer().get(i);
                com.readboy.bean.old.Point answerPoint1 =  new com.readboy.bean.old.Point((int)(leftTop.x + (answer.getLeftTopX() + parentLeftTop.getX())*ratioWidth),(int)(leftTop.y + (answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                com.readboy.bean.old.Point answerPoint2 =  new com.readboy.bean.old.Point((int)(leftTop.x + (answer.getRightBottomX() + parentLeftTop.getX())*ratioWidth),(int)(leftTop.y + (answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                locations[i] = new Location(answerPoint1,answerPoint2);
            }

            List<Point> pointScrop = new ArrayList<>();
            int marginMore = 10;
            pointScrop.add(new Point(Math.max(((leftTop.x  + realQuestionLeftTop.getX() *ratioWidth )- 2.2* marginMore),1) ,Math.max((leftTop.y + realQuestionLeftTop.getY()*ratioHeight)- 1.5*marginMore ,1)));
            pointScrop.add(new Point(Math.min(((leftTop.x  + realQuestionRightBottom.getX()*ratioWidth )  + 2.2* marginMore ),bitmap.getWidth()),Math.max((leftTop.y + realQuestionLeftTop.getY()*ratioHeight) - 1.5*marginMore,1)));
            pointScrop.add(new Point(Math.max((leftTop.x  + realQuestionLeftTop.getX()*ratioWidth)  -2.2*marginMore,1),Math.min(leftTop.y + realQuestionRightBottom.getY()*ratioHeight  + marginMore ,bitmap.getHeight())));
            pointScrop.add(new Point(Math.min((leftTop.x  + realQuestionRightBottom.getX()*ratioWidth ) + 2.2* marginMore ,bitmap.getWidth()),Math.min(leftTop.y + realQuestionRightBottom.getY()*ratioHeight   + marginMore ,bitmap.getHeight())));
            final Bitmap stretch = BitmapUtils.cropBitmap(pointScrop,bitmap);
            doNetRequest(stretch,locations,1.0d*llWidth/bitmap.getWidth(),1.0d*llHeight/bitmap.getHeight(),children.getType(),null);

        }catch (Exception e){
            LogUtils.e("dealListenQuestion error = " + e.getMessage());
        }
    }

    public void addViewForWholeTest(double ratioWidth , double ratioHeight,Location[] locations) {
        for(int j = 0 ; j< locations.length ; j++ ){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
            Location location =locations[j];
            //获取中位点
            // 30 是textSize的1.5倍换算过来的
            double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/3 + location.getTop_left().getX();
            double midY = /*1.0d*(location.getRight_bottom().getY() - location.getTop_left().getY() )/2 +*/ location.getTop_left().getY() - 20;
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
            child.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight) ,0,0);
            child.setLayoutParams(params);
            // 调用一个参数的addView方法
            llShow.addView(child,params);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    @SuppressLint("NewApi")
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
            LogUtils.d("point ======" + point.toString() + ",width = " +edge.width() + ",height = " + edge.height());
        }
        Point leftTop = points.get(0);
        Point righttop=points.get(1);
        Point leftbottom=points.get(2);
        Point rightbottom=points.get(3);
        Utils.matToBitmap(mRgbaOrigin, bitmap);


        //add by lzy for class exam demo
        //试卷宽高分别为600 和 720 px ,需要先计算拍照得宽高和真实试卷宽高得比例才好定位
        //这里要考虑到展示到设备上得时候，图片可能已经拉伸或压缩了，所以不推荐使用图片比例计算
        double leftHeight = leftbottom.y - leftTop.y;
        double rightHeight = rightbottom.y - righttop.y;
        double topWidth =  righttop.x - leftTop.x;
        double bottomWidth = rightbottom.x - leftbottom.x;

        double height = Math.min(leftHeight, rightHeight);
        double height2 = Math.max(leftHeight, rightHeight);
        double width = Math.min(bottomWidth, topWidth);
        double width2 = Math.max(bottomWidth, topWidth);

        int examHeight = 933;
        int examWidth = 662;
        double gapWidth = width2 - width;
        double ratioHeight  = height /examHeight ;
        double ratioWidth = width / examWidth;

        LogUtils.d("ratioWidth = " + ratioWidth + " , ratioHeight = " + ratioHeight   );
        LogUtils.d("leftHeight = " + leftHeight + " , rightHeight = " + rightHeight  +  ",topWidth = " + topWidth + ",bottomWidth = " + bottomWidth  );
        LogUtils.d("ratioWidth = " +  width  + " , ratioHeight00 = " + height  + ",maxWidth =" + width2 + ",maxHeight = " + height2  + ",gapWidth = " + gapWidth);



        Data data = GsonUtil.gsonToBean(getString(R.string.json_string_b),Data.class);
        Block[] blocks = data.getBlock();

        //修改各个答案显示在照片里的坐标值
        for(int j = 0 ; j< blocks.length ; j++ ){
            // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
            Block block = blocks[j];
            Location location = block.getLine().getLocation();
            LogUtils.d("Location == = " + location.toString() + " ,j =  = " + j + ",leftTop. x = " + leftTop. x  + ",leftTop. y = " + leftTop. y);

            List<Point> pointScrop = new ArrayList<>();
            int marginMore = 8;
            pointScrop.add(new Point((leftTop. x  + location.getTop_left().getX() *ratioWidth )-2* marginMore ,(leftTop.y + location.getTop_left().getY()*ratioHeight)- 2.7*marginMore ));
            pointScrop.add(new Point((leftTop. x  + location.getRight_bottom().getX()*ratioWidth )  + 2* marginMore ,(leftTop.y + location.getTop_left().getY()*ratioHeight) - 2.7*marginMore));
            pointScrop.add(new Point((leftTop. x  + location.getTop_left().getX()*ratioWidth)  - 2*marginMore,(leftTop.y + location.getRight_bottom().getY()*ratioHeight)  + 2*marginMore ));
            pointScrop.add(new Point((leftTop. x  + location.getRight_bottom().getX()*ratioWidth ) + 2* marginMore ,(leftTop.y + location.getRight_bottom().getY()*ratioHeight   + 2*marginMore )));
            Bitmap stretch = BitmapUtils.cropBitmap(pointScrop,bitmap);
            String root = getExternalCacheDir().getAbsolutePath();
            String dirName = "erweima16";
            File appDir = new File(root , dirName);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            //文件名为时间
            String path = "Image" + j + ".jpg";
            BitmapUtils.saveImageToGallery(stretch,MainActivity.this,j);

            location.setTop_left(new com.readboy.bean.old.Point((int)(leftTop.x + location.getTop_left().getX()*ratioWidth),(int)(leftTop.y + location.getTop_left().getY()*ratioHeight)));
            location.setRight_bottom(new com.readboy.bean.old.Point((int)(leftTop.x + location.getRight_bottom().getX()*ratioWidth),(int)(leftTop.y + location.getRight_bottom().getY()*ratioHeight)));
            LogUtils.d("Location111 == = " + (leftTop. x + location.getTop_left().getX()*ratioWidth) + " ,j =  = " + j + ",leftTop. y = " + (leftTop.y + location.getTop_left().getY()*ratioHeight) );
            LogUtils.d("Location222 == = " + (leftTop. x + location.getRight_bottom().getX()*ratioWidth ) + " ,j =  = " + j + ",leftTop. y = " + (leftTop.y + location.getRight_bottom().getY()*ratioHeight) );
        }
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));
        //doNetRequest(bitmap);

        LogUtils.d("ratioWidth1111 = " + llWidth/width + " , ratioHeight1111 = " + llHeight/height + ",block size = " + blocks.length );

        //addView(llWidth/600,llHeight/800);
        addViewForWholeTest(1.0d*llWidth/bitmap.getWidth(),1.0d*llHeight/bitmap.getHeight(),blocks);
       // addViewForWholeTest(llWidth/600,llHeight/800,blocks);

    }


    public void addViewForWholeTest(double ratioWidth , double ratioHeight,Block[] blocks) {
        for(int j = 0 ; j< blocks.length ; j++ ){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
            Block block = blocks[j];
            Location location = block.getLine().getLocation();
            //获取中位点
            // 30 是textSize的1.5倍换算过来的
            double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/3 + location.getTop_left().getX();
            double midY = /*1.0d*(location.getRight_bottom().getY() - location.getTop_left().getY() )/2 +*/ location.getTop_left().getY() - 20;
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
            child.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
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

    private void doNetRequest(final Bitmap bitmap, final Location[] locations, final double ratioWidth , final double ratioHeight , final int type,Location location ) {
        if (null == bitmap){
            LogUtils.d("doNetRequest null == bitmap");
            return;
        }
        Map<String, String> header = null;
        try {
            header = NetUtil.constructHeader("en", "true");

            //Bitmap转换成byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageByteArray = baos.toByteArray();
            //byte[] imageByteArray = FileUtil.read2ByteArray(path,1);
            String imageBase64 = new String(Base64.encodeBase64(imageByteArray), "UTF-8");
            String bodyParam = "image=" + imageBase64;
            LogUtils.d("result == " + "body size = " + imageByteArray.length);

            final String result = HttpUtil.doPost(WEBOCR_URL, header, bodyParam);
            LogUtils.d("result == " + result);
            switch (type){
                case 10001:
                    dealChoose(result,locations[0],ratioWidth,ratioHeight);
                    break;
                case 10006:
                    dealFillIn(result,location,ratioWidth,ratioHeight);
                    break;
                case 10023:
                    dealListen(result,locations,ratioWidth,ratioHeight);
                    break;
            }
        } catch (Exception e) {
            LogUtils.d("error = " + e);
        }
        /*new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
*/


    }

    private void dealFillIn(String result, final Location location, final double ratioWidth , final double ratioHeight){
        if (!TextUtils.isEmpty(result)) {
            BaseResponse baseResponse = GsonUtil.gsonToBean(result, BaseResponse.class);
            if (baseResponse != null) {
                String resultFinal = "";
                Line[] lines = baseResponse.getData().getBlock()[0].getLine();
                if (null != lines && lines.length > 0) {
                    List<Line> list = Arrays.asList(lines);
                    for (Line line : list) {
                        Word[] words = line.getWord();
                        for (Word word : words) {
                            String content = word.getContent();
                            String filter2 = content.replaceAll("[\\p{P}‘’“”]","");
                            if(!TextUtils.isEmpty(filter2) ){
                                resultFinal = filter2;
                                LogUtils.d("dealListen line = " + filter2);
                                break;
                            }
                        }
                    }
                }
                final String finalResultFinal = resultFinal;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addFillInAnswer(ratioWidth,ratioHeight,location, finalResultFinal);
                    }
                });
            }
        }
    }

    private void dealChoose(String result, final Location locations, final double ratioWidth , final double ratioHeight){
        if (!TextUtils.isEmpty(result)) {
            BaseResponse baseResponse = GsonUtil.gsonToBean(result, BaseResponse.class);
            if (baseResponse != null) {
                String answerResult = "";
                Line[] lines = baseResponse.getData().getBlock()[0].getLine();
                if (null != lines && lines.length > 0) {
                    List<Line> list = Arrays.asList(lines);
                    List<Line> lineList = new ArrayList(list);
                    Collections.sort(lineList);
                    for (Line line : lineList) {
                        LogUtils.d("dealListen line = " + line.toString());
                        Word[] words = line.getWord();
                        for (Word word : words) {
                            String content = word.getContent();
                            if(content.contains("(") || content.contains(")") || content.contains("（") || content.contains("）")){
                                content = content.replaceAll("（","(");
                                content = content.replaceAll("）",")");
                                String resultLast = DeviceUtil.getResultFromContent(content);
                                if(!TextUtils.isEmpty(resultLast) && resultLast.length() == 1 && PhotoUtil.checkEnglish(resultLast)){
                                    answerResult = resultLast;
                                    final String finalAnswerResult = answerResult;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //addView();
                                            LogUtils.d("dealChoose resultLast == " + finalAnswerResult);
                                            addChooseAnswer(ratioWidth,ratioHeight,locations, finalAnswerResult);

                                        }
                                    });
                                    LogUtils.d("dealChoose resultLast == " + resultLast + "ThreadId = " + Thread.currentThread().getId() + "Location = " + locations.toString());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void dealListen(String result, final Location[] locations, final double ratioWidth , final double ratioHeight){
        if (!TextUtils.isEmpty(result)) {
            BaseResponse baseResponse = GsonUtil.gsonToBean(result, BaseResponse.class);
            if (baseResponse != null) {
                Line[] lines = baseResponse.getData().getBlock()[0].getLine();
                if (null != lines && lines.length > 0) {
                    List<Line> list = Arrays.asList(lines);
                    List<Line> lineList = new ArrayList(list);
                    Collections.sort(lineList);

                    for (Line line : lineList) {
                        Word[] words = line.getWord();
                        for (Word word : words) {
                            String content = word.getContent();
                            String filter1 = content.replaceAll("\\d+","");
                            String filter2 = filter1.replaceAll("[\\p{P}‘’“”]","");

                            if(!TextUtils.isEmpty(filter2) && !PhotoUtil.isContainChinese(filter2)){
                                results.add(filter2);
                                LogUtils.d("dealListen line = " + filter2);

                            }
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //addView();
                        addListenAnswer(ratioWidth,ratioHeight,locations);

                    }
                });
            }
        }
    }

    public void addChooseAnswer(double ratioWidth , double ratioHeight,Location location,String result) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
        //获取中位点
        // 30 是textSize的1.5倍换算过来的
        double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/2 + location.getTop_left().getX();
        double midY =  location.getTop_left().getY() - 30 ;
        TextView child = new TextView(this);
        child.setTextSize(20);
        child.setText(result);

        LogUtils.d("midX  = " + midX  + ",midY === " + midY + ",location" + location.toString());
        child.setTextColor(getResources().getColor(R.color.green));
        child.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight)  ,0,0);
        child.setLayoutParams(params);

        Drawable drawable = llShow.getBackground();
        Bitmap bitmap = getRectangleBitmap(drawable,location);
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));
        llShow.addView(child,params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    public  Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    private Bitmap getRectangleBitmap(Drawable drawable,Location location){
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

    public void addFillInAnswer(double ratioWidth , double ratioHeight,Location location,String result) {
        Drawable drawable = llShow.getBackground();
        Bitmap bitmap = getRectangleBitmap(drawable,location);
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap));

        if (TextUtils.isEmpty(result)){
            LogUtils.d("addListenAnswer is null");
            return;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
        //获取中位点
        // 30 是textSize的1.5倍换算过来的
        double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/2 + location.getTop_left().getX();
        double midY = 1.0d*(location.getRight_bottom().getY() - location.getTop_left().getY() )/2 + location.getTop_left().getY() - 20 ;
        TextView child = new TextView(this);
        child.setTextSize(20);

        child.setText(result);
        LogUtils.d("midX  = " + midX  + ",midY === " + midY + ",location" + location.toString());
        child.setTextColor(getResources().getColor(R.color.green));
        child.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight)  ,0,0);
        child.setLayoutParams(params);
        // 调用一个参数的addView方法
        llShow.addView(child,params);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void addListenAnswer(double ratioWidth , double ratioHeight,Location[] locations) {
        if (results == null || results.size() == 0){
            LogUtils.d("addListenAnswer is null");
            return;
        }

        int size = Math.min(results.size(),locations.length);
        for(int j = 0 ; j< size ; j++ ){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
            Location location = locations[j];
            //获取中位点
            // 30 是textSize的1.5倍换算过来的
            double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/2 + location.getTop_left().getX();
            double midY =  location.getTop_left().getY() - 30 ;
            TextView child = new TextView(this);
            child.setTextSize(20);
            String result = results.get(j);
            child.setText(result);
            LogUtils.d("midX  = " + midX  + ",midY === " + midY + ",location" + location.toString());

            if(j % 2 == 0){
                child.setTextColor(getResources().getColor(R.color.green));

            }else {
                child.setTextColor(getResources().getColor(R.color.red));
            }
            child.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight)  ,0,0);
            child.setLayoutParams(params);
            // 调用一个参数的addView方法
            Drawable drawable = llShow.getBackground();
            Bitmap bitmap = getRectangleBitmap(drawable,location);
            llShow.setBackground(new BitmapDrawable(getResources(),bitmap));
            llShow.addView(child,params);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}



