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

		/*
		Generated recommended reviewers for openstack
		Generated recommended reviewers for openstack
		Results for data filtered only on created date:
		openstack top-k accuracy for first = 75.65078093712455
		openstack top-k accuracy for last = 72.4669603524229
		openstack MRR for first = 0.41792055228178493
		openstack MRR for last = 0.3955286820374921
		openstack average number of files = 15.713656387665198
		
		Results for more filtered data:
		openstack top-k accuracy for first = 75.3808151502676
		openstack top-k accuracy for last = 72.16961712638945
		openstack MRR for first = 0.41646988766688103
		openstack MRR for last = 0.394677164814052
		openstack average number of files = 15.248867846850557
		 */
	}
}
