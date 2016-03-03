package com.silambarasan.smartstack.home;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.silambarasan.smartstack.R;
import com.silambarasan.smartstack.home.adapter.ViewPagerAdapter;
import com.silambarasan.smartstack.home.fragments.Featured;
import com.silambarasan.smartstack.home.fragments.Hot;
import com.silambarasan.smartstack.home.fragments.Interesting;
import com.silambarasan.smartstack.home.fragments.Month;
import com.silambarasan.smartstack.home.fragments.Week;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Home extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tabs_home)
    TabLayout tabs_home;
    @Bind(R.id.viewpager_home)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(ContextCompat.getColor(Home.this, R.color.colorPrimaryDark));
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
            mToolbar.setNavigationIcon(ContextCompat.getDrawable(Home.this,
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
        setupViewPager(viewPager);
        tabs_home.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter =
                new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new Interesting(), "INTERESTING");
        adapter.addFrag(new Featured(), "FEATURED");
        adapter.addFrag(new Hot(), "HOT");
        adapter.addFrag(new Week(), "WEEK");
        adapter.addFrag(new Month(), "MONTH");

        viewPager.setAdapter(adapter);
    }

    public static Home get(Activity activity){return (Home)activity;}

    public void showToast(String content) {
        try {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        content,
                        Snackbar
                                .LENGTH_SHORT);
                View view = snackbar.getView();
                view.setBackgroundColor(ContextCompat.getColor(Home.this,
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
