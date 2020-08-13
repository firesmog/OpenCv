package com.readboy.myopencvcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readboy.bean.newexam.Answer;
import com.readboy.bean.newexam.Children;
import com.readboy.bean.newexam.ChildrenQuestion;
import com.readboy.bean.newexam.ExamBean;
import com.readboy.bean.newexam.RectangleInfo;
import com.readboy.bean.old.Location;
import com.readboy.log.LogUtils;
import com.readboy.net.HttpUtil;
import com.readboy.net.NetUtil;
import com.readboy.net.bean.BaseResponse;
import com.readboy.net.bean.Line;
import com.readboy.net.bean.Word;
import com.readboy.util.BinaryUtils;
import com.readboy.util.BitmapUtils;
import com.readboy.util.DeviceUtil;
import com.readboy.util.GrayUtils;
import com.readboy.util.GsonUtil;
import com.readboy.util.HandleImgUtils;
import com.readboy.util.PhoneTypeUtil;
import com.readboy.util.PhotoUtil;

import org.apache.commons.codec.binary.Base64;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.readboy.net.NetUtil.WEBOCR_URL;

public class TakePhotoActivity extends BaseActivity  implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {

    private RelativeLayout llShow;
    private int llWidth;
    private int llHeight;
    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;
    private ImageView ivAlbum;
    private ImageView ivPhoto;
    private ImageView ivCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takephoto);
        checkCameraPermission();
        initView();
        setAutoFocusListener();
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    private void initView(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        llShow = (RelativeLayout) findViewById(R.id.iv_show);
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        ivPhoto = (ImageView) findViewById(R.id.iv_take_photo);
        ivAlbum = (ImageView) findViewById(R.id.iv_from_album);
        ivCancel.setOnClickListener(this);
        ivAlbum.setOnClickListener(this);
        ivPhoto.setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_cancel:
                break;
            case R.id.iv_take_photo:
                mOpenCvCameraView.cancelAutoFocus();
                savePictureAccordExam(mRgba);
                llShow.setVisibility(View.VISIBLE);
                mOpenCvCameraView.setVisibility(View.GONE);
                break;
            case R.id.iv_from_album:
                break;
        }
    }
    @SuppressLint("NewApi")
    private void savePictureAccordExam(Mat frame){
        try {
            RectangleInfo info = HandleImgUtils.dealRectangleCorrect(frame,this);
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
            LogUtils.d("getExamData = = " + data.toString()   );

            int examHeight = data.getHeight();
            int examWidth = data.getWidth();
            double gapWidth = width2 - width;
            final double ratioHeight  = height/examHeight ;
            final double ratioWidth = width/examWidth;
            LogUtils.d("ratioWidth = " + ratioWidth + " , ratioHeight = " + ratioHeight   );
            LogUtils.d("ratioWidth leftHeight = " + leftHeight + " , rightHeight = " + rightHeight  +  ",topWidth = " + topWidth + ",bottomWidth = " + bottomWidth  );
            LogUtils.d("ratioWidth  width = " +  width  + " , ratioHeight00 = " + height  + ",maxWidth =" + width2 + ",maxHeight = " + height2  + ",gapWidth = " + gapWidth);
            List<Children> bigQuestion = data.getChildren();
            Bitmap stretch = BitmapUtils.cropBitmap(points,bitmap);
            llShow.setBackground(new BitmapDrawable(getResources(),stretch));
        }catch (Exception e){
            LogUtils.d("error = " + e.getMessage());
        }


    }
}