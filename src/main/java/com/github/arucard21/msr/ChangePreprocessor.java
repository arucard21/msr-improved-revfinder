package com.github.arucard21.msr;

import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class ChangePreprocessor implements Function<JsonValue, JsonObject> {

	@Override
	public JsonObject apply(JsonValue t) {
		JsonObject originalChange = t.asJsonObject();
		  String id = originalChange.getString("id", "");
          String change_id = originalChange.getString("change_id", "");
          int owner_id = originalChange.getJsonObject("owner").getInt("_account_id", -1);
          String status = originalChange.getString("status", "");
          String created = originalChange.getString("created", "");
          String updated = originalChange.getString("updated", "");
          int insertions = originalChange.getInt("insertions", -1);
          int deletions = originalChange.getInt("deletions", -1);
          String current_revision = originalChange.getString("current_revision", "");
          String reviewers = getReviewers(originalChange);
          JsonArray messages = getMessages(originalChange);
          JsonObject revisions = getRevisions(originalChange);
          String project = originalChange.getString("project", "");
          int number = originalChange.getInt("_number", -1);

		return Json.createObjectBuilder()
				.add("id", id)
				.add("change_id", change_id)
				.add("owner", owner_id)
				.add("status", status)
				.add("created", created)
				.add("updated", updated)
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

	private String getReviewers(JsonObject originalChange) {
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
		return "";
	}

}
