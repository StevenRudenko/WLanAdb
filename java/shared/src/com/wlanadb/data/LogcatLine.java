package com.wlanadb.data;

public class LogcatLine {
    public static final String TYPE_V = "V";
    public static final String TYPE_D = "D";
    public static final String TYPE_I = "I";
    public static final String TYPE_W = "W";
    public static final String TYPE_E = "E";

    public String type;
    public String tag;
    public int pid;
    public String text;

    public String full;

    private Object tagObject;

    public boolean isValid() {
        return type != null;
    }

    public boolean isWarning() {
        return TYPE_W.equals(type) || TYPE_E.equals(type);
    }

    public Object getTagObject() {
        return tagObject;
    }

    public void setTagObject(Object o) {
        tagObject = o;

    }
}
