package com.gsimedia.gsiebook.lib;

public class GSiLastPage {
	private int iPage = 0;
	private long iTimeStamp = 0;
	private boolean bDeviceIDEmpty = false;
	GSiLastPage(){
		setPage(0);
	}
	GSiLastPage(int aPage){
		setPage(aPage);
	}
	GSiLastPage(int aPage,long aTime){
		setPage(aPage);
		setTime(aTime);
	}
	public void setPage(int aPage){
		iPage = aPage;
		iTimeStamp = System.currentTimeMillis();
	}
	public void setTime(long aTime){
		iTimeStamp = aTime;
	}
	public void setDeviceIDEmpty(boolean isEmpty){
		bDeviceIDEmpty = isEmpty;
	}
	public int getPage(){
		return iPage;
	}
	public long getTime(){
		return iTimeStamp;
	}
	public boolean getDeviceIDEmpty(){
		return bDeviceIDEmpty;
	}
	@Override
	public String toString() {
//		return super.toString();
		return "GSiLastPage(Page="+iPage+" TimeStamp="+iTimeStamp+")";
	}
	
}
