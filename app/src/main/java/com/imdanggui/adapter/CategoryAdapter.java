package com.imdanggui.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import com.facebook.drawee.view.SimpleDraweeView;

import com.imdanggui.R;
import com.imdanggui.StartActivity;
import com.imdanggui.model.CategoryItem;
import com.imdanggui.util.Dlog;


import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by giseon on 2015-09-06.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    List<CategoryItem> mItems;
    ArrayList<Integer> checkList = new ArrayList<Integer>();
    public CategoryAdapter(List<CategoryItem> mItems) {
        this.mItems = mItems;
    }

    public List<CategoryItem> getmItems(){
        return mItems;
    }

    public ArrayList<Integer> getCheckListeckList(){
        return checkList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Dlog.d("create ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Dlog.d("=================");
        CategoryItem item = mItems.get(position);
        String imageBaseUrl = StartActivity.domain+"img/category/";
        Uri uri = Uri.parse( imageBaseUrl + item.getThumb());
        /*ViewUtil.bind(holder.thumb , uri.toString());*/
        SimpleDraweeView draweeView = (SimpleDraweeView) holder.thumb;
        draweeView.setImageURI(uri);

        holder.textView.setText(item.getName());
        holder.checkBox.setChecked(item.isChecked());
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( holder.checkBox.isChecked() ){
                    mItems.get(position).setChecked(true);
                }else{
                    mItems.get(position).setChecked(false);
                }
            }
        });

        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.d("##click thumb ## : " + String.valueOf(holder.checkBox.isChecked()));

                if( holder.checkBox.isChecked() ){
                    holder.checkBox.setChecked(false);
                    mItems.get(position).setChecked(false);

                }else{
                    holder.checkBox.setChecked(true);
                    mItems.get(position).setChecked(true);
                }
                //holder.checkBox.performClick();

            }
        });

    }

    @Override
    public int getItemCount() {
        if(mItems == null){
            return  0;
        }else{
            return mItems.size();
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.textView1)
        public TextView textView;
        @InjectView(R.id.imageView1)
        public SimpleDraweeView thumb;
        @InjectView(R.id.checkbox1)
        public CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
