package org.inria.websmatch.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A collection of static methods related to data structures.
 * <p>
 * 
 * 
 * - - - - - - - - - - - - - - - - -
 * <p>
 * 
 * Copyright (c) 1998-2004 Paul Wheaton
 * <p>
 * 
 * You are welcome to do whatever you want to with this source file provided
 * that you maintain this comment fragment (between the dashed lines). Modify
 * it, change the package name, change the class name ... personal or business
 * use ... sell it, share it ... add a copyright for the portions you add ...
 * <p>
 * 
 * My goal in giving this away and maintaining the copyright is to hopefully
 * direct developers back to JavaRanch.
 * <p>
 * 
 * The original source can be found at <a
 * href=http://www.javaranch.com>JavaRanch</a>
 * <p>
 * 
 * - - - - - - - - - - - - - - - - -
 * <p>
 * 
 * @author Paul Wheaton
 *         <p>
 */

public class Structures {

    private Structures() {
    } // just to make sure nobody tries to instantiate this. <p>

    /**
     * Get a sub array from a byte array.
     * <P>
     * 
     * @param b
     *            The original array. It will not be modified.
     *            <p>
     * @param index
     *            Where the new array is to be copied from. If index is 0, the
     *            very first byte from b will be included. If first is beyond
     *            the last byte in b, this method will return null.
     *            <p>
     * @param length
     *            The number of bytes to be copied. If there are fewer than
     *            length bytes left, how ever many are left will be returned.
     *            <p>
     * @return A new byte array or null if index is greater than the last
     *         element of b.
     *         <p>
     */
    public static byte[] subArray(byte[] b, int index, int length) {
	byte[] returnValue = null;
	if (index < b.length) {
	    length = Math.min(length, b.length - index);
	    returnValue = new byte[length];
	    for (int i = 0; i < length; i++) {
		returnValue[i] = b[index + i];
	    }
	}
	return returnValue;
    }

    /**
     * Get a shallow sub array from an object array.
     * <P>
     * 
     * @param obj
     *            The original array. It will not be modified.
     *            <p>
     * @param index
     *            Where the new array is to be copied from. If index is 0, the
     *            very first object reference from obj will be included. If
     *            first is beyond the last object reference in obj, this method
     *            will return null.
     *            <p>
     * @param length
     *            The number of object references to be copied. If there are
     *            fewer than length references left, how ever many are left will
     *            be returned.
     *            <p>
     * @return A new object array or null if index is greater than the last
     *         element of obj.
     *         <p>
     */
    public static Object[] subArray(Object[] obj, int index, int length) {
	Object[] returnValue = null;
	if (index < obj.length) {
	    length = Math.min(length, obj.length - index);
	    returnValue = new Object[length];
	    for (int i = 0; i < length; i++) {
		returnValue[i] = obj[index + i];
	    }
	}
	return returnValue;
    }

    public static String[] stringToArray(String text) {
	String[] returnVal = null;
	if ((text != null) && (text.length() > 0)) {
	    StringTokenizer t = new StringTokenizer(text);
	    returnVal = new String[t.countTokens()];
	    for (int i = 0; i < returnVal.length; i++) {
		returnVal[i] = t.nextToken();
	    }
	}
	return returnVal;
    }
 
    /**
     * Given a space or newline delimited string, inner strings will be
     * extracted and moved to a Set.
     * <p>
     */
    public static Set<String> stringToSet(String text) {
	Set<String> returnVal = new HashSet<String>();
	String[] s = stringToArray(text);
	if (s != null) {
	    for (int i = 0; i < s.length; i++) {
		returnVal.add(s[i]);
	    }
	}
	return returnVal;
    }

    public static String[] listToStringArray(List<?> list) {
	String[] s = null;
	if (list.size() > 0) {
	    Object[] obj = list.toArray();
	    s = new String[obj.length];
	    for (int i = 0; i < s.length; i++) {
		s[i] = (String) obj[i];
	    }
	}
	return s;
    }

}
