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
				availableReviewers.add(recommendedReviewer);				
			}
		}
		return availableReviewers;
	}

	private List<GerritUser> calcLogAVRecommendation(ReviewRecommendations reviewRecommendations, double threshold, boolean removeUnderThreshold) {
		List<GerritUser> availableReviewers = new ArrayList<>();
		String dateString = reviewRecommendations.getReviewCreated().toString().substring(0, 10);
		
		for(GerritUser recommendedReviewer: reviewRecommendations.getRecommendedReviewers())
		{
			float availableLikelihood = AvChecker.checkLogAvailabilityByDateString(dateString, recommendedReviewer.getId());
			if(removeUnderThreshold && availableLikelihood > threshold) {
				availableReviewers.add(recommendedReviewer);				
			}
		}
		return availableReviewers;
	}

	private List<GerritUser> calcWLRecommendation(ReviewRecommendations reviewRecommendations, double threshold, boolean removeOverThreshold) {
		List<GerritUser> availableReviewers = new ArrayList<>();
		String dateString = reviewRecommendations.getReviewCreated().toString().substring(0, 10);

		for(GerritUser recommendedReviewer: reviewRecommendations.getRecommendedReviewers())
		{
			double workload = WlChecker.getReviewerWorkloadByDay(dateString, recommendedReviewer.getId());
			if(removeOverThreshold && workload <= threshold) {
				availableReviewers.add(recommendedReviewer);				
			}
		}
		return availableReviewers;
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
