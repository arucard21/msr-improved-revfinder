package com.github.arucard21.msr.revfinder;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.github.arucard21.msr.Project;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;


public class RevFinder {
	private FilePathSimilarityComparator fpComparator = new FilePathSimilarityComparator();
	
	private double filePathSimilarity(String filen, String filep,int ck) {
		return fpComparator.compare(filen,filep,ck)/Math.max(filen.length(),filep.length());
	}
	

	private List<RevisionFile> getFiles(Project project, String reviews) {
		List<RevisionFile> files = new ArrayList<>();
		try {
		    JsonParser parser = Json.createParser(new FileReader(getResourceFile(String.format("filtered/%s_changes.json", project.name))));
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    			files = parser.getArrayStream()
               					.map(new RevisionFileExtractor())
               					.flatMap(Collection::stream)
               					.collect(Collectors.toList());
		    					
		    	}
		    }                
		} catch (IOException e) {
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
	
	
	
	/**
	 * TODO put the results in the return variable
	 * 
	 * @param project
	 * @param reviewn
	 */
	public List<String> generateReviewerRecommendations(Project project, String reviewn){
		List<String> reviewerRecommendations = new ArrayList<>();
		List<String> pastReviews = getPastReviews(project);
		Collections.sort(pastReviews);
		Map<String, Double> C = new HashMap<>();
		String reviewp;
		Iterator<String> iter = pastReviews.iterator();
		Iterator<String> reviewersIter;
		double score,scoreRp;
		String reviewer;
		int ck = 0;
		
		while(iter.hasNext()) {
			reviewp = iter.next();
			List<RevisionFile> filesN = getFiles(project, reviewn);
			List<RevisionFile> filesP = getFiles(project, reviewp);
			
			scoreRp = 0.0;
			for (RevisionFile fileN : filesN) {
				for (RevisionFile fileP : filesP) {
					scoreRp += filePathSimilarity(fileP.getFileName(),fileN.getFileName(),ck);
				}
			}
			scoreRp /= ((filesN.size()) * (filesP.size()));
			
			reviewersIter = getCodeReviewers(reviewp).iterator();
			while (reviewersIter.hasNext()) {
				reviewer = reviewersIter.next();
				score = C.get(reviewer);
				C.put(reviewer, score + scoreRp);
			}
		}
		return reviewerRecommendations;
	}

	private List<String> getPastReviews(Project project) {
		List<String> reviews = new ArrayList<>();
		String date = null;
		String message;
		try {
			JsonParser parser = Json.createParser(new FileReader(getResourceFile("filtered/"+project.name+"_changes.json")));
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
}
