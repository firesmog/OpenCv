package com.readboy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.readboy.bean.newexam.AnalysisBean;
import com.readboy.log.LogUtils;
import com.readboy.myopencvcamera.R;
import com.readboy.util.GsonUtil;

import java.util.List;


import io.reactivex.annotations.NonNull;

public class AnalysisAdapter extends RecyclerView.Adapter<AnalysisAdapter.ViewHolder> {
    private List<AnalysisBean> mTestList;
    private Context context;
    private OnItemClick onitemClick;
    private String baseUrl;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAnswer;
        TextView tvAnalysis;
        View testView;
        TextView tvQuestion;


        public ViewHolder(View view) {
            super(view);
            testView = view.findViewById(R.id.ll_paper_test);
            tvQuestion = view.findViewById(R.id.tv_question_real);
            tvAnswer = view.findViewById(R.id.tv_real_answer);
            tvAnalysis = view.findViewById(R.id.tv_real_analysis);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_analysis, parent, false);
        return new ViewHolder(view);
    }



    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final AnalysisBean bean = mTestList.get(position);
       /* PaperQuestion question = bean.getPaperQuestion();


        String content = question.getContent();
        String content1 = TransferUtils.transfer(content);
        String content2 = GsonUtil.delHTMLTag(question.getSolution());
        LogUtils.d("question content before=  " + content + "\\n" +",after =" + content1 + "/n"
        + ", solution  before = "+ question.getSolution()  + "/n" + ", after =  " + content2  + "/n"
        );

        if (question.getPhase() == 2) {
            baseUrl = PaperConstant.PAPER_HIGH_SCHOOL_URL;
        } else {
            baseUrl = PaperConstant.PAPER_MIDDLE_SCHOOL_URL;
        }*/
        //Parser.parse(holder.tvQuestion, TransferUtils.transfer(content), Parser.ParserData.TYPE_PAPER, baseUrl);
       // Parser.parse(holder.tvAnswer, content2, Parser.ParserData.TYPE_PAPER, baseUrl);
        //Parser.parse(holder.tvAnalysis, content2, Parser.ParserData.TYPE_PAPER, baseUrl);
        /*holder.tvAnswer.setText(parserString(content2));
        holder.tvAnalysis.setText(parserString(content2));*/




        holder.testView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (AnalysisBean guideBean : mTestList) {
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
        for (AnalysisBean guideBean : mTestList) {
            guideBean.setSelect(false);
        }
        mTestList.get(pos).setSelect(true);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTestList.size();
    }

    public AnalysisAdapter(List<AnalysisBean> testList, Context context) {
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