package com.github.arucard21.msr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;

public class DataProcessor {
	public int countChangesForAndroid() {
		int totalCount = 0;
		int i = 0;
		while(getResourceFile("android_changes_"+i+".json") != null) {
			try {
				JsonParser parser = Json.createParser(new FileReader(getResourceFile("android_changes_"+i+".json")));
				parser.next();
				totalCount+=parser.getArrayStream().count();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			i++;
		}
		return totalCount;
		
	}
	
	public int countChangesForOpenStack() {
		int totalCount = 0;
		int i = 0;
		while(getResourceFile("openstack_changes_"+i+".json") != null) {
			try {
				JsonParser parser = Json.createParser(new FileReader(getResourceFile("openstack_changes_"+i+".json")));
				parser.next();
				totalCount+=parser.getArrayStream().count();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			i++;
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
