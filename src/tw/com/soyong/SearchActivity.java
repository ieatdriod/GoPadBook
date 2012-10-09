package tw.com.soyong;

import java.util.ArrayList;
import java.util.HashMap;

import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.mebook.MebookToken;
import tw.com.soyong.mebook.SySentence;
import tw.com.soyong.utility.Util;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 搜尋Mebook書本內容
 * @author Victor
 *
 */
public class SearchActivity extends ListActivity{
	
//	static public final int DIR_MODE = 0 ;
//	static public final int PRO_FILE_MODE = 1;
//	static public final int MEBOOK_FILE_MODE = 2;
	
//	static public final String MODE_KEY = "mode";
//	static public final String CUR_PATH_KEY = "curPath";
	
    ArrayList<String> mResult = new ArrayList<String>();
    ArrayList<Integer> mSentIndex = new ArrayList<Integer>();
    int mMode = 0 ;
    private InputMethodManager imm;
    
    private EditText etKey;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        // Tell the list view which view to display when the list is empty
        getListView().setEmptyView(findViewById(R.id.empty));
        
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        
        final GradientDrawable lvSelector =new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(84, 145, 192), Color.rgb(2, 72, 131)});
        
        getListView().setSelector(lvSelector);
        
        etKey = (EditText)findViewById(R.id.searchKey);
        
        final ImageButton imgBtn_Back = (ImageButton) findViewById(R.id.SearchBack);
        
        imgBtn_Back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});        
        
        ImageButton iFindButton = (ImageButton)findViewById(R.id.btnSearchBegin);
        iFindButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				imm.hideSoftInputFromWindow(etKey.getWindowToken(), 0);
				search();
			}
		});
    }
    
   
    public  boolean	dispatchKeyEvent(KeyEvent event){
    	if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
    		if(event.getAction()==KeyEvent.ACTION_UP){
    			imm.hideSoftInputFromWindow(etKey.getWindowToken(), 0);
    			search();
    		}
    		return true;
    	}
//    	else if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
//			finish();
//			return true;
//    	}
    	return super.dispatchKeyEvent(event);
    }


    protected void search() {
    	
		final String key = etKey.getText().toString().trim().toLowerCase();
		
		if ( key.length() <= 0 ){
			return ;
		}
		
		final ProgressDialog progDlg = ProgressDialog.show(this,
				getText(R.string.progress_title),
				getText(R.string.search_message), true);
		
		final SySentence[] arr = MebookHelper.mSentenceArr;
		
		
        final Handler aHandler = new Handler(); 
        final Runnable updateResults = new Runnable() { 
            public void run() { 
                updateResults(); 
            } 
        }; 
        
		mResult.clear();
		mSentIndex.clear();
		
		TextView tv = (TextView)findViewById(R.id.empty);
		tv.setText(R.string.no_data);
        
        new Thread( new Runnable() { 
        	
        	@Override
            public void run() {        

				HashMap<Integer, String> data;
				String org ;
				String trl ;
				String content ;
				String compare ;

				int i = 0 ;
				for ( SySentence sent: arr ){
					data = sent.mData;
					org = data.get(MebookToken.TOK_ORG);
					trl = data.get(MebookToken.TOK_TRL);
					
					if ( null == org){
						org = "" ;
					}
					if ( null == trl){
						trl = "" ;
					}
						
					
					content = org + " " + trl ;
					compare = content.toLowerCase();
						
					if ( compare.indexOf(key)>=0){
						content = content.replace('\n', ' ');
						content = content.replace('\r', ' ');
						
						StringBuilder sb = new StringBuilder(content);
						Util.removeFontTag(content, sb);
						Util.removePhonetic(sb);
						content = sb.toString();
						
						mResult.add(content);
						mSentIndex.add(i);
					}
					i++;
				}
					
				aHandler.post(updateResults); 
				progDlg.dismiss();
			}
		}).start();	
	}


	protected void updateResults() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				R.layout.list_item, mResult); 
         
		setListAdapter(adapter);
		setSelection(0); 
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		int i = mSentIndex.get(position);
		returnResult(i);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			Intent intent = new Intent();
			setResult(RESULT_CANCELED, intent);
			finish();			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void returnResult(int index) {
		Bundle bundle = new Bundle();
		bundle.putInt(MeReaderActivity.CUR_INDEX, index);
		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}
}
