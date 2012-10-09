package org.iii.ideas.reader.renderer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.iii.ideas.reader.turner.AnimateDrawable;
import org.iii.ideas.reader.turner.PageTurner;
import org.iii.ideas.reader.underline.Underline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;

import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.TwmLastPage;
import com.gsimedia.animation.BaseAnimation;
import com.gsimedia.animation.CurlAnimation;

/**
 * 負責處理呈現epub內容和touch event的view。主要功能: 將renderer產生的ReaderDrawable畫成bitmap, motion event的處理, 翻頁特效, 拖曳劃線的處理。
 * @author III
 * 
 */
public class EpubView extends View{
	private ArrayList<ReaderDrawable> contentToDraw;
	private Timer iTimer =null;
	private Context ctx;
	private final static int DCLICK_TIME_INTERVAL = 250;
	//private Display display;
	public EpubView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		ctx=context;
		initialize();
	}
	
	public EpubView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx=context;
		initialize();
	}
	public EpubView(Context context){
		super(context);
		// TODO Auto-generated constructor stub
		ctx=context;
		initialize();
	}
	private EpubViewCallback ecb;
	
	/**
	 * 設定EpubView回call reader主程式activity的管道。
	 * @param ecb_ 實作EpubViewCallback interface的class
	 */
	public void setCallback(EpubViewCallback ecb_){
		ecb=ecb_;
	}
	
	private int backgroundIdx=-1;
	private boolean shouldSetBackground=false;
	
	/**
	 * 根據background drawable的index(在 array裡的index，
	 * 可參照strings.xml裡的"iii_reader_setting_book_background_style_value")設定背景圖片。
	 * @param isNightMode 是否為夜間模式，若為夜間模式不呈現背景
	 */
	public void setBackGroundIndex(boolean isNightMode){
		//Log.d("setBackGroundIndex","in");
        if(isNightMode){
        	setBackgroundDrawable(null);
        	setBackgroundColor(Color.BLACK);
        	backgroundIdx=-1;
        }else{
        	int tempIdx = RendererConfig.getBackgroundIndex(ctx,ecb.getDeliverId());
        	//Log.d("setBackGroundIndex","idx:"+tempIdx);
        	if(tempIdx!=backgroundIdx){
        		backgroundIdx=tempIdx;
        		shouldSetBackground=true;
        		//setBackgroundDrawable(RendererConfig.getBackgroundByIndex(backgroundIdx, ctx));
        	}
        }
	}
	
	private void setReaderBackground(){
		if(shouldSetBackground && backgroundIdx>0){
			setBackgroundResource(backgroundIdx);
			
			
			//BitmapFactory.Options option = new BitmapFactory.Options();
			//option.inSampleSize=2;
			//bgBm=BitmapFactory.decodeResource(ctx.getResources(), backgroundIdx, option);
			//recycle();
		}
	}
	
	private boolean isLastPage=false;
	/**
	 * 設定是否為末頁(末頁為促銷頁面，非書籍內容)。
	 * @param isLast
	 */
	public void setIsLastPage(boolean isLast){
		isLastPage=isLast;
	}
	
	private int startSpan=0;
	private int startDrawableIdx=0;
	private int endDrawableIdx=0;
	private int startIdx=0;
	private int endSpan=0;
	private int endIdx=0;
	private boolean isStartSelected=false;
	private boolean isEndSelected=false;
	
	
	private ArrayList<Underline> ulForDelete = null;
	/**
	 * 若在underline移除模式，處理要移除哪段劃線內容
	 * @param event 
	 * @return 是否消耗此event
	 */
	public boolean checkUnderlineRemoval(MotionEvent event){
		ulForDelete = null;
		if(event.getAction()!=MotionEvent.ACTION_UP){
			return true;
		}
		for(int i=0;i<contentToDraw.size();i++){
			DrawableOnClickResult result = contentToDraw.get(i).onClicked((int)event.getX(), (int)event.getY());
			if(result.getStatus()){
				ulForDelete = ecb.onDeleteUnderline(contentToDraw.get(i).getSpanIdx(), result.getIdx());
				invalidate();
				return true;
			}
		}
		return true;
	}
	
	/**
	 * 在劃線模式下處理劃哪段線
	 * @param event
	 * @return 是否消耗此event
	 */
	public boolean checkUnderline(MotionEvent event){
		for(int i=0;i<contentToDraw.size();i++){
			DrawableOnClickResult result = contentToDraw.get(i).onClicked((int)event.getX(), (int)event.getY());
			if(result.getStatus()){
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						startSpan=contentToDraw.get(i).getSpanIdx();
						startIdx=result.getIdx();
						startDrawableIdx=i;
						isStartSelected=true;
						break;
					case MotionEvent.ACTION_MOVE:
						if(isStartSelected){
							endSpan=contentToDraw.get(i).getSpanIdx();
							endIdx=result.getIdx();
							isEndSelected=true;
							endDrawableIdx=i;
							invalidate();
						}else{
							startSpan=contentToDraw.get(i).getSpanIdx();
							startIdx=result.getIdx();
							startDrawableIdx=i;
							isStartSelected=true;
						}
						break;
					case MotionEvent.ACTION_UP:
						if(isStartSelected){
							endSpan=contentToDraw.get(i).getSpanIdx();
							endDrawableIdx=i;
							endIdx=result.getIdx();
							isEndSelected=true;
						}
						break;
				}
			}
		}
		if(event.getAction()==MotionEvent.ACTION_UP && isEndSelected){
			ecb.onGetUnderline(startSpan, startIdx, endSpan, endIdx);
			isStartSelected=false;
			isEndSelected=false;
		}
		return true;
	}
	//** view the image
	boolean timeout=true; 
	Handler iTimeoutHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				ecb.handleShowMenu();				
				cancelTimer();
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	private void cancelTimer(){
		if(iTimer!= null){
			iTimer.cancel();
			iTimer= null;
		}
	}
	
	/**
	 * 檢查是否點選到hyperlink
	 * @param event
	 * @return 是否消耗此event
	 */
	public boolean checkLinks(MotionEvent event){
		if(event.getAction()==MotionEvent.ACTION_UP){
			for(int i=0;i<contentToDraw.size();i++){
				DrawableOnClickResult result = contentToDraw.get(i).onClicked((int)event.getX(), (int)event.getY());
				if(result.getStatus()){ 
					if(result.isLink()){
						ecb.onLinkClicked(result.getSrc());
						return true;
					}else if(result.isImg()){
						try {
                            long thisTime = System.currentTimeMillis();
							BitmapFactory.Options option = new BitmapFactory.Options();
							option.inJustDecodeBounds=true;
							BitmapFactory.decodeFile(result.getSrc(),option);
							if(option.outHeight>=getHeight() || option.outWidth>=getWidth()){
								//** view the image
									if (thisTime - lastTouchTime < 250){
										cancelTimer();
										ecb.onImgClicked(result.getSrc());
										return true;
									}else{
										lastTouchTime = thisTime;
										if (this.iTimer == null) {
											iTimer = new Timer();
											iTimer.schedule(new TimerTask() {

												@Override
												public void run() {
													iTimeoutHandler.sendEmptyMessage(0);
												}

											}, DCLICK_TIME_INTERVAL);
										}
										return true;
							}
							}

						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Drag start pos.
	 */
	private int dragx = 0;
	private int dragy = 0;

	private int dragx1 = 0;
	private int dragy1 = 0;
	
	/**
	 * Flag: are we being moved around by dragging.
	 */
	private int iTouchMode= EModeNone;
	private static final int EModeNone = 0;
	private static final int EModeDrag = 1;
	private static final int EModeZoom = 2;
		
	private int mTouchX = 0;
	private int mTouchY = 0;	
	
	private boolean bPointMoveOver = false;
	private long lastTouchTime = -1;
	public boolean onTouchEvent(MotionEvent event){
		boolean result = false;
		
		super.onTouchEvent(event);
		
		//20110510 benson modified for fixing wrong direction of animation while backing to previous page from last page.
		mTouchX = (int)event.getX();
		mTouchY = (int)event.getY();
		if(isLastPage){
			boolean temp=TwmLastPage.onClick(event,ecb.getContentId(),ecb.getTitleFromProfile(),ecb.getAuthors(),ecb.getPublisher(),ctx);
			redrawLastPage();
			result = temp;
		}else if(ecb.isUnderlineOpen()){
			result = checkUnderline(event);
		}else if(ecb.isUnderlineRemovalOpen()){
			checkUnderlineRemoval(event);
			result = true;
		}else if(checkLinks(event)){
			result = true;
		}else{
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				iMode = MODE_NORMAL;
				
				dragx = dragx1 = (int)event.getX();
				dragy = dragy1 = (int)event.getY();
				bPointMoveOver = false;
				
				ecb.disableFirstPage();
				result = true;
				break;
			case MotionEvent.ACTION_UP:
				
				if(!bPointMoveOver){
					dragx1 = (int)event.getX();
					dragy1 = (int)event.getY();
					if (Math.abs(dragx-dragx1) > 50 && Math.abs(dragy-dragy1) > 50){
						bPointMoveOver = true;
					}
				}
				
				/**
				 * add for multitouch to zoom
				 */
				if(iMode == MODE_NORMAL)
					result = false;
				else 
					result = true;
				iMode = MODE_NORMAL;
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN:
				/**
				 * add for multitouch to zoom
				 */
				if(bContextMenuOpen == false){
					iDownDistance = EpubView.getPointDistance(event);
					iDownFontSizeIdx = ecb.getFontSize();
					iMode = MODE_ZOOM;
				}
				result = true;
				break;
			case MotionEvent.ACTION_POINTER_1_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
				//prevent incorrectly set font size
				if(iMode == MODE_ZOOM)
					iMode = MODE_ZOOM_FINISHED;
				result = true;
				break;
			case MotionEvent.ACTION_MOVE:
				
				if(!bPointMoveOver){
					dragx1 = (int)event.getX();
					dragy1 = (int)event.getY();
					if (Math.abs(dragx-dragx1) > 50 && Math.abs(dragy-dragy1) > 50){
						bPointMoveOver = true;
					}
				}
				
				if(bContextMenuOpen == false){
					if(iMode == MODE_ZOOM)
						handleZoom(event);
				}
				result = true;
				break;
			}
			
			/*if(event.getX() >= (2*getWidth())/3  ){
				//pageDown
				if(event.getAction()==MotionEvent.ACTION_UP)
					ecb.pageDown();
				return true;
			}else if(event.getX() <= getWidth()/3 ){
				//pageUp
				if(event.getAction()==MotionEvent.ACTION_UP)
					ecb.pageUp();
				return true;
			}*/
		}
		/**
		 * don't allow more than 60 motion events per second
		 */
		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
		}
		return result;
	}
	/**
	 * multitouch to zoom related
	 */
	private void handleZoom(MotionEvent aEvent){
		float aUpDistance = EpubView.getPointDistance(aEvent);
		int aSize = iDownFontSizeIdx;
		if(aSize<0 || iDownFontSizeIdx <0){
			Log.e("TWM","font index incorrect");
		}else{
			aSize = aSize+(int)(aUpDistance - iDownDistance)/50;
			ecb.setFontSize(aSize);
		}
			
	}
	private static final int MODE_NORMAL = 0;
	private static final int MODE_ZOOM = 1;
	private static final int MODE_ZOOM_FINISHED = 2;
	private int iMode = MODE_NORMAL;
	private float iDownDistance = 0;
	private int iDownFontSizeIdx = 0;
	private static float getPointDistance(MotionEvent event){
		if(WrapMultiTouch.getPointerCount(event)!=2){
			return 0f;
		}
		
		int aPIndex0 = WrapMultiTouch.getPointerId(event,0);
		int aPIndex1 = WrapMultiTouch.getPointerId(event,1);
		float aX0 = WrapMultiTouch.getX(event,aPIndex0);
		float aX1 = WrapMultiTouch.getX(event,aPIndex1);
		float aY0 = WrapMultiTouch.getY(event,aPIndex0);
		float aY1 = WrapMultiTouch.getY(event,aPIndex1);
		float aDiffX = Math.abs(aX0-aX1);
		float aDiffY = Math.abs(aY1-aY0);
		PointF aDistance = new PointF(aDiffX,aDiffY);
		
		return aDistance.length();
	}
	/**
	 * wrap multi touch support
	 */
	static class WrapMultiTouch{
		static int getPointerCount(MotionEvent ev){
			int iRet = 0;
			try{
				iRet = ev.getPointerCount();
			}catch (Throwable e){
			}
			return iRet;
		}
		static int getPointerId(MotionEvent ev, int id){
			int iRet = 0;
			try{
				iRet = ev.getPointerId(id);
			}catch (Throwable e){
			}
			return iRet; 
		}
		static float getX(MotionEvent ev, int id){
			float iRet = 0;
			try{
				iRet = ev.getX(id);
			}catch (Throwable e){
			}
			return iRet;
		}
		static float getY(MotionEvent ev, int id){
			float iRet = 0;
			try{
    			iRet = ev.getY(id);
			}catch (Throwable e){
			}
			return iRet;
		}
	}
	
	
	/*public void setBookmark(boolean isBookmark){
		
	}*/
	
	private boolean showManual=false;
	private boolean isCoverTaskStart=false;
	/**
	 * 設定是否顯示manual
	 * @param shouldShow 是否顯示manual
	 */
	public void setShowManual(boolean shouldShow){
		showManual=shouldShow;
	}
	private ThreadHandler thandler;
	private Bitmap curPageBitmap;
	private AnimateDrawable prePageDrawable;
	private Canvas cacheCanvas;
	private int screenWidth;
	private int screenHeight;
	
	private BaseAnimation curlRotation = null;
	private Bitmap foldingBitmap_right;
	private Bitmap foldingBitmap_left;
	private Bitmap annotation;

	private void initialize(){
//		Display display = ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		screenWidth=this.getWidth();//display.getWidth();
		screenHeight=this.getHeight();//display.getHeight();
		thandler = new ThreadHandler();
		contentToDraw=new ArrayList<ReaderDrawable>();
		showManual=false;
		cacheCanvas = new Canvas();
		
		foldingBitmap_right = drawableToBitmap(getResources().getDrawable(R.drawable.folding_right));
		foldingBitmap_left = drawableToBitmap(getResources().getDrawable(R.drawable.folding_left));
		annotation = drawableToBitmap(getResources().getDrawable(R.drawable.gsi_tab01));
		//display = ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	}
	
	public void ResetBitmap()
	{
		if(curPageBitmap != null){
			//curPageBitmap.recycle();
			curPageBitmap = null;
		}
		if(prePageBitmap!=null){
//			prePageBitmap.recycle();
			prePageBitmap = null;
		}
	}
	
	private PageTurner turner;
	/**
	 * 設定換頁特效
	 * @param turner_ page turner
	 */
	public void setTurner(PageTurner turner_){
		turner=turner_;
	}
	private boolean shouldStartAnimation=false;
	
	BitmapDrawable prePageBitmapDrawable;
	//Bitmap newBm;
	
	private void redrawLastPage(){
		if(isLastPage){
			cacheCanvas.drawColor(0,PorterDuff.Mode.CLEAR);
			if(backgroundIdx==-1){
				cacheCanvas.drawColor(Color.BLACK);
			}else{
				//cacheCanvas.drawBitmap(bgBm, 0, 0, null);
				Drawable bg = this.getBackground();
				bg.setBounds(0, 0, screenWidth, screenHeight);
				bg.draw(cacheCanvas);
			}
			TwmLastPage.drawLastpage(ctx, cacheCanvas,ecb.getIsTrial(),ecb.getIsNightMode());
			invalidate();
		}
	}
	
	/**
	 * 離開閱讀時清除資料
	 */
	public void clearData(){
		try {
			if(prePageBitmap!=null){
				prePageBitmap.recycle();
			}
			if(curPageBitmap!=null){
				curPageBitmap.recycle();
			}
			if (contentToDraw != null) {
			contentToDraw.clear();
			}
			
			if (cacheCanvas != null) {
				cacheCanvas = null;
			}
			if (prePageBitmapDrawable != null) {
			prePageBitmapDrawable=null;
			}
			
			if(curlRotation!=null){
				curlRotation = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.screenHeight=h;
		this.screenWidth=w;
		if(ecb!=null){
		ecb.onViewSizeChanged();
	}
	}
	
	private Bitmap prePageBitmap = null;
	private BaseAnimation.PageIndex pageIndex = BaseAnimation.PageIndex.current;
	public void setPageIndex(BaseAnimation.PageIndex pIndex){
		switch(pIndex){
		case previous:
			pageIndex = BaseAnimation.PageIndex.previous;
			break;
		case current:
			pageIndex = BaseAnimation.PageIndex.current;
			break;
		case next:
			pageIndex = BaseAnimation.PageIndex.next;
		}
	}
	/**
	 * 將renderer傳的內容畫成bitmap。為了處理翻頁於兩張bitmap間切換
	 * @param content 頁面內容
	 * @param isPageUp 是否為上一頁
	 * @param isVertical 是否為直書書
	 */
	public void drawContent(ArrayList<ReaderDrawable> content,int isPageUp,boolean isVertical,
			boolean isCurPageBookmark, boolean isCurPageAnnotated){
		//Log.d("EV","width:"+this.getWidth());
		//Log.d("EV","h:"+this.getHeight());
		//Log.d("draw:Content","draw");
		//Log.e("JP","cache density:"+cacheCanvas.getDensity());
		setReaderBackground();
		int animationType = turner.getType();
		if(animationType != PageTurner.ROTATION){
		//if(Boolean.TRUE){
			if(curPageBitmap==null){
				curPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
				if(cacheCanvas == null){
					cacheCanvas = new Canvas();
				}else{
					cacheCanvas = null;
					cacheCanvas = new Canvas();
				}
				cacheCanvas.setBitmap(curPageBitmap);
			}else{
				//newBm.recycle();
				//if(newBm!=null)
				//	newBm.recycle();
				if(prePageBitmap==null)
					prePageBitmap = Bitmap.createBitmap(curPageBitmap);
				Bitmap temp = prePageBitmap;
				prePageBitmap=curPageBitmap;
				curPageBitmap=temp;
				cacheCanvas.setBitmap(curPageBitmap);
				//newBm=localNewBm;
				prePageBitmapDrawable= new BitmapDrawable(prePageBitmap);
//				turner.setOrientation(isPageUp, isVertical);
			}
			turner.setOrientation(isPageUp, isVertical);
			
			shouldStartAnimation=true;
			cacheCanvas.drawColor(0,PorterDuff.Mode.CLEAR);
			if(backgroundIdx==-1){
				cacheCanvas.drawColor(Color.BLACK);
			}else{
				//cacheCanvas.drawBitmap(bgBm, 0, 0, null);
				Drawable bg = this.getBackground();
				bg.setBounds(0, 0, screenWidth, screenHeight);
				bg.draw(cacheCanvas);
			}
		}else{
			//new rotating method
			if(curPageBitmap == null){
				curPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
				
				if(cacheCanvas == null){
					cacheCanvas = new Canvas();
				}else{
					cacheCanvas = null;
					cacheCanvas = new Canvas();
				}
				
				cacheCanvas.setBitmap(curPageBitmap);
				
				if(curlRotation == null){
					curlRotation = new CurlAnimation(prePageBitmap, curPageBitmap);
				}
			}else{
				//newBm.recycle();
				//if(newBm!=null)
				//	newBm.recycle();
				if(prePageBitmap == null)
					prePageBitmap = Bitmap.createBitmap(curPageBitmap);
				Bitmap temp = prePageBitmap;
				prePageBitmap=curPageBitmap;
				curPageBitmap=temp;
				cacheCanvas.setBitmap(curPageBitmap);
				//newBm=localNewBm;
				prePageBitmapDrawable= new BitmapDrawable(prePageBitmap);
				turner.setOrientation(isPageUp,isVertical);
				
				if(curlRotation == null){
					curlRotation = new CurlAnimation(prePageBitmap, curPageBitmap);
				}else{
					curlRotation.setBitmap(prePageBitmap, curPageBitmap);
				}
				final int w = getWidth();
				final int h = getHeight();
				switch (pageIndex) {
					case current:
						switch (curlRotation.getPageToScrollTo()) {
							case current:
								curlRotation.terminate();
								break;
							case previous:
								//20110510 benson modified for speeding rotation
								curlRotation.startAutoScrolling(isVertical, false, -8, BaseAnimation.Direction.rightToLeft, w, h, mTouchX, mTouchY, 3);
								break;
							case next:
								curlRotation.startAutoScrolling(isVertical, false, 8, BaseAnimation.Direction.leftToRight, w, h, mTouchX, mTouchY, 3);
								break;
						}
						break;
					case previous:
					{
						if(isVertical)
							curlRotation.startAutoScrolling(isVertical, true, -8, BaseAnimation.Direction.leftToRight, w, h, mTouchX, mTouchY, 3);
						else
							curlRotation.startAutoScrolling(isVertical, true, 8, BaseAnimation.Direction.rightToLeft, w, h, mTouchX, mTouchY, 3);
					}
						break;
					case next:
					{
						if(isVertical)
							curlRotation.startAutoScrolling(isVertical, true, 8, BaseAnimation.Direction.rightToLeft, w, h, mTouchX, mTouchY, 3);
						else
							curlRotation.startAutoScrolling(isVertical, true, -8, BaseAnimation.Direction.leftToRight, w, h, mTouchX, mTouchY, 3);
					}
						break;
				}
			}
			shouldStartAnimation = true;
			cacheCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
			if(backgroundIdx==-1){
				cacheCanvas.drawColor(Color.BLACK);
			}else{
				//cacheCanvas.drawBitmap(bgBm, 0, 0, null);
				Drawable bg = this.getBackground();
				bg.setBounds(0, 0, screenWidth, screenHeight);
				bg.draw(cacheCanvas);
			}
		}
		
		if(content==null)
			contentToDraw=new ArrayList<ReaderDrawable>();
		else
			contentToDraw=content;
		//isStart=false;
		if(isLastPage){
			TwmLastPage.drawLastpage(ctx, cacheCanvas,ecb.getIsTrial(),ecb.getIsNightMode());
		}else{

			for(int i=0;i<contentToDraw.size();i++){
				//Log.d("EpubView:onDraw","draw:"+i);
				contentToDraw.get(i).draw(cacheCanvas);
			}
		}

		if(isCurPageBookmark){	
			if(isVertical){
				cacheCanvas.drawBitmap(foldingBitmap_left, 0, 0, new Paint());
			}else{
				cacheCanvas.drawBitmap(foldingBitmap_right, screenWidth - foldingBitmap_right.getWidth(), 0, new Paint());
			}
		}
		
		if(isCurPageAnnotated){		
			cacheCanvas.drawBitmap(annotation, screenWidth - annotation.getWidth(), foldingBitmap_right.getHeight() * 2, new Paint());
		}
		
		invalidate();
	}
	
	Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), c);

		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
				.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	/**
	 * 重畫當前頁面
	 */
	public void redraw(){
		Log.d("EpubView:redraw","in");
		invalidate();
	}
	
	public void onDraw(Canvas cv){
		//Log.d("onDraw","in");
		//cv.setBitmap(bitmap);
		//Drawable bg = this.getBackground();
		//super.onDraw(cv);
		//Log.e("JP","ondraw density:"+cv.getDensity());
		//Log.e("JP","ondraw dpi:"+AndroidLibrary.getDpi(ctx));
		//Log.e("JP","ondraw dpi:"+AndroidLibrary.getYDpi(ctx));
		if(curPageBitmap!=null)  
			cv.drawBitmap(curPageBitmap, 0, 0, null);
		
		if(prePageDrawable!=null && prePageBitmap !=null){	
			prePageDrawable.draw(cv);
		}
		if(shouldStartAnimation){
			//20110427 benson modified for new rotating method.
			int animationType = turner.getType();
			if(animationType != PageTurner.ROTATION){
			//if(Boolean.TRUE){
				Animation an = turner.getAnimation();
	            //Animation an = new TranslateAnimation(0, 100, 0, 200);
	            //an.setDuration(2000);
	            //an.setRepeatCount(1);
				if(an!=null && prePageBitmapDrawable!=null){
					//Log.d("set","Animation");
					//Drawable dr = ctx.getResources().getDrawable(R.drawable.bg_cloud);
					//BitmapDrawable dr = new BitmapDrawable(curPageBitmap);
		            an.initialize(screenWidth, screenHeight, screenWidth, screenHeight);
		            //Log.e("JP","sw,sh:"+screenWidth+","+screenHeight);
					prePageBitmapDrawable.setAlpha(255);
					prePageBitmapDrawable.setBounds(0, 0, prePageBitmap.getWidth(), prePageBitmap.getHeight());
					//Log.e("JP","iw,sh:"+prePageBitmapDrawable.getIntrinsicWidth()+","+prePageBitmapDrawable.getIntrinsicHeight());
					prePageDrawable = new AnimateDrawable(prePageBitmapDrawable,an);
					prePageDrawable.setAlpha(255);
					//prePageDrawable = new AnimateDrawable(dr,an);
					an.startNow();
					prePageDrawable.draw(cv);
				}else{
					turner.callBack();
				}
				shouldStartAnimation=false;
			}else{
				int turnOrientation = turner.getOrientation();
				if(turnOrientation != PageTurner.RELOAD){
					curlRotation.doStep();
					if(curlRotation.inProgress()){
						curlRotation.draw(cv);
						if (curlRotation.getMode().Auto) {
							invalidate();
						}
						//drawFooter(cv);
					}else{
						turner.callBack();
						shouldStartAnimation=false;
					}
				}else{
					turner.callBack();
					shouldStartAnimation=false;
				}
			}
		}/*else if(curPageBitmap!=null && turner!=null){
			if(prePageDrawable ==null || !prePageDrawable.hasStarted() || prePageDrawable.hasEnded())
				turner.callBack();
		}*/
		if(showManual){
			//Log.d("isStart","true");
			//Log.d(contentToDraw.size());
			Bitmap manual = BitmapFactory.decodeResource(ctx.getResources(),R.drawable.iii_introduction);
			cv.drawBitmap(manual, new Rect(0,0,manual.getWidth(),manual.getHeight()), new Rect(0,0,getWidth(),getHeight()), null);
			if(!isCoverTaskStart){
				//Log.d("start","timer");
				//cv.drawBitmap(BitmapFactory.decodeResource(ctx.getResources(),R.drawable.iii_introduction), 0, 0, null);
				isCoverTaskStart=true;
				(new CoverThread()).start();
				//coverTimer.schedule(new CoverTask(), 5000);
			}
		}
		
		if(ecb != null){
		if(ecb.isUnderlineOpen()){
			if(isEndSelected){
				int localStartSpan,localEndSpan,localStartIdx,localEndIdx,localStartDrawIdx,localEndDrawIdx;
				if(startSpan>endSpan||(startSpan==endSpan && startIdx>endIdx)){
					localEndDrawIdx=startDrawableIdx;localStartDrawIdx=endDrawableIdx;localStartSpan=endSpan;localEndSpan=startSpan;localStartIdx=endIdx;localEndIdx=startIdx;
				}else{
					localEndDrawIdx=endDrawableIdx;localStartDrawIdx=startDrawableIdx;localStartSpan=startSpan;localEndSpan=endSpan;localStartIdx=startIdx;localEndIdx=endIdx;
				}
				int localStart=localStartIdx;
				TextPaint tp = new TextPaint();
				//tp.setColor(RendererConfig.getUnderlineColor(ctx,ecb.getDeliverId()));
				for(int i=0;localStartSpan+i<localEndSpan;i++){				
					contentToDraw.get(i+localStartDrawIdx).drawRect(cv, tp , localStart, -1,0);
					localStart=0;
				}
				contentToDraw.get(localEndDrawIdx).drawRect(cv, tp , localStart, localEndIdx,0);
			}	
		}else if(ecb.isUnderlineRemovalOpen()){
			/*mark for not highlight underline when delete*/
//			if(ulForDelete!=null){
//				TextPaint tp = new TextPaint();
//				tp.setARGB(255,255,255,255);
//				for(int i=0;i<ulForDelete.size();i++){
//					int localStartSpan=ulForDelete.get(i).span1;
//					int localStartIdx=ulForDelete.get(i).idx1;
//					int localEndSpan=ulForDelete.get(i).span2;
//					int localEndIdx=ulForDelete.get(i).idx2;
//					for(int j=0;j<contentToDraw.size();j++){
//						 int spanOfContent = contentToDraw.get(j).getSpanIdx(),drawStart=0,drawEnd=-1;
//						 if(spanOfContent==localStartSpan)
//							 drawStart=localStartIdx;
//						 if(spanOfContent==localEndSpan)
//							 drawEnd=localEndIdx;
//						 if(spanOfContent>=localStartSpan && spanOfContent<=localEndSpan){
//							 contentToDraw.get(j).drawRect(cv, tp, drawStart, drawEnd,0);
//						 }
//					}
//				}
//			}
		}
	}
		
		if(prePageDrawable !=null && prePageDrawable.hasStarted() && !prePageDrawable.hasEnded()){
			//Log.d("in","Animation");
			invalidate();
		}
	}
	
	private static final int CALL_REDRAW=0;
	class ThreadHandler extends Handler{
    	public void handleMessage(Message msg) {
    		switch( msg.what ){
    			case CALL_REDRAW:			
    				redraw();
    				break;   
    		} 
        }
    }
	
	class CoverThread extends Thread{ 
		public void run(){
			try {
				sleep(4000);
				showManual=false;
				isCoverTaskStart=false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				thandler.sendMessage(thandler.obtainMessage(CALL_REDRAW));
			}
		}
	}
	
	public boolean isLongPressed(){
		if(iMode == MODE_ZOOM || bPointMoveOver == true)
			return false;
		return true;
	}
	public Point getPressedPoint(){
		return new Point(dragx,dragy);
	}
	
	private boolean bContextMenuOpen = false;
	public void setParentContextMenuOpen(boolean bOpen){
		bContextMenuOpen = bOpen;
	}	
	
}
