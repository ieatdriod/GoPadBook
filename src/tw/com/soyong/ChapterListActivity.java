package tw.com.soyong;

import java.util.ArrayList;

import tw.com.soyong.mebook.Mebook;
import tw.com.soyong.mebook.MebookData;
import tw.com.soyong.mebook.MebookException;
import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.mebook.MebookInfo;
import tw.com.soyong.mebook.MebookToken;
import tw.com.soyong.mebook.SyChapter;
import tw.com.soyong.mebook.SyContent;
import tw.com.soyong.mebook.SyInputStream;
import tw.com.soyong.mebook.SyItem;
import tw.com.soyong.mebook.SyParser;
import tw.com.soyong.mebook.SySentence;
import tw.com.soyong.mebook.TWMMetaData;
import tw.com.soyong.utility.ChapterInfo;
import tw.com.soyong.utility.ChpInfoAdapter;
import tw.com.soyong.utility.Native;
import tw.com.soyong.utility.SyBookmark;
import tw.com.soyong.utility.Util;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.taiwanmobile.myBook_PAD.R;

/**
 * 章節列表與書籤列表畫面切換
 * @author Victor
 *
 */
public class ChapterListActivity extends ListActivity{
	
	private static final String TAG = "ChapterListActivity";
	private boolean mIsChpMode = true ;
	private boolean mIsSingleMp3 = true ;
	int mSenIndex ;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapter_list);
        
        mIsChpMode = true ;
        
        loadChapterList();
        setListAdapter(new ChpInfoAdapter(this, mChapterInfos));
        
        final ListView listView =getListView(); 

        // Tell the list view which view to display when the list is empty
        listView.setEmptyView(findViewById(R.id.empty));
        
        final GradientDrawable lvSelector =new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(84, 145, 192), Color.rgb(2, 72, 131)});
        
    
        listView.setSelector(lvSelector);
        
        Bundle extras = getIntent().getExtras();
        listView.requestFocus();
        
        mSenIndex = extras.getInt(MeReaderActivity.CUR_INDEX);
        int chpIndex = extras.getInt(MeReaderActivity.CHP_INDEX);
        listView.setSelection(chpIndex);
        listView.setItemChecked(chpIndex, true);
        listView.invalidate();
        
		// Listen for button clicks 
        final ImageButton imgBtn_TopBG = (ImageButton) findViewById(R.id.ChapterList_ImageButton_TopBG);//
        final ImageButton imgBtn_Back = (ImageButton) findViewById(R.id.ChapterList_ImageButton_Back);
       
        final ImageView imgVbg_Catalog = (ImageView) findViewById(R.id.gsimedia_title_bg_Catalog);
        final ImageButton imgBtn_Catalog = (ImageButton) findViewById(R.id.ChapterList_ImageButton_Catalog);
       
        final ImageView imgVbg_Bookmark = (ImageView) findViewById(R.id.gsimedia_title_bg_Bookmark);
        final ImageButton imgBtn_Bookmark = (ImageButton) findViewById(R.id.ChapterList_ImageButton_Bookmark);
        
        final ImageButton imgBtn_Info = (ImageButton) findViewById(R.id.ChapterList_ImageButton_Info);
        final RelativeLayout RelativeLayout_Clean = (RelativeLayout) findViewById(R.id.ChapterList_RelativeLayout_Clean);
        
        RelativeLayout_Clean.setVisibility(View.GONE);
        
        imgBtn_Back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				exit();
			}
		});        
        
        imgBtn_Catalog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imgBtn_TopBG.setBackgroundResource(R.drawable.gsi_br06);
				
				imgVbg_Catalog.setBackgroundResource(R.drawable.gsi_title_bg01);
				imgBtn_Catalog.setBackgroundResource(R.drawable.gsi_title07);
				
				imgVbg_Bookmark.setBackgroundResource(R.drawable.gsi_title_bg02_1);
				imgBtn_Bookmark.setBackgroundResource(R.drawable.gsi_title08_1);
				
				RelativeLayout_Clean.setVisibility(View.GONE);
				
				mIsChpMode = true ;
				
		        loadChapterList();
		        setListAdapter(new ChpInfoAdapter(ChapterListActivity.this, mChapterInfos));				
			}
		});
        
        imgBtn_Bookmark.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				imgBtn_TopBG.setBackgroundResource(R.drawable.gsi_br07);//ian_bar04b
				
				imgVbg_Catalog.setBackgroundResource(R.drawable.gsi_title_bg01_1);
				imgBtn_Catalog.setBackgroundResource(R.drawable.gsi_title07_1);
				
				imgVbg_Bookmark.setBackgroundResource(R.drawable.gsi_title_bg02);
				imgBtn_Bookmark.setBackgroundResource(R.drawable.gsi_title08);
				
				RelativeLayout_Clean.setVisibility(View.VISIBLE);
				
				mIsChpMode = false ;
				
				// load bookmark
		        loadBookmark();
		        setListAdapter(new ChpInfoAdapter(ChapterListActivity.this, mChapterInfos));				
		        getListView().invalidate();

			}
		});
        
        imgBtn_Info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ChapterListActivity.this, MebookInfoActivity.class);
				startActivity(intent);
			}
		}); 
        
        
        final ImageButton cleanAllBookmark = (ImageButton)this.findViewById(R.id.ChapterList_ImageButton_Clean);
        cleanAllBookmark.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				final SyBookmark bookmark = MeReaderActivity.mBookmark;
				if ( -1 == bookmark.getFirst() ){
					return ;
				}
				
	             new AlertDialog.Builder(ChapterListActivity.this)
	             .setTitle(R.string.delete_book_title)
	             .setMessage(R.string.delete_bookmark_prompt)
	             .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
        				bookmark.clear();
        		        loadBookmark();
        		        setListAdapter(new ChpInfoAdapter(ChapterListActivity.this, mChapterInfos));        				
                    }
                })
                .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();				
				
			}
        });
        
        
    }
    
    ArrayList<Integer> mBmIndex2List = new ArrayList<Integer>();

	private void loadBookmark() {
		
		mChapterInfos.clear();
		final SyBookmark bookmark = MeReaderActivity.mBookmark;
		final SySentence[] sentArr = MebookHelper.mSentenceArr;
		
		ArrayList<String> bookmarkNemArr = new ArrayList<String>();
		
		ArrayList<Integer> bmIndex2List = mBmIndex2List;
		bmIndex2List.clear();
		
		String[] nameArr;
		if ( bookmark.getLength() > 0){
		int index = bookmark.getFirst();
			String name;

			if (index >= 0) {
				bmIndex2List.add(index);
				name = getTitle(sentArr, index);				
				addOrgData(bookmarkNemArr, name);
				
				
				StringBuilder sb = new StringBuilder(name);
				Util.removeFontTag(name , sb);
				Util.removePhonetic(sb);
				name = sb.toString();				
				mChapterInfos.add(new ChapterInfo(name,false) );
				
				int last = bookmark.getLast();
				if (last > index) {
					while (index != last) {
						index = bookmark.getNext(index);

						bmIndex2List.add(index);
						name = getTitle(sentArr, index);
						addOrgData(bookmarkNemArr, name);
						
						mChapterInfos.add(new ChapterInfo(name,false) );
					}
				}
			}

			nameArr = new String[bookmarkNemArr.size()];
			bookmarkNemArr.toArray(nameArr);
		}else{
			nameArr = new String[0];
		}

	}      
    
	
	private String getTitle(final SySentence[] sentArr, int index) {
		String name;
		String trl;
		
		name = sentArr[index].mData.get(MebookToken.TOK_ORG);
		if ( null == name || name.length() <= 0){
			trl = sentArr[index].mData.get(MebookToken.TOK_TRL);
			if ( null == trl || trl.length() <= 0){
				index++;
				String format = getResources().getString(R.string.bookmark_format);
				name = String.format(format, index);
			}else{
				name = trl ;
			}
		}
		return name;
	}	

	private void addOrgData(ArrayList<String> bookmarkNemArr, String name) {
		StringBuilder sb = new StringBuilder(name);
		Util.removeFontTag(name , sb);
		Util.removePhonetic(sb);
		name = sb.toString();
		bookmarkNemArr.add(name);
	}	

	private ArrayList<ChapterInfo> mChapterInfos = new ArrayList<ChapterInfo>();
	private void loadChapterList() {
		
		mChapterInfos.clear();
		
		final TWMMetaData xml = MebookHelper.mMeta;
		
		final int trackCount = xml.getTrackCount();
		if ( trackCount > 1 ){
			mIsSingleMp3 = false ;
			String name ;
			for (int i = 1 ; i < trackCount+1 ; ++i){
				name = xml.getMP3Title(i);
				mChapterInfos.add(new ChapterInfo(name,false) );
			}
		}else {
			mIsSingleMp3 = true ;
			final ArrayList<SyChapter> chps = MebookHelper.mContent.mChapter ;
			
			ArrayList<SyChapter> chapterArr;
	
			if (null != chps && chps.size() > 0) {
				chapterArr = chps;

				boolean isValidPs = false;
				for ( SyChapter chapter : chapterArr){
					
					isValidPs = false;
					String strPs = MebookHelper.mContent.mPostscript.getData(chapter.mNo);
					
					if (null != strPs && strPs.length() > 0) {
						isValidPs = true;
					}
			
					mChapterInfos.add(new ChapterInfo(chapter.mName,isValidPs) );
				}
			}
		}
	}	
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if ( 0 == v.getClass().getName().compareTo("android.widget.ImageButton") ){
			gotoPs(position);
		} else {
			itemClick(position);
		}
	}
    
    static final String BK_ID = "bk_id";
    static final String PS_DATA = "ps_index";
    private void gotoPs(int position){
    	
    	final ArrayList<SyChapter> chps = MebookHelper.mContent.mChapter ;
    	SyChapter chapter = chps.get(position);
    	String strPs = MebookHelper.mContent.mPostscript.getData(chapter.mNo);
    	
    	Intent it = new Intent(this, PostscriptActivity.class);
		it.putExtra(BK_ID, MebookHelper.mHeaderInfo.mBookID);
		it.putExtra(PS_DATA, strPs);
		startActivity(it);
    }
    

    private ProgressDialog mProgDlg;
	private void itemClick(int position) {
		Bundle bundle = new Bundle();
		Intent intent = new Intent();

		final boolean isChpMode = mIsChpMode;
		bundle.putBoolean(MeReaderActivity.CHP_MODE, isChpMode);
		
		if (true == isChpMode) {
			
			if ( mIsSingleMp3 ) {
				ArrayList<SyChapter> chapterArr = MebookHelper.mContent.mChapter;
				SyChapter chapter = chapterArr.get(position);
				bundle.putInt(MeReaderActivity.CHP_INDEX, chapter.mSentenceIndex);
			} else {
				
				if ( MebookHelper.mTrackIndex != position+1 ){
					// load new content
					MebookHelper.closeIS();
					
					MebookHelper.mTrackIndex = position+1;
					  
					final ProgressDialog progDlg = ProgressDialog.show(this,
							getText(R.string.progress_title),
							getText(R.string.progress_message), true);
					 
					mProgDlg = progDlg;
					getBookContent(progDlg);
					return ;
					
					
				} else {
					bundle.putInt(MeReaderActivity.CHP_INDEX, mSenIndex);
				}
			}
			
		} else {
			bundle.putInt(MeReaderActivity.CUR_INDEX, mBmIndex2List.get(position));
		}
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private final void getBookContent(final ProgressDialog progDlg  ) {
		
		new Thread() {
			public void run() {
				try {
			        // get input stream by mp3 title
			        String title = MebookHelper.mMeta.getMP3Title(MebookHelper.mTrackIndex);			// begin from 1
			        SyInputStream is = new SyInputStream(MebookHelper.mSC , title);		// "The Wind and The Sun"
			        
			        // load mebook
					MebookInfo bookInfo = Mebook.isMebook(is);
			        
					Mebook book = new Mebook();
					book.load(bookInfo , is);

					MebookData bookData = book.getData();
					SyItem item;
					String str = null;
					
					if (null != bookData) {
						try {
							// get mebook content
							item = bookData.getData(MebookData.ARTICLE,
									MebookData.DATA_TXT, is);
							str = item.toString();

							// get frame table
							item = bookData.getData(MebookData.FRAME_TABLE, 0, is);	
							byte [] data = item.mLeafData;
							
							final int count = item.mItem << 2 ;
							int [] frameTable = new int[item.mItem];
							
							int j = 0 ;
							int b0 , b1 , b2, b3 ;
							for ( int i = 0 ; i < count ; i+=4){
								b3 = (int)data[i+3] & 0xff;
								b2 = (int)data[i+2] & 0xff;
								b1 = (int)data[i+1] & 0xff;
								b0 = (int)data[i] & 0xff;
								
								frameTable[j] = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0; 
								j++;
							}
							MebookHelper.mFrameTable = frameTable;
							MebookHelper.mISSyd = is ;

						} catch (MebookException e) {
							e.printStackTrace();
						}
						MebookHelper.mBookData = bookData;
						MebookHelper.mHeaderInfo = bookInfo;
						item = null;
					}
					book = null;
					
					SyParser parser = new SyParser();
					SyContent content = parser.getContent(str);
					parser = null;

					// prepare sentence array
					final int sentCount = content.getTotalSentence();
					if (0 == sentCount) {
						mHandler.sendEmptyMessage(GET_CONTENT_FAIL);
						return;
					}
					MebookHelper.mContent = content;

					SySentence [] sentenceArr = new SySentence[sentCount];
					// **Note** after call getSentenceArr  we must have keep mSentenceArr
					content.getSentenceArr(sentenceArr); 
					MebookHelper.mSentenceArr = sentenceArr;
					
					//
					// initial DRM Mp3 content
					//
					Native.InitDRMMp3(MebookHelper.mSC , title);
					Native.open("now unused");
						
					System.gc();
					mHandler.sendEmptyMessage(GET_CONTENT_DONE);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG ,"Exception !!!!");
				} finally {
					// Dismiss the Dialog
					if ( null != progDlg){
						progDlg.dismiss();
					}
				}
			}
		}.start();
	}  
	
	private static final int GET_CONTENT_DONE = 200;
	private static final int GET_CONTENT_FAIL = 201;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case GET_CONTENT_DONE:
			{
				mProgDlg.dismiss();
				mProgDlg = null;
				
				Editor editPref = getSharedPreferences(MebookHelper.mDeliverID, Context.MODE_PRIVATE).edit();
		        // last track index	
				editPref.putInt(AnReader.TRACK_INDEX, MebookHelper.mTrackIndex).commit();			
				
				Bundle bundle = new Bundle();
				Intent intent = new Intent();

				final boolean isChpMode = mIsChpMode;
				bundle.putBoolean(MeReaderActivity.CHP_MODE, isChpMode);
				bundle.putInt(MeReaderActivity.CHP_INDEX, -1);	
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
				break;
			
			case GET_CONTENT_FAIL:
			{
				// Oops !!!! big trouble when receive this message!!!
				mProgDlg.dismiss();
				mProgDlg = null;
			}
				break;
				
			default:
				super.handleMessage(msg);
			}
		}
	};		

	/**
	 *  Handle back even from UI or hot key
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			exit();
		}
		return super.onKeyDown(keyCode, event);
	}


	private void exit() {
		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		
		final boolean isChpMode = mIsChpMode;
		bundle.putBoolean(MeReaderActivity.CHP_MODE, isChpMode);
		
		if ( isChpMode ){
			bundle.putInt(MeReaderActivity.CHP_INDEX, mSenIndex);
			
		}else {
			bundle.putInt(MeReaderActivity.CUR_INDEX, mSenIndex);
		}
		
		
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}	

}
