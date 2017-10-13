package com.github.arucard21.msr.revfinder;

import java.util.List;

import com.github.arucard21.msr.Project;

public class RevFinderApplication {
	public static void main(String[] args) {
		RevFinder revFinder = new RevFinder();
		List<String> recommendations = revFinder.generateReviewerRecommendations(Project.ANDROID, ""); // not sure what this reviewn variable is for
		for (String recommendedReviewer : recommendations) {
			System.out.println(recommendedReviewer);
		}
		
		//System.out.println(LCP("src/com/android/settings/LocationSettings.java","src/com/android/settings/Utils.java"));
		//System.out.println(LCSuff("tests/auto/undo/undo.pro","src/imports/undo/undo.pro"));
	//	System.out.println(LCSubstr("res/layout/bluetooth_pin_entry.xml","tests/res/layout/operator_main.xml"));
		//System.out.println(LCSubseq("apps/CtsVerifier/src/com/android/cts/verifier/sensors/MagnetometerTestActivity.java","tests/tests/hardware/src/android/hardware/cts/SensorTest.java"));
		/*int[][] p = new int[5][3];
		for(int i=0;i<5;i++) {
			for(int j=0;j<3;j++) {
				System.out.println(p[i][j]);
			}
		}*/
		
		
	}
}
