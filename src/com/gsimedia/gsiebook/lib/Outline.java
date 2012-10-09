package com.gsimedia.gsiebook.lib;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Outline implements Parcelable{
	/**
	 * Logging tag.
	 */
	public static final String TAG = "gsiebook";
	
	public Outline(){
		pdf_outline = 0;
		title = "";
	}
	/**
	 * Holds pointer to native outline struct.
	 */
	private int pdf_outline= 0;
	
	private String title = "";
	
	public void finalize() {
		Log.i(TAG, this + ".finalize()");
	}
	public String getTitle(){
		return title;
	}
	/**
	 * parcel related interface
	 */
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(pdf_outline);
		dest.writeString(title);
	}
	private Outline(Parcel in){
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in){
		pdf_outline = in.readInt();
		title = in.readString();
	}
	public static final Parcelable.Creator<Outline> CREATOR = new Parcelable.Creator<Outline>() {

		public Outline createFromParcel(Parcel source) {
			return new Outline(source);
		}

		public Outline[] newArray(int size) {
			return new Outline[size];
		}
	};
}
