package org.iii.ideas.reader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class ImageViewer extends RelativeLayout{
	private int curX;
	private int curY;
	public ImageViewer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public ImageViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ImageViewer (Context context){
		super(context);
		// TODO Auto-generated constructor stub
	}
	  @Override 
	  public boolean onTouchEvent(MotionEvent event) {
	    switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN: {
	            curX = (int) event.getRawX();
	            curY = (int) event.getRawY();
	            break;
	        }

	        case MotionEvent.ACTION_MOVE: {
	            int x2 = (int) event.getRawX();
	            int y2 = (int) event.getRawY();
	            scrollBy(curX - x2 , curY - y2);
	            curX = x2;
	            curY = y2;
	            break;
	        }   
	        case MotionEvent.ACTION_UP: {
	            break;
	        }
	    }
	      return true; 
	  }
	
}
