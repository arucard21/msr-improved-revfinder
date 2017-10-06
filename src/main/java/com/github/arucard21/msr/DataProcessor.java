package com.github.arucard21.msr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import javax.json.Json;
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
        for(int i = 0; (resource = getResourceFile(String.format("%s_changes_%d.json", project.name, i))) != null; i++) {
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

	private File getResourceFile(String filename) {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL resource = classloader.getResource(filename);
		if (resource == null) {
			return null;
		}
		try {
			return Paths.get(resource.toURI()).toFile();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
}
