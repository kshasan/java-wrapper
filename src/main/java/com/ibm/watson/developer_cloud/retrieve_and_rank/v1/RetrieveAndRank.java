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
 * This class provides a set of methods that allow a user to use the ranker API. 
 * Specifically, it contains methods that call the following ranker API methods.
 * 
 * - Create ranker
 * - Get rankers
 * - Get status
 * - Delete ranker
 * - Rank
 * 
 * @author Kazi S. Hasan (kshasan@us.ibm.com)
 * @version v1
 */

public class RetrieveAndRank extends WatsonService {
	/** The Constant log. */
	private static final Logger log = Logger.getLogger(RetrieveAndRank.class.getName());
	
	/** Path variables */
	private static final String CREATE_RANKER_PATH = "/v1/rankers";
	private static final String GET_RANKERS_PATH = "/v1/rankers";
	private static final String GET_RANKER_PATH = "/v1/rankers/";
	private static final String DELETE_RANKER_PATH = "/v1/rankers/";
	private static final String RANK_PATH = "/v1/rankers/%s/rank";
	
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
	 *            A file with the training data i.e., the set of 
	 *            (qid, feature values, and rank) tuples
	 *            
	 * @return the ranker object
	 * @see Ranker
	 */
	public Ranker createRanker(final String name, final File trainingFile) {
		if (trainingFile == null || !trainingFile.exists())
			throw new IllegalArgumentException("trainingFile does not exist or is null");
		
		JsonObject contentJson = new JsonObject();

		if (name != null && !name.isEmpty()) {
			contentJson.addProperty("name", name);
		}
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("training_data", trainingFile);
		builder.addTextBody("training_metadata", contentJson.toString(), ContentType.TEXT_PLAIN);
		HttpEntity reqEntity = builder.build();
		
		try {
			HttpRequestBase request = Request.Post(CREATE_RANKER_PATH).withEntity(reqEntity).build();
		
			HttpResponse response = execute(request);
			String json = ResponseUtil.getString(response);
			return GsonSingleton.getGson().fromJson(json, Ranker.class);
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
		try {
			HttpRequestBase request = Request.Get(GET_RANKERS_PATH).build();
			
			HttpResponse response = execute(request);
			return ResponseUtil.getObject(response, Rankers.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Retrieves the status of a ranker.
	 * 
	 * @param rankerID
	 *            the ranker ID
	 * @return Ranker object with the status field set
	 * @see Ranker
	 */
	public Ranker getRankerStatus(String rankerID) {
		if (rankerID == null || rankerID.isEmpty())
			throw new IllegalArgumentException("rankerID can not be null or empty");

		try {
			HttpRequestBase request = Request.Get(GET_RANKER_PATH + rankerID).build();
			
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

		try {
			HttpRequestBase request = Request.Delete(DELETE_RANKER_PATH + rankerID).build();
			executeWithoutResponse(request);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets and returns the ranked answers.
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
		
		String path = String.format(RANK_PATH, rankerID);

		try {
			HttpRequestBase request = Request.Post(path).withEntity(reqEntity).build();
			
			HttpResponse response = execute(request);
			String rankingAsJson = ResponseUtil.getString(response);
			Ranking ranking = GsonSingleton.getGson().fromJson(rankingAsJson, Ranking.class);
			
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
