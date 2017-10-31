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

import static com.github.arucard21.msr.revfinder.RevFinder.getRankedReviewerList;


public class ResultFinder {
	private final Project project;
	private List<ReviewableChange> changes;
	//private List<ReviewableChange> moreFilteredChanges;
	private AvailabilityChecker AvChecker;

	public ResultFinder(Project project) throws IOException, ParseException {
		this.project = project;

		AvChecker = new AvailabilityChecker();
		AvChecker.check(project);

		changes = RevFinder.loadChanges("filtered/%s_changes.json", project);
	}



	private List<GerritUser> calcBinaryAVRecommendation(ReviewableChange change) {
		List<GerritUser> candidates = RevFinder.candidates(change, false, project);
		Map<GerritUser, Double> ranking = new HashMap<>();
		String dateString = change.getCreated().toString().substring(0, 10);

		for(GerritUser candidate : candidates)
		{
			double filepathScore = candidate.getCombinedFilepathScore();
			int availability = AvChecker.checkBinaryAvailabilityByDateString(dateString, candidate.getId());
			candidate.setAVBinaryScore(filepathScore * availability);

			ranking.put(candidate, candidate.getAVBinaryScore());
		}

		return getRankedReviewerList(ranking);
	}

	public static List<GerritUser> getRankedReviewerList(Map<GerritUser, Double> candidates) {
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

	public void writeRecommendations(String appendix){
		HashMap<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		File outputFile = getResourceFile(String.format(getRecommendationsFilename(appendix), project.name));
		if (outputFile.exists()) {
			System.out.println("OUTPUT FILE EXISTS");
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

				if(appendix.equals("AV_binary"))
				{
					calcBinaryAVRecommendation(change)
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
