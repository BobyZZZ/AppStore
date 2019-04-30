package com.bb.googleplaybb.ui.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.bb.googleplaybb.manager.ThreadManager;
import com.bb.googleplaybb.ui.adapter.holder.BaseHolder;
import com.bb.googleplaybb.ui.adapter.holder.MoreHolder;
import com.bb.googleplaybb.utils.UIUtils;
import java.util.ArrayList;

public abstract class MyBaseAdapter<T> extends BaseAdapter {

    private int TYPE_NORMAL = 1;
    private int TYPE_MORE = 0;
    private ArrayList<T> data;

    public MyBaseAdapter(ArrayList<T> data) {
        this.data = data;
        hasMore = data.size() >= 20;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getCount() - 1) {
            return TYPE_MORE;
        }
        return getInnerType(position);
    }

    @Override
    public int getCount() {
        return data.size() + 1;
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);//TYPE_MORE表示已到底，到底则加载更多数据

        BaseHolder holder;
        if (convertView == null) {
            //获取holder对象
            //加载布局,获取控件
            //convertView = inflateConvertView(holder);
            //打标记
            //convertView.setTag(holder);
            if (type == TYPE_MORE) {
                holder = new MoreHolder(hasMore());
            } else {
                holder = getHolder(position);
            }
        } else {
            holder = (BaseHolder) convertView.getTag();
        }

        //只有正常条目才需要设置数据，加载更多条目在构造方法内调用setdata
        if (type == TYPE_MORE) {
            MoreHolder moreHolder = (MoreHolder) holder;
            if (moreHolder.getData() == moreHolder.MORE_MORE) {
                //最后一个条目展示出来就加载更多数据
                loadMore(moreHolder);
            }
        } else {
            //设置数据
            holder.setData(getItem(position));
        }

        return holder.getmRootView();
    }

    private boolean isLoadMore;//是否正在加载

    private void loadMore(final MoreHolder moreHolder) {
        if (!isLoadMore) {
            isLoadMore = true;
            ThreadManager.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<T> moreData = onLoadMore();

                    UIUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (moreData != null) {
                                if (moreData.size() < 20) {
                                    moreHolder.setData(moreHolder.MORE_NONE);
                                } else {
                                    moreHolder.setData(moreHolder.MORE_MORE);
                                }
                                //将数据追加到集合
                                data.addAll(moreData);
                                MyBaseAdapter.this.notifyDataSetChanged();
                            } else {
                                moreHolder.setData(moreHolder.MORE_ERROR);
                            }
                        }
                    });
                }
            });
            isLoadMore = false;
        }

    }

    private boolean hasMore = true;

    /**
     * @return true表示有更多数据，默认有更多数据
     */
    public boolean hasMore() {
        return hasMore;
    }

    protected abstract ArrayList<T> onLoadMore();

    public int getInnerType(int position) {
        return TYPE_NORMAL;
    }

    protected abstract BaseHolder getHolder(int position);
}
