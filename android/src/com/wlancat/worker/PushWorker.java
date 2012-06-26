package com.wlancat.worker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;

import com.wlancat.data.CommandProto.Command;
import com.wlancat.utils.IOUtilities;

public class PushWorker extends BaseWorker {
  private static final String TAG = PushWorker.class.getSimpleName();
  private static final boolean DEBUG = true;

  private static final int IO_BUFFER_SIZE = 64 * 1024;

  private final InputStream in;
  private final File file;

  public PushWorker(Command command, InputStream in, OutputStream out, WorkerListener listener) {
    super(command, in, out, listener);

    this.in = new BufferedInputStream(in, IO_BUFFER_SIZE);

    final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    final String inPath = command.getParams(0);

    final File inFile = new File(inPath);
    file = new File(downloads, inFile.getName());
  }

  @Override
  public void start() {
    OutputStream fout = null;
    try {
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        file.createNewFile();
      }

      fout = new FileOutputStream(file);
      IOUtilities.copy(in, fout);
      if (DEBUG)
        Log.d(TAG, "File was saved to " + file.getAbsolutePath());
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to write file data to stream", e);
    } finally {
      IOUtilities.closeStream(fout);
      listener.onError();
    }
  }

  @Override
  public void stop() {
  }

  
}
