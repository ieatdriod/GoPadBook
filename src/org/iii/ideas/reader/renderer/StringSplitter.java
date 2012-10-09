package org.iii.ideas.reader.renderer;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * 將字串以空白區隔分成一段段substring(word)，紀錄各個substring(word)的index。
 * @author III
 * 
 */
public class StringSplitter {
	ArrayList<String> words = new ArrayList<String>();
	HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
	
	/**
	 * 進行字串分割
	 * @param s string to split
	 */
	public void split(String s){
		int start=0;
		/*while((temp=s.indexOf(" ", start))>=0){
			if(temp>start){
				addWord(s.substring(start,temp),start);
				
			}
		}*/
		int i=0;
		while(true){
			while(i<s.length()&&Character.isWhitespace(s.charAt(i))){
				i++;
			}
			if(i>=s.length())
				break;
			else
				start=i;
			while(i<s.length()&& !Character.isWhitespace(s.charAt(i))){
				i++;
			}
		
			if(i>start){
				addWord(s.substring(start, i),start);
			}
				
		}
	}
	
	private void addWord(String s, int i){
		words.add(s);
		map.put(words.size()-1, i);
	}
	
	/**
	 * 取得切割後的word array list
	 * @return 切割後的word array list
	 */
	public ArrayList<String> getWordsList(){
		return words;
	}
	
	/**
	 * 取得index對照表
	 * @return index對照表
	 */
	public HashMap<Integer,Integer> getMap(){
		return map;
	}
	
	/**
	 * 取得第i個字的index
	 * @param i 第幾個字
	 * @return index 
	 */
	public int getCharIdxFromWordIdx(int i){
		return map.get(i);
	}
	
	/*
	public int[] getIdxesFromCharIdx(int i){
		int[] idxes = new int[2];
		int j=1;
		for(;j<words.size();j++){
			if(map.get(j)>i){
				idxes[0]=j-1;
				idxes[1]=i-map.get(j-1);
				return idxes;
			}
		}
		idxes[0]=j;
		idxes[1]=idxes[1]=i-map.get(j);
		return idxes;
	}*/
}
