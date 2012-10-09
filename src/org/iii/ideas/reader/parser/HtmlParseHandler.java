package org.iii.ideas.reader.parser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;

import org.iii.ader.cssParser;
import org.iii.ideas.android.general.AndroidLibrary;
import org.iii.ideas.reader.PartialUnzipper;
import org.iii.ideas.reader.parser.property.SpecialProperty;
import org.iii.ideas.reader.renderer.RendererConfig;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * parse html 文件的sax handler，負責將文件轉為HtmlSpan的array list
 * @author III
 * 
 * 
 */
public class HtmlParseHandler extends DefaultHandler{
	/**
	 * h1~h6 hash map for parsing
	 */
	public static HashMap<String,Integer> headerMap=null;
	/**
	 * header array
	 */
	public static final  String headers[] = {"h1","h2","h3","h4","h5","h6"};
	private int listType=0;
	private int brCount=0;
	private String docDirPath;
	private String curHref="";
	private String imgSrc;
	private int headerSize=0;
	private boolean in_body=false;
	private boolean in_ul=false;
	private boolean in_ol=false;
	private boolean in_li=false;
	private boolean in_header=false;
	private boolean in_a=false;
	private boolean in_sup=false;
	private boolean in_sub=false;
	private boolean in_pre=false;
	private boolean in_u=false;
	private boolean in_strike=false;
	private boolean in_title=false;
	//private boolean in_div=false;
	private boolean in_table=false;
	//private boolean in_center=false;
	private boolean in_style=false;
	private StringBuilder styleBuilder;
	private ArrayList<HtmlSpan> content = new ArrayList<HtmlSpan>();
	private PartialUnzipper uz;
	private HtmlSpan span;
	protected boolean isCallBack = false;
	private HtmlSpanReceiver hsr;
	protected int spanCounter=0;
	private int threadIdx;
	private boolean shouldStop=false;
	private cssParser cssParser = new cssParser();
	private HashMap<String,HashMap<String,String>> cssSet;
	private boolean isCssSetSet=false;
	private Stack<CssProperty> stack = new Stack<CssProperty>();
	private int liCounter;
	/**
	 * ops tag list
	 * @author III
	 *
	 */
    public static enum TagList{
    	title,br,a,body,table,img,p,div,link,ul,ol,li,image,h1,h2,h3,h4,h5,h6,center,
    	span,font,strong,b,i,u,strike,s,hr,style,
    	//newly supported
    	address, cite, code, dfn, kbd, samp, var, del, ins, em, sub, sup, pre,
    	//extra
    	head,html,abbr, acronym,  blockquote, q,  
    	dl, dt, dd, object, param, big, small, tt, bdo,
    	caption, col, colgroup, tbody, td, tfoot, th, thead, tr,
    	area, map,  meta, base,
    }
    public static enum NonBlockElement{
    	a,span,font,strong,b,i,u,strike,s,
    	//newly supported
    	address, cite, code, dfn, kbd, samp, var, del, ins, em, sub, sup, pre,
    }
    private int specialPropertyDepth=0;
    /**
     * 
     * @param path html document path
     * @param uz_ unzipper
     */
	public HtmlParseHandler(String path,PartialUnzipper uz_){
		super();
		uz=uz_;
		docDirPath = path.substring(0, path.lastIndexOf("/")+1);
	}
	
	/**
	 * 
	 * @param path html document path
	 * @param uz_ unzipper
	 * @param hsr_ receiver
	 * @param tidx thread index
	 */
	public HtmlParseHandler(String path,PartialUnzipper uz_,HtmlSpanReceiver hsr_,final int tidx){
		super();
		uz=uz_;
		docDirPath = path.substring(0, path.lastIndexOf("/")+1);
		hsr=hsr_;
		threadIdx=tidx;
		isCallBack=true;
	}
	
	/**
	 * 將CssProperty push到stack裡，以建立css屬性的階層架構
	 * @param tagName element tag名稱
	 * @param atts 該element之attributes
	 * @param inheritBgColor 是否要繼承顏色
	 */
	public void pushCssProperty(String tagName,Attributes atts,boolean inheritBgColor){
		//String[] cssKeys = {tagName,"#"+atts.getValue("", "id"),"."+atts.getValue("", "class"),tagName+"."+atts.getValue("", "class"),tagName+"#"+atts.getValue("", "id")};
		ArrayList<String> cssKeys = new ArrayList<String>();
		//initialize
		cssKeys.add(tagName);
		String idAtt,classAtt;
		if((idAtt=atts.getValue("", "id"))!=null){
			cssKeys.add("#"+idAtt);
			cssKeys.add(tagName+"#"+idAtt);
		}
		if((classAtt=atts.getValue("", "class"))!=null){
			//Log.d("class","name:"+classAtt);
			int start=0,len=classAtt.length();
			for(int i=0;i<len;i++){
				if(Character.isWhitespace(classAtt.charAt(i))){
					if(i>start){
						String key = classAtt.substring(start, i);
						cssKeys.add("."+key);
						cssKeys.add(tagName+"."+key);
					}
						
					start=i+1;
				}
			}
			if(start<len){
				String key = classAtt.substring(start, len);
				cssKeys.add("."+key);
				cssKeys.add(tagName+"."+key);
			}
			
			/*if(!classAtt.contains(" ")){
				cssKeys.add("."+classAtt);
				cssKeys.add(tagName+"."+classAtt);
			}else{
				String[] classes=classAtt.split("\\s");
				for(String c : classes){
					cssKeys.add("."+c);
					cssKeys.add(tagName+"."+c);
				}
			}*/
			//cssKeys.add("."+atts.getValue("", "class"));
			//cssKeys.add(tagName+"."+atts.getValue("", "class"));
		}
		
		//for(int i=0;i<atts.getLength();i++){
		//	Log.d("Atts:"+atts.getLocalName(i),"type:"+atts.getType(i));
		//}
			CssProperty tempProperty = null;
			if(!stack.empty())
				tempProperty = new CssProperty(tagName,stack.peek(),inheritBgColor);
			if(isCssSetSet){
				HashMap<String,String> tempCssTable=null;
				for(String key : cssKeys){
					tempCssTable = cssSet.get(key);
					if(tempCssTable!=null){
						if(tempProperty!=null){
							tempProperty = new CssProperty(tempCssTable,tagName,tempProperty);
						}else{
							tempProperty = new CssProperty(tempCssTable,tagName);
						}
					}
				}
				/*for(int i=0;i<cssKeys.size();i++){
					//if(cssKeys[i]=null){
						tempCssTable = cssSet.get(cssKeys.get(i));
						if(tempCssTable!=null){
							if(tempProperty!=null){
								tempProperty = new CssProperty(tempCssTable,tagName,tempProperty);
							}else{
								tempProperty = new CssProperty(tempCssTable,tagName);
							}
						}
					//}
				}*/
			}
			String styleAtts = atts.getValue("", "style");
			String alignAtts = atts.getValue("", "align");
			
			if(styleAtts!=null || alignAtts!=null)
				if(styleAtts!=null || alignAtts!=null){
				HashMap<String,String> inlineStyleTable=null;
				if(styleAtts!=null)
					inlineStyleTable=cssParser.cssParseFromInline(styleAtts);
				if(inlineStyleTable==null)
					inlineStyleTable = new HashMap<String,String>();
				if(alignAtts!=null)
					inlineStyleTable.put("text-align", atts.getValue("", "align"));
				if(tempProperty!=null){
					tempProperty = new CssProperty(inlineStyleTable,tagName,tempProperty);
				}else{
					tempProperty = new CssProperty(inlineStyleTable,tagName);
				}
			}
			
			if(tempProperty!=null){
				stack.push(tempProperty);
				//Log.d("push",":"+tagName);
			}else{
				stack.push(new CssProperty(tagName));
				//Log.d("push",":"+tagName);
			}

	}
	
	/**
	 * 將CssProperty push到stack裡，並賦予當前處理的HtmlSpan該CssProperty屬性
	 * @param tagName element tag name
	 * @param atts element attributes
	 */
	public void pushAndAddCssProperty(String tagName,Attributes atts){
		pushCssProperty(tagName,atts,true);
		if(!stack.empty()){
			span.setCssProperty(stack.peek());
		}
		
	}
	
	/**
	 * 將最上層的CssProperty賦予到當前處理的HtmlSpan
	 */
	public void addCssProperty(){
		if(!stack.empty())
			span.setCssProperty(stack.peek());
	}
	
	/**
	 * 略過非block層級的property，將最上層的block層級的CssProperty賦予到當前處理的HtmlSpan
	 */
	public void addBlockLevelCssProperty(){
		if(!stack.empty()){
			for(int i=stack.size()-1;i>=0;i--){
				if(NonBlockElement.valueOf(stack.get(i).getTagName())==null){//is block element
					span.setCssProperty(stack.get(i));
				}
			}
		}
	}
	
	/**
	 * 若傳入參數tagName與stack最上層的物件裡的tag name相符，pop出(移除)stack最上層物件。用途:處理巢狀結構，在end element時呼叫此method將屬性移除，回到上一階層的屬性
	 * @param tagName tag name
	 */
	public void removeCssProperty(String tagName){
		if(!stack.empty() ){
			//Log.e("css remove","peek:"+stack.peek().getTagName());
			if(stack.peek().getTagName().equals(tagName)){
				//Log.d("start remove","tag:"+stack.peek().getTagName());
				stack.pop();
				//if(!stack.empty())
				//Log.d("AfterRemoveRemoveProperty","tag:"+stack.peek().getTagName());
			}
		}
	}
	
	/**
	 * 取得parse完後的HtmlSpan list
	 * @return HtmlSpan list
	 */
	public  ArrayList<HtmlSpan> getContent(){
		return content;
	}
	
	/**
	 * 讀取header的map，加速處理
	 */
	private static void loadHeaderMap(){
		if(headerMap==null){
			headerMap = new HashMap<String, Integer>();
			for(int i=0;i<headers.length;i++){
				headerMap.put(headers[i], i+1);
			}
		}
	}
	
	/**
	 * 取得header size
	 * @param h header string ex:h1, h2,...
	 * @return header size
	 */
	private int getHeaderSize(String h){
		loadHeaderMap();
		return headerMap.get(h);
	}
	
	String imgPath="";
	
	public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		//Log.d("Tag:",":"+localName);
		isEndTag=false;
		if(isCallBack && threadIdx!=hsr.getThreadIdx()){
			shouldStop=true;
		}else if(!in_pre){
			try {
				switch(TagList.valueOf(localName)){
				case style:
					in_style=true;
					styleBuilder = new StringBuilder();
					break;
				case title:
					in_title=true;
					break;
				case br:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan(span);
						addBlockLevelCssProperty();
					}else{
						brCount++;
					}
					break;
				case a:
					curHref = atts.getValue("", "href");
					if(curHref!=null)
						curHref = "";
					in_a=true;
					break;
				case body:
					span = new HtmlSpan();
					pushAndAddCssProperty(localName,atts);
					in_body=true;
					break;
				case table:
					in_table=true;	
					break;
				case img:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}else{
						span.clear();
					}
					try {
						AttributesImpl imgAtts = new AttributesImpl(atts);
						int styleAttIdx = imgAtts.getIndex("", "style");
						StringBuilder imgStyle = new StringBuilder();
						if(styleAttIdx>=0)
							imgStyle.append(imgAtts.getValue(styleAttIdx));
						if (atts.getValue("", "width") != null) {
							imgStyle.append("width:").append(
									atts.getValue("", "width")).append(";");
						}
						if (atts.getValue("", "height") != null) {
							imgStyle.append("height:").append(
									atts.getValue("", "height")).append(";");
						}
						if (atts.getValue("", "border") != null) {
							imgStyle.append("border:").append(
									atts.getValue("", "border")).append("px;");
						}
						if(styleAttIdx>=0)
							imgAtts.removeAttribute(styleAttIdx);
						imgAtts.addAttribute("", "style", "", "", imgStyle
								.toString());
						pushAndAddCssProperty(localName, imgAtts);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						pushAndAddCssProperty(localName,atts);
					}
					//Log.d("ContentParseHandler","StarTag:Img");
					imgSrc = atts.getValue("", "src");
					//imgSrc = URLEncoder.encode(imgSrc);
					imgPath = AndroidLibrary.getAbsPathFromRelPath(docDirPath, imgSrc);
					//imgPath = URLEncoder.encode(imgPath);
					try {
						uz.unzipFile(imgPath,false);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e("HtmlParseHandler","img unzip error:"+imgPath);
						//e.printStackTrace();
					}
					//Log.d("after img","unzip");
					if(!isCallBack || threadIdx==hsr.getThreadIdx()){
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds=true;
						BitmapFactory.decodeFile(imgPath,options);
						span.type=HtmlSpan.TYPE_IMG;
						span.content.append(imgPath);
						span.height=options.outHeight;
						span.width=options.outWidth;
						if(span.height>0 && span.width>0)
							addSpan(span);
						else{
							String alt = atts.getValue("", "alt");
							if(alt!=null && alt.length()>0){
								span.content.setLength(0);
								span.content.trimToSize();
								span.content.append(alt);
								span.height=-1;
								span.width=-1;
								addSpan(span);
							}
						}
							
						//span = new HtmlSpan();
						//addCssProperty();
					}
					break;
				case center:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}
					AttributesImpl newAtts = new AttributesImpl(atts);
					newAtts.addAttribute("", "align", "", "", "center");
					pushAndAddCssProperty(localName,newAtts);
					//in_center=true;
					break;
				case pre:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}
					in_pre=true;
					break;
				case p:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}
					pushAndAddCssProperty(localName,atts);
					//span.type=HtmlSpan.TYPE_PAR;
					//in_p=true;
					break;
				case div:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}
					pushAndAddCssProperty(localName,atts);
					//span.type=HtmlSpan.TYPE_DIV;
					//in_div=true;
					break;
				case h1:
				case h2:
				case h3:
				case h4:
				case h5:
				case h6:
					headerSize=getHeaderSize(localName);
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}
					pushAndAddCssProperty(localName,atts);
					span.type=HtmlSpan.TYPE_HEADER;
					span.setHeaderSize(headerSize);
					in_header=true; 
					break;
				case link: 
					try {
						if(atts.getValue("", "type").equalsIgnoreCase("text/css")){
							String cssPath = AndroidLibrary.getAbsPathFromRelPath(docDirPath, atts.getValue("", "href"));
							//Log.d("cssPath","is:"+cssPath);
							uz.unzipFile(cssPath,false);
							cssSet = cssParser.cssParseFromFile("file://"+cssPath);
							isCssSetSet=true;
							
							/*
							Iterator<Entry<String, HashMap<String, String>>> it = cssSet.entrySet().iterator();
							while(it.hasNext()){
								Log.d("tagkey","is:["+it.next().getKey()+"]");
							}*/
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e("HtmlParseHandler","ParsingCssError");
					}
					break;
				case ul:
					if(span.content.length()>0){
						addSpan(span);
					}
					in_ul=true;
					listType=HtmlList.TYPE_DISC;
					String ulType;
					if((ulType=atts.getValue("", "type")) != null){
						if(ulType.equals("square")){
							listType=HtmlList.TYPE_SQUARE;
						}else if(ulType.equals("circle")){
							listType=HtmlList.TYPE_CIRCLE;
						}
					}
					pushCssProperty(localName,atts,true);
					liCounter=1;
					//in_ul=true;
					break;
				case ol:
					if(span.content.length()>0){
						addSpan(span);
					}
					in_ol=true;
					listType=HtmlList.TYPE_1;
					String olType,olStart;
					if((olType=atts.getValue("", "type")) != null && olType.length()>0){
						if(olType.charAt(0)=='a'){
							listType=HtmlList.TYPE_a;
						}else if(olType.charAt(0)=='A'){
							listType=HtmlList.TYPE_A;
						}
					}
					
					liCounter=1;
					if((olStart=atts.getValue("", "start"))!=null){
						try{
							liCounter = Integer.parseInt(olStart);
						}catch(Exception e){
							liCounter=1;
							e.printStackTrace();
						}
					}
					pushCssProperty(localName,atts,true);
					//in_ol=true;
					break;
				case li:
					span = new HtmlSpan();
					String liValueAtt,liTypeAtt;
					if((liValueAtt=atts.getValue("","value"))!=null){
						try{
							liCounter = Integer.parseInt(liValueAtt);
						}catch(Exception e){
							
						}
					}
					int tempType=-1;
					if((liTypeAtt=atts.getValue("","type"))!=null){
						try{
							if(in_ol){
								if(liTypeAtt.length()>0){
									if(liTypeAtt.charAt(0)=='a'){
										tempType=HtmlList.TYPE_a;
									}else if(liTypeAtt.charAt(0)=='A'){
										tempType=HtmlList.TYPE_A;
									}
								}
							}else if(in_ul){
								if(liTypeAtt.equals("square")){
									tempType=HtmlList.TYPE_SQUARE;
								}else if(liTypeAtt.equals("circle")){
									tempType=HtmlList.TYPE_CIRCLE;
								}
							}
						}catch(Exception e){
							
						}
					}
					if(tempType>=0)
						HtmlList.addPrefix(span.content, tempType, liCounter++);
					else
						HtmlList.addPrefix(span.content, listType, liCounter++);
					/*if(in_ol){
	    				span.content.append(liCounter++).append(". ");
					}else{
	    				span.content.append("● ");
					}*/
					pushAndAddCssProperty(localName,atts);
					in_li=true;
					break;
				case image:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}else{
						span.clear();
					}
					imgSrc = atts.getValue("xlink:href");
					imgPath = AndroidLibrary.getAbsPathFromRelPath(docDirPath, imgSrc);
					try {
						uz.unzipFile(imgPath,false);
						if(!isCallBack || threadIdx==hsr.getThreadIdx()){
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inJustDecodeBounds=true;
							BitmapFactory.decodeFile(imgPath,options);
							span.type=HtmlSpan.TYPE_IMG;
							span.content.append(imgPath);
							span.height=options.outHeight;
							span.width=options.outWidth;
							if(span.height>0 && span.width>0)
								addSpan(span);
								
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e("HtmlParseHandler","svg:image unzip error:"+imgPath);
						//e.printStackTrace();
					}finally{
						span = new HtmlSpan();
						addCssProperty();
					}
					break;
				case span:
					pushCssProperty(localName,atts,false);
					specialPropertyDepth++;
					break;
				case sup:
					pushCssProperty(localName,atts,false);
					specialPropertyDepth++;
					in_sup=true;
					break;
				case sub:
					pushCssProperty(localName,atts,false);
					specialPropertyDepth++;
					in_sub=true;
					break;
				case font:
					try{
						AttributesImpl fontAtts = new AttributesImpl(atts);
						int styleAttIdx = fontAtts.getIndex("", "style");
						StringBuilder fontStyle = new StringBuilder();;
						if(styleAttIdx>=0)
							fontStyle.append(fontAtts.getValue(styleAttIdx));
	
						String fontSize = atts.getValue("", "size");
						String fontColor = atts.getValue("", "color");
							if(fontSize!=null){
								fontStyle.append("font-size:").append(100+(Integer.parseInt(fontSize)-3)*10).append("%;");
							}
							if(fontColor!=null){
								fontStyle.append("color:").append(fontColor).append(";");
							}
						if(styleAttIdx>=0)	
							fontAtts.removeAttribute(styleAttIdx);
						fontAtts.addAttribute("", "style", "", "", fontStyle.toString());
						pushCssProperty(localName,fontAtts,false);
					}catch(Exception e){
						Log.e("HtmlParseHandler:startTagFont",e.toString());
						pushCssProperty(localName,atts,false);
					}
					specialPropertyDepth++;
					break;
				case strong:
				case cite:
				case code:
				case dfn:
				case kbd:
				case samp:
				case var:
				case b:
					try{
						AttributesImpl strongAtts = new AttributesImpl(atts);
						int styleAttIdx = strongAtts.getIndex("","style");
						StringBuilder fontStyle = new StringBuilder();
						if(styleAttIdx>=0)
							fontStyle.append(strongAtts.getValue(styleAttIdx));
						fontStyle.append("font-weight:").append("bold;");
						if(styleAttIdx>=0)
							strongAtts.removeAttribute(styleAttIdx);
						strongAtts.addAttribute("", "style", "", "", fontStyle.toString());
						pushCssProperty(localName,strongAtts,false);
					}catch(Exception e){
						Log.e("HtmlParseHandler:startTagB",e.toString());
						pushCssProperty(localName,atts,false);
					}
					specialPropertyDepth++;
					break;
				case em:
				case i:
					try{
						AttributesImpl iAtts = new AttributesImpl(atts);
						int styleAttIdx = iAtts.getIndex("","style");
						StringBuilder fontStyle = new StringBuilder();
						if(styleAttIdx>=0)
							fontStyle.append(iAtts.getValue(styleAttIdx));
						fontStyle.append("font-style:").append("italic;");
						if(styleAttIdx>=0)
							iAtts.removeAttribute(styleAttIdx);
						iAtts.addAttribute("", "style", "", "", fontStyle.toString());
						pushCssProperty(localName,iAtts,false);
					}catch(Exception e){
						pushCssProperty(localName,atts,false);
						Log.e("HtmlParseHandler:startTagI",e.toString());
					}
					specialPropertyDepth++;
					break;
				case u:
				case ins:
				case address:
					pushCssProperty(localName,atts,false);
					specialPropertyDepth++;
					in_u=true;
					break;
				case del:
				case strike:
				case s:
					pushCssProperty(localName,atts,false);
					in_strike=true;
					specialPropertyDepth++;
					break;
				case hr:
					if(span.content.length()>0){
						addSpan(span);
						span = new HtmlSpan();
					}else{
						span.clear();
					}
					span.type=HtmlSpan.TYPE_IMG;
					span.subtype=HtmlSpan.SUBTYPE_HR;
					span.height=span.width=0;
					
					AttributesImpl iAtts = new AttributesImpl(atts);
					try{
						String hrAlign = atts.getValue("", "align");
						if(hrAlign==null)
							iAtts.addAttribute("", "align", "", "", "center");
						int styleAttIdx = iAtts.getIndex("","style");
						StringBuilder widthStyle = new StringBuilder();
						if(styleAttIdx>=0)
							widthStyle.append(iAtts.getValue(styleAttIdx));
						String hrWidth = atts.getValue("", "width");
						if(hrWidth!=null)
							widthStyle.append("iii_hr_width:").append(hrWidth).append(";");
						if(styleAttIdx>=0)
							iAtts.removeAttribute(styleAttIdx);
						iAtts.addAttribute("", "style", "", "", widthStyle.toString());
						if(atts.getValue("","size")!=null){
							span.content.append(atts.getValue("","size"));
						}else{
							span.content.append(RendererConfig.DEFAULT_HR_TOTAL_SIZE_STR);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					pushAndAddCssProperty(localName,iAtts);
					addSpan(span);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	} 
	
	private boolean isEndTag=false;
	public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
		//Log.d("EndTag:",localName);
		isEndTag=true;
		if(shouldStop){
    		
    	}else if(in_pre && !localName.equalsIgnoreCase("pre")){
    		
    	}else{
    		try {
    			switch(TagList.valueOf(localName)){
    			case style:
    				if(styleBuilder!=null && styleBuilder.length()>0)
    					handleCssSet(cssParser.cssParseFromContent(styleBuilder.toString()));
    				in_style=false;
    				break;
    			case title:
    				in_title=false;
    				break;
    			case a:
    				in_a=false;
    				break;
    			case body:
    				in_body=false;
    				removeCssProperty(localName);
    				if(span.content.length()>0){
    					addSpan(span);
    				}
    				break;
    			case table:
    				in_table=false;
    				break;
    			case pre:
    				in_pre=false;
    				if(span.content.length()>0){
    					addSpan(span);
    					span=new HtmlSpan();
    				}else{
    					span.clear();
    				}
    				break;
    			case p:
    				//in_p=false;
    				removeCssProperty(localName);
    				if(span.content.length()>0){
    					addSpan(span);
    					span=new HtmlSpan();
    				}else{
    					span.clear();
    				}
    				if(content.size()>0 && content.get(content.size()-1).type!=HtmlSpan.TYPE_IMG){
    					content.get(content.size()-1).type=HtmlSpan.TYPE_PAR;
    				}
    				addCssProperty();
    				break;
    			case center:
    				//in_center=false;
    				removeCssProperty(localName);
    				if(span.content.length()>0){
    					addSpan(span);
    					span=new HtmlSpan();
    				}else{
    					span.clear();
    				}
    				addCssProperty();
    				break;
    			case img:
    				removeCssProperty(localName);
    				span=new HtmlSpan();
    				addCssProperty();
    				break;
    			case div:
    				//in_div=false;
    				removeCssProperty(localName);
    				if(span.content.length()>0){
    					addSpan(span);
    					span=new HtmlSpan();
    				}else{
    					span.clear();
    				}
    				addCssProperty();
    				break;
    			case h1:
    			case h2:
    			case h3:
    			case h4:
    			case h5:
    			case h6:
    				in_header=false;
    				removeCssProperty(localName);
    				if(span.content.length()>0){
    					addSpan(span);
    					span=new HtmlSpan();
    				}else{
    					span.clear();
    				}
    				addCssProperty();
    				break;
    			case ul:
    				in_ul=false;
    				removeCssProperty(localName);
    				span = new HtmlSpan();
    				addCssProperty();
    				//in_ul=false;
    				break;
    			case ol:
    				in_ol=false;
    				removeCssProperty(localName);
    				span = new HtmlSpan();
    				addCssProperty();
    				//in_ol=false;
    				break;
    			case li:
    				if(span.content.length()>0){
    					addSpan(span);
    				}
    				removeCssProperty(localName);
    				in_li=false;
    				break;
    			case span:
    				specialPropertyDepth--;
    				removeCssProperty(localName);
    				break;
    			case sup:
    				specialPropertyDepth--;
    				in_sup=false;
    				removeCssProperty(localName);
    				break;
    			case sub:
    				specialPropertyDepth--;
    				in_sub=false;
    				removeCssProperty(localName);
    				break;
    			case font:
    				removeCssProperty(localName);
    				specialPropertyDepth--;
    				break;
				case strong:
				case cite:
				case code:
				case dfn:
				case kbd:
				case samp:
				case var:
				case b:
    				removeCssProperty(localName);
    				specialPropertyDepth--;
    				break;
				case em:
    			case i:
    				removeCssProperty(localName);
    				specialPropertyDepth--;
    				break;
				case u:
				case ins:
				case address:
    				removeCssProperty(localName);
    				in_u=false;
    				specialPropertyDepth--;
    				break;
				case del:
				case strike:
				case s:
    				removeCssProperty(localName);
    				in_strike=false;
    				specialPropertyDepth--;
    				break;
    			case hr:
    				removeCssProperty(localName);
    				span = new HtmlSpan();
    				addCssProperty();
    				break;
    			default:
    				break;
    			}
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    		}
    	}
		
	}    
	
	public void endDocument(){
		onParsingFinished();
		/*StringBuilder b = new StringBuilder();
		for(int i=0;i<content.size();i++){
			b.append(content.get(i).content).append("\n");
		}
		AndroidLibrary.writeString(b.toString(), "/sdcard/teststr.txt");*/
	}
	
	/**
	 * css parser處理完後得到css對照表，此method將這些對照表進一步處理合併
	 * @param tempSet css set
	 */
	private void handleCssSet(HashMap<String,HashMap<String,String>> tempSet){
		/*
		Iterator<Entry<String, HashMap<String, String>>> it = cssSet.entrySet().iterator();
		while(it.hasNext()){
			Log.d("tagkey","is:["+it.next().getKey()+"]");
		}*/
		if(!isCssSetSet || cssSet==null){
			cssSet=tempSet;
			isCssSetSet=true;
		}else if(tempSet!=null){
			Iterator<Entry<String, HashMap<String, String>>> it = cssSet.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, HashMap<String, String>> e= it.next();
				tempSet.put(e.getKey(), e.getValue());
			}
			cssSet=tempSet;
		}
	}

    public void characters(char ch[], int start, int length) {
    	//Log.d("Text:",new String(ch, start, length));
    	if(shouldStop){
    		
    	}else if(isEndTag && length==1 && ch[start]==10){
    		//Log.d("parser in","in");
    	}if(!in_body){
    		if(in_style){
        		styleBuilder.append(ch, start, length);
        	}else if(in_title && isCallBack){
    			hsr.onGetTitle(new String(ch, start, length));
    		}
    	}else if(in_pre){
    		StringBuilder builder = new StringBuilder();
        	builder.append(ch, start, length);
        	for(int i=0;i<builder.length();i++){
        		//if(Character.isWhitespace(builder.charAt(i))){
        			//Log.e("JP","int:"+((int)builder.charAt(i)));
        		//}
        		if(builder.charAt(i)=='	'){
        			builder.replace(i,i+1,RendererConfig.TAB_WHITESPACE);
        			i+=RendererConfig.TAB_WHITESPACE.length()-1;
        		}
        	}
        	span.content.append(builder);     	
        	if(span.content.length()>1 && span.content.charAt(span.content.length()-1)==10){
        		span.content.setLength(span.content.length()-1);
        		span.content.trimToSize();
        		CssProperty cp = new CssProperty("pre");
        		cp.setAlign(CssProperty.ALIGN_LEFT);
        		span.setCssProperty(cp);
				addSpan(span);
				span=new HtmlSpan();
			}else if(span.content.length()==1 && span.content.charAt(span.content.length()-1)==10){
				brCount++;
				span.content.setLength(0);
				span.content.trimToSize();
			}
    	}else{
        	StringBuilder builder = new StringBuilder();
        	builder.append(ch, start, length);
        	for(int i=0,j;i<builder.length();i++){
        		if(Character.isWhitespace(builder.charAt(i))){
        			for(j=i+1;j<builder.length();j++){
        				if(!Character.isWhitespace(builder.charAt(j)))
        					break;
        			}
        			builder.replace(i, j, " ");
        			i=i+1;
        		}
        	}
        	if(builder.length()==1 && Character.isWhitespace(builder.charAt(0))){ 
        		StringBuilder c = span.content;
        		if(c.length()==0 || Character.isWhitespace(c.charAt(c.length()-1))){ 
        			builder.setLength(0);
        			builder.trimToSize();
        		}
        	}
        	if(builder.length()>0){
        		if(in_table){
        			//表格處理，不改變資料
        		}else if(in_a){
        			//content.add("<a href="+curHref+">"+s.substring(i, i+1)+"</a>");
        			span.addLink(builder.toString(), curHref);
        			//content.add("<a href="+curHref+">"+chi+"</a>");
        		}else if(specialPropertyDepth>0){
        			int type=SpecialProperty.DEFAULT;
        			if(in_u){
        				type=SpecialProperty.U;
        			}else if(in_strike){	
        				type=SpecialProperty.STRIKE;
        			}else if(in_sub){
        				type=SpecialProperty.SUB;
        			}else if(in_sup){
        				type=SpecialProperty.SUP;
        			}
        			if(!stack.empty()){
    					span.addProperty(stack.peek(),builder.toString(), type);
    				}else{
    					span.content.append(builder.toString());
    				}
        		}/*else if(in_font){
        			content.add("<font color="+fontColor+">"+chi+"</font>");
        		}*/else if(in_header){
        			//headerSize
        			span.content.append(builder);
        		}else if(in_li){ 
        			//Log.d("span content:"+span.content,"length:"+span.content.length());
        			/*if(in_ul && span.content.length()==0){
        				span.content.append("● ");
        			}else if(in_ol && span.content.length()==0){
        				span.content.append(liCounter).append(". ");
        			}*/
        			span.content.append(builder);   
        		}else{
        			span.content.append(builder);   
        		}
        	}
    	}
    	isEndTag=false;
    }
    
    private boolean haveReturned=false;
    /**
     * 將剛處理完的HtmlSpan加入列表(content variable)裡。若為call back，則將此HtmlSpan回傳給html receiver
     * @param span
     */
    public void addSpan(HtmlSpan span){
    	if(brCount>0){
    		span.brCount=brCount;
    		brCount=0;
    	}
    	if(isCallBack && threadIdx==hsr.getThreadIdx()){
    		haveReturned=true;
    		content.add(span);
    		hsr.onGetHtmlSpan(spanCounter++, span,threadIdx);
    	}else{
    		content.add(span);
    	}
    }
    
    /**
     * 當parse完後呼叫receiver告知已處理完畢
     */
    public void onParsingFinished(){
    	if(isCallBack){
    		if(!haveReturned){
    			HtmlSpan localSpan = new HtmlSpan();
    			localSpan.type=HtmlSpan.TYPE_IMG;
    			localSpan.subtype=HtmlSpan.SUBTYPE_HR;
    			hsr.onGetHtmlSpan(spanCounter++, localSpan,threadIdx);
    		}
			hsr.onParsingFinished(threadIdx);
    	}
    }
    
    

}
