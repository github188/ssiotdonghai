package com.ssiot.donghai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ssiot.donghai.myzxing.MipcaActivityCapture;

public class HistoryFragment extends BaseFragment{
    public static final String tag = "HisFragment";
    private FHisBtnClickListener mFHisBtnClickListener;
    EditText mEditText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        String userkey = getArguments().getString("userkey");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        ImageButton cameraButton = (ImageButton) rootView.findViewById(R.id.startcamera);
        Button searchButton = (Button) rootView.findViewById(R.id.startsearch);
        mEditText = (EditText) rootView.findViewById(R.id.qrcode_edit);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(getActivity(), MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, REQUEST_CODE_F_SCAN);
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getActivity(), R.string.building, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    private static final int REQUEST_CODE_F_SCAN = 1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(tag, "------onActivityResult----" + requestCode +resultCode);
        switch (requestCode) {
            case REQUEST_CODE_F_SCAN:
                if (resultCode == Activity.RESULT_OK){
                    if (null != mEditText){
                        Bundle bundle = data.getExtras();
                        mEditText.setText(bundle.getString("result"));
                    }
                }
                break;

            default:
                break;
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
//        inflater.inflate(R.menu.his, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Log.v(tag, "----------------action-settting");
//                break;
//
//            default:
//                break;
//        }
        return true;
    }
    
    @Override
    public boolean canGoback(){
        return false;
    }
    
    @Override
    public void onMyBackPressed(){//add by jingbo
    }
    
    public void setClickListener(FHisBtnClickListener listen){
        mFHisBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FHisBtnClickListener {  
        void onFHisBtnClick();  
    }
}