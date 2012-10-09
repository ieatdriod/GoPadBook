package org.iii.ideas.reader.renderer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;

import org.iii.ideas.reader.parser.CssProperty;
import org.iii.ideas.reader.parser.HtmlSpan;
import org.iii.ideas.reader.parser.property.Hyperlink;
import org.iii.ideas.reader.parser.property.SpecialProperty;
import org.iii.ideas.reader.search.KeywordSearcher;
import org.iii.ideas.reader.underline.Underline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;

/**
 * 文字span經過DrawableGenerater產生的分行化橫向文字，負責呈現內容, 處理motion event, 劃線等功能。一個文字的HtmlSpan(parse所產生的一個概念上的文字區塊)由於分頁的關係可能分為數個LinedContent(當前頁面實際呈現的文字區塊)。
 * @author III
 * 
 *
 */
public class LinedContent implements ReaderDrawable{
	/**
	 * lined content flag
	 */
	public static final int LINED_CONTENT=1; 
	private String contentStr;
	/**
	 * hyperlinks
	 */
	public ArrayList<Hyperlink> links;
	//private HashMap<Integer,Integer> lineToY;
	//private ArrayList<TextLine> lines;
	private ArrayList<Underline> uls;
	private ArrayList<Integer> lineIdxes; 
	private ArrayList<Integer> lineY;
	private ArrayList<Segment> segments;
	private static int line=1;
	public static Hashtable<Integer,Integer> markerStrokeTable;
	boolean Drawend=false;
	//最後一個element為endIdxInSpan+1
	private int indent;
	//private int lineHeight;
	private int fontSize;
	private int startIdxInSpan;
	private int endIdxInSpan;
	private int x,y,w;
	private boolean isEnd;
	private int spanIdx;
	private float[][] idxMap;
	    
	/**
	 * idxMap column的constant，用以取得該字x值
	 */
	public static final int CHAR_X=0;
	/**
	 * idxMap column的constant，用以取得該字y值
	 */
	public static final int CHAR_Y=1;
	/**
	 * idxMap column的constant，用以取得該字寬
	 */
	public static final int CHAR_W=2;

	private Context ctx;
	private TextPaint paint;
	private int textAlign;
	private HtmlSpan span;
	private String deliverId;
	private int textAndLineHeightOffset=0;
	private int readerFontSize;
	private boolean shouldSetTextColor;
	private int preferTextColor;
	
	/*
	 * paint object used to draw underline
	 */
	private Paint underlinePaint = null;
	
	/**
	 * 
	 * @param span_ HtmlSpan
	 * @param readerFontSize_ 使用者設定的字體大小，作為base
	 * @param ctx_ context 
	 * @param startIdx_ 從span內哪個文字開始起算
	 * @param endIdx_ 停在span內哪一個文字
	 * @param x_ left
	 * @param y_ top 
	 * @param h_ 寬
	 * @param w_ 高
	 * @param spanIdx_ 第幾個span
	 * @param did delivery id
	 */
	public LinedContent(HtmlSpan span_,int readerFontSize_,Context ctx_,int startIdx_,int endIdx_,int x_,int y_,int h_,int w_,int spanIdx_,String did){
		readerFontSize=readerFontSize_;
		shouldSetTextColor=false;
		//Log.d("lc","initialize");
		ctx=ctx_;
		x=x_;
		y=y_; 
		segments=new ArrayList<Segment>();
		uls=new ArrayList<Underline>();
		spanIdx=spanIdx_;
		endIdxInSpan=endIdx_;
		startIdxInSpan=startIdx_;
		//lineHeight=lineHeight_;
		//Log.d("lc","lineheightis:"+lineHeight);
		span=span_;
		contentStr=span.content.toString();
		paint=span.getTextPaint(readerFontSize); 
		//Log.d("lc","tf:"+paint.getTypeface());
		//Log.d("lc","paint is Italic:"+paint.getTypeface().isItalic());
		fontSize=(int) paint.getTextSize();
		//lines=new ArrayList<String> ();
		lineIdxes=new ArrayList<Integer>();
		lineY = new ArrayList<Integer>();
		
		lineIdxes.add(startIdxInSpan);
		x=x_;
		y=y_; 
		//h=h_;
		w=w_;
		isEnd=false;
		links = span.getLinks();
		indent=0;
		textAlign=span.getAlign();
		deliverId = did;
		
		underlinePaint = new Paint();
		underlinePaint.setARGB(0x30, 0xFF, 0xFF, 0x00);
		underlinePaint.setStyle(Paint.Style.FILL);
		underlinePaint.setAntiAlias(true);
		underlinePaint.setStrokeWidth(3);
	}

	/**
	 * 重點劃線呈現
	 * @param tp 劃線用到的TextPaint(主要是顏色設定等資料)
	 * @param cv 畫在哪個canvas
	 */
	public void drawUnderline(TextPaint tp,Canvas cv){
		//tp.setColor(RendererConfig.getUnderlineColor(ctx,deliverId));	
		
		for(int i=0;i<uls.size();i++){
			int startIdx=uls.get(i).idx1;
			int endIdx=uls.get(i).idx2;
			int id=uls.get(i).id;
			if(endIdx==-1)
				endIdx=endIdxInSpan;
			if(!isUnderlineInSpan(startIdx,endIdx))
				continue;
			//drawRect(cv,tp,Math.max(startIdx, startIdxInSpan),Math.min(endIdx, endIdxInSpan),0);
			drawRect(cv,tp,Math.max(startIdx, startIdxInSpan),Math.min(endIdx, endIdxInSpan),0,true,id);
			/*if(startIdx<startIdxInSpan || startIdx>endIdxInSpan)
				startIdx=startIdxInSpan;
			if(endIdx>endIdxInSpan || endIdx<startIdxInSpan)
				endIdx=endIdxInSpan;	
			drawLine(cv,tp,startIdx,endIdx);*/
		}
	}
	
	/**
	 * 將搜尋結果highlight進行背景顏色處理
	 * @param cv canvas
	 */
	public void highlightSearchResult(Canvas cv){
		try {
			if (KeywordSearcher.kw != null) {
				String lowerKw = KeywordSearcher.kw.toLowerCase();
				String lowerStr = span.content.toString().toLowerCase();
				int kwStart,searchStart=0;
				while ((kwStart = lowerStr.indexOf(lowerKw,searchStart)) >= 0) {
					searchStart = kwStart+1;
					int kwEnd = kwStart + lowerKw.length() - 1;
					if (kwEnd >= startIdxInSpan && kwStart <= endIdxInSpan) {
						kwStart = Math.max(startIdxInSpan, kwStart);
						kwEnd = Math.min(endIdxInSpan, kwEnd);
						int size = fontSize;
						Segment s = getSegmentByIdx(kwStart);
						if(s!=null){
							size = (int) s.getTextSize();
						}
						TextPaint kwPaint = new TextPaint();
						//kwPaint.setARGB(50, 50, 0, 0);
						kwPaint.setColor(Color.RED);
						kwPaint.setAlpha(50);
						kwPaint.setStrokeWidth(size);
						drawLine(cv, kwPaint, kwStart, kwEnd,(int) Math.ceil((double)size/2));
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	
	public void drawLine(Canvas cv,TextPaint tp,int startIdx,int endIdx){
		drawLine(cv,tp,startIdx,endIdx,0);
	}
	
	/**
	 * 根據起訖文字劃刪除線
	 * @param cv canvas
	 * @param tp text paint 
	 * @param startIdx 起點index
	 * @param endIdx 終點index
	 */
	public void drawStrike(Canvas cv,TextPaint tp,int startIdx,int endIdx){
		drawLine(cv,tp,startIdx,endIdx,fontSize/3);
	}
	
	/**
	 * 根據起訖文字劃線，baselineOffset為距離文字base(橫書為下方)多高(+為base上方，-為base下方)
	 * @param cv canvas
	 * @param tp text paint
	 * @param startIdx 起點index
	 * @param endIdx 終點index
	 * @param baselineOffset offset
	 */
	private void drawLine(Canvas cv,TextPaint tp,int startIdx,int endIdx,int baselineOffset){
		int localEndIdx=endIdx,localStartIdx=startIdx;
		try{
			if(localEndIdx<0)
				localEndIdx=endIdxInSpan;
			if(localEndIdx<startIdxInSpan)
				localEndIdx=startIdxInSpan;
			if(localEndIdx>endIdxInSpan)
				localEndIdx=endIdxInSpan;
			if(localStartIdx<startIdxInSpan)
				localStartIdx=startIdxInSpan;
			if(localStartIdx>endIdxInSpan)
				localStartIdx=endIdxInSpan;
			
			float  startX;
			while(idxMap[localEndIdx][CHAR_Y]==0 && localEndIdx>=startIdxInSpan){
				localEndIdx--;
			}
			while(idxMap[localStartIdx][CHAR_Y]==0 && localStartIdx<=endIdxInSpan){
				localStartIdx++;
			}
			/*for(startX=idxMap[localStartIdx][CHAR_X],localY=idxMap[localStartIdx][CHAR_Y];localY<idxMap[localEndIdx][CHAR_Y];){
				cv.drawLine(startX, localY, x+w, localY, tp);
				startX=x;
				localY+=getLineHeight();
			}*/
			int offset = y-getTextAndLineHeightOffset();
			int endLineNo=lineY.indexOf((int)idxMap[localEndIdx][CHAR_Y]-offset);
			//Log.d("drawLine",":"+endLineNo);
			//Log.d("drawLine","edY:"+((int)idxMap[localEndIdx][CHAR_Y]-y));
			//Log.d("drawLine","startY:"+((int)idxMap[localStartIdx][CHAR_Y]-y));
			
			startX=idxMap[localStartIdx][CHAR_X];
			for(int i=lineY.indexOf((int)idxMap[localStartIdx][CHAR_Y]-offset);i<endLineNo;i++){
				//Log.d("drawLine","i:"+i);
				cv.drawLine(startX, offset+lineY.get(i)-baselineOffset, Math.min(getLineEndX(i), x+w), offset+lineY.get(i)-baselineOffset, tp);
				startX=x;
			}
			cv.drawLine(startX, offset+lineY.get(endLineNo)-baselineOffset, Math.min(idxMap[localEndIdx][CHAR_X]+getSegmentByIdx(localEndIdx).widthAt(localEndIdx, contentStr), x+w), offset+lineY.get(endLineNo)-baselineOffset, tp);
		}catch(Exception e){
			Log.v("LinedContent:drawLine",e.toString());
			Log.v("localStartIdx",":"+localStartIdx);
			Log.v("startIdxInSpan",":"+startIdxInSpan);
			Log.v("localEndIdx",":"+localEndIdx);
			Log.v("endIdxInSpan",":"+endIdxInSpan);
		}
	}	
	
	/**
	 * 根據起訖文字劃線，baselineOffset為距離文字base(橫書為下方)多高(+為base上方，-為base下方)
	 * @param cv canvas
	 * @param tp text paint
	 * @param startIdx 起點index
	 * @param endIdx 終點index
	 * @param baselineOffset offset
	 */
	public void drawRect(Canvas cv,TextPaint tp,int startIdx,int endIdx,int baselineOffset){
		int localEndIdx=endIdx,localStartIdx=startIdx;
		WeakReference<ArrayList<Bitmap>> markerBitmap=new WeakReference<ArrayList<Bitmap>>(RendererConfig.getUnderlineColor(ctx,deliverId));
		try{
			if(localEndIdx<0)
				localEndIdx=endIdxInSpan;
			if(localEndIdx<startIdxInSpan)
				localEndIdx=startIdxInSpan;
			if(localEndIdx>endIdxInSpan)
				localEndIdx=endIdxInSpan;
			if(localStartIdx<startIdxInSpan)
				localStartIdx=startIdxInSpan;
			if(localStartIdx>endIdxInSpan)
				localStartIdx=endIdxInSpan;
			
			float  startX;
			while(idxMap[localEndIdx][CHAR_Y]==0 && localEndIdx>=startIdxInSpan){
				localEndIdx--;
			}
			while(idxMap[localStartIdx][CHAR_Y]==0 && localStartIdx<=endIdxInSpan){
				localStartIdx++;
			}
			/*for(startX=idxMap[localStartIdx][CHAR_X],localY=idxMap[localStartIdx][CHAR_Y];localY<idxMap[localEndIdx][CHAR_Y];){
				cv.drawLine(startX, localY, x+w, localY, tp);
				startX=x;
				localY+=getLineHeight();
			}*/
			int offset = y-getTextAndLineHeightOffset();
			int endLineNo=lineY.indexOf((int)idxMap[localEndIdx][CHAR_Y]-offset);
			//Log.d("drawLine",":"+endLineNo);
			//Log.d("drawLine","edY:"+((int)idxMap[localEndIdx][CHAR_Y]-y));
			//Log.d("drawLine","startY:"+((int)idxMap[localStartIdx][CHAR_Y]-y));
			
			startX = idxMap[localStartIdx][CHAR_X];			
			float fontHeight = tp.descent() - tp.ascent();			
			RectF rect = new RectF();					
			for (int i = lineY.indexOf((int) idxMap[localStartIdx][CHAR_Y]
					- offset); i < endLineNo; i++) {
				rect.left = startX;
				rect.right = Math.min(getLineEndX(i), x + w);
				rect.top = offset + lineY.get(i) - baselineOffset - fontHeight
						+ RendererConfig.charSpace;
				rect.bottom = offset + lineY.get(i) - baselineOffset
						+ RendererConfig.charSpace;
				/*marker stroke*/
				if (Drawend) 
				{					
					cv.drawBitmap(markerBitmap.get().get(line), null, rect, null);		
					line = (++line) % 3;
				} else {					
					cv.drawBitmap(markerBitmap.get().get(0), null, rect, null);		
				}		
				startX = x;
			}
			rect.left = startX;
			rect.right = Math.min(idxMap[localEndIdx][CHAR_X]+getSegmentByIdx(localEndIdx).widthAt(localEndIdx, contentStr), x+w);
			rect.top = offset+lineY.get(endLineNo)-baselineOffset - fontHeight+RendererConfig.charSpace;
			rect.bottom = offset+lineY.get(endLineNo)-baselineOffset+RendererConfig.charSpace;
			if (Drawend) {				
				cv.drawBitmap(markerBitmap.get().get(line), null, rect, null);
				line = (++line) % 3;
			} else {				
				cv.drawBitmap(markerBitmap.get().get(0), null, rect, null);
			}

			Drawend = false;
		}catch(Exception e){
			Log.v("LinedContent:drawLine",e.toString());
			Log.v("localStartIdx",":"+localStartIdx);
			Log.v("startIdxInSpan",":"+startIdxInSpan);
			Log.v("localEndIdx",":"+localEndIdx);
			Log.v("endIdxInSpan",":"+endIdxInSpan);
		}
	}
	
	/**
	 * 根據第幾個取得位於哪個segment。segment定義可參照Segment class
	 * @param idx index in span
	 * @return 相對應的segment
	 */
	private Segment getSegmentByIdx(int idx){
		for(int i=0;i<segments.size();i++){
			if(segments.get(i).end>=idx){
				//Log.d("segment:"+i,"start:"+segments.get(i).getStart());
				return segments.get(i); 
			}
		}
		return null;
	}
	
        /***
	 * Draw Mark and Check Line in Table 
	 * Drawend touch Action Up =True
	 */	
	private void drawRect(Canvas cv, TextPaint tp, int startIdx, int endIdx,
				int baselineOffset, boolean drawend,int id) {
		
		Drawend = drawend;

		if (markerStrokeTable == null) {
			markerStrokeTable = new Hashtable<Integer, Integer>();
			markerStrokeTable.put(id, line);
		} else {
			if (markerStrokeTable.containsKey(id)) {
				line = markerStrokeTable.get(id);
			} else {
				markerStrokeTable.put(id, line);
			}
		}
		drawRect(cv, tp, startIdx, endIdx, baselineOffset);
	}

	/**
	 * 取得某行最後一個字(的右方)x值
	 * @param lineNo 第幾行
	 * @return 右端x值
	 */
	private float getLineEndX(int lineNo){
		int endIdx=lineIdxes.get(lineNo+1)-1;
		//Log.d("lineend","x:"+idxMap[endIdx][CHAR_X]);
		//Log.d("lineend","w:"+getSegmentByIdx(endIdx).widthAt(endIdx));
		return idxMap[endIdx][CHAR_X]+getSegmentByIdx(endIdx).widthAt(endIdx, contentStr);
	}
	
	/**
	 * 劃這個span內所有超連結
	 * @param tp text paint
	 * @param cv canvas
	 */
	public void drawLink(TextPaint tp,Canvas cv){
		//Log.d("endIdx","is:"+endIdx);
		
		if(links!=null){
			for(int i=0;i<links.size();i++){
				Hyperlink link = links.get(i);
				if(link.start>endIdxInSpan)
					break;  
				if(link.end<startIdxInSpan)
					continue;
				
				for(int j=Math.max(link.start, startIdxInSpan);j<=endIdxInSpan&&j<=link.end;j++){
					tp.setARGB(255,255,255,255);
					cv.drawText(contentStr.substring(j, j+1), idxMap[j][CHAR_X], idxMap[j][CHAR_Y], tp);
					tp.setColor(RendererConfig.getLinkColor(ctx,deliverId));
					cv.drawText(contentStr.substring(j, j+1), idxMap[j][CHAR_X], idxMap[j][CHAR_Y], tp);
				}
				drawLine(cv,tp,Math.max(link.start, startIdxInSpan),Math.min(link.end, endIdxInSpan));
				
			}
		}
		
	}
	
	/*hyperlink overlap*/
	private boolean checkIsAllSpanIsLink() {
		boolean result = false;
		if (links != null) {
			for (int i = 0; i < links.size(); i++) {
				Hyperlink link = links.get(i);
				if (link.start == startIdxInSpan && link.end == endIdxInSpan) {
					result = true;
				}
			}
		}
		return result;
	}
	
	private void addIdxMap(int idx,float localX,float localY){
		try{
			idxMap[idx][CHAR_X]=localX;
			idxMap[idx][CHAR_Y]=localY;
		}catch(Exception e){
			Log.e("addIdxMap",e.toString());
			Log.e("idx","is:"+idx);
			Log.e("endIdxInSpan","is:"+endIdxInSpan);
		}
	} 
	
	private void drawBackground(Canvas cv){
		if(!RendererConfig.isNightMode){
			Paint bgPaint = span.getBgPaint();
			if(bgPaint!=null){
				cv.drawRect(getX(), getY(), getX()+getWidth(), getY()+getHeight(), bgPaint);
			}
		}
	}
	
	/**
	 * 將一行的文字劃出來
	 * @param cv canvas
	 * @param lineNo 第幾行
	 * @param lineAlign align方式(CssProperty.ALIGN_LEFT,CssProperty.ALIGN_RIGHT,CssProperty.ALIGN_CENTER,CssProperty.ALIGN_JUSTIFY)
	 * @param start 第幾個segment開始
	 * @param localIndent 此行indent多少
	 * @param localY 此行y值
	 * @return 停在哪個segment
	 */
	private int drawLineOfText(Canvas cv,int lineNo,int lineAlign,int start, int localIndent, float localY){
		int startIdx=lineIdxes.get(lineNo);
		int endIdx=lineIdxes.get(lineNo+1);
		//Log.d("startIdx","is:"+startIdx);
		//Log.d("endIdx","is:"+endIdx);
		int lineStart=-1,lineEnd=-1;
		float lineWidth=0;
		float localX;
		int i;
		for(i=start;i<segments.size();i++){
			if(segments.get(i).getStart()>=startIdx){
				lineStart=i;
				break;
			}
		}
		for(i=lineStart;i<segments.size();i++){
			lineWidth += segments.get(i).getWidth(contentStr);
			//Log.d("width:"+i,":"+segments.get(i).getWidth(contentStr));
			if(segments.get(i).end>=endIdx-1){
				lineEnd=i;
				//Log.d("segend",":"+segments.get(i).end);
				break;
			}
		}
		//Log.d("lineWidth0","is:"+lineWidth);
		lineWidth-=segments.get(lineEnd).setEndSpaceZero(contentStr);
		lineWidth-=segments.get(lineStart).setStartSpaceZero(contentStr);
		//Log.d("lineWidth","is:"+lineWidth);
		//Log.d("width","is:"+w);
			
		switch(lineAlign){
		case CssProperty.ALIGN_LEFT:
			localX=x+localIndent;
			for(int j=lineStart;j<=lineEnd;segments.get(j).clearWidthArray(),j++){
				for(int k=segments.get(j).getStart();k<=segments.get(j).end;localX+=segments.get(j).widthAt(k,contentStr),k++){
					addIdxMap(k,localX,localY);
				}
				if(segments.get(j).bgPaint!=null){
					drawBackground(segments.get(j), cv, idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
					//cv.drawRect(idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
				}	
				//Log.d("!!!!abc",":"+segments.get(j).start);
				//Log.d("!!!!abc",":"+segments.get(j).start+":"+idxMap[CHAR_X][segments.get(j).start]);safsadf
				if(!checkIsAllSpanIsLink())/*hyperlink overlap*/
				drawText(segments.get(j), cv, contentStr.substring(segments.get(j).getStart(), segments.get(j).end+1), idxMap[segments.get(j).getStart()][CHAR_X], localY, segments.get(j).getTextPaint(shouldSetTextColor, preferTextColor));
			}
			break;
		case CssProperty.ALIGN_RIGHT:
			localX=x+w-lineWidth;
			for(int j=lineStart;j<=lineEnd;segments.get(j).clearWidthArray(),j++){
				for(int k=segments.get(j).getStart();k<=segments.get(j).end;localX+=segments.get(j).widthAt(k,contentStr),k++){
					addIdxMap(k,localX,localY);
				}
				if(segments.get(j).bgPaint!=null){
					//cv.drawRect(idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
					drawBackground(segments.get(j), cv, idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
				}
				drawText(segments.get(j), cv, contentStr.substring(segments.get(j).getStart(), segments.get(j).end+1), idxMap[segments.get(j).getStart()][CHAR_X], localY, segments.get(j).getTextPaint(shouldSetTextColor, preferTextColor) );
			}
			break;
		case CssProperty.ALIGN_CENTER:
			localX=x+(w-lineWidth)/2;
			for(int j=lineStart;j<=lineEnd;segments.get(j).clearWidthArray(),j++){
				for(int k=segments.get(j).getStart();k<=segments.get(j).end;localX+=segments.get(j).widthAt(k,contentStr),k++){
					addIdxMap(k,localX,localY);
				}
				if(segments.get(j).bgPaint!=null){
					//cv.drawRect(idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
					drawBackground(segments.get(j), cv, idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
				}
				drawText(segments.get(j), cv, contentStr.substring(segments.get(j).getStart(), segments.get(j).end+1), idxMap[segments.get(j).getStart()][CHAR_X], localY, segments.get(j).getTextPaint(shouldSetTextColor, preferTextColor));
			}
			break;
		case CssProperty.ALIGN_JUSTIFY:
			localX=x+localIndent;
			float spaceWidth=0;
			for(int l=lineStart;l<=lineEnd;l++){
				spaceWidth += segments.get(l).getSpaceWidth(contentStr);
			}
			if(spaceWidth>0){

				//Log.d("local","indent:"+localIndent);
				float spaceRatio = (w-lineWidth+spaceWidth-localIndent)/spaceWidth;
				//Log.d("space","width:"+spaceWidth);
				//Log.d("line","width:"+lineWidth);
				for(int j=lineStart;j<=lineEnd;segments.get(j).clearWidthArray(),j++){
					segments.get(j).resetSpaceWidth(spaceRatio, contentStr);
					for(int k=segments.get(j).getStart();k<=segments.get(j).end;localX+=(segments.get(j).widthAt(k,contentStr)),k++){
						//Log.d("addx","add:"+(segments.get(j).widthAt(k)));
						addIdxMap(k,localX,localY);
						//Log.d("addx",":"+idxMap[k][CHAR_X]);
					}
					if(segments.get(j).bgPaint!=null){
						//cv.drawRect(idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
						drawBackground(segments.get(j), cv, idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
					}
					for(int k=segments.get(j).getStart();k<=segments.get(j).end;k++){
						//Log.d("x",":"+idxMap[k][CHAR_X]);
						drawText(segments.get(j), cv, contentStr.substring(k, k+1), idxMap[k][CHAR_X], localY, segments.get(j).getTextPaint(shouldSetTextColor, preferTextColor));
					}
				}
			}else{
				float ratio = (float)(w-localIndent)/lineWidth;
				for(int j=lineStart;j<=lineEnd;segments.get(j).clearWidthArray(),j++){
					for(int k=segments.get(j).getStart();k<=segments.get(j).end;localX+=(segments.get(j).widthAt(k,contentStr)*ratio),k++){
						addIdxMap(k,localX,localY);
					}
					if(segments.get(j).bgPaint!=null){
						//cv.drawRect(idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
						drawBackground(segments.get(j), cv, idxMap[segments.get(j).getStart()][CHAR_X], localY-segments.get(j).getTextSize(), localX, localY+getTextAndLineHeightOffset(), segments.get(j).bgPaint);
					}
					for(int k=segments.get(j).getStart();k<=segments.get(j).end;k++){
						drawText(segments.get(j), cv, contentStr.substring(k, k+1), idxMap[k][CHAR_X], localY, segments.get(j).getTextPaint(shouldSetTextColor, preferTextColor));
					}
				}
			}
			break;
		}
		return i; //end idx of segment
	}
	
	private void drawText(Segment seg, Canvas cv, String text, float x, float y, Paint paint){
		if(seg.isSub()){
			cv.drawText(text, x, y+RendererConfig.getLinkOffset(), paint);
		}else if(seg.isSup()){
			cv.drawText(text, x, y-0.7f*paint.getTextSize(), paint);
		}else{
			cv.drawText(text, x, y, paint);
		}
	}

	private void drawBackground(Segment seg, Canvas cv, float x1, float y1, float x2, float y2, Paint bgPaint){
		if(RendererConfig.isNightMode){
			
		}else if(seg.isSub()){
			cv.drawRect(x1, y1, x2, y2, bgPaint);
		}else if(seg.isSup()){
			cv.drawRect(x1, y1-0.7f*seg.getTextSize(), x2, y2-0.7f*seg.getTextSize(), bgPaint);
		}else{
			cv.drawRect(x1, y1, x2, y2, bgPaint);
		}
	}
	
	@Override
	public void draw(Canvas cv) {
		//Log.d("endIdxInSpan",":"+endIdxInSpan);
		//for(int i=0;i<segments.size();i++){
		//	Log.d("drawSeg","segEnd:"+segments.get(i).end);
		//}
		//Log.d("idxes",":"+lineIdxes.toString());
		drawBackground(cv);
		int textColor = RendererConfig.getTextColor(ctx,deliverId);	
		if(textColor<=0){
			//Log.d("aaa","bbb");
			preferTextColor=textColor;
			shouldSetTextColor=true;
			//paint.setColor(textColor);
		}
		//Log.d("draw","tf:"+paint.getTypeface());
		idxMap=new float[endIdxInSpan+1][2];
		// TODO Auto-generated method stub
		
		int localIndent,startOfSegment=0;
		float localY;
		for(int i=0;i<getLineCount();++i){
			localY=y+lineY.get(i)-getTextAndLineHeightOffset();
			//startIdx=lineIdxes.get(i);
			//endIdx=lineIdxes.get(i+1);
			//line=contentStr.substring(startIdx, endIdx);
			if(textAlign==CssProperty.ALIGN_RIGHT){	
				localIndent=0;
			}else if(textAlign==CssProperty.ALIGN_CENTER){
				localIndent=0;
			}else if(i==0 && startIdxInSpan==0){
				localIndent=indent;
			}else{
				localIndent=0;
			}
			if( textAlign==CssProperty.ALIGN_RIGHT || textAlign==CssProperty.ALIGN_CENTER){
				startOfSegment=drawLineOfText(cv,i,textAlign,startOfSegment,localIndent,localY);
			}else if( (i==getLineCount()-1 && isEnd) || textAlign==CssProperty.ALIGN_LEFT ){ 
				//align=left
				startOfSegment=drawLineOfText(cv,i,CssProperty.ALIGN_LEFT,startOfSegment,localIndent,localY);
			}else{
				//Log.d("align","justify");
				startOfSegment=drawLineOfText(cv,i,CssProperty.ALIGN_JUSTIFY,startOfSegment,localIndent,localY);
			}
		}
		//Log.d("draw","endIdx:"+endIdx);
		
		TextPaint decorationPaint=new TextPaint(paint);
		drawSpecialProperty(cv);
		drawLink(decorationPaint,cv);
		drawUnderline(decorationPaint,cv);
		highlightSearchResult(cv);
	}
	
	/**
	 * 畫<u>和<strike>的method
	 * @param cv canvas
	 */
	private void drawSpecialProperty(Canvas cv){
		if(span.property!=null){
			for(int i=0;i<span.property.size();i++){
				if(span.property.get(i).getType()==SpecialProperty.U){
					drawLine(cv,span.property.get(i).getTextPaint(readerFontSize, span.headerSize,ctx),span.property.get(i).start,span.property.get(i).end);
				}else if(span.property.get(i).getType()==SpecialProperty.STRIKE){
					//Log.d("draw","strike");
					drawStrike(cv,span.property.get(i).getTextPaint(readerFontSize, span.headerSize,ctx),span.property.get(i).start,span.property.get(i).end);
				}
			}
		}
	}
	
	/*@Override
	public void draw(Canvas cv) {
		drawBackground(cv);
		//cv.drawPosText("abc",new float[]{20,50,50,20,80,80} , paint);
		int textColor = RendererConfig.getTextColor(ctx,deliverId);	
		if(textColor<=0)
			paint.setColor(textColor);
		String line;
		if(!isSet){
			idxMap=new float[endIdxInSpan+1][2];
			// TODO Auto-generated method stub
			
			int startIdx,endIdx,localIndent;
			float localY,localX;
			for(int i=0;i<getLineCount();++i){
				localY=y+lineY.get(i)-getTextAndLineHeightOffset();
				startIdx=lineIdxes.get(i);
				endIdx=lineIdxes.get(i+1);
				//Log.d("!!!lc:startIdx:"+startIdx,"endIdx:"+endIdx);
				//Log.d("lineStart","is:"+startIdx);
				//Log.d("lineEnd","is:"+endIdx);
				//Log.d("start","is:"+startIdxInSpan);
				//Log.d("end","is:"+endIdxInSpan);
				line=contentStr.substring(startIdx, endIdx);
				//line = AndroidLibrary.rtrimAll(line);
				if(textAlign==CssProperty.ALIGN_RIGHT){
					localX= x+w-paint.measureText(line);		
					localIndent=0;
				}else if(textAlign==CssProperty.ALIGN_CENTER){
					localX= x+(w-paint.measureText(line))/2;	
					localIndent=0;
				}else if(i==0 && startIdxInSpan==0){
					localX=x+indent;
					localIndent=indent;
				}else{
					localIndent=0;
					localX=x;
				}
				if( textAlign==CssProperty.ALIGN_RIGHT || textAlign==CssProperty.ALIGN_CENTER){
					float[] charWidth=new float[line.length()];
					paint.getTextWidths(line, charWidth);
					for(int j=0;j<line.length();localX+=charWidth[j],j++){
						addIdxMap(startIdx+j,localX,localY);
						cv.drawText(line.substring(j, j+1), localX, localY, paint);	
					}
				}else if( (i==getLineCount()-1 && isEnd) || textAlign==CssProperty.ALIGN_LEFT ){ 
					//align=left

					float[] charWidth=new float[line.length()];
					paint.getTextWidths(line, charWidth);
					for(int j=0;j<line.length();localX+=charWidth[j],j++){
						addIdxMap(startIdx+j,localX,localY);
						cv.drawText(line.substring(j, j+1), localX, localY, paint);	
					}
				}else{
					//Log.d("align","justify");
					//boolean tempb=false;
					//if(line.contains("code")){
					//	Log.d("lc","code in");
					//	tempb=true;
					//}
					StringSplitter split = new StringSplitter();
					split.split(line);
					ArrayList<String> words= split.getWordsList();
					//if(line.contains("code")){
					//	for(int z=0;z<words.size();z++)
					//		Log.d("lc","words"+words.get(z));
					//}
					if(words.size()==1){
						// align=justify, chinese

						float[] charWidth=new float[words.get(0).length()];
						paint.getTextWidths(words.get(0), charWidth);
						float ratio=(w-localIndent)/paint.measureText(words.get(0));	
						//Log.d("ratio","is:"+ratio);
						for(int j=0;j<words.get(0).length();localX+=(charWidth[j++]*ratio)){
							//Log.d("char:"+line.substring(j, j+1),"width:"+charWidth[j]+" ratioW:"+(charWidth[j]*ratio));
							addIdxMap(startIdx+j,localX,localY);
							cv.drawText(words.get(0).substring(j, j+1), localX, localY, paint);
						}
					}else{
						//align=justify，英文

						float[] wordsWidth = new float[words.size()];
						float widthCount=0;
						for(int j=0;j<words.size();j++){
							wordsWidth[j]=paint.measureText(words.get(j));
							widthCount+=wordsWidth[j];
						}
						float wordSpace = (w-widthCount-localIndent)/(words.size()-1);
						
						for(int j=0;j<words.size();localX+=wordSpace,j++){
							String word=words.get(j);
							float[] wordWidth = new float[word.length()];
							paint.getTextWidths(word, wordWidth);
							int wordIdx=lineIdxes.get(i)+split.getCharIdxFromWordIdx(j);
							//Log.d("word:"+word,"wordIdx:"+wordIdx);
							for(int k=0;k<word.length();localX+=wordWidth[k],k++){
								cv.drawText(word.substring(k, k+1), localX, localY, paint);
								addIdxMap(wordIdx+k,localX,localY);
							}
						}
					}
				}
			}
			isSet=true;
		}else{
			for(int i=startIdxInSpan;i<=endIdxInSpan;i++){
				if(idxMap[i][CHAR_X]!=0)
					//Log.d("char:"+contentStr.substring(i-startIdxInSpan, i-startIdxInSpan+1),"x:"+idxMap[i][CHAR_X]);
					cv.drawText(contentStr.substring(i, i+1), idxMap[i][CHAR_X],idxMap[i][CHAR_Y] , paint);
			}
		}
		//Log.d("draw","endIdx:"+endIdx);
		
		TextPaint decorationPaint=new TextPaint(paint);
		drawLink(decorationPaint,cv);
		drawUnderline(decorationPaint,cv);
	}*/
	
	int lineNo = 0;
	public DrawableOnClickResult onClicked(int inX,int inY){
		DrawableOnClickResult result = new DrawableOnClickResult(); 
		if(inX>=x && inX<=x+w && inY>=y && inY<=y+getHeight()){
			
			for(int l=0;l<lineY.size();l++){
				if(lineY.get(l)+y>=inY){
					lineNo=l;
					break;
				}
			}
			if(lineNo>=getLineCount())
				return result;
			int startIdx=lineIdxes.get(lineNo);
			int endIdx=lineIdxes.get(lineNo+1);
			int i;
			for(i=startIdx+1;i<endIdx;i++){
				if(idxMap[i][CHAR_X]>=inX){
					result.setStatus(true);
					result.setIdx(i-1);
					break;
				}
			}
			if(i==endIdx){
				int k=i-1;
				while(idxMap[k][CHAR_X]<=0 && k>startIdx){
					k--;
				}
				if(idxMap[k][CHAR_X]+fontSize>=inX){
					result.setStatus(true);
					result.setIdx(i-1);
				}
			}
			//!!!!!!!!!!!!!line onclick switch
			/*for(int j=0;j<links.size();j++){
				if(result.getIdx()>=links.get(j).start && result.getIdx()<=links.get(j).end){
					result.setSrc(links.get(j).href);
					break;
				}
			}*/	
		}
		//Log.d("resultIdx","is:"+result.getIdx());
		return result;
	}
	
	
	/*private void drawLineAlignLeft(final int lineNo,boolean areThereLinks,Canvas cv,TextPaint tp,float localY,Hyperlink link){
		float localX=x;
		startIdx=indexes.get(lineNo);
		float[] charWidth=new float[lines.get(lineNo).length()];
		tp.getTextWidths(lines.get(lineNo), charWidth);
		for(int j=0;j<lines.get(lineNo).length();localX+=charWidth[j],j++){
			if(inLink){
				cv.drawText(lines.get(i).substring(j, j+1), localX, localY, tp);
				if(j==link.end){
					tp.setColor(Color.BLACK);
					inLink=false;
					if( (++linkCounter)<links.size() ){
						link=links.get(linkCounter);
					}else{
						areThereLinkInSpan=false;
					}
				}
			}else{
				if(areThereLinkInSpan&&startIdx+j==link.start){
					inLink=true;
					tp.setColor(Color.BLUE);
					cv.drawText(lines.get(i).substring(j, j+1), localX, localY, tp);
				}else{
					cv.drawText(lines.get(i).substring(j, j+1), localX, localY, tp);
				}
			}
		}
	}*/
	
	/*private boolean areThereLinkInLine(int i,Hyperlink link){
		//Log.d("lineSt:","is:"+indexes.get(i));
		//Log.d("lineEnd:","is:"+(indexes.get(i)+lines.get(i).length()));
		//Log.d("linkStart:","is:"+link.start);
		//Log.d("linkend:","is:"+link.end);
		int lineEnd=lineIdxes.get(i+1)-1;
		boolean result = (lineIdxes.get(i)<=link.start && link.start< lineEnd ) || (lineIdxes.get(i)<=link.end && link.end< lineEnd) || (link.start<lineIdxes.get(i)&& link.end>=lineEnd)  ;
		return result;
	}*/
	
	private boolean isUnderlineInSpan(int ulStartIdx,int ulEndIdx){
		//Log.d("lineSt:","is:"+indexes.get(i));
		//Log.d("lineEnd:","is:"+(indexes.get(i)+lines.get(i).length()));
		//Log.d("linkStart:","is:"+link.start);
		//Log.d("linkend:","is:"+link.end);
		//boolean result = (startIdxInSpan<=ulStartIdx && ulStartIdx<= endIdxInSpan ) || (startIdxInSpan<=ulEndIdx && ulEndIdx<= endIdxInSpan) || (ulStartIdx<startIdxInSpan&& ulEndIdx>endIdxInSpan)  ;
		boolean result = !(ulStartIdx>endIdxInSpan|| ulEndIdx<startIdxInSpan );
		//Log.d("isUnderLineInSpan","v:"+result);
		return result;
	}
	
	/**
	 * 取得此區塊目前預設的文字大小 (和閱讀設定的值可能不同，因為html可能有設文字大小屬性)
	 * @return 預設的文字大小
	 */
	public int getFontSize(){
		return fontSize;
	}
	
	@Override
	public int getHeight(){
		if(lineY.size()>0){
			//<p>加入margin
			return lineY.get(lineY.size()-1)+(isEnd&&span.type==HtmlSpan.TYPE_PAR?RendererConfig.PAR_MARGIN:0);
		}else
			return 0;
	}
	
	/**
	 * 計算此內容停在span裡哪個字
	 * @param i 停在span裡哪個字
	 */
	public void setEndIdx(int i){
		endIdxInSpan=i;
	}
	
	/**
	 * 取得停在span裡哪個字 
	 * @return 停在span裡哪個字 
	 */
	public int getEndIdx(){
		return  endIdxInSpan;
	}
	
	/**
	 * 設定起始點
	 * @param i 起始點
	 */
	public void setStartIdx(int i){
		startIdxInSpan=i;
	}
	
	/**
	 * 設定終點
	 * @return 終點
	 */
	public int getStartIdx(){
		return  startIdxInSpan;
	}
	
	/**
	 * 設定此區塊的劃線資料以便進行畫線
	 * @param uls_ 此區塊的劃線資料
	 */
	public void setUnderline(ArrayList<Underline> uls_){
		uls=uls_;
	}
	
	
	/**
	 * 此區塊共有幾行
	 * @return 此區塊共有幾行
	 */
	public int getLineCount(){
		return lineIdxes.size()-1;
	}
	
	/**
	 * 加入新的一行
	 * @param id index
	 */
	public void addLine(int id){
		//Log.d("addLine","line:"+s);
		//lines.add(s);
		if(lineIdxes.size()==0 || id>lineIdxes.get(lineIdxes.size()-1)){
			 lineIdxes.add(id);
		}
	}
	
	/**
	 * 加入末行，在目前架構下和addLine(int)無異
	 * @param id index
	 */
	public void addLastLine(int id){
		if(lineIdxes.size()==0 || id>lineIdxes.get(lineIdxes.size()-1)){
			 lineIdxes.add(id);
		}
		//Log.d("addLastLine","line:"+s);
		//lines.add(s);
	}
	
	/**
	 * 加入新的一行並給予其y值
	 * @param id index
	 * @param y y
	 */
	public void addLine(int id,int y){
		//Log.d("addLine","line:"+s);
		//lines.add(s);
		if(lineIdxes.size()==0 || id>lineIdxes.get(lineIdxes.size()-1)){
			 lineIdxes.add(id);
			 lineY.add(y);
			 //Log.d("addline","y:"+y);
		}
	}
	
	/**
	 * 加入末行並給予其y值，在目前架構下和addLine(int,int)無異
	 * @param id index
	 * @param y y
	 */
	public void addLastLine(int id,int y){
		if(lineIdxes.size()==0 || id>lineIdxes.get(lineIdxes.size()-1)){
			 lineIdxes.add(id);
			 lineY.add(y);
			 //Log.d("addline","y:"+y);
		}
		//Log.d("addLastLine","line:"+s);
		//lines.add(s);
	}
	
	
	/**
	 * 標記此LinedContent已到span末尾 (即該span已處理完畢)
	 * @param isEnd_ 是否為span末尾
	 */
	public void setEndFlag(boolean isEnd_){
		isEnd=isEnd_;
	}
	
	/**
	 * 從末尾回算頁面內容所需用到的method。由於DrawableGenerater的generateTextBackward method已未使用，目前此method無實際用途。
	 * @param lineNo 第幾行
	 */
	public void trimFromStart(int lineNo){
		for(int i=0;i<lineNo;i++){
			//lines.remove(0);
			lineIdxes.remove(0);
			startIdxInSpan=lineIdxes.get(0);
		}
		contentStr=contentStr.substring(startIdxInSpan);
	}
	
	public void setPosition(int x_,int y_){
		x=x_;
		y=y_;
	}
	
	/**
	 * 是否已達span末尾
	 * @return 是否已達span末尾
	 */
	public boolean isEnd(){
		return isEnd;
	}
	
	/**
	 * 此LinedContent是否包含該span的開頭
	 * @return 是否包含該span的開頭
	 */
	public boolean isStart(){
		return startIdxInSpan==0?true:false;
	}
	
	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return w;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return x;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return y;
	}
	@Override
	public int getSpanIdx() {
		// TODO Auto-generated method stub
		return spanIdx;
	}
	public int getIndent(){
		return indent;
	}

	public void setIndent(int idt){
		indent=idt;
	}
	
	public int getAlign(){
		return textAlign;
	}
	/**
	 * 文字高度和行高的誤差，由於每行紀錄的是該行下緣(包含margin)的y值，扣掉此offset才是文字的baseline
	 * @param value 文字高度和行高的誤差
	 */
	public void setTextAndLineHeightOffset(int value){
		textAndLineHeightOffset=value;
	}
	
	/**
	 * 取得文字和行高的差距
	 * @return 文字和行高的差距
	 */
	public int getTextAndLineHeightOffset(){
		return textAndLineHeightOffset;
	}
	
	/**
	 * 新增segment
	 * @param s segment
	 */
	public void addSegment(Segment s){
			if(segments.size()>0 && segments.get(segments.size()-1).end>=s.getStart())
				segments.get(segments.size()-1).setEnd(s.getStart()-1);
			segments.add(s);
	}

	@Override
	public int isLinedContent() {
		// TODO Auto-generated method stub
		return LINED_CONTENT;
	}
}
