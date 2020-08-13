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
                .inflate(R.layout.item_choose_paper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ChooseTestBean bean = mTestList.get(position);
        holder.chapterName.setText(bean.getTestName());


        holder.testView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ChooseTestBean chooseTestBean : mTestList) {
                    chooseTestBean.setSelected(false);
                }
                bean.setSelected(true);

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