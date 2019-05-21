package com.bb.googleplaybb.ui.activity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.domain.album.ImageEntity;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by Boby on 2019/5/19.
 */

public class chooseImageActivity extends AppCompatActivity implements View.OnClickListener {

    private View mback;
    private RecyclerView mRvImage;
    private LoaderManager.LoaderCallbacks loaderCallbacks;
    private int LOADER_ID_IMAGES = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        mback.setOnClickListener(this);
    }

    private void initView() {
        mback = findViewById(R.id.back);
        mRvImage = findViewById(R.id.rv_image);
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {
        private int size = -1;
        private Cursor mCursor;

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_choose_image, null, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            onBindViewHolder(holder, mCursor);
        }

        private void onBindViewHolder(VH holder, Cursor cursor) {
            ImageEntity imageEntity = ImageEntity.fromCursor(cursor);
            Glide.with(UIUtils.getContext()).load(imageEntity.getUri()).override(size).placeholder(R.drawable.comment_pic_normal_placeholder).into(holder.mImageView);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            }
            return 0;
        }

        public void setSize(int size) {
            this.size = size;
        }


        class VH extends RecyclerView.ViewHolder {
            ImageView mImageView;
            public VH(View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.vImage);
                if (size > 0) {
                    ViewGroup.LayoutParams lp = mImageView.getLayoutParams();
                    lp.width = size;
                    lp.height = size;
                    mImageView.setLayoutParams(lp);
                }
            }
        }
    }
}
