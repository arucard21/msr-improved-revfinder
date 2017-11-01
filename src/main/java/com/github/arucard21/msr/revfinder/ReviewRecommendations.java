package com.github.arucard21.msr.revfinder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ReviewRecommendations {
	private final String reviewID;
	private final LocalDateTime createdDate;
	private final List<GerritUser> recommendedReviewers;
	
	public ReviewRecommendations(String reviewID, String createdDate, List<GerritUser> recommendedReviewers) {
		this.reviewID = reviewID;
		this.createdDate = LocalDateTime.parse(createdDate);
		this.recommendedReviewers = recommendedReviewers;
	}
	
	public ReviewRecommendations(JsonObject recommendationJSON) {
		String reviewID = recommendationJSON.getString("review_id");
		String createdDate = recommendationJSON.getString("created");
		JsonArray recommendedReviewers = recommendationJSON.asJsonObject().getJsonArray("recommended_reviewers");
		this.reviewID = reviewID;
		this.createdDate = LocalDateTime.parse(createdDate);
		this.recommendedReviewers = recommendedReviewers.stream()
				.map((reviewer) -> new GerritUser(reviewer.asJsonObject()))
				.collect(Collectors.toList());
	}
	
	public String getReviewID() {
		return reviewID;
	}
	
	public LocalDateTime getReviewCreated() {
		return createdDate;
	}

	public List<GerritUser> getRecommendedReviewers() {
		return recommendedReviewers;
	}

	public JsonObject asJsonObject(){
		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add("review_id", getReviewID())
				.add("created", getReviewCreated().toString());
		for (GerritUser recommended: getRecommendedReviewers()) {
			builder.add("recommended_reviewers", recommended.asJsonObject());
		}
		return builder.build();
	}
}
