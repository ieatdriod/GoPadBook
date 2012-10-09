package com.taiwanmobile.myBook_PAD;

import org.iii.ideas.reader.turner.PageTurner;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;

/**
 * 取得閱讀設定的值
 * @author III
 * 
 */
public class ReaderSettingGetter extends ContextWrapper{
	public ReaderSettingGetter(Context base) {
		super(base);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 取得翻轉值
	 * @param deliverId deliverId
	 * @return 翻轉值
	 */
	public int getTurningMethod(String deliverId){
		String save = getSharedPreferences("reader_Preference", 0).getString("reader_setting_flip_value", "");
		String[] temp = getResources().getStringArray(R.array.iii_reader_setting_flip_value);
		for(int i=0;i<temp.length;i++){
			if(save.equals(temp[i]))
				return i;
		}
		return PageTurner.NO_EFFECT;
	}
	/**
	 * 取得背景
	 * @param name 背景名稱
	 * @return 背景
	 */
	public Drawable getBackground(String name){
		String[] temp = getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
		int i = 0;
		do{
			if(i>=temp.length||temp[i].equals(name)){
				break;
			}
			i++;
		}while(true);
		
		switch(i){
			case 0:
				return getResources().getDrawable(R.drawable.bg_01_s);
			case 1:
				return getResources().getDrawable(R.drawable.bg_02_s);
			case 2:
				return getResources().getDrawable(R.drawable.bg_03_s);
			default:
				return getResources().getDrawable(R.drawable.bg_01_s);
		}	
		
//		switch(i){
//		case 0:
//			return getResources().getDrawable(R.drawable.bg_green_s);
//		case 1:
//			return getResources().getDrawable(R.drawable.bg_stone_s);
//		case 2:
//			return getResources().getDrawable(R.drawable.bg_maple_s);
//		case 3:
//			return getResources().getDrawable(R.drawable.bg_water_s);
//		case 4:
//			return getResources().getDrawable(R.drawable.bg_cloud_s);
//		default:
//			return getResources().getDrawable(R.drawable.bg_green_s);
//		}
	}
	
	//private static HashMap<Integer,Integer> fontSizeMap;
	public static int[] fontSizeArray;
	/**
	 * 取得字形大小
	 * @param idx 大小
	 * @return 字形大小
	 */
	public int getFontSize(int idx){
		if(fontSizeArray==null)
			fontSizeArray = ReaderSetting.getScaledFontArray(getApplicationContext());//getResources().getIntArray(R.array.iii_reader_setting_font_size);
		
		int fontSizeInPx = PtPxConverter.getPxFromPt(fontSizeArray[idx]); 
		return fontSizeInPx;

	}
	/**
	 * 取得顏色
	 * @param name 顏色名稱
	 * @return 顏色
	 */
	public int getColor(String name){
		String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
		int i = 0;
		do{
			if(i>=temp.length||temp[i].equals(name)){
				break;
			}
			i++;

		}while(true);
		//System.out.println("====================");
		//System.out.println(i);
		switch(i){
			case 0:
				return getResources().getColor(R.drawable.iii_Black_1);
			case 1:
				return getResources().getColor(R.drawable.iii_Black_2);
			case 2:
				return getResources().getColor(R.drawable.iii_Black_3);
			case 3:
				return getResources().getColor(R.drawable.iii_While_1);
			case 4:
				return getResources().getColor(R.drawable.iii_While_2);
			case 5:
				return getResources().getColor(R.drawable.iii_While_3);
			case 6:
				return getResources().getColor(R.drawable.iii_Brown_1);
			case 7:
				return getResources().getColor(R.drawable.iii_Brown_2);
			case 8:
				return getResources().getColor(R.drawable.iii_Brown_3);
			case 9:
				return getResources().getColor(R.drawable.iii_Blue_1);
			case 10:
				return getResources().getColor(R.drawable.iii_Blue_2);
			case 11:
				return getResources().getColor(R.drawable.iii_Blue_3);
			case 12:
				return getResources().getColor(R.drawable.iii_Green_1);
			case 13:
				return getResources().getColor(R.drawable.iii_Green_2);
			case 14:
				return getResources().getColor(R.drawable.iii_Green_3);			
			case 15:
				return getResources().getColor(R.drawable.iii_Orange_2);
			case 16:
				return getResources().getColor(R.drawable.iii_Orange_3);
			case 17:
				return getResources().getColor(R.drawable.iii_Yellow_1);
			case 18:
				return getResources().getColor(R.drawable.iii_Red_1);
			case 19:
				return getResources().getColor(R.drawable.iii_Red_2);
			case 20:
				return getResources().getColor(R.drawable.iii_Red_3);
			case 21:
				return getResources().getColor(R.drawable.iii_Purple_1);
			case 22:
				return getResources().getColor(R.drawable.iii_Purple_2);
			case 23:
				return getResources().getColor(R.drawable.iii_Purple_3);
			default:
				return getResources().getColor(R.drawable.iii_Black_1);				
		}
	}	
}
