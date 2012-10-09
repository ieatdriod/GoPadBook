package org.iii.ideas.reader.opf;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

import android.util.Log;

/**
 * parse opf xml的sax reader
 * @author III
 * 
 */
public class OPFReader {
	private OPFDataSet dataset;
	private ArrayList<String> spineList;
	
	/**
	 * 初始化並開始parse
	 * @param uri opf file uri
	 */
	public OPFReader(String uri){
		try{
			//Log.d("OPFpath",absPath);
			//不用tagsoup:以下三行
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser(); 
			XMLReader xr = sp.getXMLReader();
			//用tagsoup:
			//XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
			
			//分隔線
			OPFParseHandler handler = new OPFParseHandler();
			xr.setContentHandler(handler);
			xr.parse(uri);

			dataset = handler.getDataSet();
			constructSpine();
		}catch(Exception e){
			Log.d("OPFReader","Tagsoup");
			try {
				//Log.e("OPFReader",e.toString());
				//Log.e("OPFReader","path:"+absPath);
				XMLReader xr = new org.ccil.cowan.tagsoup.Parser();
				OPFParseHandler handler = new OPFParseHandler();
				xr.setContentHandler(handler);
				xr.parse(uri);
				dataset = handler.getDataSet();
				constructSpine();
			} catch (Exception e2) {
				// TODO: handle exception
				Log.e("OPFReader",e.toString());
				Log.e("OPFReader","path:"+uri);
			}
		}
	}
	
	/**
	 * 組織opf文件的spine list
	 */
	public void constructSpine(){
		//將spine裡的id轉為實際的location
		spineList = new ArrayList<String>();
		ArrayList<String> origSpine = dataset.getSpine();
		Map<String,String> Items = dataset.getItems();
		for(int i=0;i<origSpine.size();i++){
			spineList.add(Items.get(origSpine.get(i)));
		}
		
	}
	
	/**
	 * 取得parse完的metadata
	 * @return opf data set
	 */
	public OPFDataSet getDataSet(){ 
		return this.dataset;
	}	
	
	/**
	 * 取得spine list
	 * @return spine list
	 */
	public ArrayList<String> getSpineList(){
		return spineList;
	}

}
