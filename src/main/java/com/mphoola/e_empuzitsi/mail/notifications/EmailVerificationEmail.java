package com.mphoola.e_empuzitsi.mail.notifications;

import com.mphoola.e_empuzitsi.mail.BaseEmailTemplate;

public class EmailVerificationEmail extends BaseEmailTemplate {
    
    private final String userName;
    private final String verificationToken;
    private final String verificationUrl;
    
    public EmailVerificationEmail(String to, String userName, String verificationToken, String verificationUrl) {
        super(to, "Verify Your Email Address - E-Empuzitsi");
        this.userName = userName;
        this.verificationToken = verificationToken;
        this.verificationUrl = verificationUrl;
    }
    
    @Override
    public String getHtmlContent() {
        String content = """
            <div style="text-align: center; margin: 20px 0;">
                <div style="font-size: 48px;">📧</div>
            </div>
            
            <h2>Hello %s!</h2>
            <p>Thank you for creating your E-Empuzitsi account! To complete your registration and start learning, please verify your email address.</p>
            
            <div class="info-box">
                <p><strong>🔐 Email Verification Required</strong></p>
                <p>Click the button below to verify your email address and activate your account.</p>
            </div>
            
            <p><strong>Verification Token:</strong></p>
            <div style="background-color: #f8f9fa; padding: 15px; font-family: monospace; border-radius: 4px; word-break: break-all; font-size: 14px; border: 1px solid #dee2e6;">
                %s
            </div>
            
            <div class="success-box">
                <p><strong>🚀 What's Next?</strong></p>
                <p>Once verified, you'll have full access to:</p>
                <ul>
                    <li>📚 Browse and enroll in courses</li>
                    <li>📝 Take quizzes and assessments</li>
                    <li>💬 Join discussions with other learners</li>
                    <li>📊 Track your learning progress</li>
                    <li>🎓 Earn certificates</li>
                </ul>
            </div>
            
            <div class="warning-box">
                <p><strong>⚠️ Important:</strong></p>
                <ul>
                    <li>This verification link will expire in <strong>24 hours</strong></li>
                    <li>You must verify your email before accessing the platform</li>
                    <li>If you didn't create this account, please ignore this email</li>
                </ul>
            </div>
            
            <p>If you're having trouble with the button, copy and paste this link into your browser:</p>
            <p style="word-break: break-all; color: #666; background-color: #f8f9fa; padding: 10px; border-radius: 4px; border: 1px solid #dee2e6;">
                %s
            </p>
            """.formatted(userName, verificationToken, verificationUrl);
            
        return buildEmailHtml(
            "Verify Your Email Address",
            content,
            "Verify Email Address",
            verificationUrl
        );
    }
    
    @Override
    public String getTemplateName() {
        return "email-verification";
    }
}