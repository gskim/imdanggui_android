package com.imdanggui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.imdanggui.util.Dlog;
import java.io.IOException;
import java.io.InputStream;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class AgreeActivity extends AppCompatActivity {
    private String TYPEFACE_NAME = "fonts/dotum.ttf";
    private Typeface typeface = null;
    private String agreeTxt;
    @InjectView(com.imdanggui.R.id.small_tv)
    TextView small;
    @InjectView(com.imdanggui.R.id.big_tv)
    TextView big;
    @InjectView(com.imdanggui.R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_agree);
        ButterKnife.inject(this);
        configToolbar();
        Intent intent = getIntent();
        int type = intent.getExtras().getInt("type");
        String fileName;
        if(type == 1){
            fileName = "agree1.txt";
            small.setText("이용 약관");
        }else{
            fileName = "agree2.txt";
            small.setText("개인정보 보호 정책");
        }

        try {
            agreeTxt = readText(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        big.setText(agreeTxt);
        big.setVerticalScrollBarEnabled(true);
        big.setMovementMethod(new ScrollingMovementMethod());
    }

    private String readText(String file) throws IOException{
        InputStream is = getAssets().open(file);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String text = new String(buffer);

        return text;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID, null);
        ViewGroup group = (ViewGroup)view;
        int childCnt = group.getChildCount();
        for (int i = 0 ; i < childCnt ; i ++){
            View v = group.getChildAt(i);
            if( v instanceof TextView ){
                ((TextView)v).setTypeface(typeface);
            }
        }
        super.setContentView(view);
    }

    private void loadTypeface(){
        if(typeface==null)
            typeface = Typeface.createFromAsset(getAssets(), TYPEFACE_NAME);
    }
    private void configToolbar() {
        Dlog.d("========================");
        toolbar.setTitle("약관 및 정책");
        toolbar.setTitleTextColor(Color.WHITE);
        this.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(com.imdanggui.R.drawable.left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

    }

}
