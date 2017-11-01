package com.github.arucard21.msr.revfinder;

import java.util.Arrays;
import java.util.List;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
		for (Project project : Project.values()) {
			// you can skip projects by making sure the project_recommendations.json file already exists in the revfinder folder
			RevFinder revFinder = new RevFinder(project );
			List<Integer> valuesForK = Arrays.asList(1,3,5,10);
			new Thread(() -> {
				System.out.printf("[%s based-on-created] Generating recommendations\n", project.name);
				revFinder.generateReviewerRecommendations(false);
				System.out.printf("[%s based-on-created] Finished generating recommendations\n", project.name);
				System.out.printf("[%s based-on-created] top-k accuracies for each k = \n%s\n", project.name, revFinder.calculateTopKAccuracy(valuesForK, false));
				System.out.printf("[%s based-on-created] MRR = %f\n", project.name, revFinder.calculateMRR(false));
				System.out.printf("[%s based-on-created] average number of files = %f\n", project.name, revFinder.getAverageNumberFiles(false));
			}).start();
			
			new Thread(() -> {
				System.out.printf("[%s within-period] Generating recommendations\n", project.name);
				revFinder.generateReviewerRecommendations(true);
				System.out.printf("[%s within-period] Finished generating recommendations\n", project.name);
				System.out.printf("[%s within-period] top-k accuracies for each k = \n%s\n", project.name, revFinder.calculateTopKAccuracy(valuesForK, true));
				System.out.printf("[%s within-period] MRR = %f\n", project.name, revFinder.calculateMRR(true));
				System.out.printf("[%s within-period] average number of files = %f\n", project.name, revFinder.getAverageNumberFiles(true));
			}).start();
		}

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
