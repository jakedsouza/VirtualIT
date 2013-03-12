package com.poly.cloud;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.amazonaws.services.ec2.model.KeyPair;
import com.poly.cloud.virtualit.utils.AWSUtils;



public class TestStuff {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);  	
		
		AWSUtils instance = new AWSUtils() ;
	//	System.out.println(instance.createSecurityGroup("test name 3","test name 3"));
		//instance.createSecurityGroup("deleteme","deleteme");
	//	instance.createKeyPair("deleteme");
	//	String instanceid = instance.createNewInstance("deleteme","deleteme");
		instance.createImage("i-957480e7","img11");
		instance.createSnapshot("i-957480e7", "snap");
	}

}
