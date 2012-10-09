/**
 * 
 */
package com.gsimedia.sa.GSiMediaRegisterProcess;

/**
 * @author user1
 *
 */
public class IllegalNetworkException extends Exception {
	
    private Throwable cause = null;
    
    /** Creates a new instance of GSISafeSAEx */
    public IllegalNetworkException() {
        super();
    }
    
    public IllegalNetworkException(final String msg){
        super(msg);
    }
    
    public IllegalNetworkException(final Throwable cause){
        super();
        this.cause = cause;
    }
    
    public IllegalNetworkException(final String msg, final Throwable cause){
        super(msg);
        this.cause = cause;
    }    
    
    public Throwable getCause(){
        return cause;
    }    
    
    public String getMessage(){
        if (super.getMessage() != null) {
            return super.getMessage();
        } else if (cause != null) {
            return cause.toString();
        } else {
            return "";
        }
    }  
}
