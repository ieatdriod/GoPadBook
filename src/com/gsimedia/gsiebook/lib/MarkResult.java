package com.gsimedia.gsiebook.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.gsimedia.gsiebook.common.Config;
import com.taiwanmobile.myBook_PAD.R;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.util.Log;

public class MarkResult {
	/**
	 * Logging tag.
	 */
	public static final String TAG = "gsiebook";
	
	/**
	 * Page number.
	 */
	public int page;
	public int regionID;
	
	/**
	 * add found string
	 */
	private String iText = "";
	public void setText(String text){
		Log.d(Config.LOGTAG,"set find text:"+text);
		iText = text;
	}
	public String getText(Context c){
		if(c!=null){
			String aStr= c.getString(R.string.GSI_BOOKMARK_TEXT);
			String aDisplay = String.format(aStr, page+1);
			return aDisplay+" "+iText;
		}else{
			return iText;
		}
	}
	/**
	 * List of rects that mark find result occurences.
	 * In page dimensions (not scalled).
	 */
	public Region markers;
	
	public MarkResult(){
		markers = new Region();
	}
	/**
	 * Add marker.
	 */
	public void addMarker(int x0, int y0, int x1, int y1) {
		if (x0 >= x1) throw new IllegalArgumentException("x0 must be smaller than x1: " + x0 + ", " + x1);
		if (y0 >= y1) throw new IllegalArgumentException("y0 must be smaller than y1: " + y0 + ", " + y1);
		Rect nr = new Rect(x0, y0, x1, y1);
		
		if (this.markers == null){
			this.markers = new Region();
			this.markers.set(nr);
		} else {
			this.markers.union(nr);
		}
	}
	
	public String toXML() {
		StringBuilder b = new StringBuilder();
		
		final String aRootBegin= "<underline>";
		final String aRootEnd= "</underline>";
		final String aTextElem= "<text>%s</text>";	
		final String aPageElem= "<page>%d</page>";	
		final String aRectBegin= "<rect>";
		final String aRectEnd= "</rect>";
		
		//append root element
		b.append(aRootBegin);
		
		//append text element
		if (iText != null && iText.trim().length()>0)
			b.append(String.format(aTextElem, this.iText));
		else
			b.append(String.format(aTextElem, ""));
	
		//append page 
		b.append(String.format(aPageElem, this.page));
		
		//append rects
		if (this.markers != null && !this.markers.isEmpty()){
			RegionIterator iter = new RegionIterator(this.markers);			
			Rect r = new Rect();
			while (iter.next(r)) {
				b.append(aRectBegin);
//				b.append(r);
				b.append(String.valueOf(r.left)+","+ String.valueOf(r.top)+"," +String.valueOf(r.right)+","+String.valueOf(r.bottom));
				b.append(aRectEnd);
	        }	
		}
		b.append(aRootEnd);
		return b.toString();
	}
	/*
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		final String aRootBegin= "<underline>";
		final String aRootEnd= "</underline>";
		final String aTextElem= "<text>%s</text>";	
		final String aRectBegin= "<rect>";
		final String aRectEnd= "</rect>";
		
		//append root element
		b.append(aRootBegin);
		
		//append text element
		if (iText != null && iText.trim().length()>0)
			b.append(String.format(aTextElem, this.iText));
		else
			b.append(String.format(aTextElem, ""));
	
		//append rects
		if (this.markers != null && !this.markers.isEmpty()){
			RegionIterator iter = new RegionIterator(this.markers);			
			Rect r = new Rect();
			while (iter.next(r)) {
				b.append(aRectBegin);
//				b.append(r);
				b.append(String.valueOf(r.top)+","+ String.valueOf(r.left)+"," +String.valueOf(r.bottom)+","+String.valueOf(r.right));
				b.append(aRectEnd);
			}
		}
		b.append(aRootEnd);
		return b.toString();
	}
	*/
	
	public void finalize() {
		Log.i(TAG, this + ".finalize()");
	}
}
