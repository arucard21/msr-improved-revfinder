package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
//		for (Project project : Project.values()) {
		Project project = Project.OPENSTACK;
			// you can skip projects by making sure the project_recommendations.json file already exists in the revfinder folder
			RevFinder revFinder = new RevFinder(project );
			revFinder.generateReviewerRecommendations();
			System.out.println(project.name+" top-k accuracy for first = "+revFinder.calculateTopKAccuracy(10, false));
			System.out.println(project.name+" top-k accuracy for last = "+revFinder.calculateTopKAccuracy(10, true));
			System.out.println(project.name+" MRR for first = "+revFinder.calculateMRR(false));
			System.out.println(project.name+" MRR for last = "+revFinder.calculateMRR(true));
			System.out.println(project.name+" average number of files = "+revFinder.getAverageNumberFiles());
			//revFinder.printWorkload();	
//		}

	}
}
