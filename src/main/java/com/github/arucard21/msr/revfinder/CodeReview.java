package com.github.arucard21.msr.revfinder;

import javax.json.Json;
import javax.json.JsonObject;

public class CodeReview {
	private final GerritUser reviewer;
	private final int reviewScore; // score ranges from -2 to +2

	public CodeReview(int id, String name, int reviewScore) {
		this.reviewer = new GerritUser(id, name);
		this.reviewScore = reviewScore;
	}

	public CodeReview(JsonObject json, boolean filtered) {
		if (filtered) {
			this.reviewer = new GerritUser(json.getJsonObject("reviewer"));
			this.reviewScore = json.getInt("reviewScore");
		}
		else {
			this.reviewer = new GerritUser(json);
			this.reviewScore = json.getInt("value");
		}
	}

	public int getReviewScore() {
		return reviewScore;
	}
	
	public JsonObject asJsonObject(){
		return Json.createObjectBuilder()
				.add("reviewer", getReviewer().asJsonObject())
				.add("reviewScore", getReviewScore())
				.build();
	}

	public GerritUser getReviewer() {
		return reviewer;
	}
	
}
