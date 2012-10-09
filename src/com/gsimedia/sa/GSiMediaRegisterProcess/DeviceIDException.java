/**
 * 
 */
package com.gsimedia.sa.GSiMediaRegisterProcess;

/**
 * @author user1
 *
 */
public class DeviceIDException extends Exception {
    private Throwable cause = null;
    
    /** Creates a new instance of GSISafeSAEx */
    public DeviceIDException() {
        super();
    }
    
    public DeviceIDException(final String msg){
        super(msg);
    }
    
    public DeviceIDException(final Throwable cause){
        super();
        this.cause = cause;
    }
    
    public DeviceIDException(final String msg, final Throwable cause){
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
