package com.github.arucard21.msr.revfinder;


import com.github.arucard21.msr.Project;
import com.github.arucard21.msr.ReviewableChange;
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

	private List<GerritUser> calcBinaryAVRecommendation(ReviewableChange change) {
		List<GerritUser> candidates = revFinderEvaluation.candidates(change, false, project);
		Map<GerritUser, Double> ranking = new HashMap<>();
		String dateString = change.getCreated().toString().substring(0, 10);

		for(GerritUser candidate : candidates)
		{
			double filepathScore = candidate.getCombinedFilepathScore();
			int availability = AvChecker.checkBinaryAvailabilityByDateString(dateString, candidate.getId());

			if(availability == 0)
				continue;

			candidate.setAVBinaryScore(filepathScore * availability);
			ranking.put(candidate, candidate.getAVBinaryScore());
		}

		return getRankedReviewerListDescending(ranking);
	}

	private List<GerritUser> calcLogAVRecommendation(ReviewableChange change, double threshold, boolean removeUnderThreshold) {
		List<GerritUser> candidates = revFinderEvaluation.candidates(change, false, project);
		Map<GerritUser, Double> ranking = new HashMap<>();
		String dateString = change.getCreated().toString().substring(0, 10);

		for(GerritUser candidate : candidates)
		{
			double filepathScore = candidate.getCombinedFilepathScore();
			double availability = AvChecker.checkLogAvailabilityByDateString(dateString, candidate.getId());

			if(availability == 0)
				continue;

			if(removeUnderThreshold && threshold > availability)
				continue;

			candidate.setAVLogScore(filepathScore * availability);
			ranking.put(candidate, candidate.getAVLogScore());
		}

		return getRankedReviewerListDescending(ranking);
	}

	private List<GerritUser> calcWLRecommendation(ReviewableChange change, double maximum, boolean removeOverMaximum) {
		List<GerritUser> candidates = revFinderEvaluation.candidates(change, false, project);
		Map<GerritUser, Double> ranking = new HashMap<>();
		String dateString = change.getCreated().toString().substring(0, 10);

		for(GerritUser candidate : candidates)
		{
			double filepathScore = candidate.getCombinedFilepathScore();
			double workload = WlChecker.getReviewerWorkloadByDay(dateString, candidate.getId());

			if(workload == 0)
				continue;

			if(removeOverMaximum && workload > maximum)
				continue;

			candidate.setWLScore(filepathScore * workload);
			ranking.put(candidate, candidate.getWLScore());
		}

		// TODO sort ascending!
		return getRankedReviewerListDescending(ranking);
	}

	public static List<GerritUser> getRankedReviewerListDescending(Map<GerritUser, Double> candidates) {
		List<GerritUser> tempList = candidates.entrySet().stream()
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

	public void generateRecommendations(String appendix, double threshold, boolean removeUnderThreshold){
		System.out.println("[" + project.name + "] Generating: " + appendix);
		HashMap<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		File outputFile = getResourceFile(String.format(getRecommendationsFilename(appendix), project.name));

		if (outputFile.exists()) {
			System.out.println("--> " + appendix + " already generated");
			return;
		}
		try {
			outputFile.createNewFile();
			JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(new FileWriter(outputFile));
			generator.writeStartArray();

			for (ReviewableChange change : revFinderEvaluation.getChanges(false)) {
				generator.writeStartObject();
				generator.write("review_id", change.getId());
				generator.writeStartArray("recommended_reviewers");

				if(appendix.equals("AV_binary"))
				{
					calcBinaryAVRecommendation(change)
							.stream()
							.map(reviewer -> reviewer.asJsonObject())
							.forEach(generator::write);
				}
				else if(appendix.contains("AV_log"))
				{
					calcLogAVRecommendation(change, threshold, removeUnderThreshold)
							.stream()
							.map(reviewer -> reviewer.asJsonObject())
							.forEach(generator::write);
				}
				else if(appendix.contains("WL_"))
				{
					calcWLRecommendation(change, threshold, removeUnderThreshold)
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
			System.out.println(String.format("Generated recommended reviewers for %s", project.name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File getResourceFile(String filename) {
		return new File("src/main/data/", filename);
	}

	private String getRecommendationsFilename(String appendix) {
		return "resultfinder/%s_recommendations_" + appendix + ".json";
	}
}
