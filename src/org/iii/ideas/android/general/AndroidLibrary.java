package org.iii.ideas.android.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * 常用工具method的集合
 * @author III
 * 
 */
public class AndroidLibrary {
	/**
	 * 取得dpi (橫向)
	 * @param ctx context
	 * @return dpi
	 */
	public static int getDpi(Context ctx){
		 DisplayMetrics metrics = new DisplayMetrics();
		 (( WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		 //Log.d("getDpi","is:"+metrics.xdpi);
		 return (int) metrics.xdpi;
	}
	
	/**
	 * 取得縱向dpi
	 * @param ctx context
	 * @return dpi
	 */
	public static int getYDpi(Context ctx){
		 DisplayMetrics metrics = new DisplayMetrics();
		 (( WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		 //Log.d("getDpi","is:"+metrics.xdpi);
		 return (int) metrics.ydpi;
	}
	
	/**
	 * 取得系統Display物件
	 * @param ctx
	 * @return Display object
	 */
	public static Display getDisplay(Context ctx){
		return ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	}
	
	/**
	 * 解碼圖片，並根據傳入的寬高調整取樣卛，將解碼後的bitmap物件回傳。註:參數h, w並非 decode後的圖片寬高，由於取樣率以2的次方效果較佳，decode會根據這組數字來決定取樣率。
	 * @param f image file
	 * @param h 圖片呈現高
	 * @param w 圖片呈現寬
	 * @param isCeiling 當取樣率不是2的次方時調整是要取上一級還是下一級。isCeiling若為true圖片較大
	 * @return 解碼後的bitmap
	 */
	public static Bitmap decodeBitmap(File f,int h,int w,boolean isCeiling){
	    Bitmap b = null;
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f), null, o);
	        int scale = 1;
	       // if(o.outHeight*o.outWidth>800*800)
	       // 	return null;
	        boolean isSizeChanged=false;
			while(h<o.outHeight || w<o.outWidth){
				isSizeChanged=true;
				scale*=2;
				h*=2;
				w*=2;
			}
			if(isCeiling && isSizeChanged){
				scale/=2;
			}
	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        o2.inPreferredConfig = Bitmap.Config.RGB_565;
	        b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (Exception e) {
	    }
	    return b;
	}
	
	/**
	 * 清除字串右邊空白字元後回傳 (除了32以外的空白字元)
	 * @param s string
	 * @return 處理後的字串
	 */
	public static String rtrim(String s){
		//non space white space (e.g. /t /n /r)
		if(s==null) return null;
		if(s.length()<1) return "";
		int i=s.length()-1;
		while(i>=0 && Character.isWhitespace(s.charAt(i))&& (s.charAt(i))!=32 )i--;
		return s.substring(0,i+1);
	}
	
	/**
	 * 清除字串右邊空白字元後回傳 (所有空白字元)
	 * @param s string
	 * @return 處理後的字串
	 */
	public static String rtrimAll(String s){
		//所有空白char
		if(s==null) return null;
		if(s.length()<1) return "";
		int i=s.length()-1;
		while(i>=0 && Character.isWhitespace(s.charAt(i)) ){
			//Log.d("rtrim","is:"+((int)s.charAt(i)) );
			i--;
		}
		return s.substring(0,i+1);
	}
	
	/**
	 * 清除字串左邊空白字元後回傳 (所有空白字元)
	 * @param s string
	 * @return 處理後的字串
	 */
	public static String ltrimAll(String s){
		if(s==null) return null;
		if(s.length()<1) return "";
		int i=0;
		while(i<s.length() && Character.isWhitespace(s.charAt(i)) ){
			//Log.d("ltrim","is:"+((int)s.charAt(i)) );
			i++;
			}
		return s.substring(i,s.length());
	}
	
	/**
	 * 清除字串右邊空白字元後回傳 (除了32以外的空白字元)
	 * @param s string
	 * @return 處理後的字串
	 */
	public static String ltrim(String s){
		if(s==null) return null;
		if(s.length()<1) return "";
		int i=0;
		while(i<s.length() && Character.isWhitespace(s.charAt(i)) && (s.charAt(i)!=32) )i++;
		return s.substring(i,s.length());
	}
	
	/**
	 * 刪除資聊夾及其子目錄/檔案
	 * @param dir 資料夾
	 * @return 是否成功
	 */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory() && dir.exists()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    } 
    
    /**
     * 從目錄和相對路徑得到覺對路徑(處理".."符號)
     * @param dir 目錄
     * @param path 相對路徑
     * @return absolute path
     */
    public static String getAbsPathFromRelPath(String dir, String path){
    	int loc; 
    	path = "/"+path;
    	dir = dir.substring(0, dir.lastIndexOf("/"));
    	while((loc=path.indexOf(".."))>=0){
    		path = path.substring(loc+2);
    		dir = dir.substring(0, dir.lastIndexOf("/"));
    	}

    	return dir.substring(7)+path;
    }
    
    
    
    /**
     * 將字串寫成檔案
     * @param data 欲寫出字串
     * @param path 檔案路徑
     */
    public static void writeString(String data, String path){
    	try{
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(data.getBytes());
		fos.close();
		}catch(Exception e){
			Log.e("WriteString",e.toString());
		}
    }
   
    /**
     * 把某一input stream寫成檔案
     * @param in source
     * @param out 檔案路徑
     */
    public static void copyFileFromInputStream(InputStream in, File out){
    	try{
    		File parentDIR = new File(out.getParent());
    		parentDIR.mkdirs();
    		if(!out.exists()){
    			out.createNewFile();
    		}
    		FileOutputStream fos = new FileOutputStream(out);
    		byte fileBuffer[] = new byte[512];
    		//int fileIdx = -1;

    		while (in.read(fileBuffer) != -1) {
    			fos.write(fileBuffer);
    		}
    		in.close();
    		fos.close();
    	}catch(Exception e){
    		Log.e("FileNotFound",""+e);
    	}
    }
    
    
    /**
     * 複製檔案
     * @param in source
     * @param out output
     */
    public static void copyFile(File in, File out){
    	try{
    		File parentDIR = new File(out.getParent());
    		parentDIR.mkdirs();
    		if(!out.exists()){
    			out.createNewFile();
    		}
    		FileInputStream fis = new FileInputStream(in);
    		FileOutputStream fos = new FileOutputStream(out);
    		byte fileBuffer[] = new byte[512];
    		//int fileIdx = -1;

    		while ((fis.read(fileBuffer)) != -1) {
    			fos.write(fileBuffer);
    		}
    		fis.close();
    		fos.close();
    	}catch(Exception e){
    		Log.e("FileNotFound",""+e);
    	}
    }
    /**
     * 移動檔案
     * @param in source
     * @param out output
     */
    public static void moveFile(File in, File out){
    	try{
    		File parentDIR = new File(out.getParent());
    		parentDIR.mkdirs();
    		if(!out.exists()){
    			out.createNewFile();
    		} 
    		//Log.d("BBBBBOutF",out.getAbsolutePath());
    		FileInputStream fis = new FileInputStream(in);
    		FileOutputStream fos = new FileOutputStream(out);
    		byte fileBuffer[] = new byte[512];
    		//int fileIdx = -1;

    		while ((fis.read(fileBuffer)) != -1) {
    			fos.write(fileBuffer);
    		}
    		fis.close();
    		fos.close();
    	}catch(Exception e){
    		Log.e("FileNotFound",""+e);
    	}finally{
    	in.delete();
    	}
    }
    
   
    /**
     * 是否連到網路
     * @param t context
     * @return 是否連到網路 
     */
    public static boolean isConnected(Context t){
    	ConnectivityManager cm = (ConnectivityManager)t.getSystemService(Context.CONNECTIVITY_SERVICE);
    	//Log.d("WIFI",""+cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState().equals(NetworkInfo.State.CONNECTED));
    	//Log.d("MOB",""+cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState().equals(NetworkInfo.State.CONNECTED));
    	return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState().equals(NetworkInfo.State.CONNECTED) || 
    	cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState().equals(NetworkInfo.State.CONNECTED);
    }
    
    /**
     * 是否用3G連到網路
     * @param t context
     * @return 是否用3g連到網路
     */
    public static boolean is3gConnected(Context t){
    	ConnectivityManager cm = (ConnectivityManager)t.getSystemService(Context.CONNECTIVITY_SERVICE);
    	//Log.d("WIFI",""+cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState().equals(NetworkInfo.State.CONNECTED));
    	//Log.d("MOB",""+cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState().equals(NetworkInfo.State.CONNECTED));
    	return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState().equals(NetworkInfo.State.CONNECTED);
    }
    
    /**
     * 外部空間(sd card)是否mount
     * @return 外部空間(sd card)是否mount
     */
    public static boolean isExternalStorageMounted(){
    	if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
        	return true;
        }else{
        	return false;
        }
    }
  
    
    /**
     * 把String用md5 hash成hex string
     * @param t string
     * @return hex string
     * @throws NoSuchAlgorithmException 找不到對應的演算法
     */
    public static String getHexHashString(String t) throws NoSuchAlgorithmException{
    	MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(t.getBytes());
        byte[] hash = digest.digest();
        String hexString="";
        String hex;
        for(int i=0;i<hash.length;i++) {
        	hex=Integer.toHexString(0xFF & hash[i]);
            if (hex.length() == 1) {
                hexString+='0';
            }
            hexString+=hex;
        }

    	return hexString;
    }
    
    /**
     * 從路徑取出檔名(不包含附檔名)
     * @param path path of file
     * @return file name
     */
    public static String getFileName(String path){
    	//從完整path中取出檔名
    	return path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
    }  
    
    /**
     * 取得device imei
     * @param ctx context
     * @return imei code
     */
//    public static String getIMEI(Context ctx){
//    	return ((TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//    }
}
