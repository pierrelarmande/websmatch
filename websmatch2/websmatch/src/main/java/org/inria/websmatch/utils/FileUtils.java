package org.inria.websmatch.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUtils {

    /**
     * Copy a File
     * 
     * @param in
     * @param out
     * @throws IOException
     */

    public static void copyFile(File in, File out) throws IOException {
	FileChannel inChannel = new FileInputStream(in).getChannel();
	FileChannel outChannel = new FileOutputStream(out).getChannel();
	try {
	    inChannel.transferTo(0, inChannel.size(), outChannel);
	} catch (IOException e) {
	    throw e;
	} finally {
	    if (inChannel != null)
		inChannel.close();
	    if (outChannel != null)
		outChannel.close();
	}
    }

    /**
     * Move a file
     * 
     * @param in
     * @param out
     * @throws IOException
     */

    public static void moveFile(File in, File out) throws IOException {
	copyFile(in, out);
	in.delete();
    }
    
    public static byte[] loadFile(String fileCompleteURI) {
	File file = new File(fileCompleteURI);
	int size = (int) file.length();
	byte[] buffer = new byte[size];
	FileInputStream in;
	try {
	    in = new FileInputStream(file);
	    in.read(buffer);
		in.close();
	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(),e);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	return buffer;
    }
    
    public static File[] randomizeFiles(File[] files){
	
	//
	List<File> list = Arrays.asList(files);
	Collections.shuffle(list);
	//
	
	return list.toArray(new File[list.size()]);
    }

}
