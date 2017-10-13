package com.github.arucard21.msr.revfinder;

import java.util.Arrays;
import java.util.HashSet;
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

	public int compare(String filen, String filep, int ck) {
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

}
