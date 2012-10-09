package org.iii.ideas.reader.parser;

import java.util.HashMap;

import android.graphics.Color;

/**
 * 顏色格式轉換
 * @author III
 *
 */
public class ColorConverter {
	private static HashMap<String, String> ColorMap = null;	

	/**
	 * 顏色轉換
	 * @param value 顏色字串
	 * @return color
	 */
	public static int convertColor(String value){
		if(value==null || value.length()==0)
			return 9999;
		if(value.charAt(0)=='#'){
			return Color.parseColor(value);
		}else{
			try{
				return Color.parseColor(value);
			}catch(Exception e){
				if(value.contains("rgb"))
					return strTransColor_RGB2HEX(value);
				else{
					loadColorMap();
					return Color.parseColor(ColorMap.get(value.toLowerCase()));
				}
			}
		}
	}
	
	/**
	 * 將rgb格式轉為16進位格式
	 * @param _input 顏色字串
	 * @return color
	 */
	private static int strTransColor_RGB2HEX(String _input)
	{//input: rgb(xx,xx,xx), return: #FF0000
		String strResult = "#000000";//default = black = #000000
		try{
			//針對 _input 作容錯處理, Ray add, 2010.9.2
			_input = _input.replaceAll("\\s+", "");//除去空白
			
			String rgb = _input.substring(_input.indexOf("(")+1, _input.lastIndexOf(")"));
			//Log.d("Ray", "RGB=>"+rgb);
			String[] temp = null;
			temp = rgb.split(",");
			if(temp.length==3){
				int i = Integer.parseInt(temp[0]);
			    int j = Integer.parseInt(temp[1]);
			    int k = Integer.parseInt(temp[2]);
			    String strI = Integer.toHexString(i).toUpperCase();
			    String strJ = Integer.toHexString(j).toUpperCase();
			    String strK = Integer.toHexString(k).toUpperCase();
			    strResult = "#"+((strI.length()==1)?("0"+strI):strI)+((strJ.length()==1)?("0"+strJ):strJ)+((strK.length()==1)?("0"+strK):strK);
			}
		    //Log.d("Ray", "rgb2hex: " + strResult);
		}catch (Exception e){
			e.printStackTrace();
				//Log.d("Ray", "strTransColor_RGB2HEX error: " + e.toString());
		}

		
		return Color.parseColor(strResult);
	}
	
	/**
	 * 讀取顏色對照表
	 */
	public static void loadColorMap(){
		if(ColorMap==null){
			ColorMap = new HashMap<String,String>();
			ColorMap.put("aliceblue", "#F0F8FF");
			ColorMap.put("antiquewhite", "#FAEBD7");
			ColorMap.put("aqua", "#00FFFF");
			ColorMap.put("aquamarine", "#7FFFD4");
			ColorMap.put("azure", "#F0FFFF");
			ColorMap.put("beige", "#F5F5DC");
			ColorMap.put("bisque", "#FFE4C4");
			ColorMap.put("black", "#000000");
			ColorMap.put("blanchedalmond", "#FFEBCD");
			ColorMap.put("blue", "#0000FF");

			ColorMap.put("blueViolet", "#8A2BE2");
			ColorMap.put("brown", "#A52A2A");
			ColorMap.put("burlywood", "#DEB887");
			ColorMap.put("cadetblue", "#5F9EA0");
			ColorMap.put("chartreuse", "#7FFF00");
			ColorMap.put("chocolate", "#D2691E");
			ColorMap.put("coral", "#FF7F50");
			ColorMap.put("cornflowerblue", "#6495ED");
			ColorMap.put("cornsilk", "#FFF8DC");
			ColorMap.put("crimson", "#DC143C");

			ColorMap.put("cyan", "#00FFFF");
			ColorMap.put("darkblue", "#00008B");
			ColorMap.put("darkcyan", "#008B8B");
			ColorMap.put("darkgoldenrod", "#B8860B");
			ColorMap.put("darkgray", "#A9A9A9");
			ColorMap.put("darkgreen", "#006400");
			ColorMap.put("darkkhaki", "##BDB76B");
			ColorMap.put("darkmagenta", "#8B008B");
			ColorMap.put("darkolivegreen", "#556B2F");
			ColorMap.put("darkorange", "#FF8C00");

			ColorMap.put("darkorchid", "#9932CC");
			ColorMap.put("darkred", "#8B0000");
			ColorMap.put("darksalmon", "#E9967A");
			ColorMap.put("darkseagreen", "#8FBC8F");
			ColorMap.put("darkslateblue", "#483D8B");
			ColorMap.put("darkslategray", "#2F4F4F");
			ColorMap.put("darkturquoise", "#00CED1");
			ColorMap.put("darkviolet", "#9400D3");
			ColorMap.put("deeppink", "#FF1493");
			ColorMap.put("deepskyblue", "#00BFFF");

			ColorMap.put("dimgray", "#696969");
			ColorMap.put("dodgerblue", "#1E90FF");
			ColorMap.put("firebrick", "#B22222");
			ColorMap.put("floralwhite", "#FFFAF0");
			ColorMap.put("forestgreen", "#228B22");
			ColorMap.put("fuchsia", "#FF00FF");
			ColorMap.put("gainsboro", "#DCDCDC");
			ColorMap.put("ghostwhite", "#F8F8FF");
			ColorMap.put("gold", "#FFD700");
			ColorMap.put("goldenrod", "#DAA520");

			ColorMap.put("gray", "#808080");
			ColorMap.put("green", "#008000");
			ColorMap.put("greenyellow", "#ADFF2F");
			ColorMap.put("honeydew", "#F0FFF0");
			ColorMap.put("hotpink", "#FF69B4");
			ColorMap.put("indianred", "#CD5C5C");
			ColorMap.put("indigo", "#4B0082");
			ColorMap.put("ivory", "#FFFFF0");
			ColorMap.put("khaki", "#F0E68C");
			ColorMap.put("lavender", "#E6E6FA");

			ColorMap.put("lavenderblush", "#FFF0F5");
			ColorMap.put("lawngreen", "#7CFC00");
			ColorMap.put("lemonchiffon", "#FFFACD");
			ColorMap.put("lightblue", "#ADD8E6");
			ColorMap.put("lightcoral", "#F08080");
			ColorMap.put("lightcyan", "#E0FFFF");
			ColorMap.put("lightgoldenrodyellow", "#FAFAD2");
			ColorMap.put("lightgrey", "#D3D3D3");
			ColorMap.put("lightgreen", "#90EE90");
			ColorMap.put("lightpink", "#FFB6C1");

			ColorMap.put("lightsalmon", "#FFA07A");
			ColorMap.put("lightseagreen", "#20B2AA");
			ColorMap.put("lightskyblue", "#87CEFA");
			ColorMap.put("lightslategray", "#778899");
			ColorMap.put("lightsteelblue", "#B0C4DE");
			ColorMap.put("lightyellow", "#FFFFE0");
			ColorMap.put("lime", "#00FF00");
			ColorMap.put("limegreen", "#32CD32");
			ColorMap.put("linen", "#FFEBCD");
			ColorMap.put("magenta", "#FF00FF");

			ColorMap.put("maroon", "#800000");
			ColorMap.put("mediumaquamarine", "#66CDAA");
			ColorMap.put("mediumblue", "#0000CD");
			ColorMap.put("mediumorchid", "#BA55D3");
			ColorMap.put("mediumpurple", "#9370D8");
			ColorMap.put("mediumseagreen", "#3CB371");
			ColorMap.put("mediumslateblue", "#7B68EE");
			ColorMap.put("mediumspringgreen", "#00FA9A");
			ColorMap.put("mediumturquoise", "#48D1CC");
			ColorMap.put("mediumvioletred", "#C71585");

			ColorMap.put("midnightblue", "#191970");
			ColorMap.put("mintcream", "#F5FFFA");
			ColorMap.put("mistyrose", "#FFE4E1");
			ColorMap.put("moccasin", "#FFE4B5");
			ColorMap.put("navajowhite", "#FFDEAD");
			ColorMap.put("navy", "#000080");
			ColorMap.put("oldlace", "#FDF5E6");
			ColorMap.put("olive", "#808000");
			ColorMap.put("olivedrab", "#6B8E23");
			ColorMap.put("orange", "#FFA500");

			ColorMap.put("orangered", "#FF4500");
			ColorMap.put("orchid", "#DA70D6");
			ColorMap.put("palegoldenrod", "#EEE8AA");
			ColorMap.put("palegreen", "#98FB98");
			ColorMap.put("paleturquoise", "#AFEEEE");
			ColorMap.put("palevioletRred", "#D87093");
			ColorMap.put("papayawhip", "#FFEFD5");
			ColorMap.put("peachpuff", "#FFDAB9");
			ColorMap.put("peru", "#CD853F");
			ColorMap.put("pink", "#FFC0CB");

			ColorMap.put("plum", "#DDA0DD");
			ColorMap.put("powderblue", "#B0E0E6");
			ColorMap.put("purple", "#800080");
			ColorMap.put("red", "#FF0000");
			ColorMap.put("rosybrown", "#BC8F8F");
			ColorMap.put("royalblue", "#4169E1");
			ColorMap.put("saddlebrown", "#8B4513");
			ColorMap.put("salmon", "#FA8072");
			ColorMap.put("sandybrown", "#F4A460");
			ColorMap.put("seagreen", "#2E8B57");

			ColorMap.put("seashell", "#FFF5EE");
			ColorMap.put("sienna", "#A0522D");
			ColorMap.put("silver", "#C0C0C0");
			ColorMap.put("skyblue", "#87CEEB");
			ColorMap.put("slateblue", "#6A5ACD");
			ColorMap.put("slategray", "#708090");
			ColorMap.put("snow", "#FFFAFA");
			ColorMap.put("springgreen", "#00FF7F");
			ColorMap.put("steelblue", "#4682B4");
			ColorMap.put("tan", "#D2B48C");

			ColorMap.put("teal", "#008080");
			ColorMap.put("thistle", "#D8BFD8");
			ColorMap.put("tomato", "#FF6347");
			ColorMap.put("turquoise", "#40E0D0");
			ColorMap.put("violet", "#EE82EE");
			ColorMap.put("wheat", "#F5DEB3");
			ColorMap.put("white", "#FFFFFF");
			ColorMap.put("whitesmoke", "#F5F5F5");
			ColorMap.put("yellow", "#FFFF00");
			ColorMap.put("yellowgreen", "#9ACD32");
		}
	} 
}
