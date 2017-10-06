package com.github.arucard21.msr;

public class DataProcessorApplication {

	public static void main(String[] args) {
		DataProcessor processor = new DataProcessor();

		System.out.printf("Collected %d changes from Gerrit for Android\n", processor.countChangesForProject(DataProcessor.PROJECT.ANDROID));
		System.out.printf("Collected %d changes from Gerrit for Chromium\n", processor.countChangesForProject(DataProcessor.PROJECT.CHROMIUM));
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForProject(DataProcessor.PROJECT.OPENSTACK));
		System.out.printf("Collected %d changes from Gerrit for Qt\n", processor.countChangesForProject(DataProcessor.PROJECT.QT));

		//System.out.printf("Collected %d changes from Gerrit for Android\n", processor.countChangesForAndroid());
		//System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForOpenStack());
	}
}
