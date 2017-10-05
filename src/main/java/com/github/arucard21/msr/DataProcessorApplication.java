package com.github.arucard21.msr;

import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

public class DataProcessorApplication {
	private static JsonWriter writer;

	public static void main(String[] args) {
		configurePrettyPrintWriterToSysOut();
		DataProcessor processor = new DataProcessor();
		System.out.printf("Collected %d changes from Gerrit for Android\n", processor.objectsInArray(processor.getAndroidChanges()));
		System.out.printf("Collected %d changes from Gerrit for Chromium\n", processor.objectsInArray(processor.getChromiumChanges()));
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.objectsInArray(processor.getOpenstackChanges()));
		System.out.printf("Collected %d changes from Gerrit for Qt\n", processor.objectsInArray(processor.getQtChanges()));
		
		writer.write(processor.showFirstObject(processor.getAndroidChanges()));
	}

	private static JsonWriter configurePrettyPrintWriterToSysOut() {
		HashMap<String, Object> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);
		writer = Json.createWriterFactory(config).createWriter(System.out);
		return writer;
	}
}
