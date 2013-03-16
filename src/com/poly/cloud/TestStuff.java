package com.poly.cloud;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.amazonaws.services.ec2.model.KeyPair;
import com.poly.cloud.virtualit.VirtualIT;
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
		System.out.println(Calendar.getInstance().getTime());
		User user = new User("User1");
		InstanceModel instanceModel = new InstanceModel();
		instanceModel.setAmiID("ami-a8f960c1");
		instanceModel.setElasticIP("54.225.192.227");
		instanceModel.setInstanceID("i-76bf671a");
		instanceModel.setInstanceType("t1.micro");
		KeyPair keyPair = new KeyPair();
		keyPair.setKeyName("User1Key");
		instanceModel.setKeyPair(keyPair);
		instanceModel.setPublicDNSAddress("ec2-54-225-192-227.compute-1.amazonaws.com");
		instanceModel.setRootInstance(true);
		instanceModel.setSecurityGroupName("User1GROUP");
		instanceModel.setSnapshotID("snap-4d923009");
		instanceModel.setSnapshotName("user1snap");
		instanceModel.setVolumeID(null);
		user.addInstance(instanceModel);
		
		VirtualIT.startUpInstance(user);
		
		
		
		
		
		
		
		
		
		//AWSUtils utils = new AWSUtils();		
		
	}

}
