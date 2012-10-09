package org.iii.ideas.reader.opf;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * parse opf檔後存成的結構，包含書籍metadata和spine list
 * @author III
 * 
 */
public class OPFDataSet {
	//
	private String title;	
	private ArrayList<String> creator = new ArrayList<String>();
	private String date;
	private String subject;
	private String language;
	private String coverage;
	private String rights;
	private String publisher;
	private String identifier;
	private String description;
	private String type;
	private String format;
	private String source;
	private String relation;
	private HashMap<String, String> items = new HashMap<String, String>();
	private ArrayList<String> spine = new ArrayList<String>();
	
	/**
	 * 設定title
	 * @param t title
	 */
	public void setTitle(String t){
		title=t;
	}
	/**
	 * 設定作者
	 * @param c 作者
	 */
	public void addCreator(String c){ 
		//加入作者，可能不只一位
		creator.add(c);
	}
	
	/**
	 * 設定日期
	 * @param d date
	 */
	public void setDate(String d){
		date=d;
	}
	
	/**
	 * 設定主題
	 * @param s subject
	 */
	public void setSubject(String s){
		subject=s;
	}
	/**
	 * 設定語言
	 * @param language
	 */
	public void setLanguage(String l){
		language=l;
	}
	/**
	 * 設定範圍
	 * @param c coverage
	 */
	public void setCoverage(String c){
		coverage=c;
	}
	/**
	 * 設定版權
	 * @param r rights
	 */
	public void setRights(String r){
		rights=r;
	}
	/**
	 * 設定出版社
	 * @param p publisher
	 */
	public void setPublisher(String p){
		publisher=p;
	}
	/**
	 * 設定id
	 * @param i id
	 */
	public void setIdentifier(String i){
		identifier=i;
	}
	
	/**
	 * 設定書籍描述
	 * @param d description
	 */
	public void setDescription(String d){
		description=d;
	}
	/**
	 * 設定書籍類型
	 * @param t
	 */
	public void setType(String t){
		type=t;
	}
	/**
	 * 設定書籍格式
	 * @param f format
	 */
	public void setFormat(String f){
		format=f;
	}
	/**
	 * 設定來源
	 * @param s source
	 */
	public void setSource(String s){
		source=s;
	}
	/**
	 * 設定關聯
	 * @param r relation
	 */
	public void setRelation(String r){
		relation=r;
	}
	
	/**
	 * 設定作者
	 * @param c 作者列表
	 */
	public void setCreator(ArrayList<String> c){
		creator=c;
	}
	
	/**
	 * 設定items(opf manifest的item表)
	 * @param i
	 */
	public void setItems(HashMap<String,String> i){
		items=i;
	}
	
	/**
	 * 新增item到對照表
	 * @param k id
	 * @param v value
	 */
	public void addItems(String k, String v){
		//將manifest裡id和value加入item的array list
		items.put(k,v);
	}
	
	/**
	 * 將章節加到spine裡
	 * @param i 該章節相對路徑
	 */
	public void addSpine(String i){
		//將章節加入spine list
		spine.add(i);
	}
	/**
	 * 書名
	 * @return title
	 */
	public String getTitle(){
		return title;
	}
	/**
	 * 取得作者
	 * @return 作者列表
	 */
	public ArrayList<String> getCreator(){
		return creator;
	}
	
	/**
	 * 取得日期
	 * @return 日期 
	 */
	public String getDate(){
		return date;
	}
	
	/**
	 * 取得主題
	 * @return subject
	 */
	public String getSubject(){
		return subject;
	}
	
	/**
	 * 取得語言
	 * @return 語言
	 */
	public String getLanguage(){
		return language;
	}
	/**
	 * 取得涵蓋範圍
	 * @return coverage
	 */
	public String getCoverage(){
		return coverage;
	}
	
	/**
	 * 取得版權宣告
	 * @return rights
	 */
	public String getRights(){
		return rights;
	}
	
	/**
	 * 取得出版社
	 * @return publisher
	 */
	public String getPublisher(){
		return publisher;
	}
	/**
	 * 取得id
	 * @return id
	 */
	public String getIdentifier(){
		return identifier;
	}
	/**
	 * 取得描述
	 * @return description
	 */
	public String getDescription(){
		return description;
	}
	/**
	 * 取得類型
	 * @return 類型
	 */
	public String getType(){
		return type;
	}
	/**
	 * 取得格式
	 * @return format
	 */
	public String getFormat(){
		return format;
	}
	
	/**
	 * 取得來源
	 * @return source
	 */
	public String getSource(){
		return source;
	}
	
	/**
	 * 取得關聯
	 * @return relation
	 */
	public String getRelation(){
		return relation;
	}
	
	/**
	 * 取得manifest對照表
	 * @return manifest對照表
	 */
	public HashMap<String,String> getItems(){
		return items;
	}
	
	/**
	 * 取得spine list
	 * @return spine list
	 */
	public ArrayList<String> getSpine(){
		return spine;
	}
}
