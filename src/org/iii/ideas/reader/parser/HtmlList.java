package org.iii.ideas.reader.parser;

/**
 * html ol和ol的處理class
 * @author III
 * 
 */
public class HtmlList {
	public static final String CIRCLE = "○";
	public static final String DISC = "●";
	public static final String SQUARE = "■";
	public static final int TYPE_CIRCLE=1;
	public static final int TYPE_DISC=0;
	public static final int TYPE_SQUARE=2;
	public static final int TYPE_1 = 3;
	public static final int TYPE_a = 4;
	public static final int TYPE_A = 5;
	public static final int OFFSET_a = 97;
	public static final int OFFSET_A = 65;
	public static final String ITEM_DOT = ". ";
	public static final String ITEM_SPACE = " ";
	/**
	 * 在list項目前加入編號或bullet point
	 * @param item 該項目文字內容
	 * @param type 類別
	 * @param value 序號
	 * @return 加入prefix後的項目
	 */
	public static StringBuilder addPrefix(StringBuilder item,int type,int value){
		//Log.d("type",":"+type);
		switch(type){
		case TYPE_CIRCLE:
			item.append(CIRCLE).append(ITEM_SPACE);
			//item.insert(0, CIRCLE+". ");
			break;
		case TYPE_DISC:
			item.append(DISC).append(ITEM_SPACE);
			//item.insert(0, DISC+". ");
			break;
		case TYPE_SQUARE:
			item.append(SQUARE).append(ITEM_SPACE);
			//item.insert(0, SQUARE+". ");
			break;
		case TYPE_1:
			item.append(String.valueOf(value)).append(ITEM_DOT);
			//item.insert(0, String.valueOf(value)+". ");
			break;
		case TYPE_A:
			if(value<=26){
				char cA = (char)(OFFSET_A+value-1);
				item.append(cA).append(ITEM_DOT);
			}else{
				item.append(transformToAlphabet(value-1).toUpperCase()).append(ITEM_DOT);
			}
			//item.insert(0, String.valueOf(cA)+". ");
			break;
		case TYPE_a:
			if(value<=26){
				char ca = (char)(OFFSET_a+value-1);
				item.append(ca).append(ITEM_DOT);
			}else{
				item.append(transformToAlphabet(value-1)).append(ITEM_DOT);
			}
			//item.insert(0, String.valueOf(ca)+". ");
			break;
		}
		return item;
	}
	
	/**
	 * 將數字序號轉成a,b,c,...的排序
	 * @param num 序號
	 * @return 英文編號
	 */
	public static String transformToAlphabet(int num){
		final int radix =26;
		int rem=0;
		if(num==0){
			return "a";
		}else{
			StringBuilder builder = new StringBuilder();
			while(num>0){
				rem = num % radix;
				num=(num-rem)/radix;
				builder.insert(0, (char)(rem+OFFSET_a));
			}
			return builder.toString();
		}
	}
}
