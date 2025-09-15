package com.mphoola.e_empuzitsi.annotation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueValidator implements ConstraintValidator<Unique, Object> {
    
    @Autowired
    private EntityManager entityManager;
    
    private Class<?> entityClass;
    private String fieldName;
    
    @Override
    public void initialize(Unique constraintAnnotation) {
        this.entityClass = constraintAnnotation.entity();
        this.fieldName = constraintAnnotation.field();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        String jpql = String.format("SELECT COUNT(e) FROM %s e WHERE e.%s = :value", 
                                   entityClass.getSimpleName(), fieldName);
        
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("value", value);
        
        Long count = query.getSingleResult();
        return count == 0;
    }
}