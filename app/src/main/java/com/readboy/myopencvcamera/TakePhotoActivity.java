package com.readboy.myopencvcamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.readboy.bean.newexam.Answer;
import com.readboy.bean.newexam.Children;
import com.readboy.bean.newexam.ChildrenQuestion;
import com.readboy.bean.newexam.ExamBean;
import com.readboy.bean.newexam.RectangleInfo;
import com.readboy.bean.old.Location;
import com.readboy.log.LogUtils;
import com.readboy.net.HttpUtil;
import com.readboy.net.NetUtil;
import com.readboy.net.RxDisposeManager;
import com.readboy.net.bean.BaseResponse;
import com.readboy.net.bean.Line;
import com.readboy.net.bean.Word;
import com.readboy.util.AnimatorUtil;
import com.readboy.util.BitmapUtils;
import com.readboy.util.DeviceUtil;
import com.readboy.util.GsonUtil;
import com.readboy.util.HandleImgUtils;
import com.readboy.util.PhoneTypeUtil;
import com.readboy.util.PhotoUtil;

import org.apache.commons.codec.binary.Base64;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.os.SystemClock.sleep;
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
    private ImageView ivScan;
    private boolean stoped;


    Handler handler=new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            if(stoped){
                return;
            }
            scanRectangleArea(mRgba,TakePhotoActivity.this);
            handler.postDelayed(this, 2000);
        }
    };
    private RelativeLayout llAnalyze;
    private RelativeLayout llResult;
    private RelativeLayout llInclude;
    private List<Boolean> results = new ArrayList<>();


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
        AnimatorUtil.endUpAndDownAnimator();
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
        AnimatorUtil.endUpAndDownAnimator();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    private void initView(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        llShow = (RelativeLayout) findViewById(R.id.ll_show);
        llInclude = (RelativeLayout) findViewById(R.id.include_take_photo);
        llAnalyze = (RelativeLayout) findViewById(R.id.ll_analyzing);
        llResult = (RelativeLayout) findViewById(R.id.ll_show_result);
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        ivPhoto = (ImageView) findViewById(R.id.iv_take_photo);
        ivAlbum = (ImageView) findViewById(R.id.iv_from_album);
        ivScan = (ImageView) findViewById(R.id.iv_scan_test);
        ivCancel.setOnClickListener(this);
        ivAlbum.setOnClickListener(this);
        ivPhoto.setOnClickListener(this);
        mOpenCvCameraView.setCvCameraViewListener(this);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        llWidth = DeviceUtil.dip2px(this,750);
        if(PhoneTypeUtil.SYS_EMUI.equals(PhoneTypeUtil.getSystem())){
            llHeight = outMetrics.heightPixels ;
        }else {
            llHeight = outMetrics.heightPixels + 72;
        }
        Toast.makeText(this,getString(R.string.string_photo_tip),Toast.LENGTH_LONG).show();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_cancel:
                break;
            case R.id.iv_take_photo:
                mOpenCvCameraView.cancelAutoFocus();
                savePictureAccordExam(mRgba);
                break;
            case R.id.iv_from_album:
                break;
        }
    }

    //处理预览界面的简陋版本
    private void scanRectangleArea(Mat frame, Context context){
        Mat frameData = HandleImgUtils.processImage(frame,context);
        Bitmap bitmap = Bitmap.createBitmap(frameData.width(), frameData.height(), Bitmap.Config.ARGB_8888);
        Mat edge=new Mat();
        Imgproc.Canny(frameData,edge,90,270,5,true);
        Utils.matToBitmap(edge, bitmap);
        BitmapUtils.saveImageToGallery(bitmap,context,7777);
        List<Point> points = HandleImgUtils.getCornersByContour(edge);
        if(null == points){
            handler.postDelayed(runnable, 2000);
            return;
        }
        final Point leftTop = points.get(0);
        Point righttop=points.get(1);
        Point leftbottom=points.get(2);
        Point rightbottom=points.get(3);
        Utils.matToBitmap(frame, bitmap);
        final com.readboy.bean.newexam.Location location = new com.readboy.bean.newexam.Location(leftTop,rightbottom);
        Bitmap bitmap1 = BitmapUtils.getRectangleBitmap(bitmap,location);
        handler.removeCallbacks(runnable);
        stoped = true;
        mOpenCvCameraView.setVisibility(View.GONE);
        llShow.setVisibility(View.VISIBLE);
        llShow.setBackground(new BitmapDrawable(getResources(),bitmap1));

    }


    @SuppressLint("NewApi")
    private void savePictureAccordExam(final Mat frame){
        Observable.create(new ObservableOnSubscribe<RectangleInfo>() {
            // 1. 创建被观察者 & 生产事件
            @Override
            public void subscribe(ObservableEmitter<RectangleInfo> emitter) {
                final RectangleInfo info = HandleImgUtils.dealRectangleCorrect(frame,TakePhotoActivity.this);
                final Bitmap bitmap = info.getBitmap();
                BitmapUtils.saveImageToGallery(bitmap,TakePhotoActivity.this,9977);
                emitter.onNext(info);
                emitter.onComplete();
            }
        })
                //.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RectangleInfo>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(final RectangleInfo value) {
                llInclude.setVisibility(View.VISIBLE);
                llShow.setVisibility(View.VISIBLE);
                mOpenCvCameraView.setVisibility(View.GONE);
                llShow.setBackground(new BitmapDrawable(getResources(),value.getBitmap()));
                AnimatorUtil.startUpAndDownAnimator(ivScan);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAnalyzingView(value);
                    }
                },3000);
            }

            @Override
            public void onError(Throwable e) {
                LogUtils.d("savePictureAccordExam error = " + e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });


    }

    private void showAnalyzingView( RectangleInfo info){
        List < Point > points = info.getPoints();
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
        LogUtils.d("ratioWidth leftHeight = " + leftHeight + " , rightHeight = " + rightHeight  +  ",topWidth = " + topWidth + ",bottomWidth = " + bottomWidth + "ratioWidth = " + ratioWidth + " , ratioHeight = " + ratioHeight
        + ",gap = " + gapWidth);
        //todo 展示分析界面
       final Bitmap stretch =BitmapUtils.cropBitmap(points,bitmap);
        List<Children> bigQuestion = data.getChildren();
        for (final Children children : bigQuestion) {
            switch (children.getType()){
                case 10001:
                    //1.处理选择题
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dealChooseQuestion(children,leftTop,ratioWidth,ratioHeight,stretch);
                        }
                    }).start();

                    break;
                case 10006:
                    //2.处理主观填空题
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dealFillInQuestion(children,leftTop,ratioWidth,ratioHeight,stretch);
                        }
                    }).start();

                    break;
                case 10023:
                    //3.处理听力填空题
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dealListenQuestion(children,leftTop,ratioWidth,ratioHeight,stretch);
                        }
                    }).start();
                    break;
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
            Answer answer = new Answer();
            for(int i = 0 ; i < locations.length ; i++){
                answer = realQuestion.getAnswer().get(i);
                com.readboy.bean.old.Point answerPoint1 =  new com.readboy.bean.old.Point((int)((answer.getLeftTopX() + parentLeftTop.getX())*ratioWidth),(int)((answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                com.readboy.bean.old.Point answerPoint2 =  new com.readboy.bean.old.Point((int)((answer.getRightBottomX() + parentLeftTop.getX())*ratioWidth),(int)((answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                locations[i] = new Location(answerPoint1,answerPoint2);
            }
            List<Point> pointScrop = new ArrayList<>();
            int marginMore = 10;
            pointScrop.add(new Point(Math.max((( realQuestionLeftTop.getX() *ratioWidth )- 2.2* marginMore),1) ,Math.max((realQuestionLeftTop.getY()*ratioHeight)- 0.8*marginMore ,1)));
            pointScrop.add(new Point(Math.min((( realQuestionRightBottom.getX()*ratioWidth )  + 2.2* marginMore ),bitmap.getWidth()),Math.max((realQuestionLeftTop.getY()*ratioHeight) - 0.8*marginMore,1)));
            pointScrop.add(new Point(Math.max(( realQuestionLeftTop.getX()*ratioWidth)  -2.2*marginMore,1),Math.min(realQuestionRightBottom.getY()*ratioHeight  + 1.2*marginMore ,bitmap.getHeight())));
            pointScrop.add(new Point(Math.min(( realQuestionRightBottom.getX()*ratioWidth ) + 2.2* marginMore ,bitmap.getWidth()),Math.min(realQuestionRightBottom.getY()*ratioHeight   + 1.2*marginMore ,bitmap.getHeight())));
            Bitmap stretch = BitmapUtils.cropBitmap(pointScrop,bitmap);
            BitmapUtils.saveImageToGallery(stretch,this,6660 +  j);
            doNetRequest(stretch,bitmap,locations,1.0d*llWidth/bitmap.getWidth(),1.0d*llHeight/bitmap.getHeight(),children.getType(),null,answer);
        }
    }

    private void dealFillInQuestion(final Children children, Point leftTop, double ratioWidth , double ratioHeight, final Bitmap bitmap){
        com.readboy.bean.old.Point parentLeftTop = new com.readboy.bean.old.Point(children.getLeftTopX(),children.getLeftTopY());
        List<ChildrenQuestion> childQuestion = children.getChildren();
        for(int j = 1; j< childQuestion.size(); j++){
            ChildrenQuestion realQuestion = childQuestion.get(j);
            LogUtils.d("realQuestion == " + realQuestion.toString() + ",parentLT = "  + parentLeftTop.toString());
            //计算答案坐标，封装到location
            Answer answer = new Answer();
            for(int i = 0 ; i < realQuestion.getAnswer().size() ; i++){
                answer = realQuestion.getAnswer().get(i);
                com.readboy.bean.old.Point answerPoint1 =  new com.readboy.bean.old.Point((int)((answer.getLeftTopX() + parentLeftTop.getX())*ratioWidth),(int)((answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                com.readboy.bean.old.Point answerPoint2 =  new com.readboy.bean.old.Point((int)((answer.getRightBottomX() + parentLeftTop.getX())*ratioWidth),(int)((answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                final Location location = new Location(answerPoint1,answerPoint2);
                //填空题得每个答案单独处理
                final List<Point> pointScrop = new ArrayList<>();
                int marginMore = 6;

                if(i == 0){
                    pointScrop.add(new Point(Math.max((((answer.getLeftTopX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)-  1.4*marginMore),1) ,Math.max(((answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight)-1.0*marginMore ,1)));
                    pointScrop.add(new Point(Math.min((((answer.getRightBottomX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)  +  1.4*marginMore ),bitmap.getWidth()),Math.max(((answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight)- 1.0*marginMore,1)));
                    pointScrop.add(new Point(Math.max(((answer.getLeftTopX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)  -1.4*marginMore,1),Math.min((answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight + 2*marginMore ,bitmap.getHeight())));
                    pointScrop.add(new Point(Math.min(((answer.getRightBottomX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth ) +  1.4* marginMore ,bitmap.getWidth()),Math.min((answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight + 2*marginMore,bitmap.getHeight())));
                }else {
                    pointScrop.add(new Point(Math.max((((answer.getLeftTopX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)-  1.4*marginMore),1) ,Math.max(((answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight)-0.9*marginMore ,1)));
                    pointScrop.add(new Point(Math.min((((answer.getRightBottomX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)  +  1.4*marginMore ),bitmap.getWidth()),Math.max(((answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight)- 0.9*marginMore,1)));
                    pointScrop.add(new Point(Math.max(((answer.getLeftTopX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth)  - 1.4*marginMore,1),Math.min((answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight + 1.3*marginMore ,bitmap.getHeight())));
                    pointScrop.add(new Point(Math.min(((answer.getRightBottomX() + realQuestion.getLeftTopX() + parentLeftTop.getX())*ratioWidth ) +   1.4*marginMore ,bitmap.getWidth()),Math.min((answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight + 1.3*marginMore,bitmap.getHeight())));
                }

                final Bitmap stretch = BitmapUtils.cropBitmap(pointScrop,bitmap);
                BitmapUtils.saveImageToGallery(stretch,this,11111 + 100*j + i);
                final Answer finalAnswer = answer;
                doNetRequest(stretch,bitmap,null,1.0d*llWidth/bitmap.getWidth(),1.0d*llHeight/bitmap.getHeight(),children.getType(),location, finalAnswer);

            }

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
            Answer answer = new Answer();
            for(int i = 0 ; i < locations.length ; i++){
                answer = realQuestion.getAnswer().get(i);
                com.readboy.bean.old.Point answerPoint1 =  new com.readboy.bean.old.Point((int)((answer.getLeftTopX() + parentLeftTop.getX())*ratioWidth),(int)((answer.getLeftTopY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                com.readboy.bean.old.Point answerPoint2 =  new com.readboy.bean.old.Point((int)((answer.getRightBottomX() + parentLeftTop.getX())*ratioWidth),(int)((answer.getRightBottomY() + realQuestion.getLeftTopY() + parentLeftTop.getY())*ratioHeight));
                locations[i] = new Location(answerPoint1,answerPoint2);
            }

            List<Point> pointScrop = new ArrayList<>();
            int marginMore = 10;
            pointScrop.add(new Point(Math.max((( realQuestionLeftTop.getX() *ratioWidth )- 2.2* marginMore),1) ,Math.max((realQuestionLeftTop.getY()*ratioHeight)- 1.5*marginMore ,1)));
            pointScrop.add(new Point(Math.min((( realQuestionRightBottom.getX()*ratioWidth )  + 2.2* marginMore ),bitmap.getWidth()),Math.max((realQuestionLeftTop.getY()*ratioHeight) - 1.5*marginMore,1)));
            pointScrop.add(new Point(Math.max(( realQuestionLeftTop.getX()*ratioWidth)  -2.2*marginMore,1),Math.min(realQuestionRightBottom.getY()*ratioHeight  + marginMore ,bitmap.getHeight())));
            pointScrop.add(new Point(Math.min(( realQuestionRightBottom.getX()*ratioWidth ) + 2.2* marginMore ,bitmap.getWidth()),Math.min(realQuestionRightBottom.getY()*ratioHeight   + marginMore ,bitmap.getHeight())));
            final Bitmap stretch = BitmapUtils.cropBitmap(pointScrop,bitmap);
            BitmapUtils.saveImageToGallery(stretch,this,11111);
            doNetRequest(stretch,bitmap,locations,1.0d*llWidth/bitmap.getWidth(),1.0d*llHeight/bitmap.getHeight(),children.getType(),null,answer);
        }catch (Exception e){
            LogUtils.e("dealListenQuestion error = " + e.getMessage());
        }
    }


    private void doNetRequest(final Bitmap bitmap, final Bitmap bitmapShow, final Location[] locations, final double ratioWidth , final double ratioHeight , final int type, final Location location, final Answer answer ) {

        LogUtils.d("ratioWidth = " + ratioWidth + " , ratioHeight = " + ratioHeight + ",llWidth = " + llWidth + "，llHeight = " + llHeight  + " , type" + type  );
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

            final String result = HttpUtil.doPost(WEBOCR_URL, header, bodyParam,1);
            LogUtils.d("result == " + result);

           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   if(llInclude.getVisibility() == View.VISIBLE){
                       llInclude.setVisibility(View.GONE);
                       llAnalyze.setVisibility(View.VISIBLE);
                       AnimatorUtil.endUpAndDownAnimator();
                       llResult.setBackground(new BitmapDrawable(getResources(),bitmapShow));
                   }
               }
           });

            switch (type){
                case 10001:
                    dealChoose(result,locations[0],ratioWidth,ratioHeight,answer);
                    break;
                case 10006:
                    dealFillIn(result,location,ratioWidth,ratioHeight,answer);
                    break;
                case 10023:
                    dealListen(result,locations,ratioWidth,ratioHeight,answer);
                    break;
            }
        } catch (Exception e) {
            LogUtils.d("error = " + e);
        }
        /*Map<String, String> header = null;
        String imageBase64 = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageByteArray = baos.toByteArray();

        try {
            header = NetUtil.constructHeader("en", "true");
            imageBase64 = new String(Base64.encodeBase64(imageByteArray), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String bodyParam = "image=" + imageBase64;
        LogUtils.d("result == " + "body size = " + imageByteArray.length);

        HttpUtil.doPost(WEBOCR_URL, header, bodyParam)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String result) {
                        LogUtils.d("doNetRequest onNext " + result);
                        if(llInclude.getVisibility() == View.VISIBLE){
                            llInclude.setVisibility(View.GONE);
                            llAnalyze.setVisibility(View.VISIBLE);
                            AnimatorUtil.endUpAndDownAnimator();
                            llResult.setBackground(new BitmapDrawable(getResources(),bitmapShow));
                        }

                        switch (type){
                            case 10001:
                                dealChoose(result,locations[0],ratioWidth,ratioHeight,answer);
                                break;
                            case 10006:
                                dealFillIn(result,location,ratioWidth,ratioHeight,answer);
                                break;
                            case 10023:
                                dealListen(result,locations,ratioWidth,ratioHeight,answer);
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.d("doNetRequest error = " + e.getMessage());

                    }

                    @Override
                    public void onComplete() {

                    }
                });*/

    }


    private void dealChoose(String result, final Location locations, final double ratioWidth , final double ratioHeight, final Answer answer){
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
                        String[] results = dealWordEveryLine(words);
                        if(null != results && results.length > 0){
                            for (String s : results) {
                                if(!TextUtils.isEmpty(s) && s.length() == 1 && PhotoUtil.checkEnglish(s)){
                                    answerResult = s;
                                    final String finalAnswerResult = answerResult;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LogUtils.d("dealChoose resultLast == " + finalAnswerResult);
                                            addChooseAnswer(ratioWidth,ratioHeight,locations, finalAnswerResult,answer);
                                        }
                                    });
                                    return;
                                }
                            }
                        }else {
                            LogUtils.d("dealListen results is null " );

                        }

                    }
                }
            }
        }

    }

    private void dealFillIn(String result, final Location location, final double ratioWidth , final double ratioHeight, final Answer answer){
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
                        addFillInAnswer(ratioWidth,ratioHeight,location, finalResultFinal,answer);
                    }
                });
            }
        }
    }


    private void dealListen(String result, final Location[] locations, final double ratioWidth , final double ratioHeight, final Answer answer){
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
                                if(filter2.equals(answer.getContent())){
                                    results.add(true);
                                }else {
                                    results.add(false);
                                }
                                LogUtils.d("dealListen line = " + filter2);
                            }
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //addView();
                        addListenAnswer(ratioWidth,ratioHeight,locations,answer);

                    }
                });
            }
        }
    }



    private String[] dealWordEveryLine(Word[] words){
        String answerResult = "";
        StringBuilder contentBuilder = new StringBuilder();
        for (Word word : words) {
            contentBuilder.append(word);
        }
        String content = contentBuilder.toString();

        if(content.contains("(") || content.contains(")") || content.contains("（") || content.contains("）")){
            content = content.replaceAll("（","(");
            content = content.replaceAll("）",")");
            answerResult = DeviceUtil.getResultFromContent(content);
            LogUtils.d("dealChoose resultLast == " + answerResult);
        }

        if(TextUtils.isEmpty(answerResult)){
            return null;
        }else {
            return answerResult.split("￥&#&#@");
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void addChooseAnswer(double ratioWidth , double ratioHeight, Location location, String result, Answer answer) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 定义显示组件的布局管理器，为了简单，本次只定义一个TextView组件
        //获取中位点
        // 30 是textSize的1.5倍换算过来的
        double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/2 + location.getTop_left().getX();
        double midY =  1.0d*(location.getRight_bottom().getY() - location.getTop_left().getY() )/5 + location.getTop_left().getY() ;  ;
        ImageView child  = new ImageView(this);
        boolean right = answer.getContent().equals(result);
        if(right){
            child.setBackground(getResources().getDrawable(R.mipmap.ic_right));
        }else {
            child.setBackground(getResources().getDrawable(R.mipmap.ic_error));
        }

        LogUtils.d("answer  = " + answer  + ",result  === " + result + ",location" + location.toString() + "answer is right = " + right);
        params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight)  ,0,0);
        child.setLayoutParams(params);
        llResult.addView(child,params);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void addFillInAnswer(double ratioWidth , double ratioHeight, Location location, String result, Answer answer) {
        Drawable drawable = llShow.getBackground();
        Bitmap bitmap = BitmapUtils.getRectangleBitmap(drawable,location);
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
        double midY = 1.0d*(location.getRight_bottom().getY() - location.getTop_left().getY() )/8 + location.getTop_left().getY() ;
        ImageView child  = new ImageView(this);
        boolean right = answer.getContent().equals(result);
        if(right){
            child.setBackground(getResources().getDrawable(R.mipmap.ic_right));
        }else {
            child.setBackground(getResources().getDrawable(R.mipmap.ic_error));
        }
        LogUtils.d("answer  = " + answer  + ",result  === " + result + ",location" + location.toString() + "answer is right = " + right);
        params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight)  ,0,0);
        child.setLayoutParams(params);
        // 调用一个参数的addView方法
        llResult.addView(child,params);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void addListenAnswer(double ratioWidth , double ratioHeight, Location[] locations, Answer answer) {
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
            double midX = 1.0d*(location.getRight_bottom().getX() - location.getTop_left().getX())/1.2d + location.getTop_left().getX();
            double midY =  location.getTop_left().getY() - 5 ;
            ImageView child = new ImageView(this);
            boolean right = results.get(j);
            if(right){
                child.setBackground(getResources().getDrawable(R.mipmap.ic_right));
            }else {
                child.setBackground(getResources().getDrawable(R.mipmap.ic_error));
            }

            LogUtils.d("answer  = " + answer   + ",location" + location.toString() + "answer is right = " + right + ",midX = " + midX);
            params.setMargins((int)(midX*ratioWidth),(int)(midY*ratioHeight)  ,0,0);
            child.setLayoutParams(params);
            // 增加矩形框
            /*Drawable drawable = llResult.getBackground();
            Bitmap bitmap = BitmapUtils.getRectangleBitmap(drawable,location);
            llResult.setBackground(new BitmapDrawable(getResources(),bitmap));*/
            llResult.addView(child,params);
        }
    }
}