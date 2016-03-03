package com.silambarasan.smartstack.profile.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.silambarasan.smartstack.listeners.EndlessRecyclerOnScrollListener;
import com.silambarasan.smartstack.profile.StackProfile;
import com.silambarasan.smartstack.profile.adapter.MyBadgesAdapter;
import com.silambarasan.smartstack.profile.adapter.MyTagsAdapter;
import com.silambarasan.smartstack.profile.model.MyBadge;
import com.silambarasan.smartstack.profile.model.MyTag;

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
public class MyTags extends Fragment {


    @Bind(R.id.my_tags)
    RecyclerView mRecyclerView;
    @Bind(R.id.err_result_tag)
    TextView err_result_tag;
    @Bind(R.id.mytags_progressBar)
    ProgressBar mytags_progressBar;

    private String TAG = MyTags.class.getSimpleName();
    private int PAGE_COUNT = 1, PAGE_SIZE = 10;
    private boolean isReviewSwipped = false;
    private android.support.v7.widget.LinearLayoutManager mLayoutManager;
    private List<MyTag> myTags;
    private MyTagsAdapter adapter;
    private PreferenceHelper preferenceHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_tags, container, false);
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
                        getTags();
                    }
                });
                mytags_progressBar.setVisibility(View.GONE);
                err_result_tag.setVisibility(View.GONE);
                getTags();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    //me/questions?order=desc&sort=activity&site=stackoverflow
    private void getTags() {
        mytags_progressBar.setVisibility(View.VISIBLE);
        err_result_tag.setVisibility(View.GONE);
        String url = Constants.API +
                "me/tags?" +
                "page=" + PAGE_COUNT +
                "&pagesize=" + PAGE_SIZE +
                "&order=desc&sort=popular&site=stackoverflow" +
                "&access_token=" + preferenceHelper.get("access_token") +
                "&key=" + Constants.CLIENT_KEY;

        Log.e(TAG, "getAnswers(): " + url);
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.e(TAG, "onResponse(): " + response.toString());
                        mytags_progressBar.setVisibility(View.GONE);
                        bindResponse(response);
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mytags_progressBar.setVisibility(View.GONE);
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
                            err_result_tag.setVisibility(View.GONE);
                            err_result_tag.setText("No Results");
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

                    myTags = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject object = items.getJSONObject(i);

                        MyTag myTag = new MyTag();

                        myTag.setName(object.getString("name"));
                        myTag.setCount(object.getString("count"));

                        myTags.add(myTag);

                    }
                    err_result_tag.setVisibility(View.GONE);
                    if (!isReviewSwipped) {

                        adapter = new MyTagsAdapter(
                                StackProfile.get(getActivity()),
                                myTags
                        );
                        mRecyclerView.setAdapter(adapter);

                    } else {
                        updateFeed(myTags);
                    }

                } else {
                    err_result_tag.setVisibility(View.VISIBLE);
                    isReviewSwipped = false;
                    err_result_tag.setText("You have not set Tags");
                    PAGE_COUNT = 1;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void updateFeed(List<MyTag> myTags) {
        try {
            if (myTags.size() > 0) {
                for (MyTag tag : myTags) {

                    if (null != adapter) {
                        adapter.add(tag);
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
