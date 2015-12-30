package com.ssiot.donghai.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SquareLayout extends LinearLayout{
    public SquareLayout(Context context) {
        super(context);
    }
 
    public SquareLayout(Context context,AttributeSet attr) {
        super(context,attr);
    }
 
//    public SquareLayout(Context context,AttributeSet attr,int defStyle) {
//        super(context,attr,defStyle);
//    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(getDefaultSize(0,widthMeasureSpec),getDefaultSize(0,heightMeasureSpec));
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);  
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);  
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);  
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);  
        // height is equal to  width
        heightSize = widthSize = heightSize > widthSize ? widthSize : heightSize;
        
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}