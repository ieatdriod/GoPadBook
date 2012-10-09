
package com.taiwanmobile.myBook_PAD;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

import org.iii.ideas.android.general.AndroidLibrary;
import org.iii.ideas.reader.LargeImageView;
import org.iii.ideas.reader.PartialUnzipper;
import org.iii.ideas.reader.ReaderRetainObject;
import org.iii.ideas.reader.drm.NewDecipher;
import org.iii.ideas.reader.last_page.LastPageHelper;
import org.iii.ideas.reader.ncx.NCXReader;
import org.iii.ideas.reader.opf.OPFReader;
import org.iii.ideas.reader.parser.HtmlReceiver;
import org.iii.ideas.reader.parser.ParseErrorHandler;
import org.iii.ideas.reader.renderer.EpubView;
import org.iii.ideas.reader.renderer.EpubViewCallback;
import org.iii.ideas.reader.renderer.LinedContent;
import org.iii.ideas.reader.renderer.MacroRenderer;
import org.iii.ideas.reader.renderer.RendererCallback;
import org.iii.ideas.reader.renderer.RendererConfig;
import org.iii.ideas.reader.renderer.VerticalLinedContent;
import org.iii.ideas.reader.renderer.VerticalRenderer2;
import org.iii.ideas.reader.search.KeywordSearcher;
import org.iii.ideas.reader.underline.Underline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gsimedia.animation.BaseAnimation;
import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.sa.DeviceIDException;
import com.gsimedia.sa.IllegalP12FileException;
import com.gsimedia.sa.IllegalRightObjectException;
import com.gsimedia.sa.NOPermissionException;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.taiwanmobile.common.ActionItem;
import com.taiwanmobile.common.QuickAction;
import com.taiwanmobile.myBook_PAD.SimpleGestureFilter.SimpleGestureListener;

/**
 * epub reader主程式，負責UI和閱讀流程控制整合。 
 * @author III
 * 
 */
@SuppressWarnings("deprecation")  
public class Reader extends Activity implements RendererCallback, EpubViewCallback, ParseErrorHandler,SimpleGestureListener{
    /** Called when the activity is first created. */
	private RelativeLayout totalLayout;
//	private RelativeLayout rl,totalLayout;
//	private AbsoluteLayout al,al_2;
//	private LinearLayout ll;
	private TextView tv_book_title,tv_reader_book_page_info_1,tv_reader_book_page_info_2;
//	private ImageButton ib_reader_previous_page,ib_reader_next_page,
//				ib_reader_setting_bookmark,ib_reader_setting_highlight,ib_reader_setting_notes,ib_reader_search,ib_reader_setting,
//				ib_reader_setting_bookmark_del,ib_reader_setting_notes_del;
	
	private Button ib_reader_back = null;
	private Button ib_reader_catalog_jump = null;
	private ImageView ib_reader_is_note = null;
	
	private ImageView ib_reader_is_bookmark_right = null;
	private ImageView ib_reader_is_bookmark_left = null;
	
	private RelativeLayout RelativeLayout_Title = null;
	private RelativeLayout RelativeLayout_Ctls = null;
	private RelativeLayout RelativeLayout_Ctls_Colors = null;
	private RelativeLayout RelativeLayout_Ctls_Fonsize = null;
	
	RelativeLayout iFirstPageView = null;
	
	private Button helpButton = null;
	private Button rotateButton = null;
	private Button bgColorButton = null;
	private Button aColorButton = null;
	private Button aSizeButton = null;
	private Button lineColorButton = null;
	private Button nightModeButton = null;
	private Button aChineseTypeButton = null;
	private Button aFlipButton = null;
	private Button otherButton = null;
	
	private boolean bBGEnabled = false;
	private boolean bAColorEnabled = false;
	private boolean bLingColorEnabled = false;
	private boolean bASizeEnabled = false;
	
	private SeekBar sb_reader;
	private EpubView ev;
	private ProgressDialog progress;
	private AlertDialog globalAd;
	//control parameters
	private int threadIdx=0; 
	private boolean isVertical=false;
	//private boolean isFirstRender=true;
	private boolean isUnderlineOpen=false;
	private boolean isUnderlineRemovalOpen=false;
	private boolean isTouch = false;
	private boolean isTurning=true;
	private boolean isCallSetting=false;
	private boolean isStatusAlwaysOn=false;
	private boolean shouldChangeOrientation=true;
	private boolean shouldSyncToServer=false;
	private boolean isTrial=false;
	private boolean isConfigurationChanged;
	private boolean showLastPageSyncError=false;
	//private boolean hasLoadChapterCalled;
	//tool
	private ReaderRetainObject retain;
	private LastPageHelper lastHelper;
	private ThreadHandler thandler;
	private SharedPreferences settings; 
	private SharedPreferences lastPages; 
	private static MacroRenderer renderer;
	//private PartialUnzipper unzipper;
	private ReaderSettingGetter getter;
	//data
	private int screenWidth;
	private int screenHeight;
	//private MacroRenderer renderer;
	private String bookAuthors="";
	private String bookPublisher="";
	private String epubPath;
	private String contentId="";
	private String dcfPath;
	//private String opfPath;
	//private String bookTitle;
	private String titleFromProfile="";
	//private String ncxPath;
	//private String targetDir;
	//private String curSecFilename="";
	//private int curSecNo;
	//private int curPageNo=0;
	//private OPFDataSet dataset;
	//private ArrayList<String> spineList;
	//private ArrayList<Map<String,String>> tocList=null;
	private boolean isReaderClosed;
	private int fontSizeIdx;
	//private HashMap<String,String> tocMap;
	//constant
	private static final int ACTION_SHOW_READER = 1;
	//private final int ACTION_INITIALIZE_PAGE=2;
	//private final int ACTION_LOAD_CHAPTER=3;
	/*private final int ACTION_COMPUTE_CHAPTER=4;  
	private final int ACTION_INFLATE_ANN_INPUT=5;
	private final int ACTION_SHOW_ANN=6;*/
	private static final int ACTION_LOAD_CHAPTER_BY_SPAN=7;
	private static final int ACTION_GET_METADATA=8;
	private static final int ACTION_LOAD_CHAPTER_BY_PERCENTAGE=9;
	private static final int ACTION_SHOW_PROGRESS=10;
	private static final int ACTION_DISMISS_PROGRESS=11;
	private static final int ACTION_SHOW_ERROR=12;
	private static final int ACTION_SHOW_TOAST=13;
	private static final int ACTION_SHOW_STATUS=14;
	private static final int CALL_CATALOG=0;
	private static final int CALL_SETTING=1;
	private static final int CALL_ANN=2;
	private static final int CALL_SEARCH=3;
	
	public static final int DEFAULT_FONTSIZE_IDX = 2;
	
	private SimpleGestureFilter detector; 
//	private ScaleGestureDetector mScaleDetector;
//	private float mScaleFactor = 1.f;
	
	@Override
	public void onSwipe(int direction) {
	  String str = "";
	  
	  switch (direction) {
	  
	  case SimpleGestureFilter.SWIPE_RIGHT : 
		  str = "Swipe Right";
	  	  pageUp();
	      break;
	  case SimpleGestureFilter.SWIPE_LEFT :
		  str = "Swipe Left";
		  pageDown();
	      break;
	  case SimpleGestureFilter.SWIPE_DOWN :  str = "Swipe Down";
	                                                 break;
	  case SimpleGestureFilter.SWIPE_UP :    str = "Swipe Up";
	                                                 break;
	                                           
	  } 
	   Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	 }

	 @Override
	 public void onDoubleTap() {
	    Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show(); 
	 }
//	 class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//		    @Override
//		    public boolean onScale(ScaleGestureDetector detector) {
//		        mScaleFactor *= detector.getScaleFactor();
//
//		        // Don't let the object get too small or too large.
//		        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
//		        showScaleFactor();
//		        
//		        return true;
//		    }
//		}
//	 public void showScaleFactor(){
//		 Toast.makeText(this, "Scale:"+mScaleFactor, Toast.LENGTH_LONG).show();
//	 }
    @Override 
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("In","Reader");
    	 
        super.onCreate(savedInstanceState);
        detector = new SimpleGestureFilter(this,this);
//        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
    	//hide system status bar
    	final Window win = getWindow();
    	win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	//hide window title
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
        RendererConfig.dpi = AndroidLibrary.getDpi(this.getBaseContext());
        progress = new ProgressDialog(this);
        isReaderClosed=false;
        isConfigurationChanged=false;
        loadRetainedObject();
        if(!isConfigurationChanged)
        	AndroidLibrary.deleteDir(getDir("temp", Context.MODE_PRIVATE));
        initializeVariables(getIntent());
        setScreenInfo();
//        setContentView(R.layout.iii_reader);
        setContentView(R.layout.iii_gsi_viewer);
        setViewComponent();
        setListener();
        showMenu(false);
        initializeRenderer();
        startLoading();
    }
    
    int iOrientation = Configuration.ORIENTATION_PORTRAIT;
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(iOrientation != newConfig.orientation){
			iOrientation = newConfig.orientation;
//			setScreenInfo();
//			if(renderer!=null)
//				renderer.resetScreenSize(screenHeight, screenWidth);
		}
	}

    
    /**
     * 若螢幕方向轉換，直接讀取保留資料以避免重複加解密/parsing。可參照RetainObject class
     */
    private void loadRetainedObject(){
    	retain = (ReaderRetainObject) getLastNonConfigurationInstance();
    	if(retain!=null){
    		isConfigurationChanged=true;
    		Log.d("retain","isParsingF:"+retain.receiver.isParsingFinished);
    		showProgress(getResources().getString(R.string.iii_unzip_progress));
    		//renderer = retain.renderer;
    		//unzipper = retain.uz;
    	}else{
    		retain = new ReaderRetainObject();
    		retain.receiver = new HtmlReceiver(this);
    	}
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	Log.d("retain","obj");
    	retain.renderStartSpan=renderer.getRenderStartSpan();
    	retain.renderStartIdx=renderer.getRenderStartIdx();
    	isConfigurationChanged=true;
        return retain;
    }
    
    /**
     * 初始化renderer(根據直書與否初始化不同renderer)和取得最後閱讀頁
     */
    private void initializeRenderer(){
    	if(isVertical()){
        	isVertical=true;
        	renderer = new VerticalRenderer2(ev,screenHeight,screenWidth,this, getApplicationContext(),getDeliverId(),fontSizeIdx);
        	renderer.setTurningMethod(getter.getTurningMethod(getDeliverId()));
        }else{
        	isVertical=false;
        	renderer = new MacroRenderer(ev,screenHeight,screenWidth,this, getApplicationContext(),getDeliverId(),fontSizeIdx);
        	renderer.setTurningMethod(getter.getTurningMethod(getDeliverId()));
        }
    	Log.d("retain.isCallRenderOnRetain",":"+retain.isCallRenderOnRetain);
    	if(isConfigurationChanged){
    		if(retain.spineList!=null && retain.curSecNo==retain.spineList.size()-1){
    			ev.setIsLastPage(true);
        		//thandler.sendMessage(thandler.obtainMessage(ACTION_LOAD_CHAPTER_BY_SPAN,0,0));
    		}
    		if(retain.isCallRenderOnRetain){
    			String tempName = lastPages.getString(getDeliverId()+"_chap_name", null);
    			if(retain.spineList==null || tempName==null || retain.curSecFilename=="null" ||retain.spineList.indexOf(retain.curSecFilename)<0 || retain.spineList.indexOf(retain.curSecFilename)>=retain.spineList.size()){
    				renderer.loadChapterBySpanAndIdx(retain.receiver, retain.renderStartSpan, retain.renderStartIdx, retain.uz, retain.curSecFilename);
	            }else{
	            	Log.d("getLastPage","get");
	            	if(tempName.equals(retain.curSecFilename)){
	            		//retain.curSecNo=retain.spineList.indexOf(tempName);
	                	int spanNo=lastPages.getInt(getDeliverId()+"_span", 0);
	                	int idxInSpan=lastPages.getInt(getDeliverId()+"_idx", 0);
	                	//retain.curSecFilename=retain.spineList.get(retain.curSecNo);
	                	renderer.loadChapterBySpanAndIdx(retain.receiver, spanNo, idxInSpan, retain.uz, retain.curSecFilename);
	            	}else{
	            		retain.curSecNo=retain.spineList.indexOf(tempName);
	                	int spanNo=lastPages.getInt(getDeliverId()+"_span", 0);
	                	int idxInSpan=lastPages.getInt(getDeliverId()+"_idx", 0);
	                	retain.curSecFilename=retain.spineList.get(retain.curSecNo);
	                	unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,spanNo,idxInSpan,threadIdx);
	            	}
	            	
                	/*if(retain.curSecNo>0 || spanNo>0 || idxInSpan>0)
                		ev.setIsStart(false);
                	else
                		ev.setIsStart(true);
                		*/
	            }
    			//renderer.reloadChapter(retain.receiver, retain.uz, retain.curSecFilename);
    			//retain.isCallRenderOnReain=false;
    		}
    	}
    }
    
    /**
     * 開始處理書籍，如果同步開啟則先從server取得該書最後閱讀頁，否則直接進入解密階段
     */
    private void startLoading(){
    	if(isConfigurationChanged ){
    		setTitleBar();
    	}else if(shouldSyncToServer /*&& AndroidLibrary.is3gConnected(this)*/){
        	//Log.d("!!!!syncToServer","sync");
            thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_PROGRESS));
        	syncLastPage(threadIdx);
        }else{
        	//Log.d("!!!!syncToServer","NotSync:"+shouldSyncToServer);
            thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_PROGRESS));
        	decrypt(threadIdx);
        }
    }
    
    /**
     * 取得螢幕寬高
     */
    private void setScreenInfo(){
    	Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth=display.getWidth();
        screenHeight=display.getHeight();
    }
    
    /**
     * 初始化程式變數
     * @param it intent
     */
    private void initializeVariables(final Intent it){
    	String isTrialStr =it.getStringExtra("isSample");
    	if(isTrialStr!=null && isTrialStr.equals("1"))
    		isTrial=true;
    	else
    		isTrial=false;
        dcfPath = it.getData().toString().substring(7);
        contentId = it.getStringExtra("content_id");
        bookAuthors = it.getStringExtra("book_authors");
        bookPublisher = it.getStringExtra("book_publisher");
        shouldSyncToServer = it.getBooleanExtra("syncLastPage",false);
        titleFromProfile = it.getStringExtra("book_title");
        epubPath = dcfPath.substring(0, dcfPath.lastIndexOf("."))+".epub";
        getter = new ReaderSettingGetter(getApplicationContext());
        lastHelper = new LastPageHelper(getApplicationContext()); 
        lastPages = getSharedPreferences(getResources().getString(R.string.iii_last_page_name),0);  
        settings = getSharedPreferences("reader_Preference", 0);
        boolean intentVertical = it.getBooleanExtra("book_vertical", false);
        setDefaultPreferences(intentVertical);
        if(settings.getBoolean("reader_setting_screen_rotation_value",false)){
        	shouldChangeOrientation=true;
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }else{
        	shouldChangeOrientation=false;
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
       
        fontSizeIdx=settings.getInt(getDeliverId()+"reader_setting_font_size_value", DEFAULT_FONTSIZE_IDX);
        isStatusAlwaysOn = !settings.getBoolean("reader_setting_hidden_value", true);
        thandler = new ThreadHandler(this);
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	retain.isCallRenderOnRetain=true;
    	Log.d("on","result");
        try{
        	switch(requestCode){
        	case CALL_CATALOG:
        		Bundle result = data.getBundleExtra("result");
        		if(result.getBoolean("shouldJump",false) && result.getString("chap")!=null){
        			lastPages.edit().putString(getDeliverId()+"_chap_name", result.getString("chap")).commit();
        			lastPages.edit().putInt(getDeliverId()+"_time", (int) System.currentTimeMillis()).commit();
        			if(result.getBoolean("isToc",false)){
        				retain.curSecFilename=result.getString("chap");
        				retain.curSecNo=retain.spineList.indexOf(retain.curSecFilename);
            			lastPages.edit().putInt(getDeliverId()+"_span", 0).commit();
                    	lastPages.edit().putInt(getDeliverId()+"_idx", 0).commit();
        				unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,0,0,threadIdx);
        			}else{
        				retain.curSecFilename=result.getString("chap");
        				retain.curSecNo=retain.spineList.indexOf(retain.curSecFilename);
        				int span = result.getInt("span",0);
        				int idx = result.getInt("idx",0);
            			lastPages.edit().putInt(getDeliverId()+"_span", result.getInt("span",0)).commit();
                    	lastPages.edit().putInt(getDeliverId()+"_idx", result.getInt("idx",0)).commit();
        				unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,span,idx,threadIdx);
        			}
        		}else{
        			if(isConfigurationChanged){
        				renderer.loadChapterBySpanAndIdx(retain.receiver, retain.renderStartSpan, retain.renderStartIdx, retain.uz, retain.curSecFilename);
        			}else
        				renderer.reload();
        		}
        		break;
        	case CALL_SETTING:
        		//尚未改成用startActivityForResult，目前由onResume判斷是否reload
        		break;
        	case CALL_ANN:
        		//改由直接從SettingNotes.java insert
        		//Bundle bdl = data.getBundleExtra("result");
        		//if(bdl.getBoolean("shouldInsert",false)){
        		//	renderer.insertAnn(bookTitle, curSecFilename, getDeliverId(), bdl.getString("content"), renderer.getCurPageStartSpan(), renderer.getCurPageStartIdxInSpan());
        		//原本:setBookmarkAndNoteFlag();
        		//}
        		retain.isCallRenderOnRetain=true;
    			if(isConfigurationChanged){
    				renderer.loadChapterBySpanAndIdx(retain.receiver, retain.renderStartSpan, retain.renderStartIdx, retain.uz, retain.curSecFilename);
    			}else
    				renderer.reload();
        		break;
        	case CALL_SEARCH:
        		Bundle resultsS = data.getBundleExtra("result");
        		if(resultsS.getBoolean("shouldJump",false) && resultsS.getString("chap")!=null){
        			lastPages.edit().putString(getDeliverId()+"_chap_name", resultsS.getString("chap")).commit();
        			lastPages.edit().putInt(getDeliverId()+"_span", resultsS.getInt("span",0)).commit();
                	lastPages.edit().putInt(getDeliverId()+"_idx", resultsS.getInt("idx",0)).commit();
                	lastPages.edit().putInt(getDeliverId()+"_time", (int) System.currentTimeMillis()).commit();
                	retain.curSecFilename=resultsS.getString("chap");
                	retain.curSecNo=retain.spineList.indexOf(retain.curSecFilename);
        			int span = resultsS .getInt("span",0);
        			int idx = resultsS .getInt("idx",0);
        			unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,span,idx,threadIdx);
        		}else{
        			if(isConfigurationChanged){
        				renderer.loadChapterBySpanAndIdx(retain.receiver, retain.renderStartSpan, retain.renderStartIdx, retain.uz, retain.curSecFilename);
        			}else
        				renderer.reload();
        		}
        		break;
        	}
        }catch(Exception e){
        	Log.d("onAcivityResult","orientation change");
        	try {
				Bundle result = data.getBundleExtra("result");
				if(result.getBoolean("shouldJump",false) && result.getString("chap")!=null){
					lastPages.edit().putString(getDeliverId()+"_chap_name", result.getString("chap")).commit();
					if(result.getBoolean("isToc",false)){
						lastPages.edit().putInt(getDeliverId()+"_span", 0).commit();
				    	lastPages.edit().putInt(getDeliverId()+"_idx", 0).commit();
					}else{
						lastPages.edit().putInt(getDeliverId()+"_span", result.getInt("span",0)).commit();
				    	lastPages.edit().putInt(getDeliverId()+"_idx", result.getInt("idx",0)).commit();
					}
					lastPages.edit().putInt(getDeliverId()+"_time", (int) System.currentTimeMillis()).commit();
				}
				retain.isCallRenderOnRetain=true;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		
    		//Log.d("onAcivity","result jump:"+result.getBoolean("shouldJump",false));
            e.printStackTrace();
        }
              
    }
    
    private boolean isUploadLastPageCall=false;
    
    /**
     * 離開閱讀介面時呼叫的method。取得最後閱讀頁資訊後即finish
     */
    private void leaveReading(){
    	Log.d("finish","reading");
    	//renderer.closeDB();
    	isReaderClosed=true;
    	if(!isTurning && !isUploadLastPageCall){
	    	isUploadLastPageCall=true;
	    	setLastPageIntent();
		}
    	finish();
    }
    
    public void onDestroy(){
		try {
			/*if(!isTurning){
					//lastPages.edit().putInt(contentId+"_chap_no", curSecNo).commit();
					lastPages.edit().putString(getDeliverId()+"_chap_name", curSecFilename).commit();
			    	lastPages.edit().putInt(getDeliverId()+"_span", renderer.getCurPageStartSpan()).commit();
			    	lastPages.edit().putInt(getDeliverId()+"_idx", renderer.getCurPageStartIdxInSpan()).commit();
			    	lastPages.edit().putInt(getDeliverId()+"_time", (int) System.currentTimeMillis()).commit();
			    	uploadLastPage();
				}*/
			isReaderClosed = true;
			if (!isConfigurationChanged) {
				AndroidLibrary.deleteDir(getDir("temp", Context.MODE_PRIVATE));
				if(renderer!=null)
					renderer.leaveReading();
				KeywordSearcher.kw = null;
				NewDecipher.sc=null;
				threadIdx++;
			} else {
				if(renderer!=null)
					renderer.leaveReading();
			}
			renderer = null;
			thandler.sendMessage(thandler
					.obtainMessage(ACTION_DISMISS_PROGRESS));
			
			if(shouldSyncToServer && !isConfigurationChanged && lastpageIntent!=null)
				startActivity(lastpageIntent);
			Log.d("on", "destroy");
			super.onDestroy();
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
    
    /**
     * 取得是否啟動註記/畫線/書籤等功能
     * @return 是否啟動註記/畫線/書籤等功能
     */
    private boolean isFunctionEnabled(){
    	try {
			if(retain.curSecNo!=retain.spineList.size()-1)
				return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return false;
    }
    
    public void onStop(){
    	Log.d("on","stop");
    	try {
			if(progress!=null)
				progress.dismiss();
			//thandler.sendMessage(thandler.obtainMessage(ACTION_DISMISS_PROGRESS));
	    	if(globalAd !=null && globalAd.isShowing()){
				globalAd.dismiss();
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	//if(progress !=null && progress.isShowing()){
        //	thandler.sendMessage(thandler.obtainMessage(ACTION_DISMISS_PROGRESS));
    	//	Log.d("progress","dismiss:onStop");
    	//}

    	//thandler.sendMessage(thandler.obtainMessage(ACTION_DISMISS_PROGRESS));
    	super.onStop();
    }
    
    public boolean dispatchTouchEvent (MotionEvent ev){
    	this.detector.onTouchEvent(ev);
//    	mScaleDetector.onTouchEvent(ev);
    	if(!isReaderClosed){
    		return super.dispatchTouchEvent(ev);
    	}else{
    		return true;
    	}
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(!isReaderClosed){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
    			leaveReading();
    			
    			return true;
    		}
    		return super.onKeyDown(keyCode,event);
    	}else{
    		
    		return true;
    	}
    }

	public void onResume(){
		//Log.d("on","resume");
		//Log.d("aa","a");
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	super.onResume();
    	//Log.d("a","1.5");
    	if(isCallSetting && !isTurning){
    		//Log.d("b","b");
            if(settings.getBoolean("reader_setting_screen_rotation_value",false)!=shouldChangeOrientation){
            	shouldChangeOrientation=settings.getBoolean("reader_setting_screen_rotation_value",false);
            	if(shouldChangeOrientation){
            		//Log.d("b","b1");
            		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            		//isFirstRender=true;
            	}else{
            		//Log.d("b","b2");
            		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            	}
            	displayRotate();
            }
           // Log.d("c","c");
            boolean nightMode = settings.getBoolean("reader_setting_night_mode_value", false);
            ev.setBackGroundIndex(nightMode);
            RendererConfig.isNightMode=nightMode;
//            showManual();
            //ev.setReaderBackGround(settings.getBoolean("reader_setting_night_mode_value", false));
            /*
            if(settings.getBoolean("reader_setting_night_mode_value", false)){
            	ev.setBackgroundDrawable(null);
            	ev.setBackgroundColor(Color.BLACK);
            }else{
            	ev.setBackgroundDrawable(RendererConfig.getBackground(this));
            }*/
            //Log.d("d","d");
    		if(isVertical()!=isVertical){
    			isVertical=isVertical();
    			fontSizeIdx=settings.getInt(getDeliverId()+"reader_setting_font_size_value", DEFAULT_FONTSIZE_IDX);
    			int span=renderer.getCurPageStartSpan();
    			int idx=renderer.getCurPageStartIdxInSpan();
    	        if(isVertical()){
    	        	renderer = new VerticalRenderer2(ev,screenHeight,screenWidth,this,getApplicationContext(),getDeliverId(),fontSizeIdx);
    	        }else{
    	        	renderer = new MacroRenderer(ev,screenHeight,screenWidth,this,getApplicationContext(),getDeliverId(),fontSizeIdx);
    	        }
    	        renderer.setTurningMethod(getter.getTurningMethod(getDeliverId()));
    	        unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,span,idx,threadIdx);
    		}else{
    			fontSizeIdx=settings.getInt(getDeliverId()+"reader_setting_font_size_value", DEFAULT_FONTSIZE_IDX);
        		//Log.d("fontSizeIdx","resume is:"+fontSizeIdx);
    			renderer.setTurningMethod(getter.getTurningMethod(getDeliverId()));
        		boolean result = renderer.changeFontSize(fontSizeIdx);
        		isStatusAlwaysOn = !settings.getBoolean("reader_setting_hidden_value", false);
        		if(result){
        			thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_PROGRESS,getResources().getString(R.string.iii_unzip_progress)));
        			renderer.reload();
        		}else{
        			renderer.reload();
        		}
    		}
            //Log.d("e","e");
    		isCallSetting=false;
    	}
    }
    
	
	private Intent lastpageIntent;
	/**
	 * 取得最後閱讀頁資訊(用intent包裝，傳給LastPageActivity處理)
	 */
	private void setLastPageIntent(){
		final boolean sync = shouldSyncToServer;
		if(sync){
			final int span = renderer.getCurPageStartSpan();
			final int idx = renderer.getCurPageStartIdxInSpan();
			final String deliverId = getDeliverId();
			final String title = titleFromProfile;
			final String secName = retain.curSecFilename;
			lastpageIntent = new Intent(this,LastPageActivity.class);
			lastpageIntent.putExtra("span", span);
			lastpageIntent.putExtra("idx", idx);
			lastpageIntent.putExtra("sync", sync);
			lastpageIntent.putExtra("id", deliverId);
			lastpageIntent.putExtra("title", title);
			lastpageIntent.putExtra("sec", secName);
		}

		/*new Thread(){
			public void run(){
				try {
		    		if(shouldSyncToServer && AndroidLibrary.is3gConnected(Reader.this)){
		    			//thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_PROGRESS,getResources().getString(R.string.iii_last_page_upload_progress)));
		    			boolean isSuccessful = lastHelper.uploadXml(getDeliverId(), titleFromProfile, retain.curSecFilename, span, idx, (int)System.currentTimeMillis());
		    			if(isSuccessful){
		    				Log.d("!!!!!!!upload","success");
		    				//Toast.makeText(this, getResources().getString(R.string.iii_last_page_upload_success), Toast.LENGTH_SHORT);
		    				//thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_last_page_upload_success)));
		    				//Toast.makeText(Reader.this.getBaseContext(), getResources().getString(R.string.iii_last_page_upload_success), Toast.LENGTH_SHORT);
		    				//Toast.makeText(this, getResources().getString(R.string.iii_last_page_upload_success), Toast.LENGTH_SHORT).show();
		    			}else if(showLastPageSyncError){
		    				//Toast.makeText(this, getResources().getString(R.string.iii_last_page_upload_fail), Toast.LENGTH_SHORT);
		    				Toast.makeText(Reader.this.getBaseContext(), getResources().getString(R.string.iii_last_page_upload_fail), Toast.LENGTH_SHORT);
		    				//thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_last_page_upload_fail)));
		    				//Toast.makeText(this, getResources().getString(R.string.iii_last_page_upload_fail), Toast.LENGTH_SHORT).show();
		    			}
		    		}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if(showLastPageSyncError)
						Toast.makeText(Reader.this.getBaseContext(), getResources().getString(R.string.iii_last_page_upload_fail), Toast.LENGTH_SHORT);
					Log.e("Reade:onPause","uploadXml:"+e.toString());
				}
			}
		}.start();*/
	}
	
	public void onPause(){
		//Log.d("on","pause");
		super.onPause();
		if (LinedContent.markerStrokeTable != null) {
			LinedContent.markerStrokeTable.clear();
			LinedContent.markerStrokeTable = null;
		}
		if(VerticalLinedContent.markerStrokeTable!=null)
		{
			VerticalLinedContent.markerStrokeTable.clear();
			VerticalLinedContent.markerStrokeTable=null;
		}		
		resetMarkerImageTable();
	}
	
	/**
	 * 組織書籍目錄，並將不在spine上的刪除
	 */
    private void constructTocList(){
    	try{
    		NCXReader ncxr = new NCXReader("file://"+retain.ncxPath);
    		retain.tocList=ncxr.getTocList();
    		if(retain.tocList!=null){
    			for(int i=0;i<retain.tocList.size();i++){
            		if(retain.spineList.indexOf(retain.tocList.get(i).get("href"))<0){
            			retain.tocList.remove(i);
            			i--;
            		}	
            	}
    		}else{
    			retain.tocList = new ArrayList<Map<String,String>>();
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	/*
    	tocMap = new HashMap<String,String>();
    	for(int i=0;i<tocList.size();i++){
    		Log.d(tocList.get(i).get("href"), tocList.get(i).get("text"));
    		tocMap.put(tocList.get(i).get("href"), tocList.get(i).get("text"));
    	}*/
    }
    

    /**
     * 呈現錯誤訊息的method，跳出AlertDialog，上面有提示文字和確認鈕。如果shouldDestroy參數為true，按下確認鈕後即finish activity，否則僅關閉dialog
     * @param text 提示訊息
     * @param shouldDestroy 是否關閉activity
     */
    private void showErrorMessage(String text,final boolean shouldDestroy){
		AlertDialog.Builder builder = new AlertDialog.Builder(Reader.this);
		if(globalAd!=null && globalAd.isShowing() )
			globalAd.dismiss();
		globalAd=builder.create();
		globalAd.setTitle(getResources().getString(R.string.iii_error));
		globalAd.setMessage(text);
		globalAd.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.iii_showAM_ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				globalAd.dismiss();
				if(shouldDestroy){
					leaveReading();
				}
			}
			
		});
		globalAd.show();
    }
    
    /**
     * 跳出確認訊息，按下確認鈕後訊息框消失
     * @param title 訊息標題
     * @param text 訊息內文
     */
    private void showConfirmMessage(String title, String text){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if(globalAd!=null && globalAd.isShowing())
			globalAd.dismiss();
		globalAd=builder.create();
		if(title!=null && title.length()>0)
			globalAd.setTitle(title);
		globalAd.setMessage(text);
		globalAd.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.iii_showAM_ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				globalAd.dismiss();
			}
			
		});
		globalAd.show();
    }
    
    /**
     * 解壓縮並讀取(parse)章節
     * @param action 解壓縮完進行何種讀取方式
     * @param pos1 span，頁面定位點，用來判斷呈現哪一頁
     * @param pos2 index，頁面定位點，用來判斷呈現哪一頁
     * @param tidx thread index，用來判斷thread的開關
     */
    private void unzipAndLoadChapter(final int action,final int pos1,final int pos2,final int tidx){
    	Log.d("UnzipAndLoadChapter","do");
    	if(tidx==threadIdx && !isReaderClosed){
    		//Log.d("threadIdx","is:"+threadIdx);
    		thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_PROGRESS,getResources().getString(R.string.iii_unzip_progress)));
    	}
    	if(retain.curSecNo==retain.spineList.size()-1){
    		ev.setIsLastPage(true);
    		retain.receiver.startParsing("file://"+retain.targetDir+retain.curSecFilename, retain.uz);
    		thandler.sendMessage(thandler.obtainMessage(action,0,0,retain.receiver));
    	}else{		
    		ev.setIsLastPage(false);
        	isTurning=true;
        	new Thread(){
        		public void run(){
        			//a=System.currentTimeMillis();
        			//Log.e("In unzipAndLoadChapter", String.valueOf(System.currentTimeMillis()));
        			//InputStream stream=null;
        			try{
        				//Log.d("curSecFileNameInUnzipAndLoad","is:"+curSecFilename);
        				//stream = UZ.getElementInputStream(targetDir+curSecFilename,true);
        				retain.uz.unzipFile(retain.targetDir+retain.curSecFilename,true);
        				//AndroidLibrary.copyFile(new File(targetDir+curSecFilename), new File("/sdcard/chap818.xml"));
        			}catch(DeviceIDException e){
        				thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1,e.getMessage()));
        			}catch(Exception e){
        				Log.e("Reader:initializePage",e.toString());
        				//showErrorMessage(getResources().getString(R.string.iii_file_error),getResources().getString(R.string.iii_decode_error),true);
        				thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decode_error)));
        				//leaveReading();
        			}finally{
        				//retain.hasLoadChapterCalled=true;
        				retain.receiver.startParsing("file://"+retain.targetDir+retain.curSecFilename, retain.uz);
        				thandler.sendMessage(thandler.obtainMessage(action,pos1,pos2,retain.receiver));
        				//Log.e("Done unzipAndLoadChapter", String.valueOf(System.currentTimeMillis()));
        				//Log.e("unzipAndLoadChapter need ", String.valueOf(System.currentTimeMillis() - a));
        			}
        		}
        	}.start();
    	}

    }
    
	public boolean onTouchEvent(MotionEvent me){
		if(!isImgViewerActivated && me.getAction()==MotionEvent.ACTION_UP && !bContextOpen){
			if(me.getX() >= (2*screenWidth/3  )){
				if(isTouch){
					showMenu(isTouch);
				}	
				if(renderer.isVertical())
					pageUp();
				else
					pageDown();
				return true;
			}else if(me.getX() <= screenWidth/3 ){
				if(isTouch){
					showMenu(isTouch);
				}	
				if(renderer.isVertical())
					pageDown();
				else
					pageUp();
				return true;
			}else{
				showMenu(isTouch);	
			}
			return false;
		}
		return super.onTouchEvent(me);
	}

	public void handleShowMenu(){
		showMenu(isTouch);
	}
	/**
	 * 同步(下載)最後閱讀頁
	 * @param tidx 執行緒index，用以判斷執行緒開關
	 */
    private void syncLastPage(final int tidx){
    	new Thread(){
    		public void run(){
    			try{
    				boolean result = lastHelper.downloadXml(lastPages, getDeliverId());
    				if(result)
    					thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_last_page_sync_success)));
    				else if(showLastPageSyncError)
    					thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_last_page_sync_fail)));
    			}catch(DeviceIDException e){
    				thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,e.getMessage()));
    			}catch(Exception e){
    				if(showLastPageSyncError)
    					thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_last_page_sync_fail)));
    				Log.e("Reader:syncLastPage",e.toString());
    			}finally{
    				if(tidx==threadIdx)
    					decrypt(tidx);
    			}
    		}
    	}.start();
    }
    
    /**
     * 將teb解密為epub檔案
     * @param tidx thread index，用來判斷thread的開關
     */
    private void decrypt(final int tidx){
    	if(!isReaderClosed)
    		thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_PROGRESS,getResources().getString(R.string.iii_decrypt_progress)));

    	new Thread(){
    		public void run(){
    			try{
    				//Decipher.decrypt(dcfPath, epubPath, Reader.this);
    				//System.out.println(dcfPath);
    				//System.out.println(epubPath);

    				if(dcfPath.endsWith(".teb") || dcfPath.endsWith(".TEB")){
    					if(!(new File(dcfPath.substring(dcfPath.length()-3)+"epub")).exists()){
    						Log.d("decrypt","teb");
    						//a=System.currentTimeMillis();
    						//Log.e("In NewDecipher.testEPub", String.valueOf(System.currentTimeMillis()));
    						//NewDecipher.sc=null;
							NewDecipher.decryptEpub(dcfPath,epubPath , getApplicationContext());
    						//Log.e("Done NewDecipher.testEPub", String.valueOf(System.currentTimeMillis()));
    						//Log.e("NewDecipher.testEPub need ", String.valueOf(System.currentTimeMillis() - a));
    					}
    				}
    				if(tidx==threadIdx)
    					thandler.sendMessage(thandler.obtainMessage(ACTION_GET_METADATA,tidx,tidx));
    			}
//    			catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					if(!isReaderClosed)
//						thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decrypt_io_error)));
//				} catch (IllegalRightObjectException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					if(!isReaderClosed)
//						thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decrypt_p12_error)));
//				} catch (DeviceIDException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					if(!isReaderClosed)
//						thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decrypt_p12_error)));
//				} catch (NOPermissionException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					if(!isReaderClosed)
//						thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decrypt_permission_error)));
//				} catch (IllegalP12FileException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					if(!isReaderClosed)
//						thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decrypt_p12_error)));
//				}
				catch (Exception e){
					e.printStackTrace();
					String errorMsg = "";
					if(e instanceof IOException){
						errorMsg = getResources().getString(R.string.iii_decrypt_io_error);
					}else if(e instanceof IllegalRightObjectException){
						errorMsg = getResources().getString(R.string.iii_decrypt_p12_error);
					}else if(e instanceof DeviceIDException){
						errorMsg = getResources().getString(R.string.iii_decrypt_p12_error);
					}else if(e instanceof NOPermissionException){
						errorMsg = getResources().getString(R.string.iii_decrypt_permission_error);
					}else if(e instanceof IllegalP12FileException){
						errorMsg = getResources().getString(R.string.iii_decrypt_p12_error);
					}
					
					/*check new GSiMediaInputStreamProvider fail by deviceID*/
					String deviceID;
					try {
						deviceID = GSiMediaRegisterProcess.getID(getApplicationContext());
					} catch (com.gsimedia.sa.GSiMediaRegisterProcess.DeviceIDException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						deviceID = "";
					}
    				if(deviceID.length() == 0)errorMsg = getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG);
    				
					if(!isReaderClosed)
						thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, errorMsg));
				}
				/*catch(Exception e){
				Log.e("Reader:decrypt",e.toString());
				if(!isReaderClosed)
					thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decrypt_error)));
				}*/
    		}
    	}.start();
    }
     
    /**
     * 取得epub metadata。
     * @param tidx 執行緒index，用以判斷執行緒開關
     */
     private void getMetadata(final int tidx){
     	try {
     		retain.uz = new PartialUnzipper(new File(epubPath),getApplicationContext());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("Reader:getMetadata",e.toString());
			thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_decode_error)));
		}
		new Thread(new PrepareMetadataFile(this,tidx)).start();
     	//unzip(tidx);
     } 
    
     /**
      * 是否設定為直書
      * @return 是否設定為直書
      */
    private boolean isVertical(){
    	return settings.getBoolean(getDeliverId()+"reader_setting_chinese_type_value", false);
    }

    /**
     * 移到上一頁
     */
    public void pageUp(){
    	if(!isTurning){
			isTurning=true;
			if( renderer.canPageUp()){
				renderer.pageUp();
				//20110510 benson modified for fixing wrong direction of animation while backing to previous page from last page.
				ev.setPageIndex(BaseAnimation.PageIndex.previous);
			}else{
				if(retain.curSecNo>= 1){
					moveToPreviousChapter();
					ev.setPageIndex(BaseAnimation.PageIndex.previous);
				}else{
					isTurning=false;	
				}
			} 
		}
	}
	 
    /**
     * 移到下一頁
     */
	public void pageDown(){
		if(!isTurning){
			isTurning=true;
			if(renderer.canPageDown()){
				renderer.pageDown();
				ev.setPageIndex(BaseAnimation.PageIndex.next);
			}else{
				if((retain.curSecNo+1)< retain.spineList.size()){
					moveToNextChapter();
					ev.setPageIndex(BaseAnimation.PageIndex.next);
				}else{
					isTurning=false;	
				}

			}
		}
	}
	
	/**
	 * 取得目前在全書百分比
	 * @return 目前在全書百分比
	 */
	private int getPercentageInBook(){
		try{
			return (retain.curSecNo*100+renderer.getPercentageInChapter())/retain.spineList.size();
		}catch(Exception e){
			return 0;
		}
	}
	
	/**
	 * 移到下一章節
	 */
	private void moveToNextChapter(){
		retain.curSecFilename=retain.spineList.get(++retain.curSecNo);
		unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,0,0,threadIdx);
    }
    
	/**
	 * 移到前一章節
	 */
	private void moveToPreviousChapter(){
    	//Log.d("Chapter","Up");
		retain.curSecFilename=retain.spineList.get(--retain.curSecNo);
		unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,-1,-1,threadIdx);
    }
    

	/**
	 * 進入畫線模式
	 */
	private void startUnderlineMode(){
		isUnderlineRemovalOpen=false;
		isUnderlineOpen=true;
		thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_underline_hint)));
		/*draw underline on top line*/
		hideBookMark(true);
	}
	
	/**
	 * 進入畫線刪除模式
	 */
	private void startUnderlineDeleteMode(){
		isUnderlineRemovalOpen=true;
		isUnderlineOpen=false;
		thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_underline_delete_hint)));
		/*draw underline on top line*/
		hideBookMark(true);
	}
	

	/** draw underline on top line
	 *  hide bookmark imageview and don't let it get onClick event
	 * @param needHide
	 */
	private void hideBookMark(boolean needHide){
		if(needHide){
			ib_reader_is_bookmark_right.setVisibility(View.GONE);
			ib_reader_is_bookmark_left.setVisibility(View.GONE);
		}else{
			ib_reader_is_bookmark_right.setVisibility(View.VISIBLE);
			ib_reader_is_bookmark_left.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public void onRenderingFinished() {
		// TODO Auto-generated method stub
		/**
		 * 暫存章節檔案
		 */
		//AndroidLibrary.copyFile(new File(targetDir+curSecFilename),new File("/sdcard/chap123.xml"));
		//Log.e("Finished", String.valueOf(System.currentTimeMillis()));

		//2011/05/17 Jonathan set pageIndex to default fix zoom in/out will flip content. 
		ev.setPageIndex(BaseAnimation.PageIndex.current);
		//AndroidLibrary.copyFile(new File(targetDir+curSecFilename),new File("/sdcard/chap123.xml"));
		if(!isReaderClosed){
			//Log.d("aaa","bbb");
			if(retain.curSecFilename!=null){
				lastPages.edit().putString(getDeliverId()+"_chap_name", retain.curSecFilename).commit();
	    		lastPages.edit().putInt(getDeliverId()+"_span", renderer.getCurPageStartSpan()).commit();
	    		lastPages.edit().putInt(getDeliverId()+"_idx", renderer.getCurPageStartIdxInSpan()).commit();
	    		lastPages.edit().putString(getDeliverId()+"_time",  String.valueOf(System.currentTimeMillis())).commit();
			}
	    	//if(isFirstRender){
		        /*if(settings.getBoolean("reader_setting_night_mode_value", false)){
		        	ev.setBackgroundDrawable(null);
		        	ev.setBackgroundColor(Color.BLACK);
		        }else{
		        	ev.setBackgroundDrawable(RendererConfig.getBackground(this));
		        }*/
		        //isFirstRender=false;
			//}
	    	thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_STATUS));

			isTurning=false;
			progress.dismiss();
		}

		//Log.d("progress","dismiss:renderingFinished");
	}
	

	
	/**
	 * 在全書末頁顯示促銷文字
	 */
	private void setPromoPageAtLast(){
		try {
			AndroidLibrary.copyFileFromInputStream(getAssets().open("iii_last_page.html"), new File(retain.targetDir+"twmLastPage.html"));
			retain.spineList.add("twmLastPage.html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * parse opf檔案，完成後取得最後閱讀頁並呼叫unzipAndLoadChapter開啟章節閱讀
	 * @param tidx 執行緒index，用以判斷執行緒開關
	 */
	private void parse(final int tidx){
		if(tidx==threadIdx){
	        try{ 
	            OPFReader opfr = new OPFReader("file://"+retain.opfPath);
	            retain.spineList=opfr.getSpineList();
	            setPromoPageAtLast();
	            retain.dataset = opfr.getDataSet();
	            retain.bookTitle = retain.dataset.getTitle();
	            //NCXReader ncxr = new NCXReader("file://"+ncxPath);
	            //tocList=ncxr.getTocList();
	            //constructTocList();
	            setTitleBar();
	            
	            
	            retain.curSecFilename=lastPages.getString(getDeliverId()+"_chap_name", null);
            	int spanNo=0;int idxInSpan=0;
	            if(retain.curSecFilename==null || retain.curSecFilename=="null" ||retain.spineList.indexOf(retain.curSecFilename)<0 || retain.spineList.indexOf(retain.curSecFilename)>=retain.spineList.size()){
                	//ev.setIsStart(true);
                	retain.curSecNo=0;
                	retain.curSecFilename=retain.spineList.get(0);
	            }else{
	            	retain.curSecNo=retain.spineList.indexOf(retain.curSecFilename);
                	spanNo=lastPages.getInt(getDeliverId()+"_span", 0);
                	idxInSpan=lastPages.getInt(getDeliverId()+"_idx", 0);
                	retain.curSecFilename=retain.spineList.get(retain.curSecNo);
                	/*if(retain.curSecNo>0 || spanNo>0 || idxInSpan>0)
                		ev.setIsStart(false);
                	else
                		ev.setIsStart(true);
                		*/
	            }
                retain.isCallRenderOnRetain=true;
                showManual();
	            
                if(tidx==threadIdx)
                	unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,spanNo,idxInSpan,tidx);
	        }catch(Exception e){   
	            	Log.e("Reader:parse",e.toString());
	            	if(!isReaderClosed)
	            		thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, getResources().getString(R.string.iii_parse_error)));
	        }finally{          	
	                //curSecNo=spineList.indexOf(curSecFilename); 
	                //curSecY=lastPages.getInt(dataset.getTitle()+"scrollY", 0);
	        } 	
		}
    }
	
	/**
	 * 根據設定顯示操作說明
	 */
	private void showManual(){
		//Log.d("show","manual");
        if(settings.getBoolean("show_manual", true)){
        	//Log.d("show","true");
//        	showMenu(true);
//        	ev.setShowManual(true);
        	
        	iFirstPageView.post(new Runnable(){
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				iFirstPageView.setVisibility(View.VISIBLE);
    			}});
        	//showFirstPage(true);
        	
        	settings.edit().putBoolean("show_manual", false).commit();
        }       
	}
	
	private void showFirstPage(boolean aShow) {
		Log.d(Config.LOGTAG, "showFirstPage:" + aShow);
		if (aShow) {
			this.iFirstPageView.setVisibility(View.VISIBLE);
			// 110303 disable control panel when showing first page
			showMenu(aShow);
		} else
			this.iFirstPageView.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * 顯示progress dialog告知使用者正在讀取中
	 * @param message 說明文字
	 */
	private void showProgress(String message){
		try {
			if (!progress.isShowing()) {
				if (message!= null)
					progress.setMessage(message);

				progress.show();
			} else {
				if (message != null) {
					progress.setMessage(message);
					progress.show();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Reader:showProgress",e.toString());
		}
	}
	
	/**
	 * 執行緒處理class，當其他執行緒處理完發出message給此class物件回到UI thread來handle
	 * @author III
	 * 
	 */
	private static class ThreadHandler extends Handler{
		WeakReference<Reader> mReaderRef;
		ThreadHandler(Reader aReaderRef){
			mReaderRef = new WeakReference<Reader>(aReaderRef);
		}
    	public void handleMessage(Message msg) {
    		final Reader aReader = mReaderRef.get();
    		try {
				switch( msg.what ){
					case 0:				
						break;   
					case ACTION_SHOW_READER:
						if (aReader != null)
							aReader.ev.requestFocus();
						break;
					//case ACTION_LOAD_CHAPTER:
						//renderer.loadChapterByTextIdx("file://"+targetDir+curSecFilename, msg.arg1,UZ,curSecFilename);
						//break;
					case ACTION_LOAD_CHAPTER_BY_SPAN:
						if(renderer!=null && (aReader != null))
							renderer.loadChapterBySpanAndIdx((HtmlReceiver) msg.obj,msg.arg1, msg.arg2,aReader.retain.uz,aReader.retain.curSecFilename);
						break;
					case ACTION_GET_METADATA:
						aReader.getMetadata(msg.arg1);
						break;
					case ACTION_LOAD_CHAPTER_BY_PERCENTAGE:
						if(renderer!=null && (aReader != null))
							renderer.loadChapterByPercentage((HtmlReceiver) msg.obj,msg.arg1,aReader.retain.uz,aReader.retain.curSecFilename);
						break;
					case ACTION_SHOW_PROGRESS:
						if (aReader != null)
							aReader.showProgress((String) msg.obj);
						break;
					case ACTION_DISMISS_PROGRESS:
						if (aReader != null){
							try {
								if(aReader.progress!=null){
									aReader.progress.dismiss();
									//Log.d("progress","dismiss:threadhandler");
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						break;
					case ACTION_SHOW_ERROR:
						if (aReader != null){
							aReader.showErrorMessage((String) msg.obj,msg.arg1>0?true:false);
						}
						break;
					case ACTION_SHOW_TOAST:
						//Log.d("!!!!!!show","!!!!!!toast");
						if (aReader != null){
							if(!aReader.isReaderClosed)
								Toast.makeText(aReader.getApplicationContext(), (String)msg.obj, Toast.LENGTH_SHORT).show();
						}

						break;
					case ACTION_SHOW_STATUS:
						if (aReader != null){
							if(!aReader.isReaderClosed){
								aReader.setSeekBarProgress(aReader.getPercentageInBook());
								aReader.setCurPageStatus();
							}
						}

						break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
    	
    }
	/**
	 * 準備好(解壓縮)metadata檔案以進行parse
	 * @author III
	 * 
	 */
	private static class PrepareMetadataFile implements Runnable{
		int localThreadIdx;
		WeakReference<Reader> mReaderRef;

		public PrepareMetadataFile(Reader aReaderRef, final int tidx){
			super();
			mReaderRef = new WeakReference<Reader>(aReaderRef);
			localThreadIdx=tidx;
		}
		@Override
		public void run() {
			//a=System.currentTimeMillis();
			//Log.e("In UnzipJob", String.valueOf(System.currentTimeMillis()));
			// TODO Auto-generated method stub
    		final Reader aReader = mReaderRef.get();
    		if (aReader != null){
    			if(localThreadIdx==aReader.threadIdx){
            		try{
            			aReader.retain.uz.setList();
            			aReader.retain.targetDir=aReader.retain.uz.getTargetDir();
            			aReader.retain.ncxPath=aReader.retain.uz.getNcxPath();
            			aReader.retain.opfPath=aReader.retain.uz.getOpfPath();
            			//Log.e("Done UnzipJob", String.valueOf(System.currentTimeMillis()));
            			//Log.e("UnzipJob need ", String.valueOf(System.currentTimeMillis() - a));
            	        Log.d("OPFPath",":"+aReader.retain.opfPath);
            		}catch(DeviceIDException e){
            			aReader.thandler.sendMessage(aReader.thandler.obtainMessage(ACTION_SHOW_ERROR, 1, 1, e.getMessage()));
            		}catch(Exception e){
            			e.printStackTrace();
            		}finally{
            			//a=System.currentTimeMillis();
            			//Log.e("In parse", String.valueOf(System.currentTimeMillis()));
            			aReader.parse(localThreadIdx);
            			//Log.e("Done parse", String.valueOf(System.currentTimeMillis()));
            			//Log.e("parse need ", String.valueOf(System.currentTimeMillis() - a));
            		}
    			}
    		}
		}
		
	}


	@Override
	public boolean isUnderlineOpen() {
		// TODO Auto-generated method stub
		return isUnderlineOpen;
	}


	@Override
	public void onLinkClicked(String url) {
		// TODO Auto-generated method stub
		/*Log.d("url","is:"+url);
		if(url.length()>=4 && url.substring(0,4).equalsIgnoreCase("http")){
			
		}else{
			//discard deep linking
			int endIdx=url.indexOf("#");
			if(endIdx>=0){
				url=url.substring(0, endIdx);
			}
			int secNo;
			if((secNo=retain.spineList.indexOf(url))>=0){
				if(secNo!=retain.curSecNo){
					retain.curSecNo=secNo;
					retain.curSecFilename=retain.spineList.get(retain.curSecNo);
					if(!isTurning)
						unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,0,0,threadIdx);
				}
			}
		}*/
		
	}

	@Override
	public void onGetUnderline(int startSpan, int startIdx, int endSpan,
			int endIdx) {	
		// TODO Auto-generated method stub
		if(endSpan>startSpan || (endSpan==startSpan&&endIdx>=startIdx))
			renderer.insertUnderline(retain.bookTitle, retain.curSecFilename, getDeliverId(),  startSpan, startIdx, endSpan, endIdx);
		else
			renderer.insertUnderline(retain.bookTitle, retain.curSecFilename, getDeliverId(), endSpan, endIdx,  startSpan, startIdx);
		isUnderlineOpen=false;
		isUnderlineRemovalOpen=false;
		//ev.redraw();
		/*draw underline on top line*/
		hideBookMark(false);
	}

	
	//private float scale=1;
	private boolean isImgViewerActivated=false;
	/**
	 * 使用者點選圖片，reader inflate一個view放大圖片讓使用者檢視
	 */
	@Override
	public boolean onImgClicked(final String url) {
		// TODO Auto-generated method stub
		if(!isImgViewerActivated){
			try{				
				if(isTouch){
					showMenu(isTouch);
				}
				final RelativeLayout viewer = (RelativeLayout) View.inflate(this, R.layout.iii_img_viewer, null);
				//LinearLayout ll = (LinearLayout) viewer.findViewById(R.id.img_linear);
				ImageButton in = (ImageButton) viewer.findViewById(R.id.zoom_in);
				ImageButton out = (ImageButton) viewer.findViewById(R.id.zoom_out);
				ImageButton back = (ImageButton) viewer.findViewById(R.id.back);
				final LargeImageView iv = (LargeImageView) viewer.findViewById(R.id.img_view);
				totalLayout.addView(viewer, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
				totalLayout.bringToFront();
				
				File imgFile = new File(url);
				boolean successful = iv.setImage(imgFile,screenWidth,screenHeight);
				if(!successful){
					isImgViewerActivated=false;
					totalLayout.removeView(viewer);
					return false;
				}
				
				//tv_book_title.setVisibility(View.INVISIBLE);
				
				back.setOnClickListener(new OnClickListener(){ 
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						iv.recycle();
						isImgViewerActivated=false;
						totalLayout.removeView(viewer);
						tv_book_title.setVisibility(View.VISIBLE);
						//ad.dismiss();
					}
					
				});
				in.setOnClickListener(new OnClickListener(){ 
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						//iv.handleView(iv.ZOOM_IN);
						try{
							iv.resize(true);
						}catch(Exception e){
							Log.e("Reader:onImgClicked","zoomInError:"+e.toString());
						}
					}
					
				});
				out.setOnClickListener(new OnClickListener(){ 
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						//iv.handleView(iv.ZOOM_OUT);
						try{
							iv.resize(false);
						}catch(Exception e){
							Log.e("Reader:onImgClicked","zoomOutError:"+e.toString());
						}
					}
					
				});
				isImgViewerActivated=true;
			}catch(Exception e){
				Log.e("Reader:onImgClicked","decodeError");
				isImgViewerActivated=false;
			}

		}
		return true;
	}

	@Override
	public boolean isUnderlineRemovalOpen() {
		// TODO Auto-generated method stub
		return isUnderlineRemovalOpen;
	}

	@Override
	public ArrayList<Underline> onDeleteUnderline(int span, int idx) {
		// TODO Auto-generated method stub
		final ArrayList<Underline> ulForDelete = renderer.getUnderlineBySpanAndIdx(span, idx);
		if(ulForDelete!=null && ulForDelete.size()>0 ){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			globalAd=builder.create();
			globalAd.setTitle(getResources().getString(R.string.iii_underline_dialog_title));
			globalAd.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.iii_underline_dialog_delete), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					isUnderlineRemovalOpen=false;
					renderer.deleteUnderline(ulForDelete);
					/*draw underline on top line*/
					hideBookMark(false);
					renderer.reload();
					globalAd.dismiss();
				}
				
			});
			globalAd.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.iii_underline_dialog_cancel), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					/*draw underline on top line*/
					hideBookMark(false);
					globalAd.dismiss();
					renderer.reload();
					isUnderlineRemovalOpen=false;
				}
				
			});
			globalAd.show();
			return ulForDelete;
		}
		return null;
	}

	/**
	 * 根據該頁面有無書籤和註記調整view component
	 */
	public void setBookmarkAndNoteFlag(){
		try {
			if(renderer!=null){
				boolean test = renderer.isCurPageAnnotated();
				//Log.d("isCurpageAnned","is:"+test);
				//showBookmarkIcon(renderer.isCurPageBookmarked());
				showNoteIcon(test);	    
				//updateBookmarkButton(renderer.isCurPageBookmarked());
				//updateNoteButton(renderer.isCurPageAnnotated());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean getIsTrial() {
		// TODO Auto-generated method stub
		return isTrial;
	}

	@Override
	public boolean getIsNightMode() {
		// TODO Auto-generated method stub
		boolean nightMode = settings.getBoolean("reader_setting_night_mode_value", false);
		RendererConfig.isNightMode=nightMode;
		//return settings.getBoolean(getDeliverId()+"reader_setting_night_mode_value", false);
		return nightMode;
	}

	@Override
	public String getContentId() {
		// TODO Auto-generated method stub
		return contentId;
	}

	@Override
	public String getTitleFromProfile() {
		// TODO Auto-generated method stub
		return titleFromProfile;
	}
	

	public String getDeliverId(){
		return epubPath.substring(epubPath.lastIndexOf("/")+1, epubPath.lastIndexOf("."));
	}
	
	/**
	 * 設定偏好設定預設值
	 * @param isVertical 是否為直書，由於每本書有不同的橫/直書預設，故需要另外傳入
	 */
	private void setDefaultPreferences(boolean isVertical) {
		// TODO Auto-generated method stub
		settings = getSharedPreferences("reader_Preference", 0);
		if("".equals(settings.getString("reader_setting_book_background_style_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
			settings.edit().putString("reader_setting_book_background_style_value", temp[0]).commit();
		}
		settings.edit().putBoolean("show_manual", settings.getBoolean("show_manual", true)).commit();
		settings.edit().putBoolean(getDeliverId()+"reader_setting_chinese_type_value", settings.getBoolean(getDeliverId()+"reader_setting_chinese_type_value", isVertical)).commit();
		settings.edit().putBoolean("reader_setting_night_mode_value", settings.getBoolean("reader_setting_night_mode_value", false)).commit();
		settings.edit().putInt(getDeliverId()+"reader_setting_font_size_value", settings.getInt(getDeliverId()+"reader_setting_font_size_value", Reader.DEFAULT_FONTSIZE_IDX)).commit();
		
		if("".equals(settings.getString("reader_setting_font_color_value", ""))){
			//String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			settings.edit().putString("reader_setting_font_color_value", "").commit();
		}

		if("".equals(settings.getString("reader_setting_crossed_color_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			settings.edit().putString("reader_setting_crossed_color_value", temp[18]).commit();
		}
		
		if("".equals(settings.getString("reader_setting_hyperlink_color_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			settings.edit().putString("reader_setting_hyperlink_color_value", temp[9]).commit();
		}
		
		if("".equals(settings.getString("reader_setting_flip_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_flip_value);
			settings.edit().putString("reader_setting_flip_value", temp[1]).commit();
		}
		settings.edit().putBoolean("reader_setting_screen_rotation_value", settings.getBoolean("reader_setting_screen_rotation_value", true)).commit();
		settings.edit().putBoolean("reader_setting_hidden_value", settings.getBoolean("reader_setting_hidden_value", true)).commit();	
	}

	@Override
	public String getAuthors() {
		// TODO Auto-generated method stub
		return bookAuthors;
	}

	@Override
	public String getPublisher() {
		// TODO Auto-generated method stub
		return bookPublisher;
	}

	
	@Override
	public void onHtmlParseError() {
		// TODO Auto-generated method stub
		this.onRenderingFinished();
	}

	/**
	 * 第一次parse未過，改用tag soup前處理
	 */
	@Override
	public void onTagSoupReload() {
		// TODO Auto-generated method stub
		renderer.reloadChapter(retain.receiver, retain.uz, retain.curSecFilename);
	}
	
	/**
	 * 根據目前進度調整seek bar位置
	 * @param percentage  百分比
	 */
	private void setSeekBarProgress(int percentage){
		sb_reader.setProgress(percentage);
	}
	
	/**
	 * activity所有view的取得和初始化
	 */
	private void setViewComponent() {
		
		this.iFirstPageView = (RelativeLayout) findViewById(R.id.firstpage);
		
    	totalLayout = (RelativeLayout)findViewById(R.id.reader_full_layout);
 //       rl = (RelativeLayout)findViewById(R.id.rl_reader_title);
 //       ll = (LinearLayout)findViewById(R.id.ll_reader_bottom);
//        al = (AbsoluteLayout)findViewById(R.id.al_reader_note_bookmark);
 //       al_2 = (AbsoluteLayout)findViewById(R.id.al_reader_book_page_info);
        
        ev = (EpubView) findViewById(R.id.gsimedia_pdf_view);
        ev.setCallback(this);
        boolean nightMode = settings.getBoolean("reader_setting_night_mode_value", false);
        ev.setBackGroundIndex(nightMode);
        RendererConfig.isNightMode=nightMode;
        
        registerForContextMenu(ev);
        
        /*
        if(settings.getBoolean("reader_setting_night_mode_value", false)){
        	ev.setBackgroundDrawable(null);
        	ev.setBackgroundColor(Color.BLACK);
        }else{
        	ev.setBackgroundDrawable(RendererConfig.getBackground(this));
        }*/
        
        tv_book_title = (TextView) findViewById(R.id.gsimedia_title);
        tv_book_title.requestFocus();
        
        sb_reader = (SeekBar) findViewById(R.id.SeekBar);
        
        ib_reader_back = (Button) findViewById(R.id.gsimedia_btn_title_left);
        ib_reader_catalog_jump = (Button) findViewById(R.id.gsimedia_btn_title_right);
        
//        ib_reader_previous_page = (ImageButton) findViewById(R.id.ib_reader_previous_page);
//        ib_reader_next_page = (ImageButton) findViewById(R.id.ib_reader_next_page);        
       
//        ib_reader_setting_bookmark = (ImageButton) findViewById(R.id.ib_reader_setting_bookmark);
//        ib_reader_setting_bookmark_del = (ImageButton) findViewById(R.id.ib_reader_setting_bookmark_del);
//        
//        ib_reader_setting_highlight = (ImageButton) findViewById(R.id.ib_reader_setting_highlight);
//        
//        ib_reader_setting_notes = (ImageButton) findViewById(R.id.ib_reader_notes);
//        ib_reader_setting_notes_del = (ImageButton) findViewById(R.id.ib_reader_notes_del);
//        
//        ib_reader_search = (ImageButton) findViewById(R.id.ib_reader_search);
//        ib_reader_setting = (ImageButton) findViewById(R.id.ib_reader_setting);
        
        ib_reader_is_note = (ImageView) findViewById(R.id.gsimedia_img_annotation);
        ib_reader_is_bookmark_right = (ImageView) findViewById(R.id.gsimedia_img_bookmark_right);      
        this.ib_reader_is_bookmark_right.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if(isTouch)return;
				if(!renderer.isVertical()&& getPercentageInBook()!= 100){
					if(!isTurning && isFunctionEnabled()){
						if(renderer.isCurPageBookmarked()){
							renderer.deleteCurPageBookmark(retain.bookTitle, retain.curSecFilename, getDeliverId());
						}else{
							if(renderer.getBookmarkCountOfBook(getDeliverId())<10){
								renderer.insertBookmark(retain.bookTitle, retain.curSecFilename, getDeliverId());
							}else{
								showConfirmMessage(null, getResources().getString(R.string.iii_bookmark_overflow));
							}
						}
						if(renderer!=null)
							renderer.reload();
					}
				}
			}
		});
        
        
        ib_reader_is_bookmark_left = (ImageView) findViewById(R.id.gsimedia_img_bookmark_left);      
        this.ib_reader_is_bookmark_left.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if(isTouch)return;
				if(renderer.isVertical()&& getPercentageInBook()!= 100){
					if(!isTurning && isFunctionEnabled()){
						if(renderer.isCurPageBookmarked()){
							renderer.deleteCurPageBookmark(retain.bookTitle, retain.curSecFilename, getDeliverId());
						}else{
							if(renderer.getBookmarkCountOfBook(getDeliverId())<10){
								renderer.insertBookmark(retain.bookTitle, retain.curSecFilename, getDeliverId());
							}else{
								showConfirmMessage(null, getResources().getString(R.string.iii_bookmark_overflow));
							}
						}
						if(renderer!=null)
							renderer.reload();
					}
				}
			}
		});
        
       
//        ib_reader_back.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_catalog_jump.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_previous_page.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_next_page.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_setting_bookmark.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_setting_highlight.setBackgroundColor(Color.TRANSPARENT);	
//        ib_reader_setting_notes.setBackgroundColor(Color.TRANSPARENT);	
//        ib_reader_search.setBackgroundColor(Color.TRANSPARENT);	
//        ib_reader_setting.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_is_note.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_is_bookmark.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_setting_notes_del.setBackgroundColor(Color.TRANSPARENT);
//        ib_reader_setting_bookmark_del.setBackgroundColor(Color.TRANSPARENT);
        
//        tv_reader_book_page_info_1 = (TextView) findViewById(R.id.tv_reader_book_page_info_1);
        tv_reader_book_page_info_2 = (TextView) findViewById(R.id.TextView_Info_Page);
        
//        rl.bringToFront();
//        ll.bringToFront();    
//        al.bringToFront();
//        al_2.bringToFront();
        
        sb_reader.setMax(100);
        sb_reader.setProgress(0);
        sb_reader.bringToFront();
        
//        tv_book_title.setSelected(true);
//        tv_reader_book_page_info_1.setSelected(true);
        //sb_reader.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        
		RelativeLayout aCtlSeekBar = (RelativeLayout) findViewById(R.id.RelativeLayout_SeekBar);
		aCtlSeekBar.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}

		});      
        
        this.RelativeLayout_Title = (RelativeLayout) findViewById(R.id.RelativeLayout_Title);
		RelativeLayout_Title.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}

		});
		
		this.RelativeLayout_Ctls = (RelativeLayout) findViewById(R.id.RelativeLayout_Ctls);
		RelativeLayout aCtls = (RelativeLayout) findViewById(R.id.ctls);
		aCtls.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}

		});
		
		RelativeLayout_Ctls_Colors = (RelativeLayout) findViewById(R.id.ctls_color);
		RelativeLayout_Ctls_Colors.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}

		});
		
		RelativeLayout_Ctls_Fonsize = (RelativeLayout) findViewById(R.id.ctls_fonsize);
		RelativeLayout_Ctls_Fonsize.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}

		});
		
		this.helpButton = (Button) findViewById(R.id.gsimedia_btn_help);
		this.helpButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
//				settings.edit().putBoolean("show_manual", true).commit();
//				showManual();
				showFirstPage(true);
			}

		});

		this.rotateButton = (Button) findViewById(R.id.gsimedia_btn_rotate);
		this.rotateButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (shouldChangeOrientation){
					shouldChangeOrientation = false;
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}else{
					shouldChangeOrientation = true;
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
				displayRotate();
				settings.edit().putBoolean("reader_setting_screen_rotation_value", shouldChangeOrientation).commit();		
			}
		});
		displayRotate();
		
		this.bgColorButton = (Button) findViewById(R.id.gsimedia_btn_bgcolor);
		this.bgColorButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (bAColorEnabled)
					enableAColorPanel(false);
				if (bLingColorEnabled) 
					enableLineColorPanel(false);
				if (bASizeEnabled) 
					enableAsizePanel(false);
				
				if (bBGEnabled) {
					enableBGPanel(false);
				} else {
					enableBGPanel(true);
				}
			}

		});
		
		this.aColorButton = (Button) findViewById(R.id.gsimedia_btn_Acolor);
		this.aColorButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (bBGEnabled) 
					enableBGPanel(false);
				if (bLingColorEnabled) 
					enableLineColorPanel(false);
				if (bASizeEnabled) 
					enableAsizePanel(false);
				
				if (bAColorEnabled) {
					enableAColorPanel(false);
				} else {
					enableAColorPanel(true);
				}
			}

		});
		
		this.aSizeButton = (Button) findViewById(R.id.gsimedia_btn_Asize);
		this.aSizeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (bBGEnabled) 
					enableBGPanel(false);
				if (bAColorEnabled)
					enableAColorPanel(false);
				if (bLingColorEnabled) 
					enableLineColorPanel(false);
				
				if (bASizeEnabled) {
					enableAsizePanel(false);
				} else {
					enableAsizePanel(true);
				}
			}
		});
		
		this.lineColorButton = (Button) findViewById(R.id.gsimedia_btn_linecolor);
		this.lineColorButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (bBGEnabled) 
					enableBGPanel(false);
				if (bAColorEnabled)
					enableAColorPanel(false);
				if (bASizeEnabled) 
					enableAsizePanel(false);
				
				if (bLingColorEnabled) {
					enableLineColorPanel(false);
				} else {
					enableLineColorPanel(true);
				}
			}

		});
		
		this.nightModeButton = (Button) findViewById(R.id.gsimedia_btn_nightmode);
		if(getIsNightMode()){
			nightModeButton.setBackgroundResource(R.drawable.gsi_button28_btn);
		}else{
			nightModeButton.setBackgroundResource(R.drawable.gsi_button29_btn);
		}
		
		this.nightModeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				boolean nightMode = settings.getBoolean("reader_setting_night_mode_value", false);
				if(nightMode){
					nightModeButton.setBackgroundResource(R.drawable.gsi_button29_btn);
				}else{					
					nightModeButton.setBackgroundResource(R.drawable.gsi_button28_btn);
				}
				settings.edit().putBoolean("reader_setting_night_mode_value", !nightMode).commit();
				RendererConfig.isNightMode=!nightMode;//**set nightMode
				ev.setBackGroundIndex(!nightMode);

				if(renderer!=null)
					renderer.reload();
			}

		});
		
		this.aChineseTypeButton = (Button) findViewById(R.id.gsimedia_btn_chinese_type);
		if(isVertical()){
			aChineseTypeButton.setBackgroundResource(R.drawable.gsi_button32_btn);
		}else{			
			aChineseTypeButton.setBackgroundResource(R.drawable.gsi_button31_btn);
		}

		this.aChineseTypeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				changeVertical();
			}
		});
		
		this.aFlipButton = (Button) findViewById(R.id.gsimedia_btn_flip);
		this.aFlipButton.setBackgroundResource(getFlipResID(settings.getString("reader_setting_flip_value", "")));	
		this.aFlipButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String currFlipSetting = settings.getString("reader_setting_flip_value", "");
				String[] temp = getResources().getStringArray(R.array.iii_reader_setting_flip_value);
				if(temp.length > 0){
					int i = 0;
					do{
						if(i>=temp.length||temp[i].equals(currFlipSetting)){
							break;
						}
						i++;
					}while(true);
					settings.edit().putString("reader_setting_flip_value", temp[(i+1)%temp.length]).commit();
					switch((i+1)%temp.length){
						case 0:
							aFlipButton.setBackgroundResource(R.drawable.gsi_button34_btn);														
							break;
						case 1:
							aFlipButton.setBackgroundResource(R.drawable.gsi_button35_btn);							
							break;
						case 2:
							aFlipButton.setBackgroundResource(R.drawable.gsi_button33_btn);														
							break;
						default:
							aFlipButton.setBackgroundResource(R.drawable.gsi_button35_btn);							
							break;
					}
					renderer.setTurningMethod(getter.getTurningMethod(getDeliverId()));
				}
			}
		});
		
		this.otherButton = (Button) findViewById(R.id.gsimedia_btn_other);
		this.otherButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				isCallSetting=true;
	  	  		Intent intent = new Intent();
	  	  		intent.putExtra("deliverId", getDeliverId());
	  	  		intent.setClass(Reader.this, ReaderSetting.class);
	  	  		startActivity(intent);
			}

		});
    }
	
	/**
	 * 將書名顯示至標題列
	 */
	public void setTitleBar(){
		tv_book_title.post(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				tv_book_title.setText(retain.bookTitle);
			}});
	}
	
	/**
	 * 是否顯示註記圖示
	 * @param show 是否顯示
	 */
    private void showNoteIcon(boolean show) {
		// TODO Auto-generated method stub
    	if(show){
    		//Log.d("annset","true");
    		ib_reader_is_note.setVisibility(View.VISIBLE);	    		
    	}else{
	        ib_reader_is_note.setVisibility(View.INVISIBLE);	
    	}    	
	}
    
    /**
     * 是否顯示書籤圖示
     * @param show 是否顯示
     */
	private void showBookmarkIcon(boolean show) {
		// TODO Auto-generated method stub
//    	if(show){
//    		ib_reader_is_bookmark.setVisibility(View.VISIBLE);    		
//    	}else{
//    		ib_reader_is_bookmark.setVisibility(View.INVISIBLE);	
//    	}
	}
	
	/**
	 * 顯示/關閉選單
	 * @param hideMenu true隱藏選單，false開啟選單
	 */
	private void showMenu(boolean hideMenu){
		if(hideMenu && !isStatusAlwaysOn){
			isTouch = false;

			RelativeLayout_Title.setVisibility(View.INVISIBLE);
			RelativeLayout_Ctls.setVisibility(View.INVISIBLE);
			
			closeColorPanel();			
		}else{  
			//setCurPageStatus();
			if(!isTurning){
				isTouch = true;

				RelativeLayout_Title.setVisibility(View.VISIBLE);
				RelativeLayout_Ctls.setVisibility(View.VISIBLE);
				RelativeLayout_Title.bringToFront();
				RelativeLayout_Ctls.bringToFront();
				setBookmarkAndNoteFlag(); 
			}
		}    
    }
	
	private void closeColorPanel(){
		
		this.bgColorButton.setBackgroundResource(R.drawable.gsi_button26_btn);
		this.aColorButton.setBackgroundResource(R.drawable.gsi_button27_btn);
		this.aSizeButton.setBackgroundResource(R.drawable.gsi_button36);
		this.lineColorButton.setBackgroundResource(R.drawable.gsi_button24_btn);
		
		RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
		RelativeLayout_Ctls_Fonsize.setVisibility(View.INVISIBLE);
		
		bBGEnabled = false;
		bAColorEnabled = false;
		bASizeEnabled = false;
		bLingColorEnabled = false;
    }
	
	private void enableBGPanel(boolean aEnable) {
		if (aEnable) {		
			RelativeLayout ctl_color = (RelativeLayout) findViewById(R.id.ctl_color);
			ctl_color.removeAllViews();
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
			String currentValue = settings.getString("reader_setting_book_background_style_value", "");
						
			for(int i = 0; i < temp.length; i++){
				View root = (ViewGroup) inflater.inflate(R.layout.color_item_linear, null);
				LinearLayout colorItem_RL = (LinearLayout) root.findViewById(R.id.Color_item_LinearLayout);
				colorItem_RL.setId(i+1);
				colorItem_RL.setTag(temp[i]);
				colorItem_RL.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						
						settings.edit().putString("reader_setting_book_background_style_value", String.valueOf(arg0.getTag())).commit();
						
						if(!getIsNightMode())
							ev.setBackGroundIndex(false);
						
						if(renderer!=null)
							renderer.reload();
						
						enableBGPanel(true);
					}
				});

				ImageView img_color = (ImageView) root.findViewById(R.id.icon);
				img_color.setBackgroundDrawable(getBackground(temp[i]));
				
				if(currentValue.equalsIgnoreCase(String.valueOf(temp[i]))){
					ImageView img_color_select = (ImageView) root.findViewById(R.id.icon_selected);
					img_color_select.setVisibility(View.VISIBLE);
				}

				RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				RLP.addRule(RelativeLayout.RIGHT_OF, i);
				ctl_color.addView(root , RLP);
			}
			
			this.bgColorButton.setBackgroundResource(R.drawable.gsi_button26_2);
			RelativeLayout_Ctls_Colors.setVisibility(View.VISIBLE);
			bBGEnabled = true;
		} else {
			this.bgColorButton.setBackgroundResource(R.drawable.gsi_button26_btn);
			RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
			bBGEnabled = false;
		}
		bAColorEnabled = false;
		bLingColorEnabled = false;
		bASizeEnabled = false;
	}
	
	private void enableAColorPanel(boolean aEnable) {
		if (aEnable) {		
			RelativeLayout ctl_color = (RelativeLayout) findViewById(R.id.ctl_color);
			ctl_color.removeAllViews();
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			String currentValue = settings.getString("reader_setting_font_color_value", "");
						
			for(int i = 0; i < temp.length; i++){
				View root = (ViewGroup) inflater.inflate(R.layout.color_item_linear, null);
				LinearLayout colorItem_RL = (LinearLayout) root.findViewById(R.id.Color_item_LinearLayout);
				colorItem_RL.setId(i+1);
				colorItem_RL.setTag(temp[i]);
				colorItem_RL.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						
						settings.edit().putString("reader_setting_font_color_value", String.valueOf(arg0.getTag())).commit();
						
						if(renderer!=null)
							renderer.reload();
						
						enableAColorPanel(true);
					}
				});

				ImageView img_color = (ImageView) root.findViewById(R.id.icon);
				img_color.setBackgroundDrawable(getCtrlColorDrawable(temp[i]));

				if(currentValue.equalsIgnoreCase(String.valueOf(temp[i]))){
					ImageView img_color_select = (ImageView) root.findViewById(R.id.icon_selected);
					img_color_select.setVisibility(View.VISIBLE);
				}
				
				RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				RLP.addRule(RelativeLayout.RIGHT_OF, i);
				ctl_color.addView(root , RLP);
			}
			
			this.aColorButton.setBackgroundResource(R.drawable.gsi_button27_2);
			RelativeLayout_Ctls_Colors.setVisibility(View.VISIBLE);
			bAColorEnabled = true;
		} else {
			this.aColorButton.setBackgroundResource(R.drawable.gsi_button27_btn);
			RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
			bAColorEnabled = false;
		}
		bBGEnabled = false;
		bLingColorEnabled = false;
		bASizeEnabled = false;
	}

	private void enableLineColorPanel(boolean aEnable) {
		if (aEnable) {		
			RelativeLayout ctl_color = (RelativeLayout) findViewById(R.id.ctl_color);
			ctl_color.removeAllViews();
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			String currentValue = settings.getString("reader_setting_crossed_color_value", "");
						
			for(int i = 0; i < temp.length; i++){
				View root = (ViewGroup) inflater.inflate(R.layout.color_item_linear, null);
				LinearLayout colorItem_RL = (LinearLayout) root.findViewById(R.id.Color_item_LinearLayout);
				colorItem_RL.setId(i+1);
				colorItem_RL.setTag(temp[i]);
				colorItem_RL.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						
						settings.edit().putString("reader_setting_crossed_color_value", String.valueOf(arg0.getTag())).commit();
						
						if(renderer!=null)
							renderer.reload();
						
						resetMarkerImageTable();
						enableLineColorPanel(true);
					}
				});

				ImageView img_color = (ImageView) root.findViewById(R.id.icon);
				img_color.setBackgroundDrawable(getCtrlColorDrawable(temp[i]));

				if(currentValue.equalsIgnoreCase(String.valueOf(temp[i]))){
					ImageView img_color_select = (ImageView) root.findViewById(R.id.icon_selected);
					img_color_select.setVisibility(View.VISIBLE);
				}
				
				RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				RLP.addRule(RelativeLayout.RIGHT_OF, i);
				ctl_color.addView(root , RLP);
			}
			
			this.lineColorButton.setBackgroundResource(R.drawable.gsi_button25);
			RelativeLayout_Ctls_Colors.setVisibility(View.VISIBLE);
			bLingColorEnabled = true;
		} else {
			this.lineColorButton.setBackgroundResource(R.drawable.gsi_button24_btn);
			RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
			bLingColorEnabled = false;
		}
		bBGEnabled = false;
		bAColorEnabled = false;
		bASizeEnabled = false;
	}
	
	private void resetMarkerImageTable(){
		RendererConfig.resetMarkerImageArray();
	}
	
	SeekBar iSeekBar_fontsize = null;
	private void enableAsizePanel(boolean aEnable) {
		if (aEnable) {		
			RelativeLayout ctl_seekbar = (RelativeLayout) findViewById(R.id.ctl_seekbar);
			ctl_seekbar.removeAllViews();

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			View root = (ViewGroup) inflater.inflate(R.layout.fontsize_item_mereader, null);
					
			iSeekBar_fontsize = (SeekBar)root.findViewById(R.id.SeekBar_Size);
			iSeekBar_fontsize.setMax(11);
			iSeekBar_fontsize.setProgress(settings.getInt(getDeliverId()+"reader_setting_font_size_value", Reader.DEFAULT_FONTSIZE_IDX));
			iSeekBar_fontsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub		
					if(fromUser){
					if(!isTurning && isFunctionEnabled()){
						isTurning=true;
						settings.edit().putInt(getDeliverId()+"reader_setting_font_size_value", progress).commit();
						boolean result = renderer.changeFontSize(progress);
		        			renderer.reload();
		        		}
					}
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub				
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}
			}); 
			
			RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			ctl_seekbar.addView(root, RLP);
	
			
			this.aSizeButton.setBackgroundResource(R.drawable.gsi_button36_2);
			RelativeLayout_Ctls_Fonsize.setVisibility(View.VISIBLE);
			bASizeEnabled = true;
		} else {
			this.aSizeButton.setBackgroundResource(R.drawable.gsi_button36_btn);
			RelativeLayout_Ctls_Fonsize.setVisibility(View.INVISIBLE);
			bASizeEnabled = false;
		}
		bBGEnabled = false;
		bLingColorEnabled = false;
		bAColorEnabled = false;
	}
	
	private void displayRotate() {
		if (shouldChangeOrientation) {
			this.rotateButton.setBackgroundResource(R.drawable.gsi_button20_btn);
		} else {			
			this.rotateButton.setBackgroundResource(R.drawable.gsi_button21_btn);
		}
	}
	
	/**
	 * 顯示當前頁面資訊(章節名稱，百分比)
	 */
	private void setCurPageStatus(){
		if(tv_reader_book_page_info_1!=null && tv_reader_book_page_info_1.getVisibility()==View.VISIBLE){
			tv_reader_book_page_info_1.setText(renderer.getChapTitle());
		}
		if(tv_reader_book_page_info_2!=null && tv_reader_book_page_info_2.getVisibility()==View.VISIBLE){
			String progressText = getResources().getString(R.string.iii_reading_percentage);
			tv_reader_book_page_info_2.setText(progressText.substring(0, progressText.length()-1)+sb_reader.getProgress()+"%"+progressText.charAt(progressText.length()-1));
		}
		setBookmarkAndNoteFlag();
	}
	
	/**
	 * 根據當前頁面有無書籤調整書籤按鈕顯示(新增/刪除)
	 * @param bookmarked 當前頁面有無書籤
	 */
	private void updateBookmarkButton(boolean bookmarked){
//		if(bookmarked){
//			if(ib_reader_setting_bookmark!=null)
//				ib_reader_setting_bookmark.setVisibility(View.GONE);
//			if(ib_reader_setting_bookmark_del!=null)
//				ib_reader_setting_bookmark_del.setVisibility(View.VISIBLE);			
//		}else{
//			if(ib_reader_setting_bookmark!=null)
//				ib_reader_setting_bookmark.setVisibility(View.VISIBLE);
//			if(ib_reader_setting_bookmark_del!=null)
//				ib_reader_setting_bookmark_del.setVisibility(View.GONE);					
//		}
	}
	
	/**
	 * 根據當前頁面有無註記調整註記按鈕文字(新增/刪除)
	 * @param noted 當前頁面有無註記
	 */
	private void updateNoteButton(boolean noted){
//		if(noted){
//			if(ib_reader_setting_notes!=null)
//				ib_reader_setting_notes.setVisibility(View.GONE);
//			if(ib_reader_setting_notes_del!=null)
//				ib_reader_setting_notes_del.setVisibility(View.VISIBLE);
//		}else{
//			if(ib_reader_setting_notes!=null)
//				ib_reader_setting_notes.setVisibility(View.VISIBLE);
//			if(ib_reader_setting_notes_del!=null)
//				ib_reader_setting_notes_del.setVisibility(View.GONE);					
//		}		
	}
	
	/**
	 * 設定view listener
	 */
	private void setListener() {
		/*wv.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Log.d("wv","onclicked");
				setTouchView(isTouch);		
      	  	}
        }); */   
		ib_reader_is_note.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
      	  		if(renderer.isCurPageAnnotated()){
      	  			intent.putExtra("id",renderer.getCurPageAnnotation());
      	  			intent.putExtra("isAnnotated", true);
      	  			intent.putExtra("span", renderer.getCurPageStartSpan());
      	  			intent.putExtra("idx", renderer.getCurPageStartIdxInSpan());
      	  			intent.putExtra("bookName", retain.bookTitle);
      	  			intent.putExtra("chapName", retain.curSecFilename);
      	  			intent.putExtra("epubPath", getDeliverId());
          	  		intent.setClass(Reader.this, SettingNotes.class);		
          	  		startActivityForResult(intent,CALL_ANN);	
      	  		}

			}
			
		});
        ib_reader_back.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				//setTouchView(isTouch);
				leaveReading();
      	  	}
        });         
        
        ib_reader_catalog_jump.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				if(!isTurning){
					if(retain.tocList==null)
						constructTocList();
	      	  		Intent intent = new Intent();
	      	  		intent.putExtra("epub_path", getDeliverId());
	      	        ArrayList<String> tocHref = new ArrayList<String>();
	      	        ArrayList<String> tocText = new ArrayList<String>();
	      	        for(int i=0;i<retain.tocList.size();i++){
	      	        	tocHref.add(retain.tocList.get(i).get("href"));
	      	        	tocText.add(retain.tocList.get(i).get("text"));
	      	        }
	      	        intent.putStringArrayListExtra("toc_href", tocHref);
	      	        intent.putStringArrayListExtra("toc_text", tocText);	  		
	      	  		intent.setClass(Reader.this, ReaderCatalogJump.class);
	      	  		retain.isCallRenderOnRetain=false;
	      	  		startActivityForResult(intent,CALL_CATALOG);	
				}

      	  	}
        });         
        

        sb_reader.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				//Log.d("sb","onChange");
			}

			@Override
			public void onStartTrackingTouch(SeekBar sb) {
				// TODO Auto-generated method stub
				//Log.d("sb","onTouch");
			}

			@Override
			public void onStopTrackingTouch(SeekBar sb) {
				// TODO Auto-generated method stub
				//Log.d("sb","after");
				if(!isTurning){
					int progress = sb.getProgress();
					float progressFloat = progress;
					//Log.d("a","a");
					//Log.d("progress","is:"+progress);
					float progressPerChap = 100f/retain.spineList.size();
					//Log.d("progressPerChap","is:"+progressPerChap);
					//Log.d("spineList","size:"+spineList.size());
					retain.curSecNo = (int) (progress/progressPerChap);
					if(retain.curSecNo>=retain.spineList.size())
						retain.curSecNo=retain.spineList.size()-1;
					retain.curSecFilename = retain.spineList.get(retain.curSecNo);
					//Log.d("curSecfileNameInSb","is:"+retain.curSecFilename);
					while(progressFloat>=0)progressFloat-=progressPerChap;
					progressFloat+=progressPerChap;
					progress=(int) progressFloat;
					//progress=(int) (progress*100/progressPerChap);
					
					
					//Log.d("SecNo","is:"+curSecNo);
					//Log.d("progressinChap","is:"+progress);
					//Log.d("sb:progress","is:"+progress);
					//Log.d("sb:curSecNo","is:"+curSecNo);
					unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_PERCENTAGE,progress,progress,threadIdx);
				}
			}
        	
        });
        
//        ib_reader_previous_page.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//				pageUp();
//      	  	}
//        }); 
//        
//        ib_reader_next_page.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//				pageDown();
//      	  	}
//        });    
//
//        ib_reader_setting_bookmark.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//				if(!isTurning && isFunctionEnabled()){
//					if(renderer.getBookmarkCountOfBook(getDeliverId())<10){
//						renderer.insertBookmark(retain.bookTitle, retain.curSecFilename, getDeliverId());
//						setBookmarkAndNoteFlag();
//					}else{
//						showConfirmMessage(null, getResources().getString(R.string.iii_bookmark_overflow));
//					}
//				}
//      	  	}
//        });
//        
//        ib_reader_setting_highlight.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v ){
//
//				Highlight();
//				
//				// 2011/05/16 Jonathan remove codes to Highlight();
//				/*
//				if(isTurning || !isFunctionEnabled() || !renderer.textExistsOnCurPage()){
//					thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_page_no_text)));
//				}else if(renderer.isCurPageUnderlined()){
//					AlertDialog.Builder builder = new AlertDialog.Builder(Reader.this);
//					globalAd=builder.create();
//					globalAd.setTitle(getResources().getString(R.string.iii_underline_dialog_title));
//					globalAd.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.iii_underline_dialog_action), new DialogInterface.OnClickListener(){
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							if(!isTurning){
//								if(renderer.getUnderlineCountOfBook(getDeliverId())<10){
//									//isUnderlineRemovalOpen=false;
//									//isUnderlineOpen=true;
//									startUnderlineMode();
//									globalAd.dismiss();
//								}else{
//									globalAd.dismiss();
//									showConfirmMessage(null,getResources().getString(R.string.iii_underline_overflow));
//								}
//							}
//						}
//						
//					});
//					globalAd.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.iii_underline_dialog_delete), new DialogInterface.OnClickListener(){
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							//isUnderlineRemovalOpen=true;
//							startUnderlineDeleteMode();
//							globalAd.dismiss();
//						}
//						
//					});
//					globalAd.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.iii_underline_dialog_cancel), new DialogInterface.OnClickListener(){
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							globalAd.dismiss();
//						}
//						
//					});
//					globalAd.show();
//				}else{
//					if(!isTurning){
//						if(renderer.getUnderlineCountOfBook(getDeliverId())<10){
//							//isUnderlineOpen=true;
//							startUnderlineMode();
//						}else{
//							showConfirmMessage(null,getResources().getString(R.string.iii_underline_overflow));
//						}
//					}
//				}
//				showMenu(true);
//				*/
//      	  	}
//        });
//        
//        ib_reader_setting_notes.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//      	  		if(!isTurning && isFunctionEnabled()){
//          	  		Intent intent = new Intent();
//          	  		if(renderer.isCurPageAnnotated()){
//          	  			intent.putExtra("id",renderer.getCurPageAnnotation());
//          	  			intent.putExtra("isAnnotated", true);
//          	  			intent.putExtra("span", renderer.getCurPageStartSpan());
//          	  			intent.putExtra("idx", renderer.getCurPageStartIdxInSpan());
//          	  			intent.putExtra("bookName", retain.bookTitle);
//          	  			intent.putExtra("chapName", retain.curSecFilename);
//          	  			intent.putExtra("epubPath", getDeliverId());
//              	  		intent.setClass(Reader.this, SettingNotes.class);		
//              	  		retain.isCallRenderOnRetain=false;
//              	  		startActivityForResult(intent,CALL_ANN);	
//          	  		}else{
//          	  			if(renderer.getAnnotationCountOfBook(getDeliverId())<10){
//          	  				intent.putExtra("id",renderer.getCurPageAnnotation());
//          	  				intent.putExtra("span", renderer.getCurPageStartSpan());
//          	  				intent.putExtra("idx", renderer.getCurPageStartIdxInSpan());
//          	  				intent.putExtra("bookName", retain.bookTitle);
//          	  				intent.putExtra("chapName", retain.curSecFilename);
//          	  				intent.putExtra("epubPath", getDeliverId());
//          	  				intent.putExtra("isAnnotated", false);
//          	  				intent.setClass(Reader.this, SettingNotes.class);	
//          	  				retain.isCallRenderOnRetain=false;
//          	  				startActivityForResult(intent,CALL_ANN);
//          	  			}else{
//          	  				showConfirmMessage(null,getResources().getString(R.string.iii_annotation_overflow));
//          	  			}
//          	  		}
//      	  		}    
//      	  	}
//        });
//        
//        ib_reader_search.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//				
//				Search();
//				// 2011/05/16 Jonathan remove codes to Search();
//				/*
//      	  		Intent intent = new Intent();
//      	  		intent.setClass(Reader.this, ReaderSearch.class);
//      	  		intent.putStringArrayListExtra("spine_list", retain.spineList);
//      	  		intent.putExtra("epub_path", epubPath);
//      	  		retain.isCallRenderOnRetain=false;
//      	  		startActivityForResult(intent,CALL_SEARCH);	
//      	  		*/
//        }});   
//        
//        ib_reader_setting.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//				isCallSetting=true;
//      	  		Intent intent = new Intent();
//      	  		intent.putExtra("deliverId", getDeliverId());
//      	  		intent.setClass(Reader.this, ReaderSetting.class);
//      	  		startActivity(intent);
//      	  	}
//        });    
//        ib_reader_setting_bookmark_del.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//				if(!isTurning && isFunctionEnabled()){
//					renderer.deleteCurPageBookmark(retain.bookTitle, retain.curSecFilename, getDeliverId());
//					setBookmarkAndNoteFlag();
//				}
//      	  	}
//        });    
//        ib_reader_setting_notes_del.setOnClickListener(new ImageButton.OnClickListener(){
//			public void onClick(View v){
//				if(!isTurning && isFunctionEnabled() ){
//					renderer.deleteCurPageAnnotation(retain.bookTitle, retain.curSecFilename, getDeliverId());			
//					setBookmarkAndNoteFlag();
//				}
//      	  	}
//        });    
        
    }
	
	private void Highlight(){
		
		if(isTurning || !isFunctionEnabled() || !renderer.textExistsOnCurPage()){
			thandler.sendMessage(thandler.obtainMessage(ACTION_SHOW_TOAST,getResources().getString(R.string.iii_page_no_text)));
		}else if(renderer.isCurPageUnderlined()){
			AlertDialog.Builder builder = new AlertDialog.Builder(Reader.this);
			globalAd=builder.create();
			globalAd.setTitle(getResources().getString(R.string.iii_underline_dialog_title));
			globalAd.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.iii_underline_dialog_action), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(!isTurning){
						if(renderer.getUnderlineCountOfBook(getDeliverId())<10){
							//isUnderlineRemovalOpen=false;
							//isUnderlineOpen=true;
							startUnderlineMode();
							globalAd.dismiss();
						}else{
							globalAd.dismiss();
							showConfirmMessage(null,getResources().getString(R.string.iii_underline_overflow));
						}
					}
				}
				
			});
			globalAd.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.iii_underline_dialog_delete), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//isUnderlineRemovalOpen=true;
					startUnderlineDeleteMode();
					globalAd.dismiss();
				}
				
			});
			globalAd.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.iii_underline_dialog_cancel), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					globalAd.dismiss();
				}
				
			});
			globalAd.show();
		}else{
			if(!isTurning){
				if(renderer.getUnderlineCountOfBook(getDeliverId())<10){
					//isUnderlineOpen=true;
					startUnderlineMode();
				}else{
					showConfirmMessage(null,getResources().getString(R.string.iii_underline_overflow));
				}
			}
		}
		showMenu(true);
	}
	
	private void Search(){
		Intent intent = new Intent();
	  		intent.setClass(Reader.this, ReaderSearch.class);
	  		intent.putStringArrayListExtra("spine_list", retain.spineList);
	  		intent.putExtra("epub_path", epubPath);
	  		retain.isCallRenderOnRetain=false;
	  		startActivityForResult(intent,CALL_SEARCH);	
	}

	@Override
	public void onViewSizeChanged() {
		if(renderer!=null){
			setScreenInfo();
			ev.ResetBitmap();
			renderer.resetScreenSize(screenHeight, screenWidth);
			renderer.reload();
		}
	}
	/**
	 * 110511
	 * add for set font size when multitouch to zoom epub pages
	 */
	@Override
	public void setFontSize(int aSize){
		Log.d("TWM","set font size:"+aSize);
		//boundary
		if(aSize<0)
			aSize = 0;
		if(aSize>11)
			aSize = 11;
		if(fontSizeIdx != aSize){
			settings.edit().putInt(getDeliverId()+"reader_setting_font_size_value", aSize).commit();
			renderer.changeFontSize(aSize);
			renderer.reload();
			fontSizeIdx = aSize;
		}
		if(iSeekBar_fontsize != null)
			iSeekBar_fontsize.setProgress(fontSizeIdx);
	}
	@Override
	public int getFontSize() {
		if(renderer!=null)
			return renderer.getFontSize();
		else
			return -1;
	}
	
	private boolean bContextOpen = false;
    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v instanceof EpubView) {
			if(getPercentageInBook()== 100)return;
			boolean bLongPressed = ((EpubView) v).isLongPressed();
			if (bLongPressed && !isUnderlineOpen && !isUnderlineRemovalOpen) {			
				if(isTouch){
					showMenu(isTouch);
				}
				bContextOpen = true;
				ev.setParentContextMenuOpen(bContextOpen);

				final ActionItem partitionLineAction = new ActionItem();
				partitionLineAction.setIcon(getResources().getDrawable(R.drawable.quickaction_slider_grip_left));
				
				final ActionItem addAction = new ActionItem();
				addAction.setIcon(getResources().getDrawable(R.drawable.ic_add));

				final ActionItem accAction = new ActionItem();
				accAction.setIcon(getResources().getDrawable(R.drawable.ic_accept));

				final ActionItem annotAction = new ActionItem();
				if (renderer.isCurPageAnnotated()) {
					annotAction.setIcon(getResources().getDrawable(R.drawable.ic_annot_del));
				} else {
					annotAction.setIcon(getResources().getDrawable(R.drawable.ic_annot));
				}

				final QuickAction mQuickAction = new QuickAction(v);

				addAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mQuickAction.dismiss();
						Highlight();
					}
				});

				accAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mQuickAction.dismiss();
						Search();
					}
				});
				
				annotAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mQuickAction.dismiss();
						Intent intent = new Intent();
	          	  		if(renderer.isCurPageAnnotated()){
	          	  			
	        				if(!isTurning && isFunctionEnabled() ){
		    					renderer.deleteCurPageAnnotation(retain.bookTitle, retain.curSecFilename, getDeliverId());			
		    					setBookmarkAndNoteFlag();
		    					renderer.reload();
	        				}
	
	          	  		}else{
	          	  			if(renderer.getAnnotationCountOfBook(getDeliverId())<10){
	          	  				intent.putExtra("id",renderer.getCurPageAnnotation());
	          	  				intent.putExtra("span", renderer.getCurPageStartSpan());
	          	  				intent.putExtra("idx", renderer.getCurPageStartIdxInSpan());
	          	  				intent.putExtra("bookName", retain.bookTitle);
	          	  				intent.putExtra("chapName", retain.curSecFilename);
	          	  				intent.putExtra("epubPath", getDeliverId());
	          	  				intent.putExtra("isAnnotated", false);
	          	  				intent.setClass(Reader.this, SettingNotes.class);	
	          	  				retain.isCallRenderOnRetain=false;
	          	  				startActivityForResult(intent,CALL_ANN);
	          	  			}else{
	          	  				showConfirmMessage(null,getResources().getString(R.string.iii_annotation_overflow));
	          	  			}
	          	  		}
					}
				});
				
				mQuickAction.setOnDismissListener(new OnDismissListener(){

					@Override
					public void onDismiss() {
						// TODO Auto-generated method stub
						bContextOpen = false;
						ev.setParentContextMenuOpen(bContextOpen);
					}
					
				});
				
				mQuickAction.addActionItem(addAction);
				mQuickAction.addActionItem(partitionLineAction);
				mQuickAction.addActionItem(annotAction);
				mQuickAction.addActionItem(partitionLineAction);		
				mQuickAction.addActionItem(accAction);
				
				mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
				mQuickAction.showAtOffset(((EpubView) v).getPressedPoint().y);
			}
		}
	}
	
	public Drawable getCtrlColorDrawable(String name){
		String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
		int i = 0;
		do{
			if(i>=temp.length||temp[i].equals(name)){
				break;
			}
			i++;
		}while(true);
		
		switch(i){
			case 0:
				return getResources().getDrawable(R.drawable.gsi_black_1);
			case 1:
				return getResources().getDrawable(R.drawable.gsi_black_2);
			case 2:
				return getResources().getDrawable(R.drawable.gsi_black_3);
			case 3:
				return getResources().getDrawable(R.drawable.gsi_while_1);
			case 4:
				return getResources().getDrawable(R.drawable.gsi_while_2);
			case 5:
				return getResources().getDrawable(R.drawable.gsi_while_3);
			case 6:
				return getResources().getDrawable(R.drawable.gsi_brown_1);
			case 7:
				return getResources().getDrawable(R.drawable.gsi_brown_2);
			case 8:
				return getResources().getDrawable(R.drawable.gsi_brown_3);
			case 9:
				return getResources().getDrawable(R.drawable.gsi_blue_1);
			case 10:
				return getResources().getDrawable(R.drawable.gsi_blue_2);
			case 11:
				return getResources().getDrawable(R.drawable.gsi_blue_3);
			case 12:
				return getResources().getDrawable(R.drawable.gsi_green_1);
			case 13:
				return getResources().getDrawable(R.drawable.gsi_green_2);
			case 14:
				return getResources().getDrawable(R.drawable.gsi_green_3);			
			case 15:
				return getResources().getDrawable(R.drawable.gsi_orange_2);
			case 16:
				return getResources().getDrawable(R.drawable.gsi_orange_3);
			case 17:
				return getResources().getDrawable(R.drawable.gsi_yellow_1);
			case 18:
				return getResources().getDrawable(R.drawable.gsi_red_1);
			case 19:
				return getResources().getDrawable(R.drawable.gsi_red_2);
			case 20:
				return getResources().getDrawable(R.drawable.gsi_red_3);
			case 21:
				return getResources().getDrawable(R.drawable.gsi_purple_1);
			case 22:
				return getResources().getDrawable(R.drawable.gsi_purple_2);
			case 23:
				return getResources().getDrawable(R.drawable.gsi_purple_3);
			default:
				return getResources().getDrawable(R.drawable.gsi_black_1);	
		}	    
	}
	
	/**
	 * 取得背景
	 * @param name 背景名稱
	 * @return 背景
	 */
	public Drawable getBackground(String name){
		String[] temp = getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
		int i = 0;
		do{
			if(i>=temp.length||temp[i].equals(name)){
				break;
			}
			i++;
		}while(true);
		
		switch(i){
			case 0:
				return getResources().getDrawable(R.drawable.bg_01_s);
			case 1:
				return getResources().getDrawable(R.drawable.bg_02_s);
			case 2:
				return getResources().getDrawable(R.drawable.bg_03_s);
			default:
				return getResources().getDrawable(R.drawable.bg_01_s);
		}	
	}
	
	private int getFlipResID(String name){
		String[] temp = getResources().getStringArray(R.array.iii_reader_setting_flip_value);
		int i = 0;
		do{
			if(i>=temp.length||temp[i].equals(name)){
				break;
			}
			i++;
		}while(true);
		
		switch(i){
			case 0:
				return R.drawable.gsi_button34_btn;
			case 1:
				return R.drawable.gsi_button35_btn;
			case 2:
				return R.drawable.gsi_button33_btn;				
			default:
				return R.drawable.gsi_button35_btn;	
		}	
	}
	
	private void changeVertical(){
		if(isVertical()){
			aChineseTypeButton.setBackgroundResource(R.drawable.gsi_button31_btn);
		}else{			
			aChineseTypeButton.setBackgroundResource(R.drawable.gsi_button32_btn);
		}
		settings.edit().putBoolean(getDeliverId()+"reader_setting_chinese_type_value", !isVertical()).commit();
		
		fontSizeIdx = settings.getInt(getDeliverId()+"reader_setting_font_size_value", DEFAULT_FONTSIZE_IDX);
		int span=renderer.getCurPageStartSpan();
		int idx=renderer.getCurPageStartIdxInSpan();
        if(isVertical()){
        	renderer = new VerticalRenderer2(ev,screenHeight,screenWidth,this,getApplicationContext(),getDeliverId(),fontSizeIdx);
        }else{
        	renderer = new MacroRenderer(ev,screenHeight,screenWidth,this,getApplicationContext(),getDeliverId(),fontSizeIdx);
        }
        renderer.setTurningMethod(getter.getTurningMethod(getDeliverId()));
        unzipAndLoadChapter(ACTION_LOAD_CHAPTER_BY_SPAN,span,idx,threadIdx);
	}


	@Override
	public void disableFirstPage() {
		// TODO Auto-generated method stub
//		settings.edit().putBoolean("show_manual", true).commit();
//		showManual();
		this.showFirstPage(false);
	}
}
