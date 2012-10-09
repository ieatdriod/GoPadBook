package com.taiwanmobile.myBook_PAD;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tw.com.mebook.lab.listview.BookcaseAdapter;
import tw.com.soyong.utility.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.DataClass;
import com.gsimedia.sa.GSiMediaRegisterProcess.DeviceIDException;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.gsimedia.sa.GSiMediaRegisterProcess.IllegalNetworkException;
import com.gsimedia.sa.GSiMediaRegisterProcess.TimeOutException;
import com.gsimedia.sa.GSiMediaRegisterProcess.XmlException;
import com.gsimedia.sa.GSiMediaRegisterProcess.XmlP12FileException;
import com.taiwanmobile.myBook_PAD.BookList.ViewHolder;
import com.twm.android.ssoutil.LoginData;
import com.twm.android.ssoutil.TWMAuth;
import com.twm.android.ssoutil.TWMAuthListener;


public class RealBookcase extends Activity {
	
	private static final String TAG = "RealBookcase" ;
	private static final boolean CAN_RUN_ON_PHONE = true ;
	private static final boolean DEBUG = true ;
 
	private boolean mIsEdit;
	
    private Cursor cursorDBData;
	private String deviceID = "";
	private String p12Path;

	private String downloadBookUrl;

	private static  TWMDB tdb;
	private String saveFilelocation;

	private boolean unOnline = true;
	private ProgressDialog mDlg;
	private ProgressDialog mXMLDlg;
	private SharedPreferences settings;	
	
	private boolean bOnlyOneStartActivity = true;
	
        //add from vic
	public RealBookcase() {
		super();
	}

	//add
	private long downloadHeapSize = 0;
	public ViewHolder[] mainRow = null;
	private ImageButton ib_buy,ib_edit,ib_tools,ib_up_page,ib_del_top,ib_del_bottom,ib_all_select,ib_all_unselect,ib_realbook,ib_listbook,ib_auth;
	private RelativeLayout rl_top , rl_center , rl_edit_mode , rl_main;
	private int nowStyle = 1;//未讀 1 已讀 2  全部 3 4 5
	private ListView lv_main;	
	private Boolean[] allCheckBoxValue = null;
	private Boolean downloadStatus = false;
		
	private long freeSize;
	private StatFs stat;
	private int nowDownloadNum = 0;
	private final static int MAX_DOWNLOAD_NUM = 5;//上限 5   
	private RelativeLayout rl_tools_alert_dialog;
	private List<String> pbarNowStatusList = new ArrayList<String>();
	private List<Integer> pbarValueList = new ArrayList<Integer>();
	private List<String> downloadID  = new ArrayList<String>();
	
	private static final String BRAND_XML = "/brand.xml";
	public final static int listbook = 0;
	public final static int realbook = 1;
	public final static int onlinebook = 2;
	public final static int nonebooktype = 3;
	private static String token = null;
	private static boolean gprs = false;
	private static boolean networkstatus = false;
	
	private void downloadBrandData() throws ClientProtocolException, IOException, ParserConfigurationException, SAXException {

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 15000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 15000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);		
		
		DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
		HttpResponse response;
		String apiUrl = getResources().getString(R.string.web_api_brand_info);
		HttpGet httGet = new HttpGet(apiUrl);
		
		response = httpclient.execute(httGet);
		
        final HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
		String fileDir = getFilesDir().toString()+BRAND_XML;	

		FileOutputStream fos;
		fos = new FileOutputStream(new File(fileDir));

        byte[] buffer = new byte[2048]; 
        int length; 
        while ( (length = is.read(buffer)) > 0 ) { 
        	fos.write(buffer,0,length);
        } 
        //Close the streams 
        fos.flush(); 
        fos.close(); 
        is.close(); 
        buffer = null;
        
        entity.consumeContent();
	}	
	
    static final String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8*1024);
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
 
        return sb.toString();
    }  

	private HashMap<Integer, Long> mThreadMap = new HashMap<Integer, Long>() ;
	
	private synchronized void addThreadMap(int pos , long timeStamp){
		//mDownloadIDs.add(id);
		mThreadMap.put(pos, timeStamp);
		
		
		if (DEBUG) Log.w("vic", "pos:"+ pos + " timeStamp:"+ timeStamp);
	}	
	
	private synchronized long getTimeStamp(int pos){
		Long obj =  mThreadMap.get(new Integer(pos));
		if (null == obj){
			return 0 ;
		}
		
		return obj.longValue();
	}	
	private static Handler mInitHandler = new Handler();  
    
	Runnable mUpdateResults = new Runnable() {  
        public void run() {  
        	AuthSSOLogin(RealBookcase.this,realbook,getContext(),getHandler());
        }  
    };  		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isListBookReady = false ;          
        TWMImageView.setSize(BookcaseAdapter.Dip2Px(this,128),BookcaseAdapter.Dip2Px(this,128));
		if (DEBUG) Log.e("345", "+RealBook onCreate ") ;
		// check device type pad or phone
//		if (false == CAN_RUN_ON_PHONE) {
//			if (!Util.isPadResolution(this)) {
//				finish();
//				return;
//			}
//		}
		
		testNetwork();
		setContentView(R.layout.realbookcase);
		settings = getSharedPreferences("setting_Preference", 0);
		setViewComponent();
		mContext = this;
		//RealBookStartup(true);
		//AuthSSOLogin(RealBookcase.this,realbook,this,getHandler());
		if(!hasOpened())
			this.showDialog(DIALOG_CHARGE);
		else{
			mInitHandler.postDelayed(mUpdateResults,500);
		}
    }


    /**
     * first open preference
     */
	private static final String EPrefName = "TWMPref";
	private static final String EPrefInit= "PREF_INIT";
	private void firstOpen(){
		SharedPreferences settings = getSharedPreferences(EPrefName, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(EPrefInit, true);
		editor.commit();
	}
	private boolean hasOpened(){
		SharedPreferences aSettings = getSharedPreferences(EPrefName, 0);
		return aSettings.getBoolean(EPrefInit, false);
	}
    /**
     * dialog related
     */
    private static final int DIALOG_CHARGE = 0;
    @Override
	protected Dialog onCreateDialog(int id) {
    	switch(id){
    	case DIALOG_CHARGE:
    		return new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.msgbox_first_time_warning)
			.setPositiveButton(R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialoginterface, int i){           
					firstOpen();
					mInitHandler.postDelayed(mUpdateResults,500);
				}
			}).setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
					firstOpen();
					mInitHandler.postDelayed(mUpdateResults,500);
				}
			})
			.create();
    	}
		return super.onCreateDialog(id);
	}
	private void RealBookStartup(boolean online) {
		isInnerSD();
		
		try{
			deviceID = GSiMediaRegisterProcess.getID(this.getApplicationContext());	//356899020305947
		}catch(Throwable e){
		}
    	if (deviceID == null) deviceID = "";
		p12Path = this.getFilesDir().toString();
		
		settings = getSharedPreferences("setting_Preference", 0);
		tdb = new TWMDB(this);
		
		saveFilelocation = Util.getStorePath(this);
		downloadBookUrl = getResources().getString(R.string.iii_twm_download_ebook);
		


		mdismissDownloadXMLDlg();
		mXMLDlg = ProgressDialog.show(this, "", "");
		mXMLDlg.setCancelable(false);

		mXMLDlg.show();

		mdismissDlg();
		 
		mDlg = ProgressDialog.show(this, "",
					getResources().getString(R.string.iii_CheckNetworkMessage));
		mDlg.setCancelable(false);
		mDlg.show();
		
		if (online && testNetwork() ){

			mDlg.setMessage(getResources().getString(R.string.iii_CheckVerMessage));
			
			// step1: check version
			if (DEBUG) Log.e("flw", "Test NetWork ok Start to checkVersion()");
			checkVersion();
		} else {
			if (DEBUG) Log.e("flw", "Test NetWork fail Start to downloadXML()");

			unOnline = online;//for sso cancel
			if(!unOnline)
				mdismissDlg();
			else
				mDlg.setMessage(getResources().getString(R.string.iii_NetworkNotConnMessage));
			
			downloadXML();
		}
	}
    
    
	/**
	 * 取得是否有網路連接
	 * @return 是否有網路連接
	 */
	private boolean testNetwork() {
	        ConnectivityManager mgrConnectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);//(ConnectivityManager) ctxs.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo activeNetwork = mgrConnectivity.getActiveNetworkInfo();
	        if(activeNetwork != null)
	        {
	                   Log.d(TAG,"Network =>"+ConnectivityManager.TYPE_MOBILE+":"+activeNetwork.getType());
	                   if (ConnectivityManager.TYPE_MOBILE == activeNetwork.getType())
	                   {
	                	   gprs = true;
	                             //使用到GPRS網路
	                   } else {
	                	   gprs = false;
	                             //使用非GPRS網路
	                   }  
	                   if (!activeNetwork.isConnected()){
	                	   return false;
	                   }
	                   networkstatus = true;
	        }
	        return true;

	}

	
	/**
	 * 檢查p12是否存在
	 * @param path p12路徑
	 * @return p12是否存在  
	 */
	private Boolean isP12Exist(String path){
		return GSiMediaRegisterProcess.isP12Exist(path);
	}	
	
	
	/**
	 * 與伺服器checkDomain/register，下載書單並更新圖片
	 */
//	private void setInit(){
//				
//		//setDeviceID();
//		try{
//			deviceID = GSiMediaRegisterProcess.getID(this.getBaseContext());
//		}catch(Throwable e){
//		};
//    	if (deviceID == null) deviceID = "";
////    	/*test110823*/deviceID = "";
//		if (isP12Exist(p12Path)){
//			checkDomain();		
//			Log.e("checkDomain", "checkDomain");
//		}else{
//			register(p12Path);
//			Log.e("register", "register");
//		}		
//		
//		Log.e(p12Path, p12Path);
//	}
	
	private void checkUserDomain(boolean bThread) {
		final Handler handler = mHandler;
		if (DEBUG) Log.e("flw", "checkUserDomain()");
		if (bThread) {

			if (null == mDlg){
				mDlg = ProgressDialog.show(this, "", getResources().getString(R.string.iii_CheckPhoneMessage));
				mDlg.setCancelable(false);
			}else{
				mDlg.setMessage(getResources().getString(R.string.iii_CheckPhoneMessage));
			}
			mDlg.show();
			new Thread() {
				public void run() {
					mdismissDlg();
					if (isP12Exist(p12Path)) {
						checkDomain();
						

						if (false == needManage) {
							handler.sendMessage(handler.obtainMessage(
									CHECK_USER_DONE, 0, 0));
						}

					} else {
						register(p12Path);
						if (false == needManage) {
							handler.sendMessage(handler.obtainMessage(
									CHECK_USER_DONE, 1, 0));
						}

					}
				}
			}.start();
		}
		else{
			if (isP12Exist(p12Path)) {
				checkDomain();
				// mDlg.dismiss();

				if (false == needManage) {
					handler.sendMessage(handler.obtainMessage(
							CHECK_USER_DONE, 0, 0));
				}

			} else {
				register(p12Path);
				if (false == needManage) {
					handler.sendMessage(handler.obtainMessage(
							CHECK_USER_DONE, 1, 0));
				}

			}			
		}
	}
	
	
	/**
	 * 秀出訊息至畫面
	 * @param message 訊息
	 */
	private void showAlertMessage(String message) {
		new AlertDialog.Builder(this).setTitle(R.string.iii_showAM_exception)
				.setMessage(message).setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {

							}
						}).show();
	}	
	
	
	private static final int SHOW_MSG = 100;
	private static final int SHOW_STR_MSG = 101;
	private static final int CHECK_VER_ACT = 200;
	private static final int CHECK_VER_MSG = 201;
	private static final int CHECK_VER_DONE = 202;
	private static final int CHECK_USER_DONE = 300;
	private static final int MANAGE_DOMAIN = 500;
	private static final int MANAGE_DOMAIN_DONE = 501;
	private static final int DOWNLAD_XML_DONE = 600;

	private static final int REALBOOK_STARTUP = 700;
	private static final int REALBOOK_RESUME = 701;
	private static final int REALBOOK_AUTHSTATUS = 702;
	private static final int REALBOOK_DESCRIPTION = 703;
	private static final int AUTH_MSG = 710;
	private static final int DEVICEID_EMPTY_MSG = 720;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {

			String str ;
			switch (msg.what) {
			case SHOW_MSG://a
			{
				mdismissDlg();
				
				str = getResources().getString(msg.arg1);
				showAlertMessage(str);
			}break;
			
			case SHOW_STR_MSG://a
			{
				mdismissDlg();
				showAlertMessage((String)msg.obj);
			}
			break;
			case CHECK_VER_ACT://a
			{
				if (DEBUG) Log.e("flw", "+CHECK_VER_ACT");
				int force = msg.arg1;
				onCheckVer(force);
				if ( 0 == force ){
					// normal start app
					//checkUserDomain();
				}
				
			}break;
			
				
			case CHECK_VER_MSG://a
				str = getResources().getString(msg.arg1);
				mdismissDlg();
				mdismissDownloadXMLDlg();
				
				new AlertDialog.Builder(RealBookcase.this).setTitle(R.string.iii_showAM_exception)
					.setMessage(str).setPositiveButton(R.string.iii_showAM_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									checkUserDomain(true);
							}
						}).show();
				// continue start app
				break;
			case CHECK_VER_DONE://a
				// normal start app
				if (DEBUG) Log.e("flw", "+CHECK_VER_DONE");
				checkUserDomain(true);
				break;

			case CHECK_USER_DONE://a
				if (DEBUG) Log.e("flw", "+CHECK_USER_DONE");
				mdismissDlg();
				downloadXML();
				
				break;
				
			case MANAGE_DOMAIN:
				
				if (DEBUG) Log.e("flw", "+MANAGE_DOMAIN");
				manageDomain(msg.obj);
				//if (DEBUG) Log.e("flw", "-MANAGE_DOMAIN");
				break;
				
				
			case MANAGE_DOMAIN_DONE:
				if (DEBUG) Log.e("flw", "+MANAGE_DOMAIN_DONE");
				mdismissDlg();
				downloadXML();
				break;
				
			case DOWNLAD_XML_DONE://a
				if (DEBUG) Log.e("flw", "+DOWNLAD_XML_DONE");
				mdismissDownloadXMLDlg();
				initViews();
				break;

			case REALBOOK_STARTUP:{
				if (DEBUG) Log.e("flw", "+REALBOOK_STARTUP");
				int on = msg.arg1;
				if(on == 1)
					RealBookStartup(true);
				else
					RealBookStartup(false);
			}break;
			case REALBOOK_RESUME:{
				if (DEBUG) Log.e("flw", "+REALBOOK_RESUME");
				mIsEdit = false;
				RealBookResume();
			}break;
			case REALBOOK_AUTHSTATUS:
				if (DEBUG) Log.e("flw", "+REALBOOK_AUTHSTATUS");
				int auth = msg.arg1;
				if(auth ==1){
					AuthBtnStatus(true);
				}else{
					AuthBtnStatus(false);
				}
				
				break;
			case REALBOOK_DESCRIPTION:
				String desc = null;
				desc = (String) msg.obj;
				ShowAuthDescription(desc);

			case AUTH_MSG:
				showAuthMessage(msg);
				break;
				
			case DEVICEID_EMPTY_MSG:				
				showDeviceIDEmptyMessage(msg);				
				break;
			}
			
		}

	
		
	};
	private void mdismissDlg() { 
		if ( mDlg != null) {
			try {
				if (mDlg.isShowing()) {
					mDlg.dismiss();
					mDlg = null;
				}

			} catch (Exception e) {
				// nothing
				e.printStackTrace();
			}
		}
	}
	private void ShowAuthDescription(String s) {
		// TODO Auto-generated method stub
		mdismissDlg();
		mdismissDownloadXMLDlg();
		new AlertDialog.Builder(RealBookcase.this)
		.setTitle(R.string.msgbox_expire_title)
		.setMessage(s)//R.string.msgbox_expire_content)
		.setPositiveButton(R.string.iii_showAM_ok,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialoginterface, int i){
						AuthSSOLogin(RealBookcase.this,RealBookcase.realbook,getContext(),getHandler());
					}
				}
		).show();
	}
	protected void showAuthMessage(Message msg) {
		// TODO Auto-generated method stub
		String desc = (String)msg.obj;
		
		new AlertDialog.Builder(RealBookcase.this)
		.setTitle(R.string.msgbox_expire_title)
		.setMessage(desc)//R.string.msgbox_expire_content)
		.setPositiveButton(R.string.iii_showAM_ok,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialoginterface, int i){
						//retrieve token
						mdismissDlg();						
						AuthSSOLogin(RealBookcase.this,listbook,getContext(),getHandler());

						
					}
				}
		)
		.setNegativeButton(R.string.iii_showAM_cancel,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialoginterface, int i){
						//cancel
						mdismissDlg();
					}
			}
		)				
		.show();				
	}
	protected void showDeviceIDEmptyMessage(Message msg) {
		// TODO Auto-generated method stub
		String desc = (String)msg.obj;
		
		new AlertDialog.Builder(RealBookcase.this)	
		.setTitle(R.string.GSI_ERROR)
		.setMessage(desc)
		.setPositiveButton(R.string.iii_showAM_ok,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialoginterface, int i){
						//retrieve token
						mdismissDlg();						
						AuthSSOLogin(RealBookcase.this,listbook,getContext(),getHandler());


					}
				}
		)
		.setNegativeButton(R.string.iii_showAM_cancel,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialoginterface, int i){
						//cancel
						mdismissDlg();
					}
			}
		)				
		.show();				
	}
	private void mdismissDownloadXMLDlg() {
		if ( mXMLDlg != null) {
			try {
				if (mXMLDlg.isShowing()) {
					mXMLDlg.dismiss();
					mXMLDlg = null;
				}

			} catch (Exception e) {
				// nothing
				e.printStackTrace();
			}
		}
	}		
	private void manageDomain(Object obj){
		
		final Bundle data = (Bundle) obj ;
		String[] ase1,ase2;
		ase1 = (String[]) data.get("index");
		ase2 = (String[]) data.get("type");
		if (ase1.length<3){
			String[] ase3 = new String[3];
			String[] ase4 = new String[3];				
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
	
	/**
	 * 產生manageDomain列表ui
	 * @param ase1 手機index
	 * @param ase2 手機名稱
	 */
	private void manageDomain(String[] ase1,String[] ase2){	
		final String index0 = ase1[0];
		final String index1 = ase1[1];
		final String index2 = ase1[2];

		final Handler handler = mHandler ;
		new AlertDialog.Builder(RealBookcase.this)
		  .setTitle(R.string.iii_manage_domain)
		  .setCancelable(false)
		  .setItems(ase2, 
			  new DialogInterface.OnClickListener(){
				 public void onClick(DialogInterface dialog, int whichcountry) {
					 mDlg = ProgressDialog.show(RealBookcase.this, "", getResources().getString(R.string.iii_CheckPhoneMessage));
					 mDlg.setCancelable(false);
					 switch(whichcountry) {
						 case 0:
								new Thread(){
									public void run(){
										manageDomain(index0);	
										handler.sendEmptyMessage(MANAGE_DOMAIN_DONE);
									}
								}.start();	
							 break;								 
						 case 1:
								new Thread(){
									public void run(){
										manageDomain(index1);
										handler.sendEmptyMessage(MANAGE_DOMAIN_DONE);
									}
								}.start();	
							 break;								 
						 case 2:	
								new Thread(){
									public void run(){
										manageDomain(index2);	
										handler.sendEmptyMessage(MANAGE_DOMAIN_DONE);
									}
								}.start();
							 break;
						 case 3:
							 handler.sendEmptyMessage(MANAGE_DOMAIN_DONE);
							 break;
						 default :
							 handler.sendEmptyMessage(MANAGE_DOMAIN_DONE);
							 break;
					 }		
				 }				 
			  }
		  ).setNegativeButton(R.string.iii_showAM_cancel,
			  new DialogInterface.OnClickListener(){
				 public void onClick(DialogInterface dialog, int whichcountry) {
					 dialog.dismiss();
					 unOnline = false;
					 handler.sendEmptyMessage(MANAGE_DOMAIN_DONE);
				 }
		  	  }
	      )
	      .setCancelable(false)
	      .show();
	}
	/**
	 * 取得manageDomain所回傳的資料  
	 * @param index 手機index
	 */
	private void manageDomain(String index){
		final Handler handler = mHandler;
		try {
			DataClass dataclass = GSiMediaRegisterProcess.manageDomain(index,RealBookcase.this,RealBookcase.getToken());
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
				 needManage = false; 
				 checkResultCode(3,dataclass.resultCode_P12,dataclass.resultCode_Domain,dataclass.index,dataclass.type);
				 
			 }else {
				 handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_UnknowMessage, 0));
				 unOnline = false;
			 }
		} catch (IllegalNetworkException e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_NetworkNotConnMessage, 0));
			unOnline = false;
			e.printStackTrace();
		} catch (TimeOutException e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_ServerTimeOutMessage, 0));
			unOnline = false;
			e.printStackTrace();
		} catch (DeviceIDException e) {
			//handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_GetDeviceIDErrorMessage, 0));
			handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.GSI_DEVICE_ID_EMPTY_MSG, 0));
			unOnline = false;
			e.printStackTrace();
		} catch (XmlException e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_ServerReturnErrorMessage, 0));
			unOnline = false;
			e.printStackTrace();
		}catch (Exception e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_UnknowMessage, 0));
			unOnline = false;
			e.printStackTrace();
		}			
	}	
	

	private void onCheckVer(int force) {
		if (DEBUG) Log.e("flw", "onCheckVer() = ["+force+"]");
	    //TODO:ByPass
		force = 0 ;
		if (force != 0) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.iii_VerUpdateMessage)
				.setMessage(R.string.iii_VerUpdateMessageForce)
				.setCancelable(false)
				.setPositiveButton(R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface,
								int i) {
							if(mApkDLUrl != null)
								startActivity((new Intent()).setAction(
									Intent.ACTION_VIEW).setData(Uri.parse(mApkDLUrl)));
							exit();

						}
					}).setNegativeButton(R.string.iii_showAM_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface,
								int i) {
							exit();
						}
					}).show();
		} else {
			final Handler handler = mHandler;
			new AlertDialog.Builder(this)
				.setTitle(R.string.iii_VerUpdateMessage)
				.setMessage(R.string.iii_VerUpdateMessageUnforce)
				.setCancelable(false)
				.setPositiveButton(	R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface,
								int i) {
							if(mApkDLUrl != null)
								startActivity((new Intent()).setAction(
									Intent.ACTION_VIEW).setData(Uri.parse(mApkDLUrl)));
							new Thread() {
								public void run() {
									checkUserDomain(false);
									// ??setInit();
								}
							}.start();
						}
					}).setNegativeButton(R.string.iii_showAM_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface,
								int i) {
							new Thread() {
								public void run() {
									checkUserDomain(false);								
								}
							}.start();
						}
					}).show();
		}
	}
	
	/**
	 * 取得checkDomain所回傳的資料  
	 */
	private void checkDomain(){	
		final Handler handler = mHandler;
		try {
			DataClass dataclass = GSiMediaRegisterProcess.checkDomain(this,RealBookcase.getToken());	
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
				 needManage = false; 
				 checkResultCode(0,dataclass.resultCode_P12,dataclass.resultCode_Domain,dataclass.index,dataclass.type);
				 return ;
				 
			 } else {
				handler.sendMessage(handler.obtainMessage(SHOW_MSG,R.string.iii_UnknowMessage, 0));
			}
		} catch (IllegalNetworkException e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG,R.string.iii_NetworkNotConnMessage, 0));
			e.printStackTrace();
		} catch (TimeOutException e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG,R.string.iii_ServerTimeOutMessage, 0));
			e.printStackTrace();
		} catch (DeviceIDException e) {
			//handler.sendMessage(handler.obtainMessage(SHOW_MSG,R.string.iii_GetDeviceIDErrorMessage, 0));
			handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.GSI_DEVICE_ID_EMPTY_MSG, 0));
			e.printStackTrace();
		} catch (XmlException e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG,R.string.iii_ServerReturnErrorMessage, 0));
			e.printStackTrace();
		}	catch (Exception e) {
			handler.sendMessage(handler.obtainMessage(SHOW_MSG,R.string.iii_NetworkNotConnMessage, 0));
			e.printStackTrace();
		}
		unOnline = false;
	}	
	
	private boolean needManage;
	/**
	 * 依據server回傳的code做出不同動作
	 * @param model 0 checkDomain   1 register  3 manageDomain
	 * @param resultCode_P12 resultCode_P12
	 * @param resultCode_Domain resultCode_Domain
	 * @param index 手機id
	 * @param type 手機名稱
	 */
	private void checkResultCode(int model , String resultCode_P12,String resultCode_Domain,String[] index,String[] type){
		
		String resultCode = "";
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
		}

		final Handler handler = mHandler ;
		if(resultCode.equals("0")){
			if(model == 1){
				if(resultCode_Domain.equals("-1")){
					unOnline = false;
					Bundle data = new Bundle();
					data.putStringArray("index", index);
					data.putStringArray("type", type);
					needManage = true;
					handler.sendMessage(handler.obtainMessage(MANAGE_DOMAIN, data));

				}else if(resultCode_Domain.equals("-3")){
					handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_register_num_over_limit, 0) );
					unOnline = false;
				}
			}
		}else if(resultCode.equals("-10")){
			if(model == 1){
				// do notthing ???
			}
		}else if(resultCode.equals("-1")){
			unOnline = false;
			Bundle data = new Bundle();
			data.putStringArray("index", index);
			data.putStringArray("type", type);
			needManage = true;
			handler.sendMessage(handler.obtainMessage(MANAGE_DOMAIN, data));
			
		}else if(resultCode.equals("-8")){
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_plz_use_twm_net, 0) );
			unOnline = false;
		}else if(resultCode.equals("-6")){
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_server_busy_only_unline, 0) );
			unOnline = false;
		}else if(resultCode.equals("-7")||resultCode.equals("-9")){
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_server_register_error, 0) );
			unOnline = false;
		}else if(resultCode.equals("-3")){			
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_register_num_over_limit, 0) );
			unOnline = false;
		}else if(resultCode.equals("-4")){
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_cant_update_phone, 0) );
			unOnline = false;
		}else if(resultCode.equals("-20")){
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.token_expire, 0) );
			unOnline = false;
		}else{
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_UnknowMessage, 0) );
			unOnline = false;
		}	
	}	
	
	/**
	 * 取得register所回傳的資料  
	 */
	public void register(String path){
		final Handler handler = mHandler;
		try {
			DataClass dataclass = GSiMediaRegisterProcess.register(path, this,RealBookcase.getToken());
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
				needManage = false; 
				checkResultCode(1, dataclass.resultCode_P12,dataclass.resultCode_Domain,dataclass.index,dataclass.type);
			}else {
				Log.d("dataclass index =","null");
			}
		} catch (IllegalNetworkException e) {
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_NetworkNotConnMessage, 0) );
			unOnline = false;
			e.printStackTrace();
		} catch (TimeOutException e) {
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_ServerTimeOutMessage, 0) );
			unOnline = false;
			e.printStackTrace();
		} catch (XmlP12FileException e) {
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_get_p12_error, 0) );
			unOnline = false;
			e.printStackTrace();
		} catch (DeviceIDException e) {
			//handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_GetDeviceIDErrorMessage, 0) );
			handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.GSI_DEVICE_ID_EMPTY_MSG, 0));
			unOnline = false;
			e.printStackTrace();	
		} catch (XmlException e) {
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_ServerReturnErrorMessage, 0) );
			unOnline = false;
			e.printStackTrace();	
		}catch (Exception e) {
			handler.sendMessage( handler.obtainMessage(SHOW_MSG, R.string.iii_UnknowMessage, 0) );
			unOnline = false;
			e.printStackTrace();
		}	
	}	
	
	
	/**
	 * 下載書單並解析
	 * @param xml 書單
	 */
	private String ebook_error;
	private String ebook_description;
	private String ebook_update_at;
	
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
//	private List<String> ebook_cover_local_path = null;
	
    private void downloadXML() {

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
//		ebook_cover_local_path = new ArrayList<String>();
		
		final Handler handler = mHandler;
		boolean isAutoSync = settings.getBoolean("setting_auto_sync_new_book_value", true);
//isAutoSync = false ;		//TODO for debug
		if (DEBUG) Log.e("flw", "downloadXML() = ["+isAutoSync+"]");
		if (isAutoSync && unOnline) {

			final String xml = getResources().getString(
					R.string.iii_twm_download_list_by_updated)
					+ String.valueOf(settings.getString("update_at", "0")) + "&device_id=" + deviceID+"&token="+token;
			if(mXMLDlg != null){
				mXMLDlg.setMessage(getResources().getString(R.string.iii_SyncMessage));
				mXMLDlg.show();
			}
			new Thread() {

				public void run() {
					Document doc = null;
					HttpURLConnection conn = null;
					try {
						URL myURL = new URL(xml);
						
//						conn = (HttpURLConnection) myURL.openConnection();
//
//						DocumentBuilderFactory dbf = DocumentBuilderFactory
//								.newInstance();
//						DocumentBuilder db = dbf.newDocumentBuilder();
//						conn.setConnectTimeout(CONNECT_TIMEOUT);
//						conn.setReadTimeout(READ_TIMEOUT);
//						InputStream is = conn.getInputStream();
//
//						doc = db.parse(is);
//						String urlParameters = "&token="+token; 
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

//						// Send request
//						DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//						wr.writeBytes(urlParameters);
//						wr.flush();
//						wr.close();

						// Get Response
						int resp = conn.getResponseCode();
						if (resp != HttpURLConnection.HTTP_OK) {
							handler.sendMessage(handler.obtainMessage(SHOW_MSG,
									R.string.iii_NetworkNotConnMessage, 0));
							return;
						}			
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						InputStream is = conn.getInputStream();	
						doc = db.parse(is);
						
						
						NodeList nStatus = doc.getElementsByTagName("status");
						ebook_error = nStatus.item(0).getChildNodes().item(0).getNodeValue().toString();

						NodeList nDesc = doc.getElementsByTagName("description");
						ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();
						
						NodeList nData = doc.getElementsByTagName("data");
						String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
						InputStream dataStream = covertStringToStream(RevData);
						doc = db.parse(dataStream);

						
//						URL myURL = new URL(xml);
//						conn = (HttpURLConnection) myURL.openConnection();
//
//						DocumentBuilderFactory dbf = DocumentBuilderFactory
//								.newInstance();
//						DocumentBuilder db = dbf.newDocumentBuilder();
//						conn.setConnectTimeout(CONNECT_TIMEOUT);
//						conn.setReadTimeout(READ_TIMEOUT);
//						InputStream is = conn.getInputStream();
//
//						doc = db.parse(is);

						
						if (ebook_error.equals("1")) {
						NodeList nError = doc.getElementsByTagName("error");
						String error = nError.item(0).getChildNodes().item(0)
								.getNodeValue().toString();

						if (error.equals("0")) {
							NodeList nUpdate_at = doc
									.getElementsByTagName("update_at");
							ebook_update_at = nUpdate_at.item(0)
									.getChildNodes().item(0).getNodeValue()
									.toString();

							NodeList nDeliveryID = doc
									.getElementsByTagName("Delivery-ID");
							NodeList nContent_id = doc
									.getElementsByTagName("content_id");
							NodeList nTitle = doc.getElementsByTagName("title");
							NodeList nPublisher = doc
									.getElementsByTagName("publisher");
							NodeList nAuthors = doc
									.getElementsByTagName("authors");
							NodeList nEbook_type = doc
									.getElementsByTagName("ebook_type");
							NodeList nEbook_category = doc
									.getElementsByTagName("ebook_categories");
							NodeList nUpdate_date = doc
									.getElementsByTagName("update_date");
							NodeList nPurchased_at = doc
									.getElementsByTagName("purchased_at");
							NodeList nTrial = doc.getElementsByTagName("trial");
							NodeList nVertical = doc
									.getElementsByTagName("vertical");
							NodeList nTrial_due_date = doc
									.getElementsByTagName("trial_due_date");
							NodeList nCover = doc.getElementsByTagName("cover");
							NodeList nBodytypeCode = doc
									.getElementsByTagName("bodytype_code");

							for (int i = 0; i < nTitle.getLength(); i++) {
								ebook_title.add(nTitle.item(i).getChildNodes()
										.item(0).getNodeValue());
								ebook_type
										.add(nEbook_type.item(i)
												.getChildNodes().item(0)
												.getNodeValue());
								ebook_cover.add(nCover.item(i).getChildNodes()
										.item(0).getNodeValue());
								ebook_deliveryID
										.add(nDeliveryID.item(i)
												.getChildNodes().item(0)
												.getNodeValue());
								ebook_purchased_at
										.add(nPurchased_at.item(i)
												.getChildNodes().item(0)
												.getNodeValue());
								ebook_publisher
										.add(nPublisher.item(i).getChildNodes()
												.item(0).getNodeValue());
								ebook_authors
										.add(nAuthors.item(i).getChildNodes()
												.item(0).getNodeValue());
								ebook_trial.add(nTrial.item(i).getChildNodes()
										.item(0).getNodeValue());

								ebook_contentID
										.add(nContent_id.item(i)
												.getChildNodes().item(0)
												.getNodeValue());

								ebook_bodytype_code
										.add(nBodytypeCode.item(i)
												.getChildNodes().item(0)
												.getNodeValue());

								ebook_update_date
										.add(nUpdate_date.item(i)
												.getChildNodes().item(0)
												.getNodeValue());

								ebook_vertical
										.add(nVertical.item(i).getChildNodes()
												.item(0).getNodeValue());

								if (nTrial_due_date.item(i).getChildNodes()
										.getLength() > 0) {
									ebook_trial_due_date.add(nTrial_due_date
											.item(i).getChildNodes().item(0)
											.getNodeValue());
								} else {
									ebook_trial_due_date.add("");
								}

								int j = 0;
								String temp = "";
								do {
									temp = temp
											+ nEbook_category.item(i)
													.getChildNodes().item(j)
													.getChildNodes().item(0)
													.getNodeValue() + "|";
									j = j + 2;
								} while (j < nEbook_category.item(i)
										.getChildNodes().getLength());
								ebook_category.add(temp);

							}
							
							settings.edit().putString("update_at", ebook_update_at).commit();
							
							insertDB();
							handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_SyncSucessMessage, 0));
							
						} else {
							unOnline = false;
							handler.sendMessage(handler.obtainMessage(
									SHOW_STR_MSG, error));

						
						}
						}else{
							if(deviceID.length() == 0){
		 						handler.sendMessage(handler.obtainMessage(DEVICEID_EMPTY_MSG,0, 0,getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG)));
		 					}else{
							handler.sendMessage(handler.obtainMessage(
									AUTH_MSG,0, 0,ebook_description));
						}
						}
					} catch (Exception e) {
						unOnline = false;
						handler.sendMessage(handler.obtainMessage(SHOW_MSG,
								R.string.iii_get_book_list_error, 0));
						e.printStackTrace();
					}
					
					
					loadFromDB();
					
					handler.sendEmptyMessage(DOWNLAD_XML_DONE);
				}
			}.start();
		} else {
			//不同步書單
			
			new Thread(){
				public void run(){
					loadFromDB();
					handler.sendEmptyMessage(DOWNLAD_XML_DONE);
				}
			}.start();
		}
	}
    
    
    private final String TAG_AP_NAME = "AP_NAME";
    private final String TAG_AP_PIC1 = "AP_PIC1";
    private final String TAG_AP_URL = "AP_URL";
    private final String TAG_AP_FAIL_URL = "AP_FAIL_URL";
    
    private List<String> mBrandNames = null;
    private List<String> mBrandPIC1s = null;
    private List<String> mBrandURLs = null;
    private List<String> mBrandFailURLs = null;
    
    private void loadBradData(){
    	
    	mBrandNames = new ArrayList<String>();
    	mBrandPIC1s = new ArrayList<String>();
    	mBrandURLs = new ArrayList<String>();
    	mBrandFailURLs = new ArrayList<String>();
    	
		String fileDir = getFilesDir().toString()+BRAND_XML;	

		try {
			InputStream is = new FileInputStream(fileDir);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(is);
			
			NodeList apNames = doc.getElementsByTagName(TAG_AP_NAME);
			NodeList apPic1s = doc.getElementsByTagName(TAG_AP_PIC1);
			NodeList apUrls = doc.getElementsByTagName(TAG_AP_URL);
			NodeList apFailUrls = doc.getElementsByTagName(TAG_AP_FAIL_URL);
			is.close();
			
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
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
    
    /**
     * Load data into memory
     */
    private void loadFromDB(){
    	
    	//////////////////////////////////////////////////////
		try {
			downloadBrandData();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		loadBradData();
		///////////////////////////////////////////////////////

    }
    
	/**
	 * 將全部的書本資料加入手機資料庫中
	 */
    private String[] fileType = {".teb",".tvb",".tpb"};
	private void insertDB() {
		
		final List<String> deliveryIDs = ebook_deliveryID;
		final List<String> contentIDs = ebook_contentID;
		final List<String> titles = ebook_title;
		final List<String> publishers = ebook_publisher;
		final List<String> authorses = ebook_authors;
		final List<String> types = ebook_type;
		final List<String> categorys = ebook_category;
		final List<String> update_dates = ebook_update_date;
		final List<String> purchased_ats = ebook_purchased_at;
		final List<String> trials = ebook_trial;
		final List<String> verticals = ebook_vertical;
		final List<String> trial_due_dates = ebook_trial_due_date;
		final List<String> covers = ebook_cover;
		final List<String> bodytype_codes = ebook_bodytype_code;	

		String tempTypeCover ,tempType;
		final String mebookExt = getResources().getString(R.string.iii_mebook);
		final TWMDB db = tdb ;
		final int size = ebook_title.size();
		for(int i=0;i<size;i++){
			
			if(types.get(i).toString().equals(mebookExt)){
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
			
			Cursor  c = db.select("deliveryID = '"+deliveryIDs.get(i).toString()+"'");
			if(c.getCount()==0)
				db.insert(titles.get(i).toString(),types.get(i).toString(),categorys.get(i).toString(),
						covers.get(i).toString(),deliveryIDs.get(i).toString(),"0",
						String.valueOf(System.currentTimeMillis()),"0",tempTypeCover,
						purchased_ats.get(i).toString(),publishers.get(i).toString(),authorses.get(i).toString(),
						trials.get(i).toString(),"0",contentIDs.get(i).toString(),
						update_dates.get(i).toString(),verticals.get(i).toString(),trial_due_dates.get(i).toString(),
						saveFilelocation + deliveryIDs.get(i).toString()+tempType,"0",tempType,
						"");
            this.stopManagingCursor(c);
			c.close();
		}
	}  
	
	private static final int CONNECT_TIMEOUT = 15000 ; 
	private static final int READ_TIMEOUT = 15000;	
	
	private String mApkDLUrl ;
	
	/**
	 * 連接伺服器檢查是否有更新檔
	 */
	private void checkVersion(){
		if (DEBUG) Log.e("flw", "checkVersion()");
 		final Handler handler = mHandler;
 		
 		new Thread(){
 			public void run(){
 				try {
 			 		String error = "",force_update = "0";
 					
 					PackageInfo pInfo = null;
          /*Modified By Lancelot
 					Package pkgName = this.getClass().getPackage();
// 					String name = pkgName.getName();
 					pInfo = getPackageManager().getPackageInfo(pkgName.getName(),PackageManager.GET_META_DATA);
			    */
// 					pInfo = getPackageManager().getPackageInfo("com.taiwanmobile.myBook_PAD",PackageManager.GET_META_DATA);
// 					URL myURL = new URL(getResources().getString(R.string.iii_version_control)+"&version="+pInfo.versionName);
// 					HttpURLConnection conn = null;
//		   	 		conn = (HttpURLConnection)myURL.openConnection();
//		   	 		conn.setConnectTimeout (CONNECT_TIMEOUT) ;
//		   	 		conn.setReadTimeout(READ_TIMEOUT);
//		            conn.setDoInput(true);
//		            conn.connect();
//		            
//		            // The line below is where the exception is called
//		            int response = conn.getResponseCode();  
//		            if (response != HttpURLConnection.HTTP_OK) {
//		            	handler.sendMessage(handler.obtainMessage(CHECK_VER_MSG,R.string.iii_NetworkNotConnMessage, 0));
//		            	return ;
//		            }
//            
//		   	 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		   	        DocumentBuilder db = dbf.newDocumentBuilder();
//		   	        InputStream is = conn.getInputStream();
 					Document doc = null;
 			   	 	HttpURLConnection conn = null;
 					pInfo = getPackageManager().getPackageInfo("com.taiwanmobile.myBook_PAD",PackageManager.GET_META_DATA);
 					URL myURL = new URL(getResources().getString(R.string.iii_version_control)+"&version="+pInfo.versionName+"&token="+token);
 					//String urlParameters = "&version=" + pInfo.versionName+"&token="+token; 
 					conn = (HttpURLConnection) myURL.openConnection();
 					conn.setRequestMethod("POST");
 					conn.setRequestProperty("Content-Type",
 							"application/x-www-form-urlencoded");

 					
 					conn.setRequestProperty("Content-Language", "UTF-8");

 					conn.setUseCaches(false);
 					conn.setDoInput(true);
 					conn.setDoOutput(true);

 					// Send request
// 					DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
// 					wr.writeBytes(urlParameters);
// 					wr.flush();
// 					wr.close();

 					// Get Response
 					int resp = conn.getResponseCode();
 					if (resp != HttpURLConnection.HTTP_OK) {
 						handler.sendMessage(handler.obtainMessage(SHOW_MSG, R.string.iii_NetworkNotConnMessage, 0));
 						
 						return;
 					}
 					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 					DocumentBuilder db = dbf.newDocumentBuilder();
 					InputStream is = conn.getInputStream();		   	        
 					doc = db.parse(is);
 					NodeList nStatus = doc.getElementsByTagName("status");
 					ebook_error = nStatus.item(0).getChildNodes().item(0).getNodeValue().toString();
 					
 					NodeList nDesc = doc.getElementsByTagName("description");
 					ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();

 					NodeList nData = doc.getElementsByTagName("data");
 					String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
 					InputStream dataStream = covertStringToStream(RevData);
 					doc = db.parse(dataStream);
 					
// 					nError = doc.getElementsByTagName("error");
//						if (nError.item(0) != null)
//							error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
						
 					
 					
 			
//		   	        byte[] buffer = new byte[4096]; 
//		   	        int length; 
//		   	        
////		   	        while ( (length = is.read(buffer)) > 0 ) { 
////		   	        	//fos.write(buffer,0,length);
////		   	        	sb
////		   	        }   
//		   	        length = is.read(buffer);
//		   	        String str = new String(buffer , 0 , length , "UTF8");
//		   	        StringBuilder sb = new StringBuilder(str);
//		   	        int esc = sb.indexOf("&amp;");
//		   	        int nonEsc = sb.indexOf("&");
//		   	        
//		   	        if ( -1== esc && -1 != nonEsc){
//		   	        	int start = sb.length() - 1; ;
//		   	        	while (start != -1){
//		   	        		start = sb.lastIndexOf("&" , start);
//		   	        		if ( start != -1 ){
//		   	        			sb.replace(start, start+1, "&amp;");
//		   	        			start -- ;
//		   	        		}
//		   	        	}
//		   	        }
//		   	        
//		   	        byte [] buf = sb.toString().getBytes();
//		   	        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
//  	        
//		   	        doc = db.parse(bis);
 					if (ebook_error.equals("1")) {
 						
 					NodeList nError=doc.getElementsByTagName("error");
		   	        if(nError.item(0)!=null){
		   	        	error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
		   	        }

		   	        NodeList nforce_update=doc.getElementsByTagName("force_update");
		   	        if(nforce_update.item(0)!=null){
		   	        	force_update = nforce_update.item(0).getChildNodes().item(0).getNodeValue().toString();
		   	        }
		   	        NodeList ndownload_url=doc.getElementsByTagName("download_url");
   	        
		   	        if(ndownload_url.item(0)!=null){
		   	        	//mApkDLUrl = ndownload_url.item(0).getChildNodes().item(0).getNodeValue().toString();  
		   	        	final int nodeCount = ndownload_url.item(0).getChildNodes().getLength();
		   	        	StringBuilder sb2 = new StringBuilder();
		   	        	for ( int i = 0 ; i < nodeCount ; i++){
		   	        		sb2.append(ndownload_url.item(0).getChildNodes().item(i).getNodeValue().toString());
		   	        	}
		   	        	mApkDLUrl = sb2.toString();
		   	        	if (null != mApkDLUrl){
		   	        		mApkDLUrl = Uri.decode(mApkDLUrl);
		   	        	}
		   	        	sb2 = null;
		   	        }
		   	        
		   	     	if(!error.equals(getResources().getString(R.string.iii_NoNewVer))){
		   	   	        if(force_update.equals("1")){
		   	   	        	unOnline = false;

		   	   	        	handler.sendMessage(handler.obtainMessage(CHECK_VER_ACT, 1, 0));
		   	   	        }else if(force_update.equals("0")){
		   	   	        	handler.sendMessage(handler.obtainMessage(CHECK_VER_ACT, 0, 0));
		   	   	        }
		   	   	        mDlg.dismiss();
		   	   	        return ;
		   	        }
 					
		   	     	
		   	     	// why could reach here???
		   	     if (DEBUG) Log.e("flw", "checkVersion() error 1");
		   	     	handler.sendEmptyMessage(CHECK_VER_DONE);
		   	     	return ;
 				}else{
 					if(deviceID.length() == 0){
 						handler.sendMessage(handler.obtainMessage(DEVICEID_EMPTY_MSG,0, 0,getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG)));
 					}else{
 					handler.sendMessage(handler.obtainMessage(AUTH_MSG,R.string.msgbox_expire_content, 0,ebook_description));
 				}	
 				}	
  	        
				} catch (IOException e) {
					handler.sendMessage(handler.obtainMessage(CHECK_VER_MSG,R.string.iii_check_ver_error, 0));
					e.printStackTrace();
				} catch (SAXException e) {
					handler.sendMessage(handler.obtainMessage(CHECK_VER_MSG,R.string.iii_NetworkNotConnMessage, 0));
					e.printStackTrace();
				} catch (Exception e) {
					handler.sendMessage(handler.obtainMessage(CHECK_VER_MSG,R.string.iii_check_ver_error, 0));
					e.printStackTrace();
				}
	
	   	     	// why could reach here???
				if (DEBUG) Log.e("flw", "checkVersion() error 2");


					
	   	     	handler.sendEmptyMessage(CHECK_VER_DONE);
	   	     	//mDlg.dismiss();
	   	     	return ;
			}
 		}.start();
	}

	
	private void initViews(){
		initBookcaseView();
		
		setListener();
		
		isListBookReady = true;
	}
	
	
	private void initBookcaseView(){		
		BookcaseAdapter adapter = new BookcaseAdapter(this);
		
		final ListView bcv = (ListView)this.findViewById(R.id.lv_main);
		int height = bcv.getHeight();
		adapter.setHeight(height);
		
		adapter.setDB(tdb);
		adapter.setDlPath(saveFilelocation);
		adapter.setDeviceID(deviceID);
		adapter.setCoverData(getResources().getString(R.string.iii_cms_cover) /*, coverUrls ,coverLocalPaths*/ );
		adapter.setBookData(downloadBookUrl /*, deliverIDs , bookTypes , ebook_trial , ebook_contentID*/);
		adapter.setBrandData(mBrandNames, mBrandPIC1s , mBrandURLs , mBrandFailURLs );
		bcv.setAdapter(adapter);
	}
	
	

	
	/**
	 * 設定畫面UI 元件
	 */
	private void setViewComponent() {
		lv_main = (ListView)findViewById(R.id.lv_main);
		ib_buy = (ImageButton)findViewById(R.id.ib_buy);
		ib_edit = (ImageButton)findViewById(R.id.ib_edit);
		ib_tools = (ImageButton)findViewById(R.id.ib_tools);
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
		ib_auth = (ImageButton)findViewById(R.id.ib_auth);
		ib_auth.setTag(R.drawable.ivi_button21a);
		
		
		ib_tools.setBackgroundColor(Color.TRANSPARENT);

		
		ib_up_page.setBackgroundColor(Color.TRANSPARENT);	
		
//  		setRead();  		
		
		ib_buy.bringToFront();
		ib_edit.bringToFront();
		ib_tools.bringToFront();
		ib_up_page.bringToFront();
		ib_del_top.bringToFront();
		ib_del_bottom.bringToFront();
		ib_all_select.bringToFront();
		ib_all_unselect.bringToFront();
		ib_realbook.bringToFront();
		ib_listbook.bringToFront();
		setStyle();
		
		ib_listbook.setVisibility(1);
		ib_auth.bringToFront();
	}
	
	/**
	 * 設定畫面主題
	 */
	public void setStyle(){
//		if("".equals(settings.getString("setting_bookcase_background_style_value", ""))|| 
//				getResources().getStringArray(R.array.iii_bookcase_background_style_value)[0].equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_main.setBackgroundResource(R.drawable.wood_ivi_bg01);
			rl_top.setBackgroundResource(R.drawable.wood_ivi_bar03);
			ib_buy.setBackgroundResource(R.drawable.wood_ivi_button08);
			ib_edit.setBackgroundResource(R.drawable.wood_ivi_button09);
			ib_del_top.setBackgroundResource(R.drawable.wood_ivi_button09);
			rl_center.setBackgroundResource(R.drawable.wood_ivi_bar02);
			rl_edit_mode.setBackgroundResource(R.drawable.wood_ivi_bar02);
			ib_del_bottom.setBackgroundResource(R.drawable.wood_ivi_button14);
			ib_all_select.setBackgroundResource(R.drawable.wood_ivi_button14);
			ib_all_unselect.setBackgroundResource(R.drawable.wood_ivi_button14);
			ib_auth.setBackgroundResource(R.drawable.wood_ivi_button09);
			
			ib_realbook.setImageResource(R.drawable.ani_button24_btn_disable);
			ib_listbook.setImageResource(R.drawable.ani_button25_btn_enable);
		
//		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[1].equals(settings.getString("setting_bookcase_background_style_value", ""))){
//			rl_main.setBackgroundResource(R.drawable.technology_ivi_bg01);
//			rl_top.setBackgroundResource(R.drawable.technology_ivi_bar03);
//			ib_buy.setBackgroundResource(R.drawable.technology_ivi_button08);
//			ib_edit.setBackgroundResource(R.drawable.technology_ivi_button09);
//			ib_del_top.setBackgroundResource(R.drawable.technology_ivi_button09);
//			rl_center.setBackgroundResource(R.drawable.technology_ivi_bar02);
//			rl_edit_mode.setBackgroundResource(R.drawable.technology_ivi_bar02);
//			ib_del_bottom.setBackgroundResource(R.drawable.technology_ivi_button14);
//			ib_all_select.setBackgroundResource(R.drawable.technology_ivi_button14);
//			ib_all_unselect.setBackgroundResource(R.drawable.technology_ivi_button14);
//			ib_auth.setBackgroundResource(R.drawable.technology_ivi_button09);
//			
//			ib_realbook.setImageResource(R.drawable.ani_button28_btn_disable);
//			ib_listbook.setImageResource(R.drawable.ani_button29_btn_enable);				
//		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[2].equals(settings.getString("setting_bookcase_background_style_value", ""))){
//			rl_main.setBackgroundResource(R.drawable.romantic_ivi_bg01);
//			rl_top.setBackgroundResource(R.drawable.romantic_ivi_bar03);
//			ib_buy.setBackgroundResource(R.drawable.romantic_ivi_button08);
//			ib_edit.setBackgroundResource(R.drawable.romantic_ivi_button09);
//			ib_del_top.setBackgroundResource(R.drawable.romantic_ivi_button09);
//			rl_center.setBackgroundResource(R.drawable.romantic_ivi_bar02);
//			rl_edit_mode.setBackgroundResource(R.drawable.romantic_ivi_bar02);
//			ib_del_bottom.setBackgroundResource(R.drawable.romantic_ivi_button14);
//			ib_all_select.setBackgroundResource(R.drawable.romantic_ivi_button14);
//			ib_all_unselect.setBackgroundResource(R.drawable.romantic_ivi_button14);
//			ib_auth.setBackgroundResource(R.drawable.romantic_ivi_button09);
//			
//			ib_realbook.setImageResource(R.drawable.ani_button26_btn_disable);
//			ib_listbook.setImageResource(R.drawable.ani_button27_btn_enable);				
//		}
	}	
    private void AuthBtnStatus(boolean login){
    	
    	if(login){
        	//login
        	ib_auth.setImageResource(R.drawable.ivi_button21a);
        	ib_auth.setTag(R.drawable.ivi_button21a);
   		
    	}else{
    		//logout
        	ib_auth.setImageResource(R.drawable.ivi_button21b);
        	ib_auth.setTag(R.drawable.ivi_button21b);
   		
    	}
		
    }	
	/**
	 * 設定畫面事件
	 */
    private void setListener() {
		ib_buy.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(getResources().getString(R.string.iii_book_city_url))) );
				//exit();
				finish();
			}
        });
		ib_auth.setOnClickListener(new ImageButton.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ImageButton imgbtn = (ImageButton) arg0;
				//assert (R.id.ib_auth == imgbtn.getId());
				
				switch ((Integer)imgbtn.getTag()) {
				case R.drawable.ivi_button21a:
					new AlertDialog.Builder(RealBookcase.this)
					.setTitle(R.string.msgbox_logout_title)
					.setMessage(R.string.msgbox_logout_content)
					.setPositiveButton(R.string.iii_showAM_ok,
							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialoginterface, int i){
//									ib_auth.setImageResource(R.drawable.ivi_button21b);
//									ib_auth.setTag(R.drawable.ivi_button21b);
									dialoginterface.dismiss();
									AuthSSOLogout(RealBookcase.this);
									AuthSSOLogin(RealBookcase.this,realbook,getContext(),getHandler());
								}
							}
					)
					.setNegativeButton(R.string.iii_showAM_cancel,
							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialoginterface, int i){
									//cancel
									dialoginterface.dismiss();
								}
						}
					)				
					.show();						

					//logout
					break;
				case R.drawable.ivi_button21b:
//					imgbtn.setImageResource(R.drawable.ivi_button21a);
//					imgbtn.setTag(R.drawable.ivi_button21a);
					AuthSSOLogin(RealBookcase.this,realbook,getContext(),getHandler());
					//login
					break;					
				default:
//					imgbtn.setImageResource(R.drawable.ivi_button21a);
//					imgbtn.setTag(R.drawable.ivi_button21a);
					break;
				}

	
			}
			
		});		
		ib_edit.setOnClickListener(new ImageButton.OnClickListener(){
      	  	

			public void onClick(View v){
      	  		
      	  		mIsEdit = ! mIsEdit ;
      	  		final ListView bcv = (ListView)findViewById(R.id.lv_main);
      	  		BookcaseAdapter adapter = (BookcaseAdapter) bcv.getAdapter();
      	  		adapter.setEditMode(mIsEdit);
      	  		final ImageView iv = (ImageView)v;
      	  		if ( mIsEdit ){
      	  			adapter.onPause();
      	  			
          	  		ib_realbook.setVisibility(View.INVISIBLE);
          	  		ib_listbook.setVisibility(View.INVISIBLE); 
          	  		//v.setBackgroundResource(R.drawable.ivi_button20_btn);
          	  		iv.setImageResource(R.drawable.ivi_button20_btn);
          	  		
      	  		} else {
          	  		ib_realbook.setVisibility(View.VISIBLE);
          	  		ib_listbook.setVisibility(View.VISIBLE); 
          	  		iv.setImageResource(R.drawable.ivi_button09_btn);
      	  		}
      	  	}
        });

		ib_listbook.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
//				ib_realbook.setImageResource(R.drawable.ani_button24_btn_enable);
//				ib_listbook.setImageResource(R.drawable.ani_button25_btn_disable);
				//check listbook is open
				if(bOnlyOneStartActivity){
					bOnlyOneStartActivity = false;
		  	  		Intent intent = new Intent();
		  	  		intent.setClass(RealBookcase.this, TWMBook.class);
		  	  		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		  	  		startActivity(intent);
				}
			}
		});		
		ib_tools.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){

      	  		
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
    }

	/**
	 * 重新計算剩餘空間
	 */
	private void calFreeSize() {
		int size = stat.getBlockSize();
		int num = stat.getAvailableBlocks();
		freeSize = (long)num * size ;
	}

	/**
	 * 取得書本下載連結
	 * @param deliverID deliverID
	 */
	public String getDownloadBookURL(String deliverID){
		return downloadBookUrl+deliverID+"&device_id="+deviceID+"&token="+RealBookcase.getToken()+"&pointer=";
	}	


	/**
	 * 跳出工具表
	 */
	private void toolsAlert(){
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
      	  		intent.setClass(RealBookcase.this, SearchBook.class);
      	  		startActivity(intent);
      	  		rl_tools_alert_dialog.setVisibility(View.GONE);
      	  		setViewComponentEnabled(true);
      	  	}
        });    
        ib_setting.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		Intent intent = new Intent();
      	  		intent.setClass(RealBookcase.this, Setting.class);
      	  		intent.putExtra("saveFilelocation", saveFilelocation);
      	  		startActivity(intent);
      	  		rl_tools_alert_dialog.setVisibility(View.GONE);
      	  		setViewComponentEnabled(true);
      	  	}
        });
        ib_onlinebook.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		Intent intent = new Intent();
      	  		intent.setClass(RealBookcase.this, OnlineBook.class);
      	  		intent.putExtra("saveFilelocation", saveFilelocation);
      	  		startActivity(intent);
      	  		rl_tools_alert_dialog.setVisibility(View.GONE);
      	  		setViewComponentEnabled(true);
      	  	}
        });
	}		


	/**
	 * 離開
	 */
	public void exit() {
		//AuthSSOLogout(RealBookcase.this);
		if (tdb!= null)
			tdb.close();
		Log.d("Lancelot","user try to exit!!!!");
		System.exit(0);
	}
	/**
	 * 控制畫面元件是否鎖住
	 * @param enabled 是否啟用
	 */
	private void setViewComponentEnabled(boolean enabled){
		lv_main.setEnabled(enabled);		
		//iv_bookcase.setEnabled(enabled);
		ib_buy.setEnabled(enabled);
		ib_edit.setEnabled(enabled);
		ib_tools.setEnabled(enabled);
		ib_up_page.setEnabled(enabled);
		ib_del_top.setEnabled(enabled);
		ib_del_bottom.setEnabled(enabled);
		ib_all_select.setEnabled(enabled);
		ib_all_unselect.setEnabled(enabled);	
		ib_realbook.setEnabled(enabled);
		ib_listbook.setEnabled(enabled);
		rl_top.setEnabled(enabled);
		rl_center.setEnabled(enabled);
		rl_edit_mode.setEnabled(enabled);
		rl_main.setEnabled(enabled);
		ib_auth.setEnabled(enabled);
	} 
	private void threadHandleMsg(String msg) {
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		m.setData(data);
		mHandler.sendMessage(m);
	}	
	private void threadTestttMsg2(String msg) {
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		testtt2.sendMessage(m);
	}	
	private Handler testtt2 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			String msg2 = msg.getData().getString("msg");
			if(msg2.equals("")){
				
			}else{
				showAlertMessage(msg2);
			}				
			lv_main.invalidateViews();	
		}
	};	
	private long mLastUpdate = 0;
	private boolean isListBookReady; 

	/**
	 * 按鍵按下事件
	 * @param keyCode keyCode
	 * @param event event
	 * @return 事件是否被處理  
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
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
	/**
	 * 檢查存放路徑
	 */
	public void isInnerSD(){		
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

	@Override
	protected void onResume() {
		super.onResume();
		bOnlyOneStartActivity = true;
		
		if (!ssoReady && twmsso != null && bWaitingResponse)
			twmsso.NotifyOrientationChanged();
		
		if(ssoReady == false)
			return;
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RealBookResume();
	}

	private void RealBookResume() {
		if (DEBUG) Log.e("345", "+RealBook onResume ") ;
		setStyle();
		
		final ListView bcv = (ListView)this.findViewById(R.id.lv_main);
		BookcaseAdapter adapter = (BookcaseAdapter)bcv.getAdapter();
		if (null != adapter) {
			adapter.setDB(tdb);
			adapter.setBookData(downloadBookUrl);
			adapter.notifyDataSetChanged();
			
			if(mIsEdit== false){
				adapter.setEditMode(mIsEdit);
				final ImageView iv = (ImageView)ib_edit;	  		
		  		ib_realbook.setVisibility(View.VISIBLE);
		  		ib_listbook.setVisibility(View.VISIBLE); 
		  		iv.setImageResource(R.drawable.ivi_button09_btn);
			}
		}
	}	
	
	public void RealBookEditModeResume(){
		
		final ListView bcv = (ListView)findViewById(R.id.lv_main);
		BookcaseAdapter adapter = (BookcaseAdapter) bcv.getAdapter();
		adapter.setEditMode(false);
  		final ImageView iv = (ImageView)ib_edit;
  		
  		ib_realbook.setVisibility(View.VISIBLE);
  		ib_listbook.setVisibility(View.VISIBLE); 
  		iv.setImageResource(R.drawable.ivi_button09_btn);
  		
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();
		if (DEBUG) Log.e("345", "+RealBook onPause ") ;
		mdismissDlg();
		mdismissDownloadXMLDlg();
		
		final ListView bcv = (ListView)this.findViewById(R.id.lv_main);
		BookcaseAdapter adapter = (BookcaseAdapter)bcv.getAdapter();
		if ( null != adapter){
			adapter.onPause();
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
    	if(isListBookReady){
 
    		return super.dispatchKeyEvent(event);
    	}else{
       		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
    			if(event.getAction()==KeyEvent.ACTION_UP)
    			{
    				if(this.deviceID == null || this.deviceID.length() == 0){
    					exit();
    					return super.dispatchKeyEvent(event);
    				}
    		   		if (DEBUG) Log.e("flw", "+dispatchKeyEvent ") ;
    				Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.data_prepare),  
    	            Toast.LENGTH_SHORT).show(); 

    				return true;
    			}
    		}    		
     		return true;
    	}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(isListBookReady){
			return super.dispatchTouchEvent(ev);
    	}else{
    		if(ev.getAction() == ev.ACTION_UP)
    		{
    			Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.data_prepare),  
		            Toast.LENGTH_SHORT).show();     		
    		
    		}
    		return true;
    	}		

	}	
	public static TWMDB getTWMDB(){
		return tdb;
	}
	public static void setToken(String token) {
		RealBookcase.token = token;
	}

	public static String getToken() {
		Log.d("Token", "getToken =>"+token);
		return token;
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

	public  Handler getHandler(){
		return mHandler;
	}
	static TWMAuth twmsso = null;
	static boolean bWaitingResponse = false;
	public static boolean AuthSSOLogin(Activity ct,int book,Context ctx,Handler h){
		boolean b = true;
		if(twmsso != null){
			twmsso = null;
		}
		bookcasetype = book;
		mContext = ctx;

		mainHandler = h;
		
		if (networkstatus) {

			String conntype = "0";
			if (gprs)
				conntype = "0";
			else
				conntype = "1";
			twmsso = new TWMAuth(1209600);

			//twmsso.setStagingMode(ct, "Staging");

			Log.d(TAG, "AuthLogin =>" + conntype);
			bWaitingResponse = true;
			twmsso.getLoginData(ct, new MyListener(), conntype);
			Log.d(TAG, "AuthLogin-after =>" + conntype);
		}else{
			if(bookcasetype == realbook){
				mainHandler.sendMessage(mainHandler.obtainMessage(
						REALBOOK_STARTUP, 0, 0)); 
				mainHandler.sendMessage(mainHandler.obtainMessage(
						REALBOOK_RESUME, 0, 0));
				mainHandler.sendMessage(mainHandler.obtainMessage(
						REALBOOK_AUTHSTATUS, 0, 0));
			}
		}
		return b;
	}
	
	public static void AuthSSOLogout(Activity ct){
		if(twmsso == null)
			return;
		
		twmsso.logout(ct,new MyListener());
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		mContext = null;
		
		ssoReady = false;
		//twmsso.getLoginData(null, null);
		twmsso = null;
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// 請 AP 控制好，需要的時候再通知更新登入畫面
		// 若在登入畫面中，且
		// 偵測到 Screen Rotation 時，請呼叫下列 method，重新顯現正確的登入畫面。
		// 若不在登入畫面中，請勿在呼叫此 method
		if (!ssoReady && twmsso != null)
			twmsso.NotifyOrientationChanged();
	}
	public static boolean ssoReady=false;
	public static Context mContext;
//	private Context mContext;
	
	public static int bookcasetype = listbook;
	public static Handler mainHandler;
	public static class MyListener implements TWMAuthListener {
		
		
		public void onComplete(LoginData logindata){
			// 查看登入結果
			// 若 RetCode="0", 則表示用戶登入成功
			// 若 RetCode="3", 則表示輸入參數有誤
			// 若 RetCode="11", 則表示用戶取消登入
			// 詳細規格請參閱 【Client Authentication Android SDK】的規範
			Log.d(TAG,"onComplete=>"+ssoReady+"bookcasetype=>"+bookcasetype);
			String strLoginData = "Login Data Info: (TWM 公司內部 AP 限定版)\n";
//			if (logindata.getRetCode().equals("0")) {
//				setLoginStatus(true, logindata.getSubscrId());
//			}
			strLoginData = strLoginData + ": " + "getExtraInfo() = " + logindata.getExtraInfo("w96j0 284ek 284") + "\n";
			strLoginData = strLoginData + ": " + "getCompanyId() = " + logindata.getCompanyId() + "\n";
			strLoginData = strLoginData + ": " + "getHbgServiceStatus() = " + logindata.getHbgServiceStatus() + "\n";
			strLoginData = strLoginData + ": " + "getHbgUserStatus() = " + logindata.getHbgUserStatus() + "\n";
			strLoginData = strLoginData + ": " + "getIsAutoLogin() = " + logindata.getIsAutoLogin() + "\n";
			strLoginData = strLoginData + ": " + "getOp() = " + logindata.getOp() + "\n";
			strLoginData = strLoginData + ": " + "getPayMethod() = " + logindata.getPayMethod() + "\n";
			strLoginData = strLoginData + ": " + "getRetCode() = " + logindata.getRetCode() + "\n";
			strLoginData = strLoginData + ": " + "getServiceStatus() = " + logindata.getServiceStatus() + "\n";
			strLoginData = strLoginData + ": " + "getStatus() = " + logindata.getStatus() + "\n";
			strLoginData = strLoginData + ": " + "getSubscrId() = " + logindata.getSubscrId() + "\n";
			strLoginData = strLoginData + ": " + "getSubscrNo() = " + logindata.getSubscrNo() + "\n";
			strLoginData = strLoginData + ": " + "getUid() = " + logindata.getUid() + "\n";
			strLoginData = strLoginData + ": " + "getUserKind() = " + logindata.getUserKind() + "\n";
			
//			tvLoginData.setText(strLoginData);
			
			
			RealBookcase.setToken(logindata.getExtraInfo("w96j0 284ek 284"));
			int RetCode = 0;
			RetCode = Integer.valueOf(logindata.getRetCode());
			if(ssoReady == false){
				switch(bookcasetype){
				case listbook:{
					if(RetCode == 0){//succ
//						((TWMBook)mContext).TWMBookStartup(true);
//						((TWMBook)mContext).TWMBookResume();
//						((TWMBook)mContext).AuthBtnStatus(true);
						
					}else{//11:cancel 3:param error 9:other
//						((TWMBook)mContext).TWMBookStartup(false);
//						((TWMBook)mContext).TWMBookResume();
//						((TWMBook)mContext).AuthBtnStatus(false);
					}	
				}break;
				case realbook:{
					if(RetCode == 0){//succ
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_STARTUP, 1, 0)); 
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_RESUME, 0, 0));
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_AUTHSTATUS, 1, 0));
					}else{//11:cancel 3:param error 9:other
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_STARTUP, 0, 0)); 
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_RESUME, 0, 0));
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_AUTHSTATUS, 0, 0));	
					}	
				}break;
				case onlinebook:{
					if(RetCode == 0){//succ
//						((OnlineBook)mContext).OnLinebookStartup();
//						((OnlineBook)mContext).OnLinebookResume();
						((OnlineBook)mContext).OnLinebookStartup();
						((OnlineBook)mContext).OnLinebookResume();
					}else{//error do not thing
					}	
				}break;
				default://
					break;
				}
				ssoReady = true;
			}else{
				switch(bookcasetype){
				case listbook:{
					if(RetCode == 0){//succ
//						((TWMBook)mContext).TWMBookStartup(true);
//						((TWMBook)mContext).TWMBookResume();
//						((TWMBook)mContext).AuthBtnStatus(true);
						
					}else{//11:cancel 3:param error 9:other
//						((TWMBook)mContext).TWMBookStartup(false);
//						((TWMBook)mContext).TWMBookResume();
//						((TWMBook)mContext).AuthBtnStatus(false);
					}					
				}break;
				case realbook:
					if(RetCode == 0){//succ
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_STARTUP, 1, 0)); 
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_RESUME, 0, 0));
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_AUTHSTATUS, 1, 0));
					}else{//11:cancel 3:param error 9:other
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_STARTUP, 0, 0)); 
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_RESUME, 0, 0));
						mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_AUTHSTATUS, 0, 0));	
					}						
					break;
				case onlinebook:{
					if(RetCode == 0){//succ
						((OnlineBook)mContext).OnLinebookStartup();
						((OnlineBook)mContext).OnLinebookResume();
						
					}else{//error do not thing
					}	
				}break;
				default://listbook
					break;
				}
			}			

			bWaitingResponse = false;	
		}

		@Override
		public void onError(int error_type, String error_description, Throwable t) {
			// 呼叫端處理無法連線登入情況
			// 參數說明：
			// error_type: 表明錯誤類型。1:錯誤主因描述由 Throwable t 來描述，error_desctiption 為輔。
			//							2:錯誤主因描述由 error_desctiption 來描述， Throwable t is NULL。
			// 詳細規格請參閱 【Client Authentication Android SDK】的規範
			Log.d(TAG,"onError=>"+error_description);
			switch(bookcasetype){
			case listbook:{
				if (ssoReady == false) {
//					((TWMBook) mContext).TWMBookStartup(false);
//					((TWMBook) mContext).TWMBookResume();
//					ssoReady = true;
				}	
				Log.d(TAG,"onError, error_type="+error_type +", description=["+error_description+"]");
				if (error_type==1) {
					Log.d(TAG,t.getMessage());
//					((TWMBook) mContext).threadHandleMsg(error_description);
				}	
			}break;
			case realbook:{
				if (ssoReady == false){
					mainHandler.sendMessage(mainHandler.obtainMessage(
							REALBOOK_STARTUP, 0, 0)); 
					mainHandler.sendMessage(mainHandler.obtainMessage(
							REALBOOK_RESUME, 0, 0));
					mainHandler.sendMessage(mainHandler.obtainMessage(
							REALBOOK_AUTHSTATUS, 0, 0));	
				}
				if (error_type==1) {
					Log.d(TAG,t.getMessage()+" "+error_description);
					((RealBookcase) mContext).threadHandleMsg(error_description);
				}	
			}break;
			case onlinebook:{	
			}break;
			default:{//listbook
				if (ssoReady == false){
					mainHandler.sendMessage(mainHandler.obtainMessage(
							REALBOOK_STARTUP, 0, 0)); 
					mainHandler.sendMessage(mainHandler.obtainMessage(
							REALBOOK_RESUME, 0, 0));
					mainHandler.sendMessage(mainHandler.obtainMessage(
							REALBOOK_AUTHSTATUS, 0, 0));	
				}
				Log.d(TAG,"onError, error_type="+error_type +", description=["+error_description+"]");
				if (error_type==1) {
					Log.d(TAG,t.getMessage());
					((RealBookcase) mContext).threadHandleMsg(error_description);
				}				
			}break;
				
			}
			
		
			bWaitingResponse = false;
			
		}

		@Override
		public void onLogout(int ret_code) {
			// ret_code: 回傳用戶 logout 結果
			// 0:登出用戶：成功
			// 1:登出用戶：失敗
			// 詳細規格請參閱 【Client Authentication Android SDK】的規範
//	    	if (ret_code==0)
//	    		setLoginStatus(false, null);
//	    	
//	    	tvLoginData.setText("");
		}
		
	}
	public static void setMainHandler(Handler h){
		mainHandler = h;
	}
	//public static Context getContext() {
	private Context getContext() {
		// TODO Auto-generated method stub
		return mContext;
	}	
	
	private void setContext(Context pContext) {
		// TODO Auto-generated method stub
		mContext = pContext;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		if (hasFocus && bWaitingResponse){
			
			if(ssoReady == false){
				switch(bookcasetype){
				case listbook:{
				}break;
				case realbook:{
					mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_STARTUP, 0, 0)); 
					mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_RESUME, 0, 0));
					mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_AUTHSTATUS, 0, 0));	
					
				}break;
				case onlinebook:{
					
				}break;
				default://
					break;
				}
				ssoReady = true;
			}else{
				switch(bookcasetype){
				case listbook:{
		
				}break;
				case realbook:
					mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_STARTUP, 0, 0)); 
					mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_RESUME, 0, 0));
					mainHandler.sendMessage(mainHandler.obtainMessage(
								REALBOOK_AUTHSTATUS, 0, 0));	
					break;
				case onlinebook:{
	
				}break;
				default://listbook
					break;
				}
			}			

			bWaitingResponse = false;	
			
		}
		super.onWindowFocusChanged(hasFocus);
	}
	
}
