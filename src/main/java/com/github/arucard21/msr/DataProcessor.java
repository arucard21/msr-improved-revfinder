package com.github.arucard21.msr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

public class DataProcessor {
    public int countChangesForProject(Project project) {
        int totalCount = 0;
		for(File resource : getResourceFiles(project)) {
			totalCount += countChanges(resource);
		}
        return totalCount;
    }

	private int countChanges(File resource) {
		int resourceCount = 0;
		try {
		    JsonParser parser = Json.createParser(new FileReader(resource));
		    try {
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    			resourceCount+=parser.getArrayStream().count();
		    		}
		    	}
		    }                
		    catch(JsonParsingException e) {
		    	System.err.println("JSON Parsing error occurred with file: "+resource.getName());
		    	return -1;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		return resourceCount;
	}
    
    public int countFilteredChangesForProject(Project project) {
	    int totalCount = 0;
	    File resource = new File(String.format("src/main/data/filtered/%s_changes.json", project.name));
        totalCount = countChanges(resource);
	    return totalCount;
	}
    
    public int countMoreFilteredChangesForProject(Project project) {
	    int totalCount = 0;
	    File resource = new File(String.format("src/main/data/filtered/%s_changes_within_period.json", project.name));
        totalCount = countChanges(resource);
	    return totalCount;
	}

	public void filter() throws IOException {
    	for(Project project: Project.values()) {
    		filter(project);
    	}
    }
	
	public void filterMore() throws IOException {
    	for(Project project: Project.values()) {
    		filterMore(project);
    	}
    }
    
    public void filter(Project project) throws IOException {
    	HashMap<String, Object> config = new HashMap<>();
    	config.put(JsonGenerator.PRETTY_PRINTING, true);
    	File outputFile = new File(String.format("src/main/data/filtered/%s_changes.json", project.name));
    	if (outputFile.exists()) {
    		return;
    	}
    	outputFile.createNewFile();
    	JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(new FileWriter(outputFile));
    	generator.writeStartArray();
    	
    	for(File resource : getResourceFiles(project)) {
    		filterProjectChanges(project, resource, generator);
    	}
        generator.writeEnd();
        generator.flush();
        generator.close();
        System.out.println(String.format("Wrote new filtered data for %s", project.name));
    }
    
    public void filterMore(Project project) throws IOException {
    	HashMap<String, Object> config = new HashMap<>();
    	config.put(JsonGenerator.PRETTY_PRINTING, true);
    	File outputFile = new File(String.format("src/main/data/filtered/%s_changes_within_period.json", project.name));
    	if (outputFile.exists()) {
    		return;
    	}
    	outputFile.createNewFile();
    	JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(new FileWriter(outputFile));
    	generator.writeStartArray();
    	
    	File originalFilteredFile = new File(String.format("src/main/data/filtered/%s_changes.json", project.name));
    	
    	filterProjectChangesMore(project, originalFilteredFile, generator);
    	
        generator.writeEnd();
        generator.flush();
        generator.close();
        System.out.println(String.format("Wrote new, even more filtered data for %s", project.name));
    }

	private void filterProjectChangesMore(Project project, File originalFilteredFile, JsonGenerator generator) {
		try {
		    JsonParser parser = Json.createParser(new FileReader(originalFilteredFile));
		    if(parser.hasNext()) {
		    	try {
			    	if (parser.next() == Event.START_ARRAY) {
			    			parser.getArrayStream()
			    					.filter(new LastUpdatedFilter(project)) //We can add multiple pre-processing filters here
	               					.forEach(generator::write);
			    	}
		    	}
		    	catch(JsonParsingException e) {
		    		System.err.println("JSON Parsing error occurred with file: "+originalFilteredFile.getName());
		    		return;
		    	}
		    }                
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}

	private void filterProjectChanges(Project project, File resource, JsonGenerator generator) {
		try {
		    JsonParser parser = Json.createParser(new FileReader(resource));
		    if(parser.hasNext()) {
		    	try {
			    	if (parser.next() == Event.START_ARRAY) {
			    			parser.getArrayStream()
			    					.filter(new CreatedFilter(project)) //We can add multiple pre-processing filters here
	               					.map(new ChangePreprocessor())
	               					.filter(change -> change.getReviews().stream().filter(review -> review.getReviewScore() == 2).count() != 0)
	               					.map((change) -> change.asJsonObject())
			    					.forEach(generator::write);
			    	}
		    	}
		    	catch(JsonParsingException e) {
		    		System.err.println("JSON Parsing error occurred with file: "+resource.getName());
		    		return;
		    	}
		    }                
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private List<File> getResourceFiles(Project project){
		Path resourcesPath = Paths.get("src/main/data/raw/");
		String fileName = String.format("%s_changes_.*.json", project.name);
		try {
			return Files.list(resourcesPath)
					.filter(path -> path.toFile().getName().matches(fileName))
					.map(Path::toFile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	public double averageNumberOfReviewers(Project project) {
	    File resource = new File(String.format("src/main/data/filtered/%s_changes.json", project.name));
		try {
		    JsonParser parser = Json.createParser(new FileReader(resource));
		    try {
			    if(parser.hasNext()) {
			    	if (parser.next() == Event.START_ARRAY) {
			    			OptionalDouble average = parser.getArrayStream()
			    					.map(changeJSON -> new ReviewableChange(changeJSON.asJsonObject(), true).getReviews())
			    					.map(reviewsForChange -> reviewsForChange.stream().filter(review -> review.getReviewScore() == 2).count())
			    					.mapToDouble(amountOfReviews -> amountOfReviews.doubleValue())
			    					.average();
			    			return average.isPresent() ? average.getAsDouble() : 0.0;
			    		}
			    	}
			    }                
		    catch(JsonParsingException e) {
		    	System.err.println("JSON Parsing error occurred with file: "+resource.getName());
		    	return -1;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		return 0.0;
	}

	public double averageNumberOfReviewersForMoreFilteredData(Project project) {
	    File resource = new File(String.format("src/main/data/filtered/%s_changes_within_period.json", project.name));
		try {
		    JsonParser parser = Json.createParser(new FileReader(resource));
		    try {
			    if(parser.hasNext()) {
			    	if (parser.next() == Event.START_ARRAY) {
			    			OptionalDouble average = parser.getArrayStream()
			    					.map(changeJSON -> new ReviewableChange(changeJSON.asJsonObject(), true).getReviews())
			    					.map(reviewsForChange -> reviewsForChange.stream().filter(review -> review.getReviewScore() == 2).count())
			    					.mapToDouble(amountOfReviews -> amountOfReviews.doubleValue())
			    					.average();
			    			return average.isPresent() ? average.getAsDouble() : 0.0;
			    		}
			    	}
			    }                
		    catch(JsonParsingException e) {
		    	System.err.println("JSON Parsing error occurred with file: "+resource.getName());
		    	return -1;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		return 0.0;
	}
}
