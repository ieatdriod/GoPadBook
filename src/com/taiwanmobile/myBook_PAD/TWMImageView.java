package com.taiwanmobile.myBook_PAD;

import com.gsimedia.gsiebook.common.ImageTool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class TWMImageView extends ImageView {
	public TWMImageView(Context context) {
		super(context);
	} 
	public TWMImageView(Context context,AttributeSet attr){
		super(context,attr);
	}
	public static void setSize(int w, int h){
		ih = h;
		iw = w;
	}
	private static int ih = 100;
	private static int iw = 100;

	/*
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable aDrawable = this.getDrawable();
		if (aDrawable != null){
			int w = aDrawable.getIntrinsicWidth();
			int h = aDrawable.getIntrinsicHeight();
            int widthSize = resolveSize(w, widthMeasureSpec);
            int heightSize = resolveSize(h, heightMeasureSpec);
		    setMeasuredDimension(widthSize, heightSize);
		}else{
		   setMeasuredDimension(1, 1);
		}
	}
	*/
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		Bitmap resizedBitmap = null;
		if(bm!=null){
			
			this.setScaleType(ScaleType.MATRIX);
			Log.d("TWM","view width:"+getWidth());
			Log.d("TWM","bitmap width:"+bm.getWidth());
			
			//find scale factor
			float scale = (float)ImageTool.FindBestFitFract(bm.getWidth(),bm.getHeight(),iw,ih);
			
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			
			resizedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			super.setImageBitmap(resizedBitmap);
			this.requestLayout();
		}else{
			super.setImageBitmap(bm);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
       Drawable aDrawable = this.getDrawable();
       if ( null != aDrawable){
	       int aTargetWidth = this.getWidth();
	       int aTargetHeight = this.getHeight();
	       int aSrcWidth = aDrawable.getMinimumWidth();
	       int aSrcHeight= aDrawable.getMinimumHeight();
    	   float scale = (float)ImageTool.FindBestFitFract(aSrcWidth,aSrcHeight,aTargetWidth,aTargetHeight);
    	   
    	   Matrix matrix = new Matrix();
    	   matrix.setScale(scale, scale);
    	   matrix.postTranslate((aTargetWidth - scale * aSrcWidth )/2,(aTargetHeight - scale * aSrcHeight));
    	   this.setImageMatrix(matrix);
    	   /*
    	   Rect src = new Rect();
    	   Rect dest = new Rect();
    	   src.left = 0;
    	   src.top = 0;
    	   src.right = resizedBitmap.getWidth();
    	   src.bottom = resizedBitmap.getHeight();
    	   dest.top = (int) (aTargetHeight - scale * aSrcHeight);
    	   dest.bottom = aTargetHeight;
    	   dest.left = (int) Math.max(0, (aTargetWidth - scale * aSrcWidth )/2); 
    	   dest.right = (int) (dest.left + aSrcWidth*scale);
    	   canvas.drawBitmap(resizedBitmap,src,dest,aPaint);
    	   */
       }
       super.onDraw(canvas);
	}
	
}
