package org.iii.ideas.reader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.iii.ideas.android.general.AndroidLibrary;

import android.app.ProgressDialog;
import android.util.Log;

/**
 * 未使用，請參照PartialUnzipper
 * @author III
 * 
 */
public class Unzipper{

	public ProgressDialog proDlog;
	private File savedZipFile;
	private String zipDir;
	//private String savedZipPath;
	private String targetDir;
	private String opfPath;
	private String ncxPath;
	
    public Unzipper(File zipFile) {
    	String filePath = zipFile.toString();
    	zipDir = filePath.substring(0,filePath.lastIndexOf("/"));
    	zipDir += File.separator + AndroidLibrary.getFileName(filePath);
    	new File(zipDir).mkdirs();
    	//Log.d("tARGETdIR",targetDir);
    	savedZipFile=zipFile;
    	//savedZipFile = new File("/sdcard/Holmes111.epub");
    	//savedZipPath=savedZipFile.getAbsolutePath();
    	//savedZipDir = savedZipPath.substring(0,savedZipPath.lastIndexOf("/")); 
    	//Log.d("ZIPDIR",savedZipDir);
    }
    
    public String getFilePath(){
    	return savedZipFile.getAbsolutePath();
    }
   
    
    @SuppressWarnings("unchecked")
	public void unZip(){

    			try{
    				ZipFile zf = new ZipFile(savedZipFile);
    				//Log.d("ZIP1",""+savedZipFile);
    				Enumeration e = zf.getEntries();
    				ZipEntry ze = null;
    				//Log.d("ZIP2",""+savedZipFile);
    				while(e.hasMoreElements()){
    					ze=(ZipEntry)e.nextElement();
    					String zeName=ze.getName();
    					//Log.d("UNZIP",zeName);
    					String outfName=zipDir+File.separator+zeName;
    						if( zeName.lastIndexOf(".")>0 && zeName.substring(zeName.lastIndexOf(".")+1,zeName.length()).equalsIgnoreCase("opf") ){
    							if(zeName.lastIndexOf("/")>0){
    								targetDir=zipDir+File.separator+zeName.substring(0,zeName.lastIndexOf("/"))+File.separator;
    							}else{
    								targetDir=zipDir+File.separator;
    							}
    							//target_dir = savedZipDir+File.separator+zeName.substring(0,zeName.lastIndexOf("/"))+File.separator;
    							//opf_path=outfName;
    							//Log.d("UNZIP","aaa"+zeName);
    							opfPath=outfName;
    							//Log.d("OPF",opf_path);
    						}
    						if( zeName.lastIndexOf(".")>0 && zeName.substring(zeName.lastIndexOf(".")+1,zeName.length()).equalsIgnoreCase("ncx") ){
    							//ncx_path=outfName;
    							//Log.d("UNZIP","bbb"+zeName);
    							ncxPath=outfName;
    							//Log.d("NCX",ncx_path);
    						}
    					//String outfName=nd+File.separator+filename+File.separator+ze.getName();
    					
    					//Log.d("ZIP",outfName);
    					File outf = new File(outfName);
    					
    					if(ze.isDirectory()){
    						outf.mkdirs();
    						continue;
    					} 
    					if ( outfName.indexOf("/") != -1){
            			
    		            File parDir=new File(outfName.substring(0,outfName.lastIndexOf("/") ));
    		            parDir.mkdirs();
    					}

            		
    					InputStream in=zf.getInputStream(ze);
    					outf.createNewFile();
    					FileOutputStream out = new FileOutputStream(outf);

    					byte[] buffer = new byte[1024];
    					int len;
            	    
    					while((len = in.read(buffer)) >= 0){
    						out.write(buffer, 0, len);
    					}
            	    
    					in.close();
    					out.close();

    				}//end of while more element       
    			zf.close();
    			}catch (Exception e) {
    				Log.e("Unzipper",""+e);
    			}
    }
    
    public String getOpfPath(){
    	return opfPath;
    }
    
    public String getTargetDir(){
    	Log.d("TARGETDIR",targetDir);
    	return targetDir;
    }
    
    public String getNcxPath(){
    	return ncxPath;
    }
    


}
