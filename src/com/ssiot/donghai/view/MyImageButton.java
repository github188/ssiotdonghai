package com.ssiot.donghai.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

public class MyImageButton extends ImageButton{
    private String tag = "MyImageButton";
    
    int pressed = android.R.attr.state_pressed;
    
    public MyImageButton(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        setClickable(false);
    }

    public MyImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(false);
        // TODO Auto-generated constructor stub
    }
    
    public MyImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setClickable(false);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void setSelected(boolean selected) {
        Log.v(tag, "-----------------setSelected------------" + selected);
        super.setSelected(selected);
    }
    
    @Override
    protected void drawableStateChanged() {
        boolean pressed = false;
        int[] curStates = getDrawableState();
        for (int i =0 ; i <curStates.length ; i++){
            if (android.R.attr.state_pressed == curStates[i]){
                pressed = true;
                break;
            }
        }
        if (pressed){
            
        } else {
            
        }
        // TODO Auto-generated method stub
        super.drawableStateChanged();
    }
    
    
}