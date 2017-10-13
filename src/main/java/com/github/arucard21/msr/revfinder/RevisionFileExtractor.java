package com.github.arucard21.msr.revfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class RevisionFileExtractor implements Function<JsonValue, List<RevisionFile>>{

	@Override
	public List<RevisionFile> apply(JsonValue value) {
		List<RevisionFile> revisionFiles = new ArrayList<>();
		JsonObject change = value.asJsonObject();
		JsonObject revisions = change.getJsonObject("revisions");
		for (String revisionHash: revisions.keySet()) {
			JsonObject revision = revisions.getJsonObject(revisionHash);
			if (revision.containsKey("files")) {
				JsonObject files = revision.getJsonObject("files");
				for (String filePath : files.keySet()) {
					JsonObject file = files.getJsonObject(filePath);
					revisionFiles.add(
							new RevisionFile(
									filePath, 
									file.getInt("lines_inserted"), 
									file.getInt("lines_deleted"), 
									file.getInt("sizes_delta"), 
									file.getInt("size")));
				}
			}
		}
		return revisionFiles;
	}

}
