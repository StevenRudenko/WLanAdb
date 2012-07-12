package com.wlancat.worker;

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

import com.wlancat.config.MyConfig;
import com.wlancat.data.CommandProto.Command;
import com.wlancat.utils.HashHelper;
import com.wlancat.utils.IOUtilities;

public class PushWorker extends BaseWorker {
  private static final String TAG = PushWorker.class.getSimpleName();
  private static final boolean DEBUG = true;

  private final InputStream in;
  private final File file;

  public PushWorker(Command command, InputStream in, OutputStream out, WorkerListener listener) {
    super(command, in, out, listener);

    this.in = in;

    final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    final String inPath = command.getParams(0);

    final File inFile = new File(inPath);
    file = new File(downloads, inFile.getName());
  }

  @Override
  public void start() {
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
    } finally {
      IOUtilities.closeStream(fout);
    }

    if (md != null) {
      final String checksum = HashHelper.convertToHex(md.digest());
      if (MyConfig.DEBUG)
        Log.d(TAG, "Files checksums in <> out: " + command.getChecksum() + " <> " + checksum);

      if (!checksum.equals(command.getChecksum()))
        Log.e(TAG, "File is corrupted!");
    }
  }

  @Override
  public void stop() {
  }
}
