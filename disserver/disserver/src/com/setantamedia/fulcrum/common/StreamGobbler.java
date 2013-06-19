package com.setantamedia.fulcrum.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Used to ensure streamed data is processed when running things like Runtime.exec()
 * to ensure success completion of external process call
 * 
 * @author colinmanning
 *
 */
public class StreamGobbler extends Thread {

	InputStream is;
	String type;

	public StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while (br.readLine() != null) {				
				; // just gobble
			}
		} catch (IOException ioe)  {
			ioe.printStackTrace();  
		}
	}
}
