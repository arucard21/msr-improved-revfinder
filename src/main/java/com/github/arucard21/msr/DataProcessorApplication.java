package com.github.arucard21.msr;

import java.io.IOException;

public class DataProcessorApplication {

	public static void main(String[] args) throws IOException {
		DataProcessor processor = new DataProcessor();

		System.out.printf("Collected %d changes from Gerrit for Android\n", processor.countChangesForProject(DataProcessor.Project.ANDROID));
		System.out.printf("Collected %d changes from Gerrit for Chromium\n", processor.countChangesForProject(DataProcessor.Project.CHROMIUM));
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForProject(DataProcessor.Project.OPENSTACK));
		System.out.printf("Collected %d changes from Gerrit for Qt\n", processor.countChangesForProject(DataProcessor.Project.QT));
		
		processor.filter();
		
		System.out.printf("Filtered %d changes for Android\n", processor.countFilteredChangesForProject(DataProcessor.Project.ANDROID));
		System.out.printf("Filtered %d changes for Chromium\n", processor.countFilteredChangesForProject(DataProcessor.Project.CHROMIUM));
		System.out.printf("Filtered %d changes for OpenStack\n", processor.countFilteredChangesForProject(DataProcessor.Project.OPENSTACK));
		System.out.printf("Filtered %d changes for Qt\n", processor.countFilteredChangesForProject(DataProcessor.Project.QT));
	}
}
