package org.iii.ideas.reader.turner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

/**
 * 可以進行animation的drawable物件
 * @author III
 *
 */
public class TurnerDrawable extends Drawable{
	private BitmapDrawable bmd;
	private Animation an;
	private Transformation mTransformation = new Transformation();
	/**
	 * 
	 * @param bm 想要呈現的bitmap
	 */
	public TurnerDrawable(Bitmap bm){
		super();
		bmd=new BitmapDrawable(bm);
	}
	
	/**
	 * 設定bitmap和動畫
	 * @param bm bitmap
	 */
	public void setBitmapAndAnimation(Bitmap bm){
		bmd=new BitmapDrawable(bm);
	}
	
	/**
	 * 設定動畫
	 * @param an_ animation object
	 */
	public void setAnimation(Animation an_){
		an=an_;
		an.startNow();
		Log.d("animation","start");
	}
	
	/**
	 * animation是否已經開始
	 * @return animation是否已經開始
	 */
    public boolean hasStarted() {
        return an != null && an.hasStarted();
    }
    
    /**
     * animation是否已經停止
     * @return animation是否已經停止
     */
    public boolean hasEnded() {
        return an == null || an.hasEnded();
    }
	
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
        if (bmd != null) {
            int sc = canvas.save();
            Animation anim = an;
            if (anim != null) {
                anim.getTransformation(
                                    AnimationUtils.currentAnimationTimeMillis(),
                                    mTransformation);
                canvas.concat(mTransformation.getMatrix());
            }
            bmd.draw(canvas);
            canvas.restoreToCount(sc);
        }
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}
	
}
