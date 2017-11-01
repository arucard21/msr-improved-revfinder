package com.github.arucard21.msr;

import java.io.IOException;

public class DataProcessorApplication {

	public static void main(String[] args) throws IOException {
		DataProcessor processor = new DataProcessor();
		
		for (Project project : Project.values()) {
			new Thread(() -> {
			System.out.printf("Collected %d changes from Gerrit for %s\n", processor.countChangesForProject(project), project.name);
			try {
				processor.filter(project);
				processor.filterMore(project);
			} catch (IOException e) {
				System.err.printf("IOException with project %s", project.name);
				e.printStackTrace();
			}
			System.out.printf("Filtered %d changes for %s\n", processor.countFilteredChangesForProject(project), project.name);
			System.out.printf("Filtered %d changes even more for %s\n", processor.countMoreFilteredChangesForProject(project), project.name);
			System.out.printf("Average number of reviewers per change for %s: %f\n", project.name, processor.averageNumberOfReviewers(project));
			System.out.printf("Average number of reviewers per change (more filtered) for %s: %f\n", project.name, processor.averageNumberOfReviewersForMoreFilteredData(project));
			}).start();
			
		}
	}
	/*
Collected 99611 changes from Gerrit for eclipse
Wrote new filtered data for eclipse
Wrote new, even more filtered data for eclipse
Filtered 4467 changes for eclipse
Filtered 1842 changes even more for eclipse
Average number of reviewers per change for eclipse: 1.009178
Average number of reviewers per change (more filtered) for eclipse: 1.004343

Collected 374874 changes from Gerrit for mediawiki
Wrote new filtered data for mediawiki
Wrote new, even more filtered data for mediawiki
Filtered 5590 changes for mediawiki
Filtered 5445 changes even more for mediawiki
Average number of reviewers per change for mediawiki: 1.007692
Average number of reviewers per change (more filtered) for mediawiki: 1.007530

Collected 496568 changes from Gerrit for openstack
Wrote new filtered data for openstack
Wrote new, even more filtered data for openstack
Filtered 5128 changes for openstack
Filtered 4988 changes even more for openstack
Average number of reviewers per change for openstack: 1.453783
Average number of reviewers per change (more filtered) for openstack: 1.450682

Collected 181337 changes from Gerrit for qt
Wrote new filtered data for qt
Wrote new, even more filtered data for qt
Filtered 22464 changes for qt
Filtered 21851 changes even more for qt
Average number of reviewers per change for qt: 1.066907
Average number of reviewers per change (more filtered) for qt: 1.066084
	 */
}
