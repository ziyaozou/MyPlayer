package com.github.ziyao.fablibrary;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.github.ziyao.fablibrary.R;
/**
 * Created by Ziyao on 2015/9/18.
 */
public class FloatingActionButton extends ImageButton{

    private LayerDrawable normalDrawable;
    private LayerDrawable pressedDrawable;
    private StateListDrawable listDrawable;


    private Context mContext;

    public FloatingActionButton(Context context, AttributeSet attrs){
        super(context, attrs);
        this.mContext = context;
    }

    public FloatingActionButton(Context context){
        super(context);
        this.mContext = context;
    }


    public void setColor( int normal, int pressed){
        normalDrawable = ( LayerDrawable ) mContext.getResources().getDrawable(R.drawable.normal_shape);
        pressedDrawable = ( LayerDrawable ) mContext.getResources().getDrawable(R.drawable.pressed_shape);
        GradientDrawable first = ( GradientDrawable ) normalDrawable.findDrawableByLayerId(R.id.normal_drawable);
        first.setColor(normal);
        GradientDrawable second = ( GradientDrawable ) pressedDrawable.findDrawableByLayerId(R.id.pressed_drawable);
        second.setColor(pressed);

        listDrawable = new StateListDrawable();
        listDrawable.addState(new int[]{-android.R.attr.state_enabled}, normalDrawable);
        listDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);

        this.setBackgroundDrawable(listDrawable);
    }

}
