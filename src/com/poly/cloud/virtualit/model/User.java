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
	private ArrayList<Instance> instances;

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
	public ArrayList<Instance> getInstances() {
		return instances;
	}

	/**
	 * @param instances
	 *            the instances to set
	 */
	public void setInstances(ArrayList<Instance> instances) {
		this.instances = instances;
	}

	public void addInstance(Instance instance) {
		// TODO add a single instance to the array list
	}

	public void removeInstance(Instance instance) {
		// TODO remove a single instance
	}

	public void removeInstance(String instanceID) {
		// TODO remove an instance from list given instanceid
	}

}
