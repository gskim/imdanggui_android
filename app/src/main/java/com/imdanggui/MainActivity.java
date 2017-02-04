package com.imdanggui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity {

    private static final String TYPEFACE_NAME = "fonts/dotum.ttf";
    private Typeface typeface = null;
    TextView agreeBtn;
    SharedPreferences.Editor editor;
    boolean agr1 = false;
    boolean agr2  = false;
    @InjectView(com.imdanggui.R.id.agr_img1)
    ImageButton agrImg1;
    @InjectView(com.imdanggui.R.id.agr_img2)
    ImageButton agrImg2;
    @InjectView(R.id.agr_tv1)
    TextView agrtv1;
    @InjectView(R.id.agr_tv2)
    TextView agrtv2;
    String agr1Txt;
    String agr2Txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTypeface();
        setContentView(com.imdanggui.R.layout.activity_main);
        ButterKnife.inject(this);
        StartActivity sa = (StartActivity)StartActivity.startactivity;
        sa.finish();
        editor = StartActivity.setting.edit();
        try {
            agr1Txt = readText("agree1.txt");
            agr2Txt = readText("agree2.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView agree1 = (TextView)findViewById(com.imdanggui.R.id.agree1);
        agree1.setText(agr1Txt);
        agree1.setVerticalScrollBarEnabled(true);
        agree1.setMovementMethod(new ScrollingMovementMethod());

        TextView agree2 = (TextView)findViewById(com.imdanggui.R.id.agree2);
        agree2.setText(agr1Txt);
        agree2.setVerticalScrollBarEnabled(true);
        agree2.setMovementMethod(new ScrollingMovementMethod());
        agrtv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (agr1 == false) {
                    agrImg1.setBackground(getResources().getDrawable(R.drawable.check_2));
                    agr1 = true;
                } else {
                    agrImg1.setBackground(getResources().getDrawable(R.drawable.check_1));
                    agr1 = false;
                }
            }
        });
        agrtv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(agr2 == false){
                    agrImg2.setBackground(getResources().getDrawable(R.drawable.check_2));
                    agr2 = true;
                }else{
                    agrImg2.setBackground(getResources().getDrawable(R.drawable.check_1));
                    agr2 = false;
                }
            }
        });

        agrImg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (agr1 == false) {
                    agrImg1.setBackground(getResources().getDrawable(R.drawable.check_2));
                    agr1 = true;
                } else {
                    agrImg1.setBackground(getResources().getDrawable(R.drawable.check_1));
                    agr1 = false;
                }
            }
        });
        agrImg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(agr2 == false){
                    agrImg2.setBackground(getResources().getDrawable(R.drawable.check_2));
                    agr2 = true;
                }else{
                    agrImg2.setBackground(getResources().getDrawable(R.drawable.check_1));
                    agr2 = false;
                }
            }
        });

        agreeBtn = (TextView)findViewById(com.imdanggui.R.id.agree_btn);
        agreeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        agreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (agr1 == true && agr2 == true) {
                    editor.putString("agree1", "yes");
                    editor.putString("agree2", "yes");
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
                    startActivityNoAnimation(intent);
                } else {
                    //동의 토스트 띄우기
                    Toast.makeText(MainActivity.this, "이용약관에 동의해주세요.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID , null);
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
    private String readText(String file) throws IOException {
        InputStream is = getAssets().open(file);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String text = new String(buffer);

        return text;
    }

    @Override
    protected void onDestroy() {
        //setResult(RESULT_OK);
        super.onDestroy();
    }
    private void startActivityNoAnimation(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("parent" , "main");
        startActivity(intent);
        finish();
    }

}
