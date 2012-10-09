package org.iii.ideas.reader.turner;



import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.example.android.apis.animation.Rotate3dAnimation;
/**
 * 翻頁特效-翻轉，頁面以左緣或右緣為軸心翻轉。
 * @author III
 * 
 */
public class Rotater extends PageTurner{
	private Rotate3dAnimation rotation;
    float upCenterX;
    float downCenterX;
    float centerY;
    /**
     * 
     * @param h 螢幕高
     * @param w 螢幕寬
     * @param pcb_ 當翻頁特效完畢後call back的物件
     */
	public Rotater(float h,float w, PageTurnerCallback pcb_){
		super(h,w, pcb_);
		pcb=pcb_;
        upCenterX = getWidth();
        downCenterX= 0;
        centerY = getHeight() / 2.0f;
	}
	@Override
    public Animation getAnimation() {
        // Find the center of the container
    	//Log.d("ApplyRotation",""+isPageUp);

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        if(getOrientation()==PageTurner.PAGE_UP){
        	rotation = new Rotate3dAnimation(0, 90, upCenterX, centerY, 100.0f, true);
        	//rotation.setInterpolator(new AccelerateInterpolator());
        	rotation.setInterpolator(new LinearInterpolator());
            rotation.setDuration(700);
            rotation.setFillAfter(false);        
            rotation.setAnimationListener(getAnimationListener());
            return rotation;
        }else if(getOrientation()==PageTurner.PAGE_DOWN){
            rotation  = new Rotate3dAnimation(0,-90, downCenterX, centerY, 100.0f, true);
            rotation.setDuration(700);
           // rotation.setInterpolator(new AccelerateInterpolator());
            rotation.setInterpolator(new LinearInterpolator());
            rotation.setFillAfter(false);
            rotation.setAnimationListener(getAnimationListener());
            return rotation;
    	}else{
    		return null;
    	}

        //rotation.setFillAfter(true);
        //rotation.setInterpolator(new AccelerateInterpolator());
        //rotation.setAnimationListener(new DisplayNextView());

    }
	@Override
    public int getType(){
    	return ROTATION;
    }
}
