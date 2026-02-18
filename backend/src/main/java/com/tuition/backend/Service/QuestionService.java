package com.tuition.backend.Service;

import com.tuition.backend.Entity.Question;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.QuestionRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.Repository.TeacherRepository;
import com.tuition.backend.Repository.userRepository;
import com.tuition.backend.dto.QuestionDTO;
import com.tuition.backend.Entity.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tuition.backend.Service.NotificationService;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private userRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public QuestionDTO askQuestion(String username, Long teacherId, String text, String fileUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Student profile not found for user: " + username));
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found with ID: " + teacherId));

        Question question = new Question();
        question.setStudent(student);
        question.setTeacher(teacher);
        question.setQuestionText(text);
        // Handle empty string from frontend
        question.setQuestionFileUrl((fileUrl != null && !fileUrl.isEmpty()) ? fileUrl : null);
        question.setStatus("PENDING");

        Question savedQuestion = questionRepository.save(question);

        // Send notification to teacher
        notificationService.createNotification(
                teacher.getUser(),
                NotificationType.QUESTION_ASKED,
                "New Question from " + student.getFullName(),
                student.getFullName() + " has asked a new question: " + text
        );

        return mapToDTO(savedQuestion);
    }

    @Transactional
    public QuestionDTO answerQuestion(Long questionId, String text, String fileUrl) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));

        question.setAnswerText(text);
        question.setAnswerFileUrl((fileUrl != null && !fileUrl.isEmpty()) ? fileUrl : null);
        question.setAnsweredAt(LocalDateTime.now());
        question.setStatus("ANSWERED");

        Question savedQuestion = questionRepository.save(question);

        // Send notification to student
        notificationService.createNotification(
                question.getStudent().getUser(),
                NotificationType.ANSWER_RECEIVED,
                "New Answer from Teacher",
                question.getTeacher().getFullName() + " has answered your question: " + text
        );

        return mapToDTO(savedQuestion);
    }

    public List<QuestionDTO> getStudentQuestions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return questionRepository.findByStudent(student).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getTeacherQuestions(String username, String status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Teacher teacher = teacherRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<Question> questions;
        if (status != null && !status.isEmpty()) {
            questions = questionRepository.findByTeacherAndStatus(teacher, status);
        } else {
            questions = questionRepository.findByTeacher(teacher);
        }

        return questions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> getAvailableTeachers() {
        return teacherRepository.findAll().stream()
                .map(teacher -> {
                    QuestionDTO dto = new QuestionDTO();
                    dto.setTeacherId(teacher.getId());
                    dto.setTeacherName(teacher.getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private QuestionDTO mapToDTO(Question question) {
        if (question == null) return null;
        
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        
        if (question.getStudent() != null) {
            dto.setStudentId(question.getStudent().getId());
            dto.setStudentName(question.getStudent().getFullName());
        }
        
        if (question.getTeacher() != null) {
            dto.setTeacherId(question.getTeacher().getId());
            dto.setTeacherName(question.getTeacher().getFullName());
        }
        
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionFileUrl(question.getQuestionFileUrl());
        dto.setAnswerText(question.getAnswerText());
        dto.setAnswerFileUrl(question.getAnswerFileUrl());
        dto.setStatus(question.getStatus());
        dto.setAskedAt(question.getAskedAt());
        dto.setAnsweredAt(question.getAnsweredAt());
        
        return dto;
    }
}
