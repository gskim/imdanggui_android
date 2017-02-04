package com.imdanggui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.util.CustomDialog;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.adapter.CategoryHeaderAdapter;
import com.imdanggui.model.CategoryDetailHeader;
import com.imdanggui.model.HeaderBody;
import com.imdanggui.model.PostingItem;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class CategoryDetailActivity extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");
    public static String change = "no";

    public CategoryDetailActivity() {
        this.change = "no";
    }

    OkHttpClient client = new OkHttpClient();
    String js = null;

    @InjectView(com.imdanggui.R.id.recycler_view)
    RecyclerView recycler_view;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    @InjectView(com.imdanggui.R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    private CategoryHeaderAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<PostingItem> list;
    CategoryDetailHeader header;
    int id;
    String name;
    String thumb;
    int ranking;
    String yesterdayPost;
    String yesterdayReply;
    String text;
    List<PostingItem> oss;
    int lastId;
    int totalItemCount;
    HeaderBody result ;
    CustomDialog customDialog;

    int black;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        loadTypeface();
        setContentView(R.layout.activity_category_detail);
        ButterKnife.inject(this);
        black = StartActivity.setting.getInt("black", 0);
        customDialog = new CustomDialog(CategoryDetailActivity.this);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.setCancelable(false);
        customDialog.show();
        Intent intent = getIntent();
        id = intent.getExtras().getInt("id");
        name = intent.getExtras().getString("name");
        thumb = intent.getExtras().getString("thumb");
        text = intent.getExtras().getString("text");
        ranking = intent.getExtras().getInt("ranking");
        yesterdayPost = intent.getExtras().getString("yesterdayPost");
        yesterdayReply = intent.getExtras().getString("yesterdayReply");

        configToolbar();

        final JSONObject headerJS = new JSONObject();
        try {
            headerJS.put("id",id);
            headerJS.put("name" , name);
            headerJS.put("thumb" , thumb);
            headerJS.put("favorite" , text);
            headerJS.put("ranking" , ranking);
            headerJS.put("yesterdayPost" , yesterdayPost);
            headerJS.put("yesterdayReply" , yesterdayReply);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dlog.d(String.valueOf("===headerJS==="+headerJS));

        Gson gson = new Gson();
        Type listType = new TypeToken<CategoryDetailHeader>(){}.getType();
        header = gson.fromJson(String.valueOf(headerJS), listType);
        Dlog.d(String.valueOf(header));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO : 시간이 걸리는 처리 삽입
                ASynk aSynk = new ASynk();
                String[] param = new String[3];
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = "best_post_list.php";
                param[2] = headerJS.toString();
                aSynk.execute(param);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
        recycler_view.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(CategoryDetailActivity.this);
        recycler_view.setLayoutManager(mLayoutManager);
        recycler_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Dlog.d("totalcount" + String.valueOf(totalItemCount));
                if (!recycler_view.canScrollVertically(1)) {
                    totalItemCount = mLayoutManager.getItemCount();
                    Dlog.d("======================");
                    if(list != null){
                        lastId = list.get(totalItemCount - 2).getId();
                    }else{
                        lastId = 0;
                    }

                    Dlog.d(String.valueOf(lastId));
                    JSONObject addJS = new JSONObject();
                    try {
                        addJS.put("id", id);
                        addJS.put("type", "add");
                        addJS.put("lastId", lastId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Dlog.d(addJS.toString());
                    String[] addParam = new String[3];
                    addParam[0] = StartActivity.domain + "imdanggui/";
                    addParam[1] = "best_post_list.php";
                    addParam[2] = addJS.toString();
                    new AddList().execute(addParam);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItem();
            }
        });

    }
    private void refreshItem(){
        JSONObject refreshJS = new JSONObject();
        try {
            refreshJS.put("id",id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dlog.d(refreshJS.toString());
        String[] addParam = new String[3];
        addParam[0] =  StartActivity.domain + "imdanggui/";
        addParam[1] = "best_post_list.php";
        addParam[2] = refreshJS.toString();
        new ASynk().execute(addParam);

    }
    private void onItemsLoadComplete(){
        Dlog.d("=================");
        swipeRefreshLayout.setRefreshing(false);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Dlog.d("activity result");
        //mAdapter.onActivityResult(requestCode , resultCode , data);
        if( requestCode == 1 ){
            if( resultCode == RESULT_OK ){
                swipeRefreshLayout.setRefreshing(true);
                refreshItem();
            }
        }else if(requestCode == 9){
            if(resultCode == RESULT_OK){
                int rep = data.getExtras().getInt("reply");
                int pos = data.getExtras().getInt("pos");
                list.get(pos -1).setReply_count(rep);
                mAdapter.notifyDataSetChanged();
            }
        }else if(requestCode == 6){
            if(resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        }

    }
    private List<PostingItem> getData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<PostingItem>>(){}.getType();
        oss = gson.fromJson(js, listType);
        Dlog.d(oss.toString());
        return oss;
    }
    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //완료 후 실행할 처리 삽입

            mAdapter = new CategoryHeaderAdapter(header ,list);
            recycler_view.setAdapter(mAdapter);
            customDialog.cancel();

        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra("addRemove", change);
        intent.putExtra("category", id);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        //super.onBackPressed();
    }

    private void configToolbar() {
        Dlog.d("========================");
        toolbar.setTitle(name);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(com.imdanggui.R.drawable.left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("addRemove", change);
                intent.putExtra("category", id);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
    public class ASynk extends AsyncTask<String , String , String> {
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
            if(js.equals("nothing")){
            }else{
                Gson gson = new Gson();
                Type type = new TypeToken<HeaderBody>(){}.getType();
                result = gson.fromJson(js.toString(), type);
                js = result.getBody();

            }
            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(js.equals("nothing")){
                confirmHandler.sendEmptyMessage(0);
                onItemsLoadComplete();
            }else {
                try {
                    list = getData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                confirmHandler.sendEmptyMessage(0);

                onItemsLoadComplete();
            }

        }
    }
    public class AddList extends AsyncTask<String , String , String> {
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
            if(js.equals("nothing")){

            }else{
                Gson gson = new Gson();
                Type type = new TypeToken<HeaderBody>(){}.getType();
                result = gson.fromJson(js, type);
                Dlog.d("header ===" + result.getHeader());
                js = result.getBody();
            }
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(js.equals("nothing")){
                Toast.makeText(CategoryDetailActivity.this, "불러올것이 없습니다.",
                        Toast.LENGTH_SHORT).show();
                //mAdapter.notifyItemRemoved(totalItemCount);
                //onItemsLoadComplete();
            }else{
                if(mAdapter == null){
                    Dlog.d("adapter null");
                    try {
                        list = getData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter = new CategoryHeaderAdapter(header ,list);
                    recycler_view.setAdapter(mAdapter);
                }else{
                    try {
                        list.addAll(getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                mAdapter.notifyDataSetChanged();
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
}
