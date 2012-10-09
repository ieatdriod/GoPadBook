package org.iii.ideas.reader.parser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.iii.ideas.reader.PartialUnzipper;

import android.util.Log;
/**
 *  parse html時承接parse完的HtmlSpan物件的container
 * @author III
 *
 */
public class HtmlReceiver implements HtmlSpanReceiver {
	public String chapTitle;
	public ArrayList<HtmlSpan> content;
	protected int parseThreadIdx;
	public boolean isParsingFinished;
	//private PartialUnzipper uz;
	private WeakReference<ParseErrorHandler> errorhandler;
	/**
	 * 
	 * @param handler parse出錯時通知主程式的call back interface
	 */
	public HtmlReceiver(ParseErrorHandler handler){
		//uz=uz_;
		isParsingFinished=false;
		errorhandler=new WeakReference<ParseErrorHandler>(handler);
		chapTitle="";
		content = new ArrayList<HtmlSpan>();
		parseThreadIdx=0;
	}
	
	/**
	 * 設定error handler
	 * @param handler parse error handler
	 */
	public void setErrorHandler(ParseErrorHandler handler){
		errorhandler=new WeakReference<ParseErrorHandler>(handler);
	}
	
	/**
	 * parse是否完成
	 * @return parse是否完成
	 */
	public boolean isParsingFinished(){
		return isParsingFinished;
	}
	
	/**
	 * 開始parse某一html檔案
	 * @param targetUrl html file url
	 * @param uz unzipper 
	 */
	public void startParsing(final String targetUrl,final PartialUnzipper uz){
		Log.d("start","parsing");
		++parseThreadIdx;
		chapTitle="";
		if(content==null)
			content = new ArrayList<HtmlSpan>();
		else
			content.clear();
		isParsingFinished=false;
		//renderThreadIdx=parseThreadIdx;
		new Thread(){
			public void run(){
				try {
					HtmlContentReader.getContentByCallBack(targetUrl,uz,HtmlReceiver.this,parseThreadIdx);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e("MacroRenderer","setContentReceiver:"+e);
					if(errorhandler!=null){
						final ParseErrorHandler aHandler = errorhandler.get();
						if (null != aHandler)
							aHandler.onHtmlParseError();
					}
				}
			}
		}.start();
	}
	
	@Override
	public void clearContent() {
		// TODO Auto-generated method stub
		content.clear();
	}

	@Override
	public int getThreadIdx() {
		// TODO Auto-generated method stub
		return parseThreadIdx;
	}

	@Override
	public void onGetHtmlSpan(int idx, HtmlSpan span, int threadIdx) {
		// TODO Auto-generated method stub
		if(threadIdx==parseThreadIdx){
			if(idx==content.size()){
				content.add(span);
			}else{
				content.add(idx, span);
			}
		}
	}

	@Override
	public void onGetTitle(String title) {
		// TODO Auto-generated method stub
		chapTitle=title;
	}

	@Override
	public void onParsingFinished(int tid) {
		// TODO Auto-generated method stub
		//Log.d("onParsing","f0");
		if(tid==parseThreadIdx){
			//Log.d("onParsing","f1");
			isParsingFinished=true;
		}
	}

}
