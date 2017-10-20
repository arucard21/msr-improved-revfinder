package com.github.arucard21.msr.revfinder;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
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
import java.time.LocalDateTime;


public class RevFinder {
	private final Project project;
	private final List<CodeReview> reviews;
	
	public RevFinder(Project project) {
		this.project = project;
		this.reviews = loadReviews();
	}

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

	private List<RevisionFile> getFiles(CodeReview review) {
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
		return new File("src/main/data/", filename);
	}

	private double getAvailability(GerritUser reviewer, LocalDateTime date) {
		double[] availability = new double[7];
		int reviews = getNumberReviews(date, reviewer);
		double available = 0.0;
		double logSum = 0.0;
		
		for (int i = 0; i < 7; i++) {
			if (reviews > 0) {
				availability[i] = Math.log(7 - i)/Math.log(7);
			}
			available += availability[i];
			logSum += Math.log(7 - i)/Math.log(7);
		}
		return available;
	}

	private double getWorkload(GerritUser reviewer) {
		double workload;
		
		workload = getNumberFiles(reviewer)/getAverageNumberFiles();
		return workload;
	}
	
	private int getAverageNumberFiles() {
		
		return 0;
	}

	private int getNumberFiles(GerritUser reviewer) {
		
		return 0;
	}

	private int getNumberReviews(LocalDateTime date, GerritUser reviewer) {
		return 0;
	}
	
	private List<GerritUser> getCodeReviewers(CodeReview review){
		List<GerritUser> reviewers = new ArrayList<>();
		reviewers = review.getReviewers();
		return reviewers;
	}

	public List<GerritUser> generateReviewerRecommendations(CodeReview review){
		List<CodeReview> pastReviews = getPastReviews(review);
		Collections.sort(pastReviews, (review1, review2) -> (review1.getCreated().compareTo(review2.getCreated())));
		Map<GerritUser, Double> reviewersWithRecommendationScore = new HashMap<>();
		Map<GerritUser, Integer> reviewersWithRecommendationRank = new HashMap<>();
		Map<GerritUser, Integer> combinedReviewersRecommendationRank = new HashMap<>();
		double score,scoreRp;
		int rank;
		
		for (int i = 0;i<3;i++) {
			for (CodeReview reviewPast: pastReviews) {
				List<RevisionFile> filesN = getFiles(review);
				List<RevisionFile> filesP = getFiles(reviewPast);
				
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
				
				for(GerritUser codeReviewer: getCodeReviewers(reviewPast)) {
					score = reviewersWithRecommendationScore.getOrDefault(codeReviewer, new Double(0.0));
					reviewersWithRecommendationScore.put(codeReviewer, score + scoreRp);
				}
				
			}
			reviewersWithRecommendationRank = new FilePathSimilarityComparator().combination(reviewersWithRecommendationScore);
			for (GerritUser ck:reviewersWithRecommendationRank.keySet()) {
				rank = combinedReviewersRecommendationRank.getOrDefault(ck, new Integer(0));
				combinedReviewersRecommendationRank.put(ck, rank + reviewersWithRecommendationRank.get(ck));
			}
		}
		return getRankedReviewerList(combinedReviewersRecommendationRank);
	}

	private List<GerritUser> getRankedReviewerList(Map<GerritUser, Integer> combinedReviewersRecommendationRank) {
		List<GerritUser> tempList = combinedReviewersRecommendationRank.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		Collections.reverse(tempList);
		if (tempList.size() > 9) {
			return tempList.subList(0, 9);
		}
		else {
			return tempList.subList(0, tempList.size());
		}
	}

	public void generateReviewerRecommendations(){
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

	    	for (CodeReview review : reviews) {
	    		generator.writeStartObject();
	    		generator.write("review_id", review.getId());
	    		generator.writeStartArray("recommended_reviewers");
	    		generateReviewerRecommendations(review)
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

	private List<CodeReview> loadReviews() {
		List<CodeReview> reviews = new ArrayList<>();
		try {
		    JsonParser parser = Json.createParser(new FileReader(getResourceFile(String.format("filtered/%s_changes.json", project.name))));
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    			reviews = parser.getArrayStream()
		    					.map(codeReviewJson -> new CodeReview(codeReviewJson.asJsonObject(), false))
		    					.collect(Collectors.toList());
		    	}
		    }                
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return reviews;
	}

	private List<CodeReview> getPastReviews(CodeReview currentReview) {
		return reviews.stream()
				.filter(review -> review.getCreated().isBefore(currentReview.getCreated()))
				.collect(Collectors.toList());
	}
	
	public double calculateTopKAccuracy(int topK) {
		double topKAccuracy = 0.0;
		
		
		for (CodeReview r: reviews) {
			topKAccuracy += isCorrect(r, topK);
		}
		topKAccuracy  =  topKAccuracy* 100 / reviews.size();
		return topKAccuracy;
	}

	private double isCorrect(CodeReview r, int topK) {
		List<GerritUser> topKReviewers = candidates(r);
		List<GerritUser> actualReviewers = r.getReviewers();
		
		if (topKReviewers.size() > topK) {
			topKReviewers = topKReviewers.subList(0, topK-1);
		}
		
		for(GerritUser topKReviewer: topKReviewers) {
			if (actualReviewers.contains(topKReviewer)) {
				return 1.0;
			}
		}
		return 0.0;
	}
	
	public double calculateMRR() {
		double mRR = 0.0;
		int temp;
		
		for (CodeReview r: reviews) {
			temp = rank(candidates(r), r);
			if (temp != 0) {
				mRR += 1/temp;
			}
		}
		mRR /= reviews.size();
		return mRR;
	}

	private int rank(List<GerritUser> candidates, CodeReview r) {
		int lowestRank = -1;
		List<GerritUser> actualReviewers = r.getReviewers();
		
		for (GerritUser actualReviewer: actualReviewers) {
			for (int i = 0; i < candidates.size(); i++) {
				if(actualReviewer.equals(candidates.get(i))) {
					if(lowestRank == -1) {
						lowestRank = i;
					}
					else {
						if(i < lowestRank) {
							lowestRank = i;
						}
					}					
				}
			}
		}
		return lowestRank;
	}

	private List<GerritUser> candidates(CodeReview r) {
		try {
		    File resource = getResourceFile(String.format("revfinder/%s_recommendations.json", project.name));
		    
			JsonParser parser = Json.createParser(new FileReader(resource));
		    if(parser.hasNext()) {
		    	try {
			    	if (parser.next() == Event.START_ARRAY) {
			    			List<ReviewRecommendations> recommendations = parser.getArrayStream()
			    					.map(recommendationJSON -> new ReviewRecommendations(recommendationJSON.asJsonObject().getString("review_id"), recommendationJSON.asJsonObject().getJsonArray("recommended_reviewers")))
			    					.filter(recommendation -> recommendation.getReviewID().equals(r.getId()))
			    					.collect(Collectors.toList());
			    			if (recommendations.size() == 1) {
			    				return recommendations.get(0).getRecommendedReviewers();
			    			}
			    			else {
			    				System.err.println(String.format("Review with ID %d gave 0 or multiple results", r.getId()));
			    			}
			    	}
		    	}
		    	catch(JsonParsingException e) {
		    		System.err.println("JSON Parsing error occurred with file: "+resource.getName());
		    	}
		    }			
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return Collections.emptyList();
	}
}
