package com.bb.googleplaybb.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppLiked;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.LoginUtils;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Boby on 2019/5/16.
 */

public class LikedActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private ArrayList<AppLiked> mDatas;
    private View mMask;
    private View mTvUnliked;
    private View mBack;

    private AppLiked toUnlikedId;
    private LoginUtils mUtils;
    private LikedAdapter mAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, LikedActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked);
        mUtils = LoginUtils.getInstance();

        initView();
        initEvent();
        initData();
    }

    private void setStatusBarTranslate() {
        View actionBar = findViewById(R.id.action_bar);
        ViewGroup.LayoutParams lp = actionBar.getLayoutParams();
        int statusBarHeight = UIUtils.getStatusBarHeight();
        lp.height += UIUtils.getStatusBarHeight();
        actionBar.setPadding(0, statusBarHeight, 0, 0);
        UIUtils.setStatusBarTransparent(this);
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mMask = findViewById(R.id.mask);
        mTvUnliked = findViewById(R.id.tv_unliked);
        mBack = findViewById(R.id.back);

        mBack.post(new Runnable() {
            @Override
            public void run() {
                setStatusBarTranslate();
            }
        });
    }

    private void initEvent() {
        mTvUnliked.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mMask.setOnClickListener(this);
    }

    private void initData() {
        String user_id = MainActivity.currentUser.getUser_id();
        if (!TextUtils.isEmpty(user_id)) {
            mDatas = LoginUtils.getInstance().getAllLikedApp(user_id);
            if (mDatas != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                mAdapter = new LikedAdapter();
                mRecyclerView.setLayoutManager(layoutManager);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.tv_unliked:
                unLiked();
                break;
            default:
                mMask.setVisibility(View.GONE);
                break;
        }
    }

    private void unLiked() {
        if (toUnlikedId != null) {
            mMask.setVisibility(View.GONE);
            mUtils.deleteLiked(toUnlikedId.getUser_id(), toUnlikedId.getApp_id());

            int index = mDatas.indexOf(toUnlikedId);
            mDatas.remove(index);
            mAdapter.notifyItemRemoved(index);

            toUnlikedId = null;
        }
    }

    class LikedAdapter extends RecyclerView.Adapter<LikedAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_liked, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final VH holder, int position) {
            final AppLiked appLiked = mDatas.get(position);
            Glide.with(UIUtils.getContext()).load(NetHelper.URL + appLiked.getApp_icon()).into(holder.mIcon);
            holder.tvName.setText(appLiked.getApp_name());
            holder.tvDes.setText(appLiked.getApp_des());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LikedActivity.this, HomeDetailActivity.class);
                    intent.putExtra(HomeDetailActivity.PACKAGENAME, appLiked.getApp_package_name());
                    intent.putExtra(HomeDetailActivity.APPNAME, appLiked.getApp_name());

                    LikedActivity.this.startActivityForResult(intent, 0);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    toUnlikedId = mDatas.get(holder.getAdapterPosition());
                    mMask.setVisibility(View.VISIBLE);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView mIcon;
            TextView tvName, tvDes;

            public VH(View itemView) {
                super(itemView);
                mIcon = itemView.findViewById(R.id.iv_icon);
                tvName = itemView.findViewById(R.id.tv_name);
                tvDes = itemView.findViewById(R.id.tv_des);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initData();
    }
}
