package com.mphoola.e_empuzitsi.interceptor;

import com.mphoola.e_empuzitsi.security.AllowUnverifiedEmail;
import com.mphoola.e_empuzitsi.service.UserService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class EmailVerificationInterceptor implements HandlerInterceptor {
    
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    public EmailVerificationInterceptor(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Check if endpoint is explicitly marked to allow unverified users
        AllowUnverifiedEmail methodAnnotation = handlerMethod.getMethodAnnotation(AllowUnverifiedEmail.class);
        AllowUnverifiedEmail classAnnotation = handlerMethod.getBeanType().getAnnotation(AllowUnverifiedEmail.class);
        
        // If either method or class has @AllowUnverifiedEmail, skip verification
        if (methodAnnotation != null || classAnnotation != null) {
            return true;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If not authenticated, let security handle it
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        
        String email = authentication.getName();
        
        try {
            // Secure by default: require email verification unless explicitly allowed
            if (!userService.isEmailVerified(email)) {
                sendEmailVerificationRequired(response, "Email verification required to access this resource");
                return false;
            }
        } catch (Exception e) {
            sendEmailVerificationRequired(response, "Unable to verify email status");
            return false;
        }
        
        return true;
    }
    
    private void sendEmailVerificationRequired(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        var errorResponse = ApiResponse.error(message, org.springframework.http.HttpStatus.FORBIDDEN);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse.getBody());
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}