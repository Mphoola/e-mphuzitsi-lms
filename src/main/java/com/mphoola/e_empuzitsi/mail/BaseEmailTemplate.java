package com.mphoola.e_empuzitsi.mail;

public abstract class BaseEmailTemplate implements EmailTemplate {
    
    protected final String to;
    protected final String subject;
    
    public BaseEmailTemplate(String to, String subject) {
        this.to = to;
        this.subject = subject;
    }
    
    @Override
    public String getTo() {
        return to;
    }
    
    @Override
    public String getSubject() {
        return subject;
    }
    
    protected String buildEmailHtml(String title, String content, String buttonText, String buttonUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0; 
                        padding: 0; 
                        background-color: #f4f4f4; 
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background-color: #ffffff; 
                        border-radius: 10px; 
                        overflow: hidden; 
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); 
                    }
                    .header { 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                    }
                    .header h1 { 
                        margin: 0; 
                        font-size: 28px; 
                        font-weight: 300; 
                    }
                    .content { 
                        padding: 40px 30px; 
                    }
                    .button { 
                        display: inline-block; 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white !important; 
                        padding: 15px 30px; 
                        text-decoration: none; 
                        border-radius: 25px; 
                        margin: 20px 0; 
                        font-weight: 600; 
                        text-align: center; 
                        transition: transform 0.2s ease; 
                    }
                    .button:hover { 
                        transform: translateY(-2px); 
                    }
                    .footer { 
                        background-color: #f8f9fa; 
                        text-align: center; 
                        padding: 20px; 
                        color: #666; 
                        font-size: 14px; 
                        border-top: 1px solid #e9ecef; 
                    }
                    .info-box { 
                        background-color: #e3f2fd; 
                        border-left: 4px solid #2196f3; 
                        padding: 15px; 
                        margin: 20px 0; 
                        border-radius: 4px; 
                    }
                    .success-box { 
                        background-color: #e8f5e8; 
                        border-left: 4px solid #4caf50; 
                        padding: 15px; 
                        margin: 20px 0; 
                        border-radius: 4px; 
                    }
                    .warning-box { 
                        background-color: #fff3e0; 
                        border-left: 4px solid #ff9800; 
                        padding: 15px; 
                        margin: 20px 0; 
                        border-radius: 4px; 
                    }
                    @media only screen and (max-width: 600px) {
                        .container { margin: 10px; }
                        .content { padding: 20px; }
                        .header { padding: 20px; }
                        .header h1 { font-size: 24px; }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        %s
                        %s
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>The E-Empuzitsi Team</p>
                        <p>This is an automated message, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                title, 
                title, 
                content,
                buttonText != null && buttonUrl != null ? 
                    "<p style=\"text-align: center;\"><a href=\"" + buttonUrl + "\" class=\"button\">" + buttonText + "</a></p>" : ""
            );
    }
    
    protected String buildSimpleContent(String greeting, String message) {
        return """
            <h2>%s</h2>
            <p>%s</p>
            """.formatted(greeting, message);
    }
}