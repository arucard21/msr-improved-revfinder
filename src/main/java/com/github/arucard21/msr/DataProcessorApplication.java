package com.github.arucard21.msr;

public class DataProcessorApplication {

	public static void main(String[] args) {
		DataProcessor processor = new DataProcessor();

		System.out.printf("Collected %d changes from Gerrit for Android\n", processor.countChangesForProject(DataProcessor.Project.ANDROID));
		System.out.printf("Collected %d changes from Gerrit for Chromium\n", processor.countChangesForProject(DataProcessor.Project.CHROMIUM));
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForProject(DataProcessor.Project.OPENSTACK));
		System.out.printf("Collected %d changes from Gerrit for Qt\n", processor.countChangesForProject(DataProcessor.Project.QT));
	}
}
