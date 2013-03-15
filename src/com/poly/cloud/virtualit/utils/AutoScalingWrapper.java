package com.poly.cloud.virtualit.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.KeyPair;

public class AutoScalingWrapper {
	private static final Logger log = Logger
			.getLogger(AutoScalingWrapper.class.getName());
	private static AmazonAutoScalingClient scalingClient;
	private static AmazonCloudWatchClient cloudWatchClient ;
	AWSCredentials credentials = null;

	public AutoScalingWrapper(){
		init();
	}
	public void init() {

			try {
				log.info("Loading Credentials file flor cloudwatch and autoscaling");
				credentials = new PropertiesCredentials(
						AWSUtils.class
						.getResourceAsStream("/AwsCredentials.properties"));
			} catch (IOException e) {
				log.info("Error Loading credentials file");
				e.printStackTrace();
			}
			scalingClient = new AmazonAutoScalingClient(credentials);
			cloudWatchClient = new AmazonCloudWatchClient(credentials);
			log.info("Credentials file loaded");

		}
	
	public void createLaunchConfiguration(String imageId, String name,
			String keyName, String securityGroup) {
		log.info("Creating new launch config ");
		CreateLaunchConfigurationRequest launchConfigurationRequest = new CreateLaunchConfigurationRequest();
		launchConfigurationRequest.setImageId(imageId);
		launchConfigurationRequest.setLaunchConfigurationName(name);
		launchConfigurationRequest.setKeyName(keyName);
		launchConfigurationRequest.setInstanceType("t1.micro");
		launchConfigurationRequest.setSecurityGroups(Arrays
				.asList(securityGroup));
		scalingClient.createLaunchConfiguration(launchConfigurationRequest);
		log.info("Launch Configuration created ");
	}

	public void createAutoScalingGroup(String name,
			String launchConfigurationName, String loadBalancerName) {
		CreateAutoScalingGroupRequest scalingGroupRequest = new CreateAutoScalingGroupRequest();
		scalingGroupRequest.setAutoScalingGroupName(name);
		scalingGroupRequest.setLaunchConfigurationName(launchConfigurationName);
//		scalingGroupRequest.setLoadBalancerNames(Arrays
//				.asList(loadBalancerName));
		scalingGroupRequest.setMaxSize(3);
		scalingGroupRequest.setMinSize(1);
		scalingGroupRequest.setAvailabilityZones(Arrays.asList("us-east-1a","us-east-1b"));
		scalingClient.createAutoScalingGroup(scalingGroupRequest);
	}

	public String putScalingPolicy(String autoScalingGroupName,
			String policyName, int scalingAdjustment, String adjustmentType,
			int cooldown) {
		PutScalingPolicyRequest request = new PutScalingPolicyRequest();
		request.setAutoScalingGroupName(autoScalingGroupName);
		request.setAdjustmentType(adjustmentType);
		request.setCooldown(cooldown);
		request.setPolicyName(policyName);
		request.setScalingAdjustment(scalingAdjustment);
		PutScalingPolicyResult result = scalingClient.putScalingPolicy(request);
		return result.getPolicyARN() ;
	}

	public void putMetricAlarm(String name , String alarmActions , int period , int evaluationPeriods ,double threshold , ArrayList<Dimension> dimensions	) {
		PutMetricAlarmRequest request = new PutMetricAlarmRequest(); 
		request.withAlarmActions(Arrays.asList(alarmActions));
		request.setAlarmName(name);
		request.setMetricName("CPUUtilization");
		request.setNamespace("AWS/EC2");
		request.setStatistic(Statistic.Average);
		request.setPeriod(period);
		request.setEvaluationPeriods(evaluationPeriods);
		request.setThreshold(threshold);
		request.setComparisonOperator(ComparisonOperator.GreaterThanThreshold);
		request.setDimensions(dimensions);
		cloudWatchClient.putMetricAlarm(request);
	}
	
	public static void main(String args[]) {
		AWSUtils utils = new AWSUtils();
//		String securityGroupID = utils.createSecurityGroup("scalingGroup","scalingGroup");
//		KeyPair keyPair = utils.createKeyPair("scalingKeyPair");
//		String instanceID = utils.createInstance(keyPair.getKeyName(), "scalingGroup", null);
//		utils.stopInstance(instanceID);
//		String imageID  = utils.createImage(instanceID, "scalingimage");
		
		AutoScalingWrapper scale = new AutoScalingWrapper();
		scale.createLaunchConfiguration("ami-e25dc38b", "scalingLaunchConfig2", "scalingKeyPair", "scalingGroup");
		scale.createAutoScalingGroup("scalingGroup2","scalingLaunchConfig2" , null);
		String arn1 = scale.putScalingPolicy("scalingGroup2", "scalingPolicy2", 1, "ChangeInCapacity", 30);
		ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
		dimensions.add(new Dimension().withName("InstanceId").withValue("i-fbcb0f89"));
		scale.putMetricAlarm("hiCpuAlarm2", arn1, 600, 1, 80, dimensions);
		
		
	}
	
	
	
}
