package org.inria.websmatch.utils;

import java.text.Normalizer;
import java.util.Random;

public class StringUtils {

    /**
     * This method is used to clean strings for DSPL export, what it does :
     * Replace spaces by underscore for characters Replace comma by dot to be US
     * standard Replace backquote by _ Replace (); by underscore
     * 
     * @param s
     *            The string to clean
     * @return The cleaned string
     */

    public static String cleanString(String s) {

	String normalized = new String();

	// remove the thousand separator
	normalized = s.replaceAll("\\xA0", "");

	// Normalize name
	normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
	normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	normalized = normalized.toLowerCase();
	normalized = normalized.replaceAll("[^a-z]", " ");
	normalized = normalized.replaceAll("\\+s", "");

	// System.out.println("Clean header : "+s);

	/*
	 * String cleaned = s.trim();
	 * 
	 * // unaccent String temp = Normalizer.normalize(s,
	 * Normalizer.Form.NFD); Pattern pattern =
	 * Pattern.compile("\\p{InCombiningDiacriticalMarks}+"); cleaned =
	 * pattern.matcher(temp).replaceAll("");
	 * 
	 * cleaned = cleaned.replaceAll("/", "_"); cleaned =
	 * cleaned.replaceAll("\\s+", "_"); cleaned = cleaned.replaceAll(",",
	 * "."); cleaned = cleaned.replaceAll("\\'", "_"); cleaned =
	 * cleaned.replaceAll("\\(", "_"); cleaned = cleaned.replaceAll("\\)",
	 * "_"); cleaned = cleaned.replaceAll(";", "_"); cleaned =
	 * cleaned.replaceAll("%", ""); // remove the thousand separator cleaned
	 * = cleaned.replaceAll("\\xA0",""); cleaned =
	 * cleaned.replaceAll("-","_");
	 * 
	 * return cleaned;
	 */
	return normalized;
    }

    public static String randomString() {
	char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	StringBuilder sb = new StringBuilder();
	Random random = new Random();
	for (int i = 0; i < 20; i++) {
	    char c = chars[random.nextInt(chars.length)];
	    sb.append(c);
	}
	String output = sb.toString();
	return output;
    }
}
