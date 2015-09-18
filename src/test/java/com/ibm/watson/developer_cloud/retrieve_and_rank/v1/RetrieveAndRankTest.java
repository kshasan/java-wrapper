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
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.watson.developer_cloud.WatsonServiceTest;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Ranker;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Ranking;

/**
 * This class includes a set of methods to test the ranker client
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
     * An end-to-end test of the ranker client
     * Creates an ranker, checks its status, sends queries to it, and finally deletes the ranker.
     */
    @Test
    public void testRanker() {
    	String rankerName = "test-ranker-ID-java-wrapper-1";
    	
    	/**
    	 * create the ranker
    	 */
    	Ranker ranker = client.createRanker(rankerName, new File(TRAINING_FILE));
    	Assert.assertNotNull(ranker);
    	Assert.assertNotNull(ranker.getId());
    	System.out.println("ranker id = " + ranker.getId());
    	
    	/**
    	 * check its status to know if it has finished training
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	/**
    	 * get the list of rankers
    	 */
    	Assert.assertNotNull(client.getRankers());
    	
    	/**
    	 * rank an test instance
    	 */
    	Ranking ranking = client.rank(ranker.getId(), new File(TEST_FILE));
    	Assert.assertNotNull(ranking);
    	Assert.assertNotNull(ranking.getTopAnswer());
    	Assert.assertNotNull(ranking.getAnswers());
    	
    	/**
    	 * delete the ranker
    	 */
    	//client.deleteRanker("D42E5E-rank-24");
    	client.deleteRanker(ranker.getId());
    }
}
