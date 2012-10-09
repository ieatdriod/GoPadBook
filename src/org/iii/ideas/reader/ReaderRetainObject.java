package org.iii.ideas.reader;

import java.util.ArrayList;
import java.util.Map;

import org.iii.ideas.reader.opf.OPFDataSet;
import org.iii.ideas.reader.parser.HtmlReceiver;

/**
 * 為了螢幕旋轉加速的暫存class。將與呈現分頁"無關"但和解壓縮parse有關的相關資料保存於此，當screen orientation改變時此物件保存的資料無須重新處理，可跳過重新解壓縮, 解密, 和parse，直接進入分頁呈現階段。
 * @author III
 * 
 */
public class ReaderRetainObject {
	/**
	 * HtmlSpan receiver
	 */
	public HtmlReceiver receiver;
	
	/**
	 * unzipper
	 */
	public PartialUnzipper uz;
	
	/**
	 * directory of opf file; served as base dir
	 */
	public String targetDir;
	
	/**
	 * ncx path
	 */
	public String ncxPath;
	
	/**
	 * opf path
	 */
	public String opfPath;
	
	/**
	 * 章節檔案列表
	 */
	public ArrayList<String> spineList;
	
	/**
	 * 書名
	 */
	public String bookTitle;
	
	/**
	 * opf metadata
	 */
	public OPFDataSet dataset;
	
	/**
	 * 目前章節檔案名稱
	 */
	public String curSecFilename;
	
	/**
	 * 當前章節編號
	 */
	public int curSecNo;
	/**
	 * 目前所在span，以便當螢幕旋轉後仍能找到定位
	 */
	public int renderStartSpan;
	/**
	 * 目前所在index，以便當螢幕旋轉後仍能找到定位
	 */
	public int renderStartIdx;
	
	/**
	 * 是否為末頁
	 */
	public boolean isLastPage=false;
	
	/**
	 * table of content
	 */
	public ArrayList<Map<String,String>> tocList=null;
	
	/**
	 * 是否重新render
	 */
	public boolean isCallRenderOnRetain=false;
	
	public ReaderRetainObject(){

	}
}
