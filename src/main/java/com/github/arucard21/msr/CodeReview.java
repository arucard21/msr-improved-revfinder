package com.github.arucard21.msr;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.github.arucard21.msr.revfinder.GerritUser;
import com.github.arucard21.msr.revfinder.Message;

public class CodeReview{
	private static final String JSON_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.nnnnnnnnn";
	private final String id;
	private String change_id;
	private int owner_id;
	private String status;
	private LocalDateTime created;
	private LocalDateTime updated;
	private int insertions;
	private int deletions;
	private String current_revision;
	private List<GerritUser> reviewers = new ArrayList<>();
	private List<Message> messages = new ArrayList<>();
	private JsonObject revisions;
	private String project;
	private int number;
	
	public CodeReview(String id) {
		this.id = id;
	}
	
	public CodeReview(JsonObject jsonObject, boolean fullChangeJSON) {
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
		insertions = jsonObject.getInt("insertions", -1);
		deletions = jsonObject.getInt("deletions", -1);
		current_revision = jsonObject.getString("current_revision", "");
		loadMessages(jsonObject);
		loadReviewers(jsonObject);
		revisions = loadRevisions(jsonObject);
		project = jsonObject.getString("project", "");
	}
	
	public JsonObject asJsonObject() {
		JsonArrayBuilder reviewersBuilder = Json.createArrayBuilder();
		JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
		for(GerritUser reviewer: reviewers) {
			reviewersBuilder.add(reviewer.asJsonObject());
		}
		for(Message msg : messages) {
			messagesBuilder.add(msg.asJsonObject());
		}
		return Json.createObjectBuilder()
				.add("id", id)
				.add("change_id", change_id)
				.add("owner", owner_id)
				.add("status", status)
				.add("created", fromLocalDateTime(created))
				.add("updated", fromLocalDateTime(updated))
				.add("insertions", insertions)
				.add("deletions", deletions)
				.add("current_revision", current_revision)
				.add("messages", messagesBuilder)
				.add("reviewers", reviewersBuilder)
				.add("revisions", revisions)
				.add("project", project)
				.add("number", number)
				.build();
	}

	private JsonObject loadRevisions(JsonObject originalChange) {
		// TODO Auto-generated method stub
		
		/* original python code:
		 * revisions = record['revisions']
	            clean_revisions = []
	            for rev_id in revisions:
	                rev = revisions[rev_id]
	
	                if 'kind' in rev:
	                    rev_kind = rev['kind']
	                else:
	                    rev_kind = record['kind']
	
	                rev_author_date = rev['commit']['author']['date'][0:19]
	                rev_committer_date = rev['commit']['committer']['date'][0:19]
	                if 'uploader' in rev:
	                    rev_uploader = rev['uploader']['_account_id']
	                else:
	                    rev_uploader = None
	
	                files = rev['files']
	                file_names = []
	                for file in files:
	                    file_names.append(file)
	
	                clean_rev = {
	                    'id': rev_id,
	                    'kind': rev_kind,
	                    'author_date': rev_author_date,
	                    'committer_date': rev_committer_date,
	                    'uploader': rev_uploader,
	                    'files': file_names
	                }
	                clean_revisions.append(clean_rev)
		 */
		JsonObject revisions = originalChange.getJsonObject("revisions");
		return revisions == null ? Json.createObjectBuilder().build() : revisions;
	}
	
	private void loadMessages(JsonObject originalChange) {
		messages = originalChange.getJsonArray("messages").stream()
				.map(messageJSON -> new Message(messageJSON.asJsonObject()))
				.collect(Collectors.toList());
	}

	private void loadReviewers(JsonObject originalChange) {
		for(Message msg: messages) {
			GerritUser author = msg.getAuthor();
			if(author != null) {
				if (!reviewers.contains(author)) {
					int accountID = author.getId();
					if(accountID != -1) {
						if(accountID != this.owner_id) {
							reviewers.add(author);
						}
					}
				}
			}
		}
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

	public int getInsertions() {
		return insertions;
	}

	public void setInsertions(int insertions) {
		this.insertions = insertions;
	}

	public int getDeletions() {
		return deletions;
	}

	public void setDeletions(int deletions) {
		this.deletions = deletions;
	}

	public String getCurrent_revision() {
		return current_revision;
	}

	public void setCurrent_revision(String current_revision) {
		this.current_revision = current_revision;
	}

	public List<GerritUser> getReviewers() {
		return reviewers;
	}

	public void setReviewers(List<GerritUser> reviewers) {
		this.reviewers = reviewers;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
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
