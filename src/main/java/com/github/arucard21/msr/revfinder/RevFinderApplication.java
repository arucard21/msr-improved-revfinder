package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
		RevFinder revFinder = new RevFinder();
		for (Project project : Project.values()) {
			revFinder.generateReviewerRecommendations(project);
		}
		
	}
}
