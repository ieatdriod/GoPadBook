/**
 * 
 */
package com.gsimedia.sa.GSiMediaRegisterProcess;

/**
 * @author user1
 *
 */
public class DataClass {

	public String resultCode_P12 ;
	
	public String resultCode_Domain;
	
	public String domain ;
	
	public String[] index ; 
	
	public String[] type ;
	

	void setData(final String resultCode_P12, final String resultCode_Domain,
			final String domain, final String[] index, final String[] type) {
		
		this. resultCode_P12 = resultCode_P12; 
		
		this. resultCode_Domain = resultCode_Domain;
		
		this.domain = domain;
		
		this.index = index; 
		
		this.type = type;
		
	}

}
