package com.mphoola.e_empuzitsi.mail.notifications;

import com.mphoola.e_empuzitsi.mail.BaseEmailTemplate;

public class QuizCompletionEmail extends BaseEmailTemplate {
    
    private final String userName;
    private final String quizName;
    private final int score;
    private final int totalPoints;
    private final String resultsUrl;
    
    public QuizCompletionEmail(String to, String userName, String quizName, int score, int totalPoints, String resultsUrl) {
        super(to, "Quiz Results - " + quizName);
        this.userName = userName;
        this.quizName = quizName;
        this.score = score;
        this.totalPoints = totalPoints;
        this.resultsUrl = resultsUrl;
    }
    
    @Override
    public String getHtmlContent() {
        double percentage = (double) score / totalPoints * 100;
        String performanceLevel = percentage >= 80 ? "Excellent" : percentage >= 60 ? "Good" : "Needs Improvement";
        String boxClass = percentage >= 80 ? "success-box" : percentage >= 60 ? "info-box" : "warning-box";
        
        String content = """
            <h2>Hello %s!</h2>
            <p>You have completed the quiz: <strong>%s</strong></p>
            
            <div class="%s">
                <p><strong>📊 Your Results:</strong></p>
                <p style="font-size: 24px; margin: 10px 0;"><strong>%d / %d</strong> (%.1f%%)</p>
                <p><strong>Performance Level: %s</strong></p>
            </div>
            
            %s
            
            <div class="info-box">
                <p><strong>📚 Next Steps:</strong></p>
                <ul>
                    <li>Review your detailed results</li>
                    <li>Identify areas for improvement</li>
                    <li>Continue with the next lesson</li>
                    <li>Practice with additional exercises</li>
                </ul>
            </div>
            """.formatted(
                userName, 
                quizName, 
                boxClass,
                score, 
                totalPoints, 
                percentage, 
                performanceLevel,
                percentage >= 80 ? 
                    "<p>🎉 Congratulations on your excellent performance! Keep up the great work.</p>" :
                percentage >= 60 ?
                    "<p>👍 Good job! You're making solid progress. Review the areas where you can improve.</p>" :
                    "<p>💪 Don't worry, learning takes time. Review the material and try again when you're ready.</p>"
            );
            
        return buildEmailHtml(
            "Quiz Results",
            content,
            "View Detailed Results",
            resultsUrl
        );
    }
    
    @Override
    public String getTemplateName() {
        return "quiz-completion";
    }
}