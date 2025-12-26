package com.subcycle.service;

import com.subcycle.config.AppProperties;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private AppProperties appProperties;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @PostConstruct
    public void init() {
        if (mailSender == null || fromEmail == null || fromEmail.isEmpty()) {
            logger.warn("=================================================================");
            logger.warn("郵件服務未配置！");
            logger.warn("請在 backend/.env 檔案中設定以下環境變數：");
            logger.warn("  MAIL_USERNAME=your-email@gmail.com");
            logger.warn("  MAIL_PASSWORD=your-app-password");
            logger.warn("詳細設定方式請參考 backend/.env.example 檔案");
            logger.warn("=================================================================");
        } else {
            logger.info("郵件服務已啟用，發件人：{}", fromEmail);
        }
    }

    /**
     * 發送簡單純文本郵件
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (mailSender == null || fromEmail == null || fromEmail.isEmpty()) {
            logger.warn("郵件服務未配置，無法發送郵件給 {} - 主題: {}", to, subject);
            logger.warn("請在 backend/.env 檔案中設定 MAIL_USERNAME 和 MAIL_PASSWORD");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("已發送郵件給 {} - 主題: {}", to, subject);
        } catch (Exception e) {
            logger.error("發送郵件失敗：{} - 收件人: {}, 主題: {}", e.getMessage(), to, subject);
        }
    }

    /**
     * 發送 HTML 郵件
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        if (mailSender == null || fromEmail == null || fromEmail.isEmpty()) {
            logger.warn("郵件服務未配置，無法發送郵件給 {} - 主題: {}", to, subject);
            logger.warn("請在 backend/.env 檔案中設定 MAIL_USERNAME 和 MAIL_PASSWORD");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("已發送 HTML 郵件給 {} - 主題: {}", to, subject);
        } catch (Exception e) {
            logger.error("發送 HTML 郵件失敗：{} - 收件人: {}, 主題: {}", e.getMessage(), to, subject);
            throw e;
        }
    }

    /**
     * 使用模板發送郵件
     */
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) throws MessagingException {
        Context context = new Context();
        context.setVariables(variables);
        context.setVariable("appName", appProperties.getName());
        context.setVariable("logoUrl", appProperties.getLogoUrl());

        String htmlContent = templateEngine.process(templateName, context);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 發送訂閱續訂提醒
     */
    public void sendSubscriptionRenewalReminder(String to, String userName, String subscriptionName, LocalDate renewalDate, Double amount) throws MessagingException {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("subscriptionName", subscriptionName);
        context.setVariable("renewalDate", renewalDate);
        context.setVariable("amount", amount);
        context.setVariable("appName", appProperties.getName());
        context.setVariable("logoUrl", appProperties.getLogoUrl());

        String htmlContent = templateEngine.process("email/subscription-renewal", context);
        sendHtmlEmail(to, appProperties.getName() + " - 訂閱續訂提醒", htmlContent);
    }

    /**
     * 發送歡迎郵件
     */
    public void sendWelcomeEmail(String to, String userName) throws MessagingException {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("appName", appProperties.getName());
        context.setVariable("logoUrl", appProperties.getLogoUrl());

        String htmlContent = templateEngine.process("email/welcome", context);
        sendHtmlEmail(to, "歡迎加入 " + appProperties.getName(), htmlContent);
    }

    /**
     * 發送 Email 驗證郵件
     */
    public void sendEmailVerification(String to, String userName, String verificationUrl) throws MessagingException {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("appName", appProperties.getName());
        context.setVariable("logoUrl", appProperties.getLogoUrl());

        String htmlContent = templateEngine.process("email/email-verification", context);
        sendHtmlEmail(to, "歡迎加入 " + appProperties.getName() + " - 請驗證您的電子郵件", htmlContent);
    }
}
