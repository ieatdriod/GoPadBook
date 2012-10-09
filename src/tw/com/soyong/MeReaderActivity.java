package tw.com.soyong;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tw.com.soyong.mebook.Mebook;
import tw.com.soyong.mebook.MebookData;
import tw.com.soyong.mebook.MebookException;
import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.mebook.MebookToken;
import tw.com.soyong.mebook.SyChapter;
import tw.com.soyong.mebook.SyItem;
import tw.com.soyong.mebook.SySentence;
import tw.com.soyong.mebook.SyTime;
import tw.com.soyong.mebook.SyVocInfo;
import tw.com.soyong.mebook.TWMMetaData;
import tw.com.soyong.utility.ImageOnlyButton;
import tw.com.soyong.utility.ImageTextView;
import tw.com.soyong.utility.Native;
import tw.com.soyong.utility.Recorder;
import tw.com.soyong.utility.SettingPreference;
import tw.com.soyong.utility.SyBookmark;
import tw.com.soyong.utility.SySeekBar;
import tw.com.soyong.utility.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.Reader;
import com.taiwanmobile.myBook_PAD.RealBookcase;

/**
 * Viewer 閱讀功能主畫面
 * @author Victor
 *
 */
public class MeReaderActivity extends Activity implements Html.ImageGetter ,OnGestureListener,OnTouchListener {
	
	private static boolean DEBUG = false ;
	private static final String TAG = "MeReaderActivity";

	private static final int CHAPTER_LIST_REQUEST = 0;
	private static final int EXTEND_REQUEST = 2;
	private static final int POSTSCRIPT_REQUEST = 3;
	private static final int SEARCH_REQUEST = 5;
	private static final int SETTING_REQUEST = 6;
	private static final int PROMO_REQUEST = 7;

	static final String CHP_MODE = "is_chp_mode";
	static final String CHP_INDEX = "chp_index";
	static final String PS_DATA = "ps_index";
	
	/**
	 *  取得current index的key值
	 */
	public static final String CUR_INDEX = "cur_index";
	static final String SENT_DATA = "sent_data";
	static final String MODE_INDEX = "mode_index";
	static final String BK_ID = "bk_id";
	static final String PAGE_FLAG = "pageFlg";	

	int mRangeMode = 0 ;		// 0:all , 1:ab , 2:bookmark 	
	
	String mStrBookVoc;
	
	static SettingPreference mSettingPref ;
	SettingPreference mSettingBak ;

	int mCurSentence;
	// when user change seek bar from panel, set variable to true
	boolean mIsUserSeek = false;

	private IPlayerService mPlayerService = null;

	boolean mIsShowOrg = true;
	boolean mIsShowTrl = true;
	boolean mIsPicMode = false;
	boolean mIsFullScreen = false;
	boolean mIsRecordMode = false;
	boolean mIsTransDisable = false ;

	int mGotoIndex;
	int mSeekTime; // for time notify issue, we need double check notify same as
	// our request!!!!
	boolean mForceChange = false;
	boolean mIsRepeatCurrent = false;
	boolean mIsPlaying = false ;

	int mPlayCount;
	boolean mPassGap = false; // after seek, pass gap one time

	int mIndexA = -1;
	int mIndexB = -1;
	int mDuration;
	int mEosTime ;

	static final int REC_STOP = 20;
	static final int REC_PLAYING_CONTENT = 21; // play mp3
	static final int REC_PLAYING_RECFILE = 22; // play rec file
	static final int REC_RECORDING = 23;	   // in recording file
	int mRecAct = REC_STOP;
	// for record mode
	boolean mNormalRepeatCur;

	boolean mAutoPause = false;
	
	/**
	 *  Bookmark 物件，記錄當前書本的書籤資料
	 */
	public static SyBookmark mBookmark;
	boolean mIsPlayingFlag = false;
	
	boolean mIsPhonetic = false ;		// for JP Basic content, update this variable in the formatOrg()
	boolean mIsJpBasic = false;
	//** turn the page
	private boolean mInitReaderDone = false;
	private  GestureDetector mGestureDetector;   

	final GradientDrawable mlvSelector = new GradientDrawable(
			GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
					Color.rgb(84, 145, 192), Color.rgb(2, 72, 131) });

	final GradientDrawable mPanelBackground = new GradientDrawable(
			GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
					Color.rgb(0, 0, 0), Color.rgb(90, 90, 90) });
	
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			mPlayerService = IPlayerService.Stub.asInterface(service);

			if ( DEBUG ) Log.e(TAG, "onServiceConnected");
			
			try {
				mPlayerService.subscribe(mCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			try {
				mPlayerService.unSubscribe(mCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mPlayerService = null;
			if ( DEBUG ) Log.e(TAG, "onServiceDisconnected");
		}
	};
	
	
	/**
	 *  Soyong mp3 player service
	 */
	public static LocalService mSyPlayerSvc;
    private ServiceConnection mConn = new ServiceConnection() {  
        public void onServiceConnected(ComponentName className, IBinder service) {           
            mSyPlayerSvc = ((LocalService.LocalBinder)service).getService();  

            if (DEBUG){
	            Toast.makeText(MeReaderActivity.this.getApplicationContext(), "mSyPlayerSvc connected",  
	                    Toast.LENGTH_SHORT).show();  
            }
            
            if ( DEBUG ) Log.e(TAG , "mSyPlayerSvc connected" );
            
//            int count = 10;
//            while( count > 0  && false == mSyPlayerSvc.isInitial()){
//            	
//            	if ( DEBUG ) Log.e(TAG , "mSyPlayerSvc not initial ???" );
//            	
//            	try {
//					Thread.sleep(20);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				count -- ;
//            }

            mInitReaderDone = false ;
            mHandler.postDelayed( new Runnable(){

				@Override
				public void run() {
		            initReader();
		    		// from now on, we could play mp3
		    		mPlayBtn.setEnabled(true);
		    		mInitReaderDone = true ;
				}
            	
            }, 300);

        }  
  
        public void onServiceDisconnected(ComponentName className) {  
            // unexpectedly disconnected,we should never see this happen.  
            mSyPlayerSvc = null;  
            
            MebookHelper.clear();
            if ( DEBUG ) Log.e(TAG , "mSyPlayerSvc disconnected" );
        }  
    }; 	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// bind mediaplayer service 
		bindService(new Intent(IPlayerService.class.getName()), mConnection,
				Context.BIND_AUTO_CREATE);		

		/* Make the system know we want to control the volume on the MUSIC-STREAM with the Hardware-Buttons. */ 
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		//setContentView(R.layout.mereader);
		setContentView(R.layout.gsi_mereader);
		//** turn the page
		mGestureDetector =  new  GestureDetector( this ); 
		
		// load player setting
		if ( null == mSettingPref){
			mSettingPref = new SettingPreference(this);
			mSettingPref.load();
		}
		
		// mSettingPref need long before setupView()
		setupView();

		
		bindService(new Intent(this, LocalService.class), mConn ,  Context.BIND_AUTO_CREATE );


		if (DEBUG) Log.e(TAG , "end ---");		//help to measure start up time
		
		mSenTime = new SySentence();
		mSenTime.mBeginTime = new SyTime<Integer>(0);
		
		mIsJpBasic = false;
		
        TWMMetaData xml = MebookHelper.mMeta;
        String orgLang = "en_US";
        if ( null != xml){
        	
        	orgLang = xml.getOrgLang();
        	if ( null != orgLang && orgLang.length()>0 ){
        		orgLang = orgLang.trim();
        	}
        } else {
        	Log.e(TAG, "xml == null");
        }
		
		//if ( 0 == MebookHelper.mHeaderInfo.mBookID.compareTo(MebookInfo.JP_BASIC)){
        MebookHelper.mIsJpBook = false ;
        if ( orgLang.startsWith("jp_")){
        	MebookHelper.mIsJpBook = true ;
			mIsJpBasic = true ;
			
			final Button orgBtn = (Button) findViewById(R.id.gsimedia_btn_orgBtn);
			//orgBtn.setBackgroundResource(R.drawable.key_otext_ja_btn);
		}
        //** turn the page
        
		// wait SyPlayerSVC connection, then do other initReader();
	}
	
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if ( mInitReaderDone && false == shutdownRequested){
			return super.dispatchKeyEvent(event);
		}
		
		return true ;
	}



	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if ( mInitReaderDone && false == shutdownRequested ){
			return super.dispatchTouchEvent(ev);
		}
		return true ;
	}



	private void initReader() {
		// initial control
		mEosTime = MebookHelper.mContent.getEosTime();
		if ( DEBUG ) Log.e(TAG , "mEosTime:"+mEosTime);
		
		int duration = mEosTime ;
		mDuration = duration;
		mSyPlayerSvc.setEosTime(mEosTime);

		
		// Initial seekbar
		mSeekBar.setMax(duration);
		
		// Initial duration text
		if ( DEBUG ) Log.d(TAG, "duration:" + duration);
		String time = Util.formatTimeString(duration);
		mtvDuration.setText(time);
		
		SyBookmark bm = new SyBookmark(this , MebookHelper.mSentenceArr.length);
		bm.load(MebookHelper.mDeliverID+"_"+MebookHelper.mTrackIndex);
		mBookmark = bm;			
		//setupSeekbarBookmark(bm);		
	
		mCurSentence = -1; 	// set as -1, let it update in the
							// get last sentence
		SharedPreferences pref = getSharedPreferences(MebookHelper.mDeliverID, Context.MODE_PRIVATE);
		final int curSentence = pref.getInt("Title" + MebookHelper.mTrackIndex, 0);
		
		gotoSentent(curSentence);
		
	}
	
	View mTopPanel;
	TextView iTitleView = null;
//	ImageButton mOrgBtn;
//	ImageButton mTrlBtn;
	Button mPicBtn;
	private boolean mForceUpdateContent = false ;
	private void setupTopView() {

		mTopPanel = findViewById(R.id.RelativeLayout_Title);

		iTitleView = (TextView) findViewById(R.id.gsimedia_title);
		iTitleView.requestFocus();
		iTitleView.setText(MebookHelper.mBookTitle);
		
		Button backBtn = (Button) findViewById(R.id.gsimedia_btn_title_left);
		backBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
				if ( 0 == exit() ){
					//finish();
				}
			}
		});
		
//		final Button orgBtn = (Button) topPanel.findViewById(R.id.gsimedia_btn_orgBtn);
//		orgBtn.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//			
//						mIsShowOrg = !mIsShowOrg;
//				int resid = mIsShowOrg? R.drawable.ian_button01 : R.drawable.ian_button01u;
//				if ( true == mIsJpBasic ){
//					resid = mIsShowOrg? R.drawable.ian_button02 : R.drawable.ian_button02u;
//				}
//						
//				orgBtn.setBackgroundResource(resid);
//						mForceUpdateContent = true ;
//						updateContentBySentence(mCurSentence);
//						mForceUpdateContent = false ;
//
//			}
//		});
		
//		final Button trlBtn = (Button) topPanel.findViewById(R.id.gsimedia_btn_trlBtn);
//		trlBtn.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//
//						mIsShowTrl = !mIsShowTrl;
//				int resid = mIsShowTrl? R.drawable.ian_button03 : R.drawable.ian_button03u;
//						
//				trlBtn.setBackgroundResource(resid);
//						mForceUpdateContent = true ;
//						updateContentBySentence(mCurSentence);
//						mForceUpdateContent = false ;
//					}
//		});
		
		
//		final Button picBtn = (Button) topPanel.findViewById(R.id.gsimedia_btn_picBtn);
//		picBtn.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				onPicDisplay();
//			}
//		});
//		mPicBtn = picBtn;
		
		
//		try {
//			SyItem item;
//			item = MebookHelper.mBookData.getData("", MebookData.IMG_COUNT);
//			if (item.mItem <= 2){
//				picBtn.setVisibility(View.INVISIBLE);
//			}
//		} catch (MebookException e) {
//			e.printStackTrace();
//		}
		
		
		
		Button chpBtn = (Button) findViewById(R.id.gsimedia_btn_title_right);
		chpBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				final SySentence sentence = MebookHelper.mSentenceArr[mCurSentence] ;
				Intent it = new Intent(MeReaderActivity.this, ChapterListActivity.class);
				it.putExtra(CHP_INDEX, sentence.mChapterIndex);
				it.putExtra(CUR_INDEX, mCurSentence);
				startActivityForResult(it, CHAPTER_LIST_REQUEST);
				
				autoPause();
			}
		});
	}
	
	private final void setEditTextAttr(final EditText et, final Typeface font,
			final float fontSize) {
		et.setTypeface(font);
		et.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize);
		et.setTextColor(Color.rgb(56, 115, 33));
		et.setOnTouchListener(mTouchListener);
		et.setLongClickable(false);
		
		et.setLineSpacing(0.0f, 1.4f);  
	}
	
	private boolean checkIsOldVersion(){
		boolean isOldVersion = false;		
		int firstPos = Build.VERSION.RELEASE.indexOf(".");		
		String[]version = new String[3];
		version[0] = Build.VERSION.RELEASE.substring(0, firstPos);
		int secondPos = Build.VERSION.RELEASE.indexOf(".",firstPos+1);
		
		if(secondPos > 0){
			version[1] = Build.VERSION.RELEASE.substring(firstPos+1,secondPos);
		}else{
			version[1] = Build.VERSION.RELEASE.substring(firstPos+1,Build.VERSION.RELEASE.length());
		}
		
		if(Integer.parseInt(version[0]) < 2){
			isOldVersion = true;
		}else if(Integer.parseInt(version[0]) == 2){
			if(Integer.parseInt(version[1]) <= 2){
				isOldVersion = true;
			}
		}
		
		return isOldVersion;
	}
	private final void setTextViewAttr(final TextView Tv, final Typeface font,
			final float fontSize) {
		Tv.setTypeface(font);
		Tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize);
		Tv.setTextColor(Color.rgb(56, 115, 33));
			
		boolean isOldVersion = checkIsOldVersion();	
		if(!isOldVersion){
            Tv.setOnTouchListener(mTextViewTouchListener);
		}else{
			Tv.setOnTouchListener(mTouchListener);
		}
		
		Tv.setLongClickable(false);		
		Tv.setLineSpacing(0.0f, 1.4f);  
	}
	//** turn the page
	RelativeLayout bookarea;
	TextView mtvChapterName;
	EditText metOrg;
	ImageTextView mivPic;
	RelativeLayout iLLay;
	TextView texv;
	ImageView mBookmarkIcon;
	ImageView mPosAIcon;
	ImageView mPosBIcon;
	ScrollView scrollcontent;
	ScrollView scrollcontent1;
	private void setupContentView(){

		iLLay = (RelativeLayout)this.findViewById(R.id.RelativeLayout_ImageText);
		iLLay.setOnTouchListener(mTouchListener);		
		iLLay.setVisibility(View.VISIBLE);
		scrollcontent=(ScrollView)this.findViewById(R.id.scrollView_content);
		scrollcontent1 =(ScrollView)this.findViewById(R.id.scrollView_Text);
		// set font for phonetic symbol
		int firstPos = Build.VERSION.RELEASE.indexOf(".");		
		String[]version = new String[3];
		version[0] = Build.VERSION.RELEASE.substring(0, firstPos);
		int secondPos = Build.VERSION.RELEASE.indexOf(".",firstPos+1);
		if(secondPos > 0){
			version[1] = Build.VERSION.RELEASE.substring(firstPos+1,secondPos);
		}else{
			version[1] = Build.VERSION.RELEASE.substring(firstPos+1,Build.VERSION.RELEASE.length());
		}
		boolean isOldVersion = false;
		if(Integer.parseInt(version[0]) < 2){
			isOldVersion = true;
		}else if(Integer.parseInt(version[0]) == 2){
			if(Integer.parseInt(version[1]) <= 2){
				isOldVersion = true;
			}
		}
		
		if(!isOldVersion){
			scrollcontent.setOnTouchListener(mTextViewTouchListener);
			scrollcontent1.setOnTouchListener(mTextViewTouchListener);
		}else{
			bookarea=(RelativeLayout)this.findViewById(R.id.relativeLayout_bookarea) ;
			bookarea.setOnTouchListener(this);
		}
		
		final Typeface font = Typeface.createFromAsset(getAssets(),
				"fonts/syphone.ttf");
		
		final float fontSize = mSettingPref.getFontSize(this.getApplicationContext());

		EditText et = (EditText) findViewById(R.id.mereader_org_area);
		metOrg = et;
		setEditTextAttr(et, font, fontSize);

		// get chapter name control
		mtvChapterName = (TextView) findViewById(R.id.mereader_chapter_name);

		// get content display control
		ImageTextView pic = (ImageTextView) findViewById(R.id.mereader_pic_area);
		mivPic = pic;
		pic.setDisplayOrg(mIsShowOrg);
		pic.setDisplayTrl(mIsShowTrl);
		pic.setOnTouchListener(mTouchListener);
		// set Pic text size
		
		pic.setFontSize(fontSize);
		
		texv = (TextView)findViewById(R.id.textView_Spanned);
		texv.setTextSize(fontSize);
		setTextViewAttr(texv, font, fontSize);
		texv.setMovementMethod(ScrollingMovementMethod.getInstance());
		// bookmark icon
		final ImageView bmIcon = (ImageView) findViewById(R.id.bookmarkIcon);
		mBookmarkIcon = bmIcon;
		
		final ImageView posAIcon = (ImageView) findViewById(R.id.posAIcon);
		mPosAIcon = posAIcon;		
		
		final ImageView posBIcon = (ImageView) findViewById(R.id.posBIcon);
		mPosBIcon = posBIcon;			
	}
	
	// for player
	SySeekBar mSeekBar;
	TextView mtvDuration;
	TextView mtvCurTime;
	View mPlayerPanel;
	View mRecordPanel;
	ImageView mRangeIcon;
	ImageView mRepIcon;
	ImageView mExtIcon;
	Button mExtBtn;
	ImageButton mPlayBtn;
	//Button mBmBtn;
	ImageView mBmBtn;
	Button mfontSizeBtn;
	
	ImageView mivPicInd;
	ImageView mivExtInd;
	ImageView mivBmInd;
	ImageView mivAppInd;

	// for record
	TextView mRecPrompt;
	ImageOnlyButton mRecPlayBtn;
	ImageOnlyButton mRecRecordingBtn;
	ImageOnlyButton mRecStopBtn;
	
	private RelativeLayout RelativeLayout_Ctls_Colors = null;
	
	private void setupBottomView() {
		
		// record panel
		// set panel background
		
		//View panel = findViewById(R.id.record_panel);
		mRecordPanel = findViewById(R.id.record_panel);
		mRecordPanel.setBackgroundDrawable(mPanelBackground);
		
		mRecPlayBtn = (ImageOnlyButton) findViewById(R.id.rec_play);
		mRecPlayBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onRecPlay();
			}
		});

		mRecRecordingBtn = (ImageOnlyButton) findViewById(R.id.rec_recording);
		mRecRecordingBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onRecRecording();
			}
		});
		

		mRecStopBtn = (ImageOnlyButton) findViewById(R.id.rec_stop);
		mRecStopBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onRecStop();
			}
		});

		
		TextView prompt = (TextView) findViewById(R.id.record_prompt);
		mRecPrompt = prompt;

		
		//mPlayerPanel = panel;
		mPlayerPanel = findViewById(R.id.RelativeLayout_Ctls);
		
		//panel.setBackgroundDrawable(mPanelBackground);

		// seekbar panel
		View panel = findViewById(R.id.ProgressGroup_SBar_relativelayout);
		
		// get player panel control
		mSeekBar = (SySeekBar) panel.findViewById(R.id.seekbar);
		//mSeekBar = new SySeekBar(this);
		mSeekBar.setOnSeekBarChangeListener(mSeekBarChange);

		mtvDuration = (TextView) panel.findViewById(R.id.totoal_time);
		//mtvDuration = new TextView(this);
		
		mtvCurTime = (TextView) panel.findViewById(R.id.cur_time);
		//mtvCurTime = new TextView(this);
		
		// play range indicate
		mRangeIcon = (ImageView) panel.findViewById(R.id.rangeIcon);
		//mRangeIcon = new ImageView(this);
		
		// repeat indicate
		mRepIcon = (ImageView) panel.findViewById(R.id.repeatIcon);
		//mRepIcon = new ImageView(this);
		
		mExtIcon = (ImageView) panel.findViewById(R.id.extIcon);
		//mExtIcon = new ImageView(this);
		
		final ImageButton playBtn = (ImageButton)findViewById(R.id.playBtn); 
		playBtn.setEnabled(false);							// after syplaysvc connect, this button will enalbe
		mPlayBtn = playBtn;
		playBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onPlayPause();
			}
		} );

		// initial rage mode in play all
		mRangeMode = 0 ;

		// funcion panel
		
		final Button helpButton = (Button) findViewById(R.id.gsimedia_btn_help);
		helpButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				hideNonEditView();
				
				Intent it = new Intent(MeReaderActivity.this, OperatorHelpActivity.class);
				startActivity(it);	
			}
		});
		
		
		final Button repBtn  = (Button) findViewById(R.id.gsimedia_btn_repBtn);

		int visiable = mIsRepeatCurrent? View.VISIBLE:View.INVISIBLE ;
		mRepIcon.setVisibility(visiable);
		
		repBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				mIsRepeatCurrent = !mIsRepeatCurrent;
				int visiable = mIsRepeatCurrent? View.VISIBLE:View.INVISIBLE ;
				mRepIcon.setVisibility(visiable);
				
				int resid = mIsRepeatCurrent? R.drawable.key_repeaton_btn : R.drawable.key_repeatoff_btn;
				repBtn.setBackgroundResource(resid);
				
			}
		});
		
		mBmBtn = (ImageView) findViewById(R.id.gsimedia_img_bookmark_right);
		//mBmBtn = (Button) findViewById(R.id.gsimedia_btn_bmBtn);	
		mBmBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
				final boolean bBookmark = mBookmark.isMarked(mCurSentence);	
				onSetBookmark(!bBookmark);
			}
		});

		
		final Button setPosBtn = (Button) findViewById(R.id.gsimedia_btn_setPosBtn);
		setPosBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				int resid = R.drawable.key_seta_btn;
							
				if ( -1 == mIndexA ){
					resid = R.drawable.key_setb_btn ;
					
					mIndexA = mCurSentence;
					
					mPosAIcon.setVisibility(View.VISIBLE);					
					
				} else if ( -1 == mIndexB){
					resid = R.drawable.key_setoff_btn ;
					
					mIndexB = mCurSentence;
					
					// nomalize 
					if (mIndexA > mIndexB) {
						int tmp = mIndexA;
						mIndexA = mIndexB;
						mIndexB = tmp;
						
						mPosAIcon.setVisibility(View.VISIBLE);
					} else {
						mPosBIcon.setVisibility(View.VISIBLE);
					}
				} else {
					resid = R.drawable.key_seta_btn ;
					
					mIndexA = mIndexB = -1 ;
					
					mPosAIcon.setVisibility(View.INVISIBLE);
					mPosBIcon.setVisibility(View.INVISIBLE);
					
					//
					// When remove AB, need check current mode in AB or not!
					//

					if ( 1 == mRangeMode ){
						
						autoPause();
						mRangeMode = 0 ;
						//onPlayerStop();
						setRangeModeIcon(mRangeMode);
						autoReplay();
					}
				}
				
				setPosBtn.setBackgroundResource(resid);	
				
				setSeekbarAB();
			}
		});
		
		final ImageButton rangeBtn = (ImageButton) findViewById(R.id.playRangeBtn);
		rangeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				autoPause();
				
				
				int itemClick = 0 ;
				ArrayList<String> ranges = new ArrayList<String>();
				String str = getText(R.string.mereader_range_setting_item1).toString();
				ranges.add(str);
				if ( -1 != mIndexA && -1 != mIndexB ){
					str = getText(R.string.mereader_range_setting_item2).toString();
					ranges.add(str);
					
					if ( 1 == mRangeMode){
						itemClick = 1 ;
					}
				}
				
				if ( -1 != mBookmark.getFirst() ){
					str = getText(R.string.mereader_range_setting_item3).toString();
					ranges.add(str);
					
					if ( 2 == mRangeMode){
						if ( -1 != mIndexA && -1 != mIndexB ){
							itemClick = 2;
						} else {
							itemClick = 1 ;
						}
					}
				}
				
				int count = ranges.size();
				CharSequence [] items = new CharSequence[count];
				ranges.toArray(items);
				
				final AlertDialog dialog = new AlertDialog.Builder(MeReaderActivity.this)
								.setTitle(R.string.mereader_play_range_title)
								.setSingleChoiceItems (items, itemClick,  new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										
										if ( !(-1 != mIndexA && -1 != mIndexB) && 1 == which){
											which = 2 ;		// fore to play bookmark
										}
										
										setRangeModeIcon(which);
										mRangeMode = which ;
										dialog.cancel();
										
										int index = mCurSentence;
										// check seek position out of range
										if (isOutOfRange(index)) {
											index = getNextSentence();
											gotoSentent(index); // gotoSentent() will setup mForceChange										
										}

										
										autoReplay();
									}
								})
								.show();
				
				dialog.getListView().setSelector(mlvSelector);	
			}
		});

		final Button searchBtn = (Button) findViewById(R.id.gsimedia_btn_searchBtn);
		searchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSearching();
			}
		});

		final Button settingBtn = (Button) findViewById(R.id.gsimedia_btn_settingBtn);
		settingBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSetting();
			}
		});

		mfontSizeBtn = (Button) findViewById(R.id.gsimedia_btn_fontSizeBtn);
		mfontSizeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				SettingPreference  pref = mSettingPref;
//				
//				int fontType = pref.getFontType();
//				fontType = fontType >> 2 ;
//				fontType ++;
//				fontType = fontType % 4 ;
//				fontType = fontType << 2 ;
//				pref.setFontType(fontType);
//				
//				float fontSize = mSettingPref.getFontSize(getApplicationContext());
//				metOrg.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
//				mivPic.setFontSize(fontSize);
				
				if (bASizeEnabled) {
					enableAsizePanel(false);
				} else {
					enableAsizePanel(true);
				}
			}
		});

		final Button recBtn = (Button) findViewById(R.id.gsimedia_btn_recBtn);
		recBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onRecording();
			}
		});

		
		mExtBtn = (Button) findViewById(R.id.gsimedia_btn_extBtn);
		mExtBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onExtMode();
			}
		});
		
		
		final Button orgBtn = (Button) findViewById(R.id.gsimedia_btn_orgBtn);
		orgBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
			
				mIsShowOrg = !mIsShowOrg;
				int resid = mIsShowOrg ? R.drawable.key_orgtext_btn : R.drawable.key_show_orgtext_btn;
//				if (true == mIsJpBasic) {
//					resid = mIsShowOrg ? R.drawable.key_otext_ja_btn : R.drawable.key_otext_ja_u_btn;
//				}
//
				orgBtn.setBackgroundResource(resid);
				mForceUpdateContent = true;
				updateContentBySentence(mCurSentence);
				mForceUpdateContent = false;

			}
		});
		
		final Button trlBtn = (Button) findViewById(R.id.gsimedia_btn_trlBtn);
		trlBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				mIsShowTrl = !mIsShowTrl;
				int resid = mIsShowTrl ? R.drawable.key_trltext_btn : R.drawable.key_show_trltext_btn;
//
				trlBtn.setBackgroundResource(resid);
				mForceUpdateContent = true;
				updateContentBySentence(mCurSentence);
				mForceUpdateContent = false;
					}
		});
		
		mPicBtn = (Button) findViewById(R.id.gsimedia_btn_picBtn);
		mPicBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onPicDisplay();
			}
		});
		
		try {
			SyItem item;
			item = MebookHelper.mBookData.getData("", MebookData.IMG_COUNT);
			if (item.mItem <= 2){
				//mPicBtn.setVisibility(View.INVISIBLE);
				mPicBtn.setBackgroundResource(R.drawable.key_picturen);
			}
		} catch (MebookException e) {
			e.printStackTrace();
		}
		
		RelativeLayout_Ctls_Colors = (RelativeLayout) findViewById(R.id.ctls_color);
		RelativeLayout_Ctls_Colors.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}

		});
	}

	private final void setupView() {
		
		// top view: back/org/trl/pic/chp
		setupTopView();
		
		// content view: chapter name/org&trl/bookmarkIcon/picView
		setupContentView();
		
		// bottomView: player panel/function buttons/ record panel
		setupBottomView();
	}
	
	private void setRangeModeIcon(int which) {
		int resid = R.drawable.loop_all;
		switch(which){
		case 0:
			resid = R.drawable.loop_all;
			break;
		case 1:
			resid = R.drawable.loop_ab;
			break;
		case 2:
			resid = R.drawable.loop_bm;
			break;
		}
		
		mRangeIcon.setImageResource(resid);
	}	
	
	int mPlayEndTime = -1 ;
	private void onRecPlay() {
		if (true == mIsRecordMode) {
			if(DEBUG) Log.d(TAG ,"onRecPlay");
			onRecStop();

			//play content
			final SySentence sentence = MebookHelper.mSentenceArr[mCurSentence];
			mPlayEndTime =  sentence.getEndTime();
			mSyPlayerSvc.play(sentence.getBeginTime(), sentence.getEndTime());

			mRecPrompt.setText(R.string.record_message_3);
			mRecAct = REC_PLAYING_CONTENT;
			mHandler.sendMessageDelayed(mHandler.obtainMessage(PLAYER_TIME_IN_RECORD), TIME_QUERY_INTERVAL);
		}
	}

	private void onRecRecording() {
		if (true == mIsRecordMode) {
			if(DEBUG) Log.d(TAG ,"onRecRecording");

			// if cur Act == REC_PLAY
			onRecStop();
			
			// prevent reentry issue
			if ( REC_RECORDING == mRecAct){
				return ;
			}

			mRecCount = 30; // 30 sec count down
			setRecPrompt(mRecCount);
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(REC_COUNT_MSG),
					MSG_INTERVAL);
			Recorder.start();
			mRecAct = REC_RECORDING;
		}
	}

	private void onRecStop() {
		if (true == mIsRecordMode) {

			// player for recording file
			try {
				if ( null != mPlayerService ){
					mPlayerService.stop();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			// player for content mp3
			mSyPlayerSvc.stop();

			Recorder.stop();
			mPlayEndTime = -1 ;
			mRecCount = 0;
			mHandler.removeMessages(REC_COUNT_MSG);
			mRecPrompt.setText(R.string.record_message_1);
			mRecAct = REC_STOP;
			
			mHandler.removeMessages(PLAYER_TIME_CHNAGED);
			mRecPlayBtn.setEnabled(true);
			mRecRecordingBtn.setEnabled(true);			
		}
	}

	
	private void onPlayPause() {
		mIsPlaying = !mIsPlaying;
		int resid = mIsPlaying? R.drawable.key_pause_btn : R.drawable.key_play_btn;
		mPlayBtn.setBackgroundResource(resid);
		
		if ( mIsPlaying ){
			mSyPlayerSvc.play();
			mHandler.sendEmptyMessage(PLAYER_TIME_QUERY);
		} else {
			mSyPlayerSvc.pause();
			mHandler.removeMessages(PLAYER_TIME_QUERY);
		}
	}	
	
	private void ResotreTextView() {
//		metOrg.setVisibility(View.VISIBLE);
//		scrollcontent1.setVisibility(View.VISIBLE);
		RelativeLayout.LayoutParams texvlayout=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		texvlayout.addRule(RelativeLayout.BELOW, mtvChapterName.getId());
		texv.setLayoutParams(texvlayout);
		texv.setVisibility(View.VISIBLE);
	}

	private void onHidePic() {
//		mivPic.setVisibility(View.GONE);
//		iLLay.setVisibility(View.GONE);
//		scrollcontent.setVisibility(View.GONE);
		imv.setVisibility(View.GONE);
		
	}

	private void onDisplayPic() {
		mivPic.setDisplayOrg(mIsShowOrg);
		mivPic.setDisplayTrl(mIsShowTrl);
//		mivPic.setVisibility(View.VISIBLE);
//		iLLay.setVisibility(View.VISIBLE);
		RelativeLayout.LayoutParams texvlayout=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		texvlayout.addRule(RelativeLayout.BELOW, imv.getId());
		texv.setLayoutParams(texvlayout);
		texv.setVisibility(View.VISIBLE);
		imv.setVisibility(View.VISIBLE);
		scrollcontent.setVisibility(View.VISIBLE);
	}

	protected void onHideText() {

		mIsShowOrg = false;
		mIsShowTrl = false;
		if (false == mIsPicMode) {
			metOrg.setVisibility(View.GONE);
		} else {
			mivPic.setDisplayOrg(false);
			mivPic.setDisplayTrl(false);
		}
	}	

	private void onPicDisplay() {

		final SySentence[] arr = MebookHelper.mSentenceArr;
		final int index = mCurSentence;
		final SySentence sentence = arr[index];
		final boolean isValidPic =  sentence.isValidPic();

		if ( false == mIsPicMode && false == isValidPic) {
			return;
		}

		boolean bPic = mIsPicMode;

		bPic = bPic == true ? false : true;

		mIsPicMode = bPic;
		updateContentBySentence(index);
		onFullScreen();
		if (true == bPic) {
			
			// enter full screen mode
			
			onHideTextView();
			onDisplayPic();
		} else {
			onHidePic();
			ResotreTextView();
		}
//		updateContentBySentence(index);
	}

	private void onSetBookmark(boolean bSet) {
		if (null == mBookmark || mCurSentence < 0 || null == mSeekBar) {
			return;
		}

		if ( true == bSet ){
			mBookmark.set(mCurSentence);
		} else {
			mBookmark.clear(mCurSentence);
		}
		setupSeekbarBookmark(mBookmark);
		
		int visable = bSet? View.VISIBLE : View.INVISIBLE;
		mBookmarkIcon.setVisibility(visable);
		
//		int resid = bSet ? R.drawable.key_deletebm_btn : R.drawable.key_bookmark_btn;
//		
//		mBmBtn.setBackgroundResource(resid);				

	}

	// Enter / Leave Recording mode
	private void onRecording() {
		
		boolean bRec = mIsRecordMode;

		bRec = !bRec;

		Animation animShow = AnimationUtils.loadAnimation(this,
				R.anim.popup_show);
		Animation animHide = AnimationUtils.loadAnimation(this,
				R.anim.popup_hide);

		if (true == bRec) {
			autoPause();
			
			switchToRecPanel(animShow, animHide);

			// save normal play states
			// save play setting
			try {
				mSettingBak = (SettingPreference) mSettingPref.clone();
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
			
			mNormalRepeatCur = mIsRepeatCurrent;
			// end save normal play states

			// Enter record mode force repeat current ON!
			mSettingPref.setAutoRepeat(false);
			mSettingPref.setRepeatCount(0);
			mIsRepeatCurrent = true;
			
			try {
				mPlayerService.setEosTime(0);
				mPlayerService.setAutoRepeat(false);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if ( DEBUG ) Log.d(TAG, "enter rec mode :" + mCurSentence);
			
			AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			am.setStreamVolume (AudioManager.STREAM_VOICE_CALL , 18 , AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE );
		} else {
			switchToPlayPanel(animShow, animHide);

			// restore play setting
			mSettingPref.setRepeatCount(mSettingBak.getRepeatCount());
			mSettingPref.setAutoRepeat(mSettingBak.isAutoRepeat());
			mIsRepeatCurrent = mNormalRepeatCur;

			onRecStop();
			mHandler.removeMessages(PLAYER_TIME_CHNAGED);

			gotoSentent(mCurSentence);
			if (DEBUG)
				Log.d(TAG, "exit rec mode :" + mCurSentence);
			autoReplay();
		}
		mIsRecordMode = bRec;
		Recorder.clearOutputFile();
	}

	private void switchToPlayPanel(Animation animShow, Animation animHide) {
		mPlayerPanel.setVisibility(View.VISIBLE);
		mPlayerPanel.startAnimation(animShow);
		mRecordPanel.setVisibility(View.GONE);
		mRecordPanel.startAnimation(animHide);
	}

	private void switchToRecPanel(Animation animShow, Animation animHide) {
		mPlayerPanel.setVisibility(View.GONE);
		mPlayerPanel.startAnimation(animHide);
		mRecordPanel.setVisibility(View.VISIBLE);
		mRecordPanel.startAnimation(animShow);
		
		this.mfontSizeBtn.setBackgroundResource(R.drawable.gsi_button36);
		RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
		bASizeEnabled = false;
	}

	private void onSetting() {
		autoPause();

		// Setting
		Intent it = new Intent(MeReaderActivity.this, SettingActivity.class);
		startActivityForResult(it, SETTING_REQUEST);
	}

	private void onSearching() {
		autoPause();

		// Search
		Intent it = new Intent(MeReaderActivity.this, SearchActivity.class);
		startActivityForResult(it, SEARCH_REQUEST);
	}

	private static float mX1 = 0 ; //, mX2 = 0;
	private static float mY1 = 0 ; //, mY2 = 0;
	private static float mOffset = 15;
	private OnTouchListener mTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			if ( mInitReaderDone != true || true == shutdownRequested){
				return true ;
			}
			
			final int act = event.getAction();

			switch (act) {
			case MotionEvent.ACTION_DOWN:
				mX1 = event.getX();
				mY1 = event.getY();
				break;
				
			case MotionEvent.ACTION_UP:
				
				float x = event.getX();
				float y = event.getY();
				
				if ( Math.abs(x-mX1) > mOffset || Math.abs(y-mY1) > mOffset ){
					return true ;
				}
				
				Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
				int width = display.getWidth(); 
				
				if ( x < width/3){
					if(!mIsFullScreen)
					{
						onFullScreen();
					}
					prevSentence();
				} else if (x < width * 2 / 3) {

					if (false == mIsRecordMode) {
						// switch to full screen

						if (false == mIsPicMode) {
							onFullScreen();
						} else {
							onPicDisplay();
						}
					} else {
						if (DEBUG)
							Log.d(TAG, "absY < mOffset");
						return true;
					}

				} else {
					if(!mIsFullScreen)
					{
						onFullScreen();
					}
					nextSentence();
				}
				return true;

			case MotionEvent.ACTION_CANCEL:
			default:
				break;
			}

			return true;
		}
	};

	private OnTouchListener mTextViewTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			if ( mInitReaderDone != true || true == shutdownRequested){
				return true ;
			}
			
			final int act = event.getAction();

			switch (act) {
			case MotionEvent.ACTION_DOWN:
				mX1 = event.getX();
				mY1 = event.getY();
				break;
				
			case MotionEvent.ACTION_UP:
				
				float x = event.getX();
				float y = event.getY();
				
				if ( Math.abs(x-mX1) > mOffset || Math.abs(y-mY1) > mOffset ){
					return true ;
				}
				
				Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
				int width = display.getWidth(); 
				
				if ( x < width/3){
					if(!mIsFullScreen)
					{
						onFullScreen();
					}
					prevSentence();
				} else if (x < width * 2 / 3) {
						if (false == mIsPicMode) {
							onFullScreen();
						} else {
							onPicDisplay();
						}
				}else {
					if(!mIsFullScreen)
					{
						onFullScreen();
					}
					nextSentence();
				}
				return true;

			case MotionEvent.ACTION_CANCEL:
			default:
				break;
			}

			return false;
		}
	};

	void setSeekbarAB() {

		final SySentence[] sentArr = MebookHelper.mSentenceArr;

		int progA = -1;
		int progB = -1;

		if (mIndexA >= 0) {
			progA = sentArr[mIndexA].getBeginTime();
		}

		if (mIndexB >= 0) {
			progB = sentArr[mIndexB].getEndTime();
		}
		mSeekBar.setAB(progA, progB);
	}

	private boolean isValidApp(SySentence sentence) {

		boolean b = false;
		final ArrayList<SyChapter> chp = MebookHelper.mContent.mChapter;
		if (null != chp && chp.size() > 0) {
			int chpIndex = sentence.mChapterIndex;
			SyChapter chapter = chp.get(chpIndex);
			String strPs = MebookHelper.mContent.mPostscript.getData(chapter.mNo);
			if (null != strPs && strPs.length() > 0) {
				b = true;
			}
		}
		return b;
	}

	private void onExtMode() {
		
		autoPause();

		try {
			mPlayerService.unSubscribe(mCallback);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		final SySentence sentence = MebookHelper.mSentenceArr[mCurSentence];
		if (sentence.isValidExtInfo()||(mIsPhonetic&& mIsJpBasic)) {
			
			
			Intent it = new Intent(MeReaderActivity.this, ExtendActivity.class);
			it.putExtra(CUR_INDEX, mCurSentence);
			it.putExtra(BK_ID, MebookHelper.mHeaderInfo.mBookID);
			startActivityForResult(it, EXTEND_REQUEST);
		}
	}

	private void showNonEditView() {
		mTopPanel.setVisibility(View.VISIBLE);
		mtvChapterName.setVisibility(View.VISIBLE);
		mPlayerPanel.setVisibility(View.VISIBLE);
		
		iTitleView.requestFocus();
		//mtvChapterName.requestLayout();
	}

	private void hideNonEditView() {
		mTopPanel.setVisibility(View.GONE);
		mtvChapterName.setVisibility(View.GONE);
		mPlayerPanel.setVisibility(View.GONE);
		
		this.mfontSizeBtn.setBackgroundResource(R.drawable.gsi_button36);
		RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
		bASizeEnabled = false;
		//mtvChapterName.requestLayout();
	}
	
	protected void onFullScreen() {

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);	
        
		if (false == mIsFullScreen) {
			mIsFullScreen = true;
			hideNonEditView();
		} else {
			mIsFullScreen = false;
			showNonEditView();
		}
	}

	private void onHideTextView() {
//		metOrg.setVisibility(View.GONE);
//		scrollcontent1.setVisibility(View.GONE);
		//metTrl.setVisibility(View.GONE);
		texv.setVisibility(View.GONE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		int index;
		
		if ( RESULT_OK == resultCode){
			if (CHAPTER_LIST_REQUEST == requestCode ) {
				
				boolean isChpMode = data.getExtras().getBoolean(CHP_MODE);
				if (isChpMode ){
					index = data.getExtras().getInt(CHP_INDEX);
					
					// swith mp3 track
					if ( -1 == index ){
						
						mAutoPause = false ;
						mPlayBtn.setEnabled(false);
						mInitReaderDone = false ;
			            mHandler.postDelayed( new Runnable(){

							@Override
							public void run() {
								
								
								mSyPlayerSvc.setFrameTable(MebookHelper.mFrameTable);
								
					            initReader();
					    		// from now on, we could play mp3
					    		mPlayBtn.setEnabled(true);
					    		mInitReaderDone = true ;
							}
			            	
			            }, 300);						
						
						
//						initReader();
//						mAutoPause = false ;
						
						return ;
					}
					
				} else {
					index = data.getExtras().getInt(CUR_INDEX);
				}

				setupSeekbarBookmark(mBookmark);
				if ( 2 == mRangeMode &&
					 -1 == mBookmark.getFirst()){
					mRangeMode = 0 ;
					
					this.setRangeModeIcon(0);
				}					
				
				gotoSentent(index);
				autoReplay();

			} else if (EXTEND_REQUEST == requestCode ){
				try {
					mPlayerService.subscribe(mCallback);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				autoReplay();
			} else if (	POSTSCRIPT_REQUEST == requestCode) {
				autoReplay();
			} else if ( PROMO_REQUEST == requestCode ){
					
				boolean isGoNext = data.getExtras().getBoolean("goNext");
				if (isGoNext) {
					index = getNextSentence();
				} else {
					index = getPrevSentence();
				}
				gotoSentent(index);
				if (mSettingPref.isAutoRepeat()) {
					// this.onPlayPause();
					this.autoReplay();
				}
				return;
			}
		}
		
		if (SEARCH_REQUEST == requestCode) {
	
			if ( RESULT_OK == resultCode ){
				index = data.getExtras().getInt(CUR_INDEX);
				gotoSentent(index);
			}
			autoReplay();
		}else	if (SETTING_REQUEST == requestCode) {
			float fontSize = mSettingPref.getFontSize(getApplicationContext());
			metOrg.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
			mivPic.setFontSize(fontSize);

			texv.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
			
			final SySentence[] arr = MebookHelper.mSentenceArr;
			final SySentence sentence = arr[mCurSentence];

			mSpannedOrg = formatContent(sentence, mIsShowOrg , mIsShowTrl);
			updateContentBySentence(mCurSentence);
			
			mSettingPref.save();
			mPlayCount = mSettingPref.getRepeatCount();

			autoReplay();
		} 
		
		else if ( PROMO_REQUEST == requestCode ){
			
			index = getNextSentence();
			// Note: seekToIndex() will setup mForceChange
			// mForceChange = false;
			gotoSentent(index);
			
			if ( mSettingPref.isAutoRepeat() ){
				//this.onPlayPause();
				this.autoReplay();
			}
		}
	}

	private final void autoReplay() {
		
		boolean isAutoPause = mAutoPause;
		
		if ( isAutoPause ){
			mAutoPause = false ;
			
			onPlayPause();		// replay & change icon
		}
	}

	private final void autoPause() {
		
		boolean isPlaying = mIsPlaying;
		if ( isPlaying ){
			onPlayPause();		// pause player & change icon
			mAutoPause = true;
		}
	}

	OnSeekBarChangeListener mSeekBarChange = new OnSeekBarChangeListener() {
		//boolean mIsPlaying = false;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromTouch) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seek) {
			if ( DEBUG ) Log.e(TAG, "onStartTrackingTouch:");
			mIsUserSeek = true;
			autoPause();
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if ( DEBUG ) Log.e(TAG, "onStopTrackingTouch:");
			mIsUserSeek = false;
			
			final int prog = seekBar.getProgress();
			int time = prog;

			// doing fore change
			int index = getSentenceByTime(time);

			// check seek position out of range
			if (isOutOfRange(index)) {
				mCurSentence = index;
				index = getNextSentence();
			}

			gotoSentent(index); // gotoSentent() will setup mForceChange

			autoReplay();
		}
	};

/*
	private void onPlayerStop() {
	
		if (mIsPlaying){
			onPlayPause();
		}
		
		int index = getFirstSentence();
		gotoSentent(index);
		
		setRangeModeIcon(mRangeMode);
	}
*/
	private void nextSentence() {
		mForceChange = true;
		int index = getNextSentence();
		if (true == mIsRecordMode) {
			mSyPlayerSvc.stop();
			gotoSententRec(index);
			Recorder.clearOutputFile();
		} else {
			if ( isLastSentence(this.mCurSentence) ){
				this.showPromoPage();
			}else {
				gotoSentent(index);
			}
		}
	}

	private void prevSentence() {
		mForceChange = true;
		int index = getPrevSentence();
		if (true == mIsRecordMode) {
			mSyPlayerSvc.stop();
			gotoSententRec(index);
			Recorder.clearOutputFile();
		} else {
			
			if ( isLastSentence(index) ){
				this.showPromoPage();	
			}else {
				gotoSentent(index);
			}
		}
	}

	// ----------------------------------------------------------------------
	// Code showing how to deal with callbacks.
	// ----------------------------------------------------------------------

	/**
	 * This implementation is used to receive callbacks from the remote service.
	 */
	private IPlayerServiceCallback mCallback = new IPlayerServiceCallback.Stub() {
		/**
		 * This is called by the remote service regularly to tell us about new
		 * values. Note that IPC calls are dispatched through a thread pool
		 * running in each process, so the code executing here will NOT be
		 * running in our main thread like most other things -- so, to update
		 * the UI, we need to use a Handler to hop over there.
		 */

		public void timeChanged(long time) throws RemoteException {
				mHandler.sendMessage(mHandler.obtainMessage(
						PLAYER_TIME_CHNAGED, (int) time, 0));
		}
	};

	private static final int PLAYER_TIME_CHNAGED = 100;
	private static final int REC_COUNT_MSG = 500;
	//private static final int SLEEP_COUNT_MSG = 600 ;
	private static final int PLAYER_TIME_QUERY = 700;
	private static final int PLAYER_TIME_IN_RECORD = 800;
	private static final int UPLOAD_DONE = 900;
	private static final int GET_DEVICE_ID_FAIL = 1100;

	private static final long MSG_INTERVAL = 1000;
	private static final long TIME_QUERY_INTERVAL = 100;
	int mRecCount = 30; // 30 sec count down

	static boolean mIsLock = false;
	private volatile boolean shutdownRequested = false ;
	private volatile boolean mUploadDown = false ;
	private Handler mHandler = new Handler() {
		synchronized public void handleMessage(Message msg) {
			
			if ( mUploadDown == false && UPLOAD_DONE == msg.what  ){
				mUploadDown = true ;
				
				if ( 0 == msg.arg1  ){
		            Toast.makeText(MeReaderActivity.this.getApplicationContext(), getResources().getString(R.string.iii_last_page_upload_success),  
		                    Toast.LENGTH_SHORT).show(); 
				}
				
				destroy();
				finish();
				return ;
			}
			
			if ( GET_DEVICE_ID_FAIL == msg.what  ){				
				
				Toast.makeText(MeReaderActivity.this.getApplicationContext(), getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG),  
	                    Toast.LENGTH_LONG).show(); 
				
				destroy();
				finish();
				return ;
			}
			
			if ( true == shutdownRequested || null == msg ){
				return ;
			}

			switch (msg.what) {
			
			case PLAYER_TIME_CHNAGED: // time change
				if (true == mIsRecordMode) {
					onRecTimeChanged(msg.arg1);
					
				} 
				break;
				
				
			case PLAYER_TIME_IN_RECORD:
			{
				if (REC_PLAYING_CONTENT == mRecAct) {
					final int time = mSyPlayerSvc.getCurrentPosition();
					onRecPlayingContent(time);
					sendMessageDelayed(obtainMessage(PLAYER_TIME_IN_RECORD),TIME_QUERY_INTERVAL);
				}
			}
				
			case PLAYER_TIME_QUERY:
			{
				if (mIsPlaying){
					final int time = mSyPlayerSvc.getCurrentPosition();
					
					if ( DEBUG ) Log.d(TAG , "cur pos:" + time);
					onTimeChanged(time);
					sendMessageDelayed(obtainMessage(PLAYER_TIME_QUERY), TIME_QUERY_INTERVAL);
				}
			}
				break;
				
			case REC_COUNT_MSG:
				if (mRecCount <= 0) {
					if ( DEBUG ) Log.d(TAG, "0 == mRecCount");
					onRecStop();
				} else {
					--mRecCount;
					setRecPrompt(mRecCount);
					sendMessageDelayed(obtainMessage(REC_COUNT_MSG), MSG_INTERVAL);
				}
				break;
				
//			case UPLOAD_DONE:
//				
//				destroy();
//				finish();
//				
////				if ( msg.arg1 != 0 ){
////					showErrMsg();
////				} else {
////					destroy();
////					finish();
////				}
//				
//				break;

			default:
				super.handleMessage(msg);
			}
		}
	};

	private final void setRecPrompt(int count) {
		String str = getText(R.string.record_message_2).toString() + " "
				+ count;
		mRecPrompt.setText(str);
	}

	private void setupSeekbarBookmark(SyBookmark bookmark) {

		ArrayList<Integer> bmIndex2Prog = new ArrayList<Integer>();

		final SySentence[] sentArr = MebookHelper.mSentenceArr;
		int index = bookmark.getFirst();

		if (index >= 0) {
			bmIndex2Prog.add(sentArr[index].getBeginTime());

			int last = bookmark.getLast();
			if (last > index) {
				while (index != last) {
					index = bookmark.getNext(index);
					bmIndex2Prog.add(sentArr[index].getBeginTime());
				}
			}
		}
		mSeekBar.setBookmark(bmIndex2Prog);
	}
	
	final void onRecPlayingContent(int time){
		if (-1 != mPlayEndTime && time >= mPlayEndTime){
			playRecFile();
		}
	}

	final void onRecTimeChanged(int time) {
		//if ( DEBUG ) Log.w(TAG, "onRecTimeChanged:" + time);

		if (-1 != mPlayEndTime && time >= mPlayEndTime) {
			if ( DEBUG ) Log.d(TAG, "time > mPlayEndTime");

			if (REC_PLAYING_RECFILE == mRecAct) {
				if ( DEBUG ) Log.d(TAG, "onRecTimeChanged REC_PLAYING2 == mRecAct");
				onRecStop();
			} else {
				if ( DEBUG ) Log.e(TAG, "onRecTimeChanged unknow state:"+mRecAct);
				try {
					mPlayerService.stop();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void playRecFile() {
		try {

			mHandler.removeMessages(PLAYER_TIME_CHNAGED);
			// second time play user reocrd voice
			File f = new File(Recorder.REC_SOURCE);
			if (true == f.exists()) {
				mPlayerService.setDataSource(Recorder.REC_SOURCE);
				mPlayerService.seekTo(0);
				mPlayEndTime = mPlayerService.getDuration();
				if ( DEBUG ) Log.d(TAG, "REC time:" + mPlayEndTime);
				if ( mPlayEndTime > 0 ){
					mPlayerService.play();

					mRecPrompt.setText(R.string.record_message_4);
					mRecAct = REC_PLAYING_RECFILE;
				}else{
					if ( DEBUG ) Log.e(TAG, "REC time < 0" );
				}
			} else {
				if ( DEBUG ) Log.d(TAG, "REC source not exist!");
				onRecStop();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void showPromoPage() {
		
		this.autoPause();
		Intent it = new Intent(MeReaderActivity.this, LastPageActivity.class);
		String flag = null;
		if ( MebookHelper.mIsSample ){
			flag = "Try";
		}else {
			flag = "Active";
		}
		
		it.putExtra(PAGE_FLAG, flag);		// "Try","Active"
		this.startActivityForResult(it, PROMO_REQUEST);		
	}

	final void onTimeChanged(int time) {
		
		final boolean forceChange = mForceChange;
		
		if ( true == forceChange ){
			mForceChange = false ;
		}
		
		int index = getSentenceByTime(time+ LocalService.TOLERANCE);

		if ( mEosTime > 0  && time >= mEosTime ){
			Log.d(TAG , "time > eosTime" + time + "::" + mEosTime);
			//index = 0 ;
			
			//final SySentence[] arr = MebookHelper.mSentenceArr;
			index = mCurSentence+1;			
		}
		
		if (index >= 0 && (mCurSentence != index || true == forceChange)) {

			if (false == forceChange) {
				waitSilent();
			}
			
			// count control
			if (0 == mPlayCount || true == forceChange) {

				if (isOutOfRange(index)) {
					
					if ( isLastSentence2(index) && false == mSettingPref.isAutoRepeat()){

						if ( false == mSettingPref.isAutoRepeat() ){
							onPlayPause();
						} 						
						showPromoPage();
						index = 0 ;
						
						
						
					} else {
						index = getNextSentence();
						mPlayCount = mSettingPref.getRepeatCount();
						gotoSentent(index);
						if ( false == mSettingPref.isAutoRepeat() ){
							onPlayPause();
						} 
//						else {
//							index = getNextSentence();
//							mPlayCount = mSettingPref.getRepeatCount();
//							gotoSentent(index);
//							//updateContentBySentence(index);
//						}
					}
					return ;
					
				} else {
					if (false == forceChange) {
						index = getNextSentence();

						if (index != mCurSentence + 1) {
							time = seekToIndex(index);

							if ( DEBUG ) Log.e(TAG, "index:" + index + " mCurSentence:"
									+ mCurSentence);
						}
						mPlayCount = mSettingPref.getRepeatCount();
					}
					updateContentBySentence(index); // this fn will update
				}
			} else {
				mPlayCount--;

				// Note: seekToIndex() will setup mForceChange
				// mForceChange = false;
				time = seekToIndex(mCurSentence); // go back to current begin
			}
		}
		updateProgress(time);
	}

	private int seekToIndex(int index) {
		
		autoPause();
		mHandler.removeMessages(PLAYER_TIME_QUERY);
		
		int time = MebookHelper.mSentenceArr[index].getBeginTime();
		mSyPlayerSvc.seekTo(time);
		
		autoReplay();
		return time;
	}

	private final int getNextSentence() {

		int index = mCurSentence;
		if (true == mIsRepeatCurrent) {
			if (false == mForceChange) {
				return index;
			} else {
				mPlayCount = 0; // when fore change, clear play count;
				index = getNextSent(index);
			}
		} else {
			int rangeMode = mRangeMode;
			switch (rangeMode) {
			//case SyRangeModeButton.RANGE_MODE_AB:
			case 1:
				index++;
				// when user change position from seek bar,
				// index could be < indexA
				if (index > mIndexB || index < mIndexA) {
					index = mIndexA;
				}
				break;

			case 2:
				index = mBookmark.getNext(index);

				if ( DEBUG ) Log.e(TAG, "cur:" + mCurSentence + " getNext():" + index);
				break;

			case 0:
			default:
				index = getNextSent(index);
				break;
			}

		}
		return index;
	}

	private final int getNextSent(int index) {
		final int sentCount = MebookHelper.mSentenceArr.length;
		if (index < sentCount - 1) {
			index++;
		} else {
			index = 0;
		}
		return index;
	}

	private final int getPrevSentence() {
		int index = mCurSentence;

		if (true == mIsRepeatCurrent) {
			if (false == mForceChange) {
				return index;
			} else {
				mPlayCount = 0; // when fore change, clear play count;
				index = getPrevSent(index);
			}
		} else {

			int rangeMode = mRangeMode ;
			switch (rangeMode) {
			case 1:
				index--;
				if (index < mIndexA) {
					index = mIndexB;
				}
				break;
			case 2:
				index = mBookmark.getPrev(index);

				if ( DEBUG ) Log.e(TAG, "cur:" + mCurSentence + " getPrev():" + index);
				break;

			case 0:
			default:
				index = getPrevSent(index);
				break;
			}
		}
		return index;
	}

	private final int getPrevSent(int index) {
		final int sentCount = MebookHelper.mSentenceArr.length;
		if (index > 0) {
			index--;
		} else {
			index = sentCount - 1;
		}
		return index;
	}
	
/*	
	private final int getFirstSentence() {

		int index = mCurSentence;
		int rangeMode = mRangeMode;
		switch (rangeMode) {
		case 1:
			index = mIndexA;
			break;

		case 2:
			index = mBookmark.getFirst();
			break;

		case 0:
		default:
			index = 0;
			break;
		}
		
		if ( index < 0 ){
			index = 0 ;
		}
		
		return index;
	}	
*/	

	private boolean isOutOfRange(int index) {
		boolean ret = false;

		if (false == mIsRepeatCurrent) {
			int rangeMode = mRangeMode;
			
			switch (rangeMode) {
			case 1:
				if (index > mIndexB || index < mIndexA) {
					ret = true;
				}
				break;
			case 2:
				SyBookmark bm = mBookmark;
				if ( index > bm.getLast() || index < bm.getFirst() ) {
					ret = true;

					if ( DEBUG ) Log.e(TAG, "isOutOfRange bookmark");
				}
				break;

			case 0:
			default:
				final int sentCount = MebookHelper.mSentenceArr.length;
				if (index > sentCount - 1
						|| (0 == index && sentCount - 1 == mCurSentence)) {
					ret = true;
				}
				break;
			}
		}
		return ret;
	}
	
	private boolean isLastSentence(int index){
		final int sentCount = MebookHelper.mSentenceArr.length;
		if (index == sentCount - 1 ){
			return  true ;
		}
		return false ;
	}
	
	private boolean isLastSentence2(int index){
		final int sentCount = MebookHelper.mSentenceArr.length;
		if (index >= sentCount - 1 ){
			return  true ;
		}
		return false ;
	}	

	private void gotoSentent(int curSentence) {
		
		final int sentCount = MebookHelper.mSentenceArr.length;
		if (curSentence < 0) {
			curSentence = 0;
		} else if (curSentence >= sentCount) {
			curSentence = sentCount - 1;
		}

		autoPause();
		int beginTime = MebookHelper.mSentenceArr[curSentence].getBeginTime();

		int cur = curSentence;

		updateContentBySentence(cur);
		updateProgress(beginTime);

		mSyPlayerSvc.seekTo(beginTime);
		autoReplay();
	}

	private void gotoSententRec(final int curSentence) {

//		try {
//			mPlayerService.stop();
//			mHandler.removeMessages(PLAYER_TIME_CHNAGED);
//			final int beginTime = mSentenceArr[curSentence].getBeginTime();
//			mPlayerService.seekTo(beginTime);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		updateContentBySentence(curSentence);
		
		gotoSentent(curSentence);
		
		
	}

	private Comparator<SySentence> comparator = new Comparator<SySentence>() {

		@Override
		public int compare(SySentence arg0, SySentence arg1) {

			final int begin = arg0.getBeginTime();
			final int end = arg0.getEndTime();
			final int test = arg1.getBeginTime();

			int ret = 0;
			if (test < begin) {
				ret = 1;
			} else if (test >= end) {
				ret = -1;
			} else {
				ret = 0;
			}
			return ret;
		}
	};
	//**scroll bookmark
	ImageView imv ;
	SySentence mSenTime;
	int mChpIndex = -1;
	Spanned mSpannedOrg;
	Spanned mSpannedTrl;
	Spanned all ;
	private String deviceID = "";
	private ProgressDialog progDlg;

	final void updateContentBySentence(int index) {

		final SySentence[] arr = MebookHelper.mSentenceArr;

		final int max = arr.length - 1;
		if (index < 0) {
			index = 0;
		} else if (index > max) {
			if ( DEBUG ) Log.e(TAG, "updateContentBySentence index:" + index);
			index = max;
		}

		final SySentence sentence = arr[index];
		
		boolean bPic, bExt, bBookmark, bApp;
		bPic = sentence.isValidPic();
		bExt = sentence.isValidExtInfo();
		bBookmark = mBookmark.isMarked(index);
		bApp = isValidApp(sentence);
		
		if ( mCurSentence != index || true == mForceUpdateContent ){
			mCurSentence = index;
			mSpannedOrg = formatContent(sentence, mIsShowOrg , mIsShowTrl);
		}

		if (false == mIsPicMode) {
			
			mPicBtn.setEnabled(bPic);
			
			//metOrg.setText(mSpannedOrg);
			//**scroll bookmark
			texv.setText(mSpannedOrg);
		} else {

			Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.nopicture );
			imv = (ImageView)findViewById(R.id.imageView_pic);
			Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			
			if (bPic) {
				String pic = sentence.mData.get(MebookToken.TOK_PIC);
				SyItem item;
				try {
					if ( null != MebookHelper.mISSyd){
						item = MebookHelper.mBookData.getData(pic, MebookData.DATA_IMG, MebookHelper.mISSyd);
						mivPic.setImageBitmap(item.toBitmap());
						imv.setImageBitmap(item.toBitmap());				
					} else {
						mivPic.setImageBitmap(bmp);
						imv.setImageBitmap(bmp);
					}
				} catch (MebookException e) {
					e.printStackTrace();
				}
			} else {
				mivPic.setImageBitmap(bmp);
				imv.setImageBitmap(bmp);
			}
			
			RelativeLayout.LayoutParams vp = new RelativeLayout.LayoutParams(display.getWidth(), display.getHeight()/2);
			imv.setLayoutParams(vp);
			imv.setScaleType(ImageView.ScaleType.FIT_CENTER);	
			
			mivPic.setOrg(mSpannedOrg.toString());
			texv.setText(mSpannedOrg);
			texv.scrollTo(0, 0);
//			texv.requestLayout();
		}
		
		// for Jp basic
		if ( false == bExt && /*0 == MebookHelper.mHeaderInfo.mBookID.compareTo(MebookInfo.JP_BASIC)*/ mIsJpBasic){
			bExt = mIsPhonetic;
		}
		updateIndicate(bPic, bExt, bBookmark, bApp);		

		int chpIndex = sentence.mChapterIndex;
		updateChapter(chpIndex);
	}

	private final void updateIndicate(boolean pic, boolean ext,
			boolean bookmark, boolean app) {
		
		int resid ;
		resid = pic ? R.drawable.key_picture_btn : R.drawable.key_picturen;
		mPicBtn.setBackgroundResource(resid);
		

		int visiable = ext? View.VISIBLE : View.INVISIBLE;
		mExtIcon.setVisibility(visiable);
		
		mExtBtn.setEnabled(ext);
		
		resid = ext? R.drawable.key_ext_btn : R.drawable.key_extn;
		mExtBtn.setBackgroundResource(resid);

		onSetBookmark(bookmark);
		
		
		this.mPosAIcon.setVisibility(View.INVISIBLE);
		this.mPosBIcon.setVisibility(View.INVISIBLE);
		if ( mIndexA == this.mCurSentence ){
			this.mPosAIcon.setVisibility(View.VISIBLE);
		}
		if ( mIndexB == this.mCurSentence){
			this.mPosBIcon.setVisibility(View.VISIBLE);
		}
		
		
	}

	private final void updateChapter(int chpIndex) {
		if (mChpIndex != chpIndex) {
			mChpIndex = chpIndex;

			ArrayList<SyChapter> chps = MebookHelper.mContent.mChapter;
			String chapterName;
			if (chps.size() <= 0) {
				chapterName = MebookHelper.mHeaderInfo.mTitle;
			} else {
				chapterName = chps.get(chpIndex).mName;
			}

			mtvChapterName.setText(chapterName);
		}
	}

	private void waitSilent() {

		int wait = mSettingPref.getGap();
		if (wait > 0) {
			
			LocalService playerSvc = mSyPlayerSvc;

				try {
					playerSvc.pause();
					Thread.sleep(wait);
					playerSvc.play();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			
/*			
			try {
				IPlayerService player = mPlayerService;
				player.pause();
				SystemClock.sleep(wait - 40);
				player.play();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
*/			
		}
	}

	private final int getSentenceByTime(int time) {
		
		if ( null == mSenTime ){
			Log.e( TAG , "mSenTime is null!");
			mSenTime = new SySentence();
			return 0 ;
		}


		final SySentence[] arr = MebookHelper.mSentenceArr;
		int index = 0;
		final int max = arr.length - 1;
		// some content begin time > 0, like dvd2me
		if (time <= arr[0].getBeginTime()) {
			index = 0;
		} else {
			SySentence senTime = mSenTime;
			senTime.mBeginTime.setValue(time);

			index = Arrays.binarySearch(arr, senTime, comparator);
			if (index > max || index < 0) {
				if ( DEBUG ) Log.e(TAG, "getSentenceByTime(" + time + ") index:" + index
						+ " max:" + max);
				index = max;
			}
		}
		// if ( DEBUG ) Log.e(TAG, "getSentenceByTime("+time+") index:"+index +" max:"+max);
		return index;
	}

	static final String mMrkColorBegin = "<font color=\"#009f3c\">";
	static final String mHltColorBegin = "<font color=\"#0000ff\">";
	static final String mVocColorBegin = "<font color=\"#ff0000\">";
	static final String mColorEnd = "</font>";
	
	final Spanned formatContent(final SySentence sentence, boolean showOrg , boolean showTrl) {
		final HashMap<Integer, String> data = sentence.mData;
		
		StringBuilder sb = new StringBuilder();		
		if (true == showOrg) {
			final String org = data.get(MebookToken.TOK_ORG);
			
			if (null == org || org.length() <= 0) {
				sb.append(" ");
			} else {

				int hidePercent = mSettingPref.getHidePercent() * 10;
				if (hidePercent > 0 && hidePercent < 100) {
					String cloze = Native.hideCertainWords(org, hidePercent, 0);
					sb.append(cloze);
				} else {
					sb.append(org);
				}
			}

			//
			// replace "\r\n" to "<br>"
			//
			int index;
			Util.replaceCRLF(org, sb);

			if ( true == MebookHelper.mIsJpBook ) {
				mIsPhonetic = Util.removePhonetic(sb);
			}

			final String[] aStr = sentence.mHlt;
			if (null != aStr && aStr.length > 0) {
				String colorBegin;

				// add mrk icon
				if (true == sentence.isDataValid(MebookToken.TOK_MRK)) {
					colorBegin = mMrkColorBegin;
					int mrk = Integer.parseInt(data.get(MebookToken.TOK_MRK));

					String mrkTag;
					if (0 == mrk) {
						mrkTag = String.format("<img src=\"%d\"/>",
								R.drawable.mega_103);
					} else {
						mrkTag = String.format("<img src=\"%d\"/> %s%d%s ",
								R.drawable.mega_103, mMrkColorBegin, mrk,
								mColorEnd);
					}
					sb.insert(0, mrkTag + " ");
				} else {
					colorBegin = mHltColorBegin;
				}

				// hight light keyword
				int end;
				String target;

				for (int i = aStr.length - 1; i >= 0; i--) {
					index = sb.length() - 1;
					target = aStr[i].trim();
					int len = target.length();

					if (len <= 0) {
						continue;
					}

					int ph = 0;
					String[] phHlts = TextUtils.split(target, "[*]");
					ph = phHlts.length;

					if (ph > 1) {
						String phWord;
						int hitCount = 0;
						int[] pos = new int[8];
						int[] lenPh = new int[8];
						int ix = 0;
						for (int phIndex = 0; phIndex < ph; ++phIndex) {
							phWord = phHlts[phIndex].trim();
							
							// check find a word not substring
							ix = sb.indexOf(phWord,ix);
							while( -1 != ix && ix > 0 &&  Util.isAlphabet(sb.charAt(ix-1))){
								ix = sb.indexOf(phWord,ix+1);
							}							
							
							if ( -1 == ix ){
								break;
							}
							pos[hitCount] = ix;
							lenPh[hitCount] = phWord.length();
							hitCount++;
						}

						// patten bingo!
						if (hitCount > 0 && hitCount == ph) {
							for (int phIndex = ph - 1; phIndex >= 0; phIndex--) {
								end = Util.getWord(sb.toString(), pos[phIndex]
										+ lenPh[phIndex]);
								sb.insert(end, mColorEnd);
								sb.insert(pos[phIndex], mVocColorBegin);
							}
						}

					} else {
						while ((index = sb.lastIndexOf(target, index)) >= 0) {

							boolean bFind = true;
							if (index > 0) {
								if (Util.isAlphabet(sb.charAt(index - 1)) == true) {
									bFind = false;
								}
							}
							if (bFind) {

								end = Util.getWord(sb.toString(), index + len);
								sb.insert(end, mColorEnd);
								sb.insert(index, colorBegin);
							}else {
								index -= len ;
							}
						}
					}
				}
			}

			// highlight voc
			final ArrayList<SyVocInfo> vocInfoArr = sentence.mVocInfo;

			int end;
			int tagBegin, tagEnd;

			for (SyVocInfo vocInfo : vocInfoArr) {

				int len;
				String vocHlt;
				if (null != vocInfo.mHlt) {

					for (int i = vocInfo.mHlt.length - 1; i >= 0; i--) {
						index = sb.length() - 1;
						vocHlt = vocInfo.mHlt[i].trim();
						len = vocHlt.length();

						int ph = 0;
						String[] phHlts = TextUtils.split(vocHlt, "[*]");
						ph = phHlts.length;

						if (ph > 1) {
							String phWord;
							int hitCount = 0;
							int[] pos = new int[8];
							int[] lenPh = new int[8];
							int ix = 0;
							for (int phIndex = 0; phIndex < ph; ++phIndex) {
								phWord = phHlts[phIndex].trim();
								ix = sb.indexOf(phWord,ix);
								while( -1 != ix && ix > 0 &&  Util.isAlphabet(sb.charAt(ix-1))){
									ix = sb.indexOf(phWord,ix+1);
								}
								if (-1 == ix) {
									break;
								}
								pos[hitCount] = ix;
								lenPh[hitCount] = phWord.length();
								hitCount++;
							}

							// patten bingo!
							if (hitCount > 0 && hitCount == ph) {
								for (int phIndex = ph - 1; phIndex >= 0; phIndex--) {
									end = Util.getWord(sb.toString(),
											pos[phIndex] + lenPh[phIndex]);
									sb.insert(end, mColorEnd);
									sb.insert(pos[phIndex], mVocColorBegin);
								}
							}
						} else {

							while ((index = sb.lastIndexOf(vocHlt, index)) >= 0) {
								end = Util.getWord(sb.toString(), index + len);

								tagEnd = sb.lastIndexOf(mColorEnd, index);
								tagBegin = sb.lastIndexOf(mHltColorBegin, index);

								if (tagBegin > tagEnd) {
									sb.insert(end, mColorEnd + mHltColorBegin);
									sb.insert(index, mColorEnd + mVocColorBegin);
								} else {
									sb.insert(end, mColorEnd);
									sb.insert(index, mVocColorBegin);
									index++;
								}
							}
						}
					}
				} else {

					index = sb.length() - 1;
					len = vocInfo.mVoc.length();

					int findCount = 0;
					while ((index = sb.lastIndexOf(vocInfo.mVoc, index)) >= 0) {
						end = Util.getWord(sb.toString(), index + len);

						tagEnd = sb.lastIndexOf(mColorEnd, index);
						tagBegin = sb.lastIndexOf(mHltColorBegin, index);

						if (tagBegin > tagEnd) {
							sb.insert(end, mColorEnd + mHltColorBegin);
							sb.insert(index, mColorEnd + mVocColorBegin);
						} else {
							sb.insert(end, mColorEnd);
							sb.insert(index, mVocColorBegin);
						}
						findCount++;
					}

					// Workaround
					if (0 == findCount) {
						String voc = vocInfo.mVoc;
						char c = voc.charAt(0);

						if (c >= 'a' && c <= 'z') {
							c = (char) (c - 'a' + 'A');
						} else if (c >= 'A' && c <= 'Z') {
							c = (char) (c - 'A' + 'a');
						}

						StringBuilder sbVoc = new StringBuilder(voc);
						sbVoc.deleteCharAt(0);
						sbVoc.insert(0, c);
						index = sb.length() - 1;
						while ((index = sb.lastIndexOf(sbVoc.toString(), index)) >= 0) {
							end = Util.getWord(sb.toString(), index + len);
							sb.insert(end, mColorEnd);
							sb.insert(index, mVocColorBegin);
						}
					}

				}
				Util.replaceFontTag(sb);
			}
			sb.append("<br><br>");
		}
		
		// trl area
		if (true == showTrl) {
			final String trlColor = "<font color=\"#0a2000\">";
			final String endColor = "</font>";
			
			final String trl = sentence.mData.get(MebookToken.TOK_TRL);
			if (null == trl || trl.length() <= 0) {
				sb.append(" ");
			} else {
				sb.append(trlColor);
				sb.append(trl);
				Util.replaceCRLF(sb.toString(), sb);

				if ( true == MebookHelper.mIsJpBook){
					mIsPhonetic |= Util.removePhonetic(sb);
				}
				
				sb.append(endColor);
			}
		}
		
		return Html.fromHtml(sb.toString(), this, null);
	}	

	final Spanned formatOrg(final SySentence sentence) {
		final HashMap<Integer, String> data = sentence.mData;
		final String org = data.get(MebookToken.TOK_ORG);
		StringBuilder sb;

		if (null == org || org.length() <= 0) {
			sb = new StringBuilder();
			sb.append(" ");
		} else {
			
			int hidePercent = mSettingPref.getHidePercent()*10;
			if ( hidePercent > 0  && hidePercent < 100 ){
				String cloze = Native.hideCertainWords(org,hidePercent,0);
				sb = new StringBuilder(cloze);
			} else {
				sb = new StringBuilder(org);
			}
		}

		//
		// replace "\r\n" to "<br>"
		//
		int index;
		Util.replaceCRLF(org, sb);

		if (Mebook.SY_PLAN_BOOK == MebookHelper.mHeaderInfo.mType
				|| Mebook.IMG_BOOK == MebookHelper.mHeaderInfo.mType) {
			Util.replaceKK(sb);
		//}else if ( 0 == MebookHelper.mHeaderInfo.mBookID.compareTo(MebookInfo.JP_BASIC)){
		} else if ( true == MebookHelper.mIsJpBook){
			mIsPhonetic = Util.removePhonetic(sb);
		}

			// basic type content
			final String[] aStr = sentence.mHlt;
			if (null != aStr && aStr.length > 0) {
				String colorBegin;

			// add mrk icon
			if (true == sentence.isDataValid(MebookToken.TOK_MRK)) {
				colorBegin = mMrkColorBegin;
				int mrk = Integer.parseInt(data.get(MebookToken.TOK_MRK));

				String mrkTag;
				if (0 == mrk) {
					mrkTag = String.format("<img src=\"%d\"/>",
							R.drawable.mega_103);
				} else {
					mrkTag = String
							.format("<img src=\"%d\"/> %s%d%s ",
									R.drawable.mega_103, mMrkColorBegin, mrk,
									mColorEnd);
				}
				sb.insert(0, mrkTag + " ");
			} else {
				colorBegin = mHltColorBegin;
			}

			// hight light keyword
			int end;
			String target;

			for (int i = aStr.length - 1; i >= 0; i--) {
				index = sb.length() - 1;
				target = aStr[i].trim();
				int len = target.length();

				if (len <= 0) {
					continue;
				}

				int ph = 0;
				String[] phHlts = TextUtils.split(target, "[*]");
				ph = phHlts.length;

				if (ph > 1) {
					String phWord;
					int hitCount = 0;
					int[] pos = new int[8];
					int[] lenPh = new int[8];
					int ix = 0;
					for (int phIndex = 0; phIndex < ph; ++phIndex) {
						phWord = phHlts[phIndex].trim();
						ix = sb.indexOf(phWord, ix);
						if (-1 == ix) {
							break;
						}
						pos[hitCount] = ix;
						lenPh[hitCount] = phWord.length();
						hitCount++;
					}

					// patten bingo!
					if (hitCount > 0 && hitCount == ph) {
						for (int phIndex = ph - 1; phIndex >= 0; phIndex--) {
							end = Util.getWord(sb.toString(), pos[phIndex]
									+ lenPh[phIndex]);
							sb.insert(end, mColorEnd);
							sb.insert(pos[phIndex], mVocColorBegin);
						}
					}

				} else {
					while ((index = sb.lastIndexOf(target, index)) >= 0) {

						end = Util.getWord(sb.toString(), index + len);
						sb.insert(end, mColorEnd);
						sb.insert(index, colorBegin);
					}
				}
			}


			// highlight voc
			final ArrayList<SyVocInfo> vocInfoArr = sentence.mVocInfo;

			int tagBegin , tagEnd ;
			
			for (SyVocInfo vocInfo : vocInfoArr) {
				
				int len;
				String vocHlt;
				if (null != vocInfo.mHlt) {
					
					for (int i = vocInfo.mHlt.length - 1; i >= 0; i--) {
						index = sb.length() - 1;
						vocHlt = vocInfo.mHlt[i].trim();
						len = vocHlt.length();
						
						int ph = 0 ;
						String [] phHlts = TextUtils.split(vocHlt, "[*]");
						ph = phHlts.length;
						
						if (ph > 1) {
							String phWord;
							int hitCount = 0 ;
							int [] pos = new int[8];
							int [] lenPh = new int[8];
							int ix = 0 ;
							for ( int phIndex = 0 ; phIndex < ph; ++phIndex){
								phWord = phHlts[phIndex].trim();
								ix = sb.indexOf(phWord,ix);
								if ( -1 == ix ){
									break;
								}
								pos[hitCount] = ix ;
								lenPh[hitCount] = phWord.length();
								hitCount++;
							}
							
							// patten bingo!
							if ( hitCount > 0 && hitCount == ph ){
								for ( int phIndex = ph-1 ; phIndex >= 0 ; phIndex --){
									end = Util.getWord(sb.toString(), pos[phIndex]+lenPh[phIndex]);
									sb.insert(end, mColorEnd);
									sb.insert(pos[phIndex], mVocColorBegin);
								}
							}
						} else {

							while ((index = sb.lastIndexOf(vocHlt, index)) >= 0) {
								end = Util.getWord(sb.toString(), index + len);

								tagEnd = sb.lastIndexOf(mColorEnd, index);
								tagBegin = sb
										.lastIndexOf(mHltColorBegin, index);

								if (tagBegin > tagEnd) {
									sb.insert(end, mColorEnd + mHltColorBegin);
									sb.insert(index, mColorEnd+ mVocColorBegin);
								} else {
									sb.insert(end, mColorEnd);
									sb.insert(index, mVocColorBegin);
								}
							}
						}
					}
				} else {

					index = sb.length() - 1;
					len = vocInfo.mVoc.length();

					int findCount = 0;
					while ((index = sb.lastIndexOf(vocInfo.mVoc, index)) >= 0) {
						end = Util.getWord(sb.toString(), index + len);
						
						
						tagEnd = sb.lastIndexOf(mColorEnd , index );
						tagBegin = sb.lastIndexOf(mHltColorBegin , index );
						
						if ( tagBegin > tagEnd){
							sb.insert(end, mColorEnd+mHltColorBegin);
							sb.insert(index, mColorEnd+mVocColorBegin);
						}else{
							sb.insert(end, mColorEnd);
							sb.insert(index, mVocColorBegin);
						}

						findCount++;
					}

					// Workaround
					if (0 == findCount) {
						String voc = vocInfo.mVoc;
						char c = voc.charAt(0);

						if (c >= 'a' && c <= 'z') {
							c = (char) (c - 'a' + 'A');
						} else if (c >= 'A' && c <= 'Z') {
							c = (char) (c - 'A' + 'a');
						}

						StringBuilder sbVoc = new StringBuilder(voc);
						sbVoc.deleteCharAt(0);
						sbVoc.insert(0, c);
						index = sb.length() - 1;
						while ((index = sb.lastIndexOf(sbVoc.toString(), index)) >= 0) {
							end = Util.getWord(sb.toString(), index + len);
							sb.insert(end, mColorEnd);
							sb.insert(index, mVocColorBegin);
						}
					}
				}
			}
			// support <h> </h>
			Util.replaceFontTag(sb);
		}

		String strDbg = sb.toString();
		if ( DEBUG ) Log.d(TAG , strDbg);
		
		return Html.fromHtml(sb.toString(), this, null);
	}

	final Spanned formatTrl(final SySentence sentence) {
		
		final String trl = sentence.mData.get(MebookToken.TOK_TRL);
		StringBuilder sb;
		if (null == trl || trl.length() <= 0) {
			sb = new StringBuilder();
			sb.append(" ");
		} else {
			sb = new StringBuilder(trl);
			Util.replaceCRLF(trl, sb);
			
			//if ( 0 == MebookHelper.mHeaderInfo.mBookID.compareTo(MebookInfo.JP_BASIC)){
			if ( true == MebookHelper.mIsJpBook) {
				mIsPhonetic |= Util.removePhonetic(sb);
			}			
		}
		return Html.fromHtml(sb.toString());
	}

	final void updatePrgressBar(int time) {
		SeekBar seekBar = mSeekBar;
		seekBar.setProgress(time);
	}

	final void updateProgressText(int time) {
		String strTime = Util.formatTimeString(time);
		mtvCurTime.setText(strTime);
	}

	final void updateProgress(int time) {

		if (true == mIsUserSeek) {
			return;
		}

		updatePrgressBar(time);
		updateProgressText(time);
	}

	final void updateProgressByUser(int time) {
		// only update cur time

		if (false == mIsUserSeek) {
			return;
		}
		updateProgressText(time);
	}

	@Override
	public Drawable getDrawable(String src) {	// for Html.ImageGetter

		if ( DEBUG ) Log.d(TAG, "getDrawable :"+src);
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), Integer
				.parseInt(src));
		BitmapDrawable drawable = new BitmapDrawable(bmp);

		drawable.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
		return drawable;
	}

	@Override
	protected void onDestroy() {
		
		//MebookHelper.clear();

		if ( DEBUG ) Log.d(TAG, "mereaderActivity onDestroy");
		super.onDestroy();
	}
	
	/**
	 *  Handle back even from UI or hot key
	 */	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			
			if ( 0 != exit()){
				return true ;
			}

		default:
			return super.onKeyDown(keyCode, event);
		}

	}

 	private boolean testNetwork() {
//		TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);	
//		return telMgr.getDataState() == TelephonyManager.DATA_CONNECTED;
	     NetworkInfo info = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	        if (info==null || !info.isConnected()) {
	                return false;
	        }
	        return true; 		
	}  	
	
 	private static final String RET_CODE_BEGIN = "<error>";
 	private static final String RET_CODE_END = "</error>";
// 	private String mErrMsg;
	private void uploadLastPage() {
		
		//ProgressDialog progressDlg = progDlg ;
		final int sentence = this.mCurSentence;

		try{
			deviceID = GSiMediaRegisterProcess.getID(MeReaderActivity.this);
		}catch(Throwable e){deviceID = "";};
    	if (deviceID == null) deviceID = "";
    	
		new Thread() {
			

			@Override
			public void run() {
				
				if ( false == testNetwork() ){
					//mErrMsg = getResources().getString(R.string.network_not_available);
					
					if ( null != progDlg && progDlg.isShowing()  ){
						progDlg.dismiss();
						progDlg = null;
					}
					mHandler.sendMessage(mHandler.obtainMessage(UPLOAD_DONE,1 , 0 ));
					return ;
				}				

				int ret = 1 ;
				//deviceID = GSiMediaRegisterProcess.getID(MeReaderActivity.this);
				String apiUrl = getResources().getString(
						R.string.web_api_lastpage_up)+MebookHelper.mDeliverID+"&device_id="+deviceID+"&token="+RealBookcase.getToken();
				
				
				HttpPost httppost = new HttpPost(apiUrl);//apiUrl+MebookHelper.mDeliverID+ "&device_id=" + deviceID+"&token="+RealBookcase.getToken());

				String respBody = "Err.";
				try {
					
//				    private static final String STAMP_BEGIN= "<updated_at>";
//				    private static final String STAMP_END= "</updated_at>";
//				    private static final String TRACK_BEGIN= "<track>";
//				    private static final String TRACK_END = "</track>";
//				    private static final String SENT_BEGIN= "<index>";
//				    private static final String SENT_END = "</index>";					


					String upload = "xml=<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
							+ "<ebooks><ebook>"
							+ "<Delivery-ID>%s</Delivery-ID><title>%s</title>"
							+ "<track>%s</track><index>%s</index>"
							+ "</ebook></ebooks>";

					String sendXml = String.format(upload,
							MebookHelper.mDeliverID,
							MebookHelper.mHeaderInfo.mTitle,
							MebookHelper.mTrackIndex, sentence);

					StringEntity se = new StringEntity(sendXml, HTTP.UTF_8);
					se.setContentType("text/xml");

//					httppost.setHeader("Accept",
//									"text/html,application/xml,application/xhtml+xml,text/html");
//					httppost.setHeader("Content-Type",
//							"application/soap+xml;charset=UTF-8");
					
					// for SSO realte API
					httppost.setHeader("Content-Type","application/x-www-form-urlencoded");
					httppost.setHeader("Charset", "UTF-8");
					
					httppost.setEntity(se);

					HttpParams httpParameters = new BasicHttpParams();
					// Set the timeout in milliseconds until a connection is
					// established.
					int timeoutConnection = 10000;
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							timeoutConnection);
					// Set the default socket timeout (SO_TIMEOUT)
					// in milliseconds which is the timeout for waiting for
					// data.
					int timeoutSocket = 10000;
					HttpConnectionParams.setSoTimeout(httpParameters,
							timeoutSocket);

					DefaultHttpClient httpclient = new DefaultHttpClient(
							httpParameters);
					HttpResponse response;

					// exec valid user
					response = httpclient.execute(httppost);
					InputStream is = response.getEntity().getContent();
					
//					BufferedReader r = new BufferedReader(new InputStreamReader(is));
//					StringBuilder total = new StringBuilder();
//					String line;
//					while ((line = r.readLine()) != null) {
//					    total.append(line);
//					}	
					
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = null;
					try {
						db = dbf.newDocumentBuilder();
					} catch (ParserConfigurationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				
			
					//InputStream is = conn.getInputStream();
					Document doc = null ;
					try {
						doc = db.parse(is);
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						if ( null != progDlg && progDlg.isShowing()){
							progDlg.dismiss();
							progDlg = null;
						}	
					}
					NodeList nError = doc.getElementsByTagName("status");
					String ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
					
					NodeList nDesc = doc.getElementsByTagName("description");
					String ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();

					NodeList nData = doc.getElementsByTagName("data");
					String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
					InputStream dataStream = covertStringToStream(RevData);	
					
					respBody = AnReader.convertStreamToString(dataStream);
					Log.e("Token","ebook_error=>"+ebook_error+" "+"ebook_description=>"+ebook_description);
					if (ebook_error.equals("1")) {
						// <?xml version="1.0"
						// encoding="utf-8"?><response><error>0</error></response>
					// 
					int begin = respBody.indexOf(RET_CODE_BEGIN);
					
					String msg = "";
					if ( begin >= 0 ){
						begin += RET_CODE_BEGIN.length();
						int end = respBody.indexOf(RET_CODE_END,begin);
						if (end > begin) {
							msg = respBody.substring(begin, end);

							if (0 == msg.compareTo("0")) {
								ret = 0;
							} else {
								// mErrMsg =
								// getResources().getString(R.string.network_not_available);
								//mErrMsg = msg;
								ret = 1;
							}
						}else {
								// mErrMsg =
								// getResources().getString(R.string.drm_issue_unknow);
								ret = 1;
						}
					}else {
						ret = 0 ;
					}
					}else{
						//do not thing
						
					}
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					//mErrMsg = e.getMessage();
					ret = 1 ;
				} catch (IOException e) {
					e.printStackTrace();
					//mErrMsg = e.getMessage();
					ret = 1 ;
				}finally {
					if ( null != progDlg && progDlg.isShowing()){
						progDlg.dismiss();
						progDlg = null;
				}
				}
				
				if(deviceID.length() == 0){
					mHandler.sendMessage(mHandler.obtainMessage(
							GET_DEVICE_ID_FAIL, 0 , 0));					
				}else{				
				mHandler.sendMessage(mHandler.obtainMessage(
						UPLOAD_DONE, ret , 0));
			}
			}

			public InputStream covertStringToStream(String s){
				/*
				* Convert String to InputStream using ByteArrayInputStream
				* class. This class constructor takes the string byte array
				* which can be done by calling the getBytes() method.
				*/
				InputStream is = null;
				try {
					is = new ByteArrayInputStream(s.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return is;		
			}
		}.start();
	}	

	private void saveLastPage() {

		// Format the current time.
		final String dateFormat = getResources().getString(R.string.time_stamp_format);
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);		//"yyyy/MM/dd hh:mm:ss"
		Date date = new Date();
		String dateString = formatter.format(date);

		SharedPreferences pref = getSharedPreferences(MebookHelper.mDeliverID,Context.MODE_PRIVATE);
		pref.edit().putString(AnReader.TIME_STAMP, dateString).commit();

//		getPreferences(MODE_PRIVATE).edit().
//				putInt(MebookHelper.mHeaderInfo.mTitle + MebookHelper.mTrackIndex,mCurSentence)
//				.commit();
		pref.edit().putInt("Title" + MebookHelper.mTrackIndex,mCurSentence).commit();
	}
	
	//
	// return value:
	// true: exit record mode
	// false: reader mode exit
	private int exit() {
		
		if (true == mIsRecordMode) {
			onRecording();
			return 1;
		} else {
			
			if ( true == shutdownRequested){
				return -1;
			}	
			shutdownRequested = true;			
			
			if ( mIsPlaying ){
				onPlayPause();
			}
			
			if (MebookHelper.mIsSyncLastPage) {
				
				if(null != progDlg && progDlg.isShowing()){
					progDlg.dismiss();
					progDlg = null;
				}
					
				progDlg = ProgressDialog.show(this,
							getText(R.string.upload_last_page_title),
							getText(R.string.upload_last_page_message), false);
				uploadLastPage();
				return -1 ;
			} else {
				destroy();
				finish();
			}
		}
		return 0 ;
	}

	private void destroy() {
	
		saveLastPage();

		mHandler.removeMessages(PLAYER_TIME_QUERY);

		try {
			if (null != mPlayerService) {
				mPlayerService.stop();
				mPlayerService.unSubscribe(mCallback);
				mPlayerService = null;
			}
			mHandler.removeMessages(PLAYER_TIME_CHNAGED);

			if (null != mConnection) {
				// if ( DEBUG ) Log.e(TAG, "unbindService");
				unbindService(mConnection);
				mConnection = null;
			}

			if (null != mConn) {
				mSyPlayerSvc.destroy();
				unbindService(mConn);
				mConn = null;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		loadPreference();					
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
//		final ToggleButton orgBtn = (ToggleButton) findViewById(R.id.orgBtn);
//		orgBtn.setPressed(!mIsShowOrg);
//		
//		final ToggleButton trlBtn = (ToggleButton) findViewById(R.id.trlBtn);
//		trlBtn.setPressed(!mIsShowTrl);				

		mHandler.removeMessages(PLAYER_TIME_CHNAGED);
		if (-1 != mCurSentence) {
//			getPreferences(MODE_PRIVATE).edit().putInt(MebookHelper.mHeaderInfo.mTitle + MebookHelper.mTrackIndex,
//					mCurSentence).commit();
			
			SharedPreferences pref = getSharedPreferences(MebookHelper.mDeliverID,Context.MODE_PRIVATE);
			pref.edit().putInt("Title" + MebookHelper.mTrackIndex,mCurSentence).commit();			

			savePreference();
		}
	}
	
	private void loadPreference() {
		
		if ( null == mSettingPref){
			mSettingPref = new SettingPreference(this);
		}		
		mSettingPref.load();
	}	

	private void savePreference() {
		if ( null != mBookmark ){
			//mBookmark.save(MebookHelper.mHeaderInfo.mTitle+ MebookHelper.mTrackIndex);
			mBookmark.save(MebookHelper.mDeliverID+"_"+MebookHelper.mTrackIndex);
		}
		if ( null != mSettingPref ){
			mSettingPref.save();
		}
	}
	
	private boolean bASizeEnabled = false;
	int mFontLevel ;
	
	private void enableAsizePanel(boolean aEnable) {
		if (aEnable) {		
			RelativeLayout ctl_color = (RelativeLayout) findViewById(R.id.ctl_color);
			ctl_color.removeAllViews();

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			View root = (ViewGroup) inflater.inflate(R.layout.fontsize_item_mereader, null);
					
			SettingPreference  pref = mSettingPref;
			int fontType = pref.getFontType();
			
			String [] fonts = Util.getScaledFontArray(this.getApplicationContext());
			int count = fonts.length;  
			
			if ( -1 == fontType){
				fontType = count/2;
			}
			int progress = fontType*100/count;
			if ( progress > 100){
				progress = 100;
			}
	        mFontLevel = fontType;
			
			
			SeekBar iSeekBar = (SeekBar)root.findViewById(R.id.SeekBar_Size);
			iSeekBar.setMax(100);
			iSeekBar.setProgress(progress);
			iSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub		
					if ( fromUser ){
						
						String [] fonts =Util.getScaledFontArray(getApplicationContext());// getResources().getStringArray(R.array.font);
						int count = fonts.length-1;
						
						mFontLevel = progress * count / 100;

						SettingPreference  pref = mSettingPref;
						pref.setFontType(mFontLevel);
						
						float fontSize = mSettingPref.getFontSize(getApplicationContext());
						metOrg.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
						mivPic.setFontSize(fontSize);
						
						texv.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
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
			//RLP.addRule(RelativeLayout.CENTER_IN_PARENT);
			//root.setLayoutParams(RLP);
			ctl_color.addView(root, RLP);
			
			this.mfontSizeBtn.setBackgroundResource(R.drawable.gsi_button36_2);
			RelativeLayout_Ctls_Colors.setVisibility(View.VISIBLE);
			bASizeEnabled = true;
		} else {
			this.mfontSizeBtn.setBackgroundResource(R.drawable.gsi_button36_btn);
			RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
			bASizeEnabled = false;
		}
	}
        //** turn the page
	@Override
	public boolean onDown(MotionEvent event) {
		// TODO Auto-generated method stub
		mX1 = event.getX();
		mY1 = event.getY();
		return false;
	}



	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}



	@Override
	public void onLongPress(MotionEvent e) {
	}



	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}



	@Override
	public void onShowPress(MotionEvent event) {

		float x = event.getX();
		float y = event.getY();
		
		if ( Math.abs(x-mX1) > mOffset || Math.abs(y-mY1) > mOffset ){
			
		}
		
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		int width = display.getWidth(); 
		
		if ( x < width/3){
			if(!mIsFullScreen)
			{
				onFullScreen();
			}
			prevSentence();
		} else if (x < width * 2 / 3) {

			if (false == mIsRecordMode) {
				// switch to full screen

				if (false == mIsPicMode) {
					onFullScreen();
				} else {
					onPicDisplay();
				}
			} else {
				if (DEBUG)
					Log.d(TAG, "absY < mOffset");
				
			}

		} else {
			if(!mIsFullScreen)
			{
				onFullScreen();
			}
			nextSentence();
		}
	
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event); 
	}
}
