package com.imdanggui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.imdanggui.CategoryActivity;
import com.imdanggui.CategoryDetailActivity;
import com.imdanggui.CategoryPostActivity;
import com.imdanggui.R;
import com.imdanggui.StartActivity;
import com.imdanggui.model.CategoryTabItem;
import com.imdanggui.util.Dlog;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by giseon on 2015-09-06.
 */
public class CategoryTabAdapter extends RecyclerView.Adapter<CategoryTabAdapter.ViewHolder> {

    List<CategoryTabItem> mItems;
    ArrayList<Integer> checkList = new ArrayList<Integer>();
    boolean isUserCategory = false;
    public CategoryTabAdapter(List<CategoryTabItem> mItems , boolean isUserCategory) {
        this.mItems = mItems;
        this.isUserCategory = isUserCategory;
    }
/*
    public List<CategoryTabItem> getmItems(){
        return mItems;
    }

    public ArrayList<Integer> getCheckListeckList(){
        return checkList;
    }*/

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CategoryTabItem item = mItems.get(position);
        String imageBaseUrl = StartActivity.domain + "img/category/";
        Uri uri = Uri.parse( imageBaseUrl + item.getThumb());
        SimpleDraweeView draweeView = (SimpleDraweeView) holder.thumb;
        draweeView.setImageURI(uri);
        holder.textView.setText(item.getName());

        String cate;
        String[] cateArr;
        if(item.getCategory() != null){
            if(!item.getCategory().equals("")){
                cate = item.getCategory();
                cateArr = cate.split(",");
                for(int i = 0 ; i < cateArr.length ; i++){
                    if(cateArr[i].equals(String.valueOf(item.getId())) ){
                        item.setIsUser(true);
                        break;
                    }else{
                        item.setIsUser(false);
                    }
                }
            }else{
                item.setIsUser(false);
            }
        }else{
            item.setIsUser(true);
        }
        if(item.isNew()){
            holder.newImg.setVisibility(View.VISIBLE);
        }else{
            holder.newImg.setVisibility(View.GONE);
        }


        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = mItems.get(position).getId();
                Dlog.d("#####id###### : " + String.valueOf(id));
                String name = mItems.get(position).getName();
                String thumb = mItems.get(position).getThumb();
                int ranking = mItems.get(position).getRanking();
                String yesterdayPost;
                String yesterdayReply;
                if( id == 0 ) {
                    //추가하기 클릭
                    Intent intent = new Intent(v.getContext(), CategoryActivity.class);
                    intent.putExtra("parent", "tab");
                    intent.putExtra("type", "plus");
                    Activity activity = (Activity) v.getContext();
                    activity.startActivityForResult(intent, 3);
                    activity.overridePendingTransition(R.anim.custom_slide_in_top,
                            R.anim.custom_slide_out_bottom);
                }else if(id == -2){
                    //관심임당귀 클릭
                    Dlog.d("######-2########");
                    if(mItems.size() == 3){
                        Toast.makeText(v.getContext(), "관심분야를 추가해주세요.",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(v.getContext(), CategoryActivity.class);
                        intent.putExtra("parent", "tab");
                        intent.putExtra("type", "plus");
                        Activity activity = (Activity) v.getContext();
                        activity.startActivityForResult(intent, 3);
                        activity.overridePendingTransition(R.anim.custom_slide_in_top,
                                R.anim.custom_slide_out_bottom);
                    }else{
                        Intent intent;
                        intent = new Intent( v.getContext() , CategoryPostActivity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("name", name);
                        Activity activity = (Activity)v.getContext();
                        activity.startActivityForResult(intent, 4);
                        activity.overridePendingTransition(android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right);
                    }

                }else if(id == -1){
                    //삭제하기 클릭
                    if(mItems.size() == 3){
                        Toast.makeText(v.getContext(), "삭제 할 관심이 없습니다.",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(v.getContext(), CategoryActivity.class);
                        intent.putExtra("parent", "tab");
                        intent.putExtra("type", "minus");
                        Activity activity = (Activity) v.getContext();
                        activity.startActivityForResult(intent, 3);
                        activity.overridePendingTransition(R.anim.custom_slide_in_top,
                                R.anim.custom_slide_out_bottom);
                    }
                }else{
                    Intent intent;
                    if( isUserCategory == true ){
                        intent = new Intent( v.getContext() , CategoryPostActivity.class);
                        intent.putExtra("id" , id);
                        intent.putExtra("name", name);
                    }else{
                        yesterdayPost = mItems.get(position).getYesterdayPost();
                        yesterdayReply = mItems.get(position).getYesterdayReply();
                        intent = new Intent( v.getContext() , CategoryDetailActivity.class);
                        intent.putExtra("id" , id);
                        intent.putExtra("name", name);
                        intent.putExtra("thumb", thumb);
                        intent.putExtra("ranking" , ranking);
                        intent.putExtra("yesterdayPost" , yesterdayPost);
                        intent.putExtra("yesterdayReply" , yesterdayReply);
                    }
                    if( item.isUser() ){
                        intent.putExtra("text" , "remove");
                    }else{
                        intent.putExtra("text" , "add");
                    }
                    Activity activity = (Activity)v.getContext();
                    activity.startActivityForResult(intent , 4);
                    activity.overridePendingTransition(android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right);

                }
            }
        });
    }
    /*public void onActivityResult(int requestCode , int resultCode , Intent data){

    }*/

    @Override
    public int getItemCount() {
        if( mItems == null ){
            return 0;
        }else{
            return mItems.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.textView1)
        public TextView textView;
        @InjectView(R.id.imageView1)
        public SimpleDraweeView thumb;
        @InjectView(R.id.new_img)
        ImageView newImg;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);

        }
    }
}
