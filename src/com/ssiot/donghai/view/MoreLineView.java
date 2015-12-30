package com.ssiot.donghai.view;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ssiot.donghai.R;

//下载的androidcharts模板
public class MoreLineView  extends View {
    private static final String tag = "MoreLineView";
    private int mViewHeight;
    //drawBackground
    private boolean autoSetDataOfGird = true;
    private boolean autoSetGridWidth = true;
    private int dataOfAGird = 10;
    private int bottomTextHeight = 0;
    private ArrayList<String> bottomTextList;
    private ArrayList<ArrayList<Float>> dataLists;
    private ArrayList<Integer> xCoordinateList = new ArrayList<Integer>();
    private ArrayList<Integer> yCoordinateList = new ArrayList<Integer>();
    private ArrayList<ArrayList<Dot>> drawDotLists = new ArrayList<ArrayList<Dot>>();
    private Paint bottomTextPaint = new Paint();
    private Paint ycoordTextPaint = new Paint();
    private int bottomTextDescent;
    private Dot selectedDot;
    private String[] colorArray = {"#e74c3c","#2980b9","#1abc9c"};
    //popup
    private Paint popupTextPaint = new Paint();
    private final int bottomTriangleHeight = 12;
    //private Dot selectedDot;
    private boolean mShowYCoordinate = false;

    private int topLineLength = MyUtils.dip2px(getContext(), 12);; // | | 鈫this//jingbo 空出来给圆点和popup的
                                                                   //-+-+-
    private int sideLineLength = MyUtils.dip2px(getContext(),45)/3*2;// --+--+--+--+--+--+--
                                                               //  鈫this           鈫
    private int backgroundGridWidth = MyUtils.dip2px(getContext(),45);

    //Constants
    private final int popupTopPadding = MyUtils.dip2px(getContext(),2);
    private final int popupBottomMargin = MyUtils.dip2px(getContext(),5);
    private final int bottomTextTopMargin = MyUtils.sp2px(getContext(),5);
    private final int bottomLineLength = MyUtils.sp2px(getContext(), 22);
    private final int DOT_INNER_CIR_RADIUS = MyUtils.dip2px(getContext(), 2);
    private final int DOT_OUTER_CIR_RADIUS = MyUtils.dip2px(getContext(),5);
    private final int MIN_TOP_LINE_LENGTH = MyUtils.dip2px(getContext(),12);
    private final int MIN_VERTICAL_GRID_NUM = 4;
    private final int MIN_HORIZONTAL_GRID_NUM = 1;
    private final int BACKGROUND_LINE_COLOR = Color.parseColor("#EEEEEE");
    private final int BOTTOM_TEXT_COLOR = Color.parseColor("#9B9A9B");
    private final int YCOORD_TEXT_LEFT_MARGIN = MyUtils.dip2px(getContext(), 10);
    private int[] popupColorArray = {R.drawable.popup_red,R.drawable.popup_blue,R.drawable.popup_green};
    public static final int SHOW_POPUPS_All = 1;
    public static final int SHOW_POPUPS_MAXMIN_ONLY = 2;
    public static final int SHOW_POPUPS_NONE = 3;
    private int showPopupType = SHOW_POPUPS_NONE;
    public boolean showPopup = true; 
    public void setShowPopup(int popupType) {
		this.showPopupType = popupType;
	}
    class YCoordData {
    	private int y;
    	private int data;
		public int getY() {
			return y;
		}
		public void setY(int y) {
			this.y = y;
		}
		public int getData() {
			return data;
		}
		public void setData(int data) {
			this.data = data;
		}
    }

    private Runnable animator = new Runnable() {
        @Override
        public void run() {
            boolean needNewFrame = false;
            for(ArrayList<Dot> data : drawDotLists){
            	for(Dot dot : data){
                    dot.update();
                    if(!dot.isAtRest()){
                        needNewFrame = true;
                    }
                }
            }
            if (needNewFrame) {
                postDelayed(this, 25);
            }
            invalidate();
        }
    };
    public MoreLineView(Context context){
        this(context,null);
    }
    public MoreLineView(Context context, AttributeSet attrs){
        super(context, attrs);
        popupTextPaint.setAntiAlias(true);
        popupTextPaint.setColor(Color.WHITE);
        popupTextPaint.setTextSize(MyUtils.sp2px(getContext(), 13));
        popupTextPaint.setStrokeWidth(5);
        popupTextPaint.setTextAlign(Paint.Align.CENTER);

        bottomTextPaint.setAntiAlias(true);
        bottomTextPaint.setTextSize(MyUtils.sp2px(getContext(),12));
        bottomTextPaint.setTextAlign(Paint.Align.CENTER);
        bottomTextPaint.setStyle(Paint.Style.FILL);
        bottomTextPaint.setColor(BOTTOM_TEXT_COLOR);
        
        ycoordTextPaint.setAntiAlias(true);
        ycoordTextPaint.setTextSize(MyUtils.sp2px(getContext(),12));
        ycoordTextPaint.setTextAlign(Paint.Align.LEFT);
        ycoordTextPaint.setStyle(Paint.Style.FILL);
        ycoordTextPaint.setColor(BOTTOM_TEXT_COLOR);
    }

    /**
     * dataList will be reset when called is method.
     * @param bottomTextList The String ArrayList in the bottom.
     */
    public void setBottomTextList(ArrayList<String> bottomTextList){
        this.dataLists = null;
        this.bottomTextList = bottomTextList;

        Rect r = new Rect();
        int longestWidth = 0;
        String longestStr = "";
        bottomTextDescent = 0;
        for(String s:bottomTextList){
            bottomTextPaint.getTextBounds(s,0,s.length(),r);
            if(bottomTextHeight<r.height()){
                bottomTextHeight = r.height();
            }
            if(autoSetGridWidth&&(longestWidth<r.width())){
                longestWidth = r.width();
                longestStr = s;
            }
            if(bottomTextDescent<(Math.abs(r.bottom))){
                bottomTextDescent = Math.abs(r.bottom);
            }
        }

        if(autoSetGridWidth){
            if(backgroundGridWidth<longestWidth){
                backgroundGridWidth = longestWidth+(int)bottomTextPaint.measureText(longestStr,0,1);
            }
            if(sideLineLength<longestWidth/2){
                sideLineLength = longestWidth/2;
            }
        }

        refreshXCoordinateList(getHorizontalGridNum());
    }
    /**
     *
     * @param dataList The Float ArrayList for showing,
     *                 dataList.size() must < bottomTextList.size()
     */
//    public void setDataList(ArrayList<Float> dataList){
//        this.dataList = dataList;
//        if(dataList.size() > bottomTextList.size()){
//            throw new RuntimeException("dacer.LineView error:" +
//                    " dataList.size() > bottomTextList.size() !!!");
//        }
//        if(autoSetDataOfGird){
//            int biggestData = 0;
//            for(Float i:dataList){
//                if(biggestData<i){
//                    biggestData = i;
//                }
//            }
//            dataOfAGird = 1;
//            while(biggestData/10 > dataOfAGird){
//                dataOfAGird *= 10;
//            }
//        }
//        refreshAfterDataChanged();
//        setMinimumWidth(0); // It can help the LineView reset the Width,
//                                // I don't know the better way..
//        postInvalidate();
//    }
    public void setDataList(ArrayList<ArrayList<Float>> dataLists){
//    	selectedDot = null;
        this.dataLists = dataLists;
        for(ArrayList<Float> list : dataLists){
        	if(list.size() > bottomTextList.size()){
                throw new RuntimeException("dacer.LineView error:" +
                        " dataList.size() > bottomTextList.size() !!!");
            }
        }
        float biggestData = 0;
        for(ArrayList<Float> list : dataLists){
        	if(autoSetDataOfGird){
                for(Float i:list){
                    if(biggestData < i){
                        biggestData = i;
                    }
                }
        	}
        	dataOfAGird = 1;//jingbo TODO 弄懂这个,竖着的y轴上的一格的数字？  找个最小的
        	while(biggestData / 10 > dataOfAGird){
        		dataOfAGird *= 10;
        	}
        }
        
        refreshAfterDataChanged();
        showPopup = true;
        setMinimumWidth(0); // It can help the LineView reset the Width,
                                // I don't know the better way..
        postInvalidate();
    }
    public void setShowYCoordinate(boolean showYCoordinate) {
    	mShowYCoordinate = showYCoordinate;
    }

    private void refreshAfterDataChanged(){
        int verticalGridNum = getVerticalGridlNum();
//        Log.v(tag, "----verticalGridNum:"+verticalGridNum);
        refreshTopLineLength(verticalGridNum);
        
        refreshYCoordinateList(verticalGridNum);
        refreshDrawDotList(verticalGridNum);
    }

    private int getVerticalGridlNum() {
        int verticalGridNum = MIN_VERTICAL_GRID_NUM;
        if (dataLists != null && !dataLists.isEmpty()) {
            for (ArrayList<Float> list : dataLists) {
                for (Float integer : list) {
                    if (verticalGridNum < (integer + 1)) {
                        verticalGridNum = (int) (integer + 1);
                    }
                }
            }
        }
        return verticalGridNum;
    }

    private int getHorizontalGridNum(){
        if (null == bottomTextList){//add by jingbo
            return MIN_HORIZONTAL_GRID_NUM;
        }
        int horizontalGridNum = bottomTextList.size()-1;
        if(horizontalGridNum<MIN_HORIZONTAL_GRID_NUM){
            horizontalGridNum = MIN_HORIZONTAL_GRID_NUM;
        }
        return horizontalGridNum;
    }

    private void refreshXCoordinateList(int horizontalGridNum){
        xCoordinateList.clear();
        for(int i=0;i<(horizontalGridNum+1);i++){
			xCoordinateList.add(sideLineLength + backgroundGridWidth*i);
        }

    }

    private void refreshYCoordinateList(int verticalGridNum){//y轴上可分成多少份，每份的值先算好,但没必要！
        yCoordinateList.clear();
        for(int i=0;i<(verticalGridNum+1);i++){
            yCoordinateList.add(topLineLength +
            		((mViewHeight-topLineLength-bottomTextHeight-bottomTextTopMargin-
            				bottomLineLength-bottomTextDescent)*i/(verticalGridNum)));
//            Log.v(tag, "----refreshYCoordinateList---- topLineLength:"+topLineLength+" mViewHeight:"+mViewHeight + " bottomTextHeight:" +bottomTextHeight);
//            Log.v(tag, "----yc" + yCoordinateList.get(i));
        }
    }

    private void refreshDrawDotList(int verticalGridNum){
        drawDotLists.clear();
        if(dataLists != null && !dataLists.isEmpty()){
//    		if(drawDotLists.size() == 0){
            
    			for(int k = 0; k < dataLists.size(); k++){
    				drawDotLists.add(new ArrayList<MoreLineView.Dot>());
    			}
//    		}
        	for(int k = 0; k < dataLists.size(); k++){
        		int drawDotSize = drawDotLists.get(k).isEmpty() ? 0 : drawDotLists.get(k).size();
        		
        		for(int i=0;i<dataLists.get(k).size();i++){//遍历每一条线上的数据
        		    float maxValue = Collections.max(dataLists.get(k));
        		    float minValue = Collections.min(dataLists.get(k));//jingbo TODO
        		    
                    int x = xCoordinateList.get(i);
                    float valuef =  dataLists.get(k).get(i);//jingbo modify
//                    int value = (int) valuef;
//                    int y = yCoordinateList.get(verticalGridNum - value);
                    
                    int y = (int) (topLineLength + (mViewHeight-topLineLength-bottomTextHeight-bottomTextTopMargin-
                            bottomLineLength-bottomTextDescent)*(maxValue-valuef)/(maxValue-minValue));
                    if (maxValue == minValue){//值全相等 就显示在中间吧
                        y = (int) (topLineLength + (mViewHeight-topLineLength-bottomTextHeight-bottomTextTopMargin-
                                bottomLineLength-bottomTextDescent)/2);
                    }
                    if(i > drawDotSize-1){//圆点名单追加
                        drawDotLists.get(k).add(new Dot(x, 0, x, y, dataLists.get(k).get(i),k));
                    }else{//也把目标设定在约会
                    	//도트리스트에 타겟을 설정한다.
                        drawDotLists.get(k).set(i, drawDotLists.get(k).get(i).setTargetData(x,y,dataLists.get(k).get(i),k));
                    }
                }
        		
        		int temp = drawDotLists.get(k).size() - dataLists.get(k).size();
        		for(int i=0; i<temp; i++){
        			drawDotLists.get(k).remove(drawDotLists.get(k).size()-1);
        		}
        	}
        }
        removeCallbacks(animator);
        post(animator);
    }

    private void refreshTopLineLength(int verticalGridNum){
        // For prevent popup can't be completely showed when backgroundGridHeight is too small.
        // But this code not so good.//竖着的格子比较多时有问题？？ jingbo delete this
//        if((mViewHeight-topLineLength-bottomTextHeight-bottomTextTopMargin)/
//                (verticalGridNum+2)<getPopupHeight()){
            topLineLength = getPopupHeight()+DOT_OUTER_CIR_RADIUS+DOT_INNER_CIR_RADIUS+2;
            Log.v(tag, "-----------topLineLength:"+topLineLength + "   getPopupHeight"+getPopupHeight());
//        }else{
//            topLineLength = MIN_TOP_LINE_LENGTH;
//            Log.v(tag, "-----------  topLineLength:"+topLineLength + "     getPopupHeight"+getPopupHeight());
//        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackgroundLines(canvas);
        drawLines(canvas);
        drawDots(canvas);
        for(int k=0; k < drawDotLists.size(); k++){
        	float MaxValue = Collections.max(dataLists.get(k));
        	float MinValue = Collections.min(dataLists.get(k));
        	for(Dot d: drawDotLists.get(k)){
        		if(showPopupType == SHOW_POPUPS_All)
        			drawPopup(canvas, String.valueOf(d.data), d.getPoint(),popupColorArray[k%3]);
        		else if(showPopupType == SHOW_POPUPS_MAXMIN_ONLY){
        			if(d.data == MaxValue)
        				drawPopup(canvas, String.valueOf(d.data), d.getPoint(),popupColorArray[k%3]);
        			if(d.data == MinValue)
        				drawPopup(canvas, String.valueOf(d.data), d.getPoint(),popupColorArray[k%3]);
        		}
        	}
        }
}
    private void drawPopup(Canvas canvas,String num, Point point,int PopupColor){
        boolean singularNum = (num.length() == 1);
        int sidePadding = MyUtils.dip2px(getContext(),singularNum? 8:5);
        int x = point.x;
        int y = point.y-MyUtils.dip2px(getContext(),5);
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds(num,0,num.length(),popupTextRect);
        Rect r = new Rect(x-popupTextRect.width()/2-sidePadding,
                y - popupTextRect.height()-bottomTriangleHeight-popupTopPadding*2-popupBottomMargin,
                x + popupTextRect.width()/2+sidePadding,
                y+popupTopPadding-popupBottomMargin);

        NinePatchDrawable popup = (NinePatchDrawable)getResources().getDrawable(PopupColor);
        popup.setBounds(r);
        popup.draw(canvas);
        canvas.drawText(num, x, y-bottomTriangleHeight-popupBottomMargin, popupTextPaint);
    }
    /**
     *
     * @param canvas  The canvas you need to draw on.
     * @param point   The Point consists of the x y coordinates from left bottom to right top.
     *                Like is              3
     *                2
     *                1
     *                0 1 2 3 4 5
     */
    private void drawPopup(Canvas canvas,String num, Point point){
        boolean singularNum = (num.length() == 1);
        int sidePadding = MyUtils.dip2px(getContext(),singularNum? 8:5);
        int x = point.x;
        if(mShowYCoordinate == true) x += YCOORD_TEXT_LEFT_MARGIN;
        int y = point.y-MyUtils.dip2px(getContext(),5);
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds(num,0,num.length(),popupTextRect);
        Rect r = new Rect(x-popupTextRect.width()/2-sidePadding,
                y - popupTextRect.height()-bottomTriangleHeight-popupTopPadding*2-popupBottomMargin,
                x + popupTextRect.width()/2+sidePadding,
                y+popupTopPadding-popupBottomMargin);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.popup_red);
        byte chunk[] = bmp.getNinePatchChunk();
        NinePatchDrawable popup = new NinePatchDrawable(bmp, chunk, new Rect(), null);
        popup.setBounds(r);
        popup.draw(canvas);
        canvas.drawText(num, x, y-bottomTriangleHeight-popupBottomMargin, popupTextPaint);
    }

    private int getPopupHeight(){
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds("9",0,1,popupTextRect);
        Rect r = new Rect(-popupTextRect.width()/2,
                 - popupTextRect.height()-bottomTriangleHeight-popupTopPadding*2-popupBottomMargin,
                 + popupTextRect.width()/2,
                +popupTopPadding-popupBottomMargin);
        return r.height();
    }

    private void drawDots(Canvas canvas){
//        Log.v("tag", "----canvas:w:"+ canvas.getWidth() + " h:" + canvas.getHeight() + " " );
        Paint bigCirPaint = new Paint();
        bigCirPaint.setAntiAlias(true);
        Paint smallCirPaint = new Paint(bigCirPaint);
        smallCirPaint.setColor(Color.parseColor("#FFFFFF"));
        if(drawDotLists!=null && !drawDotLists.isEmpty()){
        	for(int k=0; k < drawDotLists.size(); k++){	
        		bigCirPaint.setColor(Color.parseColor(colorArray[k%3]));
        		for(Dot dot : drawDotLists.get(k)){
        		    int x = dot.x;
        		    if(mShowYCoordinate == true) x += YCOORD_TEXT_LEFT_MARGIN;
                	canvas.drawCircle(x,dot.y,DOT_OUTER_CIR_RADIUS,bigCirPaint);
                	canvas.drawCircle(x,dot.y,DOT_INNER_CIR_RADIUS,smallCirPaint);
//                	Log.v(tag, "----dot.x" + dot.x + " dot.y:" + dot.y);
            	}
        	}
        }
    }

    private void drawLines(Canvas canvas){
        Paint linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(MyUtils.dip2px(getContext(), 2));
        for(int k = 0; k<drawDotLists.size(); k ++){
        	linePaint.setColor(Color.parseColor(colorArray[k%3]));
	        for(int i=0; i<drawDotLists.get(k).size()-1; i++){
	            int x1 = drawDotLists.get(k).get(i).x;
	            int x2 = drawDotLists.get(k).get(i+1).x;
	            if (mShowYCoordinate){
	                x1 += YCOORD_TEXT_LEFT_MARGIN;
	                x2 += YCOORD_TEXT_LEFT_MARGIN;
	            }
	            canvas.drawLine(x1,
	                    drawDotLists.get(k).get(i).y,
	                    x2,
	                    drawDotLists.get(k).get(i+1).y,
	                    linePaint);
	        }
        }
    }

    private void drawBackgroundLines(Canvas canvas){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(MyUtils.dip2px(getContext(),1f));
        paint.setColor(BACKGROUND_LINE_COLOR);

        //draw vertical lines
        for(int i=0; i<xCoordinateList.size(); i++){
        	int x = xCoordinateList.get(i);
        	if(mShowYCoordinate == true) {
        	    x += YCOORD_TEXT_LEFT_MARGIN;
        	}
            canvas.drawLine(x, 0, x,
                    mViewHeight - bottomTextTopMargin - bottomTextHeight-bottomTextDescent,
                    paint);
        }

        //画横线
        for(int i=0; i<yCoordinateList.size(); i++){
            if((yCoordinateList.size()-1-i) % dataOfAGird == 0){
            	int y = yCoordinateList.get(i);
                canvas.drawLine(0, y, getWidth(), y, paint);
                
                if(mShowYCoordinate == true)
                	canvas.drawText(String.valueOf(yCoordinateList.size()-i-1), 0, y, ycoordTextPaint);
            }
        }
        //draw bottom text
        if(bottomTextList != null){
            for(int i=0;i<bottomTextList.size();i++){
            	int x = sideLineLength+backgroundGridWidth*i;
            	if(mShowYCoordinate == true) x += YCOORD_TEXT_LEFT_MARGIN;
                canvas.drawText(bottomTextList.get(i), x,
                        mViewHeight-bottomTextDescent, bottomTextPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mViewWidth = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        refreshAfterDataChanged();
        setMeasuredDimension(mViewWidth,mViewHeight);
    }

    private int measureWidth(int measureSpec){
        int horizontalGridNum = getHorizontalGridNum();
        int preferred = backgroundGridWidth*horizontalGridNum+sideLineLength*2;
//        Log.v(tag, "----measureWidth----backgroundGridWidth:" + backgroundGridWidth +" sideLineLength:"+ sideLineLength + " preferred:"+preferred);
        return getMeasurement(measureSpec, preferred);
        
    }

    private int measureHeight(int measureSpec){
        int preferred = 0;
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred){
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement;
        switch(MeasureSpec.getMode(measureSpec)){
            case MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();
        Region r = new Region();
        int width = backgroundGridWidth/2;
        if(drawDotLists != null || !drawDotLists.isEmpty()){
	        for(ArrayList<Dot> data : drawDotLists){
	        	for(Dot dot : data){
	        		r.set(dot.x-width,dot.y-width,dot.x+width,dot.y+width);
	                if (r.contains(point.x,point.y) && event.getAction() == MotionEvent.ACTION_DOWN){
	                    selectedDot = dot;
	                }else if (event.getAction() == MotionEvent.ACTION_UP){
	                    if (r.contains(point.x,point.y)){
	                        showPopup = true;
	                    }
	                }
	            }
	        }
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_UP){
            postInvalidate();
        }
        return true;
    }
    
    private int updateSelf(int origin, int target, int velocity){
        if (origin < target) {
            origin += velocity;
        } else if (origin > target){
            origin-= velocity;
        }
        if(Math.abs(target-origin)<velocity){
            origin = target;
        }
        return origin;
    }
    
    class Dot{
        int x;
        int y;
        float data;//为了显示popup用的
        int targetX;
        int targetY;
        int linenumber;
        int velocity = MyUtils.dip2px(getContext(),18);

        Dot(int x,int y,int targetX,int targetY,Float data,int linenumber){
            this.x = x;
            this.y = y;
            this.linenumber = linenumber;
            setTargetData(targetX, targetY,data,linenumber);
        }

        Point getPoint(){
            return new Point(x,y);
        }

        Dot setTargetData(int targetX,int targetY,Float data,int linenumber){
            this.targetX = targetX;
            this.targetY = targetY;
            this.data = data;
            this.linenumber = linenumber;
            return this;
        }

        boolean isAtRest(){
            return (x==targetX)&&(y==targetY);
        }

        void update(){
            x = updateSelf(x, targetX, velocity);
            y = updateSelf(y, targetY, velocity);
        }

        private int updateSelf(int origin, int target, int velocity){
            if (origin < target) {
                origin += velocity;
            } else if (origin > target){
                origin-= velocity;
            }
            if(Math.abs(target-origin)<velocity){
                origin = target;
            }
            return origin;
        }
    }
}