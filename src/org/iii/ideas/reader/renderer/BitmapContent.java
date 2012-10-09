package org.iii.ideas.reader.renderer;

import java.io.File;

import org.iii.ideas.android.general.AndroidLibrary;
import org.iii.ideas.reader.parser.CssProperty;
import org.iii.ideas.reader.parser.HtmlSpan;
import org.iii.ideas.reader.parser.ImgAtts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.Log;

/**
 * 書中圖片內容物件
 * @author III
 * 
 */
public class BitmapContent implements ReaderDrawable{
	protected int x,y,w,h;
	protected int layoutW;
	protected int layoutH;
	private int spanIdx;
	protected HtmlSpan span;
	protected boolean isVertical;
	/**
	 * 
	 * @param span_ 對應到的span
	 * @param x_ x(left)
	 * @param y_ y(top)
	 * @param w_ 寬
	 * @param h_ 高
	 * @param layoutW_ 頁面寬度
	 * @param layoutH_ 頁面高度
	 * @param spanIdx_ span編號
	 * @param isVertical_ 是否為直書
	 */
	public BitmapContent(HtmlSpan span_,int x_,int y_,int w_,int h_,int layoutW_,int layoutH_,int spanIdx_,boolean isVertical_){
		span=span_;x=x_;y=y_;w=w_;h=h_;spanIdx=spanIdx_;layoutW=layoutW_;layoutH=layoutH_;
		isVertical=isVertical_;
	}
	
	protected void drawBackground(Canvas cv){
		if(!isVertical && !RendererConfig.isNightMode){
			Paint bgPaint = span.getBgPaint();
			if(bgPaint!=null){
				cv.drawRect(getX(), getY(), getX()+layoutW, getY()+layoutH, bgPaint);
			}
		}
	}
	
	protected int drawBorder(Canvas cv){
		if(span.isImgAttsSet()){
			int borderWidth = span.getImgBorderWidth();
			int style = span.getImgBorderStyle();
			if(borderWidth>0){
				Paint borderPaint = new Paint();
				//Log.d("styleInDraw",":"+style);
				//Log.d("test","dashed:"+ImgAtts.borderStyleMap.get("dashed"));
				//Log.d("test","dotted:"+ImgAtts.borderStyleMap.get("dotted"));
				int tempColor=span.getImgBorderColor();
				if(tempColor<=0)
					borderPaint.setColor(tempColor);
				if(style==0){
					return 0;
				}else if(style==-1){
					cv.drawRect(x, y, x+w, y+h, borderPaint);
				}else if(style==ImgAtts.borderStyleMap.get("dashed")){
					//Log.d("draw","dashed");
					borderPaint.setStyle(Style.STROKE);
					borderPaint.setPathEffect(new DashPathEffect(new float[]{RendererConfig.BORDER_DASH_LENGTH,RendererConfig.BORDER_DASH_LENGTH}, 1));
					borderPaint.setStrokeWidth(borderWidth);
					/*cv.drawLine(x, y, x+w, y, borderPaint);
					cv.drawLine(x+w,y, x+w,y+h, borderPaint);
					cv.drawLine(x+w,y+h,x,y+h, borderPaint);
					cv.drawLine(x,y+h,x,y, borderPaint);*/
					drawBorderRect(cv,borderPaint,borderWidth);
				}else if(style==ImgAtts.borderStyleMap.get("dotted")){
					//Log.d("draw","dotted");
					borderPaint.setStyle(Style.STROKE);
					borderPaint.setPathEffect(new DashPathEffect(new float[]{RendererConfig.BORDER_DOT_LENGTH,RendererConfig.BORDER_DOT_LENGTH}, 1));
					borderPaint.setStrokeWidth(borderWidth);
					drawBorderRect(cv,borderPaint,borderWidth);
				}else if(style==ImgAtts.borderStyleMap.get("double")){
					//Log.d("draw","double");
					borderPaint.setStyle(Style.STROKE);
					float lineWidth = (float)borderWidth/4;
					//borderPaint.setPathEffect(new DashPathEffect(new float[]{RendererConfig.BORDER_DOT_LENGTH,RendererConfig.BORDER_DOT_LENGTH}, 1));
					borderPaint.setStrokeWidth(lineWidth);
					drawBorderRect(cv,borderPaint,borderWidth);
					drawBorderRect(cv,borderPaint,lineWidth);
				}else{
					cv.drawRect(x, y, x+w, y+h, borderPaint);
				}
				return borderWidth;
			}
		}
		return 0;

	}
	
	protected void drawBorderRect(Canvas cv,Paint borderPaint,float width){
		cv.drawLine(x+width, y+width, x+w-width, y+width, borderPaint);
		cv.drawLine(x+w-width, y+width, x+w-width,y+h-width, borderPaint);
		cv.drawLine(x+w-width,y+h-width,x+width,y+h-width, borderPaint);
		cv.drawLine(x+width,y+h-width,x+width, y+width, borderPaint);
	}
	
	/**
	 * 畫圖片，順序為畫背景,畫border,畫圖片內容
	 */
	@Override
	public void draw(Canvas cv) {
		// TODO Auto-generated method stub
		//Log.d("x","is:"+x);
		//Log.d("y","is:"+y);
			try {
				//Log.d("a","a");
				drawBackground(cv);
				int borderWidth = drawBorder(cv);
				File bmpFile = new File(span.content.toString());
				//FileInputStream bmpStream = new FileInputStream(bmpFile);
				//BitmapFactory.Options option = new BitmapFactory.Options();
				//option.inJustDecodeBounds=true;
				//BitmapFactory.decodeStream(bmpStream,null,option);
				//Log.d("imgW",":"+option.outWidth);
				//Log.d("imgH",":"+option.outHeight);
				//Log.d("H",":"+h);
				//Log.d("W",":"+w);

				
				//float ratio=Math.max(option.outWidth/(float)(w-2*borderWidth), option.outHeight/(float)(h-2*borderWidth));
				//int ratioInt = (int) Math.floor(ratio);
				//Log.d("bitmap","ratio:"+ratioInt);
				//if(ratioInt>1){
				//BitmapFactory.Options resample = new BitmapFactory.Options();
				//resample.inPurgeable=true;
				//resample.inSampleSize=ratio;
				Bitmap bm = AndroidLibrary.decodeBitmap(bmpFile,h-2*borderWidth,w-2*borderWidth,true);
				/*}else{
					try {
						bm = BitmapFactory.decodeFile(span.content.toString());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						bm=null;
					}
				}*/
				/*if(option.outHeight>1500 || option.outWidth>1500){
					BitmapFactory.Options resample = new BitmapFactory.Options();
					resample.inSampleSize=4;
					bm = BitmapFactory.decodeFile(span.content.toString(),resample);
				}else if(option.outHeight>800 || option.outWidth>800){
					BitmapFactory.Options resample = new BitmapFactory.Options();
					resample.inSampleSize=2;
					bm = BitmapFactory.decodeFile(span.content.toString(),resample);
				}else{
					bm = BitmapFactory.decodeFile(span.content.toString());
				}*/
				
				//Log.d("b","b");
				if(bm==null){
					Log.e("BitmapContent:draw","bitmap not found");
				}else{
					//Bitmap newBm;
					Matrix matrix= new Matrix();
					matrix.reset();
					//Log.d("w:"+w,"h:"+h);
					if(w-2*borderWidth!=bm.getWidth() || h-2*borderWidth!=bm.getHeight() ){
						//Log.d("bmh","is:"+bm.getHeight());
						//Log.d("bmw","is:"+bm.getWidth());
						//matrix = new Matrix();
						matrix.postScale(((float)(w-2*borderWidth)/bm.getWidth()),((float)(h-2*borderWidth)/bm.getHeight()) );
						//newBm = Bitmap.createBitmap(bm, 0,0,
						//		bm.getWidth(), bm.getHeight(), matrix, true);
						//bm.recycle();
					}//else{
					//	newBm=bm;
					//}
					drawBitmap(cv,bm,borderWidth,matrix);
					bm.recycle(); //newBm
					bm=null;//newBm
					//Log.d("BMPalign","is:"+align);
					//Log.d("BMWidth","is:"+bm.getWidth());
					/*if(span.getAlign()==CssProperty.ALIGN_RIGHT){
						cv.drawBitmap(bm, x+layoutW-bm.getWidth(), y, null);
					}else if(span.getAlign()==CssProperty.ALIGN_CENTER){
						cv.drawBitmap(bm, x+(layoutW-bm.getWidth())/2, y, null);
					}else{
						cv.drawBitmap(bm, x, y, null);
					}*/
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 


	}

	private void drawBitmap(Canvas cv,Bitmap bm,int borderWidth,Matrix m){
		if(isVertical){
			//cv.drawBitmap(bm, x+borderWidth, y+borderWidth, null);
			m.postTranslate( x+borderWidth, y+borderWidth);
			cv.drawBitmap(bm, m, null);
		}else if(span.getAlign()==CssProperty.ALIGN_RIGHT){
			//cv.drawBitmap(bm, x+layoutW-bm.getWidth()+borderWidth, y+borderWidth, null);
			m.postTranslate( x+layoutW-(w-2*borderWidth)+borderWidth, y+borderWidth);
			//Log.d("x:"+(x+layoutW-bm.getWidth()+borderWidth),"y:"+(y+borderWidth));
			cv.drawBitmap(bm, m, null);
		}else if(span.getAlign()==CssProperty.ALIGN_CENTER){
			//cv.drawBitmap(bm, x+borderWidth+(layoutW-bm.getWidth())/2, y+borderWidth, null);
			m.postTranslate( x+borderWidth+(layoutW-(w-2*borderWidth))/2, y+borderWidth);
			//Log.d("x:"+(x+borderWidth+(layoutW-bm.getWidth())/2),"y:"+(y+borderWidth));
			cv.drawBitmap(bm, m, null);
		}else{
			//cv.drawBitmap(bm, x+borderWidth, y+borderWidth, null);
			m.postTranslate( x+borderWidth, y+borderWidth);
			//Log.d("x:"+(x+borderWidth),"y:"+(y+borderWidth));
			cv.drawBitmap(bm, m, null);
		}
	}
	
	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return h;
	}
	
	/**
	 * 調整此物件在頁面的x,y位置
	 */
	public void setPosition(int x_,int y_){
		x=x_;
		y=y_;
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
	
	/**
	 * 判斷click事件是否作用在此圖片，如果是的話回傳DrawableOnClickResult標記圖片被點選且告知圖片的路徑
	 */
	@Override
	public DrawableOnClickResult onClicked(int inX, int inY) {
		// TODO Auto-generated method stub
		DrawableOnClickResult result = new DrawableOnClickResult();
		if(inX>=x && inX<=x+w && inY>=y && inY<=y+getHeight()){
			if(inX>=x+w*0.15f && inX<=x+w*0.85f && inY>=y+getHeight()*0.15f && inY<=y+getHeight()*0.85f){
				result.setSrc(span.content.toString());
				result.setStatus(true);
				result.setIsImg(true);
				result.setIdx(0);
			}
		}
		return result;
	}

	@Override
	public int getSpanIdx() {
		// TODO Auto-generated method stub
		return spanIdx;
	}

	@Override
	public void drawLine(Canvas cv, TextPaint tp, int startIdx, int endIdx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int isLinedContent() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public void drawRect(Canvas cv, TextPaint tp, int startIdx, int endIdx,
			int xOffset) {
		// TODO Auto-generated method stub
		
	}

}
