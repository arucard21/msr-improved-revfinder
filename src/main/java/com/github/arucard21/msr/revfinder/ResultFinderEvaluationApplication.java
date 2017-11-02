package com.github.arucard21.msr.revfinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
				List<String> combinedMethodsEvaluation = new ArrayList<>();
				for (File file : files) {
					System.out.printf("Evaluation for recommendations from %s\n", file.getName());
					RevFinderEvaluation revFinderEvaluation = new RevFinderEvaluation(project, file);
					List<Integer> valuesForK = Arrays.asList(1,3,5,10);
					Map<Integer, Double> topK = revFinderEvaluation.calculateTopKAccuracy(valuesForK, false);
					double mrr = revFinderEvaluation.calculateMRR(false);
					if(file.getName().contains("AVWL_binary_avRemoval_80_wlRemoval")) {
						combinedMethodsEvaluation.add(String.format("avBinaryRemove_wl80Remove;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
					}
					else if(file.getName().contains("AVWL_log20_avRemoval_80_wlRemoval")) {
						combinedMethodsEvaluation.add(String.format("avLog20Remove_wl80Remove;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
					}
					else if(file.getName().contains("AVWL_avReranking_80_wlRemoval")) {
						combinedMethodsEvaluation.add(String.format("avReranking_wl80Remove;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
					}
					else if(file.getName().contains("AVWL_binary_avRemoval_wlReranking")) {
						combinedMethodsEvaluation.add(String.format("avBinaryRemove_wlReranking;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
					}
					else if(file.getName().contains("AVWL_log20_avRemoval_wlReranking")) {
						combinedMethodsEvaluation.add(String.format("avLog20Remove_wlReranking;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
					}
					else if(file.getName().contains("AVWL_avReranking_wlReranking")) {
						combinedMethodsEvaluation.add(String.format("avReranking_wlReranking;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
					}

					System.out.printf("[%s based-on-created] top-k = %s\n", project.name, revFinderEvaluation.calculateTopKAccuracy(valuesForK, false));
					System.out.printf("[%s based-on-created] MRR = %f\n", project.name, revFinderEvaluation.calculateMRR(false));
					System.out.println("");
				}
				Collections.sort(combinedMethodsEvaluation);
				System.out.printf("Exportable data for combined methods of %s:\n", project.name);
				for(String eval: combinedMethodsEvaluation) {
					System.out.print(eval);
				}
				System.out.println("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*
	
Evaluation for recommendations from eclipse_recommendations_AVWL_log20_avRemoval_80_wlRemoval.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.581374524289233, 3=33.82583389299306, 5=50.682784866800986, 10=67.38303111708082}
[eclipse based-on-created] MRR = 0.324338

Evaluation for recommendations from eclipse_recommendations_AVWL_avReranking_wlReranking.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=13.610924557868815, 3=47.79494067606895, 5=51.42153570629058, 10=52.20505932393105}
[eclipse based-on-created] MRR = 0.283899

Evaluation for recommendations from eclipse_recommendations_AVWL_log20_avRemoval_wlReranking.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=13.476606223416162, 3=48.48891873740766, 5=53.010969330646965, 10=57.93597492724424}
[eclipse based-on-created] MRR = 0.295714

Evaluation for recommendations from eclipse_recommendations_AVWL_binary_avRemoval_80_wlRemoval.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.581374524289233, 3=33.82583389299306, 5=50.682784866800986, 10=67.38303111708082}
[eclipse based-on-created] MRR = 0.324338

Evaluation for recommendations from eclipse_recommendations_AVWL_binary_avRemoval_wlReranking.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=13.476606223416162, 3=48.48891873740766, 5=53.010969330646965, 10=57.93597492724424}
[eclipse based-on-created] MRR = 0.295714

Evaluation for recommendations from eclipse_recommendations_AV_log_60_true.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=0.0, 3=0.0, 5=0.0, 10=0.0}
[eclipse based-on-created] MRR = 0.000000

Evaluation for recommendations from eclipse_recommendations_AVWL_avReranking_80_wlRemoval.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.961943138571748, 3=47.63823595254085, 5=49.42914707857623, 10=52.16028654578017}
[eclipse based-on-created] MRR = 0.343805

Evaluation for recommendations from eclipse_recommendations_AV_log_false.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.693306469666442, 3=48.39937318110589, 5=49.6306245802552, 10=52.18267293485561}
[eclipse based-on-created] MRR = 0.392923

Evaluation for recommendations from eclipse_recommendations_AV_log_80_false.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=9.961943138571748, 3=47.63823595254085, 5=49.42914707857623, 10=52.16028654578017}
[eclipse based-on-created] MRR = 0.343805

Evaluation for recommendations from eclipse_recommendations_WL_95_false.json
[eclipse based-on-created] top-k accuracies for each k = 
{1=13.319901499888068, 3=48.30982762480412, 5=52.76471905081711, 10=56.59279158271771}
[eclipse based-on-created] MRR = 0.292508

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

Evaluation for recommendations from mediawiki_recommendations_AVWL_binary_avRemoval_wlReranking.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=33.82826475849732, 3=60.16100178890876, 5=72.34347048300536, 10=88.44364937388193}
[mediawiki based-on-created] MRR = 0.283663

Evaluation for recommendations from mediawiki_recommendations_AV_binary.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.028622540250446, 3=61.14490161001789, 5=69.2128801431127, 10=73.7567084078712}
[mediawiki based-on-created] MRR = 0.272421

Evaluation for recommendations from mediawiki_recommendations_AVWL_log20_avRemoval_80_wlRemoval.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki based-on-created] MRR = 0.299320

Evaluation for recommendations from mediawiki_recommendations_AVWL_avReranking_wlReranking.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=32.55813953488372, 3=56.81574239713775, 5=64.8479427549195, 10=74.23971377459749}
[mediawiki based-on-created] MRR = 0.251078

Evaluation for recommendations from mediawiki_recommendations_AV_log_10_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=32.84436493738819, 3=53.917710196779964, 5=59.159212880143116, 10=62.1824686940966}
[mediawiki based-on-created] MRR = 0.286995

Evaluation for recommendations from mediawiki_recommendations_AVWL_binary_avRemoval_80_wlRemoval.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki based-on-created] MRR = 0.299320

Evaluation for recommendations from mediawiki_recommendations_AV_log_80_false.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=32.665474060822895, 3=58.44364937388193, 5=66.6010733452594, 10=74.88372093023256}
[mediawiki based-on-created] MRR = 0.265070

Evaluation for recommendations from mediawiki_recommendations_WL_95_false.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=32.50447227191413, 3=57.31663685152057, 5=66.92307692307692, 10=76.47584973166369}
[mediawiki based-on-created] MRR = 0.261068

Evaluation for recommendations from mediawiki_recommendations_WL_90_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki based-on-created] MRR = 0.299320

Evaluation for recommendations from mediawiki_recommendations_AV_log_false.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=35.61717352415027, 3=59.76744186046512, 5=67.19141323792486, 10=71.25223613595706}
[mediawiki based-on-created] MRR = 0.270475

Evaluation for recommendations from mediawiki_recommendations_WL_95_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=36.79785330948122, 3=65.18783542039355, 5=77.90697674418605, 10=91.32379248658319}
[mediawiki based-on-created] MRR = 0.299320

Evaluation for recommendations from mediawiki_recommendations_AVWL_log20_avRemoval_wlReranking.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=33.82826475849732, 3=60.16100178890876, 5=72.34347048300536, 10=88.44364937388193}
[mediawiki based-on-created] MRR = 0.283663

Evaluation for recommendations from mediawiki_recommendations_AVWL_avReranking_80_wlRemoval.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=32.665474060822895, 3=58.44364937388193, 5=66.6010733452594, 10=74.88372093023256}
[mediawiki based-on-created] MRR = 0.265070

Evaluation for recommendations from mediawiki_recommendations_AV_log_40_true.json
[mediawiki based-on-created] top-k accuracies for each k = 
{1=7.388193202146691, 3=9.105545617173524, 5=9.66010733452594, 10=9.767441860465116}
[mediawiki based-on-created] MRR = 0.194857



Evaluation for recommendations from openstack_recommendations_AVWL_avReranking_80_wlRemoval.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.43291731669267, 3=63.88455538221529, 5=75.48751950078002, 10=85.23790951638065}
[openstack based-on-created] MRR = 0.289093

Evaluation for recommendations from openstack_recommendations_AVWL_avReranking_wlReranking.json
[openstack based-on-created] top-k accuracies for each k = 
{1=22.406396255850233, 3=46.43135725429017, 5=59.945397815912635, 10=78.56864274570982}
[openstack based-on-created] MRR = 0.276160

Evaluation for recommendations from openstack_recommendations_AVWL_log20_avRemoval_wlReranking.json
[openstack based-on-created] top-k accuracies for each k = 
{1=19.091263650546022, 3=42.667706708268334, 5=55.674726989079566, 10=76.18954758190327}
[openstack based-on-created] MRR = 0.276371

Evaluation for recommendations from openstack_recommendations_AVWL_binary_avRemoval_wlReranking.json
[openstack based-on-created] top-k accuracies for each k = 
{1=19.091263650546022, 3=42.667706708268334, 5=55.674726989079566, 10=76.18954758190327}
[openstack based-on-created] MRR = 0.276371

Evaluation for recommendations from openstack_recommendations_WL_95_false.json
[openstack based-on-created] top-k accuracies for each k = 
{1=18.194227769110764, 3=40.85413416536662, 5=52.80811232449298, 10=73.03042121684868}
[openstack based-on-created] MRR = 0.264690

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

Evaluation for recommendations from openstack_recommendations_AV_log_false.json
[openstack based-on-created] top-k accuracies for each k = 
{1=36.11544461778471, 3=64.52808112324493, 5=75.31201248049922, 10=85.29641185647426}
[openstack based-on-created] MRR = 0.288693

Evaluation for recommendations from openstack_recommendations_AV_log_40_true.json
[openstack based-on-created] top-k accuracies for each k = 
{1=18.91575663026521, 3=26.618564742589705, 5=27.827613104524183, 10=28.237129485179405}
[openstack based-on-created] MRR = 0.315243

Evaluation for recommendations from openstack_recommendations_AV_log_80_false.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.43291731669267, 3=63.88455538221529, 5=75.48751950078002, 10=85.23790951638065}
[openstack based-on-created] MRR = 0.289093

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

Evaluation for recommendations from openstack_recommendations_AVWL_log20_avRemoval_80_wlRemoval.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.9984399375975, 3=65.07410296411857, 5=76.77457098283931, 10=87.69500780031201}
[openstack based-on-created] MRR = 0.299112

Evaluation for recommendations from openstack_recommendations_AVWL_binary_avRemoval_80_wlRemoval.json
[openstack based-on-created] top-k accuracies for each k = 
{1=35.9984399375975, 3=65.07410296411857, 5=76.77457098283931, 10=87.69500780031201}
[openstack based-on-created] MRR = 0.299112
		
		 */
	}
}
