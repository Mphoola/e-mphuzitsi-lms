package com.mphoola.e_empuzitsi.mail.notifications;

import com.mphoola.e_empuzitsi.mail.BaseEmailTemplate;

public class WelcomeEmail extends BaseEmailTemplate {
    
    private final String userName;
    private final String platformUrl;
    
    public WelcomeEmail(String to, String userName, String platformUrl) {
        super(to, "Welcome to E-Empuzitsi - Your Learning Journey Begins!");
        this.userName = userName;
        this.platformUrl = platformUrl;
    }
    
    @Override
    public String getHtmlContent() {
        String content = """
            <div style="text-align: center; margin: 20px 0;">
                <div style="font-size: 48px;">🎉</div>
            </div>
            
            <h2>Hello %s!</h2>
            <p>Welcome to E-Empuzitsi! We're excited to have you join our learning community.</p>
            
            <div class="success-box">
                <p><strong>🚀 Your account is ready!</strong> You can now access all features of our platform.</p>
            </div>
            
            <p>Here's what you can do with your new account:</p>
            <ul>
                <li>📚 Browse and enroll in courses</li>
                <li>📝 Take interactive quizzes and assessments</li>
                <li>💬 Join discussions with fellow learners</li>
                <li>📊 Track your learning progress and achievements</li>
                <li>🎓 Earn certificates upon course completion</li>
            </ul>
            
            <div class="info-box">
                <p><strong>💡 Getting Started Tips:</strong></p>
                <ul>
                    <li>Complete your profile to get personalized recommendations</li>
                    <li>Explore our course catalog to find subjects that interest you</li>
                    <li>Join study groups to connect with other learners</li>
                    <li>Set up your learning schedule for consistent progress</li>
                </ul>
            </div>
            
            <p>Ready to start your learning journey? Click the button below to explore our platform!</p>
            """.formatted(userName);
            
        return buildEmailHtml(
            "Welcome to E-Empuzitsi!",
            content,
            "Start Learning",
            platformUrl
        );
    }
    
    @Override
    public String getTemplateName() {
        return "welcome";
    }
}