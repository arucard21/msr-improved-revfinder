package com.github.arucard21.msr.revfinder;


import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
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
	private List<ReviewableChange> moreFilteredChanges;
	
	public RevFinder(Project project) {
		this.project = project;
		changes = loadChanges("filtered/%s_changes.json", project);
		moreFilteredChanges = loadChanges("filtered/%s_changes_within_period.json", project);
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

	private List<GerritUser> generateReviewerRecommendations(ReviewableChange change, boolean moreFiltered){
		List<ReviewableChange> pastChanges = getPastReviews(change, moreFiltered);
		Collections.sort(pastChanges, (change1, change2) -> (change1.getCreated().compareTo(change2.getCreated())));
		Map<GerritUser, Double> reviewersWithRecommendationScore;
		Map<GerritUser, Integer> reviewersWithRecommendationRank = new HashMap<>();
		Map<GerritUser, Integer> combinedReviewersRecommendationRank = new HashMap<>();
		double score,scoreRp;
		int rank;
		List<RevisionFile> filesN = change.getFiles();

		for (int i = 0;i<=3;i++) {
			reviewersWithRecommendationScore = new HashMap<>();
			for (ReviewableChange reviewPast: pastChanges) {
				
				List<RevisionFile> filesP = reviewPast.getFiles();
				
				if(filesP.size() == 0) {
					continue;
				}
				
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
				
				scoreRp /= filesP.size();
				
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
		List<GerritUser> rankedReviewersByScore = combinedReviewersRecommendationRank.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		Collections.reverse(rankedReviewersByScore); // ensure high score is ranked first
		return rankedReviewersByScore;
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
	    		generator.write("created", change.getCreated().toString());
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

	private List<ReviewableChange> getPastReviews(ReviewableChange currentChange, boolean moreFiltered) {
		return getChanges(moreFiltered).stream()
				.filter(review -> review.getCreated().isBefore(currentChange.getCreated()))
				.collect(Collectors.toList());
	}
	
	private List<GerritUser> getReviewersOfChange(ReviewableChange change) {
		return change.getReviews().parallelStream()
					.map(review -> review.getReviewer())
					.collect(Collectors.toList());
	}
}
