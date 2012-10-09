package org.iii.ader;

//import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.htmlparser.Parser;

import android.util.Log;

/**
 * css parser，處理css文件parsing。
 * @author III
 * 
 */
public class cssParser {
	public static void main(String[] args) throws Exception {
		//String tt = "{abc}db";
		//String[] dd = tt.split("}");
		//String TEST_file = "file:///D:/ex/css/main.css";
    	//String cssContent ="";
    	//cssParser pb = new cssParser();
    	
    	long t1 = System.currentTimeMillis();
		//Hashtable<String,Hashtable<String,String>> cssSet1 = pb.cssParseFromContent(temp);
		long t2 = System.currentTimeMillis();
		System.out.println("time consumed:"+(t2-t1));
		//System.out.println("css from content:"+cssSet1);
		//System.out.println("test1:"+cssSet1.get("h1").get("font-size"));

		t1 = System.currentTimeMillis();
		//HashMap<String,HashMap<String,String>> cssSet2 = pb.cssParseFromFile(TEST_file);
		t2 = System.currentTimeMillis();
		//System.out.println("time consumed:"+(t2-t1));
		//System.out.println("css from file:"+cssSet2);
		//System.out.println("test2:"+cssSet2.get(".author").get("font-style"));
	}

	
	/**
	 * parse inline的css(style attribute)，回傳為attribute name -> value 的 map
	 * @param style style string
	 * @return css map
	 */
	public HashMap<String,String> cssParseFromInline(String style){
		try{
			HashMap<String,String> table = new HashMap<String,String>();
	    	style = style.replaceAll("\\s", "");
	    	String key="",value="";
	    	int i=0,start=0;;
	    	while(i<style.length()){
	    		if(style.charAt(i)==';'){
	    			value=style.substring(start, i);
	    			table.put(key, value);
	    			start=i+1;
	    		}else if(i==style.length()-1){
	    			value=style.substring(start);
	    			table.put(key, value);
	    			//start=i+1;
	    		}else if(style.charAt(i)==':'){
	    			key=style.substring(start, i);
	    			start=i+1;
	    		}
	    		i++;
	    	}
	    	return table;
		}catch(Exception e){
			Log.e("cssParser:cssParseFromInline",e.toString());
			return null;
		}
    }
	
	/**
	 * parse css字串(處理寫在<head>裡的css屬性) 回傳值為 tag name -> (attribute name -> value)的map
	 * @param cssContent
	 * @return css屬性對照表
	 * @throws Exception parse exception
	 */
    public HashMap<String,HashMap<String,String>> cssParseFromContent(String cssContent) throws Exception{
    	Parser parser = new Parser();
    	CSSTagFilter css = new CSSTagFilter(cssContent);
    	parser.setResource(cssContent);
    	parser.parse(css);
        return css.cssSet;
    }
    /**
     * parse外部css文件。回傳值為 tag name -> (attribute name -> value)的map
     * @param filePath css檔案路徑
     * @return css屬性對照表
     * @throws Exception parse exception
     */
    public HashMap<String,HashMap<String,String>> cssParseFromFile(String filePath) throws Exception{
    	
    	String cssContent = this.getContent(filePath);
    	Parser parser = new Parser();
    	CSSTagFilter css = new CSSTagFilter(cssContent);
    	parser.setResource(cssContent);
    	parser.parse(css);
        return css.cssSet;
    }
    
    /**
     * 將一檔案的內容讀出成string
     * @param url file url
     * @return content string
     */
	private String loadURL(URL url) {
		String result = "";
		try {
			// Read all the text returned by the server
			// ?c??:BIG5
			// 2??:GB2312
			//BufferedReader in;
			/*
			 * long t1 = System.currentTimeMillis(); //ins = new
			 * BufferReader(new InputStreamReader(url.openStream())); in = new
			 * BufferedReader(new InputStreamReader(url.openStream(),encode));
			 * long t2 = System.currentTimeMillis();
			 * 
			 * String str; String txtData = ""; t1 = System.currentTimeMillis();
			 * while ((str = in.readLine()) != null) { txtData = txtData + str;
			 * } in.close();
			 */
			InputStreamReader ins = new InputStreamReader(url.openStream());
			StringBuffer cb = new StringBuffer();
			char[] bs = new char[1024 * 128];// ?p?G???s???O???D?A?i?H?????j?p?A?~??W?[?o??buffer???j?p
			//long t2 = System.currentTimeMillis();
			while (true) {
				int count = ins.read(bs);
				if (count == -1) {
					break;
				} else {
					cb.append(new String(bs, 0, count));
				}
			}
			ins.close();
			//long t1 = System.currentTimeMillis();

			result = cb.toString();

			// ????
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 取得檔案字串
	 * 
	 * @param pathFile
	 * @return 內容字串
	 */
	public String getContent(String pathFile) {
		String content = "";
		try {
			URL url = new URL(pathFile);
			// content = this.loadURL2(pathFile);
			content = this.loadURL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return content.replace("&nbsp;", "").replace("&gt;", "").replaceAll("\n","<LF>").replaceAll("\t", "<TAB>");
		return content;
	}
}
