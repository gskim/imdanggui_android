package com.imdanggui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.util.SlidingTabLayout;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.adapter.PostingAdapter;
import com.imdanggui.model.PostingItem;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import butterknife.ButterKnife;


/**
 * Created by neokree on 16/12/14.
 */
public class FragmentPostList extends Fragment{
    private static final int HIDE_THRESHOLD = 5;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    Toolbar toolbar;
    SlidingTabLayout slidingTabLayout;
    ViewPager viewPager;

    public FragmentPostList() {
    }
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private PostingAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    String js = null;
    int totalItemCount;
    int lastId;
    int black;
    JSONObject jo;
    List<PostingItem> list;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    //SweetAlertDialog sweetAlertDialog;
    LinearLayout btnPosting;
    String php;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Dlog.d("온크리에이트");
        super.onCreate(savedInstanceState);
        ButterKnife.inject(getActivity());
        black = StartActivity.setting.getInt("black", 0);
        php = "post_total_reg.php";

        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO : 시간이 걸리는 처리 삽입
                ASynk aSynk = new ASynk();
                jo = new JSONObject();
                try {
                    jo.put("device" , StartActivity.device);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String[] param = new String[3];
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = php;
                param[2] = jo.toString();
                aSynk.execute(param);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();


    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dlog.d("온크리에이트뷰");
        View view = inflater.inflate(com.imdanggui.R.layout.fragment_postinglist, container, false);
        //btnPosting = (LinearLayout)view.findViewById(com.imdanggui.R.id.posting);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        slidingTabLayout = (SlidingTabLayout)getActivity().findViewById(R.id.tabHost);
        viewPager = (ViewPager)getActivity().findViewById(R.id.pager);

        recyclerView = (RecyclerView) view.findViewById(com.imdanggui.R.id.post_recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(com.imdanggui.R.id.swipeRefreshLayout);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //newState 정의
                /*SCROLL_STATE_FLING ( 2 ) : 터치 후 손을 뗀 상태에서 아직 스크롤 되고 있는 상태입니다.
                SCROLL_STATE_IDLE ( 0 ) : 스크롤이 종료되어 어떠한 애니메이션도 발생하지 않는 상태입니다.
                        SCROLL_STATE_TOUCH_SCROLL ( 1 ) : 스크린에 터치를 한 상태에서 스크롤하는 상태입니다.*/
                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    hideViews();
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    showViews();
                    controlsVisible = true;
                    scrolledDistance = 0;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                    scrolledDistance += dy;
                }

                totalItemCount = mLayoutManager.getItemCount();
                if (!recyclerView.canScrollVertically(1)) {

                    lastId = list.get(totalItemCount - 1).getId();

                    Dlog.d(String.valueOf("=======lastid=========="+lastId));
                    JSONObject addJS = new JSONObject();
                    try {
                        addJS.put("device", StartActivity.device);
                        addJS.put("type", "add");
                        addJS.put("lastId", lastId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    String[] addParam = new String[3];
                    addParam[0] = StartActivity.domain + "imdanggui/";
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

        return view;
    }

    private void hideViews() {
        viewPager.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        slidingTabLayout.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(3));
    }

    private void showViews() {
        viewPager.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        slidingTabLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3));
    }
    private void refreshItem(){
        JSONObject refreshJS = new JSONObject();
        try {
            refreshJS.put("device",StartActivity.device);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Dlog.d(refreshJS.toString());
        String[] addParam = new String[3];
        addParam[0] =  StartActivity.domain + "imdanggui/";
        addParam[1] = php;
        addParam[2] = refreshJS.toString();
        new ASynk().execute(addParam);

    }
    private void onItemsLoadComplete(){
        Dlog.d("=================");
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.d("##########requestcode##########" + String.valueOf(requestCode));
        refreshItem();

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            mAdapter = new PostingAdapter(list);
            recyclerView.setAdapter(mAdapter);
            //sweetAlertDialog.cancel();

        }
    };

    private List<PostingItem> getData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<PostingItem>>(){}.getType();
        List<PostingItem> oss = gson.fromJson(js, listType);
        Dlog.d(oss.toString());
        return oss;
    }
    public void onActivityResult11(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Dlog.d("=====frag result=======");
        if(requestCode == 9){
            int rep = data.getExtras().getInt("reply");
            int pos = data.getExtras().getInt("pos");
            list.get(pos).setReply_count(rep);
            mAdapter.notifyDataSetChanged();

        }else{
            swipeRefreshLayout.setRefreshing(true);
            refreshItem();
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
            Dlog.d(js);
            return js;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(js.equals("nothing")){
                Toast.makeText(getActivity(), "등록된 글이 없습니다.",
                        Toast.LENGTH_SHORT).show();
            }else{
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
            Dlog.d("=======================");
            Dlog.d(js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(js.equals("nothing")){
                Toast.makeText(getActivity(), "불러올것이 없습니다.",
                        Toast.LENGTH_SHORT).show();
                Dlog.d("##adapter count##" + String.valueOf(mAdapter.getItemCount()));

                //onItemsLoadComplete();
            }else{
                if(mAdapter == null){
                    Dlog.d("adapter null");
                    try {
                        list = getData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter = new PostingAdapter(list);
                    recyclerView.setAdapter(mAdapter);
                }else{
                    try {
                        list.addAll(getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                mAdapter.notifyDataSetChanged();
                //onItemsLoadComplete();
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
