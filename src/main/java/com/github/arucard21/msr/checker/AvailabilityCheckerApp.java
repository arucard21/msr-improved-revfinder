package com.github.arucard21.msr.checker;

import com.github.arucard21.msr.Project;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AvailabilityCheckerApp {

    public static void main(String[] args) throws IOException, ParseException {
        AvailabilityChecker AvChecker = new AvailabilityChecker();

        AvChecker.check(Project.OPENSTACK);

        Map<String, Set> reviewersAtDay = AvChecker.getReviewersByDay();
        System.out.println(reviewersAtDay.size());
        System.out.println("newest: " + AvChecker.getNewest());
        System.out.println("oldest: " + AvChecker.getOldest());

        for(Entry<String, Set> date : AvChecker.getReviewersByDay().entrySet()) {
            System.out.println(date.getKey() + " = " + date.getValue());
        }

        // testing
        System.out.println("679 available ? : " + AvChecker.checkBinaryAvailability(AvChecker.getDateFromString("2011-11-09 00:00:00"), 679));
        System.out.println("679 available ? : " + AvChecker.checkLogAvailability(AvChecker.getDateFromString("2011-11-09 00:00:00"), 679));

    }
}
