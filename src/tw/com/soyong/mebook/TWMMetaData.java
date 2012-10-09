package tw.com.soyong.mebook;

import java.io.IOException;
import java.io.InputStream;

import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.IllegalP12FileException;
import com.gsimedia.sa.NOPermissionException;
import com.gsimedia.sa.Permission;
import com.gsimedia.sa.xml.XMLDocument;
import com.gsimedia.sa.xml.XMLElement;
import com.gsimedia.sa.xml.XMLException;

/**
 * TEB file meta data helper class
 * @author Victor
 *
 */
public class TWMMetaData {
	private XMLElement mElem = null;
	private java.util.Vector<XMLElement> mTrackElems = new java.util.Vector<XMLElement>();

	/**
	 * TWMMetaData ctor
	 * @param isp GSiMediaInputStreamProvider
	 */
	public TWMMetaData(GSiMediaInputStreamProvider isp){
		if(isp==null)
			throw new NullPointerException();
		
		try {
			InputStream is = isp.getContentInputStream(Permission.PLAY, 1);
			if(is!=null){
	    		is = isp.getContentInputStream(Permission.PLAY, 1);
				try {
					XMLDocument xmldoc = new XMLDocument(is);
					mElem = xmldoc.getRootElement();
					mTrackElems = mElem.getChildElementsByName("track");
				} catch (XMLException e) {
					e.printStackTrace();
				}		
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NOPermissionException e) {
			e.printStackTrace();
		} catch (IllegalP12FileException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get cover container index
	 * @return index or -1
	 */
	public int getCoverContainerIndex(){
		if(mElem!=null){
			XMLElement elem = mElem.getChildElement("album_cover.container_index");
			if(elem!=null){
				try {
					return elem.getIntValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		return -1;
	}
	
	/**
	 * get total track
	 * @return total track
	 */
	public int getTrackCount(){		
		return mTrackElems.size();
	}
	
	/**
	 * get syd container index
	 * @param trackIndex specific track index
	 * @return container index or -1
	 */
	public int getSYDContainerIndexFromTrack(int trackIndex){
		if(trackIndex>mTrackElems.size()||trackIndex==0)
			return -1;
		
		XMLElement trackElem = mTrackElems.get(trackIndex-1);
		if(trackElem!=null){
			XMLElement elem = trackElem.getChildElement("data.container_index");
			if(elem!=null){
				try {
					return elem.getIntValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		return -1;
	}
	
	// *** trackIndex start from 1
	/**
	 * get mp3 container index
	 * @param trackIndex specific track index
	 * @return container index or -1
	 */
	public int getMP3ContainerIndexFromTrack(int trackIndex){
		if(trackIndex>mTrackElems.size()||trackIndex==0)
			return -1;
		
		XMLElement trackElem = mTrackElems.get(trackIndex-1);
		if(trackElem!=null){
			XMLElement elem = trackElem.getChildElement("audio.container_index");
			if(elem!=null){
				try {
					return elem.getIntValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		return -1;
	}
	
	private String getTrackChildValue(int trackIndex, String elementName){
		if(trackIndex>mTrackElems.size()||trackIndex==0)
			return null;
		
		XMLElement trackElem = mTrackElems.get(trackIndex-1);
		if(trackElem!=null){
			XMLElement elem = trackElem.getChildElement(elementName);
			if(elem!=null){
				try {
					//String debug = elem.getStrValue();
					return elem.getStrValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}	
		return null;
	}
	
	/**
	 * get mp3 title
	 * @param trackIndex track index
	 * @return mp3 title
	 */
	public String getMP3Title(int trackIndex){

		return getTrackChildValue(trackIndex, "title");
	}
	
	/**
	 * get mp3 subtitle
	 * @param trackIndex track index
	 * @return mp3 subtitle
	 */	
	public String getMP3SubTitle(int trackIndex){
		
		return getTrackChildValue(trackIndex, "subtitle");
	}
	
	/**
	 * get mp3 file length
	 * @param trackIndex track index
	 * @return length
	 */
	public int getMp3FileLength(int trackIndex){
		
		if(trackIndex>mTrackElems.size()||trackIndex==0)
			return -1;
		
		XMLElement trackElem = mTrackElems.get(trackIndex-1);
		if(trackElem!=null){
			XMLElement elem = trackElem.getChildElement("audio.file_length");
			if(elem!=null){
				try {
					return elem.getIntValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		return -1;		
	}
	
	/**
	 * get mp3 End Of Sentent string
	 * @param trackIndex track index
	 * @return eos string or null if index out of range
	 */
	public String getMp3EOS(int trackIndex){
		
		if(trackIndex>mTrackElems.size()||trackIndex==0)
			return null;
		
		XMLElement trackElem = mTrackElems.get(trackIndex-1);
		if(trackElem!=null){
			XMLElement elem = trackElem.getChildElement("audio.play.play_end");
			if(elem!=null){
				try {
					return elem.getStrValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;		
	}
	
	/**
	 * get track index
	 * @param title mp3 title
	 * @return track index or -1 if track not found
	 */
	public int getTrackIndexByTitle(final String title){
		
		final int count = getTrackCount();
		String mp3Title;
		int index = -1 ;
		for ( int i = 0 ; i < count ; ++i ){
			mp3Title = getMP3Title(i+1);
			
			if ( 0 == title.compareTo(mp3Title)){
				index = i+1;
			}
		}
		return index ;
	}
	
	public String getAlbumTitle(){
		if(mElem!=null){
			XMLElement elem = mElem.getChildElement("title");
			if(elem!=null){
				try {
					return elem.getStrValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "" ;
	}
	
	public String getAlbumPublisher() {
		if(mElem!=null){
			XMLElement elem = mElem.getChildElement("publisher");
			if(elem!=null){
				try {
					return elem.getStrValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "" ;		
	}
	
	public String getAlbumAuthor() {
		if(mElem!=null){
			XMLElement elem = mElem.getChildElement("author");
			if(elem!=null){
				try {
					return elem.getStrValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "" ;		
	}
	
	public String getOrgLang() {
		if(mElem!=null){
			XMLElement elem = mElem.getChildElement("org_language");
			if(elem!=null){
				try {
					return elem.getStrValue();
				} catch (XMLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "en_US" ;	
	}
}
