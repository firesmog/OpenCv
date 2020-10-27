package com.readboy.util;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;

public class AnimatorUtil {
    private static ObjectAnimator animator;

    public static void startUpAndDownAnimator(View view){
        animator = ObjectAnimator.ofFloat(view, "translationY", 0.0f , -800f, 800f , 0f);
        animator.setDuration(1500);//动画时间
        animator.setInterpolator(new LinearInterpolator());//实现反复移动的效果
        animator.setRepeatCount(-1);//设置动画重复次数
        animator.start();//启动动
    }

    public static void endUpAndDownAnimator(){
        if(null == animator || !animator.isRunning()){
            return;
        }
        animator.end();
    }
}
