package org.iii.ideas.reader.last_page;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * parse server端的最後閱讀頁xml，並組成Lastpage物件，供LastPageHelper比對、更新
 * @author III
 * 
 */
public class LastPageReader {
	
	/**
	 * parse最後閱讀頁xml並回傳LastPage物件
	 * @param path xml path
	 * @return LastPage物件
	 */
	public static LastPage getLastPage(String path){
		//處理notes xml
		try{   
			//File test = new File(path);
			SAXParserFactory spf = SAXParserFactory.newInstance();
	        SAXParser sp = spf.newSAXParser(); 
	        XMLReader xr = sp.getXMLReader(); 
	        LastPageParseHandler handler = new LastPageParseHandler();
	        xr.setContentHandler(handler);
	        xr.parse("file://"+path);
	        return handler.getLastPage();
		}catch(Exception e){
			Log.e("LastPageReader_E",""+e);
			return null;
		}	
	}
}	

/**
 * last page sax parse handler
 * @author III
 *
 */
class LastPageParseHandler extends DefaultHandler{
	//notes xml的parser
	//private boolean in_ebook;
	private boolean in_read_at;
	private boolean in_chapter;
	private boolean in_span;
	private boolean in_idx;
	private boolean in_error=false;
	private boolean in_Delivery_ID;
	private String dataString;
	LastPage lastPage;
	public LastPageParseHandler(){
		super();
		lastPage = new LastPage();
	}
	
	public LastPage getLastPage(){
		return lastPage;
	}
	
	public void startDocument() throws SAXException {		
		// do nothing
   }
	public void endDocument() throws SAXException {
        // do nothing
   }  	
			
	public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		dataString="";
		//Log.d("StartEle",localName);
		if (localName.equals("ebook")) {
			//in_ebook=true;
		}else if (localName.equals("read_at")) {
			in_read_at=true;
		}else if (localName.equals("span")) {
			in_span=true;
		}else if (localName.equals("chapter")) {
			in_chapter=true;
		}else if (localName.equals("idx")) {
			in_idx=true;
		}else if (localName.equals("Delivery-ID")) {
			in_Delivery_ID=true;
		}else if (localName.equals("error")) {
			in_error=true;
		}
	}
	
	public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
		//Log.d("endEle",localName+":"+dataString);
		if (localName.equals("ebook")) {
			//in_ebook=false;
		}else if (localName.equals("read_at")) {
			if(dataString.length()>0)
				lastPage.read_at=dataString.trim();
			in_read_at=false;
		}else if (localName.equals("span")) {
			if(dataString.length()>0)
				lastPage.span=parseInteger();
			in_span=false;
		}else if (localName.equals("idx")) {
			if(dataString.length()>0)
				lastPage.idx=parseInteger();
			in_idx=false;
		}else if (localName.equals("chapter")) {
			if(dataString.length()>0)
				lastPage.chapName=dataString;
			in_chapter=false;
		}else if (localName.equals("Delivery-ID")) {
			if(dataString.length()>0)
				lastPage.deliverId=dataString;
			in_Delivery_ID=false;
		}else if (localName.equals("error")) {
			lastPage.isSuccessful=false;
			if(dataString!=null){
				//Log.d("lastPageError:",":"+dataString+"]");
				dataString=dataString.trim();
				if(dataString.length()==1 && dataString.charAt(0)=='0'){
					lastPage.isSuccessful=true;
				}
			}
			in_error=false;
		}
	}
	
	private int parseInteger(){
		try{
			return Integer.parseInt(dataString);
		}catch(Exception e){
			Log.e("lastPageParsehandler",e.toString());
		}
		return -1;
	}
	
	public void characters(char ch[], int start, int length) {
		//Log.d("NotesParser",new String(ch, start, length));
        if(in_error){
        	dataString+=new String(ch, start, length);
        }else if(in_chapter){
        	dataString+=new String(ch, start, length);
        }else if(in_span){ 
        	dataString+=new String(ch, start, length);
        }else if(in_idx){
        	dataString+=new String(ch, start, length);
        }else if(in_read_at){
        	dataString+=new String(ch, start, length);
        }else if(in_Delivery_ID){
        	dataString+=new String(ch, start, length);
        }
	}
}
