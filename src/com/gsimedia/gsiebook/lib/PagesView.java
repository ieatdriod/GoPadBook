package com.gsimedia.gsiebook.lib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.gsimedia.gsiebook.common.*;
import com.taiwanmobile.myBook_PAD.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.graphics.Shader;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * View that simplifies displaying of paged documents.
 * TODO: redesign zooms, pages, margins, layout
 * TODO: use more floats for better align, or use more ints for performance ;) (that is, really analyse what should be used when)
 */
public class PagesView extends View implements View.OnTouchListener, OnImageRenderedListener{
	
	private final static int MAX_ZOOM = 5000;
	private final static int MIN_ZOOM = 1000;
	private final static int DCLICK_ZOOM = 2000;
	private final static int DCLICK_ZOOM2 = 3500;
	private final static int DCLICK_TIME_INTERVAL = 250;

	private WeakReference<PagesObserver> mPagesObserver= null;
	
	/**
	 * Source of page bitmaps.
	 */
	private PagesProvider pagesProvider = null;
	
	/**
	 * Current width of this view.
	 */
	private int width = 0;
	
	/**
	 * Current height of this view.
	 */
	private int height = 0;
	
	/**
	 * Flag: are we being moved around by dragging.
	 */
	private int iTouchMode= EModeNone;
	private static final int EModeNone = 0;
	private static final int EModeDrag = 1;
	private static final int EModeZoom = 2;
	private boolean bDoubleClick = false;
	private boolean bOffScnBmpValid= false;//offscnBmp is valid or not
	private boolean bPageChanged = false;
	/**
	 * multitouch used
	 */
	float iPrevDis;
	/**
	 * Drag start pos.
	 */
	private int dragx = 0;
	
	/**
	 * Drag start pos.
	 */
	private int dragy = 0;
	
	/**
	 * Drag pos.
	 */
	private int dragx1 = 0;
	
	/**
	 * Drag pos.
	 */
	private int dragy1 = 0;
	
	/**
	 * used to determine we are dragging our just single touch 
	 */
	private static final int K_MOVE_CONSTRAINT = 10;

	private boolean bSwipeRight = false;
	private boolean bSwipeLeft = false;
	
	/**
	 * used to set "gap" between current page and next/previous page
	 */
	private static final int K_GAP_THRESHOLD= 50;
	
	/**
	 * Position over book, not counting drag.
	 * This is position of viewports top-left corner. 
	 */
	private int left = 0;
	
	/**
	 * Position over book, not counting drag.
	 * This is position of viewports top-left corner.
	 */
	private int top = 0;
	
	/**
	 * Position over book, not counting drag.
	 * This is position of viewports center, not top-left corner.
	 */
	private Point CenterPoint= new Point(0,0);
	private Point ScaledCenterPoint= new Point(0,0);
	/**
	 * Current zoom level.
	 * 1000 is 100%.
	 */
	private int zoomLevel = 1000;
	
	/**
	 * Current rotation of pages.
	 */
	private int rotation = 0;
	
	/**
	 * Base scalling factor - how much shrink (or grow) page to fit it nicely to screen at zoomLevel = 1000.
	 * For example, if we determine that 200x400 image fits screen best, but PDF's pages are 400x800, then
	 * base scaling would be 0.5, since at base scalling, without any zoom, page should fit into screen nicely.
	 */
	private float scalling0 = 0f;
	
	/* 
	 * Base scalling factor for predict page 
	 */
	private float scalling1 = 0f;
	
	/**
	 * Page sized obtained from pages provider.
	 * These do not change.
	 */
	private int pageSizes[][];
	
	/**
	 * Find mode.
	 */
	private boolean findMode = false;

	/**
	 * Paint used to draw find results.
	 */
	private Paint findResultsPaint = null;
	private Paint curFindResultPaint = null;
	
	/*
	 * Paint used to draw mark results
	 */
	private Paint markResultsPaint = null;
	
	/*
	 * Paint used to draw mark results
	 */
	//private Paint SelectedMarkResultsPaint = null;
	
	/**
	 * Paint used to draw find results.
	 */
	private Paint PageBackgroundPaint = null;
	
	/**
	 * Currently displayed find results.
	 */
	private List<FindResult> findResults = null;

	/**
	 * marker mode.
	 */
	public static final int MarkMode_None= 0;
	public static final int MarkMode_Add = 1;
	public static final int MarkMode_Del = 2;
	private int MarkerMode = MarkMode_None;
	
	/**
	 * Currently displayed marker results.
	 */
	private ArrayList<MarkResult> markResults = null;
	private MarkResult AddMarkResults = null;
	
	/**
	 * hold the currently displayed page 
	 */
	private int currentPage = 0;
	private int predictPage = 0;
	/**
	 * offscreen buffer
	 */
	private Bitmap iOffScnBmp = null;
	private Canvas iOffScnCanvas = null;
	private Matrix iOffScnMatrix = null;
	private int iBGColor = Color.GRAY;
        /*add marker when render finished*/
	private boolean isRenderFinished = false;
	/*marker stroke*/
	private static int line=1;
	private int regionid;    
	private boolean markvertical = false;	
	private Hashtable<Point,Integer> markerStrokeTable;
    private ArrayList<Bitmap> markerBitmap;
    private ArrayList<ArrayList<Rect>> markerRects = new ArrayList<ArrayList<Rect>>();
	private final int [][]markerPicID= {
		{R.drawable.gsi_black_1_1,R.drawable.gsi_black_1_2,R.drawable.gsi_black_1_3}, 

        {R.drawable.gsi_black_2_1,R.drawable.gsi_black_2_2,R.drawable.gsi_black_2_3}, 

        {R.drawable.gsi_black_3_1,R.drawable.gsi_black_3_2,R.drawable.gsi_black_3_3},           

        {R.drawable.gsi_while_1_1,R.drawable.gsi_while_1_2,R.drawable.gsi_while_1_3}, 

        {R.drawable.gsi_while_2_1,R.drawable.gsi_while_2_2,R.drawable.gsi_while_2_3}, 

        {R.drawable.gsi_while_3_1,R.drawable.gsi_while_3_2,R.drawable.gsi_while_3_3},           

        {R.drawable.gsi_brown_1_1,R.drawable.gsi_brown_1_2,R.drawable.gsi_brown_1_3}, 

        {R.drawable.gsi_brown_2_1,R.drawable.gsi_brown_2_2,R.drawable.gsi_brown_2_3}, 

        {R.drawable.gsi_brown_3_1,R.drawable.gsi_brown_3_2,R.drawable.gsi_brown_3_3},           

        {R.drawable.gsi_blue_1_1,R.drawable.gsi_blue_1_2,R.drawable.gsi_blue_1_3}, 

        {R.drawable.gsi_blue_2_1,R.drawable.gsi_blue_2_2,R.drawable.gsi_blue_2_3}, 

        {R.drawable.gsi_blue_3_1,R.drawable.gsi_blue_3_2,R.drawable.gsi_blue_3_3},           

        {R.drawable.gsi_green_1_1,R.drawable.gsi_green_1_2,R.drawable.gsi_green_1_3}, 

        {R.drawable.gsi_green_2_1,R.drawable.gsi_green_2_2,R.drawable.gsi_green_2_3}, 

        {R.drawable.gsi_green_3_1,R.drawable.gsi_green_3_2,R.drawable.gsi_green_3_3},           
 
        {R.drawable.gsi_orange_2_1,R.drawable.gsi_orange_2_2,R.drawable.gsi_orange_2_3}, 

        {R.drawable.gsi_orange_3_1,R.drawable.gsi_orange_3_2,R.drawable.gsi_orange_3_3},           

        {R.drawable.gsi_yellow_1_1,R.drawable.gsi_yellow_1_2,R.drawable.gsi_yellow_1_3}, 
        
        {R.drawable.gsi_red_1_1,R.drawable.gsi_red_1_2,R.drawable.gsi_red_1_3}, 

        {R.drawable.gsi_red_2_1,R.drawable.gsi_red_2_2,R.drawable.gsi_red_2_3}, 

        {R.drawable.gsi_red_3_1,R.drawable.gsi_red_3_2,R.drawable.gsi_red_3_3},           

        {R.drawable.gsi_purple_1_1,R.drawable.gsi_purple_1_2,R.drawable.gsi_purple_1_3}, 

        {R.drawable.gsi_purple_2_1,R.drawable.gsi_purple_2_2,R.drawable.gsi_purple_2_3}, 

        {R.drawable.gsi_purple_3_1,R.drawable.gsi_purple_3_2,R.drawable.gsi_purple_3_3}, 
			
	};
	
	/*pdf background*/
	Bitmap pageViewBG;
	int sourceWidth, sourceHeight = 0;
	
	private boolean markerExceedLimit = false;
	
	private void DoConstruct() {
		this.findResultsPaint = new Paint();
		this.findResultsPaint.setARGB(0x80, 0x00, 0xFF, 0xFF);
		this.findResultsPaint.setStyle(Paint.Style.FILL);
		this.findResultsPaint.setAntiAlias(true);
		this.findResultsPaint.setStrokeWidth(3);
		
		this.curFindResultPaint = new Paint();
		this.curFindResultPaint.setARGB(0x80, 0xFF, 0x00, 0x00);
		this.curFindResultPaint.setStyle(Paint.Style.FILL);
		this.curFindResultPaint.setAntiAlias(true);
		this.curFindResultPaint.setStrokeWidth(3);
		
		this.PageBackgroundPaint = new Paint();
		this.PageBackgroundPaint.setARGB(0xFF, 0x88, 0x88, 0x88);
		this.PageBackgroundPaint.setStyle(Paint.Style.FILL);
		this.PageBackgroundPaint.setAntiAlias(false);
		
		this.markResultsPaint = new Paint();
		this.markResultsPaint.setARGB(0x30, 0xFF, 0xFF, 0x00);
		this.markResultsPaint.setStyle(Paint.Style.FILL);
		this.markResultsPaint.setAntiAlias(true);
		this.markResultsPaint.setStrokeWidth(3);
		/*
		this.SelectedMarkResultsPaint = new Paint();
		this.SelectedMarkResultsPaint.setColor(Color.BLACK);
		this.SelectedMarkResultsPaint.setStyle(Paint.Style.STROKE);
		this.SelectedMarkResultsPaint.setAntiAlias(true);
		this.SelectedMarkResultsPaint.setStrokeWidth(5);
		*/
		
		/**
		 * load bookmark/annotation bitmap
		 */
		iLeftBookmarkBmp = BitmapFactory.decodeResource(getResources(), R.drawable.folding_left);
		iRightBookmarkBmp = BitmapFactory.decodeResource(getResources(), R.drawable.folding_right);
		iNoteBmp = BitmapFactory.decodeResource(getResources(), R.drawable.gsi_tab01);
		
		this.setOnTouchListener(this);
	}
	
	public PagesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		DoConstruct();
		//setMarkerColor(context);
	
		setMarkerBitmap(context);
	}

	public PagesView(Context context, AttributeSet attrs) {
		super(context, attrs);
		DoConstruct();
		//setMarkerColor(context);
	    pageViewBG = BitmapFactory.decodeResource(getResources(), R.drawable.gsi_pdf_bg);
		
		sourceWidth = pageViewBG.getWidth();
		sourceHeight = pageViewBG.getHeight();
		setMarkerBitmap(context);

	}

	public PagesView(Context context) {
		super(context);
		DoConstruct();
		//setMarkerColor(context);
		
		setMarkerBitmap(context);
	}

	/**
	 * Handle size change event.
	 * Update base scaling, move zoom controls to correct place etc.
	 * @param w new width
	 * @param h new height
	 * @param oldw old width
	 * @param oldh old height
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		int bottom = top + Math.min(oldh,getOrgPageHeight(this.currentPage) * zoomLevel );
			
		this.width = w;
		this.height = h;
		
		float oldScalling = this.scalling0;
		if(pageSizes!=null){
		    this.scalling0 = Math.min(
				    ((float)this.height) / (float)this.pageSizes[this.currentPage][1],
				    ((float)this.width) / (float)this.pageSizes[this.currentPage][0]);
		    this.scalling1 = Math.min(
				    ((float)this.height) / (float)this.pageSizes[this.predictPage][1],
				    ((float)this.width) / (float)this.pageSizes[this.predictPage][0]);
		}else{
			this.scalling0 = 1;
			this.scalling1 = 1;
		}
		
		int oldzoomLevel = zoomLevel;
		
		if (oldw == 0 && oldh == 0) {
			this.left = 0;
			this.top = 0;
		}else{
			if (oldzoomLevel != MIN_ZOOM){
				int aMaxZoom = getMaxZoom();
				zoomLevel = (int)(zoomLevel * oldScalling /scalling0); 
				if(zoomLevel<MIN_ZOOM){
					zoomLevel = MIN_ZOOM;
				}else if(zoomLevel>aMaxZoom){
					zoomLevel = aMaxZoom;
				}
			}
			
			this.top = bottom - Math.min(height, getOrgPageHeight(this.currentPage) * zoomLevel );
			
			Point aCent = fitBoundary(this.left, this.top);
			if (this.left != aCent.x || this.top != aCent.y){
				this.left = aCent.x;
				this.top = aCent.y;
			}
		}
		
		//clear off screen buffer on size changed
		if(iOffScnCanvas!=null){
			iOffScnCanvas.drawColor(iBGColor);
		}
		
		bPageChanged  = true;  
		bOffScnBmpValid = false;
		
		//force redraw page
		this.invalidate();
		
	}
	
	public int setPagesProvider(PagesProvider pagesProvider) {
		int result = Config.KErrNone;
		this.pagesProvider = pagesProvider;
		if (this.pagesProvider != null) {
			try{
				this.pageSizes = this.pagesProvider.getPageSizes();
				
				if (this.width > 0 && this.height > 0) {
					this.scalling0 = Math.min(
							  ((float)this.height) / (float)this.pageSizes[0][1],
							  ((float)this.width) / (float)this.pageSizes[0][0]);
					if (pageSizes.length > 1){
						  this.scalling1 = Math.min(
								  ((float)this.height) / (float)this.pageSizes[1][1],
								  ((float)this.width) / (float)this.pageSizes[1][0]);
          }else{
              this.scalling1 = this.scalling0;
          }
					this.left = 0;
					this.top = 0;
				}
			}catch(Throwable e){
				this.pagesProvider = null;
				Log.e(Config.LOGTAG,"exception:"+e);
				result = Config.KErrFail;
			}
		} else {
			this.pageSizes = null;
		}
		if(result == Config.KErrNone)
			this.pagesProvider.setOnImageRenderedListener(this);
		return result;
	}
	
	/**
	 * Draw view.
	 * @param canvas what to draw on
	 */
	@Override
	public void onDraw(Canvas canvas) {
		this.drawPages(canvas);
	}
	
	private int getOrgPageWidth(int pageno){
		if(this.pageSizes == null)
			return 0;
		float realpagewidth = (float)this.pageSizes[pageno][this.rotation % 2 == 0 ? 0 : 1];
		float currentpagewidth = realpagewidth * Math.min(
							((float)this.height) / (float)this.pageSizes[pageno][1],
							((float)this.width) / (float)this.pageSizes[pageno][0]);

		return (int)currentpagewidth;
	}
	private int getOrgPageHeight(int pageno){
		if(this.pageSizes == null)
			return 0;
		float realpageheight = (float)this.pageSizes[pageno][this.rotation % 2 == 0 ? 1 : 0];
		float currentpageheight = realpageheight * Math.min(
							((float)this.height) / (float)this.pageSizes[pageno][1],
							((float)this.width) / (float)this.pageSizes[pageno][0]);
		return (int)currentpageheight;
	}
	/**
	 * Get current page width by page number taking into account zoom and rotation
	 * @param pageno 0-based page number
	 */
	private int getCurrentPageWidth(int pageno) {
		if(this.pageSizes == null)
			return 0;
		float realpagewidth = (float)this.pageSizes[pageno][this.rotation % 2 == 0 ? 0 : 1];
		float currentpagewidth = realpagewidth * 
			Math.min(((float)this.height) / (float)this.pageSizes[pageno][1],
					((float)this.width) / (float)this.pageSizes[pageno][0]) * 
			(this.zoomLevel*0.001f);
		return (int)currentpagewidth;
	}
	
	/**
	 * Get current page height by page number taking into account zoom and rotation.
	 * @param pageno 0-based page number
	 */
	private int getCurrentPageHeight(int pageno) {
		if(this.pageSizes == null)
			return 0;
		float realpageheight = (float)this.pageSizes[pageno][this.rotation % 2 == 0 ? 1 : 0];
		float currentpageheight = realpageheight * 
			Math.min(((float)this.height) / (float)this.pageSizes[pageno][1],
					((float)this.width) / (float)this.pageSizes[pageno][0]) * 
			(this.zoomLevel*0.001f);
		return (int)currentpageheight;
	}

	private int getPageWidthByZoom(int pageno, int requiredZoomLevel) {
		if(this.pageSizes == null)
			return 0;
		float realpagewidth = (float)this.pageSizes[pageno][this.rotation % 2 == 0 ? 0 : 1];
		float zoomedpagewidth = realpagewidth * 
			Math.min(((float)this.height) / (float)this.pageSizes[pageno][1],
					((float)this.width) / (float)this.pageSizes[pageno][0]) * 
		    (requiredZoomLevel*0.001f);
		return (int)zoomedpagewidth;
	}
	private int getPageHeightByZoom(int pageno, int requiredZoomLevel) {
		if(this.pageSizes == null)
			return 0;
		float realpageheight= (float)this.pageSizes[pageno][this.rotation % 2 == 0 ? 1 : 0];
		float zoomedpageheight = realpageheight * 
			Math.min(((float)this.height) / (float)this.pageSizes[pageno][1],
					((float)this.width) / (float)this.pageSizes[pageno][0]) * 
		    (requiredZoomLevel*0.001f);
		return (int)zoomedpageheight;
	}
		
	private void drawBackground(Canvas canvas){		
		if(canvas == null){
			Log.w(Config.LOGTAG, "canvas null");
			return;
		}
		int Xscale = (int)Math.ceil( (double)canvas.getWidth()/(double)sourceWidth);
		int Yscale = (int)Math.ceil( (double)canvas.getHeight()/(double)sourceHeight);
		for(int x=0; x < Xscale; x++){
			for(int y=0; y< Yscale; y++){
				canvas.drawBitmap(pageViewBG, sourceWidth*x, sourceHeight*y, null);
			}
		}
	}
	/**
	 * Draw pages. Also collect info what's visible and push this info to page
	 * renderer.
	 */
	synchronized private void drawPages(Canvas canvas) {
		if (this.mPagesObserver == null)
			return;
		final PagesObserver aPagesObserver = this.mPagesObserver.get();
		boolean bPageDrawed = false;
		
		/**
		 * offscreen buffer
		 */
		if (iOffScnBmp == null) {
			int aSize = (width > height) ? width : height;
			if (aSize <= 0) {
				Log.e(Config.LOGTAG, "width=" + width + " height=" + height);
				return;
			}
			iOffScnBmp = Bitmap.createBitmap(aSize, aSize,
					Bitmap.Config.RGB_565);
			iOffScnCanvas = new Canvas();
			iOffScnCanvas.setBitmap(iOffScnBmp);
			iOffScnMatrix = new Matrix();
		}
		Rect src = new Rect();
		Rect dst = new Rect();
		int pageWidth = 0;
		int pageHeight = 0;
		LinkedList<Tile> visibleTiles = new LinkedList<Tile>();
		if (this.pagesProvider != null) {
			
			pageWidth = this.getCurrentPageWidth(this.currentPage);
			pageHeight = (int) this.getCurrentPageHeight(this.currentPage);

			int tileWidth = (width < pageWidth) ? width : pageWidth;
			int tileHeight = (height < pageHeight) ? height : pageHeight;

			dst.left = (width - tileWidth) / 2;
			dst.top = (height - tileHeight) / 2;
			dst.right = dst.left + tileWidth;
			dst.bottom = dst.top + tileHeight;

			Bitmap b = null;

			boolean bFitTile = (zoomLevel <= 1000) ? true : false;
			Tile tile = new Tile(this.currentPage,
					(int) (this.zoomLevel * scalling0), (int) left, (int) top,
					this.rotation, tileWidth, tileHeight, bFitTile);
			Tile aSmalltile = getTileByZoomAtPage(1000, currentPage);
			Tile aSmalltile2 = getTileByZoomAtPage(1000, predictPage);

			// get tile only when image rendered or touch up
			if (iTouchMode == EModeNone) {
				// Log.d(Config.LOGTAG, "get tile"+tile.toString());
				b = this.pagesProvider.getPageBitmap(tile);
				if (b != null) {
					src.left = 0;
					src.top = 0;
					src.right = src.left + b.getWidth();
					src.bottom = src.top + b.getHeight();

					iOffScnMatrix = new Matrix();
					canvas.setMatrix(iOffScnMatrix);
					//canvas.drawColor(iBGColor);
					drawBackground(canvas);
					canvas.drawBitmap(b, src, dst, null);


					iOffScnCanvas.setMatrix(iOffScnMatrix);
					//iOffScnCanvas.drawColor(iBGColor);
					drawBackground(iOffScnCanvas);
					iOffScnCanvas.drawBitmap(b, src, dst, null);

					bOffScnBmpValid = true;

					iOffScnZoom = zoomLevel;
					iOffScnWidth = getPageWidthByZoom(this.currentPage,
							iOffScnZoom);
					iOffScnHeight = getPageHeightByZoom(this.currentPage,
							iOffScnZoom);
					CenterPoint.x = left + Math.min(width, iOffScnWidth) / 2;
					CenterPoint.y = top + Math.min(height, iOffScnHeight) / 2;

					aPagesObserver.onRenderingProgressEnd();
					bPageChanged = false;
					if (this.findMode)
						this.drawFindResults(canvas);
					bPageDrawed = true;
					// this.drawMarkResults(canvas);
				} else {
					visibleTiles.add(tile);
				}
			}

			if (b == null) { 
				// ok, we got cache miss, how about getting normal size to draw
				// on screen?
				// Log.d(Config.LOGTAG, "get small tile"+aSmalltile.toString());
				b = this.pagesProvider.getPageBitmap(aSmalltile);
				if (b != null) {
					src.left = 0;
					src.top = 0;
					src.right = src.left + b.getWidth();//aSmalltile.getWidth();//getOrgPageWidth(this.currentPage);
					src.bottom = src.top + b.getHeight();//aSmalltile.getHeight();// getOrgPageHeight(this.currentPage);
					dst.left = 0;
					dst.top = 0;
					dst.right = (int)((double)src.right * this.zoomLevel / aSmalltile.getZoom() * getScalingAtPage(currentPage));
					dst.bottom = (int)((double)src.bottom * this.zoomLevel / aSmalltile.getZoom() * getScalingAtPage(currentPage));
					
					drawBackground(canvas);
					
					canvas.save();
					canvas.translate(-left, -top);
					if (dst.bottom < height)
						canvas.translate(0, (height - dst.bottom) / 2);
					if (dst.right < width)
						canvas.translate((width - dst.right) / 2, 0);

					//canvas.drawColor(iBGColor);
					canvas.drawBitmap(b, src, dst, null);
	
					canvas.restore();
					aPagesObserver.onRenderingProgressEnd();
					if (this.findMode)
						this.drawFindResults(canvas);
					bPageDrawed = true;
					// this.drawMarkResults(canvas);
				} else {
					visibleTiles.addFirst(aSmalltile);
					//canvas.drawColor(Color.YELLOW);
					drawBackground(canvas);
					
					//ok..we can't even get a fit tile, let's try to get a even smaller tile
					Tile aSmallertile = getTileByZoomAtPage(100,currentPage);
					b = this.pagesProvider.getPageBitmap(aSmallertile);
					if (b != null) {
						src.left = 0;
						src.top = 0;
						src.right = src.left + b.getWidth();//aSmalltile.getWidth();//getOrgPageWidth(this.currentPage);
						src.bottom = src.top + b.getHeight();//aSmalltile.getHeight();// getOrgPageHeight(this.currentPage);
						dst.left = 0;
						dst.top = 0;
						dst.right = (int)((double)src.right * this.zoomLevel / aSmallertile.getZoom() * getScalingAtPage(currentPage));
						dst.bottom = (int)((double)src.bottom * this.zoomLevel / aSmallertile.getZoom() * getScalingAtPage(currentPage));

						drawBackground(canvas);
						
						canvas.save();
						canvas.translate(-left, -top);
						if (dst.bottom < height)
							canvas.translate(0, (height - dst.bottom) / 2);
						if (dst.right < width)
							canvas.translate((width - dst.right) / 2, 0);

						//canvas.drawColor(this.iBGColor);
						canvas.drawBitmap(b, src, dst, null);
						canvas.restore();
					}else{ 
						if (bPageChanged) {
							aPagesObserver.onRenderingProgressStart();
						}
					}
				}

//				if (bOffScnBmpValid) {
//					// canvas.drawColor(this.iBGColor);
//					src.left = 0;
//					src.top = 0;
//					src.right = src.left + width;// iOffScnBmp.getWidth();
//					src.bottom = src.top + height;// iOffScnBmp.getHeight();
//					canvas.save();
//					canvas.setMatrix(iOffScnMatrix);
//					canvas.drawBitmap(iOffScnBmp, src, src, null);
//					canvas.restore();
//					if (this.findMode)
//						this.drawFindResults(canvas);
//					bPageDrawed = true;
//					// this.drawMarkResults(canvas);
//				}
				
				
			}
			if (bPageDrawed){
				this.drawMarkResults(canvas);
				this.drawBookmark(canvas);
				this.drawNote(canvas);
			}

			//determine swipe
			this.bSwipeRight = false;
			this.bSwipeLeft= false;
//			if (iTouchMode == EModeDrag) {
				//draw previous page
				int iLeftPageIdx = bLeftToNext? getNextPageIndex(): getPreviousPageIndex();
				if (iLeftPageIdx != currentPage){
					Tile aLeftPageTile = getTileByZoomAtPage(500,iLeftPageIdx);
					b = this.pagesProvider.getPageBitmap(aLeftPageTile);
					if (null == b){
						aLeftPageTile = getTileByZoomAtPage(100,iLeftPageIdx);
						b = this.pagesProvider.getPageBitmap(aLeftPageTile);
					}
					if (b != null){
						src.left = 0;
						src.top = 0;
						src.right = src.left + b.getWidth();//aPreviousPageTile.getWidth();//getOrgPageWidth(iPreviousPageIdx);
						src.bottom = src.top + b.getHeight();//aPreviousPageTile.getHeight();//getOrgPageHeight(iPreviousPageIdx);
						dst.left = 0;
						dst.top = 0;
						dst.right = (int)((double)src.right*1000/aLeftPageTile.getZoom() * getScalingAtPage(iLeftPageIdx));
						dst.bottom = (int)((double)src.bottom*1000/aLeftPageTile.getZoom() * getScalingAtPage(iLeftPageIdx));
		
						canvas.save();
						
						int widthOffset = getPageWidthByZoom(iLeftPageIdx, 1000);
						if (-left >= width/2)
							bSwipeRight= true;
						canvas.translate(-left - widthOffset - K_GAP_THRESHOLD, 0);
						if (dst.bottom < height)
							canvas.translate(0, (height - dst.bottom) / 2);
	
						canvas.drawBitmap(b, src, dst, null);
		
						canvas.restore();
					}else{
						visibleTiles.addFirst(aLeftPageTile);
					}
				}
				
				//draw next page
				int iRightPageIdx = bLeftToNext? getPreviousPageIndex(): getNextPageIndex();
				if (iRightPageIdx != currentPage){
					Tile aRightPageTile = getTileByZoomAtPage(500,iRightPageIdx);
					b = this.pagesProvider.getPageBitmap(aRightPageTile);
					if (null == b){
						aRightPageTile = getTileByZoomAtPage(100,iRightPageIdx);
						b = this.pagesProvider.getPageBitmap(aRightPageTile);
					}
					if (b != null){
						src.left = 0;
						src.top = 0;
						src.right = src.left + b.getWidth();//aNextPageTile.getWidth();//getOrgPageWidth(iNextPageIdx);
						src.bottom = src.top + b.getHeight();//aNextPageTile.getHeight();//getOrgPageHeight(iNextPageIdx);
						dst.left = 0;
						dst.top = 0;
						dst.right =  (int)((double)src.right*1000/aRightPageTile.getZoom() * getScalingAtPage(iRightPageIdx));
						dst.bottom = (int)((double)src.bottom*1000/aRightPageTile.getZoom()* getScalingAtPage(iRightPageIdx));
		
						canvas.save();
						
						int widthOffset = getPageWidthByZoom(this.currentPage, zoomLevel) + (width - tileWidth);
						if ((-left + widthOffset) <= width/2)
							bSwipeLeft= true;
						
						canvas.translate(-left + widthOffset + K_GAP_THRESHOLD, 0);
						if (dst.bottom < height)
							canvas.translate(0, (height - dst.bottom) / 2);
	
						canvas.drawBitmap(b, src, dst, null);

						canvas.restore();
					}else{
						visibleTiles.addFirst(aRightPageTile);
					}
				}
				
//			}
			if (!this.findMode) {
				b = this.pagesProvider.getPageBitmap(aSmalltile2);
				if (b == null) {
					visibleTiles.add(aSmalltile2);
				}
			}
			this.pagesProvider.setVisibleTiles(visibleTiles);
                        /*add marker when render finished*/
			if(visibleTiles.size()==0)
			{
				isRenderFinished=true;
			}else
			{
				isRenderFinished=false;
			}
		}
	}
        /*add marker when render finished*/
	public boolean getRenderFinished()
	{
		return isRenderFinished;
	}
    /**
	 * Draw bookmark
	 * @param canvas drawing target
	 */
    private boolean isBookmarkVisible = false;
    private boolean isBookmarkOnLeft = false;
    private static Bitmap iLeftBookmarkBmp = null;
    private static Bitmap iRightBookmarkBmp = null;
    private synchronized void drawBookmark(Canvas canvas) {
    	if(isBookmarkVisible){
    		RectF r2 = getBookmarkRect();
    		Bitmap bm = (isBookmarkOnLeft)?iLeftBookmarkBmp:iRightBookmarkBmp;

    		canvas.drawBitmap(bm, new Rect(0,0,bm.getWidth(),bm.getHeight()), r2, null);
    	}
    }
    private RectF getBookmarkRect(){
    	Rect aTarget = new Rect();
    	Bitmap bm = null;
    	
    	/**
    	 * mapping to zoom
    	 */
		int pageWidth = this.getCurrentPageWidth(this.currentPage);
		int pageHeight = (int) this.getCurrentPageHeight(this.currentPage);
		
		int tileWidth = (width<pageWidth)?width:pageWidth;
		int tileHeight = (height<pageHeight)?height:pageHeight;
		
		float pagex = (width-tileWidth)/2-this.left;
		float pagey = (height-tileHeight)/2-this.top;
		
		float z = (float)this.zoomLevel * 0.001f;

		
    	if(isBookmarkOnLeft){
    		bm = iLeftBookmarkBmp;
    		int zoomedWidth = (int) (bm.getWidth()*z);
    		int zoomedHeight = (int) (bm.getHeight()*z);
    		aTarget.left = 0 ;
    		aTarget.top  = 0 ;
    		aTarget.right = zoomedWidth;
    		aTarget.bottom = zoomedHeight;
    	}else{    		
    		bm = iRightBookmarkBmp;
    		int zoomedWidth = (int) (bm.getWidth()*z);
    		int zoomedHeight = (int) (bm.getHeight()*z);
    		aTarget.left = pageWidth- zoomedWidth ;
    		aTarget.top  = 0 ;
    		aTarget.right = pageWidth;
    		aTarget.bottom = zoomedHeight ;
    	}
    	

    	RectF result = new RectF();
		result.left = aTarget.left + pagex;
		result.top  = aTarget.top + pagey;
		result.right = aTarget.right + pagex;
		result.bottom = aTarget.bottom + pagey;
		
    	return result;
    }
    /**
	 * Draw annotation
	 * @param canvas drawing target
	 */
    private boolean isNoteVisible = true;
    private static Bitmap iNoteBmp = null;
    private synchronized void drawNote(Canvas canvas) {
    	isNoteVisible = this.mPagesObserver.get().hasAnnotation();
    	if(isNoteVisible){
    		Bitmap bm = iNoteBmp;
    		RectF r2 = getNoteRect();
    		
    		canvas.drawBitmap(bm, new Rect(0,0,bm.getWidth(),bm.getHeight()), r2, null);
    	}
    }
    private RectF getNoteRect(){
    	
    	Bitmap bm = iNoteBmp;
    	
		Bitmap bmBookmark = (isBookmarkOnLeft)?iLeftBookmarkBmp:iRightBookmarkBmp;
    	Rect aTarget = new Rect();
    	
    	/**
    	 * mapping to zoom
    	 */
		int pageWidth = this.getCurrentPageWidth(this.currentPage);
		int pageHeight = (int) this.getCurrentPageHeight(this.currentPage);
		
		int tileWidth = (width<pageWidth)?width:pageWidth;
		int tileHeight = (height<pageHeight)?height:pageHeight;
		
		float pagex = (width-tileWidth)/2-this.left;
		float pagey = (height-tileHeight)/2-this.top;
		
		float z = (float)this.zoomLevel * 0.001f;
		int zoomedBookmarkHeight = (int) (bmBookmark.getHeight()*z);
		
		int zoomedNoteWidth = (int) (bm.getWidth()*z);
		int zoomedNoteHeight = (int) (bm.getHeight()*z);
		
    	aTarget.left = (pageWidth-zoomedNoteWidth) ;
    	aTarget.top  = zoomedBookmarkHeight*2 ;
    	aTarget.right = pageWidth;
    	aTarget.bottom = zoomedBookmarkHeight*2  + zoomedNoteHeight ;
    	
    	RectF result = new RectF();
		result.left = aTarget.left + pagex;
		result.top  = aTarget.top + pagey;
		result.right = aTarget.right + pagex;
		result.bottom = aTarget.bottom + pagey;
    	
    	return result;
    }
    
    
    /**
     * Draw find results.
     * TODO prettier icons
     * TODO message if nothing was found
     * @param canvas drawing target
     */
    private synchronized void drawFindResults(Canvas canvas) {
    	if (!this.findMode) throw new RuntimeException("drawFindResults but not in find results mode");
    	if (this.findResults == null || this.findResults.isEmpty()) {
    		Log.w(Config.LOGTAG, "nothing found");
    		return;
    	}
    	int index = 0;
    	for(FindResult findResult: this.findResults) {
    		if (findResult.markers == null || findResult.markers.isEmpty())
    			throw new RuntimeException("illegal FindResult: find result must have at least one marker");
    		Iterator<Rect> i = findResult.markers.iterator();
    		Rect r = null;
    		RectF r2 = new RectF();
    		
    		int pageWidth = this.getCurrentPageWidth(this.currentPage);
    		int pageHeight = (int) this.getCurrentPageHeight(this.currentPage);
    		
    		int tileWidth = (width<pageWidth)?width:pageWidth;
    		int tileHeight = (height<pageHeight)?height:pageHeight;
    		
    		Log.d(Config.LOGTAG,"left="+left+" top="+top);
    		float pagex = (width-tileWidth)/2-this.left;
    		float pagey = (height-tileHeight)/2-this.top;
    		
    		float z = (this.scalling0 * (float)this.zoomLevel * 0.001f);
    		while(i.hasNext()) {
    			r = i.next();
    			r2.left = r.left * z + pagex;
    			r2.top  = r.top * z + pagey;
    			r2.right = r.right * z + pagex;
    			r2.bottom = r.bottom * z + pagey;
    			if(index == iCurResult)
    				canvas.drawRect(r2, curFindResultPaint);
    			else
    				canvas.drawRect(r2, findResultsPaint);
    		}
    		index++;
    	}
    }
    
    /**
     * Draw mark results.
     * TODO prettier icons
     * TODO message if nothing was found
     * @param canvas drawing target
     */
	private synchronized void drawMarkResults(Canvas canvas) {
		if (this.markResults == null && AddMarkResults == null) {
			Log.w(Config.LOGTAG, "no marker found");
			return;
		}

		if (markerStrokeTable == null) {
			markerStrokeTable = new Hashtable<Point, Integer>();
		}

		int pageWidth = this.getCurrentPageWidth(this.currentPage);
		int pageHeight = (int) this.getCurrentPageHeight(this.currentPage);
		int tileWidth = (width < pageWidth) ? width : pageWidth;
		int tileHeight = (height < pageHeight) ? height : pageHeight;
		float pagex = (width - tileWidth) / 2 - this.left;
		float pagey = (height - tileHeight) / 2 - this.top;
		float z = (this.scalling0 * (float) this.zoomLevel * 0.001f);

		Region DrawRegion = new Region();
		if (null != this.markResults) {
			for (MarkResult markResult : this.markResults) {
				if (markResult.markers == null
						|| markResult.page != this.currentPage)
					continue;
				DrawRegion.op(markResult.markers, Region.Op.UNION);
			}
		}

		try {
			if (null != DrawRegion) {
				RegionIterator iter = new RegionIterator(DrawRegion);
				Rect r = new Rect();
				RectF r2 = new RectF();
				Rect old_rect = null;
				markvertical = false;
				markerRects.clear();
				
				while (iter.next(r)) {
					checkmark(old_rect, r);
					r2.left = r.left * z + pagex;
					r2.top = r.top * z + pagey;
					r2.right = r.right * z + pagex;
					r2.bottom = r.bottom * z + pagey;

					// marker stroke
					Point pt = new Point(r.left, r.top);
					if (markerStrokeTable.containsKey(pt)) {
						line = markerStrokeTable.get(pt);
					} else {
						markerStrokeTable.put(pt, line);
					}

					if (markvertical) {
						line = ((line) % 3) + 3;
						canvas.drawBitmap(markerBitmap.get(line), null, r2,
								null);
						line = ((++line) % 3) + 3;
					} else {
						line = ((line) % 3);
						canvas.drawBitmap(markerBitmap.get(line), null, r2,
								null);
						line = (++line) % 3;
					}
					old_rect = r;
				}
			}

			if (null != AddMarkResults) { // **add region in Table and Check
											// draw
											// vertical or horizontal**//
				RegionIterator iter = new RegionIterator(AddMarkResults.markers);
				Rect r = new Rect();
				RectF r2 = new RectF();
				Rect old_rect = null;
				Log.w("table", "table regionid = " + regionid);
				markvertical = false;
				markerRects.clear();
				while (iter.next(r)) {
					checkmark(old_rect, r);

					r2.left = r.left * z + pagex;
					r2.top = r.top * z + pagey;
					r2.right = r.right * z + pagex;
					r2.bottom = r.bottom * z + pagey;

					markerStrokeTable.put(new Point(r.left, r.top), line);
					if (markvertical) {
						canvas.drawBitmap(markerBitmap.get(3), null, r2, null);
						line = ((++line) % 3) + 3;
					} else {
						canvas.drawBitmap(markerBitmap.get(0), null, r2, null);
						line = (++line) % 3;
					}
					old_rect = r;
				}
			}
		} catch (Exception ex) {
			Log.e(Config.LOGTAG, "draw marker fail mssg=" + ex.getMessage());
		}
	}
    
    /** put all continuous rectangles to one. 
     * TODO: this part should be handled in library
     * @param r
     */
	private void addToMarkerRects(Rect r) {
		boolean handled = false;
		if (markerRects.size() == 0) {
			ArrayList<Rect> first = new ArrayList<Rect>();
			first.add(new Rect(r.left, r.top, r.right, r.bottom));
			markerRects.add(first);
		} else {
			for (int i = 0; i < markerRects.size(); i++) {
				ArrayList<Rect> temp = markerRects.get(i);
				if (Math.abs(r.left - temp.get(0).left) <= 5) {
					if (!r.equals(temp.get(0))) {
						temp.add(new Rect(r.left, r.top, r.right, r.bottom));
					}
					handled = true;
					break;
				}
			}
			if (!handled) {
				ArrayList<Rect> first = new ArrayList<Rect>();
				first.add(new Rect(r.left, r.top, r.right, r.bottom));
				markerRects.add(first);
			}
		}
			}
    
	/**
	 * analysis data in markerRects, 
	 * @return ArrayList<Rect>
	 */
	private ArrayList<Rect> mergeRects() {
		ArrayList<Rect> temp = new ArrayList<Rect>();
		int startX, startY, endX, endY = 0;
		for (int i = 0; i < markerRects.size(); i++) {
			ArrayList<Rect> workingRects = markerRects.get(i);
			startX = workingRects.get(0).left;
			startY = workingRects.get(0).top;
			for (int j = 0; j < workingRects.size() - 1; j++) {
				if (workingRects.get(j).bottom != workingRects.get(j + 1).top) {
					endX = workingRects.get(j).right;
					endY = workingRects.get(j).bottom;
					temp.add(new Rect(startX, startY, endX, endY));
					startX = workingRects.get(j + 1).left;
					startY = workingRects.get(j + 1).top;
				}
			}
			endX = workingRects.get(workingRects.size() - 1).right;
			endY = workingRects.get(workingRects.size() - 1).bottom;
			temp.add(new Rect(startX, startY, endX, endY));
		}
		return temp;
		}
	
  /*if recf height>width draw vertical*/

	private void checkmark(Rect old,Rect _r2)
	{
		Rect r2 = _r2;
		float width = r2.width();
		float height = r2.height();
		if (height > width) {
			markvertical = true;
		}else{
			markvertical = false;
		}
	}
	
	
	private void handleUpClickEvent(MotionEvent event){
		this.iTouchMode = EModeNone;
		int aOneThirdOfWidth = this.width/3;
		final PagesObserver aPagesObserver = this.mPagesObserver.get();
		
		// handle toggle bookmark
		RectF aBookmark = this.getBookmarkRect();
		aBookmark.bottom += aBookmark.height()/2;
		aBookmark.top -= aBookmark.height()/2;
		aBookmark.left -= aBookmark.width()/2;
		aBookmark.right += aBookmark.width()/2;
		
		RectF aNote = this.getNoteRect();
		if(aBookmark.contains(this.dragx,this.dragy)&& !aPagesObserver.isControlPanelOn()){			
			if(this.isBookmarkVisible)
				this.isBookmarkVisible = false;
			else
				this.isBookmarkVisible = true;
			// set/unset bookmark
			aPagesObserver.toggleControl(PagesObserver.MODE_BOOKMARK);
			invalidate();
		}else if(aNote.contains(this.dragx, this.dragy)){
			if(this.isNoteVisible){
				aPagesObserver.toggleControl(PagesObserver.MODE_NOTE);
			}
		}else if (this.dragx < aOneThirdOfWidth){
			//prev page
			aPagesObserver.toggleControl(PagesObserver.MODE_LEFT);
		}else if (this.dragx > aOneThirdOfWidth*2 ){
			//next page
			aPagesObserver.toggleControl(PagesObserver.MODE_RIGHT);
		}else{
			aPagesObserver.toggleControl(PagesObserver.MODE_MIDDLE);
		}
	}
	private Timer iTimer = null;
	private MotionEvent iUpEvent;
	Handler iTimeoutHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				handleUpClickEvent(iUpEvent);
				Log.d(Config.LOGTAG,"time out!");
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
		if(this.bDoubleClick==true)
			bDoubleClick = false;
	}
	
	/*reduce get marker from library*/	      
	class PDFLine {
		public int id;
		public Region box = new Region();
		public ArrayList<Rect> textLocationInLine = new ArrayList<Rect>();
		public boolean isVertical;
		public String text;
	}
	
	public void cleartextLocation(boolean isNeedGC){
		if(textLocation!=null){
			Log.d(Config.LOGTAG, "clear text location array.");
			textLocation.clear();	
			if(isNeedGC)
			System.gc();
		}
	}
	
	private String updateLineText(PDFLine line, String newStr) {
		String result = line.text;
		if (!oldMarkerText.equals(newStr)) {
			if (result.length() < 20 ) {
				result += newStr;
				if (result.length() >= 20) {
					result = result.substring(0, 20);
				}
			}
		}
		return result;
	}
	private ArrayList<PDFLine> textLocation = new ArrayList<PDFLine>();
    private String oldMarkerText = "";   

	public boolean setTextRegionFromPage() {
		try {
			isAbortAddMarker = false;
			this.cleartextLocation(false);
			List<PDFBox> location = pagesProvider
					.getAllBboxFromPage(currentPage);
			int lineCount = 0;
			PDFLine tempLine;
			Rect oldRect = null;
			int vertical_left = 0, vertical_right = 0;
			for (PDFBox obj : location) {
				if (obj.text == null)
					continue;					
				int relation = -1;
				Rect rect = new Rect(obj.left, obj.top, obj.right, obj.bottom);
				if (textLocation.size() == 0) {
					PDFLine line = new PDFLine();
					line.id = lineCount;
					line.textLocationInLine.add(rect);
					line.box.op(rect, Region.Op.UNION);
					line.text = obj.text;
					textLocation.add(line);
				} else {
					Rect newRect = new Rect(rect.left, rect.top + 1,
							rect.right, rect.bottom);					
					//int relation = checkOverlap(oldRect, rect);		
					relation = checkOverlap(oldRect, rect, obj.mode, obj.isStart);	
					if (relation == 0) {/* new line */
						PDFLine line = new PDFLine();
						line.id = ++lineCount;
						line.textLocationInLine.add(newRect);
						line.box.op(newRect, Region.Op.UNION);
						line.text = obj.text;
						textLocation.add(line);
						vertical_left = vertical_right = 0;						
					} else {
						tempLine = textLocation.get(lineCount);
						tempLine.text = updateLineText(tempLine, obj.text);
						
						if (relation == 1) {							
							if (tempLine.textLocationInLine.size() == 1) {
								vertical_left = oldRect.left + 1;
								vertical_right = oldRect.right - 1;
								Rect old = tempLine.textLocationInLine.get(0);
								Rect newR = new Rect(vertical_left,old.top,vertical_right,old.bottom);
								tempLine.textLocationInLine.clear();
								tempLine.box.setEmpty();								
								tempLine.textLocationInLine.add(newR);
								tempLine.box.set(newR);
							}
							/*special case for writing mode changed*/
							if(vertical_left == 0 && vertical_right == 0){
								PDFLine line = new PDFLine();
								line.id = ++lineCount;
								line.textLocationInLine.add(newRect);
								line.box.op(newRect, Region.Op.UNION);
								line.text = obj.text;
								textLocation.add(line);
								vertical_left = vertical_right = 0;	
								oldRect = rect;
								oldMarkerText = obj.text;								
								continue;
							}
							tempLine.isVertical = true;
							Rect verticalRect = new Rect(vertical_left,
									oldRect.bottom, vertical_right, rect.bottom);
							tempLine.textLocationInLine.add(verticalRect);
							tempLine.box.op(verticalRect, Region.Op.UNION);

						} else if (relation == 2) {
							tempLine.isVertical = false;							
							Rect horiRect = new Rect(oldRect.right,
									newRect.top, newRect.right, newRect.bottom);
							tempLine.textLocationInLine.add(horiRect);
							tempLine.box.op(horiRect, Region.Op.UNION);
						}						
					}
					
				}
				oldRect = rect;
				oldMarkerText = obj.text;	
			}
			IntersecTextTaskCount = 1;/* reset count */
			return true;
		} catch (Exception ex) {
			Log.d(Config.LOGTAG,
					"setTextRegionFromPage fail, msg=" + ex.getMessage());
			return false;
		}
	}        
	 
//	private boolean isOverlap(Rect oldR, Rect newR){
//		if(oldR == null || newR == null)return false;
//		boolean x_overlap = false;	
//		boolean y_overlap = false;
//		int minX = Math.min(oldR.left, newR.left);
//		int maxX = Math.max(oldR.right, newR.right);
//		int minY = Math.min(oldR.top, newR.top);
//		int maxY = Math.max(oldR.bottom, newR.bottom);
//		
//		if((maxX - minX)<= oldR.width()+newR.width()){
//			if((maxY - minY)<= Math.max(oldR.height(), newR.height()));
//			x_overlap = true;
//		}else if((maxY - minY)<= oldR.height()+newR.height()){
//			if((maxX - minX)<= Math.max(oldR.width(), newR.width()))
//			y_overlap = true;
//		}		
//		return x_overlap|y_overlap;
//	}

        /**
	 * 
	 * @param oldR
	 * @param newR
	 * @return 0 means no intersection, 1 means vertical, 2 means horizontal
	 */
//	private int checkOverlap(Rect oldR, Rect newR){
//		if(oldR == null || newR == null)return 0;
//		boolean x_direction_overlap = false;	
//		boolean y_direction_overlap = false;
//		int minX = Math.min(oldR.left, newR.left);
//		int maxX = Math.max(oldR.right, newR.right);
//		int minY = Math.min(oldR.top, newR.top);
//		int maxY = Math.max(oldR.bottom, newR.bottom);
//		
//		if((maxX - minX)<= oldR.width()+newR.width()&&
//			oldR.width()+newR.width()-1 <= (maxX - minX)){
//			if((maxY - minY)<= Math.max(oldR.height(), newR.height()))
//			x_direction_overlap = true;
//		}
//		if((maxY - minY)<= oldR.height()+newR.height()+5 &&
//			oldR.height()+newR.height()-5 <= (maxY - minY)){
//			if((maxX - minX)<= Math.max(oldR.width(), newR.width())+2)
//				y_direction_overlap = true;
//		}		
//		
//		int result = 0;
//		if(y_direction_overlap){
//			result = 1;
//		}else if(x_direction_overlap){
//			result = 2;
//		}
////		Log.d("checkOverlap","old L"+oldR.left+" T"+oldR.top+" R"+oldR.right+" B"+oldR.bottom);
////		Log.d("checkOverlap","new L"+newR.left+" T"+newR.top+" R"+newR.right+" B"+newR.bottom+" relation="+result);
//		return result;
//	}
	
	private int checkOverlap(Rect oldR, Rect newR, int mode, int isNewLine){		
		int result = (mode == 0) ? 2 : 1;
		switch (isNewLine) {
		case 1:
			if (!oldR.intersect(newR)) {
				if(mode == 0){
					int middleY = (int)((newR.top + newR.bottom)/2);
					if(middleY < oldR.bottom && oldR.top < middleY){
						if(newR.left - oldR.right <= 1)
							break;
					}
				}else if(mode == 1){
					int middleX = (int)((newR.left + newR.right)/2);
					if(middleX < oldR.right && oldR.left < middleX){
						if(newR.top - oldR.bottom <= 5)
							break;
					}						
				}		
				result = 0;				
			}else{
				if(mode == 0){/*check if horizontal*/				
					int middleX = (int)((newR.left + newR.right)/2);
					if(middleX < oldR.right && oldR.left < middleX){
						result = 1;/*correct relation*/
					}
				}else if(mode == 1){/*check if vertical*/
					int middleY = (int)((newR.top + newR.bottom)/2);
					if(middleY < oldR.bottom && oldR.top < middleY){
						result = 2;/*correct relation*/
					}
				}
			}
			break;
		default:			
		case 0:/*same line*/
			if(mode == 0){/*check if horizontal*/				
				int middleX = (int)((newR.left + newR.right)/2);
				if(middleX < oldR.right && oldR.left < middleX){
					result = 1;/*correct relation*/
				}
			}else if(mode == 1){/*check if vertical*/
				int middleY = (int)((newR.top + newR.bottom)/2);
				if(middleY < oldR.bottom && oldR.top < middleY){
					result = 2;/*correct relation*/
				}
			}
			break;
		}
		return result;
	}
	getIntersecTextTask task;
	int IntersecTextTaskCount = 0;
	private void getOverlapedTextRegion(final int left, int top, int right, int bottom){
		if(task!=null){
			if(task.getStatus() == Status.RUNNING){				
				return;		
			}
		}
                ++IntersecTextTaskCount;
		task = new getIntersecTextTask(left,top,right,bottom);		
		task.execute();
	}  
		
	static boolean isAbortAddMarker = false;	
	private class getIntersecTextTask extends AsyncTask<Integer,Integer,Integer> {
	   
		Rect moving_rect;
		int startX = 0, startY = 0, endX = 0, endY = 0;
		int markerStartX = 0, markerStartY = 0, markerEndX = 0, markerEndY = 0;
		int direction = 0;
		public getIntersecTextTask(int _left, int _top, int _right, int _buttom){
			setRectangle(_left, _top,  _right,  _buttom);
		}
		
		public void setRectangle(int _left, int _top, int _right, int _buttom){	
			startX = _left;
			startY = _top;
			endX = _right;
			endY = _buttom;
			moving_rect = checkRect(_left, _top,  _right,  _buttom);
		}
				
		private Rect checkRect(int _left, int _top, int _right, int _buttom){
			int left = _left;
			int right = _right;
			
			if(left > right){
				left = _right;
				right = _left;
				direction += 0x00;
			}else if(left == right){
				left = _right;
				right = _right + 1;
				direction += 0x10;
			}else{
				direction += 0x10;
			}
			
			int top = _top;
			int buttom = _buttom;
			if(top > buttom){
				top = _buttom;
				buttom = _top;
				direction += 0x00;
			}else if(top == buttom){
				top = _buttom;
				buttom = _buttom + 1;
				direction += 0x01;
			}else{
				direction += 0x01;
			}		
			
			return new Rect(left,top,right,buttom);
		}
		@Override
		protected void onCancelled() {	
			Log.i("getIntersecTextTask", "task cancel");
		}
		
		/**
		 *        | 
		 *  0x000 | 0x010
		 * -----start---------- vertical: 0x100, hori:0x000
		 *  0x001 | 0x011
		 * @param isHorizontal
		 */
		private void checkStartPoint(boolean isHorizontal) {			
			if (!isHorizontal) {
				direction += 0x100;
			}
			switch (direction) {
			case 0x010:
			case 0x110:
			case 0x111:
			case 0x000:
				/*change start point and end point*/
				markerStartX = endX;
				markerStartY = endY;
				markerEndX = startX;
				markerEndY = startY;
				break;
			default:
				markerStartX = startX; 
				markerStartY = startY;
				markerEndX = endX;
				markerEndY = endY;
				break;
			}
			
			/*reset direction*/
			direction &= 0x011;			
		}
		private String markerText;

		private Region getTextRegion(Rect rect) {
			Region overlapTextRegion = new Region();
			Region cloneRegion = new Region();
			int lastLineNumber = -1;
			boolean isHorizontal = false;
			Region region = null;
			int left = rect.left;
			int top = rect.top;
			int right = rect.right;
			int bottom = rect.bottom;
			int startLine = -1, minDistance = 20;
			int markerLimit = 500, markerCount = 0;

			if (textLocation == null) {
				Log.e(Config.LOGTAG, "textLocation null");
				return null;
			}
			for (PDFLine line : textLocation) {
				if (isAbortAddMarker)
					return null;
				region = line.box;
				cloneRegion.setEmpty();
				cloneRegion.set(region);
				if (cloneRegion.op(left, top, right, bottom,
						Region.Op.INTERSECT)) {
					Rect bounds = region.getBounds();
					isHorizontal = (bounds.right - bounds.left) >= (bounds.bottom - bounds.top);
					checkStartPoint(isHorizontal);
					if (overlapTextRegion.isEmpty()) {/* first line */
						startLine = line.id;
						markerText = line.text;
						if (textLocation == null) {
							Log.e(Config.LOGTAG, "textLocation null");
							return null;
						}
						if (checkInTheSameLine(bounds,rect,isHorizontal)) {/*start point and end point in the same line*/							
							for (Rect r : textLocation.get(line.id).textLocationInLine) {
								if (isAbortAddMarker)
									return null;		
								/*re-caculate start point */
								markerStartX = Math.min(startX, endX);
								markerStartY = Math.min(startY, endY);
								markerEndX = Math.max(startX, endX);
								markerEndY = Math.max(startY, endY);								
								if (isHorizontal) {/* check x */
									if (r.right >= markerStartX && markerEndX >= r.right) {
										overlapTextRegion.op(r, Region.Op.UNION);
									}
								} else {/* check y */
									if (r.bottom >= markerStartY && markerEndY >= r.bottom) {
										overlapTextRegion.op(r, Region.Op.UNION);
									}
								}
							}
						} else {							
							for (Rect r : textLocation.get(line.id).textLocationInLine) {
								if (isAbortAddMarker)
									return null;							
								if (isHorizontal) {/* check x */
									if (r.right >= markerStartX) {
										overlapTextRegion.op(r, Region.Op.UNION);
									}
								} else {/* check y */
									if (r.bottom >= markerStartY) {
										overlapTextRegion.op(r, Region.Op.UNION);
									}
								}
							}
						}
					} else {
						overlapTextRegion.op(region, Region.Op.UNION);
						markerCount++;
						if (markerLimit < markerCount) {
							markerExceedLimit = true;
							break;
						}
						if (isHorizontal) {/* check top , from N to S */
							int distance = markerEndY - bounds.top;
							if (0 <= distance && distance < minDistance) {
								minDistance = distance;
								lastLineNumber = line.id;
							}
						} else {/* check right , from E to W */
							int distance = markerEndX - bounds.left;
							if (0 <= distance && distance < minDistance) {
								minDistance = distance;
								lastLineNumber = line.id;
							}
						}
					}
				}
			}


			if (lastLineNumber > 0 && lastLineNumber != startLine) {/* last line */
				if (textLocation == null) {
					Log.e(Config.LOGTAG, "textLocation null");
					return null;
				}
				Rect bounds = textLocation.get(lastLineNumber).box.getBounds();
				isHorizontal = (bounds.right - bounds.left) >= (bounds.bottom - bounds.top);
				PDFLine lastLine = textLocation.get(lastLineNumber);
				overlapTextRegion.op(lastLine.box, Region.Op.DIFFERENCE);
				for (Rect r : lastLine.textLocationInLine) {
					if (isAbortAddMarker)
						return null;
					if (isHorizontal) {/* check x */
						if (r.right <= markerEndX) {		
							overlapTextRegion.op(r, Region.Op.UNION);
						}
					} else {/* check y */
						if (r.bottom <= markerEndY) {		
							overlapTextRegion.op(r, Region.Op.UNION);
						}
					}
				}
			}
			return overlapTextRegion;
		}  

		private boolean checkInTheSameLine(Rect bounds,Rect rect,boolean isHorizontal){
			boolean isTheSameLine = false;
			if(isHorizontal){/*check Y*/				
				if(bounds.top <= rect.bottom && rect.bottom <= bounds.bottom &&
				   bounds.top <= rect.top && rect.top <= bounds.bottom	){
					isTheSameLine = true;
				}
			}else{
				if(bounds.left <= rect.left && rect.left <= bounds.right &&
				   bounds.left <= rect.right && rect.right <= bounds.right	){
					isTheSameLine = true;
				}
			}
			return isTheSameLine;
		}
		public void run() {
			try {
				if (isAbortAddMarker)
					return;
				if(markerExceedLimit)
					return;
				Rect rect = moving_rect;
				MarkResult aMarkResult = new MarkResult();
				aMarkResult.page = currentPage;
				aMarkResult.markers = getTextRegion(rect);
				if (aMarkResult.markers == null)
					return;
				aMarkResult.setText(markerText);
				if (markerText == null) {
					Log.d(Config.LOGTAG, "markerText == null");
					return;
				}
				if (isAbortAddMarker)
					return;
				if (aMarkResult != null /* && !aMarkResult.markers.isEmpty() */) {
					if (null != PagesView.this.AddMarkResults) {
						PagesView.this.AddMarkResults = null;
					}
					PagesView.this.AddMarkResults = aMarkResult;
					// PagesView.this.invalidate();

				}
			} catch (Exception ex) {
				Log.d(Config.LOGTAG, ex.getMessage());
			}
		}
		@Override
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			run();		
			return null;
		}	
		
		
		@Override
		protected void onPostExecute(Integer result) {				
			//Log.i("getIntersecTextTask", "task finished.");			
			PagesView.this.invalidate();
			PagesView.this.IntersecTextTaskCount--;
			if(markerExceedLimit){				
				PagesView.this.mPagesObserver.get().onMarkerUpToLimit();
			}
			//Log.d(Config.LOGTAG, "delete getIntersecTextTask count="+PagesView.this.IntersecTextTaskCount);
			if(PagesView.this.IntersecTextTaskCount == 0){
				PagesView.this.cleartextLocation(true);
				if(markerExceedLimit){
					markerExceedLimit = false;					
				}
			}
	     }

	}

	/*end reduce get marker from library*/
	/**
	 * Handle touch event coming from Android system.
	 */
	private int   iOffScnZoom = MIN_ZOOM;
	private int   iOffScnWidth = 0;
	private int   iOffScnHeight= 0;
	private int   iFingersDown = 0;
	
	private boolean bPointMoveOver = false;
	
	public boolean onTouch(View v, MotionEvent event) {
		final PagesObserver aObserver = this.mPagesObserver.get();
		if (MarkerMode == MarkMode_Add || MarkerMode == MarkMode_Del) {
			Log.e("touch", "mark event" + event.toString());


			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				dragx = dragx1 = (int) event.getX();
				dragy = dragy1 = (int) event.getY();
				// disable first page
				aObserver.disableFirstPage();
				break;
			case MotionEvent.ACTION_UP:
				if (MarkerMode == MarkMode_Add) {
					if (AddMarkResults != null
							&& !AddMarkResults.markers.isEmpty()) {
						isAbortAddMarker = true;
						IntersecTextTaskCount--;
						
						if(PagesView.this.IntersecTextTaskCount == 0){
							PagesView.this.cleartextLocation(true);
							if(markerExceedLimit){
								markerExceedLimit = false;							
							}							
						}
						
						if (markResults != null) {
							markResults.add(AddMarkResults);
						} else {
							this.markResults = new ArrayList<MarkResult>();
							this.markResults.add(AddMarkResults);
						}
						if (aObserver != null)
							aObserver.onMarkerAdded(AddMarkResults);
						AddMarkResults = null;
						// this.invalidate();
					}
				} else {
					dragx1 = (int) event.getX();
					dragy1 = (int) event.getY();

					if (markResults != null) {
						int pageWidth = this
								.getCurrentPageWidth(this.currentPage);
						int pageHeight = (int) this
								.getCurrentPageHeight(this.currentPage);
						int tileWidth = (width < pageWidth) ? width : pageWidth;
						int tileHeight = (height < pageHeight) ? height
								: pageHeight;

						float pagex = (width - tileWidth) / 2 - this.left;
						float pagey = (height - tileHeight) / 2 - this.top;

						float z = (this.scalling0 * (float) this.zoomLevel * 0.001f);

						for (MarkResult markResult : this.markResults) {
							if (markResult.markers == null
									|| markResult.page != this.currentPage)
								continue;
							/* remove marker contains markResult.markers */
							if (markResult.markers.contains((int) ((dragx1 - pagex) / z),((int) ((dragy1 - pagey) / z)))) {
							
								markResults.remove(markResult);
								if (aObserver != null)
									aObserver.onMarkerDeleted(markResult);
								break;
							}
						}
					}
				}
				this.invalidate();
				MarkerMode = MarkMode_None;
				break;
			case MotionEvent.ACTION_MOVE:
				dragx1 = (int) event.getX();
				dragy1 = (int) event.getY();

				if (MarkerMode == MarkMode_Add && !markerExceedLimit) {
					int pageWidth = this.getCurrentPageWidth(this.currentPage);
					int pageHeight = (int) this
							.getCurrentPageHeight(this.currentPage);

					int tileWidth = (width < pageWidth) ? width : pageWidth;
					int tileHeight = (height < pageHeight) ? height
							: pageHeight;

					float z = (this.scalling0 * (float) this.zoomLevel * 0.001f);

					/* reduce get marker from library */
					getOverlapedTextRegion(
							(int) ((float) (left + dragx - (width - tileWidth) / 2) / z),
							(int) ((float) (top + dragy - (height - tileHeight) / 2) / z),
							(int) ((float) (left + dragx1 - (width - tileWidth) / 2) / z),
							(int) ((float) (top + dragy1 - (height - tileHeight) / 2) / z));
				} else if (MarkerMode == MarkMode_Del) {
					this.invalidate();
				}
				break;

			}
			/**
			 * don't allow more than 60 motion events per second
			 */
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			return true;
		} else {
			Log.e("touch", "event" + event.toString());
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.d("touch", "ACTION_DOWN");
				if (iFingersDown > 0)
					break;
				int x = (int) event.getX();
				int y = (int) event.getY();
				this.dragx = x;
				this.dragy = y;
				this.dragx1 = x;
				this.dragy1 = y;
				this.bSwipeRight = false;
				this.bSwipeLeft = false;

				this.iTouchMode = EModeDrag;

				bPointMoveOver = false;

				// disable first page
				aObserver.disableFirstPage();
				break;
			case MotionEvent.ACTION_UP:
				this.invalidate();
				if (bDoubleClick) {
					this.cancelTimer();
					if (!bEnableTouchAction)
						break;
					if ((Math.abs(iUpEvent.getX() - event.getX()) < 50)
							&& (Math.abs(iUpEvent.getY() - event.getY()) < 50)) {
						// Log.d(Config.LOGTAG,"double click");
						this.dragx = (int) iUpEvent.getX();
						this.dragy = (int) iUpEvent.getY();
						this.dragx1 = this.dragx;
						this.dragy1 = this.dragy;

						if (!bPointMoveOver) {
							if (Math.abs(dragx - dragx1) > 50
									&& Math.abs(dragy - dragy1) > 50) {
								bPointMoveOver = true;
							}
						}

						int currentWidth = getPageWidthByZoom(this.currentPage,
								zoomLevel);
						int currentHeight = getPageHeightByZoom(
								this.currentPage, zoomLevel);
						CenterPoint.x = left + Math.min(width, currentWidth)
								/ 2;
						CenterPoint.y = top + Math.min(height, currentHeight)
								/ 2;

						switch (zoomLevel) {
						case MIN_ZOOM:
							//**close menu 
							if(this.mPagesObserver.get().isControlPanelOn())
							{
								aObserver.toggleControl(PagesObserver.MODE_MIDDLE);
							}
							this.left = getPageWidthByZoom(currentPage,
									MIN_ZOOM)
									/ 2
									+ 2
									* ((int) event.getX() - width / 2);
							this.top = getPageHeightByZoom(currentPage,
									MIN_ZOOM)
									/ 2
									+ 2
									* ((int) event.getY() - height / 2);
							zoomLevel = DCLICK_ZOOM;

							Point aCent = fitBoundary(this.left, this.top);
							this.left = aCent.x;
							this.top = aCent.y;
							bOffScnBmpValid = false;
							break;
						/*
						 * case DCLICK_ZOOM: { ScaledCenterPoint.x =
						 * (int)(((float)(CenterPoint.x+
						 * ((int)event.getX()-width /2)
						 * ))*DCLICK_ZOOM2/DCLICK_ZOOM); ScaledCenterPoint.y =
						 * (int)(((float)(CenterPoint.y+
						 * ((int)event.getY()-height/2)
						 * ))*DCLICK_ZOOM2/DCLICK_ZOOM);
						 * 
						 * int pageWidth =
						 * this.getPageWidthByZoom(this.currentPage
						 * ,DCLICK_ZOOM2); int pageHeight = (int)
						 * this.getPageHeightByZoom
						 * (this.currentPage,DCLICK_ZOOM2);
						 * 
						 * int tileWidth = (width<pageWidth)?width:pageWidth;
						 * int tileHeight =
						 * (height<pageHeight)?height:pageHeight;
						 * 
						 * this.left = ScaledCenterPoint.x - tileWidth/2;
						 * this.top = ScaledCenterPoint.y - tileHeight/2;
						 * 
						 * zoomLevel = DCLICK_ZOOM2; Point aCent2=
						 * fitBoundary(this.left, this.top); this.left =
						 * aCent2.x; this.top = aCent2.y; bOffScnBmpValid =
						 * false; } break; case DCLICK_ZOOM2: {
						 * ScaledCenterPoint.x = (int)(((float)(CenterPoint.x+
						 * ((int)event.getX()-width /2)
						 * ))*MAX_ZOOM/DCLICK_ZOOM2); ScaledCenterPoint.y =
						 * (int)(((float)(CenterPoint.y+
						 * ((int)event.getY()-height/2)
						 * ))*MAX_ZOOM/DCLICK_ZOOM2);
						 * 
						 * int pageWidth =
						 * this.getPageWidthByZoom(this.currentPage,MAX_ZOOM);
						 * int pageHeight = (int)
						 * this.getPageHeightByZoom(this.currentPage,MAX_ZOOM);
						 * 
						 * int tileWidth = (width<pageWidth)?width:pageWidth;
						 * int tileHeight =
						 * (height<pageHeight)?height:pageHeight;
						 * 
						 * this.left = ScaledCenterPoint.x - tileWidth/2;
						 * this.top = ScaledCenterPoint.y - tileHeight/2;
						 * 
						 * zoomLevel = MAX_ZOOM; Point aCent2=
						 * fitBoundary(this.left, this.top); this.left =
						 * aCent2.x; this.top = aCent2.y; bOffScnBmpValid =
						 * false; } break;
						 */
						case MAX_ZOOM:
							this.left = 0;
							this.top = 0;
							zoomLevel = MIN_ZOOM;
							bOffScnBmpValid = false;
							break;
						default:
							this.left = 0;
							this.top = 0;
							zoomLevel = MIN_ZOOM;
							bOffScnBmpValid = false;
							break;
						}
						iTouchMode = EModeNone;
						this.invalidate();

					} else {
						Log.d(Config.LOGTAG, "no double click");
					}
				} else if (this.iTouchMode == EModeDrag) {
					Log.d("touch", "ACTION_UP IN DRAG");
					int pageHeight = (int) this
							.getCurrentPageHeight(this.currentPage);

					int tempX = (int) event.getX();
					int tempY = pageHeight <= height ? dragy1 : (int) event
							.getY();
					int aMaxMove = Math.max(Math.abs((tempX - this.dragx)),
							Math.abs((tempY - this.dragy)));
					if (aMaxMove > K_MOVE_CONSTRAINT) {

						this.iTouchMode = EModeNone;

						boolean bHandled = false;
						if (bSwipeLeft) {
							//int iRightPageIdx = bLeftToNext ? getPreviousPageIndex()
							//		: getNextPageIndex();
							//scrollToPage(iRightPageIdx);
							aObserver.toggleControl(PagesObserver.MODE_RIGHT);
							bHandled = true;
						} else if (bSwipeRight) {
							//int iLeftPageIdx = bLeftToNext ? getNextPageIndex()
							//		: getPreviousPageIndex();
							//scrollToPage(iLeftPageIdx);
							aObserver.toggleControl(PagesObserver.MODE_LEFT);
							bHandled = true;
						}
						if (!bHandled) {
							int tempLeft = this.left - (tempX - this.dragx1);
							int tempTop = this.top - (tempY - this.dragy1);

							Point aCent = fitBoundary(tempLeft, tempTop);
							if (tempLeft != aCent.x || tempTop != aCent.y) {
								tempLeft = aCent.x;
								tempTop = aCent.y;
							}
							tempX = tempX - (tempLeft - left);
							tempY = tempY - (tempTop - top);

							left = tempLeft;
							top = tempTop;
							iOffScnMatrix.postTranslate(tempX - this.dragx1,
									tempY - this.dragy1);

							dragx1 = tempX;
							dragy1 = tempY;
						}
					} else {
						this.iUpEvent = event;
						this.bDoubleClick = true;
						/**
						 * handle double click
						 */
						if (this.iTimer == null) {
							iTimer = new Timer();
							iTimer.schedule(new TimerTask() {

								@Override
								public void run() {
									iTimeoutHandler.sendEmptyMessage(0);
								}

							}, DCLICK_TIME_INTERVAL);
						}
					}
				} else if (this.iTouchMode == EModeZoom) {
					Log.d("touch", "ACTION_UP IN ZOOM");
					// this.dragx1 = (int)event.getX();
					// this.dragy1 = (int)event.getY();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (!bEnableTouchAction)
					break;

				if (this.iTouchMode == EModeDrag) {
					Log.d("touch", "ACTION_MOVE in drag");
					int pageHeight = this
							.getCurrentPageHeight(this.currentPage);

					int tempX = (int) event.getX();
					int tempY = pageHeight <= height ? dragy1 : (int) event
							.getY();
					this.left = this.left - (tempX - this.dragx1);
					this.top = this.top - (tempY - this.dragy1);
					iOffScnMatrix.postTranslate(tempX - this.dragx1, tempY
							- this.dragy1);
					dragx1 = tempX;
					dragy1 = tempY;

					if (!bPointMoveOver) {
						if (Math.abs(dragx - dragx1) > 50
								&& Math.abs(dragy - dragy1) > 50) {
							bPointMoveOver = true;
						}
					}

					this.invalidate();
				} else if (this.iTouchMode == EModeZoom) {
					Log.d("touch", "ACTION_MOVE in zoom");

					/**
					 * calculate zoom scale
					 */
					float aCurDis = WrapMultiTouch.getPointDistance(event);
					int oldzoomLevel = zoomLevel;
					if (aCurDis != 0) {
						float aScale = aCurDis / iPrevDis;
						this.zoomLevel *= aScale;
						this.iPrevDis = aCurDis;
					}

					if (bOffScnBmpValid == true) {
						Matrix aMatrix = new Matrix();
						aMatrix.setScale((float) zoomLevel / iOffScnZoom,
								(float) zoomLevel / iOffScnZoom);
						aMatrix.postTranslate(-((float) width * zoomLevel
								/ iOffScnZoom - width) / 2, -((float) height
								* zoomLevel / iOffScnZoom - height) / 2);
						iOffScnMatrix.set(aMatrix);
						/**
						 * calculate new top left position
						 */
						ScaledCenterPoint.x = (int) ((float) CenterPoint.x
								* zoomLevel / iOffScnZoom);
						ScaledCenterPoint.y = (int) ((float) CenterPoint.y
								* zoomLevel / iOffScnZoom);

						int pageWidth = this
								.getCurrentPageWidth(this.currentPage);
						int pageHeight = this
								.getCurrentPageHeight(this.currentPage);

						int tileWidth = (width < pageWidth) ? width : pageWidth;
						int tileHeight = (height < pageHeight) ? height
								: pageHeight;

						this.left = ScaledCenterPoint.x - tileWidth / 2;
						this.top = ScaledCenterPoint.y - tileHeight / 2;
					} else {
						int iCurrentWidth = getPageWidthByZoom(
								this.currentPage, oldzoomLevel);
						int iCurrentHeight = getPageHeightByZoom(
								this.currentPage, oldzoomLevel);
						CenterPoint.x = left + Math.min(width, iCurrentWidth)
								/ 2;
						CenterPoint.y = top + Math.min(height, iCurrentHeight)
								/ 2;
						/**
						 * calculate new top left position
						 */
						ScaledCenterPoint.x = (int) ((float) CenterPoint.x
								* zoomLevel / oldzoomLevel);
						ScaledCenterPoint.y = (int) ((float) CenterPoint.y
								* zoomLevel / oldzoomLevel);

						int pageWidth = this
								.getCurrentPageWidth(this.currentPage);
						int pageHeight = this
								.getCurrentPageHeight(this.currentPage);

						int tileWidth = (width < pageWidth) ? width : pageWidth;
						int tileHeight = (height < pageHeight) ? height
								: pageHeight;

						this.left = ScaledCenterPoint.x - tileWidth / 2;
						this.top = ScaledCenterPoint.y - tileHeight / 2;

						if (left < 0)
							left = 0;
						if (top < 0)
							top = 0;
					}

					this.invalidate();
				}
				break;
			case MotionEvent.ACTION_POINTER_1_DOWN:
				Log.d("touch", "ACTION_POINTER_1_DOWN");
				iFingersDown++;
				this.cancelTimer();
				iPrevDis = WrapMultiTouch.getPointDistance(event);

				if (iTouchMode == EModeDrag) {
					// switch from drag mode to zoom mode directly, means we
					// havn't received up event yet
					bOffScnBmpValid = false;
				}
				iTouchMode = EModeZoom;
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN:
				Log.d("touch", "ACTION_POINTER_2_DOWN");
				iFingersDown++;
				this.cancelTimer();
				iPrevDis = WrapMultiTouch.getPointDistance(event);

				if (iTouchMode == EModeDrag) {
					// switch from drag mode to zoom mode directly, means we
					// havn't received up event yet
					bOffScnBmpValid = false;
				}
				iTouchMode = EModeZoom;
				break;
			case MotionEvent.ACTION_POINTER_1_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			case MotionEvent.ACTION_POINTER_3_UP:
				if (iFingersDown > 0)
					iFingersDown--;
				this.cancelTimer();
				if (iFingersDown == 0) {
					Log.d("touch", "ACTION_POINTER_2_UP");

					int aMaxZoom = getMaxZoom();
					int oldzoomLevel = zoomLevel;
					if (zoomLevel < MIN_ZOOM) {
						zoomLevel = MIN_ZOOM;
					} else if (zoomLevel > aMaxZoom) {
						zoomLevel = aMaxZoom;
					}

					Matrix aMatrix = new Matrix();
					if (bOffScnBmpValid == true) {
						int pageWidth = this
								.getCurrentPageWidth(this.currentPage);
						int pageHeight = (int) this
								.getCurrentPageHeight(this.currentPage);

						int tileWidth = (width < pageWidth) ? width : pageWidth;
						int tileHeight = (height < pageHeight) ? height
								: pageHeight;

						aMatrix.setScale((float) zoomLevel / iOffScnZoom,
								(float) zoomLevel / iOffScnZoom);
						aMatrix.postTranslate(-((float) width * zoomLevel
								/ iOffScnZoom - width) / 2, -((float) height
								* zoomLevel / iOffScnZoom - height) / 2);
						aMatrix.postTranslate((width - tileWidth) / 2,
								(height - tileHeight) / 2);

						ScaledCenterPoint.x = (int) ((float) CenterPoint.x
								* zoomLevel / iOffScnZoom);
						ScaledCenterPoint.y = (int) ((float) CenterPoint.y
								* zoomLevel / iOffScnZoom);
						this.left = ScaledCenterPoint.x - width / 2;
						this.top = ScaledCenterPoint.y - height / 2;
					} else {
						int iCurrentWidth = getPageWidthByZoom(
								this.currentPage, oldzoomLevel);
						int iCurrentHeight = getPageHeightByZoom(
								this.currentPage, oldzoomLevel);
						CenterPoint.x = left + Math.min(width, iCurrentWidth)
								/ 2;
						CenterPoint.y = top + Math.min(height, iCurrentHeight)
								/ 2;
						/**
						 * calculate new top left position
						 */
						ScaledCenterPoint.x = (int) ((float) CenterPoint.x
								* zoomLevel / oldzoomLevel);
						ScaledCenterPoint.y = (int) ((float) CenterPoint.y
								* zoomLevel / oldzoomLevel);

						int pageWidth = this
								.getCurrentPageWidth(this.currentPage);
						int pageHeight = (int) this
								.getCurrentPageHeight(this.currentPage);

						int tileWidth = (width < pageWidth) ? width : pageWidth;
						int tileHeight = (height < pageHeight) ? height
								: pageHeight;

						this.left = ScaledCenterPoint.x - tileWidth / 2;
						this.top = ScaledCenterPoint.y - tileHeight / 2;
					}

					Point aCent = fitBoundary(this.left, this.top);
					if (this.left != aCent.x || this.top != aCent.y) {
						if (bOffScnBmpValid == true) {
							aMatrix.postTranslate(left - aCent.x, top - aCent.y);
							iOffScnMatrix.set(aMatrix);
						}
						this.left = aCent.x;
						this.top = aCent.y;
					}
					iTouchMode = EModeNone;

					this.invalidate();
				}
				break;
			case MotionEvent.ACTION_POINTER_3_DOWN:
				iFingersDown++;
				iTouchMode = EModeNone;
				break;
			case MotionEvent.ACTION_OUTSIDE:
				iTouchMode = EModeNone;
				break;
			}

			/**
			 * don't allow more than 60 motion events per second
			 */
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
			}

			return false;
		}
	}
	private Point fitBoundary(int x, int y){
		int aRightBound = getRightBound();
		int aButtomBound = getButtomBound();
	
		if(x <0)
			x = 0;
		if(x >aRightBound)
			x = aRightBound;
		if(y <0)
			y = 0;
		if(y >aButtomBound)
			y = aButtomBound;
		return new Point(x, y);
	}
	private int getRightBound(){
		int aRightBound = this.getCurrentPageWidth(currentPage)-(this.width);
		if(aRightBound<0)
			aRightBound = 0;
		return aRightBound;
	}
	private int getButtomBound(){
		int aButtomBound = (int) (this.getCurrentPageHeight(currentPage)-(this.height));
		if(aButtomBound<0)
			aButtomBound = 0;
		return aButtomBound;
	}
	
	private int getMaxZoom(){
		float scalling= Math.min(
				((float)this.height) / (float)this.pageSizes[this.currentPage][1],
				((float)this.width) / (float)this.pageSizes[this.currentPage][0]);
		float scalling_rotate= Math.min(
				((float)this.width) / (float)this.pageSizes[this.currentPage][1],
				((float)this.height) / (float)this.pageSizes[this.currentPage][0]);
		return (int)(Math.max(scalling, scalling_rotate)*MAX_ZOOM/this.scalling0);
		
	}
	/**
	 * Used as a callback from pdf rendering code.
	 * TODO: only invalidate what needs to be painted, not the whole view
	 */
	public void onImagesRendered(Map<Tile,Bitmap> renderedTiles) {
		this.post(new Runnable() {
			public void run() {
				PagesView.this.invalidate();
			}
		});
	}
	
	public void SetParentActivity(PagesObserver aObserver){
        this.mPagesObserver= new WeakReference<PagesObserver>(aObserver);
	}
	/**
	 * Handle rendering exception.
	 * Show error message and then quit parent activity.
	 * TODO: find a proper way to finish an activity when something bad happens in view.
	 */
	public void onRenderingException(RenderingException reason) {
		final PagesObserver aObserver= this.mPagesObserver.get();
		aObserver.onRenderingException(reason);

	}

	/**
	 * Move current viewport over n-th page.
	 * Page is 0-based.
	 * @param page 0-based page number
	 */
	synchronized public void scrollToPage(int page) {
		if(this.pageSizes==null)
			return;  
		
		this.bPageChanged = true;
		
		int gotoPage = page;
		if (page <=0){
			this.currentPage = 0;
			predictPage = 1; 
			gotoPage = 0;
			//marker stroke
			if(markerStrokeTable!=null)
			{
				markerStrokeTable.clear();				
			}
		}else if (this.pageSizes.length <= page){
			this.currentPage = this.pageSizes.length -1;
			predictPage = this.currentPage-1; 
			gotoPage = this.pageSizes.length;
		}else{
			if (page > this.currentPage)
				predictPage = page+1; 
			else
				predictPage = page-1; 
			this.currentPage = page;
			gotoPage = page;
			//marker stroke
			if(markerStrokeTable!=null)
			{
				markerStrokeTable.clear();				
			}
		}
		//fix predictpage
		if (predictPage <=0){
			predictPage = 0;
		}else if (this.pageSizes.length <= predictPage){
			predictPage = this.pageSizes.length -1;
		}
		
		this.scalling0 = Math.min(
				((float)this.height) / (float)this.pageSizes[this.currentPage][1],
				((float)this.width) / (float)this.pageSizes[this.currentPage][0]);
		
		this.scalling1 = Math.min(
				((float)this.height) / (float)this.pageSizes[this.predictPage][1],
				((float)this.width) / (float)this.pageSizes[this.predictPage][0]);
		
		zoomLevel = 1000;	
		Point aCent = fitBoundary(this.left, this.top);
		if (this.left != aCent.x || this.top != aCent.y){
			this.left = aCent.x;
			this.top = aCent.y;
		}
		
		bOffScnBmpValid = false;
		
		final PagesObserver aObserver= this.mPagesObserver.get();
		aObserver.onPageChange(gotoPage+1,this.pageSizes.length );
		this.isBookmarkVisible = aObserver.hasBookmark();
		
		this.invalidate();
		
	}
		
	/**
	 * Rotate pages.
	 * Updates rotation variable, then invalidates view.
	 * @param rotation rotation
	 */
	synchronized public void rotate(int rotation) {
		this.rotation = (this.rotation + rotation) % 4;
		this.invalidate();
	}
	
	/**
	 * Set find mode.
	 * @param m true if pages view should display find results and find controls
	 */
	synchronized public void setFindMode(boolean m) {
		if (this.findMode != m) {
			this.findMode = m;
			if (!m) {
				this.findResults = null;
			}
		}
	}
	
	/**
	 * Return find mode.
	 * @return find mode - true if view is currently in find mode
	 */
	public boolean getFindMode() {
		return this.findMode;
	}
	
	/**
	 * Set mark mode.
	 * @param m required mark mode
	 */
	synchronized public void setMarkerMode(int m) {
		if (this.MarkerMode!= m) {
			this.MarkerMode = m;
//			if (!m) {
//				this.markResults = null;
//			}
		}
	}
	
	/**
	 * Return mark mode.
	 * @return find mode - return mark mode, add/del/none
	 */
	synchronized public int getMarkMode() {
		return this.MarkerMode;
	}

	/*
	 * determine if this page has markers
	 */
	synchronized public boolean haseMarker() {
		if (markResults == null) 
			return false;
		boolean bHasMarker = false;
		for(MarkResult markResult: this.markResults) {
			if (markResult.markers != null && markResult.page == this.currentPage){
				bHasMarker = true;
				break;
			}
		}
		return bHasMarker;
	}
	/**
	 * Set mark results.
	 */
	synchronized public void setMarkResults(ArrayList<MarkResult> results) {
		this.markResults = results;
		this.invalidate();
	}
	
	/**
	 * Get current mark results.
	 */
	synchronized public ArrayList<MarkResult> getMarkResults() {
		return this.markResults;
	}
	
	/**
	 * Move viewport position to find result (if any).
	 * Does not call invalidate().
	 */
	private int iCurResult = 0;
	public void scrollToFindResult(int n) {
		if (this.findResults == null || this.findResults.isEmpty()) 
			return;
		iCurResult = n;
		Rect center = new Rect();
		FindResult findResult = this.findResults.get(n);
		for(Rect marker: findResult.markers) {
			center.union(marker);
		}
		int aPage = findResult.page;
		this.scrollToPage(aPage);
		
		// scroll to found position
		int pageWidth = this.getCurrentPageWidth(this.currentPage);
		int pageHeight = (int) this.getCurrentPageHeight(this.currentPage);
		int tileWidth = (width<pageWidth)?width:pageWidth;
		int tileHeight = (height<pageHeight)?height:pageHeight;
		float pagex = (width-tileWidth)/2;
		float pagey = (height-tileHeight)/2;
		float z = (this.scalling0 * (float)this.zoomLevel * 0.001f);
		
		this.left= (int)(center.centerX()*z+pagex)-width/2;
		this.top= (int)(center.centerY()*z+pagey)-height/2;
		
		Point aCent= fitBoundary(this.left, this.top);
		this.left = aCent.x;
		this.top = aCent.y;
		this.invalidate();
		
	}
	
	/**
	 * Get the current page number
	 * 
	 * @return the current page. zero based
	 */
	public int getCurrentPage() {
		return currentPage;
	}
	
	/**
	 * Get page count.
	 */
	public int getPageCount() {
		if(this.pageSizes==null)
			return 0;
		
		return this.pageSizes.length;
	}
	
	/**
	 * Set find results.
	 */
	public void setFindResults(List<FindResult> results) {
		this.findResults = results;
	}
	
	/**
	 * Get current find results.
	 */
	public List<FindResult> getFindResults() {
		return this.findResults;
	}
	/**
	 * disable touch action
	 */
	private boolean bEnableTouchAction = true;
	public void enableTouchAction(boolean aEnable){
		bEnableTouchAction = aEnable;
	}
	
	public boolean isLongPressed(){
		if(bPointMoveOver == true)
			return false;
		
		if (MarkerMode == MarkMode_Add || MarkerMode == MarkMode_Del){
			if (Math.abs(dragx-dragx1) < 50 && Math.abs(dragy-dragy1) < 50){
//            	if (aPoint != null)
//					aRect = this.pdf.getBboxFromPage(currentpage, aPoint.x, aPoint.y);
//            	if (aRect != null){
//            	}
				return true;
			}
		}else{
			//not outline Mode
			if (iTouchMode == EModeDrag && Math.abs(dragx-dragx1) < 50 && Math.abs(dragy-dragy1) < 50){
				cancelTimer();
				iTouchMode = EModeNone;
				return true;
				/*
				int	pageWidth = this.getCurrentPageWidth(this.currentPage);
				int	pageHeight = (int) this.getCurrentPageHeight(this.currentPage);
				
				int tileWidth = (width<pageWidth)?width:pageWidth;
				int tileHeight = (height<pageHeight)?height:pageHeight;
			
				return new Point(dragx+left-(width-tileWidth)/2, dragy+top-(height-tileHeight)/2);
				*/
			}
		}
		return false;
	}
	public Point getPressedPoint(){
		return new Point(dragx,dragy);
	}

	private boolean bContextMenuOpen = false;
	public void setParentContextMenuOpen(boolean bOpen){
		bContextMenuOpen = bOpen;
	}
	
	/**
	 * Set Underline Color
	 * @param ctx
	 * @return Underline Color ArrayList<Bitmap>
	 */
	private ArrayList<Bitmap> updateMarkerBitmap(Context ctx) {
		String[] colorArray = null;
		colorArray = ctx.getResources().getStringArray(
				R.array.iii_reader_setting_color_value);
		String name;
		SharedPreferences settings = ctx.getSharedPreferences(KEY_Pref, 0);
		name = settings.getString("reader_setting_crossed_color_value", "");
		int i = 0;
		do {
			if (i >= colorArray.length || colorArray[i].equals(name)) {
				break;
			}
			i++;

		} while (true);
		if (i == colorArray.length)
			i = 0;// **default is Black**//
		ArrayList<Bitmap> linearray = new ArrayList<Bitmap>();
		/*add normal marker png*/
		for (int j = 0; j < 3; j++) {
			linearray.add(BitmapFactory.decodeResource(ctx.getResources(),
					markerPicID[i][j]));
		}
		/*add rotated marker png*/
		for (int j = 0; j < 3; j++) {
			linearray.add(rotateBmp(BitmapFactory.decodeResource(ctx.getResources(),
					markerPicID[i][j])));
		}

		return linearray;

	}
	private Bitmap rotateBmp(Bitmap bmp)
    {
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
		
	}
	public void setMarkerBitmap(Context aCtx){
		if(markerBitmap != null){
			markerBitmap.clear();
			markerBitmap = null;
		}
		markerBitmap=updateMarkerBitmap(aCtx);
		invalidate();
	}
	public void setMarkerColor(Context aCtx){
		markResultsPaint.setColor(getColor(aCtx)&0x30FFFFFF);	
		invalidate();
	}
	private static final String KEY_Pref = "preference";
	private int getColor(Context ctx){
		String []colorArray=null;
		colorArray = ctx.getResources().getStringArray(R.array.iii_reader_setting_color_value);
		String name;
		SharedPreferences settings = ctx.getSharedPreferences(KEY_Pref, 0);

		name=settings.getString("reader_setting_crossed_color_value", "");
		
		int i = 0;
		do{
			if(i>= colorArray.length || colorArray[i].equals(name)){
				break;
			}
			i++;

		}while(true);
		
		switch(i){
			case 0:
				return ctx.getResources().getColor(R.drawable.iii_Black_1);
			case 1:
				return ctx.getResources().getColor(R.drawable.iii_Black_2);
			case 2:
				return ctx.getResources().getColor(R.drawable.iii_Black_3);
			case 3:
				return ctx.getResources().getColor(R.drawable.iii_While_1);
			case 4:
				return ctx.getResources().getColor(R.drawable.iii_While_2);
			case 5:
				return ctx.getResources().getColor(R.drawable.iii_While_3);
			case 6:
				return ctx.getResources().getColor(R.drawable.iii_Brown_1);
			case 7:
				return ctx.getResources().getColor(R.drawable.iii_Brown_2);
			case 8:
				return ctx.getResources().getColor(R.drawable.iii_Brown_3);
			case 9:
				return ctx.getResources().getColor(R.drawable.iii_Blue_1);
			case 10:
				return ctx.getResources().getColor(R.drawable.iii_Blue_2);
			case 11:
				return ctx.getResources().getColor(R.drawable.iii_Blue_3);
			case 12:
				return ctx.getResources().getColor(R.drawable.iii_Green_1);
			case 13:
				return ctx.getResources().getColor(R.drawable.iii_Green_2);
			case 14:
				return ctx.getResources().getColor(R.drawable.iii_Green_3);			
			case 15:
				return ctx.getResources().getColor(R.drawable.iii_Orange_2);
			case 16:
				return ctx.getResources().getColor(R.drawable.iii_Orange_3);
			case 17:
				return ctx.getResources().getColor(R.drawable.iii_Yellow_1);
			case 18:
				return ctx.getResources().getColor(R.drawable.iii_Red_1);
			case 19:
				return ctx.getResources().getColor(R.drawable.iii_Red_2);
			case 20:
				return ctx.getResources().getColor(R.drawable.iii_Red_3);
			case 21:
				return ctx.getResources().getColor(R.drawable.iii_Purple_1);
			case 22:
				return ctx.getResources().getColor(R.drawable.iii_Purple_2);
			case 23:
				return ctx.getResources().getColor(R.drawable.iii_Purple_3);
			default:
				return ctx.getResources().getColor(R.drawable.iii_Black_1);			
			
		} 
	}
	private int getPreviousPageIndex(){
		if(this.pageSizes==null)
			return currentPage;
		return Math.max(currentPage -1, 0);
	}
	private int getNextPageIndex(){
		if(this.pageSizes==null)
			return currentPage;
		return Math.min(currentPage +1, this.pageSizes.length -1 );
	}
	private Tile getTileByZoomAtPage(int zoom, int aPage){
		int currentFitPageWidth = this.getPageWidthByZoom(aPage, zoom);
		int currentFitPageHeight = (int) this.getPageHeightByZoom(aPage, zoom);
		int currentFitTileWidth = (width < currentFitPageWidth) ? width : currentFitPageWidth;
		int currentFitTileHeight = (height < currentFitPageHeight) ? height : currentFitPageHeight;
		
		boolean bFitTile;
		if (zoom <= 1000)
			bFitTile = true;
		else 
			bFitTile = false;
		
	    float scalling = Math.min(
			    ((float)this.height) / (float)this.pageSizes[aPage][1],
			    ((float)this.width) / (float)this.pageSizes[aPage][0]);
		Tile aTile = new Tile(aPage, (int) (zoom * scalling), (int) 0, (int) 0, this.rotation,
					currentFitTileWidth, currentFitTileHeight, bFitTile);
		return aTile;
	}
	private float getScalingAtPage(int aPage){
		float scalling = 1;
		if(pageSizes!=null){
			scalling = Math.min(
			    ((float)this.height) / (float)this.pageSizes[aPage][1],
			    ((float)this.width) / (float)this.pageSizes[aPage][0]);
		}
		return scalling;
	}
	
	private boolean bLeftToNext = false; // true:left to next, false:right to
	public void SetLeftToNext (boolean aLeftToNext){
		bLeftToNext = aLeftToNext;
		this.isBookmarkOnLeft = aLeftToNext;
		this.invalidate();
	}
	
}


