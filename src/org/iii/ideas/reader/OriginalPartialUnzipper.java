package org.iii.ideas.reader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import android.content.Context;
import android.util.Log;

/**
 * 解壓縮zip檔案的class，現在由於效能考量採用部分解壓縮，改由PartialUnzipper處理，現未使用
 * @author III
 * 
 */
public class OriginalPartialUnzipper{

	private File savedZipFile;
	private String zipDir;
	private String targetDir;
	private String opfPath;
	private String ncxPath;
	//private String tempDir;
	private HashMap<String,ZipEntry> map; 
	private ZipFile zf;
	private Context ctx;

    public OriginalPartialUnzipper(File zipFile,Context ctx_) {
    	//String filePath = zipFile.toString();
    	ctx=ctx_;
    	//zipDir = filePath.substring(0,filePath.lastIndexOf("/"));
    	//zipDir += File.separator +"." +ReaderLibrary.getFileName(filePath);
    	//zipDir="/sdcard/ebook/temp";
    	//zipDir="/.ebook/temp";
    	zipDir = ctx.getDir("temp", Context.MODE_PRIVATE).toString();
    	
    	new File(zipDir).mkdirs();
    	//Log.d("tARGETdIR",targetDir); 
    	savedZipFile=zipFile;
    	//savedZipFile = new File("/sdcard/Holmes111.epub");
    	//savedZipDir = savedZipPath.substring(0,savedZipPath.lastIndexOf("/")); 
    	//Log.d("ZIPDIR",savedZipDir);
    	try {
			zf = new ZipFile(savedZipFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("Unzipper:constructor",e.toString());
		}
		map = new HashMap<String,ZipEntry>();
    }
    
    /**
     * 
     * @return zip檔案路徑
     */
    public String getFilePath(){
    	return savedZipFile.getAbsolutePath();
    }
   
    @SuppressWarnings("unchecked")
    public void setList(){
		try{
			new File(zipDir).mkdirs();
			//Log.d("ZIP1",""+savedZipFile);
			Enumeration e = zf.getEntries();
			ZipEntry ze;
			//Log.d("ZIP2",""+savedZipFile);
			while(e.hasMoreElements()){
				ze=(ZipEntry)e.nextElement();
				String zeName=ze.getName();
				//Log.d("UNZIP",zeName);
				String outfName=zipDir+File.separator+zeName;
				map.put(outfName, ze);
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
						unzipFile(outfName);
					}
					if( zeName.lastIndexOf(".")>0 && zeName.substring(zeName.lastIndexOf(".")+1,zeName.length()).equalsIgnoreCase("ncx") ){
						//ncx_path=outfName;
						//Log.d("UNZIP","bbb"+zeName);
						ncxPath=outfName;
						unzipFile(outfName);
						//Log.d("NCX",ncx_path);
					}
					//unzipFile(outfName);
				//String outfName=nd+File.separator+filename+File.separator+ze.getName();
			}//end of while more element       
		}catch (Exception e) {
			Log.e("ZIPD",""+e);
		}    	
    }
    
    public void unzipFile(String outfName){
    	//Log.d("unzipFile","of:"+outfName);
    	//if(!(new File(outfName)).exists() || outfName.contains(".opf") || outfName.contains(".OPF")|| outfName.contains(".ncx")|| outfName.contains(".NCX")){
    	if(!(new File(outfName)).exists()){
        	try{
        		//Log.d("unzipfile",outfName);
        		ZipEntry ze = map.get(outfName);

            	File outf = new File(outfName);
            	
        		if( outfName.indexOf("/") != -1){	
                    File parDir=new File(outfName.substring(0,outfName.lastIndexOf("/") ));
                    parDir.mkdirs();
        		}
        		/*
        		if(zf==null)
        			Log.d("ZF","ZF is null");
        		else
        			Log.d("ZF","ZF is not null");
        		
        			Log.d("acd","1");
        			
            		if(ze==null){
            			Log.d("Ze","Ze is null");
            			Log.d("ZE",outfName);
            			}
            		else
            			Log.d("Ze","Ze is not null");
            	*/		
        			zf.getInputStream(ze);
        			//Log.d("acd","1.5");
        			InputStream in=zf.getInputStream(ze);
        			outf.createNewFile();
        			//Log.d("acd","2");
        			FileOutputStream out = new FileOutputStream(outf);
        			//Log.d("acd","3");
        			byte[] buffer = new byte[1024];
        			int len;
        	    
        			while((len = in.read(buffer)) >= 0){
        				out.write(buffer, 0, len);
        			}
        	    
        			in.close();
        			out.close();
        			
        	}catch(Exception e){
        		Log.e("Unzipper:unzipFile", e.toString());
        	}
    	}
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
    					Log.d("UNZIP",zeName);
    					String outfName=zipDir+File.separator+zeName;
    						if( zeName.lastIndexOf(".")>0 && zeName.substring(zeName.lastIndexOf(".")+1,zeName.length()).equalsIgnoreCase("opf") ){
    							if(zeName.lastIndexOf("/")>0){
    								targetDir=zipDir+File.separator+zeName.substring(0,zeName.lastIndexOf("/"))+File.separator;
    							}else{
    								targetDir=zipDir+File.separator;
    							}
    							//target_dir = savedZipDir+File.separator+zeName.substring(0,zeName.lastIndexOf("/"))+File.separator;
    							//opf_path=outfName;
    							Log.d("UNZIP","aaa"+zeName);
    							opfPath=outfName;
    							//Log.d("OPF",opf_path);
    						}
    						if( zeName.lastIndexOf(".")>0 && zeName.substring(zeName.lastIndexOf(".")+1,zeName.length()).equalsIgnoreCase("ncx") ){
    							//ncx_path=outfName;
    							Log.d("UNZIP","bbb"+zeName);
    							ncxPath=outfName;
    							//Log.d("NCX",ncx_path);
    						}
    					//String outfName=nd+File.separator+filename+File.separator+ze.getName();
    					
    					Log.d("ZIP",outfName);
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
    				Log.e("ZIPD",""+e);
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
    
    public String getZipDir(){
    	return zipDir;
    }

}
