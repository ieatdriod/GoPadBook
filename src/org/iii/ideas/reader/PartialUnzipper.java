package org.iii.ideas.reader;

import org.iii.ideas.android.general.HexWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.iii.ideas.reader.drm.NewDecipher;

import com.gsimedia.sa.DeviceIDException;

import android.content.Context;
import android.util.Log;

/**
 * 解壓縮zip檔案class，具備紀錄檔案列表和部分解壓縮功能
 * @author III
 * 
 */
public class PartialUnzipper{

	private File savedZipFile;
	private String zipDir;
	private String targetDir;
	private String epubPath;
	private String opfPath;
	private String ncxPath;
	//private String tempDir;
	private HashMap<String,ZipEntry> map; 
	private ZipFile zf;
	private Context ctx;
	/**
	 * constructor
	 * @param zipFile 欲處理的zip檔案
	 * @param ctx_ context，用來取得暫存內部資料夾
	 * @throws IOException if file not found
	 */
    public PartialUnzipper(File zipFile,Context ctx_) throws IOException {
    	epubPath = zipFile.toString();
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
    	//try {
		zf = new ZipFile(savedZipFile,"UTF-8");
		//} catch (IOException e) {
			// TODO Auto-generated catch block
		//	Log.e("Unzipper:constructor",e.toString());
		//}
		map = new HashMap<String,ZipEntry>();
    }
    

    /**
     * 取得zip檔案路徑
     * @return path
     */
    public String getFilePath(){
    	return savedZipFile.getAbsolutePath();
    }
   
    /**
     * 建立檔案對照表供後續解壓縮單一章節，並解壓縮opf和ncx兩個epub的metadata檔案。建立檔案對照表時也一同決定未來檔案的存放路徑，
     * 而檔案對照表為 檔案路徑(targetDir+opf所列的相對路徑) -> zip entry 的has hmap。
     * @throws Exception 建立清單不成功
     */
    @SuppressWarnings("unchecked")
    public void setList() throws DeviceIDException,Exception{
			new File(zipDir).mkdirs();
			//Log.d("ZIP1",""+savedZipFile);
			Enumeration e = zf.getEntries();
			ZipEntry ze;
			//Log.d("ZIP2",""+savedZipFile);
			while(e.hasMoreElements()){
				ze=(ZipEntry)e.nextElement();
				String zeName=ze.getName();
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
						//Log.d("UZ","opfpath:"+opfPath);
						unzipFile(outfName,true);
					}
					if( zeName.lastIndexOf(".")>0 && zeName.substring(zeName.lastIndexOf(".")+1,zeName.length()).equalsIgnoreCase("ncx") ){
						//ncx_path=outfName;
						//Log.d("UNZIP","bbb"+zeName);
						ncxPath=outfName;
						//Log.d("UZ","ncxpath:"+ncxPath);
						unzipFile(outfName,true);
						//Log.d("NCX",ncx_path);
					}
					//unzipFile(outfName);
				//String outfName=nd+File.separator+filename+File.separator+ze.getName();
			}//end of while more element       
	
    }
    
    /**
     * 解壓縮單一檔案，如檔案須解密(.twm)則會一同解密。註:傳入的路徑為targetDir+opf所列的相對路徑，故除非opf所列本身已有.twm否則"不含".twm的postfix
     * @param outfName 欲解壓縮的檔案，格式為targetDir+opf所列的相對路徑
     * @param shouldThrowException 是否要拋出exception讓caller來handle
     * @throws Exception 解壓縮失敗exception
     */
    public void unzipFile(String outfName, boolean shouldThrowException) throws DeviceIDException,Exception{
    	//Log.d("unzipFile","of:"+outfName);
    	//if(!(new File(outfName)).exists() || outfName.contains(".opf") || outfName.contains(".OPF")|| outfName.contains(".ncx")|| outfName.contains(".NCX")){
    	//Long a=System.currentTimeMillis();
		//Log.e("In unzipAndLoadChapter -- unzipFile", String.valueOf(System.currentTimeMillis()));
    	//outfName = URLDecoder.decode(outfName);
    	if(!(new File(outfName)).exists()){
        	try{
        		//Log.d("unzipfile","name:"+outfName.substring(outfName.lastIndexOf(File.separator)+1));
        		//Log.d("unzipfile","nameInByte:"+HexWriter.bytesToHex(outfName.substring(outfName.lastIndexOf(File.separator)+1).getBytes()));
        		ZipEntry ze = map.get(outfName);
        		Boolean shouldDecrypt=false;
        		if(ze==null){
        			//Log.d("set","shouldDecrypt");
        			shouldDecrypt=true;
        			outfName+=".twm";
        			ze=map.get(outfName);
        		}

            	File outf = new File(outfName);
            	
        		if( outfName.contains(File.separator)){	
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
        			//zf.getInputStream(ze);
        			//Log.d("acd","1.5");
        			
        		
        		if(shouldDecrypt){
        			//withBuffer:
        			InputStream in=zf.getInputStream(ze);
    				String originalName = outfName.substring(0, outfName.length()-4);//new:
    				NewDecipher.decryptChapterWithBuffer(ze.getSize(),in,originalName, epubPath, ctx);
        			
    				/*
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
        			String originalName = outfName.substring(0, outfName.length()-4);
        			NewDecipher.decryptChapter(originalName, epubPath, ctx);*/
    			}else{
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
    			}

        			
        	}catch(DeviceIDException e){
        		if(shouldThrowException)
        			throw(e);
        	}catch(Exception e){
        		Log.e("Unzipper:unzipFile", e.toString());
        		Log.e("Unzipper:unzipFile", "file name:"+outfName);
        		if(shouldThrowException)
        			throw(e);
        	}
    	}
		//Log.e("Done unzipAndLoadChapter--unzipFile", String.valueOf(System.currentTimeMillis()));
		//Log.e("unzipAndLoadChapter--unzipFile need ", String.valueOf(System.currentTimeMillis() - a));
    }
    
    /**
     * 同unzipFile，差別僅在不將檔案寫出而是將InputStream回傳，目前未使用(未測試可靠度)
     * @param outfName  欲解壓縮的檔案，格式為targetDir+opf所列的相對路徑
     * @param shouldThrowException 是否throw exception
     * @return input stream
     * @throws Exception 解壓縮失敗exception
     */
//    public InputStream getElementInputStream(String outfName, boolean shouldThrowException) throws Exception{
//    	Log.d("getElementInputStream","in");
//    	//Log.d("unzipFile","of:"+outfName);
//    	//if(!(new File(outfName)).exists() || outfName.contains(".opf") || outfName.contains(".OPF")|| outfName.contains(".ncx")|| outfName.contains(".NCX")){
//    	if(!(new File(outfName)).exists()){
//        	try{
//        		Log.d("getElementInputStream",outfName);
//        		ZipEntry ze = map.get(outfName);
//        		Boolean shouldDecrypt=false;
//        		if(ze==null){
//        			//Log.d("set","shouldDecrypt");
//        			shouldDecrypt=true;
//        			outfName+=".twm";
//        			ze=map.get(outfName);
//        		}
//
//            	File outf = new File(outfName);
//        		if( outfName.contains(File.separator)){	
//                    File parDir=new File(outfName.substring(0,outfName.lastIndexOf("/") ));
//                    parDir.mkdirs();
//        		}
//        			
//    			//zf.getInputStream(ze);
//    			//Log.d("acd","1.5");
//        		if(!shouldDecrypt){
//        			Log.d("return","zf stream");
//        			return zf.getInputStream(ze);
//        		}else{
//        			InputStream in= zf.getInputStream(ze);
//        			outf.createNewFile();
//        			FileOutputStream out = new FileOutputStream(outf);
//        			byte[] buffer = new byte[1024];
//        			int len;
//        	    
//        			while((len = in.read(buffer)) >= 0){
//        				out.write(buffer, 0, len);
//        			}
//        	    
//        			in.close();
//        			out.close();
//    				String originalName = outfName.substring(0, outfName.length()-4);
//        			return NewDecipher.getChapterInputStream(originalName, epubPath, ctx);
//        		}
//        	}catch(Exception e){
//        		Log.e("Unzipper:unzipFile", e.toString());
//        		Log.e("Unzipper:unzipFile", "file name:"+outfName);
//        		if(shouldThrowException)
//        			throw(e);
		//return null;
//        	}
//    	}else{
//    		return new FileInputStream(new File(outfName));
//    	}
//		//return null;
//    }

    /**
     * 解壓縮所有檔案，目前未使用
     */
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
    
    /**
     * 取得opf路徑
     * @return opf path
     */
    public String getOpfPath(){
    	return opfPath;
    }
    
    /**
     * 取得opf所在資料夾，作為相對路徑存取的base
     * @return directory of opf
     */
    public String getTargetDir(){
    	Log.d("TARGETDIR",targetDir);
    	return targetDir;
    }
    
    /**
     * 取得ncx路徑
     * @return ncx path
     */
    public String getNcxPath(){
    	return ncxPath;
    }
    
    /**
     * 取得zip檔案所在目錄路徑
     * @return zip檔案所在目錄路徑 
     */
    public String getZipDir(){
    	return zipDir;
    }

}
