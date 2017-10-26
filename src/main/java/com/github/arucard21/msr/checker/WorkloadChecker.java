package com.github.arucard21.msr.checker;

import com.github.arucard21.msr.Project;
import com.sun.xml.internal.bind.v2.TODO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class WlReview {
    String date;
    int reviewerID;
    int nrFiles;

    WlReview(String date, int reviewerID, int nrFiles) {
        this.date = date;
        this.reviewerID = reviewerID;
        this.nrFiles = nrFiles;
    }
}

public class WorkloadChecker {
    public Set<JSONObject> getPresumedBots() {
        return presumedBots;
    }

    private Set presumedBots;
    private HashMap workload;

    // workload == files to review
    private HashMap avgWorkloadByDay;
    private HashMap dayToWorkload;


    public WorkloadChecker() {
        presumedBots = new HashSet<JSONObject>();
        avgWorkloadByDay = new HashMap<String, Float>();

        // map<reviewerID, map<day, files>>
        workload = new HashMap<Integer, HashMap<String, Integer>>();

        // map<day, map<reviewerID, files>>
        dayToWorkload = new HashMap<String, HashMap<Integer, Integer>>();
    }

    public void check(Project project) throws Exception {

        JSONParser parser = new JSONParser();
        String filename = project.name + "_changes.json";
        JSONArray reviews = (JSONArray) parser.parse(new FileReader(getResourceFile(filename)));

        int breakpoint = 0;
        int countEmptyRevisions = 0;
        HashMap authorFq = new HashMap<String, Integer>();

        for(Object rev : reviews)
        {
            JSONObject review = (JSONObject) rev;
            int ownerID = ((Long) review.get("owner")).intValue();

            String changeID = (String) review.get("change_id");
            if(isExcluded(changeID, project))
                continue;

            String fromDate = ((String) review.get("created")).substring(0, 10);
            String toDate = fromDate;

            JSONArray messages = (JSONArray) review.get("messages");
            int msgCount = messages.size();
            int nrReviews = 0;
            ArrayList workloads = new ArrayList<WlReview>();
            HashSet reviewHashes = new HashSet<String>();
            HashMap reviewerToFilesize = new HashMap<String, Integer>();

            for(int i = 0; i < msgCount; i++)
            {
                JSONObject msg = (JSONObject) messages.get(i);

                // TODO PREPROCESSING
                if(! msg.containsKey("author")) continue;
                JSONObject author = (JSONObject) msg.get("author");
                toDate = ((String) msg.get("date")).substring(0, 10);

                int reviewerID = ((Long) author.get("_account_id")).intValue();

                // TODO FILTER IN PREPROCESSING
                boolean isOwner = (ownerID == reviewerID);
                boolean isBot = isBot(author);

                if(isOwner || isBot)
                {
                    fromDate = toDate;
                    continue;
                }

                String authorName = (String) author.get("name") + "[" + reviewerID + "]";
                if(authorFq.containsKey(authorName))
                    authorFq.put(authorName, (int) authorFq.get(authorName) + 1);
                else
                    authorFq.put(authorName, 1);

                nrReviews++;
                int revision = ((Long) msg.get("_revision_number")).intValue();
                int fileCount = getFileCount(review, revision);
                if(fileCount == 0) countEmptyRevisions++;
                //System.out.println("[" + i + "] " + revision + "(" + fileCount + "): " + fromDate + " - " + toDate + " : " + reviewerID);

                addReviews(workloads, reviewHashes, reviewerToFilesize, reviewerID, fileCount, fromDate, toDate);
                fromDate = toDate;
            }

            HashSet dayOnlyOnce = new HashSet<String>();
            HashSet dayOnlyOnce2 = new HashSet<String>();
            for(Object wlRev : workloads)
            {
                WlReview wlReview = (WlReview) wlRev;
                //System.out.println("[*] " + wlReview.date + "\t" + wlReview.nrFiles + " => " + reviewerToFilesize.get(wlReview.date + wlReview.reviewerID) + "\t" + wlReview.reviewerID );

                // build reviewerID --> workload
                int workloadOfCurrentReview = (int) reviewerToFilesize.get(wlReview.date + wlReview.reviewerID);
                HashMap revIdWorkload = new HashMap<String, Integer>();
                if(workload.containsKey(wlReview.reviewerID))
                {
                    revIdWorkload = (HashMap) workload.get(wlReview.reviewerID);
                    if(revIdWorkload.containsKey(wlReview.date)
                            && ! dayOnlyOnce.contains(wlReview.date + wlReview.reviewerID))
                    {
                        workloadOfCurrentReview += (int) revIdWorkload.get(wlReview.date);
                    }
                }
                revIdWorkload.put(wlReview.date, workloadOfCurrentReview);
                workload.put(wlReview.reviewerID, revIdWorkload);
                dayOnlyOnce.add(wlReview.date + wlReview.reviewerID);

                // build date --> workload
                workloadOfCurrentReview = (int) reviewerToFilesize.get(wlReview.date + wlReview.reviewerID);
                HashMap revDayWorkload = new HashMap<Integer, Integer>();
                if(dayToWorkload.containsKey(wlReview.date))
                {
                    revDayWorkload = (HashMap) dayToWorkload.get(wlReview.date);
                    if(revDayWorkload.containsKey(wlReview.reviewerID)
                            && ! dayOnlyOnce2.contains(wlReview.date + wlReview.reviewerID))
                    {
                        workloadOfCurrentReview += (int) revDayWorkload.get(wlReview.reviewerID);
                    }
                }
                revDayWorkload.put(wlReview.reviewerID, workloadOfCurrentReview);
                dayToWorkload.put(wlReview.date, revDayWorkload);
                dayOnlyOnce2.add(wlReview.date + wlReview.reviewerID);
            }

            breakpoint++;
            if(breakpoint == -1)
            {
                break;
            }
        }

        //printWorkloadByReviewerId();
        printWorkloadByDate();
        printAvgWorkloadByDate();
        //printAuthorFQ(authorFq);

        System.out.println("\n-------------------------------------------");
        printPresumedBots();

        // TODO PREPROCESSING
        System.out.println("\n-------------------------------------------");
        System.out.println("*] empty revisions: " + countEmptyRevisions);
        System.out.println("-------------------------------------------\n");
    }

    private boolean isExcluded(String changeID, Project project) {
        ArrayList excluded = new ArrayList();

        if(project.equals(Project.OPENSTACK))
        {
            // reviews go over several years
            excluded.add("I1aafec1b2a3943e0f6c86f0228ab29f181a7ffce");
            excluded.add("I00ebc716dccbe9fb97e2b8a3cb5d5e496bc7719b");
            excluded.add("I310adfe00a3ad6c3631a71c9d2b1befb12a31f5a");
        }

        if(project.equals(Project.QT))
        {
            excluded.add("I0d1f95f3d1194b33113175689cfe2832ede0ffa4");
            excluded.add("I39e4156bd9ba743afa7e106e934c90227fbf2b8b");
            excluded.add("I1267979af7601129e5483f8785d4982a1f2f8182");
            excluded.add("I353debd4338f2a3ce2fa1cfa1bff9dd2e36f05ab");
            excluded.add("Ibe246d47ab7667692386b0f9333150c195948282");
            excluded.add("I5799d0e7a835537b46a520e2dfe5d78b891f0c89");
            excluded.add("Ifc6e22f135a1f6bff7c0fa8bef3ea7e1042ae819");
            excluded.add("I851e0b1c3f80a7b33a38cb1ab2665dc0f3c73adc");
            excluded.add("Ic1ef67f500f9ff92d36164d515f4e004ef2a10bc");
            excluded.add("I8c187908c4483f90f1b99fa6182ce1532385173d");
            excluded.add("I85c00dd88547f8dea9b1e1ef2da31d2f2e28a172");
            excluded.add("I2d28270563eebb495b82fac8ec23346c98e859f9");
            excluded.add("I85c00dd88547f8dea9b1e1ef2da31d2f2e28a172");
            excluded.add("If69149678e7fba6d812d31dcc17877427f9a6122");
            excluded.add("Idd9819c3f4c48f98ef92831d5e8e5ac0fa42283c");
            excluded.add("I85c00dd88547f8dea9b1e1ef2da31d2f2e28a172");
            excluded.add("I70887773952ecaa61da21077ffec321fd5fabbb1");
            excluded.add("I3316498e5bb10c416138ca14c3a7f8b143c8e544");
            excluded.add("Ia784d4cbc4f37c925aa49e53d04faf06a7169a73");
            excluded.add("Ib5a07730836a42533d5488882e877074ccceea4c");
            excluded.add("I4c683c08876cc6fa934971399af7e48b160168fc");
            excluded.add("I8510f8d67c22230653ec0f1c252c01bc95f3c386");
            excluded.add("Ifa62ea7983905ecb11863257e18ea04565474fc9");
            excluded.add("If43866d74b49273816fb80059507da70498402a6");
            excluded.add("I70efb0db869b9e7de862edd0cd4754764e2e2099");
            excluded.add("I73872956f462624a77d5258883b8c45c4afa6b36");
        }

        return excluded.contains(changeID);
    }

    private void printPresumedBots() {
        System.out.println("*] presumed bots:");
        for(JSONObject author : getPresumedBots())
        {
            System.out.println("\t" + author);
        }
    }

    private void printAuthorFQ(HashMap authorFq) {
        SortedSet<String> keysString = new TreeSet<String>(authorFq.keySet());;
        for (String name : keysString)
        {
            int fq = (int) authorFq.get(name);
            System.out.println(fq + "\t" + name);
        }
    }

    private void printAvgWorkloadByDate() {
        SortedSet<String> keysString = new TreeSet<String>(avgWorkloadByDay.keySet());
        for (String date : keysString)
        {
            float avgReviews = (float) avgWorkloadByDay.get(date);
            System.out.println(date + " : " + avgReviews);
        }
    }

    private void printWorkloadByDate() {
        SortedSet<String> keysString = new TreeSet<String>(dayToWorkload.keySet());
        for (String date : keysString)
        {
            System.out.println(date);
            HashMap revWorkloads = (HashMap) dayToWorkload.get(date);

            SortedSet<Integer> keysInt = new TreeSet<Integer>(revWorkloads.keySet());
            int overallWorkloadOfDay = 0;
            for (Integer reviewerID : keysInt)
            {
                int workloadOfDay = (int) revWorkloads.get(reviewerID);
                overallWorkloadOfDay += workloadOfDay;
                System.out.println("\t" + reviewerID + " => " + workloadOfDay);
            }
            float avgWorkload = (float) overallWorkloadOfDay / revWorkloads.size();
            avgWorkloadByDay.put(date, avgWorkload);
        }
    }

    private void printWorkloadByReviewerId() {
        SortedSet<Integer> keysInt = new TreeSet<Integer>(workload.keySet());
        for (Integer reviewerID : keysInt)
        {
            System.out.println(reviewerID);
            HashMap revWorkloads = (HashMap) workload.get(reviewerID);

            SortedSet<String> keysString = new TreeSet<String>(revWorkloads.keySet());
            for (String date : keysString)
            {
                int workloadOfDay = (int) revWorkloads.get(date);
                System.out.println("\t" + date + " => " + workloadOfDay);
            }
        }
    }

    private void addReviews(ArrayList workloads, HashSet reviewHashes, HashMap reviewerToFilesize, int reviewerID, int size, String fromDate, String toDate) {
        Date from = getDateFromString(fromDate + " 00:00:00");
        Date to = getDateFromString(toDate + " 00:00:00");

        // add one day for .before comparison
        Calendar cal = Calendar.getInstance();
        cal.setTime(to);
        cal.add(Calendar.DATE, 1);
        to = cal.getTime();

        for(cal.setTime(from); from.before(to);)
        {
            //System.out.println("\t" + from + " after " + to + " = "+ from.before(to));
            String dateString = getDateStringFromDate(from);
            String id = dateString + reviewerID + size;
            if(reviewHashes.add(id))
            {
                String filesizeID = dateString + reviewerID;
                int currentSize = size;
                if(reviewerToFilesize.containsKey(filesizeID))
                {
                    currentSize = (int) reviewerToFilesize.get(filesizeID);
                    currentSize = currentSize > size ? currentSize : size;
                }
                reviewerToFilesize.put(filesizeID, currentSize);
                workloads.add(new WlReview(dateString, reviewerID, size));
            }

            cal.add(Calendar.DATE, 1);
            from = cal.getTime();
        }
    }

    private int getFileCount(JSONObject review, int revision_number) throws Exception {
        JSONObject revisions = (JSONObject) review.get("revisions");

        for (Object revKey : revisions.keySet())
        {
            JSONObject revision = (JSONObject) revisions.get(revKey.toString());
            int currentRevNr = ((Long) revision.get("_number")).intValue();
            if(currentRevNr == revision_number)
            {
                //TODO PREPROCESSING
                if(! revision.containsKey("files"))
                    return 0;

                return ((JSONObject) revision.get("files")).size();
            }
        }

        return 0;

        //TODO PREPROCESSING
        //throw new Exception("revision number not found");
    }

    private boolean isBot(JSONObject author) {
        boolean isBot = false;
        //isBot |= ! author.containsKey("email");
        isBot |= ((String) author.get("name")).contains("Jenkins");
        isBot |= ((String) author.get("name")).contains("CI");
        isBot |= ((String) author.get("name")).contains("Continuous Integration");
        isBot |= ((String) author.get("name")).contains("Qt");

        if(isBot) presumedBots.add(author);
        return isBot;
    }

    private File getResourceFile(String filename) {
        return new File("src/main/data/filtered", filename);
    }

    public Date getDateFromString(String dateString) {
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
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

    public float getAvgWorkloadByDay(String day) {
        if(! avgWorkloadByDay.containsKey(day))
            return 0;

        float avgWorkload = (float) avgWorkloadByDay.get(day);
        return avgWorkload;
    }

    public float getReviewerWorkloadByDay(int revID, String day) {
        if(! dayToWorkload.containsKey(day))
            return 0;

        HashMap workloads = (HashMap) dayToWorkload.get(day);
        if(! workloads.containsKey(revID))
            return 0;

        int workload = (int) workloads.get(revID);
        float avgWorkload = (float) avgWorkloadByDay.get(day);
        return workload / avgWorkload;
    }
}
