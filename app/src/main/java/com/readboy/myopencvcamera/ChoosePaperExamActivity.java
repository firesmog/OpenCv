package com.readboy.myopencvcamera;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.readboy.adapter.ChooseCourseAdapter;
import com.readboy.adapter.ChoosePaperAdapter;
import com.readboy.bean.newexam.ChooseTestBean;

import java.util.ArrayList;
import java.util.List;

public class ChoosePaperExamActivity extends BaseActivity {
    RecyclerView rvCourse;
    RecyclerView rvPaper;
    private List<ChooseTestBean> list = new ArrayList<>();
    private int[] source = {R.mipmap.ic_chinese,R.mipmap.ic_math,R.mipmap.ic_english,R.mipmap.ic_geography,R.mipmap.ic_chemistry,R.mipmap.ic_bios};
    private String[] course = {"语文","数学","英语","地理","化学","生物","物理","政治","历史","Java"};
    private ChoosePaperAdapter paperAdapter;
    private ChooseCourseAdapter courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_test_exam);
        initCourseRecycleView();
        initPaperRecycleView();
    }

    private void initPaperRecycleView(){
        rvPaper = findViewById(R.id.rv_recyclerView_paper);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        rvPaper.setLayoutManager(layoutManager);
        initPaperData();
        paperAdapter = new ChoosePaperAdapter(list,this);
        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.custom_divider));
        rvPaper.addItemDecoration(divider);
        rvPaper.setAdapter(paperAdapter);
    }


    private void initCourseRecycleView(){
        rvCourse = findViewById(R.id.rv_recyclerView_course);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        rvCourse.setLayoutManager(layoutManager);
        initCourseData();
        courseAdapter = new ChooseCourseAdapter(list,this);
        rvCourse.setAdapter(courseAdapter);
    }

    private void initCourseData() {
        for(int i = 0; i < course.length; i++) {
            String text =course[i];
            if(i == 0 ){
                list.add(new ChooseTestBean(source[i],text,true));
            } else if (i < source.length){
                list.add(new ChooseTestBean(source[i],text,false));
            }else{
                list.add(new ChooseTestBean(source[5],text,false));
            }
        }
    }

    private void initPaperData() {
        for(int i = 0; i < 10; i++) {
            String text = "测试卷  " + i;
            if(i == 0 ){
                list.add(new ChooseTestBean(source[i],text,true));
            } else if (i < source.length){
                list.add(new ChooseTestBean(source[i],text,false));
            }else{
                list.add(new ChooseTestBean(source[5],text,false));
            }
        }
    }

}