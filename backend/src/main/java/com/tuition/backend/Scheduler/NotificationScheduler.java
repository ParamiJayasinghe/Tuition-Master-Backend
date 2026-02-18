package com.tuition.backend.Scheduler;

import com.tuition.backend.Entity.NotificationType;
import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Repository.TeacherRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.Repository.FeeRepository;
import com.tuition.backend.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class NotificationScheduler {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private NotificationService notificationService;

    // Run every day at 08:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendPendingFeeNotifications() {
        System.out.println("Running scheduled task to notify pending fees...");
        
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Teacher> teachers = teacherRepository.findAll();

        for (Teacher teacher : teachers) {
            // Find students created by this teacher
            List<Student> students = studentRepository.findAllByCreatedBy(teacher);
            
            int pendingCount = 0;
            StringBuilder studentNames = new StringBuilder();

            for (Student student : students) {
                String[] subjects = student.getSubjects() != null ? student.getSubjects().split(",") : new String[0];
                
                for (String subject : subjects) {
                    String trimmedSub = subject.trim();
                    if (trimmedSub.isEmpty()) continue;

                    // Check if a PAID record exists for this month/year/subject
                    boolean isPaid = feeRepository.existsByStudentAndSubjectAndMonthAndYearAndStatus(
                            student, trimmedSub, month, year, "PAID"
                    );

                    if (!isPaid) {
                        pendingCount++;
                        if (studentNames.length() > 0) studentNames.append(", ");
                        studentNames.append(student.getFullName()).append(" (").append(trimmedSub).append(")");
                    }
                }
            }

            if (pendingCount > 0) {
                notificationService.createNotification(
                        teacher.getUser(),
                        NotificationType.PENDING_FEE_REMINDER,
                        "Pending Fee Reminder",
                        "You have " + pendingCount + " pending fee payments for " + now.getMonth().name() + ": " + studentNames.toString()
                );
            }
        }
    }
}
