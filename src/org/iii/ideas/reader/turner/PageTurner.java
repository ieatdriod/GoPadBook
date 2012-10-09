package org.iii.ideas.reader.turner;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * 翻頁特效的base物件，預設無任何特效。Slider(滑動)和Rotater(翻轉)皆繼承此物件，只override取得動畫的method
 * @author III
 * 
 */
public class PageTurner {
	/**
	 * 動畫類型:無特效
	 */
	public static final int NO_EFFECT=0;
	/**
	 * 動畫類型:滑動
	 */
	public static final int SLIDE=2;
	/**
	 * 動畫類型:翻轉
	 */
	public static final int ROTATION=1;
	/**
	 * 翻頁方向:reload(無方向)
	 */
	public static final int RELOAD=0;
	/**
	 * 翻頁方向:上一頁
	 */
	public static final int PAGE_UP=1;
	/**
	 * 翻頁方向:下一頁
	 */
	public static final int PAGE_DOWN=2;
	private float height; 
	private float width;
    protected PageTurnerCallback pcb;
    
    /**
     * 
     * @param h 高(翻頁特效施行的範圍)
     * @param w 寬(翻頁特效施行的範圍)
     * @param pcb_ 當翻頁特效完畢後call back的物件
     */
	public PageTurner(float h,float w, PageTurnerCallback pcb_){
		super();
		pcb=pcb_;
		height=h;
		width=w;
	}
	
	/**
	 * 取得翻頁動畫物件
	 * @return 翻頁動畫物件
	 */
    public Animation getAnimation(){
    	Animation animation = new TranslateAnimation(
      		      Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
      		      Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
      		  );
    	animation.setDuration(0);
    	//animation.setFillAfter(true);
        animation.setAnimationListener(getAnimationListener());
        //return animation;
        return null;
    }
    
    public float getHeight(){
    	return height;
    }
    public float getWidth(){
    	return width;
    }
    
    /**
     * call back通知翻頁特效執行完畢
     */
    public void callBack(){
    	//Log.d("turner","cb");
    	pcb.onTurningFinished();
    }
    private int orientation=RELOAD;
    /**
     * 設定此次翻頁類型(是上一頁,下一頁,或reload)，決定特效的方向
     * @param type 類型，可為PageTurner.PAGE_UP, PageTurner.PAGE_DOWN, PageTurner.RELOAD
     * @param isVertical 是否為直書
     */
    public void setOrientation(int type,boolean isVertical){
    	if(isVertical){
    		if(type==PAGE_UP)
    			orientation=PAGE_DOWN;
    		else if(type==PAGE_DOWN)
    			orientation=PAGE_UP;
    		else
    			orientation= type;
    	}else{
    		orientation=type;
    	}
    }
    
    /**
     * 取得翻頁方向
     * @return 翻頁方向
     */
    public int getOrientation(){
    	return orientation;
    }
    
    /**
     * 取得動畫listener，用以決定動畫完成後執行動作
     * @return 動畫listener
     */
    public DisplayNextView getAnimationListener(){
    	return new DisplayNextView();
    }
    
    private class DisplayNextView implements Animation.AnimationListener {
        private DisplayNextView() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            //wv.post(new UpdateWV(pageUp));
        	//Log.d("onAnimation","end");
        	callBack();
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }
    
    /**
     * 取得動畫類型(參照fields)
     * @return 動畫類型
     */
    public int getType(){
    	return NO_EFFECT;
    }
}
