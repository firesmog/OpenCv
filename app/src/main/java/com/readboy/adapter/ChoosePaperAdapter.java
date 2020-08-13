package com.readboy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.readboy.bean.newexam.ChooseTestBean;
import com.readboy.myopencvcamera.R;

import java.util.List;

import io.reactivex.annotations.NonNull;

public class ChoosePaperAdapter extends RecyclerView.Adapter<ChoosePaperAdapter.ViewHolder> {
    private List<ChooseTestBean> mTestList;
    private Context context;
    private OnItemClick onitemClick;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View testView;
        TextView testName;
        ImageView ivSelected;
        ImageView ivIcon;


        public ViewHolder(View view) {
            super(view);
            testView = view.findViewById(R.id.ll_text);
            testName = view.findViewById(R.id.tv_choose_test);
            ivSelected = view.findViewById(R.id.iv_choose_sign);
            ivIcon = view.findViewById(R.id.iv_icon);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_paper, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ChooseTestBean bean = mTestList.get(position);
        holder.testName.setText(bean.getTestName());
        holder.ivIcon.setBackground(context.getResources().getDrawable(bean.getTestIconId()));
        if(bean.isSelected()){
            holder.ivSelected.setVisibility(View.VISIBLE);
        }else {
            holder.ivSelected.setVisibility(View.GONE);
        }

        holder.testView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ChooseTestBean chooseTestBean : mTestList) {
                    chooseTestBean.setSelected(false);
                }
                bean.setSelected(true);
                holder.ivSelected.setVisibility(View.VISIBLE);

                if(onitemClick != null){
                    onitemClick.onItemClick(position);
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTestList.size();
    }

    public ChoosePaperAdapter(List<ChooseTestBean> testList, Context context) {
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