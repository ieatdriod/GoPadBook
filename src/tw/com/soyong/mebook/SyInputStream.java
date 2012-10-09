
package tw.com.soyong.mebook;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.IllegalP12FileException;
import com.gsimedia.sa.NOPermissionException;
import com.gsimedia.sa.Permission;
import com.gsimedia.sa.io.contentstream.ContentInputStream;

public class SyInputStream extends InputStream {
	
	static public final int MODE_SYD = 0 ;
	static public final int MODE_MP3 = 1 ;
	static public final int MODE_COVER = 2 ;
    
    static final short BIG_ENDIAN = 1;
    static final short LITTLE_ENDIAN = 2;
    
    protected short mEndian;
    protected String mCharset;
    
    // for DRMAgent
    protected GSiMediaInputStreamProvider mSc = null;
    private int mContentIndex ;
    private ContentInputStream mCis = null;
    protected String mTitle;
    
    private long  mPosition;
    private long  mLength;
    public int mOpenState;
    
    public SyInputStream(String fileName) throws FileNotFoundException{
    	this( fileName, "r" );
    }
    
    public SyInputStream(String  fileName, String  mode) throws FileNotFoundException{
    	super();
    	
        mEndian = BIG_ENDIAN;
        mCharset = "US-ASCII";
        mPosition = 0 ;
        mLength = 0 ;
    }
    
    public SyInputStream(GSiMediaInputStreamProvider provider , String titleName ){
    	this(provider,titleName,MODE_SYD);
    }    
    
    public SyInputStream(GSiMediaInputStreamProvider provider , String titleName , int mode){

    	TWMMetaData metadata = new TWMMetaData(provider);
    	if(metadata==null){
    		return ;
    	}
    	
        mEndian = BIG_ENDIAN;
        mCharset = "US-ASCII";
        mPosition = 0 ;
        mLength = 0 ;    	
    	
    	mSc = provider;
    	mTitle = titleName;
    	
    	int track = metadata.getTrackIndexByTitle(titleName);
    	int index ;
    	if ( MODE_SYD == mode){
    		index = metadata.getSYDContainerIndexFromTrack(track);
    	} else {
    		//MODE_MP3
    		index = metadata.getMP3ContainerIndexFromTrack(track);
    	}
    	
    	mContentIndex = index;
    	mOpenState = -1 ;
    	try {
			mCis = provider.getContentInputStream(Permission.PLAY, mContentIndex);
    		mLength = mCis.size();
    		mOpenState = 0 ;
		} catch (IOException e) {
			e.printStackTrace();
			mOpenState = 3;
		} catch (NOPermissionException e) {
			e.printStackTrace();
			mOpenState = 1 ;
		} catch (IllegalP12FileException e) {
			e.printStackTrace();
			mOpenState = 2 ;
		}    	
    }
    
    // only for get COVER
    public SyInputStream(GSiMediaInputStreamProvider provider, int mode){ 
    	
    	// only for get COVER
    	if ( mode != MODE_COVER){
    		return ;
    	}
    	
    	TWMMetaData metadata = new TWMMetaData(provider);
    	if(metadata==null){
    		return ;
    	}
    	
        mEndian = BIG_ENDIAN;
        mCharset = "US-ASCII";
        mPosition = 0 ;
        mLength = 0 ;    	
    	
    	mSc = provider;
    	mTitle = "";
    	
    	int index = metadata.getCoverContainerIndex();

    	mContentIndex = index;
    	
    	mOpenState = -1 ;
    	try {
    		mCis = provider.getContentInputStream(Permission.PLAY, mContentIndex);
    		mLength = mCis.size();
    		mOpenState = 0 ;
		} catch (IOException e) {
			e.printStackTrace();
			mOpenState = 3 ;
		} catch (NOPermissionException e) {
			e.printStackTrace();
			mOpenState = 1;
		} catch (IllegalP12FileException e) {
			e.printStackTrace();
			mOpenState = 2 ;
		}       
    }
    
    public SyInputStream(final SyInputStream is ){
    	this(is.mSc , is.mTitle);
    }
    
    public boolean setGsiParam( GSiMediaInputStreamProvider provider , int contentIndex ){
    	
    	mContentIndex = contentIndex;
    	mSc = provider;
    	mOpenState = -1;
    	try {
			mCis = provider.getContentInputStream(Permission.PLAY, mContentIndex);
			mLength = mCis.size();
			mOpenState = 0 ;
		} catch (IOException e) {
			e.printStackTrace();
			mOpenState = 3;
			return false;
		} catch (NOPermissionException e) {
			e.printStackTrace();
			mOpenState = 1;
			return false;
		} catch (IllegalP12FileException e) {
			e.printStackTrace();
			mOpenState = 2 ;
			return false ;
		}
    	
    	return true ;
    }
    
    /**
     * Returns the position in the stream.
     *
     * @return      the position in the stream.
     */
    
    public long getFilePointer() {
        return mPosition;
    }
    
    /**
     * Set the endian mode for reading integer.
     *
     * @param       i Specify either LITTLE_ENDIAN or BIG_ENDIAN.
     * @exception   java.lang.Exception thrown if this method is not passed
     *              either LITTLE_ENDIAN or BIG_ENDIAN.
     */
    final public void setEndian(short i) throws Exception {
        if ((i == BIG_ENDIAN) || (i == LITTLE_ENDIAN))
            mEndian = i;
        else
            throw (new Exception(
                "Must be SyInputStream.LITTLE_ENDIAN or SyInputStream.BIG_ENDIAN"));
    }
    
    /**
     * Sets Charset name for method readFixedString.
     *
     * @param       charsetName will be used in method readFixedString.
     */
    final public void setCharsetName(String charsetName) {
        mCharset = charsetName;
    }
    
    /**
     * Reads length bytes and transforms to String.
     *
     * @param       length specifies how many bytes to read.
     * @return      a String after reading length bytes.
     */
    public String readFixedString(int length) throws IOException {
        String s = "";
        byte [] b = new byte[length];
        read(b);

        try {
            s = new String(b, mCharset);
        } catch (UnsupportedEncodingException e) {
            //System.out.println(e);
        }
        b = null;
        return s.trim();
    }

    /**
     * Reads length bytes and transforms to String.
     *
     * @param       length specifies how many bytes to read.
     * @return      a String after reading length bytes.
     */
    final public String readFixedString2(int length) throws IOException {
        String s = "";
        byte [] b = new byte[length];
        read(b);

        if (0xff == (b[0]&0xff) && 0xfe == (b[1]&0xff)) {
            s = new String( b , 2 , b.length-2 , "UTF-16LE");
        } else {
            // find \0 for C string end symbol
            int len = b.length;
            int i = 0;
            for (; i < len; ++i) {
                if (0 == b[i]) {
                    break;
                }
            }
            for (; i < len; ++i) {
                b[i] = 0;
            }

            try {
                s = new String(b, mCharset);
            } catch (UnsupportedEncodingException e) {
                //System.out.println(e);
            }
        }
        return s.trim();
    }
    
    /**
     * Reads length bytes and transforms to integer.
     *
     * @param       length specifies how many bytes to read. The valid data is
     *              ranged from 0 to 9.
     * @return      an integer value after reading length bytes.
     */
    final public int readDigit(final int length) throws IOException {
        int digit = 0;
        int ch;
        
        for (int i=0; i<length; ++i) {
            ch = read();
            if ( ch < 0 ){
                throw new EOFException();
            }
            
            if ( ch >= '0' && ch <= '9' ) {
                digit = digit * 10;
                digit += ch-'0';
            }
        }
        return digit;
    }

	@Override
	public int read() throws IOException {
        int ch = mCis.read();
        if ( ch != -1 ){
            ++mPosition;
        }
        return ch;		
	}
	
    public int read(byte[] b) throws IOException {
        int n = mCis.read(b);
        if ( n != -1 ){
            mPosition += n;
        }
        return n;
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        int n = mCis.read(b, off, len);
        if ( n != -1 ){
            mPosition += n;
        }
        return n;

    }	
    
    public long skip(long n) throws IOException {
    	long nSkip =  mCis.skip(n);
    	mPosition += nSkip;
    	return nSkip;
    }  
    
    public int  skipBytes  (int count) throws IOException {
    	return (int) skip(count);
    }
    
    
    public void close() throws IOException {
    	this.mCis.close();
    	this.mPosition = 0 ;
    }   

    public void seek(long pos) throws IOException {
    	if ( pos >= mPosition && pos <= mLength ){
    		long skipLen = pos - mPosition;
    		long nn = mCis.skip(skipLen);
    		mPosition += nn;
    	} else if ( pos < mPosition ){
    		ContentInputStream is = mCis;
    		is.close();
    		
    		//reopen mCis
    		mOpenState = -1 ;
    		try {
				is = mSc.getContentInputStream(Permission.PLAY, mContentIndex);
	    		mPosition = is.skip(pos);
	    		mCis = is;	
	    		mOpenState = 0 ;
			} catch (NOPermissionException e) {
				e.printStackTrace();
				mOpenState = 1 ;
			} catch (IllegalP12FileException e) {
				e.printStackTrace();
				mOpenState = 2 ;
			}

    	} else {
    		mPosition = pos;
    	}
	}
    
    /**
     * Reads 4 bytes and transforms to integer according to _endian setting.
     *
     * @return      an integer value after reading 4 bytes.
     */
    public int readInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0){
            throw new EOFException();
        }

        int ret ;
        if (mEndian == BIG_ENDIAN)
            ret = ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0));
        else
            ret = ((ch1 << 0) | (ch2 << 8) | (ch3 << 16) | (ch4 << 24));
        return ret;
    }

	public long length() {
		return mLength;
	}    
}
