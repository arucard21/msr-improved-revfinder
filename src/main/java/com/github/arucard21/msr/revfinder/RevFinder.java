package com.github.arucard21.msr.revfinder;


import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import javax.json.stream.JsonParser.Event;
import com.github.arucard21.msr.ReviewableChange;
import com.github.arucard21.msr.Project;
import java.util.Collections;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class RevFinder {
	private final Project project;
	private List<ReviewableChange> changes;
	
	public RevFinder(Project project) {
		this.project = project;
		loadChanges();
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

	private File getResourceFile(String filename) {
		return new File("src/main/data/", filename);
	}
	
	public double getAverageNumberFiles() {
		OptionalDouble average = changes.stream()
				.mapToDouble(change -> new Double(change.getFiles().size()))
				.average();
		return average.isPresent() ? average.getAsDouble() : 0.0;
	}

	public List<GerritUser> generateReviewerRecommendations(ReviewableChange change){
		List<ReviewableChange> pastChanges = getPastReviews(change);
		Collections.sort(pastChanges, (change1, change2) -> (change1.getCreated().compareTo(change2.getCreated())));
		Map<GerritUser, Double> reviewersWithRecommendationScore = new HashMap<>();
		Map<GerritUser, Integer> reviewersWithRecommendationRank = new HashMap<>();
		Map<GerritUser, Integer> combinedReviewersRecommendationRank = new HashMap<>();
		double score,scoreRp;
		int rank;
		
		for (int i = 0;i<3;i++) {
			for (ReviewableChange reviewPast: pastChanges) {
				List<RevisionFile> filesN = change.getFiles();
				List<RevisionFile> filesP = reviewPast.getFiles();
				
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
				
				for(GerritUser reviewer: getReviewersOfChange(reviewPast)) {
					score = reviewersWithRecommendationScore.getOrDefault(reviewer, new Double(0.0));
					reviewersWithRecommendationScore.put(reviewer, score + scoreRp);
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

	    	for (ReviewableChange change : changes) {
	    		generator.writeStartObject();
	    		generator.write("review_id", change.getId());
	    		generator.writeStartArray("recommended_reviewers");
	    		generateReviewerRecommendations(change)
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

	private void loadChanges() {
		try {
			File filteredChangesFile = getResourceFile(String.format("filtered/%s_changes.json", project.name));
	    	if (!filteredChangesFile.exists()) {
	    		changes = Collections.emptyList();
	    	}
		    JsonParser parser = Json.createParser(new FileReader(filteredChangesFile));
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    			changes = parser.getArrayStream()
		    					.map(changeJSON -> new ReviewableChange(changeJSON.asJsonObject(), true))
		    					.collect(Collectors.toList());
		    	}
		    }                
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private List<ReviewableChange> getPastReviews(ReviewableChange currentChange) {
		return changes.stream()
				.filter(review -> review.getCreated().isBefore(currentChange.getCreated()))
				.collect(Collectors.toList());
	}
	
	public double calculateTopKAccuracy(int topK, boolean last) {
		double topKAccuracy = 0.0;
		
		
		for (ReviewableChange r: changes) {
			topKAccuracy += isCorrect(r, topK, last);
		}
		if(changes.size() > 0) {
			return  topKAccuracy * 100 / changes.size();
		}
		else {			
			return 0;
		}
	}

	private double isCorrect(ReviewableChange change, int topK, boolean last) {
		List<GerritUser> topKReviewers = candidates(change);
		GerritUser actualReviewer = getActualReviewer(change, last);
		
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
	
	public double calculateMRR(boolean last) {
		double mRR = 0.0;
		int temp;
		
		for (ReviewableChange r: changes) {
			temp = rank(candidates(r), r, last);
			if (temp != 0) {
				mRR +=  (double) 1/temp;
			}
	
		}
		if(changes.size() > 0) {
			return mRR / changes.size();
		}
		else {
			return 0;
		}
	}

	private int rank(List<GerritUser> candidates, ReviewableChange r, boolean last) {
		int lowestRank = -1;
		GerritUser actualReviewer = getActualReviewer(r, last);
		
		
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

	private List<GerritUser> candidates(ReviewableChange r) {
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
