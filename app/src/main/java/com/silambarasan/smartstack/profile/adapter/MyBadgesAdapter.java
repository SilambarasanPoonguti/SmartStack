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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by poonguti on 02-03-2016.
 */
public class MyBadgesAdapter extends RecyclerView.Adapter<MyBadgesAdapter.BadgeHolder> {

    private Context context;
    private List<MyBadge> myBadges;

    public MyBadgesAdapter(Context context, List<MyBadge> myBadges) {
        this.context = context;
        this.myBadges = myBadges;
    }

    @Override
    public BadgeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_my_badges, parent, false);
        return new BadgeHolder(view);
    }

    @Override
    public void onBindViewHolder(BadgeHolder holder, int position) {

        MyBadge badge = myBadges.get(position);

        if(badge.getRank().equalsIgnoreCase("silver")){
            holder.badge.setImageResource(R.drawable.btn_badge_silver);
        }else if(badge.getRank().equalsIgnoreCase("gold")){
            holder.badge.setImageResource(R.drawable.btn_badge_gold);
        }else  if(badge.getRank().equalsIgnoreCase("bronze")){
            holder.badge.setImageResource(R.drawable.btn_badge_bronze);
        }
        holder.badge_title.setText(badge.getBadge());
        holder.badge_count.setText("( "+badge.getBadgeCount()+" )");

    }

    public void add(MyBadge items) {
        try {
            myBadges.add(items);
//        this.notifyDataSetChanged();
            this.notifyItemInserted(myBadges.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public int getItemCount() {
        return myBadges.size();
    }

    class BadgeHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.badge)
        ImageView badge;
        @Bind(R.id.badge_title)
        TextView badge_title;
        @Bind(R.id.badge_count)
        TextView badge_count;

        public BadgeHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
