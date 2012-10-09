package com.taiwanmobile.myBook_PAD;


import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.iii.ideas.reader.annotation.AnnotationDB;
import org.iii.ideas.reader.bookmark.Bookmarks;
import org.iii.ideas.reader.last_page.LastPageHelper;
import org.iii.ideas.reader.underline.UnderlineDB;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tw.com.mebook.util.ImageDownloader;
import tw.com.soyong.AnReader;
import tw.com.soyong.utility.SyBookmark;
import tw.com.soyong.utility.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gsimedia.gsiebook.RendererActivity;
import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.DataClass;
import com.gsimedia.sa.GSiMediaRegisterProcess.DeviceIDException;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.gsimedia.sa.GSiMediaRegisterProcess.IllegalNetworkException;
import com.gsimedia.sa.GSiMediaRegisterProcess.TimeOutException;
import com.gsimedia.sa.GSiMediaRegisterProcess.XmlException;
import com.gsimedia.sa.GSiMediaRegisterProcess.XmlP12FileException;
import com.gsimedia.sa.Internet.Internet;
import com.taiwanmobile.myBook_PAD.BookList.ViewHolder;

/**
 * 本地書櫃
 * @author III
 * 
 */
public class TWMBook extends Activity{
	
  
	private static final boolean DEBUG = true ;
	
	private boolean isEditModel = false ;
	//private final static float TARGET_HEAP_UTILIZATION = 0.75f;   
	//private final static int CWJ_HEAP_SIZE = 20* 1024* 1024 ;   
	private String ebook_error;
	private String ebook_update_at;
	//private AlertDialog ad; 
	
	private List<String> ebook_deliveryID = null;	
	private List<String> ebook_contentID = null;	
	private List<String> ebook_title = null;	
	private List<String> ebook_publisher = null;
	private List<String> ebook_authors = null;	
	private List<String> ebook_type = null;		
	private List<String> ebook_category = null;
	private List<String> ebook_update_date = null;
	private List<String> ebook_purchased_at = null;
	private List<String> ebook_trial = null;
	private List<String> ebook_vertical = null;	
	private List<String> ebook_trial_due_date = null;	
	private List<String> ebook_cover = null;
	private List<String> ebook_bodytype_code = null;	
	
	private RelativeLayout rl_tools_alert_dialog;
	
	//private int tempAllListArg = 0;
	
	private SharedPreferences settings;	
	
	//private ImageView iv_bookcase;
	private ListView lv_main;	
	
	private Boolean[] allCheckBoxValue = null;
	
	private Boolean downloadStatus = false;
	
	private ViewHolder[] mainRow = null;	
	
	private RelativeLayout rl_top , rl_center , rl_edit_mode , rl_main;
	private ImageButton ib_buy,ib_edit,ib_tools,ib_readed,ib_read,ib_all,ib_up_page,ib_del_top,ib_del_bottom,ib_all_select,ib_all_unselect,ib_realbook,ib_listbook;
	//110504 add for login button
	private ImageButton ib_login;
	//private Drawable da = null;	  
	private myBookList mybl;
	private myAllBookList bla;
	private String downloadBookUrl = "http://delivery.twmebook.match.net.tw/DeliverWeb/downloadEBook?deliver_id=";
	private String saveFilelocation = "/sdcard/twmebook/";
	private String deviceID = "";
	
	private TWMDB tdb = null; //= new TWMDB(this);
	private String[] fileType = {".teb",".tvb",".tpb",};
	private Cursor cursorDBData,cursorDBDataThread;
	private int nowStyle = 1;//未讀 1 已讀 2  全部 3 4 5
	//private int upDataCount;
	private List<DownloadPbar> dp = null;
	//private List<Integer> dp_num = null;
	//private int isdownloading = 0;
	//private boolean isChange = false;
	//private List<Integer> delArray = new ArrayList<Integer>();
	//private String[][] listToArray = new String[2][];
	
	private ProgressDialog pDialog;
	private boolean unOnline ;
	//private boolean isDelModel = false;
	private String download_url;
	
	private String p12Path;
	private StatFs stat;    
	private long freeSize;
	private long downloadHeapSize = 0;
	private List<String> downloadID  = new ArrayList<String>();
	
	private int nowDownloadNum = 0;
	private final static int MAX_DOWNLOAD_NUM = 5;//上限 5   
	
	//private List<Integer> pbarIndexList = new ArrayList<Integer>();
	private List<Integer> pbarValueList = new ArrayList<Integer>();
	private List<String> pbarNowStatusList = new ArrayList<String>();
	//private List<Annotation> aaa = null;	
	//08-13 02:10:20.463: ERROR/(1970): /data/data/test.Table/files
	
	//private ArrayList<String> mDownloadIDs = new ArrayList<String>();
	private HashMap<Integer, Long> mThreadMap = new HashMap<Integer, Long>() ;
	private boolean isListBookReady = false;
	private Object lock = new Object();
	private ProgressDialog mPrepareDlg = null; 
//	private Timer m_Timer = null;
//	private void startTimer(){
//		if(m_Timer != null)return;
//		m_Timer = new Timer();
//		
//		
//	}
	private synchronized void addThreadMap(int pos , long timeStamp){
		//mDownloadIDs.add(id);
		mThreadMap.put(pos, timeStamp);
		
		
		if (DEBUG) Log.w("vic", "pos:"+ pos + " timeStamp:"+ timeStamp);
	}
	
	private synchronized long getTimeStamp(int pos){
		//return mDownloadIDs.contains(id);
		Long obj =  mThreadMap.get(new Integer(pos));
		if (null == obj){
			return 0 ;
		}
		
		return obj.longValue();
	}

	@Override
	protected void onCreate(Bundle icicle){
		
		super.onCreate(icicle);  
		isListBookReady = false;
		setContentView(R.layout.iii_main);
		p12Path = this.getFilesDir().toString();
		
		setDeviceID();
//		//p12Path = "/sdcard";

//		//
////		Build.VERSION ver = new Build.VERSION();
////		int sdkCode = Integer.parseInt(ver.SDK);
//		 DisplayMetrics metrics = new DisplayMetrics();
//		 getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		 float screen_height = 0;
//		 float screen_width = 0;
//		 screen_height = (int)(metrics.heightPixels *metrics.density);
//		 screen_width = (int)(metrics.widthPixels*metrics.density);
//		 
//		 if(screen_height <1023 &&  screen_width< 599)//1024x600
//		 {
//			 //init dialog
//	         Toast.makeText(TWMBook.this, getResources().getString(R.string.illegal_device),  
//	                    Toast.LENGTH_LONG).show(); 
//
//			 finish();
//			 return;
//		 }
//		 
//		if (DEBUG) Log.e("vic", "onCreate ") ;
		isInnerSD();
		setViewComponent();
		setListener();
		
		downloadBookUrl = getResources().getString(R.string.iii_twm_download_ebook);
//		tdb = RealBookcase.getTWMDB();
//		if(tdb == null)
//			tdb = new TWMDB(this);
		
		tdb = new TWMDB(this);
		initList();
		
		//VMRuntime.getRuntime().setTargetHeapUtilization(TARGET_HEAP_UTILIZATION);   
		//long oldSize = VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
		
//		pDialog = ProgressDialog.show(TWMBook.this, "", getResources().getString(R.string.iii_CheckNetworkMessage));
//		pDialog.setCancelable(false);
//		tdb = new TWMDB(this);
		new Thread(){
			public void run(){
				try{
					if (testNetwork()){
						Message m2 = new Message();
						Bundle data2 = m2.getData();
						data2.putString("msg", getResources().getString(R.string.iii_CheckVerMessage));
						setpDialog.sendMessage(m2);
						unOnline = true;
						Log.e("checkVersion", "checkVersion");
						//setNewInit(); 
						//checkVersion();
						//setInit();
						//return ;
					}else{
						unOnline = false;
						//threadTestttMsg(getResources().getString(R.string.iii_NetworkNotConnMessage));
						//initList();
						//upImage();
					}
				}catch(Exception e){
					threadTestttMsg(getResources().getString(R.string.iii_UnknowMessage));
					//initList();
					unOnline = false;
					e.printStackTrace();
				}
				
				updateFileIsExists();
			}
		}.start();	
		
		loadBradData();
		
		
//	    File folderA = new File(Util.getStorePath(this));
//	    
//	    FileObserver observer = new FileObserver(
//	    		Util.getStorePath(this),
//	            FileObserver.ALL_EVENTS){
//	 
//	        @Override
//	        public void onEvent(int event, String path) {
//	            Log.e("TWMBook fileobs", "[onEvent]" +
//	                    ", Thread ID: "+ (Thread.currentThread().getId()) +
//	                    ", Event ID: "+ (event & FileObserver.ALL_EVENTS) +
//	                    ", File path: "+path);
//	 
//	        }
//	 
//	    };
//	 
//	    observer.startWatching();
	 
	
	}
	@Override
	public Object getLastNonConfigurationInstance() {
		// TODO Auto-generated method stub
		if (DEBUG) Log.e("vic", "getLastNonConfigurationInstance ") ;
		return super.getLastNonConfigurationInstance();
	}

	/**
	 * 與伺服器checkDomain/register，下載書單並更新圖片
	 */
	/*private void setInit(){
		if (DEBUG) Log.e("flw", "+setInit ") ;
		Message m2 = new Message();
		Bundle data2 = m2.getData();
		data2.putString("msg", getResources().getString(R.string.iii_CheckPhoneMessage));		
		setpDialog.sendMessage(m2);						
		setDeviceID();
		
		if (isP12Exist(p12Path)){
			checkDomain();		
			Log.e("checkDomain", "checkDomain");
		}else{
			register(p12Path);
			Log.e("register", "register");
		}		
		
		Log.e(p12Path, p12Path);

		settings = getSharedPreferences("setting_Preference", 0);
		
		if(settings.getBoolean("setting_auto_sync_new_book_value", true)){
			if(unOnline){	
				m2 = new Message();
				data2 = m2.getData();
				data2.putString("msg", getResources().getString(R.string.iii_SyncMessage));
				setpDialog.sendMessage(m2);
				downloadXML(getResources().getString(R.string.iii_twm_download_list_by_updated)+String.valueOf(settings.getString("update_at", "0"))+"&device_id="+deviceID);
			}
			if(unOnline){
				insertDB();
				//threadTestttMsg(getResources().getString(R.string.iii_SyncSucessMessage));

				Log.e("  upImage   ", "upImage");   
				upImage();
			}
		}else{
			threadTestttMsg("");
		}
		
		updateFileIsExists();
	}
	*/
	/**
	 * 與伺服器checkDomain/register，下載書單並更新圖片 for new listbook
	 */
	/*private void setNewInit(){
		if (DEBUG) Log.e("flw", "+setNewInit ") ;
//		Message m2 = new Message();
//		Bundle data2 = m2.getData();
//		data2.putString("msg", getResources().getString(R.string.iii_CheckPhoneMessage));		
//		setpDialog.sendMessage(m2);						
		setDeviceID();
		
//		if (isP12Exist(p12Path)){
//			checkDomain();		
//			Log.e("checkDomain", "checkDomain");
//		}else{
//			register(p12Path);
//			Log.e("register", "register");
//		}		
		
		Log.e(p12Path, p12Path);

		settings = getSharedPreferences("setting_Preference", 0);
		
		if(settings.getBoolean("setting_auto_sync_new_book_value", true)){
			if(unOnline){	
//				m2 = new Message();
//				data2 = m2.getData();
//				data2.putString("msg", getResources().getString(R.string.iii_SyncMessage));
//				setpDialog.sendMessage(m2);
				downloadXML(getResources().getString(R.string.iii_twm_download_list_by_updated)+String.valueOf(settings.getString("update_at", "0"))+"&device_id="+deviceID);
			}
			if(unOnline){
				insertDB();
//				threadTestttMsg(getResources().getString(R.string.iii_SyncSucessMessage));

				Log.e("  upImage   ", "upImage");   
				upImage();
			}
		}else{
//			threadTestttMsg("");
		}
	}	*/
	private Handler setpDialog = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Log.e("flw", "+setpDialog handleMessage ") ;
//			String msg2 = msg.getData().getString("msg");
//			pDialog.setMessage(msg2);
		}
	};
	
	private Handler testtt2 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Log.e("flw", "+testtt2 handleMessage ") ;
			String msg2 = msg.getData().getString("msg");
			if(msg2.equals("")){
				
			}else{
				showAlertMessage(msg2);
			}				
			lv_main.invalidateViews();	
		}
	};
	
	private volatile Handler testtt = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Log.e("flw", "+testtt handleMessage ") ;
			String msg2 = msg.getData().getString("msg");
			if(pDialog !=null && pDialog.isShowing()){
				if (DEBUG) Log.e("vic", "****Dialog Dismiss "+msg2) ;
				pDialog.dismiss();
				pDialog = null;
			}
			initList();		
			if(msg2.equals("")){
				
			}else{
				showAlertMessage(msg2);
			}				
			lv_main.invalidateViews();	
		}
	};
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Bundle bd = msg.getData();
			String desc = bd.getString("msg");
			new AlertDialog.Builder(TWMBook.this)
			.setTitle(R.string.msgbox_expire_title)
			.setMessage(desc)//R.string.msgbox_expire_content)
			.setPositiveButton(R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//retrieve token
							//startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(url)) );
							if(pDialog != null && pDialog.isShowing()){
								pDialog.dismiss();
								pDialog = null;
							}							
							RealBookcase.AuthSSOLogin(TWMBook.this,RealBookcase.listbook, getBaseContext(),null);

							
						}
					}
			)
			.setNegativeButton(R.string.iii_showAM_cancel,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//cancel
							//finish();
							if(pDialog != null && pDialog.isShowing()){
								pDialog.dismiss();
								pDialog = null;
							}
						}
				}
			)				
			.show();		
		}
	};	
	/**
	 * 依照是否強制更新做不同動作
	 */
	/*private Handler checkVer = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Log.e("flw", "+checkVer handleMessage ") ;
			String force = msg.getData().getString("force");
			final String url = msg.getData().getString("url");
			
			if(force.equals("1")){
				new AlertDialog.Builder(TWMBook.this)
				.setTitle(R.string.iii_VerUpdateMessage)
				.setMessage(R.string.iii_VerUpdateMessageForce)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(url)) );
								exit();
								
							}
						}
				)
				.setNegativeButton(R.string.iii_showAM_cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								exit();
							}
					}
				)				
				.show();	
			}else{
				new Thread(){
				public void run(){
					setInit(); 
				}
			}.start();					
//				new AlertDialog.Builder(TWMBook.this)
//				.setTitle(R.string.iii_VerUpdateMessage)
//				.setMessage(R.string.iii_VerUpdateMessageUnforce)
//				.setPositiveButton(R.string.iii_showAM_ok,
//						new DialogInterface.OnClickListener(){
//							public void onClick(DialogInterface dialoginterface, int i){
//								startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(url)) );								
//								new Thread(){
//									public void run(){
//										setInit(); 
//									}
//								}.start();									
//							}
//						}
//				)
//				.setNegativeButton(R.string.iii_showAM_cancel,
//						new DialogInterface.OnClickListener(){
//							public void onClick(DialogInterface dialoginterface, int i){
//								new Thread(){
//									public void run(){
//										setInit(); 
//									}
//								}.start();		
//							}
//					}
//				)				
//				.show();	
			}
		}
	};
	*/
	private static final int CONNECT_TIMEOUT = 15000 ; 
	private static final int READ_TIMEOUT = 15000;
	
	/**
	 * 連接伺服器檢查是否有更新檔
	 */
//	public boolean checkVersion(){
	/*private boolean checkVersion(){
		if (DEBUG) Log.e("flw", "+checkVersion ") ;
		PackageInfo pInfo = null;
		Document doc ;
   	 	HttpURLConnection conn = null;
 		String error = "",force_update = "0";
 		boolean force_update_b = true;
		try {
			pInfo = getPackageManager().getPackageInfo("com.taiwanmobile.myBook_PAD",PackageManager.GET_META_DATA);
			
   	 		URL myURL = new URL(getResources().getString(R.string.iii_version_control)+"&version="+pInfo.versionName);
   	 		//URL myURL = new URL(getResources().getString(R.string.iii_version_control)+"&version="+"0.5.0");
   	 		//test2(getResources().getString(R.string.iii_version_control)+"&version="+"0.5.0");
   	 		
   	 		conn = (HttpURLConnection)myURL.openConnection();
   	 		conn.setConnectTimeout (CONNECT_TIMEOUT) ;
   	 		conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoInput(true);
            conn.connect();
            // The line below is where the exception is called
            int response = conn.getResponseCode();  
            if (response != HttpURLConnection.HTTP_OK) {
            	//threadTestttMsg(getResources().getString(R.string.iii_NetworkNotConnMessage));
            	return false ;
            }
   	 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   	        DocumentBuilder db = dbf.newDocumentBuilder();
   	        InputStream is = conn.getInputStream();
  	        
   	        doc = db.parse(is);
   	        NodeList nError=doc.getElementsByTagName("error");
   	        if(nError.item(0)!=null)
   	        	error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        //Log.e("error", error);//
   	        //NodeList nversion=doc.getElementsByTagName("version");
   	        //if(nversion.item(0)!=null)
   	        	//version = nversion.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        //Log.e("version", version);
   	        NodeList nforce_update=doc.getElementsByTagName("force_update");
   	        if(nforce_update.item(0)!=null)
   	        	force_update = nforce_update.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        //Log.e("force_update", force_update);
   	        NodeList ndownload_url=doc.getElementsByTagName("download_url");
   	        if(ndownload_url.item(0)!=null)
   	        	download_url = ndownload_url.item(0).getChildNodes().item(0).getNodeValue().toString();  
   	        //Log.e("download_url", download_url);
   	     	if(!error.equals(getResources().getString(R.string.iii_NoNewVer))){
		
   	     		Message m = new Message();
   	     		Bundle data = m.getData();
   	   	        if(force_update.equals("1")){
   	   	        	unOnline = false;
   					data.putString("force", "1");
   					data.putString("url", download_url);
   					checkVer.sendMessage(m);
   	   	        	force_update_b = false;
   	   	        }else if(force_update.equals("0")){
   	   	        	unOnline = true;
   					data.putString("force", "0");
   					data.putString("url", download_url);
   					checkVer.sendMessage(m);  
   	   	        	force_update_b = true;
   	   	        }else{
   	   	        	setInit();   
   	   	        }
   	        }else{
				setInit();   
   	        }   	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_check_ver_error));
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_check_ver_error));
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_check_ver_error));
			e.printStackTrace();
		}
		return force_update_b;		
	}
	*/
	/**
	 * 製作出manageDomain陣列　預設 3 個
	 */
	/*private Handler manage = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Log.e("flw", "+manage handleMessage") ;
			System.out.println(msg.getData().get("index"));
			System.out.println(msg.getData().get("type"));
			String[] ase1,ase2;
			ase1 = (String[]) msg.getData().get("index");
			ase2 = (String[]) msg.getData().get("type");
			if (ase1.length<3){
				String[] ase3 = new String[3],ase4 = new String[3];				
				for(int i=0;i<ase1.length;i++){
					ase3[i] = ase1[i];
					ase4[i] = ase2[i];
				}
				for(int i=ase1.length;i<3;i++){
					ase3[i] = "null";
					ase4[i] = "null";
				}	
				manageDomain(ase3,ase4);
			}else{
				manageDomain(ase1,ase2);	
			}			
		}
	};*/
	/**
	 * 取得checkDomain所回傳的資料  
	 */
//	public void checkDomain(){		
	/*private void checkDomain(){		
		try {
			if (DEBUG) Log.e("flw", "+checkDomain ") ;
			DataClass dataclass = GSiMediaRegisterProcess.checkDomain(TWMBook.this,RealBookcase.getToken());
			 if(dataclass !=  null) {
				 String disString = "";
				 if(dataclass.resultCode_P12 != null){
					 disString += "resultCode_P12= ";
					 disString += dataclass.resultCode_P12;
				 }
				 if(dataclass.resultCode_Domain != null){
					 disString += ", resultCode_Domain= ";
					 disString += dataclass.resultCode_Domain;
				 }
				 if(dataclass.domain != null){
					 disString += ", domain= ";
					 disString += dataclass.domain;
				 }
				 if(dataclass.index != null && dataclass.index.length > 0){
					 disString += ", index[0]= ";
					 disString += dataclass.index[0];
				 }
				 if(dataclass.type != null && dataclass.type.length > 0){
					 disString += ", type[0]= ";
					 disString += dataclass.type[0];
				 }
				 if(dataclass.index != null && dataclass.index.length > 1){
					 disString += ", index[1]= ";
					 disString += dataclass.index[1];
				 }
				 if(dataclass.type != null && dataclass.type.length > 1){
					 disString += ", type[1]= ";
					 disString += dataclass.type[1];
				 }
				 if(dataclass.index !=null && dataclass.index.length > 2){
					 disString += ", index[2]= ";
					 disString += dataclass.index[2];
				 }
				 if(dataclass.type != null && dataclass.type.length > 2){
					 disString += ", type[2]= ";
					 disString += dataclass.type[2];
				 }
				 Log.e("disString", disString);
				 checkResultCode(0,dataclass.resultCode_P12,dataclass.resultCode_Domain,dataclass.index,dataclass.type);
				 
			 }else {
				 	threadTestttMsg(getResources().getString(R.string.iii_UnknowMessage));
					unOnline = false;
			 }
		} catch (IllegalNetworkException e) {
			// TODO Auto-generated catch block
			//threadTestttMsg(getResources().getString(R.string.iii_NetworkNotConnMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (TimeOutException e) {
			// TODO Auto-generated catch block		
			threadTestttMsg(getResources().getString(R.string.iii_ServerTimeOutMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (DeviceIDException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_GetDeviceIDErrorMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_ServerReturnErrorMessage));
			unOnline = false;
			e.printStackTrace();
		}	catch (Exception e) {
			// TODO Auto-generated catch block
			//threadTestttMsg(getResources().getString(R.string.iii_NetworkNotConnMessage));
			unOnline = false;
			e.printStackTrace();
		}	
	}*/
	
	/**
	 * 依據server回傳的code做出不同動作
	 * @param model 0 checkDomain   1 register  3 manageDomain
	 * @param resultCode_P12 resultCode_P12
	 * @param resultCode_Domain resultCode_Domain
	 * @param index 手機id
	 * @param type 手機名稱
	 */
	/*private void checkResultCode(int model , String resultCode_P12,String resultCode_Domain,String[] index,String[] type){
		if (DEBUG) Log.e("flw", "+checkResultCode ") ;
		String resultCode = "";
		//Log.e(" model ", String.valueOf(model));
		//Log.e(" resultCode_P12 ", resultCode_P12);
		//Log.e(" resultCode_Domain ", resultCode_Domain);
		if(model == 0){
			if(resultCode_Domain == null){
				resultCode = resultCode_P12;
			}else{
				resultCode = resultCode_Domain;
			}			
		}else if(model == 1){
			resultCode = resultCode_P12;
		}else if(model == 3){
			resultCode = resultCode_Domain;
			//unOnline = true;
		}

		//resultCode_P12= -8
		
		if(resultCode.equals("0")){
			if(model == 1){
				if(resultCode_Domain.equals("-1")){
					threadTestttMsg("");
					unOnline = false;
					
					Message m = new Message();
					Bundle data = m.getData();
					data.putStringArray("index", index);
					data.putStringArray("type", type);
					manage.sendMessage(m);	
					Log.e(" 2 ", " 2 ");
				}else if(resultCode_Domain.equals("-3")){
					threadTestttMsg(getResources().getString(R.string.iii_register_num_over_limit));
					unOnline = false;
					Log.e(" 3 ", " 3 ");
				}
			}
		}else if(resultCode.equals("-10")){
			if(model == 1){
				if(resultCode_Domain.equals("-1")){
					threadTestttMsg("test1");
				}else if(resultCode_Domain.equals("-3")){
					threadTestttMsg("test2");
				}else{
					threadTestttMsg("test3");
				}
			}
		}else if(resultCode.equals("-1")){
			threadTestttMsg("");
			Log.e(" 1 ", " 1 ");
			Message m = new Message();
			Bundle data = m.getData();
			data.putStringArray("index", index);
			data.putStringArray("type", type);
			manage.sendMessage(m);	
			unOnline = false;
		}else if(resultCode.equals("-8")){
			threadTestttMsg(getResources().getString(R.string.iii_plz_use_twm_net));
			unOnline = false;
		}else if(resultCode.equals("-6")){
			threadTestttMsg(getResources().getString(R.string.iii_server_busy_only_unline));
			unOnline = false;
		}else if(resultCode.equals("-7")||resultCode.equals("-9")){
			threadTestttMsg(getResources().getString(R.string.iii_server_register_error));
			unOnline = false;
		}else if(resultCode.equals("-3")){			
			threadTestttMsg(getResources().getString(R.string.iii_register_num_over_limit));
			unOnline = false;
		}else if(resultCode.equals("-4")){
			threadTestttMsg(getResources().getString(R.string.iii_cant_update_phone));
			unOnline = false;
		}else{
			threadTestttMsg(getResources().getString(R.string.iii_UnknowMessage));
			unOnline = false;
		}		
	}*/
	/**
	 * 產生manageDomain列表ui
	 * @param ase1 手機index
	 * @param ase2 手機名稱
	 */
	/*private void manageDomain(String[] ase1,String[] ase2){
		if (DEBUG) Log.e("flw", "+manageDomain 1") ;
		final String index0 = ase1[0];
		final String index1 = ase1[1];
		final String index2 = ase1[2];
		Log.e(ase2[0], ase2[0]);
		Log.e(ase2[1], ase2[1]);
		Log.e(ase2[2], ase2[2]);
		new AlertDialog.Builder(TWMBook.this)
		  .setTitle(R.string.iii_manage_domain)
		  .setItems(ase2, 
			  new DialogInterface.OnClickListener(){
				 public void onClick(DialogInterface dialog, int whichcountry) {
					 pDialog = ProgressDialog.show(TWMBook.this, "", getResources().getString(R.string.iii_CheckPhoneMessage));
					 pDialog.setCancelable(false);
					 switch(whichcountry) {
						 case 0:
								new Thread(){
									public void run(){
										manageDomain(index0);		
										Message m2 = new Message();
										Bundle data2;
										unOnline = true;
										settings = getSharedPreferences("setting_Preference", 0);
										if(settings.getBoolean("setting_auto_sync_new_book_value", true)){
											if(unOnline){	
												m2 = new Message();
												data2 = m2.getData();
												data2.putString("msg", getResources().getString(R.string.iii_SyncMessage));
												setpDialog.sendMessage(m2);
												downloadXML(getResources().getString(R.string.iii_twm_download_list_by_updated)+String.valueOf(settings.getLong("update_at", 0))+"&device_id="+deviceID);
											}
											if(unOnline){
												insertDB();
												//threadTestttMsg(getResources().getString(R.string.iii_SyncSucessMessage));
												Log.e("  upImage   ", "upImage");   
												upImage();
											}
										}else{
											threadTestttMsg("");
										}
									}
								}.start();	
							 break;								 
						 case 1:
								new Thread(){
									public void run(){
										manageDomain(index1);		
										Message m2 = new Message();
										Bundle data2;
										unOnline = true;
										settings = getSharedPreferences("setting_Preference", 0);
										if(settings.getBoolean("setting_auto_sync_new_book_value", true)){
											if(unOnline){	
												m2 = new Message();
												data2 = m2.getData();
												data2.putString("msg", getResources().getString(R.string.iii_SyncMessage));
												setpDialog.sendMessage(m2);
												downloadXML(getResources().getString(R.string.iii_twm_download_list_by_updated)+String.valueOf(settings.getLong("update_at", 0))+"&device_id="+deviceID);
											}
											if(unOnline){
												insertDB();
												//threadTestttMsg(getResources().getString(R.string.iii_SyncSucessMessage));
												Log.e("  upImage   ", "upImage");   
												upImage();
											}
										}else{
											threadTestttMsg("");
										}
									}
								}.start();	
							 break;								 
						 case 2:	
								new Thread(){
									public void run(){
										manageDomain(index2);		
										Message m2 = new Message();
										Bundle data2;
										unOnline = true;
										settings = getSharedPreferences("setting_Preference", 0);
										if(settings.getBoolean("setting_auto_sync_new_book_value", true)){
											if(unOnline){	
												m2 = new Message();
												data2 = m2.getData();
												data2.putString("msg", getResources().getString(R.string.iii_SyncMessage));
												setpDialog.sendMessage(m2);
												downloadXML(getResources().getString(R.string.iii_twm_download_list_by_updated)+String.valueOf(settings.getLong("update_at", 0))+"&device_id="+deviceID);
											}
											if(unOnline){
												insertDB();
												//threadTestttMsg(getResources().getString(R.string.iii_SyncSucessMessage));
												Log.e("  upImage   ", "upImage");   
												upImage();
											}
										}else{
											threadTestttMsg("");
										}
									}
								}.start();
							 break;
						 case 3:
							 break;
						 default :
							 break;
					 }		
				 }				 
			  }
		  ).setNegativeButton(R.string.iii_showAM_cancel,
			  new DialogInterface.OnClickListener(){
				 public void onClick(DialogInterface dialog, int whichcountry) {
					 dialog.dismiss();
					 unOnline = false;
				 }
		  	  }
	      )
	      .setCancelable(false)
	      .show();
	}*/
	/**
	 * 取得manageDomain所回傳的資料  
	 * @param index 手機index
	 */
	/*private void manageDomain(String index){
		if (DEBUG) Log.e("flw", "+manageDomain 2") ;
		try {
			DataClass dataclass = GSiMediaRegisterProcess.manageDomain(index,TWMBook.this,RealBookcase.getToken());
			 if(dataclass !=  null) {
				 String disString = "";
				 if(dataclass.resultCode_P12 != null){
					 disString += "resultCode_P12= ";
					 disString += dataclass.resultCode_P12;
				 }
				 if(dataclass.resultCode_Domain != null){
					 disString += ", resultCode_Domain= ";
					 disString += dataclass.resultCode_Domain;
				 }
				 if(dataclass.domain != null){
					 disString += ", domain= ";
					 disString += dataclass.domain;
				 }
				 if(dataclass.index != null && dataclass.index.length > 0){
					 disString += ", index[0]= ";
					 disString += dataclass.index[0];
				 }
				 if(dataclass.type != null && dataclass.type.length > 0){
					 disString += ", type[0]= ";
					 disString += dataclass.type[0];
				 }
				 if(dataclass.index != null && dataclass.index.length > 1){
					 disString += ", index[1]= ";
					 disString += dataclass.index[1];
				 }
				 if(dataclass.type != null && dataclass.type.length > 1){
					 disString += ", type[1]= ";
					 disString += dataclass.type[1];
				 }
				 if(dataclass.index !=null && dataclass.index.length > 2){
					 disString += ", index[2]= ";
					 disString += dataclass.index[2];
				 }
				 if(dataclass.type != null && dataclass.type.length > 2){
					 disString += ", type[2]= ";
					 disString += dataclass.type[2];
				 }
				 Log.e("disString", disString);
				 checkResultCode(3,dataclass.resultCode_P12,dataclass.resultCode_Domain,dataclass.index,dataclass.type);
				 
			 }else {
				 	threadTestttMsg(getResources().getString(R.string.iii_UnknowMessage));
					unOnline = false;
			 }
		} catch (IllegalNetworkException e) {
			// TODO Auto-generated catch block
			//threadTestttMsg(getResources().getString(R.string.iii_NetworkNotConnMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (TimeOutException e) {
			// TODO Auto-generated catch block		
			threadTestttMsg(getResources().getString(R.string.iii_ServerTimeOutMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (DeviceIDException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_GetDeviceIDErrorMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_ServerReturnErrorMessage));
			unOnline = false;
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_UnknowMessage));
			unOnline = false;
			e.printStackTrace();
		}			
	}
	*/
	
	/**
	 * 檢查檔案是否存在 若有變動則更新資料庫
	 */
	private void updateFileIsExists() {
		if (DEBUG) Log.e("flw", "+updateFileIsExists ") ;
		long c , d ;
		c = System.currentTimeMillis();
		
		//allen
//		cursorDBData = tdb.select();
//		cursorDBData.moveToFirst();
		
		final Cursor cc = tdb.select();
		
		cc.moveToFirst();
		
		
		File a ,b;
		downloadHeapSize = 0;
		downloadID.clear();
		final int count = cc.getCount(); //cursorDBData.getCount();
		String str19 ;
		for(int i=0;i<count;i++){
			str19 = cc.getString(19);	//19,書的路徑
			a = new File(str19);
			if (!a.exists()){
				b = new File(str19+".tmp");
				if(!b.exists()){
					tdb.update(cc.getInt(0), "isDownloadBook", "0");
				}else{
					tdb.update(cc.getInt(0), "isDownloadBook", "3");
//					downloadHeapSize = downloadHeapSize + cursorDBData.getLong(20);
//					downloadID.add(cc.getString(5));
				}
			}else{
				if(!cc.getString(8).equals("1")){	//8,下載狀態 (0:no,1:已下載,2:下載中,3:下載但未完成)
					a.delete();
				}
			}
			if(cc.getString(8).equals("2")){
				tdb.update(cc.getInt(0), "isDownloadBook", "3");
			}
			
			final String str9 = cc.getString(9);	//9,本機圖片位置
			//if(cursorDBData.getString(9).indexOf(".")>0){
			if ( null != str9 && str9.indexOf(".")>0 ){
				a = new File(str9);
				if (!a.exists()){
					if( cc.getString(2).equals(getResources().getString(R.string.iii_mebook)) ){
						tdb.update(cc.getInt(0), "coverPath", "ivi_nonepict02");		
					}else{
						tdb.update(cc.getInt(0), "coverPath", "ivi_nonepict01");		
					}									
				}
			}
			cc.moveToNext();
		}
		
		d = System.currentTimeMillis();
		
		synchronized (lock){
			isListBookReady = true;
			if(mPrepareDlg != null && mPrepareDlg.isShowing())
			{
				mPrepareDlg.dismiss();
				mPrepareDlg = null;
			}
		}
		if ( d-c > 4000){
			//Toast.makeText(this, "updateFileIsExists > 4 sec", Toast.LENGTH_SHORT).show();
		}
//vic		cursorDBData.close();
	}
	/**
	 * 設定手機ID
	 */
	public void setDeviceID(){
		if (DEBUG) Log.e("flw", "+setDeviceID ") ;
		try{
			deviceID = "";
			deviceID = GSiMediaRegisterProcess.getID(getApplicationContext());
		}catch(Throwable e){};
    	if (deviceID == null) deviceID = "";
    	
		//deviceID = GSiMediaRegisterProcess.getID(this.getApplicationContext()); 
		//System.out.println(deviceID);  //don't print here...printing null will cause crash
	}
	/**
	 * 取得書本下載連結
	 * @param deliverID deliverID
	 */
	public String getDownloadBookURL(String deliverID){
		if (DEBUG) Log.e("flw", "+getDownloadBookURL ") ;
		if(deviceID.length() == 0){
			return null;
		}
		return downloadBookUrl+deliverID+"&device_id="+deviceID+"&token="+RealBookcase.getToken()+"&pointer=";
	}
	/**
	 * 按鍵按下事件
	 * @param keyCode keyCode
	 * @param event event
	 * @return 事件是否被處理  
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (DEBUG) Log.e("flw", "+onKeyDown ["+keyCode+"]") ;
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(rl_tools_alert_dialog!=null){
				if(rl_tools_alert_dialog.getVisibility()==View.VISIBLE){
					rl_tools_alert_dialog.setVisibility(View.GONE);
					setViewComponentEnabled(true);
					return true;
				}	
			} else {
				exit();
			}
		}		
		return super.onKeyDown(keyCode, event);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if ( false == isDataReady() ){
			if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
    			if(event.getAction()==KeyEvent.ACTION_UP)
    			{
    		   		if (DEBUG) Log.e("flw", "+dispatchKeyEvent ") ;
//    				Toast.makeText(TWMBook.this, getResources().getString(R.string.data_prepare),  
//    	            Toast.LENGTH_SHORT).show(); 

    				return true;
    			}
    		}    		
     		return true;
    	}else {
    		return super.dispatchKeyEvent(event);
    	}
       	
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

    	if ( false == isDataReady() ){
    		if(ev.getAction() == ev.ACTION_UP)
    		{
    			if (DEBUG) Log.e("flw", "+dispatchTouchEvent ="+ev.getAction()) ;
//    			Toast.makeText(TWMBook.this, getResources().getString(R.string.data_prepare),  
//		            Toast.LENGTH_SHORT).show();     		

    			return true;
    		}
    		return true;
    	}else{
    		return super.dispatchTouchEvent(ev);
    	}
    		
	}

	/**
	 * 檢查存放路徑
	 */
	public void isInnerSD(){		
		if (DEBUG) Log.e("flw", "+isInnerSD ") ;
		 File a = new File(getExternalFilesDir(null), "twmebook");
		 saveFilelocation = a.getPath() + "/";
		 new File(saveFilelocation).mkdir();
		 
		 File b = new File(saveFilelocation);
		if ( !b.exists() ||  !b.isDirectory()) {
			saveFilelocation = getBaseContext().getFilesDir().toString() + "/";
		 	new File(saveFilelocation).mkdir();
		}

		stat = new StatFs(saveFilelocation);
		calFreeSize();
	}
	/**
	 * 重新計算剩餘空間
	 */
	private void calFreeSize() {
		if (DEBUG) Log.e("flw", "+calFreeSize ") ;
		int size = stat.getBlockSize();
		int num = stat.getAvailableBlocks();
		freeSize = (long)num * size ;
	}
	/**
	 * 檢查p12是否存在
	 * @param path p12路徑
	 * @return p12是否存在  
	 */
	public Boolean isP12Exist(String path){
		if (DEBUG) Log.e("flw", "+isP12Exist ") ;
		return GSiMediaRegisterProcess.isP12Exist(path);
	}
	/**
	 * 取得register所回傳的資料  
	 */
	/*private void register(String path){
		if (DEBUG) Log.e("flw", "+register ") ;
		try {
			DataClass dataclass = GSiMediaRegisterProcess.register(path, TWMBook.this,RealBookcase.getToken());
			if(dataclass !=  null) {
				String disString = "";
				if(dataclass.resultCode_P12 != null){
					disString += "resultCode_P12= ";
					disString += dataclass.resultCode_P12;
				}
				if(dataclass.resultCode_Domain != null){
					disString += ", resultCode_Domain= ";
					disString += dataclass.resultCode_Domain;
				}
				if(dataclass.domain != null){
					disString += ", domain= ";
					disString += dataclass.domain;
				}
				if(dataclass.index != null && dataclass.index.length > 0){
					disString += ", index[0]= ";
					disString += dataclass.index[0];
				}
				if(dataclass.type != null && dataclass.type.length > 0){
					disString += ", type[0]= ";
					disString += dataclass.type[0];
				}
				if(dataclass.index != null && dataclass.index.length > 1){
					disString += ", index[1]= ";
					disString += dataclass.index[1];
				}
				if(dataclass.type != null && dataclass.type.length > 1){
					disString += ", type[1]= ";
					disString += dataclass.type[1];
				}
				if(dataclass.index !=null && dataclass.index.length > 2){
					disString += ", index[2]= ";
					disString += dataclass.index[2];
				}
				if(dataclass.type != null && dataclass.type.length > 2){
					disString += ", type[2]= ";
					disString += dataclass.type[2];
				}
				 Log.e("disString 234", disString);
				checkResultCode(1, dataclass.resultCode_P12,dataclass.resultCode_Domain,dataclass.index,dataclass.type);
			}else {
				Log.d("dataclass index =","null");
			}
		} catch (IllegalNetworkException e) {
			// TODO Auto-generated catch block
			//threadTestttMsg(getResources().getString(R.string.iii_NetworkNotConnMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (TimeOutException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_ServerTimeOutMessage));
			unOnline = false;
			e.printStackTrace();
		} catch (XmlP12FileException e) {
			// TODO Auto-generated catch block		
			threadTestttMsg(getResources().getString(R.string.iii_get_p12_error));
			unOnline = false;
			e.printStackTrace();
		} catch (DeviceIDException e) {
			threadTestttMsg(getResources().getString(R.string.iii_GetDeviceIDErrorMessage));
			unOnline = false;
			e.printStackTrace();	
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_ServerReturnErrorMessage));
			unOnline = false;
			e.printStackTrace();	
		}catch (Exception e) {
			// TODO Auto-generated catch block
			threadTestttMsg(getResources().getString(R.string.iii_UnknowMessage));
			unOnline = false;
			e.printStackTrace();
		}	
	}
	*/
	/**
	 * 取得是否有網路連接
	 * @return 是否有網路連接
	 */
	private Boolean testNetwork() {
		if (DEBUG) Log.e("flw", "+testNetwork ") ;
//		TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);	
//		return telMgr.getDataState() == TelephonyManager.DATA_CONNECTED;
	     NetworkInfo info = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	        if (info==null || !info.isConnected()) {
	                return false;
		}
	        return true; 		
	}
	/**
	 * 將全部的書本資料設為已下載
	 */
	public void setAllDownload(){
		if (DEBUG) Log.e("flw", "+setAllDownload ") ;
		cursorDBData = tdb.select();
		Log.d("cursorDBData()", Integer.toString(cursorDBData.getCount()));
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			tdb.update(cursorDBData.getInt(0), "isdownloadbook", "1");
			cursorDBData.moveToNext();
		}
//vic		cursorDBData.close();
		initList();
	}
	/**
	 * 將全部的書本資料加入手機資料庫中
	 */
	private void insertDB() {
		if (DEBUG) Log.e("flw", "+insertDB ") ;
		//cursorDBDataTemp = tdb.selectOrderBy("isread = '0'","buytime"+" DESC" );
		//cursorDBDataTemp2 = tdb.select("isread = '0'");
		//upDataCount = cursorDBDataTemp.getCount();
		//showAlertMessage("upDataCount"+String.valueOf(upDataCount));
		for(int i=0;i<ebook_title.size();i++){
			//int tag = ebook_cover.get(i).toString().lastIndexOf("/");
			//String fileName = ebook_cover.get(i).toString().substring(tag + 1, ebook_cover.get(i).toString().length());	
			String tempTypeCover ,tempType;
			if(ebook_type.get(i).toString().equals(getResources().getString(R.string.iii_mebook))){
				tempTypeCover = "ivi_nonepict02";
				tempType = fileType[1];
				
			}
			//pdf
			else if(ebook_bodytype_code.get(i).toString().equals("202")||ebook_bodytype_code.get(i).toString().equals("212"))
			{
				tempTypeCover = "ivi_nonepict01";//chang to PDF icon
				tempType = fileType[2];		
			}
			else{
				tempTypeCover = "ivi_nonepict01";
				tempType = fileType[0];
			}
			Cursor  c = tdb.select("deliveryID = '"+ebook_deliveryID.get(i).toString()+"'");
			if(c.getCount()==0)
			//	if( !ebook_bodytype_code.get(i).toString().equals("202") && !ebook_bodytype_code.get(i).toString().equals("212"))
					tdb.insert(ebook_title.get(i).toString(),ebook_type.get(i).toString(),ebook_category.get(i).toString(),
						ebook_cover.get(i).toString(),ebook_deliveryID.get(i).toString(),"0",
						String.valueOf(System.currentTimeMillis()),"0",tempTypeCover,
						ebook_purchased_at.get(i).toString(),ebook_publisher.get(i).toString(),ebook_authors.get(i).toString(),
						ebook_trial.get(i).toString(),"0",ebook_contentID.get(i).toString(),
						ebook_update_date.get(i).toString(),ebook_vertical.get(i).toString(),ebook_trial_due_date.get(i).toString(),
						saveFilelocation + ebook_deliveryID.get(i).toString()+tempType,"0",tempType,
						"");
//vic			c.close();
		}
		//Log.d("ebook_title.size()", Integer.toString(ebook_title.size()));
		//tdb.close();
		//showAlertMessage("ebook_title.size()"+String.valueOf(ebook_title.size()));
	}

	/**
	 * 下載圖片
	 * @param url 網址
	 * @param dID 書本在資料庫的id
	 * @param org 無作用
	 */
    private void downloadImage(String url , String dID, String org){
    	if (DEBUG) Log.e("flw", "+downloadImage ") ;
//   		try {
//   			//cursorDBDataTemp = tdb.select("_id = "+id);
//   			//cursorDBDataTemp.moveToFirst();
//   			URL myURL = new URL( getResources().getString(R.string.iii_cms_cover) + url );   			
//   			int tag = myURL.getFile().toString().lastIndexOf("/");
//   			InputStream conn = myURL.openStream();
//   			String fileName = myURL.getFile().toString().substring(tag + 1, myURL.getFile().toString().length());
//   			FileOutputStream fos = new FileOutputStream(saveFilelocation+fileName);
//   			byte[] buf = new byte[1024];
//   			while (true) {
//   				int bytesRead = conn.read(buf);
//   				if (bytesRead == -1)
//   					break;
//   				fos.write(buf, 0, bytesRead);
//   			}
//   			conn.close();
//   			fos.close();
//   			tdb.updateByDeliveryId(dID , "coverpath" , saveFilelocation+fileName);	
//   			mybl.setEbook_cover(dID, saveFilelocation+fileName);
//   			//reListImage(Integer.valueOf(id));	
//   		} catch(Exception e) {
//   			//tdb.updateByDeliveryId(dID , "coverpath" , org);	
//   			//mybl.setEbook_cover(dID, org);
//   		}
    }
	/**
	 * 設定畫面事件
	 */
    private void setListener() {
		ib_buy.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(getResources().getString(R.string.iii_book_city_url))) );
				exit();
			}
        });
		// 110504 add for login button
		ib_login.setOnClickListener(new ImageButton.OnClickListener(){

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(TWMBook.this)
				.setTitle(R.string.iii_relogin)
				.setMessage(R.string.iii_relogin_confirm)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								String deviceID = "";
								try{
									deviceID = GSiMediaRegisterProcess.getID(getApplicationContext());
								}catch(Throwable e){};
						    	if (deviceID == null) deviceID = "";
						    	
						    	if(deviceID.length() == 0){
						    		threadTestttMsg2(getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG));
						    	}else{
								LogoutTask aTask = new LogoutTask();
								aTask.execute(deviceID);
							}
						}
						}
				)
				.setNegativeButton(R.string.iii_showAM_cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){

							}
					}
				)				
				.show();	
				
			}
			
		});
		ib_edit.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  		if (DEBUG) Log.d("vic", "edit click");
      	  		
//      	  		ib_edit.setEnabled(false);
//      	  		ib_edit.postDelayed(new Runnable(){ 
//					@Override 
//					public void run() { 
//						ib_edit.setEnabled(true);
//					} 
//				},300);      	  		
      	  		
      	  		
//      			for(int i=0;i<dp.size();i++){
//      				dp.get(i).setCancel(true);
//      			}
      	  		
      	  		for (DownloadPbar dpbar : dp){
      	  			dpbar.setCancel(true);
      	  		}
      	  		
//      	  		for (DownloadPbar dpbar : dp){
//      	  			try {
//						dpbar.join(100);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//      	  		}      	  		
      			
      		    dp = new ArrayList<DownloadPbar>();
      		    //dp_num = new ArrayList<Integer>(); 
//      		    resetNowDownloadNum();
      	  		ib_del_top.setVisibility(View.VISIBLE);
      	  		ib_del_bottom.setVisibility(View.VISIBLE);
      	  		ib_all_select.setVisibility(View.VISIBLE);
      	  		ib_all_unselect.setVisibility(View.VISIBLE);
      	  		rl_edit_mode.setVisibility(View.VISIBLE);
      	  		rl_center.setVisibility(View.INVISIBLE);
      	  		ib_edit.setVisibility(View.INVISIBLE);
      	  		ib_del_bottom.setEnabled(false);
      	  		setListOrEdit(1,true);
      	  		isEditModel = true;
      	  		ib_realbook.setVisibility(View.INVISIBLE);
      	  		ib_listbook.setVisibility(View.INVISIBLE);
      	  	}
        });
		ib_realbook.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
			//check listbook is open
			exit();
		
			}
		});		
		ib_tools.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}

      	  		
      	  		ib_tools.setEnabled(false);
      	  		ib_tools.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_tools.setEnabled(true);
					} 
				},400);          	  		
      	  		
      	  		toolsAlert();
      	  		
      	  	// for gsi PDF testing
//				Intent it = null;
//				it= new Intent(TWMBook.this,RendererActivity.class);
//			 	it.setData(Uri.parse(Config.FILESCHEME+"://"+Environment.getExternalStorageDirectory()+"/test.pdf"));	
//			 	startActivity(it);      	  	      	  		
      	  	}	
        });       
		ib_read.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  		if (DEBUG) Log.d("vic", "read click");
      	  		
      	  		ib_read.setEnabled(false);
      	  		ib_read.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_read.setEnabled(true);
					} 
				},400);         	  		
      	  		
      	  		setUnRead(); 
      	  		ib_up_page.setVisibility(View.GONE);
      	  		ib_edit.setVisibility(View.VISIBLE);
      	  		setListOrEdit(1,false);
      	  	}
        });		
		ib_readed.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  	if (DEBUG) Log.d("vic", "readed click");
      	  		
      	  		ib_readed.setEnabled(false);
      	  		ib_readed.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_readed.setEnabled(true);
					} 
				},400);       	  		
      	  		
      	  		setRead();    		
      	  		ib_up_page.setVisibility(View.GONE);
      	  		ib_edit.setVisibility(View.VISIBLE);
      	  		setListOrEdit(2,false);
      	  	}
		});
		ib_all.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				
				if ( false == isDataReady() ){
					return ;
				}
				
				if (DEBUG) Log.d("vic", "all click");
				
				ib_all.setEnabled(false);
				ib_all.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_all.setEnabled(true);
					} 
				},400);  				
				
				nowStyle = 3;
      	  		ib_read.setImageResource(R.drawable.ivi_button10b);
      	  		ib_readed.setImageResource(R.drawable.ivi_button11b);
      	  		ib_all.setImageResource(R.drawable.ivi_button12a); 
      	  		ib_del_top.setVisibility(View.GONE);
      	  		ib_edit.setVisibility(View.INVISIBLE);
      		  	setAllList();
      	  	}
		});
		ib_up_page.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  		ib_up_page.setEnabled(false);
      	  		ib_up_page.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_up_page.setEnabled(true);
					} 
				},400);       	  		
      	  		
      	  		//bl.setAllIsEdit(1);      	  		
      	  		//showAlertMessage(String.valueOf(bl.getItem(0)));
      	  		//bl.getViewHolder().cb.setChecked(true);
      	  		//showAlertMessage(String.valueOf(allCheckBox.size()));
				nowStyle = 3;
      	  		ib_read.setImageResource(R.drawable.ivi_button10b);
      	  		ib_readed.setImageResource(R.drawable.ivi_button11b);
      	  		ib_all.setImageResource(R.drawable.ivi_button12a); 
      		  	setAllList();      	  		
      		  	ib_up_page.setVisibility(View.GONE);
      	  		
      	  		//showAlertMessage(String.valueOf(allCheckBoxValue.length));      	  		
      	  	}
		});
		ib_del_top.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  		if ( DEBUG ) Log.e("vic", "cancel edit click");
      	  		
//      	  		ib_del_top.setEnabled(false);
//      	  		ib_del_top.postDelayed(new Runnable(){ 
//					@Override 
//					public void run() { 
//						ib_del_top.setEnabled(true);
//					} 
//				},300);       	  		
      	  		
      	  		//showAlertMessage("離開");
      	  		downloadStatus = true;
      	  		//setListOrEdit(1,true);
      	  		if(nowStyle==1){
          	  		ib_read.setImageResource(R.drawable.ivi_button10a);
          	  		ib_readed.setImageResource(R.drawable.ivi_button11b);
          	  		ib_all.setImageResource(R.drawable.ivi_button12b); 
          	  		ib_up_page.setVisibility(View.GONE);
          	  		rl_edit_mode.setVisibility(View.GONE);
          	  		rl_center.setVisibility(View.VISIBLE);          	  		
          	  		setListOrEdit(1,false);      	  			
      	  		}else if(nowStyle==2){
          	  		setRead();    		
          	  		ib_up_page.setVisibility(View.GONE);
          	  		rl_edit_mode.setVisibility(View.GONE);
          	  		rl_center.setVisibility(View.VISIBLE);         	  		
          	  		setListOrEdit(2,false);      	  			
      	  		}else if(nowStyle==3){
          	  		ib_read.setImageResource(R.drawable.ivi_button10b);
          	  		ib_readed.setImageResource(R.drawable.ivi_button11b);
          	  		ib_all.setImageResource(R.drawable.ivi_button12a); 		
          	  		ib_up_page.setVisibility(View.GONE);
          	  		rl_edit_mode.setVisibility(View.GONE);
          	  		rl_center.setVisibility(View.VISIBLE);         	  		
          	  		setListOrEdit(3,false);
      	  		}
      	  		ib_del_top.setVisibility(View.GONE);
      	  		ib_edit.setVisibility(View.VISIBLE);
      	  		lv_main.invalidateViews();
      	  		downloadStatus = false;
      	  		isEditModel = false;
      	  		ib_realbook.setVisibility(View.VISIBLE);
      	  		ib_listbook.setVisibility(View.VISIBLE);
      	  		//lv_main.invalidate();
      	  		//lv_main.invalidateViews();
      	  	}
		});
		ib_del_bottom.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		//showAlertMessage(String.valueOf(allCheckBoxValue.length));
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  		ib_del_bottom.setEnabled(false);
      	  		ib_del_bottom.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_del_bottom.setEnabled(true);
					} 
				},400);  
      	  		
				new AlertDialog.Builder(TWMBook.this)
				.setTitle(R.string.iii_check_del_book)
				.setMessage(R.string.iii_check_del_message)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								//pDialog = ProgressDialog.show(TWMBook.this, "", getResources().getString(R.string.iii_setting_deling));
								//pDialog.setCancelable(false);
								//new Thread(){
									//public void run(){
										if(nowStyle == 1){//未讀			
											cursorDBData = tdb.selectOrderBy("isread = '0'","buytime"+" DESC" );
										}else if(nowStyle == 2){//已讀
											cursorDBData = tdb.selectOrderBy("isread = '1'","lastreadtime"+" DESC" );
										}	
						      			cursorDBData.moveToFirst();
						      	  		for(int j=0;j<allCheckBoxValue.length;j++){
						      	  			if(allCheckBoxValue[j]==true){
						      	  				tdb.delete(cursorDBData.getInt(0));
						      	  				delDeviceBook(cursorDBData.getString(2),cursorDBData.getString(5),cursorDBData.getString(21));
						      	  				long tempHeapSize =0;
						      	  				tempHeapSize = downloadHeapSize;
						      	  				downloadHeapSize = downloadHeapSize - cursorDBData.getLong(20);
						      	  				if(downloadHeapSize < 0	)
						      	  					downloadHeapSize = tempHeapSize;
						      	  				new File(cursorDBData.getString(9)).delete();
						      	  				new File(cursorDBData.getString(19)).delete();
						      	  				new File(cursorDBData.getString(19)+".tmp").delete();
						      	  				new File(cursorDBData.getString(19).substring(0, cursorDBData.getString(19).lastIndexOf(".")) + ".epub").delete();
/*												SyBookmark bm = new SyBookmark(TWMBook.this, 0);
												bm.delBookmark(cursorDBData.getString(5));
											    AnnotationDB adb = new AnnotationDB(getBaseContext());
											    adb.deleteAnnByEpubPath(cursorDBData.getString(5));
											    adb.closeDB();
											    Bookmarks bms= new Bookmarks(getBaseContext());
											    bms.deleteBookmarksByEpubPath(cursorDBData.getString(5));
											    bms.closeDB();
											    UnderlineDB ul= new UnderlineDB(getBaseContext());
											    ul.deleteUnderlineByEpubPath(cursorDBData.getString(5));
											    ul.closeDB(); 	*/	
						      	  			}
						      	  			cursorDBData.moveToNext();
						      	  		}
										//Message m = new Message();
										//Bundle data = m.getData();
										//data.putString("msg", getResources().getString(R.string.iii_setting_del_done));
										//testtt.sendMessage(m);	
									//}										
								//}.start();
				      	  		setListOrEdit(1,true);
				      	  		calFreeSize();
							}
						}
				)
				.setNegativeButton(R.string.iii_showAM_cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){

							}
					}
				)				
				.show();      	 
      	  	}
		});
		ib_all_select.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  		ib_all_select.setEnabled(false);
      	  		ib_all_select.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_all_select.setEnabled(true);
					} 
				},400);
      	  		
      	  		for(int i=0;i<allCheckBoxValue.length;i++){
      	  			allCheckBoxValue[i]=true;
      	  		}
      	  		lv_main.invalidateViews();
      	  		ib_del_bottom.setEnabled(true);
      	  	}
		});
		ib_all_unselect.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		
				if ( false == isDataReady() ){
					return ;
				}
      	  		
      	  		ib_all_unselect.setEnabled(false);
      	  		ib_all_unselect.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						ib_all_unselect.setEnabled(true);
					} 
				},400);      	  		
      	  		
      	  		for(int i=0;i<allCheckBoxValue.length;i++){
      	  			allCheckBoxValue[i]=false;
      	  		}
      	  		lv_main.invalidateViews();
      	  		ib_del_bottom.setEnabled(false);
      	  	}
		});				
		lv_main.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				if ( false == isDataReady() ){
					return true;
				}
				
				boolean isLongClick = false; 
      	  		int num = 0;
      	  		boolean isClickDown = false; 
      	  		for(int i=0;i<dp.size();i++){
      	  			if (arg2 == dp.get(i).getArg()){
      	  				num = i;
      	  				isClickDown = true;
      	  			}
      	  		}
      	  		if(isClickDown == true){
          	  		setNowDownloadNum(false);
    				dp.get(num).setCancel(true);
//    				try {
//						dp.get(num).join(100);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
      	  		}	
				
				if (nowStyle == 4){
					isLongClick = true;
					Log.e("isLongClick 4", " 4 4 4 4 4 4 4");
				}else if (nowStyle == 3){
					isLongClick = true;
					Log.e("isLongClick 3", " 3 3 3 3 333 3 3 ");
				}else{
					if(nowStyle == 1){//未讀			
						cursorDBData = tdb.selectOrderBy("isread = '0'","buytime"+" DESC" );
					}else if(nowStyle == 2){//已讀
						cursorDBData = tdb.selectOrderBy("isread = '1'","lastreadtime"+" DESC" );
					}					
					
					cursorDBData.moveToPosition(arg2);
					isLongClick = true;
					String[] item = {getResources().getString(R.string.iii_long_click_del),getResources().getString(R.string.iii_long_click_score)};

					new AlertDialog.Builder(TWMBook.this)
					  .setItems(item, 
						  new DialogInterface.OnClickListener(){
							 public void onClick(DialogInterface dialog, int whichcountry) {
									 switch(whichcountry) {
										 case 0:	
												new AlertDialog.Builder(TWMBook.this)
												.setTitle(R.string.iii_check_del_book)
												.setMessage(R.string.iii_check_del_message)
												.setPositiveButton(R.string.iii_showAM_ok,
														new DialogInterface.OnClickListener(){
															public void onClick(DialogInterface dialoginterface, int i){												      	  		
										      	  				tdb.delete(cursorDBData.getInt(0));
										      	  				delDeviceBook(cursorDBData.getString(2),cursorDBData.getString(5),cursorDBData.getString(21));
										      	  				new File(cursorDBData.getString(9)).delete();
										      	  				new File(cursorDBData.getString(19)).delete();
										      	  				new File(cursorDBData.getString(19)+".tmp").delete();
										      	  				new File(cursorDBData.getString(19).substring(0, cursorDBData.getString(19).lastIndexOf(".")) + ".epub").delete();
												      	  		setListOrEdit(1,false); 
												      	  		calFreeSize();
															}
														}
												)
												.setNegativeButton(R.string.iii_showAM_cancel,
														new DialogInterface.OnClickListener(){
															public void onClick(DialogInterface dialoginterface, int i){
															}
													}
												)				
												.show(); 
											 break;								 
										 case 1:
											 startActivity((new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(getResources().getString(R.string.iii_book_score_url)+cursorDBData.getString(15))));
											 break;							 
									 }						
							 }				 
						  }
					  ).setCancelable(true)
				      .show();
				}
				return isLongClick;
			}
		});
		
		// nowStyle, 
		// 1:未讀,2:已讀,3:大分類,4:小分類
		lv_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				if ( false == isDataReady() ){
					return ;
				}
				
//				//to avoid arg2 not match db size
//				if(cursorDBData.getCount() < (arg2+1)){
//					mybl.reDraw();
//					return;
//				}
//				
				
				
				if(!isEditModel){				
					//Toast.makeText(TWMBook.this," temptv.getText =  "+mainRow[arg2].cancel.getVisibility(), Toast.LENGTH_SHORT).show();
					//System.out.println(temptv.getText().toString());
					Log.e("345", "in");
					if (nowStyle == 4){		// 點選小分類，進行開書

						if(arg2 > 0){
							//ib_del_top.setVisibility(View.VISIBLE);

							cursorDBData = tdb.select("deliveryID = '"+bla.getListItemClickBookDeliveryID(arg2)+"'");	

							if(cursorDBData.getCount()>0){
								cursorDBData.moveToFirst();
								switch(Integer.valueOf(cursorDBData.getString(8))) { 
//								case 0: 		            		
//									//testStartDownload(arg2,getDownloadBookURL(cursorDBData.getString(5)),saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(2));
//									//	lv_main.invalidateViews();
//									Log.e("", "123");
//									break; 
								case 1: 		   
									lv_main.setEnabled(false);
									lv_main.postDelayed(new Runnable(){ 
										@Override 
										public void run() { 
											lv_main.setEnabled(true);
										} 
									},400);
									
									
									File f = new File(cursorDBData.getString(19));
									if ( false == f.exists() ){
										String url = getDownloadBookURL(cursorDBData.getString(5));
//										if(url == null){
//											threadTestttMsg2(getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG));
//										}else{
											testStartDownload(arg2,url,saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(21),0);
//										}
									}else {
									
										Log.e("", "openBook");
					            		tdb.update(cursorDBData.getInt(0), "lastreadtime", String.valueOf(System.currentTimeMillis()));
					            		tdb.update(cursorDBData.getInt(0) , "isread" , "1" );	
	
					            		openBook(cursorDBData.getString(2),cursorDBData.getString(19),
					            				p12Path,cursorDBData.getString(13),cursorDBData.getString(9),
					            				settings.getBoolean("setting_auto_sync_last_read_page_value", true),cursorDBData.getString(15),
					            				cursorDBData.getString(1),cursorDBData.getString(12),cursorDBData.getString(11),cursorDBData.getString(3),cursorDBData.getString(17),cursorDBData.getString(21));   		
									}
				            		
				            		break; 
//								case 2: 		            		
//									//testStartDownload(arg2,getDownloadBookURL(cursorDBData.getString(5)),saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(2));
//									//	lv_main.invalidateViews();
//									break;	
//								case 3: 		            		
//									//testStartDownload(arg2,getDownloadBookURL(cursorDBData.getString(5)),saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(2));
//									//lv_main.invalidateViews();
//									break;			     
								}							
							}
		            											
							//showAlertMessage("開啟書本-- 路徑為 "+saveFilelocation + cursorDBData.getString(5) + fileType);
						}	
	/*					setAllListDetail(groupPosition,childPosition);
						ib_up_page.setVisibility(View.VISIBLE);
						nowStyle = 4;	*/				
					}else if (nowStyle == 3){

						if(bla.getTempAllTag(arg2).equals("1")){
							nowStyle = 4;
							setAllListDetail(arg2);
							//tempAllListArg = arg2;
							ib_edit.setVisibility(View.INVISIBLE);
							ib_up_page.setVisibility(View.VISIBLE);
						} else if (bla.getTempAllTag(arg2).equals("5") ){


							String url = mBrandURLs.get(arg2-1);
							boolean avaliable = isIntentAvailable(TWMBook.this , url);
							if ( !avaliable ){
								url = mBrandFailURLs.get(arg2-1);
							}
							
							startActivity((new Intent()).setAction(
									Intent.ACTION_VIEW).setData(Uri.parse(url)));
							
						}
					}else{
						
						Cursor cReaddb = null;
						Cursor cUnReaddb = null;
						Cursor cdb = null;
						Log.e("345", "nowstyle =>"+nowStyle+ "arg=> "+arg2);
							if(nowStyle == 1){//未讀		
								
								cUnReaddb = tdb.selectOrderBy("isread = '0'","buytime"+" DESC" );
								cUnReaddb.moveToPosition(arg2);
								cdb = cUnReaddb;
							}else if(nowStyle == 2){//已讀
		
								cReaddb = tdb.selectOrderBy("isread = '1'","lastreadtime"+" DESC" );
								cReaddb.moveToPosition(arg2);
								cdb = cReaddb;
							}
							else{//if
								//allen log
						
								cUnReaddb = tdb.selectOrderBy("isread = '0'","buytime"+" DESC" );
								cUnReaddb.moveToPosition(arg2);
								cdb = cUnReaddb;
							}
								
							Log.e("345", "cdb =>"+cdb.getCount());
//					cursorDBData.moveToFirst();
//					for(int i=0;i<arg2;i++){
//						cursorDBData.moveToNext(); 
//					}
					//cursorDBData.moveToPosition(arg2);

					//showAlertMessage(String.valueOf(cursorDBData.getString(0)));
						if(cdb.getCount() <= (arg2)){
							//initList();
							//mybl.reDraw();
							return;
						}
							
							
						switch (Integer.valueOf(cdb.getString(8))) {
						case 0:

							testStartDownload(arg2,
									getDownloadBookURL(cdb.getString(5)),
									saveFilelocation, cdb.getString(5),
									cdb.getInt(0), cdb.getString(21), 0);
							// lv_main.invalidateViews();
							break;
						case 1:
							lv_main.setEnabled(false);
							lv_main.postDelayed(new Runnable() {
								@Override
								public void run() {
									lv_main.setEnabled(true);
								}
							}, 400);

							File f = new File(cdb.getString(19));
							if (false == f.exists()) {
								testStartDownload(arg2,
										getDownloadBookURL(cdb.getString(5)),
										saveFilelocation, cdb.getString(5),
										cdb.getInt(0), cdb.getString(21), 0);
							} else {

								tdb.update(cdb.getInt(0), "lastreadtime",
										String.valueOf(System
												.currentTimeMillis()));
								tdb.update(cdb.getInt(0), "isread", "1");
								// Log.e("Boolean.valueOf(cursorDBData.getString(13))",
								// String.valueOf(Boolean.valueOf(cursorDBData.getString(13))));
								// Log.e("cursorDBData.getString(13))",
								// cursorDBData.getString(13));

								Log.e("cdb.getString(5) id", cdb.getString(5));
								openBook(
										cdb.getString(2),
										cdb.getString(19),
										p12Path,
										cdb.getString(13),
										cdb.getString(9),
										settings.getBoolean(
												"setting_auto_sync_last_read_page_value",
												true), cdb.getString(15), cdb
												.getString(1), cdb
												.getString(12), cdb
												.getString(11), cdb
												.getString(3), cdb
												.getString(17), cdb
												.getString(21));
							}
							break;
						case 2:
							testStartDownload(arg2,
									getDownloadBookURL(cdb.getString(5)),
									saveFilelocation, cdb.getString(5),
									cdb.getInt(0), cdb.getString(21), 1);
							// lv_main.invalidateViews();
							break;
						case 3:
							testStartDownload(arg2,
									getDownloadBookURL(cdb.getString(5)),
									saveFilelocation, cdb.getString(5),
									cdb.getInt(0), cdb.getString(21), 1);
							// lv_main.invalidateViews();
							break;
						}
					}
				}
			}
		});
	}
    
    
	private boolean isDataReady() {
		boolean b = false ;
		synchronized (lock){
			b = isListBookReady;
		}

    	if(!b){
//			Toast.makeText(TWMBook.this, getResources().getString(R.string.data_prepare),  
//    	            Toast.LENGTH_SHORT).show(); 
    		if(mPrepareDlg !=null)
    		{
    		mPrepareDlg = ProgressDialog.show(TWMBook.this, "", 
    				getResources().getString(R.string.data_prepare), true);	
    		}
    	}
    	return b ;
	}    
    
	public static boolean isIntentAvailable(Context context, String url) {
		if (DEBUG) Log.e("flw", "+isIntentAvailable ") ;
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(url));
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}    
    
    
	/**
	 * 刪除書本
	 * @param type 書本種類
	 * @param deliveryid deliveryid
	 */
    private void delDeviceBook(String type,String deliveryid,String fileformat) {	
    	if (DEBUG) Log.e("flw", "+delDeviceBook ") ;
    	Log.e(type, deliveryid);

    	Context ctx = getBaseContext();
    	if(fileformat.equals(fileType[1])){
 			new File(saveFilelocation + deliveryid + fileType[1]).delete();
			SyBookmark bm = new SyBookmark(TWMBook.this, 0);
			bm.delBookmark(deliveryid);
			
			AnReader.deleteLastPageOfBook(ctx, deliveryid);
 		}
    	//PDF
    	if(fileformat.equals(fileType[2]))
    	{
    		GSiDatabaseAdapter.delBookmark(ctx, deliveryid);
    		GSiDatabaseAdapter.delAnnotation(ctx, deliveryid);
    		GSiDatabaseAdapter.delMarker(ctx, deliveryid);
    		GSiDatabaseAdapter.deleteLastPageOfBook(ctx, deliveryid);
    	}
    	
	    AnnotationDB adb = new AnnotationDB(ctx);
	    adb.deleteAnnByEpubPath(deliveryid);
	    adb.closeDB();
	    Bookmarks bm= new Bookmarks(ctx);
	    bm.deleteBookmarksByEpubPath(deliveryid);
	    bm.closeDB();
	    UnderlineDB ul= new UnderlineDB(ctx);
	    ul.deleteUnderlineByEpubPath(deliveryid);
	    ul.closeDB();
	    LastPageHelper.deleteLastPageOfBook(this, deliveryid);
	}	
	/**
	 * 開啟書本
	 * @param type 有書書  或 epub
	 * @param coverPath 檔案路徑
	 * @param p1 p12 p12路徑
	 * @param p2 isSample 是否試閱
	 * @param p3 coverPath 圖片路徑
	 * @param p4 syncLastPage 同步最後閱讀頁
	 * @param p5 content_id 書本content id
	 * @param p6 book_title 書本名稱
	 * @param p7 book_authors 書本作者
	 * @param p8 book_publisher 書本出版社
	 * @param p9 book_category 書本類別
	 * @param p10 book_vertical 書本是否垂直觀看
	 */
	private void openBook(String type,String coverPath,String p1,String p2,String p3,Boolean p4,String p5,String p6,String p7,String p8,String p9,String p10, String p11) {
		if (DEBUG) Log.e("flw", "+openBook ") ;
		//this.showAlertMessage("開啟書本-- 路徑為 "+coverpath);		
 		Intent it = null;
 		if(type.equals(getResources().getString(R.string.iii_mebook))){
 			it= new Intent(TWMBook.this,AnReader.class);
 			it.setData(Uri.parse("mebook://"+coverPath));			
		}else if(p11.equals(fileType[2])) {
			//PDF
    		
//    		it= new Intent(this,RendererActivity.class);
//    	 	it.setData(aUri);	
//    	 	it.putExtra(Config.KEY_p12,Environment.getExternalStorageDirectory().getPath());
//    	 	it.putExtra(Config.KEY_isSample, "0");
////    	 	it.putExtra(Config.KEY_coverPath, clickedFile.getAbsolutePath());
//    	 	it.putExtra(Config.KEY_coverPath, Environment.getExternalStorageDirectory()+"/cover.png");
//    	 	it.putExtra(Config.KEY_syncLastPage, true); 
//    	 	it.putExtra(Config.KEY_content_id, clickedFile.getPath());
//    	 	it.putExtra(Config.KEY_book_title, clickedFile.getName());
//    	 	it.putExtra(Config.KEY_book_authors, "test author");
//    	 	it.putExtra(Config.KEY_book_publisher, "test publisher");
//    	 	it.putExtra(Config.KEY_book_category, "test category");
//
//    	 	it.putExtra("book_vertical", false);
//
//    	 	startActivity(it);
			
			it= new Intent(TWMBook.this,RendererActivity.class);
 			it.setData(Uri.parse("pdf://"+coverPath));//"mnt/sdcard/sample.pdf"));	
 		
 		}else{
 			it= new Intent(TWMBook.this,Reader.class);
 			it.setData(Uri.parse("epub://"+coverPath));	
 		} 		
 		it.putExtra("p12",p1);
 		it.putExtra("isSample", p2);
 		it.putExtra("coverPath", p3);
 		it.putExtra("syncLastPage", p4);//同步最後閱讀頁
 		it.putExtra("content_id", p5);
 		it.putExtra("book_title", p6);
 		it.putExtra("book_authors", p7);
 		it.putExtra("book_publisher", p8);
 		it.putExtra("book_category", p9);
 		if(p10.equals("0"))
 			it.putExtra("book_vertical", false);
 		else
 			it.putExtra("book_vertical", true);
 		it.putExtra("book_token", RealBookcase.getToken());
		startActivity(it);
	
	}
	
	/**
	 * 觸發下載書本
	 * @param arg2 書本位置
	 * @param sf 網址
	 * @param loc 存放位置
	 * @param tfp 存檔名稱
	 * @param id 書本id 
	 * @param type 書本種類
	 * @param mod 當前模式
	 */
	public static class testStartDownloadInfo implements Parcelable{
		int arg2;
		String sf;
		String loc;
		String tfp;
		int id;
		String type;
		int mod;
		testStartDownloadInfo(int _arg2,String _sf,String _loc,String _tfp,int _id,String _type,int _mod){
			arg2 = _arg2;
			sf = _sf;
			loc = _loc;
			tfp = _tfp;
			id = _id;
			type = _type;
			mod = _mod;
		}
		@Override
		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(arg2);
			dest.writeString(sf);
			dest.writeString(loc);
			dest.writeString(tfp);
			dest.writeInt(id);
			dest.writeString(type);
			dest.writeInt(mod);
		}
		public void readFromParcel(Parcel source){
			arg2 = source.readInt();
			sf = source.readString();
			loc = source.readString();
			tfp = source.readString();
			id = source.readInt();
			type = source.readString();
			mod = source.readInt();
		}
		private testStartDownloadInfo(Parcel p){
			this.readFromParcel(p);
		}
		public static final Parcelable.Creator<testStartDownloadInfo> CREATOR = new Parcelable.Creator<testStartDownloadInfo>() {

			public testStartDownloadInfo createFromParcel(Parcel source) {
				return new testStartDownloadInfo(source);
			}

			public testStartDownloadInfo[] newArray(int size) {
				return new testStartDownloadInfo[size];
			}

		};
	}
	private void testStartDownload(final testStartDownloadInfo aInfo){
		this.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				testStartDownload(aInfo.arg2,aInfo.sf,aInfo.loc,aInfo.tfp,aInfo.id,aInfo.type,aInfo.mod);
			}
			
		});
	}
	private void testStartDownload(final int arg2,String sf,String loc,String tfp,int id,String type,int mod){	
		if (DEBUG) Log.e("flw", "+testStartDownload ") ;
		//       testStartDownload(arg2,getDownloadBookURL(cursorDBData.getString(5)),saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(2));
		try{
			if(getNowDownloadNum() < MAX_DOWNLOAD_NUM){
				
				// monkey
				ViewHolder vh;
				try {
					vh = mainRow[arg2];
				}
				catch (IndexOutOfBoundsException e){
					e.printStackTrace();
					vh = null;
				}
				if ( null == vh ){
					return ;
				}				
				setNowDownloadNum(true);
				//nowDownloadNum = nowDownloadNum + 1;
				//Log.e("now  nowDownloadNum", String.valueOf(nowDownloadNum));
				tdb.update(id , "isdownloadbook" , "2" );
				
								
//				synchronized (this) {
				pbarNowStatusList.set(arg2, "2");	
				//vh.nowStatus = "2";
				
					//Log.v("arg2","   "+String.valueOf(arg2)+"  ");
					//vh.pbar.setProgress(0);	
					vh.pbar.setVisibility(View.VISIBLE);
					vh.cancel.setVisibility(View.VISIBLE);	

//				}
			
				mybl.setEebook_isdownloadbook(arg2,"2");
			
				//lv_main.invalidateViews();
			
				DownloadPbar subDp;
		 		if(type.equals(fileType[0])){
		 			subDp = new DownloadPbar(sf,loc,tfp+fileType[0]+".tmp",id,mod,nowStyle);
		 		
		 		}else if(type.equals(fileType[1])){
		 			subDp = new DownloadPbar(sf,loc,tfp+fileType[1]+".tmp",id,mod,nowStyle);
		 		}else{
		 			subDp = new DownloadPbar(sf,loc,tfp+fileType[2]+".tmp",id,mod,nowStyle);
		 		}					
		 		subDp.setArg(arg2);
		 		long timeStamp = System.currentTimeMillis();
		 		subDp.setTimeStamp(timeStamp);
		 		addThreadMap(arg2, timeStamp);
		 		subDp.setInfo(new testStartDownloadInfo(arg2,sf,loc,tfp,id,type,mod));
			 	subDp.start();	
		 		dp.add(subDp);
		 		//dp_num.add(arg2);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		

 		
 		//if(dp_num.size()==0){
 			
 			//mybl.setEebook_isdownloadbook(arg2,"2");
 		

 		//}else{
 			//mybl.setEebook_isdownloadbook(arg2,"3");
 			//dp_num.add(id);
 		//}
 		//dp.get(dp.size()-1).setArg(arg2);
 		//dp.get(dp.size()-1).start();
		//subDp.setArg(arg2);
		//subDp.start();
		//lv_main.invalidateViews();
	}
	
	private long mLastUpdate = 0; 
	/**
	 * 更新下載進度條
	 */
	private Handler testHandlerDownloadBook = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Log.e("flw", "+testHandlerDownloadBook handleMessage") ;
			int percent = msg.getData().getInt("percent");
			int position = msg.getData().getInt("Position");
			int nowS = msg.getData().getInt("nowStyle");
			
			
/*			for(int i=0;i<pbarIndexList.size();i++){
				if(pbarIndexList.get(i)==position){
					pbarValueList.set(i, percent);
				}
			}*/
			if( nowStyle == nowS ){
			
				final ViewHolder vh = mainRow[position];
				if(vh!=null){
						pbarValueList.set(position, percent);
						long cur = System.currentTimeMillis();
						if (percent == 0 || cur - mLastUpdate >= 1500) {
							mLastUpdate = cur;
							lv_main.invalidateViews();
							//vh.pbar.invalidate();
						}

						if (percent == 100) {
							
							pbarNowStatusList.set(position, "1");
							mybl.setEebook_isdownloadbook(position, "1");
							lv_main.invalidateViews();
							//vh.pbar.invalidate();
						}
				}
			}
		}
	};
	/**
	 * 設定全部列表
	 * @param arg2
	 */
	private void setAllListDetail(int arg2) {
		if (DEBUG) Log.e("flw", "+setAllListDetail ") ;
/*		da = da.createFromPath(saveFilelocation+"listbackground.jpg");
		cursorDBData = tdb.select();
		//blad = new BookListAllDetail(this,cursorDBData,bla.getDetailTitle(groupPosition, childPosition),bla.getBookCount(groupPosition, childPosition));
		
		lv_main.setVisibility(View.VISIBLE);
		lv_main.setAdapter(blad);
		lv_main.setBackgroundDrawable(da);
	   	tdb.close();*/
		cursorDBData = tdb.select("isdownloadbook = '1'");	
	    bla = new myAllBookList(this,cursorDBData,tdb,bla.getTempAllCount(),bla.getTempAllTag(),arg2);
//vic	    cursorDBData.close();
		lv_main.setAdapter(bla);	    
	}
	/**
	 * 設定畫面UI 元件
	 */
	private void setViewComponent() {
		if (DEBUG) Log.e("flw", "+setViewComponent ") ;
		lv_main = (ListView)findViewById(R.id.lv_main);
/*		lv_main.setFooterDividersEnabled(false);
		lv_main.setHapticFeedbackEnabled(false);
		lv_main.setHeaderDividersEnabled(false);
		lv_main.setAlwaysDrawnWithCacheEnabled(false);
		lv_main.setAnimationCacheEnabled(false);
		lv_main.setDrawingCacheEnabled(false);
		lv_main.setSmoothScrollbarEnabled(false);
		lv_main.setSaveEnabled(false);
		lv_main.setScrollContainer(false);*/
		
		//iv_bookcase = (ImageView)findViewById(R.id.iv_bookcase);
		ib_buy = (ImageButton)findViewById(R.id.ib_buy);
		// 110504 add for login button
		ib_login = (ImageButton)findViewById(R.id.ib_login);
		ib_edit = (ImageButton)findViewById(R.id.ib_edit);
		ib_tools = (ImageButton)findViewById(R.id.ib_tools);
		ib_readed = (ImageButton)findViewById(R.id.ib_readed);
		ib_read = (ImageButton)findViewById(R.id.ib_read);
		ib_all = (ImageButton)findViewById(R.id.ib_all);
		ib_up_page = (ImageButton)findViewById(R.id.ib_up_page);
		ib_del_top = (ImageButton)findViewById(R.id.ib_del_top);
		ib_del_bottom = (ImageButton)findViewById(R.id.ib_del_bottom);
		ib_all_select = (ImageButton)findViewById(R.id.ib_all_select);
		ib_all_unselect = (ImageButton)findViewById(R.id.ib_all_unselect);
		ib_realbook =  (ImageButton)findViewById(R.id.ib_realbook);
		ib_listbook =  (ImageButton)findViewById(R.id.ib_listbook);
		rl_top = (RelativeLayout)findViewById(R.id.rl_top);
		rl_center = (RelativeLayout)findViewById(R.id.rl_center);		
		rl_edit_mode = (RelativeLayout)findViewById(R.id.rl_edit_mode);
		rl_main = (RelativeLayout)findViewById(R.id.rl_main);
		
		
		ib_tools.setBackgroundColor(Color.TRANSPARENT);

		
		ib_up_page.setBackgroundColor(Color.TRANSPARENT);	
		
  		setUnRead();  		
		
		ib_buy.bringToFront();
		// 110504 add for login button
		ib_login.bringToFront();
		ib_edit.bringToFront();
		ib_tools.bringToFront();
		ib_read.bringToFront();
		ib_readed.bringToFront();
		ib_all.bringToFront();
		ib_up_page.bringToFront();
		ib_del_top.bringToFront();
		ib_del_bottom.bringToFront();
		ib_all_select.bringToFront();
		ib_all_unselect.bringToFront();
		ib_realbook.bringToFront();
		ib_listbook.bringToFront();
		setStyle();
	}  
	/**
	 * 設定畫面主題
	 */
	public void setStyle(){
		if (DEBUG) Log.e("flw", "+setStyle ") ;
		settings = getSharedPreferences("setting_Preference", 0);
		if("".equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_main.setBackgroundResource(R.drawable.wood_ivi_bg01);
			rl_top.setBackgroundResource(R.drawable.wood_ivi_bar03);
			ib_buy.setBackgroundResource(R.drawable.wood_ivi_button08);
			// 110504 add for login button
			ib_login.setBackgroundResource(R.drawable.wood_ivi_button09);
			ib_edit.setBackgroundResource(R.drawable.wood_ivi_button09);
			ib_del_top.setBackgroundResource(R.drawable.wood_ivi_button09);
			rl_center.setBackgroundResource(R.drawable.wood_ivi_bar02);
			ib_readed.setBackgroundResource(R.drawable.wood_ivi_button11);
			ib_read.setBackgroundResource(R.drawable.wood_ivi_button10);
			ib_all.setBackgroundResource(R.drawable.wood_ivi_button12);
			rl_edit_mode.setBackgroundResource(R.drawable.wood_ivi_bar02);
			ib_del_bottom.setBackgroundResource(R.drawable.wood_ivi_button14);
			ib_all_select.setBackgroundResource(R.drawable.wood_ivi_button14);
			ib_all_unselect.setBackgroundResource(R.drawable.wood_ivi_button14);
			//normal
			ib_realbook.setImageResource(R.drawable.ani_button24_btn_enable);
			ib_listbook.setImageResource(R.drawable.ani_button25_btn_disable);
		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[0].equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_main.setBackgroundResource(R.drawable.wood_ivi_bg01);
			rl_top.setBackgroundResource(R.drawable.wood_ivi_bar03);
			ib_buy.setBackgroundResource(R.drawable.wood_ivi_button08);
			// 110504 add for login button
			ib_login.setBackgroundResource(R.drawable.wood_ivi_button09);
			ib_edit.setBackgroundResource(R.drawable.wood_ivi_button09);
			ib_del_top.setBackgroundResource(R.drawable.wood_ivi_button09);
			rl_center.setBackgroundResource(R.drawable.wood_ivi_bar02);
			ib_readed.setBackgroundResource(R.drawable.wood_ivi_button11);
			ib_read.setBackgroundResource(R.drawable.wood_ivi_button10);
			ib_all.setBackgroundResource(R.drawable.wood_ivi_button12);
			rl_edit_mode.setBackgroundResource(R.drawable.wood_ivi_bar02);
			ib_del_bottom.setBackgroundResource(R.drawable.wood_ivi_button14);
			ib_all_select.setBackgroundResource(R.drawable.wood_ivi_button14);
			ib_all_unselect.setBackgroundResource(R.drawable.wood_ivi_button14);
			//normal
			ib_realbook.setImageResource(R.drawable.ani_button24_btn_enable);
			ib_listbook.setImageResource(R.drawable.ani_button25_btn_disable);			
		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[1].equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_main.setBackgroundResource(R.drawable.technology_ivi_bg01);
			rl_top.setBackgroundResource(R.drawable.technology_ivi_bar03);
			ib_buy.setBackgroundResource(R.drawable.technology_ivi_button08);
			// 110504 add for login button
			ib_login.setBackgroundResource(R.drawable.technology_ivi_button09);
			ib_edit.setBackgroundResource(R.drawable.technology_ivi_button09);
			ib_del_top.setBackgroundResource(R.drawable.technology_ivi_button09);
			rl_center.setBackgroundResource(R.drawable.technology_ivi_bar02);
			ib_readed.setBackgroundResource(R.drawable.technology_ivi_button11);
			ib_read.setBackgroundResource(R.drawable.technology_ivi_button10);
			ib_all.setBackgroundResource(R.drawable.technology_ivi_button12);
			rl_edit_mode.setBackgroundResource(R.drawable.technology_ivi_bar02);
			ib_del_bottom.setBackgroundResource(R.drawable.technology_ivi_button14);
			ib_all_select.setBackgroundResource(R.drawable.technology_ivi_button14);
			ib_all_unselect.setBackgroundResource(R.drawable.technology_ivi_button14);
			//black
			ib_realbook.setImageResource(R.drawable.ani_button28_btn_enable);
			ib_listbook.setImageResource(R.drawable.ani_button29_btn_disable);			
		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[2].equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_main.setBackgroundResource(R.drawable.romantic_ivi_bg01);
			rl_top.setBackgroundResource(R.drawable.romantic_ivi_bar03);
			ib_buy.setBackgroundResource(R.drawable.romantic_ivi_button08);
			// 110504 add for login button
			ib_login.setBackgroundResource(R.drawable.romantic_ivi_button09);
			ib_edit.setBackgroundResource(R.drawable.romantic_ivi_button09);
			ib_del_top.setBackgroundResource(R.drawable.romantic_ivi_button09);
			rl_center.setBackgroundResource(R.drawable.romantic_ivi_bar02);
			ib_readed.setBackgroundResource(R.drawable.romantic_ivi_button11);
			ib_read.setBackgroundResource(R.drawable.romantic_ivi_button10);
			ib_all.setBackgroundResource(R.drawable.romantic_ivi_button12);
			rl_edit_mode.setBackgroundResource(R.drawable.romantic_ivi_bar02);
			ib_del_bottom.setBackgroundResource(R.drawable.romantic_ivi_button14);
			ib_all_select.setBackgroundResource(R.drawable.romantic_ivi_button14);
			ib_all_unselect.setBackgroundResource(R.drawable.romantic_ivi_button14);
			//pink
			ib_realbook.setImageResource(R.drawable.ani_button26_btn_enable);
			ib_listbook.setImageResource(R.drawable.ani_button27_btn_disable);			
		}
	}
	/**
	 * 暫停時處理事項 將有下載的書全部暫停
	 */
	public void onPause(){
		super.onPause(); 
		
//		for(int i=0;i<dp.size();i++){
//			dp.get(i).setCancel(true);
//		}
		
		if ( null != dp ){
			for ( DownloadPbar dpbar : dp ){
				dpbar.setCancel(true);
			}
		}
		
//		for ( DownloadPbar dpbar : dp ){
//			try {
//				dpbar.join(100);
//			}catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}		
		
		//nowDownloadNum = 0;
		resetNowDownloadNum();
	    dp = new ArrayList<DownloadPbar>();
	    //dp_num = new ArrayList<Integer>(); 
	}
	/**
	 * 回覆時處理事項 更新列表
	 */
	public void onResume(){
		super.onResume();
//		updateFileIsExists();
		setStyle();
		downloadStatus = true;
		if(ib_del_top.getVisibility()==View.VISIBLE){
			if(nowStyle == 1){//未讀			
				setListOrEdit(1,true);
			}else if(nowStyle == 2){//已讀
				setListOrEdit(2,true);
			}
		}else{
			if(nowStyle == 1){//未讀			
				setListOrEdit(1,false);
			}else if(nowStyle == 2){//已讀
				setListOrEdit(2,false);
			}
		}
		if(nowStyle == 3){//已讀
			cursorDBData = tdb.select();
			initDownload();
			setCheckBok();
			bla = new myAllBookList(this,cursorDBData,tdb);
			lv_main.setAdapter(bla);
			//tdb.close();
//vic			cursorDBData.close();
		}else if(nowStyle == 4){//已讀
			nowStyle  = 3;
			ib_up_page.setVisibility(View.GONE);
			cursorDBData = tdb.select();
			initDownload();
			setCheckBok();
			bla = new myAllBookList(this,cursorDBData,tdb);
			lv_main.setAdapter(bla);
			//tdb.close();
//vic			cursorDBData.close();
		}
		//upImage();
		downloadStatus = false;
		if (DEBUG) Log.e("vic", "onResume ") ;
		//lv_main.invalidateViews();
		calFreeSize();
	}
	/**
	 * 初始化下載變數
	 */
	private void initDownload(){
		if (DEBUG) Log.e("flw", "+initDownload ") ;
		final int count = cursorDBData.getCount();
		ViewHolder [] vhs = new ViewHolder[count];
		//mainRow = new ViewHolder[count];	
		//isDownloadProgressBar = new String[cursorDBData.getCount()];	
		for(int i=0;i < count;i++){
			vhs[i] = null;
			//isDownloadProgressBar[i] = "0";
		}
		mainRow = vhs;
	}
	/**
	 * 設定列表
	 * @param now 模式
	 * @param listOrEdit 是否是編輯模式
	 */
	private void setListOrEdit(int now , boolean listOrEdit){
		if (DEBUG) Log.e("flw", "+setListOrEdit ") ;
		//lv_main.setVisibility(View.VISIBLE);
		if(nowStyle == 1){//未讀			
			cursorDBData = tdb.selectOrderBy("isread = '0'","buytime"+" DESC" );
			//showAlertMessage("共有"+String.valueOf(cursorDBData.getCount())+"未讀過的書");		
		}else if(nowStyle == 2){//已讀
			cursorDBData = tdb.selectOrderBy("isread = '1'","lastreadtime"+" DESC" );
			//showAlertMessage("共有"+String.valueOf(cursorDBData.getCount())+"已讀過的書");	
		}
		else
		{
			if (DEBUG) Log.e("listbook","11 now style=> "+nowStyle+"DB count =>"+cursorDBData.getCount()) ;
		}
		Log.e("345","TWMBook setListOrEdit =>"+cursorDBData.getCount()+" nowStyle =>"+nowStyle);
		initDownload();
		setCheckBok();
		//if(mybl == null)
			mybl = new myBookList(this,cursorDBData,listOrEdit,this.tdb);//true edit false list
	    lv_main.setAdapter(mybl);
	    
	    
	    
	    if (true == listOrEdit) {	// edit mode
			ib_del_top.setEnabled(false);
			ib_del_top.postDelayed(new Runnable() {
				@Override
				public void run() {
					
					if (DEBUG) Log.e("vic", "edit cancel enable");
					ib_del_top.setEnabled(true);
				}
			}, 800);
		} else {
			
			ib_edit.setEnabled(false);
			ib_edit.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (DEBUG) Log.e("vic", "edit enable");
					ib_edit.setEnabled(true);
				}
			}, 800);			
			
		}
	    
	    
//vic	    cursorDBData.close();
	    //tdb.close();			
	}	
	/**
	 * 設定全部列表
	 */
	private void setAllList() {
		if (DEBUG) Log.e("flw", "+setAllList ") ;
		//da = da.createFromPath(saveFilelocation+"listbackground.jpg");
		cursorDBData = tdb.select();
		initDownload();
		setCheckBok();
		//this.showAlertMessage(String.valueOf(cursorDBData.getCount()));
		bla = new myAllBookList(this,cursorDBData,tdb);
		lv_main.setAdapter(bla);
//vic		cursorDBData.close();
/*		lv_main.setVisibility(View.GONE);
		elv_main.setVisibility(View.VISIBLE);
		elv_main.setAdapter(bla);
		elv_main.setBackgroundDrawable(da);*/
	    //tdb.close();
	}
	/**
	 * 設定CheckBok存放值的變數
	 */
	public void setCheckBok(){
		if (DEBUG) Log.e("flw", "+setCheckBok ") ;
		//allCheckBox = new CheckBox[cursorDBData.getCount()];	
		final int count = cursorDBData.getCount();
		allCheckBoxValue = new Boolean[count];
		for(int i=0;i < count;i++){
			//allCheckBox[i] = null;
			allCheckBoxValue[i] = false;
		}
	}
	
	
	/**
	 * 建構進入畫面
	 */
	private void initList() {
		
		cursorDBData = tdb.selectOrderBy("isread = '1'","lastreadtime"+" DESC" );
		if (DEBUG) Log.e("345", "+TWMBook initList "+cursorDBData.getCount()) ;
//		Log.e("cursorDBData", String.valueOf(cursorDBData.getCount()));
		if(cursorDBData.getCount()>0){
			cursorDBData = tdb.selectOrderBy("isread = '1'","lastreadtime"+" DESC" );
			setRead(); 
		}else{
//vic			cursorDBData.close();
			nowStyle = 1;
			cursorDBData = tdb.selectOrderBy("isread = '0'","buytime"+" DESC" );
			setUnRead();
		}
//setUnRead();
		initDownload();
		setCheckBok();		
//		if (DEBUG) Log.e("listbook","22 now style=> "+nowStyle+"DB count =>"+cursorDBData.getCount()) ;
//		mybl = new myBookList(this,cursorDBData,false);
//	    lv_main.setAdapter(mybl);
	    //tdb.close();
	    dp = new ArrayList<DownloadPbar>();
	    //dp_num = new ArrayList<Integer>(); 
	    //tdb.close();
//vic	    cursorDBData.close();	    
	}
	/**
	 * 進入未讀模式
	 */
	private void setUnRead() {
		if (DEBUG) Log.e("flw", "+setUnRead ") ;
		nowStyle = 1;		// 未讀
		ib_read.setImageResource(R.drawable.ivi_button10a);
		ib_readed.setImageResource(R.drawable.ivi_button11b);
		ib_all.setImageResource(R.drawable.ivi_button12b);
	}
	/**
	 * 進入已讀模式
	 */
	private void setRead() {
		if (DEBUG) Log.e("flw", "+setRead ") ;
		nowStyle = 2;		// 已讀
		ib_read.setImageResource(R.drawable.ivi_button10b);
		ib_readed.setImageResource(R.drawable.ivi_button11a);
		ib_all.setImageResource(R.drawable.ivi_button12b);
	}
	/**
	 * 更新圖片 如果圖片不存在或下載失敗 就重新下載
	 */
	private void upImage(){		
		if (DEBUG) Log.e("flw", "+upImage ") ;
		Log.e("", "download img");
		cursorDBDataThread = tdb.select();

		cursorDBDataThread.moveToFirst();
		new Thread(){
			public void run(){
				try{
					for(int i = 0;i < cursorDBDataThread.getCount() ; i++){
						String cover_2 = cursorDBDataThread.getString(4).toString();
						String coverPath_2 = cursorDBDataThread.getString(9).toString();
						File coverFile = new File(saveFilelocation+cover_2);
						
						if( coverPath_2.equals("ivi_nonepict02") || coverPath_2.equals("ivi_nonepict01") ){
							if(!coverFile.exists()||coverFile.length()==0){
								downloadImage(cover_2,cursorDBDataThread.getString(5).toString(),coverPath_2);
								Message m = new Message();
								handlerDownloadImage.sendMessage(m);
							}else{
					   			tdb.updateByDeliveryId(cursorDBDataThread.getString(5).toString() , "coverpath" , saveFilelocation+cover_2);	
					   			mybl.setEbook_cover(cursorDBDataThread.getString(5).toString(), saveFilelocation+cover_2);
							}	
						}
						cursorDBDataThread.moveToNext();
					}
					//tdb.close();
//vic					cursorDBDataThread.close();
				}catch(Exception e){
					//showAlertMessage(e.toString());
				}
			}
		}.start();			   		
	}
	
	private Handler handlerDownloadImage = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Log.e("flw", "+handlerDownloadImage handleMessage") ;
/*			int p = msg.getData().getInt("position");
			int id = msg.getData().getInt("DBrowID");*/
/*			if(nowStyle == 1){//未讀			
				setListOrEdit(1,false);
			}else if(nowStyle == 2){//已讀
				setListOrEdit(2,false);
			}*/
			lv_main.invalidateViews();
		}		
	};	
	/**
	 * 下載書單並解析
	 * @param xml 書單
	 */
    /*private void downloadXML(String xml){
    	if (DEBUG) Log.e("flw", "+downloadXML ") ;
		ebook_deliveryID = new ArrayList<String>();    
		ebook_contentID = new ArrayList<String>();    
		ebook_title = new ArrayList<String>();    
		ebook_publisher = new ArrayList<String>();    
		ebook_authors = new ArrayList<String>();    
		ebook_type = new ArrayList<String>();    
		ebook_category = new ArrayList<String>();    
		ebook_update_date = new ArrayList<String>();    
		ebook_purchased_at = new ArrayList<String>();    
		ebook_trial = new ArrayList<String>();    
		ebook_vertical = new ArrayList<String>();    
		ebook_trial_due_date = new ArrayList<String>();    
		ebook_cover = new ArrayList<String>(); 
		ebook_bodytype_code = new ArrayList<String>(); 
		
    	Document doc ;
   	 	HttpURLConnection conn = null;
   	 	try{
   	 		URL myURL = new URL(xml);
//   	 		conn = (HttpURLConnection)myURL.openConnection();
//   	 		
//   	 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//   	        DocumentBuilder db = dbf.newDocumentBuilder();
//   	 		conn.setConnectTimeout (10000) ;
//   	 		conn.setReadTimeout(10000);
//   	        InputStream is = conn.getInputStream();
   	        //InputStream is = new FileInputStream (saveFilelocation+"addtextXml.xml");
   	 	String urlParameters = "&token="+RealBookcase.getToken(); 
   	 		conn = (HttpURLConnection)myURL.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		conn.setRequestProperty("Content-Length",
				"" + Integer.toString(urlParameters.getBytes().length));
		conn.setRequestProperty("Content-Language", "UTF-8");

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
   	 		
		// Send request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		// Get Response
		int resp = conn.getResponseCode();
		if (resp != HttpURLConnection.HTTP_OK) {
			threadTestttMsg(getResources().getString(
					R.string.iii_NetworkNotConnMessage));
			return;
		}			
   	 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   	        DocumentBuilder db = dbf.newDocumentBuilder();
   	        InputStream is = conn.getInputStream();
   	        
   	        doc = db.parse(is);
   	        
		NodeList nStatus = doc.getElementsByTagName("status");
		String Status = nStatus.item(0).getChildNodes().item(0).getNodeValue().toString();

		NodeList nDesc = doc.getElementsByTagName("description");
		String ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();
		
		NodeList nData = doc.getElementsByTagName("data");
		String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
		InputStream dataStream = covertStringToStream(RevData);
		doc = db.parse(dataStream);
   	        //doc = db.parse(is);
   	       
		
		if(Status.equals("1")){
   	        NodeList nError=doc.getElementsByTagName("error");
   	        ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        
   	        if(ebook_error.equals("0")){
   	   	        NodeList nUpdate_at=doc.getElementsByTagName("update_at");   	
   	        	ebook_update_at = nUpdate_at.item(0).getChildNodes().item(0).getNodeValue().toString();
   	   	        //Log.e("nUpdate_at.getLength()", String.valueOf(nUpdate_at.getLength()));
   	   	        
   	   	        NodeList nDeliveryID=doc.getElementsByTagName("Delivery-ID");
   	   	        NodeList nContent_id=doc.getElementsByTagName("content_id");
   	   	        NodeList nTitle=doc.getElementsByTagName("title");
   	   	        NodeList nPublisher=doc.getElementsByTagName("publisher");
   	   	     	NodeList nAuthors=doc.getElementsByTagName("authors");
   	   	     	NodeList nEbook_type=doc.getElementsByTagName("ebook_type");
   	   	        NodeList nEbook_category=doc.getElementsByTagName("ebook_categories");
   	   	        NodeList nUpdate_date=doc.getElementsByTagName("update_date");
   	   	        NodeList nPurchased_at=doc.getElementsByTagName("purchased_at"); 
   	   	        NodeList nTrial=doc.getElementsByTagName("trial"); 
   	   	        NodeList nVertical=doc.getElementsByTagName("vertical");  
   	   	        NodeList nTrial_due_date=doc.getElementsByTagName("trial_due_date");  
   	   	        NodeList nCover=doc.getElementsByTagName("cover"); 
   	   	        NodeList nBodytypeCode=doc.getElementsByTagName("bodytype_code");
   	   	        
   	   	   		for(int i = 0 ; i < nTitle.getLength() ; i++){
   	   	   			ebook_title.add(nTitle.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			ebook_type.add(nEbook_type.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			ebook_cover.add(nCover.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			ebook_deliveryID.add(nDeliveryID.item(i).getChildNodes().item(0).getNodeValue());   
   	   	   			ebook_purchased_at.add(nPurchased_at.item(i).getChildNodes().item(0).getNodeValue());   
   	   	   			ebook_publisher.add(nPublisher.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			ebook_authors.add(nAuthors.item(i).getChildNodes().item(0).getNodeValue());   
   	   	   			ebook_trial.add(nTrial.item(i).getChildNodes().item(0).getNodeValue()); 	
   	   	   			
   	   	   			ebook_contentID.add(nContent_id.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			
   	   	   			ebook_bodytype_code.add(nBodytypeCode.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			//Log.e("1+++++++++", String.valueOf(nUpdate_date.item(i).getChildNodes().getLength()));
   	   	   			ebook_update_date.add(nUpdate_date.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			
   	   	   			//Log.e("2+++++++++", String.valueOf(nVertical.item(i).getChildNodes().getLength()));
   	   	   			ebook_vertical.add(nVertical.item(i).getChildNodes().item(0).getNodeValue());   
   	   	   			
   	   	   			if(nTrial_due_date.item(i).getChildNodes().getLength()>0){
   	   	   				ebook_trial_due_date.add(nTrial_due_date.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			}else{
   	   	   				ebook_trial_due_date.add("");
   	   	   			}
   	   	   			
   	   	   			//Log.e("3+++++++++", String.valueOf(nVertical.item(i).getChildNodes().getLength()));
   	   	   			int j = 0;
   	   	   			String temp = "";
   	   	   			do{
   	   	   				temp = temp+ nEbook_category.item(i).getChildNodes().item(j).getChildNodes().item(0).getNodeValue()+"|";
   	   	   				j= j + 2;
   	   	   			}while(j < nEbook_category.item(i).getChildNodes().getLength());   	
   	   	   			ebook_category.add(temp);
   	   	   			//Log.e("4+++++++++", String.valueOf(nVertical.item(i).getChildNodes().getLength()));
   	   	   		}
   	   	   		settings.edit().putString("update_at", ebook_update_at).commit();   	   	        
   	        }else{
   	        	unOnline = false;
   	        	threadTestttMsg(ebook_error);   	        	
   	        }
		}else{
			threadHandleMsg(ebook_description);
		}
   	 	}catch(Exception e){
			unOnline = false;
			threadTestttMsg(getResources().getString(R.string.iii_get_book_list_error));
			e.printStackTrace();
   	 	}   	 	
	}	
	*/
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

	/**
	 * 秀出訊息至畫面
	 * @param message 訊息
	 */
	private void showAlertMessage(String message){
		if (DEBUG) Log.e("flw", "+showAlertMessage ") ;
		try{
			new AlertDialog.Builder(TWMBook.this)
	  		.setTitle(R.string.iii_showAM_exception)
	  		.setMessage(message)
	  		.setPositiveButton(R.string.iii_showAM_ok,
	  				new DialogInterface.OnClickListener(){
  						public void onClick(DialogInterface dialoginterface, int i){
      
  						}
        			}
       		).create()
       		.show();  
		}catch(Exception e){
			
		}  	 
	}
	
	/**
	 * 控制畫面元件是否鎖住
	 * @param enabled 是否啟用
	 */
	private void setViewComponentEnabled(boolean enabled){
		if (DEBUG) Log.e("flw", "+setViewComponentEnabled ") ;
		lv_main.setEnabled(enabled);		
		//iv_bookcase.setEnabled(enabled);
		ib_buy.setEnabled(enabled);
		// 110504 add for login button
		ib_login.setEnabled(enabled);
		ib_edit.setEnabled(enabled);
		ib_tools.setEnabled(enabled);
		ib_readed.setEnabled(enabled);
		ib_read.setEnabled(enabled);
		ib_all.setEnabled(enabled);
		ib_up_page.setEnabled(enabled);
		ib_del_top.setEnabled(enabled);
		ib_del_bottom.setEnabled(enabled);
		ib_all_select.setEnabled(enabled);
		ib_all_unselect.setEnabled(enabled);	
		ib_realbook.setEnabled(enabled);
		rl_top.setEnabled(enabled);
		rl_center.setEnabled(enabled);
		rl_edit_mode.setEnabled(enabled);
		rl_main.setEnabled(enabled);
	} 
	/**
	 * 跳出工具表
	 */
	private void toolsAlert(){
		if (DEBUG) Log.e("flw", "+toolsAlert ") ;
		//LayoutInflater factory = LayoutInflater.from(this);
        //final View toolsView = factory.inflate(R.layout.iii_tools_alert_dialog, null);
        
		rl_tools_alert_dialog =(RelativeLayout)findViewById(R.id.rl_tools_alert_dialog);
        
        ImageButton ib_search = (ImageButton)findViewById(R.id.ib_search);
        ImageButton ib_setting = (ImageButton)findViewById(R.id.ib_setting);
        ImageButton ib_onlinebook = (ImageButton)findViewById(R.id.ib_onlinebook);
        setViewComponentEnabled(false);
        rl_tools_alert_dialog.setVisibility(View.VISIBLE);
        ib_search.setBackgroundColor(Color.TRANSPARENT);	
        ib_setting.setBackgroundColor(Color.TRANSPARENT);	
        ib_onlinebook.setBackgroundColor(Color.TRANSPARENT);        
        
        ib_search.bringToFront();
        ib_setting.bringToFront();
        ib_onlinebook.bringToFront();
        
/*        Builder ad = new Builder(TWMBook.this);
        ad.setView(toolsView);
        ad.create();
        final Dialog ad2 = ad.show();*/
        
        ib_search.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		Intent intent = new Intent();
      	  		intent.setClass(TWMBook.this, SearchBook.class);
      	  		startActivity(intent);
      	  		rl_tools_alert_dialog.setVisibility(View.GONE);
      	  		setViewComponentEnabled(true);
      	  	}
        });    
        ib_setting.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		Intent intent = new Intent();
      	  		intent.setClass(TWMBook.this, Setting.class);
      	  		intent.putExtra("saveFilelocation", saveFilelocation);
      	  		startActivity(intent);
      	  		rl_tools_alert_dialog.setVisibility(View.GONE);
      	  		setViewComponentEnabled(true);
      	  	}
        });
        ib_onlinebook.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		Intent intent = new Intent();
      	  		intent.setClass(TWMBook.this, OnlineBook.class);
      	  		intent.putExtra("saveFilelocation", saveFilelocation);
      	  		startActivity(intent);
      	  		rl_tools_alert_dialog.setVisibility(View.GONE);
      	  		setViewComponentEnabled(true);
      	  	}
        });
	}	
	private void threadHandleMsg(String msg) {
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		m.setData(data);
		mHandler.sendMessage(m);
	}
	
	private void threadTestttMsg(String msg) {
		if (DEBUG) Log.e("flw", "+threadTestttMsg ") ;
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		testtt.sendMessage(m);
	}
	
	private void threadTestttMsg2(String msg) {
		if (DEBUG) Log.e("flw", "+threadTestttMsg2 ") ;
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		testtt2.sendMessage(m);
	}
	
	/**
	 * handle login process
	 */
	public static class DownloadInfo{
		int iPos = 0;			//download book position
		String iError = null;	// download error message
		testStartDownloadInfo itestStartDownloadInfo;
		
		DownloadInfo(int aPos,String aErr,testStartDownloadInfo aInfo){
			iPos = aPos;
			iError = aErr;
			itestStartDownloadInfo = aInfo;
		}
	}
	private LoginDialogController iLogin = new LoginDialogController();
	private LoginDialogObserver iLoginObs = new LoginDialogObserver(){
		
		@Override
		public void LoginComplete(LoginDialogController aController,Object aUserData,
				int err) {
			DownloadInfo aInfo = (DownloadInfo)aUserData;
			switch(err){
			case LoginDialogObserver.KErrNone:
				aController.DismissLoginDialog();
				// login success start download again
				testStartDownload(aInfo.itestStartDownloadInfo);
				break;
			case LoginDialogObserver.KErrCancel:
				// cancel to show message
				threadTestttMsg2(aInfo.iError);
				break;
			}
		}
		
	};

	/**
	 * 負責處理同時下載多本書本
	 * @author III
	 * 
	 */
	public class DownloadPbar extends DownloadFile {
		private int arg2 = 0;
		private Boolean pause  = false;
		private int percent = 0;
		private File file = null;
		//private boolean isDel = false;
		private long serverFileSize ;
		//private volatile boolean cancel = true ;
		private volatile int cancel = 99 ;
		private int model;
		private String errorMsg = "";
		private int dpNowStyle;
		private boolean isdownload = true;
		RandomAccessFile oSavedFile = null;
		private long mTimeStamp = 0 ;
				
		public DownloadPbar(String sf,String loc,String tfp,int i,int mod,int nowS){
			super(sf,loc,tfp,i);
			if (DEBUG) Log.e("flw", "+DownloadPbar ") ;
			model = mod;
			dpNowStyle = nowS;
			lv_main.invalidateViews();
		}
		/**
		 * for restart download
		 */
		private testStartDownloadInfo iInfo = null;
		public void setInfo(testStartDownloadInfo aInfo){
			iInfo = aInfo;
		}
		
		/**
		 * 設定是清單的第幾本產品
		 * @param arg 第幾項
		 */
		public void setArg(int arg){
			if (DEBUG) Log.e("flw", "+setArg ") ;
			arg2 = arg;
		}
		/**
		 * 取得本執行緒在清單中的位置
		 * @return 本執行緒在清單中的位置
		 */
		public int getArg(){
			if (DEBUG) Log.e("flw", "+getArg ") ;
			return arg2;
		}
		
		public void setTimeStamp(long timeStamp){
			if (DEBUG) Log.e("flw", "+setTimeStamp ") ;
			mTimeStamp = timeStamp ;
		}
		/**
		 * 取得下載百分比
		 * @return 百分比  
		 */
		public int getPercent(){
			//if (DEBUG) Log.e("flw", "+getPercent ") ;
			//return (int)(new File(location+tempFilePath).length()/serverFileSize * 100);
			//return (int) (mReadBytes*100/serverFileSize);
			
			if ( file != null ){
				if (DEBUG) Log.e("flw", "+getPercent ") ;
				return (int) (file.length() * 100 / serverFileSize);
			}
			return 0 ;
		}
		
		public String gettest(){
			if (DEBUG) Log.e("flw", "+gettest ") ;
			return String.valueOf((file.length()-block) + " - "+getServerFileSize());
		}
		
//		private static final int RETRY_TIMEOUT = 1000 ;
		@SuppressWarnings("finally")
		public synchronized void run(){		
			if (DEBUG) Log.e("flw", "+run ") ;
			int retry = 3;
			URL myURL = null;
			//InputStream conn = null;
			InputStream is = null;
			HttpURLConnection conn = null;
			
			//HttpURLConnection httpConn = null;
			byte[] buff = new byte[64];
			String a = null;
			int currentRead = 0;	

			cancel = 0 ;
			
//			Random ran = new Random(System.currentTimeMillis());
//			int random = ran.nextInt(500)+100;
//			try {
//				Thread.sleep(random);
//			} catch (InterruptedException e1) {
//			}
			
			while ( 0 == cancel && retry > 0) {
				try {
					file = new File(location+tempFilePath);
					
					//serverFile
					oSavedFile = new RandomAccessFile(file,"rw");
					
					if(model==0){  //重新下載
						file.delete();
						oSavedFile = new RandomAccessFile(file,"rw");
						oSavedFile.seek(0);					
						serverFile = serverFile + "0";
						Log.e("model==0","model==0");
					}else{			//續傳
						oSavedFile.seek(file.length()-(int)(file.length()%20480));
						serverFile = serverFile + String.valueOf((int)(file.length()/20480));
						Log.e("model==0","model"+String.valueOf(model));
						
					}
					if (0 != cancel){
						doCancel(0);
						return ;
					}

					boolean isConnected = false ;
					while (false == isConnected && 0 == cancel && retry > 0) {
						
						// connect to server
//						myURL = new URL(serverFile);
//
//						httpConn = (HttpURLConnection)myURL.openConnection();
//						httpConn.setConnectTimeout (CONNECT_TIMEOUT) ;
//						httpConn.setReadTimeout(READ_TIMEOUT);
//						httpConn.setDoInput(true);
//						httpConn.setDoOutput(true);
//						httpConn.setRequestMethod("GET");
//						httpConn.setUseCaches(false);
//						
//						//if (DEBUG) Log.e("vic" , "connect +++");
//						httpConn.connect();
//						//if (DEBUG) Log.e("vic" , "connect ---");
//
//						if (0 != cancel ){
//							continue;
//						}
//						
//						int response = httpConn.getResponseCode();
//						
//						
//						if (response != HttpURLConnection.HTTP_OK) {
//							retry -- ;
//							//wait(RETRY_TIMEOUT);
//							continue ;
//						}
//						
//						if (0 != cancel ){
//							continue;
//						}
//
//						conn = httpConn.getInputStream();
//						currentRead = conn.read(buff);
///*						if ( currentRead != buff.length ){
//							retry -- ;
//							//wait(RETRY_TIMEOUT);
//							continue ;
//						}*/
						myURL = new URL(serverFile);

						//myURL = new URL("http://61.64.54.35/testcode/rf.asp"); 
						//String urlParameters = "&token="+token; 
						conn = (HttpURLConnection) myURL.openConnection();
						conn.setRequestMethod("POST");
						conn.setRequestProperty("Content-Type",
								"application/x-www-form-urlencoded");

//						conn.setRequestProperty("Content-Length",
//								"" + Integer.toString(urlParameters.getBytes().length));
						conn.setRequestProperty("Content-Language", "UTF-8");
						
						conn.setUseCaches(false);
						conn.setDoInput(true);
						conn.setDoOutput(true);
						
						// Send request
						DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
						//wr.writeBytes(urlParameters);
						wr.flush();
						wr.close();
						
						// Get Response
						int resp = conn.getResponseCode();
						if (resp != HttpURLConnection.HTTP_OK) {
							retry -- ;
//							//wait(RETRY_TIMEOUT);
							continue;
						}
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						is = conn.getInputStream();						

						currentRead = is.read(buff);
						a = new String(buff).trim();

						if (true == a.startsWith("00")) {
							isConnected = true ;
							break;
						} else if (true == a.startsWith("01")) {
							getServerErrMsg(a);
								threadTestttMsg2(errorMsg);
							
							if (dpNowStyle == nowStyle) {
								threadErrorDownloadBookMsg(arg2);
							}
							
							if ( 0 == cancel ){
								setNowDownloadNum(false);
							}
							doCancel(1);						
							return;
						}else if (true == a.startsWith("02")){//02 
							getServerErrMsg(a);
							threadHandleMsg(errorMsg);
							setNowDownloadNum(false);
							
							doCancel(1);
							return;
						}else if (true == a.startsWith("03")){//03
							getServerErrMsg(a);
							threadHandleMsg(errorMsg);
							setNowDownloadNum(false);

							doCancel(1);
							return;
						}else if (true == a.startsWith("04")){//04 
							getServerErrMsg(a);
							threadHandleMsg(errorMsg);
							setNowDownloadNum(false);
							
							doCancel(1);
							return;
						} else {
							retry -- ;
							is.close();
							conn.disconnect();
							is = null;
							conn = null;
							myURL = null;
							//wait(RETRY_TIMEOUT);
							continue ;
						}
					}
					
					if (0 != cancel || retry <= 0 || false == isConnected){
						
						if ( 0 == cancel ){
							setNowDownloadNum(false);
							throwErrorMsg(getResources().getString(R.string.iii_server_SocketTimeout));	
						}
						
						doCancel(2);
						return ;
					}

					getServerErrMsg(a);
					serverFileSize = Long.valueOf(a.substring(a.indexOf(",")+1, a.lastIndexOf(",")));	

					final String dID = tempFilePath.substring(0, tempFilePath.indexOf("."));
					boolean isDownloadIDExist = false ; //downloadID.contains(dID);
					
					for ( String dlID : downloadID ){
						if(dlID.equals(dID)){
							isDownloadIDExist = true;
							break;
						}							
					}
					
					if(isDownloadIDExist == false){
						downloadHeapSize = downloadHeapSize + serverFileSize;
						downloadID.add(dID);						
					}
					
					isdownload = true ;
					if(freeSize < downloadHeapSize){
						threadTestttMsg2(getResources().getString(R.string.iii_download_no_space));
						file.delete();
						isdownload = false;
						tdb.update(id , "isdownloadbook" , "0" );
						if( dpNowStyle == nowStyle ){
							mybl.setEebook_isdownloadbook(arg2, "3");
						}	
						downloadHeapSize = downloadHeapSize - serverFileSize;
						calFreeSize();
//						for(int i=0;i<downloadID.size();i++){
//							if(downloadID.get(i).equals(tempFilePath.substring(0, tempFilePath.indexOf(".")))){
//								downloadID.remove(i);
//							}
//						}
						
						//downloadID.remove(dID);

						Message m = new Message();
						reflashView.sendMessage(m);
						setNowDownloadNum(false);
						return ;
					}
					
					if(isdownload && 0 == cancel){
						
						
						tdb.update(id , "bookSize" , String.valueOf(serverFileSize) );
						tdb.update(id , "bookPath" , location+tempFilePath.substring(0, tempFilePath.lastIndexOf(".")) );

						int per = 0;
						buff = new byte[512];
						while ((currentRead = is.read(buff)) > 0 && 0 == cancel) {
								
							oSavedFile.write(buff, 0, currentRead);

							if (0 != cancel) {
								doCancel(3);
								return;
							}
							percent = getPercent();

							if (per < percent && 0 == cancel) {
								per = percent;
								Message m = new Message();
								Bundle data = m.getData();
								tdb.update(id, "downloadPercent", String.valueOf(percent));
								data.putInt("percent", percent);
								data.putInt("Position", arg2);
								data.putInt("nowStyle", dpNowStyle);
								m.setData(data);
								testHandlerDownloadBook.sendMessage(m);
							}
						}
						
						if (0 != cancel ){
							doCancel(4);
							return ;
						}
						
						oSavedFile.close();
						is.close();
						

						if(getPercent()==100){
							tdb.update(id , "isdownloadbook" , "1" );
							if( dpNowStyle == nowStyle ){
								mybl.setEebook_isdownloadbook(arg2, "1");		
							}							
							file.renameTo(new File(location+tempFilePath.substring(0, tempFilePath.lastIndexOf("."))));
							downloadHeapSize = downloadHeapSize - serverFileSize;
							calFreeSize();
							for(int i=0;i<downloadID.size();i++){
								if(downloadID.get(i).equals(dID)){
									downloadID.remove(i);
								}
							}
//							downloadID.remove(dID);
						}else{
							tdb.update(id , "isdownloadbook" , "3" );
							if( dpNowStyle == nowStyle ){
								mybl.setEebook_isdownloadbook(arg2, "3");
							}							
						}
						retry = 0 ;
						setNowDownloadNum(false);
						if(DEBUG) Log.d("vic", "download finish :"+ dID);
						return ;
					}
				} catch (SocketException e){
					e.printStackTrace();
					if (DEBUG) Log.e("vic", "SocketException cancel:"+ cancel);
//					retry -- ;
//					try {
//						wait(RETRY_TIMEOUT);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//						doCancel();
//						return ;
//					}
					
					//if ( 0 == retry){
					
					if ( 0 == cancel){
						throwErrorMsg(getResources().getString(R.string.iii_server_SocketException));
						if( dpNowStyle == nowStyle ){
							threadErrorDownloadBookMsg(arg2);			
						}	
						setNowDownloadNum(false);
					}	
						doCancel(5);
						
						return ;
					//}					
				} catch (SocketTimeoutException e) {
						e.printStackTrace();
						if (DEBUG) Log.e("vic", "SocketTimeout cancel:"+cancel);
						//retry -- ;
						
						if (0 != cancel) {
							doCancel(10);
							return ;
						}
						
						//if (0 == retry) {
							if (0 == cancel) {
								throwErrorMsg(getResources().getString(R.string.iii_server_SocketTimeout));
										
								if (dpNowStyle == nowStyle) {
									threadErrorDownloadBookMsg(arg2);
								}
								setNowDownloadNum(false);
							}
							doCancel(6);
	
							return;
						//}
					
//					try {
//						wait(RETRY_TIMEOUT);
//						if (DEBUG)Log.e("vic", "SocketTimeout wakeup retry:" + retry);					
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//						doCancel(9);
//						if (DEBUG) Log.e("vic", "SocketTimeout interrupt");
//						return ;
//					}
					
					
				} catch (Exception e) {
    
					e.printStackTrace();
					if (DEBUG) Log.e("vic", "other exception cancel:"+cancel);

					if ( 0 == cancel){
						throwErrorMsg(getResources().getString(R.string.iii_server_SocketTimeout));
						setNowDownloadNum(false);
					}
					
					if( dpNowStyle == nowStyle ){
						threadErrorDownloadBookMsg(arg2);			
					}
					doCancel(7);
					
					return ;
				} finally {
					if (DEBUG) Log.d("vic", "enter finally block");
					
					if ( is != null){
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						is = null;
					}
					
					if ( null != conn){
						conn.disconnect();
						conn=null;
					}
					
					if ( myURL != null){
						myURL = null;
					}
					return ;
				}
				
				
			} //end while ( 0 == cancel && retry > 0) {
			
			if ( 0 == cancel ){
				setNowDownloadNum(false);
			}
			
			if ( 0 == retry){
				throwErrorMsg(getResources().getString(R.string.iii_server_SocketTimeout));
			}
			doCancel(8);
		}
		/**
		 * 取得伺服器回傳錯誤訊息
		 * @param a 訊息
		 */
		private void getServerErrMsg(String a) {
			if (DEBUG) Log.e("flw", "+getServerErrMsg ") ;
//			if ( DEBUG ){
//				long time = System.currentTimeMillis();
//				File file = new File("/sdcard/"+time+".log");
//				try {
//					RandomAccessFile log = new RandomAccessFile(file,"rw");
//					log.writeChars(a);
//					log.close();
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
			
			String msg = a.substring(a.lastIndexOf(",") + 1 ).trim() ;
			if ( msg.startsWith("no problem")){
				errorMsg = "no problem";
			}else {
				errorMsg = msg ;
			}
		}
		
		/**
		 * 在螢幕秀出訊息
		 * @param msg 訊息
		 */
		private void throwErrorMsg(String msg ) {
			if (DEBUG) Log.e("flw", "+throwErrorMsg ") ;
			if(errorMsg.equals("")||errorMsg.equals("no problem")){
				threadTestttMsg2(msg);								
			}else{
				threadTestttMsg2(errorMsg);	
			}
		}
		
		private boolean isCanceling = false ;
		/**
		 * 下載中斷
		 * @param no 中斷點
		 */
		private void doCancel(int no) {
			if (DEBUG) Log.e("flw", "+doCancel ") ;
			if (false == isCanceling) {
				isCanceling = true;

				if (DEBUG) Log.d("vic", "doCancel +++" + no +" pos:"+arg2);
					
				if (null != oSavedFile) {
					try {
						oSavedFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					oSavedFile = null;
				}

				file = null;

				tdb.update(id, "isdownloadbook", "3");
				if (dpNowStyle == nowStyle) {
					
					long stamp = getTimeStamp(arg2);
					if ( mTimeStamp == stamp) {
						mybl.setEebook_isdownloadbook(arg2, "3");
					}else {
						if (DEBUG) Log.e("vic", "mTimeStamp != stamp, mTimeStamp:" +mTimeStamp + " stamp:"+ stamp);
						isCanceling = false ;
						return ;
					}
				}
				Message m = new Message();
				reflashView.sendMessage(m);

				// setNowDownloadNum(false);

				if (DEBUG) Log.d("vic", "doCancel ---" + no);
					
				
				isCanceling = false ;
			}
		}
		
		private Handler errorDownloadBook = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if (DEBUG) Log.e("flw", "+errorDownloadBook handleMessage") ;
				try {
					int arg2 = msg.getData().getInt("Position");
					ViewHolder vh = mainRow[arg2];
					vh.pbar.setVisibility(View.GONE);
					vh.cancel.setVisibility(View.GONE);
					vh.icon.setAlpha(100);		
					vh.text.setTextColor(Color.GRAY);
					vh.text.setBackgroundColor(Color.alpha(100));		
					pbarNowStatusList.set(arg2, "3");
					//vh.nowStatus = "0";			
					mybl.setEebook_isdownloadbook(arg2,"3");
					//lv_main.invalidateViews();
					vh.pbar.invalidate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		private Handler reflashView = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if (DEBUG) Log.e("flw", "+reflashView handleMessage") ;
				lv_main.invalidateViews();
			}
		};
		
		private void threadErrorDownloadBookMsg(int position) {
			if (DEBUG) Log.e("flw", "+threadErrorDownloadBookMsg ") ;
			Message m = new Message();
			Bundle data = m.getData();
			data.putInt("Position", position);
			errorDownloadBook.sendMessage(m);
		}
		/**
		 * 中斷
		 * @param cel 中斷
		 */
		public void setCancel(boolean cel){
			if (DEBUG) Log.e("flw", "+setCancel ") ;
			//if ( DEBUG ) Log.d("vic" , "setCancel:" + cel);
			//cancel = cel;
			
			if ( true == cel ){
				cancel ++ ;
				setNowDownloadNum(false);
				interrupt();
			}else {
				cancel = 0 ;
			}
			
			if ( DEBUG ) Log.d("vic" , "setCancel:"+cancel);
		}
		/**
		 * 回傳取消值
		 * @return 取消 
		 */
		public int cancel(){
			if (DEBUG) Log.e("flw", "+cancel ") ;
			return cancel;
		}
		
		public synchronized void del() {
			if (DEBUG) Log.e("flw", "+del ") ;
			//isDel = true;
		}
		/**
		 * 暫停
		 */
		public synchronized void pause() {
			if (DEBUG) Log.e("flw", "+pause ") ;
			pause = true;
		}
		/**
		 * 重新開始
		 * @param percent 百分比
		 */
		public synchronized void restart(int percent) {
			if (DEBUG) Log.e("flw", "+restart 1") ;
			restart(false,percent);
		}
		/**
		 * 重新開始
		 * @param fromBegin 是否重新
		 * @param per 百分比
		 */
		public synchronized void restart(boolean fromBegin,int per) {
			if (DEBUG) Log.e("flw", "+restart 2") ;
			if (pause) {
				if (fromBegin) {
					percent = per;
				}
				pause = false;
				this.notify();
			}
		}
		/**
		 * 暫停
		 */
		protected void pauseJob() {
			if (DEBUG) Log.e("flw", "+pauseJob ") ;
			if (pause) {
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
					
					}
				}
			}
		}
	}		
	/**
	 * 設定下載數量增加或減少
	 * @param fag 增加或減少
	 */
	public synchronized void setNowDownloadNum(boolean fag) {
		if (DEBUG) Log.e("flw", "+setNowDownloadNum ") ;
		if (fag==true){
			nowDownloadNum ++;
		}else{
			nowDownloadNum --;
		}
		
		if ( DEBUG ) Log.d("vic", "setNowDownloadNum: "+ fag + " :"+ nowDownloadNum); 
		
		if ( nowDownloadNum < 0 || nowDownloadNum > 5 ){
			if ( DEBUG ) Log.e("vic", "oops setNowDownloadNum: "+ fag + " :"+ nowDownloadNum); 
		}
		
		if ( nowDownloadNum < 0 ){
			nowDownloadNum = 0 ;
		}
		
	}
	/**
	 * 取得目前下載數量
	 * @return 下載數量  
	 */
	public synchronized int getNowDownloadNum() {
		if (DEBUG) Log.e("flw", "+getNowDownloadNum ") ;
		return nowDownloadNum;
	}
	/**
	 * 將下載數量設定0
	 */
	public synchronized void resetNowDownloadNum(){
		if (DEBUG) Log.e("flw", "+resetNowDownloadNum ") ;
		nowDownloadNum = 0 ;
	}
	/**
	 * 離開
	 */
	private void exit() {
		if (DEBUG) Log.e("flw", "+exit ") ;
//		for ( DownloadPbar dpbar : dp ){
//			dpbar.setCancel(true);
//		}
		if ( false == isDataReady() ){
			return ;
		}

		//tdb.close();
		finish();
	}
	
	
	private static final String BRAND_XML = "/brand.xml";
	
    private final String TAG_AP_NAME = "AP_NAME";
    private final String TAG_AP_PIC1 = "AP_PIC1";
    private final String TAG_AP_URL = "AP_URL";
    private final String TAG_AP_FAIL_URL = "AP_FAIL_URL";	
	
    private List<String> mBrandNames = null;
    private List<String> mBrandPIC1s = null;
    private List<String> mBrandURLs = null;
    private List<String> mBrandFailURLs = null;
    
    private void loadBradData(){
    	if (DEBUG) Log.e("flw", "+loadBradData ") ;
    	mBrandNames = new ArrayList<String>();
    	mBrandPIC1s = new ArrayList<String>();
    	mBrandURLs = new ArrayList<String>();
    	mBrandFailURLs = new ArrayList<String>();
    	
		String fileDir = this.getFilesDir().toString()+BRAND_XML;	

		try {
			InputStream is = new FileInputStream(fileDir);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(is);
			
			NodeList apNames = doc.getElementsByTagName(TAG_AP_NAME);
			NodeList apPic1s = doc.getElementsByTagName(TAG_AP_PIC1);
			NodeList apUrls = doc.getElementsByTagName(TAG_AP_URL);
			NodeList apFailUrls = doc.getElementsByTagName(TAG_AP_FAIL_URL);
			
			final int count = apNames.getLength();
			for ( int i =0 ; i < count ; i++){
				mBrandNames.add(apNames.item(i).getChildNodes().item(0).getNodeValue().toString());
				mBrandPIC1s.add(apPic1s.item(i).getChildNodes().item(0).getNodeValue().toString());
				mBrandURLs.add(apUrls.item(i).getChildNodes().item(0).getNodeValue().toString());
				mBrandFailURLs.add(apFailUrls.item(i).getChildNodes().item(0).getNodeValue().toString());
			}
			
			apNames = null;
			apPic1s = null;
			apUrls = null;
			apFailUrls = null;
			
			
//			NodeList apdes = doc.getElementsByTagName("DESCRIPTION");
//			len = apdes.getLength();
//			String des;
//			for ( int i = 0 ; i < len; i++){
//				des =  apdes.item(i).getChildNodes().item(0).getNodeValue().toString();
//				Log.e(TAG , apdes.item(i).getChildNodes().item(0).getNodeValue().toString());
//			}					
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }		


	/**
	 * 建構本地書櫃 全部列表清單
	 * @author III
	 * 
	 */
	public class myAllBookList extends BookListAllDetail{
		/**
		 * 取得列表中的某一欄
		 * @param position 位置
		 * @param convertView 當前的view
		 * @param parent ViewGroup
		 * @return 列表中的某一欄
		 */
		@Override
		public synchronized View getView(int position, View convertView, ViewGroup parent) {
			if (DEBUG) Log.e("flw", "+getView 1") ;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.iii_all_type_row, null);
				mainRow[position] = new ViewHolder();
				mainRow[position].text = (TextView) convertView.findViewById(R.id.all_text);
				mainRow[position].rl = (RelativeLayout) convertView.findViewById(R.id.rl);
				mainRow[position].icon = (ImageView) convertView.findViewById(R.id.icon);
				mainRow[position].iconTag = (ImageView) convertView.findViewById(R.id.iconTag);
				convertView.setTag(mainRow[position]);		
			}else{
				mainRow[position] = (ViewHolder) convertView.getTag();
			}
			SharedPreferences settings = mInflater.getContext().getSharedPreferences("setting_Preference", 0);
			
			int brandCount = mBrandNames.size();
//			if ( position < brandCount ){
//				mainRow[position].text.setText(mBrandNames.get(position).toString());
//				mainRow[position].text.setSingleLine(true);
//				mainRow[position].text.setWidth(width);
//				mainRow[position].text.setHeight(44);
//				mainRow[position].text.setBackgroundColor(Color.alpha(150));
//				mainRow[position].text.setTextColor(Color.rgb(50, 50, 50));
//				mainRow[position].text.setMarqueeRepeatLimit(6);	
//				mainRow[position].text.setTextSize(20);	
//				mainRow[position].text.setEnabled(true);
//
//				
//			}else {
//					position -= brandCount;
				if(tempAllTag.get(position)==0){
					mainRow[position].text.setText(tempAllCount.get(position).toString());
					mainRow[position].text.setSingleLine(true);
					mainRow[position].text.setWidth(width);
					mainRow[position].text.setMarqueeRepeatLimit(6);	
					mainRow[position].text.setHeight(30);
					if("".equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
					}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[0].equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
					}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[1].equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.technology_ivi_pict01);	
					}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[2].equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.romantic_ivi_pict01);	
					}			
					mainRow[position].text.setTextColor(Color.WHITE);
					mainRow[position].text.setTextSize(20);	
					mainRow[position].text.setEnabled(false);
				}else if(tempAllTag.get(position)==1){
					mainRow[position].text.setText(tempAllCount.get(position).toString());
					mainRow[position].text.setSingleLine(true);
					mainRow[position].text.setWidth(width);
					mainRow[position].text.setHeight(44);
					mainRow[position].text.setBackgroundColor(Color.alpha(150));
					mainRow[position].text.setTextColor(Color.rgb(50, 50, 50));
					mainRow[position].text.setMarqueeRepeatLimit(6);	
					mainRow[position].text.setTextSize(20);	
					mainRow[position].text.setEnabled(true);
				}else if(tempAllTag.get(position)==2){
					mainRow[position].text.setText(tempAllCount.get(position).toString());
					mainRow[position].text.setSingleLine(true);
					mainRow[position].text.setWidth(width);
					mainRow[position].text.setMarqueeRepeatLimit(6);	
					mainRow[position].text.setHeight(30);
					if("".equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
					}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[0].equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
					}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[1].equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.technology_ivi_pict01);	
					}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[2].equals(settings.getString("setting_bookcase_background_style_value", ""))){
						mainRow[position].text.setBackgroundResource(R.drawable.romantic_ivi_pict01);	
					}			
					mainRow[position].text.setTextColor(Color.WHITE);
					mainRow[position].text.setTextSize(20);	
					mainRow[position].text.setEnabled(false);
				}else if(tempAllTag.get(position)==3){//實
					mainRow[position].rl.setVisibility(View.VISIBLE);
					mainRow[position].icon.setVisibility(View.VISIBLE);
					mainRow[position].iconTag.setVisibility(View.VISIBLE);
					if(tempTrialTag.get(position).equals("1")){
						mainRow[position].iconTag.setImageResource(R.drawable.ivi_icon01);
					}else if(tempTrialTag.get(position).equals("3")){
						mainRow[position].iconTag.setImageResource(R.drawable.ivi_icon03);
					}else if(tempTrialTag.get(position).equals("4")){
						mainRow[position].iconTag.setImageResource(R.drawable.ivi_icon03);
					}else{
						mainRow[position].iconTag.setImageDrawable(null);
					}
					
					if(tempTypeTag.get(position).equals(getResources().getString(R.string.iii_mebook))){
						mainRow[position].text.setText(tempAllCount.get(position).toString()+getResources().getString(R.string.iii_mebook_title));
					}else{
						mainRow[position].text.setText(tempAllCount.get(position).toString());
					}
					mainRow[position].text.setSingleLine(true);
					mainRow[position].text.setWidth(width);
					mainRow[position].text.setHeight(44);
					mainRow[position].text.setBackgroundColor(Color.alpha(150));
					mainRow[position].text.setTextColor(Color.rgb(50, 50, 50));
					mainRow[position].text.setMarqueeRepeatLimit(6);	
					mainRow[position].text.setTextSize(20);	
					mainRow[position].text.setEnabled(true);
					if(tempCoverTag.get(position).toString().indexOf(".")>0){
						//Log.e("setImageBitmap", "setImageBitmap");
						
						try {
							mainRow[position].icon.setImageBitmap(BitmapFactory.decodeFile(tempCoverTag.get(position).toString()));
						}
						catch (OutOfMemoryError e){
							e.printStackTrace();
							
							System.gc();
							System.gc();
						}
					}else{
						//Log.e("setImageResource", "setImageResource");
						/**
						 * 20110421 add to download cover when not exist
						 */
						Bitmap defBmp = null;
						if(tempCoverTag.get(position).equals("ivi_nonepict02")){
							defBmp = BitmapFactory.decodeResource(getResources(),R.drawable.ivi_nonepict02);
						}else{
							defBmp = BitmapFactory.decodeResource(getResources(),R.drawable.ivi_nonepict01);
						}
						String aCoverUrl = getResources().getString(R.string.iii_cms_cover)+this.ebook_cover_url.get(position);
						this.iDownloader.download(aCoverUrl, mainRow[position].icon, defBmp);
					}
				}else if(tempAllTag.get(position)==4){//需
					mainRow[position].text.setText(tempAllCount.get(position).toString());
					mainRow[position].text.setSingleLine(true);
					mainRow[position].text.setWidth(width);
					mainRow[position].text.setHeight(44);				
					mainRow[position].text.setBackgroundColor(Color.alpha(150));
					mainRow[position].text.setTextColor(Color.GRAY);
					mainRow[position].text.setMarqueeRepeatLimit(6);	
					mainRow[position].text.setTextSize(20);	
					mainRow[position].text.setEnabled(false);
				}else if (tempAllTag.get(position)==5){
					mainRow[position].text.setText(tempAllCount.get(position).toString());
					mainRow[position].text.setSingleLine(true);
					mainRow[position].text.setWidth(width);
					mainRow[position].text.setHeight(44);
					mainRow[position].text.setBackgroundColor(Color.alpha(150));
					mainRow[position].text.setTextColor(Color.rgb(50, 50, 50));
					mainRow[position].text.setMarqueeRepeatLimit(6);	
					mainRow[position].text.setTextSize(20);	
					mainRow[position].text.setEnabled(true);
				}
//			}
			return convertView;
		}

/*		public myAllBookList(Context context, Cursor c, String detailTitle,
				List<String> book) {
			super(context, c, detailTitle, book);
			// TODO Auto-generated constructor stub
		}*/

		public myAllBookList(TWMBook twmBook, Cursor cursorDBData, TWMDB tdb,
				List<String> tempAllCount, List<Integer> tempAllTag, int arg2) {
			super(twmBook,cursorDBData, tdb,tempAllCount,tempAllTag,arg2);
			// TODO Auto-generated constructor stub
			if (DEBUG) Log.e("flw", "+myAllBookList 1 ") ;
		}

		public myAllBookList(TWMBook twmBook, Cursor cursorDBData, TWMDB tdb) {
			super(twmBook,cursorDBData, tdb);
			// TODO Auto-generated constructor stub
			if (DEBUG) Log.e("flw", "+myAllBookList 2 ") ;
		}
		
	}
	/**
	 * 建構本地書櫃 書本清單
	 * @author III
	 * 
	 */
	public class myBookList extends BookList{
		//private List<String> nowStatus;
		protected ImageDownloader iDownloader = null; 
		
		public myBookList(Context context,Cursor c,Boolean ie,TWMDB db) {
			super(context);
			if (DEBUG) Log.e("listbook", "+myBookList ") ;
			
			iDownloader = new ImageDownloader();
			iDownloader.setMode(ImageDownloader.Mode.CORRECT);
			iDownloader.setDb(db);
			iDownloader.setDlPath(Util.getStorePath(context));
			
			mInflater = LayoutInflater.from(context);
			isEdit = ie;
			myCursor = c;
			//isDownload = id;
			ebook_title = new ArrayList<String>();   
			ebook_title.clear();
			ebook_cover = new ArrayList<String>();   
			ebook_cover_url = new ArrayList<String>();   
			ebook_isdownloadbook = new ArrayList<String>();  
			ebook_deliveryid = new ArrayList<String>();
			ebook_trial = new ArrayList<String>();
			ebook_id = new ArrayList<String>(); 	
			ebook_type = new ArrayList<String>();
			myCursor.moveToFirst();
			
			
			//pbarIndexList.clear();
			pbarValueList.clear();
			pbarNowStatusList.clear();
			
			for(int i=0;i<myCursor.getCount();i++){
				ebook_title.add(myCursor.getString(1));
				ebook_cover.add(myCursor.getString(9));
				ebook_cover_url.add(c.getString(4));
				ebook_isdownloadbook.add(myCursor.getString(8));	
				ebook_deliveryid.add(c.getString(5));
				ebook_trial.add(c.getString(13));
				ebook_id.add(c.getString(0));		
				ebook_type.add(c.getString(2));	
				//pbarIndexList.add(i);
				pbarValueList.add(0);
				pbarNowStatusList.add(myCursor.getString(8));	
				myCursor.moveToNext(); 
			}
		}
		/**
		 * 設定書本封面
		 * @param deliveryid delivery id
		 * @param value 圖片位置
		 */
		public void setEbook_cover(String deliveryid,String value){
			if (DEBUG) Log.e("flw", "+setEbook_cover ") ;
			int position=0;
			for(int i=0;i<ebook_deliveryid.size();i++){
				if(ebook_deliveryid.get(i).equals(deliveryid)){
					position = i;
					break;
				}
			}
			if(ebook_deliveryid.get(position).equals(deliveryid)){
				ebook_cover.set(position, value);
			}			
		}
		/**
		 * 設定書是否被下載過
		 * @param position 位置
		 * @param value value
		 */
		public void setEebook_isdownloadbook(int position,String value){
			if (DEBUG) Log.e("flw", "+setEebook_isdownloadbook ") ;
			ebook_isdownloadbook.set(position, value);
			pbarNowStatusList.set(position, value);
			//mainRow[position].nowStatus = value;
		}
		@Override
		public int getCount(){
			if (DEBUG) Log.e("345","BookList getCount => "+cursorDBData.getCount() ) ;
			if(cursorDBData.getCount() == 2){
				if (DEBUG) Log.e("345","aaaa getCount => "+cursorDBData.getCount() ) ;
			}
			return cursorDBData.getCount();
		}
		/**
		 * 取得列表中的某一欄
		 * @param position 位置
		 * @param convertView 當前的view
		 * @param parent ViewGroup
		 * @return 列表中的某一欄
		 */
		@Override
		public synchronized View getView(final int position, View convertView, ViewGroup parent) {


			try{
				
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.iii_file_row, null);
					mainRow[position] = new ViewHolder();
					mainRow[position].cb = (CheckBox) convertView.findViewById(R.id.cb);
					mainRow[position].icon = (ImageView) convertView.findViewById(R.id.icon);
					mainRow[position].iconTag = (ImageView) convertView.findViewById(R.id.iconTag);
					mainRow[position].text = (TextView) convertView.findViewById(R.id.text);				
					mainRow[position].pbar = (ProgressBar)convertView.findViewById(R.id.progress);
					mainRow[position].cancel = (ImageButton) convertView.findViewById(R.id.cancel);		
					//mainRow[position].nowStatus = ebook_isdownloadbook.get(position);
					//Log.v("convertView == null","-------------------");
					convertView.setTag(mainRow[position]);
				}else{
					mainRow[position] = (ViewHolder) convertView.getTag();
					//mainRow[position].nowStatus = ebook_isdownloadbook.get(position);
				}
				
				mainRow[position].cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
						if(isChecked){
							allCheckBoxValue[position] = true;
							ib_del_bottom.setEnabled(true);
						}else{
							allCheckBoxValue[position] = false;
							ib_del_bottom.setEnabled(false);
							for(int i=0;i<allCheckBoxValue.length;i++){
								if(allCheckBoxValue[i] == true){
									ib_del_bottom.setEnabled(true);
									break;
								}
							}
						}												
					}
				});
				
//				mainRow[position].text.setOnTouchListener(new TextView.OnTouchListener() {
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						// TODO Auto-generated method stub
//						Log.e("pbar OnTouch", "pbar OnTouch");
//						return false;
//					}
//				});

				mainRow[position].cancel.setOnClickListener(new ImageButton.OnClickListener(){
		      	  	public void onClick(final View v){
		      	  		
		      	  		if (DEBUG) Log.e("vic", "cancel :"+position );
		      	  		
		      	  		v.setEnabled(false);
		      	  		v.postDelayed(new Runnable(){ 
							@Override 
							public void run() { 
								v.setEnabled(true);
							} 
						},400);		      	  		
		      	  		
		      	  		//int num = dp_num.indexOf(new Integer(position)) ;
		      	  		int num = -1;
		      	  		
		      	  		for(int i=0;i<dp.size();i++){
		      	  			if (position == dp.get(i).getArg()){
		      	  				num = i;
		      	  			}
		      	  		}

		      	  		if ( num < 0 ){
		      	  			if (DEBUG) Log.e("vic", "dp_num index:-1 pos:"+position );
		      	  			return ;
		      	  		}
		      	  		
		      	  		// cancel thread
						dp.get(num).setCancel(true);
	      	  		
		      	  		//nowDownloadNum  = nowDownloadNum - 1;
		      	  		//new File(saveFilelocation + ebook_deliveryid.get(position) +fileType).delete();
		      	  		tdb.update(Integer.valueOf(ebook_id.get(position)) , "isdownloadbook" , "0" );
		      	  		
		      	  		System.out.println("cancel  position "+position);
						mainRow[position].pbar.setVisibility(View.GONE);
						mainRow[position].cancel.setVisibility(View.GONE);
						//Log.v("1","1");
						mainRow[position].icon.setAlpha(100);
						mainRow[position].text.setTextColor(Color.RED);		
						mainRow[position].text.setBackgroundColor(Color.alpha(100));
						mainRow[position].pbar.invalidate();
						
						pbarNowStatusList.set(position, "0");
						//mainRow[position].nowStatus = "0";					
						ebook_isdownloadbook.set(position, "0");
//						lv_main.invalidateViews(); 	
//						dp.get(num).setCancel(true);
//						try {
//							dp.get(num).join(100);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
						
						//vic setNowDownloadNum(false);
					}
				});
				//mainRow[position].pbar.setMax(100);
				mainRow[position].cancel.setBackgroundColor(Color.TRANSPARENT);
				//mainRow[position].pbar.bringToFront();
				//mainRow[position].iconTag.bringToFront();
				mainRow[position].iconTag.setBackgroundColor(Color.TRANSPARENT);
				//試閱
				//mainRow[position].icon.setBackgroundResource(R.drawable.iii_trial);
				if(ebook_trial.get(position).equals("1")){
					mainRow[position].iconTag.setImageResource(R.drawable.ivi_icon01);
				}else if(ebook_trial.get(position).equals("3")){
					mainRow[position].iconTag.setImageResource(R.drawable.ivi_icon03);
				}else if(ebook_trial.get(position).equals("4")){
					mainRow[position].iconTag.setImageResource(R.drawable.ivi_icon03);
				}else{
					mainRow[position].iconTag.setImageDrawable(null);
				}
				
				if(ebook_type.get(position).equals(getResources().getString(R.string.iii_mebook))){
					mainRow[position].text.setText(ebook_title.get(position).toString()+getResources().getString(R.string.iii_mebook_title));
				}else{
					mainRow[position].text.setText(ebook_title.get(position).toString());
				}
				
				
				if(allIsEdit == 1){
					mainRow[position].cb.setChecked(true);
				}else if (allIsEdit == 2){
					mainRow[position].cb.setSelected(false);
				}			
					
				//Log.v("-------------------","-------------------");
/*				if(pbarNowStatusList.get(position).equals("1")){
					mainRow[position].cancel.setVisibility(View.GONE);
					mainRow[position].pbar.setVisibility(View.GONE);
					//Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____1");
				}else if(pbarNowStatusList.get(position).equals("0")){
					mainRow[position].cancel.setVisibility(View.GONE);
					mainRow[position].pbar.setVisibility(View.GONE);
					//Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____0");
				}else if(pbarNowStatusList.get(position).equals("2")){
					mainRow[position].cancel.setVisibility(View.VISIBLE);
					mainRow[position].pbar.setVisibility(View.VISIBLE);
					
					//Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____2");
				}else if(pbarNowStatusList.get(position).equals("3")){
					mainRow[position].cancel.setVisibility(View.GONE);
					mainRow[position].pbar.setVisibility(View.GONE);
					//Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____3");
				}*/
				//Log.v("-------------------","-------------------");
				
				if(allCheckBoxValue[position]==false){
					mainRow[position].cb.setChecked(false);
				}else{
					mainRow[position].cb.setChecked(true);
				}
				//Log.e("ebook_cover.get(position).toString()", String.valueOf(ebook_cover.get(position).toString().indexOf(".")));
				if(ebook_cover.get(position).toString().indexOf(".")>0){
					//Log.e("setImageBitmap", "setImageBitmap");
					
					try {
						if(BitmapFactory.decodeFile(ebook_cover.get(position).toString())==null){
							if(ebook_type.get(position).equals(getResources().getString(R.string.iii_mebook))){
								mainRow[position].icon.setImageResource(R.drawable.ivi_nonepict02);
								tdb.update(Integer.valueOf(ebook_id.get(position)) , "coverPath" , "ivi_nonepict02" );
							}else{
								mainRow[position].icon.setImageResource(R.drawable.ivi_nonepict01);
								tdb.update(Integer.valueOf(ebook_id.get(position)) , "coverPath" , "ivi_nonepict01" );
							}
						}else{
							mainRow[position].icon.setImageBitmap(BitmapFactory.decodeFile(ebook_cover.get(position).toString()));
						}	
					}catch (OutOfMemoryError e){
						e.printStackTrace();
						
						System.gc();
						System.gc();
					}
				}else{
					//Log.e("setImageResource", "setImageResource");
					
					/**
					 * 20110421 add to download cover when not exist
					 */
					Bitmap defBmp = null;
					if(ebook_cover.get(position).equals("ivi_nonepict02")){
						defBmp = BitmapFactory.decodeResource(getResources(),R.drawable.ivi_nonepict02);
					}else{
						defBmp = BitmapFactory.decodeResource(getResources(),R.drawable.ivi_nonepict01);
					}
					String aCoverUrl = getResources().getString(R.string.iii_cms_cover)+this.ebook_cover_url.get(position);
					iDownloader.download(aCoverUrl, mainRow[position].icon, defBmp);
				}
/*				for(int i=0;i<pbarIndexList.size();i++){
					if(position==pbarIndexList.get(i)){
						mainRow[position].pbar.setProgress(pbarValueList.get(position));
					}
				}*/
				/*if(mainRow[position].pbar.getVisibility()==View.VISIBLE){
					mainRow[position].pbar.setProgress(mainRow[position].pbarValue);
				}*/				
				//mainRow[position].icon.setScaleType(ScaleType.FIT_XY);
				mainRow[position].text.setSingleLine(true);
				mainRow[position].text.setWidth(width);
				mainRow[position].text.setMarqueeRepeatLimit(6);
				mainRow[position].cancel.setImageResource(R.drawable.ivi_button18);
			//	mainRow[position].text.setTextSize(TypedValue.COMPLEX_UNIT_PT,10);
				if (ebook_isdownloadbook.get(position).toString().equals("0")){
					mainRow[position].icon.setAlpha(100);		
					//mainRow[position].text.setBackgroundColor(Color.alpha(100));
					mainRow[position].text.setTextColor(Color.GRAY);
					mainRow[position].pbar.setVisibility(View.GONE);
					mainRow[position].cancel.setVisibility(View.GONE);
				}else if(ebook_isdownloadbook.get(position).toString().equals("1")){
					
/*
					Cursor c = tdb.select2(ebook_deliveryid.get(position));
					c.moveToFirst();
					File f = new File(c.getString(19));
					if ( false == f.exists() ){
						mainRow[position].icon.setAlpha(100);		
						//mainRow[position].text.setBackgroundColor(Color.alpha(100));
						mainRow[position].text.setTextColor(Color.GRAY);
						mainRow[position].pbar.setVisibility(View.GONE);
						mainRow[position].cancel.setVisibility(View.GONE);
					}else {
*/
						mainRow[position].icon.setAlpha(255);		
						mainRow[position].text.setTextColor(Color.BLACK);
						//mainRow[position].text.setBackgroundColor(Color.alpha(255));
						mainRow[position].pbar.setVisibility(View.GONE);
						mainRow[position].cancel.setVisibility(View.GONE);
//					}
				}else if(ebook_isdownloadbook.get(position).toString().equals("2")){
					mainRow[position].icon.setAlpha(100);		
					mainRow[position].text.setTextColor(Color.GRAY);
					//mainRow[position].text.setBackgroundColor(Color.argb(20, 50, 50, 0));
					mainRow[position].cancel.setVisibility(View.VISIBLE);
					mainRow[position].pbar.setVisibility(View.VISIBLE);
					mainRow[position].pbar.setProgress(pbarValueList.get(position));
				}else {
					//Log.v("-----    3     ------","-------    3    -----");
					mainRow[position].icon.setAlpha(100);	
					mainRow[position].text.setTextColor(Color.GRAY);
					//mainRow[position].text.setBackgroundColor(Color.argb(20, 50, 50, 0));
					mainRow[position].cancel.setVisibility(View.GONE);
					mainRow[position].pbar.setVisibility(View.GONE);
				}
				
				if(isEdit){
					mainRow[position].cancel.setVisibility(View.GONE);
					mainRow[position].pbar.setVisibility(View.GONE);
					mainRow[position].cb.setVisibility(View.VISIBLE);
				}else{
					mainRow[position].cb.setVisibility(View.GONE);
				}
				
				if(downloadStatus){
					mainRow[position].cancel.setVisibility(View.GONE);
					mainRow[position].pbar.setVisibility(View.GONE);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			//return super.getView(position, convertView, parent);
			return convertView;
		}		

	}
	@Override
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		if (DEBUG) Log.e("flw", "+onRetainNonConfigurationInstance ") ;
		return super.onRetainNonConfigurationInstance();
		

		
	}	
		//110504 add for login button
	private static final String LogoutAPI = "http://124.29.140.83/DeliverWeb/LogOut?deviceId=";
	private static final int LOGOUT_SUCCESS = 0;
	private static final int LOGOUT_FAILED= 1;
	private class LogoutTask extends AsyncTask<String, Integer, Integer> {
		private WeakReference<Context> iContextRef = null;
		@Override
		protected void onPostExecute(Integer result) {
//			LoginDialogController aController = new LoginDialogController();
//			aController.ShowLoginDialog(iContextRef.get(),iLoginObs,MODERELOGIN);
			
			super.onPostExecute(result);
			if (result.intValue() == LOGOUT_SUCCESS){
				Toast.makeText(getApplicationContext(),R.string.iii_setting_logout_success , Toast.LENGTH_SHORT).show();
				
				if ( null != tdb ){
					tdb.close();
					tdb = null;
				}
				Intent i = getBaseContext().getPackageManager()
	             .getLaunchIntentForPackage( "com.taiwanmobile.myBook_PAD" );
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}else/*failed*/ {
				Toast.makeText(getApplicationContext(),R.string.iii_setting_logout_failed, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Integer doInBackground(String... arg0) {
			iContextRef = new WeakReference<Context>(TWMBook.this);
			String deviceID = arg0[0];
	    	
	    	Integer ret = new Integer(LOGOUT_FAILED);
	    	
	    	HttpParams httpParameters = new BasicHttpParams();
	    	int timeoutConnection = 10000;
	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	    	int timeoutSocket = 10000;
	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);  
	    	
	    	DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
	    	String apiUrl = LogoutAPI;
	    	try {
		    	HttpGet httGet = new HttpGet(apiUrl+deviceID);
	    	
	    		httpclient.execute(httGet);

	    		ret = new Integer(LOGOUT_SUCCESS);
	    	}catch(Throwable e){
	    		e.printStackTrace();
	    		Log.e("TWM","logout exception!");
	    	}
			return ret;
		}
	}
}