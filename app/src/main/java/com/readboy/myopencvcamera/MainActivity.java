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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
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
import com.readboy.net.BaseRequest;
import com.readboy.net.HttpUtil;
import com.readboy.net.NetUtil;
import com.readboy.net.RequestInterface;
import com.readboy.net.bean.BaseResponse;
import com.readboy.net.bean.ExamResponse;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

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
    private boolean isChoosed;
    private int fileNum = 99;
    private GifDrawable gifDrawable;
    private GifImageView gifImageView;

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
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBottomUIMenu();
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        mOpenCvCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenCvCameraView.setAutoFocus();
            }
        });
        llShow = (RelativeLayout) findViewById(R.id.ll_show);
       gifImageView = (GifImageView)findViewById(R.id.gif_iv);
       gifDrawable = (GifDrawable) gifImageView.getDrawable();
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                savePictureAccordExam(mRgba);
            }
        },3000);
    }

    protected void hideBottomUIMenu() {
        final View decorView = getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(uiOptions);
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        gifDrawable.start();
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

    @Override
    protected void onStop() {
        super.onStop();
        gifDrawable.stop();
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
        mRgba = inputFrame.rgba();
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
                /*Intent it = new Intent();
                it.setClassName("com.readboy.studyword","com.readboy.studyword.MainActivity");
                it.putExtra("sl_record_id",20L);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);*/
                llShow.setVisibility(View.GONE);
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mViewMode = VIEW_MODE_CANNY;
                break;
            case R.id.exitItem:
                finish();
                break;
            case R.id.clickItem:
                mOpenCvCameraView.cancelAutoFocus();
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                savePictureAccordExam(mRgba);
                mViewMode = VIEW_MODE_CLICK;
                break;
        }
        return true;
    }




    @SuppressLint("NewApi")
    private void savePictureAccordExam(final Mat frame){
        if(!gifDrawable.isRunning()){
        gifDrawable.start();
        gifDrawable.setLoopCount( 0); //设置播放的次数，播放完了就自动停止
    }
        Observable.create(new ObservableOnSubscribe<RectangleInfo>() {
            // 1. 创建被观察者 & 生产事件
            @Override
            public void subscribe(ObservableEmitter<RectangleInfo> emitter) {
                //long firstTime = System.currentTimeMillis();
                final RectangleInfo value = HandleImgUtils.getRectangleMain(frame,MainActivity.this);
               /* long secondTime = System.currentTimeMillis();
                LogUtils.d("savePictureAccordExam used time = " + (secondTime - firstTime));*/
                final RectangleInfo info = HandleImgUtils.dealRectangleCorrect(frame,value,MainActivity.this);
               /* long thirdTime = System.currentTimeMillis();
                LogUtils.d("savePictureAccordExam used time222 = " + (thirdTime - secondTime ));*/
                RectangleInfo info1 =showAnalyzingView(info);
               /* long fourTime = System.currentTimeMillis();
                LogUtils.d("savePictureAccordExam used time333 = " + (fourTime - thirdTime ));*/

                emitter.onNext(info1);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RectangleInfo>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(final RectangleInfo info) {
                        LogUtils.d("savePictureAccordExam onNextr = " );
                        gifDrawable.stop(); //停止播放
                        llShow.setVisibility(View.VISIBLE);
                        llShow.setBackground(new BitmapDrawable(getResources(),info.getBitmap()));
                        mOpenCvCameraView.setVisibility(View.GONE);
                        getScore();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.d("savePictureAccordExam error = " + e.getMessage());
                        //ShowToastUtils.showToast(MainActivity.this,"当前照片不规范，请调整姿势重新拍摄",Toast.LENGTH_SHORT);
                        //llShow.setVisibility(View.GONE);
                        //mOpenCvCameraView.setVisibility(View.VISIBLE);
                        //mOpenCvCameraView.enableView();
                        continuePhotoDelay();
                    }

                    @Override
                    public void onComplete() {
                        LogUtils.d("savePictureAccordExam onComplete ");

                    }
                });


    }

    private void getScore(){
        RequestInterface request = BaseRequest.getServer();
        File file = new File(BitmapUtils.getFilePath(MainActivity.this,fileNum++));
        LogUtils.d("savePictureAccordExam fileName = " + file.getName());
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        request.getScore(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ExamResponse>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(final ExamResponse info) {
                        LogUtils.d("savePictureAccordExam onNextr = " );
                        if(null != info ){
                            LogUtils.d("savePictureAccordExam onNextr = " + info.toString() );
                            if(info.getF_responseNo() == 100000){
                                if(!TextUtils.isEmpty(info.getF_score())){
                                    ShowToastUtils.showToast(MainActivity.this,"识别成功，当前试卷得分为" + info.getF_score() + "分",Toast.LENGTH_SHORT);
                                    continuePhotoDelay();
                                }
                            }else {
                                //ShowToastUtils.showToast(MainActivity.this,"当前照片不规范，请调整姿势重新拍摄",Toast.LENGTH_SHORT);
                                continuePhotoDelay();

                            }


                        }

                        //llShow.setVisibility(View.VISIBLE);
                        //mOpenCvCameraView.setVisibility(View.GONE);
                        //showAnalyzingView(value);
                        //continuePhotoDelay();

                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.d("savePictureAccordExam error = " + e.getMessage());
                       // ShowToastUtils.showToast(MainActivity.this,"当前照片不规范，请调整姿势重新拍摄",Toast.LENGTH_SHORT);
                        /*llShow.setVisibility(View.GONE);
                        mOpenCvCameraView.setVisibility(View.VISIBLE);
                        mOpenCvCameraView.enableView();*/
                        //continuePhotoDelay();
                    }

                    @Override
                    public void onComplete() {
                        LogUtils.d("savePictureAccordExam onComplete ");

                    }
                });
    }

    private void continuePhotoDelay(){
        mOpenCvCameraView.setVisibility(View.VISIBLE);
        llShow.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                savePictureAccordExam(mRgba);
                mViewMode = VIEW_MODE_CLICK;
            }

        }, 5000);
    }


    private RectangleInfo showAnalyzingView( RectangleInfo info){
        List < Point > points = info.getPoints();
        final Bitmap bitmap = info.getBitmap();

        //todo 展示分析界面
        final Bitmap stretch =BitmapUtils.cropBitmap(points,bitmap);
        info.setBitmap(stretch);
        BitmapUtils.saveImageToGallery(stretch,MainActivity.this,fileNum);
        isChoosed = false;
        return info;

        /*LogUtils.d("savePictureAccordExam continuePhotoDelay ");
        Toast.makeText(MainActivity.this,"扫描完成，请放入下一张试卷",Toast.LENGTH_SHORT).show();
        llShow.setVisibility(View.GONE);
        mOpenCvCameraView.setVisibility(View.VISIBLE);
        mViewMode = VIEW_MODE_RGBA;
        mOpenCvCameraView.cancelAutoFocus();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
    }



}



