package com.gsimedia.gsiebook;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Rect;
import android.util.Log;

import com.gsimedia.common.GSiDataSource;
import com.gsimedia.gsiebook.lib.FindResult;
import com.gsimedia.gsiebook.lib.MarkResult;
import com.gsimedia.gsiebook.lib.Outline;
import com.gsimedia.gsiebook.lib.PDFBox;




/**
 * Native PDF - interface to native code.
 */
public class PDF {
	static {
        System.loadLibrary("pdfcore");
	}
	
	/**
	 * Simple size class used in JNI to simplify parameter passing.
	 * This shouldn't be used anywhere outide of pdf-related code.
	 */
	public static class Size implements Cloneable {
		public int width;
		public int height;
		
		public Size() {
			this.width = 0;
			this.height = 0;
		}
		
		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		public Size clone() {
			return new Size(this.width, this.height);
		}
	}

	
	/**
	 * Holds pointer to native pdf_t struct.
	 */
	private int pdf_ptr = 0;

	/**
	 * Parse bytes as PDF file and store resulting pdf_t struct in pdf_ptr.
	 * @return error code
	 */
	synchronized private native int parseBytes(byte[] bytes);
	
	/**
	 * Parse PDF file.
	 * @param fileName pdf file name
	 * @return error code
	 */
	synchronized private native int parseFile(String fileName);
	
	
	/**
	 * Parse PDF file.
	 * @param fileName pdf file name
	 * @return error code
	 */
	synchronized private native int parseSAFile(InputStream sastream);
	
	/**
	 * Parse PDF file.
	 * @param fd opened file descriptor
	 * @return error code
	 */
	synchronized private native int parseFileDescriptor(FileDescriptor fd);

	/**
	 * Construct PDF structures from bytes stored in memory.
	 */
	public PDF(byte[] bytes) {
		this.parseBytes(bytes);
	}
	
	/**
	 * Construct PDF structures from file sitting on local filesystem.
	 */
	public PDF(File file) {
		this.parseFile(file.getAbsolutePath());
	}
	
	/**
	 * Construct PDF structures from file sitting on local filesystem.
	 */
	public PDF(InputStream sastream) {
		this.parseSAFile(sastream);
	}
	
	/**
	 * Construct PDF structures from opened file descriptor.
	 * @param file opened file descriptor
	 */
	public PDF(FileDescriptor file) {
		this.parseFileDescriptor(file);
	}
	
	/**
	 * Return page count from pdf_t struct.
	 */
	synchronized public native int getPageCount();
	
	/**
	 * Render a page.
	 * @param n page number, starting from 0
	 * @param zoom page size scalling
	 * @param left left edge
	 * @param right right edge
	 * @param passes requested size, used for size of resulting bitmap
	 * @return bytes of bitmap in Androids format
	 */
	synchronized public native int[] renderPage(int n, int zoom, int left, int top, int rotation, PDF.Size rect);
	
	/**
	 * Get PDF page size, store it in size struct, return error code.
	 * @param n 0-based page number
	 * @param size size struct that holds result
	 * @return error code
	 */
	synchronized public native int getPageSize(int n, PDF.Size size);
	
	/**
	 * Find text on given page, return list of find results.
	 */
	synchronized public native List<FindResult> find(String text, int page);
	
	/**
	 * get outlines
	 */
	synchronized public native List<Outline> getOutLine(Outline aOutline);
	
	/**
	 * get pagenum from outline
	 */
	synchronized public native int getLinkPage(Outline aOutline);
	
	/**
	 * get text from page 
	 */
	synchronized public native List<String> getTextFromPage(int page, boolean fullpage);
	
	/*reduce get marker from library*/
	/**
	 * get bbox from page 
	 */
	synchronized public native List<PDFBox> getAllBboxFromPage(int page);
	
	/**
	 * get user marker on given page, return list of marker results.
	 */
	synchronized public native MarkResult getMarker(int page, int x0,int y0, int x1, int y1);
	
	/**
	 * Free memory allocated in native code.
	 */
	synchronized private native void freeMemory();
	
	public void recycle() {
		Log.d("Lancelot", "PDF finalized");
		this.freeMemory();
		GSiDataSource.CloseFile();
	}
}
