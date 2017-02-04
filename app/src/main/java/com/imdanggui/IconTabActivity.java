package com.imdanggui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.imdanggui.fragment.FragmentTabCategory;
import com.imdanggui.util.BackPressCloseHandler;
import com.imdanggui.util.Dlog;
import com.imdanggui.util.SlidingTabLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

/**
 * Created by neokree on 30/12/14.
 */
public class IconTabActivity extends AppCompatActivity  {

    SharedPreferences.Editor editor;

    int black;
    private ViewPager pager;
    private ViewPagerAdapter pagerAdapter;

    private Resources res;
    FragmentPostList fragmentPostList = new FragmentPostList();
    FragmentNewList fragmentNewList = new FragmentNewList();
    FragmentTabCategory fragmentTabCategory = new FragmentTabCategory();
    private BackPressCloseHandler backPressCloseHandler;
    SlidingTabLayout slidingTabLayout;
    ImageView tab1;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.posting)
    LinearLayout btnPosting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_icons);
        ButterKnife.inject(this);
        res = this.getResources();
        black = StartActivity.setting.getInt("black", 0);
        toolbar.setTitle("");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.findViewById(R.id.top_logo).setVisibility(View.VISIBLE);
        this.setSupportActionBar(toolbar);

        backPressCloseHandler = new BackPressCloseHandler(this);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabHost);
        pager = (ViewPager) this.findViewById(com.imdanggui.R.id.pager);
        // init view pager
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);
        //int w = getLcdSIzeWidth();
        int w = getSizeWidth();

        //slidingTabLayout.setMinimumWidth((int) density * 90);
        slidingTabLayout.setCustomTabView(R.layout.item_tab1, R.string.kakao_app_key,  w / 3);

        tab1 = (ImageView)slidingTabLayout.getChildAt(0).findViewById(R.id.tab1);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.selected_tab_icon);
            }
        });
        slidingTabLayout.setViewPager(pager);

        //2번째 페이지 호출
        pager.setCurrentItem(1);

        btnPosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();

                if (black >= 3) {
                    Toast.makeText(IconTabActivity.this, "다수의 신고로 인해 글을 등록하실 수 없습니다.",
                            Toast.LENGTH_SHORT).show();
                } else if (now < StartActivity.setting.getLong("postTime", 0) + 30000) {
                    Toast.makeText(IconTabActivity.this, "글을 등록하신지 얼마 안되셨네요...",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(IconTabActivity.this, PostActivity.class);
                    intent.putExtra("category", "all");
                    startActivityForResult(intent, 0);
                    //getActivity().startActivityForResult(intent , 0);
                    overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Dlog.d("=====CreateOptionMenu===============");
        getMenuInflater().inflate(com.imdanggui.R.menu.main, menu);
        return true;
    }
    public int getSizeWidth(){
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
    public int getLcdSIzeWidth() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    public int getLcdSIzeHeight() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }
    private void hideViews() {
        btnPosting.animate().translationY(-btnPosting.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    private void showViews() {
        btnPosting.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == com.imdanggui.R.id.setting){
            String passwordUse = StartActivity.setting.getString("passworduse", "no");
            if (passwordUse.equals("no")) {
                Intent intent = new Intent(IconTabActivity.this, SettingActivity.class);
                startActivityForResult(intent, 6);
                //getActivity().startActivityForResult(intent , 0);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            } else {
                Intent intent = new Intent(IconTabActivity.this, PwdActivity.class);
                intent.putExtra("setting", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, 0);
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private Typeface typeface = null;
    private static final String TYPEFACE_NAME = "fonts/dotum.ttf";
    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID, null);
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
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.d(String.valueOf("##requestCode##"+requestCode));
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                Dlog.d("====result ok===");
                fragmentPostList.onActivityResult11(requestCode, resultCode, data);
            }
        }else if (requestCode == 3){
            //카테고리 액티비티 카테고리 추가 /삭제로 화면 떳다가 꺼질때
            if(resultCode == RESULT_OK){
                Dlog.d("====result ok  request code 3===");
                fragmentTabCategory.onActivityResult11(requestCode , resultCode , data);
                fragmentPostList.onActivityResult11(requestCode, resultCode, data);
                fragmentNewList.onActivityResult11(requestCode , resultCode , data);
            }
        }else if( requestCode == 4){
            //category detail 액티비티 종료시
            if(resultCode == RESULT_OK){
                String change = data.getExtras().getString("addRemove");
                if(change.equals("yes")){
                    fragmentTabCategory.onActivityResult11(requestCode, resultCode, data);
                    fragmentPostList.onActivityResult11(requestCode, resultCode, data);
                    fragmentNewList.onActivityResult11(requestCode , resultCode , data);
                }
            }
        }else if(requestCode == 6){
            if(resultCode == RESULT_OK){
                finish();
            }
        }else if(requestCode == 9){
            if(pager.getCurrentItem() == 2){
                if (resultCode == RESULT_OK){
                    fragmentPostList.onActivityResult11(requestCode, resultCode, data);
                }
            }else if(pager.getCurrentItem() == 1){
                fragmentNewList.onActivityResult11(requestCode, resultCode, data);
            }
            //포스트 신규정렬 리스트에서 실행후
            //포스트디테일화면 종료시

        }
    }



   private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int num) {
            Dlog.d("-==================");

            switch(num){
                case 0:
                    hideViews();
                    return fragmentTabCategory = new FragmentTabCategory();
                case 1:
                    showViews();
                    return fragmentNewList = new FragmentNewList();
                case 2:
                    showViews();
                    return fragmentPostList =  new FragmentPostList();
                default:
                    return new FragmentPostList();
            }
        }
        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0: return "tab 11111";
                case 1: return "tab 22222";
                case 2: return "tab 33333";
                default: return null;
            }
        }


   }
}