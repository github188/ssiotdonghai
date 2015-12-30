package com.ssiot.donghai.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ssiot.donghai.R;

public class IconView extends RelativeLayout{
    
    private ImageButton mImgView = null;  
    private TextView mTextView = null;  
    private Context mContext;  
    
    public IconView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        LayoutInflater.from(context).inflate(R.layout.icon_view, this, true);  
        mContext = context;  
        mImgView = (ImageButton)findViewById(R.id.icon_img);  
        mTextView = (TextView)findViewById(R.id.icon_text);
        setClickable(true);
    }
    
    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        LayoutInflater.from(context).inflate(R.layout.icon_view, this, true);  
//        e(index, defaultValue)
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IconView);
//        ta.getAttributeIntValue(R.styleable.IconView_myimg, 0);
//        ta.getInt(R.styleable.IconView_myimg, 0);
        int imgRes = ta.getResourceId(R.styleable.IconView_myimg, 0);
        int txtRes = ta.getResourceId(R.styleable.IconView_mytxt, 0);
        ta.recycle();
        mContext = context;  
        mImgView = (ImageButton)findViewById(R.id.icon_img);  
        mTextView = (TextView)findViewById(R.id.icon_text);
        setClickable(true);
        if (imgRes != 0){
            mImgView.setImageResource(imgRes);
        }
        if (txtRes != 0){
            mTextView.setText(txtRes);
        }
    }

//    public IconView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        // TODO Auto-generated constructor stub
//    }
    
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
            mImgView.setPressed(true);
        } else {
            mImgView.setPressed(false);
        }
        super.drawableStateChanged();
    }
    
    public void setImageResource(int resId){  
        mImgView.setImageResource(resId);  
    }  
      
    /*设置文字接口*/  
    public void setText(String str){  
        mTextView.setText(str);  
    }  
    /*设置文字大小*/  
    public void setTextSize(float size){  
        mTextView.setTextSize(size);  
    }  
    
}