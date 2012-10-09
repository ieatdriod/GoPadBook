package org.iii.ideas.reader.renderer;

import android.graphics.Color;

/**
 * 未使用
 * @author III
 *
 */
public class RendererConfig_bak {
	public static final int statusBarHeight=40; 
	//public static final int lineSpace=4;
	public static boolean isVertical=false;
	public static final int[] fontSizes = {20,23,28,30,35,38};
	public static final int[] lineSpaces = {10,12,14,15,18,20};
	public static final int[] headerSizes = {};
	public static final int hMargin=81;
	public static final int toLeft=16;
	public static final int toTop=25;
	public static final int wMargin=32;
	public static final int screenHeight=800;
	public static final int screenWidth=600;
	//public static final String bmImgFile = "bm.png";
	public static final String annImgFile = "ann.png";
	public static final String bmImgDir = "imgs";
	public static int getLineSpace(int fontSizeIdx){
		return lineSpaces[fontSizeIdx];
	}
	
	public static int getFontSize(int fontSizeIdx){
		return fontSizes[fontSizeIdx];
	}
	
	public static int getLinkColor(){
		return Color.BLUE;
	}
	
	public static boolean isLinkUnderlined(){
		return true;
	}
	public static int getUnderlineOffset(){
		return 2;
	}
	
	public static int getTextColor(){
		return Color.BLACK;
	}
	
	public static int getHeaderFontSize(int fontSizeIdx,int headerIdx){
		if(headerIdx>2){
			return fontSizes[fontSizeIdx]+2;
		}else{
			return fontSizes[fontSizeIdx]+4;
		}
	}
}
