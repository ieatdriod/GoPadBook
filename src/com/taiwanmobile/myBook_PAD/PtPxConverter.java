package com.taiwanmobile.myBook_PAD;

import org.iii.ideas.reader.renderer.RendererConfig;

/**
 * pt <-> px 轉換工具
 * @author III
 * 
 */
public class PtPxConverter {
	//public static int[] pts = {9 , 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24};
	//public static int[] pxs = {12 , 13, 15, 16, 17, 19, 21, 22, 24, 26, 29, 32};
	/** 
	 * pt轉換px
	 * @param pt pt
	 * @return px
	 */
	public static int getPxFromPt(int pt){
		int dpi = RendererConfig.dpi;
		//Log.d("ptToPx","is:"+pt*72/dpi);
		return dpi*pt/72;
		//return (int) (2*pt);
		
		/*for(int i=0;i<pts.length;i++){
			if(pt==pts[i])2
				return pxs[i];
		}
		return 0;*/
		
	}
	/** 
	 * px轉換pt
	 * @param px px
	 * @return pt
	 */
	public static int getPtFromPx(int px){
		//Log.d("pxToPt","is:"+dpi*px/72);
		int dpi = RendererConfig.dpi;
		return px*72/dpi;
		//return (int) (px)/2;
		/*for(int i=0;i<pxs.length;i++){
			if(px==pxs[i])
				return pts[i];
		}
		return 0;*/
	}
}
