package com.github.arucard21.msr.revfinder;

import java.util.Arrays;
import java.util.List;

import com.github.arucard21.msr.Project;

public class RevFinderEvaluationApplication {
	public static void main(String[] args) {
//		for (Project project : Project.values()) {
		Project project = Project.MEDIAWIKI;
			// you can skip projects by making sure the project_recommendations.json file already exists in the revfinder folder
			RevFinderEvaluation revFinderEvaluation = new RevFinderEvaluation(project);
			List<Integer> valuesForK = Arrays.asList(1,3,5,10);
			new Thread(() -> {
				System.out.printf("[%s based-on-created] top-k accuracies for each k = \n%s\n", project.name, revFinderEvaluation.calculateTopKAccuracy(valuesForK, false));
				System.out.printf("[%s based-on-created] MRR = %f\n", project.name, revFinderEvaluation.calculateMRR(false));
			}).start();
			
			new Thread(() -> {
				System.out.printf("[%s within-period] top-k accuracies for each k = \n%s\n", project.name, revFinderEvaluation.calculateTopKAccuracy(valuesForK, true));
				System.out.printf("[%s within-period] MRR = %f\n", project.name, revFinderEvaluation.calculateMRR(true));
			}).start();
//		}

		/*

[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki within-period] top-k accuracies for each k = 
{1=37.33700642791552, 3=65.58310376492194, 5=78.49403122130394, 10=91.643709825528}
[mediawiki based-on-created] MRR = 0.299320
[mediawiki within-period] MRR = 0.298570
		
		 */
	}
}
