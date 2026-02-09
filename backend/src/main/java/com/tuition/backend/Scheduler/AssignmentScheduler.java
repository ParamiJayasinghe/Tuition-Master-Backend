package com.tuition.backend.Scheduler;

import com.tuition.backend.Service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssignmentScheduler {

    @Autowired
    private AssignmentService assignmentService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkForExpiredAssignments() {
        System.out.println("Running scheduled task to expire assignments...");
        assignmentService.updateAssignmentsStatus();
    }
}
