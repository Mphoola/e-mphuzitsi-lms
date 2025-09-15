package com.mphoola.e_empuzitsi.mail.notifications;

import com.mphoola.e_empuzitsi.mail.BaseEmailTemplate;

/**
 * Email template for sending user credentials to newly created users
 */
public class UserCredentialsEmail extends BaseEmailTemplate {
    
    private final String userName;
    private final String temporaryPassword;
    private final String frontendUrl;
    private final String accountType;
    
    public UserCredentialsEmail(String to, String userName, String temporaryPassword, String frontendUrl, String accountType) {
        super(to, "Welcome to E-Empuzitsi LMS - Your Account Details");
        this.userName = userName;
        this.temporaryPassword = temporaryPassword;
        this.frontendUrl = frontendUrl;
        this.accountType = accountType;
    }
    
    @Override
    public String getHtmlContent() {
        String content = String.format("""
            <div style="text-align: center; margin: 20px 0;">
                <div style="font-size: 48px;">🎓</div>
            </div>
            
            <h2>Hello %s! 👋</h2>
            <p>Your account has been successfully created in the E-Empuzitsi Learning Management System. Below are your login credentials:</p>
            
            <div class="info-box">
                <h3>🔐 Your Login Credentials</h3>
                <table style="width: 100%%; margin: 15px 0;">
                    <tr>
                        <td style="padding: 8px; font-weight: bold; color: #495057;">Email:</td>
                        <td style="padding: 8px; font-family: 'Courier New', monospace; background: #f8f9fa; border-radius: 4px;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold; color: #495057;">Temporary Password:</td>
                        <td style="padding: 8px; font-family: 'Courier New', monospace; background: #f8f9fa; border-radius: 4px;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold; color: #495057;">Account Type:</td>
                        <td style="padding: 8px; font-family: 'Courier New', monospace; background: #f8f9fa; border-radius: 4px;">%s</td>
                    </tr>
                </table>
            </div>
            
            <div class="warning-box">
                <p><strong>⚠️ Important Security Notice:</strong><br>
                This is a temporary password. For your security, please change it immediately after your first login.</p>
            </div>
            
            <h3>📚 What's Next?</h3>
            <ul>
                <li>✅ Log in to your account using the credentials above</li>
                <li>✅ Change your password in your profile settings</li>
                <li>✅ Complete your profile information</li>
                <li>✅ Start exploring your courses and materials</li>
            </ul>
            
            <p>If you have any questions or need assistance, please don't hesitate to contact our support team.</p>
            <p>Happy Learning! 🌟</p>
            """, userName, to, temporaryPassword, accountType);
        
        return buildEmailHtml(
            "Welcome to E-Empuzitsi LMS", 
            content, 
            "🚀 Login to Your Account", 
            frontendUrl
        );
    }
    
    @Override
    public String getTemplateName() {
        return "user-credentials";
    }
}