package com.poly.cloud;

import java.io.IOException;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;

public class Init {

	public static void initSimpleDB() throws IOException {
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(
				new PropertiesCredentials(Init.class
						.getResourceAsStream("/AwsCredentials.properties")));
		// Create a domain
		String myDomain = "Users";
		System.out.println("Creating domain called " + myDomain + ".\n");		
		sdb.createDomain(new CreateDomainRequest(myDomain));
		
		PutAttributesRequest putAttributesRequest = new PutAttributesRequest();
		putAttributesRequest.withDomainName(myDomain);
	//	putAttributesRequest.s
		
		// List domains
		System.out.println("Listing all domains in your account:\n");
		for (String domainName : sdb.listDomains().getDomainNames()) {
			System.out.println("  " + domainName);

		}

	}

}
