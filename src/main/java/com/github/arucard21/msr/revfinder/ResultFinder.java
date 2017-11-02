package com.github.arucard21.msr.revfinder;


import com.github.arucard21.msr.Project;
import com.github.arucard21.msr.checker.AvailabilityChecker;
import com.github.arucard21.msr.checker.WorkloadChecker;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class ResultFinder {
	private final Project project;
	private AvailabilityChecker AvChecker;
	private WorkloadChecker WlChecker;
	private RevFinderEvaluation revFinderEvaluation;

	public ResultFinder(Project project) throws Exception {
		this.project = project;

		AvChecker = new AvailabilityChecker();
		AvChecker.check(project);

		WlChecker = new WorkloadChecker();
		WlChecker.check(project);

		revFinderEvaluation = new RevFinderEvaluation(project);
	}

	private List<GerritUser> calcBinaryAVRecommendation(ReviewRecommendations reviewRecommendations) {
		List<GerritUser> availableReviewers = new ArrayList<>();
		String dateString = reviewRecommendations.getReviewCreated().toString().substring(0, 10);

		for(GerritUser recommendedReviewer: reviewRecommendations.getRecommendedReviewers())
		{
			boolean available = AvChecker.checkBinaryAvailabilityByDateString(dateString, recommendedReviewer.getId());
			if(available) {
				GerritUser availableReviewer = new GerritUser(recommendedReviewer.getId(), recommendedReviewer.getName());
				availableReviewer.setAVBinaryScore(available ? 1.0 : 0.0);
				availableReviewers.add(availableReviewer);				
			}
		}
		return availableReviewers;
	}

	private List<GerritUser> calcLogAVRecommendation(ReviewRecommendations reviewRecommendations, double threshold, boolean removeUnderThreshold) {
		List<GerritUser> availableReviewers = new ArrayList<>();
		String dateString = reviewRecommendations.getReviewCreated().toString().substring(0, 10);
		Map<GerritUser,Double> lCPScores = new HashMap<>();
		Map<GerritUser,Double> lCSuffScores = new HashMap<>();
		Map<GerritUser,Double> lCSubstrScores = new HashMap<>();
		Map<GerritUser,Double> lCSubseqScores = new HashMap<>();
		List<Map<GerritUser,Double>> allScores = new ArrayList<>();
		
		for(GerritUser recommendedReviewer: reviewRecommendations.getRecommendedReviewers())
		{
			float availableLikelihood = AvChecker.checkLogAvailabilityByDateString(dateString, recommendedReviewer.getId());
			GerritUser availableReviewer = new GerritUser(recommendedReviewer.getId(), recommendedReviewer.getName());
			availableReviewer.setAVLogScore(availableLikelihood);
			availableReviewer.setLCPScore(availableLikelihood * recommendedReviewer.getLCPScore());
			availableReviewer.setLCSuffScore(availableLikelihood * recommendedReviewer.getLCSuffScore());
			availableReviewer.setLCSubstrScore(availableLikelihood * recommendedReviewer.getLCSubstrScore());
			availableReviewer.setLCSubseqScore(availableLikelihood * recommendedReviewer.getLCSubseqScore());
			if(removeUnderThreshold ) {
				if (availableLikelihood > threshold) {
					availableReviewers.add(availableReviewer);				
				}
			}
			else {
				lCPScores.put(availableReviewer, availableReviewer.getLCPScore());
				lCSuffScores.put(availableReviewer, availableReviewer.getLCSuffScore());
				lCSubstrScores.put(availableReviewer, availableReviewer.getLCSubstrScore());
				lCSubseqScores.put(availableReviewer, availableReviewer.getLCSubseqScore());
			}
		}
		if (!removeUnderThreshold) {
			allScores.add(lCPScores);
			allScores.add(lCSuffScores);
			allScores.add(lCSubstrScores);
			allScores.add(lCSubseqScores);
			availableReviewers = getRankedReviewerList(bordaBasedCombination(allScores));
		}
		 
		
		return availableReviewers;
	}

	private List<GerritUser> calcWLRecommendation(ReviewRecommendations reviewRecommendations, double threshold, boolean removeOverThreshold) {
		List<GerritUser> availableReviewers = new ArrayList<>();
		String dateString = reviewRecommendations.getReviewCreated().toString().substring(0, 10);
		Map<GerritUser,Double> lCPScores = new HashMap<>();
		Map<GerritUser,Double> lCSuffScores = new HashMap<>();
		Map<GerritUser,Double> lCSubstrScores = new HashMap<>();
		Map<GerritUser,Double> lCSubseqScores = new HashMap<>();
		List<Map<GerritUser,Double>> allScores = new ArrayList<>();
		
		for(GerritUser recommendedReviewer: reviewRecommendations.getRecommendedReviewers())
		{
			double workload = WlChecker.getReviewerWorkloadByDay(dateString, recommendedReviewer.getId());
			GerritUser availableReviewer = new GerritUser(recommendedReviewer.getId(), recommendedReviewer.getName());
			availableReviewer.setWLScore(workload);
			if (workload != 0.0) {
				availableReviewer.setLCPScore(recommendedReviewer.getLCPScore()/workload);
				availableReviewer.setLCSuffScore(recommendedReviewer.getLCSuffScore()/workload);
				availableReviewer.setLCSubstrScore(recommendedReviewer.getLCSubstrScore()/workload);
				availableReviewer.setLCSubseqScore(recommendedReviewer.getLCSubseqScore()/workload);
			}
			if(removeOverThreshold) {
				if(workload <= threshold) {	
					availableReviewers.add(availableReviewer);				
				}
			}	else {
				lCPScores.put(availableReviewer, availableReviewer.getLCPScore());
				lCSuffScores.put(availableReviewer, availableReviewer.getLCSuffScore());
				lCSubstrScores.put(availableReviewer, availableReviewer.getLCSubstrScore());
				lCSubseqScores.put(availableReviewer, availableReviewer.getLCSubseqScore());
			}
		}
		if (!removeOverThreshold) {
			allScores.add(lCPScores);
			allScores.add(lCSuffScores);
			allScores.add(lCSubstrScores);
			allScores.add(lCSubseqScores);
			availableReviewers = getRankedReviewerList(bordaBasedCombination(allScores));
		}
		
		return availableReviewers;
	}

	private Map<GerritUser, Integer> bordaBasedCombination(List<Map<GerritUser,Double>> allScores){
		Map<GerritUser, Integer> ranks = new HashMap<>();
		int M;
		int techniqueRank=0;	
		
		
		for (Map<GerritUser,Double> techniqueScores: allScores) {
			M = 0;
			
			for(Double score: techniqueScores.values()) {
				if (score != 0.0) {
					M += 1;
				}
			}
			
		
		    for (GerritUser reviewer: techniqueScores.keySet()) {
				techniqueRank = ranks.getOrDefault(reviewer, 0);
				techniqueRank += -1 * rank(reviewer,techniqueScores) + M;
				ranks.put(reviewer, techniqueRank);
			}
		}

		return ranks;
	}
	
	private int rank(GerritUser reviewer, Map<GerritUser, Double> techniqueScores) {
		double ranks = 0.0;
		int temp = 0;
		Object[] totalRanks = null;
		
		
		ranks = techniqueScores.get(reviewer);
		totalRanks = techniqueScores.values().toArray();
		Arrays.sort(totalRanks);
		for (int j=0; j<totalRanks.length;j++) {
			if(totalRanks[j].equals(ranks)) {
				temp = totalRanks.length - j;
			}
		}	
		return temp;
	}
	
	private List<GerritUser> getRankedReviewerList(Map<GerritUser, Integer> combinedReviewersRecommendationRank) {
		List<GerritUser> rankedReviewersByScore = combinedReviewersRecommendationRank.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		Collections.reverse(rankedReviewersByScore); // ensure high score is ranked first
		return rankedReviewersByScore;
	}
	
	public void generateRecommendations(String appendix, double threshold, boolean removeUnderThreshold){
		System.out.println("[" + project.name + "] Generating: " + appendix);
		HashMap<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		File outputFile = getResourceFile(String.format(getImprovedRecommendationsFilename(appendix), project.name));

		if (outputFile.exists()) {
			System.out.println("--> " + appendix + " already generated");
			return;
		}
		try {
			outputFile.createNewFile();
			JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(new FileWriter(outputFile));
			generator.writeStartArray();

			for (ReviewRecommendations reviewRecommendations : revFinderEvaluation.getRecommendations(false)) {
				generator.writeStartObject();
				generator.write("review_id", reviewRecommendations.getReviewID());
				generator.write("created", reviewRecommendations.getReviewCreated().toString());
				generator.writeStartArray("recommended_reviewers");

				if(appendix.equals("AV_binary"))
				{
					calcBinaryAVRecommendation(reviewRecommendations)
							.stream()
							.map(reviewer -> reviewer.asJsonObject())
							.forEach(generator::write);
				}
				else if(appendix.contains("AV_log"))
				{
					calcLogAVRecommendation(reviewRecommendations, threshold, removeUnderThreshold)
							.stream()
							.map(reviewer -> reviewer.asJsonObject())
							.forEach(generator::write);
				}
				else if(appendix.contains("WL_"))
				{
					calcWLRecommendation(reviewRecommendations, threshold, removeUnderThreshold)
							.stream()
							.map(reviewer -> reviewer.asJsonObject())
							.forEach(generator::write);
				}

				generator.writeEnd();
				generator.writeEnd();
			}
			generator.writeEnd();
			generator.flush();
			generator.close();
			System.out.println(String.format("Generated improved reviewer recommendations for %s", project.name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getResourceFile(String filename) {
		return new File("src/main/data/", filename);
	}

	private String getImprovedRecommendationsFilename(String appendix) {
		return "resultfinder/%s_recommendations_" + appendix + ".json";
	}
}
