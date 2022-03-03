package com.readboy.myopencvcamera;

//import org.apache.commons.codec.binary.Base64;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.readboy.net.FileUtil;
import com.readboy.net.HttpUtil;
import com.readboy.net.NetUtil;
import com.readboy.net.bean.BaseResponse;
import com.readboy.net.bean.Line;
import com.readboy.net.bean.Word;
import com.readboy.util.BitmapUtils;
import com.readboy.util.GsonUtil;
import com.readboy.util.PhotoUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.InvalidMarkException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.readboy.net.NetUtil.WEBOCR_URL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.initUndistortRectifyMap;
import org.opencv.core.Core;

@SuppressLint("NewApi")

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

    private static  final int TAKE_PHOTO_REQUEST = 1130;
    private static final int OPEN_CANMER = 1122;
    private String[] answer = {"Read", "boy", "girl", "child", "home", "Car", "bike", "book", "good","bad","tiger","apple","android","target","parent"
            ,"bus","cow","horse","house","water","fire","smog","at","where","when","how","what","why","ask","which"};
    private List<String> results = new ArrayList<>();
    DecimalFormat format = new DecimalFormat("#.00");

    private int mViewMode;
    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;
    private Mat last_mRgb;
    private Mat mRgb;
    private double sdThresh = 20;
    private double sdThresh2 = 5;
    private boolean motion = false;
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
        last_mRgb = new Mat(height, width, CvType.CV_8UC4);
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
//                Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                mRgba = mRgbaOrigin.clone();
                //保存当前图像，用于下次计算

                distMap(mRgba.clone(),last_mRgb.clone());
                last_mRgb = mRgbaOrigin.clone();
//                Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case VIEW_MODE_RGBA:
                hasSaved = false;
                mRgbaOrigin = inputFrame.rgba();
                mRgba = inputFrame.rgba();
                mRgba = mRgbaOrigin.clone();
                distMap(mRgba.clone(),last_mRgb.clone());
                //保存当前图像，用于下次计算
                last_mRgb = mRgbaOrigin.clone();
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


    public void distMap(Mat a,Mat b)
    {
        long curTime = System.currentTimeMillis();
        int roi_h = 480;
        int roi_w = 640;

        Imgproc.resize(a,a,new Size(roi_w,roi_h));
        Imgproc.resize(b,b,new Size(roi_w,roi_h));
        Mat diff = new Mat(a.rows(),a.cols(),CvType.CV_8UC3);
        Core.absdiff(a,b,diff);
        MatOfDouble tmp_m = new MatOfDouble();
        MatOfDouble tmp_sds = new MatOfDouble();
        Core.meanStdDev(diff,tmp_m,tmp_sds);
        Imgproc.GaussianBlur(diff,diff,new Size(3,3),0);
        double stdev = tmp_sds.get(0, 0)[0];
        if(stdev > sdThresh)
        {
            motion = true;
        }
        else
        {
            if(motion && stdev < sdThresh2)
            {
                //此处已完成翻页，触发一次拍照
                motion = false;
            }
        }
        LogUtils.d("该函数计算事件为： " + (System.currentTimeMillis() - curTime));
    }
}



