package org.iii.ideas.reader.ncx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * parse ncx文件將ncx文件轉成相對應的tree時的資料結構。ncx檔允許nested標籤，故用tree來描述
 * @author III
 * 
 */
public class NCXTree {
	private ArrayList<NCXNode> tree; 
	private int curNode;
	
	public NCXTree(){
		tree=new ArrayList<NCXNode>();
	}
	
	/**
	 * 插入新node
	 */
	public void insert(){
		tree.add(new NCXNode(curNode));
		curNode=tree.size()-1;
	}
	
	/**
	 * 更改目前處理的node的值
	 * @param key id
	 * @param value value
	 */
	public void edit(String key, String value){
		if(key.equals("t")){ //text
			NCXNode newNode= tree.get(curNode);
			newNode.setText(value);
			tree.set(curNode,newNode);
		}else if(key.equals("h")){ //text
			NCXNode newNode= tree.get(curNode);
			newNode.setHref(value);
			tree.set(curNode,newNode);
		}
	}
	
	/**
	 * 移到當前node的parent
	 */
	public void up(){
		curNode=tree.get(curNode).getParent();
	}
	
	/**
	 * 取得ncx tree
	 * @return ncx tree
	 */
	public ArrayList<NCXNode> getTree(){
		return tree;
	}
	
	/**
	 * 將tree轉為array list
	 * @return toc list
	 */
	public ArrayList<Map<String,String>> toList(){
		ArrayList<Map<String,String>> tocList = new ArrayList<Map<String,String>>();
		for(int i=0;i<tree.size();i++){
			Map<String,String> item = new HashMap<String,String> ();
			item.put("text", tree.get(i).getText());
			item.put("href", tree.get(i).getHref());
			tocList.add(item);
		}
		return tocList;
	}
}
