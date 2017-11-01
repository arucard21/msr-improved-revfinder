package com.github.arucard21.msr.revfinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.arucard21.msr.Project;

public class ResultFinderEvaluationApplication {
	public static void main(String[] args) {
		List<Project> projects = Arrays.asList(Project.ECLIPSE, Project.MEDIAWIKI, Project.OPENSTACK);
		for (Project project : projects) {
			Path resourcesPath = Paths.get("src/main/data/resultfinder/");
			String fileName = String.format("%s_recommendations_.*.json", project.name);
			List<File> files;
			try {
				files = Files.list(resourcesPath)
						.filter(path -> path.toFile().getName().matches(fileName))
						.map(Path::toFile)
						.collect(Collectors.toList());
				for (File file : files) {
					System.out.printf("Evaluation for recommendations from %s\n", file.getName());
					RevFinderEvaluation revFinderEvaluation = new RevFinderEvaluation(project, file);
					List<Integer> valuesForK = Arrays.asList(1,3,5,10);
					System.out.printf("[%s based-on-created] top-k accuracies for each k = \n%s\n", project.name, revFinderEvaluation.calculateTopKAccuracy(valuesForK, false));
					System.out.printf("[%s based-on-created] MRR = %f\n", project.name, revFinderEvaluation.calculateMRR(false));
					System.out.println("");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*
	
Evaluation for recommendations from eclipse_recommendations_AV_log_60_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=0.0, 3=0.0, 5=0.0, 10=0.0}
[eclipse based-on-created] MRR = 0.000000

Evaluation for recommendations from eclipse_recommendations_AV_log_80_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=0.0, 3=0.0, 5=0.0, 10=0.0}
[eclipse based-on-created] MRR = 0.000000

Evaluation for recommendations from eclipse_recommendations_AV_log_40_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=0.42534139243340047, 3=0.42534139243340047, 5=0.42534139243340047, 10=0.42534139243340047}
[eclipse based-on-created] MRR = 0.065256

Evaluation for recommendations from eclipse_recommendations_WL_95_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.581374524289233, 3=33.82583389299306, 5=50.682784866800986, 10=67.38303111708082}
[eclipse based-on-created] MRR = 0.324338

Evaluation for recommendations from eclipse_recommendations_WL_80_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.581374524289233, 3=33.82583389299306, 5=50.682784866800986, 10=67.38303111708082}
[eclipse based-on-created] MRR = 0.324338

Evaluation for recommendations from eclipse_recommendations_AV_log_10_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.491828967987464, 3=48.01880456682337, 5=49.09335124244459, 10=51.46630848444146}
[eclipse based-on-created] MRR = 0.398532

Evaluation for recommendations from eclipse_recommendations_WL_90_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.581374524289233, 3=33.82583389299306, 5=50.682784866800986, 10=67.38303111708082}
[eclipse based-on-created] MRR = 0.324338

Evaluation for recommendations from eclipse_recommendations_AV_log_20_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=24.065368256100292, 3=24.69218715021267, 5=24.736959928363554, 10=24.759346317438997}
[eclipse based-on-created] MRR = 0.609219

Evaluation for recommendations from eclipse_recommendations_AV_binary.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.693306469666442, 3=48.354600402955, 5=49.67539735840609, 10=52.45130960376091}
[eclipse based-on-created] MRR = 0.389208



Evaluation for recommendations from mediawiki_recommendations_AV_log_80_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=0.0, 3=0.0, 5=0.0, 10=0.0}
[mediawiki based-on-created] MRR = 0.000000

Evaluation for recommendations from mediawiki_recommendations_WL_80_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki based-on-created] MRR = 0.299320

Evaluation for recommendations from mediawiki_recommendations_AV_log_20_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=24.525939177101968, 3=35.42039355992844, 5=38.17531305903399, 10=39.856887298747765}
[mediawiki based-on-created] MRR = 0.325634

Evaluation for recommendations from mediawiki_recommendations_AV_log_60_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=0.0, 3=0.0, 5=0.0, 10=0.0}
[mediawiki based-on-created] MRR = 0.000000

Evaluation for recommendations from mediawiki_recommendations_AV_binary.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.028622540250446, 3=61.14490161001789, 5=69.2128801431127, 10=73.7567084078712}
[mediawiki based-on-created] MRR = 0.272421

Evaluation for recommendations from mediawiki_recommendations_AV_log_10_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=32.84436493738819, 3=53.917710196779964, 5=59.159212880143116, 10=62.1824686940966}
[mediawiki based-on-created] MRR = 0.286995

Evaluation for recommendations from mediawiki_recommendations_WL_90_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki based-on-created] MRR = 0.299320

Evaluation for recommendations from mediawiki_recommendations_WL_95_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki based-on-created] MRR = 0.299320

Evaluation for recommendations from mediawiki_recommendations_AV_log_40_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=7.388193202146691, 3=9.105545617173524, 5=9.66010733452594, 10=9.767441860465116}
[mediawiki based-on-created] MRR = 0.194857



Evaluation for recommendations from openstack_recommendations_WL_80_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.9984399375975, 3=65.07410296411857, 5=76.77457098283931, 10=87.69500780031201}
[openstack based-on-created] MRR = 0.299112

Evaluation for recommendations from openstack_recommendations_AV_log_80_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=0.0, 3=0.0, 5=0.0, 10=0.0}
[openstack based-on-created] MRR = 0.000000

Evaluation for recommendations from openstack_recommendations_AV_log_10_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=36.037441497659906, 3=64.0210608424337, 5=74.16146645865835, 10=83.65834633385336}
[openstack based-on-created] MRR = 0.283692

Evaluation for recommendations from openstack_recommendations_AV_log_60_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=0.0, 3=0.0, 5=0.0, 10=0.0}
[openstack based-on-created] MRR = 0.000000

Evaluation for recommendations from openstack_recommendations_AV_log_40_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=18.91575663026521, 3=26.618564742589705, 5=27.827613104524183, 10=28.237129485179405}
[openstack based-on-created] MRR = 0.315243

Evaluation for recommendations from openstack_recommendations_WL_90_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.9984399375975, 3=65.07410296411857, 5=76.77457098283931, 10=87.69500780031201}
[openstack based-on-created] MRR = 0.299112

Evaluation for recommendations from openstack_recommendations_AV_log_20_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=34.08736349453978, 3=58.95085803432137, 5=67.37519500780031, 10=73.5179407176287}
[openstack based-on-created] MRR = 0.262093

Evaluation for recommendations from openstack_recommendations_AV_binary.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.881435257410295, 3=64.48907956318253, 5=75.25351014040561, 10=85.64742589703589}
[openstack based-on-created] MRR = 0.290646

Evaluation for recommendations from openstack_recommendations_WL_95_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.9984399375975, 3=65.07410296411857, 5=76.77457098283931, 10=87.69500780031201}
[openstack based-on-created] MRR = 0.299112
		
		 */
	}
}
