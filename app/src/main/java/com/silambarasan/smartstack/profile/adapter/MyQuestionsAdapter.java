package com.silambarasan.smartstack.profile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.silambarasan.smartstack.R;
import com.silambarasan.smartstack.profile.model.MyQuestion;
import com.silambarasan.smartstack.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import silambarasan.com.smartstacklib.widgets.StackTags;

/**
 * Created by Admin on 02-03-2016.
 */
public class MyQuestionsAdapter extends RecyclerView.Adapter<MyQuestionsAdapter.MyQuestionHolder> {


    private Context context;
    private List<MyQuestion> questions;

    public MyQuestionsAdapter(Context context, List<MyQuestion> questions) {
        this.context = context;
        this.questions = questions;
    }

    @Override
    public MyQuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_my_questions, parent, false);
        return new MyQuestionHolder(view);
    }

    @Override
    public void onBindViewHolder(MyQuestionHolder holder, int position) {

        MyQuestion question = questions.get(position);

        holder.score.setText(question.getScore() + " Votes");
        holder.answer_count.setText(question.getAnswer_count() + " Answers");

        holder.title.setText(Html.fromHtml(question.getTitle()));

        holder.date.setText(question.getCreation_date());

        if (question.getTags() != null && question.getTags().length > 0) {
            holder.tags_layout.setVisibility(View.VISIBLE);
            String[] tags = question.getTags();
            holder.tags_layout.setTags(tags);
        } else {
            holder.tags_layout.setVisibility(View.INVISIBLE);
        }

        holder.tags_layout.setOnTagClickListener(new StackTags.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                Toast.makeText(context, tag, Toast.LENGTH_SHORT).show();
            }
        });

        if (question.getView_count() != null) {
            holder.view_count.setVisibility(View.VISIBLE);
            String views = "";
            int length = question.getView_count().length();
            if (length < 2) {
                views = "" + length + " views";
            } else if (length > 3) {
                views = "" + question.getView_count().substring(0,1) + "k views";
            } else if (length > 4) {
                views = "" + question.getView_count().substring(0, 2) + "k views";
            }
            holder.view_count.setText(views);
        } else {
            holder.view_count.setVisibility(View.INVISIBLE);
        }

    }

    public void add(MyQuestion items) {
        try {
            questions.add(items);
//        this.notifyDataSetChanged();
            this.notifyItemInserted(questions.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return questions.size();
    }

    class MyQuestionHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.score)
        TextView score;
        @Bind(R.id.answer_count)
        TextView answer_count;
        @Bind(R.id.view_count)
        TextView view_count;
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.date)
        TextView date;

        @Bind(R.id.tags_layout)
        StackTags tags_layout;

        public MyQuestionHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


}
