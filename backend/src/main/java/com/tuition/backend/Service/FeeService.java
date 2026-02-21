package com.tuition.backend.Service;

import com.tuition.backend.Entity.Fee;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Repository.FeeRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.config.AuditLog;
import com.tuition.backend.dto.FeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeService {

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Transactional
    @AuditLog(action = "CREATE_FEE", targetType = "Fee")
    public FeeDto createFee(FeeDto feeDto) {
        Student student = studentRepository.findById(feeDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Fee fee = new Fee();
        fee.setStudent(student);
        fee.setSubject(feeDto.getSubject()); // Directly set subject
        fee.setAmount(feeDto.getAmount());
        fee.setMonth(feeDto.getMonth());
        fee.setYear(feeDto.getYear());
        fee.setDueDate(feeDto.getDueDate());
        fee.setStatus(feeDto.getStatus() != null ? feeDto.getStatus() : "PENDING");
        fee.setGrade(feeDto.getGrade() != null ? feeDto.getGrade() : student.getGrade());
        fee.setNotes(feeDto.getNotes());
        fee.setPaymentMethod(feeDto.getPaymentMethod());
        fee.setPaidOn(feeDto.getPaidOn());

        Fee savedFee = feeRepository.save(fee);
        return mapToDto(savedFee);
    }

    @Transactional
    @AuditLog(action = "UPDATE_FEE", targetType = "Fee")
    public FeeDto updateFee(Long id, FeeDto feeDto) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee record not found"));

        if (feeDto.getStatus() != null) fee.setStatus(feeDto.getStatus());
        if (feeDto.getPaymentMethod() != null) fee.setPaymentMethod(feeDto.getPaymentMethod());
        if (feeDto.getPaidOn() != null) fee.setPaidOn(feeDto.getPaidOn());
        if (feeDto.getNotes() != null) fee.setNotes(feeDto.getNotes());

        // Allow updating other fields if necessary

        Fee updatedFee = feeRepository.save(fee);
        return mapToDto(updatedFee);
    }

    @Autowired
    private com.tuition.backend.Repository.userRepository userRepository;

    @Autowired
    private com.tuition.backend.Repository.TeacherRepository teacherRepository;

    public List<FeeDto> getFees(String grade, Integer month, Integer year, String status, String subject) {
        // 0. Get Logged-in Teacher
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.tuition.backend.Entity.User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // Find teacher profile for this user
        com.tuition.backend.Entity.Teacher currentTeacher = teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for current user"));

        // 1. Fetch Students (Filtered by Teacher)
        Specification<Student> studentSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Crucial: Only students created by this teacher
            predicates.add(cb.equal(root.get("createdBy"), currentTeacher));

            if (grade != null && !grade.isEmpty()) {
                predicates.add(cb.equal(root.get("grade"), grade));
            }
            if (subject != null && !subject.isEmpty()) {
                predicates.add(cb.like(root.get("subjects"), "%" + subject + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Student> students = studentRepository.findAll(studentSpec);

        // 2. Fetch PAID Existing Fees
        // We only care about what IS paid. Everything else is implicitly pending.
        Specification<Fee> feeSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only fetch PAID ones to check against
            predicates.add(cb.equal(root.get("status"), "PAID"));

            if (grade != null && !grade.isEmpty()) {
                predicates.add(cb.equal(root.get("grade"), grade));
            }
            if (month != null) {
                predicates.add(cb.equal(root.get("month"), month));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }
            if (subject != null && !subject.isEmpty()) {
                predicates.add(cb.like(root.get("subject"), "%" + subject + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Fee> paidFees = feeRepository.findAll(feeSpec);

        // 3. Merge / Generate Virtual Fees
        List<FeeDto> result = new ArrayList<>();

        for (Student s : students) {
            String[] studentSubjects = s.getSubjects() != null ? s.getSubjects().split(",") : new String[0];
            
            for (String sub : studentSubjects) {
                String trimmedSub = sub.trim();
                
                // Subject filter
                if (subject != null && !subject.isEmpty() && !trimmedSub.toLowerCase().contains(subject.toLowerCase())) {
                    continue;
                }

                // Check if PAID fee exists
                Fee paidFee = paidFees.stream()
                        .filter(f -> f.getStudent().getId().equals(s.getId()) 
                                && f.getSubject().equalsIgnoreCase(trimmedSub)
                                && (month == null || f.getMonth().equals(month)) 
                                && (year == null || f.getYear().equals(year)))
                        .findFirst()
                        .orElse(null);

                if (paidFee != null) {
                     // PAID Record exists.
                     // Filter by requested status
                     if (status == null || status.isEmpty() || status.equalsIgnoreCase("PAID")) {
                         result.add(mapToDto(paidFee));
                     }
                } else {
                    // No PAID record. 
                    // This implies "PENDING".
                    if (status == null || status.isEmpty() || "PENDING".equalsIgnoreCase(status)) {
                        FeeDto virtualFee = new FeeDto();
                        virtualFee.setStudentId(s.getId());
                        virtualFee.setStudentName(s.getFullName());
                        virtualFee.setSubject(trimmedSub);
                        virtualFee.setGrade(s.getGrade());
                        virtualFee.setMonth(month != null ? month : java.time.LocalDate.now().getMonthValue());
                        virtualFee.setYear(year != null ? year : java.time.LocalDate.now().getYear());
                        virtualFee.setStatus("PENDING");
                        virtualFee.setAmount(java.math.BigDecimal.ZERO);
                        // id is null
                        result.add(virtualFee);
                    }
                }
            }
        }

        return result;
    }

    public List<FeeDto> getMyFees() {
        // 0. Get Logged-in Student
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.tuition.backend.Entity.User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Student currentStudent = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Student profile not found for current user"));

        // 1. Fetch ALL PAID Fees for this student
        List<Fee> paidFees = feeRepository.findByStudent(currentStudent);
        
        // 2. Determine subjects
        String[] subjects = currentStudent.getSubjects() != null ? currentStudent.getSubjects().split(",") : new String[0];
        
        List<FeeDto> result = new ArrayList<>();
        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        // 3. Generate Fee Records (Paid + Pending) for the current year
        // Strategy: Iterate through months (1 to 12) for each subject
        // If a paid record exists, use it. If not, create a virtual pending record (if month <= currentMonth + 1 ?)
        // Use a simple logic: Show all months for the current year. PENDING if not paid.
        
        for (String subject : subjects) {
            String trimmedSubject = subject.trim();
            if (trimmedSubject.isEmpty()) continue;

            for (int month = 1; month <= 12; month++) {
                // Check if paid fee exists for this subject, month, year
                final int m = month;
                Fee paidFee = paidFees.stream()
                        .filter(f -> f.getSubject().equalsIgnoreCase(trimmedSubject)
                                && f.getMonth() == m
                                && f.getYear() == currentYear)
                        .findFirst()
                        .orElse(null);

                if (paidFee != null) {
                    result.add(mapToDto(paidFee));
                } else {
                    // Create Virtual Pending Fee
                    // Optionally, only show pending up to current month? 
                    // Let's show all months but mark future ones as "UPCOMING"? 
                    // Requirement says "amount to be paid", usually implies past/current due.
                    // Let's mark past months as PENDING (Overdue logic handled in frontend?), future as UPCOMING?
                    // For simplicity, let's call everything not paid as PENDING for now, or distinguish based on month.
                    
                    String status = "PENDING";
                    if (month > currentMonth) {
                         status = "UPCOMING";
                    }

                    FeeDto virtualFee = new FeeDto();
                    virtualFee.setStudentId(currentStudent.getId());
                    virtualFee.setStudentName(currentStudent.getFullName());
                    virtualFee.setSubject(trimmedSubject);
                    virtualFee.setGrade(currentStudent.getGrade());
                    virtualFee.setMonth(month);
                    virtualFee.setYear(currentYear);
                    virtualFee.setStatus(status);
                    virtualFee.setAmount(java.math.BigDecimal.ZERO); // Or fetch class fee amount if we had a setup for it
                    
                    result.add(virtualFee);
                }
            }
        }
        
        // Add any paid fees from previous years if desired? 
        // For now, let's include all historical PAID fees too, even if not in current year loop above?
        // The loop above covers current year. 
        // Let's just add any paid fees that are NOT in the current year to the result as well.
        for (Fee fee : paidFees) {
            if (fee.getYear() != currentYear) {
                result.add(mapToDto(fee));
            }
        }

        // Sort: Latest year, Latest month
        result.sort((a, b) -> {
            int yearCompare = b.getYear().compareTo(a.getYear());
            if (yearCompare != 0) return yearCompare;
            return b.getMonth().compareTo(a.getMonth());
        });

        return result;
    }

    private FeeDto mapToDto(Fee fee) {
        return new FeeDto(
                fee.getId(),
                fee.getStudent().getId(),
                fee.getStudent().getFullName(),
                fee.getSubject(),
                fee.getGrade(),
                fee.getAmount(),
                fee.getMonth(),
                fee.getYear(),
                fee.getDueDate(),
                fee.getStatus(),
                fee.getPaidOn(),
                fee.getPaymentMethod(),
                fee.getNotes()
        );
    }
}
