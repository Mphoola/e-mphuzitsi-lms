package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.subject.SubjectRequest;
import com.mphoola.e_empuzitsi.dto.subject.SubjectResponse;
import com.mphoola.e_empuzitsi.dto.subject.SubjectResponseSimple;
import com.mphoola.e_empuzitsi.entity.Subject;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectService {
    
    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }
    
    public SubjectResponse createSubject(SubjectRequest request) {
        Subject subject = Subject.builder()
                .name(request.getName())
                .build();
                
        Subject savedSubject = subjectRepository.save(subject);
        return mapToSubjectResponse(savedSubject);
    }
    
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        
        // Check if name already exists for a different subject
        if (subjectRepository.existsByName(request.getName()) && 
            !subject.getName().equals(request.getName())) {
            throw new ResourceConflictException("Subject already exists with name: " + request.getName());
        }
        
        subject.setName(request.getName());
        Subject updatedSubject = subjectRepository.save(subject);
        return mapToSubjectResponse(updatedSubject);
    }
    
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        
        // Check if subject has associated lesson components or students
        Long lessonCount = subjectRepository.countLessonsBySubjectId(id);
        Long studentCount = subjectRepository.countStudentsBySubjectId(id);
        
        if (lessonCount > 0 || studentCount > 0) {
            throw new ResourceConflictException("Cannot delete subject. It has " + lessonCount + 
                " lesson(s) and " + studentCount + " student(s) associated with it.");
        }
        
        subjectRepository.delete(subject);
    }
    
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return mapToSubjectResponse(subject);
    }
    
    @Transactional(readOnly = true)
    public List<SubjectResponseSimple> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::mapToSubjectResponseSimple)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectByName(String name) {
        Subject subject = subjectRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with name: " + name));
        return mapToSubjectResponse(subject);
    }
    
    private SubjectResponse mapToSubjectResponse(Subject subject) {
        Long lessonCount = subjectRepository.countLessonsBySubjectId(subject.getId());
        Long studentCount = subjectRepository.countStudentsBySubjectId(subject.getId());
        
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .lessonCount(lessonCount)
                .studentCount(studentCount)
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }
    
    private SubjectResponseSimple mapToSubjectResponseSimple(Subject subject) {
        Long lessonCount = subjectRepository.countLessonsBySubjectId(subject.getId());
        Long studentCount = subjectRepository.countStudentsBySubjectId(subject.getId());
        
        return SubjectResponseSimple.builder()
                .id(subject.getId())
                .name(subject.getName())
                .lessonCount(lessonCount)
                .studentCount(studentCount)
                .build();
    }
}