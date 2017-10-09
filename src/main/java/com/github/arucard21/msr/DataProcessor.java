package com.github.arucard21.msr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class DataProcessor {

    public enum Project {ANDROID("android"), CHROMIUM("chromium"), OPENSTACK("openstack"), QT("qt");
    	private final String name;
    	Project(String name){
    		this.name = name;
    	}
    }

    public int countChangesForProject(Project project) {
        int totalCount = 0;
        File resource;
        for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d.json", project.name, i))).exists(); i++) {
            try {
                JsonParser parser = Json.createParser(new FileReader(resource));
                if(parser.hasNext()) {
                	if (parser.next() == Event.START_ARRAY) {
                		totalCount+=parser.getArrayStream().count();
                	}
                }                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return totalCount;
    }
    
    public int countFilteredChangesForProject(Project project) {
	    int totalCount = 0;
	    File resource = getResourceFile(String.format("filtered/%s_changes.json", project.name));
        try {
            JsonParser parser = Json.createParser(new FileReader(resource));
            if(parser.hasNext()) {
            	if (parser.next() == Event.START_ARRAY) {
            		totalCount+=parser.getArrayStream().count();
            	}
            }                
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
    	
        for(int i = 0; (resource = getResourceFile(String.format("raw/%s_changes_%d.json", project.name, i))).exists(); i++) {
            try {
                JsonParser parser = Json.createParser(new FileReader(resource));
                if(parser.hasNext()) {
                	if (parser.next() == Event.START_ARRAY) {
                			parser.getArrayStream()
                					.filter(new PeriodFilter()) //We can add multiple pre-processing filters here
                					.forEach(generator::write);
                	}
                }                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        generator.writeEnd();
        generator.flush();
        generator.close();
        System.out.println("Wrote new filtered data");
    }

	private File getResourceFile(String filename) {
		return new File("src/main/resources/", filename);
	}
}
