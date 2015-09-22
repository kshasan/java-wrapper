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

import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Ranker;

/**
 * This class shows how to create a ranker using the wrapper
 *
 * @author Kazi S. Hasan (kshasan@us.ibm.com)
 * @version v1
 */

public class RetrieveAndRankCreateRankerExample {
	public static void main(String[] args) {
		RetrieveAndRank client = new RetrieveAndRank();
	    client.setUsernameAndPassword("<username>", "<password>");
	    client.setEndPoint("<url>");
	    
	    String rankerName = "ranker-example-1";
	    String trainingFilePath = "src/test/resources/ranker_train.csv";
	    
    	try(InputStream trainingFile = new FileInputStream(new File(trainingFilePath))) {
	    	Ranker ranker = client.createRanker(rankerName, trainingFile);
	    	if(ranker != null) {
	    		System.out.println("ranker id = " + ranker.getId());
	    	}
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
	}
}
