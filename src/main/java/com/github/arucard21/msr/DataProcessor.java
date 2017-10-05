package com.github.arucard21.msr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonParser;

public class DataProcessor {
	
	private static JsonParser androidChanges;
	private static JsonArray chromiumChanges;
	private static JsonArray openstackChanges;
	private static JsonArray qtChanges;
	
	public static void main(String[] args) {
		loadChanges();
		System.out.printf("Collected %d changes from Gerrit for Android\n", jsonArraySize(androidChanges));
//		System.out.printf("Collected %d changes from Gerrit for Chromium\n", chromiumChanges.size());
//		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", openstackChanges.size());
//		System.out.printf("Collected %d changes from Gerrit for Qt\n", qtChanges.size());
	}

	private static long jsonArraySize(JsonParser parser) {	
		parser.next();
		return parser.getArrayStream().count();
	}

	/**
	 * Uses {@link JsonReader} to load the JSON files into memory
	 * 
	 * If the files turn out to be too large, we may need to switch to JsonParser in a streaming way.
	 *  
	 * @throws FileNotFoundException
	 */
	private static void loadChanges() {
		try {
			androidChanges = Json.createParser(new FileReader(Paths.get(getResourceFile("android_changes.json").toURI()).toFile()));
//			chromiumChanges = Json.createParser(new FileReader(Paths.get(getResourceFile("chromium_changes.json").toURI()).toFile())).getArray();
//			openstackChanges = Json.createParser(new FileReader(Paths.get(getResourceFile("openstack_changes.json").toURI()).toFile())).getArray();
//			qtChanges = Json.createParser(new FileReader(Paths.get(getResourceFile("qt_changes.json").toURI()).toFile())).getArray();
		} catch (FileNotFoundException e) {
			System.err.println("File containing Gerrit changes could not be found");
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static URL getResourceFile(String filename) {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL resource = classloader.getResource(filename);
		return resource;
	}
}
