package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

import org.iii.ideas.reader.parser.HtmlSpan;

import android.content.Context;
import android.util.Log;

/**
 * 直書renderer
 * @author III
 * 
 */
public class VerticalRenderer2 extends MacroRenderer{
	
	/**
	 * constructor
	 * @param ev_ reader宣告的EpubView
	 * @param h 高
	 * @param w 寬
	 * @param rcb_ callback interface，此為主程式
	 * @param ctx_ context
	 * @param path epub path
	 * @param fontSizeIdx_ 第幾級字 
	 */
	public VerticalRenderer2(EpubView ev_,int h,int w,RendererCallback rcb_,Context ctx_,String path,int fontSizeIdx_){
		super(ev_,h,w,rcb_,ctx_,path,fontSizeIdx_);
	}
	
	public void forwardRender(final int tidx,final int pageNo){
		//final boolean localIsClosed=isClosed;
		new Thread(){
			public void run(){
				ArrayList<ReaderDrawable> contentToDraw = new ArrayList<ReaderDrawable>();
				try{
					curPageEndSpan = curPageStartSpan;
					curPageEndIdxInSpan = curPageStartIdxInSpan;
					//Log.d("VerticalRenderer:fowardRender","In");
					int curW=0; 
					int startIdx=curPageStartIdxInSpan;
					int i;
					for(i=curPageStartSpan;!receiver.isParsingFinished || (receiver.isParsingFinished&&i<receiver.content.size());i++){
						while(i>=receiver.content.size() && (!receiver.isParsingFinished) ){
								sleep(100);
						}
						if(i>=receiver.content.size())
							break;
						HtmlSpan span = receiver.content.get(i);
						//Log.d("i","is"+i+" content:"+span.content);
						if(receiver.content.get(i).type==HtmlSpan.TYPE_IMG){
							if(span.subtype==HtmlSpan.SUBTYPE_HR){
								HrContent hr = new HrContent(span, RendererConfig.toLeft+screenWidth-curW, RendererConfig.toTop, 0, 0, widthLeft(curW),
										heightLeft(), i,true,rcb.getDeliverId(),heightLeft(),widthLeft(0));
								if(hr.getWidth()>widthLeft(curW)){
									curPageEndSpan=i-1;
									curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
									break;
								}else{
									contentToDraw.add(hr);
									curW+=hr.getWidth();
								}
							}else{
								if(span.width<=0 || span.height<=0){
									BitmapAltContent alt = new BitmapAltContent(span, RendererConfig.toLeft+screenWidth-curW, RendererConfig.toTop, 0, 0, widthLeft(0),
											heightLeft(), i,getter.getFontSize(fontSizeIdx),ctx,rcb.getDeliverId(),true);
									if(alt.getWidth()>widthLeft(curW)){
										curPageEndSpan=i-1;
										curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
										break;
									}else{
										contentToDraw.add(alt);
										curW+=alt.getWidth();
										continue;
									}
								}
								int newH=0,newW=0;
								//int result[] = span.getImgSize(widthLeft(), heightLeft(0));
								//newW=result[0];newW=result[1];
								int result[] = resizeImg(i,span);
								newW=result[0];newH=result[1];
								float r = properRatio(((float)widthLeft(curW)/newW),((float)heightLeft()/newH));
								if(r>=minPercentage || curW==0){
									//int newH = (int) (span.height*r);
									//int newW = (int) (span.width*r);
									curW+=newW;
									//Log.d("x",":"+(RendererConfig.toLeft+screenWidth-curW));
									contentToDraw.add(DrawableGenerator.generateBitmap(i,span, RendererConfig.toLeft+screenWidth-curW, RendererConfig.toTop, newH, newW,heightLeft(),widthLeft(curW),true ) );
									
								}else{
									curPageEndSpan=i-1;
									curPageEndIdxInSpan=receiver.content.get(i-1).content.length()-1;
									break;
								}   
							}
						}else{
							VerticalLinedContent lc;
							if(span.type==HtmlSpan.TYPE_HEADER){
								//Log.d("in","header");
								lc = VerticalDrawableGenerator.generateVerticalText(ctx,i,span, RendererConfig.toLeft+screenWidth-curW, RendererConfig.toTop, RendererConfig.getVerticalHeaderFontSize(getter.getFontSize(fontSizeIdx), span.headerSize), heightLeft(), widthLeft(curW), 0, 0, RendererConfig.getVerticalLineSpace(fontSizeIdx), startIdx,rcb.getDeliverId());
							}else{
								//Log.d("in","normal");
								lc = VerticalDrawableGenerator.generateVerticalText(ctx,i,span, RendererConfig.toLeft+screenWidth-curW, RendererConfig.toTop, getter.getFontSize(fontSizeIdx), heightLeft(), widthLeft(curW), 0, 0, RendererConfig.getVerticalLineSpace(fontSizeIdx), startIdx,rcb.getDeliverId());
							}
							lc.setUnderline(ulHelper.getUnderlineDividedBySpan(epubPath, chapName, i));
							//Log.d("lc","coint:"+lc.getLineCount());
							if(lc.isEnd()){
								//Log.d("lc","finish");
								contentToDraw.add(lc);
								curW+=lc.getWidth();
							}else{
								//Log.d("not","finish");
								int eid=lc.getEndIdx();
								//Log.d("get","eid");
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
						

						startIdx=0;
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
				}finally{
					//Log.d("call","draw");
					//contentToDrawGlobal=contentToDraw;
					//thandler.sendMessage(thandler.obtainMessage(ACTION_LOAD_PAGE));
					if(tidx==renderThreadIdx && !isRendererClosed()){
						if(contentToDraw.size()>0){
							if(pageNo>=pages.size()){
								Page p = new Page(contentToDraw,curPageStartSpan,curPageStartIdxInSpan);
								p.setEnd(curPageEndSpan, curPageEndIdxInSpan);
								pages.add(p);
								curPageNo=pages.size()-1;
							}else{
								pages.get(pageNo).setContent(contentToDraw);
								curPageNo=pageNo;
							}
							//thandler.sendMessage(thandler.obtainMessage(ACTION_RECEIVE_PAGE));
						}
						thandler.sendMessage(thandler.obtainMessage(ACTION_RECEIVE_PAGE,tidx,curPageNo));
					}
				}
			}
		}.start();
		//end for
		/*Log.d("ForWardRender","Finished");
		Log.d("curPageStartSpan","is"+curPageStartSpan);
		Log.d("curPageEndSpan","is"+curPageEndSpan);
		Log.d("curPageStartIdxInSpan","is"+curPageStartIdxInSpan);
		Log.d("curPageEndIdxInSpan","is"+curPageEndIdxInSpan);*/
	}
	
	/**
	 * 調整圖片大小以便分頁
	 * @param i 第幾個span
	 * @param span an HtmlSpan object
	 * @return new int[]{圖片寬,圖片高}
	 */
	public int[] resizeImg(int i,HtmlSpan span){
		float r = properRatio(((float)widthLeft(0)/span.width),((float)heightLeft()/span.height));
		int w = (int) (span.width * r);
		int h = (int) (span.height * r);
		//receiver.content.remove(i);
		//receiver.content.add(i, span);
		return new int[]{w,h};
	}

	/**
	 * 計算分頁用method。取得剩餘寬度
	 * @param curW
	 * @return 剩餘寬
	 */
	private int widthLeft(final int curW){
		//Log.d("WidthLeft",""+(screenWidth-curW-margin));
		int width = ev.getWidth()-RendererConfig.wMargin-curW;
		if(width<0){
			Log.e("TWM","width incorrect!");
			width = 1;
		}
		return width;
	}
	
	/**
	 * 計算分頁用method。取得剩餘高度
	 * @return 剩餘高
	 */
	private int heightLeft(){
		//Log.d("WidthLeft",""+(screenWidth-curW-margin));
		int height = ev.getHeight()-RendererConfig.hMargin;
		if(height<0){
			Log.e("TWM","height incorrect");
			height = 1;
		}
		return height;
	}
	
	@Override
	public boolean isVertical(){
		return true;
	}
}
