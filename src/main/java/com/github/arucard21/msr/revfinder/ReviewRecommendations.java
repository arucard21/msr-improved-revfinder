package com.github.arucard21.msr.revfinder;

import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ReviewRecommendations {
	private final String reviewID;
	private final List<Reviewer> recommendedReviewers;
	
	public ReviewRecommendations(String reviewID, List<Reviewer> recommendedReviewers) {
		this.reviewID = reviewID;
		this.recommendedReviewers = recommendedReviewers;
	}
	
	public ReviewRecommendations(String reviewID, JsonArray recommendedReviewers) {
		this.reviewID = reviewID;
		this.recommendedReviewers = recommendedReviewers.stream()
				.map((reviewer) -> new Reviewer(reviewer.asJsonObject()))
				.collect(Collectors.toList());
	}
	
	public String getReviewID() {
		return reviewID;
	}

	public List<Reviewer> getRecommendedReviewers() {
		return recommendedReviewers;
	}

	public JsonObject asJsonObject(){
		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add("review_id", getReviewID());
		for (Reviewer recommended: getRecommendedReviewers()) {
			builder.add("recommended_reviewers", recommended.asJsonObject());
		}
		return builder.build();
	}
}
