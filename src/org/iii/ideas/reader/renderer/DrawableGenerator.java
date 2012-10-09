package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

import org.iii.ideas.reader.parser.HtmlSpan;
import org.iii.ideas.reader.parser.property.SpecialProperty;

import android.content.Context;
import android.text.TextPaint;
import android.util.Log;

/**
 * 將parse出來的物件轉換為實際上可畫的內容。Last Modification: 2011-01-04
 * @author III
 * 
 */
public class DrawableGenerator {
	/**
	 * 產生分行的文字內容
	 * @param ctx context
	 * @param spanIdx span編號
	 * @param span 來源span
	 * @param x left
	 * @param y top
	 * @param readerFontSize 使用者設定的文字大小，作為base
	 * @param height 高
	 * @param width 寬
	 * @param wMargin 橫向margin
	 * @param hMargin 縱向margin
	 * @param startIdx 從該span中第幾個字開始處理
	 * @param deliverId delivery id
	 * @return 分行的文字內容
	 */
	public static LinedContent generateText(Context ctx,int spanIdx,HtmlSpan span, int x, 
			int y, int readerFontSize,int height,int width,int wMargin,int hMargin,final int startIdx,String deliverId){
		if(startIdx>=span.content.length()){
			Log.e("DrawableGenerator:generateText","startIdx out of bound");
			return null;
		}
		//Log.e("JP","span:"+span.content.toString());
		if(span.property!=null){
			for(int i=0;i<span.property.size();i++){
				Log.e("JP","sp:"+span.property.get(i).start+" -- "+span.property.get(i).end);
			}
		}
		
		//Log.d("generator",":"+span.content);
		TextPaint defaultPaint = span.getTextPaint(readerFontSize);
		//float maxTextSize = defaultPaint.getTextSize();
		//final int lineHeight = span.getLineHeight(readerFontSize);
		//Log.d("dg","lineheightis:"+lineHeight);
		int fontSize=(int) defaultPaint.getTextSize();
		String s=span.content.toString();
		width-=wMargin;
		height-=hMargin;
		
		LinedContent lc = new LinedContent(span,readerFontSize,ctx,startIdx,-1,x,y,height,width,spanIdx,deliverId);
		//lc.setHyperlink(span.getLinks());
		//lc.setAlign(span.getAlign());
		//Log.d("dg","align:"+span.getAlign());
		float curH=0;
		//int curLineStartIdx=0;
		//float[] ws = new float[s.length()];
		//tp.getTextWidths(s, ws);
		float[] measuredWidth = new float[1];
		int widthCount;
		
		if(startIdx==0){
			widthCount = span.getIndent(fontSize);
		}else{
			widthCount=0;
		}
		lc.setIndent(widthCount);
		//String line="";
		/*if(curH>height){
			lc.setEndIdx(-1);
			return lc;
		}*/
		ArrayList<SpecialProperty> property = span.property;
		//if(property!=null){
		//	for(int i=0;i<property.size();i++){
		//		Log.d("sp:start:"+property.get(i).start,"end:"+property.get(i).end);
		//	}
		//}
		int endOfSegment;
		int startOfSegment=startIdx;
		int curPropertyIdx=0;
		SpecialProperty curProperty=null;
		boolean in_property=false;
		TextPaint curPaint=defaultPaint;
		if(property==null || property.size()==0 || property.get(property.size()-1).end<startIdx){
			endOfSegment=s.length();
		}else{
			for(int i=0;i<property.size();i++){
				if(property.get(i).end>=startIdx){
					curProperty = property.get(i);
					curPropertyIdx=i;
					break;
				}
			}
			if(curProperty.start<=startIdx){
				endOfSegment=curProperty.end+1;
				in_property=true;
				curPaint=curProperty.getTextPaint(readerFontSize, span.headerSize,ctx);
			}else{
				in_property=false;
				endOfSegment=curProperty.start;
			}
		}
		int charIdx=startIdx;
		int curLineStartIdx=startIdx;
		float curLineH=-1;
		boolean shouldSetLineHeightOffset=true;
		while(true){
			//可考慮有測量過才assign
			//Log.d("charIdx0","is:"+charIdx);
			//Log.d("widthCount0",":"+widthCount);
			int lineHeightTemp;
			if(in_property){
				//Log.d("textSize","is:"+curPaint.getTextSize());
				//if(span.type==HtmlSpan.TYPE_HEADER)
					//Log.d("inProperty","andHeader");
				lineHeightTemp=curProperty.getLineHeight(readerFontSize,span.headerSize);
				if(lineHeightTemp>curLineH){
					curLineH =lineHeightTemp; 
					if(shouldSetLineHeightOffset)
						lc.setTextAndLineHeightOffset((int) (curLineH-curPaint.getTextSize()));
					//Log.d("curLineH","is:"+curLineH);
				}
			}else{
				//Log.d("textSize","is:"+defaultPaint.getTextSize());
				lineHeightTemp = span.getLineHeight(readerFontSize);
				if(lineHeightTemp>curLineH){
					curLineH =lineHeightTemp;
					//Log.d("curLineHAfet","set:"+curLineH);
					if(shouldSetLineHeightOffset)
						lc.setTextAndLineHeightOffset((int) (curLineH-curPaint.getTextSize()));
				}
			}
			
			charIdx = startOfSegment + curPaint.breakText(s, startOfSegment,endOfSegment,true, width-widthCount, measuredWidth);
			if(startOfSegment<endOfSegment){
				if(in_property)
					//JP, 0103, 改為傳入curProperty，而非bgPaint
					lc.addSegment(new Segment(startOfSegment,endOfSegment-1,curPaint,curProperty));
				else
					lc.addSegment(new Segment(startOfSegment,endOfSegment-1,curPaint,null));
			}
			
			widthCount+=measuredWidth[0];
			/*Log.d("mw",":"+measuredWidth[0]);
			Log.d("widthCount",":"+widthCount);
			Log.d("curH:","is:"+curH);
			Log.d("charIdx","is:"+charIdx);
			Log.d("s","length:"+s.length());
			Log.d("startSeg",":"+startOfSegment);
			Log.d("endSeg",":"+endOfSegment);
			Log.d("curLineStart",":"+curLineStartIdx);*/
			
			if(charIdx==s.length()){
				curH+=curLineH;
				if(curH<=height){
					lc.setEndIdx(s.length()-1);
					lc.setEndFlag(true);
					lc.addLastLine(s.length(),(int) curH);
					return lc;
				}else{
					//lc.addLine(curLineStartIdx,(int) (curH-curLineH));
					lc.setEndIdx(curLineStartIdx-1);
					return lc;
				}		
			}else if(charIdx==endOfSegment){
				startOfSegment=endOfSegment;
				//modified by JP, 2010-12-19 (change ">=" to ">") 
				if(startOfSegment>(curProperty.end) ){ //換property 
					if(curPropertyIdx+1<property.size()){
						curPropertyIdx++;
						curProperty = property.get(curPropertyIdx);
					}else{
						curPaint=defaultPaint;
						endOfSegment = s.length();
						in_property=false;
						continue;
					}
				}
				if(curProperty.start<=startOfSegment){ 
					endOfSegment = curProperty.end+1;
					curPaint = curProperty.getTextPaint(readerFontSize, span.headerSize,ctx);
					in_property=true;
				}else{
					in_property=false;
					curPaint = defaultPaint;
					endOfSegment = curProperty.start;
				}
			}else{//換行
				widthCount=0;
				curH+=curLineH;
				curLineH=-1;
				//Log.d("curH:","is:"+curH);
				if(curH>height){
					//lc.addLine(curLineStartIdx,(int) (curH-curLineH));
					lc.setEndIdx(curLineStartIdx-1);
					return lc;
				}
				
				/*if(Character.isWhitespace(s.charAt(charIdx))){
					curLineStartIdx = charIdx+1;
					startOfSegment = curLineStartIdx;
					lc.addLine(curLineStartIdx+startIdx);
				}*/
				if( charIdx==startOfSegment || (!LineBreak.isEnglishLetter(s.charAt(charIdx)) && LineBreak.canBeFirstInLine(s.charAt(charIdx))) ){
					//Log.d("!!E-AddLine:","is:"+(charIdx+startIdx));
					curLineStartIdx = charIdx;	
					lc.addLine(curLineStartIdx,(int) curH);
					startOfSegment=curLineStartIdx;
				}{//back trace
					int traceStop = Math.max(curLineStartIdx, startOfSegment);
					for(int j=charIdx-1;j>=traceStop;j--){
						if(Character.isWhitespace(s.charAt(j))){
							lc.addLine(j+1,(int) curH);
							//Log.d("!!C-AddLine:","is:"+(startIdx+j+1));
							curLineStartIdx=j+1;
							startOfSegment=curLineStartIdx;
							break;
						}else if(!LineBreak.isEnglishLetter(s.charAt(j)) && LineBreak.canBeFirstInLine(s.charAt(j+1))){
							//Log.d("!!D-AddLine:","is:"+(startIdx+j+1));
							lc.addLine(j+1,(int) curH);
							curLineStartIdx=j+1;
							startOfSegment=curLineStartIdx;
							break;
						}else if(j==traceStop){
							if(j==curLineStartIdx){
								//Log.d("!!A-AddLine:","is:"+(startIdx+charIdx));
								lc.addLine(charIdx,(int) curH);
								curLineStartIdx=charIdx;
								startOfSegment=curLineStartIdx;
							}else{
								//Log.d("!!B-AddLine:","is:"+(startIdx+j));
								lc.addLine(j,(int) curH);
								curLineStartIdx=j;
								startOfSegment=curLineStartIdx;
							}
							break;
						}
					}
				}
			}
		}
		
		
		/*for(i=0;i<ws.length;i++){
			//Log.d("i","cgatAti:"+s.charAt(i));
			ch=s.charAt(i);
			if( i>curLineStartIdx || !Character.isSpace(ch) ){
				widthCount+=ws[i];
				//line+=ch;
				if(widthCount>width){
					if(Character.isSpace(ch)){//white space
						//Log.d("ab","cd");
						//line=line.substring(0,line.length()-1);
						curLineStartIdx=i+1;
					}else if(ws[i]>=fontSize &&  LineBreak.canBeFirstInLine(ch)){//中文
						//line=line.substring(0,line.length()-1);
						curLineStartIdx=i;
						i--;
					}else{
						//back tracing to break line
						for(int j=i-1;j>=curLineStartIdx;j--){
							//Log.d("-line","l:"+line.length());
							//Log.d("-j","is:"+j+" char:"+s.charAt(j));
							//Log.d("curLineStartIdx","is:"+curLineStartIdx);
							if(Character.isSpace(s.charAt(j))){
								//line=line.substring(0,j-curLineStartIdx+1);
								i=j;
								curLineStartIdx=j+1;
								break;
							}else if(ws[j]>=fontSize){//中文
								//Log.d("line","length:"+line.length());
								//Log.d("curStart","idx:"+curLineStartIdx );
								if(!LineBreak.canBeFirstInLine(s.charAt(j+1)) ){
									//直接抓下一個字，不再進行檢查
									if(j==curLineStartIdx){
										//line=line.substring(0,1);
										i=curLineStartIdx;
										curLineStartIdx++;
									}else{
										//line=line.substring(0, j-curLineStartIdx);
										i=j-1;
										curLineStartIdx=j;
										break;
									}
								}else{
									//line=line.substring(0, j-curLineStartIdx+1);
									i=j;
									curLineStartIdx=j+1;
									break;
								}
							}else if(j==curLineStartIdx){//no breaking point
								//to hyphenate word
								//Log.d("Hyphen","hyphen");
								//HyphenatedWord hw = hyphen(line);
								//line=line.substring(0,line.length()-1);
								i--;
								curLineStartIdx=i+1;
							}
						}//end of back tracing
					}
					//hts.addLine(line,curLineStartIdx);
					//Log.d("EndLine","PutInLine:"+(curLineStartIdx+startIdx));
					//textIdxOfLine.add(curLineStartIdx+startIdx);
					//lineCount++;
					lc.addLine(startIdx+curLineStartIdx);
					//line="";
					widthCount=0;
					curH+=lineHeight;
					if(curH>height){  
						lc.setEndIdx(startIdx+curLineStartIdx-1);
						return lc;
					}
				}
			}else{
				curLineStartIdx++;
			}
		}
		if(i==ws.length){
			//Log.d("isLast2","i:"+i);
			lc.addLastLine(startIdx+ws.length);
			lc.setEndIdx(startIdx+i-1);
			lc.setEndFlag(true);
		}*/
		
		//end for
		//Log.d("E:Str","l:"+str.length());
		//Log.d("E:startIdx","is:"+startIdx);
		//return lc;
	}
	
	/**
	 * 逆向產生文字，目前未使用
	 * @param ctx
	 * @param spanIdx
	 * @param span
	 * @param x
	 * @param y
	 * @param readerFontSize
	 * @param height
	 * @param width
	 * @param wMargin
	 * @param hMargin
	 * @param endIdx
	 * @param deliverId
	 * @return
	 */
	public static LinedContent generateTextBackward(Context ctx,int spanIdx,HtmlSpan span, int x, int y, 
			int readerFontSize,int height,int width,int wMargin,int hMargin,int endIdx, String deliverId){
		if(endIdx>span.content.length()){
			Log.e("DrawableGenerator:generateTextBackward","startIdx out of bound");
			return null;
		}
		if(endIdx<0)
			endIdx=span.content.length()-1;	
		String s=span.content.substring(0,endIdx+1);
		width-=wMargin;
		height-=hMargin;
		TextPaint tp = span.getTextPaint(readerFontSize);
		final int lineHeight = span.getLineHeight(readerFontSize);
		//Log.d("dg","lineheightis:"+lineHeight);
		int fontSize=(int) tp.getTextSize();
		//Log.d("heightInGen:","is:"+height);
		LinedContent lc = new LinedContent(span,readerFontSize,ctx,0,endIdx,x,y,height,width,spanIdx,deliverId);
		//lc.setHyperlink(span.getLinks());
		//lc.setAlign(span.getAlign());
		float curH=lineHeight;
		int curLineStartIdx=0;

		float[] ws = new float[s.length()];
		tp.getTextWidths(s, ws);
		int widthCount = span.getIndent(fontSize);
		lc.setIndent(widthCount);
		//String line="";
		char ch;
		//int lineCount=0;
		//start calculating
		if(curH>height){
			lc.setStartIdx(-1);
			return lc;
		}
		for(int i=0;i<ws.length;i++){
			ch=s.charAt(i);
			if(i>curLineStartIdx || !Character.isSpace(ch) ){
				widthCount+=ws[i];
				
				if(widthCount>width){
					if(Character.isSpace(ch)){//white space
						//line=line.substring(0,line.length()-1);
						curLineStartIdx=i+1;
					}else if(ws[i]>=fontSize &&  LineBreak.canBeFirstInLine(ch)){//中文
						//line=line.substring(0,line.length()-1);
						curLineStartIdx=i;
						i--;
					}else{
						//back tracing to break line
						for(int j=i-1;j>=curLineStartIdx;j--){
							//Log.d("-line","l:"+line.length());
							//Log.d("-j","is:"+j+" char:"+s.charAt(j));
							//Log.d("curLineStartIdx","is:"+curLineStartIdx);
							if(Character.isSpace(s.charAt(j))){
								//line=line.substring(0,j-curLineStartIdx+1);
								i=j;
								curLineStartIdx=j+1;
								break;
							}else if(ws[j]>=fontSize){//中文
								//Log.d("line","length:"+line.length());
								//Log.d("curStart","idx:"+curLineStartIdx );
								if(!LineBreak.canBeFirstInLine(s.charAt(j+1)) ){
									//直接抓下一個字，不再進行檢查
									if(j==curLineStartIdx){
										//line=line.substring(0,1);
										i=curLineStartIdx;
										curLineStartIdx++;
									}else{
										//line=line.substring(0, j-curLineStartIdx);
										i=j-1;
										curLineStartIdx=j;
										break;
									}
								}else{
									//line=line.substring(0, j-curLineStartIdx+1);
									i=j;
									curLineStartIdx=j+1;
									break;
								}
							}else if(j==curLineStartIdx){//no breaking point
								//to hyphenate word
								//Log.d("Hyphen","hyphen");
								//HyphenatedWord hw = hyphen(line);
								//line=line.substring(0,line.length()-1);
								i--;
								curLineStartIdx=i+1;
							}
						}//end of back tracing
					}
					//hts.addLine(line,curLineStartIdx);
					//Log.d("EndLine","PutInLine:"+(curLineStartIdx+startIdx));
					//textIdxOfLine.add(curLineStartIdx+startIdx);
					//lineCount++;
					lc.addLine(curLineStartIdx);
					//line="";
					widthCount=0;
					/*curH+=lineHeight;
					if(curH>height){  
						lc.setEndIdx(curLineStartIdx-1);
						return lc;
					}*/
				}else if(i==ws.length-1){
					lc.addLastLine(i+1);
				}
			}else{
				curLineStartIdx++;
			}
		}//end for
		//Log.d("E:Str","l:"+str.length());
		//Log.d("E:startIdx","is:"+startIdx);
		//Log.d("beforeTrim","count:"+lc.getLineCount());
		int lineNo = ((int)height/lineHeight);
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
	
	/**
	 * 產生可畫的BitmapContent物件
	 * @param spanIdx span編號
 	 * @param span 來源span
	 * @param x left
	 * @param y top
	 * @param imgHeight 欲顯示的高度
	 * @param imgWidth  欲顯示的寬度
	 * @param layoutHeight 頁面高
	 * @param layoutWidth 頁面寬
	 * @param isVertical 是否為直書
	 * @return BitmapContent物件
	 */
	public static BitmapContent generateBitmap(int spanIdx,HtmlSpan span, int x, int y, int imgHeight,int imgWidth,int layoutHeight,int layoutWidth,boolean isVertical){
		if(span.type==HtmlSpan.TYPE_IMG)
			return new BitmapContent(span,x,y,imgWidth,imgHeight,layoutWidth,layoutHeight,spanIdx,isVertical);
		else 
			return null;
	}
}
