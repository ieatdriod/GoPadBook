package com.gsimedia.gsiebook.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class GSiBookmark implements Parcelable{
	public Integer iPage = Integer.valueOf(0);
	public String iTitle = null;
	public GSiBookmark(Integer aPage, String aTitle){
		iPage = aPage;
		iTitle = aTitle;
	}
	/**
	 * parcelable related methods
	 */
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(iPage.intValue());
		dest.writeString(iTitle);
	}
	public void readFromParcel(Parcel source){
		iPage = Integer.valueOf(source.readInt());
		iTitle = source.readString();
	}
	private GSiBookmark(Parcel p){
		this.readFromParcel(p);
	}
	public static final Parcelable.Creator<GSiBookmark> CREATOR = new Parcelable.Creator<GSiBookmark>() {

		public GSiBookmark createFromParcel(Parcel source) {
			return new GSiBookmark(source);
		}

		public GSiBookmark[] newArray(int size) {
			return new GSiBookmark[size];
		}

	};
}