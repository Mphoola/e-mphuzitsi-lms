package com.mphoola.e_empuzitsi.mail;

public interface EmailTemplate {
    
    String getTo();
    String getSubject();
    String getHtmlContent();
    String getTemplateName();
    
    default String getFrom() {
        return null; // Will use default from EmailProperties
    }
    
    default String getReplyTo() {
        return null; // Will use default from EmailProperties
    }
}