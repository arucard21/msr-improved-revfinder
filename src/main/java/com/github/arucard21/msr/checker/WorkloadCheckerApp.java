package com.github.arucard21.msr.checker;

import com.github.arucard21.msr.Project;

public class WorkloadCheckerApp {

    public static void main(String[] args) throws Exception {
        WorkloadChecker WlChecker = new WorkloadChecker();

        WlChecker.check(Project.OPENSTACK);

        String day = "2011-11-04";
        int revID = 5638;
        float revWorkload = WlChecker.getReviewerWorkloadByDay(revID, day);

        System.out.println("workload at " + day + " for " + revID + " = " + revWorkload);
    }
}
