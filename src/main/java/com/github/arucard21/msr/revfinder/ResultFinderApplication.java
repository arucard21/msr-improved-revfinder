package com.github.arucard21.msr.revfinder;

import com.github.arucard21.msr.Project;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResultFinderApplication {

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	public static void main(String[] args) throws IOException, ParseException {

		Project project = Project.OPENSTACK;
		ResultFinder resultFinder = new ResultFinder(project);
		print("Results for data filtered only on created date:");
		//print(project.name + " top-k (10) accuracy = " + resultFinder.calculateTopKAccuracy(10, false, false));

		print(dtf.format(LocalDateTime.now()));
		//print(project.name + " ... with binary AV  = " + resultFinder.calculateTopKAccuracyBinaryAvailability(10));
		print(dtf.format(LocalDateTime.now()));
		//print(project.name + " ... with log AV (0.1)  = " + resultFinder.calculateTopKAccuracyLogAvailability(10, 0.1));
		print(dtf.format(LocalDateTime.now()));
		//print(project.name + " ... with log AV (0.2)  = " + resultFinder.calculateTopKAccuracyLogAvailability(10, 0.2));
		print(dtf.format(LocalDateTime.now()));
		//print(project.name + " ... with log AV (0.3)  = " + resultFinder.calculateTopKAccuracyLogAvailability(10, 0.3));
		print(dtf.format(LocalDateTime.now()));

		// openstack top-k (10) accuracy = 75.65078093712455
		// openstack ... with binary AV  = 72.62715258309971
		// openstack ... with log AV (0.1)  = 69.40328394072887
		// openstack ... with log AV (0.2)  = 58.129755706848215
		// openstack ... with log AV (0.3)  = 42.49098918702443
		// openstack ... with log AV (0.4)  = 18.702442931517822

	}

	static void print(String str) {
		System.out.println(str);
	}
}
