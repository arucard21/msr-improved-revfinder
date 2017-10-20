package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
		RevFinder revFinder = new RevFinder();
		for (Project project : Project.values()) {
			if (project.equals(Project.QT)) {
			}
			else {
				revFinder.generateReviewerRecommendations(project);
			}
		}
		//int topK = 10;
		//Project project = Project.ANDROID;
		//System.out.println(revFinder.calculateTopKAccuracy(topK, project));
		//System.out.println(revFinder.calculateMRR(project));
	}
}
