package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
//		for (Project project : Project.values()) {
		Project project = Project.OPENSTACK;
			// you can skip projects by making sure the project_recommendations.json file already exists in the revfinder folder
			RevFinder revFinder = new RevFinder(project );
			revFinder.generateReviewerRecommendations(false);
			revFinder.generateReviewerRecommendations(true);
			System.out.println("Results for data filtered only on created date:");
			System.out.println(project.name+" top-k accuracy for first = "+revFinder.calculateTopKAccuracy(10, false, false));
			System.out.println(project.name+" top-k accuracy for last = "+revFinder.calculateTopKAccuracy(10, true, false));
			System.out.println(project.name+" MRR for first = "+revFinder.calculateMRR(false, false));
			System.out.println(project.name+" MRR for last = "+revFinder.calculateMRR(true, false));
			System.out.println(project.name+" average number of files = "+revFinder.getAverageNumberFiles(false));
			System.out.println("");
			System.out.println("Results for more filtered data:");
			System.out.println(project.name+" top-k accuracy for first = "+revFinder.calculateTopKAccuracy(10, false, true));
			System.out.println(project.name+" top-k accuracy for last = "+revFinder.calculateTopKAccuracy(10, true, true));
			System.out.println(project.name+" MRR for first = "+revFinder.calculateMRR(false, true));
			System.out.println(project.name+" MRR for last = "+revFinder.calculateMRR(true, true));
			System.out.println(project.name+" average number of files = "+revFinder.getAverageNumberFiles(true));
//		}

	}
}
