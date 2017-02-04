package com.imdanggui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.View;
import android.view.ViewGroup;
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
import com.imdanggui.adapter.MyPushAdapter;
import com.imdanggui.model.HeaderBody;
import com.imdanggui.model.PostingItem;
import com.imdanggui.model.PushItem;
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
public class MyPushListActivity extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");

    OkHttpClient client = new OkHttpClient();
    String js = null;
    String header_js = null;
    @InjectView(com.imdanggui.R.id.recycler_view)
    RecyclerView recycler_view;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    @InjectView(com.imdanggui.R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    private MyPushAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<PostingItem> list;
    List<PushItem> push_list;
    int id;
    int lastId;
    int totalItemCount;
    HeaderBody result ;
    CustomDialog customDialog;
    JSONObject jo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_category_detail);
        ButterKnife.inject(this);
        customDialog = new CustomDialog(MyPushListActivity.this);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.setCancelable(false);
        customDialog.show();
        configToolbar();
        Intent intent = getIntent();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ASynk aSynk = new ASynk();
                jo = new JSONObject();
                try {
                    jo.put("device" , StartActivity.device);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String[] param = new String[3];
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = "mypush_list.php";
                param[2] = jo.toString();
                aSynk.execute(param);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Dlog.d(js);

            }
        }).start();
        recycler_view.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(MyPushListActivity.this);
        recycler_view.setLayoutManager(mLayoutManager);
        recycler_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = mLayoutManager.getItemCount();
                Dlog.d("totalcount" + String.valueOf(totalItemCount));
                if (!recyclerView.canScrollVertically(1)) {
                    Dlog.d("======================");
                    lastId = push_list.get(totalItemCount - 1).getId();
                    Dlog.d(String.valueOf(lastId));
                    JSONObject addJS = new JSONObject();
                    try {
                        addJS.put("device", StartActivity.device);
                        addJS.put("type", "add");
                        addJS.put("lastId", lastId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Dlog.d(addJS.toString());
                    String[] addParam = new String[3];
                    addParam[0] = StartActivity.domain + "imdanggui/";
                    addParam[1] = "mypush_list.php";
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

    public void refreshItem(){
        JSONObject refreshJS = new JSONObject();
        try {
            refreshJS.put("device",StartActivity.device);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dlog.d(refreshJS.toString());
        String[] addParam = new String[3];
        addParam[0] = StartActivity.domain + "imdanggui/";
        addParam[1] = "mypush_list.php";
        addParam[2] = refreshJS.toString();
        new ASynk().execute(addParam);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 9){
            if(resultCode == RESULT_OK){
                int rep = data.getExtras().getInt("reply");
                int pos = data.getExtras().getInt("pos");
                list.get(pos).setReply_count(rep);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void onItemsLoadComplete(){
        Dlog.d("=================");
        swipeRefreshLayout.setRefreshing(false);
    }
    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //완료 후 실행할 처리 삽입
            mAdapter = new MyPushAdapter(push_list, list);
            recycler_view.setAdapter(mAdapter);
            customDialog.cancel();
        }
    };

    @Override
    protected void onPause() {
        Dlog.d("#####PAUSE#########");
        unregisterReceiver(receiver);
        super.onPause();
    }

    private void configToolbar() {
        toolbar.setTitle("새로운 알림");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(com.imdanggui.R.drawable.left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //unregisterReceiver(receiver);
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
    private List<PushItem> getData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<PushItem>>(){}.getType();
        List<PushItem> oss = gson.fromJson(js, listType);
        Dlog.d(oss.toString());
        return oss;
    }
    private List<PostingItem> getPost() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<PostingItem>>(){}.getType();
        List<PostingItem> oss = gson.fromJson(header_js, listType);
        Dlog.d(oss.toString());
        return oss;
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
                Toast.makeText(getApplicationContext(), "표시할 알림이 없습니다.",
                        Toast.LENGTH_SHORT).show();
            }else{
                Gson gson = new Gson();
                Type type = new TypeToken<HeaderBody>(){}.getType();
                result = gson.fromJson(js.toString(), type);
                header_js = result.getHeader();
                js = result.getBody();
                try {
                    list = getPost();
                    push_list = getData();
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
                Toast.makeText(getApplicationContext(), "끝입니다.",
                        Toast.LENGTH_SHORT).show();
                //mAdapter.notifyItemRemoved(totalItemCount);
                //onItemsLoadComplete();
            }else{
                Gson gson = new Gson();
                Type type = new TypeToken<HeaderBody>(){}.getType();
                result = gson.fromJson(js.toString(), type);
                header_js = result.getHeader();
                js = result.getBody();
                if(mAdapter == null){
                    Dlog.d("adapter null");
                    try {
                        list = getPost();
                        push_list = getData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter = new MyPushAdapter(push_list , list);
                    recycler_view.setAdapter(mAdapter);
                }else{
                    try {
                        list.addAll(getPost());
                        push_list.addAll(getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    BroadcastReceiver receiver;

    @Override
    protected void onResume() {
        Dlog.d("#######resume######");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.imdanggui.MyPushListActivity");
        receiver = new PushBroadcastReceiver();
        registerReceiver(receiver , intentFilter);
        super.onResume();
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
    private class PushBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Dlog.d("#########receive#########");
            refreshItem();
        }
    }
}
