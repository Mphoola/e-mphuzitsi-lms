package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.config.EmailProperties;
import com.mphoola.e_empuzitsi.mail.EmailTemplate;
import com.mphoola.e_empuzitsi.mail.notifications.PasswordResetEmail;
import com.mphoola.e_empuzitsi.mail.notifications.PasswordResetConfirmationEmail;
import com.mphoola.e_empuzitsi.mail.notifications.WelcomeEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    
    public EmailService(JavaMailSender mailSender, EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
    }
    
    /**
     * Send email using template system
     */
    @Async("emailTaskExecutor")
    public void sendEmail(EmailTemplate emailTemplate) {
        try {
            log.info("Sending email: {} to {}", emailTemplate.getTemplateName(), emailTemplate.getTo());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(
                emailTemplate.getFrom() != null ? emailTemplate.getFrom() : emailProperties.getFrom(), 
                emailProperties.getName()
            );
            helper.setTo(emailTemplate.getTo());
            helper.setReplyTo(
                emailTemplate.getReplyTo() != null ? emailTemplate.getReplyTo() : emailProperties.getReplyTo()
            );
            helper.setSubject(emailTemplate.getSubject());
            helper.setText(emailTemplate.getHtmlContent(), true);
            
            mailSender.send(message);
            
            log.info("Email sent successfully: {} to {}", emailTemplate.getTemplateName(), emailTemplate.getTo());
            
        } catch (MessagingException e) {
            log.error("Failed to send email {} to {}: {}", emailTemplate.getTemplateName(), emailTemplate.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email {} to {}: {}", emailTemplate.getTemplateName(), emailTemplate.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send password reset email
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String email, String resetToken) {
        String resetLink = emailProperties.getFrontendUrl() + "/auth/reset-password?token=" + resetToken;
        String userName = email.substring(0, email.indexOf('@')); // Extract name from email
        
        PasswordResetEmail emailTemplate = new PasswordResetEmail(email, userName, resetToken, resetLink);
        sendEmail(emailTemplate);
    }
    
    /**
     * Send welcome email
     */
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String email, String name) {
        WelcomeEmail emailTemplate = new WelcomeEmail(email, name, emailProperties.getFrontendUrl());
        sendEmail(emailTemplate);
    }
    
    /**
     * Send password reset confirmation email
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetConfirmationEmail(String email, String name) {
        String loginUrl = emailProperties.getFrontendUrl() + "/auth/login";
        PasswordResetConfirmationEmail emailTemplate = new PasswordResetConfirmationEmail(email, name, loginUrl);
        sendEmail(emailTemplate);
    }
}
