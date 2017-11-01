package com.github.arucard21.msr.revfinder;

import java.util.Arrays;
import java.util.List;

import com.github.arucard21.msr.Project;

public class RevFinderEvaluationApplication {
	public static void main(String[] args) {
		List<Project> projects = Arrays.asList(Project.ECLIPSE, Project.MEDIAWIKI, Project.OPENSTACK);
		for (Project project : projects) {
//		Project project = Project.MEDIAWIKI;
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
		}

		/*

[eclipse within-period] top-k accuracies for each k = 
{1=15.960912052117264, 3=24.9728555917481, 5=47.50271444082519, 10=54.28881650380022}
[eclipse within-period] MRR = 0.177906
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.581374524289233, 3=33.82583389299306, 5=50.682784866800986, 10=67.38303111708082}
[eclipse based-on-created] MRR = 0.324338

[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki within-period] top-k accuracies for each k = 
{1=37.33700642791552, 3=65.58310376492194, 5=78.49403122130394, 10=91.643709825528}
[mediawiki based-on-created] MRR = 0.299320
[mediawiki within-period] MRR = 0.298570

[openstack based-on-created] top-k accuracies for each k = 
{1=35.9984399375975, 3=65.07410296411857, 5=76.77457098283931, 10=87.69500780031201}
[openstack within-period] top-k accuracies for each k = 
{1=35.88612670408982, 3=64.77546110665597, 5=76.6439454691259, 10=87.65036086607859}
[openstack based-on-created] MRR = 0.299112
[openstack within-period] MRR = 0.298865
		
		 */
	}
}
