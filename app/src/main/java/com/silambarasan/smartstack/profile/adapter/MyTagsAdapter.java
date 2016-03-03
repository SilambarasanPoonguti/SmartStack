package com.silambarasan.smartstack.profile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.silambarasan.smartstack.R;
import com.silambarasan.smartstack.profile.model.MyBadge;
import com.silambarasan.smartstack.profile.model.MyQuestion;
import com.silambarasan.smartstack.profile.model.MyTag;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Admin on 02-03-2016.
 */
public class MyTagsAdapter extends RecyclerView.Adapter<MyTagsAdapter.TagHolder> {

    private Context context;
    private List<MyTag> myTags;

    public MyTagsAdapter(Context context, List<MyTag> myTags) {
        this.context = context;
        this.myTags = myTags;
    }

    @Override
    public TagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_my_tags,
                parent, false);
        return new TagHolder(view);
    }

    @Override
    public void onBindViewHolder(TagHolder holder, int position) {

        MyTag myTag = myTags.get(position);
        holder.tag_title.setText(myTag.getName());
        holder.tag_count.setText("( "+myTag.getCount()+" )");

    }

    public void add(MyTag items) {
        try {
            myTags.add(items);
//        this.notifyDataSetChanged();
            this.notifyItemInserted(myTags.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public int getItemCount() {
        return myTags.size();
    }

    class TagHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tag_title)
        TextView tag_title;
        @Bind(R.id.tag_count)
        TextView tag_count;

        public TagHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
