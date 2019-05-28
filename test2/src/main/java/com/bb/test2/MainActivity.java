package com.bb.test2;

import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bb.test2.interfaces.ApiService;
import com.bumptech.glide.Glide;

import org.json.JSONArray;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<String> mDatas;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoginActivity2.startLoginActivity(this);

        initView();

        //initData();
    }

    private void initData() {
        mDatas = new ArrayList<>();
        for (int i = 0; i < 220; i++) {
            mDatas.add(i + "");
        }

        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext(ApiService.URL + "app/banner");
            }
        });

        observable.observeOn(Schedulers.io())
                .map(new Function<String, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> apply(String s) throws Exception {
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request build = new Request.Builder().url(s).build();
                        Response response = okHttpClient.newCall(build).execute();
                        ArrayList<String> banners = null;
                        if (response.isSuccessful()) {
                            banners = new ArrayList<>();
                            String result = response.body().string();
                            Log.e("zyc", "accept: " + result);
                            JSONArray jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String string = jsonArray.getString(i);
                                banners.add(string);
                            }
                        }
                        return banners;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> banner) throws Exception {
                        RvAdapter rvAdapter = new RvAdapter(mDatas);
                        rvAdapter.setBanners(banner);
                        mRecyclerView.setAdapter(rvAdapter);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        mRecyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
                    }
                });
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recyclerView);

    }

    class RvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<String> banners;
        private ArrayList<String> datas;
        private RecyclerView.ViewHolder mBannerHolder;
        private boolean bannerInited;

        public RvAdapter(ArrayList<String> datas) {
            this.datas = datas;
        }

        public void setBanners(ArrayList<String> list) {
            banners = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            RecyclerView.ViewHolder holder;
            if (viewType == 0) {
                //banner
                if (mBannerHolder != null) {
                    return mBannerHolder;
                }
                View view = inflater.inflate(R.layout.list_item_banner, parent, false);
                holder = new BannerHolder(view);
                mBannerHolder = holder;
            } else {
                //正常条目
                View view = inflater.inflate(R.layout.list_item_normal, parent, false);
                holder = new NormalHolder(view);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof BannerHolder) {
                if (!bannerInited) {
                    BannerHolder bannerHolder = (BannerHolder) holder;
                    BannerAdapter bannerAdapter = new BannerAdapter(banners);
                    bannerHolder.mViewPager.setAdapter(bannerAdapter);
//                bannerHolder.mViewPager.setCurrentItem(10000 * banners.size());
                    bannerHolder.startPlay();
                    bannerInited = true;
                }

            } else if (holder instanceof NormalHolder) {
                position -= 1;
                String text = mDatas.get(position);
                NormalHolder normalHolder = (NormalHolder) holder;
                normalHolder.mTextView.setText(text);
            }
        }

        @Override
        public int getItemCount() {
            return datas.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        class NormalHolder extends RecyclerView.ViewHolder {
            TextView mTextView;

            public NormalHolder(View itemView) {
                super(itemView);
                mTextView = itemView.findViewById(R.id.tv);
            }
        }

        class BannerHolder extends RecyclerView.ViewHolder {
            ViewPager mViewPager;

            public BannerHolder(View itemView) {
                super(itemView);
                mViewPager = itemView.findViewById(R.id.vp);
            }

            public void startPlay() {
                new PlayTask(mViewPager).start();
            }

            class PlayTask implements Runnable {
                private ViewPager mViewPager;

                public PlayTask(ViewPager viewPager) {
                    mViewPager = viewPager;
                }

                public void start() {

                    int currentItem = mViewPager.getCurrentItem();
                    mViewPager.setCurrentItem(currentItem);
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.postDelayed(this, 1000);
                }

                @Override
                public void run() {
                    int currentItem = mViewPager.getCurrentItem();
                    mViewPager.setCurrentItem(++currentItem);
                    mHandler.postDelayed(this, 1000);
                }
            }
        }
    }

    class BannerAdapter extends PagerAdapter {
        private ArrayList<String> datas;

        public BannerAdapter(ArrayList<String> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position = position % datas.size();
            String path = datas.get(position);
            ImageView imageView = new ImageView(MainActivity.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(lp);
            Glide.with(MainActivity.this).load(ApiService.URL + path).into(imageView);

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}

