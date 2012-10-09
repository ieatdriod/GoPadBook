package com.gsimedia.gsiebook;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.taiwanmobile.common.ActionItem;
import com.taiwanmobile.common.QuickAction;
import com.taiwanmobile.myBook_PAD.R;
import com.gsimedia.common.GSiDataSource;
import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.gsiebook.lib.FinderSingleton;
import com.gsimedia.gsiebook.lib.GSiAnnotation;
import com.gsimedia.gsiebook.lib.GSiBookmark;
import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter;
import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter.GetLastPageResult;
import com.gsimedia.gsiebook.lib.FindResult;
import com.gsimedia.gsiebook.lib.MarkResult;
import com.gsimedia.gsiebook.lib.Outline;
import com.gsimedia.gsiebook.lib.PagesObserver;
import com.gsimedia.gsiebook.lib.PagesView;
import com.gsimedia.gsiebook.lib.RenderingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Document display activity.
 */
public class RendererActivity extends Activity implements PagesObserver {

	private final static String TAG = Config.LOGTAG;

	private PDF pdf = null;
	private PagesView pagesView = null;
	private PDFPagesProvider pdfPagesProvider = null;

	private MenuItem findTextMenuItem = null;
	private MenuItem clearFindTextMenuItem = null;
	private MenuItem markModeMenuItem = null;

	private RelativeLayout findButtonsLayout = null;
	private ImageButton findPrevButton = null;
	private ImageButton findNextButton = null;
	private ImageButton findHideButton = null;
	private ImageButton findALLButton = null;

	private Button goDetailButton = null;
	private Button goBackButton = null;

	private Button bookmarkColorButton = null;
	// 2011 05/25 Jonathan add to test bookmark
	private Button redPenButton = null;
	private Button yellowPenButton = null;
	private Button bluePenButton = null;

	private SeekBar pageSeekBar = null;

	private Button helpButton = null;
	private Button rotateButton = null;
	private Button switchButton = null;

	private TextView InfoTextViewTitle = null;
	private TextView InfoTextViewPage = null;
	// currently opened file path
	private String filePath = "/";
	private String iContentID = null;

	private String findText = null;
	private Integer currentFindResultPage = null;
	private Integer currentFindResultNumber = null;

	private RelativeLayout RelativeLayout_Title = null;
	private RelativeLayout RelativeLayout_Ctls = null;
	private RelativeLayout RelativeLayout_Info = null;
	
	private RelativeLayout RelativeLayout_Ctls_Colors = null;
	
	private boolean bControlEnabled = false;
	private boolean bColorEnabled = false;

	private GSiDatabaseAdapter iDatabase = null;

	private boolean bIsPreview = true;

	RelativeLayout iLastPage = null;
	TextView iLastPageMsg = null;
	Button iLastPageBtn = null;
	TextView iTitleView = null;
	RelativeLayout iFirstPageView = null;

	private ProgressDialog iProgressDialog = null;

	public static final String KEY_TOC = "TOC";
	public static final String KEY_Bookmark = "bookmark";
	public static final String KEY_Annotation = "annotation";
	public static final String KEY_Marker = "marker";
	public static final String KEY_BookID = "bookid";
	public static final String KEY_AuthType = "authtype";
	public static final String KEY_FirstPage = "showFirstPage";
	public static final String KEY_Fixed = "fixedOrientation";
	public static final String KEY_Switch = "pageSwitch";
	public static final String KEY_Find = "findresult";
	public static final String KEY_FindText = "findtext";

	// public static final int Result_Cancel= -1;
	public static final int Result_TOC = 0;
	public static final int Result_Bookmark = 1;
	public static final int Result_Marker = 2;
	public static final int Result_Annotation = 3;
	public static final int Result_Setting = 4;
	public static final int Result_Find = 5;

	public static final int Request_detail = 0;
	public static final int Request_annotation = 1;
	public static final int Request_setting = 2;
	public static final int Request_find = 3;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case Request_detail:
			int aPage = 0;
			switch (resultCode) {
			case Result_TOC:
				Outline aOutline = data.getExtras().getParcelable(KEY_TOC);
				int aPagenum = this.pdf.getLinkPage(aOutline);
				Log.d(Config.LOGTAG, "get outline " + aOutline.getTitle());
				aPage = aPagenum - 1;
				break;
			case Result_Bookmark:
				GSiBookmark aBookmark = data.getExtras().getParcelable(
						KEY_Bookmark);
				Log.d(Config.LOGTAG, "get bookmark " + aBookmark.iTitle);
				aPage = aBookmark.iPage.intValue();
				break;
			case Result_Marker:
				aPage = data.getExtras().getInt(KEY_Marker);
				break;
			case Result_Annotation:
				GSiAnnotation aAnn = data.getExtras().getParcelable(
						KEY_Annotation);
				Log.d(Config.LOGTAG, "get annotation " + aAnn.iAnnotation);
				aPage = aAnn.iPage.intValue();
				break;
			/*
			 * case Result_Cancel: this.iDatabase.open(); aPage =
			 * iDatabase.getLastPage
			 * (iDeliverID,false,RendererActivity.this,iDeliverID); break;
			 */
			}
			if (this.pdf != null) {
				gotoPage(aPage);
				this.iDatabase.open();
				this.iDatabase.setLastPage(this.iDeliverID, aPage, false,
						getApplicationContext(), this.iDeliverID, this.iBookToken);
			}
			break;
		case Request_annotation:
			String aNote = data.getExtras().getString(KEY_Annotation);
			this.iDatabase.open();
			this.addAnnotation(aNote);
			break;
		case Request_setting:
			switch (resultCode) {
			case Result_Setting:
				bResultToShowFirstPage = data.getExtras().getBoolean(
						KEY_FirstPage);
				this.bScreenFixed = !data.getExtras().getBoolean(KEY_Fixed);
				this.bLeftToNext = data.getExtras().getBoolean(KEY_Switch);
				pagesView.SetLeftToNext(bLeftToNext);
				
				int aMode = (bLeftToNext) ? GSiDatabaseAdapter.MODE_LEFT
						: GSiDatabaseAdapter.MODE_RIGHT;
				GSiDatabaseAdapter.setSwitchSetting(this, iDeliverID, aMode);

				this.savePreference();
				this.setFixedOrientation(this.bScreenFixed);
				break;
			}
			break;
		case Request_find:
			if (resultCode == Result_Find) {
				this.clearFind();
				FindResult aResult = data.getExtras().getParcelable(KEY_Find);
				String aFindText = data.getStringExtra(KEY_FindText);
				Log.d(Config.LOGTAG, aResult.toString());
				// go to find result
				this.gotoPage(aResult.page);
				this.findText = aFindText;
				this.newfind(aResult.numOfPage);
			}
			break;
		}
	}

	private boolean bResultToShowFirstPage = false;

	OpenPdfTask iOpenTask = null;

	/**
	 * Called when the activity is first created. TODO: initialize dialog fast,
	 * then move file loading to other thread TODO: add progress bar for file
	 * load TODO: add progress icon for file rendering
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e(Config.LOGTAG, "RendererActivity onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gsi_viewer);

		findViews();
		this.handleIntent(getIntent());
		this.restorePreference();

		this.pagesView.SetParentActivity(this);

		iDatabase = new GSiDatabaseAdapter(getApplicationContext());
		// async task to open pdf
		iOpenTask = new OpenPdfTask();
		iOpenTask.execute(this.iPathUri);

		// send keyboard events to this view
		pagesView.setFocusable(true);
		pagesView.setFocusableInTouchMode(true);

		registerForContextMenu(pagesView);		
	}

	/**
	 * find views
	 */
	private void findViews() {
		
		this.iFirstPageView = (RelativeLayout) findViewById(R.id.firstpage);
		this.iTitleView = (TextView) findViewById(R.id.gsimedia_title);
		this.iTitleView.requestFocus();
		this.iLastPage = (RelativeLayout) findViewById(R.id.lastpage);
		this.iLastPageMsg = (TextView) findViewById(R.id.msg);
		this.iLastPageBtn = (Button) findViewById(R.id.action);
		
		this.pageSeekBar = (SeekBar) findViewById(R.id.SeekBar);
		this.pageSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							int pagecount = pagesView.getPageCount() + 1;
							int curpage = progress * pagecount
									/ seekBar.getMax() + 1;
							curpage = (curpage > pagecount) ? pagecount
									: curpage;
							InfoTextViewPage.setText(String.valueOf(curpage)
									+ " / " + String.valueOf(pagecount));
						}
					}

					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						int pagecount = pagesView.getPageCount() + 1;
						int progress = seekBar.getProgress();
						int aTmp = seekBar.getMax();
						if (aTmp == 0)
							aTmp = 1;
						int curpage = progress * pagecount / aTmp;
						gotoPage(curpage);
					}

				});
		this.goDetailButton = (Button) findViewById(R.id.gsimedia_btn_title_right);
		this.goDetailButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ArrayList<Outline> aOutlines = getOutLine(null);
				Intent aNext = getIntent();// new Intent();
				aNext.setClass(RendererActivity.this, DetailActivity.class);
				Bundle aBundle = aNext.getExtras();
				aBundle.putParcelableArrayList(KEY_TOC, aOutlines);
				aBundle.putString(KEY_BookID, iDeliverID);
				aNext.putExtras(aBundle);
				startActivityForResult(aNext, Request_detail);
			}

		});
		this.goBackButton = (Button) findViewById(R.id.gsimedia_btn_title_left);
		this.goBackButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				exit();
			}

		});

		this.helpButton = (Button) findViewById(R.id.gsimedia_btn_help);
		this.helpButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				bResultToShowFirstPage = false;
				showFirstPage(true);
			}

		});

		this.rotateButton = (Button) findViewById(R.id.gsimedia_btn_rotate);
		this.rotateButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (bScreenFixed)
					bScreenFixed = false;
				else
					bScreenFixed = true;

				saveRotate(bScreenFixed);
				displayRotate();
			}
		});

		this.switchButton = (Button) findViewById(R.id.gsimedia_btn_switch);
		this.switchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (bLeftToNext)
					bLeftToNext = false;
				else
					bLeftToNext = true;

				pagesView.SetLeftToNext(bLeftToNext);
				saveSwitch(bLeftToNext);
				displaySwitch();
			}

		});


		this.bookmarkColorButton = (Button) findViewById(R.id.gsimedia_btn_bookmarkcolor);
		this.bookmarkColorButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				if (bColorEnabled) {
					enableColorPanel(false);
				} else {
					enableColorPanel(true);
				}
			}
		});

		this.findButtonsLayout = (RelativeLayout) findViewById(R.id.findButtonsLayout);
		this.findPrevButton = (ImageButton) findViewById(R.id.findPrevButton);
		this.findNextButton = (ImageButton) findViewById(R.id.findNextButton);
		this.findHideButton = (ImageButton) findViewById(R.id.findHideButton);
		this.findALLButton = (ImageButton) findViewById(R.id.findALLButton);
		this.setFindButtonHandlers();

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
		RelativeLayout aCtlSeekBar = (RelativeLayout) findViewById(R.id.RelativeLayout_SeekBar);
		aCtlSeekBar.setOnTouchListener(new OnTouchListener() {

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
		
		// 2011/05/23 Jonathan move page info to progressbar
		// this.RelativeLayout_Info =
		// (RelativeLayout)findViewById(R.id.RelativeLayout_Info);
		this.InfoTextViewTitle = (TextView) findViewById(R.id.TextView_Info_Title);
		this.InfoTextViewPage = (TextView) findViewById(R.id.TextView_Info_Page);

		this.iBookmarkView = (ImageView) findViewById(R.id.gsimedia_img_bookmark);
		this.iAnnotationView = (ImageView) findViewById(R.id.gsimedia_img_annotation);
		this.iAnnotationView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// show annotation
				Intent aNext = new Intent();
				Bundle aBundle = new Bundle();
				aBundle.putInt(AnnotationActivity.KEY_MODE,
						AnnotationActivity.ModeShow);
				String aNote = null;
				for (int i = 0; i < iAnnotations.size(); i++) {
					if (iAnnotations.get(i).iPage.intValue() == pagesView
							.getCurrentPage())
						aNote = iAnnotations.get(i).iAnnotation;
				}
				aBundle.putString(AnnotationActivity.KEY_NOTE, aNote);
				aNext.putExtras(aBundle);
				aNext.setClass(RendererActivity.this, AnnotationActivity.class);
				startActivityForResult(aNext, Request_annotation);
			}

		});

		this.pagesView = (com.gsimedia.gsiebook.lib.PagesView) findViewById(R.id.gsimedia_pdf_view);

	}

	/**
	 * handle intent
	 */
	private Uri iPathUri = null;
	private String iDeliverID = null;
	private boolean bSyncLastPage = false;
	private String iP12Path = null;
	private String iAuthor = null;
	private String iPublisher = null;
	private String iTitle = null;
	private boolean bBookVertical = false;
	private String iBookToken = null;
	private void handleIntent(Intent intent) {
		// file path
		iPathUri = intent.getData();

		filePath = iPathUri.getPath();
		iDeliverID = new File(filePath).getName();

		bSyncLastPage = intent.getBooleanExtra(Config.KEY_syncLastPage, false);

		if (iDeliverID.endsWith(Config.FILE_EXT)) {
			// if encrypted file
			iDeliverID = iDeliverID.replace(Config.FILE_EXT, "");
		} else {
			// not encrypted file
			bSyncLastPage = false;
			Log.e(Config.LOGTAG, "not " + Config.FILE_EXT
					+ " file, disable sync last page.");
		}

		// book id used only on buy URL
		iContentID = intent.getStringExtra(Config.KEY_content_id);

		// p12 path
		iP12Path = intent.getStringExtra(Config.KEY_p12);

		// isPreview
		String aIsPreview = intent.getStringExtra(Config.KEY_isSample);
		if (aIsPreview.equals("1"))
			this.bIsPreview = true;
		else
			this.bIsPreview = false;

		// book title
		String aTitle = intent.getStringExtra(Config.KEY_book_title);
		this.iTitleView.setText(aTitle);
		// for related link
		iTitle = aTitle;
		iAuthor = intent.getStringExtra(Config.KEY_book_authors);
		iPublisher = intent.getStringExtra(Config.KEY_book_publisher);
		// handle switch page
		// true:left to next, false:right to next
		bBookVertical = intent.getBooleanExtra(Config.KEY_book_vertical, false);
		
		iBookToken = intent.getStringExtra(Config.KEY_book_token);
		
		this.handlePageSwitchDirection();
	}

	/**
	 * bookmark related
	 */
	public boolean hasBookmark() {
		if (pdf == null)
			return false;
		if (iBookmarks == null)
			return false;
		int aCurPage = this.pagesView.getCurrentPage();
		boolean result = false;
		for (int i = 0; i < iBookmarks.size(); i++) {
			if (iBookmarks.get(i).iPage.intValue() == aCurPage) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void addBookmark() {
		int aCurPage = this.pagesView.getCurrentPage();
		/*
		 * ArrayList<String> TextFromPage = (ArrayList<String>)
		 * this.pdf.getTextFromPage(aCurPage, true); if (TextFromPage != null &&
		 * TextFromPage.size() > 0){
		 * Log.d(Config.LOGTAG,"Line from page: "+TextFromPage.get(0)); String
		 * bookmarkText = ""; for (int i=0;i<TextFromPage.size();i++)
		 * bookmarkText += TextFromPage.get(i)+" ";
		 * iDatabase.addBookmark(iDeliverID, new
		 * GSiBookmark(Integer.valueOf(aCurPage),bookmarkText)); }else{
		 * iDatabase.addBookmark(iDeliverID, new
		 * GSiBookmark(Integer.valueOf(aCurPage
		 * ),"bookmark of page "+(aCurPage+1))); }
		 */
		// 110126 modified by water: change request to display page number and
		// title only
		// String bookmarkText =
		// iTitleView.getText()+"  "+String.format(getString(R.string.GSI_BOOKMARK_TEXT),aCurPage+1);
		iDatabase.addBookmark(iDeliverID, new GSiBookmark(Integer
				.valueOf(aCurPage), ""));

		Log.d(Config.LOGTAG, "bookmark of " + aCurPage + " added");
		this.iBookmarks = iDatabase.getBookmarks(this.iDeliverID, this,
				iTitleView.getText().toString());
//		displayBookmark();
	}

	private void removeBookmark() {
		int aCurPage = this.pagesView.getCurrentPage();
		iDatabase.removeBookmark(iDeliverID, new GSiBookmark(Integer
				.valueOf(aCurPage), ""));
		Log.d(Config.LOGTAG, "bookmark of " + aCurPage + " removed");
		this.iBookmarks = iDatabase.getBookmarks(this.iDeliverID, this,
				iTitleView.getText().toString());
//		displayBookmark();
	}

	private void handleMark() {
		
		int bMarkerMode = this.pagesView.getMarkMode();
		boolean bHasMarker = this.pagesView.haseMarker();
		if (PagesView.MarkMode_None == bMarkerMode && bHasMarker == false) {
			if(!this.pagesView.setTextRegionFromPage()){
				Toast.makeText(this.getApplicationContext(),
						getResources().getString(R.string.iii_page_no_text),
						Toast.LENGTH_SHORT).show();
				return;
			}
			System.gc();
			enableControlPanel(false);
			Toast.makeText(this.getApplicationContext(),
					getResources().getString(R.string.GSI_MARK_ADD_NOTIFY),
					Toast.LENGTH_SHORT).show();
			this.pagesView.setMarkerMode(PagesView.MarkMode_Add);
		} else if (PagesView.MarkMode_None == bMarkerMode && bHasMarker == true) {
			enableControlPanel(false);
			// ask user what to do, add or del or cancel
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			AlertDialog dialog = builder.setTitle(
					getResources().getString(R.string.GSI_MARKER_OPT))
					.setCancelable(true).setNeutralButton(
							R.string.GSI_MARKER_DELETE,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Toast
											.makeText(
													getApplicationContext(),
													getResources()
															.getString(
																	R.string.GSI_MARK_DEL_NOTIFY),
													Toast.LENGTH_SHORT).show();
									pagesView
											.setMarkerMode(PagesView.MarkMode_Del);
									dialog.dismiss();
								}
							}).setPositiveButton(R.string.GSI_MARKER_MARK,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if(!RendererActivity.this.pagesView.setTextRegionFromPage()){
										Toast.makeText(getApplicationContext(),
												getResources().getString(R.string.iii_page_no_text),
												Toast.LENGTH_SHORT).show();
										return;									
									}
									System.gc();
									Toast.makeText(
													getApplicationContext(),
													getResources()
															.getString(
																	R.string.GSI_MARK_ADD_NOTIFY),
													Toast.LENGTH_SHORT).show();
									pagesView
											.setMarkerMode(PagesView.MarkMode_Add);
									dialog.dismiss();
								}
							}).setNegativeButton(R.string.GSI_MARKER_CANCEL,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									pagesView
											.setMarkerMode(PagesView.MarkMode_None);
									dialog.dismiss();
								}
							}).create();
			dialog.show();
		}
	}

	/**
	 * annotation related
	 */
	public boolean hasAnnotation() {
		if (pdf == null)
			return false;
		if (iAnnotations == null)
			return false;
		int aCurPage = this.pagesView.getCurrentPage();
		boolean result = false;
		for (int i = 0; i < iAnnotations.size(); i++) {
			if (iAnnotations.get(i).iPage.intValue() == aCurPage) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void addAnnotation(String aNote) {
		int aCurPage = this.pagesView.getCurrentPage();
		iDatabase.addAnnotation(iDeliverID, new GSiAnnotation(Integer
				.valueOf(aCurPage), aNote));
		Log.d(Config.LOGTAG, "annotation of " + aCurPage + " added");
		this.iAnnotations = iDatabase.getAnnotations(this.iDeliverID);
//		this.displayAnnotation();
		this.pagesView.invalidate();
	}

	private void removeAnnotation() {
		int aCurPage = this.pagesView.getCurrentPage();
		iDatabase.removeAnnotation(iDeliverID, new GSiAnnotation(Integer
				.valueOf(aCurPage), ""));
		Log.d(Config.LOGTAG, "annotation of " + aCurPage + " removed");
		this.iAnnotations = iDatabase.getAnnotations(this.iDeliverID);
//		this.displayAnnotation();
		this.pagesView.invalidate();
	}

	public boolean isControlPanelOn(){
		return bControlEnabled;
	}
	/**
	 * Save the current page before exiting
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (iProgressDialog != null) {
			iProgressDialog.dismiss();
			iProgressDialog = null;
		}
		if (this.pdf != null && pdfPagesProvider != null) {
			int aLastPage = 0;
			if (this.bIsLastPageVisible)
				aLastPage = this.pdfPagesProvider.getPageCount();
			else
				aLastPage = this.pagesView.getCurrentPage();
			iDatabase.setLastPage(iDeliverID, aLastPage, false,
					getApplicationContext(), this.iDeliverID, this.iBookToken);

		}
		iDatabase.close();
		if (pdfPagesProvider != null) {
			this.pdfPagesProvider.StopRendering();
		}
		if (this.isFinishing()) {
			if (iOpenTask.getStatus() != AsyncTask.Status.FINISHED)
				iOpenTask.cancel(false);
			if (pdfPagesProvider != null) {
				Log.e(Config.LOGTAG, "clear RendererListener");
				this.pdfPagesProvider.clearCache();
				this.pdfPagesProvider.setOnImageRenderedListener(null);
			}
			if (this.pdf != null) {
				pdf.recycle();
				pdf = null;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// initialize settings
		if (this.pdf != null) {
			iDatabase.open();
			iBookmarks = iDatabase.getBookmarks(iDeliverID,
					RendererActivity.this, iTitleView.getText().toString());
			iAnnotations = iDatabase.getAnnotations(iDeliverID);			
			this.updateMarkers(true);

			if (bResultToShowFirstPage) {
				bResultToShowFirstPage = false;
				showFirstPage(true);
			}

		}

	}

	private ImageView iBookmarkView = null;
	private ImageView iAnnotationView = null;
	private ArrayList<GSiBookmark> iBookmarks = null;
	private ArrayList<GSiAnnotation> iAnnotations = null;
	private ArrayList<MarkResult> iMarkResults = null;

	/*
	private void displayBookmark() {
		if (this.hasBookmark()) {
			this.bookmarkButton
					.setBackgroundResource(R.drawable.gsi_button05_1_btn);
			iBookmarkView.setVisibility(View.VISIBLE);
		} else {
			this.bookmarkButton
					.setBackgroundResource(R.drawable.gsi_button05_btn);
			iBookmarkView.setVisibility(View.INVISIBLE);
		}
	}
	private void displayAnnotation() {
		if (this.hasAnnotation()) {
			iAnnotationView.setVisibility(View.VISIBLE);
			this.annotationButton
					.setBackgroundResource(R.drawable.gsi_button07_1_btn);
		} else {
			iAnnotationView.setVisibility(View.INVISIBLE);
			this.annotationButton
					.setBackgroundResource(R.drawable.gsi_button07_btn);
		}
	}
	*/
	
	private void displayRotate() {
		if (bScreenFixed) {
			this.rotateButton.setBackgroundResource(R.drawable.gsi_button21_btn);
		} else {			
			this.rotateButton.setBackgroundResource(R.drawable.gsi_button20_btn);
		}
	}
	
	private void displaySwitch() {
		if (bLeftToNext) {
			this.switchButton.setBackgroundResource(R.drawable.gsi_button23_btn);
		} else {			
			this.switchButton.setBackgroundResource(R.drawable.gsi_button22_btn);
		}
	}

	/**
	 * Set handlers on findNextButton and findHideButton.
	 */
	private void setFindButtonHandlers() {
		this.findPrevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RendererActivity.this.findPrev();
			}
		});
		this.findNextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RendererActivity.this.findNext();
			}
		});
		this.findHideButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RendererActivity.this.findHide();
			}
		});
		this.findALLButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				goFindActivity(findText);
			}
		});
	}

	private void goFindActivity(String aFindText) {
		Intent aNext = new Intent();
		aNext.setClass(RendererActivity.this, FindResultActivity.class);
		aNext.putExtra(FindResultActivity.KEY_FindText, aFindText);
		startActivityForResult(aNext, Request_find);
	}

	/**
	 * Return PDF instance wrapping file referenced by Intent. Currently reads
	 * all bytes to memory, in future local files should be passed to native
	 * code and remote ones should be downloaded to local tmp dir.
	 * 
	 * @return error message string got pdf instance assign to this.pdf
	 */
	private String getPDF(Uri aUri) {
		Uri uri = aUri;
		if (uri.getScheme().equals(Config.FILESCHEME)) {
			if (filePath.endsWith(Config.FILE_EXT)) {
				int result = (int) GSiDataSource.setPath(filePath, iP12Path,
						this.getApplicationContext());
				if (result < 0) {
					int aMsg = 0;
					switch (result) {
					case GSiDataSource.EFileStatus_Error1:
						aMsg = R.string.GSI_FILE_ERROR_1;
						break;
					case GSiDataSource.EFileStatus_Error2:
						aMsg = R.string.GSI_FILE_ERROR_2;
						break;
					case GSiDataSource.EFileStatus_Error3:
						aMsg = R.string.GSI_FILE_ERROR_3;
						break;
					case GSiDataSource.EFileStatus_Error4:
						aMsg = R.string.GSI_FILE_ERROR_4;
						break;
					case GSiDataSource.EFileStatus_Error5:
						aMsg = R.string.GSI_DEVICE_ID_EMPTY_MSG;
						break;
					}
					pdf = null;
					return getString(aMsg);
				}

				// dump file content
				/*
				 * dump data InputStream is = GSiDataSource.GetInputStream();
				 * 
				 * File aFile = new File(filePath+".dump");
				 * 
				 * aFile.deleteOnExit(); try { aFile.createNewFile(); } catch
				 * (IOException e2) { e2.printStackTrace(); }
				 * 
				 * OutputStream outputStream = null; try { outputStream = new
				 * FileOutputStream (aFile); } catch (FileNotFoundException e1)
				 * { e1.printStackTrace(); }
				 * 
				 * byte [] abuffer = new byte[4096]; try { int ret = 0; do { ret
				 * = is.read(abuffer); if (ret > 0)
				 * outputStream.write(abuffer,0,ret); }while (ret > 0);
				 * outputStream.flush(); outputStream.close(); } catch
				 * (IOException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */

				pdf = new PDF(GSiDataSource.GetInputStream());
				return null;
			} else {
				pdf = new PDF(new File(filePath));
				return null;
			}
		} else if (uri.getScheme().equals("content")) {
			ContentResolver cr = this.getContentResolver();
			FileDescriptor fileDescriptor;
			try {
				fileDescriptor = cr.openFileDescriptor(uri, "r")
						.getFileDescriptor();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e); // TODO: handle errors
			}
			pdf = new PDF(fileDescriptor);
			return null;
		} else {
			throw new RuntimeException("don't know how to get filename from "
					+ uri);
		}
	}

	/**
	 * Handle menu.
	 * 
	 * @param menuItem
	 *            selected menu item
	 * @return true if menu item was handled
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem == this.findTextMenuItem) {
			this.showFindDialog();
		} else if (menuItem == this.clearFindTextMenuItem) {
			this.clearFind();
		} else if (menuItem == this.markModeMenuItem) {
			int bMarkerMode = this.pagesView.getMarkMode();
			boolean bHasMarker = this.pagesView.haseMarker();
			if (PagesView.MarkMode_None == bMarkerMode && bHasMarker == false)
				this.pagesView.setMarkerMode(PagesView.MarkMode_Add);
			else if (PagesView.MarkMode_None == bMarkerMode
					&& bHasMarker == true) {
				// ask user what to do, add or del or cancel
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				AlertDialog dialog = builder.setTitle(
						getResources().getString(R.string.GSI_MARKER_OPT))
						.setCancelable(true).setNeutralButton(
								R.string.GSI_MARKER_DELETE,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										pagesView
												.setMarkerMode(PagesView.MarkMode_Del);
										dialog.dismiss();
									}
								}).setPositiveButton(R.string.GSI_MARKER_MARK,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										pagesView
												.setMarkerMode(PagesView.MarkMode_Add);
										dialog.dismiss();
									}
								}).setNegativeButton(
								R.string.GSI_MARKER_CANCEL,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										pagesView
												.setMarkerMode(PagesView.MarkMode_None);
										dialog.dismiss();
									}
								}).create();
				dialog.show();
			}
		}
		return false;
	}

	private void clearFind() {
		this.currentFindResultPage = null;
		this.currentFindResultNumber = null;
		this.pagesView.setFindMode(false);
		this.findButtonsLayout.setVisibility(View.GONE);
	}

	private ArrayList<Outline> getOutLine(Outline root) {
		if (pdf == null)
			return null;
		ArrayList<Outline> retrivedOutLine = (ArrayList<Outline>) this.pdf
				.getOutLine(root);
		ArrayList<Outline> retOutLine = new ArrayList<Outline>();
		if (root != null) {
			retOutLine.add(root);
		}
		if (retrivedOutLine != null) {
			int aSize = retrivedOutLine.size();
			for (int i = 0; i < aSize; i++) {
				ArrayList<Outline> aOutlines = getOutLine(retrivedOutLine
						.get(i));
				if (aOutlines != null)
					retOutLine.addAll(aOutlines);
			}
			retrivedOutLine.clear();
		}
		return retOutLine;
	}

	/**
	 * Called after submitting go to page dialog.
	 * 
	 * @param page
	 *            page number, 0-based
	 */
	private int iCurPage = 0;

	private void gotoPage(int page) {
		Log.i(TAG, "rewind to page " + page);
		int aTotalPageCount = this.pagesView.getPageCount();
		if (aTotalPageCount <= 0) {
			Log.e(Config.LOGTAG, "page count = " + aTotalPageCount);
			return;
		}
		iCurPage = page;
		this.showFirstPage(false);
		if (page >= aTotalPageCount) {
			this.showLastPage(true);
			this.pagesView.scrollToPage(aTotalPageCount - 1);// jump to last pdf
		} else {
			if (bIsLastPageVisible && (page == aTotalPageCount - 2))
				this.showLastPage(false);
			else {
				this.showLastPage(false);
				if (this.pagesView != null) {
					this.pagesView.scrollToPage(page);
					final int pagecount = aTotalPageCount;
					final int curpage = this.pagesView.getCurrentPage() + 1;
					InfoTextViewPage.setText(String.valueOf(curpage) + " / "
							+ String.valueOf(pagecount + 1));
					if (pagesView.getFindMode())
						this.newfind(0);
				}
			}
		}
	}

	/**
	 * Create options menu, called by Android system.
	 * 
	 * @param menu
	 *            menu to populate
	 * @return true meaning that menu was populated
	 */
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * super.onCreateOptionsMenu(menu); // this.gotoPageMenuItem =
	 * menu.add(R.string.goto_page); // this.rotateRightMenuItem =
	 * menu.add(R.string.rotate_page_left); // this.rotateLeftMenuItem =
	 * menu.add(R.string.rotate_page_right); this.findTextMenuItem =
	 * menu.add(R.string.find_text); this.clearFindTextMenuItem =
	 * menu.add(R.string.clear_find_text); this.markModeMenuItem =
	 * menu.add(R.string.menu_item_mark_label); // this.getOutLineMenuItem =
	 * menu.add(R.string.get_outline); // this.aboutMenuItem =
	 * menu.add(R.string.about); return true; }
	 */

	/**
	 * Prepare menu contents. Hide or show "Clear find results" menu item
	 * depending on whether we're in find mode.
	 * 
	 * @param menu
	 *            menu that should be prepared
	 */
	/*
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) {
	 * super.onPrepareOptionsMenu(menu); if(pagesView!=null)
	 * this.clearFindTextMenuItem.setVisible(this.pagesView.getFindMode());
	 * return true; }
	 */

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i(TAG, "onConfigurationChanged(" + newConfig + ")");
	}

	/**
	 * Show find dialog. Very pretty UI code ;)
	 */
	private void showFindDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		final InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		alert.setView(input);
		alert.setIcon(android.R.drawable.ic_dialog_info);
		alert.setTitle(R.string.GSI_SEARCH_DIALOG_TITLE);
		alert.setPositiveButton(R.string.GSI_OK,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// perform search
						findText = input.getText().toString();
						findText(findText);
						// close control bar
						enableControlPanel(false);
						mgr.toggleSoftInput(
								InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					}
				});

		alert.setNegativeButton(R.string.GSI_CANCEL,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// do nothing
						dialog.cancel();
						mgr.toggleSoftInput(
								InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					}
				});
		alert.show();
		/**
		 * open soft-keyboard
		 */
		mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	private void findText(String text) {
		if (text.trim().length() > 0) {
			Log.d(TAG, "findText(" + text + ")");
			this.findText = text;
			this.find(Finder.Mode_Forward);
		} else {
			Toast.makeText(getApplicationContext(),
					getString(R.string.GSI_SEARCH_NOTEXT_HINT),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Called when user presses "next" button in find panel.
	 */
	private void findNext() {
		this.find(Finder.Mode_Forward);
	}

	/**
	 * Called when user presses "prev" button in find panel.
	 */
	private void findPrev() {
		this.find(Finder.Mode_Backward);
	}

	/**
	 * Called when user presses hide button in find panel.
	 */
	private void findHide() {
		if (this.pagesView != null)
			this.pagesView.setFindMode(false);
		this.currentFindResultNumber = null;
		this.currentFindResultPage = null;
		this.findButtonsLayout.setVisibility(View.GONE);
	}

	/**
	 * Helper class that handles search progress, search cancelling etc.
	 */
	static class Finder implements Runnable, DialogInterface.OnCancelListener,
			DialogInterface.OnClickListener {
		public static final int Mode_inPage = 0;
		public static final int Mode_Forward = 1;
		public static final int Mode_Backward = 2;

		private RendererActivity parent = null;
		// private boolean forward;
		private int iMode = Mode_inPage;
		private AlertDialog dialog = null;
		private String text;
		private int startingPage;
		private int pageCount;
		private boolean cancelled = false;

		/**
		 * Constructor for finder.
		 * 
		 * @param parent
		 *            parent activity
		 */
		public Finder(RendererActivity parent, int aMode) {
			this.parent = parent;
			this.iMode = aMode;
			this.text = parent.findText;
			this.pageCount = parent.pagesView.getPageCount();
			if (parent.currentFindResultPage != null) {
				if (aMode == Mode_Forward) {
					// this.startingPage = (parent.currentFindResultPage + 1) %
					// pageCount;
					this.startingPage = parent.currentFindResultPage + 1;
				} else if (aMode == Mode_Backward) {
					// this.startingPage = (parent.currentFindResultPage - 1 +
					// pageCount) % pageCount;
					this.startingPage = parent.currentFindResultPage - 1;
				}
			} else {
				this.startingPage = parent.iCurPage;
			}
		}

		public void setDialog(AlertDialog dialog) {
			this.dialog = dialog;
		}

		public void run() {
			int page = -1;
			this.cancelled = false;
			this.createAndShowDialog();
			if (iMode == Mode_inPage) {
				page = this.startingPage;
				List<FindResult> findResults = this.findOnPage(page);
				if (findResults != null && !findResults.isEmpty()) {
					Log.d(TAG, "found something at page " + page + ": "
							+ findResults.size() + " results");
					this.dismissDialog();
					this.showFindResults(findResults, page);
					return;
				}
			} else {
				/*
				 * for(int i = 0; i < this.pageCount; ++i) { boolean forward =
				 * (iMode == Mode_Forward)?true:false; if (this.cancelled) {
				 * this.dismissDialog(); return; } page = (startingPage +
				 * pageCount + (forward ? i : -i)) % this.pageCount; Log.d(TAG,
				 * "searching on " + page); this.updateDialog(page);
				 * List<FindResult> findResults = this.findOnPage(page); if
				 * (findResults != null && !findResults.isEmpty()) { Log.d(TAG,
				 * "found something at page " + page + ": " + findResults.size()
				 * + " results"); this.dismissDialog();
				 * this.showFindResults(findResults, page); return; } }
				 */
				int aSearchRange = (iMode == Mode_Backward) ? startingPage
						: pageCount - startingPage;
				for (int i = 0; i <= aSearchRange; ++i) {
					boolean forward = (iMode == Mode_Backward) ? false : true;
					if (this.cancelled) {
						this.dismissDialog();
						return;
					}
					page = (startingPage + (forward ? i : -i));
					if (page >= this.pageCount || page < 0)
						break;
					Log.d(TAG, "searching on " + page);
					this.updateDialog(page + 1);
					List<FindResult> findResults = this.findOnPage(page);
					if (findResults != null && !findResults.isEmpty()) {
						Log.d(TAG, "found something at page " + page + 1 + ": "
								+ findResults.size() + " results");
						this.dismissDialog();
						this.showFindResults(findResults, page);
						return;
					}
				}
				this.dismissDialog();
				if (iMode != Finder.Mode_inPage) {
					this.parent.runOnUiThread(new Runnable() {

						public void run() {
							Toast.makeText(parent.getApplicationContext(),
									R.string.GSI_SEARCH_NOT_FOUND,
									Toast.LENGTH_SHORT).show();
						}
					});
				}

			}
			/* TODO: show "nothing found" message */
			this.dismissDialog();
		}

		/**
		 * Called by finder thread to get find results for given page. Routed to
		 * PDF instance. If result is not empty, then finder loop breaks,
		 * current find position is saved and find results are displayed.
		 * 
		 * @param page
		 *            page to search on
		 * @return results
		 */
		private List<FindResult> findOnPage(int page) {
			if (this.text == null)
				throw new IllegalStateException("text cannot be null");
			return this.parent.pdf.find(this.text, page);
		}

		private void createAndShowDialog() {
			this.parent.runOnUiThread(new Runnable() {
				public void run() {
					if (iMode != Finder.Mode_inPage) {
						if (Finder.this.dialog == null) {
							String title = Finder.this.parent.getString(
									R.string.GSI_SEARCH_FOR).replace("%1$s",
									Finder.this.text);
							// String message =
							// Finder.this.parent.getString(R.string.page_of).replace("%1",
							// String.valueOf(Finder.this.startingPage)).replace("%2",
							// String.valueOf(pageCount));
							String message = Finder.this.parent.getString(
									R.string.GSI_SEARCHING_ON).replace("%1$s",
									String.valueOf(Finder.this.startingPage));
							AlertDialog.Builder builder = new AlertDialog.Builder(
									Finder.this.parent);
							AlertDialog dialog = builder.setTitle(title)
									.setMessage(message).setCancelable(true)
									.setNegativeButton(R.string.GSI_CANCEL,
											Finder.this).create();
							dialog.setOnCancelListener(Finder.this);
							Finder.this.dialog = dialog;
							Finder.this.dialog.show();
							Log.d(TAG, "setting dialog");
						}
					}
				}
			});
		}

		public void updateDialog(final int page) {
			this.parent.runOnUiThread(new Runnable() {
				public void run() {
					// String message =
					// "page_of";//Finder.this.parent.getString(R.string.page_of).replace("%1",
					// String.valueOf(page)).replace("%2",
					// String.valueOf(pageCount));
					if (iMode != Finder.Mode_inPage) {
						String message = Finder.this.parent.getString(
								R.string.GSI_SEARCHING_ON).replace("%1$s",
								String.valueOf(page));
						if (Finder.this.dialog != null)
							Finder.this.dialog.setMessage(message);
					}
				}
			});
		}

		public void dismissDialog() {
			Log.d(TAG, "dismissDialog");
			Finder.this.parent.iFinder = null;
			// final AlertDialog dialog = Finder.this.dialog;
			this.parent.runOnUiThread(new Runnable() {
				public void run() {
					if (null != Finder.this.dialog) {
						Log.d(TAG, "dismissDialog not null");
						Finder.this.dialog.dismiss();
					}
					Log.d(TAG, "dismissDialog null");
				}
			});
		}

		public void onCancel(DialogInterface dialog) {
			Log.d(TAG, "onCancel(" + dialog + ")");
			this.cancelled = true;
			Finder.this.parent.iFinder = null;
		}

		public void onClick(DialogInterface dialog, int which) {
			Log.d(TAG, "onClick(" + dialog + ")");
			this.cancelled = true;
			Finder.this.parent.iFinder = null;
		}

		private void showFindResults(final List<FindResult> findResults,
				final int page) {
			this.parent.runOnUiThread(new Runnable() {
				public void run() {
					Log.d(Config.LOGTAG, "show find results");
					int fn = (Finder.this.iMode == Finder.Mode_Backward) ? findResults
							.size() - 1
							: 0;
					Finder.this.parent.currentFindResultPage = page;
					if (Finder.this.iMode != Finder.Mode_inPage)
						Finder.this.parent.currentFindResultNumber = fn;
					Finder.this.parent.pagesView.setFindResults(findResults);
					Finder.this.parent.pagesView.setFindMode(true);
					Finder.this.parent.pagesView
							.scrollToFindResult(Finder.this.parent.currentFindResultNumber
									.intValue());					
					Finder.this.parent.findButtonsLayout
							.setVisibility(View.VISIBLE);
					Finder.this.parent.pagesView.invalidate();
				}
			});
		}
	};

	/**
	 * GUI for finding text. Used both on initial search and for "next" and
	 * "prev" searches. Displays dialog, handles cancel button, hides dialog as
	 * soon as something is found.
	 * 
	 * @param
	 */
	private Finder iFinder = null;

	private void find(int aMode) {
		if (iFinder == null) {
			if (this.currentFindResultPage != null) {
				/* searching again */
				int nextResultNum = (aMode == Finder.Mode_Forward) ? this.currentFindResultNumber + 1
						: this.currentFindResultNumber - 1;
				if (nextResultNum >= 0
						&& nextResultNum < this.pagesView.getFindResults()
								.size()) {
					/*
					 * no need to really find - just focus on given result and
					 * exit
					 */
					this.currentFindResultNumber = nextResultNum;
					this.pagesView.scrollToFindResult(nextResultNum);					
					this.pagesView.invalidate();
					return;
				}
			}

			/* finder handles next/prev and initial search by itself */
			iFinder = new Finder(this, aMode);
			Thread finderThread = new Thread(iFinder);
			finderThread.start();
		} else {
			Log.e(Config.LOGTAG, "finder not null!");
		}
	}

	private void newfind(int num) {
		this.currentFindResultPage = null;
		this.currentFindResultNumber = Integer.valueOf(num);
		pagesView.setFindResults(null);
		/* finder handles next/prev and initial search by itself */
		iFinder = new Finder(this, Finder.Mode_inPage);
		Thread finderThread = new Thread(iFinder);
		finderThread.start();
	}

	private void enableControlPanel(boolean aEnable) {
		if (aEnable) {
			final int pagecount = this.pagesView.getPageCount();
			final int curpage = this.pagesView.getCurrentPage() + 1;
			if (this.bIsLastPageVisible)
				InfoTextViewPage.setText(String.valueOf(curpage + 1) + " / "
						+ String.valueOf(pagecount + 1));
			else
				InfoTextViewPage.setText(String.valueOf(curpage) + " / "
						+ String.valueOf(pagecount + 1));
			RelativeLayout_Title.setVisibility(View.VISIBLE);
			RelativeLayout_Ctls.setVisibility(View.VISIBLE);
			// 2011/05/23 Jonathan move page info to progressbar
			// RelativeLayout_Info.setVisibility(View.VISIBLE);
			bControlEnabled = true;
			this.iTitleView.requestFocus();
		} else {
			RelativeLayout_Title.setVisibility(View.INVISIBLE);
			RelativeLayout_Ctls.setVisibility(View.INVISIBLE);
			enableColorPanel(false);
			// 2011/05/23 Jonathan move page info to progressbar
			// RelativeLayout_Info.setVisibility(View.INVISIBLE);
			bControlEnabled = false;
		}
	}
	
	private void enableColorPanel(boolean aEnable) {
		if (aEnable) {		
			RelativeLayout ctl_color = (RelativeLayout) findViewById(R.id.ctl_color);
			ctl_color.removeAllViews();
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			
			SharedPreferences settings = getSharedPreferences(KEY_Pref, 0);
			String currentValue = settings.getString("reader_setting_crossed_color_value", "");
						
			for(int i = 0; i < temp.length; i++){
				View root = (ViewGroup) inflater.inflate(R.layout.color_item, null);
				RelativeLayout colorItem_RL = (RelativeLayout) root.findViewById(R.id.Color_item_relativelayout);
				colorItem_RL.setId(i+1);
				colorItem_RL.setTag(temp[i]);
				colorItem_RL.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						SharedPreferences settings = getSharedPreferences(KEY_Pref, 0);
						settings.edit().putString("reader_setting_crossed_color_value", String.valueOf(arg0.getTag())).commit();
						//pagesView.setMarkerColor(getApplicationContext());
						pagesView.setMarkerBitmap(getApplicationContext());
						
						enableColorPanel(true);
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
			
			this.bookmarkColorButton.setBackgroundResource(R.drawable.gsi_button25);
			RelativeLayout_Ctls_Colors.setVisibility(View.VISIBLE);
			bColorEnabled = true;
		} else {
			this.bookmarkColorButton.setBackgroundResource(R.drawable.gsi_button24_btn);
			RelativeLayout_Ctls_Colors.setVisibility(View.INVISIBLE);
			bColorEnabled = false;
		}
	}

	public void toggleControl(int aMode) {
		switch (aMode) {
		case PagesObserver.MODE_MIDDLE:
			if (!this.pagesView.getFindMode()) {
				if (bControlEnabled) {
					enableControlPanel(false);
				} else {
					enableControlPanel(true);
				}
			}
			break;
		case PagesObserver.MODE_RIGHT:
			if(bControlEnabled)
			{
				enableControlPanel(false);
			}
			if (bLeftToNext)
				gotoPage(pagesView.getCurrentPage() - 1);
			else
				gotoPage(pagesView.getCurrentPage() + 1);
			break;
		case PagesObserver.MODE_LEFT:
			if(bControlEnabled)
			{
				enableControlPanel(false);
			}
			if (bLeftToNext)
				gotoPage(pagesView.getCurrentPage() + 1);
			else
				gotoPage(pagesView.getCurrentPage() - 1);
			break;
		case PagesObserver.MODE_BOOKMARK:
			if (bControlEnabled)
				break;
			if(this.hasBookmark()){
				this.removeBookmark();
			}else{
				this.addBookmark();
			}
			break;
		case PagesObserver.MODE_NOTE:
			// show annotation
			Intent aNext = new Intent();
			Bundle aBundle = new Bundle();
			aBundle.putInt(AnnotationActivity.KEY_MODE,
					AnnotationActivity.ModeShow);
			String aNote = null;
			for (int i = 0; i < iAnnotations.size(); i++) {
				if (iAnnotations.get(i).iPage.intValue() == pagesView
						.getCurrentPage())
					aNote = iAnnotations.get(i).iAnnotation;
			}
			aBundle.putString(AnnotationActivity.KEY_NOTE, aNote);
			aNext.putExtras(aBundle);
			aNext.setClass(RendererActivity.this, AnnotationActivity.class);
			startActivityForResult(aNext, Request_annotation);
			break;
		}
	}

	public void onRenderingException(RenderingException reason) {
		if (iProgressDialog != null) {
			iProgressDialog.dismiss();
			iProgressDialog = null;
		}
	}

	public void onRenderingProgressStart() {
		/*
		 * Log.d("Lancelot", "Progress Start");
		 */
		if (iProgressDialog == null) {
			iProgressDialog = ProgressDialog.show(this, "", this
					.getText(R.string.GSI_PROGRESS_RENDERING), true, true);
		}
		else{/*iProgressDialog could be dismiss by system if it is cancelable*/
			if(!iProgressDialog.isShowing()){				
				iProgressDialog.dismiss();
				iProgressDialog = null;		
			}			
		}
	}

	
	public void onRenderingProgressEnd() {
		/*
		 * Log.d("Lancelot", "Progress End");
		 */
		if (iProgressDialog != null) {
			iProgressDialog.dismiss();
			iProgressDialog = null;
		}			
		
		this.updateMarkers(false);
	}
	
	private int oldPage = -1;
	private void updateMarkers(boolean isForceUpdate){
		int currentPage = pagesView.getCurrentPage();
		if(!isForceUpdate){			
			if(oldPage!=currentPage){
				oldPage = currentPage;
				iMarkResults = iDatabase.getMarkers(iDeliverID,currentPage);			
				pagesView.setMarkResults(iMarkResults);
			}
		}else{
			oldPage = currentPage;
			iMarkResults = iDatabase.getMarkers(iDeliverID,currentPage);			
			pagesView.setMarkResults(iMarkResults);
		}
	}
	public void onMarkerAdded(MarkResult aMarkResult) {
		if (iDatabase == null || aMarkResult == null)
			return;
		int aMaxRegion = iDatabase.getMaxRegionGroupID(this.iDeliverID)+1;
		iDatabase.addMarker(iDeliverID, aMarkResult, aMaxRegion);

		Log.d(Config.LOGTAG, "marker of " + aMarkResult.page + " added");
		
                /*wait too long if markers growing, direct add marker from caller*/
		//this.iMarkResults = iDatabase.getMarkers(this.iDeliverID);
		

		// pagesView.setMarkResults(iMarkResults);
	}

	private MarkResult aMarkerDelete = null;

	public void onMarkerDeleted(MarkResult aMarkResult) {
		if (iDatabase == null || aMarkResult == null)
			return;
		// ASK USER delete or not
		aMarkerDelete = aMarkResult;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		AlertDialog dialog = builder.setTitle(
				getResources().getString(R.string.GSI_MARKER_OPT))
				.setCancelable(true).setNeutralButton(
						R.string.GSI_MARKER_DELETE,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								iDatabase.removeMarker(iDeliverID,
										aMarkerDelete);

								Log.d(Config.LOGTAG, "marker of "
										+ aMarkerDelete.page + " added");

                                /*wait too long if markers growing, direct remove marker*/
								//iMarkResults = iDatabase.getMarkers(iDeliverID);
								iMarkResults.remove(aMarkerDelete);
								pagesView.invalidate();
								aMarkerDelete = null;
								// pagesView.setMarkResults(iMarkResults);
								dialog.dismiss();
							}
						}).setNegativeButton(R.string.GSI_MARKER_CANCEL,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								//iMarkResults = iDatabase.getMarkers(iDeliverID);
								updateMarkers(true);
								aMarkerDelete = null;
								//pagesView.setMarkResults(iMarkResults);
								dialog.dismiss();
							}
						}).create();
		dialog.show();
	}

	public void onPageChange(int aNewPage, int totalpage) {
		if (!bIsLastPageVisible) {
			InfoTextViewPage.setText(String.valueOf(aNewPage) + " / "
					+ String.valueOf(totalpage + 1));
			this.pageSeekBar.setMax(totalpage);
			this.pageSeekBar.setProgress(aNewPage - 1);
//			displayBookmark();
//			displayAnnotation();
			displayRotate();
			displaySwitch();
		}
	}

	public void onMarkerUpToLimit(){	
		Toast.makeText(
				getApplicationContext(),
				getApplicationContext().getString(
						R.string.GSI_MARKER_UP_TO_LIMIT),
				Toast.LENGTH_SHORT).show();		
	}
	
	private boolean bIsLastPageVisible = false;

	private void showLastPage(boolean aShow) {
		if (aShow) {
			bIsLastPageVisible = true;
			int aCnt = this.pagesView.getPageCount() + 1;
			InfoTextViewPage.setText(String.valueOf(aCnt) + " / "
					+ String.valueOf(aCnt));
			this.pageSeekBar.setMax(aCnt - 1);
			this.pageSeekBar.setProgress(aCnt - 1);
			iBookmarkView.setVisibility(View.INVISIBLE);

			iAnnotationView.setVisibility(View.INVISIBLE);
			
			// button go to URL
			if (this.bIsPreview) {
				iLastPageMsg.setText(R.string.GSI_PREVIEW_MSG);
				iLastPageBtn.setBackgroundResource(R.drawable.gsi_trial01_btn);
			} else {
				iLastPageMsg.setText(R.string.GSI_THANKS_MSG);
				iLastPageBtn.setBackgroundResource(R.drawable.gsi_trial02_btn);
			}
			iLastPageBtn.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					openWeb();
				}

			});
			this.pagesView.enableTouchAction(false);
			iLastPage.setVisibility(View.VISIBLE);
		} else {
			bIsLastPageVisible = false;
			int aCnt = this.pagesView.getPageCount() + 1;
			InfoTextViewPage.setText(String.valueOf(aCnt - 1) + " / "
					+ String.valueOf(aCnt));
			this.pageSeekBar.setMax(aCnt - 1);
			this.pageSeekBar.setProgress(aCnt - 2);
			iLastPage.setVisibility(View.GONE);
//			displayBookmark();
//			displayAnnotation();
			displayRotate();
			displaySwitch();
			this.pagesView.enableTouchAction(true);
		}
	}

	private void openWeb() {
		String url = null;
		if (bIsPreview)
			url = getString(R.string.web_api_buy) + iContentID;
		else {
			url = getString(R.string.web_api_related);
			if ((iAuthor != null) && (iAuthor != "")) {
				url = url + iAuthor;
			} else {
				if (iPublisher != null && iPublisher != "") {
					url = url + iPublisher;
				} else {
					url = url + iTitle;
				}
			}
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	private void showFirstPage(boolean aShow) {
		Log.d(Config.LOGTAG, "showFirstPage:" + aShow);
		if (aShow) {
			this.iFirstPageView.setVisibility(View.VISIBLE);
			// 110303 disable control panel when showing first page
			enableControlPanel(false);
		} else
			this.iFirstPageView.setVisibility(View.INVISIBLE);
	}

	private void setFixedOrientation(boolean aFixed) {
		if (aFixed)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	private static final String KEY_Pref = "preference";
	private boolean bScreenFixed = false;

	private void savePreference() {
		SharedPreferences aSetting = getSharedPreferences(KEY_Pref, 0);
		aSetting.edit().putBoolean(KEY_Fixed, bScreenFixed).commit();
	}

	private void restorePreference() {
		SharedPreferences aSetting = getSharedPreferences(KEY_Pref, 0);
		// 110303 set default rotate screen enable
		bScreenFixed = aSetting.getBoolean(KEY_Fixed, false);
		this.setFixedOrientation(bScreenFixed);
	}

	private void exit() {
		if (this.pdf != null && this.iDatabase != null)
			this.iDatabase.setLastPage(this.iDeliverID, iCurPage,
					this.bSyncLastPage, getApplicationContext(),
					this.iDeliverID, this.iBookToken);
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/**
		 * handle back key
		 */
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			this.exit();
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean bLeftToNext = false; // true:left to next, false:right to
											// next

	private void handlePageSwitchDirection() {
		int aMode = GSiDatabaseAdapter.getSwitchSetting(this, this.iDeliverID);
		switch (aMode) {
		case GSiDatabaseAdapter.MODE_DEFAULT:
			this.bLeftToNext = bBookVertical;
			aMode = (bBookVertical) ? GSiDatabaseAdapter.MODE_LEFT
					: GSiDatabaseAdapter.MODE_RIGHT;
			GSiDatabaseAdapter.setSwitchSetting(this, iDeliverID, aMode);
			break;
		case GSiDatabaseAdapter.MODE_LEFT:
			this.bLeftToNext = true;
			break;
		case GSiDatabaseAdapter.MODE_RIGHT:
			this.bLeftToNext = false;
			break;
		default:
			Log.e(Config.LOGTAG, "get switch mode error!");
		}
		
		pagesView.SetLeftToNext(bLeftToNext);
	}

	/**
	 * waiting dialog
	 */
	private ProgressDialog iWaitingDialog = null;

	public void OpenWaitingDialog() {
		if (iWaitingDialog == null)
			iWaitingDialog = new ProgressDialog(RendererActivity.this);
		iWaitingDialog.setMessage(getString(R.string.GSI_LOADING_MSG));
		iWaitingDialog.setCancelable(false);
		iWaitingDialog.show();
	}

	public void CloseWaitingDialog() {
		if (iWaitingDialog != null) {
			iWaitingDialog.dismiss();
		}
	}

	/**
	 * async task for open pdf
	 * 
	 * @author water
	 * 
	 */
	class OpenPdfResult {
		int iResult;
		String iMsg;
		GSiDatabaseAdapter.GetLastPageResult iLastPage;

		OpenPdfResult(int aResult, String aMsg,
				GSiDatabaseAdapter.GetLastPageResult aLastPage) {
			iResult = aResult;
			iMsg = aMsg;
			iLastPage = aLastPage;
		}

		int getResult() {
			return iResult;
		}

		String getMsg() {
			return iMsg;
		}

		int getLastPage() {
			return iLastPage.getLastPage();
		}

		boolean getHttpResult() {
			return iLastPage.getHttpResult();
		}
	}

	class OpenPdfTask extends AsyncTask<Uri, Integer, OpenPdfResult> {

		@Override
		protected void onCancelled() {
			if (pdfPagesProvider != null) {
				Log.e(Config.LOGTAG, "clear RendererListener");
				pdfPagesProvider.clearCache();
				pdfPagesProvider.setOnImageRenderedListener(null);
			}
			if (pdf != null) {
				pdf.recycle();
				pdf = null;
			}
			CloseWaitingDialog();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(OpenPdfResult result) {
			super.onPostExecute(result);
			// display error message
			if (result.getResult() != Config.KErrNone) {
				pdf = null;
				pdfPagesProvider = null;
				String aMsg = (result.getMsg() == null) ? getString(R.string.GSI_FORMAT_INCORRECT)
						: result.getMsg();
				if (!RendererActivity.this.isFinishing()) {
					new AlertDialog.Builder(RendererActivity.this).setTitle(
							R.string.GSI_ERROR).setIcon(
							android.R.drawable.ic_dialog_alert)
							.setMessage(aMsg).setPositiveButton(
									R.string.GSI_OK,
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {
											exit();
										}
									}).setCancelable(false).show();
				}
				CloseWaitingDialog();
				return;
			}
			// initialize settings
			if (pdf != null) {
				iBookmarks = iDatabase.getBookmarks(iDeliverID,
						RendererActivity.this, iTitleView.getText().toString());
				iAnnotations = iDatabase.getAnnotations(iDeliverID);
				/*render finished will update iMarkResults*/
				//iMarkResults = iDatabase.getMarkers(iDeliverID);								

//				displayBookmark();
//				displayAnnotation();
				displayRotate();
				displaySwitch();
				
				//pagesView.setMarkResults(iMarkResults);

				if (result.getHttpResult())
					Toast.makeText(
							getApplicationContext(),
							getApplicationContext().getString(
									R.string.GSI_LASTPAGE_DOWNLOAD_MSG),
							Toast.LENGTH_SHORT).show();
				int aLastPage = (result != null) ? result.getLastPage() : -1;
				if (aLastPage == -1)
					showFirstPage(true);
				else
					gotoPage(aLastPage);
				if (bResultToShowFirstPage) {
					bResultToShowFirstPage = false;
					showFirstPage(true);
				}
				handlePageSwitchDirection();

				// set finder for FindResultActivity
				FinderSingleton.getInstance().setFile(pdf);
			}
			// close progress dialog
			CloseWaitingDialog();
		}

		@Override
		protected void onPreExecute() {
			// open progress dialog
			OpenWaitingDialog();
			iDatabase.open();
			super.onPreExecute();
		}

		@Override
		protected OpenPdfResult doInBackground(Uri... arg0) {
			String aResult = getPDF(arg0[0]);
			int err = Config.KErrNone;
			if (aResult == null) { // getPDF success
				pdfPagesProvider = new PDFPagesProvider(pdf);
				if (pdfPagesProvider != null)
					err = pagesView.setPagesProvider(pdfPagesProvider);
				else
					err = Config.KErrFail;
			} else {
				err = Config.KErrFail;
			}

			GetLastPageResult aLastPage = null;
			if (err == Config.KErrNone && iDatabase != null) { // getPDF success
				aLastPage = iDatabase.getLastPage(iDeliverID, bSyncLastPage,
						getApplicationContext(), iDeliverID, iBookToken);
				if(aLastPage.getDeviceIDEmpty()){
					err = Config.KErrFail;
					aResult = getApplicationContext().getString(
							R.string.GSI_DEVICE_ID_EMPTY_MSG);
				}
			} else {
				aLastPage = new GetLastPageResult(false, -1);
			}
			return new OpenPdfResult(err, aResult, aLastPage);
		}

	}

	public void disableFirstPage() {
		this.showFirstPage(false);
	}

	private boolean bContextOpen = false;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);

		// AdapterView.AdapterContextMenuInfo info =
		// (AdapterView.AdapterContextMenuInfo) menuInfo;
		// menu.setHeaderTitle(((TextView) info.targetView).getText());

		if (v instanceof PagesView) {
			if(bIsLastPageVisible)return;
			boolean bLongPressed = ((PagesView) v).isLongPressed();
			if (bLongPressed) {
                if(!((PagesView) v).getRenderFinished())return;/*add marker when render finished*/
                if(bControlEnabled)
    			{
    				enableControlPanel(false);
    			}
				bContextOpen = true;
				pagesView.setParentContextMenuOpen(bContextOpen);

				final ActionItem partitionLineAction = new ActionItem();
				partitionLineAction.setIcon(getResources().getDrawable(R.drawable.quickaction_slider_grip_left));
				
				final ActionItem addAction = new ActionItem();
				addAction.setIcon(getResources().getDrawable(R.drawable.ic_add));

				final ActionItem accAction = new ActionItem();
				accAction.setIcon(getResources().getDrawable(R.drawable.ic_accept));

				final ActionItem annotAction = new ActionItem();
				if (hasAnnotation()) {
					annotAction.setIcon(getResources().getDrawable(R.drawable.ic_annot_del));
				} else {
					annotAction.setIcon(getResources().getDrawable(R.drawable.ic_annot));
				}

				final QuickAction mQuickAction = new QuickAction(v);

				addAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mQuickAction.dismiss();
						handleMark();
					}
				});

				accAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mQuickAction.dismiss();
						showFindDialog();
					}
				});

				annotAction.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mQuickAction.dismiss();

						showFirstPage(false);
						if (!bIsLastPageVisible) {
							if (hasAnnotation())
								removeAnnotation();
							else {
								Intent aNext = new Intent();
								Bundle aBundle = new Bundle();
								aBundle.putInt(AnnotationActivity.KEY_MODE,
										AnnotationActivity.ModeEdit);
								aNext.putExtras(aBundle);
								aNext.setClass(RendererActivity.this,
										AnnotationActivity.class);
								startActivityForResult(aNext,
										Request_annotation);
							}
						}
					}
				});

				mQuickAction.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss() {
						// TODO Auto-generated method stub
						bContextOpen = false;
						pagesView.setParentContextMenuOpen(bContextOpen);
					}

				});

				mQuickAction.addActionItem(addAction);
				mQuickAction.addActionItem(partitionLineAction);
				mQuickAction.addActionItem(annotAction);
				mQuickAction.addActionItem(partitionLineAction);
				mQuickAction.addActionItem(accAction);
				
				mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
				mQuickAction.showAtOffset(((PagesView) v).getPressedPoint().y);
			}
		}
	}

	private void saveRotate(boolean bRotate) {
		savePreference();
		setFixedOrientation(bScreenFixed);
	}

	private void saveSwitch(boolean bSwitch) {
		int aMode = (bSwitch) ? GSiDatabaseAdapter.MODE_LEFT
				: GSiDatabaseAdapter.MODE_RIGHT;
		GSiDatabaseAdapter.setSwitchSetting(this, iDeliverID, aMode);
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
}
