package com.gsimedia.gsiebook.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class GSiAnnotation implements Parcelable{
	public Integer iPage = Integer.valueOf(0);
	public String iAnnotation = null;
	public GSiAnnotation(Integer aPage, String aAnno){
		iPage = aPage;
		iAnnotation = aAnno;
	}
	/**
	 * parcelable related methods
	 */
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(iPage.intValue());
		dest.writeString(iAnnotation);
	}
	public void readFromParcel(Parcel source){
		iPage = Integer.valueOf(source.readInt());
		iAnnotation = source.readString();
	}
	private GSiAnnotation(Parcel p){
		this.readFromParcel(p);
	}
	public static final Parcelable.Creator<GSiAnnotation> CREATOR = new Parcelable.Creator<GSiAnnotation>() {

		public GSiAnnotation createFromParcel(Parcel source) {
			return new GSiAnnotation(source);
		}

		public GSiAnnotation[] newArray(int size) {
			return new GSiAnnotation[size];
		}


	};
}
