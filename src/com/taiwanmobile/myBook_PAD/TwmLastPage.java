package com.taiwanmobile.myBook_PAD;

import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.text.TextPaint;
import android.view.MotionEvent;

/**
 * 處理台哥大書籍最後促銷頁
 * @author III
 * 
 */
public class TwmLastPage {
	private static boolean isButtonDown=false;
	private static boolean globalIsTrial=false;
	private static final int margin=10; 
	private static int buttonTop;
	private static int buttonBottom;
	private static int buttonLeft;
	private static int buttonRight;
	
	/**
	 * 傳入canvas將促銷頁的內容呈現出來。將螢幕分成三等份，分別呈現icon, 說明文字, 按鈕
	 * @param ctx 
	 * @param cv 將促銷頁呈現在哪個canvas
	 * @param isTrial 是否為試閱
	 * @param isNightMode 是否為夜間模式
	 */
	public static void drawLastpage(Context ctx,Canvas cv,Boolean isTrial,Boolean isNightMode){
		globalIsTrial=isTrial;
		Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_lastpage01);
		int regionH = cv.getHeight()/3;
		drawIcon(cv,margin,regionH-margin,cv.getWidth(),icon);
		//cv.drawBitmap(icon, 78, 61, null);
		//cv.drawBitmap(BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_bar05), 0, 436, null);
		TextPaint tp = new TextPaint();
		tp.setAntiAlias(true);
		if(isNightMode)
			tp.setColor(Color.WHITE);
		else
			tp.setColor(Color.BLACK);
		//tp.setTextSize(20);
		if(isTrial){
			String[] lines={ctx.getResources().getString(R.string.iii_last_page_hint_trial_1),ctx.getResources().getString(R.string.iii_last_page_hint_trial_2)}; 
			//String line1=ctx.getResources().getString(R.string.iii_last_page_hint_trial_1);
			//String line2=ctx.getResources().getString(R.string.iii_last_page_hint_trial_2);
			//cv.drawText(line1, (cv.getWidth()-tp.measureText(line1))/2, 220, tp);
			//cv.drawText(line2, (cv.getWidth()-tp.measureText(line2))/2, 260, tp);
			drawText(cv,regionH,regionH*2,cv.getWidth(),lines,tp);
			if(isButtonDown){
				drawButton(cv,regionH*2+margin,cv.getHeight()-margin,cv.getWidth(),BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial01a));
				//Log.d("LastPage:Draw","down");
				//cv.drawBitmap(BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial01a), 78, 374, null);
			}else{
				drawButton(cv,regionH*2+margin,cv.getHeight()-margin,cv.getWidth(),BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial01));
				//cv.drawBitmap(BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial01), 78, 374, null);
			}
		}else{
			Resources rs=ctx.getResources();
			/*String line1=ctx.getResources().getString(R.string.iii_last_page_hint_formal_1);
			String line2=ctx.getResources().getString(R.string.iii_last_page_hint_formal_2);
			String line3=ctx.getResources().getString(R.string.iii_last_page_hint_formal_3);
			cv.drawText(line1, (cv.getWidth()-tp.measureText(line1))/2, 220, tp);
			cv.drawText(line2, (cv.getWidth()-tp.measureText(line2))/2, 260, tp);
			cv.drawText(line3, (cv.getWidth()-tp.measureText(line3))/2, 300, tp);*/
			String[] lines={rs.getString(R.string.iii_last_page_hint_formal_1),rs.getString(R.string.iii_last_page_hint_formal_2),rs.getString(R.string.iii_last_page_hint_formal_3)};
			drawText(cv,regionH,regionH*2,cv.getWidth(),lines,tp);
			if(isButtonDown){
				//Log.d("LastPage:Draw","down");
				drawButton(cv,regionH*2+margin,cv.getHeight()-margin,cv.getWidth(),BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial02a));
				//cv.drawBitmap(BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial02a), 78, 374, null);
			}else
				//cv.drawBitmap(BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial02), 78, 374, null);
				drawButton(cv,regionH*2+margin,cv.getHeight()-margin,cv.getWidth(),BitmapFactory.decodeResource(ctx.getResources(),R.drawable.ani_trial02));
		}
	}
	
	/**
	 * 畫促銷頁icon部分
	 * @param cv canvas
	 * @param yTop icon區塊上方y值
	 * @param yBot icon區塊下方y值
	 * @param screenWidth 螢幕寬
	 * @param bm bitmap
	 */
	private static void drawIcon(Canvas cv,int yTop, int yBot, int screenWidth, Bitmap bm){
		try {
			int h = bm.getHeight();
			int w = bm.getWidth();
			float ratio = Math.min((float)(yBot-yTop)/h, (float)screenWidth/w);
			ratio = ratio<1?ratio:1;
			//Log.d("iconR",":"+ratio);
			//int newH = h*ratio;
			//int newW = w*ratio;
			Matrix m = new Matrix();
			m.postScale(ratio, ratio);
			m.postTranslate((screenWidth-w)/2, yTop+(yBot-yTop-h*ratio)/2);
			//bm = Bitmap.createScaledBitmap(bm, w, h, false);
			cv.drawBitmap(bm, m,null);
			bm.recycle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * 畫促銷頁促銷文字部分
	 * @param cv canvas
	 * @param yTop 促銷文字區塊上方y值
	 * @param yBot 促銷文字區塊下方y值
	 * @param screenWidth 螢幕寬
	 * @param lines 促銷文字(分行)
	 * @param paint text paint
	 */
	private static void drawText(Canvas cv,int yTop, int yBot, int screenWidth, String[] lines,TextPaint paint){
		try {
			int lineMaxLen=-1;
			for(String s:lines){
				if(s.length()>lineMaxLen)
					lineMaxLen=s.length();
			}
			int fontSize = (int) Math.min((yBot-yTop)/lines.length*0.6, (screenWidth-2*margin)/lineMaxLen);
			//if(fontSize>maxFontSize)
			//	fontSize=maxFontSize;
			paint.setTextSize(fontSize);
			int y=yTop+fontSize;
			for(int i=0;i<lines.length;i++,y+=(fontSize/0.6f)){
				cv.drawText(lines[i], (cv.getWidth()-paint.measureText(lines[i]))/2, y, paint);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 畫促銷頁促銷按鈕部分
	 * @param cv canvas
	 * @param yTop 促銷按鈕區塊上方y值
	 * @param yBot 促銷按鈕區塊下方y值
	 * @param screenWidth 螢幕寬
	 * @param bm 按鈕圖片
	 */
	private static void drawButton(Canvas cv,int yTop, int yBot, int screenWidth,Bitmap bm){
		try {
			int h = bm.getHeight();
			int w = bm.getWidth();
			//float ratio = Math.min((float)(yBot-yTop)/h, (float)screenWidth/w);
			//ratio = ratio<1?ratio:1;
			//h*=ratio;
			//w*=ratio;
			buttonLeft=(screenWidth-w)/2;
			buttonRight=buttonLeft+w;
			//buttonTop=yTop+(yBot-yTop-h)/2;
			buttonTop=yTop;
			buttonBottom=buttonTop+h;
			//bm = Bitmap.createScaledBitmap(bm, w, h, false);
			cv.drawBitmap(bm, buttonLeft, buttonTop, null);
			bm.recycle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * 處理促銷頁touch event
	 * @param ev canvas
	 * @param contentId content id，用來連結到server網頁
	 * @param title 書名，用來連結到server網頁
	 * @param authors 作者，用來連結到server網頁
	 * @param publisher 出版社，用來連結到server網頁
	 * @param ctx context
	 * @return 是否click在元件上
	 */
	public static boolean onClick(MotionEvent ev, String contentId,String title,String authors,String publisher,Context ctx){
		if(ev.getX()>=buttonLeft && ev.getX()<=buttonRight && ev.getY()>=buttonTop && ev.getY()<=buttonBottom){
			if(isButtonDown && ev.getAction()==MotionEvent.ACTION_UP){
				if(globalIsTrial){
					//試用
					ctx.startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(ctx.getResources().getString(R.string.iii_twm_last_trial)+contentId)) );					
				}else{
					//正式
					if(!authors.equals("")){
						ctx.startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(ctx.getResources().getString(R.string.iii_twm_last_formal)+URLEncoder.encode(authors))) );
					}else if (!publisher.equals("")){
						ctx.startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(ctx.getResources().getString(R.string.iii_twm_last_formal)+URLEncoder.encode(publisher))) );
					}else{
						ctx.startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(ctx.getResources().getString(R.string.iii_twm_last_formal)+URLEncoder.encode(title))) );	
					}					
				}
				isButtonDown=false;
			}else if(!isButtonDown){
				isButtonDown=true;
			}
			//Log.d("lastPage:omClick","isButtonDowm:"+isButtonDown);
			return true;
		}else{
			isButtonDown=false;
			return false;
		}

	}
}
