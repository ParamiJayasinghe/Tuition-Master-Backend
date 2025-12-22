package com.tuition.backend.Controller;

import com.tuition.backend.Service.FeeService;
import com.tuition.backend.dto.FeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@CrossOrigin(origins = "http://localhost:5173") // Adjust origin if needed
public class FeeController {

    @Autowired
    private FeeService feeService;

    @PostMapping
    public ResponseEntity<FeeDto> createFee(@RequestBody FeeDto feeDto) {
        FeeDto createdFee = feeService.createFee(feeDto);
        return ResponseEntity.ok(createdFee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeeDto> updateFee(@PathVariable Long id, @RequestBody FeeDto feeDto) {
        FeeDto updatedFee = feeService.updateFee(id, feeDto);
        return ResponseEntity.ok(updatedFee);
    }

    @GetMapping
    public ResponseEntity<List<FeeDto>> getFees(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subject) {
        List<FeeDto> fees = feeService.getFees(grade, month, year, status, subject);
        return ResponseEntity.ok(fees);
    }
}
