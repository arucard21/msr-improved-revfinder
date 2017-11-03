package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
		for (Project project : Project.values()) {
			// you can skip projects by making sure the project_recommendations.json file already exists in the revfinder folder
			RevFinder revFinder = new RevFinder(project );
			new Thread(() -> {
				System.out.printf("[%s based-on-created] Generating recommendations\n", project.name);
				revFinder.generateReviewerRecommendations(false);
				System.out.printf("[%s based-on-created] Finished generating recommendations\n", project.name);
				System.out.printf("[%s based-on-created] average number of files = %f\n", project.name, revFinder.getAverageNumberFiles(false));
			}).start();
			
			new Thread(() -> {
				System.out.printf("[%s within-period] Generating recommendations\n", project.name);
				revFinder.generateReviewerRecommendations(true);
				System.out.printf("[%s within-period] Finished generating recommendations\n", project.name);
				System.out.printf("[%s within-period] average number of files = %f\n", project.name, revFinder.getAverageNumberFiles(true));
			}).start();
		}

		/*

[eclipse based-on-created] Generating recommendations
[eclipse within-period] Generating recommendations
Generated recommended reviewers for eclipse
[eclipse within-period] Finished generating recommendations
[eclipse within-period] average number of files = 14.496743
Generated recommended reviewers for eclipse
[eclipse based-on-created] Finished generating recommendations
[eclipse based-on-created] average number of files = 14.221401
Generated recommended reviewers for openstack

[mediawiki based-on-created] Generating recommendations
[mediawiki within-period] Generating recommendations
Generated recommended reviewers for mediawiki
[mediawiki within-period] Finished generating recommendations
[mediawiki within-period] average number of files = 2.925436
Generated recommended reviewers for mediawiki
[mediawiki based-on-created] Finished generating recommendations
[mediawiki based-on-created] average number of files = 3.259750

[openstack based-on-created] Generating recommendations
[openstack within-period] Generating recommendations
[openstack within-period] Finished generating recommendations
[openstack within-period] average number of files = 15.115678
Generated recommended reviewers for openstack
[openstack based-on-created] Finished generating recommendations
[openstack based-on-created] average number of files = 15.565523

[qt based-on-created] Generating recommendations
[qt within-period] Generating recommendations
Generated recommended reviewers for qt
[qt within-period] Finished generating recommendations
[qt within-period] average number of files = 11.106494
Generated recommended reviewers for qt
[qt based-on-created] Finished generating recommendations
[qt based-on-created] average number of files = 11.203259

		 */
	}
}
