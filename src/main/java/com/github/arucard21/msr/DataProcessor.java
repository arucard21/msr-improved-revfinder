package com.github.arucard21.msr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;

public class DataProcessor {
	
	public JsonObject showFirstObject(JsonParser parser) {
		while(parser.hasNext()) {
			switch(parser.next()) {
				case START_ARRAY: 
					break;
				case START_OBJECT: 
					return parser.getObject();
				case KEY_NAME: 
					break;
				case VALUE_STRING: 
					break;
				case VALUE_NUMBER: 
					break;
				case VALUE_TRUE: 
					break;
				case VALUE_FALSE: 
					break;
				case VALUE_NULL: 
					break;
				case END_OBJECT: 
					break;
				case END_ARRAY: 
					break;
				default: 
					break;
			}
		}
		return null;
	}

	public JsonParser getAndroidChanges() {
		try {
			return Json.createParser(new FileReader(getResourceFile("android_changes.json")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public JsonParser getChromiumChanges() {
		try {
			return Json.createParser(new FileReader(getResourceFile("chromium_changes.json")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public JsonParser getOpenstackChanges() {
		try {
			return Json.createParser(new FileReader(getResourceFile("openstack_changes.json")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public JsonParser getQtChanges() {
		try {
			return Json.createParser(new FileReader(getResourceFile("qt_changes.json")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Count the amount of objects in the first array you find with the given parser.
	 * 
	 * @param parser for the JSON data
	 * @return count of objects in the array
	 */
	public int objectsInArray(JsonParser parser) {
		int count = 0;
		int arrayDepth = 0;
		while(parser.hasNext()) {
			switch(parser.next()) {
				case START_ARRAY: 
					arrayDepth++;
					break;
				case START_OBJECT: 
					if (arrayDepth == 1) {
						count++;
					}
					break;
				case KEY_NAME: 
					break;
				case VALUE_STRING: 
					break;
				case VALUE_NUMBER: 
					break;
				case VALUE_TRUE: 
					break;
				case VALUE_FALSE: 
					break;
				case VALUE_NULL: 
					break;
				case END_OBJECT: 
					break;
				case END_ARRAY: 
					arrayDepth--;
					break;
				default: 
					break;
			}
		}
		return count;
	}
	
	private File getResourceFile(String filename) {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL resource = classloader.getResource(filename);
		try {
			return Paths.get(resource.toURI()).toFile();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
}
