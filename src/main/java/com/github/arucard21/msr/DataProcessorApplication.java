package com.github.arucard21.msr;

import java.io.IOException;

public class DataProcessorApplication {

	public static void main(String[] args) throws IOException {
		DataProcessor processor = new DataProcessor();

		System.out.printf("Collected %d changes from Gerrit for Android\n", processor.countChangesForProject(Project.ANDROID));
		System.out.printf("Collected %d changes from Gerrit for Chromium\n", processor.countChangesForProject(Project.CHROMIUM));
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForProject(Project.OPENSTACK));
		System.out.printf("Collected %d changes from Gerrit for Qt\n", processor.countChangesForProject(Project.QT));
		
		processor.filter();
		
		System.out.printf("Filtered %d changes for Android\n", processor.countFilteredChangesForProject(Project.ANDROID));
		System.out.printf("Filtered %d changes for Chromium\n", processor.countFilteredChangesForProject(Project.CHROMIUM));
		System.out.printf("Filtered %d changes for OpenStack\n", processor.countFilteredChangesForProject(Project.OPENSTACK));
		System.out.printf("Filtered %d changes for Qt\n", processor.countFilteredChangesForProject(Project.QT));
	}
}
