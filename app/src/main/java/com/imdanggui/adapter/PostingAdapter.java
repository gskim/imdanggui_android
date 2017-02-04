package com.imdanggui.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imdanggui.PostDetailActivity;
import com.imdanggui.R;
import com.imdanggui.StartActivity;
import com.imdanggui.model.PostingItem;
import com.imdanggui.util.Dlog;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

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
 * Created by user on 2015-09-07.
 */
public class PostingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<PostingItem> postingItems;

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");
    SweetAlertDialog sweetAlertDialog;
    OkHttpClient client = new OkHttpClient();
    String singoJS;
    boolean singoClick = false;
    public PostingAdapter(List<PostingItem> postingItems) {
        this.postingItems = postingItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        if(viewType == TYPE_FOOTER){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_footer, parent, false);
            return new VHFooter(v);
        }else{
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_posting, parent, false);
            // set the view's size, margins, paddings and layout parameters

            return new ViewHolder(v);
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Dlog.d("===bind position ==== : " + String.valueOf(position));
        String regdateStr = null;
        String categoryName = null;
        if(holder instanceof ViewHolder){
            final PostingItem currentItem = getItem(position);
            final ViewHolder VHitem = (ViewHolder)holder;

            VHitem.textView.setText(currentItem.getText());
            VHitem.nickname.setText(currentItem.getNickname());
            categoryName = currentItem.getCategory()+"에서";
            VHitem.category.setText(categoryName);
            if( currentItem.getRegdate() == null){
                regdateStr = "1초전";
            }else{
                regdateStr = currentItem.getRegdate();
            }
            VHitem.regdate.setText(regdateStr);
            VHitem.reply.setText(String.valueOf(currentItem.getReply_count()));
            VHitem.random.setText("[" + currentItem.getRandom() + "]");
            if(position % 2 == 0){
                VHitem.reply.setBackgroundResource(R.drawable.item_posting_replycount);
                VHitem.post_item.setBackgroundResource(R.color.post1);
            }else{
                VHitem.reply.setBackgroundResource(R.drawable.item_posting_replycount_sec);
                VHitem.post_item.setBackgroundResource(R.color.post2);
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
                    Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                    intent.putExtra("pos" , position);
                    intent.putExtra("id", postingItems.get(position).getId());
                    intent.putExtra("singo", postingItems.get(position).getSingo());
                    intent.putExtra("text", postingItems.get(position).getText());
                    intent.putExtra("nickname", postingItems.get(position).getNickname());
                    intent.putExtra("regdate", postingItems.get(position).getRegdate());
                    intent.putExtra("reply" , postingItems.get(position).getReply_count());
                    intent.putExtra("category" , postingItems.get(position).getCategory());
                    intent.putExtra("random", postingItems.get(position).getRandom());
                    Activity activity = (Activity) v.getContext();
                    activity.startActivityForResult(intent , 9);
                    activity.overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);


                }
            });
        }



    }
    private PostingItem getItem(int position)
    {
        return postingItems.get(position) ;
    }
    @Override
    public int getItemViewType(int position) {
        Dlog.d("postingitems size"+ String.valueOf(postingItems.size()));

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if(postingItems == null){
            return 0;
        }else{
            return postingItems.size();
        }

    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @InjectView(R.id.category)
        TextView category;
        @InjectView(R.id.post_item)
        LinearLayout post_item;
        @InjectView(R.id.info_text)
        TextView textView;
        @InjectView(R.id.user)
        TextView nickname;
        @InjectView(R.id.regdate)
        TextView regdate;
        @InjectView(R.id.reply)
        TextView reply;
        @InjectView(R.id.random)
        TextView random;


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
