package com.wlancat.worker;

import java.io.File;

import com.wlancat.data.CommandProto.Command;

public class InstallWorker extends PushWorker {

  public InstallWorker(Command command) {
    super(command);
  }

  @Override
  public boolean execute() {
    if (file == null || !getExtension(file).equals("apk"))
      return false;

    if (!super.execute())
      return false;

    if (listener != null)
      listener.onSuccess(); 
    return true;
  }

  public File getApkFile() {
    return file;
  }

  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1)
      ext = s.substring(i+1).toLowerCase();

    if (ext == null)
      return "";
    return ext;
  }
}
