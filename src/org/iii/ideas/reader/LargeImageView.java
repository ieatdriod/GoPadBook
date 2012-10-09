package org.iii.ideas.reader;

import java.io.File;

import org.iii.ideas.android.general.AndroidLibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
/**
 * 大圖片瀏覽view，具備拖曳瀏覽/zoom in/zoom out等功能
 * @author created as a tutorial by XCaffeinated from anddev, customized by JP  
 *
 */
public class LargeImageView extends View {
	private Bitmap largeBm=null; //bitmap large enough to be scrolled
	private Bitmap fullBm=null;
	private Rect displayRect = null; //rect we display to
	private Rect scrollRect = null; //rect we scroll over our bitmap with
	private int scrollRectX = 0; //current left location of scroll rect
	private int scrollRectY = 0; //current top location of scroll rect
	private float scrollByX = 0; //x amount to scroll by
	private float scrollByY = 0; //y amount to scroll by
	private float startX = 0; //track x from one ACTION_MOVE to the next
	private float startY = 0; //track y from one ACTION_MOVE to the next
	private int displayWidth=0;
	private int displayHeight=0;
	private Context ctx;
	private float scale=1f;
	private static int DISPLAY_BOUND=800;
	private static float MIN_SCALE=0.1f;

	private static final int MAX_SIZE=800;
	private int buttonH=40;
	public LargeImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		ctx=context;
		initialize();
	}
	
	public LargeImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx=context;
		initialize();
	}
	
	public LargeImageView(Context context){
		super(context);
		// TODO Auto-generated constructor stub
		ctx=context;
		initialize();
	}
	
	/**
	 * 呼叫此class所有Bitmap物件的recycle method，釋放記憶體
	 */
	public void recycle(){
		if(largeBm!=null){
			largeBm.recycle();
			largeBm=null;
		}
		if(fullBm!=null){
			fullBm.recycle();
			fullBm=null;
		}
	}
	
	/**
	 * 初始化
	 */
	private void initialize(){
    	Display display = ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        displayWidth=this.getWidth();//display.getWidth();
//        displayHeight=this.getHeight()-buttonH;//display.getHeight()-buttonH;
        displayWidth= display.getWidth();
        displayHeight= display.getHeight()-buttonH;
		displayRect = new Rect(0, 0, displayWidth, displayHeight);
		scrollRect = new Rect(0, 0, displayWidth, displayHeight);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        displayWidth=w;//display.getWidth();
        displayHeight=h-buttonH;//display.getHeight()-buttonH;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * 若參數enlarge為true，將圖片往上放大一級，否則縮小一級
	 * @param enlarge  true放大，false縮小
	 */
	public void resize(boolean enlarge){
		if(fullBm!=null && largeBm!=null){
			if(enlarge){
				int w = largeBm.getWidth();
				int h = largeBm.getHeight();
				scale *= 1.1f;
				float maxScale = Math.min((float)DISPLAY_BOUND/w, (float)DISPLAY_BOUND/h);
				if(scale>maxScale)
					scale=maxScale;
				Matrix m = new Matrix();
				m.postScale(scale, scale);
				largeBm = Bitmap.createBitmap(fullBm, 0, 0, fullBm.getWidth(), fullBm.getHeight(), m, false);
				if(largeBm.getWidth()>fullBm.getWidth() ||largeBm.getHeight()>fullBm.getHeight()||largeBm.getWidth()==0|| largeBm.getHeight()==0)
					largeBm=fullBm;
			}else{
				float tempScale = scale*0.9f;
				if(tempScale>MIN_SCALE && fullBm.getHeight()*tempScale>=1 && fullBm.getWidth()*tempScale>=1){
					scale=tempScale;
					Matrix m = new Matrix();
					m.postScale(scale, scale);
					try{
					largeBm = Bitmap.createBitmap(fullBm, 0, 0, fullBm.getWidth(), fullBm.getHeight(), m, false);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			//Log.d("resize","invalidate");
			displayRect.set(0,0, largeBm.getWidth()<displayWidth?largeBm.getWidth():displayWidth, largeBm.getHeight()<displayHeight?largeBm.getHeight():displayHeight);
			invalidate();
		}
	}
	
	/**
	 * 讓此view顯示傳入的image
	 * @param imgFile image file
	 * @param screenWidth 螢幕寬
	 * @param screenHeight 螢幕高
	 * @return 是否成功
	 */
	public boolean setImage(File imgFile,int screenWidth,int screenHeight){
		DISPLAY_BOUND=(int) Math.max(screenHeight*1.1f, screenWidth*1.1f);
		if(DISPLAY_BOUND>MAX_SIZE)
			DISPLAY_BOUND=MAX_SIZE;
		fullBm = AndroidLibrary.decodeBitmap(imgFile, DISPLAY_BOUND, DISPLAY_BOUND, true);
		scale=1;
		if(fullBm==null){
			return false;
		}else{
			//Log.d("set","img:draw");
			DISPLAY_BOUND=Math.max(fullBm.getWidth(), fullBm.getHeight());
			largeBm=fullBm;
			displayRect.set(0,0, largeBm.getWidth()<displayWidth?largeBm.getWidth():displayWidth, largeBm.getHeight()<displayHeight?largeBm.getHeight():displayHeight);
			//Log.d("setImage","rect:"+displayRect);
			Log.d("displayRect:","w:"+displayRect.width());
			Log.d("displayRect:","h:"+displayRect.height());
			invalidate();
			return true;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(largeBm==null)
			return true;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// Remember our initial down event location.
				startX = event.getRawX();
				startY = event.getRawY();
				//Log.d("down","down");
				break;

			case MotionEvent.ACTION_MOVE:
				//** Set displayRect size**//
				if(largeBm.getWidth()>displayWidth||largeBm.getHeight()>displayHeight){			
				float x = event.getRawX();
				float y = event.getRawY();
				// Calculate move update. This will happen many times
				// during the course of a single movement gesture.
				scrollByX = x - startX; //move update x increment
				scrollByY = y - startY; //move update y increment
				startX = x; //reset initial values to latest
				startY = y;
				invalidate(); //force a redraw
				//Log.d("up","up");
				break;
		}
		}
		return true; //done with this event so consume it
	}

	public void onDraw(Canvas canvas) {
		//Log.d("draw","draw0");
		if(largeBm !=null){
			// Our move updates are calculated in ACTION_MOVE in the opposite direction
			// from how we want to move the scroll rect. Think of this as dragging to
			// the left being the same as sliding the scroll rect to the right.
			//Log.d("draw","draw");
			//** Set displayRect size**//
			displayRect.set(0,0, largeBm.getWidth()<displayWidth?largeBm.getWidth():displayWidth, largeBm.getHeight()<displayHeight?largeBm.getHeight():displayHeight);

			int newScrollRectX = scrollRectX - (int)scrollByX;
			int newScrollRectY = scrollRectY - (int)scrollByY;
			//Log.d("bmw:"+largeBm.getWidth(),"bmH:"+largeBm.getHeight());
			//Log.d("0newscrollX:"+newScrollRectX,"newscrolly:"+newScrollRectY);
			// Don't scroll off the left or right edges of the bitmap.
			if (newScrollRectX < 0){
				newScrollRectX = 0;
			}else if (largeBm.getWidth()>displayWidth &&newScrollRectX > (largeBm.getWidth() - displayWidth)){
				newScrollRectX = (largeBm.getWidth() - displayWidth);
			}else if (largeBm.getWidth()<=displayWidth && newScrollRectX > 0){
				newScrollRectX = 0;
			}
			//Log.d("1newscrollX:"+newScrollRectX,"newscrolly:"+newScrollRectY);
			// Don't scroll off the top or bottom edges of the bitmap.
			if (newScrollRectY < 0){
				newScrollRectY = 0;
			}else if (largeBm.getHeight()>displayHeight && newScrollRectY > (largeBm.getHeight() - displayHeight)){
				newScrollRectY = (largeBm.getHeight() - displayHeight);
			}else if (largeBm.getHeight()<=displayHeight && newScrollRectY > 0){
				newScrollRectY = 0;
			}
			//Log.d("2newscrollX:"+newScrollRectX,"newscrolly:"+newScrollRectY);
			int scrollEndX=newScrollRectX + displayWidth,
			scrollEndY=newScrollRectY + displayHeight;
			if(scrollEndX>largeBm.getWidth()){
				scrollEndX=largeBm.getWidth();
			}
			if(scrollEndY>largeBm.getHeight()){
				scrollEndY=largeBm.getHeight();
			}	
			// We have our updated scroll rect coordinates, set them and draw.
			scrollRect.set(newScrollRectX, newScrollRectY, 
				scrollEndX, scrollEndY);
			Paint paint = new Paint();
			canvas.drawBitmap(largeBm, scrollRect, displayRect, paint);
			// Reset current scroll coordinates to reflect the latest updates, 
			// so we can repeat this update process.
			scrollRectX = newScrollRectX;
			scrollRectY = newScrollRectY;
			//Log.d("screll","rect:"+scrollRect);
			//Log.d("scrollx:"+scrollRectX,"scrollRectY:"+scrollRectY);
			//Log.d("scrollx:"+displayRect,"scrollRectY:"+scrollRectY);
		}

	}
}
