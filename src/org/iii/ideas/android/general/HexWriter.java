package org.iii.ideas.android.general;

/**
 * HexWriter.java
 * Copyright (c) 2002 by Dr. Herong Yang
 * This program allows you to convert and data file to a new data 
 * in Hex format with 16 bytes (32 Hex digits) per line.
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 將byte寫成16進位字串
 * @author Dr. Herong Yang
 *
 */
public class HexWriter {
   private static final char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                             '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   public static void main(String[] a) {
      String inFile = a[0];
      String outFile = a[1];
      int bufSize = 16;
      byte[] buffer = new byte[bufSize];
      String crlf = System.getProperty("line.separator");
      try {
         FileInputStream in = new FileInputStream(inFile);
         OutputStreamWriter out = new OutputStreamWriter(
            new FileOutputStream(outFile));
         int n = in.read(buffer,0,bufSize);
	 String s = null;
         int count = 0;
         while (n!=-1) {
            count += n;
            s = bytesToHex(buffer,0,n);
            out.write(s);
            out.write(crlf);
            n = in.read(buffer,0,bufSize);
         }
         in.close();
         out.close();
         System.out.println("Number of input bytes: "+count);
      } catch (IOException e) {
         System.out.println(e.toString());
      }
   }
   
   /**
    * byte array轉成16進位字串
    * @param b byte array
    * @param off offset
    * @param len length
    * @return hex string
    */
   public static String bytesToHex(byte[] b, int off, int len) {
      StringBuffer buf = new StringBuffer();
      for (int j=0; j<len; j++)
         buf.append(byteToHex(b[off+j]));
      return buf.toString();
   }
   
   /**
    * byte array轉成16進位字串
    * @param b byte array
    * @return hex string
    */
   public static String bytesToHex(byte[] b) {
	      StringBuffer buf = new StringBuffer();
	      for (int j=0; j<b.length; j++)
	         buf.append(byteToHex(b[j]));
	      return buf.toString();
   }
   
   /**
    * 單一byte轉為hex string
    * @param b byte
    * @return hex string
    */
   public static String byteToHex(byte b) {
      char[] a = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
      return new String(a);
   }
}