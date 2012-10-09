package com.gsimedia.gsiebook;

import java.util.ArrayList;

import com.taiwanmobile.myBook_PAD.R;

import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.gsiebook.common.ImageTool;
import com.gsimedia.gsiebook.lib.GSiAnnotation;
import com.gsimedia.gsiebook.lib.GSiBookmark;
import com.gsimedia.gsiebook.lib.GSiContentAdapter;
import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter;
import com.gsimedia.gsiebook.lib.MarkResult;
import com.gsimedia.gsiebook.lib.Outline;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailActivity extends ListActivity {

	private ArrayList<Outline> iTOC = null;
	private ArrayList<GSiBookmark> iBookmarks = null;
	private ArrayList<GSiAnnotation> iAnnotations = null;
	private ArrayList<MarkResult> iMarkresults= null;
	private GSiContentAdapter iAdapter = null;
	private static final int IMAGE_SIZE = 500;
	
	
	private static final int EDisplayTOC = 0;
	private static final int EDisplayBookmark = 1;
	private static final int EDisplayAnnotation = 2;
	private static final int EDisplayMarker = 3;
	private static final int EDisplayInfo = 4;
	private int NowDisplay = 0;
	private GSiDatabaseAdapter iDatabase= null;
	private String iBookID = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsi_detail);
		findViews();
		iDatabase = new GSiDatabaseAdapter(getApplicationContext());
		
		Intent intent = getIntent();
		iTOC = getTOC(intent);
		iBookID = intent.getExtras().getString(RendererActivity.KEY_BookID);
		handleIntent(intent);
		
		iAdapter = new GSiContentAdapter(this,iTOC);
		this.getListView().setAdapter(iAdapter);
		
		this.displayToc();
	}
	private String iContentID = null;
	private String iAuthor = null;
	private String iPublisher = null;
	private String iTitle = null;
	private String iType = null;
	private String iCoverPath = null;
	private void handleIntent(Intent intent){
		iContentID = intent.getStringExtra(Config.KEY_content_id);
		iAuthor = intent.getStringExtra(Config.KEY_book_authors);
		iPublisher = intent.getStringExtra(Config.KEY_book_publisher);
		iTitle = intent.getStringExtra(Config.KEY_book_title);
		iType = intent.getStringExtra(Config.KEY_book_category);
		iCoverPath = intent.getStringExtra(Config.KEY_coverPath);
		Log.d(Config.LOGTAG,"got cover path:"+iCoverPath);
		
		iTypeView.setText(iType);
		iTitleView.setText(iTitle);
		iPublisherView.setText(iPublisher);
		iAuthorView.setText(iAuthor);
		/**
		 * set cover image
		 */
//		iCoverView.setImageURI(Uri.parse(iCoverPath));
		Bitmap aCoverImg = ImageTool.decodeImage(iCoverPath, null, IMAGE_SIZE, IMAGE_SIZE);
		iCoverView.setImageBitmap(aCoverImg);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		iDatabase.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		iDatabase.open();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(!iAdapter.getSelectionMode()){
			// set result 
			Intent intent = new Intent();
			Bundle aBundle = new Bundle();
			switch(NowDisplay){
			case EDisplayTOC:
				intent.setClass(this, RendererActivity.class);
				aBundle.putParcelable(RendererActivity.KEY_TOC, this.iTOC.get(position));
				intent.putExtras(aBundle);
				this.setResult(RendererActivity.Result_TOC, intent);
				finish();
				break;
			case EDisplayBookmark:
				intent.setClass(this, RendererActivity.class);
				aBundle.putParcelable(RendererActivity.KEY_Bookmark, this.iBookmarks.get(position));
				intent.putExtras(aBundle);
				this.setResult(RendererActivity.Result_Bookmark, intent);
				finish();
				break;
			case EDisplayMarker:
				intent.setClass(this, RendererActivity.class);
				aBundle.putInt(RendererActivity.KEY_Marker, this.iMarkresults.get(position).page);
				intent.putExtras(aBundle);
				this.setResult(RendererActivity.Result_Marker, intent);
				finish();
				break;
			case EDisplayAnnotation:
				intent.setClass(this, RendererActivity.class);
				aBundle.putParcelable(RendererActivity.KEY_Annotation, this.iAnnotations.get(position));
				intent.putExtras(aBundle);
				this.setResult(RendererActivity.Result_Annotation, intent);
				finish();
				break;
			}
		}else{
			SparseBooleanArray aItems = iAdapter.getCheckedItems();
			boolean oldValue = aItems.get(position, false);
            aItems.put(position, !oldValue);
            iAdapter.setCheckedItems(aItems);
		}
	}
	private Button iBackButton = null;
	private Button iDelButton = null;
	
	private Button iTocButton = null; //@+id/gsimedia_btn_toc
	private Button iBookmarkButton = null;
	private Button iMarkerButton = null;
	private Button iAnnoButton = null;
	private Button iInfoButton = null;
	private Button iRateButton = null;
	
	private RelativeLayout iInfoView = null;
	
	private TextView iEmpty = null;
	
	//book info related
	private TextView iTypeView = null;
	private TextView iTitleView = null;
	private TextView iPublisherView = null;
	private TextView iAuthorView = null;
	private ImageView iCoverView = null;
	private ImageView iTitleBarView = null;
	private void findViews(){
		iTitleBarView = (ImageView)findViewById(R.id.gsimedia_title);
		iInfoView = (RelativeLayout) findViewById(R.id.bookinfo);
		iTypeView = (TextView)findViewById(R.id.type);
		iTitleView = (TextView)findViewById(R.id.title);
		iPublisherView = (TextView)findViewById(R.id.publisher);
		iAuthorView = (TextView)findViewById(R.id.author);
		iCoverView = (ImageView)findViewById(R.id.cover);
		
		iEmpty = (TextView) getListView().getEmptyView();
		iBackButton = (Button) findViewById(R.id.gsimedia_btn_title_left);
		iBackButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				finish();
			}
			
		});
		iDelButton = (Button) findViewById(R.id.gsimedia_btn_title_right);
		iDelButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// show/hide check-box of list
				if(iAdapter.getSelectionMode()){
					iAdapter.setSelectionMode(false);
					switch(NowDisplay){
					case EDisplayBookmark:
						removeSelectedBookmark();
						break;
					case EDisplayAnnotation:
						removeSelectedAnnotation();
						break;
					case EDisplayMarker:
						removeSelectedMarker();
						break;
					}
				}else{
					iAdapter.setSelectionMode(true);
				}
				iAdapter.notifyDataSetChanged();
			}
			
		});
		
		iTocButton = (Button) findViewById(R.id.gsimedia_btn_toc);
		iTocButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				displayToc();
			}
			
		});
		iBookmarkButton = (Button) findViewById(R.id.gsimedia_btn_bookmark);
		iBookmarkButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				displayBookmark();
			}
			
		});
		iMarkerButton = (Button) findViewById(R.id.gsimedia_btn_marker);
		iMarkerButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				displayMarker();
			}
			
		});
		iAnnoButton = (Button) findViewById(R.id.gsimedia_btn_annotation);
		iAnnoButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				displayAnnotation();
			}
			
		});
		iInfoButton = (Button) findViewById(R.id.gsimedia_btn_info);
		iInfoButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				displayInfo();
			}
			
		});
		iRateButton = (Button) findViewById(R.id.more);
		iRateButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				String url = getString(R.string.web_api_rating)+iContentID;
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
			
		});
 	}
	private void displayToc(){
//		iTitleBarView.setText("目錄跳頁");
		iTitleBarView.setImageResource(R.drawable.gsi_title01);
		this.iInfoView.setVisibility(View.INVISIBLE);
		this.clearSelectedMode();
		iDelButton.setVisibility(View.INVISIBLE);
		iEmpty.setText(R.string.GSI_NO_TOC);
		this.NowDisplay = EDisplayTOC;
		this.iAdapter.setOutlines(iTOC);
		iAdapter.notifyDataSetChanged();
	}
	private void displayBookmark(){
//		iTitleBarView.setText("書籤索引");
		iTitleBarView.setImageResource(R.drawable.gsi_title02);
		this.iInfoView.setVisibility(View.INVISIBLE);
		this.clearSelectedMode();
		iDelButton.setVisibility(View.VISIBLE);
		iEmpty.setText("");
		this.NowDisplay = EDisplayBookmark;
		iBookmarks = iDatabase.getBookmarks(iBookID,this,this.iTitle);
		this.iAdapter.setBookmarks(iBookmarks);
		iAdapter.notifyDataSetChanged();
	}
	private void displayMarker(){
//		iTitleBarView.setText("劃線索引");
		iTitleBarView.setImageResource(R.drawable.gsi_title03);
		this.iInfoView.setVisibility(View.INVISIBLE);
		this.clearSelectedMode();
		iDelButton.setVisibility(View.VISIBLE);
		iEmpty.setText("");
		this.NowDisplay = EDisplayMarker;
		iMarkresults= iDatabase.getMarkers(iBookID, false);
		this.iAdapter.setMarkers(iMarkresults);
		iAdapter.notifyDataSetChanged();
	}
	private void displayAnnotation(){
//		iTitleBarView.setText("註記索引");
		iTitleBarView.setImageResource(R.drawable.gsi_title04);
		this.iInfoView.setVisibility(View.INVISIBLE);
		this.clearSelectedMode();
		iDelButton.setVisibility(View.VISIBLE);
		iEmpty.setText("");
		this.NowDisplay = EDisplayAnnotation;
		this.iAnnotations = iDatabase.getAnnotations(iBookID);
		this.iAdapter.setAnnotations(this.iAnnotations);
		iAdapter.notifyDataSetChanged();
	}
	private void displayInfo(){
//		iTitleBarView.setText("本書資訊");
		iTitleBarView.setImageResource(R.drawable.gsi_title05);
		this.clearSelectedMode();
		iDelButton.setVisibility(View.INVISIBLE);
		iEmpty.setText("");
		this.NowDisplay = EDisplayInfo;
		this.iInfoView.setVisibility(View.VISIBLE);
	}
	private ArrayList<Outline> getTOC(Intent aIntent){
		if(aIntent == null)
			return null;
		ArrayList<Outline> aResult = aIntent.getExtras().getParcelableArrayList(RendererActivity.KEY_TOC);
		return aResult;
	}
	private void clearSelectedMode(){
		iAdapter.setSelectionMode(false);
		SparseBooleanArray aSelected = iAdapter.getCheckedItems();
		aSelected.clear();
		iAdapter.setCheckedItems(aSelected);
	}
	private void removeSelectedBookmark(){
		if(iBookmarks == null){
			Log.e(Config.LOGTAG,"removeSelectedBookmark iBookmarks = null");
			return;
		}
		SparseBooleanArray aSelected = iAdapter.getCheckedItems();
		for(int i=0;i<this.iBookmarks.size();i++){
			if(aSelected.get(i, false))
				iDatabase.removeBookmark(iBookID, iBookmarks.get(i));
		}
		aSelected.clear();
		iAdapter.setCheckedItems(aSelected);
		iBookmarks = iDatabase.getBookmarks(iBookID,this,iTitle);
		iAdapter.setBookmarks(iBookmarks);
	}
	private void removeSelectedAnnotation(){
		if(iAnnotations == null){
			Log.e(Config.LOGTAG,"removeSelectedAnnotation iAnnotations = null");
			return;
		}
		SparseBooleanArray aSelected = iAdapter.getCheckedItems();
		for(int i=0;i<this.iAnnotations.size();i++){
			if(aSelected.get(i, false))
				iDatabase.removeAnnotation(iBookID, iAnnotations.get(i));
		}
		aSelected.clear();
		iAdapter.setCheckedItems(aSelected);
		iAnnotations = iDatabase.getAnnotations(iBookID);
		iAdapter.setAnnotations(iAnnotations);
	}
	private void removeSelectedMarker(){
		if(iMarkresults == null){
			Log.e(Config.LOGTAG,"removeSelectedMarker iMarkresults = null");
			return;
		}
		SparseBooleanArray aSelected = iAdapter.getCheckedItems();
		for(int i=0;i<this.iMarkresults.size();i++){
			if(aSelected.get(i, false))
				iDatabase.removeMarker(iBookID, iMarkresults.get(i));
		}
		aSelected.clear();
		iAdapter.setCheckedItems(aSelected);
		iMarkresults = iDatabase.getMarkers(iBookID, false);
		iAdapter.setMarkers(iMarkresults);
	}
}
