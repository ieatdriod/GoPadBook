package com.gsimedia.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

import com.gsimedia.gsiebook.TWMMetaData;
import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.sa.*;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.gsimedia.sa.io.contentstream.ContentInputStream;

public class GSiDataSource {
	public static final int EFileStatus_NotFound = -1;
	public static final int EFileStatus_NotAccessable= -2;    	
	public static final int EFileStatus_NotAuthorized= -3;    	
	
	public static final int EFileStatus_Error1 = -4;
	public static final int EFileStatus_Error2 = -5;
	public static final int EFileStatus_Error3 = -6;
	public static final int EFileStatus_Error4 = -7;
	public static final int EFileStatus_Error5 = -8;
	
    private static GSiMediaInputStreamProvider fileProvider = null;
    public static ContentInputStream sastream = null; 
    private static TWMMetaData metadata = null;
    private static String Openedfilepath = "";
    private static String LastTriedfilepath = "";
    private static File file = null;
    private static long filesize = 0;
    private static final java.lang.String TPB= "TPB";
    
	public  static final int meta_Author= 0;
	public  static final int meta_Title= 1;
	public  static final int meta_Publisher= 2;
	public  static final int meta_Editor = 3;
   
	private static byte[] mBuffer = new byte[40960];
    private static String ParseRequiredType(String filepath){
		String aRequiredType = "";
    	if (filepath.toLowerCase().endsWith(Config.FILE_EXT))
    		aRequiredType = TPB;
    	return aRequiredType;
    }
    
    private static int EInvalidContainerIndex = -1;
    private static int ReturnMainContainerIndex(TWMMetaData MediaMetaData){
//    	return EInvalidContainerIndex;
    	return 3;
    }    
    private static int ReturnAlbumContainerIndex(TWMMetaData MediaMetaData){
    	int AlbumContainerIndex = EInvalidContainerIndex;
    	return 2;
//		return AlbumContainerIndex;
    }
    
    private static String ReturnTitle(TWMMetaData MediaMetaData){
		return null;
    }    
    private static String ReturnAlbum(TWMMetaData MediaMetaData){
		return null;
    }    
    
    private static Vector<String> ReturnAuthors(TWMMetaData MediaMetaData){
		return null;
    }    
    
    
    public synchronized static long setPath(String filepath, String p12Path, Context aContext){
    	filesize = 0;
    	int result = 0;
    	String aRequiredType = ParseRequiredType(filepath);
    	if (aRequiredType.matches(TPB)){
    		if ((null == sastream) || Openedfilepath.compareToIgnoreCase(filepath)!=0){
				fileProvider = null;
				sastream = null;
				metadata = null;
				Openedfilepath = "";
				LastTriedfilepath = filepath;
    			try{
        			fileProvider = new GSiMediaInputStreamProvider(filepath,p12Path,aContext);
    			}catch(IOException e){
    				result = EFileStatus_Error1;
    				Log.e(Config.LOGTAG,e.toString());
    			}catch(IllegalRightObjectException e){
    				result = EFileStatus_Error1;
    				Log.e(Config.LOGTAG,e.toString());
    			}catch(DeviceIDException e){
    				result = EFileStatus_Error4;
    				Log.e(Config.LOGTAG,e.toString());
    			}catch(Throwable e){
    				result = EFileStatus_Error1;
    				Log.e(Config.LOGTAG,e.toString());
    			}
    			if(result != 0){/*check new GSiMediaInputStreamProvider fail by deviceID*/
    				String deviceID;
					try {
						deviceID = GSiMediaRegisterProcess.getID(aContext);
					} catch (com.gsimedia.sa.GSiMediaRegisterProcess.DeviceIDException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						deviceID = "";
					}
    				if(deviceID.length() == 0)result = EFileStatus_Error5;
    			}
    			try{
    				if (filepath.endsWith(Config.FILE_EXT)){//||
    					metadata = new TWMMetaData(fileProvider);
    					if (metadata == null) {
    						return 0;
    					}
    					Log.d(Config.LOGTAG,"main container index = "+ReturnMainContainerIndex(metadata));
	        			sastream = fileProvider.getContentInputStream(Permission.DISPLAY,ReturnMainContainerIndex(metadata));
	        			sastream.mark(0);
	        			Openedfilepath = filepath;
    				}
    			}catch(IOException e){
    				result = EFileStatus_Error2;
    				Log.e(Config.LOGTAG,e.toString());
    			}catch(NOPermissionException e){
    				result = EFileStatus_Error3;
    				Log.e(Config.LOGTAG,e.toString());
    			}catch(IllegalP12FileException e){
    				result = EFileStatus_Error4;
    				Log.e(Config.LOGTAG,"p12 exception:"+p12Path);
    				Log.e(Config.LOGTAG,e.toString());
    			}catch (Throwable e){
					e.printStackTrace();
					//just in case
    				fileProvider = null;
    				sastream = null;
    				metadata = null;
    				Openedfilepath = "";
    				result = EFileStatus_Error1;
    			}
    		}
    		if(result!=0)
    			return result;
    		
    		if (null != sastream ){
    			filesize = sastream.size();
    			return filesize;
    		}else{
    			return EFileStatus_NotAuthorized;
    		}
    	}
    	return EFileStatus_NotAuthorized;
    }
    
    public static synchronized void CloseFile(){
    	file = null;
    	fileProvider = null;
		sastream = null;
		metadata = null;
		Openedfilepath = "";
    }
    public static synchronized InputStream GetInputStream(){
		if (null != sastream && null != metadata){
			try {
				sastream.close();
				sastream = fileProvider.getContentInputStream(Permission.DISPLAY,ReturnMainContainerIndex(metadata));
				sastream.mark(0);
				return sastream;
				//return sastream;
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fileProvider = null;
				sastream = null;
				metadata = null;
				Openedfilepath = "";					
			}
			return null;
		}else{
			return null;
		}
    }
 
    public static synchronized long GetFileSize(){
    	return filesize;
    }

    public static synchronized String GetLastTriedfilepath(){
    	return LastTriedfilepath;
    }
    public static synchronized boolean IsCurrentFileExist(){
    	File iFile = new File(LastTriedfilepath);
		return iFile.exists();
    }
    public static synchronized String GetStringMetadata(int MetaType){
    	boolean bFileExist = IsCurrentFileExist();
		String retStr  = null;
    	if (!bFileExist){
    		return "";
    	}
		if (null!=metadata){
			switch (MetaType){
    			case meta_Title:
    				retStr = ReturnTitle(metadata);
    				break;
    			case meta_Author:
    				Vector<String> artists = ReturnAuthors(metadata);
    				break;
    			case meta_Publisher:
    			case meta_Editor:
				default:
					break;
			}
		}
		return retStr;
    }

    public synchronized static int saread(Object fileHandle, byte[]aBuffer, int offset, int nBytes){
    	//Log.d("Lancelot","fread called");
    	int len = -1;
    	if (fileHandle != null){
    		if (fileHandle instanceof  ContentInputStream ){
	    		try {
//	    			int avail = ((ContentInputStream) fileHandle).available();
//	    			Log.d(Config.LOGTAG,"avail="+String.valueOf(avail));
//	    			Log.d(Config.LOGTAG,"read="+String.valueOf(nBytes));
	    			
					len = ((ContentInputStream) fileHandle).read(aBuffer,offset,nBytes);
//    				Log.d(Config.LOGTAG, "end read!!!!, readed = "+String.valueOf(len));
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}else{
    		if (null != sastream){
	    		try {
					len = sastream.read(aBuffer,offset,nBytes);
//    				Log.d(Config.LOGTAG, "end read!!!!");
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
   		}
    			
    	return len;
    }
    
    public static final int SEEK_SUCCESS= 0;
    public static final int SEEK_FAILED= -1;
    
    public synchronized static int saseek(Object fileHandle, int offset){
		if (null != sastream){
			try {
				int avil = sastream.available();
//  					sastream.close();
//    					Log.d("Seek", String.valueOf(avil));
//  					sastream = (ContentInputStream) GSiDataSource.GetInputStream();
				sastream.reset();
				sastream.mark(0);
				avil = sastream.available();
//    					Log.d("Seek", String.valueOf(avil));
				if (offset > 0)
					sastream.skip(offset);
				avil = sastream.available();
//    					Log.d("Seek","seek offset = "+ String.valueOf(offset));
				return offset;
			} catch (IOException e) {
    					// TODO Auto-generated catch block
				e.printStackTrace();
				return SEEK_FAILED;
			}
		}else{
				return SEEK_FAILED;
		}
    }
}
