package com.silambarasan.smartstack.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.silambarasan.smartstack.R;
import com.silambarasan.smartstack.behaviors.NetworkErrorActivity;
import com.silambarasan.smartstack.constants.ConnectionHelper;
import com.silambarasan.smartstack.constants.Constants;
import com.silambarasan.smartstack.helper.PreferenceHelper;
import com.silambarasan.smartstack.profile.StackProfile;
import com.thefinestartist.finestwebview.FinestWebView;
import com.thefinestartist.finestwebview.listeners.WebViewListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;

public class Authenticate extends AppCompatActivity {


    private String TAG = Authenticate.class.getCanonicalName();

    private PreferenceHelper preferenceHelper;
    @Bind(R.id.authenticate_container)
    RelativeLayout authenticate_container;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.btn_login)
    FancyButton btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);
        ButterKnife.bind(this);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(ContextCompat.getColor(Authenticate.this, R.color.colorPrimaryDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initToolBar();
        init();
    }

    private void initToolBar() {
        try {
            mToolbar.setTitle(Html.fromHtml(getResources().getString(R.string.app_name)));
            setSupportActionBar(mToolbar);
            assert getSupportActionBar() != null;
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolbar.setNavigationIcon(ContextCompat.getDrawable(Authenticate.this,
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

        preferenceHelper = new PreferenceHelper(this);

        if (preferenceHelper.get("access_token").equalsIgnoreCase("")) {
            String url = Constants.URL + "oauth/dialog?client_id="
                    + Constants.CLIENT_ID
                    + "&scope=no_expiry&redirect_uri=https://stackexchange.com/oauth/login_success";
            getAuthenticate(url);
        } else {
            Intent intent = new Intent(Authenticate.this, StackProfile.class);
            intent.putExtra("access_token", Constants.ACCESS_TOKEN);
            startActivity(intent);
            finish();
        }
        btn_login.setIconResource(R.drawable.ic_person_outline_white_24dp);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Constants.URL + "oauth/dialog?client_id="
                        + Constants.CLIENT_ID
                        + "&scope=no_expiry&redirect_uri=https://stackexchange.com/oauth/login_success";
                getAuthenticate(url);
            }
        });
    }

    private void getAuthenticate(String url) {

        new FinestWebView.Builder(this)
                .setWebViewListener(webViewListener)
                .theme(R.style.FinestWebViewTheme)
                .titleDefault("SmartStack Login")
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
            if (url.startsWith("https://stackexchange.com/oauth/login_success#access_token")) {
                if (url.contains("#")) {
                    String[] urls = url.split("#");
                    String access_token = urls[1];
                    String[] tokens = access_token.split("=");
                    Constants.ACCESS_TOKEN = tokens[1];
                    preferenceHelper.set("access_token", tokens[1]);
                    navigate();

                }
            }
        }

    };

    private void navigate() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] startingLocation = new int[2];
                authenticate_container.getLocationOnScreen(startingLocation);
                startingLocation[0] += authenticate_container.getWidth() / 2;
                StackProfile.startUserProfileFromLocation(startingLocation,
                        Authenticate.this);
                finish();
                overridePendingTransition(0, 0);
            }
        }, 200);
    }


}
