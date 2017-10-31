package com.github.arucard21.msr.revfinder;


import com.github.arucard21.msr.Project;
import com.github.arucard21.msr.ReviewableChange;
import com.github.arucard21.msr.checker.AvailabilityChecker;
import org.json.simple.parser.ParseException;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class ResultFinder {
	private final Project project;
	private List<ReviewableChange> changes;
	//private List<ReviewableChange> moreFilteredChanges;
	private AvailabilityChecker AvChecker;

	public ResultFinder(Project project) throws IOException, ParseException {
		this.project = project;
		changes = loadChanges("filtered/%s_changes.json");

		AvChecker = new AvailabilityChecker();
		AvChecker.check(project);
		//moreFilteredChanges = loadChanges("filtered/%s_changes_within_period.json");
	}

	private File getResourceFile(String filename) {
		return new File("src/main/data/", filename);
	}
	
	public List<ReviewableChange> getChanges() {
		return changes;
		//return moreFiltered ? moreFilteredChanges : changes;
	}

	private String getRecommendationsFilename(boolean moreFiltered) {
		return moreFiltered ? "revfinder/%s_recommendations_from_within_period.json" : "revfinder/%s_recommendations.json";
	}

	private List<ReviewableChange> loadChanges(String changesFile) {
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
	
	public double calculateTopKAccuracy(int topK, boolean last, boolean moreFiltered) {
		double topKAccuracy = 0.0;
		
		for (ReviewableChange r: getChanges()) {
			topKAccuracy += isCorrect(r, topK, last, false);
		}
		if(getChanges().size() > 0) {
			return  topKAccuracy * 100 / getChanges().size();
		}
		else {			
			return 0;
		}
	}

	public double calculateTopKAccuracyBinaryAvailability(int topK) {
		double topKAccuracy = 0.0;

		for (ReviewableChange r: getChanges()) {
			topKAccuracy += isCorrectAndBinaryAvailable(r, topK, false, false);
		}
		if(getChanges().size() > 0) {
			return  topKAccuracy * 100 / getChanges().size();
		}
		else {
			return 0;
		}
	}

	public double calculateTopKAccuracyLogAvailability(int topK, double threshold) {
		double topKAccuracy = 0.0;

		for (ReviewableChange r: getChanges()) {
			topKAccuracy += isCorrectAndLogAvailable(r, topK, threshold, false, false);
		}
		if(getChanges().size() > 0) {
			return  topKAccuracy * 100 / getChanges().size();
		}
		else {
			return 0;
		}
	}

	private double isCorrect(ReviewableChange change, int topK, boolean last, boolean moreFiltered) {
		List<GerritUser> topKReviewers = candidates(change, moreFiltered);
		GerritUser actualReviewer = getActualReviewer(change, last);
		if(actualReviewer == null) {
			return 0.0;
		}
		
		if (topKReviewers.size() > topK) {
			topKReviewers = topKReviewers.subList(0, topK-1);
		}
		
		for(GerritUser topKReviewer: topKReviewers) {
			if (actualReviewer.equals(topKReviewer)) {
				return 1.0;
			}
		}
		return 0.0;
	}

	private double isCorrectAndBinaryAvailable(ReviewableChange change, int topK, boolean last, boolean moreFiltered) {
		List<GerritUser> topKReviewers = candidates(change, moreFiltered);
		GerritUser actualReviewer = getActualReviewer(change, last);
		if(actualReviewer == null) {
			return 0.0;
		}

		if (topKReviewers.size() > topK) {
			topKReviewers = topKReviewers.subList(0, topK-1);
		}

		for(GerritUser topKReviewer: topKReviewers)
		{
			String dateString = change.getCreated().toString().substring(0, 10);
			if(actualReviewer.equals(topKReviewer) && AvChecker.checkBinaryAvailabilityByDateString(dateString, topKReviewer.getId()))
			{
				return 1.0;
			}
		}
		return 0.0;
	}

	private double isCorrectAndLogAvailable(ReviewableChange change, int topK, double threshold, boolean last, boolean moreFiltered) {
		List<GerritUser> topKReviewers = candidates(change, moreFiltered);
		GerritUser actualReviewer = getActualReviewer(change, last);
		if(actualReviewer == null) {
			return 0.0;
		}

		if (topKReviewers.size() > topK) {
			topKReviewers = topKReviewers.subList(0, topK-1);
		}

		for(GerritUser topKReviewer: topKReviewers)
		{
			String dateString = change.getCreated().toString().substring(0, 10);
			if(actualReviewer.equals(topKReviewer) && AvChecker.checkLogAvailabilityByDateString(dateString, topKReviewer.getId()) > threshold)
			{
				return 1.0;
			}
		}
		return 0.0;
	}

	/**
	 * Get the actual reviewer of a change.
	 * 
	 * The actual reviewer is one that gave a +2 review score. 
	 * If multiple of these exist, we'll use the one that was 
	 * chronologically first in reviewing. 
	 * 
	 * @param change is the change for which we want the reviewer.
	 * @return the actual reviewer.
	 */
	private GerritUser getActualReviewer(ReviewableChange change, boolean last) {
		List<CodeReview> reviews = change.getReviews();
		List<GerritUser> reviewersWithScore2 = reviews.stream()
				.filter(review -> review.getReviewScore() == 2)
				.map(review -> review.getReviewer())
				.collect(Collectors.toList());
		if(reviewersWithScore2.size() >= 1) {
			if(reviewersWithScore2.size() == 1) {				
				return reviewersWithScore2.get(0);
			}
			else{
				return getChronologically(reviewersWithScore2, change, last);
			}
		}
		else{
			return null;
		}
	}
	
	private GerritUser getChronologically(List<GerritUser> reviewersWithScore2, ReviewableChange change, boolean last) {
		List<Message> reviewerMessages = change.getMessages().stream()
				.filter(message -> reviewersWithScore2.contains(message.getAuthor()))
				.collect(Collectors.toList());
		Collections.sort(reviewerMessages, (message1, message2) -> (message1.getDate().compareTo(message2.getDate())));
		if (last) {
			Collections.reverse(reviewerMessages);
		}
		return reviewerMessages.get(0).getAuthor();
	}

	private List<GerritUser> getReviewersOfChange(ReviewableChange change) {
		return change.getReviews().stream()
					.map(review -> review.getReviewer())
					.collect(Collectors.toList());
	}
	
	public double calculateMRR(boolean last, boolean moreFiltered) {
		double mRR = 0.0;
		int temp;
		
		for (ReviewableChange r: getChanges()) {
			temp = rank(candidates(r, false), r, last, false);
			if (temp != 0) {
				mRR +=  (double) 1/temp;
			}
	
		}
		if(getChanges().size() > 0) {
			return mRR / getChanges().size();
		}
		else {
			return 0;
		}
	}

	private int rank(List<GerritUser> candidates, ReviewableChange r, boolean last, boolean moreFiltered) {
		int lowestRank = -1;
		GerritUser actualReviewer = getActualReviewer(r, last);
		if(actualReviewer == null) {
			return 0;
		}
		
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
		
		if (lowestRank == -1) {
			return 0;
		}
		return lowestRank;
	}

	private List<GerritUser> candidates(ReviewableChange r, boolean moreFiltered) {
		try {
		    File resource = getResourceFile(String.format(getRecommendationsFilename(moreFiltered), project.name));
		    
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
			    				if (recommendations.size() == 0) {
			    					System.err.println(String.format("Review with ID %s gave 0 results", r.getId()));
			    				}
			    				else {
			    					System.err.println(String.format("Review with ID %s gave multiple results", r.getId()));
			    				}
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
