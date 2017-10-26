package com.github.arucard21.msr.checker;

import com.github.arucard21.msr.Project;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AvailabilityChecker {
    public Map<String, Set> getReviewersByDay() {
        return reviewersByDay;
    }

    public Date getOldest() {
        return oldest;
    }

    public Date getNewest() {
        return newest;
    }

    private Map<String, Set> reviewersByDay;
    private Date oldest;
    private Date newest;
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    public AvailabilityChecker() {
        reviewersByDay = new HashMap<>();
        oldest = getDateFromString("2018-01-01 00:00:00");
        newest = getDateFromString("1990-01-01 00:00:00");
    }

    public void check(Project project) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        String filename = project.name + "_changes_small.json";
        JSONArray reviews = (JSONArray) parser.parse(new FileReader(getResourceFile(filename)));

        for (Object rev : reviews)
        {
            JSONObject review = (JSONObject) rev;
            int owner = ((Long) review.get("owner")).intValue();

            JSONArray messages = (JSONArray) review.get("messages");
            for (Object m : messages)
            {
                JSONObject msg = (JSONObject) m;

                // TODO PREPROCESSING
                if(! msg.containsKey("author")) continue;
                JSONObject author = (JSONObject) msg.get("author");

                // TODO PREPROCESSING
                if(! author.containsKey("_account_id")) continue;
                int reviewer = ((Long) author.get("_account_id")).intValue();

                if(owner != reviewer)
                {
                    // review happening
                    String dayString = ((String) msg.get("date")).substring(0, 10);
                    String dateString = dayString + " 00:00:00";
                    Date date = getDateFromString(dateString);
                    addReview(dayString, reviewer);

                    if(date.after(newest))
                        newest = date;

                    if(date.before(oldest))
                        oldest = date;
                }
            }
        }
    }

    public boolean checkBinaryAvailability(Date today, int reviewer) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);

        for(int i = 1; i <= 7; i++)
        {
            cal.add(Calendar.DATE, -1);
            Date date = cal.getTime();
            String dateString = getDateStringFromDate(date);

            if(! reviewersByDay.containsKey(dateString))
                continue;

            System.out.println(reviewer + " ? " + date.toString() + " : " + reviewersByDay.get(getDateStringFromDate(date)));
            if(reviewersByDay.get(dateString).contains(reviewer))
                return true;
        }

        return false;
    }

    public float checkLogAvailability(Date today, int reviewer) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        float avScore = 0;
        double sum = 4.3810663;

        for(int i = 1; i <= 7; i++) {
            cal.add(Calendar.DATE, -1);
            Date date = cal.getTime();
            String dateString = getDateStringFromDate(date);

            if (!reviewersByDay.containsKey(dateString))
                continue;

            System.out.println(reviewer + " ? " + date.toString() + " : " + reviewersByDay.get(getDateStringFromDate(date)));
            if (reviewersByDay.get(dateString).contains(reviewer)) {
                double score = Math.log(7 - i + 1) / Math.log(7);
                avScore += score;
                //System.out.println("  + " + i + " : " + score);
            }
            sum += Math.log(7 - i + 1) / Math.log(7);
        }
        return (float) (avScore / sum);
    }

    private File getResourceFile(String filename) {
        return new File("src/main/data/filtered", filename);
    }

    public Date getDateFromString(String dateString) {
        DateFormat df = new SimpleDateFormat(dateFormat);
        Date date = null;
        try {
            date = df.parse(dateString);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private String getDateStringFromDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format.format(date);
        return dateString;
    }

    private void addReview(String date, int id) {
        Set reviewers = reviewersByDay.get(date);
        if(reviewers == null) reviewers = new HashSet();
        reviewers.add(id);
        reviewersByDay.put(date, reviewers);
    }
}
