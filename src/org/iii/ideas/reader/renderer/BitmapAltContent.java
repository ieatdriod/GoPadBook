package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

import org.iii.ideas.reader.parser.HtmlSpan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.text.TextPaint;

/**
 * 圖片替代文字。如果圖片不存在且有設alt attribute，則產生此一物件顯示替代物件
 * @author III
 * 
 */
public class BitmapAltContent extends BitmapContent{
	private String altText;
	private TextPaint tp;
	private ArrayList<String> text;
	private int lineHeight; 
	private String deliverId;
	private Context ctx;
	/**
	 * 
	 * @param span_ 對應到的span
	 * @param x_ x(left)
	 * @param y_ y(top)
	 * @param w_ 寬
	 * @param h_ 高
	 * @param layoutW_ 頁面寬度
	 * @param layoutH_ 頁面高度
	 * @param spanIdx_ 第幾個span
	 * @param readerFontSize_ 目前設定的字體大小
	 * @param ctx_ context
	 * @param deliverId_ delivery id
	 * @param isVertical_ 是否為直書
	 */
	public BitmapAltContent(HtmlSpan span_, int x_, int y_, int w_, int h_,
			int layoutW_, int layoutH_, int spanIdx_,int readerFontSize_,Context ctx_,String deliverId_,boolean isVertical_) {
		super(span_, x_, y_, w_, h_, layoutW_, layoutH_, spanIdx_,isVertical_);
		// TODO Auto-generated constructor stub
		ctx=ctx_;
		deliverId=deliverId_;
		altText = span.content.toString();
		//Log.d("alttext",":"+altText);
		tp = span.getTextPaint(readerFontSize_);
		text = new ArrayList<String>();
		int startIdx=0,endIdx=0,widthLimit=layoutW-2*RendererConfig.IMG_ALT_MARGIN;
		float[] measuredWidth=new float[1];
		while(true){
			endIdx = startIdx+tp.breakText(altText, startIdx,altText.length(),true, widthLimit, measuredWidth);
			text.add(altText.substring(startIdx,endIdx));
			startIdx=endIdx;
			if(endIdx==altText.length())
				break;
		}
		lineHeight = (int) (tp.getTextSize()*1.5f);
		super.layoutH = h=(int) (lineHeight*text.size())+2*RendererConfig.IMG_ALT_MARGIN;
		if(text.size()==1){
			w=(int) measuredWidth[0]+2*RendererConfig.IMG_ALT_MARGIN;
		}else{
			w=layoutW;
		}
		if(isVertical){
			x-=w;
		}
		//Log.d("alt h:"+h,"w:"+w);
	}

	/*public BitmapAltContent(HtmlSpan span_,String altText_, int x_, int y_, int w_, int h_,
			int layoutW_, int layoutH_, int spanIdx_,int readerFontSize_,Context ctx_,String deliverId_,boolean isVertical_) {
		super(span_, x_, y_, w_, h_, layoutW_, layoutH_, spanIdx_,isVertical_);
		// TODO Auto-generated constructor stub
		ctx=ctx_;
		deliverId=deliverId_;
		altText = altText_;
		//Log.d("alttext",":"+altText);
		tp = span.getTextPaint(readerFontSize_);
		text = new ArrayList<String>();
		int startIdx=0,endIdx=0,widthLimit=layoutW-2*RendererConfig.IMG_ALT_MARGIN;
		float[] measuredWidth=new float[1];
		while(true){
			endIdx = startIdx+tp.breakText(altText, startIdx,altText.length(),true, widthLimit, measuredWidth);
			text.add(altText.substring(startIdx,endIdx));
			startIdx=endIdx;
			if(endIdx==altText.length())
				break;
		}
		lineHeight = (int) (tp.getTextSize()*1.5f);
		super.layoutH = h=(int) (lineHeight*text.size())+2*RendererConfig.IMG_ALT_MARGIN;
		if(text.size()==1){
			w=(int) measuredWidth[0]+2*RendererConfig.IMG_ALT_MARGIN;
		}else{
			w=layoutW;
		}
		if(isVertical){
			x-=w;
		}
		//Log.d("alt h:"+h,"w:"+w);
	}*/
	
	private void drawCross(Canvas cv){
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		cv.drawLine(x, y, x+RendererConfig.IMG_ALT_MARGIN, y+RendererConfig.IMG_ALT_MARGIN, paint);
		cv.drawLine(x+RendererConfig.IMG_ALT_MARGIN, y, x, y+RendererConfig.IMG_ALT_MARGIN, paint);
		
	}
	
	private void drawAltBorder(Canvas cv){
		Paint borderPaint = new Paint();
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setPathEffect(new DashPathEffect(new float[]{RendererConfig.BORDER_DOT_LENGTH,RendererConfig.BORDER_DOT_LENGTH}, 1));
		borderPaint.setStrokeWidth(0);
		drawBorderRect(cv,borderPaint,0);
	}
	
	@Override
	public void draw(Canvas cv) {
		// TODO Auto-generated method stub
		//Log.d("in","drawAlt");
		drawBackground(cv);
		drawCross(cv);
		drawAltBorder(cv);
		int textColor = RendererConfig.getTextColor(ctx,deliverId);	
		if(textColor<=0)
			tp.setColor(textColor);
		int startX = x+RendererConfig.IMG_ALT_MARGIN,startY=(int) (y+RendererConfig.IMG_ALT_MARGIN+tp.getTextSize());
		for(int i=0;i<text.size();startY+=lineHeight,i++){
			cv.drawText(text.get(i), startX, startY, tp);
		}
	}
	@Override
	public DrawableOnClickResult onClicked(int inX, int inY) {
		// TODO Auto-generated method stub
		DrawableOnClickResult result = new DrawableOnClickResult();
		if(inX>=getX() && inX<=getX()+getWidth() && inY>=getY() && inY<=getY()+getHeight()){
			//result.setSrc(span.content.toString());
			result.setStatus(true);
			//result.setIsImg(true);
			result.setIdx(0);
			
		}
		return result;
	}
}
