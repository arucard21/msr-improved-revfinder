package com.github.arucard21.msr.revfinder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FilePathSimilarityComparator {
	
	private String[] path2List(String fileString){
		return fileString.split("/"); 
	}
	
	private int LCP(String filen,String filep) {
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

	private int LCSuff(String filen, String filep) {
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

	private int LCSubstr(String filen, String filep) {
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

	private int LCSubseq(String filen, String filep) {
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

	public double compare(String filen, String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		double StringComparison = LCP(filen,filep)/Math.max(file1.length,file2.length);
		return StringComparison;
	}
	
	public double compare1(String filen, String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		double StringComparison = LCSuff(filen,filep)/Math.max(file1.length,file2.length);
		return StringComparison;
	}
	
	public double compare2(String filen, String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		double StringComparison = LCSubstr(filen,filep)/Math.max(file1.length,file2.length);
		return StringComparison;
	}
	
	public double compare3(String filen, String filep) {
		String[] file1 = path2List(filen);
		String[] file2 = path2List(filep);
		double StringComparison = LCSubseq(filen,filep)/Math.max(file1.length,file2.length);
		return StringComparison;
	}

	public Map<GerritUser,Integer> combination(Map<GerritUser, Double> C) {
		Map<GerritUser,Integer> s = new HashMap<>();
		int M = 0;
		int k = 0;
		
		for(Double rank:C.values()) {
			if (rank != 0.0) {
				M += 1;
			}
		}
		
		
		for (GerritUser ck:C.keySet()) {
			k = -1 * rank(ck,C) + M;
			s.put(ck, k);
		}
		return s;
	}
	
	private int rank(GerritUser ck, Map<GerritUser, Double> C) {
		double ranks = 0.0;
		int temp = 0;
		Object[] totalRanks = null;
		int count = 0;
		
		
		ranks = C.get(ck);
		totalRanks = C.values().toArray();
		Arrays.sort(totalRanks);
		for (int j=0; j<totalRanks.length;j++) {
			if (totalRanks[j].equals(0.0)){
				count = j+1;
			}
			if(totalRanks[j].equals(ranks)) {
				temp = totalRanks.length - j;
			}
		}	
		return temp;
	}


}
