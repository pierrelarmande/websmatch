package org.inria.websmatch.utils;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1Utils {

    public static byte[] getHash(String password)
	    throws NoSuchAlgorithmException {
	byte[] input = password.getBytes();
	MessageDigest digest = MessageDigest.getInstance("SHA-1");
	digest.update(input, 0, input.length);
	int hashLength = 20; // SHA-1 donne un hash de longueur 20
	byte[] hash = new byte[hashLength];
	try {
	    digest.digest(hash, 0, hashLength);
	} catch (DigestException e) {
	    L.Error(e.getMessage(),e);
	}
	return hash;
    }

    /**
     * Returns the specified bytes array hash as a String
     * 
     * @param hash
     *            the hash as a bytes array
     * @return the hash as a String
     */
    public static String hashToString(byte[] hash) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < hash.length; i++) {
	    int v = hash[i] & 0xFF;
	    if (v < 16) {
		sb.append("0");
	    }
	    sb.append(Integer.toString(v, 16));
	}
	return sb.toString();
    }

}
