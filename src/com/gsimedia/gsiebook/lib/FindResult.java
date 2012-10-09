package com.gsimedia.gsiebook.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.gsimedia.gsiebook.common.Config;
import com.taiwanmobile.myBook_PAD.R;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


/**
 * Find result.
 */
public class FindResult implements Parcelable{
	
	/**
	 * Logging tag.
	 */
	public static final String TAG = "gsiebook";
	
	/**
	 * Page number.
	 */
	public int page;
	/**
	 * this findresult is the n result of this page
	 */
	public int numOfPage;

	/**
	 * List of rects that mark find result occurences.
	 * In page dimensions (not scalled).
	 */
	public List<Rect> markers;
	
	/**
	 * add found string
	 */
	private String iText;
	public void setText(String text){
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
	 * Add marker.
	 */
	public void addMarker(int x0, int y0, int x1, int y1) {
		if (x0 >= x1) throw new IllegalArgumentException("x0 must be smaller than x1: " + x0 + ", " + x1);
		if (y0 >= y1) throw new IllegalArgumentException("y0 must be smaller than y1: " + y0 + ", " + y1);
		if (this.markers == null)
			this.markers = new ArrayList<Rect>();
		Rect nr = new Rect(x0, y0, x1, y1);
		if (this.markers.isEmpty()) {
			this.markers.add(nr);
		} else {
			this.markers.get(0).union(nr);
		}
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("FindResult(");
		if (this.markers == null || this.markers.isEmpty()) {
			b.append("no markers");
		} else {
			Iterator<Rect> i = this.markers.iterator();
			Rect r = null;
			while(i.hasNext()) {
				r = i.next();
				b.append(r);
				if (i.hasNext()) b.append(", ");
			}
		}
		b.append(")");
		return b.toString();
	}
	
	public void finalize() {
		Log.i(TAG, this + ".finalize()");
	}
	/**
	 * parcelable related
	 */
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(numOfPage);
		dest.writeInt(page);
		dest.writeString(iText);
		dest.writeTypedList(this.markers);
	}
	public void readFromParcel(Parcel source){
		numOfPage = Integer.valueOf(source.readInt());
		page = Integer.valueOf(source.readInt());
		iText = source.readString();
		this.markers = new ArrayList<Rect>();
		source.readTypedList(this.markers, Rect.CREATOR);
	}
	public FindResult(){}
	private FindResult(Parcel p){
		this.readFromParcel(p);
	}
	public static final Parcelable.Creator<FindResult> CREATOR = new Parcelable.Creator<FindResult>() {

		public FindResult createFromParcel(Parcel source) {
			return new FindResult(source);
		}

		public FindResult[] newArray(int size) {
			return new FindResult[size];
		}

	};
	
}
