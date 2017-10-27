package com.github.arucard21.msr;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.github.arucard21.msr.revfinder.CodeReview;

public class ReviewableChange{
	private static final String JSON_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.nnnnnnnnn";
	private final String id;
	private String change_id;
	private int owner_id;
	private String status;
	private LocalDateTime created;
	private LocalDateTime updated;
	private String current_revision;
	private List<CodeReview> reviews = new ArrayList<>();
	private JsonObject revisions;
	private String project;
	private int number;
	
	public ReviewableChange(String id) {
		this.id = id;
	}
	
	public ReviewableChange(JsonObject jsonObject, boolean fullChangeJSON) {
		id = jsonObject.getString("id", "");
		change_id = jsonObject.getString("change_id", "");
		if (fullChangeJSON) {
			owner_id = jsonObject.getJsonObject("owner").getInt("_account_id", -1);
		}
		else {
			owner_id = jsonObject.getInt("owner", -1);
		}
		status = jsonObject.getString("status", "");
		setCreated(jsonObject.getString("created", ""));
		setUpdated(jsonObject.getString("updated", ""));
		current_revision = jsonObject.getString("current_revision", "");
		loadReviews(jsonObject);
		revisions = loadRevisions(jsonObject);
		project = jsonObject.getString("project", "");
	}
	
	public JsonObject asJsonObject() {
		JsonArrayBuilder reviewersBuilder = Json.createArrayBuilder();
		for(CodeReview review: reviews) {
			reviewersBuilder.add(review.asJsonObject());
		}
		return Json.createObjectBuilder()
				.add("id", id)
				.add("change_id", change_id)
				.add("owner", owner_id)
				.add("status", status)
				.add("created", fromLocalDateTime(created))
				.add("updated", fromLocalDateTime(updated))
				.add("current_revision", current_revision)
				.add("reviews", reviewersBuilder)
				.add("revisions", revisions)
				.add("project", project)
				.add("number", number)
				.build();
	}

	private JsonObject loadRevisions(JsonObject originalChange) {
		
		JsonObject revisions = originalChange.getJsonObject("revisions");
		return revisions == null ? Json.createObjectBuilder().build() : revisions;
	}

	private void loadReviews(JsonObject originalChange) {
		JsonObject labels = originalChange.getJsonObject("labels");
		if (labels == null || labels.isEmpty()) {
			return;
		}
		JsonObject codeReviews = labels.getJsonObject("Code-Review");
		if (codeReviews == null || codeReviews.isEmpty()) {
			return;
		}
		reviews = codeReviews.getJsonArray("all").stream()
					.map(reviewerJSON -> new CodeReview(reviewerJSON.asJsonObject()))
					.collect(Collectors.toList());
	}
	
	private LocalDateTime toLocalDateTime(String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(JSON_DATE_TIME_PATTERN);
		LocalDateTime  date = LocalDateTime.parse(dateString, formatter);
		return date;
	}
	
	private String fromLocalDateTime(LocalDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(JSON_DATE_TIME_PATTERN);
		return date.format(formatter);
	}

	public String getChange_id() {
		return change_id;
	}

	public void setChange_id(String change_id) {
		this.change_id = change_id;
	}

	public int getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(int owner_id) {
		this.owner_id = owner_id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}
	
	public void setCreated(String created) {
		this.created = toLocalDateTime(created);
	}

	public LocalDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(LocalDateTime updated) {
		this.updated = updated;
	}
	
	public void setUpdated(String updated) {
		this.updated = toLocalDateTime(updated);
	}

	public String getCurrent_revision() {
		return current_revision;
	}

	public void setCurrent_revision(String current_revision) {
		this.current_revision = current_revision;
	}

	public List<CodeReview> getReviews() {
		return reviews;
	}

	public void setReviewers(List<CodeReview> reviews) {
		this.reviews = reviews;
	}

	public JsonObject getRevisions() {
		return revisions;
	}

	public void setRevisions(JsonObject revisions) {
		this.revisions = revisions;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getId() {
		return id;
	}
}
