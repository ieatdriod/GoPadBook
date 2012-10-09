package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

import org.iii.ideas.reader.PartialUnzipper;
import org.iii.ideas.reader.annotation.Annotation;
import org.iii.ideas.reader.annotation.AnnotationDB;
import org.iii.ideas.reader.bookmark.Bookmark;
import org.iii.ideas.reader.bookmark.Bookmarks;
import org.iii.ideas.reader.parser.HtmlReceiver;
import org.iii.ideas.reader.parser.HtmlSpan;
import org.iii.ideas.reader.turner.PageTurner;
import org.iii.ideas.reader.turner.PageTurnerCallback;
import org.iii.ideas.reader.turner.Rotater;
import org.iii.ideas.reader.turner.Slider;
import org.iii.ideas.reader.underline.Underline;
import org.iii.ideas.reader.underline.UnderlineDB;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.ReaderSettingGetter;

/**
 * 負責閱讀資料管理, 計算分頁, 呈現內容，主要是巨觀層次的layout(如何把一個個區塊組合成一頁)。 
 * @author III
 * 
 */
public class MacroRenderer implements PageTurnerCallback{
	/*
	 * JP, 2011-01-05, 修改br支援
	 */
	protected EpubView ev;
	protected int screenHeight;
	protected int screenWidth, fontSizeIdx, totalH, totalW;
	protected int curPageNo=0;
	//private int startIdx=0;
	//protected ArrayList<HtmlSpan> content;
	protected final float minPercentage = 1f; 
	//private final int statusBarHeight=50;  
	protected Bookmarks bmHelper;
	protected AnnotationDB annHelper;
	protected UnderlineDB ulHelper;
	protected RendererCallback rcb;
	protected String tests="";
	protected int curPageStartSpan=0,curPageStartIdxInSpan=0,curPageEndSpan=0,curPageEndIdxInSpan=0,textCount=0;
	protected int renderStartSpan,renderStartIdx;
	//protected boolean isParsingFinished=false;
	//protected final int ACTION_LOAD_PAGE=0;
	protected final int ACTION_CALL_RENDER=1;
	protected final int ACTION_RECEIVE_PAGE=2;
	protected ThreadHandler thandler=new ThreadHandler(); 
	protected String chapName; //file name
	//protected String chapTitle;
	protected String epubPath;
	//protected int parseThreadIdx=0;
	protected int renderThreadIdx;
	protected ArrayList<Page> pages;
	//protected ArrayList<ReaderDrawable> contentToDrawGlobal;
	protected Context ctx;
	protected int isPageUp=PageTurner.RELOAD;
	protected ReaderSettingGetter getter;
	protected boolean isRendererClosed;
	protected HtmlReceiver receiver;
	/**
	 * renderer id，當螢幕方向旋轉時renderer可能會重新啟動。
	 */
	public static int globalRendererId=0;
	private int rendererId;
	/**
	 * constructor
	 * @param ev_ epub view
	 * @param h 螢幕高
	 * @param w 螢幕寬
	 * @param rcb_ renderer和main activity溝通管道
	 * @param ctx_ context
	 * @param path epub path
	 * @param fontSizeIdx_ 使用者設定文字大小
	 */
	public MacroRenderer(EpubView ev_,int h,int w,RendererCallback rcb_,Context ctx_,String path,int fontSizeIdx_){
		globalRendererId++;
		rendererId=globalRendererId;
		Log.d("new","renderer:"+rendererId);
		setRendererStatus(false);
		epubPath=path;
		ev=ev_;
		//chapTitle="";
		rcb=rcb_;
		ctx=ctx_;
		totalH=h;
		totalW=w;
		bmHelper = new Bookmarks(ctx);
		annHelper = new AnnotationDB(ctx);
		ulHelper = new UnderlineDB(ctx);
		pages = new ArrayList<Page>();
		//turner = new PageTurner(ev,totalH,totalW,this);
		//turner = new Slider(ev,totalH,totalW,this);
		ev.setTurner(new Rotater(totalH,totalW,this));
		screenHeight=h-RendererConfig.hMargin;
		screenWidth=w-RendererConfig.wMargin;
		fontSizeIdx=fontSizeIdx_;  
		getter= new ReaderSettingGetter(ctx);
		//Log.d("VTextRenderer:Constructor",pageIndex.toString());
	}
	
	public void resetScreenSize(int h,int w){
		totalH=h;
		totalW=w;
		screenHeight=h-RendererConfig.hMargin;
		screenWidth=w-RendererConfig.wMargin;
	}
	
	/**
	 * 當前頁面起始span
	 * @return 起始span
	 */
	public int getCurPageStartSpan(){
		return curPageStartSpan;
	}
	
	/**
	 * 當前頁面起始字為span中第幾個字
	 * @return 當前頁面起始字為span中第幾個字
	 */
	public int getCurPageStartIdxInSpan(){
		return curPageStartIdxInSpan;
	}
	
	/**
	 * 當前頁面終點span
	 * @return 終點span
	 */
	public int getCurPageEndSpan(){
		return curPageEndSpan;
	}
	
	/**
	 * 當前頁面末字位於span中第幾個字
	 * @return 當前頁面末字位於span中第幾個字
	 */
	public int getCurPageEndIdxInSpan(){
		return curPageEndIdxInSpan;
	}
	


	/**
	 *  離開閱讀時清除資料
	 */
	private void clearData(){
		setRendererStatus(true);
		Log.d("closeDb","id:"+rendererId);
		//Log.d("close","db");
		renderThreadIdx++;
		if(ev!=null){
			ev.clearData();
			ev=null;
		}
		pages.clear();
		bmHelper.closeDB();
		ulHelper.closeDB();
		annHelper.closeDB();
	}
	
	/**
	 * 離開閱讀
	 */
	public void leaveReading(){
		renderThreadIdx++;
		clearData();
	}
	
	/**
	 * 開始計算呈現
	 */
	public void render(){
		if(ev.getWidth()==0||ev.getHeight()==0){
			Log.e("TWM","size=0 skip render");
			return;
		}
		try {
			//Log.d("render","render");
			final int localRenderThreadIdx = renderThreadIdx;
			renderStartSpan = curPageStartSpan;
			renderStartIdx = curPageStartIdxInSpan;
			//Log.d("renderSpan",":"+renderStartSpan);
			//Log.d("renderIdx",":"+renderStartIdx);
			if (pages.size() == 0 || renderStartSpan < 0) {
				curPageStartSpan = 0;
				curPageStartIdxInSpan = 0;
				forwardRender(localRenderThreadIdx, pages.size());
			} else {
				int pageIdx = -1;
				for (int i = 0; i < pages.size(); i++) {
					if (pages.get(i).endSpan > renderStartSpan
							|| (pages.get(i).endSpan == renderStartSpan && pages
									.get(i).endIdx >= renderStartIdx)) {
						pageIdx = i;
						break;
					}
				}
				if (pageIdx >= 0) {
					pages.get(curPageNo).clearContent();
					curPageNo = pageIdx;
					loadPage();
				} else {
					curPageEndSpan = pages.get(pages.size() - 1).endSpan;
					curPageEndIdxInSpan = pages.get(pages.size() - 1).endIdx;
					pages.get(pages.size() - 1).clearContent();
					if (receiver.content.get(curPageEndSpan).type == HtmlSpan.TYPE_IMG) {
						curPageStartSpan = curPageEndSpan + 1;
						curPageStartIdxInSpan = 0;
					} else if (curPageEndIdxInSpan < receiver.content
							.get(curPageEndSpan).content.length() - 1) {
						curPageStartSpan = curPageEndSpan;
						curPageStartIdxInSpan = curPageEndIdxInSpan + 1;
					} else {
						curPageStartSpan = curPageEndSpan + 1;
						curPageStartIdxInSpan = 0;
					}
					forwardRender(localRenderThreadIdx, pages.size());
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.e("page","size:"+pages.size());
			Log.e("content","size:"+receiver.content.size());
		} 
	}
	
	private void onReceivePage(final int tidx,final int pageNo){
		try {
			//Log.d("on","receive");
			//if(pages.size()==0)
			//	Log.d("on","receive size:0");
			//else 
			//	Log.d("on","endSpan:"+pages.get(pages.size()-1).endSpan);
			
			if(tidx!=renderThreadIdx || isRendererClosed() || receiver.content.size()==0){
				
			}if( renderStartSpan>=0 &&
					( curPageEndSpan>renderStartSpan 
					|| (curPageEndSpan==renderStartSpan && curPageEndIdxInSpan>=renderStartIdx)
					)
				){
				curPageNo=pageNo;
				loadPage();
			}else if( receiver.isParsingFinished && curPageEndSpan==receiver.content.size()-1 && 
					(curPageEndIdxInSpan >= receiver.content.get(receiver.content.size()-1).content.length()-1
						||	receiver.content.get(curPageEndSpan).type==HtmlSpan.TYPE_IMG)
					){
				curPageNo=pageNo;
				loadPage();
			}else{
				if(receiver.content.get(curPageEndSpan).type==HtmlSpan.TYPE_IMG){
					curPageStartSpan=curPageEndSpan+1;
					curPageStartIdxInSpan=0;
				}else if(curPageEndIdxInSpan<receiver.content.get(curPageEndSpan).content.length()-1){
					curPageStartSpan=curPageEndSpan;
					curPageStartIdxInSpan=curPageEndIdxInSpan+1;
				}else{
					curPageStartSpan=curPageEndSpan+1;
					curPageStartIdxInSpan=0;
				}
				if(pageNo<pages.size())
					pages.get(pageNo).clearContent();
				forwardRender(tidx,pages.size());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadPage(){
		try {
			//Log.d("loadPage","in");
			if(isRendererClosed()){
			}if(curPageNo<pages.size() && curPageNo>=0){
				//Log.d("loadPage","load");
				renderStartSpan = curPageStartSpan = pages.get(curPageNo).startSpan;
				renderStartIdx = curPageStartIdxInSpan = pages.get(curPageNo).startIdx;
				curPageEndSpan = pages.get(curPageNo).endSpan;
				curPageEndIdxInSpan = pages.get(curPageNo).endIdx;
				if(pages.get(curPageNo).isContentNull()){
					forwardRender(renderThreadIdx,curPageNo);
				}else{
					ArrayList<ReaderDrawable> draw=pages.get(curPageNo).getContent(); 
					for(int i=0;i<draw.size();i++){
						try{
							if(draw.get(i).isLinedContent()==LinedContent.LINED_CONTENT){
								((LinedContent)draw.get(i)).setUnderline(ulHelper.getUnderlineDividedBySpan(epubPath, chapName, ((LinedContent)draw.get(i)).getSpanIdx()));
							}else if(draw.get(i).isLinedContent()==VerticalLinedContent.VERTICAL_CONTENT){
								((VerticalLinedContent)draw.get(i)).setUnderline(ulHelper.getUnderlineDividedBySpan(epubPath, chapName, ((VerticalLinedContent)draw.get(i)).getSpanIdx()));
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					if(ev!=null)
						ev.drawContent(pages.get(curPageNo).getContent(), isPageUp, isVertical(), isCurPageBookmarked(), isCurPageAnnotated());
				}
			}else{
				curPageNo=0;
				curPageStartSpan = 0;
				curPageStartIdxInSpan = 0;
				renderStartSpan = 0;
				renderStartIdx = 0;
				curPageEndSpan = 0;
				curPageEndIdxInSpan = 0;
				if(ev!=null)
					ev.drawContent(null, isPageUp,isVertical(), isCurPageBookmarked(), isCurPageAnnotated());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	/**
	 * 正向計算頁面內容，每滿一頁即回傳。
	 * @param tidx 執行緒編號，用來判斷執行緒該不該停止
	 * @param pageNo 將產生的頁面存在第幾頁
	 */
	public void forwardRender(final int tidx,final int pageNo){
		new Thread(){
			public void run(){
				final 
				ArrayList<ReaderDrawable> contentToDraw = new ArrayList<ReaderDrawable>();
				try{
					curPageEndSpan = curPageStartSpan;
					curPageEndIdxInSpan = curPageStartIdxInSpan;
					int curH=0; 
					int startIdx=curPageStartIdxInSpan;
					int i;
					//if(receiver==null)
						//Log.d("r","is null");
					for(i=curPageStartSpan;!receiver.isParsingFinished || (receiver.isParsingFinished&&i<receiver.content.size());startIdx=0,i++){
						while(i>=receiver.content.size() && (!receiver.isParsingFinished) && tidx==renderThreadIdx && !isRendererClosed()){
								sleep(100);
								//Log.d("a","a");
						}
						if(i>=receiver.content.size())
							break;
						HtmlSpan span = receiver.content.get(i);
						//JP, 處理br空行, 2011.01.05 
						if(curH>0 || i==0 ){
							curH += span.brCount*getter.getFontSize(fontSizeIdx);
						}
						if(span.type==HtmlSpan.TYPE_IMG){
							if(span.subtype==HtmlSpan.SUBTYPE_HR){
								HrContent hr = new HrContent(span, RendererConfig.toLeft, curH+RendererConfig.toTop, 0, 0, widthLeft(),
										heightLeft(curH), i,false,rcb.getDeliverId(),heightLeft(0),widthLeft());
								if(hr.getHeight()>heightLeft(curH)){
									curPageEndSpan=i-1;
									curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
									break;
								}else{
									contentToDraw.add(hr);
									curH+=hr.getHeight();
								}
							}else{
								if(span.width<=0 || span.height<=0){
									BitmapAltContent alt = new BitmapAltContent(span, RendererConfig.toLeft, curH+RendererConfig.toTop, 0, 0, widthLeft(),
											heightLeft(0), i,getter.getFontSize(fontSizeIdx),ctx,rcb.getDeliverId(),false);
									if(alt.getHeight()>heightLeft(curH)){
										curPageEndSpan=i-1;
										curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
										break;
									}else{
										contentToDraw.add(alt);
										curH+=alt.getHeight();
										continue;
									}
								}
								int newH=0,newW=0;
								int result[] = span.getImgSize(widthLeft(), heightLeft(0));
								newW=result[0];newH=result[1];
								//Log.d("newH0:"+newH,"newW0:"+newW);
								result = resizeImg(i,span,newW,newH);
								newW=result[0];newH=result[1];
								//Log.d("newH:"+newH,"newW:"+newW);
								float r = properRatio(((float)widthLeft()/newW),((float)heightLeft(curH)/newH));
								if(r>=minPercentage || curH==0){
									//newH = (int) (span.height*r);
									//newW = (int) (span.width*r);
									BitmapContent bmpContent = DrawableGenerator.generateBitmap(i,span, RendererConfig.toLeft, curH+RendererConfig.toTop, newH, newW,heightLeft(curH),widthLeft(),false);
									if(bmpContent==null)
										continue;
									contentToDraw.add(bmpContent);
									curH+=newH;
								}else{
									curPageEndSpan=i-1;
									curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
									break;
								}   
							}
						}else{
							LinedContent lc = DrawableGenerator.generateText(ctx,i,span, RendererConfig.toLeft, curH+RendererConfig.toTop, getter.getFontSize(fontSizeIdx), heightLeft(curH), widthLeft(), 0, 0, startIdx,rcb.getDeliverId());
							if(lc==null)
								continue;
							lc.setUnderline(ulHelper.getUnderlineDividedBySpan(epubPath, chapName, i));
							//Log.d("lc","coint:"+lc.getLineCount());
							if(lc.isEnd()){
								//Log.d("lc","finish");
								contentToDraw.add(lc);
								curH+=lc.getHeight();
							}else{
								//Log.d("not","finish");
								int eid=lc.getEndIdx();
								//Log.d("eid","is:"+eid);
								if(eid>=0){
									contentToDraw.add(lc);
									curPageEndSpan=i;
									curPageEndIdxInSpan=eid;
									break;
								}else{
									curPageEndSpan=i-1;
									curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
									break;
								}
							}
						}
					}
					if(i==receiver.content.size()){
						if(i==0){
							curPageEndSpan=curPageEndIdxInSpan=0;
						}else{
							curPageEndSpan=i-1;
							curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
						}
					}
				}catch(Exception e){
					Log.e("MacroRender:forwardRender",e.toString());
					e.printStackTrace();
				}finally{
					//Log.d("render","finally");
					//Log.d("call","draw");
					//Log.d("endSpan","is:"+curPageEndSpan);
					//Log.d("endIdxInSpan","is:"+curPageEndIdxInSpan);
					//Log.d("forward","add Content size:"+contentToDraw.size());
					//Log.d("Render",":"+renderThreadIdx);
					//Log.d("thread",":"+tidx);
					//Log.d("isClosed",":"+isRendererClosed());
					if(tidx==renderThreadIdx && !isRendererClosed()){
						//Log.d("ef","gh");
						if(contentToDraw.size()>0){
							if(pageNo>=pages.size()){
								Page p = new Page(contentToDraw,curPageStartSpan,curPageStartIdxInSpan);
								p.setEnd(curPageEndSpan, curPageEndIdxInSpan);
								pages.add(p);
								curPageNo=pages.size()-1;
							}else{
								/*#2605 Chris update curpage curPageEndSpan curPageEndIdxInSpan */
								pages.get(pageNo).setEnd(curPageEndSpan, curPageEndIdxInSpan);
								pages.get(pageNo).setContent(contentToDraw);
								curPageNo=pageNo;
							}
							//thandler.sendMessage(thandler.obtainMessage(ACTION_RECEIVE_PAGE));
						}
						thandler.sendMessage(thandler.obtainMessage(ACTION_RECEIVE_PAGE,tidx,curPageNo));
					}
					//thandler.sendMessage(thandler.obtainMessage(ACTION_LOAD_PAGE));
				}
			}
		}.start();
	}
	
	/**
	 * 根據圖片原始大小和欲呈現大小調整圖片寬高。
	 * @param i 原本為圖片span編號，現在未使用
	 * @param span 圖片span
	 * @param newW 欲呈現寬
	 * @param newH 欲呈現高
	 * @return int[]{width, height}，調整後寬高
	 */
	public int[] resizeImg(int i,HtmlSpan span,int newW,int newH){
		float r = properRatio(((float)widthLeft()/newW),((float)heightLeft(0)/newH));
		int w = (int) (newW * r);
		int h = (int) (newH * r);
		//receiver.content.remove(i);
		//receiver.content.add(i, span);
		return new int[]{w,h};
	}
	
	/**
	 * 根據id刪除註記
	 * @param id id
	 */
	public void deleteAnnById(int id){
		annHelper.deleteAnnById(id);
	}	
	
	/**
	 * 能否上一頁，若非章節開頭則可
	 * @return 能否上一頁
	 */
	public boolean canPageUp(){
		isPageUp=PageTurner.PAGE_UP;
		if(curPageNo>0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 能否下一頁，若非章節結尾則可
	 * @return 能否下一頁
	 */
	public boolean canPageDown(){
		//Log.d("can","pageDown");
		isPageUp=PageTurner.PAGE_DOWN;
		if(receiver.content.size()==0 || curPageEndSpan >= receiver.content.size() ){
			return false;
		}else if(curPageEndSpan<receiver.content.size()-1 ){
			//Log.d("curPageEndSpan:"+curPageEndSpan,"size:"+content.size());
			return true;
		}else if( receiver.content.get(curPageEndSpan).type!=HtmlSpan.TYPE_IMG&& curPageEndIdxInSpan<receiver.content.get(curPageEndSpan).content.length()-1 ){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 移到上一頁
	 */
	public void pageUp(){
		//Log.d("VTR","pageUp");
		isPageUp=PageTurner.PAGE_UP;
		if(canPageUp()){
			/*#2605 Chris remove curpage*/
			pages.remove(curPageNo);
			curPageNo--;  
			loadPage();
		}
	}
	
	/**
	 * 移到下一頁
	 */
	public void pageDown(){
		isPageUp=PageTurner.PAGE_DOWN;
		//Log.d("VTR","pageDown");
		if(canPageDown()){
			//curPageNo++;
			if(receiver.content.get(curPageEndSpan).type==HtmlSpan.TYPE_IMG){
				curPageStartSpan=curPageEndSpan+1;
				curPageStartIdxInSpan=0;
			}else if(curPageEndIdxInSpan<receiver.content.get(curPageEndSpan).content.length()-1){
				curPageStartSpan=curPageEndSpan;
				curPageStartIdxInSpan=curPageEndIdxInSpan+1;
			}else{
				curPageStartSpan=curPageEndSpan+1;
				curPageStartIdxInSpan=0;
			}
			render();
		}	
	}
	
	
	private int widthLeft(){
		//Log.d("WidthLeft",""+(screenWidth-curW-margin));
		int width = ev.getWidth()-RendererConfig.wMargin;
		if(width < 0){
			Log.e("TWM","width incorrect");
			width = 1;
		}
		return width;
	}
	
	private int heightLeft(final int curH){
		//Log.d("WidthLeft",""+(screenWidth-curW-margin));
		int height = ev.getHeight()-RendererConfig.hMargin-curH;
		if(height<0){
			Log.e("TWM","height incorrect");
			height = 1;
		}
		return height;
	}
	
	float properRatio(final float a,final float b){
		//Log.d("ProperRatio","a:"+a+" b:"+b);
		return Math.min(a, b)<1?Math.min(a, b):1;
	}
	
	/**
	 * 插入註記
	 * @param title 書名
	 * @param chapName 章節相對路徑
	 * @param epubPath epub path 
	 * @param input 註記內容
	 * @param span span
	 * @param idx index
	 */
	public void insertAnn(String title, String chapName, String epubPath,String input,int span,int idx){
		Log.d("HTextRenderer:insertAnn","in");
		try{
			if(span<receiver.content.size()){
				Annotation bm = new Annotation();
				bm.bookName=title;
				bm.position1=span;
				bm.position2=idx;
				bm.chapterName=chapName;
				bm.content=input;
				/*if(content.get(span).content.substring(idx).length()>=20){
					bm.description = content.get(span).content.substring(idx,idx+20);
				}else{
					bm.description = content.get(span).content.substring(idx);
//				}*/
				bm.description = getDescription(span,idx,curPageEndSpan,curPageEndIdxInSpan);
				bm.epubPath=epubPath;
				annHelper.insertAnn(bm);
				//Log.d("DBCount","is:"+bmHelper.getCount());
			}
		}catch(Exception e){
			Log.e("MacroRenderer:insertAnn",e.toString());
		}
		
	}
	
	/**
	 * 插入書籤
	 * @param title 書名
	 * @param chapName 章節相對路徑
	 * @param epubPath epub path
	 */
	public void insertBookmark(String title, String chapName, String epubPath){
		try{
			if(!isCurPageBookmarked() && receiver.content.size()>0){
				Log.d("HTextRenderer:insertBookmark","in");
				Bookmark bm = new Bookmark();
				bm.bookName=title;
				bm.chapterName=chapName;
				//int start= (curPageStartIdxInSpan>=0)?curPageStartIdxInSpan:0;
				/*if(content.get(curPageStartSpan).content.substring(start).length()>=20){
					bm.description = content.get(curPageStartSpan).content.substring(start,start+20);
				}else{
					bm.description = content.get(curPageStartSpan).content.substring(start);
				}*/
				bm.description = getDescription(curPageStartSpan,curPageStartIdxInSpan,curPageEndSpan,curPageEndIdxInSpan);
				bm.epubPath=epubPath;
				bm.position1=curPageStartSpan;
				bm.position2=curPageStartIdxInSpan;
				bmHelper.insertBookmark(bm);
				//Log.d("DBCount","is:"+bmHelper.getCount());
			}
		}catch(Exception e){
			Log.e("MacroRenderer:insertBookmark",e.toString());
		}
	}
	
	/**
	 * 刪除當前頁面書籤
	 * @param title 書名
	 * @param chapName 章節相對路徑
	 * @param epubPath epub path
	 */
	public void deleteCurPageBookmark(String title, String chapName, String epubPath){
		bmHelper.deleteCurPageBookmark(epubPath, chapName, curPageStartSpan, curPageStartIdxInSpan, curPageEndSpan, curPageEndIdxInSpan);
	}
	

	/**
	 * 當前頁面是否有插入書籤
	 * @return
	 */
	public boolean isCurPageBookmarked(){
		return bmHelper.isCurPageBookmarked(epubPath,chapName,curPageStartSpan, curPageStartIdxInSpan,curPageEndSpan, curPageEndIdxInSpan );
	}
	
	/**
	 * 本頁是否有文字(可畫線)
	 * @return 本頁是否有文字
	 */
	public boolean textExistsOnCurPage(){
		try {
			for (int i = curPageStartSpan; i <= curPageEndSpan; i++) {
				if (receiver.content.get(i).type != HtmlSpan.TYPE_IMG && receiver.content.get(i).content.length()>0) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		
	}
	
	/**
	 * 調整字體大小  
	 * @param fs 字體大小index，參照strings.xml的iii_reader_setting_font_size
	 * @return 是否成功
	 */
	public boolean changeFontSize(int fs){
		if(fs!=fontSizeIdx){
			fontSizeIdx=fs;
		pages=null;
		pages=new ArrayList<Page>();
		//render();
		return true;
		}
		return false;
	}
	public int getFontSize(){
		return fontSizeIdx;
	}
	

	/**
	 * 重新呈現當前頁面
	 */
	public void reload(){
		isPageUp=PageTurner.RELOAD;
		render();
	}
	
    class ThreadHandler extends Handler{
    	public void handleMessage(Message msg) {
    		switch( msg.what ){
    			/*case ACTION_LOAD_PAGE:		
    				//ev.drawContent(null);
    				//turner.apply(isPageUp);
    				ev.drawContent(contentToDrawGlobal,isPageUp);
    				break;   
    			case ACTION_CALL_FORWARD_RENDER:		
    				forwardRender();	
    				break;   */
    			case ACTION_RECEIVE_PAGE:
    				onReceivePage(msg.arg1,msg.arg2);
    				break;
    			case ACTION_CALL_RENDER:
    				render();
    				break;
    		} 
        }
    	
    }
    
	/**
	 * 根據百分比讀取章節
	 * @param r parser結果的接收container
	 * @param per percentage, 0~100
	 * @param uz unzipper
	 * @param chapName_ 章節相對路徑
	 */
	public void loadChapterByPercentage(HtmlReceiver r, int per, PartialUnzipper uz, String chapName_) {
		// TODO Auto-generated method stub
		//Log.e("In loadChapterByPercentage", String.valueOf(System.currentTimeMillis()));
		//Log.d("loadChapter","isClosed:"+isRendererClosed());
		Log.d("load","renderer:"+rendererId);
		renderThreadIdx++;
		pages.clear();
		receiver=r;
		chapName = chapName_;
		curPageStartSpan=per;
		curPageStartIdxInSpan=0;
		render();
	}
	
	/**
	 * 根據目前定位重讀章節
	 * @param r parser結果的接收container
	 * @param uz unzipper
	 * @param chapName_ 章節相對路徑
	 */
	public void reloadChapter(HtmlReceiver r, PartialUnzipper uz, String chapName_) {
		// TODO Auto-generated method stub
		//Log.e("In loadChapterByPercentage", String.valueOf(System.currentTimeMillis()));
		Log.d("reload","chapter");
		renderThreadIdx++;
		pages.clear();
		receiver=r;
		chapName = chapName_;
		curPageStartSpan=renderStartSpan;
		curPageStartIdxInSpan=renderStartIdx;
		render();
	}

	
	/**
	 * 根據span和idx讀取章節
	 * @param r parser結果的接收container
	 * @param startSpan span
	 * @param startIdx idx
	 * @param uz unzipper
	 * @param chapName_ 章節相對路徑
	 */
	public void loadChapterBySpanAndIdx(HtmlReceiver r, int startSpan,
			int startIdx, PartialUnzipper uz, String chapName_) {
		// TODO Auto-generated method stub
		//Log.d("loadChapter","isClosed:"+isRendererClosed());
		Log.d("load","renderer:"+rendererId);
		renderThreadIdx++;
		pages.clear();
		chapName = chapName_;
		//content = HTextContentReader.getContent(targetURL,uz);
		receiver=r;
		//Log.d("HTextParsingHandler","Finished");
		curPageStartSpan=startSpan;curPageStartIdxInSpan=startIdx;
		render();
	}

	


	/**
	 * 目前於章節中的百分比
	 * @return 目前於章節中的百分比
	 */
	public int getPercentageInChapter() {
		// TODO Auto-generated method stub
		if(receiver.content==null || receiver.content.size()==0)
			return 0;
		else
			return (curPageStartSpan+1)*100/receiver.content.size();
	}



	/**
	 * 章節名稱 ，即html檔中的<title>ChapterTitle</title>
	 * @return 章節名稱
	 */
	public String getChapTitle() {
		// TODO Auto-generated method stub
		return receiver.chapTitle;
	}

	/**
	 * 根據起訖span和idx取得文字描述(用於劃線)
	 * @param span1 起點span
	 * @param idx1 起點index
	 * @param span2 終點span
	 * @param idx2 終點index
	 * @return description
	 */
	public String getUnderlineDescription(int span1, int idx1, int span2, int idx2){
		StringBuilder builder = new StringBuilder();
		try {
			int startIdx=idx1;
			for(int i=span1;i<=span2;i++,startIdx=0){
				if(receiver.content.get(i).type==HtmlSpan.TYPE_IMG)
					continue;
				if(i<span2){
					builder.append(receiver.content.get(i).content.substring(startIdx));
				}else{
					builder.append(receiver.content.get(i).content.substring(startIdx,idx2+1));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder.toString();
	}
	
	/**
	 * 新增劃線內容至DB
	 * @param title 書名
	 * @param chapName 章節名稱
	 * @param epubPath epub path
	 * @param span1 起點span
	 * @param idx1 起點index
	 * @param span2 終點span
	 * @param idx2 終點index
	 */
	public void insertUnderline(String title, String chapName, String epubPath,
			 int span1, int idx1, int span2, int idx2) {
		// TODO Auto-generated method stub
		try{
			if(span1<receiver.content.size() && span2<receiver.content.size()){
				Underline bm = new Underline();
				bm.bookName=title;
				bm.span1=span1;
				bm.idx1=idx1;
				
				bm.span2=span2;
				bm.idx2=idx2;
				bm.chapterName=chapName;
				bm.content="";
				//bm.description = content.get(i).content.substring(startIdx);
				bm.epubPath=epubPath;
				bm.description=getUnderlineDescription(span1, idx1, span2, idx2);
				/*int startIdx=idx1;
				for(int i=span1;i<=span2;i++,startIdx=0){
					if(content.get(i).type==HtmlSpan.TYPE_IMG)
						continue;
					if(i<span2){
						bm.description += content.get(i).content.substring(startIdx);
					}else{
						bm.description += content.get(i).content.substring(startIdx,idx2+1);
					}
				}*/
				ulHelper.insertUnderline(bm,this);
				reload();
			}
		}catch(Exception e){
			Log.e("MacroRenderer:insertUnderline",e.toString());
		}		
	}
	
	/**
	 * 根據起訖點取得當前頁面描述
	 * @param span1 起點span
	 * @param idx1 起點index
	 * @param span2 終點span
	 * @param idx2 終點index
	 * @return 當前頁面描述
	 */
	private String getDescription(int span1,int idx1,int span2,int idx2){
		StringBuilder desc=new StringBuilder();
		int startIdx=idx1;
		for(int i=span1;i<=span2;i++,startIdx=0){
			if(receiver.content.get(i).type!=HtmlSpan.TYPE_IMG){
				try {
					if(i<span2)
						desc.append(receiver.content.get(i).content.substring(startIdx));
					else
						desc.append(receiver.content.get(i).content.substring(startIdx,idx2+1));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				if(receiver.content.get(i).subtype==HtmlSpan.SUBTYPE_HR)
					desc.append(ctx.getString(R.string.iii_description_hr));
				else
					desc.append(ctx.getString(R.string.iii_description_img));
			}
			if(desc.length()>20){
				desc.setLength(20);
				desc.trimToSize();
				break;
			}
		}
		return desc.toString();
	}
	
	/**
	 * 當前頁面有無註記
	 * @return 有無註記
	 */
	public boolean isCurPageAnnotated() {
		// TODO Auto-generated method stub
		return annHelper.isCurPageAnnotated(epubPath,chapName,curPageStartSpan, curPageStartIdxInSpan,curPageEndSpan, curPageEndIdxInSpan );
	}

	/**
	 * 取得當前頁面註記id
	 * @return 註記id
	 */
	public int getCurPageAnnotation() {
		// TODO Auto-generated method stub
		return annHelper.getCurPageAnnotationId(epubPath,chapName,curPageStartSpan, curPageStartIdxInSpan,curPageEndSpan, curPageEndIdxInSpan );
	}
	
	/**
	 * 刪除當前頁面註記
	 * @param title 書名
	 * @param chapName 章節名稱
	 * @param epubPath epub path
	 */
	public void deleteCurPageAnnotation(String title, String chapName,
			String epubPath) { 
		// TODO Auto-generated method stub
		annHelper.deleteCurPageAnnotation(epubPath, chapName, curPageStartSpan, curPageStartIdxInSpan, curPageEndSpan, curPageEndIdxInSpan);
	}

	/**
	 * 設定翻頁特效模式，type值可參照PageTurner
	 * @param type 翻頁類型
	 */
	public void setTurningMethod(int type) {
		// TODO Auto-generated method stub
			 switch(type){
			 	case PageTurner.NO_EFFECT: 
			 		//turner=new PageTurner(ev,totalH,totalW,this);
			 		if(ev!=null)
			 			ev.setTurner(new PageTurner(totalH,totalW,this));
			 		break;
			 	case PageTurner.ROTATION: 
			 		//turner=new PageTurner(ev,totalH,totalW,this);
			 		//turner=new Rotater(ev,totalH,totalW,this);
			 		if(ev!=null)
			 			ev.setTurner(new Rotater(totalH,totalW,this));
			 		break;
			 	case PageTurner.SLIDE: 
			 		//turner=new PageTurner(ev,totalH,totalW,this);
			 		if(ev!=null)
			 			ev.setTurner(new Slider(totalH,totalW,this));
			 		break;
			 }
	}

	@Override
	public void onTurningFinished() {
		// TODO Auto-generated method stub
		//ev.drawContent(contentToDrawGlobal,isPageUp);
		rcb.onRenderingFinished();	
	}

	/**
	 * 當前頁面有無劃線
	 * @return 有無劃線
	 */
	public boolean isCurPageUnderlined() {
		// TODO Auto-generated method stub
		return ulHelper.isCurPageUnderlined(epubPath, chapName, curPageStartSpan, curPageStartIdxInSpan, curPageEndSpan, curPageEndIdxInSpan);
	}


	/**
	 * 根據某定位點取得相關劃線內容
	 * @param span span
	 * @param idx index
	 * @return 劃線內容
	 */
	public ArrayList<Underline> getUnderlineBySpanAndIdx(int span,int idx) {
		// TODO Auto-generated method stub
		return ulHelper.getUnderlineBySpanAndIdx(epubPath, chapName, span, idx);
	}
	
	/**
	 * 根據傳入物件刪除db中相關註記
	 * @param underlines 註記物件
	 */
	public void deleteUnderline(ArrayList<Underline> underlines) {
		// TODO Auto-generated method stub
		ulHelper.deleteUnderline(underlines);
	}
	
	/**
	 * 取得這本書書籤筆數
	 * @param epubPath epub path
	 * @return 書籤筆數
	 */
	public int getBookmarkCountOfBook(String epubPath){
		return bmHelper.getBookmarkCountByEpubPath(epubPath);
	}
	
	/**
	 * 取得這本書註記筆數
	 * @param epubPath epub path
	 * @return 註記筆數
	 */
	public int getAnnotationCountOfBook(String epubPath){
		return annHelper.getAnnsCountByEpubPath(epubPath);
	}
	
	/**
	 * 取得這本書畫線筆數
	 * @param epubPath epub path
	 * @return 畫線筆數
	 */
	public int getUnderlineCountOfBook(String epubPath){
		return ulHelper.getUnderlineCountByEpubPath(epubPath);
	}
	
	/**
	 * 是否為直書
	 * @return 是否為直書
	 */
	public boolean isVertical(){
		return false;
	}
	/**
	 * 呈現起點span
	 * @return 呈現起點span
	 */
	public int getRenderStartSpan(){
		return renderStartSpan;
	}
	/**
	 * 呈現起點idx
	 * @return 呈現起點idx
	 */
	public int getRenderStartIdx(){
		return renderStartIdx;
	}
	
	/**
	 * renderer是否關閉
	 * @return renderer是否關閉
	 */
	public boolean isRendererClosed(){
		return isRendererClosed;
	}
	
	/**
	 * 設定renderer狀態
	 * @param isClosed renderer是否關閉
	 */
	private void setRendererStatus(boolean isClosed){
		isRendererClosed=isClosed;
		//Log.d("rendererId:"+rendererId,"setClosed:"+isRendererClosed());
	}
}
