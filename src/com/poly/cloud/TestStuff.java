package com.poly.cloud;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;



public class TestStuff {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);  	
		
		AWSUtils instance = new AWSUtils() ;
		instance.createSecurityGroup("test name 2","test name 2");
		instance.createKeyPair("test name 2");
		System.out.println(instance.createNewInstance("test name 2", "test name 2"));
		
		
	}

}
