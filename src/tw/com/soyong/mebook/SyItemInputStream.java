 
package tw.com.soyong.mebook;

import java.io.FileNotFoundException;
import java.io.IOException;

public class SyItemInputStream extends SyInputStream {
	
	// JAVA virtual machine always used big-endian, Intel x86 used little-endian. 
	public final static int swabInt(int v) {
	     return  (v >>> 24) | (v << 24) | 
	       ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
	     }	
    
    public SyItemInputStream(final String fileName) throws FileNotFoundException {
        super(fileName);
    }
    
    public SyItemInputStream(final SyInputStream sis){
    	super(sis);
    }
    
    public boolean readSyItem(SyItem si, int buildType) throws IOException {
        
        // Read SyItem
        try {
			si.mID = readFixedString(SyItem.SY_TAG_LEN);
	        si.mHdSize = read();
	
	        
	        si.mFlag = swabInt(readInt());
	        si.mItem = swabInt(readInt());
	        si.mValue = swabInt(readInt());
	        si.mSize = swabInt(readInt());
	        
	        si.mPosition = getFilePointer();
		} catch (IOException e) {
			e.printStackTrace();
			return false ;
		}
        
        // Check if SyItem is valid or not.
        if ( !SyItem.isValidate(si) ){
            return false;
        }
        
        // Read data in SyItem.
        if ( !SyItem.isNode(si) ) {
            // This SyItem is a leaf.
            if ( (buildType & SyBook.BT_HEAD_ONLY) != 0 ){
            	skipBytes ( si.mSize);
            }
            else if ( (buildType & SyBook.BT_ALL) != 0 ||
                ((buildType & SyBook.BT_DATA_INCLUDE) != 0 && SyItem.isType(si,buildType)) ||
                ((buildType & SyBook.BT_DATA_EXCLUDE) != 0 && !SyItem.isType(si,buildType)) ) {
                si.mLeafData = new byte[si.mSize];
                read(si.mLeafData);
            } else {
            	skipBytes(si.mSize);
            }
        }
        
        return true;
    }
}