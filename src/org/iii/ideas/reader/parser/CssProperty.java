package org.iii.ideas.reader.parser;

import java.util.HashMap;

import org.iii.ideas.reader.renderer.RendererConfig;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.taiwanmobile.myBook_PAD.PtPxConverter;

/**
 * 處理呈現屬性(如大小,顏色,align等)的class
 * @author III
 * 
 *
 */
public class CssProperty {
	/**
	 * 單位，百分比
	 */
	public final static int UNIT_PER=0;
	/**
	 * 單位，em
	 */
	public final static int UNIT_EM=1;
	/**
	 * 單位，px
	 */
	public final static int UNIT_PX=2;

	/**
	 * 置左對齊
	 */
	public final static int ALIGN_LEFT=0;
	/**
	 * 左右對齊
	 */
	public final static int ALIGN_JUSTIFY=1;
	/**
	 * 置右對齊
	 */
	public final static int ALIGN_RIGHT=2;
	/**
	 * 置中對齊
	 */
	public final static int ALIGN_CENTER=3;
	/**
	 * font family，Droid Sans Mono
	 */
	public final static String MONO = "DroidSansMono";
	/**
	 * font family，Droid Serif
	 */
	public final static String SERIF = "DroidSerif";
	/**
	 * font family，Droid Sans
	 */
	public final static String SANS = "DroidSans";
	//private UnitValue margins[]=new UnitValue[4];
	//private boolean isMarginsSet=false;
	private float fontSize;
	private boolean isFontSizeSet=false;
	private float textIndent;
	private boolean isIndentSet=false;
	private float lineHeight;
	private boolean isLineHeightSet=false;
	private int textAlign;
	private boolean isAlignSet=false;
	private boolean isBold;
	private boolean isItalic;
	private String tagName;
	private HashMap<String,String> cssSet;
	private int color;
	private boolean isColorSet=false;
	private int bgColor;
	private boolean isBgColorSet=false;
	private boolean inheritBgColor=true;
	private boolean isHrWidthSet=false;
	private UnitValue hrWidth;
	private ImgAtts imgAtts=null;
	private boolean isImgAttsSet=false;
	private Typeface typeface =null;
	private boolean isFontFamilySet = false;
	
	/**
	 * 該element之tag名稱(如div, p, b等)
	 * @param tag tag name
	 */
	public CssProperty(String tag){
		tagName=tag;
		initialize();	
		/*if(tagName.equalsIgnoreCase("hr")){
			Log.d("call handle","hrcolor");
			handleHrWidth();
			handleHrColor();
		}*/
	}
	
	/**
	 * 
	 * @param tag 該element之tag名稱
	 * @param parent parent property
	 * @param inheritBgColor_ 是否要繼承背景顏色
	 */
	public CssProperty(String tag,CssProperty parent,boolean inheritBgColor_){
		tagName=tag;
		inheritBgColor=inheritBgColor_;
		initialize();	
		inheritParent(parent);
		/*if(tagName.equalsIgnoreCase("hr")){
			Log.d("call handle","hrcolor");
			handleHrWidth();
			handleHrColor();
		}*/
		
	}
	
	/**
	 * 
	 * @param cssSet_ parse後的css對照表
	 * @param tag 該element之tag名稱
	 */
	public CssProperty(HashMap<String,String> cssSet_,String tag){
		tagName=tag;
		initialize();	
		if(cssSet_!=null){
			cssSet=cssSet_;
			handleProperty();
		}
	}
	
	/**
	 * 開始處理屬性
	 */
	private void handleProperty(){
		handleFontSize();
		handleFontStyle();
		handleFontWeight();
		handleAlign();
		handleLineHeight();
		handleIndent();
		//handleMargin();
		handleFontFamily();
		if(tagName.equalsIgnoreCase("img")){
			handleImgAttributes();
		}
		if(tagName.equalsIgnoreCase("hr")){
			//Log.d("call handle","hrcolor");
			handleHrWidth();
			//handleHrColor();
		}else{
			handleColor();
			handleBgColor();
		}
		cssSet=null;
	}
	
	/**
	 * 
	 * @param cssSet_ css parser產生的css對照表
	 * @param tag 該element之tag名稱
	 * @param parent parent property
	 */
	public CssProperty(HashMap<String,String> cssSet_,String tag, CssProperty parent){
		tagName=tag;
		initialize();	
		inheritParent(parent);
		if(cssSet_!=null){
			cssSet=cssSet_;
			handleProperty();
		}
	}
	
	/**
	 * 處理圖片屬性
	 */
	private void handleImgAttributes(){
		if(isImgAttsSet)
			imgAtts.setValue(cssSet.get("border"),cssSet.get("border-width"),cssSet.get("width"),cssSet.get("height"));
		else
			setImgAttributes(new ImgAtts(cssSet.get("border"),cssSet.get("border-width"),cssSet.get("width"),cssSet.get("height")));
	}
	
	/**
	 * 設定圖片屬性
	 * @param atts 圖片屬性物件
	 */
	private void setImgAttributes(ImgAtts atts){
		imgAtts = atts;
		isImgAttsSet=true;
	}
	
	/**
	 * 取得圖片屬性
	 * @return 圖片屬性
	 */
	public ImgAtts getImgAtts(){
		return imgAtts;
	}
	
	/**
	 * 處理繼承parent property
	 * @param parent parent property
	 */
	public void inheritParent(CssProperty parent){
		setBold(parent.isBold());
		setItalic(parent.isItalic);
		/*if(parent.isMarginsSet())
			setMargins(parent.getMargins());*/
		if(parent.isFontSizeSet())
			setFontSize(parent.getFontSize());
		if(parent.isIndentSet())
			setIndent(parent.getIndent());
		if(parent.isLineHeightSet())
			setLineHeight(parent.getLineHeight());
		if(parent.isAlignSet())
			setAlign(parent.getAlign());
		if(parent.isColorSet)
			setColor(parent.getColor());
		if(parent.isBgColorSet && inheritBgColor)
			setBgColor(parent.getBgColor());
		if(parent.isImgAttsSet()){
			setImgAttributes(new ImgAtts(parent.getImgAtts()));
		}
		if(parent.isFontFamilySet){
			setFontFamily(parent.getFontFamily());
		}
	}
	
	/**
	 * 初始化class
	 */
	private void initialize(){
		fontSize=1f;
		/*for(int i=0;i<margins.length;i++){
			margins[i]=new UnitValue(0f,UNIT_PX);
		}*/
		textIndent=0;
		lineHeight=1.5f;
		textAlign=ALIGN_LEFT;
		isBold=false;
		isItalic=false;
	}
	
	/**
	 * 設定相對應的element tag 名稱
	 * @param tag tag name
	 */
	public void setTagName(String tag){
		tagName=tag;
	}
	
	/**
	 * 取得tag名稱
	 * @return tag name
	 */
	public String getTagName(){
		return tagName;
	}
	
	/**
	 * 設定字體大小
	 * @param v 字體大小值
	 */
	public void setFontSize(float v){
		//Log.d("SetFS","value:"+v);
		isFontSizeSet=true;
		fontSize=v;
	}
	
	/**
	 * 設定對齊方式
	 * @param align 對齊方式
	 */
	public void setAlign(int align){
		//Log.d("setAlign tagName","is:"+tagName);
		//Log.d("SetAlign","v:"+align);
		isAlignSet=true;
		textAlign=align;
	}
	
	/**
	 * 設定縮排
	 * @param v 縮排多少
	 */
	public void setIndent(float v){
		//Log.d("SetIndent","value:"+v);
		isIndentSet=true;
		textIndent=v;
	}
	
	/**
	 * 設定行高
	 * @param v 行高
	 */
	public void setLineHeight(float v){
		//Log.d("SetLineH","value:"+v);
		isLineHeightSet=true;
		if(v>1)
			lineHeight=v;
		else
			lineHeight=1;
			
	}
	
	/**
	 * 設定顏色
	 * @param color_ 顏色
	 */
	public void setColor(int color_){
		isColorSet=true;
		color=color_;
	}
	
	/**
	 * 設定背景顏色
	 * @param color_ 背景顏色
	 */
	public void setBgColor(int color_){
		//Log.d("CssProperty","bgcolor:"+color);
		isBgColorSet=true;
		bgColor=color_;
	}
	
	/**
	 * 設定字體(font family)
	 * @param tf 字體
	 */
	public void setFontFamily(Typeface tf){
		//Log.d("setFontfamily",":"+tf.toString());
		isFontFamilySet=true;
		typeface = tf;
	}
	
	/**
	 * 處理字體屬性
	 */
	private void handleFontFamily(){
		String value;
		if((value=cssSet.get("font-family"))!=null){
			//Log.d("in","handlefontfamilyv1:"+value+"]");
			//value=value.trim();
			//Log.d("in","handlefontfamilyv2:"+value+"]");
			if(value.equalsIgnoreCase(MONO)){
				setFontFamily(Typeface.MONOSPACE);
			}else if(value.equalsIgnoreCase(SANS)){
				setFontFamily(Typeface.SANS_SERIF);
			}else if(value.equalsIgnoreCase(SERIF)){
				setFontFamily(Typeface.SERIF);
			}
		}
	}
	
	/**
	 * 處理分隔線寬度
	 */
	public void handleHrWidth(){
		String value;
		if((value=cssSet.get("iii_hr_width"))!=null){
			value=value.trim();
			setHrWidth(getUnitValueFromString(value));
		}
	}
	
	/**
	 * 設定分隔線寬度
	 * @param uv 寬度
	 */
	public void setHrWidth(UnitValue uv){
		if(uv!=null){
			isHrWidthSet=true;
			hrWidth=uv;
		}
	}
	
	/**
	 * 取得分隔線寬度
	 * @param w 螢幕寬度
	 * @return 分隔線寬度
	 */
	public int getHrWidth(int w){
		if(isHrWidthSet && hrWidth != null){
			switch(hrWidth.unit){
			case UNIT_PER:
				return (int) (w*hrWidth.value);
			case UNIT_EM:
				return (int) (RendererConfig.DEFAULT_EM_SIZE*w);
			case UNIT_PX:
				return (int) hrWidth.value;
			}
		}
		
		return w;
	}
	
	/**
	 * hr寬度是否設定
	 * @return hr寬度是否設定
	 */
	public boolean isHrWidthSet(){
		return isHrWidthSet;
	}
	
	/**
	 * 將字串(如"10px")轉為UnitValue物件
	 * @param value 數值字串
	 * @return unit value
	 */
	public static UnitValue getUnitValueFromString(String value){
		int idx;
		UnitValue uv = new UnitValue(0f,UNIT_PX);
		try{
			if((idx=value.indexOf("em")) >= 0){
				uv.unit = UNIT_EM;
				uv.value=Float.parseFloat(value.substring(0, idx));
			}else if((idx=value.indexOf("%")) >= 0){
				uv.unit = UNIT_PER;
				uv.value=Float.parseFloat(value.substring(0, idx))/100f;
			}else if((idx=value.indexOf("px")) >= 0){
				uv.unit = UNIT_PX;
				uv.value=Float.parseFloat(value.substring(0, idx));
			}else if((idx=value.indexOf("pt")) >= 0){
				uv.unit = UNIT_PX;
				uv.value=PtPxConverter.getPxFromPt((int) Float.parseFloat(value.substring(0, idx)));
			}else if((idx=value.indexOf("thin")) >= 0){
				uv.unit = UNIT_PX;
				uv.value=RendererConfig.BORDER_THIN;
			}else if((idx=value.indexOf("medium")) >= 0){
				uv.unit = UNIT_PX;
				uv.value=RendererConfig.BORDER_MEDIUM;
			}else if((idx=value.indexOf("thick")) >= 0){
				uv.unit = UNIT_PX;
				uv.value=RendererConfig.BORDER_THICK;
			}else{
				uv.unit = UNIT_PX;
				uv.value = Float.parseFloat(value);
			}
		}catch(Exception e){
			//Log.e("CssProperty:getUnitValueFromString",e.toString());
			return null;
		}
		return uv;
	}
	
	/**
	 * 從字串標明的值轉為比例
	 * @param value 數值字串 
	 * @return 比例
	 */
	private float getValueFromString(String value){
		int idx;
		float v = 1f;
		try{
			if((idx=value.indexOf("em")) >= 0){
				v=Float.parseFloat(value.substring(0, idx));
			}else if((idx=value.indexOf("%")) >= 0){
				v=Float.parseFloat(value.substring(0, idx))/100f;
			}else if((idx=value.indexOf("px")) >= 0){
				v=Float.parseFloat(value.substring(0, idx))/RendererConfig.DEFAULT_EM_SIZE;
			}else if((idx=value.indexOf("pt")) >= 0){
				v=PtPxConverter.getPxFromPt((int) Float.parseFloat(value.substring(0, idx)))/RendererConfig.DEFAULT_EM_SIZE;
			}else if(value.equals("xx-small") ){
				v=0.5f;
			}else if(value.equals("x-small") ){
				v=0.6f;
			}else if(value.equals("smaller") ){
				v=0.7f;
			}else if(value.equals("small") ){
				v=0.85f;
			}else if(value.equals("xx-large") ){
				v=1.5f;
			}else if(value.equals("x-large") ){
				v=1.4f;
			}else if(value.equals("larger") ){
				v=1.3f;
			}else if(value.equals("large") ){
				v=1.15f;
			}else if(value.equals("medium") ){
				v=1;
			}else{
				v = Float.parseFloat(value)/RendererConfig.DEFAULT_EM_SIZE;
			}
		}catch(Exception e){
			//Log.e("CssProperty:getValueFromString",e.toString());
		}
		return v;
	}
	
	/**
	 * 處理字體大小
	 */
	private void handleFontSize(){
		String value;
		if((value=cssSet.get("font-size"))!=null){
			value=value.trim();
			setFontSize(getValueFromString(value));
			//setAlign(getValueFromString(cssSet.get("line-height")));
		}
	}
	
	/**
	 * 處理font style如粗/斜體
	 */
	private void handleFontStyle(){
		String value;
		if((value=cssSet.get("font-style"))!=null){
			value=value.trim();
			if(value.equalsIgnoreCase("italic")){
				setItalic(true);
			}
			//setAlign(getValueFromString(cssSet.get("line-height")));
		}
	}
	
	/**
	 * 處理文字的weight(如bold)
	 */
	private void handleFontWeight(){
		String value;
		if((value=cssSet.get("font-weight"))!=null){
			value=value.trim();
			if(value.equalsIgnoreCase("bold")){
				setBold(true);
			}
			//setAlign(getValueFromString(cssSet.get("line-height")));
		}
	}
	/**
	 * 處理對齊方式
	 */
	private void handleAlign(){
		String value;
		if((value=cssSet.get("text-align"))!=null){
			value=value.trim();
			//Log.d("tag:"+tagName,"alignValue:"+value);
			if(value.equalsIgnoreCase("left")){
				setAlign(ALIGN_LEFT);
			}else if(value.equalsIgnoreCase("right")){
				setAlign(ALIGN_RIGHT);
			}else if(value.equalsIgnoreCase("center")){
				setAlign(ALIGN_CENTER);
			}else{
				setAlign(ALIGN_JUSTIFY);
			}
			//setAlign(getValueFromString(cssSet.get("line-height")));
		}
	}
	
	/**
	 * 處理行高
	 */
	private void handleLineHeight(){
		if(cssSet.get("line-height")!=null){
			setLineHeight(getValueFromString(cssSet.get("line-height")));
		}
	}
	
	/**
	 * 處理縮排
	 */
	private void handleIndent(){
		if(cssSet.get("text-indent")!=null){
			//Log.d("handleIndent","vStr:"+cssSet.get("text-indent"));
			setIndent(getValueFromString(cssSet.get("text-indent")));
		}
	}
	
	/**
	 * 處理顏色
	 */
	private void handleColor(){
		try {
			if (cssSet.get("color") != null) {
				//Log.d("cssSet.getColor",":"+cssSet.get("color"));
				//Log.d("handleIndent","vStr:"+cssSet.get("text-indent"));
				int tempColor = parseColor(cssSet.get("color"));
				if(tempColor<=0)
					setColor(tempColor);
			}
		} catch (Exception e) {
			// TODO: handle exception
			//Log.e("CssProperty:handleColor",e.toString());
			//Log.e("CssProperty:handleColor",cssSet.get("color"));
		}
	}
	
	/**
	 * parse顏色
	 * @param color color string
	 * @return color(int)
	 */
	private int parseColor(String color){
		try{
			//return Color.parseColor(color);
			//Log.d("Color",":"+color);
			return ColorConverter.convertColor(color.trim());    
		}catch(Exception e){
			//Log.e("CssProperty:parseColor",e.toString());
			return 9999;
		}
	}
	

	/**
	 * 處理背景顏色
	 */
	private void handleBgColor(){
		try {
			if (cssSet.get("background-color") != null) {
				//Log.d("cssSet.getColor",":"+cssSet.get("color"));
				//Log.d("handleIndent","vStr:"+cssSet.get("text-indent"));
				int tempColor = parseColor(cssSet.get("background-color"));
				if(tempColor<=0)
					setBgColor(tempColor);
			}
		} catch (Exception e) {
			// TODO: handle exception
			//Log.e("CssProperty:handleBgColor",e.toString());
			//Log.e("CssProperty:handleBgColor",cssSet.get("color"));
		}
	}
	

	/**
	 * 設定為粗體
	 * @param isB 是否為粗體
	 */
	public void setBold(boolean isB){
		isBold=isB;
	}
	
	/**
	 * 設定為斜體
	 * @param isI 是否為斜體
	 */
	public void setItalic(boolean isI){
		//Log.d("tag:"+tagName,"setItalic:"+isI);
		isItalic=isI;
	}
	
	Paint bgPaint;
	/**
	 * 取得背景paint
	 * @return 背景paint
	 */
	public Paint getBgPaint(){
		if(isBgColorSet){
			if(bgPaint==null)
				bgPaint = new Paint();
			bgPaint.setColor(bgColor);
			return bgPaint;
		}else{
			return null;
		}
	}
	
	TextPaint textPaint=null;
	/**
	 * 取得文字TextPaint
	 * @param fs 閱讀設定字體大小
	 * @param headerSize header size
	 * @return text paint
	 */
	public TextPaint getTextPaint(int fs,int headerSize){
		//Log.d("cssProperty","initialize is italic:"+isItalic);
		if(textPaint==null){
			textPaint=new TextPaint();
			textPaint.setColor(Color.BLACK);
			RendererConfig.enhanceTextPaint(textPaint);
			if(isColorSet)
				textPaint.setColor(color);
			if(isBold){
				if(isItalic){
					textPaint.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD_ITALIC));
				}else{
					textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				}
			}else{
				if(isItalic){
					textPaint.setTypeface(Typeface.create(Typeface.SERIF,Typeface.ITALIC));
				}else{
					if(isFontFamilySet){
						//Log.d("getTPTypeface0",":"+typeface);
						textPaint.setTypeface(typeface);
						//Log.d("getTPTypeface1",":"+textPaint.getTypeface());
					}else{
						textPaint.setTypeface(Typeface.defaultFromStyle( Typeface.NORMAL));
					}
				}
			}
			
		}

		//Log.d("cssProperty","test ss is italic:"+Typeface.create(Typeface.SANS_SERIF,Typeface.ITALIC).isItalic());
		//Log.d("cssProperty","test s is italic:"+Typeface.create(Typeface.SERIF,Typeface.ITALIC).isItalic());
		//Log.d("cssProperty","test m is italic:"+Typeface.create(Typeface.MONOSPACE,Typeface.ITALIC).isItalic());
		//Log.d("cssProperty","return is italic:"+paint.getTypeface().isItalic());
		if(headerSize>0 && headerSize<6)
			textPaint.setTextSize(getHeaderFontSizeInPx(fs,headerSize));
		else
			textPaint.setTextSize(getFontSizeInPx(fs));
		return textPaint;	
	}
	
	/**
	 * 取得字體大小，以px為單位
	 * @param fs 閱讀設定字體大小
	 * @param headerSize header size
	 * @return 字體大小
	 */
	public int getHeaderFontSizeInPx(int fs, int headerSize){
		if(isFontSizeSet()){
			//Log.d("getHeader","fs:"+(fs*fontSize));
			return (int) (fs*fontSize);
		}else{
			return (int) (fs*HeaderConverter.getEm(headerSize));
		}
	}
	
	/**
	 * 取得font family Typeface物件
	 * @return typeface
	 */
	public Typeface getFontFamily(){
		return typeface;
	}
	
	/**
	 * 取得header行高，以px為單位
	 * @param fs 閱讀設定字體大小
	 * @param headerSize header size
	 * @return header行高
	 */
	public int getHeaderLineHeightInPx(int fs, int headerSize){
		//Log.d("getHeaderLineHeightInPx","fs:"+fs);
		//Log.d("getHeaderLineHeightInPx","lh ratio:"+lineHeight);
		//Log.d("getHeaderLineHeightInPx","h ratio:"+HeaderConverter.getEm(headerSize));
		if(isLineHeightSet()){
			return (int) (getHeaderFontSizeInPx(fs,headerSize)*lineHeight);
		}else if(isFontSizeSet()){
			return (int) (getHeaderFontSizeInPx(fs,headerSize)*1.5f);			
		}else{
			return (int) (fs*HeaderConverter.getEm(headerSize)*1.5f);
		}
	}
	
	/**
	 * 取得字體大小，以px為單位
	 * @param fs 字體大小base
	 * @return 字體大小
	 */
	public int getFontSizeInPx(int fs){
		return (int) (fs*fontSize);
	}
	
	/**
	 * 取得字體大小(與base的比例，如0.9, 1.2，故為float)
	 * @return 字體大小
	 */
	public float getFontSize(){
		return fontSize;
	}
	
	/**
	 * 取得顏色
	 * @return 顏色
	 */
	public int getColor(){
		return color;
	}
	
	/**
	 * 取得背景顏色
	 * @return 背景顏色
	 */
	public int getBgColor(){
		return bgColor;
	}
	
	/**
	 * 取得行高，以px為單位
	 * @param fs 字體大小base
	 * @return 行高
	 */
	public int getLineHeightInPx(int fs){
		//Log.d("getLineHeightInPx","fs:"+getFontSizeInPx(fs));
		//Log.d("getLineHeightInPx","lh ration:"+lineHeight);
			return (int) (getFontSizeInPx(fs)*lineHeight);
	}
	
	/**
	 * 取得行高(比例)
	 * @return 行高
	 */
	public float getLineHeight(){
		return lineHeight;
	}
	/**
	 * 取得縮排大小
	 * @param fs 字體大小base
	 * @return 縮排大小
	 */
	public int getIndentInPx(int fs){
		if(textAlign==ALIGN_CENTER || textAlign==ALIGN_RIGHT)
			return 0;
		else
			return (int) (fs*textIndent);
	}
	
	/**
	 * 取得縮排
	 * @return 縮排比例
	 */
	public float getIndent(){
		return textIndent;
	}
	
	/**
	 * 取得對齊方式
	 * @return 對齊方式
	 */
	public int getAlign(){
		//Log.d("GetAlign","is:"+textAlign);
		return textAlign;
	}
	
	/**
	 * 是否為粗體
	 * @return 是否為粗體
	 */
	public boolean isBold(){
		return isBold;
	}
	
	/**
	 * 是否為斜體
	 * @return 是否為斜體
	 */
	public boolean isItalic(){
		return isItalic;
	}
	
	/**
	 * 是否設定字體大小
	 * @return 是否設定字體大小
	 */
	public boolean isFontSizeSet(){
		return isFontSizeSet;
	}
	
	/**
	 * 是否設定indent
	 * @return  是否設定indent
	 */
	public boolean isIndentSet(){
		return isIndentSet;
	}
	
	/**
	 * 是否設定行高
	 * @return 是否設定行高
	 */
	public boolean isLineHeightSet(){
		return isLineHeightSet;
	}
	
	/**
	 * 是否設定對齊方式
	 * @return 是否設定對齊方式
	 */
	public boolean isAlignSet(){
		return isAlignSet;
	}
	
	/**
	 * 是否設定圖片屬性
	 * @return 是否設定圖片屬性
	 */
	public boolean isImgAttsSet(){
		return isImgAttsSet;
	}
	
}