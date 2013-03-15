package com.poly.cloud;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.amazonaws.services.ec2.model.KeyPair;
import com.poly.cloud.virtualit.model.InstanceModel;
import com.poly.cloud.virtualit.model.User;
import com.poly.cloud.virtualit.utils.AWSUtils;

public class TestStuff {

	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME)
		.setLevel(Level.FINE);

		AWSUtils utils = new AWSUtils();		
		
	}

}
