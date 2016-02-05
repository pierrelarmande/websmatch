package org.inria.websmatch.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String convertDate(String dateStr){

	    DateFormat readFormat = new SimpleDateFormat( "EEE MMM dd hh:mm:ss zzz yyyy",Locale.US);
	    DateFormat writeFormat = new SimpleDateFormat( "dd/MM/yyyy");
	    Date date = null;
	    try {
	       date = readFormat.parse( dateStr );
	    } catch ( ParseException e ) {
	        L.Error(e.getMessage(),e);
	    }

	    String formattedDate = "";
	    if( date != null ) {
		formattedDate = writeFormat.format( date );
		return formattedDate;
	    }
	    
	    return null;
    }  
}
