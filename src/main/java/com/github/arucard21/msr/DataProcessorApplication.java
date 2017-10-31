package com.github.arucard21.msr;

import java.io.IOException;

public class DataProcessorApplication {

	public static void main(String[] args) throws IOException {
		DataProcessor processor = new DataProcessor();

//		System.out.printf("Collected %d changes from Gerrit for Eclipse\n", processor.countChangesForProject(Project.ECLIPSE));
		System.out.printf("Collected %d changes from Gerrit for MediaWiki\n", processor.countChangesForProject(Project.MEDIAWIKI));
		System.out.printf("Collected %d changes from Gerrit for OpenStack\n", processor.countChangesForProject(Project.OPENSTACK));
		System.out.printf("Collected %d changes from Gerrit for Qt\n", processor.countChangesForProject(Project.QT));
		System.out.println("");

//		processor.filter(Project.ECLIPSE);
//		processor.filterMore(Project.ECLIPSE);
		processor.filter(Project.MEDIAWIKI);
		processor.filterMore(Project.MEDIAWIKI);
		processor.filter(Project.OPENSTACK);
		processor.filterMore(Project.OPENSTACK);
		processor.filter(Project.QT);
		processor.filterMore(Project.QT);
		
//		System.out.printf("Filtered %d changes for Android\n", processor.countFilteredChangesForProject(Project.ECLIPSE));
		System.out.printf("Filtered %d changes for MediaWiki\n", processor.countFilteredChangesForProject(Project.MEDIAWIKI));
		System.out.printf("Filtered %d changes for OpenStack\n", processor.countFilteredChangesForProject(Project.OPENSTACK));
		System.out.printf("Filtered %d changes for Qt\n", processor.countFilteredChangesForProject(Project.QT));
		System.out.println("");
//		System.out.printf("Filtered %d changes even more for Eclipse\n", processor.countMoreFilteredChangesForProject(Project.ECLIPSE));
		System.out.printf("Filtered %d changes even more for MediaWiki\n", processor.countMoreFilteredChangesForProject(Project.MEDIAWIKI));
		System.out.printf("Filtered %d changes even more for OpenStack\n", processor.countMoreFilteredChangesForProject(Project.OPENSTACK));
		System.out.printf("Filtered %d changes even more for Qt\n", processor.countMoreFilteredChangesForProject(Project.QT));
		System.out.println("");

//		System.out.printf("Average number of reviewers per change for Eclipse: %f\n", processor.averageNumberOfReviewers(Project.ECLIPSE));
		System.out.printf("Average number of reviewers per change for MediaWiki: %f\n", processor.averageNumberOfReviewers(Project.MEDIAWIKI));
		System.out.printf("Average number of reviewers per change for OpenStack: %f\n", processor.averageNumberOfReviewers(Project.OPENSTACK));
		System.out.printf("Average number of reviewers per change for Qt: %f\n", processor.averageNumberOfReviewers(Project.QT));
		System.out.println("");
//		System.out.printf("Average number of reviewers per change (more filtered) for Eclipse: %f\n", processor.averageNumberOfReviewersForMoreFilteredData(Project.ECLIPSE));
		System.out.printf("Average number of reviewers per change (more filtered) for MediaWiki: %f\n", processor.averageNumberOfReviewersForMoreFilteredData(Project.MEDIAWIKI));
		System.out.printf("Average number of reviewers per change (more filtered) for OpenStack: %f\n", processor.averageNumberOfReviewersForMoreFilteredData(Project.OPENSTACK));
		System.out.printf("Average number of reviewers per change (more filtered) for Qt: %f\n", processor.averageNumberOfReviewersForMoreFilteredData(Project.QT));
	}
	/*
	Collected 374874 changes from Gerrit for MediaWiki
	Collected 496568 changes from Gerrit for OpenStack
	Collected 181337 changes from Gerrit for Qt
	
	Filtered 5568 changes for MediaWiki
	Filtered 4994 changes for OpenStack
	Filtered 21919 changes for Qt
	
	Filtered 5430 changes even more for MediaWiki
	Filtered 4858 changes even more for OpenStack
	Filtered 21368 changes even more for Qt
	
	Average number of reviewers per change for MediaWiki: 1.007184
	Average number of reviewers per change for OpenStack: 1.460753
	Average number of reviewers per change for Qt: 1.066472
	
	Average number of reviewers per change (more filtered) for MediaWiki: 1.007182
	Average number of reviewers per change (more filtered) for OpenStack: 1.457390
	Average number of reviewers per change (more filtered) for Qt: 1.065706
	 */
}
