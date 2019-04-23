package com.bb.googleplaybb.ui.fragment;

import android.view.View;

import com.bb.googleplaybb.domain.TypeInfo;
import com.bb.googleplaybb.net.protocol.TypeNetProtocol;
import com.bb.googleplaybb.ui.adapter.MyBaseAdapter;
import com.bb.googleplaybb.ui.adapter.holder.BaseHolder;
import com.bb.googleplaybb.ui.adapter.holder.TitleHolder;
import com.bb.googleplaybb.ui.adapter.holder.TypeHolder;
import com.bb.googleplaybb.ui.view.LoadingPage;
import com.bb.googleplaybb.ui.view.MyListView;
import com.bb.googleplaybb.utils.UIUtils;

import java.util.ArrayList;

/**
 * Created by Boby on 2018/7/10.
 */

public class TypeFragment extends BaseFragment {

    private ArrayList<TypeInfo> data;

    @Override
    public LoadingPage.ResultState onLoad() {
        TypeNetProtocol protocol = new TypeNetProtocol();
        data = protocol.getData(0);
        return check(data);
    }

    @Override
    public View onCreateSuccessView() {
        MyListView myListView = new MyListView(UIUtils.getContext());

        myListView.setAdapter(new TypeAdapter(data));
        return myListView;
    }

    class TypeAdapter extends MyBaseAdapter<TypeInfo> {
        public TypeAdapter(ArrayList<TypeInfo> data) {
            super(data);
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        @Override
        public int getInnerType(int position) {
            if (data.get(position).isTitle) {
                return super.getInnerType(position) + 1;
            }
            return super.getInnerType(position);
        }

        @Override
        protected ArrayList<TypeInfo> onLoadMore() {
            return null;
        }

        @Override
        protected BaseHolder getHolder(int position) {
            if (data.get(position).isTitle) {
                return new TitleHolder();
            } else {
                return new TypeHolder();
            }
        }

        @Override
        public boolean hasMore() {
            return false;
        }
    }
}
