package com.readboy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.readboy.bean.newexam.AnalysisBean;
import com.readboy.bean.newexam.ExamBean;
import com.readboy.log.LogUtils;
import com.readboy.myopencvcamera.R;
import com.readboy.util.DeviceUtil;
import com.readboy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;


import cn.dream.exerciseanalysis.common.PaperConstant;
import cn.dream.exerciseanalysis.entity.PaperQuestion;
import cn.dream.exerciseanalysis.parser.Parser;
import cn.dream.exerciseanalysis.util.TransferUtils;
import cn.dream.exerciseanalysis.widget.ExerciseTextView;
import io.reactivex.annotations.NonNull;

public class AnalysisAdapter extends RecyclerView.Adapter<AnalysisAdapter.ViewHolder>  {
    private List<AnalysisBean> mTestList;
    private Context context;
    private OnItemClick onitemClick;
    private String baseUrl;
    private ExamBean examBean;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ExerciseTextView tvAnswer;
        ExerciseTextView tvAnalysis;
        LinearLayout llView;
        ExerciseTextView tvRealQuestion;
        TextView tvQuestion;


        public ViewHolder(View view) {
            super(view);
            llView = view.findViewById(R.id.ll_paper);
            tvRealQuestion = view.findViewById(R.id.tv_question_real);
            tvQuestion = view.findViewById(R.id.tv_question);
            tvAnswer = view.findViewById(R.id.tv_real_answer);
            tvAnalysis = view.findViewById(R.id.tv_real_analysis);
            tvAnswer.setForbidClick(true);
            tvRealQuestion.setForbidClick(true);
            tvAnalysis.setForbidClick(true);

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
        PaperQuestion question = bean.getPaperQuestion();
        dealUrl(question);
        dealshowAnalysis(holder,question);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d("TAG bind click = true position111111 = " + position);
                if(null != onitemClick){
                    onitemClick.onItemClick(position);
                }
            }
        };
        holder.llView.setOnClickListener(listener);
        /*holder.tvRealQuestion.setOnClickListener(listener);
        holder.tvAnswer.setOnClickListener(listener);
        holder.tvAnalysis.setOnClickListener(listener);*/

    }





    private void dealUrl(PaperQuestion question){
        if (question.getPhase() == 2) {
            baseUrl = PaperConstant.PAPER_HIGH_SCHOOL_URL;
        } else {
            baseUrl = PaperConstant.PAPER_MIDDLE_SCHOOL_URL;
        }
    }



    private void dealshowAnalysis(ViewHolder holder,PaperQuestion question){
        String answer = dealAnswerAccordType(question);
        StringBuilder builder1 = new StringBuilder();
       // builder1.append("【").append(DeviceUtil.getQuestionAccordType(examBean,question.getType())).append("】");
        //holder.tvQuestion.setText(builder1.toString());

        switch (question.getType()) {
            case 99999:
                Parser.parse(holder.tvRealQuestion, TransferUtils.transfer("单词听写"), Parser.ParserData.TYPE_PAPER, baseUrl);
                Parser.parse(holder.tvAnswer, TransferUtils.transfer("暂无"), Parser.ParserData.TYPE_PAPER, baseUrl);
                Parser.parse(holder.tvAnalysis, TransferUtils.transfer("暂无"), Parser.ParserData.TYPE_PAPER, baseUrl);
                break;
            case 10001:
                String content = question.getContent();
                StringBuilder builder = new StringBuilder(content);
                List<Object> object = question.getOptions();
                if(null != object && object.size() > 0 ){
                    for (int i = 0; i < object.size(); i++) {
                        Object o = object.get(i);
                        if(i == 0){
                            builder.append(StringUtil.numberToLetter(i + 1)).append("、").append(String.valueOf(o));
                        }else {
                            builder.append("<br>").append(StringUtil.numberToLetter(i + 1)).append("、").append(String.valueOf(o));
                        }
                    }
                }
                Parser.parse(holder.tvRealQuestion, TransferUtils.transfer(builder.toString()), Parser.ParserData.TYPE_PAPER, baseUrl);
                Parser.parse(holder.tvAnswer, TransferUtils.transfer(answer), Parser.ParserData.TYPE_PAPER, baseUrl);
                Parser.parse(holder.tvAnalysis, TransferUtils.transfer(dealSolution(question.getSolution())), Parser.ParserData.TYPE_PAPER, baseUrl);
                break;
            case 10005:
            case 10006:
                Parser.parse(holder.tvRealQuestion, TransferUtils.transfer(question.getContent()), Parser.ParserData.TYPE_PAPER, baseUrl);
                Parser.parse(holder.tvAnswer, TransferUtils.transfer(answer), Parser.ParserData.TYPE_PAPER, baseUrl);
                Parser.parse(holder.tvAnalysis, TransferUtils.transfer(dealSolution(question.getSolution())), Parser.ParserData.TYPE_PAPER, baseUrl);
                break;
        }
    }
    private String dealAnswerAccordType(PaperQuestion question){
        int type = question.getType();
        Object answer = question.getAnswer();
        String result = "";
        switch (type){
            //填空题答案是数组
            case 10005:
            case 10006:
                result = dealFillIn(answer);
                break;
            case 10001:
                result = dealChoose(answer);
        }
        return result;
    }

    private String dealChoose(Object answer){
        String s = String.valueOf(answer);
        int i = Integer.parseInt(s);
        return StringUtil.numberToLetter(i + 1);
    }

    private String dealFillIn(Object answer){
        StringBuilder builder = new StringBuilder();
        ArrayList<String> list = (ArrayList<String>) answer;
        for (String s : list) {
            builder.append(s).append("&nbsp;&nbsp;");
        }

        return builder.toString();
    }

    private String dealSolution(String solution){
        solution  =  solution.replaceFirst("【解答】","");
        return solution.replace("【分析】","分析：");
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

    public AnalysisAdapter(List<AnalysisBean> testList, Context context,ExamBean examBean) {
        mTestList = testList;
        this.examBean = examBean;
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