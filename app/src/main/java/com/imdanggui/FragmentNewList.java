package com.imdanggui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.util.SlidingTabLayout;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.adapter.PostingAdapter;
import com.imdanggui.fragment.FragmentTabCategory;
import com.imdanggui.model.PostingItem;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;


/**
 * Created by neokree on 16/12/14.
 */
public class FragmentNewList extends Fragment{
    private static final int HIDE_THRESHOLD = 5;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    Toolbar toolbar;
    SlidingTabLayout slidingTabLayout;
    ViewPager viewPager;


    int scrollCnt = 0;
    public FragmentNewList() {
        Dlog.d("생성자");
    }
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private PostingAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    String js = null;
    int totalItemCount;
    int lastId;
    JSONObject jo;
    List<PostingItem> list;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    private Button btnClosePopup;
    PopupWindow pwindo;
    SharedPreferences.Editor editor;
    private int mWidthPixels , mHeightPixels;

    LinearLayout btnPosting;

    int black;
    SweetAlertDialog sweetAlertDialog;
    String php;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Dlog.d("온크리에이트");
        Fresco.initialize(getActivity());
        super.onCreate(savedInstanceState);
        ButterKnife.inject(getActivity());
        black = StartActivity.setting.getInt("black", 0);
        php = "post_total_reply.php";
        WindowManager w = getActivity().getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        //since SDK_INT = 1;
        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;
        //상태바와 메뉴바의 크기를 포함해서 재계산
        if(Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17){
            Dlog.d("##############17이하");
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }else if(Build.VERSION.SDK_INT >= 17){
            Dlog.d("##############17이상");
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d , realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        mWidthPixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, metrics);
        mHeightPixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, metrics);



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
                Dlog.d(js);

            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.d("##########requestcode##########" + String.valueOf(requestCode));
        refreshItem();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dlog.d("온크리에이트뷰");
        View view = inflater.inflate(com.imdanggui.R.layout.fragment_postinglist, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        slidingTabLayout = (SlidingTabLayout)getActivity().findViewById(R.id.tabHost);
        viewPager = (ViewPager)getActivity().findViewById(R.id.pager);
        /*btnPosting = (LinearLayout)view.findViewById(com.imdanggui.R.id.posting);
        btnPosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();

                if(black >= 3) {
                    Toast.makeText(getActivity(), "다수의 신고로 인해 글을 등록하실 수 없습니다.",
                            Toast.LENGTH_SHORT).show();
                }else if(now < StartActivity.setting.getLong("postTime" , 0) + 60000){
                    Toast.makeText(getActivity(), "글을 등록하신지 얼마 안되셨네요...",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent( getActivity() ,PostActivity.class);
                    intent.putExtra("category" , "all");
                    startActivityForResult(intent, 0);
                    //getActivity().startActivityForResult(intent , 0);
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);
                }
            }
        });
*/


        recyclerView = (RecyclerView) view.findViewById(com.imdanggui.R.id.post_recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(com.imdanggui.R.id.swipeRefreshLayout);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
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
                Dlog.d("totalcount" + String.valueOf(totalItemCount));
                if (!recyclerView.canScrollVertically(1)) {
                    totalItemCount = mLayoutManager.getItemCount();
                    Dlog.d("======================");
                    Dlog.d("##totalCnt### " + String.valueOf(totalItemCount) + (" #####"));
                    Dlog.d("##scrollCnt### " + String.valueOf(scrollCnt) + (" #####"));

                    lastId = list.get(totalItemCount - 1).getId();

                    Dlog.d(String.valueOf("##upload Cnt### : " + String.valueOf(totalItemCount - 1 - scrollCnt)));
                    JSONObject addJS = new JSONObject();
                    try {
                        addJS.put("device", StartActivity.device);
                        addJS.put("type", "add");
                        addJS.put("lastId", lastId );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    scrollCnt++;
                    Dlog.d(addJS.toString());
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
                Dlog.d("=================");
                refreshItem();
            }
        });

        return view;
    }

    private void hideViews() {
        slidingTabLayout.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        viewPager.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(3));
    }

    private void showViews() {
        slidingTabLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        viewPager.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3));
    }
    private void refreshItem(){
        scrollCnt = 0;
        JSONObject refreshJS = new JSONObject();
        try {
            refreshJS.put("device",StartActivity.device);
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
    private void initiatePopupWindow(){
        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(com.imdanggui.R.layout.popup_main, (ViewGroup) getActivity().findViewById(com.imdanggui.R.id.popup_element));
            SimpleDraweeView popup_img = (SimpleDraweeView)layout.findViewById(com.imdanggui.R.id.img_popup);
            String imageBaseUrl = StartActivity.domain + "img/popup/";
            Uri uri = Uri.parse( imageBaseUrl + StartActivity.setting.getString("popupImage" , null));
            Dlog.d("####" + uri +"####");
            popup_img.setImageURI(uri);
            pwindo = new PopupWindow(layout , mWidthPixels , mHeightPixels , true);
            pwindo.setAnimationStyle(0);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

            btnClosePopup = (Button)layout.findViewById(com.imdanggui.R.id.btn_clost_popup);
            btnClosePopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor = StartActivity.setting.edit();
                    editor.putBoolean("popup" , false);
                    editor.commit();
                    pwindo.dismiss();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //완료 후 실행할 처리 삽입
            mAdapter = new PostingAdapter(list);
            recyclerView.setAdapter(mAdapter);
            //sweetAlertDialog.cancel();
            getActivity().stopService(FragmentTabCategory.service);
            StartActivity.setting = getActivity().getSharedPreferences("setting" , 0);
            Boolean open =StartActivity.setting.getBoolean("popup", false);
            Boolean version = StartActivity.setting.getBoolean("version", false);
            if(version == false){
                //마켓으로~
                sweetAlertDialog = new SweetAlertDialog(getActivity() ,WARNING_TYPE);
                sweetAlertDialog.setTitleText("새버전 출시")
                        .setContentText("업그레이드를 위해 마켓으로 이동하겠습니다.")
                        .setConfirmText("네")
                        .showCancelButton(false).show();
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                        Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                        marketLaunch.setData(Uri.parse("market://details?id=com.imdanggui") );
                        startActivity(marketLaunch);
                        getActivity().finish();
                    }
                });
            }
            if(open == true && version == true){
                initiatePopupWindow();
            }



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
                scrollCnt--;
                Toast.makeText(getActivity(), "불러올것이 없습니다.",
                        Toast.LENGTH_SHORT).show();

                //onItemsLoadComplete();
            }else{
                if(mAdapter == null){
                    Dlog.d("adapter null");
                    try {
                        list = getData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    /*mAdapter = new PostingAdapter(list);
                    recyclerView.setAdapter(mAdapter);*/
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
