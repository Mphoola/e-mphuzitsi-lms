package com.mphoola.e_empuzitsi.mail.notifications;

import com.mphoola.e_empuzitsi.mail.BaseEmailTemplate;

public class PasswordResetEmail extends BaseEmailTemplate {
    
    private final String resetToken;
    private final String resetLink;
    private final String userName;
    
    public PasswordResetEmail(String to, String userName, String resetToken, String resetLink) {
        super(to, "Password Reset Request - E-Empuzitsi");
        this.resetToken = resetToken;
        this.resetLink = resetLink;
        this.userName = userName;
    }
    
    @Override
    public String getHtmlContent() {
        String content = """
            <h2>Hello %s!</h2>
            <p>We received a request to reset your password for your E-Empuzitsi account.</p>
            
            <div class="info-box">
                <p><strong>🔐 Password Reset Instructions:</strong></p>
                <ul>
                    <li>Click the button below to reset your password</li>
                    <li>This link will expire in <strong>60 minutes</strong></li>
                    <li>If you didn't request this, please ignore this email</li>
                </ul>
            </div>
            
            <p><strong>Reset Token:</strong></p>
            <div style="background-color: #f8f9fa; padding: 15px; font-family: monospace; border-radius: 4px; word-break: break-all; font-size: 14px; border: 1px solid #dee2e6;">
                %s
            </div>
            
            <div class="warning-box">
                <p><strong>⚠️ Security Notice:</strong></p>
                <p>Never share this token with anyone. Our team will never ask for your password or reset token.</p>
            </div>
            
            <p>If you're having trouble with the button, copy and paste this link into your browser:</p>
            <p style="word-break: break-all; color: #666; background-color: #f8f9fa; padding: 10px; border-radius: 4px; border: 1px solid #dee2e6;">
                %s
            </p>
            """.formatted(userName, resetToken, resetLink);
            
        return buildEmailHtml(
            "Password Reset Request",
            content,
            "Reset My Password",
            resetLink
        );
    }
    
    @Override
    public String getTemplateName() {
        return "password-reset";
    }
}