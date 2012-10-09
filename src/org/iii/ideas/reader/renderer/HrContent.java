package org.iii.ideas.reader.renderer;

import org.iii.ideas.reader.parser.CssProperty;
import org.iii.ideas.reader.parser.HtmlSpan;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * 分隔線
 * @author III
 * 
 */
public class HrContent extends BitmapContent{
	//private boolean isVertical=false;

	String deliverId;
	/**
	 * 
	 * @param span_ HtmlSpan
	 * @param x_ left
	 * @param y_ top
	 * @param w_ 寬
	 * @param h_ 高
	 * @param layoutW_ 頁面寬
	 * @param layoutH_ 頁面高
	 * @param spanIdx_ span編號(在該章節第幾個span)
	 * @param isVertical_ 是否為直書
	 * @param deliverId_  deliver id
	 * @param maxHeight 最大允許高度
	 * @param maxWidth 最大允許寬度
	 */
	public HrContent(HtmlSpan span_, int x_, int y_, int w_, int h_, int layoutW_,
			int layoutH_, int spanIdx_,boolean isVertical_,String deliverId_,int maxHeight,int maxWidth) {
		super(span_, x_, y_, w_, h_, layoutW_, layoutH_, spanIdx_,isVertical_);
		// TODO Auto-generated constructor stub
		//isVertical = isVertical_;
		deliverId = deliverId_;
		int size=-1;
		size = (int) CssProperty.getUnitValueFromString(span.content.toString()).getValue(true);
		if(size<=0)
			size=RendererConfig.DEFAULT_HR_LINE_SIZE;		
		if(isVertical){
			w= size;
			layoutW=w+RendererConfig.DEFAULT_HR_MARGIN;
			if(layoutW>maxWidth){
				layoutW=maxWidth;
				w = layoutW - RendererConfig.DEFAULT_HR_MARGIN;
			}
			h=span.getCssProperty().getHrWidth(layoutH);
			if(h>layoutH || h<=0)
				h=layoutH;
			
			//Log.d("vertical","h:"+h);
		}else{
			h= size;
			layoutH=h+RendererConfig.DEFAULT_HR_MARGIN;
			if(layoutH>maxHeight){
				layoutH=maxHeight;
				h = layoutH - RendererConfig.DEFAULT_HR_MARGIN;
			}
			
			w=span.getCssProperty().getHrWidth(layoutW);
			//Log.d("width","get");
			if(w>layoutW || w<=0)
				w=layoutW;
		}
	}
	
	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return layoutH;
	}
	

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return layoutW;
	}
	
	@Override
	public void draw(Canvas cv) {
		drawBackground(cv);
		//TextPaint tp = span.getTextPaint((int) RendererConfig.DEFAULT_EM_SIZE) ;
		//int textColor = RendererConfig.getTextColor(ctx,deliverId);	
		float margin = RendererConfig.DEFAULT_HR_MARGIN/2;
		//if(textColor<=0)
		//	tp.setColor(textColor);
		RectF rect;
		if(isVertical){
			switch(span.getAlign()){
			case CssProperty.ALIGN_LEFT:
				rect = new RectF(x+margin, y, x+w+margin, y+h);
				//cv.drawRect(x+margin, y, x+w+margin, y+h, tp);
				break;
			case CssProperty.ALIGN_RIGHT:
				rect = new RectF(x+margin, y+layoutH-h, x+w+margin, y+layoutH);
				//cv.drawRect(x+margin, y+layoutH-h, x+w+margin, y+layoutH, tp);
				break;
			default:
			case CssProperty.ALIGN_CENTER:
				int offset = (layoutH-h)/2;
				//Log.d("offset",":"+offset);
				//Log.d("h",":"+h);Log.d("layouth",":"+layoutH);
				//Log.d("l:"+(x+margin)+" t:"+(y+offset),"r:"+(x+w+margin)+" b:"+(y+offset+w));
				rect = new RectF(x+margin, y+offset, x+w+margin, y+offset+h);
				//cv.drawRect(x+margin, y+offset, x+w+margin, y+offset+w, tp);
				break;
			}
		}else{
			switch(span.getAlign()){
			case CssProperty.ALIGN_LEFT:
				rect = new RectF(x, y+margin, x+w, y+h+margin);
				//cv.drawRect(x, y+margin, x+w, y+h+margin, tp);
				break;
			case CssProperty.ALIGN_RIGHT:
				rect = new RectF(x+layoutW-w, y+margin, x+layoutW, y+h+margin);
				//cv.drawRect(x+layoutW-w, y+margin, x+w, y+h+margin, tp);
				break;
			default:
			case CssProperty.ALIGN_CENTER:
				int offset = (layoutW-w)/2;
				rect = new RectF(x+offset, y+margin, x+offset+w, y+h+margin);
				//cv.drawRect(x+offset, y+margin, x+offset+w, y+h+margin, tp);
				break;
			}
		}
		drawHrRect(rect,cv);
		//cv.drawRect(rect, tp);
	}
	
	private void drawHrRect(RectF rect,Canvas cv){
		Paint paint = new Paint();
		paint.setColor(Color.parseColor("#585858"));
		cv.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
		cv.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);
		paint.setColor(Color.parseColor("#F0F0F0"));
		cv.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint);
		cv.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
	}
	
	protected void drawBackground(Canvas cv){
		if(!isVertical && !RendererConfig.isNightMode){
			Paint bgPaint = span.getCssProperty().getBgPaint();
			if(bgPaint!=null){
				cv.drawRect(x, y, x+layoutW, y+layoutH, bgPaint);
			}
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
