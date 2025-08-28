package com.mphoola.e_empuzitsi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
    
    private String from = "noreply@e-empuzitsi.com";
    private String name = "E-Empuzitsi LMS";
    private String replyTo = "support@e-empuzitsi.com";
    private String frontendUrl = "http://localhost:3000";
    
    // Getters and Setters
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getReplyTo() {
        return replyTo;
    }
    
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
    
    public String getFrontendUrl() {
        return frontendUrl;
    }
    
    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }
}
