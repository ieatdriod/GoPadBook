package tw.com.mebook.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import tw.com.mebook.lab.listview.BookcaseAdapter;
import tw.com.mebook.lab.listview.BookcaseAdapter.DL_STATE;
import tw.com.mebook.util.ImageDownloader.BitmapDownloaderTask;
import tw.com.mebook.util.ImageDownloader.Mode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.taiwanmobile.myBook_PAD.LoginDialogController;
import com.taiwanmobile.myBook_PAD.LoginDialogObserver;
import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.RealBookcase;
import com.taiwanmobile.myBook_PAD.TWMBook;
import com.taiwanmobile.myBook_PAD.TWMDB;

public class BookDownloader {

	private static final boolean DEBUG = true ;
    private static final String TAG = "BookDownloader";	
	
    private static final int MAX_TASK = 5 ;
    private static int mTaskCount = 0;
    private String mDLPath ;
    private String mDLUrl;
    private String mDeviceID;
    private WeakReference<Activity> mCtx;
    
    
    private static final int PENDING_MSG = 100;
    private BookcaseAdapter mAdapter ;
    private int mPendingCount = 0 ;
    private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if ( null != mAdapter){
				mAdapter.notifyDataSetChanged();
				mPendingCount = 0 ;
			}
			
		}
    };
    
    private final static HashMap<String, BookDownloaderTask> sTaskCache = new HashMap<String, BookDownloaderTask>(MAX_TASK);
    
    public BookDownloader(Activity ctx){
    	mCtx = new WeakReference<Activity>(ctx) ;
    }
    
    public void setAdapter(BookcaseAdapter adapter){
    	mAdapter = adapter;
    }
    
	private DL_STATE [] mDLStates;	// 0:no act, 1:downloading, 2:downloaded
	private int [] mProgs;
	private TWMDB mDb;
	private Cursor mCursor;
    
    public void download(String url , String type, View bookInfoView, int index ) {
    	
    	if ( null == url || null == bookInfoView){
    		return ;
    	}

    	//resetPurgeTimer();
    	BookDownloaderTask task = getTaskFromCache(url);
    	if ( null == task ){
    		Log.e(TAG, "download forceDownload :"+url);
    		forceDownload(url, type,bookInfoView , index );
    	} else {
    		task.setInfo(url,type,bookInfoView,index);
    		if ( true == task.cancel(true) ){
    			this.removeTaskFromCache(url);
    			Log.e(TAG, "download cancel task :"+url);
    		}else{
    			Log.e(TAG, "download cancel task fail :"+url);
    		}
    	}
    }
    
    public void cancel(String url){
    	if ( null == url ){
    		return ;
    	}
    	
    	BookDownloaderTask task = getTaskFromCache(url);
    	if ( null != task){
    		if(DEBUG) Log.d(TAG , "task:"+url+ " cancel");
    		task.cancel(true);
    		removeTaskFromCache(url);
    	}
    	
    	
    }
    
    public void setDLPath(String path){
    	mDLPath = path ;
    }  
    
    public void setDLUrl(String dlUrl){
    	mDLUrl = dlUrl;
    }
    
    public void setDeviceID(String deviceID){
    	mDeviceID = deviceID;
    }
    
    public void setDb(TWMDB db , Cursor cursor){
    	mDb = db ;
    	mCursor = cursor;
    }
    
    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, String type, View bookInfoView, int index) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (url == null) {
            return;
        }

        if (cancelPotentialDownload(url, bookInfoView)) {
        	
//        	if ( mTaskCount >= MAX_TASK ){
//        		if ( DEBUG ) Log.w(TAG, "mTaskCount:"+mTaskCount+ " >= MAX_TASK " );
//        		return ;
//        	} else {
//        		mTaskCount ++;
//        		BookDownloaderTask task = new BookDownloaderTask(bookInfoView, index);
//        		task.setType(type);
//        		addTaskToCache(url, task);
//        		task.execute(url);
//        		
//        		mDLStates[index] = DL_STATE.DLING;
//        	}
        	
    		BookDownloaderTask task = new BookDownloaderTask(bookInfoView, index);
    		task.setInfo(url,type,bookInfoView,index);
    		if ( addTaskToCache(url, task) ){
        		task.setType(type);
        		task.setUrl(url);
    			task.execute(url);  
    			mDLStates[index] = DL_STATE.DLING;
    		}
        }

    }    
    
    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String url, View bookInfoView) {
    	BookDownloaderTask bookDownloaderTask = null;
    	synchronized (sTaskCache ){
    		bookDownloaderTask = sTaskCache.get(url);
    	}    	

        if (bookDownloaderTask != null) {
            String fileUrl = bookDownloaderTask.url;
            if ((fileUrl == null) || (!fileUrl.equals(url))) {
            	bookDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }
    
	//////////////////////////////////////////////////////////////////

	private String getServerErrMsg(String a) {
		String err ;
		String msg = a.substring(a.lastIndexOf(",") + 1 ).trim() ;
		if ( msg.startsWith("no problem")){
			err = "no problem";
		}else {
			err = msg ;
		}
		return err;
	}    
    
    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 10;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, BookDownloaderTask> sHardTaskCache =
        new LinkedHashMap<String, BookDownloaderTask>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, BookDownloaderTask> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftTaskCache.put(eldest.getKey(), new SoftReference<BookDownloaderTask>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<BookDownloaderTask>> sSoftTaskCache =
        new ConcurrentHashMap<String, SoftReference<BookDownloaderTask>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };    
    
    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    private boolean addTaskToCache(String url, BookDownloaderTask task) {
    	boolean b = false ;
        if (task != null && url != null) {
            synchronized (sHardTaskCache) {
            	int count = sHardTaskCache.size();
            	
            	if ( count < MAX_TASK ){
            		sHardTaskCache.remove(url);
               	 	BookDownloaderTask v = sHardTaskCache.put(url, task);
               	 	Log.d("BookDownloader" , ""+count + " url:"+ url);
               	 	b= true ;
               	 	
            	}else {
            		Log.d("BookDownloader" , ""+count + "> Max_TASK");
            	}
            }
        }
        return b ;
    }
    
    private void removeTaskFromCache(String url){
    	if ( url != null){
    		synchronized (sHardTaskCache) {
    			sHardTaskCache.remove(url);
    			
    			if (DEBUG){
	    			int count = sHardTaskCache.size();
	    			Log.d("BookDownloader" , "remove:"+count );
    			}
    		}
    	}
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private BookDownloaderTask getTaskFromCache(String url) {
    	
        // First try the hard reference cache
        synchronized (sHardTaskCache) {
            final BookDownloaderTask task = sHardTaskCache.get(url);
            if (task != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardTaskCache.remove(url);
                sHardTaskCache.put(url, task);
                return task;
            } 
            return null;
        }
    }
    
    
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardTaskCache.clear();
        sSoftTaskCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }    
        
	//////////////////////////////////////////////////////////////////////////////////////
    long mFreeSize;
    private StatFs stat;
	private void calFreeSize() {
		int size = stat.getBlockSize();
		int num = stat.getAvailableBlocks();
		mFreeSize = (long)num * size ;
	}
    
	
	private String mDLErrMsg = null;
    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BookDownloaderTask extends AsyncTask<String, Integer, Long> {
    	
    	private LoginInfo iInfo = null;
    	public void setInfo(String url , String type, View bookInfoView, int index ){
    		iInfo = new LoginInfo();
    		iInfo.setInfo(url, type, bookInfoView, index);
    	}
    	private int mLastErr = 0 ;
    	
        Long downloadBook(String deliverID) {
        	
	        	
	        	String url = mDLUrl + deliverID + "&device_id=" + mDeviceID +"&token="+RealBookcase.getToken()+"&pointer=";
	        	HttpURLConnection conn = null;
	        	File file = new File(mDLPath+deliverID+type+".tmp");
	        	url = url + String.valueOf((int)(file.length()/20480));
	        	URL myURL = null;
	        	try {
					myURL = new URL(url);
				} catch (MalformedURLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
		
	        	
	        	final long len = file.length();
	        	RandomAccessFile oSavedFile;
	        	int curFileSize = 0 ;
				try {
					oSavedFile = new RandomAccessFile(file,"rw");
					curFileSize = (int) (len-(int)(len%20480)) ;
					oSavedFile.seek(curFileSize);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					mLastErr = 1 ;			// open file fail, file not found
					return null;
				} catch (IOException e) {
					e.printStackTrace();	
					mLastErr = 1 ;			// open file fail, io error
					return null;
				}
	        	
	        	        	
	        	
	        	//////////////////////////////////////////////////////////
	//            final int IO_BUFFER_SIZE = 4 * 1024;
//	        	final int CONNECT_TIMEOUT = 15000 ; 
//	        	final int READ_TIMEOUT = 15000;
//	            
//	          	HttpParams httpParameters = new BasicHttpParams();
//	    		// Set the timeout in milliseconds until a connection is established.
//	    		int timeoutConnection = CONNECT_TIMEOUT;
//	    		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//	    		// Set the default socket timeout (SO_TIMEOUT) 
//	    		// in milliseconds which is the timeout for waiting for data.
//	    		int timeoutSocket = READ_TIMEOUT;
//	    		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);		
//	    		
//	            // AndroidHttpClient is not allowed to be used from the main thread
//	            final HttpClient client = AndroidHttpClient.newInstance("Android");
//	                
//	            final HttpGet getRequest = new HttpGet(url);
//	            //getRequest.setParams(httpParameters);
	            
            try {
//                HttpResponse response = client.execute(getRequest);
//                final int statusCode = response.getStatusLine().getStatusCode();
//                if (statusCode != HttpStatus.SC_OK) {
//                    Log.w(TAG, "Error " + statusCode +
//                            " while retrieving file from " + url);
//                    
//                    mLastErr = 2; 	// response not OK
//                    return null;
//                }
//                	        
//                final HttpEntity entity = response.getEntity();
//                if (entity != null) {
//                	byte[] buff = new byte[64];
//                	String a = null;
//                	long fileSize = 0 ;
//                	int currentRead = 0 ;
                    InputStream inputStream = null;
            	conn = (HttpURLConnection) myURL.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
	    		
//				conn.setRequestProperty("Content-Length",
//						"" + Integer.toString(urlParameters.getBytes().length));
				conn.setRequestProperty("Content-Language", "UTF-8");
	                
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				// Send request
				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				//wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
				int resp = conn.getResponseCode();
				if (resp != HttpURLConnection.HTTP_OK) {
					 Log.w(TAG, "Error " + resp +
                            " while retrieving file from " + url);
                    
                    mLastErr = 2; 	// response not OK
                    return null;
                }
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				inputStream = conn.getInputStream();
            //  final HttpEntity entity = response.getEntity();
                	        
              if (inputStream != null) {
                	byte[] buff = new byte[64];
                	String a = null;
                	long fileSize = 0 ;
                	int currentRead = 0 ;
                    try {
                       // inputStream = entity.getContent();
                        
                        inputStream.read(buff);
                        a = new String(buff).trim();
                        BigInteger bigInt = new BigInteger(buff);
                        String hexString = bigInt.toString(16); // 16 is the radix

                        mDLErrMsg = getServerErrMsg(a);
                        if (true == a.startsWith("00")) {
                        	// server response OK
                        }else if(true == a.startsWith("02")){
                        	mLastErr = 3;	// server response error msg
                        	//RealBookcase.AuthSSOLogin((RealBookcase)mCtx, RealBookcase.realbook, mCtx,((RealBookcase)mCtx).getHandler());
                        	threadHandleMsg(mDLErrMsg);
                        	return null;
                        	
                        }else if(true == a.startsWith("03")){
                        	mLastErr = 3;	// server response error msg
                        	//RealBookcase.AuthSSOLogin((RealBookcase)mCtx, RealBookcase.realbook, mCtx,((RealBookcase)mCtx).getHandler());
                        	threadHandleMsg(mDLErrMsg);
                        	return null;
                        }else if(true == a.startsWith("04")){
                        	mLastErr = 3;	// server response error msg
                        	//RealBookcase.AuthSSOLogin((RealBookcase)mCtx, RealBookcase.realbook, mCtx,((RealBookcase)mCtx).getHandler());
                        	threadHandleMsg(mDLErrMsg);
                        	return null;
                        } else /*if (true == a.startsWith("01"))*/ {
                        	
                        	mLastErr = 3;	// server response error msg
                        	return null;
                        }
                        
                        
    					fileSize = Long.valueOf(a.substring(a.indexOf(",")+1, a.lastIndexOf(","))); 
    					
    		    		stat = new StatFs(mDLPath);
    		    		calFreeSize();	//mFreeSize
    					
    		    		if ( mFreeSize < fileSize ){
    		    			mLastErr = 4 ;	// disk not enough space
    		    			return null;
    		    		}
    					
    					buff = null;
    					buff = new byte[1024];
    					int percent = 0 ;
    					int perTmp = 0 ;
    					boolean b;
    					while ((currentRead = inputStream.read(buff)) > 0 ) { 
    						b = isCancelled();
    						if ( true == b ){
    							if(DEBUG) Log.e(TAG, "downloadBook detect cancel event:"+ url);
    							mLastErr = 5 ; 	// user cancel

    							return null;
    						}
    						
    						oSavedFile.write(buff, 0, currentRead);
    						curFileSize+=currentRead;
    						
    						double la = curFileSize;
    						double lb = fileSize;
    						double lc = la/lb*100;
    						
    						perTmp = (int)(lc);
    						if (  perTmp!= percent ){
    							percent = perTmp ;
    							
    							publishProgress (percent);
    						}
    					}
    					
                    	if ( null != oSavedFile){
                    		oSavedFile.close();
                    		oSavedFile = null;
                    		
                    		if ( curFileSize == fileSize ){
                    			file.renameTo(new File(mDLPath+deliverID+type));
                    			
                    			updateDb(deliverID);
                    			
                    			Editor editPref = mCtx.get().getSharedPreferences("DL_BOOK", Context.MODE_PRIVATE).edit();
                    			editPref.putBoolean(deliverID, true).commit();
                    		}
                    	} 
                    	
                        return fileSize;
                    } finally {
                    	if ( null != oSavedFile){
                    		oSavedFile.close();
                    	}                    	
                    	
						try {
							if (inputStream != null) {
								inputStream.close();
							}

							//entity.consumeContent();
						} catch (Exception e) {
							e.printStackTrace();
						}
                    }
                }
            } catch (SocketTimeoutException e) {
            	//getRequest.abort();
            	mLastErr = 9;		// socket timeout
            	
            } catch (SocketException e){ // socket exception
            	//getRequest.abort();
            	mLastErr = 10 ;
            	
            } catch (IOException e) {
                //getRequest.abort();
                Log.w(TAG, "I/O error while retrieving data from " + url, e);
                mLastErr = 6;		// receive data error 
            } catch (IllegalStateException e) {
                //getRequest.abort();		
                Log.w(TAG, "Incorrect URL: " + url);
                mLastErr = 7 ; 		// incorrect url
            } catch (Exception e) {
                //getRequest.abort();
                Log.w(TAG, "Error while retrieving data from " + url, e);
                mLastErr = 8 ; 		// unknow error
            } finally {
//                if ((client instanceof AndroidHttpClient)) {
//                    ((AndroidHttpClient) client).close();
//                }
//ask victor??        
            	conn.disconnect();
            }
            
            //mLastErr = 8 ; 		// unknow error
            return null;
        }	    	
    	
    	
        @Override
		protected void onPreExecute() {
			super.onPreExecute();
			
            if (viewReference != null) {
                View bookInfoView = viewReference.get();
                
                ProgressBar prog = (ProgressBar)bookInfoView.findViewById(R.id.progress);
                prog.setVisibility(View.VISIBLE);
                prog.setProgress(0);
            }			
			
		}


		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			
			if (DEBUG) Log.w(TAG , "onCancelled:" + url);
			
			if ( null == url ){
				url = "test fail!!!";
			}
			
        	doCancel();
		}


		private void doCancel() {
			mDLStates[mId] = DL_STATE.NOACT;
        	
        	if ( null != viewReference ){
        		View bookInfoView = viewReference.get();
        		ProgressBar prog = (ProgressBar)bookInfoView.findViewById(R.id.progress);
            	prog.setVisibility(View.INVISIBLE);
        	}
        	removeTaskFromCache(url);
        	if (DEBUG) Log.e(TAG , "doCancel:" + url);
        	mTaskCount-- ;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			
			Integer v = values[0];
            if (viewReference != null) {
                View bookInfoView = viewReference.get();
                if ( null != bookInfoView){
                ProgressBar prog = (ProgressBar)bookInfoView.findViewById(R.id.progress);
                prog.setProgress(v);
                }
                
                mProgs[mId]=v;
            }
		}

		private String type;
		private String url;
        private final WeakReference<View> viewReference;
        private int mId ;

        public BookDownloaderTask(View bookInfoView , int index) {
        	viewReference = new WeakReference<View>(bookInfoView);
        	mId = index ;
        }
        
        public void setUrl(String url){
        	this.url = url;
        }
        
        /**
         * Actual download method.
         */
        @Override
        protected Long doInBackground(String... params) {
            String paurl = params[0];
            
            if(DEBUG)Log.w(TAG, "doInBackground url:"+paurl);
            return downloadBook(paurl);
        }
        
        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Long bookSize) {
        	
        	if ( null == bookSize){
        		showErrorMsg();
        	}
        	
            if (isCancelled()|| null == bookSize) {
                //bitmap = null;
            	if (DEBUG) Log.e(TAG , "onPostExecute & detect cancelled :" + url);
            	doCancel();
            	return ;
            } else {
            	if (DEBUG) Log.e(TAG , "onPostExecute: " + url);
            }

            mTaskCount-- ;
            
            if (viewReference != null ) {
                View bookInfoView = viewReference.get();
                
                Integer index = (Integer) bookInfoView.getTag();
                
                BookDownloaderTask task = getTaskFromCache(url);
                if ( task == this && index == mId){
	                ProgressBar prog = (ProgressBar)bookInfoView.findViewById(R.id.progress);
	
	                prog.setVisibility(View.INVISIBLE);
	                ImageView imageView = (ImageView)bookInfoView.findViewById(R.id.cover);
	                imageView.setAlpha(255);
	                bookInfoView.setEnabled(true);
                }else {
                	Log.e("god" , "mId miss match, triger pending check timer !!!");
                	
                	if ( 0 == mPendingCount ){
                		mHandler.sendEmptyMessageDelayed(PENDING_MSG, 6000);
                	}
                	
                	mPendingCount++;
                }
                
                
                mDLStates[mId]= DL_STATE.DLED;
                mProgs[mId]= 100;

                updateDb(url);
            }  
            
            removeTaskFromCache(url);
        }


		private void showErrorMsg() {

			String msg = null;
			switch( mLastErr ){
			default:
			case 0:		// no error
				break;
			case 1:		// open file fail
				break;
			case 2:		// response not OK
				break;
			case 3:		// server response error msg
				//mDLErrMsg
				msg = mDLErrMsg;
				break;
			case 4:		// disk not enough space
				//R.string.iii_download_no_space
				msg = mCtx.get().getResources().getString(R.string.iii_download_no_space);
				break;
			case 5:		// user cancel
				break;
			case 6:		// receive data error 
				break;
			case 7:		// incorrect url
				break;
			case 8:		// unknow err
				//R.string.iii_server_SocketTimeout
				break;
			case 9:
				//R.string.iii_server_SocketTimeout
				msg = mCtx.get().getResources().getString(R.string.iii_server_SocketTimeout);
				break;
			case 10:
				//R.string.iii_server_SocketException
				msg = mCtx.get().getResources().getString(R.string.iii_server_SocketException);
				break;
				
			}
			//110414 add for login dialog before download
			if((mDLErrMsg != null) && 
			   (mDLErrMsg.equals("非台灣大哥大行動網路連線或使用錯誤APN")||mDLErrMsg.equals("目前僅提供離線閱讀!請使用台灣大哥大網!"))){
				LoginDialogController aController = new LoginDialogController();
				iInfo.setMsg(mDLErrMsg);
				aController.ShowLoginDialog(mCtx.get(), iLoginObs, iInfo);
			}else{
				if ( null != msg && msg.length() > 0){
					Toast.makeText(mCtx.get().getApplicationContext(), msg,  Toast.LENGTH_SHORT).show(); 
				}
			}
			
			if(DEBUG) Log.e(TAG , "showErrorMsg type:"+ mLastErr + " msg:"+msg);
		}
    }
    public static class LoginInfo{
    	String iMsg;
    	public void setMsg(String aMsg){iMsg = aMsg;}
    	String url,type;
    	View bookInfoView;
    	int index;
    	public void setInfo(String _url , String _type, View _bookInfoView, int _index){
    		url = _url;
    		type = _type;
    		bookInfoView = _bookInfoView;
    		index = _index;
    	}
    	
    }
    private LoginDialogObserver iLoginObs = new LoginDialogObserver(){

		@Override
		public void LoginComplete(final LoginDialogController aController,
				Object aUserData, final int err) {
			final LoginInfo aInfo = (LoginInfo)aUserData;
			if(mCtx.get() instanceof Activity){
			final Activity a = mCtx.get();
			a.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					switch(err){
					case LoginDialogObserver.KErrNone:
						aController.DismissLoginDialog();
						//re-download
						forceDownload(aInfo.url,aInfo.type,aInfo.bookInfoView,aInfo.index);
						break;
					case LoginDialogObserver.KErrCancel:
						//show message
						Toast.makeText(mCtx.get().getApplicationContext(), aInfo.iMsg,  Toast.LENGTH_SHORT).show(); 
						break;
					}
				}
				
			});
			}else{
				Log.e("TWM","mCtx in BookDownloader need to be acitvity context");
			}
			
		}
    	
    };
    
    private void updateDb(String did ){
		if ( null != did && did.length() > 0  ){
			mDb.updateByDeliveryId(did , "isdownloadbook" , "1");
			
			int pos = mCursor.getPosition();
			mCursor.requery();
			mCursor.moveToPosition(pos);
		}
    }    
    
    
	public void setOutputData(DL_STATE[] dlStates, int[] progs) {
		mDLStates = dlStates;
		mProgs = progs;
		
	}

	public void cancelAll() {
		synchronized (sHardTaskCache) {
			Collection<BookDownloaderTask> tasks = sHardTaskCache.values();
			BookDownloaderTask [] arr = new BookDownloaderTask[tasks.size()] ;
			tasks.toArray(arr);
			
			for ( BookDownloaderTask task : arr ){
				task.cancel(true);
			}
			
			if ( null != mHandler){
				mHandler.removeMessages(PENDING_MSG);
			}
			
			
			for (int i = mDLStates.length-1 ; i>=0 ; --i ){
				mDLStates[i] = DL_STATE.NOACT;
			}
		}
	}
	private void threadHandleMsg(String msg) {
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		m.setData(data);
		mAuthHandler.sendMessage(m);
	}	
	private Handler mAuthHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Bundle bd = msg.getData();
			String desc = bd.getString("msg");
			//new AlertDialog.Builder(RealBookcase.getContext())
			Context icontext = mCtx.get();
			new AlertDialog.Builder(icontext)
			.setTitle(R.string.msgbox_expire_title)
			.setMessage(desc)//R.string.msgbox_expire_content)
			.setPositiveButton(R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//retrieve token
							
							RealBookcase.AuthSSOLogin((RealBookcase)mCtx.get(), RealBookcase.realbook, 
									mCtx.get().getApplicationContext(),	((RealBookcase)mCtx.get()).getHandler());
							
							
						}
					}
			)
			.setNegativeButton(R.string.iii_showAM_cancel,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//cancel
							//finish();

						}
				}
			)				
			.show();		
		}
	};		
}
