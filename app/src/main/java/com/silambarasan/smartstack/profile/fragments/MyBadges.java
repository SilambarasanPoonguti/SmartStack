package com.silambarasan.smartstack.profile.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.silambarasan.smartstack.R;
import com.silambarasan.smartstack.constants.Constants;
import com.silambarasan.smartstack.helper.AppInstance;
import com.silambarasan.smartstack.helper.PreferenceHelper;
import com.silambarasan.smartstack.home.Home;
import com.silambarasan.smartstack.listeners.EndlessRecyclerOnScrollListener;
import com.silambarasan.smartstack.profile.StackProfile;
import com.silambarasan.smartstack.profile.adapter.MyBadgesAdapter;
import com.silambarasan.smartstack.profile.adapter.MyQuestionsAdapter;
import com.silambarasan.smartstack.profile.model.MyBadge;
import com.silambarasan.smartstack.profile.model.MyQuestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.FlipInRightYAnimator;

/**
 * Created by poonguti on 02-03-2016.
 */
public class MyBadges extends Fragment {

    @Bind(R.id.my_badges)
    RecyclerView mRecyclerView;
    @Bind(R.id.err_result_badge)
    TextView err_result_badge;
    @Bind(R.id.mybadges_progressBar)
    ProgressBar mybadges_progressBar;

    private String TAG = MyBadges.class.getSimpleName();
    private int PAGE_COUNT = 1, PAGE_SIZE = 10;
    private boolean isReviewSwipped = false;
    private List<MyBadge> myBadges;
    private MyBadgesAdapter adapter;
    private PreferenceHelper preferenceHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_badges, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initComponents();
    }

    private void initComponents() {
        try {
            if (isAdded()) {
                preferenceHelper = new PreferenceHelper(StackProfile.get(getActivity()));
                mRecyclerView.setHasFixedSize(true);
                final LinearLayoutManager layoutManager =
                        new LinearLayoutManager(StackProfile.get(getActivity()));
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setItemAnimator(new FlipInRightYAnimator());
                mRecyclerView.getItemAnimator().setAddDuration(300);
                mRecyclerView.getItemAnimator().setRemoveDuration(300);

                mRecyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int current_page) {
                        isReviewSwipped = true;
                        PAGE_COUNT += 1;
                        getBadges();
                    }
                });

                mybadges_progressBar.setVisibility(View.GONE);
                err_result_badge.setVisibility(View.GONE);
                getBadges();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    //me/questions?order=desc&sort=activity&site=stackoverflow
    private void getBadges() {

        mybadges_progressBar.setVisibility(View.VISIBLE);
        err_result_badge.setVisibility(View.GONE);
        String url = Constants.API +
                "me/badges?" +
                "page=" + PAGE_COUNT +
                "&pagesize=" + PAGE_SIZE +
                "&order=desc&sort=rank&site=stackoverflow" +
                "&access_token=" + preferenceHelper.get("access_token") +
                "&key=" + Constants.CLIENT_KEY;

        Log.e(TAG, "getAnswers(): " + url);
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.e(TAG, "onResponse(): " + response.toString());
                        mybadges_progressBar.setVisibility(View.GONE);
                        bindResponse(response);
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        mybadges_progressBar.setVisibility(View.GONE);

                        if (error instanceof TimeoutError) {
                            showToast("Connection timeout. Please try again");
                        } else if (error instanceof ServerError
                                || error instanceof AuthFailureError) {
                            showToast("Unable to connect server. Please try later");
                        } else if (error instanceof NetworkError
                                || error instanceof NoConnectionError) {
                            showToast("Network is unreachable");
                        } else {
                            showToast("No Results");
                            err_result_badge.setVisibility(View.GONE);
//                            err_result_badge.setText("No Results");
                        }
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(3 * 1000, 5, 1.0f));
        AppInstance.getInstance().addToRequestQueue(request, TAG);
    }

    private void bindResponse(JSONObject response) {
        try {
            if (null != response) {

                JSONArray items = response.getJSONArray("items");
                if (items.length() > 0) {

                    myBadges = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject object = items.getJSONObject(i);

                        MyBadge myBadge = new MyBadge();

                        myBadge.setBadge(object.getString("name"));
                        myBadge.setBadgeCount(object.getString("award_count"));
                        myBadge.setRank(object.getString("rank"));

                        myBadges.add(myBadge);

                    }

                    if (!isReviewSwipped) {

                        adapter = new MyBadgesAdapter(
                                StackProfile.get(getActivity()),
                                myBadges
                        );
                        mRecyclerView.setAdapter(adapter);

                    } else {
                        updateFeed(myBadges);
                    }

                } else {
                    err_result_badge.setVisibility(View.VISIBLE);
                    isReviewSwipped = false;
//                    err_result_badge.setText("You have not earn badges");
                    PAGE_COUNT = 1;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void updateFeed(List<MyBadge> myBadges) {
        try {
            if (myBadges.size() > 0) {
                for (MyBadge badge : myBadges) {

                    if (null != adapter) {
                        adapter.add(badge);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String content) {
        try {
            if (isAdded()) {
                StackProfile.get(getActivity()).showToast(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
