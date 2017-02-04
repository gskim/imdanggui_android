package com.imdanggui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.imdanggui.PostDetailActivity;
import com.imdanggui.R;
import com.imdanggui.StartActivity;
import com.imdanggui.model.PostingItem;
import com.imdanggui.model.PushItem;
import com.imdanggui.util.Dlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by user on 2015-09-07.
 */
public class MyPushAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<PostingItem> postingItems;
    List<PushItem> pushItems;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=UTF-8");
    OkHttpClient client = new OkHttpClient();
    JSONObject jo;
    SharedPreferences.Editor editor;
    public MyPushAdapter(List<PushItem> pushItems, List<PostingItem> postingItems) {
        this.postingItems = postingItems;
        this.pushItems = pushItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mypush, parent, false);
            return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        String regdateStr;
        if(holder instanceof ViewHolder){
            final PushItem currentItem = getItem(position);
            final ViewHolder VHitem = (ViewHolder)holder;

            if( currentItem.getRegdate() == null){
                regdateStr = "1초전";
            }else{
                regdateStr = currentItem.getRegdate();
            }
            VHitem.regdate.setText(regdateStr);
            VHitem.textView.setText(currentItem.getMessage());

            if(currentItem.getRead().equals("y")){
                Dlog.d("########position : " + String.valueOf(position) + "#######");
                Dlog.d("#######READ Y ######");
                VHitem.textView.setTextColor(Color.GRAY);
                VHitem.regdate.setTextColor(Color.GRAY);
                VHitem.read.setBackgroundResource(R.drawable.ic_push_off);
                VHitem.push_item.setBackgroundResource(R.drawable.tv_bg_selector_push_read);
            }else{
                VHitem.textView.setTextColor(Color.WHITE);
                VHitem.regdate.setTextColor(Color.WHITE);
                Dlog.d("########position : " + String.valueOf(position) + "#######");
                Dlog.d("#######READ X ######");
                VHitem.read.setBackgroundResource(R.drawable.ic_push_on);
                VHitem.push_item.setBackgroundResource(R.drawable.tv_bg_selector);
            }


            VHitem.push_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VHitem.textView.setTextColor(Color.GRAY);
                    VHitem.regdate.setTextColor(Color.GRAY);
                    VHitem.push_item.setBackgroundResource(R.color.post2);
                    VHitem.read.setBackgroundResource(R.drawable.ic_push_off);
                    VHitem.push_item.setBackgroundResource(R.drawable.tv_bg_selector_push_read);
                    if( currentItem.getRead().equals("n") ){
                        if(StartActivity.setting.getInt("pushcount" , 0) == 0){
                        }else{
                            editor = StartActivity.setting.edit();
                            editor.putInt("pushcount", StartActivity.setting.getInt("pushcount", 0) - 1);
                            editor.commit();
                            Dlog.d("#####" + String.valueOf(StartActivity.setting.getInt("pushcount" , 0)));
                        }
                        currentItem.setRead("y");
                    }
                    jo = new JSONObject();
                    try {
                        jo.put("id" , currentItem.getId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String[] param = new String[3];
                    param[0] = StartActivity.domain+"imdanggui/";
                    param[1] = "push_read.php";
                    param[2] = jo.toString();
                    new PushRead().execute(param);
                    //포스트 리스트 클릭
                    if(postingItems.get(position).getText() == null || postingItems.get(position).getSingo() >= 5){
                        Toast.makeText(v.getContext(), "삭제된 게시글입니다.",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                        intent.putExtra("pos" , position);
                        intent.putExtra("id", postingItems.get(position).getId());
                        intent.putExtra("singo", postingItems.get(position).getSingo());
                        intent.putExtra("text", postingItems.get(position).getText());
                        intent.putExtra("nickname", postingItems.get(position).getNickname());
                        intent.putExtra("regdate", postingItems.get(position).getRegdate());
                        intent.putExtra("reply", postingItems.get(position).getReply_count());
                        intent.putExtra("category", postingItems.get(position).getCategory());
                        intent.putExtra("random", postingItems.get(position).getRandom());
                        Activity activity = (Activity) v.getContext();
                        activity.startActivityForResult(intent , 9);
                        activity.overridePendingTransition(android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right);
                    }

                }
            });
        }



    }
    private PushItem getItem(int position)
    {
        return pushItems.get(position) ;
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
        @InjectView(R.id.push_item)
        LinearLayout push_item;
        @InjectView(R.id.read)
        ImageView read;
        @InjectView(R.id.text)
        TextView textView;
        @InjectView(R.id.regdate)
        TextView regdate;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }
    public class PushRead extends AsyncTask<String , String ,String >{
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String urlPlus = params[1];
            String json = params[2];
            try {
                post(url + urlPlus, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
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
