package org.iii.ideas.reader.renderer;


import org.iii.ideas.reader.parser.HtmlSpan;

import android.content.Context;
import android.util.Log;

/**
 * DrawableGenerator的直書版，將HtmlSpan轉為VerticalLinedContent
 * @author III
 * 
 */
public class VerticalDrawableGenerator extends DrawableGenerator{
	/**
	 * 產生分行的直書文字區塊
	 * @param ctx context
	 * @param spanIdx 張章節第幾個span
	 * @param span HtmlSpan
	 * @param x left
	 * @param y top
	 * @param fontSize 文字大小
	 * @param height 高
	 * @param width 寬
	 * @param wMargin 橫向margin
	 * @param hMargin 直向margin
	 * @param lineSpace 行高
	 * @param startIdx 從該span第幾個字開始計算
	 * @param deliverId
	 * @return 分行的直書文字區塊 
	 */
	public static VerticalLinedContent generateVerticalText(Context ctx,int spanIdx,HtmlSpan span, int x, int y, int fontSize,int height,
			int width,int wMargin,int hMargin,int lineSpace,int startIdx,String deliverId){
		if(startIdx>=span.content.length()){
			Log.e("DrawableGenerator:generateText","startIdx out of bound");
			return null;
		}
		//Log.d("!!!!!vertical","fontSize:"+fontSize);
		String s=span.content.substring(startIdx);
		width-=wMargin;
		height-=hMargin;
		VerticalLinedContent lc = new VerticalLinedContent(span,ctx,fontSize,lineSpace,startIdx,-1,x,y,height,width,spanIdx,deliverId);
		lc.setHyperlink(span.getLinks());
		final int lineWidth = fontSize+lineSpace;
		float curW=lineWidth;
		//start calculating
		if(curW>width){
			lc.setEndIdx(-1);
			return lc;
		}
		int charPerLine = height/(fontSize+RendererConfig.charSpace);
		int i,endOfLine=charPerLine;
		for(i=0,endOfLine=charPerLine;endOfLine<s.length();endOfLine+=charPerLine,i+=charPerLine){
			lc.addLine(s.substring(i, endOfLine), startIdx+endOfLine);
			curW+=lineWidth;
			if(curW>width){  
				lc.setEndIdx(startIdx+endOfLine-1);
				return lc;
			}
		} 
		lc.addLastLine(s.substring(i));
		lc.setEndIdx(startIdx+s.length()-1);
		lc.setEndFlag(true);
		
		return lc;
	}
	/**
	 * 目前未使用
	 * @param ctx
	 * @param spanIdx
	 * @param span
	 * @param x
	 * @param y
	 * @param fontSize
	 * @param height
	 * @param width
	 * @param wMargin
	 * @param hMargin
	 * @param lineSpace
	 * @param endIdx
	 * @param deliverId
	 * @return
	 */
	public static VerticalLinedContent generateVerticalTextBackward(Context ctx,int spanIdx,HtmlSpan span, int x, int y, int fontSize,
			int height,int width,int wMargin,int hMargin,int lineSpace,int endIdx,String deliverId){
		if(endIdx>span.content.length()){
			Log.e("DrawableGenerator:generateTextBackward","startIdx out of bound");
			return null;
		}
		if(endIdx<0)
			endIdx=span.content.length()-1;	
		String s=span.content.substring(0,endIdx+1);
		width-=wMargin;
		height-=hMargin;
		//Log.d("heightInGen:","is:"+height);
		VerticalLinedContent lc = new VerticalLinedContent(span,ctx,fontSize,lineSpace,0,endIdx,x,y,height,width,spanIdx,deliverId);
		lc.setHyperlink(span.getLinks());
		
		final int lineWidth = fontSize+lineSpace;
		float curW=lineWidth;
		//int lineCount=0;
		//start calculating
		if(curW>width){
			lc.setEndIdx(-1);
			return lc;
		}
		int charPerLine = height/(fontSize+RendererConfig.charSpace);
		int i,endOfLine=charPerLine;
		for(i=0,endOfLine=charPerLine;endOfLine<s.length();endOfLine+=charPerLine,i+=charPerLine){
			lc.addLine(s.substring(i, endOfLine), endOfLine);
		} 
		lc.addLastLine(s.substring(i));
		lc.setEndIdx(s.length()-1);
		lc.setEndFlag(true);
		
		int lineNo = width/lineWidth;
		//Log.d("lineNo","is:"+lineNo);
		if(lineNo==0){
			lc.setStartIdx(-1);
		}else if(lc.getLineCount()>lineNo){
			lc.trimFromStart(lc.getLineCount()-lineNo);
		}else{
			lc.setStartIdx(0);
		}
		
		return lc;
	}
	
}
