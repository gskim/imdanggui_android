package com.imdanggui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.util.CustomDialog;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Request;
import com.imdanggui.adapter.CategoryAdapter;
import com.imdanggui.model.CategoryItem;
import com.imdanggui.util.BackPressCloseHandler;
import com.imdanggui.util.Dlog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class CategoryActivity extends AppCompatActivity  {
    public static final String DRAG_POSITION = "drag_position";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");
    OkHttpClient client = new OkHttpClient();
    String js = null;
    TestPost tp;

    @InjectView(com.imdanggui.R.id.recycler_view)
    RecyclerView recycler_view;
    @InjectView(com.imdanggui.R.id.btnnext)
    Button nextBtn;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    CategoryAdapter mItems = null;
    private BackPressCloseHandler backPressCloseHandler;
    //ProgressDialog progDialog;
    JSONArray categoryNumber;
    Intent intent;
    String parent;
    String type;
    CustomDialog customDialog;
    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(getApplicationContext());
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_category);
        ButterKnife.inject(this);

        intent = getIntent();
        final JSONObject headerJS = new JSONObject();
        if(intent.getExtras().getString("parent").equals("main")){
            parent = "main";
            title = "관심 분야를 선택하세요";
        }else{
            parent = "tab";
            type = intent.getExtras().getString("type");
            if(type.equals("plus")){
                nextBtn.setText("추가하기");
                title = "추가할 관심분야를 선택하세요";
            }else{
                nextBtn.setText("삭제하기");
                title = "삭제할 관심분야를 선택하세요";
            }
               try {
                headerJS.put("device" , StartActivity.device);
                headerJS.put("type" , type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        configToolbar();
        backPressCloseHandler = new BackPressCloseHandler(this);
        customDialog = new CustomDialog(CategoryActivity.this);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.setCancelable(false);
        customDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Dlog.d("==============");
                String[] param = new String[2];
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = headerJS.toString();
                tp = new TestPost();
                tp.execute(param);
                Dlog.d("==============");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Dlog.d("---------------------");
            }

        }).start();

        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new GridLayoutManager(CategoryActivity.this, 3));
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minCnt = 1;
                if(parent.equals("main")){
                    minCnt = 5;
                }

                List<CategoryItem> items = (List<CategoryItem>) mItems.getmItems();
                if(items == null){
                    Toast.makeText(CategoryActivity.this, "추가 할 관심이 없습니다.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    categoryNumber = new JSONArray();

                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).isChecked()) {
                            categoryNumber.put(items.get(i).getId());
                        }
                    }
                    if(categoryNumber.length() < minCnt){
                        if(parent.equals("main")){
                            Toast.makeText(CategoryActivity.this, "다섯개 이상 선택하여주세요.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(CategoryActivity.this, "하나 이상 선택하여주세요.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        customDialog = new CustomDialog(CategoryActivity.this);
                        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        customDialog.setCancelable(false);
                        customDialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String device = StartActivity.device;
                                JSONObject jo = new JSONObject();
                                try {
                                    jo.put("user", device);
                                    jo.put("category", categoryNumber);
                                    jo.put("parent" , parent);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if(parent.equals("tab")){
                                    try {
                                        jo.put("type" , type);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Dlog.d(jo.toString());
                                String[] aa = new String[2];
                                aa[0] = StartActivity.domain + "imdanggui/send_category.php";
                                aa[1] = jo.toString();
                                CategorySend send = new CategorySend();
                                send.execute(aa);
                            }
                        }).start();
                    }
                }


            }
        });

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

    private Handler confirmHandler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Dlog.d("==============");
            //완료 후 실행할 처리 삽입
            recycler_view.setAdapter(mItems);
            customDialog.cancel();


        }
    };


    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //완료 후 실행할 처리 삽입
            customDialog.cancel();
            setResult(RESULT_OK);
            if(parent.equals("main")){
                startIconTabActivity();
            }else{
                finish();
            }

            //finish();
        }
    };

    @Override
    public void onBackPressed() {
        if(parent.equals("tab")){
            finish();
            overridePendingTransition(R.anim.custom_slide_in_top,
                    R.anim.custom_slide_out_bottom);
        }else{
            backPressCloseHandler.onBackPressed();
        }


    }

    private List<CategoryItem> getData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CategoryItem>>(){}.getType();
        List<CategoryItem> oss = gson.fromJson(js, listType);

        return oss;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.menu_category, menu);*/
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configToolbar() {
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); //true 면 뒤로가기 버튼 생성
    }

    private void startIconTabActivity() {
        Intent intent = new Intent(this, IconTabActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivityNoAnimation(intent);

    }
    private void startActivityNoAnimation(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.custom_slide_in_top,
                R.anim.custom_slide_out_bottom);
        finish();
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
            Dlog.d("##category##"+js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Dlog.d("==============");
            if(s == null || s.equals(null)){
                customDialog.cancel();
                Toast.makeText(CategoryActivity.this, "추가할 목록이 없습니다.",
                        Toast.LENGTH_SHORT).show();

            }else{
                try {
                    mItems = new CategoryAdapter(getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                confirmHandler1.sendEmptyMessage(0);
            }

        }
    }
    public class CategorySend extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {
            String url = params[0];
            String json = params[1];
            String result = null;
            try {
                result = post(url, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Dlog.d(result);
            Dlog.d("============" + result + "===================");

            return result;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            confirmHandler.sendEmptyMessage(0);
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
