package com.github.arucard21.msr;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.github.arucard21.msr.revfinder.Reviewer;

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
	private JsonArray reviewers;
	private JsonArray messages;
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
		messages = getMessages(jsonObject);
		reviewers = getReviewers(jsonObject);
		revisions = getRevisions(jsonObject);
		project = jsonObject.getString("project", "");
	}
	
	public JsonObject asJsonObject() {
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
				.add("reviewers", reviewers)
				.add("messages", messages)
				.add("revisions", revisions)
				.add("project", project)
				.add("number", number)
				.build();
	
	}

	private JsonObject getRevisions(JsonObject originalChange) {
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

	private JsonArray getMessages(JsonObject originalChange) {
		// TODO Auto-generated method stub
		
		/* original python code:
		 messages = record['messages']
	            reviewers = []
	            clean_messages = []
	            for msg in messages:
	                msg_id = msg['id']
	
	                if 'author' not in msg:
	                    dropped += 1
	                    continue
	
	                if '_account_id' not in msg['author']:
	                    dropped += 1
	                    continue
	                msg_author_id = msg['author']['_account_id']
	                reviewers.append(msg_author_id)
	                msg_date = msg['date'][0:19]
	                msg_type = ("review", "self")[owner_id == msg_author_id]
	
	                clean_msg = {
	                    'id': msg_id,
	                    'author': msg_author_id,
	                    'type': msg_type,
	                    'date': msg_date,
	                }
	                clean_messages.append(clean_msg)
		 */
		JsonArray messages = originalChange.getJsonArray("messages");
		return messages == null ? Json.createArrayBuilder().build() : messages;
	}

	private JsonArray getReviewers(JsonObject originalChange) {
		// TODO Auto-generated method stub
		
		/* original python code:
		 reviewers = []
	            clean_messages = []
	            for msg in messages:
	                msg_id = msg['id']
	
	                if 'author' not in msg:
	                    dropped += 1
	                    continue
	
	                if '_account_id' not in msg['author']:
	                    dropped += 1
	                    continue
	                msg_author_id = msg['author']['_account_id']
	                reviewers.append(msg_author_id)
	                msg_date = msg['date'][0:19]
	                msg_type = ("review", "self")[owner_id == msg_author_id]
	
	                clean_msg = {
	                    'id': msg_id,
	                    'author': msg_author_id,
	                    'type': msg_type,
	                    'date': msg_date,
	                }
	                clean_messages.append(clean_msg)
	
	            reviewers = list(set(reviewers))
	
	            if owner_id in reviewers:
	                reviewers.remove(owner_id) 
		 
		 */
		JsonArray messages = getMessages();
		JsonObject message;
		JsonArrayBuilder reviewer = Json.createArrayBuilder();
		JsonArray reviewers = null;
		String name = "";
		
		for (int i = 0; i< messages.size();i++) {
			message = messages.getJsonObject(i);
			try {
				name = message.getJsonObject("author").getString("name");
			} catch(NullPointerException e) {
				name = "";
			}
			if (message.getString("message").contains("Code-Review")) {
				reviewer.add(Json.createObjectBuilder()
								.add("id",message.getJsonObject("author").getInt("_account_id"))
								.add("name",name));			
			}
		}
		reviewers = reviewer.build();
		
		return reviewers;
	}
	
	public List<Reviewer> getFullReviewers(){
		List<Reviewer>  rev = new ArrayList<>();
		for(int i = 0; i < this.reviewers.size(); i++) {
			rev.add(new Reviewer(this.reviewers.getJsonObject(i).getInt("id"),this.reviewers.getJsonObject(i).getString("name")));
		}
		return rev;
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

	public JsonArray getReviewers() {
		return reviewers;
	}

	public void setReviewers(JsonArray reviewers) {
		this.reviewers = reviewers;
	}

	public JsonArray getMessages() {
		return messages;
	}

	public void setMessages(JsonArray messages) {
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
