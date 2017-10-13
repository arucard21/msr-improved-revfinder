package com.github.arucard21.msr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class DataProcessor {
    public int countChangesForProject(Project project) {
        int totalCount = 0;
        File resource;
        if (project.equals(Project.QT)) {
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d_abandoned.json", project.name, i))).exists(); i++) {
    			totalCount += countProjectChanges(resource);
    		}
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d_deferred.json", project.name, i))).exists(); i++) {
    			totalCount += countProjectChanges(resource);
    		}
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d_merged.json", project.name, i))).exists(); i++) {
    			totalCount += countProjectChanges(resource);
    		}
    	}
    	else{
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d.json", project.name, i))).exists(); i++) {
    			totalCount += countProjectChanges(resource);
    		}
    	}
        return totalCount;
    }

	private int countProjectChanges(File resource) {
		int resourceCount = 0;
		try {
		    JsonParser parser = Json.createParser(new FileReader(resource));
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    		resourceCount+=parser.getArrayStream().count();
		    	}
		    }                
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		return resourceCount;
	}
    
    public int countFilteredChangesForProject(Project project) {
	    int totalCount = 0;
	    File resource = getResourceFile(String.format("filtered/%s_changes.json", project.name));
        totalCount = countProjectChanges(resource);
	    return totalCount;
	}

	public void filter() throws IOException {
    	for(Project project: Project.values()) {
    		filter(project);
    	}
    }
    
    public void filter(Project project) throws IOException {
    	File resource;
    	HashMap<String, Object> config = new HashMap<>();
    	config.put(JsonGenerator.PRETTY_PRINTING, true);
    	File outputFile = getResourceFile(String.format("filtered/%s_changes.json", project.name));
    	if (outputFile.exists()) {
    		return;
    	}
    	outputFile.createNewFile();
    	JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(new FileWriter(outputFile));
    	generator.writeStartArray();
    	
    	if (project.equals(Project.QT)) {
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d_abandoned.json", project.name, i))).exists(); i++) {
    			filterProjectChanges(project, resource, generator);
    		}
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d_deferred.json", project.name, i))).exists(); i++) {
    			filterProjectChanges(project, resource, generator);
    		}
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d_merged.json", project.name, i))).exists(); i++) {
    			filterProjectChanges(project, resource, generator);
    		}
    	}
    	else{
    		for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d.json", project.name, i))).exists(); i++) {
    			filterProjectChanges(project, resource, generator);
    		}
    	}
        generator.writeEnd();
        generator.flush();
        generator.close();
        System.out.println(String.format("Wrote new filtered data for %s", project.name));
    }

	private void filterProjectChanges(Project project, File resource, JsonGenerator generator) {
		try {
		    JsonParser parser = Json.createParser(new FileReader(resource));
		    if(parser.hasNext()) {
		    	if (parser.next() == Event.START_ARRAY) {
		    			parser.getArrayStream()
		    					.filter(new PeriodFilter(project)) //We can add multiple pre-processing filters here
               					.map(new ChangePreprocessor())
		    					.forEach(generator::write);
		    	}
		    }                
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private File getResourceFile(String filename) {
		return new File("src/main/resources/", filename);
	}
}
