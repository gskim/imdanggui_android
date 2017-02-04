package com.imdanggui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE;
import static cn.pedant.SweetAlert.SweetAlertDialog.SUCCESS_TYPE;
import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

public class SettingActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();
    public MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    Boolean isReset = false;
    String js;
    JSONObject jo;
    SweetAlertDialog sweetAlertDialog;
    SharedPreferences.Editor editor;
    @InjectView(com.imdanggui.R.id.new_push)
    ImageView newPush;
    @InjectView(com.imdanggui.R.id.agree1)
    TextView agree1;
    @InjectView(com.imdanggui.R.id.agree2)
    TextView agree2;
    @InjectView(com.imdanggui.R.id.help)
    TextView help;
    @InjectView(com.imdanggui.R.id.rating)
    TextView rating;
    @InjectView(com.imdanggui.R.id.reset)
    TextView reset;
    @InjectView(com.imdanggui.R.id.version)
    TextView version;

    @InjectView(com.imdanggui.R.id.mypush)
    LinearLayout mypush;
    @InjectView(com.imdanggui.R.id.myreply)
    TextView myreply;
    @InjectView(com.imdanggui.R.id.mypost)
    TextView mypost;
    @InjectView(com.imdanggui.R.id.password_switch)
    SwitchCompat passwordSwitch;
    @InjectView(com.imdanggui.R.id.push_switch)
    SwitchCompat pushSwitch;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    SharedPreferences setting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_setting);
        ButterKnife.inject(this);
        toolbar.setTitle("환경설정");
        toolbar.setTitleTextColor(Color.WHITE);
        this.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(com.imdanggui.R.drawable.left_arrow));
        //버젼가져오기
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName() , 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version.setText("Ver "+pInfo.versionName);

        setting = getSharedPreferences("setting" , 0);
        String pwdUse = setting.getString("passworduse" , "no");
        Boolean pushUse = setting.getBoolean("push" , true);
        if(pwdUse.equals("no")){
            passwordSwitch.setChecked(false);
        }else{
            passwordSwitch.setChecked(true);
        }
        if(pushUse == true){
            pushSwitch.setChecked(true);
        }else{
            pushSwitch.setChecked(false);
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.d("##############");
                unregisterReceiver(receiver);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        pushSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                jo = new JSONObject();
                if (isChecked) {
                    try {
                        jo.put("device", StartActivity.device);
                        jo.put("push", "on");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        jo.put("device", StartActivity.device);
                        jo.put("push", "off");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                String[] param = new String[3];
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = "push_update.php";
                param[2] = jo.toString();

                new PushAsynk().execute(param);
            }
        });
        passwordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = StartActivity.setting.edit();
                if(isChecked){
                    Intent intent = new Intent( getApplicationContext() ,PwdActivity.class);
                    intent.putExtra("setting", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    unregisterReceiver(receiver);
                    startActivityForResult(intent, 0);

                }else{
                    editor.putString("passworduse" , "no");
                    editor.putString("password", null);
                    editor.commit();
                }
            }
        });
        mypost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.d("############mypost#########");
                Intent intent = new Intent(getApplicationContext(), MyPostListActivity.class);
                startAct(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        myreply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyReplyListActivity.class);
                startAct(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        mypush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyPushListActivity.class);
                startAct(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
                startAct(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        agree1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AgreeActivity.class);
                intent.putExtra("type" , 1);
                startAct(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        agree2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AgreeActivity.class);
                intent.putExtra("type" , 2);
                startAct(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                //marketLaunch.setData(Uri.parse("market://details?id=com.imdanggui") );
                marketLaunch.setData(Uri.parse("market://details?id=com.imdanggui") );
                startActivity(marketLaunch);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog = new SweetAlertDialog(SettingActivity.this ,WARNING_TYPE);
                sweetAlertDialog.setTitleText("레드썬!")
                        .setContentText("모든 기록들을 지우시겠습니까?")
                        .setCancelText("잠깐만요!")
                        .setConfirmText("네 지워주세요!")
                        .showCancelButton(true).show();
                sweetAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                });
                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener(){

                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if(isReset == false){
                            isReset = true;
                            JSONObject resetJS = new JSONObject();
                            try {
                                resetJS.put("device", StartActivity.device);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Dlog.d(resetJS.toString());
                            String[] singoParam = new String[3];
                            singoParam[0] = StartActivity.domain + "imdanggui/";
                            singoParam[1] = "user_reset.php";
                            singoParam[2] = resetJS.toString();
                            new Reset().execute(singoParam);
                        }
                    }
                });

            }
        });

    }
    public void startAct(Intent intent){
        startActivity(intent);
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(receiver);
        finish();
        this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            if(resultCode == RESULT_CANCELED){
                passwordSwitch.setChecked(false);
            }
        }
    }

    private Typeface typeface = null;
    private static final String TYPEFACE_NAME = "fonts/dotum.ttf";
    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID , null);
        ViewGroup group = (ViewGroup)view;
        int childCnt = group.getChildCount();
        for (int i = 0 ; i < childCnt ; i ++){
            View v = group.getChildAt(i);
            if( v instanceof TextView){
                ((TextView)v).setTypeface(typeface);
            }
        }
        super.setContentView(view);
    }
    private void loadTypeface(){
        if(typeface==null)
            typeface = Typeface.createFromAsset(getAssets(), TYPEFACE_NAME);
    }
    public class PushAsynk extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {

            String url = params[0];
            String urlPlus = params[1];
            String json = params[2];
            try {
                js = post(url + urlPlus, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Dlog.d("=======================");
            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            editor = StartActivity.setting.edit();
            if(s.equals("on")){
                editor.putBoolean("push" , true);
            }else{
                editor.putBoolean("push" , false);
            }

            editor.commit();
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
    BroadcastReceiver receiver;

    @Override
    protected void onResume() {
        Dlog.d("#######resume######");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.imdanggui.SettingActivity");
        receiver = new PushBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
        if( StartActivity.setting.getInt("pushcount" , 0) == 0 ){
            newPush.setVisibility(View.GONE);
        }else{
            newPush.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }
    private class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Dlog.d("#########receive#########");
            newPush.setVisibility(View.VISIBLE);
        }
    }
    public class Reset extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {

            String url = params[0];
            String urlPlus = params[1];
            String json = params[2];
            try {
                js = post(url + urlPlus, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Dlog.d("=======================");
            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SharedPreferences pref = getSharedPreferences("setting", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
            sweetAlertDialog
                    .setTitleText("완료!")
                    .setContentText("임당귀를 종료 합니다.")
                    .setConfirmText("네")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener(){
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            unregisterReceiver(receiver);
                            sweetAlertDialog.cancel();
                            setResult(RESULT_OK);
                            finish();
                        }
                    })
                    .showCancelButton(false)
                    .changeAlertType(SUCCESS_TYPE);
            sweetAlertDialog.show();
        }
    }
}
