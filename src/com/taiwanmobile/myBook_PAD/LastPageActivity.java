package com.taiwanmobile.myBook_PAD;

import org.iii.ideas.android.general.AndroidLibrary;
import org.iii.ideas.reader.last_page.LastPageHelper;

import com.gsimedia.sa.DeviceIDException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * 上傳最後閱讀頁的activity，離開閱讀時將reader activity finish，移到此activity開啟上傳最後閱讀頁的thread後即finish回到書櫃
 * @author III
 * 
 */
public class LastPageActivity extends Activity{
	private static final int SHOW_TOAST=0;
	private final ThreadHandler handler = new ThreadHandler();
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent it = getIntent();
		final int span = it.getIntExtra("span", 0);
		final int idx = it.getIntExtra("idx", 0);
		final boolean sync = it.getBooleanExtra("sync", false);
		final String deliverId = it.getStringExtra("id");
		final String title = it.getStringExtra("title");
		final String secName = it.getStringExtra("sec");
		final LastPageHelper lastHelper = new LastPageHelper(getApplicationContext());
		new Thread(){
			public void run(){
				try {
		    		if(sync /*&& AndroidLibrary.is3gConnected(LastPageActivity.this)*/){
		    			//thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_PROGRESS,getResources().getString(R.string.iii_last_page_upload_progress)));
		    			boolean isSuccessful = lastHelper.uploadXml(deliverId, title, secName, span, idx, System.currentTimeMillis() );
		    			if(isSuccessful){
		    				Log.d("!!!!!!!upload","success");
		    				//Toast.makeText(this, getResources().getString(R.string.iii_last_page_upload_success), Toast.LENGTH_SHORT);
		    				handler.sendMessage(handler.obtainMessage(SHOW_TOAST,getResources().getString(R.string.iii_last_page_upload_success)));
		    				//Toast.makeText(Reader.this.getBaseContext(), getResources().getString(R.string.iii_last_page_upload_success), Toast.LENGTH_SHORT);
		    				//Toast.makeText(this, getResources().getString(R.string.iii_last_page_upload_success), Toast.LENGTH_SHORT).show();
		    			}else{
		    				Log.d("!!!!!!!upload","no");
		    			}
		    		}
				}catch(DeviceIDException e){
					handler.sendMessage(handler.obtainMessage(SHOW_TOAST,e.getMessage()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d("!!!!!!!upload","no");
					e.printStackTrace();
				}/*tfinally{
					ry {
						if(progress.isShowing())
							progress.dismiss();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
			}
		}.start();
		finish();
	}
	
	private class ThreadHandler extends Handler{
    	public void handleMessage(Message msg) {
    		try {
				switch( msg.what ){
					case SHOW_TOAST:		
						Toast.makeText(LastPageActivity.this.getApplicationContext(), (String)msg.obj, Toast.LENGTH_SHORT).show();
						break;   
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
    	
    }
}
