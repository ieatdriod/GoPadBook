package com.gsimedia.gsiebook;

import java.io.IOException;
import java.io.InputStream;

import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.IllegalP12FileException;
import com.gsimedia.sa.NOPermissionException;
import com.gsimedia.sa.Permission;
import com.gsimedia.sa.xml.XMLDocument;
import com.gsimedia.sa.xml.XMLElement;
import com.gsimedia.sa.xml.XMLException;

public class TWMMetaData {
	private XMLElement mElem = null;
	private java.util.Vector<XMLElement> mTrackElems = new java.util.Vector<XMLElement>();

	public TWMMetaData(GSiMediaInputStreamProvider isp) throws NullPointerException{
		if(isp==null)
			throw new NullPointerException();
		
		try {
			InputStream is = isp.getContentInputStream(Permission.PLAY, 1);
			if(is!=null){				
				try {
					XMLDocument xmldoc = new XMLDocument(is);
					mElem = xmldoc.getRootElement();
					mTrackElems = mElem.getChildElementsByName("track");
				} catch (XMLException e) {
					// TODO Auto-generated catch block
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
		} catch (IllegalP12FileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getCoverContainerIndex(){
		if(mElem!=null){
			XMLElement elem = mElem.getChildElement("album_cover.container_index");
			if(elem!=null){
				try {
					return elem.getIntValue();
				} catch (XMLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return -1;
	}
	
	public int getTrackCount(){		
		return mTrackElems.size();
	}
}
