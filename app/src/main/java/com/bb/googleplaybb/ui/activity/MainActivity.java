package com.bb.googleplaybb.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.User;
import com.bb.googleplaybb.global.GooglePlayApplication;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.ui.fragment.BaseFragment;
import com.bb.googleplaybb.ui.fragment.HomeFragment;
import com.bb.googleplaybb.utils.FileUtils;
import com.bb.googleplaybb.utils.FragmentFactory;
import com.bb.googleplaybb.utils.LoginUtils;
import com.bb.googleplaybb.utils.SharePreferenceUtils;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.viewpagerindicator.TabPageIndicator;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabPageIndicator mIndicator;
    private MyAdapter mAdapter;
    private Toolbar mToolbar;
    private NavigationView mNavigatinView;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout mDrawer;
    private ImageView mTouxiang;
    private TextView mLogin;

    private int REQUEST_DELETE = 100;
    private int REQUEST_LOGIN = 101;

    public static final int RESULT_DELETE = 200;
    public static final int RESULT_LOGIN = 201;

    public static User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();

        initView();
        initToolBar();
        login();
    }

    private void login() {
        setUserManageVisible(false);
        User userFromSP = SharePreferenceUtils.getUser(this);
        if (userFromSP != null) {
            User result = LoginUtils.getInstance().findUser(userFromSP.getUser_id(), userFromSP.getUser_pwd());
            if (result != null) {
                loginSuccess(result);
            }
        }
    }

    private void setUserManageVisible(boolean visible) {
        Menu menu = mNavigatinView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.user_manage);
        if (menuItem != null) {
            menuItem.setVisible(visible);
        }
    }

    private void initPermission() {
        int checkSelfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void initToolBar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(mToolbar);

        mDrawer = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close);
        toggle.syncState();
        //mDrawer.setDrawerListener(toggle);
    }

    private void initView() {
        mViewPager = findViewById(R.id.vp_pager);
        mIndicator = findViewById(R.id.indicator);
        mNavigatinView = findViewById(R.id.navigation_view);
        View headerView = mNavigatinView.inflateHeaderView(R.layout.navigation_header);
        mTouxiang = headerView.findViewById(R.id.iv_touxiang);
        mLogin = headerView.findViewById(R.id.tv_login);

        if (currentUser == null) {
            mLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginActivity.startForResult(MainActivity.this, 0);
                }
            });
        } else {
            mLogin.setText(currentUser.getUser_id());
        }

        mNavigatinView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_modifyIP:
                        final Dialog dialog = new Dialog(MainActivity.this);
                        View view = dialog.getLayoutInflater().inflate(R.layout.dialog_modify_ip, null);
                        dialog.setContentView(view);
                        Button btn_modify = view.findViewById(R.id.btn_modify);
                        Button btn_cancel = view.findViewById(R.id.btn_cancel);
                        final EditText et_ip = view.findViewById(R.id.et_ip);
                        et_ip.setText(GooglePlayApplication.getIp());
                        btn_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        btn_modify.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String ip = et_ip.getText().toString();
                                NetHelper.URL = "http://" + ip + ":8080/WebInfos/";
                                GooglePlayApplication.putIp(ip);
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        break;
                    case R.id.navigation_downloading:
                        //打开正在下载页面
                        DownloadingManagerActivity.startActivityForResult(MainActivity.this, 0);
                        break;
                    case R.id.navigation_liked:
                        //打开正在下载页面
                        LikedActivity.start(MainActivity.this);
                        break;
                    case R.id.navigation_login_out:
                        logout();
                        setUserManageVisible(false);
                        break;
                }
                item.setChecked(true);
                mDrawer.closeDrawers();
                return true;
            }
        });

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mIndicator.setViewPager(mViewPager);
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                FragmentFactory.createFragment(position).loadData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(500);
                    FragmentFactory.createFragment(0).loadData();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    class MyAdapter extends FragmentPagerAdapter {

        private String[] tabNames;

        public MyAdapter(FragmentManager fm) {
            super(fm);
            tabNames = UIUtils.getStringArray(R.array.tab_names);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabNames[position];
        }

        @Override
        public BaseFragment getItem(int position) {
            System.out.println("getItem : " + position);
            BaseFragment fragment = FragmentFactory.createFragment(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return tabNames.length;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_DELETE:
                int deleteCount = data.getIntExtra(DownloadingManagerActivity.DELETE, 0);
                if (deleteCount > 0) {
//                    FragmentFactory.createFragment(mViewPager.getCurrentItem()).loadData();
                    BaseFragment fragment = FragmentFactory.createFragment(mViewPager.getCurrentItem());
                    fragment.refresh();
                }
                break;

            case RESULT_LOGIN:
                String user_id = data.getStringExtra("user_id");
                String user_pwd = data.getStringExtra("user_pwd");
                User user = LoginUtils.getInstance().findUser(user_id, user_pwd);
                if (user != null) {
                    loginSuccess(user);
                }
                break;
        }
    }

    private void loginSuccess(User user) {
        currentUser = user;
        mLogin.setText(user.getUser_name());
        mLogin.setOnClickListener(null);

        setUserManageVisible(true);

        String photoPath = user.getUser_photo_path();
        if (!TextUtils.isEmpty(photoPath)) {
            RequestOptions requestOptions1 = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).circleCrop();
            //将照片显示在 ivImage上
            Glide.with(this).load(photoPath).apply(requestOptions1).into(mTouxiang);
        }
    }

    private void logout() {
        if (currentUser != null) {
//            currentUser.setUser_id("");
//            currentUser.setUser_pwd("");
//            currentUser.setUser_name("");
            currentUser = null;
            SharePreferenceUtils.setUser(this, "", "");

            mLogin.setText(R.string.login);
            mLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginActivity.startForResult(MainActivity.this, 0);
                }
            });
            mTouxiang.setImageResource(R.drawable.select_picture);
        }
    }

    //    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//        //申请权限
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);
//    } else {
//        //无需申请
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            //用户同意了，可以去放肆了
//        } else {
//            //权限被用户拒绝了，洗洗睡吧
//        }
//    }

//    private void initActionBar() {
//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
////        actionBar.setDisplayShowHomeEnabled(true);
////        actionBar.setDisplayUseLogoEnabled(true);
////        actionBar.setLogo(R.drawable.ic_launcher);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer_am);
//
//        toggle = new ActionBarDrawerToggle(this, mDrawer, R.string.drawer_open, R.string.drawer_close);
//        toggle.syncState();//同步一下
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        getMenuInflater().inflate(R.menu.menu_main,menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                //弹出侧边栏
//                toggle.onOptionsItemSelected(item);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
