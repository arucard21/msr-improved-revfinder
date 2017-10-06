package com.github.arucard21.msr;

public class DataProcessorApplication {

	public static void main(String[] args) {
		DataProcessor processor = new DataProcessor();
		System.out.printf("Collected %d changes from Gerrit for Android\n", processor.countChangesForAndroid());
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForOpenStack());
		
	}
}
