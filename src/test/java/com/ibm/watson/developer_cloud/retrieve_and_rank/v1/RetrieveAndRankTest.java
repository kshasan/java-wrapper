/**
 * Copyright 2015 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.watson.developer_cloud.retrieve_and_rank.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.watson.developer_cloud.WatsonServiceTest;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Ranker;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Ranking;

/**
 * This class includes a set of methods to test the {@link RetrieveAndRank} class.
 *
 * @author Kazi S. Hasan (kshasan@us.ibm.com)
 * @version v1
 */

public class RetrieveAndRankTest extends WatsonServiceTest {
	/** The Constant log. */
    private static final Logger log = Logger.getLogger(RetrieveAndRankTest.class.getName());

    /** Ranker status */
    private static final String RANKER_TRAINING_STATUS = "Training";
    private static final String RANKER_AVAILABLE_STATUS = "Available";
    
    /** Training and test file */
    private static final String TRAINING_FILE = "src/test/resources/ranker_train.csv";
    private static final String TEST_FILE = "src/test/resources/ranker_test.csv";
    
    /** The client. */
    private RetrieveAndRank client;
        
    /**
     * Initialize the client
     */
    @Before
    public void initializeRankerClient() {
        client = new RetrieveAndRank();
        client.setUsernameAndPassword(
				prop.getProperty("retrieve_and_rank.username"),
				prop.getProperty("retrieve_and_rank.password"));
        client.setEndPoint(prop.getProperty("retrieve_and_rank.url"));
    }
	
    /**
     * An end-to-end test which
     * - Creates a ranker
     * - Checks its status
     * - Get the list of rankers for the user
     * - Sends a query to the ranker created in the first step and 
     * - Deletes the ranker.
     */
    @Test
    public void testRanker() {
    	String rankerName = "test-ranker-ID-java-wrapper-1";
    	
    	/**
    	 * create the ranker
    	 */
    	Ranker ranker = null;
    	try(InputStream trainingFile = new FileInputStream(new File(TRAINING_FILE))) {
	    	ranker = client.createRanker(rankerName, trainingFile);
	    	Assert.assertNotNull(ranker);
	    	Assert.assertNotNull(ranker.getId());
	    	System.out.println("ranker id = " + ranker.getId());
    	} catch(IOException e) {
    		log.log(Level.SEVERE,"Error while creating ranker", e);
    	}
    	
    	/**
    	 * keep checking its status until it has finished training
    	 */
    	while(true){
    		String status = client.getRankerStatus(ranker.getId()).getStatus().toString();
    		Assert.assertNotNull("current status of " + ranker.getId()+ " is " + status, status);
    		if (!RANKER_TRAINING_STATUS.equalsIgnoreCase(status)) {
                if (!RANKER_AVAILABLE_STATUS.equalsIgnoreCase(status)) {
                    throw new RuntimeException(
                            "Problem with training ranker (status=" + status + ")");
                }
                System.out.println("Ranker is Available!");
                break;
            }
            try {
            	/** wait time between two checks */
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				log.log(Level.SEVERE,"Error getting ranker status", e);
			}
    	}
    	
    	/**
    	 * get the list of rankers
    	 */
    	Assert.assertNotNull(client.getRankers());
    	
    	/**
    	 * rank test instances
    	 */
    	try(InputStream testFile = new FileInputStream(new File(TEST_FILE))) {
    		//calling rank with -1 so that the default number of top answers are returned
	    	Ranking ranking = client.rank(ranker.getId(), testFile, -1);
	    	Assert.assertNotNull(ranking);
	    	Assert.assertNotNull(ranking.getTopAnswer());
	    	System.out.println("top answer = " + ranking.getTopAnswer());
    	} catch(IOException e) {
    		log.log(Level.SEVERE,"Error ranking test instances", e);
    	}
    	
    	try(InputStream testFile = new FileInputStream(new File(TEST_FILE))) {
	    	//another call with a valid number for top answers
	    	int topAnswers = 5;
	    	Ranking ranking = client.rank(ranker.getId(), testFile, topAnswers);
	    	Assert.assertNotNull(ranking);
	    	Assert.assertNotNull(ranking.getAnswers());
    	} catch(IOException e) {
    		log.log(Level.SEVERE,"Error ranking test instances", e);
    	}
    	
    	/**
    	 * delete the ranker
    	 */
    	client.deleteRanker(ranker.getId());
    }
}
