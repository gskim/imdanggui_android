package com.imdanggui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imdanggui.IconTabActivity;
import com.imdanggui.StartActivity;
import com.imdanggui.util.CustomDialog;
import com.imdanggui.util.SlidingTabLayout;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.R;
import com.imdanggui.adapter.CategoryTabAdapter;
import com.imdanggui.model.CategoryTabItem;
import com.imdanggui.service.IconActivityService;
import com.imdanggui.util.Dlog;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.imdanggui.StartActivity.device;

public class FragmentTabCategory extends Fragment{
    private static final int HIDE_THRESHOLD = 5;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    Toolbar toolbar;
    SlidingTabLayout slidingTabLayout;
    ViewPager viewPager;

    public FragmentTabCategory() {
        Dlog.d("생성자");
    }
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private RecyclerView.Adapter mAdapterUser;
    private RecyclerView.LayoutManager mLayoutManagerUser;
    private RecyclerView.Adapter mAdapterAll;
    private RecyclerView.LayoutManager mLayoutManagerAll;
    String js = null;
    String unuserjs = null;
    public static Intent service;
    public static CustomDialog customDialog;
    RecyclerView recyclerViewUser;
    RecyclerView recyclerViewAll;
    List<CategoryTabItem> list;
    List<CategoryTabItem> unUserlist;
    LinearLayout rl1;
    LinearLayout rl2;
    TextView tv1;
    TextView tv2;

    @Override
    public void onResume() {
        super.onResume();
        Dlog.d(("###########resume#########"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Dlog.d("온크리에이트");
        super.onCreate(savedInstanceState);
        ButterKnife.inject(getActivity());
        service = new Intent(getActivity() , IconActivityService.class);
        getActivity().startService(service);
        WindowManager w = getActivity().getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        customDialog = new CustomDialog(getActivity());
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.setCancelable(false);
        customDialog.show();

        //Thread 사용은 선택이 아니라 필수
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO : 시간이 걸리는 처리 삽입
                ASynk aSynk = new ASynk();
                ASynkUnuser aSynkUnuser = new ASynkUnuser();
                String[] param = new String[3];
                String[] unuserParam = new String[3];
                JSONObject jo = new JSONObject();
                try {
                    jo.put("device" , device);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = "user_category.php";
                param[2] = jo.toString();
                aSynk.execute(param);
                unuserParam[0] = StartActivity.domain +  "imdanggui/";
                unuserParam[1] = "unuser_category.php";
                unuserParam[2] = jo.toString();
                aSynkUnuser.execute(unuserParam);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Dlog.d("온크리에이트뷰");
        View view = inflater.inflate(R.layout.tab_category, container, false);
        final TabHost tabHost = (TabHost)view.findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab1").setContent(R.id.tab1).setIndicator(getString(R.string.tab1));
        tabHost.addTab(spec1);
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab2").setContent(R.id.tab2).setIndicator(getString(R.string.tab2));
        tabHost.addTab(spec2);
        tabHost.getTabWidget().setStripEnabled(false);

        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        rl1 = (LinearLayout) tabHost.getTabWidget().getChildAt(0);
        rl1.setGravity(Gravity.CENTER_VERTICAL);
        rl1.setBackgroundResource(R.drawable.tabs_bg_selector_left);

        tv1 = (TextView) rl1.getChildAt(1);
        tv1.setLayoutParams(tvParams);
        tv1.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
        tv1.setPadding(10, 0, 10, 0);
        tv1.setGravity(Gravity.CENTER);
        tv1.setBackgroundResource(R.drawable.tabs_text_selector);
        tv1.setTextColor(getResources().getColor(R.color.selected_tab_icon));
        tv1.setTextSize(16);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);

        rl2 = (LinearLayout) tabHost.getTabWidget().getChildAt(1);
        rl2.setGravity(Gravity.CENTER_VERTICAL);
        rl2.setBackgroundResource(R.drawable.tabs_bg_selector_right);
        tv2 = (TextView) rl2.getChildAt(1);
        tv2.setLayoutParams(tvParams);
        tv2.setTextAppearance(getActivity(), android.R.style.TextAppearance_DeviceDefault_Medium);
        tv2.setPadding(10, 0, 10, 0);
        tv2.setGravity(Gravity.CENTER);
        tv2.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
        tv2.setTextSize(16);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);

        recyclerViewUser = (RecyclerView) view.findViewById(R.id.user_recycler_view);
        recyclerViewAll = (RecyclerView) view.findViewById(R.id.all_recycler_view);
        recyclerViewUser.setHasFixedSize(true);
        mLayoutManagerUser = new GridLayoutManager(getActivity() , 3);
        recyclerViewUser.setLayoutManager(mLayoutManagerUser);
        recyclerViewAll.setHasFixedSize(true);
        mLayoutManagerAll = new GridLayoutManager(getActivity() , 3);
        recyclerViewAll.setLayoutManager(mLayoutManagerAll);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        slidingTabLayout = (SlidingTabLayout)getActivity().findViewById(R.id.tabHost);
        viewPager = (ViewPager)getActivity().findViewById(R.id.pager);
        recyclerViewUser.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    hideViews();
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    showViews();
                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                    scrolledDistance += dy;
                }
            }
        });

        recyclerViewAll.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
            }
        });
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabId.equals("Tab1")){
                    tv1.setTextColor(getResources().getColor(R.color.selected_tab_icon));
                    //recyclerViewUser.setBackgroundResource(R.color.tab_back_selected);
                    tv2.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
                    //recyclerViewAll.setBackgroundResource(R.color.tab_back_unselected);
                }else{
                    tv2.setTextColor(getResources().getColor(R.color.selected_tab_icon));
                    //recyclerViewAll.setBackgroundResource(R.color.tab_back_selected);
                    tv1.setTextColor(getResources().getColor(R.color.unselect_tab_icon));
                    //recyclerViewUser.setBackgroundResource(R.color.tab_back_unselected);
                }

            }
        });
        return view;
    }


    private Handler confirmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //완료 후 실행할 처리 삽입
            mAdapterUser = new CategoryTabAdapter(list , true);
            mAdapterAll = new CategoryTabAdapter(unUserlist , false);

            recyclerViewUser.setAdapter(mAdapterUser);
            recyclerViewAll.setAdapter(mAdapterAll);
            mAdapterAll.notifyDataSetChanged();
            mAdapterUser.notifyDataSetChanged();

        }
    };

    private void hideViews() {
        slidingTabLayout.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(4));
        viewPager.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(4));
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(5));
    }

    private void showViews() {
        slidingTabLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(4));
        viewPager.animate().translationY(0).setInterpolator(new DecelerateInterpolator(4));
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(5));
    }
    public void onActivityResult11(int requestCode, int resultCode, Intent data) {
        getActivity().startService(service);
        refreshList();
    }
    public void refreshList(){
        customDialog = new CustomDialog(getActivity());
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.setCancelable(false);
        customDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] param = new String[3];
                String[] unuserParam = new String[3];
                JSONObject jo = new JSONObject();
                try {
                    jo.put("device" , device);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                param[0] = StartActivity.domain + "imdanggui/";
                param[1] = "user_category.php";
                param[2] = jo.toString();
                new ASynk().execute(param);
                unuserParam[0] = StartActivity.domain + "imdanggui/";
                unuserParam[1] = "unuser_category.php";
                unuserParam[2] = jo.toString();
                new ASynkUnuser().execute(unuserParam);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private List<CategoryTabItem> getData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CategoryTabItem>>(){}.getType();
        List<CategoryTabItem> oss = gson.fromJson(js, listType);
        Dlog.d(oss.toString());
        return oss;
    }
    private List<CategoryTabItem> getUnuserData() throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CategoryTabItem>>(){}.getType();
        List<CategoryTabItem> oss = gson.fromJson(unuserjs, listType);
        Dlog.d(oss.toString());
        return oss;
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
            Dlog.d("JS============"+js);
            return js;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
                try {
                    list = getData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    public class ASynkUnuser extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {
            String url = params[0];
            String urlPlus = params[1];
            String json = params[2];

            try {
                unuserjs = post(url + urlPlus, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Dlog.d(unuserjs);
            return unuserjs;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("nothing")){

            }else{
                try {
                    unUserlist = getUnuserData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
