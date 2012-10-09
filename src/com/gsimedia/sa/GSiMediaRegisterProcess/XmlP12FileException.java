/**
 * 
 */
package com.gsimedia.sa.GSiMediaRegisterProcess;

/**
 * @author user1
 *
 */
public class XmlP12FileException extends Exception {
    private Throwable cause = null;
    
    /** Creates a new instance of GSISafeSAEx */
    public XmlP12FileException() {
        super();
    }
    
    public XmlP12FileException(final String msg){
        super(msg);
    }
    
    public XmlP12FileException(final Throwable cause){
        super();
        this.cause = cause;
    }
    
    public XmlP12FileException(final String msg, final Throwable cause){
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
            return null;
        }
    }  
}