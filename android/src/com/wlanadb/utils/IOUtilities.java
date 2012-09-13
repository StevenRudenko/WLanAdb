package com.wlanadb.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class IOUtilities {
  private static final String TAG = IOUtilities.class.getSimpleName();

  public static final int FILE_BUFFER_SIZE = 4 * 1024;

  /**
   * Copy the content of the input stream into the output stream, using a
   * temporary byte array buffer whose size is defined by
   * {@link #FILE_BUFFER_SIZE}.
   * 
   * @param in
   *          The input stream to copy from.
   * @param out
   *          The output stream to copy to.
   * 
   * @throws java.io.IOException
   *           If any error occurs during the copy.
   */
  public static void copyFile(InputStream in, OutputStream out, long length) throws IOException {
    long totalRead = 0;
    byte[] b = new byte[FILE_BUFFER_SIZE];
    int read;
    while ((read = in.read(b)) > 0) {
      out.write(b, 0, read);
      out.flush();

      totalRead += read;
      if (totalRead >= length)
        break;
    }
  }

  /**
   * Closes the specified stream.
   * 
   * @param stream
   *          The stream to close.
   */
  public static void closeStream(Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {
        Log.e(TAG, "Could not close stream", e);
      }
    }
  }

  /**
   * Read the contents of an input stream into a String.
   * 
   * @param is
   *          - input stream of data
   * @return text representation of input stream
   * @throws IOException
   *           if failed to read data from input stream
   */
  public static String readString(InputStream is) throws IOException {
    BufferedReader ib = new BufferedReader(new InputStreamReader(is));
    StringBuffer sb = new StringBuffer();
    String temp = ib.readLine();
    while (temp != null) {
      sb.append(temp);
      sb.append("\n");
      temp = ib.readLine();
    }
    return sb.toString();
  }

  /**
   * Writes string into file.
   * 
   * @param file
   *          - file to write output
   * @param data
   *          - data to write
   * @return true if everything went okay. false - otherwise.
   */
  public static boolean writeToFile(File file, String data) {
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(file);
      os.write(data.getBytes());
      os.flush();
      closeStream(os);
      os = null;
      return true;
    } catch (FileNotFoundException e) {
      Log.e(TAG, "File not found", e);
    } catch (IOException e) {
      Log.e(TAG, "Could not close stream", e);
    } finally {
      closeStream(os);
    }
    return false;
  }

  /**
   * Reads files data.
   * 
   * @param file
   *          - file to read
   * @return byte array of file data
   */
  public static byte[] readFile(File file) {
    FileInputStream is = null;
    try {
      is = new FileInputStream(file);

      // Get the size of the file
      final long length = file.length();

      if (length > Integer.MAX_VALUE) {
        Log.w(TAG, "The file is too big");
        return null;
      }

      // Create the byte array to hold the data
      final byte[] bytes = new byte[(int) length];

      // Read in the bytes
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
          && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
        offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
        Log.e(TAG,
            "The file was not completely read: " + file.getAbsolutePath());
        return null;
      }

      closeStream(is);
      is = null;

      return bytes;
    } catch (IOException e) {
      Log.e(TAG, "Fail to read file: " + file.getAbsolutePath(), e);
    } finally {
      closeStream(is);
    }

    return null;
  }

  /**
   * Converts the image URI to the direct file system path of the image file.
   * @param context
   * @param contentUri
   * @return
   */
  public static String getRealPathFromUri(Context context, Uri contentUri) {
    String path = null;

    final String scheme = contentUri.getScheme();
    if (ContentResolver.SCHEME_FILE.equals(scheme)) {
      path = contentUri.getPath();
    } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
      Log.d(TAG, "Getting real path from URI: " + contentUri);
      String[] proj = { MediaStore.MediaColumns.DATA };
      Cursor cursor = context.getContentResolver().query(contentUri,
          proj, // Which columns to return
          null, // WHERE clause; which rows to return (all rows)
          null, // WHERE clause selection arguments (none)
          null); // Order-by clause (ascending by name)
      if (cursor == null)
        return null;
      final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
      if (cursor.moveToFirst()) {
        path = cursor.getString(index);
      }
      cursor.close();
    }
    return path;
  }

  /**
   * 
   * @param context
   * @param uri
   * @return
   * @throws FileNotFoundException
   */
  public static InputStream getStreamByUri(Context context, Uri uri) throws FileNotFoundException {
    if (uri == null)
      return null;
    final String scheme = uri.getScheme();
    if (ContentResolver.SCHEME_CONTENT.equals(scheme)
        || ContentResolver.SCHEME_FILE.equals(scheme)) {
      return context.getContentResolver().openInputStream(uri);
    }
    return null;
  }

  /**
   * Returns human readable value of bytes. Kb (KiB), Mb (MiB)...
   * @param bytes
   * @param si
   * @return
   */
  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

}
