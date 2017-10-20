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
import java.util.Arrays;
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

	private double getAvailability(Reviewer reviewer, LocalDateTime date) {
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

	private double getWorkload(Reviewer reviewer) {
		double workload;
		
		workload = getNumberFiles(reviewer)/getAverageNumberFiles();
		return workload;
	}
	
	private int getAverageNumberFiles() {
		
		return 0;
	}

	private int getNumberFiles(Reviewer reviewer) {
		
		return 0;
	}

	private int getNumberReviews(LocalDateTime date, Reviewer reviewer) {
		return 0;
	}
	
	private List<Reviewer> getCodeReviewers(CodeReview review){
		List<Reviewer> reviewers = new ArrayList<>();
		reviewers = review.getFullReviewers();
		return reviewers;
	}

	public List<Reviewer> generateReviewerRecommendations(CodeReview review){
		List<CodeReview> pastReviews = getPastReviews(review);
		Collections.sort(pastReviews, (review1, review2) -> (review1.getCreated().compareTo(review2.getCreated())));
		Map<Reviewer, Double> reviewersWithRecommendationScore = new HashMap<>();
		Map<Reviewer, Integer> reviewersWithRecommendationRank = new HashMap<>();
		Map<Reviewer, Integer> combinedReviewersRecommendationRank = new HashMap<>();
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
		List<Reviewer> tempList = combinedReviewersRecommendationRank.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
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
		    					.map(codeReviewJson -> new CodeReview(codeReviewJson.asJsonObject()))
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
		topKAccuracy /= reviews.size() * 100;
		return topKAccuracy;
	}

	private double isCorrect(CodeReview r, int topK) {
		List<Reviewer> topKReviewers = new ArrayList<>();
		List<Reviewer> actualReviewers = r.getFullReviewers();
		
		for(Reviewer topKReviewer: topKReviewers) {
			if (actualReviewers.contains(topKReviewer)) {
				return 1;
			}
		}
		return 0;
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

	private int rank(List<Reviewer> candidates, CodeReview r) {
		int[] ranks = new int[10];
		int j = 0;
		List<Reviewer> actualReviewers = r.getFullReviewers();
		
		for (Reviewer actualReviewer: actualReviewers) {
			for (int i = 0; i < candidates.size(); i++) {
				if (candidates.get(i).equals(actualReviewer)) {
					ranks[j] = i;
					j++;
				}
			}
		}
		Arrays.sort(ranks);
		return ranks[ranks.length-1];
	}

	private List<Reviewer> candidates(CodeReview r) {
		List<Reviewer> reviewers = new ArrayList<>();
		try {
		    JsonParser parser = Json.createParser(new FileReader(getResourceFile(String.format("revfinder/%s_recommendations.json", project.name))));
		    while(parser.hasNext()) {
		    	if (parser.next() == Event.KEY_NAME) {
		    		if (parser.getString().equals("review_id")) {
		    			if (parser.next() == Event.VALUE_STRING) {
		    				if (parser.getString().equals(r.getId())) {
		    					if (parser.next() == Event.START_ARRAY) {
		    						reviewers = parser.getArrayStream()
		    			    					.map(reviewerJson -> new Reviewer(reviewerJson.asJsonObject().getInt("ID"),reviewerJson.asJsonObject().getString("Name")))
		    			    					.collect(Collectors.toList());
		    					}
		    				}
		    			}
		    		}
		    	}
		    }			
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return reviewers;
	}
}
