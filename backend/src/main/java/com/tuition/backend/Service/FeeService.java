package com.tuition.backend.Service;

import com.tuition.backend.Entity.Fee;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Repository.FeeRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.dto.FeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeService {

    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private StudentRepository studentRepository;

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

        // 2. Fetch Existing Fees (Ignore status filter here to check existence correctly)
        Specification<Fee> feeSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // We need fees for these students. We can filter by the same criteria as students or just check in memory.
            // But strict filtering here helps performance.
            // Note: Fee doesn't have "createdBy", but it links to Student.
            // Since we iterate students, we can filter existence in memory or fetch all potential fees.
            // Let's filter by the parameters provided.

            if (grade != null && !grade.isEmpty()) {
                predicates.add(cb.equal(root.get("grade"), grade));
            }
            if (month != null) {
                predicates.add(cb.equal(root.get("month"), month));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }
            // IMPORTANT: Do NOT filter by status here. We need to know if a record exists AT ALL.
            /* if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            } */
            if (subject != null && !subject.isEmpty()) {
                predicates.add(cb.like(root.get("subject"), "%" + subject + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Fee> existingFees = feeRepository.findAll(feeSpec);

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

                // Check if ANY fee exists for this specific slot
                Fee existing = existingFees.stream()
                        .filter(f -> f.getStudent().getId().equals(s.getId()) 
                                && f.getSubject().equalsIgnoreCase(trimmedSub)
                                && (month == null || f.getMonth().equals(month)) 
                                && (year == null || f.getYear().equals(year)))
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                     // Record exists.
                     // NOW apply the status filter to decide if we include it.
                     if (status == null || status.isEmpty() || existing.getStatus().equalsIgnoreCase(status)) {
                         result.add(mapToDto(existing));
                     }
                } else {
                    // No existing fee record.
                    // This implies it is "PENDING".
                    // Only include if we are looking for PENDING or ALL.
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
