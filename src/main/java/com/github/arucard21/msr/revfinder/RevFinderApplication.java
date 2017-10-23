package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
		for (Project project : Project.values()) {
			// you can skip projects by making sure the project_recommendations.json file already exists in the revfinder folder
			RevFinder revFinder = new RevFinder(project );
			revFinder.generateReviewerRecommendations();
			System.out.println(revFinder.calculateTopKAccuracy(10));
			System.out.println(revFinder.calculateMRR());
			System.out.println(revFinder.getAverageNumberFiles());
			//revFinder.printWorkload();	
		}

	}
}
