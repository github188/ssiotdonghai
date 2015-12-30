package com.ssiot.donghai;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils {
    public static final String PREF_AUTOUPDATE = "autoupdate";
    
    public static void setStringToFile(String str){
        try {
            File f = new File("/sdcard/sqlcmd.txt");
            FileOutputStream fo = new FileOutputStream(f);
            byte[] bytes = str.getBytes();
            fo.write(bytes);
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }
    
    public static Dialog createLoadingDialog(Context context, String msg) {  
        LayoutInflater inflater = LayoutInflater.from(context);  
        View v = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view  
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局  
        // main.xml中的ImageView  
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);  
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字  
        // 加载动画  
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
                context, R.anim.loading_animation);  
        // 使用ImageView显示动画  
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);  
        tipTextView.setText(msg);// 设置加载信息  
  
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog  
  
        loadingDialog.setCancelable(false);// 不可以用“返回键”取消  
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(  
                LinearLayout.LayoutParams.MATCH_PARENT,  
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局  
        return loadingDialog;
    }
    
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    
    public static void changePicture(Context c){
        BitmapDrawable b = (BitmapDrawable) c.getResources().getDrawable(R.drawable.video_rtsp_6);
        Bitmap img = b.getBitmap();
        int width = img.getWidth();         //获取位图的宽    
        int height = img.getHeight();       //获取位图的高    
            
        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组    
            
        img.getPixels(pixels, 0, width, 0, 0, width, height);    
        int alpha = 0xFF << 24;     
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {    
                int grey = pixels[width * i + j];    
                    
                int red = ((grey  & 0x00FF0000 ) >> 16);    
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);    
                    
//                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);    
//                grey = alpha | (grey << 16) | (grey << 8) | grey;  
//                grey = alpha | (grey & 0x0000FF00);
                grey = (grey & 0xff00FF00);
                grey = ((grey << 17) -1) & grey;
                if ((grey & 0x0000ff00) < 0x00001100 ){
                    grey = (0x00 <<24) & grey;
                    grey = 0x00ffffff;
                }
                
                //--------------------------------
                int a = ((grey  & 0xff000000 ) >> 24);
                grey = (a<<24) | 0x69c911;
                //--------------------------------
                pixels[width * i + j] = grey;
            }    
        }    
        Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        
        File f = new File("/sdcard/mmm3.png");
        try {
            FileOutputStream out = new FileOutputStream(f);
            result.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void changePic2(Context c){
        BitmapDrawable b = (BitmapDrawable) c.getResources().getDrawable(R.drawable.connect_fail_2);
        Bitmap img = b.getBitmap();
        int width = img.getWidth();         //获取位图的宽    
        int height = img.getHeight();       //获取位图的高    
        Log.v("Utils", "--------changePic2-------" + width + " h:"+ height);
        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组    
            
        img.getPixels(pixels, 0, width, 0, 0, width, height);    
        int alpha = 0xFF << 24;     
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {    
                int grey = pixels[width * i + j];    
                    
                int red = ((grey  & 0x00FF0000 ) >> 16);    
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);    
                    
//                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);    
//                grey = alpha | (grey << 16) | (grey << 8) | grey;  
//                grey = alpha | (grey & 0x0000FF00);
                int a = ((grey  & 0xff000000 ) >> 24);  
                
                grey = a | (150 << 16 )| (150 <<8) | 150;
                grey = (a<<24) | 0x999999;
                pixels[width * i + j] = grey;
            }    
        }    
        Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        
        File f = new File("/sdcard/mmm3.png");
        try {
            FileOutputStream out = new FileOutputStream(f);
            result.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    public static HashMap<String, String> parseXml(InputStream inStream) throws Exception {   
        HashMap<String, String> hashMap = new HashMap<String, String>();
    
        // 实例化一个文档构建器工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 通过文档构建器工厂获取一个文档构建器
        DocumentBuilder builder = factory.newDocumentBuilder();
        // 通过文档通过文档构建器构建一个文档实例
        Document document = builder.parse(inStream);
        //获取XML文件根节点
        Element root = document.getDocumentElement();
        //获得所有子节点
        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++)
        {
            //遍历子节点
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element) childNode;
            //版本号
                if ("version".equals(childElement.getNodeName()))
                {
                    hashMap.put("version",childElement.getFirstChild().getNodeValue());
                }
                //软件名称
                else if (("name".equals(childElement.getNodeName())))
                {
                    hashMap.put("name",childElement.getFirstChild().getNodeValue());
                }
                //下载地址
                else if (("url".equals(childElement.getNodeName())))
                {
                    hashMap.put("url",childElement.getFirstChild().getNodeValue());
                }
                //版本说明
                else if("versionInfo".equals(childElement.getNodeName()))
                {
                    hashMap.put("versionInfo",childElement.getFirstChild().getNodeValue());
                }
            }
        }
        return hashMap;
    }
    
    public static ArrayList<String> parseJSON_MultiTiming(String str){//定时的
        ArrayList<String> ret = new ArrayList<String>();
        try {
            JSONArray localJSONArray = new JSONArray(str);
            for (int i = 0; i < localJSONArray.length(); i++) {
                JSONObject groupJSONObject = localJSONArray.optJSONObject(i);
                ret.add(groupJSONObject.toString());
//                String id = groupJSONObject.getString("ID");
//                String startTime = groupJSONObject.getString("StartTime");
//                String endTime = groupJSONObject.getString("EndTime");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    
}