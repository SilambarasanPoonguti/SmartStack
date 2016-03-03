package com.silambarasan.smartstack.home.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
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
import com.silambarasan.smartstack.profile.adapter.MyQuestionsAdapter;
import com.silambarasan.smartstack.profile.model.MyQuestion;
import com.silambarasan.smartstack.utils.TimeAgo;
import com.silambarasan.smartstack.view.LoadingProgress;

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
public class Week extends Fragment {


    @Bind(R.id.week_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.err_result_week)
    TextView err_result_questions;
    @Bind(R.id.week_progressBar)
    ProgressBar week_progressBar;

    private String TAG = Week.class.getSimpleName();
    private int PAGE_COUNT = 1, PAGE_SIZE = 10;
    private int TOTAL_FEEDS = 0;
    private boolean isReviewSwipped = false;
    private LinearLayoutManager mLayoutManager;
    private List<MyQuestion> myQuestions;
    private MyQuestionsAdapter adapter;
    private PreferenceHelper preferenceHelper;
    private LoadingProgress loadingProgress;
    private Handler handlerDialog;
    private Runnable runnableDialog;

    public Week() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_week, container, false);
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
                preferenceHelper = new PreferenceHelper(Home.get(getActivity()));
                mRecyclerView.setHasFixedSize(true);
                final LinearLayoutManager layoutManager =
                        new LinearLayoutManager(Home.get(getActivity()));
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                recyclerView.setLayoutManager(layoutManager);

//                mLayoutManager = new LinearLayoutManager(getActivity());
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setItemAnimator(new FlipInRightYAnimator());
                mRecyclerView.getItemAnimator().setAddDuration(300);
                mRecyclerView.getItemAnimator().setRemoveDuration(300);

                mRecyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int current_page) {
                        isReviewSwipped = true;
                        PAGE_COUNT = current_page;
                        getQuestions();
                    }
                });
                week_progressBar.setVisibility(View.GONE);
                err_result_questions.setVisibility(View.GONE);
                getQuestions();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    //https://api.stackexchange.com/2.2/
    // questions?page=1&pagesize=10&order=desc&sort=week&site=stackoverflow
    private void getQuestions() {
        week_progressBar.setVisibility(View.VISIBLE);
        err_result_questions.setVisibility(View.GONE);
        String url = Constants.API +
                "questions?page=" + PAGE_COUNT +
                "&pagesize=" + PAGE_SIZE +
                "&order=desc&sort=week&site=stackoverflow";

        Log.e(TAG, "getAnswers(): " + url);
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.e(TAG, "onResponse(): " + response.toString());
                        week_progressBar.setVisibility(View.GONE);
                        bindResponse(response);
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        week_progressBar.setVisibility(View.GONE);

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
                            err_result_questions.setVisibility(View.GONE);
                            err_result_questions.setText("No Results");
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

                    myQuestions = new ArrayList<>();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject object = items.getJSONObject(i);

                        MyQuestion question = new MyQuestion();
                        JSONArray tags = object.getJSONArray("tags");
                        if (tags == null)
                            question.setTags(new String[0]);
                        else {
                            String[] arr = new String[tags.length()];
                            for (int t = 0; t < arr.length; t++) {
                                arr[t] = tags.optString(t);
                            }
                            question.setTags(arr);
                        }

                        question.setIs_answered(object.getBoolean("is_answered"));
                        question.setAnswer_count(object.getString("answer_count"));
                        question.setView_count(object.getString("view_count"));
                        question.setScore(object.getString("score"));
                        question.setCreation_date(getPostedByDate(object.getString("creation_date")).toString());
                        question.setTitle(object.getString("title"));
                        question.setLink(object.getString("link"));

                        myQuestions.add(question);

                    }

                    if (!isReviewSwipped) {

                        adapter = new MyQuestionsAdapter(
                                Home.get(getActivity()),
                                myQuestions
                        );
                        mRecyclerView.setAdapter(adapter);

                    } else {
                        updateFeed(myQuestions);
                    }

                } else {
                    isReviewSwipped = false;
                    err_result_questions.setVisibility(View.VISIBLE);
                    err_result_questions.setText("You have not asked any questions");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private CharSequence getPostedByDate(String datestring) {
        CharSequence dateSeq = "";
        try {
            long timestamp = Long.parseLong(datestring);
            dateSeq = TimeAgo.getTimeAgo(timestamp, Home.get(getActivity()));

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return dateSeq;
    }

    private void updateFeed(List<MyQuestion> myQuestions) {
        try {
            if (myQuestions.size() > 0) {
                for (MyQuestion question : myQuestions) {

                    if (null != adapter) {
                        adapter.add(question);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppInstance.getInstance().getRequestQueue().cancelAll(TAG);

    }


    private void showToast(String content) {
        try {
            if (isAdded()) {
                Home.get(getActivity()).showToast(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
