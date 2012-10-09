package com.gsimedia.gsiebook.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class ImageTool {
    /**
     * calculate BestFitFactor
     * @param SourceWidth
     * @param SourceHeight
     * @param TargetWidth
     * @param TargetHeight
     * @return factor
     */
	public static double FindBestFitFract(int SourceWidth, int SourceHeight, int TargetWidth, int TargetHeight){
		double WidthFract, HeightFract,TargetFract;

		WidthFract =(double)TargetWidth/ SourceWidth;
		HeightFract=(double)TargetHeight/ SourceHeight;
		if ((WidthFract>=1) && (HeightFract>=1)){ 
			if (WidthFract > HeightFract)
				TargetFract = HeightFract;
			else
				TargetFract = WidthFract;
		}else if ((WidthFract >1) && (HeightFract <1) ||
				  (WidthFract <1) && (HeightFract >1)){
			if (WidthFract > HeightFract)
				TargetFract = HeightFract;
			else
				TargetFract = WidthFract;
		}else if ((WidthFract<=1) && (HeightFract<=1)){ 
			if (WidthFract > HeightFract)
				TargetFract = HeightFract;
			else
				TargetFract = WidthFract;
		}else{
			TargetFract = 1;
		}
		return TargetFract;
	}
	

	public static Bitmap decodeImage(byte[] data, BitmapFactory.Options bfo,int aLength, int aWidth, int aHeight){
		if (bfo==null)
			bfo = new BitmapFactory.Options();
		Bitmap bm = null;
		bfo.inJustDecodeBounds = true;
		bm = BitmapFactory.decodeByteArray(data, 0, aLength,bfo);
		bm=null;
		//find scale factor
		int aTWidth = aWidth; 
		int aTHeight = aHeight;
		double scale = ImageTool.FindBestFitFract(bfo.outWidth,bfo.outHeight,aTWidth,aTHeight);
		int power;
		if (scale > 1)
			power = 1;
		else{
			power = (int) Math.ceil((Math.log(1/scale)/Math.log(2d)));
		}
		power = power*power;
		//set power as 2^x
		if(power==9)
			power = 8;
		if(power==25)
			power = 16;
		if(power==36)
			power = 32;
		
		bfo.inSampleSize = power;
		bfo.inJustDecodeBounds = false;
		
		//decode image
		bm = BitmapFactory.decodeByteArray(data, 0, aLength,bfo);
		return bm;
	}
	public static Bitmap decodeImage(Uri aUri, BitmapFactory.Options bfo,int aWidth, int aHeight, Activity caller){
		if (bfo==null)
			bfo = new BitmapFactory.Options();
        InputStream in = null;
        Bitmap bm = null;
        try {
        	bfo.inJustDecodeBounds = true;
        	in = caller.getContentResolver().openInputStream(aUri);
        	bm = BitmapFactory.decodeStream(in, null, bfo);
        	in.close();
        	bm=null;
        	//find scale factor
        	int aTWidth = aWidth; 
        	int aTHeight = aHeight;
        	double scale = ImageTool.FindBestFitFract(bfo.outWidth,bfo.outHeight,aTWidth,aTHeight);
        	int power;
        	if (scale > 1)
        		power = 1;
        	else{
        		power = (int) Math.ceil((Math.log(1/scale)/Math.log(2d)));
        	}
        	power = power*power;
        	//set power as 2^x
        	if(power==9)
        		power = 8;
        	if(power==25)
        		power = 16;
        	if(power==36)
        		power = 32;
        	
        	bfo.inSampleSize = power;
        	bfo.inJustDecodeBounds = false;
        	
        	//decode image
        	in = caller.getContentResolver().openInputStream(aUri);
        	bm = BitmapFactory.decodeStream(in,null, bfo);
        	in.close();
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return bm;
	}
	public static Bitmap decodeImage(String aPath,BitmapFactory.Options bfo, Activity caller){
		Display display = ((WindowManager) caller.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		int aTWidth = display.getWidth(); 
		int aTHeight = display.getHeight();
		Log.d(Config.LOGTAG,"screen width="+aTWidth+" height="+aTHeight);
		return decodeImage(aPath, bfo, aTWidth, aTHeight);
	}
	public static Bitmap decodeImage(String aPath,BitmapFactory.Options bfo, int aWidth, int aHeight){
		if (bfo==null)
			bfo = new BitmapFactory.Options();
		//check file if exist
		File aFile = new File(aPath);
		if(!aFile.exists()){
			Log.e(Config.LOGTAG,"decode Image Not exist! "+aPath);
			return null;
		}
        bfo.inJustDecodeBounds = true;
		Bitmap bm = BitmapFactory.decodeFile(aPath, bfo);
		bm=null;
        //find scale factor
		int aTWidth = aWidth; 
		int aTHeight = aHeight;
        double scale = ImageTool.FindBestFitFract(bfo.outWidth,bfo.outHeight,aTWidth,aTHeight);
        int power;
        if (scale > 1)
        	power = 1;
        else{
        	power = (int) Math.ceil((Math.log(1/scale)/Math.log(2d)));
        }
        power = power*power;
        //set power as 2^x
        if(power==9)
        	power = 8;
        if(power==25)
        	power = 16;
        if(power==36)
        	power = 32;
        
        bfo.inSampleSize = power;
        bfo.inJustDecodeBounds = false;
        
        //decode image
        bm = BitmapFactory.decodeFile(aPath,bfo);
        return bm;
	}
	/**
	 * 
	 * @param aContext: application context
	 * @return
	 */
	public static double getDip(Context aContext){
		return aContext.getResources().getDisplayMetrics().density;// + 0.5f;
	}
}
