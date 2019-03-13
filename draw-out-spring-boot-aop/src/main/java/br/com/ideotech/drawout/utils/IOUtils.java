package br.com.ideotech.drawout.utils;

import java.io.IOException;
import java.io.Reader;

public class IOUtils {

	public static byte[] toByteArray(Reader reader) throws IOException {
		char[] charArray = new char[8 * 1024];
	    StringBuilder builder = new StringBuilder();
	    int numCharsRead;
	    while ((numCharsRead = reader.read(charArray, 0, charArray.length)) != -1) {
	        builder.append(charArray, 0, numCharsRead);
	    }
	    byte[] targetArray = builder.toString().getBytes();
	 
	    reader.close();
	    
	    return targetArray;
	}
	
	public static String toString(Reader reader) throws IOException {
		return new String(toByteArray(reader));
	}
}
