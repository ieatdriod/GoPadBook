package tw.com.soyong.utility;

import java.util.ArrayList;

import com.taiwanmobile.myBook_PAD.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

public class SySeekBar extends SeekBar {
	
	private static boolean DEBUG = false ;
	
	BitmapDrawable mBookmark;
	int mH;
	int mW;
	int mThumbW ;
	
	BitmapDrawable mBmpA;
	BitmapDrawable mBmpB;
	int mA = -1 ;
	int mB = -1 ;
	
	ArrayList<Integer> mBookmarkPos = new ArrayList<Integer>(); 

	public SySeekBar(Context context) {
		super(context);
		init();
	}

	public SySeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SySeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {

		mBookmark = (BitmapDrawable)getResources().getDrawable(R.drawable.cue06);
		Bitmap bm = mBookmark.getBitmap();
		mW = bm.getWidth();
		
		BitmapDrawable bmpDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.gsi_time02);
		this.setThumb(bmpDraw);
		mThumbW = bmpDraw.getBitmap().getWidth();

		mBmpA = (BitmapDrawable)getResources().getDrawable(R.drawable.cue04);
		mBmpB = (BitmapDrawable)getResources().getDrawable(R.drawable.cue05);	
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		

		BitmapDrawable bmp = mBookmark;
		
		if (DEBUG) {
			int bmW = mW;
			ArrayList<Integer> posList = mBookmarkPos;
			for (Integer pos : posList) {
				pos += this.getThumbOffset();
				bmp.setBounds(pos, 0, pos + bmW, 10);
				bmp.draw(canvas);
			}
		}
		
//		if ( mA >= 0 ){
//			bmp = mBmpA;
//			bmp.setBounds(mA,0, mA+bmp.getBitmap().getWidth(), 10);
//			bmp.draw(canvas);
//		}
//		
//		if ( mB >= 0){
//			bmp = mBmpB;
//			bmp.setBounds(mB,0, mB+bmp.getBitmap().getWidth(), 10);
//			bmp.draw(canvas);
//		}
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec+5);
	}

	
	public void setBookmark(){
		final int prog = this.getProgress()/100;
		final int max = this.getMax()/100;
		final int width = getSeekbarWidth();
		
		int value = (prog*width/max); 

		Integer pos = new Integer(value);
		
		ArrayList<Integer> posList = mBookmarkPos;
		if (posList.size() <= 0  ){
			return ;
		}
		
		if ( false == posList.contains( pos )){
			posList.add(pos);
			
			if ( DEBUG ) Log.e("sySeekbar" , "prog:"+prog +" offset:"+getThumbOffset());
		}
		invalidate();
	}

	private final int getSeekbarWidth() {
		final int width = this.getWidth()-this.getPaddingLeft()-this.getPaddingRight()-mThumbW/2;
		return width;
	}
	
	
	public void setBookmark( ArrayList<Integer> list ){
		
		ArrayList<Integer> posList = mBookmarkPos;
		posList.clear();
		
		final int max = this.getMax()/100;
		final int width = getSeekbarWidth();	
		
		for ( Integer prog : list ){
			posList.add( ((prog/100)*width/max) );
		}
		invalidate();
	}
	
	public void setAB( int A , int B){
		
		final int max = this.getMax()/100;
		final int width = getSeekbarWidth();
		
		int progA = A;
		if ( A > 0){
			progA = (A/100)*width/max;
		}
		mA = progA ;
		
		int progB = B;
		if ( B > 0){
			progB = (B/100)*width/max;
		}
		mB = progB ;
		
		invalidate();
	}
	
}
