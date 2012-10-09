package org.iii.ideas.reader;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 *  用來解決webview無法讀取裝置上檔案(/data/data/...)的問題，現未使用
 * @author III
 *
 */
public class LocalFileContentProvider extends ContentProvider {
	////content provider，
   private static final String URI_PREFIX = "content://org.iii.ideas";
   
   public static String constructUri(String url) {
       Uri uri = Uri.parse(url);
       return uri.isAbsolute() ? url : URI_PREFIX + url;
   }	
   
   @Override
   public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
       //String path = uri.getPath();
       //if(path.substring(0, ))
	   Log.d("ContentProvider","URI:"+uri.toString());
	   File file = new File(uri.getPath());
       ParcelFileDescriptor parcel = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
       return parcel;
   }

   @Override
   public boolean onCreate() {
       return true;
   }

   @Override
   public int delete(Uri uri, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public String getType(Uri uri) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Uri insert(Uri uri, ContentValues contentvalues) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

}
