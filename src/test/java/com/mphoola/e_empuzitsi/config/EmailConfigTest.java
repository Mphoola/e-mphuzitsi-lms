package com.mphoola.e_empuzitsi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {EmailConfig.class, EmailProperties.class})
@TestPropertySource(properties = {
    "spring.mail.host=test.smtp.server.com",
    "spring.mail.port=587",
    "spring.mail.username=testuser",
    "spring.mail.password=testpass",
    "spring.mail.protocol=smtp",
    "spring.mail.properties.mail.smtp.auth=true",
    "spring.mail.properties.mail.smtp.starttls.enable=true",
    "spring.mail.properties.mail.smtp.starttls.required=false",
    "spring.mail.properties.mail.smtp.ssl.enable=false",
    "spring.mail.properties.mail.smtp.connectiontimeout=5000",
    "spring.mail.properties.mail.smtp.timeout=5000",
    "spring.mail.properties.mail.smtp.writetimeout=5000",
    "app.email.from=test@example.com",
    "app.email.name=Test App",
    "app.email.reply-to=support@example.com",
    "app.email.frontend-url=http://localhost:3000"
})
@DisplayName("EmailConfig Tests")
class EmailConfigTest {

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailProperties emailProperties;

    @Test
    @DisplayName("Should create EmailConfig bean successfully")
    void should_create_email_config_bean() {
        // Then
        assertNotNull(emailConfig);
    }

    @Test
    @DisplayName("Should create JavaMailSender bean with correct configuration")
    void should_create_java_mail_sender_bean() {
        // Then
        assertNotNull(javaMailSender);
        assertThat(javaMailSender).isInstanceOf(JavaMailSenderImpl.class);
        
        JavaMailSenderImpl mailSender = (JavaMailSenderImpl) javaMailSender;
        
        // Test basic configuration
        assertThat(mailSender.getHost()).isEqualTo("test.smtp.server.com");
        assertThat(mailSender.getPort()).isEqualTo(587);
        assertThat(mailSender.getUsername()).isEqualTo("testuser");
        assertThat(mailSender.getPassword()).isEqualTo("testpass");
        assertThat(mailSender.getProtocol()).isEqualTo("smtp");
        assertThat(mailSender.getDefaultEncoding()).isEqualTo("UTF-8");
    }

    @Test
    @DisplayName("Should configure JavaMailSender properties correctly")
    void should_configure_java_mail_sender_properties() {
        // Given
        JavaMailSenderImpl mailSender = (JavaMailSenderImpl) javaMailSender;
        
        // Then
        assertThat(mailSender.getJavaMailProperties()).isNotNull();
        
        // Test SMTP properties
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtp.starttls.required")).isEqualTo("false");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtp.ssl.enable")).isEqualTo("false");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtp.connectiontimeout")).isEqualTo("5000");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtp.timeout")).isEqualTo("5000");
        assertThat(mailSender.getJavaMailProperties().getProperty("mail.smtp.writetimeout")).isEqualTo("5000");
    }

    @Test
    @DisplayName("Should create EmailProperties bean with correct values")
    void should_create_email_properties_bean() {
        // Then
        assertNotNull(emailProperties);
        assertThat(emailProperties.getFrom()).isEqualTo("test@example.com");
        assertThat(emailProperties.getName()).isEqualTo("Test App");
        assertThat(emailProperties.getReplyTo()).isEqualTo("support@example.com");
        assertThat(emailProperties.getFrontendUrl()).isEqualTo("http://localhost:3000");
    }

    @Test
    @DisplayName("Should use default values for EmailProperties when not configured")
    void should_use_default_values_for_email_properties() {
        // Given
        EmailProperties defaultProperties = new EmailProperties();
        
        // Then
        assertThat(defaultProperties.getFrom()).isEqualTo("noreply@e-empuzitsi.com");
        assertThat(defaultProperties.getName()).isEqualTo("E-Empuzitsi LMS");
        assertThat(defaultProperties.getReplyTo()).isEqualTo("support@e-empuzitsi.com");
        assertThat(defaultProperties.getFrontendUrl()).isEqualTo("http://localhost:3000");
    }

    @Test
    @DisplayName("Should allow EmailProperties to be modified")
    void should_allow_email_properties_to_be_modified() {
        // Given
        EmailProperties properties = new EmailProperties();
        
        // When
        properties.setFrom("custom@example.com");
        properties.setName("Custom App");
        properties.setReplyTo("help@example.com");
        properties.setFrontendUrl("https://example.com");
        
        // Then
        assertThat(properties.getFrom()).isEqualTo("custom@example.com");
        assertThat(properties.getName()).isEqualTo("Custom App");
        assertThat(properties.getReplyTo()).isEqualTo("help@example.com");
        assertThat(properties.getFrontendUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("Should create JavaMailSender with proper SSL configuration")
    void should_create_java_mail_sender_with_ssl_configuration() {
        // Given
        EmailConfig config = new EmailConfig();
        
        // Use reflection to set private fields for testing
        setFieldValue(config, "host", "ssl.server.com");
        setFieldValue(config, "port", 465);
        setFieldValue(config, "username", "ssluser");
        setFieldValue(config, "password", "sslpass");
        setFieldValue(config, "protocol", "smtp");
        setFieldValue(config, "auth", true);
        setFieldValue(config, "starttlsEnable", false);
        setFieldValue(config, "starttlsRequired", false);
        setFieldValue(config, "sslEnable", true);
        setFieldValue(config, "connectionTimeout", "10000");
        setFieldValue(config, "timeout", "10000");
        setFieldValue(config, "writeTimeout", "10000");
        
        // When
        JavaMailSender mailSender = config.javaMailSender();
        
        // Then
        assertNotNull(mailSender);
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
        assertThat(impl.getHost()).isEqualTo("ssl.server.com");
        assertThat(impl.getPort()).isEqualTo(465);
        assertThat(impl.getJavaMailProperties().getProperty("mail.smtp.ssl.enable")).isEqualTo("true");
    }

    private void setFieldValue(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
