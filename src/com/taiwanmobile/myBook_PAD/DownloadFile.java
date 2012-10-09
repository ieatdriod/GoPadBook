package com.taiwanmobile.myBook_PAD;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * 下載檔案
 * @author III
 * 
 */
public class DownloadFile extends Thread{

	protected String serverFile = "http://fs.mis.kuas.edu.tw/~m1095345122/";
	protected String location = "/sdcard/";
	protected long block = 20480; // 20K
	protected String tempFilePath = "travel.rar";   //temp
	protected int id;
	/**
	 * 宣告初始化
	 * @param sf 網址
	 * @param loc 存放位置
	 * @param tfp 檔名
	 * @param i id
	 */
	public DownloadFile(String sf,String loc,String tfp,int i){
		serverFile = sf;
		location = loc;
		tempFilePath = tfp;
		id = i;
		//testnetsize();
		//testdownload();
		//testredownload();		
	}
	/**
	 * 設定block
	 * @param b block
	 */
	public void setBlock(long b){
		block = b;
	}
	/**
	 * 設定網址
	 * @param sf 網址
	 */
	public void setServerFile(String sf){
		serverFile = sf;
	}
	/**
	 * 設定存放位置
	 * @param loc 存放位置
	 */
	public void setlocation(String loc){
		location = loc;
	}	
	/**
	 * 下載執行緒
	 */
	public void run(){
		try {
			File file = new File(location+tempFilePath);
			//System.out.println(file.length());	
			//System.out.println(testnetsize());	
			URL myURL = new URL(serverFile);
			
			HttpURLConnection httpConnection = (HttpURLConnection)myURL.openConnection(); 
			httpConnection.setRequestProperty("User-Agent","NetFox"); 
			if(file.length()>block){
				httpConnection.setRequestProperty("RANGE","bytes="+ (file.length()-block) + "-"+getServerFileSize());
			}else{
				httpConnection.setRequestProperty("RANGE","bytes=0-"+getServerFileSize());
			}			 
			
			InputStream input = httpConnection.getInputStream(); 
			
			//FileOutputStream fos = new FileOutputStream("/travel.rar");
			
			RandomAccessFile oSavedFile = new RandomAccessFile(location+tempFilePath,"rw");
			if(file.length()>block){
				oSavedFile.seek((file.length()-block));		
			}else{
				oSavedFile.seek(0);		
			}	
			byte[] buf = new byte[1024];
			//int i = 0;
			int currentRead = 0;
			//long a = System.currentTimeMillis();
			while( (currentRead = input.read(buf,0,1024)) > 0){ 
				oSavedFile.write(buf, 0, currentRead); 
	        } 
			//long b = System.currentTimeMillis();
			//System.out.println(b-a);	
			
			//System.out.println("download ok");
		} catch(Exception e) {
			//System.out.println(e.toString());
		}		
	}
	/**
	 * 不使用執行緒下載檔案
	 */
	public void getServerFile(){	
		try {
			URL myURL = new URL(serverFile+tempFilePath); 
			int tag = myURL.getFile().toString().lastIndexOf("/");
			InputStream conn = myURL.openStream();
			String fileName = myURL.getFile().toString().substring(tag + 1, myURL.getFile().toString().length());
			FileOutputStream fos = new FileOutputStream(location+fileName);
			byte[] buf = new byte[1024];
			int i = 0;
			//long a = System.currentTimeMillis();
			while (true) {
				i++;
				int bytesRead = conn.read(buf);
				if (bytesRead == -1)
					break;
				fos.write(buf, 0, bytesRead);
				//System.out.println(i);
				//if (i == 20)   20480 20K
					//break;				
			}
			//long b = System.currentTimeMillis();
			//System.out.println(b-a);
			//File file = new File("/"+fileName);
			//System.out.println(file.getName());	
			//System.out.println(file.length());	
			//System.out.println("download ok");
		} catch(Exception e) {
			System.out.println(e.toString());
		}		
	}
	/**
	 * 取得伺服器端檔案大小
	 */
	public long getServerFileSize(){
		//long nFileLength = 0;
		int nFileLength = 0;
		try{
			URL url = new URL(serverFile+tempFilePath); 
			HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection ();
			httpConnection.setRequestProperty("User-Agent","NetFox");
			nFileLength = httpConnection.getContentLength();
			httpConnection.disconnect();
/*			String sHeader;
			for(int i=1;;i++){
				sHeader=httpConnection.getHeaderFieldKey(i);
				if(sHeader!=null){
					if(sHeader.equals("Content-Length")){
						nFileLength = Integer.parseInt(httpConnection.getHeaderField(sHeader));
						break;
					}
				}else
					break;
			}	*/		
		}catch(Exception e){
			System.out.println(e.toString());
		}		
		//System.out.println(nFileLength);
		return (long)nFileLength;
		//return nFileLength;
	}		
}
