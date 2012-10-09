package tw.com.soyong.mebook;

public class MebookException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3839266426180484420L;

	public MebookException() {
        super();
    }
    
    public MebookException(final String desc) {
        super(desc);
    }
}
