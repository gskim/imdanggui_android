package com.imdanggui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.util.CustomDialog;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

public class PostActivity extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");
    String getCategory;
    String category;
    OkHttpClient client = new OkHttpClient();
    ArrayAdapter<String> adapter;
    String js;
    TestPost tp;
    ArrayList<String> oss;
    Intent intent;
    SweetAlertDialog sweetAlertDialog;
    @InjectView(com.imdanggui.R.id.editText)
    EditText editText;
    @InjectView(com.imdanggui.R.id.text_count)
    TextView textView;
    @InjectView(com.imdanggui.R.id.category)
    Spinner spinner;
    @InjectView(com.imdanggui.R.id.nickname)
    EditText nickname;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.spinner_iv)
    ImageView spinner_iv;
    @InjectView(R.id.tv1)
            TextView tv1;
    @InjectView(R.id.tv2)
            TextView tv2;
    CustomDialog customDialog;
    SharedPreferences.Editor editor;
    boolean postOk = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_post);
        ButterKnife.inject(this);
        configToolbar();
        intent = getIntent();
        getCategory = intent.getExtras().getString("category" );
        if(getCategory.equals("-2")) {
            customDialog = new CustomDialog(PostActivity.this);
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            customDialog.setCancelable(false);
            customDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String device = StartActivity.device;
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("device", device);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String[] param = new String[2];
                    param[0] = StartActivity.domain + "imdanggui/name_category_backup.php";
                    param[1] = jo.toString();
                    tp = new TestPost();
                    tp.execute(param);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        }else if(getCategory.equals("all")){
            customDialog = new CustomDialog(PostActivity.this);
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            customDialog.setCancelable(false);
            customDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String device = StartActivity.device;
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("device", device);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String[] param = new String[2];
                    param[0] = StartActivity.domain + "imdanggui/name_category.php";
                    param[1] = jo.toString();
                    tp = new TestPost();
                    tp.execute(param);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        }else{
            js = "[" + getCategory + "]";
            getData();
            spinner.setEnabled(false);
        }
        spinner_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });
        nickname.addTextChangedListener(new TextWatcher() {
            String previousString = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousString= s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (nickname.getLineCount() >= 2)
                {
                    nickname.setText(previousString);
                    nickname.setSelection(nickname.length());
                }
            }
        });

        TextView send = (TextView) toolbar.findViewById(com.imdanggui.R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.d("############");
                if (nickname.getText().toString().trim().getBytes().length <= 0) {
                    Toast.makeText(PostActivity.this, "닉네임을 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                } else if (editText.getText().toString().trim().getBytes().length <= 0) {
                    Toast.makeText(PostActivity.this, "내용을 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                } else if (category == null || category == "" || category.getBytes().length <= 0) {
                    Toast.makeText(PostActivity.this, "카테고리를 선택해주세요",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if(postOk == true){
                        postOk = false;
                        customDialog = new CustomDialog(PostActivity.this);
                        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        customDialog.setCancelable(false);
                        customDialog.show();
                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("nickname", nickname.getText());
                            jo.put("text", editText.getText());
                            jo.put("category", category);
                            jo.put("device", StartActivity.device);
                            jo.put("random", StartActivity.setting.getString("random", "000000"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Dlog.d(jo.toString());
                        String[] postParam = new String[2];
                        postParam[0] = StartActivity.domain + "imdanggui/register_posting.php";
                        postParam[1] = jo.toString();
                        SendPost sendPost = new SendPost();
                        sendPost.execute(postParam);
                        editor = StartActivity.setting.edit();
                        editor.putLong("postTime", System.currentTimeMillis());
                        editor.commit();
                    }else{

                    }
                }
            }
        });



        editText.addTextChangedListener(new TextWatcher() {
            String strCur;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                strCur = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 200) {
                    editText.setText(strCur);
                    editText.setSelection(start);
                } else {
                    textView.setText(String.valueOf(s.length()) + "/200");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = oss.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    private void configToolbar() {
        toolbar.setTitle("글쓰기");
        toolbar.setTitleTextColor(Color.WHITE);
        this.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(com.imdanggui.R.drawable.left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetAlertDialog = new SweetAlertDialog(PostActivity.this ,WARNING_TYPE);
                sweetAlertDialog.setTitleText("")
                        .setContentText("작성을 취소하시겠습니까?")
                        .setCancelText("아니요")
                        .setConfirmText("네")
                        .showCancelButton(true).show();
                sweetAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        sweetAlertDialog.cancel();
                    }
                });
                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                        finish();
                        overridePendingTransition(android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right);

                    }
                });
            }
        });
    }
    private void getData(){
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<String>>(){}.getType();
        oss = gson.fromJson(js, listType);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item , oss);
        spinner.setPrompt("카테고리");
        spinner.setAdapter(adapter);
    }
    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //완료 후 실행할 처리 삽입
            //progDialog.dismiss();
            customDialog.cancel();
        }
    };

    @Override
    public void onBackPressed() {
        sweetAlertDialog = new SweetAlertDialog(this ,WARNING_TYPE);
        sweetAlertDialog.setTitleText("")
                .setContentText("작성을 취소하시겠습니까?")
                .setCancelText("아니요")
                .setConfirmText("네")

                .showCancelButton(true).show();
        sweetAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                sweetAlertDialog.cancel();
            }
        });
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.cancel();
                finish();
                overridePendingTransition(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.imdanggui.R.menu.menu_post, menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.imdanggui.R.id.send) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public class TestPost extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {
            String url = params[0];
            String json = params[1];
            try {
                js = post(url, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Dlog.d("===========");
            Dlog.d("##" + js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            getData();
            confirmHandler.sendEmptyMessage(0);
        }
    }
    public class SendPost extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {
            String url = params[0];
            String json = params[1];
            try {
                js = post(url, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Dlog.d("===========");

            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setResult(RESULT_OK, intent);
            customDialog.cancel();
            Toast.makeText(PostActivity.this, "등록되었습니다",
                    Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);

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
}
