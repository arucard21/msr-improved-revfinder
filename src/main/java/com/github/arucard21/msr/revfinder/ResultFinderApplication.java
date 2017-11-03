package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResultFinderApplication {

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	public static void main(String[] args) throws Exception {
		for (Project project : Project.values()) {
			ResultFinder resultFinder = new ResultFinder(project);

			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_binary", 0, false);
	
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_log_false", 0.0, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_log_10_true", 0.1, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_log_20_true", 0.2, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_log_40_true", 0.4, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_log_60_true", 0.6, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_log_80_true", 0.8, true);

			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("WL_80_true", 0.8, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("WL_90_true", 0.9, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("WL_95_true", 0.95, true);
		
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("AV_log_80_false", 0.8, false);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendations("WL_95_false", 0.95, false);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendationsCombined("AVWL_binary_avRemoval_80_wlRemoval", true, 0.0, true, 0.8, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendationsCombined("AVWL_log20_avRemoval_80_wlRemoval", false, 0.2, true, 0.8, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendationsCombined("AVWL_avReranking_80_wlRemoval", false, 0.0, false, 0.8, true);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendationsCombined("AVWL_binary_avRemoval_wlReranking", true, 0.0, true, 0.0, false);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendationsCombined("AVWL_log20_avRemoval_wlReranking", false, 0.2, true, 0.0, false);
			
			print(dtf.format(LocalDateTime.now()));
			resultFinder.generateRecommendationsCombined("AVWL_avReranking_wlReranking", false, 0.0, false, 0.0, false);
		}
	}

	static void print(String str) {
		System.out.println(str);
	}
}
