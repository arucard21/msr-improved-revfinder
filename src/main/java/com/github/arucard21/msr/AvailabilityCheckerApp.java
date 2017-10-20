package com.github.arucard21.msr;

import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AvailabilityCheckerApp {

    public static void main(String[] args) throws IOException, ParseException {
        AvailabilityChecker AvChecker = new AvailabilityChecker();

        AvChecker.check(Project.OPENSTACK);

        Map<String, ArrayList> reviewersAtDay = AvChecker.getReviewersAtDay();
        System.out.println(reviewersAtDay.size());
        System.out.println("newest: " + AvChecker.getNewest());
        System.out.println("oldest: " + AvChecker.getOldest());

        Iterator it = AvChecker.getReviewersAtDay().entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }

        // testing
        //AvChecker.checkBinaryAvailability(AvChecker.getDateFromString("2011-11-09 00:00:00"), 679);
    }
}
