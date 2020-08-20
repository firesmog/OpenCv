package com.readboy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.readboy.bean.newexam.ChooseTestBean;
import com.readboy.bean.newexam.GuideBean;
import com.readboy.myopencvcamera.R;

import java.util.List;

import io.reactivex.annotations.NonNull;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.ViewHolder> {
    private List<GuideBean> mTestList;
    private Context context;
    private OnItemClick onitemClick;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View testView;
        TextView chapterName;


        public ViewHolder(View view) {
            super(view);
            testView = view.findViewById(R.id.ll_paper_test);
            chapterName = view.findViewById(R.id.tv_paper_chapter);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_guide, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final GuideBean bean = mTestList.get(position);
        holder.chapterName.setText(String.valueOf(bean.getCurItem()));

        if(bean.isSelect()){
           holder.chapterName.setBackground(context.getResources().getDrawable(R.drawable.button_circle_shape_pressed));
           holder.chapterName.setTextColor(context.getResources().getColor(R.color.color_ffffff));
        }else {
            holder.chapterName.setBackground(context.getResources().getDrawable(R.drawable.button_circle_shape));
            holder.chapterName.setTextColor(context.getResources().getColor(R.color.color_0095ff));
        }


        holder.testView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (GuideBean guideBean : mTestList) {
                    guideBean.setSelect(false);
                }
                bean.setSelect(true);

                if(onitemClick != null){
                    onitemClick.onItemClick(position);
                }
                notifyDataSetChanged();
            }
        });
    }


    public void setItemSelect(int pos){
        for (GuideBean guideBean : mTestList) {
            guideBean.setSelect(false);
        }
        mTestList.get(pos).setSelect(true);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTestList.size();
    }

    public GuideAdapter(List<GuideBean> testList, Context context) {
        mTestList = testList;
        this.context = context;
    }

    //定义一个点击事件的接口
    public interface OnItemClick {
        void onItemClick(int position);
    }

    public void setOnItemClick(OnItemClick onitemClick) {
        this.onitemClick = onitemClick;
    }

}