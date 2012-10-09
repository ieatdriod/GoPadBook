package com.gsimedia.gsiebook;

import java.util.ArrayList;

import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.gsiebook.lib.FindResult;
import com.gsimedia.gsiebook.lib.FinderObserver;
import com.gsimedia.gsiebook.lib.FinderSingleton;
import com.gsimedia.gsiebook.lib.GSiFindResultAdapter;
import com.taiwanmobile.myBook_PAD.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class FindResultActivity extends ListActivity implements FinderObserver , OnScrollListener{

	public static final String KEY_FindText = "findtext";
	private static final int SEARCH_MAX = 50;
	private ArrayList<FindResult> iResultArray = null;
	private GSiFindResultAdapter iAdapter = null;
	
	private InputMethodManager imm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsi_finder);
		iResultArray = new ArrayList<FindResult>();
		iResultArray.clear();
		
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		handleIntent(getIntent());
		findViews();

		iAdapter = new GSiFindResultAdapter(this,iResultArray);
		this.getListView().setAdapter(iAdapter);
		this.getListView().setOnScrollListener(this);
		
	}
	@Override
	protected void onPause() {
		FinderSingleton.getInstance().cancelFind();
		FinderSingleton.getInstance().unregistObserver(this);
		iResultArray.clear();
		super.onPause();
	}
	@Override
	protected void onResume() {
		FinderSingleton.getInstance().registObserver(this);
		FinderSingleton.getInstance().find(iFindText, 0, SEARCH_MAX);
		iEmptyView.setText(R.string.GSI_SEARCHING);
		bHasMore = false;
		iAdapter.setHasMore(bHasMore);
		iAdapter.notifyDataSetChanged();
		super.onResume();
	}
	/**
	 * find views
	 */
	private EditText iTextView = null;
	private TextView iEmptyView = null;
	private ImageButton iBackButton = null;
	private ImageButton iFindButton = null;
	private void findViews(){
		iEmptyView = (TextView) findViewById(android.R.id.empty);
		iTextView = (EditText) findViewById(R.id.searchKey);
		iTextView.setText(iFindText);
		iTextView.setOnEditorActionListener(new OnEditorActionListener(){

			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					iFindButton.performClick();
		        }
				return false;
			}
			
		});
		iBackButton = (ImageButton) findViewById(R.id.SearchBack);
		iBackButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				FinderSingleton.getInstance().cancelFind();
				finish();
			}
		});
		
		iFindButton = (ImageButton)findViewById(R.id.btnSearchBegin);
		iFindButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				imm.hideSoftInputFromWindow(iTextView.getWindowToken(), 0);
				reFind();
			}
		});
	}
	private void reFind(){
		if (iTextView.getText().toString().trim().length() > 0){
			//disable button prevent click again
			iFindButton.setClickable(false);
			//start a new search
			iFindText = iTextView.getText().toString();
			
		    iResultArray.clear();
		    FinderSingleton.getInstance().find(iFindText, 0, SEARCH_MAX);
		    iEmptyView.setText(R.string.GSI_SEARCHING);
		    bHasMore = false;
		    iAdapter.setHasMore(bHasMore);
		    iAdapter.notifyDataSetChanged();
	    }else{
	    	Toast.makeText(getApplicationContext(), getString(R.string.GSI_SEARCH_NOTEXT_HINT), Toast.LENGTH_SHORT).show();
	    }
	}
	/**
	 * handle intent
	 * @param aIntent
	 */
	private String iFindText = null;
	private void handleIntent(Intent aIntent){
		//get text to find
		iFindText = aIntent.getExtras().getString(KEY_FindText);
	}
	private boolean bHasMore = false;
	public void NotifyFindComplete(int err, ArrayList<FindResult> aResult) {
		if(aResult.size()<SEARCH_MAX)
			bHasMore = false;
		else
			bHasMore = true;
		
		// find complete
		iResultArray.addAll(aResult);
		Log.d(Config.LOGTAG,"got search result:"+aResult.size());
		// set empty view text
		if(iResultArray.size()==0){
			iEmptyView.setText(R.string.GSI_SEARCH_NOT_FOUND);
		}
			
		// notify list adapter to update
		iAdapter.setHasMore(bHasMore);
		iAdapter.notifyDataSetChanged();
		// enable button
		iFindButton.setClickable(true);
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(position>=iResultArray.size()){
			//get more find result
			/*
			if(iResultArray.size()>0){
				int aPage = iResultArray.get(iResultArray.size()-1).page+1;
				FinderSingleton.getInstance().find(iFindText, aPage, SEARCH_MAX);
			}else{
				bHasMore = false;
				iAdapter.notifyDataSetChanged();
			}
			*/
		}else{
			FinderSingleton.getInstance().cancelFind();
			//go back pdfview
			Intent intent = new Intent();
			Bundle aBundle = new Bundle();
			intent.setClass(this, RendererActivity.class);
			aBundle.putString(RendererActivity.KEY_FindText, this.iFindText);
			aBundle.putParcelable(RendererActivity.KEY_Find, this.iResultArray.get(position));
			intent.putExtras(aBundle);
			this.setResult(RendererActivity.Result_Find, intent);
			finish();
		}
		super.onListItemClick(l, v, position, id);
	}
	public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {

        boolean loadMore = /* maybe add a padding */
            firstVisible + visibleCount >= totalCount;

        if(loadMore) {
        	//get more find result
			if(iResultArray.size()>0){
				if(bHasMore){
					if(iFindText.trim().length()>0){
						int aPage = iResultArray.get(iResultArray.size()-1).page+1;
						FinderSingleton.getInstance().find(iFindText, aPage, SEARCH_MAX);
					}else{
						bHasMore = false;
						iAdapter.notifyDataSetChanged();
					}
				}
			}else{
				bHasMore = false;
				iAdapter.notifyDataSetChanged();
			}
        }
	}
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}
	

}
