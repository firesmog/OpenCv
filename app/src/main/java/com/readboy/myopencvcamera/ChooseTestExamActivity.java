package com.readboy.myopencvcamera;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.readboy.adapter.TestExamAdapter;
import com.readboy.bean.newexam.ChooseTestBean;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.PSource;

public class ChooseTestExamActivity extends BaseActivity {
    RecyclerView recyclerView;
    private List<ChooseTestBean> list = new ArrayList<>();
    private int[] source = {R.mipmap.ic_chinese,R.mipmap.ic_math,R.mipmap.ic_english,R.mipmap.ic_geography,R.mipmap.ic_chemistry,R.mipmap.ic_bios};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_test_exam);
        recyclerView = findViewById(R.id.rv_recyclerView);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        initFruits();
        TestExamAdapter adapter = new TestExamAdapter(list,this);
        recyclerView.setAdapter(adapter);
    }

    private void initFruits() {
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