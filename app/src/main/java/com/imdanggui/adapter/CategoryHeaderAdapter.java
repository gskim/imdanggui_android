package com.imdanggui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kakao.AppActionBuilder;
import com.kakao.AppActionInfoBuilder;
import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;
import com.kakao.KakaoTalkLinkMessageBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.CategoryDetailActivity;
import com.imdanggui.CategoryPostActivity;
import com.imdanggui.PostDetailActivity;
import com.imdanggui.R;
import com.imdanggui.StartActivity;
import com.imdanggui.model.CategoryDetailHeader;
import com.imdanggui.model.PostingItem;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE;
import static cn.pedant.SweetAlert.SweetAlertDialog.SUCCESS_TYPE;
import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

/**
 * Created by user on 2015-09-10.
 */
public class CategoryHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");
    OkHttpClient client = new OkHttpClient();
    String singoJS;
    String result;
    JSONObject jsonObject;
    VHHeader VHheader;
    CategoryDetailHeader header;
    List<PostingItem> postingItems;
    boolean singoClick = false;
    SweetAlertDialog sweetAlertDialog;
    private KakaoLink kakaoLink;
    private KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public static boolean foot = false;

    public CategoryHeaderAdapter( CategoryDetailHeader header,  List<PostingItem> postingItems) {
        this.postingItems = postingItems;
        this.header = header;

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Dlog.d("########"+String.valueOf(foot));
        Dlog.d(String.valueOf("=====viewType======"+viewType));
        Dlog.d("==============create==============");
        if(viewType == TYPE_HEADER)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_category_posting, parent, false);
            DisplayMetrics dm = parent.getContext().getResources().getDisplayMetrics();
            int heightPixels = dm.heightPixels;
            Dlog.d("#####height pixels### : " + String.valueOf(heightPixels));
            //statebar 높이 25dp , toolbar 높이 55dp
            int toolbarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP , 80 , parent.getContext().getResources().getDisplayMetrics());
            v.setMinimumHeight(heightPixels - toolbarHeight);
            return  new VHHeader(v);
        }
        else if(viewType == TYPE_ITEM)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_posting, parent, false);
            return new ViewHolder(v);
        }
        else if(viewType == TYPE_FOOTER){
            Dlog.d("=======footer==========");
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_footer, parent, false);
            return new VHFooter(v);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof VHHeader)
        {
            Dlog.d("=========bind============");
            VHheader = (VHHeader)holder;
            String name = header.getName();
            String thumb = header.getThumb();
            int ranking = header.getRanking();


            if(header.getFavorite().equals("remove")){
                VHheader.favorite.setText("관심 임당귀에서 제거");
            }
            VHheader.favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    jsonObject = new JSONObject();
                    try {
                        jsonObject.put("categoryId" , header.getId());
                        jsonObject.put("device" , StartActivity.device);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(header.getFavorite().equals("remove")){
                        try {
                            jsonObject.put("addRemove" , "remove");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        VHheader.favorite.setText("관심 임당귀에 추가");
                        header.setFavorite("add");
                    }else{
                        try {
                            jsonObject.put("addRemove" , "add");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        VHheader.favorite.setText("관심 임당귀에서 제거");
                        header.setFavorite("remove");
                    }
                    if(CategoryDetailActivity.change.equals("no")){
                        CategoryDetailActivity.change = "yes";
                    }else{
                        CategoryDetailActivity.change = "no";
                    }
                    String[] param = new String[3];
                    param[0] = StartActivity.domain+"imdanggui/";
                    param[1] = "category_update.php";
                    param[2] = jsonObject.toString();
                    new ASynk().execute(param);

                    Toast.makeText(v.getContext(), "반영되었습니다.",
                            Toast.LENGTH_SHORT).show();
                }
            });
            String imageBaseUrl = StartActivity.domain+"img/category/";
            Uri uri = Uri.parse(imageBaseUrl + thumb);
            SimpleDraweeView draweeView = (SimpleDraweeView) VHheader.simpleDraweeView;
            draweeView.setImageURI(uri);
            VHheader.name.setText(name);
            VHheader.ranking.setText(ranking + " 위");

            VHheader.yesterdayPost.setText(header.getYesterdayPost() + " 개");
            VHheader.yesterdayReply.setText(header.getYesterdayReply() + " 개");

            VHheader.kakao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        String message = "관심사별 익명 게시판 \n" +
                                "임금님 귀는 당나귀 귀!! \n" +
                                "[임당귀]";
                        kakaoLink = KakaoLink.getKakaoLink(v.getContext());
                        kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

                        kakaoTalkLinkMessageBuilder.addAppButton("임당귀로 이동",
                                new AppActionBuilder()
                        .setAndroidExecuteURLParam("market://details?id=com.imdanggui").build());
                        kakaoTalkLinkMessageBuilder.addText(message);
                        kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder.build(), v.getContext());
                        kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

                    } catch (KakaoParameterException e) {
                        Log.e("error", e.getMessage());
                    }
                }
            });
            VHheader.all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent goAll = new Intent(v.getContext(), CategoryPostActivity.class);
                    goAll.putExtra("id", header.getId());
                    goAll.putExtra("name", header.getName());

                    Activity activity = (Activity) v.getContext();
                    activity.startActivityForResult(goAll, 6);
                    activity.overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);
                }
            });


        }
        else if(holder instanceof ViewHolder)
        {
            final PostingItem currentItem = getItem(position - 1 );
            final ViewHolder VHitem = (ViewHolder)holder;

            VHitem.textView.setText(currentItem.getText());
            VHitem.user.setText(currentItem.getNickname());
            VHitem.regdate.setText(currentItem.getRegdate());
            VHitem.reply.setText( String.valueOf(currentItem.getReply_count()));
            VHitem.random.setText("["+ currentItem.getRandom() +"]");
            VHitem.category.setText(currentItem.getCategory() + "에서");
            if(position % 2 == 0){
                VHitem.post_item.setBackgroundResource(R.color.post2);
            }else{
                VHitem.post_item.setBackgroundResource(R.color.post1);
            }
            VHitem.post_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    sweetAlertDialog = new SweetAlertDialog(v.getContext() ,WARNING_TYPE);
                    sweetAlertDialog.setTitleText("철컹철컹?")
                            .setContentText("게시글을 신고 하시겠습니까?")
                            .setCancelText("아니요!")
                            .setConfirmText("네 신고합니다!")
                            .showCancelButton(true).show();
                    sweetAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                        }
                    });
                    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                            if(singoClick == false){
                                singoClick = true;
                                JSONObject singoJS = new JSONObject();
                                try {
                                    singoJS.put("postId", currentItem.getId());
                                    singoJS.put("device", StartActivity.device);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Dlog.d(singoJS.toString());
                                String[] singoParam = new String[3];
                                singoParam[0] = StartActivity.domain+"imdanggui/";
                                singoParam[1] = "register_singo.php";
                                singoParam[2] = singoJS.toString();
                                new Singo().execute(singoParam);
                            }else{

                            }

                        }
                    });
                    return true;
                }
            });

            VHitem.post_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //포스트 리스트 클릭
                    Intent intent = new Intent(v.getContext() , PostDetailActivity.class);
                    intent.putExtra("pos" ,position);
                    intent.putExtra("id", postingItems.get(position - 1).getId());
                    intent.putExtra("singo" , postingItems.get(position - 1).getSingo());
                    intent.putExtra("text" , postingItems.get(position - 1).getText());
                    intent.putExtra("nickname" , postingItems.get(position - 1).getNickname());
                    intent.putExtra("regdate", postingItems.get(position - 1).getRegdate());
                    intent.putExtra("random" , postingItems.get(position - 1).getRandom());
                    intent.putExtra("category" , postingItems.get(position - 1).getCategory());
                    intent.putExtra("reply" , postingItems.get(position - 1).getReply_count());
                    //v.getContext().startActivity(intent);
                    Activity activity = (Activity)v.getContext();
                    activity.startActivityForResult(intent , 9);
                    activity.overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);
                }
            });
        }
    }
    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }
    public void updateSuccess(){
        VHheader.favorite.setEnabled(true);
    }

    private boolean isPositionHeader(int position)
    {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        if(postingItems == null){
            return 0 + 1;
        }else{
            if(foot == true){
                return postingItems.size()+2;
            }else{
                return postingItems.size()+1;
            }

        }
    }
    private PostingItem getItem(int position)
    {
        return postingItems.get(position) ;
    }

    class VHHeader extends RecyclerView.ViewHolder{
        @InjectView(R.id.all)
        Button all;
        @InjectView(R.id.kakao)
        Button kakao;
        @InjectView(R.id.imageView1)
        SimpleDraweeView simpleDraweeView;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.favorite)
        Button favorite;
        @InjectView(R.id.yesterdayPost)
        TextView yesterdayPost;
        @InjectView(R.id.yesterdayReply)
        TextView yesterdayReply;
        @InjectView(R.id.ranking)
        TextView ranking;



        public VHHeader(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @InjectView(R.id.post_item)
        LinearLayout post_item;
        @InjectView(R.id.info_text)
        TextView textView;
        @InjectView(R.id.user)
        TextView user;
        @InjectView(R.id.regdate)
        TextView regdate;
        @InjectView(R.id.reply)
        TextView reply;
        @InjectView(R.id.random)
        TextView random;
        @InjectView(R.id.category)
        TextView category;
        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }
    class VHFooter extends RecyclerView.ViewHolder{


        public VHFooter(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }

    public class ASynk extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {
            String url = params[0];
            String urlPlus = params[1];
            String json = params[2];
            CategoryDetailHeader yesterdayHeader;
            try {
                result = post(url + urlPlus, json);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Dlog.d("=======================");
            return result;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateSuccess();
        }
    }
    public class Singo extends AsyncTask<String , String , String> {
        @Override
        protected String doInBackground(String[] params) {

            String url = params[0];
            String urlPlus = params[1];
            String json = params[2];
            try {
                singoJS = post(url + urlPlus, json);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return singoJS;
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
}
