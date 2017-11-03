package com.github.arucard21.msr.revfinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.arucard21.msr.Project;

public class ResultFinderEvaluationApplication {
	public static void main(String[] args) {
		for (Project project : Project.values()) {
			new Thread(() -> {
				Path resourcesPath = Paths.get("src/main/data/resultfinder/");
				String fileName = String.format("%s_recommendations_.*.json", project.name);
				List<File> files;
				try {
					files = Files.list(resourcesPath)
							.filter(path -> path.toFile().getName().matches(fileName))
							.map(Path::toFile)
							.collect(Collectors.toList());
					List<String> separatedMethodsEvaluation = new ArrayList<>();
					List<String> combinedMethodsEvaluation = new ArrayList<>();
					for (File file : files) {
						Map<String, List<String>> results = runEvaluation(project, file);
						separatedMethodsEvaluation = results.get("separated");
						combinedMethodsEvaluation = results.get("combined");
					}
					Collections.sort(separatedMethodsEvaluation);
					System.out.printf("Exportable data for separated methods of %s:\n", project.name);
					for(String eval: separatedMethodsEvaluation) {
						System.out.print(eval);
					}
					System.out.println("");
					
					Collections.sort(combinedMethodsEvaluation);
					System.out.printf("Exportable data for combined methods of %s:\n", project.name);
					for(String eval: combinedMethodsEvaluation) {
						System.out.print(eval);
					}
					System.out.println("");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	private static Map<String, List<String>> runEvaluation(Project project, File file) {
		List<String> separatedMethodsEvaluation = new ArrayList<>();
		List<String> combinedMethodsEvaluation = new ArrayList<>();
		RevFinderEvaluation revFinderEvaluation = new RevFinderEvaluation(project, file);
		List<Integer> valuesForK = Arrays.asList(1,3,5,10);
		Map<Integer, Double> topK = revFinderEvaluation.calculateTopKAccuracy(valuesForK, false);
		double mrr = revFinderEvaluation.calculateMRR(false);
		
		if(file.getName().contains("AV_binary")) {
			separatedMethodsEvaluation.add(String.format("AV_binary;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AV_log_false")) {
			separatedMethodsEvaluation.add(String.format("AV_log0;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AV_log_10_true")) {
			separatedMethodsEvaluation.add(String.format("AV_log10;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AV_log_20_true")) {
			separatedMethodsEvaluation.add(String.format("AV_log20;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AV_log_40_true")) {
			separatedMethodsEvaluation.add(String.format("AV_log40;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AV_log_60_true")) {
			separatedMethodsEvaluation.add(String.format("AV_log60;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AV_log_80_true")) {
			separatedMethodsEvaluation.add(String.format("AV_log80;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("WL_80_true")) {
			separatedMethodsEvaluation.add(String.format("WL_80;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("WL_90_true")) {
			separatedMethodsEvaluation.add(String.format("WL_90;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("WL_95_true")) {
			separatedMethodsEvaluation.add(String.format("WL_95;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AV_log_80_false")) {
			separatedMethodsEvaluation.add(String.format("AV_reranking;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("WL_95_false")) {
			separatedMethodsEvaluation.add(String.format("WL_reranking;%f;%f;%f;%f;%f\n", topK.get(1), topK.get(3), topK.get(5), topK.get(10), mrr));
		}
		else if(file.getName().contains("AVWL_binary_avRemoval_80_wlRemoval")) {
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
		Map<String, List<String>> separatedAndCombined = new HashMap<>();
		separatedAndCombined.put("separated", separatedMethodsEvaluation);
		separatedAndCombined.put("combined", combinedMethodsEvaluation);
		return separatedAndCombined;
	}
	
	/*
	
Exportable data for separated methods of eclipse:
AV_binary;9.693306;48.354600;49.675397;52.451310;0.389208
AV_log0;9.693306;48.399373;49.630625;52.182673;0.392923
AV_log10;9.491829;48.018805;49.093351;51.466308;0.398532
AV_log20;24.065368;24.692187;24.736960;24.759346;0.609219
AV_log40;0.425341;0.425341;0.425341;0.425341;0.065256
AV_log60;0.000000;0.000000;0.000000;0.000000;0.000000
AV_log80;0.000000;0.000000;0.000000;0.000000;0.000000
AV_reranking;9.961943;47.638236;49.429147;52.160287;0.343805
WL_80;9.581375;33.825834;50.682785;67.383031;0.324338
WL_90;9.581375;33.825834;50.682785;67.383031;0.324338
WL_95;9.581375;33.825834;50.682785;67.383031;0.324338
WL_reranking;13.319901;48.309828;52.764719;56.592792;0.292508

Exportable data for combined methods of eclipse:
avBinaryRemove_wl80Remove;9.581375;33.825834;50.682785;67.383031;0.324338
avBinaryRemove_wlReranking;13.476606;48.488919;53.010969;57.935975;0.295714
avLog20Remove_wl80Remove;9.581375;33.825834;50.682785;67.383031;0.324338
avLog20Remove_wlReranking;13.476606;48.488919;53.010969;57.935975;0.295714
avReranking_wl80Remove;9.961943;47.638236;49.429147;52.160287;0.343805
avReranking_wlReranking;13.610925;47.794941;51.421536;52.205059;0.283899

Exportable data for separated methods of mediawiki:
AV_binary;36.028623;61.144902;69.212880;73.756708;0.272421
AV_log0;35.617174;59.767442;67.191413;71.252236;0.270475
AV_log10;32.844365;53.917710;59.159213;62.182469;0.286995
AV_log20;24.525939;35.420394;38.175313;39.856887;0.325634
AV_log40;7.388193;9.105546;9.660107;9.767442;0.194857
AV_log60;0.000000;0.000000;0.000000;0.000000;0.000000
AV_log80;0.000000;0.000000;0.000000;0.000000;0.000000
AV_reranking;32.665474;58.443649;66.601073;74.883721;0.265070
WL_80;36.797853;65.187835;77.906977;91.323792;0.299320
WL_90;36.797853;65.187835;77.906977;91.323792;0.299320
WL_95;36.797853;65.187835;77.906977;91.323792;0.299320
WL_reranking;32.504472;57.316637;66.923077;76.475850;0.261068

Exportable data for combined methods of mediawiki:
avBinaryRemove_wl80Remove;36.797853;65.187835;77.906977;91.323792;0.299320
avBinaryRemove_wlReranking;33.828265;60.161002;72.343470;88.443649;0.283663
avLog20Remove_wl80Remove;36.797853;65.187835;77.906977;91.323792;0.299320
avLog20Remove_wlReranking;33.828265;60.161002;72.343470;88.443649;0.283663
avReranking_wl80Remove;32.665474;58.443649;66.601073;74.883721;0.265070
avReranking_wlReranking;32.558140;56.815742;64.847943;74.239714;0.251078

Exportable data for separated methods of openstack:
AV_binary;35.881435;64.489080;75.253510;85.647426;0.290646
AV_log0;36.115445;64.528081;75.312012;85.296412;0.288693
AV_log10;36.037441;64.021061;74.161466;83.658346;0.283692
AV_log20;34.087363;58.950858;67.375195;73.517941;0.262093
AV_log40;18.915757;26.618565;27.827613;28.237129;0.315243
AV_log60;0.000000;0.000000;0.000000;0.000000;0.000000
AV_log80;0.000000;0.000000;0.000000;0.000000;0.000000
AV_reranking;35.432917;63.884555;75.487520;85.237910;0.289093
WL_80;35.998440;65.074103;76.774571;87.695008;0.299112
WL_90;35.998440;65.074103;76.774571;87.695008;0.299112
WL_95;35.998440;65.074103;76.774571;87.695008;0.299112
WL_reranking;18.194228;40.854134;52.808112;73.030421;0.264690

Exportable data for combined methods of openstack:
avBinaryRemove_wl80Remove;35.998440;65.074103;76.774571;87.695008;0.299112
avBinaryRemove_wlReranking;19.091264;42.667707;55.674727;76.189548;0.276371
avLog20Remove_wl80Remove;35.998440;65.074103;76.774571;87.695008;0.299112
avLog20Remove_wlReranking;19.091264;42.667707;55.674727;76.189548;0.276371
avReranking_wl80Remove;35.432917;63.884555;75.487520;85.237910;0.289093
avReranking_wlReranking;22.406396;46.431357;59.945398;78.568643;0.276160

Exportable data for separated methods of qt:
AV_binary;5.702457;29.166667;39.102564;51.486823;0.265332
AV_log0;5.662393;28.948540;38.862179;51.161859;0.263619
AV_log10;5.595620;28.556802;38.198896;50.066774;0.259559
AV_log20;5.163818;25.881410;33.351140;42.832977;0.241503
AV_log40;2.310363;9.241453;10.714922;12.161681;0.334186
AV_log60;0.000000;0.000000;0.000000;0.000000;0.000000
AV_log80;0.000000;0.000000;0.000000;0.000000;0.000000
AV_reranking;4.353632;28.004808;37.246261;50.275997;0.264199
WL_80;5.733618;29.478276;39.383013;52.069979;0.267911
WL_90;5.733618;29.478276;39.383013;52.069979;0.267911
WL_95;5.733618;29.478276;39.383013;52.069979;0.267911
WL_reranking;11.529558;22.418091;29.077635;40.843127;0.145641

Exportable data for combined methods of qt:
avBinaryRemove_wl80Remove;5.733618;29.478276;39.383013;52.069979;0.267911
avBinaryRemove_wlReranking;12.651353;23.949430;30.755876;42.859687;0.150545
avLog20Remove_wl80Remove;5.733618;29.478276;39.383013;52.069979;0.267911
avLog20Remove_wlReranking;12.651353;23.949430;30.755876;42.859687;0.150545
avReranking_wl80Remove;4.353632;28.004808;37.246261;50.275997;0.264199
avReranking_wlReranking;12.869480;24.020655;30.769231;42.757301;0.148210
			
	 */
}
