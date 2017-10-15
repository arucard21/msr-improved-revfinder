package com.github.arucard21.msr.revfinder;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.github.arucard21.msr.CodeReview;
import com.github.arucard21.msr.Project;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;


public class RevFinder {
	private double filePathSimilarity(String filen, String filep) {
		return new FilePathSimilarityComparator().compare(filen,filep);
	}
	
	private double filePathSimilarity1(String filen, String filep) {
		return new FilePathSimilarityComparator().compare1(filen,filep);
	}
	
	private double filePathSimilarity2(String filen, String filep) {
		return new FilePathSimilarityComparator().compare2(filen,filep);
	}
	
	private double filePathSimilarity3(String filen, String filep) {
		return new FilePathSimilarityComparator().compare3(filen,filep);
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
									file.getInt("lines_inserted",0), 
									file.getInt("lines_deleted",0), 
									file.getInt("sizes_delta",0), 
									file.getInt("size",0)));
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
		reviewers = review.getFullReviewers();
		return reviewers;
	}

	public List<Reviewer> generateReviewerRecommendations(Project project, CodeReview review){
		List<CodeReview> pastReviews = getPastReviews(project, review);
		Collections.sort(pastReviews, (review1, review2) -> (review1.getCreated().compareTo(review2.getCreated())));
		Map<Reviewer, Double> reviewersWithRecommendationScore = new HashMap<>();
		Map<Reviewer, Integer> reviewersWithRecommendationRank = new HashMap<>();
		Map<Reviewer, Integer> combinedReviewersRecommendationRank = new HashMap<>();
		double score,scoreRp;
		int rank;
		
		for (int i = 0;i<3;i++) {
			for (CodeReview reviewPast: pastReviews) {
				List<RevisionFile> filesN = getFiles(project, review);
				List<RevisionFile> filesP = getFiles(project, reviewPast);
				
				scoreRp = 0.0;
				for (RevisionFile fileN : filesN) {
					for (RevisionFile fileP : filesP) {
						if (i == 0) {
							scoreRp += filePathSimilarity(fileP.getFileName(),fileN.getFileName());
						}
						else if (i == 1) {
							scoreRp += filePathSimilarity1(fileP.getFileName(),fileN.getFileName());
						}
						else if (i == 2) {
							scoreRp += filePathSimilarity2(fileP.getFileName(),fileN.getFileName());
						}
						else if (i == 3) {
							scoreRp += filePathSimilarity3(fileP.getFileName(),fileN.getFileName());
						}
					}
				}
				scoreRp /= ((filesN.size()) * (filesP.size()));
				
				for(Reviewer codeReviewer: getCodeReviewers(reviewPast)) {
					score = reviewersWithRecommendationScore.getOrDefault(codeReviewer, new Double(0.0));
					reviewersWithRecommendationScore.put(codeReviewer, score + scoreRp);
				}
				
			}
			reviewersWithRecommendationRank = new FilePathSimilarityComparator().combination(reviewersWithRecommendationScore);
			for (Reviewer ck:reviewersWithRecommendationRank.keySet()) {
				rank = combinedReviewersRecommendationRank.getOrDefault(ck, new Integer(0));
				combinedReviewersRecommendationRank.put(ck, rank + reviewersWithRecommendationRank.get(ck));
			}
		}
		return getRankedReviewerList(combinedReviewersRecommendationRank);
	}

	private List<Reviewer> getRankedReviewerList(Map<Reviewer, Integer> combinedReviewersRecommendationRank) {
		return combinedReviewersRecommendationRank.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	public void generateReviewerRecommendations(Project project){
    	HashMap<String, Object> config = new HashMap<>();
    	config.put(JsonGenerator.PRETTY_PRINTING, true);
    	File outputFile = getResourceFile(String.format("revfinder/%s_recommendations.json", project.name));
    	if (outputFile.exists()) {
    		return;
    	}
    	try {
			outputFile.createNewFile();
	    	JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(new FileWriter(outputFile));
	    	generator.writeStartArray();

	    	List<CodeReview> reviews = getReviews(project);
	    	for (CodeReview review : reviews) {
	    		generator.writeStartObject();
	    		generator.write("review_id", review.getId());
	    		generator.writeStartArray("recommended_reviewers");
	    		generateReviewerRecommendations(project, review)
	    				.stream()
	    				.map(reviewer -> reviewer.asJsonObject())
	    				.forEach(generator::write);
	    		generator.writeEnd();
	    		generator.writeEnd();
	    	}
	        generator.writeEnd();
	        generator.flush();
	        generator.close();
	        System.out.println(String.format("Generated recommended reviewers for %s", project.name));
    	} catch (IOException e) {
    		e.printStackTrace();
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
