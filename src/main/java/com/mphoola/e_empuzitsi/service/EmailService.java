package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.config.EmailProperties;
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
     * Send password reset email
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailProperties.getFrom(), emailProperties.getName());
            helper.setTo(email);
            helper.setReplyTo(emailProperties.getReplyTo());
            helper.setSubject("Password Reset Request - " + emailProperties.getName());
            
            String resetLink = emailProperties.getFrontendUrl() + "/auth/reset-password?token=" + resetToken;
            
            String htmlContent = buildPasswordResetEmailHtml(resetToken, resetLink);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send welcome email (example of additional email functionality)
     */
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String email, String name) {
        try {
            log.info("Sending welcome email to: {}", email);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailProperties.getFrom(), emailProperties.getName());
            helper.setTo(email);
            helper.setReplyTo(emailProperties.getReplyTo());
            helper.setSubject("Welcome to " + emailProperties.getName() + "!");
            
            String htmlContent = buildWelcomeEmailHtml(name);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("Welcome email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
            // Don't throw exception for welcome email - it's not critical
        }
    }
    
    /**
     * Send password reset confirmation email
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetConfirmationEmail(String email, String name) {
        try {
            log.info("Sending password reset confirmation email to: {}", email);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailProperties.getFrom(), emailProperties.getName());
            helper.setTo(email);
            helper.setReplyTo(emailProperties.getReplyTo());
            helper.setSubject("Password Reset Successful - " + emailProperties.getName());
            
            String htmlContent = buildPasswordResetConfirmationEmailHtml(name);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("Password reset confirmation email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to {}: {}", email, e.getMessage());
            // Don't throw exception - it's not critical if confirmation email fails
        }
    }
    
    /**
     * Build HTML content for password reset email
     */
    private String buildPasswordResetEmailHtml(String resetToken, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { 
                        display: inline-block; 
                        background-color: #3498db; 
                        color: white; 
                        padding: 12px 24px; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .button:hover { background-color: #2980b9; }
                    .token { 
                        background-color: #ecf0f1; 
                        padding: 15px; 
                        font-family: monospace; 
                        border-left: 4px solid #3498db; 
                        margin: 15px 0;
                        word-break: break-all;
                        font-size: 14px;
                    }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    @media only screen and (max-width: 600px) {
                        .container { padding: 10px; }
                        .content { padding: 20px; }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Password Reset Request</h1>
                        <p>%s</p>
                    </div>
                    
                    <div class="content">
                        <h2>Hello!</h2>
                        <p>We received a request to reset your password for your %s account.</p>
                        
                        <p>If you made this request, you can reset your password by clicking the button below:</p>
                        
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset My Password</a>
                        </p>
                        
                        <p><strong>Or use this reset token manually:</strong></p>
                        <div class="token">%s</div>
                        
                        <div class="warning">
                            <p><strong>⚠️ Important Security Information:</strong></p>
                            <ul>
                                <li>This reset link will expire in <strong>60 minutes</strong></li>
                                <li>If you didn't request this reset, please ignore this email</li>
                                <li>Your password won't change until you create a new one</li>
                                <li>Never share this token with anyone</li>
                            </ul>
                        </div>
                        
                        <p>If you're having trouble clicking the button, copy and paste the following URL into your browser:</p>
                        <p style="word-break: break-all; color: #666; background-color: #f8f9fa; padding: 10px; border-radius: 4px;">%s</p>
                    </div>
                    
                    <div class="footer">
                        <p>This email was sent by %s</p>
                        <p>If you have any questions, please contact our support team at %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                emailProperties.getName(),
                emailProperties.getName(), 
                emailProperties.getName(),
                resetLink, 
                resetToken, 
                resetLink,
                emailProperties.getName(),
                emailProperties.getReplyTo()
            );
    }
    
    /**
     * Build HTML content for welcome email
     */
    private String buildWelcomeEmailHtml(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Welcome to %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #27ae60; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { 
                        display: inline-block; 
                        background-color: #27ae60; 
                        color: white; 
                        padding: 12px 24px; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to %s!</h1>
                    </div>
                    
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>Welcome to %s! We're excited to have you on board.</p>
                        
                        <p>You can now access all the features of our learning management system:</p>
                        <ul>
                            <li>📚 Access course materials</li>
                            <li>📝 Take quizzes and assignments</li>
                            <li>💬 Participate in discussions</li>
                            <li>📊 Track your progress</li>
                        </ul>
                        
                        <p style="text-align: center;">
                            <a href="%s" class="button">Start Learning</a>
                        </p>
                        
                        <p>If you have any questions, don't hesitate to reach out to our support team.</p>
                    </div>
                    
                    <div class="footer">
                        <p>Best regards,<br>The %s Team</p>
                        <p>Support: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                emailProperties.getName(),
                emailProperties.getName(),
                name,
                emailProperties.getName(),
                emailProperties.getFrontendUrl(),
                emailProperties.getName(),
                emailProperties.getReplyTo()
            );
    }
    
    /**
     * Build HTML content for password reset confirmation email
     */
    private String buildPasswordResetConfirmationEmailHtml(String name) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset Successful - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #27ae60; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { 
                        display: inline-block; 
                        background-color: #27ae60; 
                        color: white; 
                        padding: 12px 24px; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .success-icon { 
                        font-size: 48px; 
                        color: #27ae60; 
                        text-align: center; 
                        margin: 20px 0; 
                    }
                    .info-box { 
                        background-color: #d5edda; 
                        border: 1px solid #c3e6cb; 
                        padding: 15px; 
                        border-radius: 5px; 
                        margin: 15px 0; 
                    }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    @media only screen and (max-width: 600px) {
                        .container { padding: 10px; }
                        .content { padding: 20px; }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Password Reset Successful</h1>
                        <p>%s</p>
                    </div>
                    
                    <div class="content">
                        <div class="success-icon">🔐</div>
                        
                        <h2>Hello %s!</h2>
                        <p>Your password has been successfully reset for your %s account.</p>
                        
                        <div class="info-box">
                            <p><strong>🎉 Success!</strong> You can now log in with your new password.</p>
                        </div>
                        
                        <p>Your account is secure and you can now access all features of our learning management system:</p>
                        <ul>
                            <li>📚 Access your course materials</li>
                            <li>📝 Continue with quizzes and assignments</li>
                            <li>💬 Participate in discussions</li>
                            <li>📊 Track your learning progress</li>
                        </ul>
                        
                        <p style="text-align: center;">
                            <a href="%s/auth/login" class="button">Login Now</a>
                        </p>
                        
                        <div class="info-box">
                            <p><strong>🛡️ Security Tips:</strong></p>
                            <ul>
                                <li>Keep your new password secure and don't share it with anyone</li>
                                <li>Consider using a password manager for better security</li>
                                <li>If you didn't make this change, contact support immediately</li>
                            </ul>
                        </div>
                        
                        <p>If you have any questions or concerns, please don't hesitate to contact our support team.</p>
                    </div>
                    
                    <div class="footer">
                        <p>Best regards,<br>The %s Team</p>
                        <p>Support: %s | <a href="%s">Visit our platform</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                emailProperties.getName(),
                emailProperties.getName(),
                name,
                emailProperties.getName(),
                emailProperties.getFrontendUrl(),
                emailProperties.getName(),
                emailProperties.getReplyTo(),
                emailProperties.getFrontendUrl()
            );
    }
}
