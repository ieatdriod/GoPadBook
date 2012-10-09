package org.iii.ideas.reader.parser;

import org.iii.ideas.reader.renderer.RendererConfig;

/**
 * parser處理數值的相關class，將某個值分為單位和數字部分
 * @author III
 * 
 */
public class UnitValue{
	/**
	 * 數字部分
	 */
	public float value;
	/**
	 * 單位部分，可能的值如CssProperty.UNIT_EM, CssProperty.UNIT_PX, CssProperty.UNIT_PER等
	 */
	public int unit;
	/**
	 * 
	 * @param v value, 數字部分
	 * @param u unit, 單位
	 */
	public UnitValue(float v,int u){
		unit=u;value=v;
	}
	
	/**
	 * 取得數字部分
	 * @param isPercentageX100 如果單位為百分比，是否要乘以100
	 * @return value
	 */
	public float getValue(boolean isPercentageX100){
		switch(unit){
		case CssProperty.UNIT_EM:
			return RendererConfig.DEFAULT_EM_SIZE*value;
		default:
		case CssProperty.UNIT_PX:
			return value;
		case CssProperty.UNIT_PER:
			if(isPercentageX100)
				return value*100;
			else
				return value;
		}
	}
	
	/**
	 * 取得真正可以用來處理的值
	 * @param perBase 百分比的base
	 * @return value
	 */
	public float getValue(int perBase){
		switch(unit){
		case CssProperty.UNIT_EM:
			return RendererConfig.DEFAULT_EM_SIZE*value;
		default:
		case CssProperty.UNIT_PX:
			return value;
		case CssProperty.UNIT_PER:
			return value*perBase;
		}
	}
}