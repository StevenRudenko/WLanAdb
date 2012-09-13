package com.wlanadb.worker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Environment;
import android.util.Log;

import com.wlanadb.config.MyConfig;
import com.wlanadb.data.CommandProto.Command;
import com.wlanadb.utils.HashHelper;
import com.wlanadb.utils.IOUtilities;

public class PushWorker extends BaseWorker {
  private static final String TAG = PushWorker.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  protected final File file;

  private InputStream in;

  /*TODO: Write the image to disk first. Then try to open sockets 
   * in parallel each transferring a different offset of the
   * image (e.g. split the image to 3 jobs, transfer each in
   * parallel to the others). This will workaround some TCP
   * behaviors.
   */

  public PushWorker(Command command) {
    super(command);

    final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    final String inPath = command.getParams(0);

    final File inFile = new File(inPath);
    file = new File(downloads, inFile.getName());

    final int count = command.getParamsCount();
    if (count == 0) {
      if (DEBUG)
        Log.v(TAG, "No parameters were set");
    } else {
      if (DEBUG)
        Log.v(TAG, "There were " + count + " parameters set");
    }

    for (int i=0; i<count; ++i) {
      final String param = command.getParams(i);
      if (DEBUG)
        Log.v(TAG, "" + i + " parameter: " + param);
    }
  }

  @Override
  public void setInputStream(InputStream in) {
    super.setInputStream(in);
    this.in = in;
  }

  @Override
  public boolean execute() {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      listener.onError();
    }

    OutputStream fout = null;
    try {
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        file.createNewFile();
      }

      fout = new DigestOutputStream(new FileOutputStream(file), md);
      IOUtilities.copyFile(in, fout, command.getLength());
      if (DEBUG)
        Log.d(TAG, "File was saved to " + file.getAbsolutePath() + " [" + file.length() + "]");
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to write file data to stream", e);
      listener.onError();
      return false;
    } finally {
      IOUtilities.closeStream(fout);
    }

    if (md != null) {
      final String checksum = HashHelper.convertToHex(md.digest());
      if (MyConfig.DEBUG)
        Log.d(TAG, "Files checksums in <> out: " + command.getChecksum() + " <> " + checksum);

      if (!checksum.equals(command.getChecksum())) {
        Log.e(TAG, "File is corrupted!");
        return false;
      }

      return true;
    }

    return false;
  }

  @Override
  public void terminate() {
  }
}
