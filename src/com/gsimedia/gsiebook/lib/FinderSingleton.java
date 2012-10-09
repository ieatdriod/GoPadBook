package com.gsimedia.gsiebook.lib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.gsimedia.gsiebook.PDF;
import com.gsimedia.gsiebook.common.Config;

public class FinderSingleton {
	private FinderSingleton(){}
	private static FinderSingleton iInstance = null;
	private static WeakReference<PDF> iPDFRef = null;
	public static FinderSingleton getInstance(){
		if(iInstance == null){
			iInstance = new FinderSingleton();
			//do initialize
			iObsArray = new ArrayList<FinderObserver>();
		}
		return iInstance;
	}
	public void setFile(PDF aFile){
		iPDFRef = new WeakReference<PDF>(aFile);
	}
	/**
	 * finder observer
	 */
	private static ArrayList<FinderObserver> iObsArray = null;
	public void registObserver(FinderObserver aObs){
		if(!iObsArray.contains(aObs))
			iObsArray.add(aObs);
	}
	public void unregistObserver(FinderObserver aObs){
		if(iObsArray.contains(aObs))
			iObsArray.remove(aObs);
	}
	private void NotifyFindComplete(int err,ArrayList<FindResult> aResult){
		for(int i=0;i<iObsArray.size();i++)
			iObsArray.get(i).NotifyFindComplete(err,aResult);
	}
	/**
	 * find action related 
	 */
	public void cancelFind(){
		if(bRunning)
			bCancel = true;
	}
	private static boolean bRunning = false;
	private static FindAsyncTask iTask = null;
	public boolean find(String aText, int aStart, int aMax){
		if(iPDFRef!=null){
			if(iPDFRef.get()!=null){
				if(aText.trim().length()>0){
					if(!bRunning){
						bRunning = true;
						iTask = new FindAsyncTask();
						iTask.execute(new FindInfo(aText,aStart,aMax));
						return true;
					}else{
						return true;
					}
				}else{
					return false;
				}
			}else
				return false;
		}else
			return false;
	}
	private class FindInfo{
		public String iText = null;
		public int iStart = 0;
		public int iMax = 0;
		FindInfo(String aText, int aStart, int aMax){
			iText = aText;
			iStart = aStart;
			iMax = aMax;
		}
	}
	private boolean bCancel = false;
	private class FindAsyncTask extends AsyncTask<FindInfo, Void, ArrayList<FindResult>>{

		@Override
		protected void onPreExecute() {
			bCancel = false;
			super.onPreExecute();
		}

		@Override
		protected ArrayList<FindResult> doInBackground(FindInfo... params) {
			String aText = params[0].iText;
			int aStart = params[0].iStart;
			int aMax = params[0].iMax;
			ArrayList<FindResult> aResult = null;
			if(iPDFRef!=null){
				if(iPDFRef.get()!=null){
					aResult = new ArrayList<FindResult>();
					aResult.clear();
					// real find
					PDF aPDF = iPDFRef.get();
					for(int i=aStart;i<aPDF.getPageCount();i++){
						if(bCancel)
							break;
						List<FindResult> aFound = aPDF.find(aText, i);
						if(aFound!=null){
							//give a index
							for(int idx=0;idx<aFound.size();idx++)
								aFound.get(idx).numOfPage=idx;
							aResult.addAll(aFound);
						}
						if(aResult.size()>=aMax)
							break;
					}
				}else{
					Log.e(Config.LOGTAG,"PDF reference in FinderSingleton is no longer exist!");
				}
			}else{
				Log.e(Config.LOGTAG,"set PDF to FinderSingleton first!");
			}
			return aResult;
		}

		@Override
		protected void onPostExecute(ArrayList<FindResult> result) {
			if(result!=null)
				NotifyFindComplete(FinderObserver.KErrNone,result);
			else
				NotifyFindComplete(FinderObserver.KErrNotFound,null);
			bRunning = false;
			bCancel = false;
			super.onPostExecute(result);
		}

	}
	
	
}
