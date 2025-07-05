package com.mrdotxin.nexusmind.ai.tool.component;

import com.mrdotxin.nexusmind.config.EmailConfig;
import com.sun.mail.util.MailSSLSocketFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class MailSendTool {
    private final EmailConfig emailConfig;

    public MailSendTool(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    @Tool(description = "Use this to send emails via SMTP protocol. Supports sending to single recipient.")
    public String sendEmail(
            @ToolParam(description = "Recipient email address") String to,
            @ToolParam(description = "Email subject") String subject,
            @ToolParam(description = "Email content") String content
    ) {
        try {
            Properties prop = getProperties();

            // 创建会话
            Session session = Session.getInstance(prop);

            // 创建邮件
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getFrom()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(content);

            // 发送邮件
            Transport transport = session.getTransport();
            transport.connect(emailConfig.getSmtpHost(),
                            emailConfig.getFrom(),
                            emailConfig.getPassword());
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            return "Email sent successfully to " + to;
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }

    private @NotNull Properties getProperties() throws GeneralSecurityException {
        Properties prop = new Properties();
        prop.setProperty("mail.debug", "true");
        prop.setProperty("mail.host", emailConfig.getSmtpHost());
        prop.setProperty("mail.smtp.auth", "true");
        prop.setProperty("mail.transport.protocol", "smtp");

        // SSL加密配置
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        prop.put("mail.smtp.port", emailConfig.getSmtpPort());
        return prop;
    }

}
