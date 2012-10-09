package org.iii.ideas.reader.renderer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;

import org.iii.ideas.reader.parser.HtmlSpan;
import org.iii.ideas.reader.parser.property.Hyperlink;
import org.iii.ideas.reader.search.KeywordSearcher;
import org.iii.ideas.reader.underline.Underline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;
/**
 * LinedContent直書版
 * @author III
 * 
 */
public class VerticalLinedContent implements ReaderDrawable{
	/**
	 * vertical content
	 */
	public final static int VERTICAL_CONTENT=2;
	/**
	 * hyperlinks list
	 */
	public ArrayList<Hyperlink> links;
	//private HashMap<Integer,Integer> lineToY;
	private ArrayList<String> lines;
	private ArrayList<Underline> uls;
	private ArrayList<Integer> indexes; 
	public static Hashtable<Integer,Integer> markerStrokeTable;
	private boolean Drawend=false;
	private static int line = 3;
	private int lineSpace;
	private int fontSize;
	private int startIdxInSpan;
	private int endIdxInSpan;
	private int y,h,toRight;
	private boolean isEnd;
	private int spanIdx;
	private float[][] idxMap;
	private char[] content;
	private final int CHAR_X=0;
	private final int CHAR_Y=1;
	//private final int CHAR_W=2;
	private Context ctx;
	private String deliverId;
	private HtmlSpan span;
	
	/*
	 * paint object used to draw underline
	 */
	private Paint underlinePaint = null;
	/**
	 * 
	 * @param span_ HtmlSpan
	 * @param ctx_ context
	 * @param fontSize_ 字體大小
	 * @param lineSpace_ 行間距
	 * @param startIdx_ 開始index
	 * @param endIdx_ 結束index
	 * @param toRight_ x(到螢幕右緣距離)
	 * @param y_  top
	 * @param h_ 高
	 * @param w_ 寬
	 * @param spanIdx_ 第幾個span
	 * @param did deliver id
	 */
	public VerticalLinedContent(HtmlSpan span_,Context ctx_,int fontSize_,int lineSpace_,int startIdx_,int endIdx_,int toRight_,int y_,int h_,int w_,int spanIdx_,String did){
		span=span_;
		ctx=ctx_;
		toRight=toRight_;
		y=y_; 
		uls=new ArrayList<Underline>();
		spanIdx=spanIdx_;
		endIdxInSpan=endIdx_;
		startIdxInSpan=startIdx_;
		lineSpace=lineSpace_;
		fontSize=fontSize_;
		lines=new ArrayList<String> ();
		indexes=new ArrayList<Integer>();
		
		indexes.add(startIdxInSpan);
		h=h_;//w=w_;
		isEnd=false;
		links = new ArrayList<Hyperlink>();
		deliverId=did;
		
		
		underlinePaint = new Paint();
		underlinePaint.setARGB(0x30, 0xFF, 0xFF, 0x00);
		underlinePaint.setStyle(Paint.Style.FILL);
		underlinePaint.setAntiAlias(true);
		underlinePaint.setStrokeWidth(3);	}

	/**
	 * 畫underline
	 * @param tp text paint
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
			drawRect(cv,tp,Math.max(startIdx, startIdxInSpan),Math.min(endIdx, endIdxInSpan),0,true,id);
		}
	}
	
	/**
	 * 將搜尋結果highlight加底色強調
	 * @param cv canvas
	 */
	public void highlightSearchResult(Canvas cv){
		try {
			if (KeywordSearcher.kw != null) {
				String lowerKw = KeywordSearcher.kw.toLowerCase();
				String lowerStr = span.content.toString().toLowerCase();
				int kwStart,searchStart=0;
				//Log.d("kw",":"+kw);
				while ((kwStart = lowerStr.indexOf(lowerKw,searchStart)) >= 0) {
					searchStart = kwStart+1;
					int kwEnd = kwStart + lowerKw.length() - 1;
					//Log.d("kwstart",":"+kwStart);
					//Log.d("kwend",":"+kwEnd);
					//Log.d("startIdx",":"+startIdxInSpan);
					//Log.d("endIdx",":"+endIdxInSpan);
					if (kwEnd >= startIdxInSpan && kwStart <= endIdxInSpan) {
						//kwStart = Math.max(startIdxInSpan, kwStart);
						//kwEnd = Math.min(endIdxInSpan, kwEnd);
						TextPaint kwPaint = new TextPaint();
						//kwPaint.setARGB(50, 50, 0, 0);
						kwPaint.setColor(Color.RED);
						kwPaint.setAlpha(50);
						kwPaint.setStrokeWidth(fontSize);
						drawLine(cv, kwPaint, kwStart, kwEnd,fontSize/2);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 根據文字起訖點畫線
	 * @param cv canvas
	 * @param tp paint
	 * @param startIdx 起點
	 * @param endIdx 終點
	 * @param xOffset 距離baseline多遠(直書baseline為文字左緣)
	 */
	public void drawLine(Canvas cv,TextPaint tp,int startIdx,int endIdx,int xOffset){
		try{
			if(endIdx<0)
				endIdx=endIdxInSpan;

			float localX;
			float startY=idxMap[startIdx][CHAR_Y]-fontSize;
			int localEndIdx=endIdx,localStartIdx=startIdx;
			while(idxMap[localEndIdx][CHAR_X]==0 && localEndIdx>=startIdxInSpan){
				localEndIdx--;
			}
			while(idxMap[localStartIdx][CHAR_X]==0 && localStartIdx<=endIdxInSpan){
				localStartIdx++;
				startY=idxMap[localStartIdx][CHAR_Y]-fontSize;
			}
			//Log.d("drawLine","startY:"+startY);
			//Log.d("drawLine","localX:"+localX);
			for(localX=idxMap[localStartIdx][CHAR_X];localX>idxMap[endIdx][CHAR_X];localX-=getLineWidth()){
				cv.drawLine(localX+xOffset, startY, localX+xOffset, y+h, tp);
				startY=y;
			}
			cv.drawLine(localX+xOffset, startY, localX+xOffset, idxMap[endIdx][CHAR_Y], tp);
		}catch(Exception e){
			Log.e("VerticalLinedContent",e.toString());
			Log.e("startIdx","is:"+startIdx);
			Log.e("endIdx","is:"+endIdx);
			Log.e("startIdxInSpan","is:"+startIdxInSpan);
			Log.e("endIdxInSapn","is:"+endIdxInSpan);
		}
	}
	
	/**
	 * 根據文字起訖點畫線
	 * @param cv canvas
	 * @param tp paint
	 * @param startIdx 起點
	 * @param endIdx 終點
	 * @param xOffset 距離baseline多遠(直書baseline為文字左緣)
	 */
	public void drawRect(Canvas cv,TextPaint tp,int startIdx,int endIdx,int xOffset){
		WeakReference<ArrayList<Bitmap>> markerBitmap=new WeakReference<ArrayList<Bitmap>>(RendererConfig.getUnderlineColor(ctx,deliverId));
		try{
			if(endIdx<0)
				endIdx=endIdxInSpan;

			float localX;
			float startY=idxMap[startIdx][CHAR_Y]-fontSize;
			int localEndIdx=endIdx,localStartIdx=startIdx;
			while(idxMap[localEndIdx][CHAR_X]==0 && localEndIdx>=startIdxInSpan){
				localEndIdx--;
			}
			while(idxMap[localStartIdx][CHAR_X]==0 && localStartIdx<=endIdxInSpan){
				localStartIdx++;
				startY=idxMap[localStartIdx][CHAR_Y]-fontSize;
			}
			RectF rect = new RectF();
			//underlinePaint.setColor(tp.getColor() & 0x30FFFFFF);
			
			for(localX=idxMap[localStartIdx][CHAR_X];localX>idxMap[endIdx][CHAR_X];localX-=getLineWidth()){
				rect.left = localX+xOffset;
				rect.right = localX+xOffset+fontSize;
				rect.top = startY+RendererConfig.charSpace;
				rect.bottom= y+h+RendererConfig.charSpace;
				
				if(Drawend)		
				{
					cv.drawBitmap(markerBitmap.get().get(line), null, rect, null);										
					line = ((++line) % 3)+3;
				}else
				{
					cv.drawBitmap(markerBitmap.get().get(3), null, rect, null);										
				}				
				startY=y;
			}
			rect.left = localX+xOffset;
			rect.right = localX+xOffset+fontSize;
			rect.top = startY+RendererConfig.charSpace;
			rect.bottom= idxMap[endIdx][CHAR_Y]+RendererConfig.charSpace;
			
				if(Drawend)		
				{
					cv.drawBitmap(markerBitmap.get().get(line), null, rect, null);
					line = ((++line) % 3)+3;
				}else
				{
					cv.drawBitmap(markerBitmap.get().get(3), null, rect, null);				
			}
			
			Drawend=false;
		}catch(Exception e){
			Log.e("VerticalLinedContent",e.toString());
			Log.e("startIdx","is:"+startIdx);
			Log.e("endIdx","is:"+endIdx);
			Log.e("startIdxInSpan","is:"+startIdxInSpan);
			Log.e("endIdxInSapn","is:"+endIdxInSpan);
		}
	}
	/***
	 * Draw Mark and Check Line in Table 
	 * Drawend touch Action Up =True
	 */
		public void drawRect(Canvas cv, TextPaint tp, int startIdx, int endIdx,
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

	

	public void drawLine(Canvas cv,TextPaint tp,int startIdx,int endIdx){
		drawLine(cv,tp,startIdx,endIdx,0);
	}
	
	/**
	 * 畫超連結底線
	 * @param tp text paint
	 * @param cv canvas
	 */
	public void drawLink(TextPaint tp,Canvas cv){
		//Log.d("endIdx","is:"+endIdx);
		for(int i=0;i<links.size();i++){
			Hyperlink link = links.get(i);
			if(link.start>endIdxInSpan)
				break;  
			if(link.end<startIdxInSpan)
				continue;
			//float tempY=idxMap[link.start][CHAR_Y];
			//float startX=idxMap[link.start][CHAR_X];
			for(int j=link.start;j<=endIdxInSpan&&j<=link.end;j++){
				//Log.d("j","is:"+j);
				tp.setARGB(255,255,255,255);
				cv.drawText(""+content[j], idxMap[j][CHAR_X], idxMap[j][CHAR_Y], tp);
				tp.setColor(RendererConfig.getLinkColor(ctx,deliverId));
				cv.drawText(""+content[j], idxMap[j][CHAR_X], idxMap[j][CHAR_Y], tp);
				
			}
			drawLine(cv,tp,Math.max(link.start, startIdxInSpan),Math.min(link.end, endIdxInSpan));
		}
	}
	
	/**
	 * 將算好的文字定位加到array，以便畫線/定位
	 * @param idx 第幾個字
	 * @param localX x
	 * @param localY y
	 * @param ch charAt(idx)
	 */
	private void addIdxMap(int idx,float localX,float localY,char ch){
		idxMap[idx][CHAR_X]=localX;
		idxMap[idx][CHAR_Y]=localY;
		//idxMap[idx][CHAR_W]=charWidth;
		content[idx]=ch;
	} 
	@Override
	public void draw(Canvas cv) {
		//Log.d("draw","endIdx:"+endIdx);
		idxMap=new float[endIdxInSpan+1][2];
		content = new char[endIdxInSpan+1];
		// TODO Auto-generated method stub
		//Log.d("draw","x:"+x+" y:"+y);
		//Log.d("!!!!!draw","fontsize:"+fontSize);
		TextPaint tp = new TextPaint();
		tp.setTextSize(fontSize);
		int color=RendererConfig.getTextColor(ctx,deliverId);
		if(color<=0)
			tp.setColor(RendererConfig.getTextColor(ctx,deliverId));
		else
			tp.setColor(Color.BLACK);
		//tp.setTypeface(Typeface.SANS_SERIF);   
		tp.setAntiAlias(true);
		
		int startIdx;
		float localY,localX=toRight-fontSize;
		/*hyperlink overlap*/
		boolean ignore = false;
		if(checkIsAllSpanIsLink() && lines.get(0).substring(0, 1).startsWith(" ")){
			ignore = true;
		}
		
		for(int i=0;i<lines.size();localX-=getLineWidth(),i++){
			//if(areThereLinkInSpan && areThereLinkInLine(i,link))
			//	areThereLinkInLine=true;
			//lineToY.put(i, (int)(localY-y)/(fontSize+lineSpace));
			localY=y+fontSize;
			startIdx=indexes.get(i);
			for(int j=0;j<lines.get(i).length();localY+=fontSize+RendererConfig.charSpace,j++){
				if(ignore){/*hyperlink overlap*/
					ignore = false;
					localY-=(fontSize+RendererConfig.charSpace);
					continue;
				}
				addIdxMap(startIdx+j,localX,localY,lines.get(i).charAt(j));
				//Log.d("!!!!!X:"+localX,"y:"+localY);
				cv.drawText(lines.get(i).substring(j, j+1), localX, localY, tp);	
			}
		}
		drawLink(tp,cv);
		drawUnderline(tp,cv);
		highlightSearchResult(cv);
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

	public DrawableOnClickResult onClicked(int inX,int inY){
		DrawableOnClickResult result = new DrawableOnClickResult(); 
		if(inX>=getX() && inX<=getX()+getWidth() && inY>=y && inY<=y+getHeight()){
			int lineNo = (getX()+getWidth()-inX)/(fontSize+lineSpace);
			if(lineNo>=indexes.size() || lineNo>=lines.size())
				return result;
			int startIdx=indexes.get(lineNo);
			int i=0;
			for(int startY=y+fontSize+RendererConfig.charSpace;startY<inY;i++,startY+=fontSize+RendererConfig.charSpace);
			int resultIdx=i+startIdx;
			if(resultIdx>endIdxInSpan)
				resultIdx=endIdxInSpan;
			result.setStatus(true);
			result.setIdx(resultIdx);
			//是否偵測link點選，目前未開放
			/*for(int j=0;j<links.size();j++){
				if(links.get(j).start<=resultIdx && links.get(j).end>=resultIdx){
					result.setSrc(links.get(j).href);
					return result;
				}
			}*/
		}
		return result;
			
	}
	
	/**
	 * 是否有underline存在
	 * @param ulStartIdx underline起始定位
	 * @param ulEndIdx underline終點定位
	 * @return 是否有underline存在
	 */
	private boolean isUnderlineInSpan(int ulStartIdx,int ulEndIdx){
		//Log.d("lineSt:","is:"+indexes.get(i));
		//Log.d("lineEnd:","is:"+(indexes.get(i)+lines.get(i).length()));
		//Log.d("linkStart:","is:"+link.start);
		//Log.d("linkend:","is:"+link.end);
		boolean result = !(ulStartIdx>endIdxInSpan|| ulEndIdx<startIdxInSpan );
		return result;
	}
	
	/**
	 * 取得文字大小
	 * @return 文字大小
	 */
	public int getFontSize(){
		return fontSize;
	}
	
	public int getWidth(){
		return (int) (getLineCount()*getLineWidth());
	}
	
	/**
	 * 取得每行寬度
	 * @return 每行寬度
	 */
	public float getLineWidth(){
			return fontSize+lineSpace;
	}
	/**
	 * 設定終點index
	 * @param i 終點index
	 */
	public void setEndIdx(int i){
		endIdxInSpan=i;
	}
	
	/**
	 * 取得終點index
	 * @return 終點index
	 */
	public int getEndIdx(){
		return  endIdxInSpan;
	}
	
	/**
	 * 設定起點index
	 * @param i 起點index
	 */
	public void setStartIdx(int i){
		startIdxInSpan=i;
	}
	
	/**
	 * 取得起點index
	 * @return 起點index
	 */
	public int getStartIdx(){
		return  startIdxInSpan;
	}
	
	/**
	 * 設定此span超連結資料
	 * @param links_ span超連結資料
	 */
	public void setHyperlink(ArrayList<Hyperlink> links_){
		links=links_;
	}
	
	/**
	 * 設定此span underline資料
	 * @param uls_ span underline資料
	 */
	public void setUnderline(ArrayList<Underline> uls_){
		uls=uls_;
	}
	
	
	/**
	 * 取得內容分為幾行文字
	 * @return 內容分為幾行文字
	 */
	public int getLineCount(){
		return lines.size();
	}
	
	/**
	 * 加入新的一行
	 * @param s 該行文字
	 * @param id 該行定位點
	 */
	public void addLine(String s,int id){
		//Log.d("addLine","line:"+s);
		lines.add(s);
		indexes.add(id);
	}
	
	/**
	 * 加入末行
	 * @param s 末行文字
	 */
	public void addLastLine(String s){
		//Log.d("addLastLine","line:"+s);
		lines.add(s);
		//x=(int) (toRight-getWidth());
	}

	/**
	 * 取得分行後的文字
	 * @return 分行後的文字
	 */
	public ArrayList<String> getLines(){
		return lines;
	} 
	
	/**
	 * 設定是否已達對應到span的終點(即已將該span處理完畢)
	 * @param isEnd_ 是否已達對應到span的終點
	 */
	public void setEndFlag(boolean isEnd_){
		isEnd=isEnd_;
	}
	
	/**
	 * 未使用
	 * @param lineNo
	 */
	public void trimFromStart(int lineNo){
		for(int i=0;i<lineNo;i++){
			lines.remove(0);
			indexes.remove(0);
			startIdxInSpan=indexes.get(0);
		}
		//x=(int) (toRight-getWidth());
	}
	
	/**
	 * 是否已達相對應span終點(即該span是否處理完畢)
	 * @return 是否已達相對應span終點
	 */
	public boolean isEnd(){
		return isEnd;
	}
	
	/**
	 * 是否為該span起點
	 * @return 是否為該span起點
	 */
	public boolean isStart(){
		return startIdxInSpan==0?true:false;
	}
	
	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return h;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return (int) (toRight-getLineWidth()*getLineCount());
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

	@Override
	public void setPosition(int x_, int y_) {
		// TODO Auto-generated method stub
		y=y_;
		toRight=x_+getWidth();
	}

	@Override
	public int isLinedContent() {
		// TODO Auto-generated method stub
		return VERTICAL_CONTENT;
	}
}
