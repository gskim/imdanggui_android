package com.imdanggui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.model.HeaderBody;
import com.imdanggui.util.CustomDialog;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.adapter.PostDetailAdapter;
import com.imdanggui.model.PostDetailHeader;
import com.imdanggui.model.ReplyItem;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.*;

public class PostDetailActivity extends AppCompatActivity {
    boolean singoClick = false;
    boolean isReply = false;
    int pos;
    int id;
    int singo;
    String text;
    String nickname;
    String regdate;
    String category;
    String random;
    int reply;
    HeaderBody result ;
    public static final MediaType JSON = MediaType.parse("application/json; charset=UTF-8");
    OkHttpClient client = new OkHttpClient();
    String js = null;
    @InjectView(com.imdanggui.R.id.reply_send)
    TextView replySend;
    @InjectView(com.imdanggui.R.id.reply_text)
    EditText replyText;
    @InjectView(com.imdanggui.R.id.recycler_view)
    RecyclerView recycler_view;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    @InjectView(com.imdanggui.R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<ReplyItem> list;
    PostDetailHeader header;

    List<ReplyItem> oss;
    int lastId;
    int totalItemCount;
    JSONObject headerJO;
    String[] param;
    JSONObject addJS;
    Intent intent;
    private AlertDialog mDialog = null;
    CustomDialog customDialog;
    SweetAlertDialog sweetAlertDialog;
    String replyMessage;
    SharedPreferences.Editor editor;
    String postId;
    String isPush;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_post_detail);
        ButterKnife.inject(this);
        configToolbar();
        customDialog = new CustomDialog(PostDetailActivity.this);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.setCancelable(false);
        customDialog.show();
        intent = getIntent();
        postId = intent.getExtras().getString("postId", null);
        if(postId != null){
            id = Integer.parseInt(postId);
            isPush = "push";
        }else{
            isPush = "no";
            pos = intent.getExtras().getInt("pos");
            id = intent.getExtras().getInt("id");
            singo = intent.getExtras().getInt("singo");
            text = intent.getExtras().getString("text");
            nickname = intent.getExtras().getString("nickname");
            regdate = intent.getExtras().getString("regdate");
            reply = intent.getExtras().getInt("reply");
            category = intent.getExtras().getString("category");
            random = intent.getExtras().getString("random");
            headerJO = new JSONObject();
            try {
                headerJO.put("id" , id);
                headerJO.put("singo" , singo);
                headerJO.put("text" , text);
                headerJO.put("nickname" , nickname);
                headerJO.put("regdate" , regdate);
                headerJO.put("reply" , reply);
                headerJO.put("category" , category);
                headerJO.put("random" , random);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Gson gson = new Gson();
            Type listType = new TypeToken<PostDetailHeader>(){}.getType();
            header = gson.fromJson(String.valueOf(headerJO), listType);
        }


        new Thread(new Runnable() {
        @Override
        public void run() {
            //TODO : 시간이 걸리는 처리 삽입
            JSONObject jo = new JSONObject();
            try {
                jo.put("postId" , id);
                jo.put("isPush" , isPush);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ASynk aSynk = new ASynk();
            param = new String[3];
            param[0] = StartActivity.domain + "imdanggui/";
            param[1] = "reply_list_second.php";
            param[2] = jo.toString();
            aSynk.execute(param);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //dismiss(다이알로그종료)는 반드시 새로운 쓰레드 안에서 실행되어야한다

        }
    }).start();
    recycler_view.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(PostDetailActivity.this);
    recycler_view.setLayoutManager(mLayoutManager);
    recycler_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            totalItemCount = mLayoutManager.getItemCount();
            Dlog.d("totalcount" + String.valueOf(totalItemCount));
            if (!recycler_view.canScrollVertically(1)) {
                Dlog.d("======================");
                lastId = list.get(totalItemCount - 2).getId();
                Dlog.d(String.valueOf(lastId));
                addJS = new JSONObject();
                try {
                    addJS.put("postId", id);
                    addJS.put("type", "add");
                    addJS.put("lastId", lastId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Dlog.d(addJS.toString());
                String[] addParam = new String[3];
                addParam[0] = StartActivity.domain + "imdanggui/";
                addParam[1] = "reply_list_second.php";
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
        replySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isReply == false){

                    long now = System.currentTimeMillis();
                    if(now < StartActivity.setting.getLong("replyTime" , 0) + 30000){
                        Toast.makeText(PostDetailActivity.this, "댓글을 등록하신지 얼마 안되셨네요...",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        if(replyText.getText().toString().trim().getBytes().length <= 0){
                            Toast.makeText(PostDetailActivity.this, "내용을 입력해주세요",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            isReply = true;

                            replyMessage = replyText.getText().toString();
                            replyText.setText("");
                            JSONObject jo = new JSONObject();
                            // 댓글이 하나라도 있을경우 마지막댓글단사람의 randomid 가져오기
                            /*if(mAdapter.getItemCount() != 1){
                                String replyDevice = list.get(mAdapter.getItemCount()-1).getDevice();
                                if(replyDevice.equals(StartActivity.device)){
                                    //본인댓글바로밑에 본인이 또달경우

                                }else{
                                    try {
                                        jo.put("replyDevice" , replyDevice);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }*/

                            try {
                                jo.put("text" , replyMessage);
                                jo.put("device" , StartActivity.device);
                                jo.put("postId" , id);
                                jo.put("totalcount" , mLayoutManager.getItemCount());
                                jo.put("random" , StartActivity.setting.getString("random" , "000000"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String[] postParam = new String[2];
                            postParam[0] = StartActivity.domain + "imdanggui/send_reply.php";
                            postParam[1] = jo.toString();
                            new SendPost().execute(postParam);
                            replyMessage = "";
                            editor = StartActivity.setting.edit();
                            editor.putLong("replyTime", System.currentTimeMillis());
                            editor.commit();
                        }
                    }
                }


            }
        });

    }

    private void refreshItem(){
        JSONObject refreshJS = new JSONObject();
        try {
            refreshJS.put("postId",id);
            refreshJS.put("isPush" , isPush);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dlog.d(refreshJS.toString());
        String[] addParam = new String[3];
        addParam[0] = StartActivity.domain + "imdanggui/";
        addParam[1] = "reply_list_second.php";
        addParam[2] = refreshJS.toString();
        new ASynk().execute(addParam);

    }
    private void onItemsLoadComplete(){
        Dlog.d("=================");
        swipeRefreshLayout.setRefreshing(false);
    }
    private void configToolbar() {
        Dlog.d("========================");
        toolbar.setTitle("이전으로");
        toolbar.setTitleTextColor(Color.WHITE);
        this.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(com.imdanggui.R.drawable.left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(postId != null){
                 //iconactivity 실행
                    Intent iconIntent = new Intent(getApplicationContext() , IconTabActivity.class);
                    startActivityNoAnimation(iconIntent);
                }else{
                    intent.putExtra("reply" , header.getReply());
                    intent.putExtra("pos" , pos);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            }
        });
    }
    private List<ReplyItem> getData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ReplyItem>>(){}.getType();
        oss = gson.fromJson(js, listType);
        Dlog.d(oss.toString());
        return oss;
    }
    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //완료 후 실행할 처리 삽입
            Dlog.d("####header : " + header);
            if(js.equals("nothing")){
                Dlog.d(js);
                mAdapter = new PostDetailAdapter(header ,list);
            }else{
                mAdapter = new PostDetailAdapter(header ,list);
            }
            recycler_view.setAdapter(mAdapter);
            customDialog.cancel();
        }
    };
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
    public void onBackPressed() {
        if(postId != null){
            //iconactivity 실행
            Intent iconIntent = new Intent(getApplicationContext() , IconTabActivity.class);
            startActivityNoAnimation(iconIntent);
        }else{
            intent.putExtra("reply" , header.getReply());
            intent.putExtra("pos" , pos);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);
        }

        //super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(com.imdanggui.R.menu.menu_post_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuid = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
    private void startActivityNoAnimation(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Dlog.d("result no ok");
        if(resultCode == RESULT_OK){
            Dlog.d("result ok");
          if(requestCode == 0){
              swipeRefreshLayout.setRefreshing(true);
              refreshItem();
          }
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
            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            isReply = false;
            InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(replyText.getWindowToken(), 0);
            Toast.makeText(PostDetailActivity.this, "등록되었습니다",
                    Toast.LENGTH_SHORT).show();
            int c = header.getReply();
            header.setReply(c + 1);
            if(list == null){
                Dlog.d("#######null#######");
                mAdapter.notifyDataSetChanged();
                refreshItem();
            }else{
                Dlog.d("###else#####");
                if(list.size() <= 7){
                    Dlog.d("###55555#####");
                    mAdapter.notifyDataSetChanged();
                    refreshItem();
                }else{
                    if(js.equals("nothing")){
                        mAdapter.notifyDataSetChanged();
                    }else{
                        try {
                            list.addAll(getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //mAdapter.notifyItemInserted(totalItemCount);
                        mAdapter.notifyDataSetChanged();
                        recycler_view.smoothScrollToPosition(mLayoutManager.getItemCount()-3);

                    }
                }
            }
        }
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
            Dlog.d("######"+ js + "######");
            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String head;
            if(js.equals("nothing")){
                confirmHandler.sendEmptyMessage(0);
                onItemsLoadComplete();
            }else {
                Gson gson = new Gson();
                Type type = new TypeToken<HeaderBody>(){}.getType();
                result = gson.fromJson(js.toString(), type);
                if(postId != null){
                    Dlog.d("####postId" + String.valueOf(postId));
                    head = result.getHeader();
                    Dlog.d("###header###" + head);
                    Gson gsonhead = new Gson();
                    Type listType = new TypeToken<PostDetailHeader>(){}.getType();
                    header = gsonhead.fromJson(head, listType);

                }
                js = result.getBody();
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
            Dlog.d("=======================");
            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(js.equals("nothing")){
                Toast.makeText(PostDetailActivity.this, "불러올 댓글이 없습니다.",
                        Toast.LENGTH_SHORT).show();
            }else{
                if(mAdapter == null){
                    Dlog.d("adapter null");
                    try {
                        list = getData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter = new PostDetailAdapter(header ,list);
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
    public class Singo extends AsyncTask<String , String , String> {
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
            singoClick = false;
            if(s.equals("twice")){
                sweetAlertDialog .setTitleText("철컹철컹!")
                        .setContentText("이미 신고 접수 하셨습니다.")
                        .setConfirmText("알겠습니다.")
                        .setConfirmClickListener(null)
                        .showCancelButton(false)
                        .changeAlertType(ERROR_TYPE);
                sweetAlertDialog.show();

            }else{

                sweetAlertDialog
                        .setTitleText("철컹철컹!")
                        .setContentText("신고가 접수되었습니다.")
                        .setConfirmText("알겠습니다.")
                        .setConfirmClickListener(null)
                        .showCancelButton(false)
                        .changeAlertType(SUCCESS_TYPE);
                sweetAlertDialog.show();
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
    /**
     * base 다이얼로그
     * @return ab
     */
    private AlertDialog createDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("게시글을 신고하시겠습니까");
        //ab.setMessage("내용"); // 내용추가
        ab.setCancelable(false);
        //ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher));  // 아이콘 추가

        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                JSONObject singoJS = new JSONObject();
                try {
                    singoJS.put("postId",id);
                    singoJS.put("device" , StartActivity.device);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Dlog.d(singoJS.toString());
                String[] singoParam = new String[3];
                singoParam[0] = StartActivity.domain + "imdanggui/";
                singoParam[1] = "register_singo.php";
                singoParam[2] = singoJS.toString();
                new Singo().execute(singoParam);
            }
        });

        ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setDismiss(mDialog);
            }
        });

        return ab.create();
    }


    /**
     * Infalter 다이얼로그
     * @return ab
     */
/*    private AlertDialog createInflaterDialog() {
        final View innerView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("Title");
        ab.setView(innerView);

        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setDismiss(mDialog);
            }
        });

        ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setDismiss(mDialog);
            }
        });

        return ab.create();
    }*/

    /**
     * 다이얼로그 종료
     * @param dialog
     */
    private void setDismiss(Dialog dialog){
        if(dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}
