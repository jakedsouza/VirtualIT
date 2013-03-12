package com.poly.cloud;

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
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Instance;
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
import com.amazonaws.services.ec2.model.transform.RunInstancesRequestMarshaller;
import com.amazonaws.services.ec2.model.transform.SecurityGroupStaxUnmarshaller;

public class AWSUtils {
	private static final Logger log = Logger.getLogger(AWSUtils.class
			.getName());
	private static final String SERURITY_GROUP_NAME = "Assignment1SecurityGroup";
	private static final String SECURITY_GROUP_DESCRIPTION = "Security group for assignment 1";
	private static final String KEY_FILE_NAME = "Assignment1.pem";
	private static final String KEY_PAIR_NAME = "Assignment1KeyPair";
	private static final String ALL_ACCESS_IP_RANGE = "0.0.0.0/0";
	static AmazonEC2 ec2;
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
	 * Creates a default security group with default security group name and
	 * description
	 * 
	 * @throws IOException
	 */
	public void createSecurityGroup() {
		createSecurityGroup(SERURITY_GROUP_NAME, SECURITY_GROUP_DESCRIPTION);
	}

	/**
	 * Creates a new security group with group name Assignment1SecurityGroup.
	 * The function first checks if the same security group exists . If it does
	 * exist , then we just add our ingress request to it . If the group does
	 * not exist , a new one is created and the ingress ports are added . For
	 * this assignment , all ports are opened from 0 to 65535 for all IP ranges.
	 * 
	 * @param securityGroupName
	 *            The name of the security group
	 * @param securityGroupDescription
	 *            The description of the security group
	 * @throws IOException
	 */
	public void createSecurityGroup(String securityGroupName,
			String securityGroupDescription) {
		init();
		log.info("Creating new security group with name " + securityGroupName);
		boolean securityGroupExists = false;
		List<SecurityGroup> existingSecurityGroupsResult = ec2
				.describeSecurityGroups().getSecurityGroups();
		for (SecurityGroup securityGroup : existingSecurityGroupsResult) {
			if (securityGroup.getGroupName().equals(securityGroupName)) {
				securityGroupExists = true;
			}
		}
		if (!securityGroupExists) {
			CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest(
					securityGroupName, securityGroupDescription);
			ec2.createSecurityGroup(createSecurityGroupRequest);
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
			log.info("security group " + securityGroupName
					+ " already exists not creating new group");
		}

	}

	public void createKeyPair() {
		createKeyPair(KEY_PAIR_NAME);
	}

	public void createKeyPair(String keyName) {
		init();
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
			KeyPair keyPair = ec2.createKeyPair(keyPairRequest).getKeyPair();

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
	}

	public String createNewInstance(String keyName, String securityGroupName) {
		init();
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.withImageId("ami-76f0061f")
		.withInstanceType(InstanceType.T1Micro).withMinCount(1)
		.withMaxCount(1).withKeyName(keyName)
		.withSecurityGroups(securityGroupName);
		RunInstancesResult result = ec2.runInstances(runInstancesRequest);
		return result.getReservation().getInstances().get(0).getInstanceId();

	}

	public void deleteInstance(String instanceId) {
		ec2.terminateInstances(new TerminateInstancesRequest()
		.withInstanceIds(instanceId));
	}

	public void stopInstance(String instanceId) {
		ec2.stopInstances(new StopInstancesRequest()
		.withInstanceIds(instanceId));
	}

	public void startInstance(String instanceId) {
		ec2.startInstances(new StartInstancesRequest()
		.withInstanceIds(instanceId));
	}
	
	public void createSnapshot(String volumeId){ 
	CreateSnapshotRequest request = new CreateSnapshotRequest();
	request.withVolumeId(volumeId);
		
	CreateSnapshotResult result =ec2.createSnapshot(request);
		Instance instance ;
		//instance.cre
		DescribeVolumesRequest volumesRequest = new DescribeVolumesRequest();
		
		DescribeVolumesResult volumesResult = ec2.describeVolumes();
		List<Volume> volumes = volumesResult.getVolumes() ;
			// iterate over volumes 
		    // for each volume v 
			Volume v ; 
		//	v.getAttachments().get(0).getInstanceId() ;
		
		
		
		
	}

}
