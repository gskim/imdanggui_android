package com.imdanggui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imdanggui.PostDetailActivity;
import com.imdanggui.R;
import com.imdanggui.model.PostingItem;
import com.imdanggui.util.Dlog;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by user on 2015-09-07.
 */
public class MyPostingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<PostingItem> postingItems;

    public MyPostingAdapter(List<PostingItem> postingItems) {
        this.postingItems = postingItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mywrite, parent, false);
            return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Dlog.d("===bind position ==== : " + String.valueOf(position));
        String regdateStr = null;

        if(holder instanceof ViewHolder){
            final PostingItem currentItem = getItem(position);
            final ViewHolder VHitem = (ViewHolder)holder;
            VHitem.textView.setText(currentItem.getText());

            if( currentItem.getRegdate() == null){
                regdateStr = "1초전";
            }else{
                regdateStr = currentItem.getRegdate();
            }
            VHitem.regdate.setText(regdateStr);


            VHitem.write_item.setOnClickListener(new View.OnClickListener() {
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
    public int getItemCount() {
        if(postingItems == null){
            return 0;
        }else{
            return postingItems.size();
        }

    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        @InjectView(R.id.write_item)
        LinearLayout write_item;
        @InjectView(R.id.text)
        TextView textView;
        @InjectView(R.id.regdate)
        TextView regdate;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }


}
