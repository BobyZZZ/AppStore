package com.bb.test2.interfaces;

import android.database.Observable;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Boby on 2019/5/23.
 */

public interface ApiService {
    public static String URL = "http://192.168.1.100:8080/WebInfos/";

    @GET(URL+"{fileName}")
    Observable<ArrayList<String>> getBanners(@Path("fileName")String fileName);
}
