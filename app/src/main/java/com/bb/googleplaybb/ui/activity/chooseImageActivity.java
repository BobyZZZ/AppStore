package com.bb.googleplaybb.ui.activity;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.utils.UIUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Boby on 2019/5/19.
 */

public class chooseImageActivity extends AppCompatActivity implements View.OnClickListener {

    private View mback;
    private RecyclerView mRvImage;
    private LoaderManager.LoaderCallbacks loaderCallbacks;
    private int LOADER_ID_IMAGES = 0;
    private ArrayList<String> mList;
    private ArrayList fileNames;


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
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        fileNames = new ArrayList();
        while (cursor != null && cursor.moveToNext()) {
            String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            String description = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
            fileNames.add(new String(data, 0, data.length - 1));
        }

        ImageAdapter adapter = new ImageAdapter(fileNames);
        mRvImage.setLayoutManager(new GridLayoutManager(this, 4));
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        adapter.setSize(displayMetrics.widthPixels / 4);
        mRvImage.setAdapter(adapter);
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
        private ArrayList<String> datas;

        public ImageAdapter(ArrayList list) {
            this.datas = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_choose_image, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, final int position) {
//            if (!mCursor.moveToPosition(position)) {
//                throw new IllegalStateException("couldn't move cursor to position " + position);
//            }
//            onBindViewHolder(holder, mCursor);
            String fileName = datas.get(position);
//            Bitmap bitmap = BitmapFactory.decodeFile(fileName);
            Glide.with(UIUtils.getContext()).load(fileName).into(holder.mImageView);
//            holder.mImageView.setImageBitmap(bitmap);
        }

//        private void onBindViewHolder(VH holder, Cursor cursor) {
//            ImageEntity imageEntity = ImageEntity.fromCursor(cursor);
//            Glide.with(UIUtils.getContext()).load(imageEntity.getUri()).override(size).placeholder(R.drawable.comment_pic_normal_placeholder).into(holder.mImageView);
//            holder.mImageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//        }

        @Override
        public int getItemCount() {
//            if (mCursor != null) {
//                return mCursor.getCount();
//            }
//            return 0;
            return datas.size();
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
