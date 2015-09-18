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
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Ranker;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Rankers;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.models.Ranking;
import com.ibm.watson.developer_cloud.service.Request;
import com.ibm.watson.developer_cloud.service.WatsonService;
import com.ibm.watson.developer_cloud.util.GsonSingleton;
import com.ibm.watson.developer_cloud.util.ResponseUtil;

/**
 *
 * @author Kazi S. Hasan (kshasan@us.ibm.com)
 * @version v1
 */

public class RetrieveAndRank extends WatsonService {
	/** The Constant log. */
	private static final Logger log = Logger.getLogger(RetrieveAndRank.class.getName());
	
	/**
	 * Instantiates a new ranker client.
	 */
	public RetrieveAndRank() {
	}
	
	/**
	 * Sends data to create and train a ranker, and returns information
	 * about the new ranker. The status has the value of `Training` when the
	 * operation is successful, and might remain at this status for a while.
	 * 
	 * @param name
	 *            Name of the ranker
	 * @param trainingFile
	 *            A file with the set of instances (i.e., qid and feature values) and 
	 *            their rank (i.e., ground truth) used to train the ranker
	 *            
	 * @return the ranker
	 * @see Ranker
	 */
	public Ranker createRanker(final String name, final File trainingFile) {
		if (trainingFile == null || !trainingFile.exists())
			throw new IllegalArgumentException("trainingFile does not exist or is null");
		
		JsonObject contentJson = new JsonObject();

		if (name != null && !name.isEmpty()) {
			contentJson.addProperty("name", name);
		}
		
		try {
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("training_metadata",contentJson.toString(), ContentType.TEXT_PLAIN);
			builder.addBinaryBody("training_data", trainingFile);
			HttpEntity reqEntity = builder.build();
			
			HttpRequestBase request = Request.Post("/v1/rankers").withEntity(reqEntity).build();
		
			HttpResponse response = execute(request);
			String rankerAsJson = ResponseUtil.getString(response);
			System.out.println(rankerAsJson);
			Ranker ranker = GsonSingleton.getGson().fromJson(rankerAsJson, Ranker.class);
			return ranker;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}
	
	/**
	 * Retrieves the list of rankers for the user.
	 * 
	 * @return the ranker list
	 * @see Ranker
	 */
	public Rankers getRankers() {
		HttpRequestBase request = Request.Get("/v1/rankers").build();

		try {
			HttpResponse response = execute(request);
			return ResponseUtil.getObject(response, Rankers.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Retrieves the status of a classifier.
	 * 
	 * @param rankerID
	 *            the ranker ID
	 * @return Ranker object with the status field set
	 * @see Ranker
	 */
	public Ranker getRankerStatus(String rankerID) {
		if (rankerID == null || rankerID.isEmpty())
			throw new IllegalArgumentException("rankerID can not be null or empty");

		HttpRequestBase request = Request.Get("/v1/rankers/" + rankerID).build();

		try {
			HttpResponse response = execute(request);
			return ResponseUtil.getObject(response, Ranker.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deletes a ranker.
	 * 
	 * @param rankerID
	 *            the ranker ID
	 * @see Ranker
	 */
	public void deleteRanker(String rankerID) {
		if (rankerID == null || rankerID.isEmpty())
			throw new IllegalArgumentException("rankerID can not be null or empty");

		HttpRequestBase request = Request.Delete("/v1/rankers/" + rankerID).build();
		executeWithoutResponse(request);
	}
	
	/**
	 * Returns the ranked answers returned by the ranker.
	 * 
	 * @param rankerID
	 *            The ranker ID
	 * @param testFile
	 *            The file with the list of test instances to rank
	 * @return the ranking of the answers
	 */
	public Ranking rank(final String rankerID, final File testFile) {
		if (rankerID == null || rankerID.isEmpty())
			throw new IllegalArgumentException("rankerID can not be null or empty");
		
		if (testFile == null || !testFile.exists())
			throw new IllegalArgumentException("testFile does not exist or is null");

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("answer_data", testFile);
		HttpEntity reqEntity = builder.build();
		
		String path = String.format("/v1/retrieve-and-rank/%s/rank", rankerID);

		HttpRequestBase request = Request.Post(path).withEntity(reqEntity).build();

		try {
			System.out.println("1");
			HttpResponse response = execute(request);
			System.out.println("2");
			if(response == null) System.out.println("3");
			String rankerAsJson = ResponseUtil.getString(response);
			System.out.println("4 = " + rankerAsJson);
			Ranking ranking = GsonSingleton.getGson().fromJson(rankerAsJson, Ranking.class);
			
			return ranking;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RankerClient [getEndPoint()=");
		builder.append(getEndPoint());
		builder.append("]");
		return builder.toString();
	}
}
