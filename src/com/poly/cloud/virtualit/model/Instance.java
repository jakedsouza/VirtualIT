package com.poly.cloud.virtualit.model;

import com.amazonaws.services.ec2.model.KeyPair;

/**
 * Class to contain all information of a single instance attached to a user .
 * 
 * @author jake , mihir
 * @version 0.001
 */

public class Instance {
	/**
	 * The ID of the instance
	 */
	private String instanceID;

	/**
	 * The elastic IP associated with the instance
	 */
	private String elasticIP;

	/**
	 * The public dns address for the instance
	 */
	private String publicDNSAddress;

	/**
	 * The volume id of the root volume
	 */
	private String volumeID;

	/**
	 * the ami id of the instance
	 */
	private String amiID;

	/**
	 * The snapshot ID of the instance
	 */
	private String snapshotID;

	/**
	 * The name of the snapshot
	 */
	private String snapshotName;

	/**
	 * The instance type , T1Micro,M1Small,M1Medium,M1Large etc
	 */
	private String instanceType;

	/**
	 * The name of the security group of the instance
	 */
	private String securityGroupName;
	
	/**
	 * The id of the security group of the instance
	 */
	private String securityGroupID;

	/**
	 * The key pair for the instance . note we have the key material stored here
	 * . May not be a good idea
	 */
	private KeyPair keyPair;

	/**
	 * flag to tell if this is the main instance for the user or not
	 */
	private boolean isRootInstance;

	/**
	 * @return the instanceID
	 */
	public String getInstanceID() {
		return instanceID;
	}

	/**
	 * @param instanceID
	 *            the instanceID to set
	 */
	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	/**
	 * @return the elasticIP
	 */
	public String getElasticIP() {
		return elasticIP;
	}

	/**
	 * @param elasticIP
	 *            the elasticIP to set
	 */
	public void setElasticIP(String elasticIP) {
		this.elasticIP = elasticIP;
	}

	/**
	 * @return the publicDNSAddress
	 */
	public String getPublicDNSAddress() {
		return publicDNSAddress;
	}

	/**
	 * @param publicDNSAddress
	 *            the publicDNSAddress to set
	 */
	public void setPublicDNSAddress(String publicDNSAddress) {
		this.publicDNSAddress = publicDNSAddress;
	}

	/**
	 * @return the volumeID
	 */
	public String getVolumeID() {
		return volumeID;
	}

	/**
	 * @param volumeID
	 *            the volumeID to set
	 */
	public void setVolumeID(String volumeID) {
		this.volumeID = volumeID;
	}

	/**
	 * @return the amiID
	 */
	public String getAmiID() {
		return amiID;
	}

	/**
	 * @param amiID
	 *            the amiID to set
	 */
	public void setAmiID(String amiID) {
		this.amiID = amiID;
	}

	/**
	 * @return the snapshotID
	 */
	public String getSnapshotID() {
		return snapshotID;
	}

	/**
	 * @param snapshotID
	 *            the snapshotID to set
	 */
	public void setSnapshotID(String snapshotID) {
		this.snapshotID = snapshotID;
	}

	/**
	 * @return the snapshotName
	 */
	public String getSnapshotName() {
		return snapshotName;
	}

	/**
	 * @param snapshotName
	 *            the snapshotName to set
	 */
	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}

	/**
	 * @return the instanceType
	 */
	public String getInstanceType() {
		return instanceType;
	}

	/**
	 * @param instanceType
	 *            the instanceType to set
	 */
	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	/**
	 * @return the securityGroupName
	 */
	public String getSecurityGroupName() {
		return securityGroupName;
	}

	/**
	 * @param securityGroupName
	 *            the securityGroupName to set
	 */
	public void setSecurityGroupName(String securityGroupName) {
		this.securityGroupName = securityGroupName;
	}

	/**
	 * @return the keyPair
	 */
	public KeyPair getKeyPair() {
		return keyPair;
	}

	/**
	 * @param keyPair
	 *            the keyPair to set
	 */
	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	/**
	 * @return the isRootInstance
	 */
	public boolean isRootInstance() {
		return isRootInstance;
	}

	/**
	 * @param isRootInstance
	 *            the isRootInstance to set
	 */
	public void setRootInstance(boolean isRootInstance) {
		this.isRootInstance = isRootInstance;
	}

}
