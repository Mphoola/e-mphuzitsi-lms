package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.config.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailService
 * Tests email sending functionality for password reset and welcome emails
 */
@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        lenient().when(emailProperties.getFrom()).thenReturn("noreply@test.com");
        lenient().when(emailProperties.getName()).thenReturn("Test LMS");
        lenient().when(emailProperties.getReplyTo()).thenReturn("support@test.com");
        lenient().when(emailProperties.getFrontendUrl()).thenReturn("https://test.com");
    }

    @Test
    void sendPasswordResetEmail_WithValidData_ShouldSendEmailSuccessfully() throws MessagingException {
        // Given
        String email = "user@example.com";
        String resetToken = "test-reset-token";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendPasswordResetEmail(email, resetToken);

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        
        // Verify email properties were accessed (getReplyTo called twice - in method and in HTML template)
        verify(emailProperties).getFrom();
        verify(emailProperties, atLeast(1)).getName();
        verify(emailProperties, atLeast(2)).getReplyTo(); // Called twice - once in method, once in template
        verify(emailProperties).getFrontendUrl();
    }

    @Test
    void sendPasswordResetEmail_WithGenericException_ShouldThrowRuntimeException() {
        // Given
        String email = "user@example.com";
        String resetToken = "test-reset-token";
        
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server down"));

        // When/Then
        assertThatThrownBy(() -> emailService.sendPasswordResetEmail(email, resetToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send email")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeEmail_WithValidData_ShouldSendEmailSuccessfully() throws MessagingException {
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendWelcomeEmail(email, name);

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        
        // Verify email properties were accessed (getReplyTo called twice - in method and in HTML template)
        verify(emailProperties).getFrom();
        verify(emailProperties, atLeast(1)).getName();
        verify(emailProperties, atLeast(2)).getReplyTo(); // Called twice - once in method, once in template
        verify(emailProperties).getFrontendUrl();
    }

    @Test
    void sendWelcomeEmail_WithException_ShouldNotThrowException() {
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server down"));

        // When/Then - Should not throw exception (welcome email is not critical)
        emailService.sendWelcomeEmail(email, name);

        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldUseCorrectEmailTemplate() throws MessagingException {
        // Given
        String email = "user@example.com";
        String resetToken = "test-reset-token";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendPasswordResetEmail(email, resetToken);

        // Then - Verify the HTML template building was invoked
        verify(mailSender).createMimeMessage();
        verify(emailProperties).getFrontendUrl(); // Used in reset link
        verify(emailProperties, atLeast(1)).getName(); // Used multiple times in template
        verify(emailProperties, atLeast(2)).getReplyTo(); // Used twice - method and template
    }

    @Test
    void sendWelcomeEmail_ShouldUseCorrectEmailTemplate() throws MessagingException {
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        emailService.sendWelcomeEmail(email, name);

        // Then - Verify the HTML template building was invoked
        verify(mailSender).createMimeMessage();
        verify(emailProperties).getFrontendUrl(); // Used in welcome template
        verify(emailProperties, atLeast(1)).getName(); // Used multiple times in template
        verify(emailProperties, atLeast(2)).getReplyTo(); // Used twice - method and template
    }

    @Test
    void emailService_ShouldHaveCorrectDependencies() {
        // Then - Verify that the service has the correct dependencies
        assertThat(emailService).isNotNull();
        
        // This test ensures the service can be instantiated with its dependencies
        // The constructor injection is tested implicitly by the successful creation
        // of the service instance in the test setup
    }
}
