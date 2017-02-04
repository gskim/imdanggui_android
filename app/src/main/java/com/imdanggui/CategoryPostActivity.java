package com.imdanggui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.util.CustomDialog;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.adapter.PostingAdapter;
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

/**
 * Created by user on 2015-09-17.
 */
public class CategoryPostActivity extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");

    OkHttpClient client = new OkHttpClient();
    String js = null;
    @InjectView(com.imdanggui.R.id.recycler_view)
    RecyclerView recycler_view;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    @InjectView(com.imdanggui.R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.posting)
    LinearLayout posting;

    private PostingAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<PostingItem> list;
    int id;
    String name;
    List<PostingItem> oss;
    int lastId;
    int totalItemCount;
    HeaderBody result ;
    CustomDialog customDialog;
    int black;
    String php;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTypeface();
        setContentView(R.layout.activity_category_postlist);
        ButterKnife.inject(this);
        black = StartActivity.setting.getInt("black", 0);
        customDialog = new CustomDialog(CategoryPostActivity.this);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.setCancelable(false);
        customDialog.show();
        Intent intent = getIntent();
        id = intent.getExtras().getInt("id");
        if(id == -2){
            php = "post_all.php";
        }else{
            php = "post_list.php";
        }
        name = intent.getExtras().getString("name");
        configToolbar();
        final JSONObject headerJS = new JSONObject();
        try {
            headerJS.put("id",id);
            headerJS.put("device" , StartActivity.device);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dlog.d(String.valueOf("===headerJS===" + headerJS));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO : 시간이 걸리는 처리 삽입
                ASynk aSynk = new ASynk();
                String[] param = new String[3];
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = php;
                param[2] = headerJS.toString();
                aSynk.execute(param);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        posting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();

                if (black >= 3) {
                    Toast.makeText(CategoryPostActivity.this, "다수의 신고로 인해 글을 등록하실 수 없습니다.",
                            Toast.LENGTH_SHORT).show();
                } else if (now < StartActivity.setting.getLong("postTime", 0) + 30000) {
                    Toast.makeText(CategoryPostActivity.this, "글을 등록하신지 얼마 안되셨네요...",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(CategoryPostActivity.this, PostActivity.class);
                    if(id == -2){
                        intent.putExtra("category" , String.valueOf(id));
                    }else{
                        intent.putExtra("category" , name);
                    }
                    
                    startActivityForResult(intent, 0);
                    //getActivity().startActivityForResult(intent , 0);
                    overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);
                }
            }
        });

        recycler_view.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(CategoryPostActivity.this);
        recycler_view.setLayoutManager(mLayoutManager);
        recycler_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recycler_view.canScrollVertically(1)) {
                    totalItemCount = mLayoutManager.getItemCount();

                    lastId = list.get(totalItemCount - 1).getId();

                    Dlog.d(String.valueOf(lastId));
                    JSONObject addJS = new JSONObject();
                    try {
                        addJS.put("id", id);
                        addJS.put("device" , StartActivity.device);
                        addJS.put("type", "add");
                        addJS.put("lastId", lastId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Dlog.d(addJS.toString());
                    String[] addParam = new String[3];
                    addParam[0] =  StartActivity.domain + "imdanggui/";
                    addParam[1] = php;
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
            refreshJS.put("device" , StartActivity.device);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dlog.d(refreshJS.toString());
        String[] addParam = new String[3];
        addParam[0] = StartActivity.domain + "imdanggui/";
        addParam[1] = php;
        addParam[2] = refreshJS.toString();
        new ASynk().execute(addParam);

    }
    private void onItemsLoadComplete(){
        Dlog.d("=================");
        swipeRefreshLayout.setRefreshing(false);
    }

    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            mAdapter = new PostingAdapter(list);
            recycler_view.setAdapter(mAdapter);
            customDialog.cancel();
        }
    };
    private void configToolbar() {
        Dlog.d("========================");
        toolbar.setTitle(name);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(com.imdanggui.R.drawable.left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    private List<PostingItem> getData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<PostingItem>>(){}.getType();
        List<PostingItem> oss = gson.fromJson(js, listType);
        Dlog.d(oss.toString());
        return oss;
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
                list.get(pos).setReply_count(rep);
                mAdapter.notifyDataSetChanged();
            }
        }else if(requestCode == 0){
            if(resultCode == RESULT_OK){
                swipeRefreshLayout.setRefreshing(true);
                refreshItem();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
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
            Dlog.d(js);
            return js;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(js.equals("nothing")){
                Toast.makeText(getApplicationContext(), "등록된 글이 없습니다.",
                        Toast.LENGTH_SHORT).show();
            }else{
                Gson gson = new Gson();
                Type type = new TypeToken<HeaderBody>(){}.getType();
                result = gson.fromJson(js.toString(), type);

                js = result.getBody();
                try {
                    list = getData();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            confirmHandler.sendEmptyMessage(0);
            onItemsLoadComplete();
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

            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(js.equals("nothing")){
                Toast.makeText(getApplicationContext(), "불러올것이 없습니다.",
                        Toast.LENGTH_SHORT).show();

                //onItemsLoadComplete();
            }else{
                Gson gson = new Gson();
                Type type = new TypeToken<HeaderBody>(){}.getType();
                result = gson.fromJson(js.toString(), type);

                js = result.getBody();
                if(mAdapter == null){
                    Dlog.d("adapter null");
                    try {
                        list = getData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter = new PostingAdapter(list);
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
