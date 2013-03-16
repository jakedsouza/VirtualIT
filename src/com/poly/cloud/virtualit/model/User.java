package com.poly.cloud.virtualit.model;

import java.util.ArrayList;

/**
 * Class to hold all information about a single user and all the instances he
 * has
 * 
 * @author jake , mihir
 * @version 0.001
 */
public class User {

	private static int USER_COUNT;
	private String id;
	private String username;
	private ArrayList<InstanceModel> instances;

	@SuppressWarnings("unused")
	private User() {
		// private constructor so that no user is created without a user name
	}

	/**
	 * Creates a new user
	 * 
	 * @param username
	 */
	public User(String username) {
		this.id = Integer.toString(USER_COUNT + 1);
		this.username = username;
		this.instances = new ArrayList<InstanceModel>();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the instances
	 */
	public ArrayList<InstanceModel> getInstances() {
		return instances;
	}

	/**
	 * @param instances
	 *            the instances to set
	 */
	public void setInstances(ArrayList<InstanceModel> instances) {
		this.instances = instances;
	}

	public void addInstance(InstanceModel instance) {
		this.instances.add(instance);
	}

	public void removeInstance(InstanceModel instance) {
		this.instances.remove(instance);
	}

	public void removeInstance(String instanceID) {
		for(InstanceModel instance : instances){
			if(instance.getInstanceID().equals(instanceID)){
				instances.remove(instance);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("User Name : " + username + " ,");
		for (InstanceModel model : instances) {
			sb.append(model.toString());
		}
		sb.append("}");
		return sb.toString();
	}

}
