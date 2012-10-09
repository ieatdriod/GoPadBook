package org.iii.ideas.reader.drm;

/*import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.gsimedia.sa.DeviceIDException;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.IllegalP12FileException;
import com.gsimedia.sa.IllegalRightObjectException;
import com.gsimedia.sa.NOPermissionException;
import com.gsimedia.sa.Permission;
import com.gsimedia.sa.io.contentstream.ContentInputStream;
import com.gsimedia.sa.xml.XMLDocument;
import com.gsimedia.sa.xml.XMLElement;
import com.gsimedia.sa.xml.XMLException;*/

/**
 * 未使用，請見NewDecipher
 * @author III
 * 
 */
public class Decipher {
	/*public static String getP12Path(){
		if((new File("/sdcard/sd")).exists())
			return "/sdcard/sd";
		
		return "/sdcard";
	}
	
	public static void decrypt(String dcfPath,String epubPath,Context ctx){
		try {
			int epubIndex=0;
			//Log.d("EpubPath","is:"+epubPath);
			GSiMediaInputStreamProvider sc = new GSiMediaInputStreamProvider(dcfPath, getP12Path(),ctx);
			XMLElement mElem=null;
			ContentInputStream cis = null; 
			java.util.Vector<XMLElement> mTrackElems = new java.util.Vector<XMLElement>(); 
			try {
			   	InputStream is = sc.getContentInputStream(Permission.PLAY, 1);
			   	if(is!=null){
			   		try {
			   			XMLDocument xmldoc = new XMLDocument(is);
			   			mElem = xmldoc.getRootElement();
			   			mTrackElems = mElem.getChildElementsByName("track");
			   		} catch (XMLException e) {
			   			//TODO Auto-generated catch block
			   			e.printStackTrace();
			   		}
			   		is.close();
			   	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NOPermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(mTrackElems.size()>1)
				epubIndex =  -1;
			
			XMLElement trackElem = mTrackElems.get(0);
			
			if(trackElem!=null){
				XMLElement elem = trackElem.getChildElement("data.container_index");				
				if(elem!=null){
					try{
						epubIndex = elem.getIntValue();
					}catch(XMLException e){
						e.printStackTrace();
					}
				}
			}
			
			//cis = sc.getContentInputStream(Permission.PLAY, 2);
			cis = sc.getContentInputStream(Permission.PLAY, epubIndex);
			FileOutputStream fos = new FileOutputStream(epubPath);
			byte[] buf = new byte[1024];
			while (true) {
				int bytesRead = cis.read(buf);
			    if (bytesRead == -1)
			    	break;
			    fos.write(buf, 0, bytesRead);
			}
			cis.close();
			fos.close();    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		} catch (IllegalRightObjectException e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		} catch (IllegalP12FileException e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		} catch (NOPermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeviceIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}