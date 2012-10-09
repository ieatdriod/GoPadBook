package com.taiwanmobile.myBook_PAD;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.iii.ideas.reader.parser.HtmlReceiver;
import org.iii.ideas.reader.search.KeywordSearcher;
import org.iii.ideas.reader.search.SearchCallback;
import org.iii.ideas.reader.search.SearchResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 處理閱讀搜尋
 * @author III
 * 
 */
public class ReaderSearch extends Activity implements SearchCallback{
	/*
	 * to do:
	 * 改用array adapter
	 * 最下面row裝載progress bar
	 */
	/** Called when the activity is first created. */
	private ImageButton ib_reader_search_back;
	private ImageButton iFindButton = null;
	
	private EditText et_reader_search_text_input;
	
	private ListView resultLv;
	private ProgressDialog progress;
	private ProgressBar progressBar;
	private String path;
	private ArrayList<String> spineList;
	private ThreadHandler thandler;
	private final int CHAPTER_FINISHED=0;
	private final int SEARCH_FINISHED=1;
	private InputMethodManager imm;
	private int threadIdx=0;
	private boolean isSearchStart=false;
	private ArrayList<SearchResult> results;
	private ArrayAdapter<SearchResult> adapter;
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iii_reader_search);
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        thandler = new ThreadHandler();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent it = getIntent();
        path = it.getStringExtra("epub_path");
        spineList = it.getStringArrayListExtra("spine_list");
        setViewComponent();
        setListener();
        progress = new ProgressDialog(this);
        progressBar = (ProgressBar) this.findViewById(R.id.progress_bar);
        //progressBar.setIndeterminate(true);
    }
    
	/**
	 * 提示訊息
	 * @param title 訊息title
	 * @param text 訊息文字
	 */
    private void showHint(String title, String text){
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog ad=builder.create();
		if(title!=null && title.length()>0)
			ad.setTitle(title);
		ad.setMessage(text);
		ad.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.iii_showAM_ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				ad.dismiss();
				startSearch();
			}
			
		});
		ad.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.iii_showAM_cancel), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				ad.dismiss();
			}
			
		});
		ad.show();
    }
	/**
	 * 暫停搜尋
	 */
    private void stopSearch(){
		isSearchStart=false;
    	threadIdx++;
    	if(progress!=null && progress.isShowing())
    		progress.dismiss();
    }
    
    private void clearAdapter(){
		if(adapter == null)
			adapter = new ArrayAdapter<SearchResult>(this, R.layout.iii_ditem_row, R.id.searchResult, new ArrayList<SearchResult>());
		else
			adapter.clear();
    }
    SearchResult dummy = new SearchResult();
	private void clearResultsList(){
		if(results==null)
			results = new ArrayList<SearchResult>();
		else
			results.clear();
    }
	
	private class DeviceIDHandler extends Handler{		
    	public void handleMessage(Message msg) {
    		Toast.makeText( getApplicationContext(), (String)msg.obj, Toast.LENGTH_LONG);    		
        }    	
    }
	DeviceIDHandler handle = new DeviceIDHandler();
	/**
	 * 開始搜尋
	 */
    private void startSearch(){

    	threadIdx++;
    	String kw = et_reader_search_text_input.getText().toString().trim();
    	if(kw.length()>0){
        	isSearchStart=true;
			//showProgress();
    		progressBar.setVisibility(View.VISIBLE);
    		progressBar.setProgress(0);
			clearResultsList();
			clearAdapter();
			resultLv.setAdapter(adapter);
			//addProgressBarAtFirst();
			
    		try {
        		KeywordSearcher searcher = new KeywordSearcher();
    			searcher.searchKW(threadIdx,kw, path, spineList, this.getApplicationContext(),this,handle);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			Log.e("ReaderSearch:startSearch",e.toString());
    	    	//if(progress!=null && progress.isShowing())
    	    		//progress.dismiss();
    		}
    	}
    }
	/**
	 * 按鍵事件處理
	 * @param event 事件 
	 * @return 事件是否被執行
	 */
    public  boolean	dispatchKeyEvent(KeyEvent event){
    	if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
    		if(event.getAction()==KeyEvent.ACTION_UP){
    			imm.hideSoftInputFromWindow(et_reader_search_text_input.getWindowToken(), 0);
    			showHint(null,getResources().getString(R.string.iii_search_hint));
    		}
    		return true;
    	}else if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
    		if(event.getAction()==KeyEvent.ACTION_UP){
    			if(isSearchStart){
    				stopSearch();
    			}else{
    				Intent it=new Intent();
    				Bundle result = new Bundle();
    				result.putBoolean("shouldJump", false);
    				it.putExtra("result", result);
    				setResult(RESULT_OK, it);
    				finish();
    			}
    		}
			return true;
    	}
    	return super.dispatchKeyEvent(event);
    }
	/**
	 * 設定畫面view
	 */
    private void setViewComponent() {        
    	et_reader_search_text_input = (EditText)findViewById(R.id.et_reader_search_text_input);
    	
    	ib_reader_search_back = (ImageButton)findViewById(R.id.ib_reader_search_back);
    	resultLv = (ListView)findViewById(R.id.lv_reader_search_result);
//    	ib_reader_search_back.setBackgroundColor(Color.TRANSPARENT);
//    	et_reader_search_text_input.setBackgroundColor(Color.TRANSPARENT);
//    	imm.showSoftInput(et_reader_search_text_input, 0);
    	
    	iFindButton = (ImageButton)findViewById(R.id.btnSearchBegin);
    	
    }
	/**
	 * 設定畫面view Listener
	 */
	private void setListener() {
		ib_reader_search_back.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				Intent it=new Intent();
				Bundle result = new Bundle();
				result.putBoolean("shouldJump", false);
				it.putExtra("result", result);
				setResult(RESULT_OK, it);
				finish();
      	  	}
        });
		
		resultLv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				// TODO Auto-generated method stub
				if(results !=null && pos<results.size() && pos>=0){
					Intent it=new Intent();
					Bundle result = new Bundle();
					result.putBoolean("shouldJump", true);
					result.putString("chap", results.get(pos).chapterName);
					//Log.d("d","d");
					result.putInt("span", results.get(pos).span);
					result.putInt("idx", results.get(pos).idx);
					it.putExtra("result", result);
					setResult(RESULT_OK, it);
					finish();
				}
			}
			
		});
		
		iFindButton.setOnClickListener(new OnClickListener(){
			public synchronized void onClick(View v) {
				imm.hideSoftInputFromWindow(et_reader_search_text_input.getWindowToken(), 0);
	    		showHint(null,getResources().getString(R.string.iii_search_hint));
			}
		});
		
		/*this.resultLv.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(final AbsListView view, final int first, 
                    final int visible, final int total) {
				// TODO Auto-generated method stub
				if (visible < total && (first + visible == total)) {
		            Log.d("OnScrollListener - end of list", "fvi: " + 
		               first + ", vic: " + visible + ", tic: " + total);
		            // this line gets called twice
		            onLastListItemDisplayed(total, visible);
		        }

			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
			}
			
		});*/
    }
	
	/*protected void onLastListItemDisplayed(int total, int visible) {
		if (total <= results.size()) {
			// find last item in the list
			View item = resultLv.getChildAt(visible - 1);
			item.findViewById(R.id.searchResult).setVisibility(View.GONE);
			item.findViewById(R.id.progress).setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}*/
	/**
	 * 取得結果
	 * @param results_ 結果 
	 */
	@Override
	public void onGetResults(ArrayList<SearchResult> newResults, int prog) {
		// TODO Auto-generated method stub
		//Log.d("result","size:"+results.size());
		//if(newResults!=null && newResults.size()>0){
			//Log.e("JP","onGetResult:"+newResults.size());
			//for(int i=0;i<newResults.size();i++)
				//adapter.add(newResults.get(i));
			//Log.e("JP","onGetResult total:"+adapter.getCount());
			//results.addAll(newResults);
			thandler.sendMessage(thandler.obtainMessage(CHAPTER_FINISHED,prog,prog,newResults));
		//}
	}		
	/**
	 * 設定執行緒
	 */
	private class ThreadHandler extends Handler{
    	@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
    		switch( msg.what ){
 /*   			case SEARCH_FINISHED:				
    				progress.dismiss();
    				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(2);
    				Map<String, Object> map;
    				if(results==null || results.size()==0){
    					//text.add(results.get(i).description);
    					map = new HashMap<String, Object>();
    					map.put("searchResult", getResources().getString(R.string.iii_search_not_found));
    					list.add(map);
    				}else{
    					for(int i=0;i<results.size();i++){
        					//text.add(results.get(i).description);
        					map = new HashMap<String, Object>();
        					map.put("searchResult", results.get(i).description);
        					list.add(map);
        				}
    				}
    				SimpleAdapter notes = new SimpleAdapter(ReaderSearch.this, list, R.layout.iii_ditem_row,
    						new String[] { "searchResult"}, new int[] { R.id.searchResult});
    				lv_reader_search_result.setAdapter(notes);
    				lv_reader_search_result.setOnItemClickListener(new OnItemClickListener(){

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int pos, long arg3) {
							// TODO Auto-generated method stub
							if(results !=null && pos<results.size()){
								Intent it=new Intent();
								Bundle result = new Bundle();
								result.putBoolean("shouldJump", true);
								result.putString("chap", results.get(pos).chapterName);
								//Log.d("d","d");
								result.putInt("span", results.get(pos).span);
								result.putInt("idx", results.get(pos).idx);
								it.putExtra("result", result);
								setResult(RESULT_OK, it);
								finish();
							}
						}
    					
    				});
    				break;   */
    			case CHAPTER_FINISHED:				
    				/*progress.dismiss();
    				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(2);
    				Map<String, Object> map;
    				if(results==null || results.size()==0){
    					//text.add(results.get(i).description);
    					map = new HashMap<String, Object>();
    					map.put("searchResult", getResources().getString(R.string.iii_search_not_found));
    					list.add(map);
    				}else{
    					for(int i=0;i<results.size();i++){
        					//text.add(results.get(i).description);
        					map = new HashMap<String, Object>();
        					map.put("searchResult", results.get(i).description);
        					list.add(map);
        				}
    				}
    				SimpleAdapter notes = new SimpleAdapter(ReaderSearch.this, list, R.layout.iii_ditem_row,
    						new String[] { "searchResult"}, new int[] { R.id.searchResult});
    				resultLv.setAdapter(notes);*/
    				progressBar.setProgress(msg.arg1);
    				ArrayList<SearchResult> newResults = (ArrayList<SearchResult>) msg.obj;
    				if(newResults!=null){
        				for(int i=0;i<newResults.size();i++)
        					adapter.add( newResults.get(i));
        				//Log.e("JP","onGetResult total:"+adapter.getCount());
        				results.addAll(newResults);
    				}
    				break;   
    			case SEARCH_FINISHED:
    				progressBar.setVisibility(View.GONE);
    				if(results.size()==0)
    					Toast.makeText(ReaderSearch.this.getApplicationContext(), ReaderSearch.this.getString(R.string.iii_search_not_found), Toast.LENGTH_LONG).show();
    				break;
    		} 
        }
    	
    }



	@Override
	public int getThreadIdx() {
		return threadIdx;
	}

	@Override
	public void onSearchFinished() {
		// TODO Auto-generated method stub
		isSearchStart=false;
		thandler.sendMessage(thandler.obtainMessage(SEARCH_FINISHED));
		
		//adapter.remove(dummy);
		//adapter.notifyDataSetChanged();
		//this.removeProgress();
	}
}