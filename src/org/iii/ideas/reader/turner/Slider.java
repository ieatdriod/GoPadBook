package org.iii.ideas.reader.turner;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * 翻頁特效-滑動，頁面往左方(上一頁)或右方(下一頁)滑行，reload則無特效。
 * @author III
 * 
 */
public class Slider extends PageTurner{
	/**
	 * 
	 * @param h 螢幕高
	 * @param w 螢幕寬
	 * @param pcb_ 當翻頁特效完畢後call back的物件
	 */
	public Slider(float h,float w, PageTurnerCallback pcb_){
		super(h,w, pcb_);
		pcb=pcb_;
	}
	
	@Override
    public Animation getAnimation() {
    	Animation animation;
    	if(getOrientation()==PageTurner.PAGE_UP){
    		//Log.d("return","pageDown");
        	animation = new TranslateAnimation(
        		      Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
        		      Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        		  );
        	animation.setDuration(500);
        	animation.setFillAfter(false);
        	animation.setAnimationListener(getAnimationListener());
        	return animation;
    	}else if(getOrientation()==PageTurner.PAGE_DOWN){
    		//Log.d("return","pageUp");
        	animation = new TranslateAnimation(
      		      Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
      		      Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
      		  );
        	animation.setDuration(500);
        	animation.setFillAfter(false);
            animation.setAnimationListener(getAnimationListener());
            return animation;
    	}else{
    		//return super.getAnimation();
    		return null;
    	}

        //rotation.setAnimationListener(new DisplayNextView());


    }
	@Override
    public int getType(){
    	return SLIDE;
    }
    /*private final class UpdateWV implements Runnable {  	
    	boolean isPageUp;
        public UpdateWV(boolean pageUp) {
        	isPageUp=pageUp;
        }

        public void run() {
            if(isPageUp){
            	rotation = new Rotate3dAnimation(180, 0, upCenterX, centerY, 310.0f, false);	       	
            }
            else{
            	rotation = new Rotate3dAnimation(-180, 0, downCenterX, centerY, 310.0f, false);
            }
            /*
            if (mPosition > -1) {
                mPhotosList.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.requestFocus();

                rotation = new Rotate3dAnimation(90, 180, centerX, centerY, 310.0f, false);
            } else {
                mImageView.setVisibility(View.GONE);
                mPhotosList.setVisibility(View.VISIBLE);
                mPhotosList.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            }
            *//*
            rotation.setDuration(0);
            rotation.setFillAfter(false);
            rotation.setInterpolator(new DecelerateInterpolator());

            wv.startAnimation(rotation);
            pcb.onTurningFinished();
        }
    }*/
}
