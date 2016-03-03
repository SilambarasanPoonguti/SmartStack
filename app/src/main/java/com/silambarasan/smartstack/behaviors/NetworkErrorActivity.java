package com.silambarasan.smartstack.behaviors;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.silambarasan.smartstack.R;
import com.silambarasan.smartstack.authentication.Authenticate;
import com.silambarasan.smartstack.constants.ConnectionHelper;

import java.util.ArrayList;
import java.util.List;

import silambarasan.com.smartstacklib.widgets.error.ProgressActivity;

public class NetworkErrorActivity extends AppCompatActivity {

    private String TAG = NetworkErrorActivity.class.getCanonicalName();
    static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private boolean isPaused;
    Toolbar activityToolbar;
    ProgressActivity progressActivity;

    private View.OnClickListener errorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkConnectivity();
        }
    };

    private void checkConnectivity() {
        if (ConnectionHelper.isConnectedOrConnecting(getApplicationContext())) {
            hideErrorsBar(true);
        } else {
            hideErrorsBar(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_error);

        activityToolbar = (Toolbar) findViewById(R.id.activityToolbar);
        progressActivity = (ProgressActivity) findViewById(R.id.progress);

        setToolbar();

        IntentFilter filter = new IntentFilter(CONNECTIVITY_CHANGE_ACTION);
        this.registerReceiver(mChangeConnectionReceiver, filter);

        Drawable emptyDrawable = new IconicsDrawable(this,
                GoogleMaterial.Icon.gmd_shopping_basket)
                .colorRes(android.R.color.white);
        Drawable errorDrawable = new IconicsDrawable(this,
                GoogleMaterial.Icon.gmd_signal_wifi_off)
                .colorRes(android.R.color.white);


        //Add which views you don't want to hide. In this case don't hide the toolbar
        List<Integer> skipIds = new ArrayList<>();
        skipIds.add(R.id.activityToolbar);

        String state = getIntent().getStringExtra("STATE");
        switch (state) {
            case "LOADING":
                progressActivity.showLoading(skipIds);
                setTitle("Loading");
                break;
            case "EMPTY":
                progressActivity.showEmpty(emptyDrawable,
                        "Empty Shopping Cart",
                        "Please add things in the cart to continue.", skipIds);
                setTitle("Empty");
                break;
            case "ERROR":
                progressActivity.showError(errorDrawable,
                        "No Connection",
                        "We could not establish a connection with our servers. Please try again when you are connected to the internet.",
                        "Try Again", errorClickListener, skipIds);
                setTitle("Error");
                break;
        }
    }

    private void setToolbar() {
        setSupportActionBar(activityToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle("Progress Activity");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void hideErrorsBar(boolean hide) {
        try {

            if (hide) {
//                Intent intent = new Intent(getApplicationContext(),
//                        Authenticate.class);
//                intent.putExtra("STATE", "CONNECTED");
//                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
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
//        hideErrorsBar(true);
        checkConnectivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChangeConnectionReceiver != null) {
            this.unregisterReceiver(mChangeConnectionReceiver);
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

}
