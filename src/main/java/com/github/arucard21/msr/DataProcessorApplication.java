package com.github.arucard21.msr;

import java.io.IOException;

public class DataProcessorApplication {

	public static void main(String[] args) throws IOException {
		DataProcessor processor = new DataProcessor();

		System.out.printf("Collected %d changes from Gerrit for Android\n", processor.countChangesForProject(Project.ANDROID));
		System.out.printf("Collected %d changes from Gerrit for Chromium\n", processor.countChangesForProject(Project.CHROMIUM));
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForProject(Project.OPENSTACK));
		System.out.printf("Collected %d changes from Gerrit for Qt\n", processor.countChangesForProject(Project.QT));
		/*
		 * Latest Output:
			Collected 9051 changes from Gerrit for Android
			Collected 9463 changes from Gerrit for Chromium
			Collected 493548 changes from Gerrit for OpenStack
			Collected 180092 changes from Gerrit for Qt
		 */
		
		processor.filter();
		
		System.out.printf("Filtered %d changes for Android\n", processor.countFilteredChangesForProject(Project.ANDROID));
		System.out.printf("Filtered %d changes for Chromium\n", processor.countFilteredChangesForProject(Project.CHROMIUM));
		System.out.printf("Filtered %d changes for OpenStack\n", processor.countFilteredChangesForProject(Project.OPENSTACK));
		System.out.printf("Filtered %d changes for Qt\n", processor.countFilteredChangesForProject(Project.QT));
		/*
		 * Latest Output:
			Filtered 420 changes for Android
			Filtered 20 changes for Chromium
			Filtered 5966 changes for OpenStack
			Filtered 24789 changes for Qt
		 */
	}
}
