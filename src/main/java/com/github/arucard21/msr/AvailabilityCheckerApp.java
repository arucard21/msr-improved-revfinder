package com.github.arucard21.msr;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AvailabilityCheckerApp {

    public static void main(String[] args) throws IOException, ParseException {
        AvailabilityChecker AvChecker = new AvailabilityChecker();

        AvChecker.check(Project.OPENSTACK);

        Map<String, List<Integer>> reviewersAtDay = AvChecker.getReviewersAtDay();
        System.out.println(reviewersAtDay.size());
        System.out.println("newest: " + AvChecker.getNewest());
        System.out.println("oldest: " + AvChecker.getOldest());

        for(Entry<String, List<Integer>> date : AvChecker.getReviewersAtDay().entrySet()) {
            System.out.println(date.getKey() + " = " + date.getValue());
        }

        // testing
        //AvChecker.checkBinaryAvailability(AvChecker.getDateFromString("2011-11-09 00:00:00"), 679);
    }
}
