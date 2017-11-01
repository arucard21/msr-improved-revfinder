package com.github.arucard21.msr.revfinder;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import javax.json.stream.JsonParser.Event;
import com.github.arucard21.msr.ReviewableChange;
import com.github.arucard21.msr.Project;

import java.util.Collections;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class RevFinderEvaluation {
	private final Project project;
	private List<ReviewableChange> changes;
	private List<ReviewableChange> moreFilteredChanges;
	private List<ReviewRecommendations> recommendations;
	private List<ReviewRecommendations> moreFilteredRecommendations;
	
	public RevFinderEvaluation(Project project) {
		this.project = project;
		this.changes = loadChanges("filtered/%s_changes.json", project);
		this.moreFilteredChanges = loadChanges("filtered/%s_changes_within_period.json", project);
		this.recommendations = loadRecommendations(false);
		this.moreFilteredRecommendations = loadRecommendations(true);
	}

	public List<ReviewableChange> getChanges(boolean moreFiltered) {
		return moreFiltered ? moreFilteredChanges : changes;
	}

	public List<ReviewRecommendations> getRecommendations(boolean moreFiltered) {
		return moreFiltered ? moreFilteredRecommendations : recommendations;
	}
	
	private List<ReviewableChange> loadChanges(String changesFile, Project project) {
		try {
			File filteredChangesFile = getResourceFile(String.format(changesFile, project.name));
	    	if (!filteredChangesFile.exists()) {
	    		return Collections.emptyList();
	    	}
		    JsonParser parser = Json.createParser(new FileReader(filteredChangesFile));
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    			return parser.getArrayStream()
		    					.map(changeJSON -> new ReviewableChange(changeJSON.asJsonObject(), true))
		    					.collect(Collectors.toList());
		    	}
		    }                
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private List<ReviewRecommendations> loadRecommendations(boolean moreFiltered) {
		try {
		    File resource = getResourceFile(String.format(getRecommendationsFilename(moreFiltered), project.name));
		    if(!resource.exists()) {
		    	System.err.printf("Recommendations file %s has not been generated yet\n", resource.getName());
		    	return Collections.emptyList();
		    }
			JsonParser parser = Json.createParser(new FileReader(resource));
		    if(parser.hasNext()) {
		    	try {
			    	if (parser.next() == Event.START_ARRAY) {
			    		// no parallel() here since it's important to keep the order of the recommendations intact
			    		return parser.getArrayStream()
			    				.map(recommendationJSON -> new ReviewRecommendations(recommendationJSON.asJsonObject().getString("review_id"), recommendationJSON.asJsonObject().getJsonArray("recommended_reviewers")))
			    				.collect(Collectors.toList());
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

	private static File getResourceFile(String filename) {
		return new File("src/main/data/", filename);
	}

	private static String getRecommendationsFilename(boolean moreFiltered) {
		return moreFiltered ? "revfinder/%s_recommendations_from_within_period.json" : "revfinder/%s_recommendations.json";
	}

	public Map<Integer, Double> calculateTopKAccuracy(List<Integer> valuesForK, boolean moreFiltered) {
		Map<Integer, Double> topKAccuracies = new HashMap<>();
		//ensure that the accuracy for each K is never null
		for (Integer valueForK : valuesForK) {
			topKAccuracies.put(valueForK, 0.0);
		}
		
		for (ReviewableChange r: getChanges(moreFiltered)) {
			Map<Integer, Boolean> correctPerK = isCorrect(r, valuesForK, moreFiltered);
			for (Integer valueForK : valuesForK) {
				topKAccuracies.put(valueForK, topKAccuracies.get(valueForK) + (correctPerK.get(valueForK) ? 1.0 : 0.0));
			}
		}
		if(getChanges(moreFiltered).size() > 0) {
			for (Integer valueForK : valuesForK) {
				topKAccuracies.put(valueForK, ((topKAccuracies.get(valueForK) * 100) / getChanges(moreFiltered).size()));
			}
		}
		return topKAccuracies;
	}

	private Map<Integer, Boolean> isCorrect(ReviewableChange change, List<Integer> valuesForK, boolean moreFiltered) {
		List<GerritUser> topKReviewers = candidates(change, moreFiltered, project);
		List<GerritUser> actualReviewers = getActualReviewers(change);
		Map<Integer, Boolean> correctPerK = new HashMap<>();
		
		for(int i = 0; i < topKReviewers.size(); i++) {
			GerritUser topKReviewer = topKReviewers.get(i); 
			if (actualReviewers.contains(topKReviewer)) {
				for (Integer valueForK : valuesForK) {
					if (i < valueForK) {
						correctPerK.put(valueForK, true);
						break;
					}
				}
			}
		}
		// make sure every k has a value
		for(Integer valueForK : valuesForK) {
			if (correctPerK.get(valueForK) == null) {
				correctPerK.put(valueForK, false);
			}
		}
		return correctPerK;
	}

	private List<GerritUser> getActualReviewers(ReviewableChange change) {
		List<CodeReview> reviews = change.getReviews();
		List<GerritUser> reviewersWithScore2 = reviews.parallelStream()
				.filter(review -> review.getReviewScore() == 2)
				.map(review -> review.getReviewer())
				.collect(Collectors.toList());
		if(reviewersWithScore2.size() >= 1) {	
			return reviewersWithScore2;
		}
		else{
			return reviews.parallelStream()
					.filter(review -> review.getReviewScore() == 1)
					.map(review -> review.getReviewer())
					.collect(Collectors.toList());
		}
	}

	public double calculateMRR(boolean moreFiltered) {
		double mRR = 0.0;
		int temp;
		
		for (ReviewableChange r: getChanges(moreFiltered)) {
			temp = rank(candidates(r, moreFiltered, project), r, moreFiltered);
			if (temp != 0) {
				mRR +=  (double) 1/temp;
			}
	
		}
		if(getChanges(moreFiltered).size() > 0) {
			return mRR / getChanges(moreFiltered).size();
		}
		else {
			return 0;
		}
	}

	private int rank(List<GerritUser> candidates, ReviewableChange r, boolean moreFiltered) {
		int currentRank = 0;
		List<GerritUser> actualReviewers = getActualReviewers(r);
		
		while(currentRank < candidates.size()) {
			if(actualReviewers.contains(candidates.get(currentRank))) {
				break;
			}
			currentRank++;
		}
		return currentRank;
	}

	public List<GerritUser> candidates(ReviewableChange r, boolean moreFiltered, Project project) {
		// no parallelStream() here since it's important to keep the order of the recommendations intact
		return getRecommendations(moreFiltered).stream()
				.filter(recommendation -> recommendation.getReviewID().equals(r.getId()))
				.map(recommendation -> recommendation.getRecommendedReviewers())
				.flatMap(listOfReviewers -> listOfReviewers.stream())
				.distinct()
				.collect(Collectors.toList());
	}
}
