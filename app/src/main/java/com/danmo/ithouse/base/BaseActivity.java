package com.danmo.ithouse.base;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.danmo.commonutil.leak.IMMLeaks;
import com.danmo.ithouse.R;
import com.danmo.ithouse.util.Config;

import java.io.Serializable;
import java.util.List;

/**
 * Created by user on 2017/9/12.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout rootLayout;
    protected FrameLayout flActivityContainer;
    protected ViewHolder mViewHolder;
    private Toast mToast;
    private Fragment mFragment;
    private NavigationView.OnNavigationItemSelectedListener mListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_git_hub:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.GITHUB)));
                    break;
                case R.id.action_blog:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.BLOG)));
                    break;
            }
            return false;
        }
    };

    public static void openActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewHolder = new ViewHolder(getLayoutInflater(), null, getLayoutId());
        setContentView(R.layout.activity_drawer);

        initDatas();
        initViews(mViewHolder, mViewHolder.getRootView());
        IMMLeaks.fixFocusedViewLeak(this.getApplication()); // 修复 InputMethodManager 引发的内存泄漏
    }

    @LayoutRes
    protected abstract int getLayoutId();

    /**
     * 初始化数据，调用位置在 initViews 之前
     */
    protected void initDatas() {
    }

    /**
     * 初始化 View， 调用位置在 initDatas 之后
     */
    protected void initViews(ViewHolder holder, View root) {
        flActivityContainer = (FrameLayout) findViewById(R.id.activity_container);
        flActivityContainer.addView(LayoutInflater.from(this).inflate(getLayoutId(), flActivityContainer, false));
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(mListener);

//        View fakeStatusBar = findViewById(R.id.fake_status_bar);
        rootLayout = (DrawerLayout) findViewById(R.id.root_layout);
//        BarUtils.setStatusBarAlpha4Drawer(this, rootLayout, fakeStatusBar, 0, false);
    }

    // 默认点击左上角是结束当前 Activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 发出一个短Toast
     *
     * @param text 内容
     */
    public void toastShort(String text) {
        toast(text, Toast.LENGTH_SHORT);
    }

    /**
     * 发出一个长toast提醒
     *
     * @param text 内容
     */
    public void toastLong(String text) {
        toast(text, Toast.LENGTH_LONG);
    }

    private void toast(final String text, final int duration) {
        if (!TextUtils.isEmpty(text)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mToast == null) {
                        mToast = Toast.makeText(getApplicationContext(), text, duration);
                    } else {
                        mToast.setText(text);
                        mToast.setDuration(duration);
                    }
                    mToast.show();
                }
            });
        }
    }

    protected void openActivity(Class<?> cls) {
        openActivity(this, cls);
    }

    /**
     * 打开 Activity 的同时传递一个数据
     */
    protected <V extends Serializable> void openActivity(Class<?> cls, String key, V value) {
        openActivity(this, cls, key, value);
    }

    /**
     * 打开 Activity 的同时传递一个数据
     */
    public <V extends Serializable> void openActivity(Context context, Class<?> cls, String key, V value) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(key, value);
        context.startActivity(intent);
    }


    @SuppressWarnings("RestrictedApi")
    protected void clearOldFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (transaction == null || fragments == null || fragments.size() == 0)
            return;
        boolean doCommit = false;
        for (Fragment fragmentOld : fragments) {
            if (fragmentOld != fragment && fragmentOld != null) {
                transaction.remove(fragmentOld);
                doCommit = true;
            }
        }
        if (doCommit)
            transaction.commitNow();
    }

    protected void addFragment(int frameLayoutId, Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (fragment.isAdded()) {
                if (mFragment != null) {
                    transaction.hide(mFragment).show(fragment);
                } else {
                    transaction.show(fragment);
                }
            } else {
                if (mFragment != null) {
                    transaction.hide(mFragment).add(frameLayoutId, fragment);
                } else {
                    transaction.add(frameLayoutId, fragment);
                }
            }
            mFragment = fragment;
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }
}
