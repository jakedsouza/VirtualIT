package com.poly.cloud.virtualit;

import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Logger;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.poly.cloud.virtualit.model.InstanceModel;
import com.poly.cloud.virtualit.model.User;
import com.poly.cloud.virtualit.utils.AWSUtils;

public class VirtualIT {
	private static final Logger log = Logger
			.getLogger(VirtualIT.class.getName());
	private static final String SECURITY_GROUP_NAME = "VirtualITSecurityGroup";
	private static final String KEY_NAME = "VirtualITKeyPair";
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		log.info("Creating two users ") ;
		
		User u1 = initializeUser("User1");
		User u2 = initializeUser("User2");	
		backupUser(u1);
		backupUser(u2);	
		log.info("First Day 2 users created ");		
	}
	
	
	public static User initializeUser(String name){
		
		log.info("Creating user " + name) ;		
		User u = new User(name);
		
		AWSUtils utils = new AWSUtils();
		
		String securityGroupName = u.getUsername() + "GROUP";
		String keyName = u.getUsername() + "Key";
		log.info("Creating first instance for user" );
		
		InstanceModel i = new InstanceModel();
		
		// Create security groups 
		String securityGroupID = utils.createSecurityGroup(securityGroupName, securityGroupName);
		
		// Create key pairs 
		KeyPair keyPair = utils.createKeyPair(keyName);
		
		// Create 2 instances 
		String instanceId = utils.createInstance(keyPair.getKeyName(),securityGroupName, null);
		
		// Wait till instances are in running state 
	//	utils.waitTillInstanceIsRunning(Arrays.asList(instanceId));
		
		// Allocate 2 new elastic IP's 
		String ip = utils.alocateIP();
		
		//Associate the 2 new elastic IP's with the instances 
		utils.associateIp(ip,instanceId);
		
		// Generating cpu load 
		while(!utils.generateLoad(instanceId, ip, keyPair.getKeyName(),true));
				
		Instance awsInstance = utils.getInstanceInformation(instanceId);
			
		i.setAmiID(awsInstance.getImageId());
		i.setElasticIP(awsInstance.getPublicIpAddress());
		i.setInstanceID(awsInstance.getInstanceId());
		i.setInstanceType(awsInstance.getInstanceType());
		i.setKeyPair(keyPair);
		i.setPublicDNSAddress(awsInstance.getPublicDnsName());
		i.setRootInstance(true);
		i.setSecurityGroupName(securityGroupName);		
		u.addInstance(i);
	//	u2.addInstance(i2);
		
		log.info("User created and instance initialised with elastic ip");
		return u ;
	}
	
	public static User backupUser(User u ){
		log.info("Shutting down user " + u.getUsername() + " instances ");
		AWSUtils utils = new AWSUtils();
		
		InstanceModel instanceModel = u.getInstances().get(0);
		String instanceId = instanceModel.getInstanceID() ;
		log.info("Stopping instance " + instanceId );
		utils.stopInstance(instanceId);
		
		log.info("Detaching root volume from user");
		String volumeId = utils.getVolumeIDFromInstanceID(instanceId);
		utils.detachVolumeFromInstance(volumeId, instanceId);
		
		log.info("Creating AMI of instance ") ;
		String imageId = utils.createImage(instanceId, "ami_" + u.getUsername()+"_" + Calendar.getInstance());
		log.info("Ami Created ") ;
		
		log.info("Creating Snapshot of volume " + volumeId ) ;
		String snapshotName = "snap_"+ u.getUsername()+"_" + Calendar.getInstance() ;
		String snapShotId = utils.createSnapshotFromVolumeID(volumeId,snapshotName);
		log.info("Snapshot of created" ) ;
		
		log.info("Terminating instance " + instanceId);
		utils.deleteInstance(instanceId);
		log.info("Instance terminated " ) ;
		instanceModel.setSnapshotID(snapShotId);
		instanceModel.setSnapshotName(snapshotName);
		instanceModel.setVolumeID(volumeId);
		instanceModel.setAmiID(imageId);
		u.removeInstance(instanceModel);
		u.addInstance(instanceModel);
		return u;		
	}
	
	

}
