package com.poly.cloud.virtualit.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;

public class AWSUtils {
	private static final Logger log = Logger
			.getLogger(AWSUtils.class.getName());
	static final String SECURITY_GROUP_NAME = "Assignment1SecurityGroup";
	static final String SECURITY_GROUP_DESCRIPTION = "Security group for assignment 1";
	private static final String KEY_PAIR_NAME = "Assignment1KeyPair";
	private static final String ALL_ACCESS_IP_RANGE = "0.0.0.0/0";
	private static final String DEFAULT_IMAGE_ID = "ami-76f0061f";

	private static AmazonEC2 ec2;
	AWSCredentials credentials = null;

	public AWSUtils() {
		init();
	}

	public void init() {
		if (ec2 != null) {
			return;
		} else {
			try {
				log.info("Loading Credentials file ");
				credentials = new PropertiesCredentials(
						AWSUtils.class
						.getResourceAsStream("/AwsCredentials.properties"));
			} catch (IOException e) {
				log.info("Error Loading credentials file");
				e.printStackTrace();
			}
			ec2 = new AmazonEC2Client(credentials);
			log.info("Credentials file loaded");

		}
	}

	/**
	 * Creates a new security group. The method first checks if the same
	 * security group exists . If it does exist , then we just add our ingress
	 * request to it . If the group does not exist , a new one is created and
	 * the ingress ports are added . For this assignment , all ports are opened
	 * from 0 to 65535 for all IP ranges.
	 * 
	 * @param securityGroupName
	 * @param securityGroupDescription
	 * @return securityGroupID The ID of the security group created or the id of
	 *         the existing security group
	 */
	public String createSecurityGroup(String securityGroupName,
			String securityGroupDescription) {
		log.info("Creating new security group with name " + securityGroupName);

		// init();

		String createdSecurityGroupID = null;
		boolean securityGroupExists = false;

		// get existing security groups
		List<SecurityGroup> existingSecurityGroupsResult = ec2
				.describeSecurityGroups().getSecurityGroups();
		// check if the group exists and if it does , get its ID
		for (SecurityGroup securityGroup : existingSecurityGroupsResult) {
			if (securityGroup.getGroupName().equals(securityGroupName)) {
				securityGroupExists = true;
				createdSecurityGroupID = securityGroup.getGroupId();
			}
		}

		// if group does not exist then create a new one
		if (!securityGroupExists) {
			CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest(
					securityGroupName, securityGroupDescription);
			CreateSecurityGroupResult createSecurityGroupResult = ec2
					.createSecurityGroup(createSecurityGroupRequest);
			createdSecurityGroupID = createSecurityGroupResult.getGroupId();
			AuthorizeSecurityGroupIngressRequest ingressRequest = new AuthorizeSecurityGroupIngressRequest()
			.withIpPermissions(
					new IpPermission().withIpProtocol("tcp")
					.withFromPort(0).withToPort(65535)
					.withIpRanges(ALL_ACCESS_IP_RANGE))
					.withGroupName(securityGroupName);
			ec2.authorizeSecurityGroupIngress(ingressRequest);
			log.info("security group " + securityGroupName
					+ " created successfully");
		} else {
			log.info("security group "
					+ securityGroupName
					+ " already exists not creating new group will return existing security group id ");
		}
		// return the group id
		return createdSecurityGroupID;
	}

	/**
	 * Creates a new key pair and also creates the private key file . If the key
	 * pair exists , nothing is done and returns null
	 * 
	 * @param keyName
	 * @return {@link KeyPair} the keyPair created null if the key pair already
	 *         exists .
	 * 
	 *         TODO may need to delete keypair if it already exists or may be
	 *         rename it . not sure
	 */
	public KeyPair createKeyPair(String keyName) {
		// init();
		KeyPair keyPair = null;
		log.info("Creating a new keyPair with name " + keyName);
		boolean keyPairExists = false;
		List<KeyPairInfo> existingKeyPairs = ec2.describeKeyPairs()
				.getKeyPairs();
		for (KeyPairInfo keyPairInfo : existingKeyPairs) {
			if (keyPairInfo.getKeyName().equals(keyName)) {
				keyPairExists = true;
			}
		}
		if (!keyPairExists) {
			CreateKeyPairRequest keyPairRequest = new CreateKeyPairRequest()
			.withKeyName(keyName);
			keyPair = ec2.createKeyPair(keyPairRequest).getKeyPair();

			Path target = Paths.get(keyName);
			FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
					.asFileAttribute(PosixFilePermissions
							.fromString("rw-------"));
			try {
				Files.deleteIfExists(target);
				Files.createFile(target, attr);
				BufferedWriter bufferedWriter;
				bufferedWriter = new BufferedWriter(new FileWriter(new File(
						keyName)));
				bufferedWriter.write(keyPair.getKeyMaterial());
				bufferedWriter.flush();
				bufferedWriter.close();
			} catch (IOException e) {
				log.severe("Error writing key file");
				log.throwing(AWSUtils.class.getName(), "CreateKeyPair", e);
			}
			log.info("Key Pair " + keyName + " Created Successfully");

		} else {
			log.info("Key pair " + keyName
					+ " already exists not creating new one");
		}
		return keyPair;
	}

	// ---------------------Instance related code ----------------------------
	/**
	 * Creates an instance . If imageID is null , a new instance is created with
	 * a default ami .
	 * 
	 * @param keyName
	 * @param securityGroupName
	 * @param imageID
	 * @return instanceID the instanceID of the instance created.
	 */
	public String createInstance(String keyName, String securityGroupName,
			String imageID) {
		// init();
		log.info("Creating instance");
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.setInstanceType(InstanceType.T1Micro);
		runInstancesRequest.setMinCount(1);
		runInstancesRequest.setMaxCount(1);

		if (imageID == null) {
			imageID = DEFAULT_IMAGE_ID;
			runInstancesRequest.setImageId(imageID);
		}
		if (keyName != null) {
			runInstancesRequest.setKeyName(keyName);
		}
		if (securityGroupName == null) {
			runInstancesRequest.setSecurityGroups(Arrays
					.asList(securityGroupName));
		}
		RunInstancesResult result = ec2.runInstances(runInstancesRequest);
		String instanceID = result.getReservation().getInstances().get(0)
				.getInstanceId();
		log.info("Done creating new instance : " + instanceID);
		return instanceID;
	}

	/**
	 * Deletes the instance with the given instance id
	 * 
	 * @param instanceId
	 */
	public void deleteInstance(String instanceId) {
		// init();
		log.info("Terminating instance " + instanceId);
		ec2.terminateInstances(new TerminateInstancesRequest()
		.withInstanceIds(instanceId));
		log.info("Done terminating instance " + instanceId);
	}

	/**
	 * Stops the instance with the given instance ID
	 * 
	 * @param instanceId
	 */
	public void stopInstance(String instanceId) {
		// init();
		log.info("Stopping instance " + instanceId);
		ec2.stopInstances(new StopInstancesRequest()
		.withInstanceIds(instanceId));
		log.info("Done stopping instance " + instanceId);
	}

	/**
	 * Starts the instance with the given instance ID
	 * 
	 * @param instanceId
	 */
	public void startInstance(String instanceId) {
		// init();
		log.info("Starting instance " + instanceId);
		ec2.startInstances(new StartInstancesRequest()
		.withInstanceIds(instanceId));
		log.info("Done starting instance " + instanceId);
	}

	// ----------------------------Image and snapshot related code
	// --------------------------
	/**
	 * Creates an EBS backed image of an instance
	 * 
	 * @param instanceId
	 *            The instance id of which the image is to be made
	 * @param name
	 *            The Name of the image
	 * @return the new imageID of the created image
	 */
	public String createImage(String instanceId, String name) {
		// init();
		log.info("Creating new image of instance " + instanceId
				+ " with image name : " + name);
		CreateImageRequest request = new CreateImageRequest();
		request.setInstanceId(instanceId);
		request.setName(name);
		CreateImageResult result = ec2.createImage(request);
		log.info("Done creating new image of instance " + instanceId
				+ " with image name : " + name);
		return result.getImageId();
	}

	/**
	 * Creates a snapshot of the root volume of a specific instance
	 * 
	 * @param instanceID
	 * @param description
	 * @return the snapshot ID of the snapshot
	 */
	public String createSnapshotFromInstanceID(String instanceID,
			String description) {
		String volumeId = getVolumeIDFromInstanceID(instanceID);
		String snapshotID = createSnapshotFromVolumeID(volumeId, description);
		return snapshotID;
	}

	/**
	 * Creates a snapshot of a specific volume given the volumeID
	 * 
	 * @param volumeID
	 * @param description
	 * @return the snapshot ID of the snapshot
	 */
	public String createSnapshotFromVolumeID(String volumeID, String description) {
		// init();
		log.info("Creating new snapshot of volume " + volumeID);
		CreateSnapshotRequest request = new CreateSnapshotRequest();
		request.setVolumeId(volumeID);
		request.setDescription(description);
		CreateSnapshotResult result = ec2.createSnapshot(request);
		log.info("Done creating new snapshot of volume " + volumeID);
		return result.getSnapshot().getSnapshotId();
	}

	public void deleteImage(String imageID, boolean deleteSnapshot) {
		// Get the image from imageID
		// init();
		log.info("Deleting image " + imageID);
		DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest()
		.withImageIds(imageID);
		DescribeImagesResult describeImagesResult = ec2
				.describeImages(describeImagesRequest);
		Image image = describeImagesResult.getImages().get(0);
		ec2.deregisterImage(new DeregisterImageRequest(imageID));
		// Get the snapshotID
		if (deleteSnapshot) {
			String snapshotID = image.getBlockDeviceMappings().get(0).getEbs()
					.getSnapshotId();
			deleteSnapShot(snapshotID);
		}
		log.info("Done image " + imageID);
	}

	public void deleteSnapShot(String snapshotID) {
		// init();
		log.info("Deleting snapshot " + snapshotID);
		DeleteSnapshotRequest request = new DeleteSnapshotRequest(snapshotID);
		ec2.deleteSnapshot(request);
		log.info("Done deleting snapshot " + snapshotID);
	}

	public String createNewVolume(){
		CreateVolumeRequest cvr = new CreateVolumeRequest();
        cvr.setAvailabilityZone("us-east-1a");
        cvr.setSize(10); //size = 10 gigabytes
    	CreateVolumeResult volumeResult = ec2.createVolume(cvr);
    	String createdVolumeId = volumeResult.getVolume().getVolumeId();     
    	return createdVolumeId;
	}
	public void attachVolumeToInstance(String volumeId, String instanceId,
			String devicePath) {
		log.info("Attaching volume " + volumeId + "to instance " + instanceId);
		AttachVolumeRequest avr = new AttachVolumeRequest();
		avr.setVolumeId(volumeId);
		avr.setInstanceId(instanceId);
		avr.setDevice(devicePath);
		ec2.attachVolume(avr);
		log.info("Done attaching volume " + volumeId + "to instance "
				+ instanceId);
	}

	public void detachVolumeFromInstance(String volumeId, String instanceId) {
		log.info("Detattaching volume " + volumeId + "to instance "
				+ instanceId);
		DetachVolumeRequest dvr = new DetachVolumeRequest();
		dvr.setVolumeId(volumeId);
		dvr.setInstanceId(instanceId);
		ec2.detachVolume(dvr);
		log.info("Done detattaching volume " + volumeId + "to instance "
				+ instanceId);
	}

	public void generateLoad(String isntanceID) {
		// TODO
	}

	public String getVolumeIDFromInstanceID(String instanceID) {
		List<Volume> volumes = ec2.describeVolumes().getVolumes();
		String volumeID = null;
		for (Volume volume : volumes) {
			List<VolumeAttachment> attachments = volume.getAttachments();
			for (VolumeAttachment volumeAttachment : attachments) {
				if (volumeAttachment.getInstanceId().equals(instanceID)) {
					volumeID = volumeAttachment.getVolumeId();
				}
			}
		}
		return volumeID;
	}

	// TODO Mihir
	public void getInstanceMetrics(String instanceID) {

	}

	// Elastic IP related code

	public String alocateIP() {
		log.info("Allocating new Ip address");
		AllocateAddressResult elasticResult = ec2.allocateAddress();
		String elasticIp = elasticResult.getPublicIp();
		log.info("New Ip Address is : " + elasticIp);
		return elasticIp;
	}

	public void associateIp(String elasticIp, String instanceId) {
		log.info("Associating ip : " + elasticIp + " to instance : "
				+ instanceId);
		AssociateAddressRequest aar = new AssociateAddressRequest()
		.withInstanceId(instanceId).withPublicIp(elasticIp);
		AssociateAddressResult result = ec2.associateAddress(aar);
		log.info("Finished associating ip : " + elasticIp + " to instance : "
				+ instanceId);
	}

	public void disassociateIp(String elasticIp) {
		log.info("Disassociating ip : " + elasticIp);
		DisassociateAddressRequest dar = new DisassociateAddressRequest();
		dar.setPublicIp(elasticIp);
		ec2.disassociateAddress(dar);
		log.info("Finished disassociating ip : " + elasticIp);
	}
	
	// Auto scaling code 
	
	public void enableAutoScaling(String instanceId){
		//TODO
	}
	
	public void disableAutoScaling(String instanceId){
		//TODO
	}

}
