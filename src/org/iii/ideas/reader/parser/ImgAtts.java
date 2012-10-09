package org.iii.ideas.reader.parser;

import java.util.HashMap;

import org.iii.ideas.reader.renderer.RendererConfig;

import android.util.Log;

/**
 * 圖片呈現相關屬性。
 * @author III
 *
 */
public class ImgAtts{
	//public static enum styleSet{dotted,dashed};
	public static HashMap<String,Integer> borderStyleMap=null; 
	public static final String[] styleSetArray = {"none","dashed","dotted","solid","double","groove","ridge","inset","outset"};
	UnitValue width;
	UnitValue height;
	UnitValue borderWidth;
	int borderColor=9999;
	int borderStyle=-1;
	/**
	 * 
	 * @param parent parent attributes
	 */
	public ImgAtts(ImgAtts parent){
		width=parent.getWidth();
		height=parent.getHeight();
		borderWidth=parent.getBorderWidth();
		borderColor=parent.getBorderColor();
		borderStyle=parent.getBorderStyle();
	}
	
	/**
	 * 
	 * @param borderStr border屬性字串
	 * @param borderWidthStr border寬度字串
	 * @param widthStr 圖片寬度字串
	 * @param heightStr 圖片高度字串
	 */
	public ImgAtts(String borderStr,String borderWidthStr,String widthStr,String heightStr){
		setValue(borderStr,borderWidthStr,widthStr,heightStr);
	}
	
	/**
	 * parse和處理圖片屬性
	 * @param borderStr border屬性字串
	 * @param borderWidthStr border寬度字串
	 * @param widthStr 圖片寬度字串
	 * @param heightStr 圖片高度字串
	 */
	public void setValue(String borderStr,String borderWidthStr,String widthStr,String heightStr){
		setBorderStyleSet();
		
		if(widthStr!=null){
			width = CssProperty.getUnitValueFromString(widthStr);
		}
		if(heightStr!=null){
			height = CssProperty.getUnitValueFromString(heightStr);
		}
		if(borderStr!=null){
			borderStr=borderStr.trim();
			String[] borderAtts = borderStr.split("\\s");
			for(int i=0;i<borderAtts.length;i++){
				try {
					if (Character.isDigit(borderAtts[i].charAt(0))) {
						borderWidth = CssProperty
								.getUnitValueFromString(borderAtts[i]);
					} else if (borderStyleMap.containsKey(borderAtts[i])) {
						borderStyle = (borderStyleMap.get(borderAtts[i])==null?-1:borderStyleMap.get(borderAtts[i]).intValue());
						//Log.d("style:"+borderAtts[i],"value:"+borderStyle);
					} else {
						borderColor = ColorConverter
								.convertColor(borderAtts[i]);
					}
				} catch (Exception e) {
					// TODO: handle exception
					Log.e("ImgAtts:"+e.toString(),"value:"+borderAtts[i]);
				}
			}
			if(borderWidth==null){
				borderWidth = new UnitValue(RendererConfig.BORDER_THIN,CssProperty.UNIT_PX);
			}
		}
		//Log.d("style1:","value:"+borderStyle);
		if(borderWidthStr!=null){
			borderWidth = CssProperty
			.getUnitValueFromString(borderWidthStr);
		}
	}
	
	/**
	 * 將border style列表加入到屬性中
	 */
	public static void setBorderStyleSet(){
		//if(styleSet.dashed== styleSet.valueOf("dashed"))
		if(borderStyleMap==null){
			borderStyleMap = new HashMap<String,Integer>();
			for(int i=0;i<styleSetArray.length;i++){
				borderStyleMap.put(styleSetArray[i],i);
			}
		}
	}
	
	/**
	 * 取得圖片寬度
	 * @return 圖片寬度
	 */
	public UnitValue getWidth(){
		return width;
	}
	
	/**
	 * 取得圖片高度
	 * @return 圖片高度
	 */
	public UnitValue getHeight(){
		return height;
	}
	
	/**
	 * 取得border寬度
	 * @return border寬度
	 */
	public UnitValue getBorderWidth(){
		return borderWidth;
	}
	
	/**
	 * 取得border寬度，以PX為單位
	 * @return border寬度，以PX為單位
	 */
	public int getBorderWidthInPx(){
		if(borderWidth!=null){
			return (int) borderWidth.getValue(true);
		}else{
			return 0;
		}
	}
	
	/**
	 * 取得圖片寬度，以px為單位
	 * @param screenWidth 螢幕寬
	 * @return 圖片寬度，以px為單位
	 */
	public int getWidthInPx(int screenWidth){
		if(width!=null){
			int value = (int) width.getValue(screenWidth);
			return value;
		}else{
			return -1;
		}
	}
	
	/**
	 * 取得圖片高度，以px為單位
	 * @param screenHeight 螢幕高
	 * @return 圖片高度
	 */
	public int getHeightInPx(int screenHeight){
		if(height!=null){
			int value = (int) height.getValue(screenHeight);
			return value;
		}else{
			return -1;
		}
	}
	
	/**
	 * 取得border style
	 * @return border style
	 */
	public int getBorderStyle(){
		//Log.d("getStyle0",":"+borderStyle);
		return borderStyle;
	}
	
	/**
	 * 取得border顏色
	 * @return border顏色
	 */
	public int getBorderColor(){
		return borderColor;
	}
}
