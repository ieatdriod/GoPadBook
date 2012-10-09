package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;

import com.taiwanmobile.myBook_PAD.R;

/**
 * 呈現Config和閱讀設定值取得的covenience class
 * @author III
 * 
 */
public class RendererConfig {
	/**
	 * application context
	 */
	public static int dpi;
	/**
	 * 是否開啟文字反鉅齒
	 */
	public static final boolean isAntiAliasOpen=true;
	/**
	 * tab字串
	 */
	public static final String TAB_WHITESPACE="        ";
	/**
	 * 狀態列高
	 */
	public static final int statusBarHeight=40; 

	/**
	 * 文字間距
	 */
	public static final int charSpace=4;
	/**
	 * hr margin
	 */
	public static final int hMargin=60;
	/**
	 * 左方margin
	 */
	public static final int toLeft=50;
	/**
	 * 上方margin
	 */
	public static final int toTop=40;
	/**
	 * 橫向margin
	 */
	public static final int wMargin=90;
	/**
	 * hr預設寬度
	 */
	public static final String DEFAULT_HR_TOTAL_SIZE_STR = "2px";
	/**
	 * hr預設margin
	 */
	public static final int DEFAULT_HR_MARGIN = 6;
	/**
	 * hr預設大小
	 */
	public static final int DEFAULT_HR_LINE_SIZE = 2;
	/**
	 * 預設em base
	 */
	public static final float DEFAULT_EM_SIZE = 16f;
	/**
	 * border thin寬度值
	 */
	public static final int BORDER_THIN = 3;
	/**
	 * border MEDIUM 寬度值
	 */
	public static final int BORDER_MEDIUM = 7;
	/**
	 * 段落margin
	 */
	public static final int PAR_MARGIN=15;
	/**
	 * border thick 寬度值
	 */
	public static final int BORDER_THICK = 15;
	/**
	 * border dash長度
	 */
	public static final int BORDER_DASH_LENGTH = 7;
	/**
	 * border dot長度
	 */
	public static final int BORDER_DOT_LENGTH = 2;
	/**
	 * image替代文字margin
	 */
	public static final int IMG_ALT_MARGIN = 15;
	
	/**
	 * 上下標比例
	 */
	public static final float SUB_SUP_RATIO = 0.6f;
	private final static int GET_FONT=0;
	private final static int GET_LINK=1;
	private final static int GET_LINE=2;
	
	public static boolean isNightMode=false;
	
	/**marker stroke**/
	private static ArrayList<Bitmap> markerImageArray;
	private final static int [][]markerPicID= {
		 {R.drawable.gsi_black_1_1,R.drawable.gsi_black_1_2,R.drawable.gsi_black_1_3}, 
	
         {R.drawable.gsi_black_2_1,R.drawable.gsi_black_2_2,R.drawable.gsi_black_2_3}, 

         {R.drawable.gsi_black_3_1,R.drawable.gsi_black_3_2,R.drawable.gsi_black_3_3},           

         {R.drawable.gsi_while_1_1,R.drawable.gsi_while_1_2,R.drawable.gsi_while_1_3}, 

         {R.drawable.gsi_while_2_1,R.drawable.gsi_while_2_2,R.drawable.gsi_while_2_3}, 

         {R.drawable.gsi_while_3_1,R.drawable.gsi_while_3_2,R.drawable.gsi_while_3_3},           

         {R.drawable.gsi_brown_1_1,R.drawable.gsi_brown_1_2,R.drawable.gsi_brown_1_3}, 

         {R.drawable.gsi_brown_2_1,R.drawable.gsi_brown_2_2,R.drawable.gsi_brown_2_3}, 

         {R.drawable.gsi_brown_3_1,R.drawable.gsi_brown_3_2,R.drawable.gsi_brown_3_3},           

         {R.drawable.gsi_blue_1_1,R.drawable.gsi_blue_1_2,R.drawable.gsi_blue_1_3}, 

         {R.drawable.gsi_blue_2_1,R.drawable.gsi_blue_2_2,R.drawable.gsi_blue_2_3}, 

         {R.drawable.gsi_blue_3_1,R.drawable.gsi_blue_3_2,R.drawable.gsi_blue_3_3},           

         {R.drawable.gsi_green_1_1,R.drawable.gsi_green_1_2,R.drawable.gsi_green_1_3}, 

         {R.drawable.gsi_green_2_1,R.drawable.gsi_green_2_2,R.drawable.gsi_green_2_3}, 

         {R.drawable.gsi_green_3_1,R.drawable.gsi_green_3_2,R.drawable.gsi_green_3_3},           
       
         {R.drawable.gsi_orange_2_1,R.drawable.gsi_orange_2_2,R.drawable.gsi_orange_2_3}, 

         {R.drawable.gsi_orange_3_1,R.drawable.gsi_orange_3_2,R.drawable.gsi_orange_3_3},           
         {R.drawable.gsi_yellow_1_1,R.drawable.gsi_yellow_1_2,R.drawable.gsi_yellow_1_3}, 
         {R.drawable.gsi_red_1_1,R.drawable.gsi_red_1_2,R.drawable.gsi_red_1_3}, 

         {R.drawable.gsi_red_2_1,R.drawable.gsi_red_2_2,R.drawable.gsi_red_2_3}, 

         {R.drawable.gsi_red_3_1,R.drawable.gsi_red_3_2,R.drawable.gsi_red_3_3},           

         {R.drawable.gsi_purple_1_1,R.drawable.gsi_purple_1_2,R.drawable.gsi_purple_1_3}, 

         {R.drawable.gsi_purple_2_1,R.drawable.gsi_purple_2_2,R.drawable.gsi_purple_2_3}, 

         {R.drawable.gsi_purple_3_1,R.drawable.gsi_purple_3_2,R.drawable.gsi_purple_3_3}, 
			
	};
	/**
	 * 取得直書行間距
	 * @param fontSizeIdx 文字大小index
	 * @return 直書行間距
	 */
	public static int getVerticalLineSpace(int fontSizeIdx){
		return 10;
	}
	
	/**
	 * 取得連結顏色
	 * @param ctx context
	 * @param deliverId deliver id
	 * @return 連結顏色
	 */
	public static int getLinkColor(Context ctx,String deliverId){
		//Log.d("get","linkcolor");
		return getColor(ctx,GET_LINK,deliverId);
	}
	

	/**
	 * 調整TextPaint效果
	 * @param paint TextPaint
	 */
	public static TextPaint enhanceTextPaint(TextPaint paint){
		if(isAntiAliasOpen)
			paint.setAntiAlias(true);
		return paint;
		//paint.setSubpixelText(true);
	}
	
	/**
	 * 取得畫線顏色
	 * @param ctx context
	 * @param deliverId deliver id
	 * @return 畫線顏色
	 */
	public static ArrayList<Bitmap> getUnderlineColor(Context ctx,String deliverId){		
		return getunlineColor(ctx,GET_LINE,deliverId);		
	}
	
	/**
	 * 超連結是否畫底線
	 * @return 超連結是否畫底線
	 */
	public static boolean isLinkUnderlined(){
		return true;
	}
	
	/**
	 * 取得底線和文字下緣的offset
	 * @return 底線和文字下緣的offset
	 */
	public static int getLinkOffset(){
		return 2;
	}
	
	/**
	 * 取得文字顏色
	 * @param ctx context
	 * @param deliverId deliver id
	 * @return 文字顏色
	 */
	public static int getTextColor(Context ctx,String deliverId){
		//Log.d("getColor","deliverId:"+deliverId);
		return getColor(ctx,GET_FONT,deliverId);
	}
	
	/**
	 * 取得直書header(h1~h6)的文字大小
	 * @param fontSize 文字大小base
	 * @param headerIdx 幾號header
	 * @return 直書header(h1~h6)的文字大小
	 */
	public static int getVerticalHeaderFontSize(int fontSize,int headerIdx){
		if(headerIdx>2){
			return fontSize+2;
		}else{
			return fontSize+4;
		}
	}
	
	/**
	 * Clear markerImageArray
	 */
	public static void resetMarkerImageArray(){
		
		if(markerImageArray != null){
			markerImageArray.clear();
			markerImageArray = null;
		}
	}
	/**
	 * 
	 * @param ctx context
	 * @param type RendererConfig.GET_FONT, RendererConfig.GET_LINE or RendererConfig.GET_LINK
	 * @param deliverId deliver id
	 * @return  ArrayList<Bitmap>
	 */
	private static ArrayList<Bitmap> getunlineColor(Context ctx, int type,
			String deliverId) {
		if(markerImageArray != null)return markerImageArray;
		else markerImageArray = new ArrayList<Bitmap>();
		
		if (colorArray == null)
			colorArray = ctx.getResources().getStringArray(
					R.array.iii_reader_setting_color_value);
		String name;
		SharedPreferences settings = ctx.getSharedPreferences(
				"reader_Preference", 0);
		name = settings.getString("reader_setting_crossed_color_value", "");
		// Log.d("getColor","name:"+name);
		int i = 0;
		do {
			if (i >= colorArray.length || colorArray[i].equals(name)) {
				break;
			}
			i++;

		} while (true);

		if (i >= colorArray.length) {
			i = 0;
		}

		for (int j = 0; j < 3; j++) {
			int line = markerPicID[i][j];
				markerImageArray.add(BitmapFactory.decodeResource(ctx.getResources(),
						line));
		}
		for (int j = 0; j < 3; j++) {
			int line = markerPicID[i][j];			
			markerImageArray.add(rotateBmp(BitmapFactory.decodeResource(ctx.getResources(),line)));
		}

		return markerImageArray;
	}
	private static Bitmap rotateBmp(Bitmap bmp)
	{
		Matrix matrix = new Matrix(); 
        matrix.postRotate(90); 
        int width = bmp.getWidth(); 
        int height = bmp.getHeight(); 
        return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
	
	}
	private static String[] colorArray;
	/**
	 * 取得顏色
	 * @param ctx context
	 * @param type RendererConfig.GET_FONT, RendererConfig.GET_LINE or RendererConfig.GET_LINK
	 * @param deliverId deliver id
	 * @return 顏色
	 */
	public static int getColor(Context ctx,int type,String deliverId){
		if(colorArray==null)
			colorArray = ctx.getResources().getStringArray(R.array.iii_reader_setting_color_value);
		String name;
		SharedPreferences settings = ctx.getSharedPreferences("reader_Preference", 0);
		if(settings.getBoolean("reader_setting_night_mode_value", false)){
			return Color.WHITE;
		}
		if(type==GET_FONT){
			name=settings.getString("reader_setting_font_color_value", "");
			if(name.equals(""))
				return 9999;
		}else if(type==GET_LINK){
			name=settings.getString("reader_setting_hyperlink_color_value", "");

		}else{
			name=settings.getString("reader_setting_crossed_color_value", "");
		}
		//Log.d("getColor","name:"+name);
		int i = 0;
		do{
			if(i>= colorArray.length || colorArray[i].equals(name)){
				break;
			}
			i++;

		}while(true);
		//System.out.println("====================");
		//System.out.println(i);
		switch(i){
			case 0:
				return ctx.getResources().getColor(R.drawable.iii_Black_1);
			case 1:
				return ctx.getResources().getColor(R.drawable.iii_Black_2);
			case 2:
				return ctx.getResources().getColor(R.drawable.iii_Black_3);
			case 3:
				return ctx.getResources().getColor(R.drawable.iii_While_1);
			case 4:
				return ctx.getResources().getColor(R.drawable.iii_While_2);
			case 5:
				return ctx.getResources().getColor(R.drawable.iii_While_3);
			case 6:
				return ctx.getResources().getColor(R.drawable.iii_Brown_1);
			case 7:
				return ctx.getResources().getColor(R.drawable.iii_Brown_2);
			case 8:
				return ctx.getResources().getColor(R.drawable.iii_Brown_3);
			case 9:
				return ctx.getResources().getColor(R.drawable.iii_Blue_1);
			case 10:
				return ctx.getResources().getColor(R.drawable.iii_Blue_2);
			case 11:
				return ctx.getResources().getColor(R.drawable.iii_Blue_3);
			case 12:
				return ctx.getResources().getColor(R.drawable.iii_Green_1);
			case 13:
				return ctx.getResources().getColor(R.drawable.iii_Green_2);
			case 14:
				return ctx.getResources().getColor(R.drawable.iii_Green_3);			
			case 15:
				return ctx.getResources().getColor(R.drawable.iii_Orange_2);
			case 16:
				return ctx.getResources().getColor(R.drawable.iii_Orange_3);
			case 17:
				return ctx.getResources().getColor(R.drawable.iii_Yellow_1);
			case 18:
				return ctx.getResources().getColor(R.drawable.iii_Red_1);
			case 19:
				return ctx.getResources().getColor(R.drawable.iii_Red_2);
			case 20:
				return ctx.getResources().getColor(R.drawable.iii_Red_3);
			case 21:
				return ctx.getResources().getColor(R.drawable.iii_Purple_1);
			case 22:
				return ctx.getResources().getColor(R.drawable.iii_Purple_2);
			case 23:
				return ctx.getResources().getColor(R.drawable.iii_Purple_3);
			default:
				return ctx.getResources().getColor(R.drawable.iii_Black_1);		
			
		} 
	}
	
	/**
	 * 取得背景drawable index
	 * @param ctx context 
	 * @param deliverId deliver id
	 * @return 背景drawable index
	 */
	public static int getBackgroundIndex(Context ctx,String deliverId){
		String[] temp = ctx.getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
		SharedPreferences settings = ctx.getSharedPreferences("reader_Preference", 0);
		String name = settings.getString("reader_setting_book_background_style_value", "");
		int i = 0;
		do{
			if(i>=temp.length||temp[i].equals(name)){
				break;
			}
			i++;
		}while(true);
		
		switch(i){
		case 0:
			return R.drawable.bg_01;
		case 1:
			return R.drawable.bg_02;
		case 2:
			return R.drawable.bg_03;
		default:
			return R.drawable.bg_01;
		}
		
//		switch(i){
//		case 0:
//			return R.drawable.bg_green;
//		case 1:
//			return R.drawable.bg_stone;
//		case 2:
//			return R.drawable.bg_maple;
//		case 3:
//			return R.drawable.bg_water;
//		case 4:
//			return R.drawable.bg_cloud;
//		default:
//			return R.drawable.bg_green;
//		}
	}
	
	/**
	 * 根據index取得背景drawable物件
	 * @param idx index，可參照strings.xml背景部分
	 * @param ctx context
	 * @return 背景drawable物件
	 */
	public static Drawable getBackgroundByIndex(int idx,Context ctx){
		switch(idx){
		case 0:
			return ctx.getResources().getDrawable(R.drawable.bg_01);
		case 1:
			return ctx.getResources().getDrawable(R.drawable.bg_02);
		case 2:
			return ctx.getResources().getDrawable(R.drawable.bg_03);
		default:
			return ctx.getResources().getDrawable(R.drawable.bg_01);
		}
		
//		switch(idx){
//		case 0:
//			return ctx.getResources().getDrawable(R.drawable.bg_green);
//		case 1:
//			return ctx.getResources().getDrawable(R.drawable.bg_stone);
//		case 2:
//			return ctx.getResources().getDrawable(R.drawable.bg_maple);
//		case 3:
//			return ctx.getResources().getDrawable(R.drawable.bg_water);
//		case 4:
//			return ctx.getResources().getDrawable(R.drawable.bg_cloud);
//		default:
//			return ctx.getResources().getDrawable(R.drawable.bg_green);
//		}
	}
	/**
	 * 取得背景的drawable物件
	 * @param ctx context
	 * @param deliverId deliver id
	 * @return 背景的drawable物件
	 */
	public static Drawable getBackground(Context ctx,String deliverId){
		String[] temp = ctx.getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
		SharedPreferences settings = ctx.getSharedPreferences("reader_Preference", 0);
		String name = settings .getString("reader_setting_book_background_style_value", "");
		int i = 0;
		do{
			if(i>=temp.length||temp[i].equals(name)){
				break;
			}
			i++;
		}while(true);
		
		switch(i){
		case 0:
			return ctx.getResources().getDrawable(R.drawable.bg_01);
		case 1:
			return ctx.getResources().getDrawable(R.drawable.bg_02);
		case 2:
			return ctx.getResources().getDrawable(R.drawable.bg_03);
		default:
			return ctx.getResources().getDrawable(R.drawable.bg_01);
		}	
		
//		switch(i){
//		case 0:
//			return ctx.getResources().getDrawable(R.drawable.bg_green);
//		case 1:
//			return ctx.getResources().getDrawable(R.drawable.bg_stone);
//		case 2:
//			return ctx.getResources().getDrawable(R.drawable.bg_maple);
//		case 3:
//			return ctx.getResources().getDrawable(R.drawable.bg_water);
//		case 4:
//			return ctx.getResources().getDrawable(R.drawable.bg_cloud);
//		default:
//			return ctx.getResources().getDrawable(R.drawable.bg_green);
//		}	  
	}
}
