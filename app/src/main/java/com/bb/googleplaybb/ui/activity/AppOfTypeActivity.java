package com.bb.googleplaybb.ui.activity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.AppInfo;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.manager.ThreadManager;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.net.protocol.BaseNetProtocol;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.ProgressArc;
import com.bb.googleplaybb.utils.BitmapHelper;
import com.bb.googleplaybb.utils.StringUtils;
import com.bb.googleplaybb.utils.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Boby on 2019/4/23.
 */

public class AppOfTypeActivity extends AppCompatActivity {

    private SwipeRefreshLayout mRefreshLayout;
    private String mKey;
    private BaseNetProtocol<ArrayList<AppInfo>> mProtocol;
    private ArrayList<AppInfo> mData;
    private RecyclerView mRecyclerView;
    private boolean hasMore;


    public static void startAppOfTypeActivity(Context context, String key) {
        Intent intent = new Intent(context, AppOfTypeActivity.class);
        intent.putExtra("key", key);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_of_app);
        mKey = getIntent().getStringExtra("key");
        initProtocol(mKey);
        initView();
    }

    public void initView() {
        mRefreshLayout = findViewById(R.id.vRefreshLayout);
        final LoadingPage loadingPage = new LoadingPage(UIUtils.getContext()) {
            @Override
            protected ResultState onLoad() {
                ResultState resultState = AppOfTypeActivity.this.loadData();
                return resultState;
            }

            @Override
            public View onCreateSuccessView() {
                return AppOfTypeActivity.this.onCreateSuccessView();
            }
        };
        mRefreshLayout.addView(loadingPage);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        loadingPage.loadData();
    }

    public void initProtocol(final String key) {
        mProtocol = new BaseNetProtocol<ArrayList<AppInfo>>() {
            @Override
            public ArrayList<AppInfo> parseData(String json) {
                try {
                    if (!StringUtils.isEmpty(json)) {
                        JSONArray ja = new JSONArray(json);

                        if (ja != null) {
                            ArrayList<AppInfo> appInfos = new ArrayList<>();
                            for (int i = 0; i < ja.length(); i++) {
                                AppInfo appInfo = new AppInfo();
                                JSONObject appInfoObject = ja.getJSONObject(i);
                                appInfo.id = appInfoObject.getString("id");
                                appInfo.name = appInfoObject.getString("name");
                                appInfo.packageName = appInfoObject.getString("packageName");
                                appInfo.iconUrl = appInfoObject.getString("iconUrl");
                                appInfo.stars = (float) appInfoObject.getDouble("stars");
                                appInfo.size = appInfoObject.getLong("size");
                                appInfo.downloadUrl = appInfoObject.getString("downloadUrl");
                                appInfo.des = appInfoObject.getString("des");

                                appInfos.add(appInfo);
                            }
                            return appInfos;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public String getParams() {
                return "";
            }

            @Override
            public String getKey() {
                return "app/" + key;
            }

            @Override
            public String getCacheName() {
                return key;
            }
        };
    }

    public LoadingPage.ResultState loadData() {
        mData = mProtocol.getData(0);
        if (mData != null && mData.size() >= 20) {
            hasMore = true;
        }
        return check(mData);
    }

    public void loadMore() {
        if (hasMore) {
            ThreadManager.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<AppInfo> moreData = mProtocol.getData(mData.size());
                    final int oldSize = mData.size();
                    hasMore = moreData.size() >= 20;
                    if (moreData.size() > 0) {
                        mData.addAll(moreData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.getAdapter().notifyItemRangeChanged(oldSize, moreData.size());
                            }
                        });
                    }
                }
            });
        }
    }

    public View onCreateSuccessView() {
        mRecyclerView = new RecyclerView(UIUtils.getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(UIUtils.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new TypeAdapter());
        return mRecyclerView;
    }

    public void refresh() {
        ThreadManager.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                mData = mProtocol.getData(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        mRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    public LoadingPage.ResultState check(ArrayList data) {
        if (data != null) {
            if (data.size() > 0) {
                return LoadingPage.ResultState.RESULT_SUCCESS;
            } else {
                return LoadingPage.ResultState.RESULT_EMPTY;
            }
        }
        return LoadingPage.ResultState.RESULT_ERROR;
    }

    public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeHolder> {
        @Override
        public TypeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.list_item_home, null);
            TypeHolder typeHolder = new TypeHolder(view);
            return typeHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull TypeHolder holder, int position) {
            AppInfo data = mData.get(position);
            bindAppHolder(holder, data);
            if (position == mData.size() - 1) {
                loadMore();
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void bindAppHolder(TypeHolder holder, final AppInfo data) {
            holder.setData(data);

            holder.tvName.setText(data.name);
            holder.tvSize.setText(Formatter.formatFileSize(UIUtils.getContext(), data.size));
            holder.tvDes.setText(data.des);
            holder.rbStart.setRating(data.stars);

            BitmapHelper.getBitmapUtils().display(holder.ivIcon, NetHelper.URL + data.iconUrl);

            int mCurrentState;
            float mProgress;
            DownloadInfo downloadInfo = AppDownloadManager.getDownloadManager().getDownloadInfo(data);
            if (downloadInfo == null) {
                downloadInfo = DownloadInfo.copy(data);
                File file = new File(downloadInfo.getFilePath());
                if (!file.exists()) {
                    mCurrentState = AppDownloadManager.STATE_UNDO;
                    mProgress = 0;
                } else if (file.length() == data.size) {
                    mCurrentState = AppDownloadManager.STATE_SUCCESS;
                    mProgress = 1;
                } else {
                    mCurrentState = AppDownloadManager.STATE_PAUSE;
                    mProgress = file.length() / (float) data.size;
                }
            } else {
                mCurrentState = downloadInfo.mCurrentState;
                mProgress = downloadInfo.getProgress();
            }

            holder.refreshUI(mCurrentState, mProgress, data.id);//第一次手动调用
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeDetailActivity.startHomeDetailActivity(UIUtils.getContext(), data.packageName, data.name);
                }
            });
        }

        public class TypeHolder extends RecyclerView.ViewHolder implements AppDownloadManager.DownloadObserver, View.OnClickListener {
            private TextView tvName, tvSize, tvDes;
            private ImageView ivIcon;
            private RatingBar rbStart;
            private FrameLayout flProgress;
            private TextView tvDownload;
            private final AppDownloadManager mDm;
            private float mProgress;
            private int mCurrentState;
            private final ProgressArc mProgressArc;
            private AppInfo data;

            public TypeHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tv_name);
                tvSize = view.findViewById(R.id.tv_size);
                tvDes = view.findViewById(R.id.tv_des);
                ivIcon = view.findViewById(R.id.iv_icon);
                rbStart = view.findViewById(R.id.rb_start);
                flProgress = view.findViewById(R.id.fl_progress);
                tvDownload = view.findViewById(R.id.tv_download);
                flProgress.setOnClickListener(this);

                mProgressArc = new ProgressArc(UIUtils.getContext());
                //设置进度条直径
                mProgressArc.setArcDiameter(UIUtils.dip2px(31));
                //设置进度条颜色
                mProgressArc.setProgressColor(R.color.progress);
                mProgressArc.setBackgroundResource(R.drawable.ic_download);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

                //将进度条添加到布局
                flProgress.addView(mProgressArc, params);

                mDm = AppDownloadManager.getDownloadManager();
                mDm.registeredObserver(this);
            }

            public void setData(AppInfo appInfo) {
                this.data = appInfo;
            }

            public void refreshUI(int state, float progress, String id) {
                //由于listview的重用机制，刷新之前要确保是同一个应用
//        if (!getData().id.equals(id)) {
//            return;
//        }
                System.out.println("refreshUI：   state:" + state + ";progress:" + progress);
                mCurrentState = state;
                mProgress = progress;
                switch (state) {
                    case AppDownloadManager.STATE_UNDO:
                        mProgressArc.setBackgroundResource(R.drawable.ic_download);
                        mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                        tvDownload.setText("下载");
                        break;
                    case AppDownloadManager.STATE_PAUSE:
                        mProgressArc.setBackgroundResource(R.drawable.ic_resume);
                        mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                        mProgressArc.setProgress(progress, false);
                        tvDownload.setText("暂停");
                        System.out.println("暂停..........");
                        break;
                    case AppDownloadManager.STATE_WAITING:
                        mProgressArc.setBackgroundResource(R.drawable.ic_download);
                        mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_WAITING);
                        tvDownload.setText("等待中");
                        break;
                    case AppDownloadManager.STATE_DOWNLOADING:
                        mProgressArc.setBackgroundResource(R.drawable.ic_pause);
                        mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_DOWNLOADING);
                        mProgressArc.setProgress(progress, false);
                        tvDownload.setText((int) (progress * 100) + "%");
                        break;
                    case AppDownloadManager.STATE_SUCCESS:
                        mProgressArc.setBackgroundResource(R.drawable.ic_install);
                        mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                        tvDownload.setText("安装");
                        break;
                    case AppDownloadManager.STATE_ERROR:
                        mProgressArc.setBackgroundResource(R.drawable.ic_redownload);
                        mProgressArc.setStyle(ProgressArc.PROGRESS_STYLE_NO_PROGRESS);
                        tvDownload.setText("下载失败");
                        break;
                }
            }

            public void refreshUIonUIThread(final DownloadInfo downloadInfo) {
                if (data != null && data.id.equals(downloadInfo.id)) {
                    UIUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int state = downloadInfo.mCurrentState;
                            float progress = downloadInfo.getProgress();
                            refreshUI(state, progress, downloadInfo.id);
                        }
                    });
                }
            }

            //在主线程和子线程中都有调用
            @Override
            public void notifyDownloadStateChange(DownloadInfo downloadInfo) {
                refreshUIonUIThread(downloadInfo);
            }

            //在子线程中调用
            @Override
            public void notifyDownloadProgressChange(DownloadInfo downloadInfo) {
                if (downloadInfo.getProgress() != mProgress) {
                    refreshUIonUIThread(downloadInfo);
                }
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fl_progress:
                        if (mCurrentState == AppDownloadManager.STATE_UNDO || mCurrentState == AppDownloadManager.STATE_PAUSE || mCurrentState == AppDownloadManager.STATE_ERROR) {
                            mDm.download(data);
                        } else if (mCurrentState == AppDownloadManager.STATE_DOWNLOADING || mCurrentState == AppDownloadManager.STATE_WAITING) {
                            mDm.pause(data);
                        } else if (mCurrentState == AppDownloadManager.STATE_SUCCESS) {
                            mDm.install(data);
                        }
                        break;
                }
            }
        }
    }
}

