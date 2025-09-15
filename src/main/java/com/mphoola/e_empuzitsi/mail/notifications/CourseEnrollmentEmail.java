package com.mphoola.e_empuzitsi.mail.notifications;

import com.mphoola.e_empuzitsi.mail.BaseEmailTemplate;

public class CourseEnrollmentEmail extends BaseEmailTemplate {
    
    private final String userName;
    private final String courseName;
    private final String courseUrl;
    
    public CourseEnrollmentEmail(String to, String userName, String courseName, String courseUrl) {
        super(to, "Course Enrollment Confirmation - " + courseName);
        this.userName = userName;
        this.courseName = courseName;
        this.courseUrl = courseUrl;
    }
    
    @Override
    public String getHtmlContent() {
        String content = """
            <h2>Hello %s!</h2>
            <p>Congratulations! You have successfully enrolled in <strong>%s</strong>.</p>
            
            <div class="success-box">
                <p><strong>🎓 Enrollment Confirmed!</strong> You now have full access to all course materials.</p>
            </div>
            
            <p>What's next?</p>
            <ul>
                <li>📖 Access course materials and lectures</li>
                <li>📝 Complete assignments and quizzes</li>
                <li>💬 Join course discussions</li>
                <li>🏆 Earn your certificate upon completion</li>
            </ul>
            
            <div class="info-box">
                <p><strong>💡 Study Tips:</strong></p>
                <ul>
                    <li>Set a regular study schedule</li>
                    <li>Participate actively in discussions</li>
                    <li>Don't hesitate to ask questions</li>
                    <li>Track your progress regularly</li>
                </ul>
            </div>
            """.formatted(userName, courseName);
            
        return buildEmailHtml(
            "Course Enrollment Successful",
            content,
            "Access Course",
            courseUrl
        );
    }
    
    @Override
    public String getTemplateName() {
        return "course-enrollment";
    }
}