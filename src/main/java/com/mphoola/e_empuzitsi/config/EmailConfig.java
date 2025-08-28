package com.mphoola.e_empuzitsi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties({EmailProperties.class})
public class EmailConfig {
    
    private static final Logger log = LoggerFactory.getLogger(EmailConfig.class);
    
    @Value("${spring.mail.host}")
    private String host;
    
    @Value("${spring.mail.port}")
    private int port;
    
    @Value("${spring.mail.username}")
    private String username;
    
    @Value("${spring.mail.password}")
    private String password;
    
    @Value("${spring.mail.protocol:smtp}")
    private String protocol;
    
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean auth;
    
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean starttlsEnable;
    
    @Value("${spring.mail.properties.mail.smtp.starttls.required:false}")
    private boolean starttlsRequired;
    
    @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}")
    private boolean sslEnable;
    
    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:10000}")
    private String connectionTimeout;
    
    @Value("${spring.mail.properties.mail.smtp.timeout:10000}")
    private String timeout;
    
    @Value("${spring.mail.properties.mail.smtp.writetimeout:10000}")
    private String writeTimeout;
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // Basic configuration
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol(protocol);
        mailSender.setDefaultEncoding("UTF-8");
        
        // Configure properties
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", String.valueOf(auth));
        properties.setProperty("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));
        properties.setProperty("mail.smtp.starttls.required", String.valueOf(starttlsRequired));
        properties.setProperty("mail.smtp.ssl.enable", String.valueOf(sslEnable));
        properties.setProperty("mail.smtp.connectiontimeout", connectionTimeout);
        properties.setProperty("mail.smtp.timeout", timeout);
        properties.setProperty("mail.smtp.writetimeout", writeTimeout);
        
        mailSender.setJavaMailProperties(properties);
        
        // Log configuration (without sensitive data)
        log.info("Email configured - Host: {}, Port: {}, Protocol: {}, Auth: {}", 
                 host, port, protocol, auth);
        
        return mailSender;
    }
}
