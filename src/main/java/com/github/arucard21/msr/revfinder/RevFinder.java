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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class RevFinder {
	private final Project project;
	private List<ReviewableChange> changes;
	private List<ReviewableChange> moreFilteredChanges;
	
	public RevFinder(Project project) {
		this.project = project;
		changes = loadChanges("filtered/%s_changes.json");
		moreFilteredChanges = loadChanges("filtered/%s_changes_within_period.json");
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
	
	
	
	public List<ReviewableChange> getChanges(boolean moreFiltered) {
		return moreFiltered ? moreFilteredChanges : changes;
	}

	public double getAverageNumberFiles(boolean moreFiltered) {
		OptionalDouble average = getChanges(moreFiltered).stream()
				.mapToDouble(change -> new Double(change.getFiles().size()))
				.average();
		return average.isPresent() ? average.getAsDouble() : 0.0;
	}

	public List<GerritUser> generateReviewerRecommendations(ReviewableChange change, boolean moreFiltered){
		List<ReviewableChange> pastChanges = getPastReviews(change, moreFiltered);
		Collections.sort(pastChanges, (change1, change2) -> (change1.getCreated().compareTo(change2.getCreated())));
		Map<GerritUser, Double> reviewersWithRecommendationScore;
		Map<GerritUser, Integer> reviewersWithRecommendationRank = new HashMap<>();
		Map<GerritUser, Integer> combinedReviewersRecommendationRank = new HashMap<>();
		double score,scoreRp;
		int rank;
		

		for (int i = 0;i<=3;i++) {
			reviewersWithRecommendationScore = new HashMap<>();
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
				if (!(filesN.size() == 0)) {
					scoreRp /= filesN.size();
				}
				if (!(filesP.size() == 0)) {
					scoreRp /= filesP.size();
				}
				for(GerritUser reviewer: getReviewersOfChange(reviewPast)) {
					score = reviewersWithRecommendationScore.getOrDefault(reviewer, new Double(0.0));
					reviewersWithRecommendationScore.put(reviewer, score + scoreRp);
				}
				
				
			}
			for (GerritUser reviewer: reviewersWithRecommendationScore.keySet()) {
				if (i == 0) {
					reviewer.setLCPScore(reviewersWithRecommendationScore.get(reviewer));
				}else if (i == 1) {
					reviewer.setLCSuffScore(reviewersWithRecommendationScore.get(reviewer));
				}else if (i == 2) {
					reviewer.setLCSubstrScore(reviewersWithRecommendationScore.get(reviewer));
				}else if (i == 3) {
					reviewer.setLCSubseqScore(reviewersWithRecommendationScore.get(reviewer));
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

	public void generateReviewerRecommendations(boolean moreFiltered){
    	HashMap<String, Object> config = new HashMap<>();
    	config.put(JsonGenerator.PRETTY_PRINTING, true);
		File outputFile = getResourceFile(String.format(getRecommendationsFilename(moreFiltered), project.name));
    	if (outputFile.exists()) {
    		return;
    	}
    	try {
			outputFile.createNewFile();
	    	JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(new FileWriter(outputFile));
	    	generator.writeStartArray();

	    	for (ReviewableChange change : getChanges(moreFiltered)) {
	    		generator.writeStartObject();
	    		generator.write("review_id", change.getId());
	    		generator.writeStartArray("recommended_reviewers");
	    		generateReviewerRecommendations(change, moreFiltered)
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

	private List<ReviewableChange> getPastReviews(ReviewableChange currentChange, boolean moreFiltered) {
		return getChanges(moreFiltered).stream()
				.filter(review -> review.getCreated().isBefore(currentChange.getCreated()))
				.collect(Collectors.toList());
	}
	
	public double calculateTopKAccuracy(int topK, boolean last, boolean moreFiltered) {
		double topKAccuracy = 0.0;
		
		for (ReviewableChange r: getChanges(moreFiltered)) {
			topKAccuracy += isCorrect(r, topK, last,moreFiltered);
		}
		if(getChanges(moreFiltered).size() > 0) {
			return  topKAccuracy * 100 / getChanges(moreFiltered).size();
		}
		else {			
			return 0;
		}
	}

	private double isCorrect(ReviewableChange change, int topK, boolean last, boolean moreFiltered) {
		List<GerritUser> topKReviewers = candidates(change, moreFiltered);
		//GerritUser actualReviewer = getFirst(change, last);
		//if(actualReviewer == null) {
			//return 0.0;
		//}
		List<GerritUser> actualReviewers = getActualReviewers(change);
		
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
	
	private GerritUser getFirstNonBotMessage(ReviewableChange change, boolean last) {
		List<GerritUser> bots = new ArrayList<>();
		bots.add(new GerritUser(3,"Jenkins"));
		bots.add(new GerritUser(9,"LaunchpadSync"));
		bots.add(new GerritUser(2166,"SmokeStack CI"));
		bots.add(new GerritUser(5494,"Trivial Rebase"));
		bots.add(new GerritUser(8871,"Elastic Recheck"));
		
		List<GerritUser> first = change.getMessages().parallelStream()
				.filter(message -> !bots.contains(message.getAuthor()))
				.map(message -> message.getAuthor())
				.collect(Collectors.toList());
		if (first.size() != 0) {
			return first.get(0);
		}else {
			return null;
		}
	}
	
	private GerritUser getChronologically(List<GerritUser> reviewersWithScore2, ReviewableChange change, boolean last) {
		List<Message> reviewerMessages = change.getMessages().parallelStream()
				.filter(message -> reviewersWithScore2.contains(message.getAuthor()))
				.collect(Collectors.toList());
		Collections.sort(reviewerMessages, (message1, message2) -> (message1.getDate().compareTo(message2.getDate())));
		if (last) {
			Collections.reverse(reviewerMessages);
		}
		return reviewerMessages.get(0).getAuthor();
	}

	private List<GerritUser> getReviewersOfChange(ReviewableChange change) {
		return change.getReviews().parallelStream()
					.map(review -> review.getReviewer())
					.collect(Collectors.toList());
	}
	
	public double calculateMRR(boolean last, boolean moreFiltered) {
		double mRR = 0.0;
		int temp;
		
		for (ReviewableChange r: getChanges(moreFiltered)) {
			temp = rank(candidates(r, moreFiltered), r, last, moreFiltered);
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

	private int rank(List<GerritUser> candidates, ReviewableChange r, boolean last, boolean moreFiltered) {
		int lowestRank = -1;
		//GerritUser actualReviewer = getFirst(r, last);
		//if(actualReviewer == null) {
			//return 0;
		//}
		List<GerritUser> actualReviewers = getActualReviewers(r);
		
		for (int i = 0; i < candidates.size(); i++) {
			if(actualReviewers.contains(candidates.get(i))) {
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
