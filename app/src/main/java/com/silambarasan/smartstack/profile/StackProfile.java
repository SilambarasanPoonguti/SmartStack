package com.silambarasan.smartstack.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
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
import com.silambarasan.smartstack.authentication.Authenticate;
import com.silambarasan.smartstack.behaviors.NetworkErrorActivity;
import com.silambarasan.smartstack.constants.ConnectionHelper;
import com.silambarasan.smartstack.constants.Constants;
import com.silambarasan.smartstack.helper.AppInstance;
import com.silambarasan.smartstack.helper.PreferenceHelper;
import com.silambarasan.smartstack.home.Home;
import com.silambarasan.smartstack.home.adapter.ViewPagerAdapter;
import com.silambarasan.smartstack.profile.fragments.MyBadges;
import com.silambarasan.smartstack.profile.fragments.MyQuestions;
import com.silambarasan.smartstack.profile.fragments.MyTags;
import com.silambarasan.smartstack.utils.CircleTransformation;
import com.silambarasan.smartstack.view.RevealBackgroundView;
import com.squareup.picasso.Picasso;
import com.thefinestartist.finestwebview.FinestWebView;
import com.thefinestartist.finestwebview.listeners.WebViewListener;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StackProfile extends AppCompatActivity
        implements RevealBackgroundView.OnStateChangeListener {

    static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private boolean isPaused;
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";
    private String TAG = StackProfile.class.getCanonicalName();
    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;

    @Bind(R.id.tabs_profile)
    TabLayout tlUserProfileTabs;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.ssProfilePhoto)
    ImageView ssProfilePhoto;
    @Bind(R.id.vUserDetails)
    View vUserDetails;
    @Bind(R.id.btnReputation)
    Button btnReputation;
    @Bind(R.id.vUserStats)
    View vUserStats;
    @Bind(R.id.vUserProfileRoot)
    View vUserProfileRoot;

    @Bind(R.id.ssProfileName)
    TextView ssProfileName;
    @Bind(R.id.ssProfileID)
    TextView ssProfileID;
    @Bind(R.id.ssProfileWebsite)
    TextView ssProfileWebsite;
    @Bind(R.id.ssProfileGoldPoints)
    TextView ssProfileGoldPoints;
    @Bind(R.id.ssProfileSilverPoints)
    TextView ssProfileSilverPoints;
    @Bind(R.id.ssProfileBronzePoints)
    TextView ssProfileBronzePoints;
    @Bind(R.id.profile_progressBar)
    ProgressBar profile_progressBar;

    private int avatarSize;
    private PreferenceHelper preferenceHelper;

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, StackProfile.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack_profile);
        ButterKnife.bind(this);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(ContextCompat.getColor(StackProfile.this, R.color.colorPrimaryDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initToolBar();
        IntentFilter filter = new IntentFilter(CONNECTIVITY_CHANGE_ACTION);
        this.registerReceiver(mChangeConnectionReceiver, filter);
        init();
    }


    private void initToolBar() {
        try {
            mToolbar.setTitle(Html.fromHtml(getResources().getString(R.string.app_name)));
            setSupportActionBar(mToolbar);
            assert getSupportActionBar() != null;
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolbar.setNavigationIcon(ContextCompat.getDrawable(StackProfile.this,
                    R.drawable.ic_arrow_back_white_24dp));
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        } catch (Exception bug) {
            bug.printStackTrace();
        }
    }

    private void init() {
        preferenceHelper = new PreferenceHelper(StackProfile.this);
        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.user_profile_avatar_size);
        setupViewPager(viewPager);
        tlUserProfileTabs.setupWithViewPager(viewPager);
        setProfile();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_feed) {
            startActivity(new Intent(StackProfile.this, Home.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new MyQuestions(), "Questions");
        adapter.addFrag(new MyTags(), "Tags");
        adapter.addFrag(new MyBadges(), "Badges");
        viewPager.setAdapter(adapter);
    }


    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            tlUserProfileTabs.setVisibility(View.VISIBLE);
            vUserProfileRoot.setVisibility(View.VISIBLE);
            animateUserProfileOptions();
            animateUserProfileHeader();
        } else {
            tlUserProfileTabs.setVisibility(View.INVISIBLE);
            vUserProfileRoot.setVisibility(View.INVISIBLE);
        }
    }

    private void animateUserProfileOptions() {
        tlUserProfileTabs.setTranslationY(-tlUserProfileTabs.getHeight());
        tlUserProfileTabs.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
    }

    private void animateUserProfileHeader() {
        vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
        ssProfilePhoto.setTranslationY(-ssProfilePhoto.getHeight());
        vUserDetails.setTranslationY(-vUserDetails.getHeight());
        vUserStats.setAlpha(0);

        vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        ssProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
        vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
    }

    //https://api.stackexchange.com/2.2/me?order=desc&sort=reputation&site=stackoverflow
    private void setProfile() {

        String url = Constants.API +
                "me?order=desc&sort=reputation&site=stackoverflow"+
                "&access_token=" + preferenceHelper.get("access_token") +
                "&key=" + Constants.CLIENT_KEY;

        Log.e(TAG, "setProfile(): " + url);
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.e(TAG, "onResponse(): " + response.toString());
                        profile_progressBar.setVisibility(View.GONE);
                        bindResponse(response);
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        profile_progressBar.setVisibility(View.VISIBLE);
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
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject object = items.getJSONObject(i);
                        JSONObject badge_counts = new JSONObject(object.getString("badge_counts"));

                        ssProfileName.setText(object.getString("display_name"));
                        ssProfileID.setText("User ID: " + object.getString("user_id"));
                        ssProfileWebsite.setText(object.getString("website_url"));
                        btnReputation.setText("Reputation: " + object.getString("reputation"));
                        ssProfileGoldPoints.setText(badge_counts.getString("gold"));
                        ssProfileSilverPoints.setText(badge_counts.getString("silver"));
                        ssProfileBronzePoints.setText(badge_counts.getString("bronze"));

                        String profilePhoto = object.getString("profile_image");
                        Picasso.with(this)
                                .load(profilePhoto)
                                .placeholder(R.drawable.img_circle_placeholder)
                                .resize(avatarSize, avatarSize)
                                .centerCrop()
                                .transform(new CircleTransformation())
                                .into(ssProfilePhoto);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        String url = Constants.URL + "users/logout";
        new FinestWebView.Builder(this)
                .setWebViewListener(webViewListener)
                .theme(R.style.FinestWebViewTheme)
                .titleDefault("SMARTSTACK LOGOUT")
                .statusBarColorRes(R.color.colorPrimaryDark)
                .toolbarColorRes(R.color.colorPrimary)
                .titleColorRes(R.color.finestWhite)
                .urlColorRes(R.color.bluePrimary)
                .iconDefaultColorRes(R.color.finestWhite)
                .progressBarColorRes(R.color.colorAccent)
                .webViewBuiltInZoomControls(true)
                .webViewDisplayZoomControls(true)
                .swipeRefreshColorRes(R.color.colorPrimaryDark)
                .menuColorRes(R.color.colorPrimaryDark)
                .menuTextColorRes(R.color.finestWhite)
                .dividerHeight(0)
                .gradientDivider(false)
                .setCustomAnimations(R.anim.activity_open_enter, R.anim.activity_open_exit, R.anim.activity_close_enter, R.anim.activity_close_exit)
                .injectJavaScript("javascript: document.getElementById('msg').innerHTML='Hello " + "TheFinestArtist" + "!';")
                .disableIconClose(true)
                .show(url);

    }

    private WebViewListener webViewListener = new WebViewListener() {
        @Override
        public void onPageFinished(String url) {
            super.onPageFinished(url);

            Log.e(TAG, "onPageFinished()url: " + url);

            if (url.equalsIgnoreCase(Constants.URL)) {

                Constants.ACCESS_TOKEN = "";
                preferenceHelper.set("access_token", "");
                Intent intent = new Intent(StackProfile.this, Authenticate.class);
                startActivity(intent);
                finish();
            }
        }

    };


    public static StackProfile get(Activity activity) {
        return (StackProfile) activity;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        checkConnectivity();
    }

    private void checkConnectivity() {
        if (ConnectionHelper.isConnectedOrConnecting(getApplicationContext())) {
            hideErrorsBar(true);
        } else {
            hideErrorsBar(false);
        }
    }

    private final BroadcastReceiver mChangeConnectionReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (CONNECTIVITY_CHANGE_ACTION.equals(action) && !isPaused) {
                //check internet connection
                if (!ConnectionHelper.isConnectedOrConnecting(getApplicationContext())) {
                    if (context != null) {
                        boolean show = false;
                        if (ConnectionHelper.lastNoConnectionTs == -1) {//first time
                            show = true;
                            ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();
                        } else {
                            if (System.currentTimeMillis() - ConnectionHelper.lastNoConnectionTs > 1000) {
                                show = true;
                                ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();
                            }
                        }

                        if (show && ConnectionHelper.isOnline) {
                            hideErrorsBar(false);
                            ConnectionHelper.isOnline = false;
                        }
                    }
                } else {
                    hideErrorsBar(true);
                    ConnectionHelper.isOnline = true;
                }
            }
        }
    };

    public void hideErrorsBar(boolean hide) {
        try {

            if (!hide) {
                Intent intent = new Intent(getApplicationContext(),
                        NetworkErrorActivity.class);
                intent.putExtra("STATE", "ERROR");
                startActivityForResult(intent, 7);

            } else {
                init();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7) {
            if (data != null) {
                init();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChangeConnectionReceiver != null) {
            this.unregisterReceiver(mChangeConnectionReceiver);
        }
        AppInstance.getInstance().getRequestQueue().cancelAll(TAG);

    }

    public void showToast(String content) {
        try {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    content,
                    Snackbar
                            .LENGTH_SHORT);
            View view = snackbar.getView();
            view.setBackgroundColor(ContextCompat.getColor(StackProfile.this,
                    R.color.bluePrimary));
            TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            textView.setTextColor(getResources().getColor(R.color.white));
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
