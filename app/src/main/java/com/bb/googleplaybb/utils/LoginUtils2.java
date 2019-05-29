package com.bb.googleplaybb.utils;

import android.text.TextUtils;
import android.util.Log;

import com.bb.googleplaybb.domain.AppLiked;
import com.bb.googleplaybb.domain.User;
import com.bb.googleplaybb.global.GooglePlayApplication;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Boby on 2019/5/16.
 */

public class LoginUtils2 {
    public static final int TYPE_ID = 0;
    public static final int TYPE_NAME = 1;

    public static String BASE_URL = "http://" + GooglePlayApplication.getDBIp() + ":8080/user/";
    public static String LOGIN_URL = "http://" + GooglePlayApplication.getDBIp() + ":8080/user/login";
    public static String REGISTER_URL = "http://" + GooglePlayApplication.getDBIp() + ":8080/user/register";
    public static String CHECK_ID_URL = "http://" + GooglePlayApplication.getDBIp() + ":8080/user/checkId/";
    public static String CHECK_Name_URL = "http://" + GooglePlayApplication.getDBIp() + ":8080/user/checkName/";
    public static String UPLOAD_URL = "http://" + GooglePlayApplication.getDBIp() + ":8080/user/upload";

    public static void modifyDBIP(String ip) {
        BASE_URL = "http://" + ip + ":8080/user/";
        LOGIN_URL = "http://" + ip + ":8080/user/login";
        REGISTER_URL = "http://" + ip + ":8080/user/register";
        CHECK_ID_URL = "http://" + ip + ":8080/user/checkId/";
        CHECK_Name_URL = "http://" + ip + ":8080/user/checkName/";
        UPLOAD_URL = "http://" + ip + ":8080/user/upload";
    }

    /**
     * 服务器接口：
     *
     * @param id
     * @param pwd
     * @throws IOException
     * @ResponseBody
     * @PostMapping("/user/login") String login(@RequestParam("id") String id,@RequestParam("pwd") String pwd) {
     */
    public static void login(final String id, final String pwd, final OnResult onResult) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("");
            }
        }).observeOn(Schedulers.io())
                .map(new Function<String, ResponseResult>() {
                    @Override
                    public ResponseResult apply(String s) {
                        OkHttpClient client = new OkHttpClient();
                        MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("id", id)
                                .addFormDataPart("pwd", pwd)
                                .build();
                        Request request = new Request.Builder().url(LOGIN_URL)
                                .post(body)
                                .build();
                        ResponseResult result = new ResponseResult();
                        try {
                            Response response = client.newCall(request).execute();
                            result.setResponse(response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return result;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseResult>() {
                    @Override
                    public void accept(ResponseResult responseResult) throws Exception {
                        Response response = responseResult.getResponse();
                        User user = new User();
                        if (response != null) {
                            user.setResultCode(User.RESULT_ID_OR_PWD_WRONG);
                        }
                        if (response != null && response.isSuccessful()) {
                            String responseBody = response.body().string();
                            if (!TextUtils.isEmpty(responseBody)) {
                                user = User.fromResponse(responseBody);
                            }
                        }
                        if (onResult != null) {
                            onResult.onResult(user);
                        }
                    }
                });
    }

    /**
     * 服务器接口：
     *
     * @param id
     * @param pwd
     * @param userName
     * @param photoPath
     * @ResponseBody
     * @PostMapping("/user/register") String register(@RequestParam("id") String id,@RequestParam("pwd") String pwd,@RequestParam("userName") String userName,@RequestParam("photoPath") String photoPath) {
     */
    public static void register(final String id, final String pwd, final String userName, final String photoPath, final OnResult onResult) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("");
            }
        }).observeOn(Schedulers.io())
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) {
                        OkHttpClient client = new OkHttpClient();
                        MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("id", id)
                                .addFormDataPart("pwd", pwd)
                                .addFormDataPart("userName", userName)
                                .addFormDataPart("photoPath", photoPath)
                                .build();
                        Request request = new Request.Builder().url(REGISTER_URL)
                                .post(body)
                                .build();

                        try {
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();
                                if (!TextUtils.isEmpty(responseBody)) {
                                    JSONObject jo = new JSONObject(responseBody);
                                    boolean result = jo.getBoolean("data");
                                    return result;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (onResult != null) {
                            onResult.onResult(aBoolean);
                        }
                    }
                });
    }

    /**
     * 服务器接口
     *
     * @param type
     * @param s
     * @return
     * @ResponseBody
     * @GetMapping("/user/checkId/{userId}")
     */
    public static boolean isExisted(int type, String s) {
        OkHttpClient client = new OkHttpClient();
        Request request = null;
        if (type == TYPE_ID) {
            request = new Request.Builder().url(CHECK_ID_URL + s)
                    .build();
        } else if (type == TYPE_NAME) {
            request = new Request.Builder().url(CHECK_Name_URL + s)
                    .build();
        }

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response != null) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                boolean result = jsonObject.getBoolean("data");
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return false;
    }

    /*--------------------------------------------------------------------------------*/

    /**
     * 服务器接口：
     *
     * @param appLiked
     * @return
     * @ResponseBody
     * @PostMapping("/user/liked") boolean like(@RequestBody AppLiked appLiked) {｝
     */
    public static Response liked(AppLiked appLiked) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        String json = gson.toJson(appLiked);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Request request = new Request.Builder().url(BASE_URL + "liked").post(requestBody).build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 服务器接口：
     *
     * @param user_id
     * @param app_id
     * @param onResult
     * @ResponseBody
     * @GetMapping("/user/unliked/{user_id}/{app_id}")
     */
    public static void unLiked(final String user_id, final String app_id, final OnResult onResult) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("");
            }
        })
                .observeOn(Schedulers.io())
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) throws Exception {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(BASE_URL + "unliked/" + user_id + "/" + app_id).build();
                        try {
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                String string = response.body().string();
                                if (!TextUtils.isEmpty(string)) {
                                    JSONObject jsonObject = new JSONObject(string);
                                    boolean result = jsonObject.getBoolean("data");
                                    return result;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (onResult != null) {
                            onResult.onResult(aBoolean);
                        }
                    }
                });
    }

    /**
     * 服务器接口：
     *
     * @param user_id
     * @param app_id
     * @param onResult
     * @ResponseBody
     * @GetMapping("/user/isLiked/{user_id}/{app_id}")
     */
    public static void isLiked(final String user_id, final String app_id, final OnResult onResult) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext(app_id);
            }
        }).observeOn(Schedulers.io())
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) throws Exception {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(BASE_URL + "isLiked/" + user_id + "/" + app_id).build();
                        try {
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();
                                if (!TextUtils.isEmpty(responseBody)) {
                                    boolean result = new JSONObject(responseBody).getBoolean("data");
                                    return result;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (onResult != null) {
                            onResult.onResult(aBoolean);
                        }
                    }
                });
    }


    /**
     * 服务器接口：
     *
     * @param user_id
     * @param onResult
     * @ResponseBody
     * @GetMapping("/user/getAllLiked/{user_id}")
     */
    public static void getAllLikedApp(final String user_id, final OnResult onResult) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext(user_id);
            }
        }).observeOn(Schedulers.io())
                .map(new Function<String, ArrayList<AppLiked>>() {
                    @Override
                    public ArrayList<AppLiked> apply(String s) throws Exception {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(BASE_URL + "getAllLiked/" + user_id).build();
                        try {
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                String bodyString = response.body().string();
                                if (!TextUtils.isEmpty(bodyString)) {
                                    Log.e("client:getAllLikedApp", "bodyString: " + bodyString);
                                    ArrayList<AppLiked> result = new ArrayList<>();
                                    JSONArray ja = new JSONObject(bodyString).getJSONArray("data");
                                    for (int i = 0; i < ja.length(); i++) {
                                        JSONObject jo = ja.getJSONObject(i);
                                        AppLiked appLiked = new AppLiked();
                                        appLiked.setApp_des(jo.getString("app_des"));
                                        appLiked.setApp_icon(jo.getString("app_icon"));
                                        appLiked.setApp_id(jo.getString("app_id"));
                                        appLiked.setApp_name(jo.getString("app_name"));
                                        appLiked.setApp_package_name(jo.getString("app_package_name"));
                                        appLiked.setUser_id(jo.getString("user_id"));
                                        result.add(appLiked);
                                    }
                                    return result;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<AppLiked>>() {
                    @Override
                    public void accept(ArrayList<AppLiked> list) throws Exception {
                        if (onResult != null) {
                            onResult.onResult(list);
                        }
                    }
                });
    }

    public interface OnResult<T> {
        void onResult(T result);
    }

    static class ResponseResult {
        private boolean success;
        private Response response;

        public ResponseResult() {
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Response getResponse() {
            return response;
        }

        public void setResponse(Response response) {
            this.response = response;
        }
    }
}
