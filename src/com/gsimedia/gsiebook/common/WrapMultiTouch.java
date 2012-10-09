package com.gsimedia.gsiebook.common;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

/**
 * wrap multi touch support
 */
public class WrapMultiTouch{
	public static int getPointerCount(MotionEvent ev){
		int iRet = 0;
		try{
			iRet = ev.getPointerCount();
		}catch (Throwable e){
		}
		return iRet;
	}
	public static int getPointerId(MotionEvent ev, int id){
		int iRet = 0;
		try{
			iRet = ev.getPointerId(id);
		}catch (Throwable e){
		}
		return iRet; 
	}
	public static float getX(MotionEvent ev, int id){
		float iRet = 0;
		try{
			iRet = ev.getX(id);
		}catch (Throwable e){
		}
		return iRet;
	}
	public static float getY(MotionEvent ev, int id){
		float iRet = 0;
		try{
			iRet = ev.getY(id);
		}catch (Throwable e){
		}
		return iRet;
	}
	
	public static PointF getMidPoint(MotionEvent event) {
		PointF point = new PointF(0,0);
		if(WrapMultiTouch.getPointerCount(event)!=2){
			Log.e(Config.LOGTAG,"[getMidPoint]only one point! x="+event.getX()+" y="+event.getY());
			return point;
		}
		float x = WrapMultiTouch.getX(event,0) + WrapMultiTouch.getX(event,1);
		float y = WrapMultiTouch.getY(event,0) + WrapMultiTouch.getY(event,1);
		point.set(x / 2, y / 2);
		return point;
	}	
	public static float getPointDistance(MotionEvent event){
		if(WrapMultiTouch.getPointerCount(event)!=2){
			Log.e(Config.LOGTAG,"[getPointDistance]:only one point! x="+event.getX()+" y="+event.getY());
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
}