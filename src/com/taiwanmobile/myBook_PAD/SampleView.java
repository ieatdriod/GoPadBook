package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 閱讀設定 預覽視窗
 * @author III
 * 
 */
public class SampleView extends View{
	private int nowTextColor = Color.BLACK;
	private int nowLineColor = Color.BLACK;
	private int nowHyperColor = Color.BLACK;
	private int nowModel = 0;	// 0文字 1底線 2超連結 
	private boolean isHorizontal = false;	// false 橫書  true 直書 
	//private boolean dayOrNight = true;	// true 日夜模式  false 平常模式
	private int nowSize = 12;
	private float nowWidth = 280;
	private float nowHeight = 80;
	private List<String> rowString = new ArrayList<String>();
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		rowString.clear();
		String view1 = getResources().getString(R.string.iii_sample_text_1)+" "+getResources().getString(R.string.iii_sample_text_2);
		//String view2 = getResources().getString(R.string.iii_sample_text_2);
		
		Paint paint = new Paint();			
		paint.setAntiAlias(true);
		
		if(nowModel == 2){
			paint.setColor(nowHyperColor);	
		}else{
			paint.setColor(nowTextColor);
		}		
		
		paint.setTextSize(nowSize);
		float[] textSize = new float[view1.length()];
		paint.getTextWidths(view1, textSize);
		
		if(isHorizontal){
			String temp1 = view1;
			//String temp2 = view2;
			int j=0;
			boolean tag = false;
			do{
				
				for(int i=0;i<temp1.length();i++){
					tag = false;
					j = i;
					if(((i+1)*nowSize)>nowHeight){
						tag = true;
						break;
					}
				}
				if(tag == false){
					j = j + 1;
					rowString.add(temp1.substring(0, j));
					temp1 = "";
				}else{
					rowString.add(temp1.substring(0, j));
					temp1 = temp1.substring(j, temp1.length());
				}
				j=temp1.length();
			}while(temp1.length() > 0);

/*			do{
				
				for(int i=0;i<temp2.length();i++){
					tag = false;
					j = i;
					if(((i+1)*nowSize)>nowHeight){
						tag = true;
						break;
					}
				}
				if(tag == false){
					j = j + 1;
					rowString.add(temp2.substring(0, j));
					temp1 = "";
				}else{
					rowString.add(temp2.substring(0, j));
					temp2 = temp1.substring(j, temp2.length());
				}
				j=temp2.length();
			}while(temp2.length() > 0);*/
			
		}else{
			String temp1 = view1;
			//String temp2 = view2;
			int j=0;
			boolean tag = false;
			float sum = 0;
			do{				
				for(int i=0;i<temp1.length();i++){
					tag = false;
					j = i;					
					sum = sum + textSize[i];
					if(sum>nowWidth){
						tag = true;
						sum = 0;
						break;
					}
				}
				if(tag == false){
					j = j + 1;
					rowString.add(temp1.substring(0, j));
					temp1 = "";
					for(int x=0;x<textSize.length;x++){
						textSize[x]=0;
					}
				}else{
					rowString.add(temp1.substring(0, j));
					temp1 = temp1.substring(j, temp1.length());
					float[] textSizeTemp = new float[view1.length()-j];
					
					for(int x=0;x<textSize.length-j;x++){
						textSizeTemp[x]= textSize[j+x];
					}
					textSize = textSizeTemp;
				}
				j=temp1.length();
			}while(temp1.length() > 0);
			
			
/*			sum = sum + textSize[i];
			if(sum>nowWidth){
				tag = true;
				break;
			}	*/
			
/*			do{
				
				for(int i=0;i<temp2.length();i++){
					tag = false;
					j = i;
					if(((i+1)*nowSize)>nowWidth){
						tag = true;
						break;
					}
				}
				if(tag == false){
					j = j + 1;
					rowString.add(temp2.substring(0, j));
					temp1 = "";
				}else{
					rowString.add(temp2.substring(0, j));
					temp1 = temp2.substring(j, temp2.length());
				}
				j=temp2.length();
			}while(temp2.length() > 0);*/
		}
		
		if(isHorizontal){
			for(int i=0; i < rowString.size()  ;i++){
				for(int j=0; j < rowString.get(i).length() ;j++){
					canvas.drawText(rowString.get(i).substring(j, j+1), nowWidth - nowSize * (i+1) - i*4, nowSize * (j+1), paint);
				}							
			}
		}else{
			for(int i=0; i < rowString.size() ;i++){
				canvas.drawText(rowString.get(i), 0, (i+1) * nowSize + i*4, paint);
			}
		}

		//float[] floatArray1 = new float[view1.length()];
		//paint.getTextWidths(view1, floatArray1);

		if(nowModel>0){
			if(nowModel == 2){
				paint.setColor(nowHyperColor);	
			}else{
				paint.setColor(nowLineColor);
			}	
			if(isHorizontal){
				for(int i=0; i < rowString.size()  ;i++){
					canvas.drawLine(nowWidth - nowSize * (i+1) - i*4, 0, nowWidth - nowSize * (i+1) - i*4, rowString.get(i).length() * nowSize, paint);
				}				
			}else{
				for(int i=0; i < rowString.size()  ;i++){
					canvas.drawLine(0, (nowSize + 4)*(i+1), rowString.get(i).length() * paint.getTextSize(), (nowSize + 4)*(i+1), paint);
				}				
			}
		}

		
		
		//Log.e("paint.getTextWidths(view, floatArray1)", String.valueOf(paint.getTextWidths(view1, floatArray1)));
		//Log.e("floatArray1", String.valueOf(floatArray1[0]));
		//Log.e("floatArray1", String.valueOf(floatArray1[1]));
		//Log.e("floatArray1", String.valueOf(floatArray1[2]));
		Log.e("canvas.getHeight()", String.valueOf(canvas.getHeight()));
		Log.e("canvas.getWidth()", String.valueOf(canvas.getWidth()));
		
	}
	/**
	 * 設定是否水平顯示
	 * @param horizontal 是否水平
	 */
	public void setHorizontal(boolean horizontal){
		isHorizontal = horizontal;
		redraw();
	}	
	/**
	 * 更新畫面
	 */
	public void redraw(){
		invalidate();
	}
	/**
	 * 設定超鏈接顏色
	 * @param color 顏色
	 */
	public void setHyperColor(int color){
		nowHyperColor = color;
		nowModel = 2;
		redraw();
	}
	/**
	 * 設定劃綫顏色
	 * @param color 顏色
	 */
	public void setLineColor(int color){
		nowLineColor = color;
		nowModel = 1;
		redraw();
	}
	/**
	 * 設定文字大小
	 * @param size 大小
	 */
	public void setTextSize(int size){
		nowSize = size;
		redraw();
	}
	/**
	 * 設定文字顏色
	 * @param color 顏色
	 */
	public void setTextColor(int color){
		nowTextColor = color;	
		nowModel = 0;
		redraw();
	}
	
	public SampleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public SampleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SampleView(Context context) {
		super(context);
	}    	
}
