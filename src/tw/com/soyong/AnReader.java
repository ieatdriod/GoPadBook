package tw.com.soyong;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import tw.com.soyong.mebook.Mebook;
import tw.com.soyong.mebook.MebookData;
import tw.com.soyong.mebook.MebookException;
import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.mebook.MebookInfo;
import tw.com.soyong.mebook.SyContent;
import tw.com.soyong.mebook.SyInputStream;
import tw.com.soyong.mebook.SyItem;
import tw.com.soyong.mebook.SyParser;
import tw.com.soyong.mebook.SySentence;
import tw.com.soyong.mebook.TWMMetaData;
import tw.com.soyong.utility.Native;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gsimedia.sa.DeviceIDException;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.IllegalRightObjectException;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.RealBookcase;


/**
 * MeReader View 的程式進入點, 負責<br>
 *  Initial DRM library，<br>載入書封圖檔 ，<br>
 * 載入Mebook資料 
 */

public class AnReader extends Activity {
	
	private static boolean DEBUG = false ;
	private static final String TAG = "AnReader";
	private static boolean DUMMY = false ;
	
	// preference file for DRM
	private static final String P12FOLDER = "p12"; //"DRM_p12Folder";
	static final String TRACK_INDEX = "DRM_TrackIndex";
	private static final int DEFAULT_TRACK_INDEX = 0; 			// begin from 1
	static final String TIME_STAMP = "UploadTimeStamp";
	
	private static final String TRY_VER = "isSample";
//	private static final boolean DEFAULT_TRY_VER = true;
	private static final String COVER_PATH = "coverPath";
	private static final String SYNC_LAST_PAGE = "syncLastPage" ;
	private static final boolean DEFAULT_SYNC_LAST_PAGE = true;
	private static final String CONTENT_ID = "content_id";
	private static final String BOOK_TITLE = "book_title";
	private static final String BOOK_AUTHORS = "book_authors";
	private static final String BOOK_PUBLISHER = "book_publisher";
	private static final String BOOK_CATEGORY = "book_category";
	
	/**
	 * store data retrieve from getFilesDir()
	 */
	public static String fileDir ;
	
	/**
	 * 取得sd卡的路徑&名字
	 * @return sd card full path name
	 */
    public static String getExternalStoreageName() {
    	File f = android.os.Environment.getExternalStorageDirectory();
    	String filename = f.getAbsolutePath();
    	
    	if((new File(filename+"/sd")).exists()){
    		filename = filename+"/sd";
    	}
		return filename;
	}	
    
	private String ebook_error;
	private String ebook_description;	
    
    private final Bitmap getAlbumCover() {
    	
    	SyInputStream is = new SyInputStream(MebookHelper.mSC , SyInputStream.MODE_COVER );
    	Bitmap bmp = BitmapFactory.decodeStream(is);
		
		try {
			is.close();
			is = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return bmp;
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
	
    private static final String STAMP_BEGIN= "<updated_at>";
    private static final String STAMP_END= "</updated_at>";
    private static final String TRACK_BEGIN= "<track>";
    private static final String TRACK_END = "</track>";
    private static final String SENT_BEGIN= "<index>";
    private static final String SENT_END = "</index>";
    
//    private static String mErrMsg ;
    private final void loadLastPage() {
    	 
    	
		new Thread() {
			@Override
			public void run() {
				SharedPreferences pref = getSharedPreferences(
						MebookHelper.mDeliverID, Context.MODE_PRIVATE);

				int localLastTrack = pref.getInt(TRACK_INDEX,
						DEFAULT_TRACK_INDEX);
				MebookHelper.mTrackIndex = localLastTrack;
				
				
				if (MebookHelper.mIsSyncLastPage) {
					
					if ( false == testNetwork() ){
						//mErrMsg = getResources().getString(R.string.network_not_available);
						
						mHandler.sendMessage(mHandler.obtainMessage(LOAD_LAST_PAGE_DONE,1 , 0 ));
						
						if ( null != progDlg && progDlg.isShowing()){
							progDlg.dismiss();
							
						}
						return ;
					}


					String xml= "";
					try {
						xml = downloadLastPage();
					} catch (DeviceIDException e) {
						e.printStackTrace();
						//mErrMsg = e.getMessage();
						
						mHandler.sendMessage(mHandler.obtainMessage(GET_DEVICE_ID_FAIL,0 , 0  ));
						
						if ( null != progDlg && progDlg.isShowing()){
							progDlg.dismiss();
							
						}
						return ;
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						//mErrMsg = e.getMessage();
						
						mHandler.sendMessage(mHandler.obtainMessage(LOAD_LAST_PAGE_DONE,1 , 0  ));
						
						if ( null != progDlg && progDlg.isShowing()){
							progDlg.dismiss();
							
						}
						return ;
						
					} catch (IOException e) {
						e.printStackTrace();
						//mErrMsg = e.getMessage();
						
						mHandler.sendMessage(mHandler.obtainMessage(LOAD_LAST_PAGE_DONE,1 , 0  ));
						
						if ( null != progDlg && progDlg.isShowing() ){
							progDlg.dismiss();
							
						}
						return ;
					}finally{
						if ( null != progDlg && progDlg.isShowing()){
							progDlg.dismiss();
							progDlg = null;
						}
					}
					
					// parser xml, retrive: last track/last sentence/time stamp
					String serverTimeStamp = null;
					Date serverStamp = new Date();
					int begin = xml.indexOf(STAMP_BEGIN);
					int end;
					if (begin >= 0) {
						begin += STAMP_BEGIN.length();
						end = xml.indexOf(STAMP_END, begin);

						if (end > begin) {
							serverTimeStamp = xml.substring(begin, end );
							if ( serverTimeStamp.length() > 0  ){
								serverStamp = new Date(Long.parseLong(serverTimeStamp));
							}
						} else {
							mHandler.sendMessage(mHandler.obtainMessage(LOAD_LAST_PAGE_DONE,1 , 0 ));
							return ;
						}
					} else {
						mHandler.sendMessage(mHandler.obtainMessage(LOAD_LAST_PAGE_DONE,1 , 0 ));
						return ;
					}

					// Parse the previous string back into a Date.
					final String dateFormat = getResources().getString(
							R.string.time_stamp_format);
					SimpleDateFormat formatter = new SimpleDateFormat(
							dateFormat);
					ParsePosition pos = new ParsePosition(0);					
					String localTimeStamp = pref.getString(TIME_STAMP,
							"2010/01/01 12:00:00"); // "yyyy/MM/dd hh:mm:ss"
					Date stamp = formatter.parse(localTimeStamp, pos);

					if (stamp.before(serverStamp)) {
						
						int serverLastTrack = -1;

						begin = xml.indexOf(TRACK_BEGIN);
						if (begin >= 0) {
							begin += TRACK_BEGIN.length();
							end = xml.indexOf(TRACK_END , begin);
							if (end > begin) {
								serverLastTrack = Integer.parseInt(xml
										.substring(begin, end));
							}

							if (serverLastTrack >= 0) {
								localLastTrack = serverLastTrack;
							}
						}

						int sentence;
						begin = xml.indexOf(SENT_BEGIN);
						if ( begin > 0 ){
							begin += SENT_BEGIN.length();
							end = xml.indexOf(SENT_END , begin);
							 if ( end > begin){
								 final String strSent = xml.substring(begin,end);
								 sentence =Integer.parseInt(strSent);
								 
//								 SharedPreferences.Editor edit = pref.edit();
//								 edit.putInt(MebookHelper.mHeaderInfo.mTitle + localLastTrack ,sentence).commit();
								 
								 pref.edit().putInt("Title" + localLastTrack ,sentence).commit();
							 }
						}
					}
				}

				MebookHelper.mTrackIndex = localLastTrack;
				
				if ( null != progDlg && progDlg.isShowing()){
					progDlg.dismiss();
				
				}
				
				mHandler.sendMessage(mHandler.obtainMessage(LOAD_LAST_PAGE_DONE,0 , 0  ));
			}
		}.start();
    }

	private final void getInputStreamProvider(final String bookPathName, final String p12Path) {
			
		new Thread() {
			@Override
			public void run() {

				try {
					// for dummy version, need specific full file path with
					// name
					// ("/sdcard/gsi/test.arg")
					GSiMediaInputStreamProvider sc = new GSiMediaInputStreamProvider(
							bookPathName, p12Path, getApplicationContext());

					// for release version, need two phase open
					// 1st: pass an folder path
					// 2nd: catch p12File exception , call api to d
					// GSiMediaInputStreamProvider sc = new
					// GSiMediaInputStreamProvider("/sdcard/gsi/471000000010000082-01-01.tvb",
					// "/sdcard/gsi/", MeReaderActivity.this);
					MebookHelper.mMeta = new TWMMetaData(sc);
					MebookHelper.mSC = sc;

					mHandler.sendEmptyMessage(INPUT_STREAM_PROVIDER_OPENED);
					return;
//				} catch (IOException e) {
//					e.printStackTrace();
//					// break;
//				} catch (IllegalRightObjectException e) {
//					e.printStackTrace();
//					// break;
//				} catch (DeviceIDException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					String deviceID;
					try {
						deviceID = GSiMediaRegisterProcess.getID(getApplicationContext());
					} catch (com.gsimedia.sa.GSiMediaRegisterProcess.DeviceIDException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						deviceID = "";
					}
					if(deviceID.length() == 0){
						mHandler.sendEmptyMessage(GET_DEVICE_ID_FAIL);
						return;
					}
				} 

				mHandler.sendEmptyMessage(INPUT_STREAM_PROVIDER_OPEN_FAIL);
				return ;
			}
		}.start();
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
	
	private String downloadLastPage() throws DeviceIDException,ClientProtocolException, IOException {

		String respBody = "Err";
		Document doc = null ;
   	 	HttpURLConnection conn = null;
		String deviceID;
		try{
			deviceID = GSiMediaRegisterProcess.getID(this);
		}catch(Throwable e){deviceID = "";};
    	if (deviceID == null) deviceID = "";
		
		String apiUrl = getResources().getString(R.string.web_api_lastpage_down);
		URL fileUrl = new URL(apiUrl+MebookHelper.mDeliverID+"&device_id="+deviceID);
		
		//URL fileUrl = new URL("http://61.64.54.35/testcode/rf.asp"); 
		String urlParameters = "&token="+RealBookcase.getToken(); 
		conn = (HttpURLConnection) fileUrl.openConnection();
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
//			threadTestttMsg(getResources().getString(
//					R.string.iii_NetworkNotConnMessage));
			return respBody;
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream is = conn.getInputStream();

//		BufferedReader r = new BufferedReader(new InputStreamReader(is));
//		StringBuilder total = new StringBuilder();
//		String line;
//		while ((line = r.readLine()) != null) {
//		    total.append(line);
//		}		
		
		try {
			doc = db.parse(is);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList nError = doc.getElementsByTagName("status");
		ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
		
		NodeList nDesc = doc.getElementsByTagName("description");
		ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();

		NodeList nData = doc.getElementsByTagName("data");
		String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
		InputStream dataStream = covertStringToStream(RevData);		
//		HttpParams httpParameters = new BasicHttpParams();
//		// Set the timeout in milliseconds until a connection is established.
//		int timeoutConnection = 10000;
//		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//		// Set the default socket timeout (SO_TIMEOUT) 
//		// in milliseconds which is the timeout for waiting for data.
//		int timeoutSocket = 10000;
//		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);		
//		
//		DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
//		HttpResponse response;
//		String apiUrl = getResources().getString(R.string.web_api_lastpage_down);
//		HttpGet httGet = new HttpGet(apiUrl+MebookHelper.mDeliverID+ "&device_id=" + deviceID);
//
//        String respBody = "Err";
//
//		response = httpclient.execute(httGet);
		Log.e("Token","ebook_error=>"+ebook_error+" "+"ebook_description=>"+ebook_description+respBody);
		if (ebook_error.equals("1")) {
		respBody = AnReader.convertStreamToString(dataStream);
		
		if ( DEBUG) Log.e(TAG ,respBody );
		return respBody;
		}else{
			if(deviceID.length() == 0){
				throw new DeviceIDException();
			}
			
			return respBody;
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
	
	private final void getBookContent() {
		
		if ( MebookHelper.mTrackIndex <= 0 ){
			Log.e(TAG ,"track index exception !!!!");
			return ;
		}
		
		if ( null == MebookHelper.mMeta ){
			Log.e(TAG ,"mMeta == null !!!!");
			return ;
		}
		
		new Thread() {
			@Override
			public void run() {
				
				try {
			        // get input stream by mp3 title
			        String title = MebookHelper.mMeta.getMP3Title(MebookHelper.mTrackIndex);			// begin from 1
			        SyInputStream is = new SyInputStream(MebookHelper.mSC , title);						// "The Wind and The Sun"
			        if ( is.mOpenState != 0){
			        	int resid = R.string.drm_issue_unknow;
			        	switch( is.mOpenState ){
			    		case 1:
			    			resid = R.string.drm_issue_rightobject;
			    			break;
			    		case 2:
			    			resid = R.string.drm_issue_p12;
			    			break;
			    		case 3:
			    			resid = R.string.drm_issue_io;
			    			break;
			    		default:
			    			resid = R.string.drm_issue_unknow;
			    			break;
			        	}
			        	mHandler.sendMessage(mHandler.obtainMessage(DRM_ISSUE,resid , 0  ));
			        	return ;
			        }
			        
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
								b3 = data[i+3] & 0xff;
								b2 = data[i+2] & 0xff;
								b1 = data[i+1] & 0xff;
								b0 = data[i] & 0xff;
								
								frameTable[j] = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0; 
								j++;
							}
							MebookHelper.mFrameTable = frameTable;
							MebookHelper.mISSyd = is ;

							if (DEBUG) Log.d(TAG, str);
						} catch (MebookException e) {
							e.printStackTrace();
						}
						MebookHelper.mBookData = bookData;
						MebookHelper.mHeaderInfo = bookInfo;
						item = null;
					}
					book = null;
					
					
			        TWMMetaData xml = MebookHelper.mMeta;
			        String orgLang = "en_US";
			        if ( null != xml){
			        	
			        	orgLang = xml.getOrgLang();
			        	if ( null != orgLang && orgLang.length()>0 ){
			        		orgLang = orgLang.trim();
			        	}
			        } 
					
			        MebookHelper.mIsJpBook = false ;
			        if ( orgLang.startsWith("jp_")){
			        	MebookHelper.mIsJpBook = true ;
					}
					
					
					
					SyParser parser = new SyParser();
					SyContent content = parser.getContent(str);
					parser = null;

					// prepare sentence array
					final int sentCount = content.getTotalSentence();
					if (0 == sentCount) {
						if ( DEBUG ) Log.d(TAG, "Total Sentence = 0 !!!!");
						
						mHandler.sendEmptyMessage(GET_CONTENT_FAIL);
						return;
					}
					MebookHelper.mContent = content;

					SySentence [] sentenceArr = new SySentence[sentCount];
					content.getSentenceArr(sentenceArr); 
					MebookHelper.mSentenceArr = sentenceArr;
					
					//
					// initial DRM Mp3 content
					//
					if ( false ==  Native.InitDRMMp3(MebookHelper.mSC , title) ){
						mHandler.sendMessage(mHandler.obtainMessage(DRM_ISSUE, R.string.file_open_fail, 0 ));
						return ;
					}
					Native.open("");
						
					System.gc();
					mHandler.sendEmptyMessage(GET_CONTENT_DONE);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG ,"Exception !!!!");
					mHandler.sendEmptyMessage(GET_CONTENT_FAIL);
				} finally {
					// Dismiss the Dialog
//					if ( null != progDlg){
//						progDlg.dismiss();
//					}
				}
			}
		}.start();
	}  

	private void showErrMsg(final int resid){
        new AlertDialog.Builder(this)
        .setTitle(R.string.drm_issue_title)
        .setMessage(resid)
        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int whichButton) {
        	   finish();		// can't open content, return to book store
           }
       })
       .show();		
	}
	
	private void showErrMsg(final int title,final int message){
        new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int whichButton) {
        	   finish();		
           }
       })
       .show();		
	}
	
	private static final int INPUT_STREAM_PROVIDER_OPENED = 100;
	private static final int INPUT_STREAM_PROVIDER_OPEN_FAIL = 101;
	private static final int GET_CONTENT_DONE = 200;
	private static final int GET_CONTENT_FAIL = 201;
	//private static final int GET_CONTENT_FAIL_REGISTER = 202;
	private static final int DRM_ISSUE = 300;
	private static final int LOAD_LAST_PAGE_DONE = 400;
	private static final int GET_DEVICE_ID_FAIL = 500;

	private long mHelpStartTime = 0 ;
	private volatile boolean mUserCancel = false;
	private ProgressDialog progDlg = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if ( true == shutdownRequested ) {
				return ;
			}
			
			switch (msg.what) {
			case INPUT_STREAM_PROVIDER_OPENED:
				if (DEBUG) Log.d(TAG , "INPUT_STREAM_PROVIDER_OPENED");
				
				canBack = false ;
				
				Bitmap bmp = getAlbumCover();
				if ( null != bmp){
					ImageView img = (ImageView)findViewById(R.id.coverView);
					img.setImageBitmap(bmp);
				}
				
				if (MebookHelper.mIsSyncLastPage){
					if(progDlg == null)
					progDlg = ProgressDialog.show(AnReader.this,
							getText(R.string.load_last_page_title),
							getText(R.string.load_last_page_message), false);
				}
				loadLastPage();
				break;
				
			case INPUT_STREAM_PROVIDER_OPEN_FAIL:
				if (DEBUG) Log.d(TAG , "INPUT_STREAM_PROVIDER_OPEN_FAIL");
				showErrMsg(R.string.input_stream_provider_open_fail);
				break;
				
			case LOAD_LAST_PAGE_DONE:
			{
				mHelpStartTime = 0 ;
				
				if (DEFAULT_TRACK_INDEX == MebookHelper.mTrackIndex) {
					// First time use
					final RelativeLayout helpView = (RelativeLayout) findViewById(R.id.helpView);
					helpView.setOnTouchListener( new OnTouchListener(){

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							mUserCancel = true ;
							return false;
						}
						
					});
					
					helpView.setVisibility(View.VISIBLE);
					MebookHelper.mTrackIndex = 1 ;
					
					mHelpStartTime = System.currentTimeMillis();
					
					
				}
				
				getBookContent();
//				if ( 0 == msg.arg1 ){
//					getBookContent(null);
//				} else {
//					showErrMsg();
//				}
				
				if ( MebookHelper.mIsSyncLastPage && 0 == msg.arg1  ){
		            Toast.makeText(AnReader.this.getApplicationContext(), getResources().getString(R.string.iii_last_page_sync_success),  
		                    Toast.LENGTH_SHORT).show(); 
				}				
				
				canBack = true ;
			}
				break;
				
			case GET_CONTENT_DONE:
			{
				if (DEBUG) Log.d(TAG, "GET_CONTENT_DONE");
				
				
				if ( mHelpStartTime > 0  ){
					final RelativeLayout helpView = (RelativeLayout) findViewById(R.id.helpView);
					if ( View.VISIBLE == helpView.getVisibility()){
						if ( System.currentTimeMillis()- mHelpStartTime < 10000 && false == mUserCancel ){
							sendMessageDelayed(obtainMessage(GET_CONTENT_DONE),500);
							return ;
						}
					}
				}

		        Intent it = new Intent(AnReader.this, MeReaderActivity.class);
		       	startActivity(it);				
				finish() ; // finish AnReader
			}
				break;
			
			case GET_CONTENT_FAIL:
				if (DEBUG) Log.d(TAG, "GET_CONTENT_FAIL");
				//showErrMsg();
				break;
			
//			case GET_CONTENT_FAIL_REGISTER:
//				
//				
//				//showErrMsg( this. R.string. .file_open_fail);
//				break;
				
			case DRM_ISSUE:
				// can't open content, return to book store
				showErrMsg(msg.arg1);
								
				break;
				
			case GET_DEVICE_ID_FAIL:
				showErrMsg(R.string.GSI_ERROR,R.string.GSI_DEVICE_ID_EMPTY_MSG);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        fileDir = getFilesDir().toString();	
        
        MebookHelper.clear();
        
//        SyBookmark bm = new SyBookmark(this, 0);
//        String xml = bm.getBookmarkXml("ERT01241000006201");
//        Log.e(TAG , xml);
//        
//        bm.setBookmark("12345", "2", "1000110");
        
        if (DEBUG) Log.e(TAG , "begin +++");	//help to measure start up time
        
		setContentView(R.layout.splashscreen);
        
        String bookPath = null;
        
        if ( DUMMY ){
        	//bookPath = "/sdcard/gsi/471000000010000082-01-01.tvb";
        	//bookPath = "/sdcard/gsi/471000000010000081-01-01.tvb";
        	//bookPath = getExternalStoreageName()+"/471000000010000081-01-01.tvb";
        	bookPath = "/sdcard/gsi/471000000010000081-00-01.tvb";
        	//bookPath = "/sdcard/gsi/471000000010000081-01-01.tvb";
        	
	        MebookHelper.mP12Folder = getExternalStoreageName() + "/gsi";
	        MebookHelper.mCoverPath = getExternalStoreageName() + "/1.jpg";
	        MebookHelper.mIsSyncLastPage = false;
	        MebookHelper.mIsSample = false;
	        MebookHelper.mContentID = "ERT01241000006";
        } else {
			// get uri param
			final Uri uri = getIntent().getData();
			if (null != uri) {
				bookPath = uri.getPath();
				
		        Bundle extras = getIntent().getExtras();
		        
		        MebookHelper.mP12Folder = extras.getString(P12FOLDER);
		        //MebookHelper.mIsSample = extras.getBoolean(TRY_VER, DEFAULT_TRY_VER);
		        MebookHelper.mCoverPath = extras.getString(COVER_PATH) ;
		        MebookHelper.mIsSyncLastPage = extras.getBoolean(SYNC_LAST_PAGE,DEFAULT_SYNC_LAST_PAGE );
		        MebookHelper.mContentID  = extras.getString(CONTENT_ID);
		        MebookHelper.mBookTitle = extras.getString(BOOK_TITLE);
		        MebookHelper.mBookAuthors = extras.getString(BOOK_AUTHORS);
		        MebookHelper.mBookPublisher = extras.getString(BOOK_PUBLISHER);
		        MebookHelper.mBookCategory = extras.getString(BOOK_CATEGORY);
		        
		        String strIsSample = extras.getString(TRY_VER).trim();
		        MebookHelper.mIsSample = false ;
		        if ( 0 == strIsSample.compareTo("1") ){
		        	MebookHelper.mIsSample = true ;
		        }
			}
		}
        
        String fileName = bookPath.substring( bookPath.lastIndexOf('/')+1 , bookPath.lastIndexOf(".tvb") );
        MebookHelper.mDeliverID = fileName ;
        
        if ( DUMMY ){
        	MebookHelper.mDeliverID = "ERT01241000006201";
        }

        // Ansync function call, will receive message
        getInputStreamProvider(bookPath, MebookHelper.mP12Folder);
    }
    
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if ( false == shutdownRequested){
			return super.dispatchKeyEvent(event);
		}
		
		return true ;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if ( false == shutdownRequested ){
			return super.dispatchTouchEvent(ev);
		}
		return true ;
	}   
    
	@Override
	protected void onPause() {
		super.onPause();
		if(null != progDlg && progDlg.isShowing()){
			progDlg.dismiss();
			progDlg = null;
		}

		Editor editPref = getSharedPreferences(MebookHelper.mDeliverID, Context.MODE_PRIVATE).edit();
		editPref.putInt(TRACK_INDEX, MebookHelper.mTrackIndex).commit();
	}
	
	private volatile boolean shutdownRequested = false ;
	private volatile boolean canBack = true ;
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:{
			
			if ( false == canBack ){
				return true ;
			}
			
			shutdownRequested = true ;
		}
		
		default:
			return super.onKeyDown(keyCode, event);
		}

	}
	
	/**
	 * 刪除某本書最後閱讀頁紀錄(只刪local端)
	 * @param ctx context
	 * @param did deliver id
	 * @return true:無錯誤發生; false: 發生錯誤
	 */
	public static boolean deleteLastPageOfBook(Context ctx, String did){
		try {
			SharedPreferences pref = ctx.getSharedPreferences(did, Context.MODE_PRIVATE);
			pref.edit().clear().commit();
			return true;
		} catch (NotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
//	/**
//	 * 刪除所有最後閱讀頁
//	 * @param ctx context
//	 * @return true:無錯誤發生; false: 發生錯誤
//	 */
//	public static boolean deleteAllLastPage(Context ctx){
//		try {
//			SharedPreferences lastPages = ctx.getSharedPreferences(ctx.getResources().getString(R.string.iii_last_page_name),0);  
//			lastPages.edit().clear().commit();
//			return true;
//		} catch (NotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
//	}	
	
}

