package com.imdanggui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PwdActivity extends AppCompatActivity {

    @InjectView(com.imdanggui.R.id.one)
    TextView one;
    @InjectView(com.imdanggui.R.id.two)
    TextView two;
    @InjectView(com.imdanggui.R.id.three)
    TextView three;
    @InjectView(com.imdanggui.R.id.four)
    TextView four;
    @InjectView(com.imdanggui.R.id.five)
    TextView five;
    @InjectView(com.imdanggui.R.id.six)
    TextView six;
    @InjectView(com.imdanggui.R.id.seven)
    TextView seven;
    @InjectView(com.imdanggui.R.id.eight)
    TextView eight;
    @InjectView(com.imdanggui.R.id.nine)
    TextView nine;
    @InjectView(com.imdanggui.R.id.zero)
    TextView zero;
    @InjectView(com.imdanggui.R.id.delete)
    TextView delete;
    @InjectView(com.imdanggui.R.id.pwd1)
    ImageView img1;
    @InjectView(com.imdanggui.R.id.pwd2)
    ImageView img2;
    @InjectView(com.imdanggui.R.id.pwd3)
    ImageView img3;
    @InjectView(com.imdanggui.R.id.pwd4)
    ImageView img4;
    @InjectView(com.imdanggui.R.id.tv_title)
    TextView title;
    boolean pwd1 = false;
    boolean pwd2 = false;
    boolean pwd3 = false;
    boolean pwd4 = false;
    boolean isFirst = true;
    boolean isSetting;
    SharedPreferences.Editor editor;
    String password = null;
    String password2 = null;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.imdanggui.R.layout.activity_pwd);
        ButterKnife.inject(this);
        intent = getIntent();
        isSetting = intent.getExtras().getBoolean("setting");

        editor = StartActivity.setting.edit();
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("1");
            }
        });
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("2");
            }
        });
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("3");
            }
        });
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("4");
            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("5");
            }
        });
        six.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("6");
            }
        });
        seven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("7");
            }
        });
        eight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("8");
            }
        });
        nine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("9");
            }
        });
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPwd("0");
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClick();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED , intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);

    }

    public void checkPwd(String num){
        if(isSetting){
            if(pwd1 == false){
                //첫글자삽입
                pwd1 = true;
                img1.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));
                if(isFirst== true){
                    password = num;
                }else{
                    password2 = num;
                }


            }else if(pwd2 == false){
                //두번째글자 삽입
                pwd2 = true;
                img2.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));
                if(isFirst == true){
                    password = password + num;
                }else{
                    password2 = password2 + num;
                }


            }else if(pwd3 == false){
                //세번째 글자 삽입
                pwd3 = true;
                img3.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));

                if(isFirst == true){
                    password = password + num;
                }else{
                    password2 = password2 + num;
                }

            }else {
                img4.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));
                if(isFirst == true){
                    password = password + num;
                }else{
                    password2 = password2 + num;
                }
                //비번 모두 입력됨 한번더 입력받도록 화면 textview , imageview change
                //만약 두번째 입력된거면 이전입력비번과 동일한지 체크하고 맞으면 저장후 finish 틀리면 리셋하고 다시 입력받도록 하고 toast 띄우기
                if(isFirst == false){
                    //두번째 비번세팅
                    if(password.equals(password2)){
                        editor.putString("passworduse" , "yes");
                        editor.putString("password" , password);
                        editor.commit();
                        setResult(RESULT_OK, intent);
                        finish();
                    }else{
                        //첫비번 두번째 비번이 다를때
                        pwd1  = false;
                        pwd2  = false;
                        pwd3  = false;
                        password2 = null;
                        //이미지뷰 리셋
                        img1.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                        img2.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                        img3.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                        img4.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                        //토스트 띄우기
                        Toast.makeText(PwdActivity.this, "다시 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    }
                }else{
                    isFirst = false;
                    //첫번째 비번세팅
                    pwd1  = false;
                    pwd2  = false;
                    pwd3  = false;
                    //이미지 리셋
                    img1.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    img2.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    img3.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    img4.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    //텍스트뷰 변경
                    title.setText("비밀번호를 한번더 입력해주세요");
                }

            }
        }else{
            //등록된 비번 확인하러 체크
            if(pwd1 == false){
                pwd1 = true;
                img1.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));
                password = num;

            }else if(pwd2 == false){
                pwd2 = true;
                img2.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));
                password = password + num;

            }else if(pwd3 == false){
                pwd3 = true;
                img3.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));
                password = password + num;
            }else{
                pwd4 = true;
                img4.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_after));
                password = password + num;
                String pwd = StartActivity.setting.getString("password" , null);

                if( pwd.equals(password) ){
                    Intent intent = new Intent( this ,SettingActivity.class);
                    startActivityForResult(intent, 6);
                    //getActivity().startActivityForResult(intent , 0);
                    this.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    finish();
                }else{
                    pwd1  = false;
                    pwd2  = false;
                    pwd3  = false;
                    //이미지 리셋
                    img1.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    img2.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    img3.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    img4.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
                    Toast.makeText(PwdActivity.this, "틀렸습니다...",
                            Toast.LENGTH_SHORT).show();
                }


            }

        }
    }
    public void deleteClick(){
        if(pwd3 == true){
            pwd3 = false;
            img3.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
            if(isFirst == true){
                password = password.substring(0 , 2);
            }else{
                password2 = password2.substring(0 , 2);
            }
        }else if(pwd2 == true){
            pwd2 = false;
            img2.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
            if(isFirst == true){
                password = password.substring(0 , 1);
            }else{
                password2 = password2.substring(0 , 1);
            }
        }else if(pwd1 == true){
            pwd1 = false;
            img1.setBackground(getResources().getDrawable(com.imdanggui.R.drawable.lock_before));
            if(isFirst == true){
                password = null;
            }else{
                password2 = null;
            }
        }
    }
}
