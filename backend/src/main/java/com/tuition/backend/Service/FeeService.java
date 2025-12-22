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

    public List<FeeDto> getFees(String grade, Integer month, Integer year, String status, String subject) {
        Specification<Fee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (grade != null && !grade.isEmpty()) {
                predicates.add(cb.equal(root.get("grade"), grade));
            }
            if (month != null) {
                predicates.add(cb.equal(root.get("month"), month));
            }
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (subject != null && !subject.isEmpty()) {
                // Direct filter on subject column
                predicates.add(cb.like(root.get("subject"), "%" + subject + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return feeRepository.findAll(spec).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
