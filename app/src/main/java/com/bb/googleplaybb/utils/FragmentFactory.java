package com.bb.googleplaybb.utils;

import android.util.Log;

import com.bb.googleplaybb.ui.fragment.AppFragment;
import com.bb.googleplaybb.ui.fragment.BaseFragment;
import com.bb.googleplaybb.ui.fragment.GameFragment;
import com.bb.googleplaybb.ui.fragment.HomeFragment;
import com.bb.googleplaybb.ui.fragment.RankFragment;
import com.bb.googleplaybb.ui.fragment.RecommendFragment;
import com.bb.googleplaybb.ui.fragment.TopicFragment;
import com.bb.googleplaybb.ui.fragment.TopicFragment2;
import com.bb.googleplaybb.ui.fragment.TypeFragment;

import java.util.HashMap;

/**
 * Created by Boby on 2018/7/10.
 */

public class FragmentFactory {

    public static HashMap<Integer, BaseFragment> fragments = new HashMap<>();

    public static BaseFragment createFragment(int position) {
        BaseFragment baseFragment = fragments.get(position);
        if (baseFragment == null) {
            Log.i("zyc", "position:" + position +"   baseFragment == null!!! ");
            switch (position) {
                case 0:
                    baseFragment = new HomeFragment();
                    break;
                case 1:
                    baseFragment = new AppFragment();
                    break;
                case 2:
                    baseFragment = new GameFragment();
                    break;
                case 3:
                    baseFragment = new TopicFragment();
                    break;
                case 4:
                    baseFragment = new RecommendFragment();
                    break;
                case 5:
                    baseFragment = new TypeFragment();
                    break;
                case 6:
                    baseFragment = new RankFragment();
                    break;
            }
            fragments.put(position, baseFragment);
        }

        return baseFragment;
    }
}
