package com.bb.googleplaybb.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.mvp.IUserView;
import com.bb.googleplaybb.mvp.UserPresenter;
import com.bb.googleplaybb.utils.FileUtils;
import com.bb.googleplaybb.utils.LoginUtils;
import com.bb.googleplaybb.utils.SharePreferenceUtils;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by Boby on 2019/5/16.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, IUserView {

    private UserPresenter mPresenter;
    private EditText vId;
    private EditText vPwd;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private TextView mIdTips, mPwdTips, mUserNameTips;

    private boolean idIsOk, pwdIsOk, nameIsOk;
    private boolean toLogin = true;
    private View mRegisterBtns;
    private View mLoginBtns;
    private ImageView mIcon;
    private Button mBtnBackToLogin;
    private Button mBtnConfirm;
    private EditText mEtUserName;
    private View mRegisterLayout;

    private String photoPath;
    private int REQUEST_CODE_PICK_PHOTO = 0;

    public static void startForResult(Activity context, int requestCode) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPresenter = new UserPresenter(this);

        initView();
        initEvent();
    }

    private void initView() {
        vId = findViewById(R.id.et_id);
        vPwd = findViewById(R.id.et_pwd);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnLogin = findViewById(R.id.btn_login);
        mIdTips = findViewById(R.id.tv_id_tips);
        mPwdTips = findViewById(R.id.tv_pwd_tips);
        mUserNameTips = findViewById(R.id.tv_user_name_tips);

        mRegisterLayout = findViewById(R.id.register_layout);
        mRegisterBtns = findViewById(R.id.layout_register_btn);
        mLoginBtns = findViewById(R.id.layout_login_btn);
        mIcon = findViewById(R.id.iv_icon);
        mBtnBackToLogin = findViewById(R.id.btn_back_to_login);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mEtUserName = findViewById(R.id.et_user_name);
    }

    private void initEvent() {
        mBtnRegister.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);
        mBtnBackToLogin.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
        mIcon.setOnClickListener(this);

        vId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (TextUtils.isEmpty(text)) {
                    mIdTips.setVisibility(View.VISIBLE);
                    mIdTips.setText("帐号不能为空");
                    idIsOk = false;
                } else {
                    //查找数据库，用户名是否已存在
                    if (!toLogin && LoginUtils.getInstance().isPhoneExist(text)) {
                        mIdTips.setVisibility(View.VISIBLE);
                        mIdTips.setText("帐号已存在");
                        idIsOk = false;
                    } else if (isPhoneNumber(text)) {
                        mIdTips.setVisibility(View.GONE);
                        idIsOk = true;
                    } else {
                        mIdTips.setVisibility(View.VISIBLE);
                        mIdTips.setText("请输入正确手机号码");
                        idIsOk = false;
                    }
                }
                setBtnEnable();
            }
        });

        vPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String pwd = s.toString();
                if (pwd.length() < 6) {
                    mPwdTips.setVisibility(View.VISIBLE);
                    mPwdTips.setText("密码长度至少6位");
                    pwdIsOk = false;
                } else if (pwd.length() > 10) {
                    mPwdTips.setVisibility(View.VISIBLE);
                    mPwdTips.setText("密码长度至多10位");
                    pwdIsOk = false;
                } else {
                    mPwdTips.setVisibility(View.GONE);
                    pwdIsOk = true;
                }
                setBtnEnable();
            }
        });

        mEtUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (TextUtils.isEmpty(text)) {
                    mUserNameTips.setVisibility(View.VISIBLE);
                    mUserNameTips.setText("用户名不能为空");
                    nameIsOk = false;
                } else {
                    //查找数据库，用户名是否已存在
                    if (LoginUtils.getInstance().isUserNameExist(text)) {
                        mUserNameTips.setVisibility(View.VISIBLE);
                        mUserNameTips.setText("用户名已存在");
                        nameIsOk = false;
                    } else {
                        mUserNameTips.setVisibility(View.GONE);
                        nameIsOk = true;
                    }
                }
                setBtnEnable();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                switchToLogin(false);
                break;
            case R.id.btn_login:
                //登录
                login();
                break;
            case R.id.btn_back_to_login:
                //返回登录，隐藏用户名，头像
                switchToLogin(true);
                break;
            case R.id.btn_confirm:
                //注册
                boolean registerResult = mPresenter.register();
                if (registerResult) {
                    //登录
                    Toast.makeText(UIUtils.getContext(), "注册成功", Toast.LENGTH_LONG).show();
                    login();
                }
                break;
            case R.id.iv_icon:
                //选择头像
                Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intentToPickPic, REQUEST_CODE_PICK_PHOTO);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                Uri uri = data.getData();
                photoPath = FileUtils.getFilePathByUri(this, uri);

                if (!TextUtils.isEmpty(photoPath)) {
                    RequestOptions requestOptions1 = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).circleCrop();
                    //将照片显示在 ivImage上
                    Glide.with(this).load(photoPath).apply(requestOptions1).into(mIcon);
                }
                break;
        }
    }

    private void switchToLogin(boolean toLogin) {
        if (toLogin) {
            mRegisterLayout.setVisibility(View.GONE);
            mLoginBtns.setVisibility(View.VISIBLE);
            mRegisterBtns.setVisibility(View.GONE);

            if (mIdTips.getText().toString().equals("帐号已存在")) {
                mIdTips.setVisibility(View.GONE);
            }
        } else {
            mRegisterLayout.setVisibility(View.VISIBLE);
            mLoginBtns.setVisibility(View.GONE);
            mRegisterBtns.setVisibility(View.VISIBLE);

            if (LoginUtils.getInstance().isPhoneExist(getId())) {
                mIdTips.setVisibility(View.VISIBLE);
                mIdTips.setText("帐号已存在");
            }
        }
        this.toLogin = toLogin;
    }

    private void setBtnEnable() {
        mBtnLogin.setEnabled(idIsOk && pwdIsOk);
        mBtnConfirm.setEnabled(idIsOk && pwdIsOk && nameIsOk);
    }

    private void login() {
        boolean result = mPresenter.login();
        if (result) {
            SharePreferenceUtils.setUser(this, getId(), getPwd());
            Intent intent = new Intent();
            intent.putExtra("user_id", getId());
            intent.putExtra("user_pwd", getPwd());
            setResult(MainActivity.RESULT_LOGIN, intent);
            finish();
        } else {
            //id或密码错误提示
            Toast.makeText(UIUtils.getContext(), "id或密码错误", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String getId() {
        return vId.getText().toString();
    }

    @Override
    public String getPwd() {
        return vPwd.getText().toString();
    }

    @Override
    public String getUserName() {
        return mEtUserName.getText().toString();
    }

    @Override
    public String getPhotoPath() {
        return photoPath;
    }

    public boolean isPhoneNumber(String number) {
        if (number == null || number.length() != 11) {
            return false;
        }
        String regex = "^1[3|4|5|7|8][0-9]\\d{4,8}$";
        return number.matches(regex);
    }

    private boolean isPwdOk(String pwd) {
        return !TextUtils.isEmpty(pwd) && pwd.length() >= 6 && pwd.length() <= 10;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            EditText editText = (EditText) v;
            String text = editText.getText().toString();
            switch (v.getId()) {
                case R.id.et_user_name:
                    if (TextUtils.isEmpty(text)) {
                        mUserNameTips.setVisibility(View.VISIBLE);
                        mUserNameTips.setText("用户名不能为空");
                        nameIsOk = false;
                    } else {
                        //查找数据库，用户名是否已存在
                        if (LoginUtils.getInstance().isUserNameExist(text)) {
                            mUserNameTips.setVisibility(View.VISIBLE);
                            mUserNameTips.setText("用户名已存在");
                            nameIsOk = false;
                        } else {
                            mUserNameTips.setVisibility(View.GONE);
                            nameIsOk = true;
                        }
                    }
                    break;
                case R.id.et_id:

                    break;
                case R.id.et_pwd:
                    if (isPwdOk(text)) {
                        mPwdTips.setVisibility(View.GONE);
                        pwdIsOk = true;
                    } else {
                        mPwdTips.setVisibility(View.VISIBLE);
                        pwdIsOk = false;
                    }
                    break;
            }
            setBtnEnable();
        }
    }
}
