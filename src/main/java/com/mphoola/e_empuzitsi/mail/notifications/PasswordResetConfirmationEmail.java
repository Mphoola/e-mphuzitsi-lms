package com.mphoola.e_empuzitsi.mail.notifications;

import com.mphoola.e_empuzitsi.mail.BaseEmailTemplate;

public class PasswordResetConfirmationEmail extends BaseEmailTemplate {
    
    private final String userName;
    private final String loginUrl;
    
    public PasswordResetConfirmationEmail(String to, String userName, String loginUrl) {
        super(to, "Password Reset Successful - E-Empuzitsi");
        this.userName = userName;
        this.loginUrl = loginUrl;
    }
    
    @Override
    public String getHtmlContent() {
        String content = """
            <div style="text-align: center; margin: 20px 0;">
                <div style="font-size: 48px; color: #4caf50;">🔐</div>
            </div>
            
            <h2>Hello %s!</h2>
            <p>Your password has been successfully reset for your E-Empuzitsi account.</p>
            
            <div class="success-box">
                <p><strong>🎉 Success!</strong> You can now log in with your new password.</p>
            </div>
            
            <p>Your account is secure and you can now access all features of our learning management system:</p>
            <ul>
                <li>📚 Access your course materials</li>
                <li>📝 Continue with quizzes and assignments</li>
                <li>💬 Participate in discussions</li>
                <li>📊 Track your learning progress</li>
            </ul>
            
            <div class="info-box">
                <p><strong>🛡️ Security Tips:</strong></p>
                <ul>
                    <li>Keep your new password secure and don't share it with anyone</li>
                    <li>Consider using a password manager for better security</li>
                    <li>If you didn't make this change, contact support immediately</li>
                </ul>
            </div>
            
            <p>If you have any questions or concerns, please don't hesitate to contact our support team.</p>
            """.formatted(userName);
            
        return buildEmailHtml(
            "Password Reset Successful",
            content,
            "Login Now",
            loginUrl
        );
    }
    
    @Override
    public String getTemplateName() {
        return "password-reset-confirmation";
    }
}