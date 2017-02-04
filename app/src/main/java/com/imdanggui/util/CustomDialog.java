package com.imdanggui.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.widget.ImageView;

import com.imdanggui.R;

/**
 * Created by giseon on 2015-09-27.
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        ImageView imageView = (ImageView)findViewById(R.id.loading_img);
        imageView.setBackgroundResource(R.anim.loading);

        AnimationDrawable frameAnimation = (AnimationDrawable)imageView.getBackground();
        frameAnimation.start();
    }

    @Override
    public void hide() {
        super.hide();
    }
}
