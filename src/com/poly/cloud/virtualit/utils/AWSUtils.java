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
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.BundleInstanceRequest;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.S3Storage;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Storage;
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

		init();

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
		init();
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
		init();
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
		return result.getReservation().getInstances().get(0).getInstanceId();
	}

	/**
	 * Deletes the instance with the given instance id
	 * 
	 * @param instanceId
	 */
	public void deleteInstance(String instanceId) {
		init();
		ec2.terminateInstances(new TerminateInstancesRequest()
		.withInstanceIds(instanceId));
	}

	/**
	 * Stops the instance with the given instance ID
	 * 
	 * @param instanceId
	 */
	public void stopInstance(String instanceId) {
		init();
		ec2.stopInstances(new StopInstancesRequest()
		.withInstanceIds(instanceId));
	}

	/**
	 * Starts the instance with the given instance ID
	 * 
	 * @param instanceId
	 */
	public void startInstance(String instanceId) {
		init();
		ec2.startInstances(new StartInstancesRequest()
		.withInstanceIds(instanceId));
	}

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
		init();
		CreateImageRequest request = new CreateImageRequest();
		request.setInstanceId(instanceId);
		request.setName(name);
		CreateImageResult result = ec2.createImage(request);
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
		init();
		CreateSnapshotRequest request = new CreateSnapshotRequest();
		request.setVolumeId(volumeID);
		request.setDescription(description);
		CreateSnapshotResult result = ec2.createSnapshot(request);
		return result.getSnapshot().getSnapshotId();
	}

	public void deleteImage(String imageID, boolean deleteSnapshot) {
		// Get the image from imageID
		init();
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
	}

	public void deleteSnapShot(String snapshotID) {
		init();
		DeleteSnapshotRequest request = new DeleteSnapshotRequest(snapshotID);
		ec2.deleteSnapshot(request);		 
	}

	public void attachVolumeToInstance() {

	}

	public void detachVolumeFromInstance() {

	}

	public void generateLoad(String isntanceID){
		
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

}
