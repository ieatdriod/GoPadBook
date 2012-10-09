package org.iii.ideas.reader.ncx;

import java.util.ArrayList;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse ncx文件的sax handler
 * @author III
 * 
 */
public class NCXParseHandler extends DefaultHandler{
	//ncx檔parser，負責生成toc之array list
	private boolean in_navPoint;
	private boolean in_navLabel;
	private boolean in_text;
	private NCXTree tocTree;
	private String curText;
	private String curHref;
	
	public void startDocument() throws SAXException {
		tocTree = new NCXTree();
   }
	public void endDocument() throws SAXException {
        // do nothing
   }  	
	
	/**
	 * 將ncx tree轉為array list並回傳
	 * @return ncx list
	 */
	public ArrayList<Map<String,String>> getTocList(){
		return tocTree.toList();
	}
			
	public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		if (localName.equals("navPoint")) {
			in_navPoint=true;
			tocTree.insert();
		}else if (localName.equals("navLabel")) {
			in_navLabel=true;
		}else if (localName.equals("content")) {
			curHref=atts.getValue("src");
			if(curHref.lastIndexOf("#")>0){
				curHref=curHref.substring(0,curHref.lastIndexOf("#"));				
			}
			tocTree.edit("h", curHref);
		}else if (localName.equals("text")) {
			in_text=true;
			curText="";
		}
	}
	
	public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
		//Log.d("OPFdebug",localName);
		if (localName.equals("navPoint")) {
			in_navPoint=false;
			tocTree.up();

		}else if (localName.equals("navLabel")) {
			in_navLabel=false;
			if(in_navPoint)
				tocTree.edit("t", curText);
		}else if (localName.equals("text")) {
			in_text=false;
		}
	}
	
	public void characters(char ch[], int start, int length) {
        if(in_text && in_navLabel){
        	curText+=new String(ch, start, length);
        }
	}
}