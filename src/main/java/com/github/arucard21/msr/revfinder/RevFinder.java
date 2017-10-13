package com.github.arucard21.msr.revfinder;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.github.arucard21.msr.ChangePreprocessor;
import com.github.arucard21.msr.CodeReview;
import com.github.arucard21.msr.Project;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;


public class RevFinder {
	private double filePathSimilarity(String filen, String filep,int ck) {
		return new FilePathSimilarityComparator().compare(filen,filep,ck)/Math.max(filen.length(),filep.length());
	}

	private List<RevisionFile> getFiles(Project project, CodeReview review) {
		List<RevisionFile> revisionFiles = new ArrayList<>();
		JsonObject revisions = review.getRevisions();
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

	private File getResourceFile(String filename) {
		return new File("src/main/resources/", filename);
	}

	private List<Reviewer> getCodeReviewers(CodeReview review){
		List<Reviewer> reviewers = new ArrayList<>();
		return reviewers;
	}

	public void generateReviewerRecommendations(Project project, CodeReview review){
		List<CodeReview> pastReviews = getPastReviews(project, review);
		Collections.sort(pastReviews, (review1, review2) -> (review1.getCreated().compareTo(review2.getCreated())));
		Map<String, Double> C = new HashMap<>();
		double score,scoreRp;
		int ck = 0;
		
		for (CodeReview reviewPast: pastReviews) {
			List<RevisionFile> filesN = getFiles(project, review);
			List<RevisionFile> filesP = getFiles(project, reviewPast);
			
			scoreRp = 0.0;
			for (RevisionFile fileN : filesN) {
				for (RevisionFile fileP : filesP) {
					scoreRp += filePathSimilarity(fileP.getFileName(),fileN.getFileName(),ck);
				}
			}
			scoreRp /= ((filesN.size()) * (filesP.size()));
			
			for(Reviewer codeReviewer: getCodeReviewers(reviewPast)) {
				score = C.getOrDefault(codeReviewer.getId(), new Double(0.0));
				C.put(codeReviewer.getId(), score + scoreRp);
			}
		}
	}

	public void generateReviewerRecommendations(Project project){
		List<CodeReview> reviews = getReviews(project);
		for (CodeReview review : reviews) {
			generateReviewerRecommendations(project, review);
		}
	}
	
	private List<CodeReview> getReviews(Project project) {
		List<CodeReview> reviews = new ArrayList<>();
		try {
		    JsonParser parser = Json.createParser(new FileReader(getResourceFile(String.format("filtered/%s_changes.json", project.name))));
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    			reviews = parser.getArrayStream()
		    					.map(codeReviewJson -> new CodeReview(codeReviewJson.asJsonObject()))
		    					.collect(Collectors.toList());
		    	}
		    }                
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return reviews;
	}

	private List<CodeReview> getPastReviews(Project project, CodeReview currentReview) {
		return getReviews(project)
				.stream()
				.filter(review -> review.getCreated().isBefore(currentReview.getCreated()))
				.collect(Collectors.toList());
	}
}
