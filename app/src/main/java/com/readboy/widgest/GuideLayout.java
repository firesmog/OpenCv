package com.readboy.widgest;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readboy.log.LogUtils;
import com.readboy.myopencvcamera.R;


/**
 * Created by hubert
 * <p>
 * Created on 2017/7/27.
 */
public class GuideLayout extends FrameLayout {

    public static final int DEFAULT_BACKGROUND_COLOR = 0x66000000;

    private Paint mPaint;
    private float downX;
    private float downY;
    private int touchSlop;
    private Context context;
    private RectF rectF;
    //笔记当前为第几题
    private int curQue;
    private OnGuideViewClickListener listener;


    public GuideLayout(Context context) {
        super(context);
        this.context = context;
        init();

    }

    public GuideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GuideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GuideLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mPaint.setXfermode(xfermode);

        //设置画笔遮罩滤镜,可以传入BlurMaskFilter或EmbossMaskFilter，前者为模糊遮罩滤镜而后者为浮雕遮罩滤镜
        //这个方法已经被标注为过时的方法了，如果你的应用启用了硬件加速，你是看不到任何阴影效果的
        mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.INNER));
        //关闭当前view的硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        //ViewGroup默认设定为true，会使onDraw方法不执行，如果复写了onDraw(Canvas)方法，需要清除此标记
        setWillNotDraw(false);

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }


    public void drawRectangle(Context context,RectF rect,int queType){
        this.context = context;
        this.rectF = rect;
        if(curQue == queType){
            return;
        }
        curQue = queType;
        invalidate();
    }


    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        LogUtils.d("onClickHighLightArea x = " + event.toString());
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                if(null != listener){
                    listener.onClickHighLightArea(downX,downY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float upX = event.getX();
                float upY = event.getY();
                break;

        }
        return super.onTouchEvent(event);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(DEFAULT_BACKGROUND_COLOR);
        if(null != rectF){
            mPaint.setColor(context.getResources().getColor(R.color.color_FF000000));
            canvas.drawRect(rectF,mPaint);
            mPaint.setColor(DEFAULT_BACKGROUND_COLOR);
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    public void setListener(OnGuideViewClickListener listener) {
        this.listener = listener;
    }

    public interface OnGuideViewClickListener{
        void onClickHighLightArea(float posX,float posY);
    }

}
