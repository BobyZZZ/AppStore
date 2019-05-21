package com.bb.googleplaybb.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.DownloadInfo;
import com.bb.googleplaybb.manager.AppDownloadManager;
import com.bb.googleplaybb.net.NetHelper;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import static com.bb.googleplaybb.ui.activity.MainActivity.RESULT_DELETE;


public class DownloadingManagerActivity extends AppCompatActivity implements AppDownloadManager.DownloadObserver, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private AppDownloadManager mDownloadManager;
    private ArrayList<DownloadInfo> mDownloadInfos;
    private DownloadingAdapter mAdapter;
    private View mEmptyView;
    private TextView mStartAndPause;
    private boolean mStart;
    private View mDeleteLayout;
    private View mTvDelete;
    private boolean showCheckBox;
    private CheckBox mSelectAll;
    private View mCancel;
    private int mDeleteCount;
    public static final String DELETE = "delete";


    public static void startActivityForResult(Activity context, int requestCode) {
        Intent intent = new Intent(context, DownloadingManagerActivity.class);
        context.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloading);
        mDownloadManager = AppDownloadManager.getDownloadManager();
        initData();
        initView();
    }

    @Override
    protected void onDestroy() {
        mDownloadManager.unregisteredObserver(this);
        super.onDestroy();
    }

    private void initData() {
        mDownloadInfos = mDownloadManager.getAllUnfinishedDownloadTask();
        mDownloadManager.registeredObserver(this);
    }

    public void initView() {
        mRecyclerView = findViewById(R.id.list);
        mEmptyView = findViewById(R.id.empty_layout);
        View back = findViewById(R.id.back);
        mStartAndPause = findViewById(R.id.start_and_pause);
        mDeleteLayout = findViewById(R.id.delete_layout);
        mTvDelete = findViewById(R.id.tv_delete);
        mSelectAll = findViewById(R.id.select_all);
        mCancel = findViewById(R.id.cancel);

//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager = new RecyclerViewNoBugLinearLayoutManager(this);
        mAdapter = new DownloadingAdapter();
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        shouldShowEmptyView();
        showRightText();

        mSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAllCheckState(isChecked);
                mAdapter.notifyDataSetChanged();
            }
        });

        back.setOnClickListener(this);
        mStartAndPause.setOnClickListener(this);
        mTvDelete.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    private void startOrPauseAllTask() {
        if (mStart) {
            //全部开始
            for (DownloadInfo info : mDownloadInfos) {
                mDownloadManager.download(info);
            }
            mStartAndPause.setText(R.string.pause_all);
        } else {
            //全部暂停
            for (DownloadInfo info : mDownloadInfos) {
                if (info.mCurrentState == AppDownloadManager.STATE_DOWNLOADING || info.mCurrentState == AppDownloadManager.STATE_WAITING) {
                    mDownloadManager.pause(info);
                }
            }
            mStartAndPause.setText(R.string.start_all);
        }
        mStart = !mStart;
    }

    public void shouldShowEmptyView() {
        mEmptyView.setVisibility(mDownloadInfos.size() == 0 ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(mDownloadInfos.size() == 0 ? View.GONE : View.VISIBLE);
        mStartAndPause.setVisibility(mDownloadInfos.size() == 0 ? View.GONE : View.VISIBLE);
    }

    private void showRightText() {
        for (DownloadInfo info : mDownloadInfos) {
            if (info.mCurrentState == AppDownloadManager.STATE_DOWNLOADING || info.mCurrentState == AppDownloadManager.STATE_WAITING) {
                //只要有一个正在下载或等待，即显示全部暂停
                mStart = false;
                mStartAndPause.setText(R.string.pause_all);
                return;
            }
        }
        mStart = true;
        mStartAndPause.setText(R.string.start_all);
    }

    private void showDeleteLayout(boolean showCheckBox) {
        mDeleteLayout.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);
    }

    @Override
    public void notifyDownloadStateChange(final DownloadInfo downloadInfo) {
        UIUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mDownloadInfos.size(); i++) {
                    DownloadInfo info = mDownloadInfos.get(i);
                    if (info.id.equals(downloadInfo.id)) {
                        final int index = i;
                        if (downloadInfo.mCurrentState == AppDownloadManager.STATE_SUCCESS) {
                            mDownloadInfos.remove(index);
                            //删除
                            shouldShowEmptyView();
                            mAdapter.notifyItemRangeChanged(index, mDownloadInfos.size() - index);
                        } else {
                            mDownloadInfos.set(index, downloadInfo);
                            mAdapter.notifyItemChanged(index, "stateChange");
                        }
                        showRightText();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void notifyDownloadProgressChange(final DownloadInfo downloadInfo) {
        UIUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("zzz", "zzznotifyDownloadProgressChange: " + downloadInfo.getProgress() * 100 + "%");
                for (int i = 0; i < mDownloadInfos.size(); i++) {
                    final DownloadInfo info = mDownloadInfos.get(i);
                    int v = (int) info.getProgress() * 100;
                    float v2 = downloadInfo.getProgress() * 100;
                    if (info.id.equals(downloadInfo.id) && v != v2) {
                        final int index = i;
                        mDownloadInfos.set(index, downloadInfo);
                        if (info.mCurrentState != AppDownloadManager.STATE_PAUSE) {
                            mAdapter.notifyItemChanged(index, "progressChange");
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        setAllCheckState(false);
        //返回，后刷新页面
        Intent intent = new Intent();
        intent.putExtra(DELETE,mDeleteCount);
        setResult(RESULT_DELETE,intent);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_and_pause:
                startOrPauseAllTask();
                break;
            case R.id.back:
                setAllCheckState(false);
                //返回，后刷新页面
                Intent intent = new Intent();
                intent.putExtra(DELETE,mDeleteCount);
                setResult(RESULT_DELETE,intent);
                finish();
                break;
            case R.id.tv_delete:
                //删除下载任务
                deleteCheckedTask();
                shouldShowEmptyView();

                //消除选中状态
                setAllCheckState(false);
                showCheckBox = false;
                showDeleteLayout(false);
                mStartAndPause.setEnabled(true);
                mSelectAll.setChecked(false);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.cancel:
                if (showCheckBox) {
                    setAllCheckState(false);
                    showCheckBox = false;
                    showDeleteLayout(false);
                    mStartAndPause.setEnabled(true);
                    mSelectAll.setChecked(false);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * 删除下载任务，同时删除本地文件
     */
    private void deleteCheckedTask() {
        ArrayList<DownloadInfo> toDelete = new ArrayList<>();
        for (DownloadInfo info : mDownloadInfos) {
            if (info.getChecked()) {
                Log.e("deleteCheckedTask", "deleteCheckedTask: " + info);
                toDelete.add(info);
                mDownloadManager.delete(info);
            }
        }
        mDownloadInfos.removeAll(toDelete);
        mDeleteCount = toDelete.size();
    }

    private void setAllCheckState(boolean checked) {
        for (DownloadInfo info : mDownloadInfos) {
            info.setChecked(checked);
        }
    }

    class DownloadingAdapter extends RecyclerView.Adapter<DownloadingAdapter.VH> {
        public String TAG = "DownloadingAdapter";

        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_downloading, parent, false);
            VH vh = new VH(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else if (payloads.get(0).equals("stateChange")) {
                DownloadInfo downloadInfo = mDownloadInfos.get(position);
                if (downloadInfo.mCurrentState == AppDownloadManager.STATE_DOWNLOADING) {
                    holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_downloading));
                    holder.mTip.setText((int) (downloadInfo.getProgress() * 100) + "%");
                } else if (downloadInfo.mCurrentState == AppDownloadManager.STATE_PAUSE) {
                    holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_paused));
                    holder.mTip.setText("已暂停");
                } else if (downloadInfo.mCurrentState == AppDownloadManager.STATE_WAITING) {
                    holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_downloading));
                    holder.mTip.setText("等待中");
                } else if (downloadInfo.mCurrentState == AppDownloadManager.STATE_ERROR) {
                    holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_paused));
                    holder.mTip.setText("下载失败，请重试");
                } else {
                    holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_downloading));
                    holder.mTip.setText("下载成功");
                }
                Log.e(TAG, "onBindViewHolder: stateChange" + downloadInfo.mCurrentState);
            } else if (payloads.get(0).equals("progressChange")) {
                DownloadInfo info = mDownloadInfos.get(position);
                holder.mProgressBar.setProgress((int) (info.getProgress() * 100));
                holder.mTip.setText((int) (info.getProgress() * 100) + "%");
                Log.e(TAG, "onBindViewHolder: progressChange" + (int) (info.getProgress() * 100) + "%" + "---" + info.mCurrentState);
            } else if (payloads.get(0).equals("showCheckBox")) {
                holder.mCheckBox.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final VH holder, final int position) {
            final DownloadInfo downloadInfo = mDownloadInfos.get(position);
            Log.e(TAG, "onBindViewHolder: " + position + "--"+downloadInfo);
            if (showCheckBox) {
                holder.mCheckBox.setVisibility(View.VISIBLE);
                holder.mCheckBox.setChecked(downloadInfo.getChecked());
                holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mDownloadInfos.get(holder.getAdapterPosition()).setChecked(isChecked);
                        Log.e(TAG, "onCheckedChanged: " + position + "--getAdapterPosition:" + holder.getAdapterPosition()+"--"+downloadInfo);
                    }
                });
            } else {
                holder.mCheckBox.setVisibility(View.GONE);
            }

            Glide.with(DownloadingManagerActivity.this).load(NetHelper.URL + downloadInfo.icon).into(holder.mIcon);
            holder.mName.setText(downloadInfo.name);
            holder.mProgressBar.setProgress((int) (downloadInfo.getProgress() * 100));
            if (downloadInfo.mCurrentState == AppDownloadManager.STATE_DOWNLOADING) {
                holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_downloading));
                holder.mTip.setText((int) (downloadInfo.getProgress() * 100) + "%");
            } else if (downloadInfo.mCurrentState == AppDownloadManager.STATE_PAUSE) {
                holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_paused));
                holder.mTip.setText("已暂停");
            } else if (downloadInfo.mCurrentState == AppDownloadManager.STATE_WAITING) {
                holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_downloading));
                holder.mTip.setText("等待中");
            } else if (downloadInfo.mCurrentState == AppDownloadManager.STATE_ERROR) {
                holder.mTip.setTextColor(getResources().getColor(R.color.text_tip_color_paused));
                holder.mTip.setText("下载失败，请重试");
            }
            holder.mDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeDetailActivity.startHomeDetailActivity(UIUtils.getContext(), downloadInfo.packageName, downloadInfo.name);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (showCheckBox) {
                        //显示checkbox时点击item操作checkbox
                        boolean checked = !holder.mCheckBox.isChecked();
                        holder.mCheckBox.setChecked(checked);
//                        downloadInfo.setChecked(checked);
                        modifySelectAllState();
                    } else {
                        //没显示checkbox时
                        if (downloadInfo.mCurrentState == AppDownloadManager.STATE_UNDO || downloadInfo.mCurrentState == AppDownloadManager.STATE_PAUSE || downloadInfo.mCurrentState == AppDownloadManager.STATE_ERROR) {
                            mDownloadManager.download(downloadInfo);
                        } else if (downloadInfo.mCurrentState == AppDownloadManager.STATE_DOWNLOADING || downloadInfo.mCurrentState == AppDownloadManager.STATE_WAITING) {
                            mDownloadManager.pause(downloadInfo);
                        }
                        showRightText();
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!showCheckBox) {
                        showCheckBox = true;
                        //全部开始或全部暂停按钮disable
                        mStartAndPause.setEnabled(false);
                        downloadInfo.setChecked(true);
                        mAdapter.notifyDataSetChanged();
                        showDeleteLayout(true);

                        //全部选中时修改全选为勾选状态
                        modifySelectAllState();
                    }
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDownloadInfos.size();
        }

        class VH extends RecyclerView.ViewHolder {
            public ImageView mIcon;
            public TextView mName, mTip, mDetail;
            public ProgressBar mProgressBar;
            public CheckBox mCheckBox;

            public VH(View itemView) {
                super(itemView);
                mIcon = itemView.findViewById(R.id.iv_icon);
                mName = itemView.findViewById(R.id.tv_name);
                mTip = itemView.findViewById(R.id.textTip);
                mDetail = itemView.findViewById(R.id.tv_app_detail);
                mProgressBar = itemView.findViewById(R.id.progress);
                mCheckBox = itemView.findViewById(R.id.checkbox);
            }
        }

    }

    private void modifySelectAllState() {
        int count = 0;
        for (DownloadInfo info : mDownloadInfos) {
            if (info.getChecked()) {
                count++;
            }
        }
        if (count == mDownloadInfos.size()) {
            mSelectAll.setChecked(true);
        } else {
            mSelectAll.setOnCheckedChangeListener(null);
            mSelectAll.setChecked(false);
            mSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setAllCheckState(isChecked);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public class RecyclerViewNoBugLinearLayoutManager extends LinearLayoutManager {
        public RecyclerViewNoBugLinearLayoutManager(Context context) {
            super(context);
        }

        public RecyclerViewNoBugLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public RecyclerViewNoBugLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                //try catch一下
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }
    }
}
