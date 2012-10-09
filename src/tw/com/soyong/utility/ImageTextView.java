package tw.com.soyong.utility;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

public class ImageTextView extends ImageButton {
	
	private static boolean DEBUG = false ;
//	static private final String TAG = "ImageTextView";
	
	Bitmap mBmp = null;
	String mStrOrg;
	String mStrTrl;
	boolean mIsShowTrl;
	boolean mIsShowOrg;
	float mFontSize = (float) 20.0 ;
	
	public ImageTextView(Context context) {
		super(context);
	}

	public ImageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ImageTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		drawBmp(canvas, null);
		
		int y = getHeight()-2;
		if (mIsShowTrl){
			y = drawText(canvas, mStrTrl , y );
			y -= 12 ;
		}
		
		if ( mIsShowOrg){
			y = drawText(canvas, mStrOrg , y );
		}
	}

	private int drawText(Canvas canvas, final String str, int y ) {
		
		if ( null == str || str.length()<=0){
			return y;
		}
		
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setTextSize(mFontSize);
		
		ArrayList<String> aStr = new ArrayList<String>();
		
		final int maxWidth = getWidth()-10;
		
		String[] lineArr = TextUtils.split(str, "[\n\r]");
		
		int count = 0 ;
		String strLine ;

		for ( String line : lineArr ){
			if ( line.length() <= 0 ){
				continue;
			}
			
			count = p.breakText(line, true, maxWidth, null);
			while (count < line.length() - 1) {
				strLine = getLine(line, count);
				aStr.add(strLine);
				line = line.substring(strLine.length());
				count = p.breakText(line, true, maxWidth, null);
			}
			aStr.add(line);
		}
		
		Paint.FontMetrics metric =  p.getFontMetrics();
		int fontHeight = (int) (-metric.top);
		
		int x ;
		int w = maxWidth;
		int strWidth;
		int lines = aStr.size();

		for ( int i = lines-1 ; i >= 0 ; i--){
			String line = aStr.get(i);
			strWidth = (int) p.measureText(line);
			x = (w-strWidth)/2+5;
			
			y -= fontHeight;
			drawText(canvas , line , x , y , p );
		}
		return y ;
	}

	private void drawBmp(Canvas canvas, Paint p) {
		final Bitmap bmp = mBmp;
		if ( null != bmp ){
			Rect dst = new Rect();
			final int w = bmp.getWidth();
			final int h = bmp.getHeight();
			final int width = getWidth()-4;
			final int height = getHeight()-2;
			
			int rateH;
			int rateW;
			int rate;
			
			rateW = width*100/w;
			rateH = height*100/h;
			rate = rateW < rateH ? rateW : rateH;
			
			int h1 = h*rate/100;
			int w1 = w*rate/100 ;
			
			if ( mIsShowOrg || mIsShowTrl){
				dst.set((width-w1)/2+2, 1, w1, h1);
			}else{
				dst.set((width-w1)/2+2, (height-h1)/2+1, w1, h1);
			}
			
			if ( DEBUG ) Log.e("ImageTextView" , "scr w:"+width+ " scr h:"+height +"bmp w:" +w1 +"bmp h:"+h1 + " w:" + w + " h:"+h);
				
			canvas.drawBitmap(mBmp, null, dst, p);
		}
	}

	private final void drawText(Canvas canvas, String line, int x, int y, Paint p) {
		p.setColor(Color.rgb(56, 115, 33));
		p.setFakeBoldText(false);
		canvas.drawText(line , x , y , p);
	}

	private final String getLine(String str, int count) {
		int i = count -1;
		for ( ; i >= 0 ; i--){
			if ( false == Util.isAlphabet(str.charAt(i))) {
				break;
			}
		}
		return str.substring(0 , i+1);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		mBmp = bm ;
		invalidate();
	}
	
	public final void setOrg(String str){
		mStrOrg = str ;
		invalidate();
	}
	
	public final void setTrl(String str){
		mStrTrl = str ;
		invalidate();
	}
	
	public final void setDisplayOrg(boolean b){
		mIsShowOrg = b ;
		invalidate();
	}
	
	public final void setDisplayTrl(boolean b){
		mIsShowTrl = b ;
		invalidate();
	}
	
	public final void setFontSize( float f){
		//mFontSize = f+ 2.0f;
		//mFontSize = f ;
		
		final float density = getContext().getResources().getDisplayMetrics().density;
		float dpi = density * 160;
		float px = f * dpi / 72 ;
		mFontSize = px ;
		
	}
}
