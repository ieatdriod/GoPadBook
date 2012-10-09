package com.taiwanmobile.myBook_PAD;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
/**
 * 透過webview的方式開啓網頁
 * @author III
 * 
 */
public class TWMWebView extends Activity {
    /** Called when the activity is first created. */
    @Override 
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("In","webView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iii_wv);
        WebView wv = (WebView) findViewById(R.id.wv);
        Bundle bundle = this.getIntent().getExtras();
        String strURI = bundle.getString("url");
        wv.loadUrl(strURI);
    }
}