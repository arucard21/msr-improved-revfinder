package com.github.arucard21.msr;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.Math;


public class CodeReviewersRanking {

	private static String[] path2List(String fileString){
		return fileString.split("/"); 
	}
	
	private static int LCP(String filen,String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		int commonPath = 0;
		int minLength = Math.min(file1.length,file2.length);
		for (int i = 0; i < minLength; i++) {
			if (file1[i].equals(file2[i])) {
				commonPath += 1;
			}
			else {
				break;
			}
		}
		return commonPath;
	}
	
	private static int LCSuff(String filen, String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		int commonPath = 0;
		int r = Math.min(file1.length,file2.length);
		for (int i = r - 1 ; i >= 0; i-- ) {
			if (file1[i].equals(file2[i])) {
				commonPath += 1;
			}
			else {
				break;
			}
		}
		return commonPath;
	}
	
	private static int LCSubstr(String filen, String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		int commonPath = 0;
		Set<String> setFilen = new HashSet<String>(Arrays.asList(file1));
		Set<String> setFilep = new HashSet<String>(Arrays.asList(file2));
		int[][] mat;
	
		setFilen.retainAll(setFilep);
		if ( setFilen.size() > 0) {
			mat = new int[(file1.length+1)][(file2.length+1)];
			for (int i = 0; i <= file1.length; i++) {
				for (int j = 0; j <= file2.length; j++) {
					if ((i == 0 ) || (j == 0)) {
						mat[i][j] = 0;
					}
					else if (file1[i-1].equals(file2[j-1])) {
						mat[i][j] = mat[i-1][j-1] + 1;
						commonPath = Math.max(commonPath,mat[i][j]);
					}
					else {
						mat[i][j] = 0;
					}
				}
			}
		}
		return commonPath;
 	}
	
	private static int LCSubseq(String filen, String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		int commonPath = 0;
		Set<String> setFilen = new HashSet<String>(Arrays.asList(file1));
		Set<String> setFilep = new HashSet<String>(Arrays.asList(file2));
		int[][] L;
		
		setFilen.retainAll(setFilep);
		if (setFilen.size() > 0) {
			L = new int[file1.length+1][file2.length+1];
			for (int i = 0; i <= file1.length; i++) {
				for (int j = 0; j <= file2.length; j++) {
					if ((i == 0 ) || (j == 0)) {
						L[i][j] = 0;
					}
					else if (file1[i-1].equals(file2[j-1])) {
						L[i][j] = L[i-1][j-1] + 1;
					}
					else {
						L[i][j] = Math.max(L[i-1][j], L[i][j-1]);
					}
				}
				commonPath = L[file1.length][file2.length];
			}
		}
		else {
			commonPath = 0;
		}
		return commonPath;
	}
	
	private double filePathSimilarity(String filen, String filep,int ck) {
		return StringComparison(filen,filep,ck)/Math.max(filen.length(),filep.length());
	}
	

	private int StringComparison(String filen, String filep, int ck) {
		int[] R = {LCP(filen,filep), LCSuff(filen,filep), LCSubstr(filen,filep), LCSubseq(filen,filep)};
		return Combination(ck, R);
	}

	private int Combination(int ck, int[] R) {
		int s = 0;
		int[] M;
		for (int i = 0; i < 4;i++) {
			//s += M[i] - rank(ck,R[i]);
		}
		return s;
	}
	
	private List<String> getFiles(String reviews) {
		List<String> files = null;
		String keyName;
		int temp = 0;
		try {
			JsonParser parser = Json.createParser(new FileReader(getResourceFile("filtered/android_changes.json")));
			Event event = parser.next();
			while (parser.hasNext()) {
				event = parser.next();
				switch(event) {
					case START_ARRAY:
					case END_ARRAY:
					case START_OBJECT:
					case END_OBJECT:
					case VALUE_FALSE:
					case VALUE_NULL:
					case VALUE_TRUE:
					  	  break;
					case KEY_NAME:
					   	  if (parser.getString().equals("files")) {
					   		  parser.next();
					   		  temp +=1;
					   		  while(temp != 0) {
						   		  switch(event) {
						    		  case START_ARRAY:
								      case END_ARRAY:
								      case START_OBJECT:
								    	  temp += 1;
								      case END_OBJECT:
								    	  temp -=1;
								      case VALUE_FALSE:
								      case VALUE_NULL:
								      case VALUE_TRUE:
								    	  break;
								      case KEY_NAME:
								    	  keyName = parser.getString();
								    	  if ((keyName.equals("size")) || (keyName.equals("size_delta")) || (keyName.equals("lines_inserted")) || (keyName.equals("lines_deleted"))) {
								    		  break;
								    	  }
								    	  else {
								    		  if(files.contains(keyName)){
								    			  break;
								    		  }
								    		  else {
								    			  files.add(keyName);
								    		  }
								    	  }
								    	  break;
								     case VALUE_STRING:
								     case VALUE_NUMBER:
					                      break;   	  
						   		  
						   		  }
					   		  }
					    		
					    }
					   	break;
					 case VALUE_STRING:
					 case VALUE_NUMBER:
		                  break;   	  
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		return files;
	}
		
	

	private File getResourceFile(String filename) {
		return new File("src/main/resources/", filename);
	}

		
	
	private List<String> getCodeReviewers(String review){
		List<String> reviewers = null;
		return reviewers;
	}
	
	
	
	public void codeReviewersRanking(String reviewn){
		List<String> pastReviews = getPastReviews();
		sortbyDate(pastReviews);
		Map<String, Double> C = null;
		String reviewp;
		Iterator<String> iter = pastReviews.iterator();
		Iterator<String> filesnIter;
		Iterator<String> filespIter;
		Iterator<String> reviewersIter;
		double score,scoreRp;
		String reviewer;
		int ck = 0;
		
		while(iter.hasNext()) {
			reviewp = iter.next();
			List<String> Filesn = getFiles(reviewn);
			List<String> Filesp = getFiles(reviewp);
			
			scoreRp = 0.0;
			filesnIter = Filesn.iterator();
			filespIter = Filesp.iterator();
			while(filesnIter.hasNext()) {
				while(filespIter.hasNext()) {
					scoreRp += filePathSimilarity(filesnIter.next(),filespIter.next(),ck);
				}
			}
			scoreRp /= ((Filesn.size()) * (Filesp.size()));
			
			reviewersIter = getCodeReviewers(reviewp).iterator();
			while (reviewersIter.hasNext()) {
				reviewer = reviewersIter.next();
				score = C.get(reviewer);
				C.put(reviewer, score + scoreRp);
			}
		}

	}

	private void sortbyDate(List<String> pastReviews) {
		java.util.Collections.sort(pastReviews);
		
	}

	private List<String> getPastReviews() {
		List<String> reviews = null;
		String date = null;
		String message;
		try {
			JsonParser parser = Json.createParser(new FileReader(getResourceFile("filtered/android_changes.json")));
			Event event = parser.next();
			while (parser.hasNext()) {
				event = parser.next();
				switch(event) {
					case START_ARRAY:
					case END_ARRAY:
					case START_OBJECT:
					case END_OBJECT:
					case VALUE_FALSE:
					case VALUE_NULL:
					case VALUE_TRUE:
					  	  break;
					case KEY_NAME:
					   	  if (parser.getString().equals("date")) {
					   		  parser.next();
					   		  date = parser.getString();	
					   	  }
					   	  parser.next();
					   	  if (parser.getString().equals("message")) {
					   		  parser.next();
					   		  message = parser.getString();
					   		  if (message.contains("Code-Review")) {
					   			  reviews.add(date);
					   		  }
					   	  }
					   	break;
					 case VALUE_STRING:
					 case VALUE_NUMBER:
		                  break;   	  
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		return reviews;
	}

	public static void main(String[] args) {
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
