package org.iii.ideas.reader.search;

import java.io.File;
import java.util.ArrayList;

import org.iii.ideas.reader.PartialUnzipper;

import com.gsimedia.sa.DeviceIDException;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * 關鍵字全文搜尋主程式，逐一解壓縮並搜尋過所有章節，建立搜尋結果索引
 * @author III
 * 
 */
public class KeywordSearcher {
	//private static final int SEARCH_CHAPTER=0;
	//private ThreadHandler handler = new ThreadHandler();
	private PartialUnzipper UZ;
	//private ArrayList<SearchResult> results;
	private String targetDir;
	private ArrayList<String> fileList;
	/**
	 * keyword
	 */
	public static String kw=null;
	private SearchCallback scb;
	private int startThreadIdx;
	private Handler handle = null;
	/**
	 * 搜尋關鍵字
	 * @param startThreadIdx_ 執行緒index，搜尋過程會檢查此一index判斷搜尋是否要繼續
	 * @param kw_ 關鍵字
	 * @param epubPath epub path
	 * @param fileList_ 檔案列表 (即從opf文件parse出來的spine list)
	 * @param ctx context
	 * @param scb_ call back物件
	 * @throws Exception  搜尋exception
	 */
	public void searchKW(int startThreadIdx_,String kw_, String epubPath, ArrayList<String> fileList_, Context ctx,SearchCallback scb_,Handler _handle) throws Exception{
		//Log.d("in","search");
		scb=scb_;
		startThreadIdx=startThreadIdx_;
		kw=kw_;
		handle = _handle;
		UZ = new PartialUnzipper(new File(epubPath),ctx);
		fileList = fileList_;
		try{
		UZ.setList();
		}catch(DeviceIDException e){
			handle.sendMessage(handle.obtainMessage(ACTION_SHOW_ERROR, 1, 1,e.getMessage()));
		}
		targetDir = UZ.getTargetDir();
		//results = new ArrayList<SearchResult>();
		if(fileList.size()>0)
			start();
	}
	/**
	 * 判斷是否該停止搜尋
	 * @return true: stop. false: continue
	 */
	private boolean shouldStop(){
		return startThreadIdx==scb.getThreadIdx()?false:true;
	}
	

	private void searchChapter(final int chapNo){
		ArrayList<SearchResult> result =SearchReader.getResults("file://"+targetDir+fileList.get(chapNo), UZ, kw, fileList.get(chapNo));
		if(result==null)
			Log.d("result","is_null");
		//results.addAll(result);
		if(!shouldStop())
			scb.onGetResults(result,(chapNo+1)*100/fileList.size());
		/*if(chapNo==fileList.size()-1){ 
			if(!shouldStop()){
				//scb.onGetResults(results);
				scb.onSearchFinished();
			}
		}else{
			if(!shouldStop())
				unzipChapter(chapNo+1);
		}*/
	}
	
	private static final int ACTION_SHOW_ERROR=0;
	private void start(){
		//Log.d("unzip0","chap:"+chapNo);
		new Thread(){
			public void run(){
				try{
					for(int i=0;i<fileList.size();i++){
						if(!shouldStop())
							UZ.unzipFile(targetDir+fileList.get(i),true);
						if(!shouldStop())
							searchChapter(i);
					}
					if(!shouldStop()){
						//scb.onGetResults(results);
						scb.onSearchFinished();
					}
					/*if(!shouldStop())
						UZ.unzipFile(targetDir+fileList.get(chapNo),true);
					if(!shouldStop())
						searchChapter(chapNo);*/
				}catch(DeviceIDException e){
					handle.sendMessage(handle.obtainMessage(ACTION_SHOW_ERROR, 1, 1,e.getMessage()));
    				
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					//if(!shouldStop())
						//handler.sendMessage(handler.obtainMessage(SEARCH_CHAPTER,chapNo,chapNo));
				}
			}
		}.start();
	}
	/*class ThreadHandler extends Handler{
    	public void handleMessage(Message msg) {
    		switch( msg.what ){
    			//case SEARCH_CHAPTER:
    				//searchChapter(msg.arg1);
    				//break;   
    		}
    	
    	}
	}*/
}
