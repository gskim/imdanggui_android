package com.imdanggui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.imdanggui.R;
import com.imdanggui.model.PostDetailHeader;
import com.imdanggui.model.ReplyItem;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by user on 2015-09-10.
 */
public class PostDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    PostDetailHeader header;
    List<ReplyItem> replyItems;
    public PostDetailAdapter(PostDetailHeader header, List<ReplyItem> replyItems) {
        this.replyItems = replyItems;
        this.header = header;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_post_detail_header, parent, false);
            return  new VHHeader(v);
        }
        else if(viewType == TYPE_ITEM)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
            return new ViewHolder(v);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof VHHeader)
        {
            VHHeader VHheader = (VHHeader)holder;
            VHheader.post_text.setText(header.getText());
            VHheader.post_nickname.setText(header.getNickname());
            VHheader.post_regdate.setText(header.getRegdate());
            VHheader.reply.setText( String.valueOf(header.getReply()) );
            VHheader.category.setText( header.getCategory() + "에서");
            VHheader.post_random.setText("[" + header.getRandom() + "]");
        }
        else if(holder instanceof ViewHolder)
        {
            final ReplyItem currentItem = getItem(position - 1);
            final ViewHolder VHitem = (ViewHolder)holder;
            VHitem.reply_text.setText(currentItem.getText());
            String regdate = currentItem.getRegdate();
            if(regdate == null || regdate.equals("")){
                regdate = "1초전";
            }else{
                regdate = currentItem.getRegdate();
            }
            VHitem.reply_regdate.setText(regdate);
            String reply_num = String.valueOf(position);
            if( Integer.valueOf(reply_num) >= 100){
                reply_num = "99+";
            }
            VHitem.reply_num.setText(reply_num);
            VHitem.reply_random.setText("[" + currentItem.getRandom() + "]");
        }
    }
    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position)
    {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        if(replyItems == null){
            return 0+1;
        }else{
            return replyItems.size()+1;
        }

    }
    private ReplyItem getItem(int position)
    {
        return replyItems.get(position);
    }

    class VHHeader extends RecyclerView.ViewHolder{
        @InjectView(R.id.post_text)
        TextView post_text;
        @InjectView(R.id.post_nickname)
        TextView post_nickname;
        @InjectView(R.id.post_regdate)
        TextView post_regdate;
        @InjectView(R.id.post_reply_count)
        TextView reply;
        @InjectView(R.id.category)
        TextView category;
        @InjectView(R.id.random)
        TextView post_random;
        public VHHeader(View v) {
            super(v);
            ButterKnife.inject(this, v);

        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        @InjectView(R.id.reply_text)
        TextView reply_text;
        @InjectView(R.id.reply_regdate)
        TextView reply_regdate;
        @InjectView(R.id.reply_num)
        TextView reply_num;
        @InjectView(R.id.random)
        TextView reply_random;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
        }
    }
}
