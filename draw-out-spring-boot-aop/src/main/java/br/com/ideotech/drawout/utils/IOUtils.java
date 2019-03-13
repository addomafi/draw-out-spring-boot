/**
 * Copyright 2019 Adauto Martins <adauto.martin@ideotech.com.br>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
