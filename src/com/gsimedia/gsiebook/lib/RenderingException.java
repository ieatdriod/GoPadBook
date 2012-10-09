package com.gsimedia.gsiebook.lib;

public class RenderingException extends Exception {
	private static final long serialVersionUID = 1010978161527539002L;
	
	/*public RenderingException(String message) {
		super(message);
	}*/
	private int page = 0;
	public RenderingException(int message) {
		super(""+message);
		page = message;
	}
	public int GetPageNumber(){
		return page;
	}
}
