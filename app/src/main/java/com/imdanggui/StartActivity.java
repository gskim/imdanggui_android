package com.imdanggui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import retrofit2.Call;
import android.widget.Toast;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.service.ServiceGenerator;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.model.Setting;
import com.imdanggui.util.Dlog;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit2.Callback;
import retrofit2.Response;

import static com.imdanggui.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.imdanggui.CommonUtilities.EXTRA_MESSAGE;
import static com.imdanggui.CommonUtilities.SENDER_ID;

public class StartActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    AsyncTask<String, String, String> mRegisterTask;
    Intent intent;
    //public static String domain = "http://dlaekdrnl2.godohosting.com/";
    public static String domain = "http://dlaekdrnl2.godohosting.com/";
    public static String device;
    public static Activity startactivity;
    public static SharedPreferences setting;
    public static String regId;
    SharedPreferences.Editor editor;
    String[] param;
    String js;
    PackageInfo pInfo;
    String postId = null ;
    okhttp 쓰면 코드길어져요?
    길어진다기보단 딱히 짧게 해주는게 없음
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.imdanggui.R.layout.activity_start);
        //URQAController.InitializeAndStartSession(getApplicationContext(),"31942DEB");
        setting = getSharedPreferences("setting" , 0);
        startactivity = StartActivity.this;
        device = getDeviceSerialNumber();
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName() , 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Intent getIntent = getIntent();
        if(getIntent != null){
            Dlog.d("#######extra#######");
            postId =  getIntent().getStringExtra("postId");
        }
        Dlog.d("######postid" + String.valueOf(postId));


        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);
        registerReceiver(mHandleMessageReceiver, new IntentFilter(
                DISPLAY_MESSAGE_ACTION));
        // Get GCM registration id
        regId = GCMRegistrar.getRegistrationId(this);

        Dlog.d("##AppVersion##" + pInfo.versionCode );

        if(regId.equals("")){
            regId = setting.getString("regId" , "");
        }
        // Check if regid already presents
        //구글에 등록되어있는지 체크
        if (regId.equals("")) {
            RetrofitService retrofitService = ServiceGenerator.createService(RetrofitService.class);
            Call<Setting> settingCall = retrofitService.postSetting(device,regId,"new","000000");
            settingCall.enqueue(new Callback<Setting>() {
                @Override
                public void onResponse(Call<Setting> call, Response<Setting> response) {

                }

                @Override
                public void onFailure(Call<Setting> call, Throwable throwable) {

                }
            });
            Dlog.d("====equals====");
            // Registration is not present, register now with GCM

            Dlog.d("=======server else=========");
            param = new String[3];
            JSONObject jo = new JSONObject();
            try {
                jo.put("device" , device);
                jo.put("regid" , regId);
                jo.put("type" , "new");
                jo.put("random" , "000000");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            param[0] = domain + "imdanggui/";
            param[1] = "register.php";
            param[2] = jo.toString();
            mRegisterTask = new AsyncTask<String, String, String>() {
                @Override
                protected String doInBackground(String... params) {
                    String url = params[0];
                    String urlPlus = params[1];
                    String json = params[2];
                    try {
                        js = post(url + urlPlus, json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Dlog.d("######JS#####" + js);
                    return js;
                }
                @Override
                protected void onPostExecute(String result) {

                    Gson gson = new Gson();
                    Type type = new TypeToken<Setting>(){}.getType();
                    Setting info = gson.fromJson(js, type);

                    editor = setting.edit();
                    editor.putInt("black", info.getBlack());
                    editor.putString("random" , info.getRandom());
                    if(pInfo.versionName.equals(info.getVersion().trim())){
                        editor.putBoolean("version" , true);
                    }else{
                        editor.putBoolean("version", false);
                    }
                    if( setting.getInt("popupCount" , -1) != info.getPopup() && info.getOpen().equals("y")) {
                        editor.putBoolean("popup", true);
                        editor.putInt("popupCount", info.getPopup());
                        editor.putString("popupImage", info.getPopupImg());
                    }
                    editor.commit();
                    getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in,
                                    android.R.anim.fade_out);
                            finish();
                        }
                    }, 1000);
                    mRegisterTask = null;
                }
            };
            mRegisterTask.execute(param);
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            Dlog.d("====equals else====");
            // Device is already registered on GCM
            if (GCMRegistrar.isRegisteredOnServer(this)) {
                Dlog.d("=======server reg=========");
                // Skips registration.
                //이미 등록되어있을때
                param = new String[3];
                JSONObject jo = new JSONObject();
                try {
                    jo.put("device" , device);
                    jo.put("regid" , regId);
                    jo.put("random" , setting.getString("random" , "000000"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                param[0] = domain + "imdanggui/";
                param[1] = "register.php";
                param[2] = jo.toString();
                mRegisterTask = new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        String url = params[0];
                        String urlPlus = params[1];
                        String json = params[2];
                        try {
                            js = post(url + urlPlus, json);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Dlog.d("JS============"+js);
                        return js;
                    }
                    @Override
                    protected void onPostExecute(String result) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<Setting>(){}.getType();
                        Setting info = gson.fromJson(js, type);
                        editor = setting.edit();
                        editor.putInt("black" , info.getBlack());
                        editor.putString("random", info.getRandom());
                        editor.putString("regId" , regId);
                        if(pInfo.versionName.equals(info.getVersion().trim())){
                            editor.putBoolean("version" , true);
                        }else{
                            editor.putBoolean("version" , false);
                        }
                        if( setting.getInt("popupCount" , -1) != info.getPopup() && info.getOpen().equals("y") ){
                            editor.putBoolean("popup" , true);
                            editor.putInt("popupCount", info.getPopup());
                            editor.putString("popupImage", info.getPopupImg());
                        }
                        editor.commit();
                        getWindow().getDecorView().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(setting.getString("agree1" , "no").equals("yes")){
                                    if(setting.getString("agree2" , "no").equals("yes")){
                                        if(postId != null){
                                            intent = new Intent(getApplicationContext() , PostDetailActivity.class);
                                            intent.putExtra("postId" , postId);
                                        }else{
                                            intent = new Intent(getApplicationContext() , IconTabActivity.class);
                                        }

                                        startActivityNoAnimation(intent);
                                        overridePendingTransition(android.R.anim.fade_in,
                                                android.R.anim.fade_out);
                                        finish();
                                    }
                                }else{
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in,
                                            android.R.anim.fade_out);
                                    finish();
                                }

                            }
                        }, 1000);
                        mRegisterTask = null;

                    }
                };
                mRegisterTask.execute(param);

            } else {
                Dlog.d("######################elseelseelse");
                GCMRegistrar.register(this, SENDER_ID);
                param = new String[3];
                JSONObject jo = new JSONObject();
                try {
                    jo.put("device" , device);
                    jo.put("regid" , regId);
                    jo.put("random" , setting.getString("random" , "000000"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                param[0] = domain + "imdanggui/";
                param[1] = "register.php";
                param[2] = jo.toString();
                mRegisterTask = new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        String url = params[0];
                        String urlPlus = params[1];
                        String json = params[2];

                        try {
                            js = post(url + urlPlus, json);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Dlog.d("JS============"+js);
                        return js;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<Setting>(){}.getType();
                        Setting info = gson.fromJson(js, type);
                        editor = setting.edit();
                        editor.putInt("black" , info.getBlack());
                        editor.putString("random" , info.getRandom());
                        editor.putString("regId" , regId);
                        if(pInfo.versionName.equals(info.getVersion().trim())){
                            editor.putBoolean("version" , true);
                        }else{
                            editor.putBoolean("version" , false);
                        }
                        if( setting.getInt("popupCount" , -1) != info.getPopup() && info.getOpen().equals("y") ){
                            editor.putBoolean("popup", true);
                            editor.putInt("popupCount", info.getPopup());
                            editor.putString("popupImage", info.getPopupImg());
                        }

                        editor.commit();
                        getWindow().getDecorView().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(setting.getString("agree1" , "no").equals("yes")){
                                    if(setting.getString("agree2" , "no").equals("yes")){
                                        if(postId != null){
                                            intent = new Intent(getApplicationContext() , PostDetailActivity.class);
                                            intent.putExtra("postId" , postId);
                                        }else{
                                            intent = new Intent(getApplicationContext() , IconTabActivity.class);
                                        }
                                        startActivityNoAnimation(intent);
                                        overridePendingTransition(android.R.anim.fade_in,
                                                android.R.anim.fade_out);
                                        finish();
                                    }
                                }else{
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in,
                                            android.R.anim.fade_out);
                                    finish();
                                }
                            }
                        }, 1000);
                        mRegisterTask = null;
                    }
                };
                mRegisterTask.execute(param);
            }
        }

    }
    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public void onBackPressed() {
        //종료 못하게
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                Dlog.d("=======result ok==========");
                finish();
            }
        }
    }

    private static String getDeviceSerialNumber() {
        try {
            return (String) Build.class.getField("SERIAL").get(null);
        } catch (Exception ignored) {
            return null;
        }
    }
    private void startActivityNoAnimation(Intent intent) {
        Dlog.d("startActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Dlog.d("=============================");
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());
            /**
             * Take appropriate action on this message
             * depending upon your app requirement
             * For now i am just displaying it on the screen
             * */
            // Showing received message
            //lblMessage.append(newMessage + "\n");
            Toast.makeText(getApplicationContext(), "임당귀: " + newMessage, Toast.LENGTH_LONG).show();

            // Releasing wake lock
            WakeLocker.release();
        }
    };

    @Override
    protected void onDestroy() {
        Dlog.d("=============================");
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        try {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        } catch (Exception e) {

        }
        super.onDestroy();
    }
}
