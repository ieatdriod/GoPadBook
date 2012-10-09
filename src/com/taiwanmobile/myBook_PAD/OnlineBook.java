package com.taiwanmobile.myBook_PAD;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import tw.com.soyong.AnReader;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gsimedia.gsiebook.RendererActivity;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.taiwanmobile.myBook_PAD.BookList.ViewHolder;
import com.taiwanmobile.myBook_PAD.TWMBook.testStartDownloadInfo;
/**
 * 線上書櫃
 * @author III
 * 
 */
public class OnlineBook extends Activity{
	private static final boolean DEBUG = true ;
	private static final int CONNECT_TIMEOUT = 15000 ; 
	private static final int READ_TIMEOUT = 15000;
	protected static Context mContext;
	//private final static int CWJ_HEAP_SIZE = 20* 1024* 1024 ; 
	private String saveFilelocation = "/sdcard/twmebook/";
	private RelativeLayout rl_online_top,rl_online_main;
	private boolean unOnline ;
	private ListView lv_online_main;	
	private ImageButton ib_online_back;
	private List<String> ebook_isDownload = new ArrayList<String>();    
	
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
	//private TWMDB tdb = new TWMDB(this);
	private TWMOnlineDB todb = null;
	private TWMDB tdb = null;
	private String[] fileType = {".teb",".tvb",".tpb",};
	private Cursor cursorDBData,cursorDBData_2,cursorDBDataThread;
	private myBookList mybl;
	private ViewHolder[] mainRow = null;	
	private List<DownloadPbar> dp = null;
	//private List<Integer> dp_num = null;
	private SharedPreferences settings;	
	private String downloadBookUrl = "http://124.29.140.83/DeliverWeb/downloadEBook?deliver_id=";
	private String ebook_error;
	private String ebook_description;
	private StatFs stat;    
	private long freeSize;
	private long downloadHeapSize = 0;
	private List<String> downloadID = new ArrayList<String>();
	
	private int nowDownloadNum = 0;
	private final static int MAX_DOWNLOAD_NUM = 5;//上限 5   
	
	private ProgressDialog pDialog;
	//private List<Integer> pbarIndexList = new ArrayList<Integer>();
	private List<Integer> pbarValueList = new ArrayList<Integer>();
	private List<String> pbarNowStatusList = new ArrayList<String>();
	private boolean isBack = false ;
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
  	  		if ( null != tdb){
  	  			tdb.close();
  	  			tdb = null;
  	  		}
  	  		
  	  		if ( null != todb ){
  	  			todb.close();
  	  			todb = null;
  	  		}
  	  		
  	  		finish();
		}		
		return super.onKeyDown(keyCode, event);
	}
	
	//private ArrayList<String> mDownloadIDs = new ArrayList<String>();
	private HashMap<Integer, Long> mThreadMap = new HashMap<Integer, Long>() ;
	
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);    
		setContentView(R.layout.iii_online_main);
		mContext = this;
		OnLinebookStartup();	
		//TWMBook.AuthSSOLogin(OnlineBook.this,TWMBook.onlinebook,OnlineBook.mContext);
	}

	public void OnLinebookStartup() {
		todb = new TWMOnlineDB(this);	
		todb.openDB();
		settings = getSharedPreferences("setting_Preference", 0);
		tdb = new TWMDB(this);
		isInnerSD();
		setViewComponent();
		setStyle();
		setListener();
		unOnline = true;
		pDialog = ProgressDialog.show(OnlineBook.this, "", getResources().getString(R.string.iii_download_online_list));
		pDialog.setCancelable(false);
		delAllData();
		downloadXMLThread();
	}
	private void downloadXMLThread(){
		new Thread(){
			public void run(){
				try{					
					downloadBookUrl = getResources().getString(R.string.iii_twm_download_ebook);
					downloadXML(getResources().getString(R.string.iii_twm_download_all_list));
					if(unOnline){
						insertDB();
						updateFileIsExists();
						initList();	
						Message m = new Message();
						setInitList.sendMessage(m);						
						upImage();
					}
				}catch(Exception e){
					threadTestttMsg(getResources().getString(R.string.iii_OnlineCantGetBooklist),FROM_downloadXML,null);	
					e.printStackTrace();
				}
			}
		}.start();	
	}
	
	private void threadTestttMsg(String msg,int aFrom,Object aInfo) {
		if(OnlineBook.this!=null){
			Message m = new Message();
			Bundle data = m.getData();
			data.putString("msg", msg);
			data.putInt(MSGFROM, aFrom);
			data.putParcelable(MSGDOWNLOAD, (testStartDownloadInfo)aInfo);
			testtt.sendMessage(m);
		}
	}
	public void threadHandleMsg(String msg) {
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		m.setData(data);
		mHandler.sendMessage(m);
	}	
	/**
	 * 設定畫面主題
	 */
	public void setStyle(){
		if("".equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_online_top.setBackgroundResource(R.drawable.wood_ivi_bar03);
			rl_online_main.setBackgroundResource(R.drawable.wood_ivi_bg03);
			ib_online_back.setBackgroundResource(R.drawable.wood_ivi_button09);
		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[0].equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_online_top.setBackgroundResource(R.drawable.wood_ivi_bar03);
			rl_online_main.setBackgroundResource(R.drawable.wood_ivi_bg03);
			ib_online_back.setBackgroundResource(R.drawable.wood_ivi_button09);
		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[1].equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_online_top.setBackgroundResource(R.drawable.technology_ivi_bar03);
			rl_online_main.setBackgroundResource(R.drawable.technology_ivi_bg03);
			ib_online_back.setBackgroundResource(R.drawable.technology_ivi_button09);
		}else if(getResources().getStringArray(R.array.iii_bookcase_background_style_value)[2].equals(settings.getString("setting_bookcase_background_style_value", ""))){
			rl_online_top.setBackgroundResource(R.drawable.romantic_ivi_bar03);
			rl_online_main.setBackgroundResource(R.drawable.romantic_ivi_bg03);
			ib_online_back.setBackgroundResource(R.drawable.romantic_ivi_button09);
		}
	}
	/**
	 * 更新圖片 如果圖片不存在或下載失敗 就重新下載
	 */
	private void upImage(){				
		Log.e("", "download img");
		cursorDBDataThread = todb.selectOrderBy("isdownloadbook ASC , buyTime DESC");							 
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
								todb.updateByDeliveryId(cursorDBDataThread.getString(5).toString() , "coverpath" , saveFilelocation+cover_2);	
					   			mybl.setEbook_cover(cursorDBDataThread.getString(5).toString(), saveFilelocation+cover_2);
							}							
						}
						cursorDBDataThread.moveToNext();
					}
					//cursorDBDataThread.close();
				}catch(Exception e){
					//showAlertMessage(e.toString());
					e.printStackTrace();
				}
			}
		}.start();			   		
	}
	/**
	 * 下載圖片
	 * @param url 網址
	 * @param dID 書本在資料庫的id
	 * @param org 無作用
	 */
    private void downloadImage(String url , String dID, String org){
   		try {
   			//cursorDBDataTemp = tdb.select("_id = "+id);
   			//cursorDBDataTemp.moveToFirst();
   			URL myURL = new URL( getResources().getString(R.string.iii_cms_cover) + url );   			
   			int tag = myURL.getFile().toString().lastIndexOf("/");
   			InputStream conn = myURL.openStream();
   			String fileName = myURL.getFile().toString().substring(tag + 1, myURL.getFile().toString().length());
   			FileOutputStream fos = new FileOutputStream(saveFilelocation+fileName);
   			byte[] buf = new byte[1024];
   			while (true) {
   				int bytesRead = conn.read(buf);
   				if (bytesRead == -1)
   					break;
   				fos.write(buf, 0, bytesRead);
   			}
   			conn.close();
   			fos.close();
   			todb.updateByDeliveryId(dID , "coverpath" , saveFilelocation+fileName);	
   			tdb.updateByDeliveryId(dID , "coverpath" , saveFilelocation+fileName);	
   			mybl.setEbook_cover(dID, saveFilelocation+fileName);
   			//reListImage(Integer.valueOf(id));	
   		} catch(Exception e) {
   			e.printStackTrace();
   			//todb.updateByDeliveryId(dID , "coverpath" , org);	
   			//mybl.setEbook_cover(dID, org);
   		}
    }
	
	private Handler handlerDownloadImage = new Handler(){
		@Override
		public void handleMessage(Message msg) {
		
			lv_online_main.invalidateViews();
		}		
	};	
	/**
	 * 建構進入畫面
	 */
	private void initList() {
		cursorDBData = todb.selectOrderBy("isdownloadbook ASC , buyTime DESC");
		mainRow = new ViewHolder[cursorDBData.getCount()];	
		cursorDBData.moveToFirst();
		ebook_isDownload.clear();
		for(int i=0;i < cursorDBData.getCount();i++){
			ebook_isDownload.add(cursorDBData.getString(22));
			cursorDBData.moveToNext();
			mainRow[i] = null;
		}
		mybl = new myBookList(this,cursorDBData,false);
		
	    dp = new ArrayList<DownloadPbar>();
	    //dp_num = new ArrayList<Integer>(); 
	    //todb.close();
	    //cursorDBData.close();	
	}
	
	private Handler setInitList = new Handler(){
		@Override
		public void handleMessage(Message msg) {
		
			lv_online_main.setAdapter(mybl);
			pDialog.dismiss();
		}		
	};	
	/**
	 * 刪除資料庫資料
	 */
	private void delAllData(){		
		todb.deleteAll();
/*		cursorDBData = todb.select();
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			todb.delete(cursorDBData.getInt(0));
			cursorDBData.moveToNext();
		}	*/
		//cursorDBData.close();	
	}
	/**
	 * 將全部的書本資料加入手機資料庫中
	 */
	private void insertDB() {
		if(ebook_error!=null){
			if(ebook_error.equals("0")){
				for(int i=0;i<ebook_title.size();i++){
					String tempTypeCover ,tempType;
					//mebook
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
					//epub
					else{
						tempTypeCover = "ivi_nonepict01";
						tempType = fileType[0];
					}
					//if( !ebook_bodytype_code.get(i).toString().equals("202") && !ebook_bodytype_code.get(i).toString().equals("212"))
						todb.insert(ebook_title.get(i).toString(),ebook_type.get(i).toString(),ebook_category.get(i).toString(),
								ebook_cover.get(i).toString(),ebook_deliveryID.get(i).toString(),"0",
								String.valueOf(System.currentTimeMillis()),"0",tempTypeCover,
								ebook_purchased_at.get(i).toString(),ebook_publisher.get(i).toString(),ebook_authors.get(i).toString(),
								ebook_trial.get(i).toString(),"0",ebook_contentID.get(i).toString(),
								ebook_update_date.get(i).toString(),ebook_vertical.get(i).toString(),ebook_trial_due_date.get(i).toString(),
								saveFilelocation + ebook_deliveryID.get(i).toString()+tempType,"0",tempType,
								"0");
				}
				Log.d("ebook_title.size()", Integer.toString(ebook_title.size()));
				
			}else{
				threadTestttMsg(ebook_error,FROM_insertDB,null);	
				//showAlertMessage(ebook_error);
			}
		}else{
			//showAlertMessage(getResources().getString(R.string.iii_server_no_response));
			threadTestttMsg(getResources().getString(R.string.iii_server_no_response),FROM_insertDB,null);
		}
		//showAlertMessage("ebook_title.size()"+String.valueOf(ebook_title.size()));
	}
	/**
	 * 暫停時處理事項 將有下載的書全部暫停
	 */
	public void onPause(){
		super.onPause();
		OnLinebookOnPause();
	}

	private void OnLinebookOnPause() {
		for ( DownloadPbar dpbar : dp ){
			dpbar.setCancel(true);
		}
/* 		for(int i=0;i<dp.size();i++){
			dp.get(i).setCancel(true);
		} */
		resetNowDownloadNum();
	    dp = new ArrayList<DownloadPbar>();
	    //dp_num = new ArrayList<Integer>(); 
	}
	/**
	 * 回覆時處理事項 更新列表
	 */
	public void onResume(){
		super.onResume();
		OnLinebookResume();
	}

	public void OnLinebookResume() {
		initList();
		lv_online_main.setAdapter(mybl);
	}
	/**
	 * 秀出訊息至畫面
	 * @param message 訊息
	 */
	private void showAlertMessage(String message){
		try{
			new AlertDialog.Builder(OnlineBook.this)
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
	 * 檢查檔案是否存在 若有變動則更新資料庫
	 */
	private void updateFileIsExists() {
		cursorDBData = todb.select();
		cursorDBData_2 = tdb.select();
		
		cursorDBData.moveToFirst();
		cursorDBData_2.moveToFirst();
		//File a ;
		downloadHeapSize = 0;
		downloadID.clear();    
		for(int i=0;i<cursorDBData.getCount();i++){
			for(int j=0;j<cursorDBData_2.getCount();j++){
				if(cursorDBData.getString(5).equals(cursorDBData_2.getString(5))){
					String temp = cursorDBData_2.getString(8);
					String temp2 = temp;
					if(!temp.equals("1")){
						if(!temp.equals("0")){
							temp = "0";
//							downloadHeapSize = downloadHeapSize + cursorDBData_2.getLong(20);
//							downloadID.add(cursorDBData_2.getString(5));
						}
					}
					todb.update(cursorDBData.getInt(0), cursorDBData_2.getString(1), cursorDBData_2.getString(2),cursorDBData_2.getString(3),
							cursorDBData_2.getString(4), cursorDBData_2.getString(5),cursorDBData_2.getString(6),
							cursorDBData_2.getString(7), temp,cursorDBData_2.getString(9),
							cursorDBData_2.getString(10), cursorDBData_2.getString(11),cursorDBData_2.getString(12),
							cursorDBData_2.getString(13), cursorDBData_2.getString(14),cursorDBData_2.getString(15),
							cursorDBData_2.getString(16), cursorDBData_2.getString(17),cursorDBData_2.getString(18),
							cursorDBData_2.getString(19), cursorDBData_2.getString(20),cursorDBData_2.getString(21),
							temp2);
					break;
				}
				cursorDBData_2.moveToNext();
			}
/*			a = new File(cursorDBData.getString(19));
			Log.e("cursorDBData.getString(19)", cursorDBData.getString(19));
			if (  a.exists() ){
				todb.update(cursorDBData.getInt(0), "isDownloadBook", "1");
				Log.e("cursorDBData.getString(19)", "==========="+cursorDBData.getString(19));
			}*/
			cursorDBData_2.moveToFirst();
			cursorDBData.moveToNext();
		}
		//todb.close();
		//tdb.close();
		//cursorDBData.close();
		//cursorDBData_2.close();
	}
	/**
	 * 下載書單並解析
	 * @param xml 書單
	 */
	private void downloadXML(String string) {
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
        	String deviceID ="";
        	try{
        		deviceID = GSiMediaRegisterProcess.getID(this.getApplicationContext());
    		}catch(Throwable e){};
        	if (deviceID == null) deviceID = "";
        	
   	 		URL myURL = new URL(string+"?device_id="+deviceID);
   	 		//URL myURL = new URL("http://61.64.54.35/testcode/rf.asp");
   	 		String urlParameters ="&token="+RealBookcase.getToken();
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
						R.string.iii_NetworkNotConnMessage),FROM_downloadXML,null);
				return ;
			}

   	 		
   	 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   	        DocumentBuilder db = dbf.newDocumentBuilder();
   	        conn.setConnectTimeout(10000);
   	        conn.setReadTimeout(10000);
   	        InputStream is = conn.getInputStream();
   	        //InputStream is = new FileInputStream (saveFilelocation+"addtextXml.xml");
   	        
//   	        doc = db.parse(is);
//   	        //doc.getDocumentElement().normalize();
//   	        NodeList nError=doc.getElementsByTagName("error");
//   	        //NodeList nUpdate_at=doc.getElementsByTagName("update_at");
//   	        //NodeList nTotal_entries=doc.getElementsByTagName("total_entries");
//   	        ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        doc = db.parse(is);

			NodeList nError = doc.getElementsByTagName("status");
			String error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();

			NodeList nDesc = doc.getElementsByTagName("description");
			String description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();
			
			NodeList nData = doc.getElementsByTagName("data");
			String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
			InputStream dataStream = covertStringToStream(RevData);
			doc = db.parse(dataStream);   	        
   	        //ebook_update_at = nUpdate_at.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        //ebook_total_entries = Integer.valueOf(nTotal_entries.item(0).getChildNodes().item(0).getNodeValue().toString());
   	        if(error.equals("1")){
   	        	nError=doc.getElementsByTagName("error");
   	        	ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        	
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
   	   	   			
   	   	   			//Log.e("1+++++++++", String.valueOf(nVertical.item(i).getChildNodes().getLength()));
   	   	   			ebook_vertical.add(nVertical.item(i).getChildNodes().item(0).getNodeValue());   
   	   	   			
   	   	   			if(nTrial_due_date.item(i).getChildNodes().getLength()>0){
   	   	   				ebook_trial_due_date.add(nTrial_due_date.item(i).getChildNodes().item(0).getNodeValue());
   	   	   			}else{
   	   	   				ebook_trial_due_date.add("");
   	   	   			}
   	   	   			
   	   	   			
   	   	   			int j = 0;
   	   	   			String temp = "";
   	   	   			do{
   	   	   				temp = temp+ nEbook_category.item(i).getChildNodes().item(j).getChildNodes().item(0).getNodeValue()+"|";
   	   	   				j= j + 2;
   	   	   			}while(j < nEbook_category.item(i).getChildNodes().getLength());   	
   	   	   			ebook_category.add(temp);
   	   	   		}
   	        }else if(error.equals("4")){
   	        	if(deviceID.length()==0){
   	        		description = getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG);
   	        	}else{
   	        	nError=doc.getElementsByTagName("error");
   	        	description = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
   	        	}
   	        	unOnline = false;
   	        	threadTestttMsg(description,FROM_downloadXML,null);
   	        }else{
   	        	unOnline = false;
//   	        	if (null == ebook_error){
//   	        		ebook_error = getResources().getString(R.string.token_expire);
//   	        	}
//   	        	threadTestttMsg(ebook_error,FROM_downloadXML,null);
   	        	threadTestttMsg(description,FROM_downloadXML,null);
   	        }
   	 	}catch(Exception e){
   	 		unOnline = false;
   	 		threadTestttMsg(getResources().getString(R.string.iii_get_book_list_error),FROM_downloadXML,null);
   	 		e.printStackTrace();
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

	/**
	 * 設定畫面事件
	 */
	private void setListener() {

		ib_online_back.setOnClickListener(new ImageButton.OnClickListener(){
      	  	public void onClick(View v){
      	  		isBack = true;
      	  		if ( null != tdb){
      	  			tdb.close();
      	  			tdb = null;
      	  		}
      	  		
      	  		if ( null != todb ){
      	  			todb.close();
      	  			todb = null;
      	  		}
      	  		
      	  		finish();
      	  	}	
        });  	
		lv_online_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//cursorDBData = todb.selectOrderBy("isdownloadbook ASC , buyTime DESC");	
				//cursorDBData.moveToFirst();
				
				/*for(int i=0;i<arg2;i++){
					cursorDBData.moveToNext(); 
				}*/
				cursorDBData.moveToPosition(arg2);
				switch(Integer.valueOf(ebook_isDownload.get(arg2))) { 
	            	case 0: 
	            		String url = getDownloadBookURL(cursorDBData.getString(5));
//	            		if(url == null){
//	            			threadTestttMsg(getApplicationContext().getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG),0,null);
//	            		}else{
	            			testStartDownload(arg2,url,saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(21),0);
//	            		}
	            		//lv_main.invalidateViews();
	            		break; 
	            	case 1: 
	            		lv_online_main.setEnabled(false);
	            		lv_online_main.postDelayed(new Runnable(){ 
							@Override 
							public void run() { 
								lv_online_main.setEnabled(true);
							} 
						},400);
	            		tdb.updateByDeliveryId(cursorDBData.getString(5), "lastreadtime", String.valueOf(System.currentTimeMillis()));
	            		tdb.updateByDeliveryId(cursorDBData.getString(5) , "isread" , "1" );	

	            		openBook(cursorDBData.getString(2),cursorDBData.getString(19),
	            				OnlineBook.this.getFilesDir().toString(),cursorDBData.getString(13),cursorDBData.getString(9),
	            				settings.getBoolean("setting_auto_sync_last_read_page_value", true),cursorDBData.getString(15),
	            				cursorDBData.getString(1),cursorDBData.getString(12),cursorDBData.getString(11),cursorDBData.getString(3),cursorDBData.getString(17),cursorDBData.getString(21));
	            		
	            		break; 
	            	case 2: 	            		
	            		testStartDownload(arg2,getDownloadBookURL(cursorDBData.getString(5)),saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(21),2);
	            		//lv_main.invalidateViews();
	            		break;	
	            	case 3: 	            		
	            		testStartDownload(arg2,getDownloadBookURL(cursorDBData.getString(5)),saveFilelocation,cursorDBData.getString(5),cursorDBData.getInt(0),cursorDBData.getString(21),3);
	            		//lv_main.invalidateViews();
	            		break;		            		
				}	
				//tdb.close();
				//cursorDBData.close();
			}
		});		
	}
	
	public String getDownloadBookURL(String deliverID){
    	String deviceID ="";
    	try{
    		deviceID = GSiMediaRegisterProcess.getID(this.getApplicationContext());
		}catch(Throwable e){};
    	if (deviceID == null) deviceID = "";
    	
    	if(deviceID.length() == 0){
    		return null;
    	}
		return downloadBookUrl+deliverID+"&device_id="+deviceID+"&token="+RealBookcase.getToken()+"&pointer=";
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
	private void testStartDownload(final testStartDownloadInfo aInfo){
		this.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				testStartDownload(aInfo.arg2,aInfo.sf,aInfo.loc,aInfo.tfp,aInfo.id,aInfo.type,aInfo.mod);
			}
			
		});
	}
	private void testStartDownload(final int arg2,String sf,String loc,String tfp,int id,String type,int mod){	
		try{
			if(getNowDownloadNum() < MAX_DOWNLOAD_NUM){
				//nowDownloadNum = nowDownloadNum + 1;
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
				todb.update(id , "bookOtherInfo" , "2" );
				ebook_isDownload.set(arg2, "2");
				//mainRow[arg2].nowStatus = "2";
				pbarNowStatusList.set(arg2, "2");	
				Log.v("arg2","   "+String.valueOf(arg2)+"  ");
				
				vh.pbar.setVisibility(View.VISIBLE);
				vh.cancel.setVisibility(View.VISIBLE);
			
				mybl.setEebook_isdownloadbook(arg2,"2");
				//lv_online_main.invalidateViews();
			
			
				DownloadPbar subDp;
				
		 		if(type.equals(fileType[0])){
		 			subDp = new DownloadPbar(sf,loc,tfp+fileType[0]+".tmp",id,mod);
		 		
		 		}else if(type.equals(fileType[1])){
		 			subDp = new DownloadPbar(sf,loc,tfp+fileType[1]+".tmp",id,mod);
		 		}else{
		 			subDp = new DownloadPbar(sf,loc,tfp+fileType[2]+".tmp",id,mod);
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



 		//dp.add(subDp);
 		//dp_num.add(arg2);
 		//dp.get(dp.size()-1).setArg(arg2);
 		//dp.get(dp.size()-1).start();
		//subDp.setArg(arg2);
		//subDp.start();
 		//lv_online_main.invalidateViews();
	}
	
	private long mLastUpdate = 0; 
	/**
	 * 更新下載進度條
	 */
	private Handler testHandlerDownloadBook = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//Log.e("", "vvvvvvvvvvvvvvv");
			int percent = msg.getData().getInt("percent");
			int position = msg.getData().getInt("Position");
			String dID = msg.getData().getString("dID");
			
			if(mainRow[position]!=null){
				pbarValueList.set(position, percent);
				//mainRow[position].pbar.setProgress(percent);
				long cur = System.currentTimeMillis();
				if (percent == 0 || cur - mLastUpdate >= 1500) {
				//if ( cur - mLastUpdate >= 1500){
					mLastUpdate = cur;
					lv_online_main.invalidateViews();
					//mainRow[position].pbar.invalidate();
	   			}
			
				if(percent==100){

					pbarNowStatusList.set(position, "1");
					mybl.setEebook_isdownloadbook(position, "1");
					ebook_isDownload.set(position, "1");
					lv_online_main.invalidateViews();
					Cursor cursorDBTest = tdb.select("deliveryID = '"+dID+"'");
					if(cursorDBTest.getCount()>0){
						cursorDBTest.moveToFirst();
						todb.updateByDeliveryId( dID , "isDownloadBook","1");
						tdb.updateByDeliveryId( dID , "isDownloadBook","1");
					}
					
/* 					mainRow[position].pbar.setVisibility(View.GONE);
					mainRow[position].cancel.setVisibility(View.GONE);
					mainRow[position].icon.setAlpha(255);		
					mainRow[position].text.setTextColor(Color.BLACK);
					mainRow[position].text.setBackgroundColor(Color.alpha(255));		
					//mainRow[position].nowStatus = "1";
					pbarNowStatusList.set(position, "1");
					//Log.v("  XXXXXXXXXXXXXXX  ","  0  ");
		   			mybl.setEebook_isdownloadbook(position,"1");
		   			ebook_isDownload.set(position, "1");
		   			lv_online_main.invalidateViews(); */
		   			
		   			
/* 					Cursor cursorDBTest = tdb.select("deliveryID = '"+dID+"'");
					if(cursorDBTest.getCount()>0){
						//cursorDBTest.moveToFirst();
						todb.updateByDeliveryId( dID , "isDownloadBook","1");
						tdb.updateByDeliveryId( dID , "isDownloadBook","1");
					} */
	/*				cursorDBData = todb.selectOrderBy("isdownloadbook ASC , buyTime DESC");
					mainRow = new ViewHolder[cursorDBData.getCount()];	
					for(int i=0;i < cursorDBData.getCount();i++){
						mainRow[i] = null;
					}
					mybl = new myBookList(OnlineBook.this,cursorDBData,false);
					lv_online_main.setAdapter(mybl);*/				
				}	
			}
		}
	};	
	
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
	private void openBook(String type,String coverPath,String p1,String p2,String p3,Boolean p4,String p5,String p6,String p7,String p8,String p9,String p10,String p11) {
		//this.showAlertMessage("開啟書本-- 路徑為 "+coverpath);	
		Intent it = null;
		if(type.equals(getResources().getString(R.string.iii_mebook))){
 			it= new Intent(OnlineBook.this,AnReader.class);
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
			
			it= new Intent(OnlineBook.this,RendererActivity.class);
 			it.setData(Uri.parse("pdf://"+coverPath));	
 		}else{
 			it= new Intent(OnlineBook.this,Reader.class);
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
	 * 設定畫面UI 元件
	 */
	private void setViewComponent() {
		rl_online_main = (RelativeLayout)findViewById(R.id.rl_online_main);
		rl_online_top = (RelativeLayout)findViewById(R.id.rl_online_top);
		
		lv_online_main = (ListView)findViewById(R.id.lv_online_main);
		
		ib_online_back = (ImageButton)findViewById(R.id.ib_online_back);
		
		ib_online_back.bringToFront();
		
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
	/**
	 * 重新計算剩餘空間
	 */
	private void calFreeSize() {
		int size = stat.getBlockSize();
		int num = stat.getAvailableBlocks();
		freeSize = (long)num * size ;
	}

	private static final int FROM_downloadXML = 0;
	private static final int FROM_DdownloadPbar = 1;
	private static final int FROM_insertDB = 2;
	private static final String MSGFROM = "msgfrom";
	private static final String MSGDOWNLOAD = "msgdownload";
	private Handler testtt = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(!isBack){
				final String msg2 = msg.getData().getString("msg");
				final int aFrom = msg.getData().getInt(MSGFROM,-1);
				final testStartDownloadInfo aInfo = msg.getData().getParcelable(MSGDOWNLOAD);
				pDialog.dismiss();
				//initList();
				//lv_online_main.setAdapter(mybl);
				if(msg2.equals("")){				
					
				}else if(msg2.equals("非台灣大哥大行動網路連線或使用錯誤APN")
						||msg2.equals("目前僅提供離線閱讀!請使用台灣大哥大網!")){
					LoginDialogController aLogin = new LoginDialogController();
					aLogin.ShowLoginDialog(OnlineBook.this, new LoginDialogObserver(){

						@Override
						public void LoginComplete(LoginDialogController aController,Object aUserData,
								int err) {
							int from = (Integer)aUserData;
							switch(err){
							case LoginDialogObserver.KErrNone:
								aController.DismissLoginDialog();
								if(from== FROM_downloadXML){
									unOnline = true;
								downloadXMLThread();
								}else{
									// re-download book
									if(aInfo!=null){
										testStartDownload(aInfo);
									}else{
										//!!should not come here
										Log.e("TWM","no download info!");
									}
								}
								break;
							case LoginDialogObserver.KErrCancel:
								showAlertMessage(msg2);
								break;
							}
						}
						
					},new Integer(aFrom));
				}else{
					showAlertMessage(msg2);
				}				
			}						
		}
	};
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Bundle bd = msg.getData();
			String desc = bd.getString("msg");
			new AlertDialog.Builder(OnlineBook.this)
			.setTitle(R.string.msgbox_expire_title)
			.setMessage(desc)//R.string.msgbox_expire_content)
			.setPositiveButton(R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//retrieve token
							//startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(url)) );
//				  	  		if ( null != tdb){
//				  	  			tdb.close();
//				  	  			tdb = null;
//				  	  		}
//				  	  		
//				  	  		if ( null != todb ){
//				  	  			todb.close();
//				  	  			todb = null;
//				  	  		}
//				  	  		
//				  	  		finish();
							if( null != pDialog && pDialog.isShowing()){
								pDialog.dismiss();
								pDialog = null;
							}
							RealBookcase.AuthSSOLogin(OnlineBook.this,RealBookcase.onlinebook,OnlineBook.mContext,null);

							
						}
					}
			)
			.setNegativeButton(R.string.iii_showAM_cancel,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//cancel
							//finish();
							if( null != pDialog && pDialog.isShowing()){
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
		private Long serverFileSize ;
		private volatile int cancel = 99 ;
		private String errorMsg = "";
		private int model;
		private boolean isdownload = true;
		RandomAccessFile oSavedFile = null;
		private long mTimeStamp = 0 ;
		private String dID;
		
		public DownloadPbar(String sf,String loc,String tfp,int i,int mod){
			super(sf,loc,tfp,i);	
			model = mod;
			lv_online_main.invalidateViews();
		}
		
		/**
		 * 設定是清單的第幾本產品
		 * @param arg 第幾項
		 */
		public void setArg(int arg){
			arg2 = arg;
		}
		/**
		 * for restart download
		 */
		private testStartDownloadInfo iInfo = null;
		public void setInfo(testStartDownloadInfo aInfo){
			iInfo = aInfo;
		}

		/**
		 * 取得本執行緒在清單中的位置
		 * @return 本執行緒在清單中的位置
		 */
		public int getArg(){
			return arg2;
		}
		
		public void setTimeStamp(long timeStamp){
			mTimeStamp = timeStamp ;
		}
		/**
		 * 取得下載百分比
		 * @return 百分比  
		 */
		public int getPercent(){
			//return (int)(new File(location+tempFilePath).length()/serverFileSize * 100);
			if ( file != null ){
				return (int) (file.length() * 100 / serverFileSize);
			}
			return 0 ;
		}		
		
		public String gettest(){
			return String.valueOf((file.length()-block) + " - "+getServerFileSize());
		}		
		//private static final int RETRY_TIMEOUT = 1000 ;
		@SuppressWarnings("finally")
		public synchronized void run(){		
			int retry = 3;
			//int tryOpen = 5 ;
			URL myURL = null;
			InputStream is = null;
			HttpURLConnection conn = null;
			byte[] buff = new byte[64];
			String a = null;
			int currentRead = 0;
			cancel = 0 ;
			while ( 0 == cancel && retry > 0) {
				
				try {
					
					
					file = new File(location+tempFilePath);
	/*				URL myURL = new URL(serverFile);
					//System.out.println(serverFile);
					//serverFile
					InputStream conn = myURL.openStream();
					
					RandomAccessFile oSavedFile = new RandomAccessFile(location+tempFilePath,"rw");
					oSavedFile.seek(0);*/
					oSavedFile = new RandomAccessFile(file,"rw");
					
					if(model==0){
						file.delete();
						oSavedFile = new RandomAccessFile(file,"rw");
						oSavedFile.seek(0);					
						serverFile = serverFile + "0";
						Log.e("model==0","model==0");
					}else{
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
						myURL = new URL(serverFile);

						//myURL = new URL("http://61.64.54.35/testcode/rf.asp"); 
						String urlParameters = "&token="+RealBookcase.getToken(); 
						conn = (HttpURLConnection) myURL.openConnection();
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
							retry -- ;
//							//wait(RETRY_TIMEOUT);
							continue;
						}
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						//InputStream is = bbbconn.getInputStream();
//						bbbconn = (HttpURLConnection)myURL.openConnection();
//						bbbconn.setConnectTimeout (CONNECT_TIMEOUT) ;
//						bbbconn.setReadTimeout(READ_TIMEOUT);
//						bbbconn.setDoInput(true);
//						bbbconn.setDoOutput(true);
//						bbbconn.setRequestMethod("GET");
//						bbbconn.setUseCaches(false);					
//						
//						if (DEBUG) Log.e("vic" , "connect +++");
//						bbbconn.connect();
//						if (DEBUG) Log.e("vic" , "connect ---");
//
//						if (0 != cancel ){
//							continue;
//						}
//						
//						int response = bbbconn.getResponseCode();
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

						is = conn.getInputStream();
						currentRead = is.read(buff);
/* 						if ( currentRead != buff.length ){
							retry -- ;
							//wait(RETRY_TIMEOUT);
							continue ;
						} */
						
						a = new String(buff).trim();

						if (true == a.startsWith("00")) {
							isConnected = true ;
							break;
						} else if (true == a.startsWith("01")) {
							getServerErrMsg(a);
							threadTestttMsg(errorMsg,OnlineBook.FROM_DdownloadPbar,this.iInfo);							
							threadErrorDownloadBookMsg(arg2);
							if ( 0 == cancel ){
								setNowDownloadNum(false);
							}
							doCancel(1);	
							return;

						} else if (true == a.startsWith("02")){//02 
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
					}//while
					
					if (0 != cancel || retry <= 0 || false == isConnected){
						
						if ( 0 == cancel ){
							setNowDownloadNum(false);
							throwErrorMsg(getResources().getString(R.string.iii_server_SocketTimeout));	
						}
						doCancel(2);
						return ;
					}
					getServerErrMsg(a);
					//errorMsg =  a.substring(a.lastIndexOf(",") + 1,a.length()).trim();
					serverFileSize = Long.valueOf(a.substring(a.indexOf(",")+1, a.lastIndexOf(",")));		
					
					dID = tempFilePath.substring(0, tempFilePath.indexOf("."));
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
						threadTestttMsg(getResources().getString(R.string.iii_download_no_space),OnlineBook.FROM_DdownloadPbar,this.iInfo);
						file.delete();
						isdownload = false;
						todb.update(id , "bookOtherInfo" , "0" );
						mybl.setEebook_isdownloadbook(arg2, "0");
						
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

						

						Cursor cursorDBTest = tdb.select("deliveryID = '"+tempFilePath.substring(0, tempFilePath.indexOf("."))+"'");
						
						if(cursorDBTest.getCount()>0){
							//cursorDBTest.moveToFirst();
							todb.update(id , "isDownloadBook","0");
							todb.update(id , "bookSize" , String.valueOf(serverFileSize) );
							tdb.update(id , "isDownloadBook","3");
							tdb.update(id , "bookSize" , String.valueOf(serverFileSize) );
							tdb.update(id , "bookPath" , location+tempFilePath.substring(0, tempFilePath.lastIndexOf(".")) );	
						}else{
							cursorDBTest = todb.select("deliveryID = '"+tempFilePath.substring(0, tempFilePath.indexOf("."))+"'");
							cursorDBTest.moveToFirst();
							
							tdb.insert(cursorDBTest.getString(1), cursorDBTest.getString(2),cursorDBTest.getString(3),
									cursorDBTest.getString(4), cursorDBTest.getString(5),cursorDBTest.getString(6),
									cursorDBTest.getString(7), cursorDBTest.getString(8),cursorDBTest.getString(9),
									cursorDBTest.getString(10), cursorDBTest.getString(11),cursorDBTest.getString(12),
									cursorDBTest.getString(13), cursorDBTest.getString(14),cursorDBTest.getString(15),
									cursorDBTest.getString(16), cursorDBTest.getString(17),cursorDBTest.getString(18),
									cursorDBTest.getString(19), cursorDBTest.getString(20),cursorDBTest.getString(21),
									cursorDBTest.getString(22));
						}			
						//cursorDBTest.close();
						//System.out.println(serverFileSize);
						int per = 0;
						//int percent = 0;
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
									data.putString("dID", dID);
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
							//nowDownloadNum  = nowDownloadNum - 1;
							//setNowDownloadNum(false);
							if(getPercent()==100){
								todb.update(id , "isdownloadbook" , "1" );
								todb.update(id , "bookOtherInfo" , "1" );
								tdb.update(id , "isdownloadbook" , "1" );
								ebook_isDownload.set(arg2, "1");
								mybl.setEebook_isdownloadbook(arg2,"1");
								file.renameTo(new File(location+tempFilePath.substring(0, tempFilePath.lastIndexOf("."))));
								downloadHeapSize = downloadHeapSize - serverFileSize;
								calFreeSize(); 
								for(int i=0;i<downloadID.size();i++){
									if(downloadID.get(i).equals(dID)){
										downloadID.remove(i);
									}
								}	
							}else{
								todb.update(id , "bookOtherInfo" , "3" );
								mybl.setEebook_isdownloadbook(arg2,"0");
								ebook_isDownload.set(arg2, "3");
							}	
							//tdb.close();
							//todb.close();
							retry = 0 ;
							setNowDownloadNum(false);
							if(DEBUG) Log.d("vic", "download finish :"+ dID);
							return ;
						}
					} catch (SocketException e){
						e.printStackTrace();
					
						Log.e("TWMBook", "SocketException retry :"+ retry);
						/* 					retry -- ;
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						} */
					
						if ( 0 == cancel){
							threadTestttMsg(getResources().getString(R.string.iii_server_SocketException),OnlineBook.FROM_DdownloadPbar,this.iInfo);
							
							threadErrorDownloadBookMsg(arg2);
							//ebook_isDownload.set(arg2, "0");
							setNowDownloadNum(false);

							//nowDownloadNum  = nowDownloadNum - 1;
						}
						doCancel(5);
						//ebook_isDownload.set(arg2, "0");
						return ;
					}catch (SocketTimeoutException e) {
				
						e.printStackTrace();
						if (DEBUG) Log.e("vic", "SocketTimeout cancel:"+cancel);
//						retry -- ;
						
						if (0 != cancel) {
							doCancel(10);
							return ;
						}
						
//						if (0 == retry) {
							if (0 == cancel) {
								throwErrorMsg(getResources().getString(
										R.string.iii_server_SocketTimeout));
								threadErrorDownloadBookMsg(arg2);
								//ebook_isDownload.set(arg2, "0");
								setNowDownloadNum(false);
							}
							doCancel(6);
							//ebook_isDownload.set(arg2, "0");
							return;
//						}
					
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
						//ebook_isDownload.set(arg2, "0");
						setNowDownloadNum(false);
					}
					threadErrorDownloadBookMsg(arg2);	
					doCancel(7);
					//ebook_isDownload.set(arg2, "0");
					
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
			}
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
			if(errorMsg.equals("")||errorMsg.equals("no problem")){
				threadTestttMsg(msg,OnlineBook.FROM_DdownloadPbar,this.iInfo);								
			}else{
				threadTestttMsg(errorMsg,OnlineBook.FROM_DdownloadPbar,this.iInfo);	
			}
		}
		private boolean isCanceling = false ;
		/**
		 * 下載中斷
		 * @param no 中斷點
		 */
		private void doCancel(int no) {
			
			if (false == isCanceling) {
				isCanceling = true;

				if (DEBUG) Log.d("vic", "doCancel +++" + no);
					
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
				todb.update(id , "bookOtherInfo" , "3" );
				long stamp = getTimeStamp(arg2);
				if ( mTimeStamp == stamp) {
					mybl.setEebook_isdownloadbook(arg2, "3");
				}else {
					if (DEBUG) Log.e("vic", "mTimeStamp != stamp, mTimeStamp:" +mTimeStamp + " stamp:"+ stamp);
					isCanceling = false ;
					return ;
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
				try {
					int arg2 = msg.getData().getInt("Position");
					mainRow[arg2].pbar.setVisibility(View.GONE);
					mainRow[arg2].cancel.setVisibility(View.GONE);
					mainRow[arg2].icon.setAlpha(100);		
					mainRow[arg2].text.setTextColor(Color.GRAY);
					mainRow[arg2].text.setBackgroundColor(Color.alpha(100));		
					//mainRow[arg2].nowStatus = "0";
					pbarNowStatusList.set(arg2, "0");
					mybl.setEebook_isdownloadbook(arg2,"0");
					lv_online_main.invalidateViews();	
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		};
		
		private Handler reflashView = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				lv_online_main.invalidateViews();
			}
		};
		
		private void threadErrorDownloadBookMsg(int position) {
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
			return cancel;
		}	

		public synchronized void del() {
			//isDel = true;
		}
		/**
		 * 暫停
		 */
		public synchronized void pause() {
			pause = true;
		}
		/**
		 * 重新開始
		 * @param percent 百分比
		 */
		public synchronized void restart(int percent) {
			restart(false,percent);
		}
		/**
		 * 重新開始
		 * @param fromBegin 是否重新
		 * @param per 百分比
		 */
		public synchronized void restart(boolean fromBegin,int per) {
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
		return nowDownloadNum;
	}
	/**
	 * 將下載數量設定0
	 */
	public synchronized void resetNowDownloadNum(){
		nowDownloadNum = 0 ;
		if (DEBUG) Log.e("vic", "resetNowDownloadNum") ;
	}
	
/*	private void exit() {
		
		for ( DownloadPbar dpbar : dp ){
			dpbar.setCancel(true);
		}
		
		tdb.close();
		todb.close();
		System.exit(0);
	}*/
	/**
	 * 建構線上書櫃清單
	 * @author III
	 * 
	 */
	public class myBookList extends BookList{
		//private List<String> nowStatus;
		
		public myBookList(Context context,Cursor c,Boolean ie) {
			super(context);
			mInflater = LayoutInflater.from(context);
			isEdit = ie;
			myCursor = c;
			//isDownload = id;
			ebook_title = new ArrayList<String>();   	
			ebook_cover = new ArrayList<String>();   
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
			ebook_isdownloadbook.set(position, value);
			//mainRow[position].nowStatus = value;
			pbarNowStatusList.set(position, value);
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
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.iii_file_row, null);
				mainRow[position] = new ViewHolder();
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
	      	  		//nowDownloadNum  = nowDownloadNum - 1;
					dp.get(num).setCancel(true);
	      	  		
	      	  		Log.e("cancel","cancel");
	      	  		//new File(saveFilelocation + ebook_deliveryid.get(position) +fileType).delete();
	      	  		tdb.updateByDeliveryId(ebook_deliveryid.get(position) , "isdownloadbook" , "3" );
	      	  		
	      	  		System.out.println("cancel  position "+position);
					mainRow[position].pbar.setVisibility(View.GONE);
					mainRow[position].cancel.setVisibility(View.GONE);
					//Log.v("1","1");
					mainRow[position].icon.setAlpha(100);
					mainRow[position].text.setTextColor(Color.RED);		
					mainRow[position].text.setBackgroundColor(Color.alpha(100));
					mainRow[position].pbar.invalidate();
					//mainRow[position].nowStatus = "0";
					pbarNowStatusList.set(position, "0");
					ebook_isdownloadbook.set(position, "0");
					//lv_online_main.invalidateViews(); 	
					//dp.get(num).setCancel(true);	      	  		
				}
			});
			
			mainRow[position].cancel.setBackgroundColor(Color.TRANSPARENT);
			mainRow[position].pbar.bringToFront();
			mainRow[position].iconTag.bringToFront();
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

			
			//Log.v("-------------------","-------------------");
/*			if(mainRow[position].nowStatus.equals("1")){
				mainRow[position].cancel.setVisibility(View.GONE);
				mainRow[position].pbar.setVisibility(View.GONE);
				//Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____1");
			}else if(mainRow[position].nowStatus.equals("0")){
				mainRow[position].cancel.setVisibility(View.GONE);
				mainRow[position].pbar.setVisibility(View.GONE);
				//Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____0");
			}else if(mainRow[position].nowStatus.equals("2")){
				mainRow[position].cancel.setVisibility(View.VISIBLE);
				mainRow[position].pbar.setVisibility(View.VISIBLE);
				Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____2");
			}else if(mainRow[position].nowStatus.equals("3")){
				mainRow[position].cancel.setVisibility(View.GONE);
				mainRow[position].pbar.setVisibility(View.GONE);
				//Log.v("mainRow[position].nowStatus",String.valueOf(position)+"____3");
			}*/
			//Log.v("-------------------","-------------------");
			
			
			//mainRow[position].icon.setImageDrawable(Drawable.createFromPath(getResources().getString(R.string.iii_cms_cover)+ebook_cover.get(position).toString()));
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
				if(ebook_cover.get(position).equals("ivi_nonepict02")){
					mainRow[position].icon.setImageResource(R.drawable.ivi_nonepict02);
				}else{
					mainRow[position].icon.setImageResource(R.drawable.ivi_nonepict01);
				}					
			}
			mainRow[position].text.setSingleLine(true);
			mainRow[position].text.setWidth(width);
			mainRow[position].text.setMarqueeRepeatLimit(6);
			mainRow[position].cancel.setImageResource(R.drawable.ivi_button18);
			mainRow[position].text.setTextSize(TypedValue.COMPLEX_UNIT_PT,10);
			if (ebook_isdownloadbook.get(position).toString().equals("0")){
				mainRow[position].icon.setAlpha(100);		
				//mainRow[position].text.setBackgroundColor(Color.alpha(100));
				mainRow[position].text.setTextColor(Color.GRAY);
				mainRow[position].pbar.setVisibility(View.GONE);
				mainRow[position].cancel.setVisibility(View.GONE);
			}else if(ebook_isdownloadbook.get(position).toString().equals("1")){
				mainRow[position].icon.setAlpha(255);		
				mainRow[position].text.setTextColor(Color.BLACK);
				//mainRow[position].text.setBackgroundColor(Color.alpha(255));
				mainRow[position].pbar.setVisibility(View.GONE);
				mainRow[position].cancel.setVisibility(View.GONE);
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
			return super.getView(position, convertView, parent);
		}
		
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (DEBUG) Log.e("flw", "+onConfigurationChanged ") ;
	}
}
