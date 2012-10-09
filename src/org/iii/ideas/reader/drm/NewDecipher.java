package org.iii.ideas.reader.drm;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.widget.Toast;

import com.gsimedia.sa.DeviceIDException;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.IllegalP12FileException;
import com.gsimedia.sa.IllegalRightObjectException;
import com.gsimedia.sa.NOPermissionException;
import com.gsimedia.sa.Permission;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.gsimedia.sa.io.contentstream.ContentInputStream;
import com.taiwanmobile.myBook_PAD.R;

/**
 * drm解密class
 * @author III
 * 
 */
public class NewDecipher {
	/**
	 * gsi stream provider
	 */
	public static GSiMediaInputStreamProvider sc=null;
	
	/**
	 * 取得p12路徑
	 * @param ctx context
	 * @return path of p12 file
	 */
	public static String getP12Path(Context ctx){
		return ctx.getFilesDir().toString();
	}
	
	/**
	 *  根據epub路徑取得dcf路徑
	 * @param epubPath epub路徑
	 * @return dcf path
	 */
	public static String getDcfPathFromEpubPath(String epubPath){
		//Log.d("dcfPath",epubPath.replace("epub", "teb"));
		return epubPath.replace("epub", "teb");
	}
	
	/**
	 * 解密epub
	 * @param dcfPath dcfr路徑
	 * @param epubPath epub路徑 
	 * @param context context
	 * @throws IOException io exception
	 * @throws IllegalRightObjectException ro錯誤
	 * @throws DeviceIDException device id exception
	 * @throws NOPermissionException 無permission exception
	 * @throws IllegalP12FileException p12有誤exception
	 */
    public static void decryptEpub(String dcfPath,String epubPath, Context context) throws IOException, IllegalRightObjectException, DeviceIDException, NOPermissionException, IllegalP12FileException{
    	sc = new GSiMediaInputStreamProvider(dcfPath, getP12Path(context), context);
    	//GSiMediaInputStreamProvider sc = new GSiMediaInputStreamProvider(dcfPath, getP12Path(context), context);
		ContentInputStream epubCIS = sc.getContentInputStream(Permission.DISPLAY, 2);		
		FileOutputStream fos = new FileOutputStream(epubPath);
		byte[] buff = new byte[1024];
		while (true) {
			int bytesRead = epubCIS.read(buff);
		    if (bytesRead == -1)
		    	break;
		    fos.write(buff, 0, bytesRead);
		}
		epubCIS.close();
		fos.close();    
				


    }
    
    /**
     * 解密並取得某一章節的InputStream
     * @param outPath 輸出路徑
     * @param epubPath epub路徑
     * @param context context
     * @return input stream
     */
//    public static InputStream getChapterInputStream(String outPath, String epubPath,Context context){
//    	try {
//    		//Log.d("a","a");
//    		if(sc==null)
//    			sc = new GSiMediaInputStreamProvider(getDcfPathFromEpubPath(epubPath), getP12Path(context), context);
//    		//GSiMediaInputStreamProvider sc = new GSiMediaInputStreamProvider(getDcfPathFromEpubPath(epubPath), getP12Path(context), context);
//			try{
//				//Log.d("a","b");
//				ContentInputStream epubCIS = sc.getContentInputStream(Permission.DISPLAY, 2);	
//				//Log.d("a","c");				
//				String enPath = outPath+".twm";
//				File enFile = new File(enPath);
//				FileInputStream enStream = new FileInputStream(enFile);
//				int length = (int) enFile.length();  
//				byte[] data = new byte[length];
//				int off = 0;
//				int readLen = 0;
//				while((readLen=enStream.read(data, off, length-off))>0){
//					off += readLen;
//				}
//				//Log.d("a","d");
//				int decLen = epubCIS.decryptEPUBData(data, 0, length, data, 0);
//				//Log.d("a","e");
//				if(decLen>0){
//					return new ByteArrayInputStream(data);
//					/*
//					BufferedOutputStream dest = new BufferedOutputStream(
//							new FileOutputStream(outPath), decLen);
//					dest.write(data, 0, decLen);
//					dest.flush();
//					dest.close();
//					*/			
//				}
//				//AndroidLibrary.copyFile(new File(outPath),new File("/sdcard/chap.xml"));
//			}catch(NOPermissionException e){
//				e.printStackTrace();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalRightObjectException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalP12FileException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DeviceIDException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//    }
    

    /**
     * 解密某一章節並寫成檔案
     * @param outPath 輸出檔案路徑
     * @param epubPath epub path
     * @param context context
     */
//    public static void decryptChapter(String outPath, String epubPath,Context context){
//    	try {
//    		//Log.d("a","a");
//    		if(sc==null)
//    			sc = new GSiMediaInputStreamProvider(getDcfPathFromEpubPath(epubPath), getP12Path(context), context);
//    		//GSiMediaInputStreamProvider sc = new GSiMediaInputStreamProvider(getDcfPathFromEpubPath(epubPath), getP12Path(context), context);
//			try{
//				//Log.d("a","b");
//				ContentInputStream epubCIS = sc.getContentInputStream(Permission.DISPLAY, 2);	
//				//Log.d("a","c");				
//				String enPath = outPath+".twm";
//				File enFile = new File(enPath);
//				FileInputStream enStream = new FileInputStream(enFile);
//				int length = (int) enFile.length();  
//				byte[] data = new byte[length];
//				int off = 0;
//				int readLen = 0;
//				while((readLen=enStream.read(data, off, length-off))>0){
//					off += readLen;
//				}
//				//Log.d("a","d");
//				int decLen = epubCIS.decryptEPUBData(data, 0, length, data, 0);
//				//Log.d("a","e");
//				if(decLen>0){
//					//Log.d("a","f");
//					//Log.d("outPath","is:"+outPath);
//					BufferedOutputStream dest = new BufferedOutputStream(
//							new FileOutputStream(outPath), decLen);
//					dest.write(data, 0, decLen);
//					dest.flush();
//					dest.close();			
//				}
//				//AndroidLibrary.copyFile(new File(outPath),new File("/sdcard/chap.xml"));
//			}catch(NOPermissionException e){
//				e.printStackTrace();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalRightObjectException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalP12FileException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DeviceIDException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
    /**
     * 用固定大小的buffer來decrypt檔案，避免out of memory
     * @param entrySize 檔案大小
     * @param enStream 檔案stream
     * @param outPath 寫出路徑
     * @param epubPath epub path
     * @param context context
     * @return
     */
    public static boolean decryptChapterWithBuffer(long entrySize,InputStream enStream,String outPath, String epubPath,Context context)throws DeviceIDException,Exception{
    	if(entrySize<=0)
    		return false;
    	try {
    		//Log.d("a","a");
    		if(sc==null)
    			sc = new GSiMediaInputStreamProvider(getDcfPathFromEpubPath(epubPath), getP12Path(context), context);
    		//GSiMediaInputStreamProvider sc = new GSiMediaInputStreamProvider(getDcfPathFromEpubPath(epubPath), getP12Path(context), context);
			try{
				ContentInputStream epubCIS = sc.getContentInputStream(Permission.DISPLAY, 2);		
				//String enPath = outPath+".twm";
				FileOutputStream fos = new FileOutputStream(outPath);
				//FileInputStream enStream = new FileInputStream(enFile);
				int readLen = 0;
				//boolean ret = true;
				byte[] data = new byte[1024];
				byte[] out;
				while ((readLen = enStream.read(data)) > 0) {
					out = null;
					entrySize -= readLen;
					if (entrySize>0){
						out = epubCIS.updateEPUBData(data, 0,
								readLen);
					}else{
						out = epubCIS.doFinal(data, 0, readLen);
					}
					if (out == null) {
						//ret = false;
						break;
					}

					fos.write(out);
				}
				data=null;
				enStream.close();
				fos.close();
				//AndroidLibrary.copyFile(new File(outPath),new File("/sdcard/chap.xml"));
			}catch(NOPermissionException e){
				e.printStackTrace();
			}
		} 
//    	catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalRightObjectException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalP12FileException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DeviceIDException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		catch (Exception e){
			e.printStackTrace();
			/*check new GSiMediaInputStreamProvider fail by deviceID*/
			String deviceID = GSiMediaRegisterProcess.getID(context);
			if(deviceID.length() == 0){
				String errorMsg = context.getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG);				
				throw new DeviceIDException(errorMsg);
			}
			
			
		}
		return true;
    }
}
