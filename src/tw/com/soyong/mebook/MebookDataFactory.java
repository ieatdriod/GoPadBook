package tw.com.soyong.mebook;

import java.util.ArrayList;

public class MebookDataFactory {
//	static final String DVD2ME	= "BK_DVD2ME";
//	static final String BOOK2ME = "BK_BK2ME";
//	static final String MUSIC2ME= "BK_MU2ME";
	static final String PICBOOK = "BK_PIC";
	static final String JP_BASIC = "BK_BASICU";
	static final String BASIC_UNICODE = "BK_BASIC2";
	
	
	static public final boolean isSupportBook(MebookInfo bookInfo){
		
	    	boolean ret = false ;
	    	switch( bookInfo.mType ){
	    	case Mebook.SY_PLAN_BOOK:
	    	case Mebook.IMG_BOOK:
	    		ret= true;
	    		break;
	    	
	    	case Mebook.SY_BOOK:
	    		String str = bookInfo.mBookID;
	    		if ( 0 == str.compareTo(BASIC_UNICODE)){
	    			ret = true;
//	    		}else if ( 0 == str.compareTo(DVD2ME)){
//	    			ret = true;
//	    		}else if ( 0 == str.compareTo(BOOK2ME)){
//	    			ret = true;
//	    		}else if ( 0 == str.compareTo(MUSIC2ME)){
//	    			ret = true;
	    		}else if ( 0 == str.compareTo(JP_BASIC)){
	    			return true;
	    		}
	    		break;
	    	}
			return ret;
	}
	
	final MebookData createMebookData(final int type, final MebookHeader header){
		
		MebookData bookData = null;
		int encMode = header.mEncodeMode;
		
//		if ( Mebook.SY_PLAN_BOOK == type){
//			bookData = new SyPlanBook(encMode);
//		}else if ( Mebook.IMG_BOOK == type){
//			bookData = new ImgBook(encMode);
//		}else if ( Mebook.SY_BOOK == type){
			//String bookID = header.mBookID;
			
			//if ( 0 == bookID.compareTo(BASIC_UNICODE)){
				bookData = new BasicUnicode<SyItem>(encMode){
					public SyItem getT(){
						return new SyItem();
					}
				};			
//			} else if ( 0 == bookID.compareTo(DVD2ME) ||
//				 0 == bookID.compareTo(BOOK2ME) ||
//				 0 == bookID.compareTo(MUSIC2ME) ){
//				bookData = new Dvd2Me<SyItem>(encMode){
//					// Note: here must implement public SyItem getT()
//					// for tamplate class specific
//					public SyItem getT(){
//						return new SyItem();
//					}
//				};
//			}else if ( 0 == bookID.compareTo(JP_BASIC)){
//				bookData = new JpBasic<SyItem>(encMode){
//					public SyItem getT(){
//						return new SyItem();
//					}
//				};				
//			}else {
//				bookData = new SyImageBook<SyItem>(encMode){
//					public SyItem getT(){
//						return new SyItem();
//					}
//				};
//			}
//		}
		return bookData;
	}
	
	
	
	public void loadData(int type, final SyInputStream is,
			ArrayList<Long> headerPos, ArrayList<Long> dataSize, Mebook book)
			throws MebookException {

		MebookHeader header;
		MebookData bookData;
		long pos;
		long headerSize;

		switch (type) {
//		case Mebook.SY_PLAN_BOOK:
		case Mebook.SY_BOOK: {
			pos = headerPos.get(0);
			header = new MebookHeader();
			headerSize = header.load(is, pos);

			// use simple factory
			bookData = createMebookData(type, header);
			if (null == bookData) {
				throw new MebookException();
			}
			bookData.load(is, header.mDataPosition, (int) (dataSize
					.get(0) - headerSize));

			book.mHeader = header;
			book.mBookData = bookData;
		}
			break;

//		case Mebook.IMG_BOOK: {
//			pos = headerPos.get(0);
//			header = new MebookHeader();
//			headerSize = header.load(fileName, pos);
//
//			// use simple factory
//			bookData = createMebookData(Mebook.SY_PLAN_BOOK, header);
//			if (null == bookData) {
//				throw new MebookException();
//			}
//
//			bookData.load(fileName, header.mDataPosition, (int) (dataSize
//					.get(0) - headerSize));
//
//			book.mHeader = header;
//			SyPlanBook planBook = (SyPlanBook) bookData;
//
//			// /////////////////////////////////////////////////////////////////////////////////
//			pos = headerPos.get(1);
//			header = new MebookHeader();
//			headerSize = header.load(fileName, pos);
//
//			// use simple factory
//			bookData = createMebookData(Mebook.SY_BOOK, header);
//
//			if (null == bookData) {
//				throw new MebookException();
//			}
//
//			bookData.load(fileName, header.mDataPosition, (int) (dataSize
//					.get(1) - headerSize));
//
//			ImgBook imgBook;
//			imgBook = (ImgBook) createMebookData(Mebook.IMG_BOOK, header);
//
//			imgBook.attach(planBook, (SyImageBook<SyItem>) bookData);
//			book.mBookData = imgBook;
//		}
//			break;

		}
	}	
	
//	@SuppressWarnings("unchecked")
	public void loadData(int type, String fileName, ArrayList<Long> headerPos,
			ArrayList<Long> dataSize , Mebook book ) throws MebookException{
		
		MebookHeader header;
		MebookData bookData;
		long pos;
		long headerSize;
		
//		switch (type) {
//		case Mebook.SY_PLAN_BOOK:
//		case Mebook.SY_BOOK: {
			pos = headerPos.get(0);
			header = new MebookHeader();
			headerSize = header.load(fileName, pos);

			// use simple factory
			bookData = createMebookData(type, header);
			if (null == bookData) {
				throw new MebookException();
			}
			bookData.load(fileName, header.mDataPosition, (int) (dataSize
					.get(0) - headerSize));

			book.mHeader = header;
			book.mBookData=bookData;
//		}
//			break;
//
//		case Mebook.IMG_BOOK: {
//			pos = headerPos.get(0);
//			header = new MebookHeader();
//			headerSize = header.load(fileName, pos);
//
//			// use simple factory
//			bookData = createMebookData(Mebook.SY_PLAN_BOOK, header);
//			if (null == bookData) {
//				throw new MebookException();
//			}
//
//			bookData.load(fileName, header.mDataPosition, (int) (dataSize
//					.get(0) - headerSize));
//
//			book.mHeader = header ;
//			SyPlanBook planBook =  (SyPlanBook) bookData;
//			
//			// /////////////////////////////////////////////////////////////////////////////////
//			pos = headerPos.get(1);
//			header = new MebookHeader();
//			headerSize = header.load(fileName, pos);
//
//			// use simple factory
//			bookData = createMebookData(Mebook.SY_BOOK, header);
//			
//			if (null == bookData) {
//				throw new MebookException();
//			}
//
//			bookData.load(fileName, header.mDataPosition, (int) (dataSize
//					.get(1) - headerSize));
//
//			ImgBook imgBook;
//			imgBook = (ImgBook)createMebookData(Mebook.IMG_BOOK, header);
//			
//			
//			imgBook.attach(planBook, (SyImageBook<SyItem>)bookData);
//			book.mBookData = imgBook;
//		}
//			break;
//
//		}		
	}
}
