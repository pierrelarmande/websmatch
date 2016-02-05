package org.inria.websmatch.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

public class L {

    final static Logger log = Logger.getLogger(L.class.getName());

    private static final boolean _DEBUG = false;
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());

    }

    public static void Debug(String className, String s,boolean withDate){
        if(_DEBUG && withDate) log.debug(now()+" "+className+" : "+ s);
    }

    public static void Debug(Object obj, String s,boolean withDate){
        String className = obj.getClass().getSimpleName();
        Debug(className, s, withDate);
    }

    public static void Error(String s, Exception e){
        log.error(s, e);
    }
}
