package com.btc_store.rabbit.service.impl;

import com.btc_store.domain.model.custom.SiteModel;
import com.btc_store.rabbit.dto.EmailRequestDto;
import com.btc_store.rabbit.service.EmailService;
import com.btc_store.service.ParameterService;
import com.btc_store.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final ParameterService parameterService;
    private final SiteService siteService;
    
    @Override
    public void sendEmail(EmailRequestDto emailRequest) {
        try {
            // Get site from emailRequest (not from HTTP request context)
            String siteCode = emailRequest.getSiteCode() != null ? emailRequest.getSiteCode() : "btcstore";
            var siteModel = siteService.getSiteModel(siteCode);
            
            // Mail gönderme tipini kontrol et
            String mailSendType = parameterService.getValueByCode("mail.send.type", siteModel);
            
            if (mailSendType == null || mailSendType.isEmpty()) {
                log.error("mail.send.type parametresi tanımlı değil!");
                throw new RuntimeException("Mail gönderme tipi tanımlı değil");
            }
            
            log.info("Mail gönderme tipi: {}", mailSendType);
            
            // Şimdilik sadece SMTP destekleniyor (gmail, exchange vb.)
            if ("gmail".equalsIgnoreCase(mailSendType) || "exchange".equalsIgnoreCase(mailSendType)) {
                sendEmailViaSMTP(emailRequest, siteModel);
            } else if ("sap".equalsIgnoreCase(mailSendType)) {
                log.warn("SAP mail gönderimi henüz desteklenmiyor");
                throw new RuntimeException("SAP mail gönderimi desteklenmiyor");
            } else if ("pg".equalsIgnoreCase(mailSendType)) {
                log.warn("Posta güvercini henüz desteklenmiyor 🕊️");
                throw new RuntimeException("Posta güvercini desteklenmiyor");
            } else {
                log.error("Bilinmeyen mail gönderme tipi: {}", mailSendType);
                throw new RuntimeException("Bilinmeyen mail gönderme tipi: " + mailSendType);
            }
            
        } catch (Exception e) {
            log.error("Email gönderilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Email gönderilemedi", e);
        }
    }
    
    private void sendEmailViaSMTP(EmailRequestDto emailRequest, SiteModel siteModel) throws Exception {
        // SMTP ayarlarını ParameterService'den al
        String smtpIp = parameterService.getValueByCode("mail.smtp.ip", siteModel);
        String smtpPort = parameterService.getValueByCode("mail.smtp.port", siteModel);
        String smtpUsername = parameterService.getValueByCode("mail.smtp.username", siteModel);
        String smtpPassword = parameterService.getValueByCode("mail.smtp.password", siteModel);
        String smtpFrom = parameterService.getValueByCode("mail.smtp.from", siteModel);
        String smtpAuth = parameterService.getValueByCode("mail.smtp.auth", siteModel);
        String smtpStartTls = parameterService.getValueByCode("mail.smtp.starttls.enable", siteModel);
        
        // Zorunlu parametreleri kontrol et
        if (smtpIp == null || smtpIp.isEmpty()) {
            throw new RuntimeException("mail.smtp.ip parametresi tanımlı değil!");
        }
        
        // Dynamic JavaMailSender oluştur
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpIp);
        mailSender.setPort(smtpPort != null ? Integer.parseInt(smtpPort) : 587);
        
        if (smtpUsername != null && !smtpUsername.isEmpty()) {
            mailSender.setUsername(smtpUsername);
        }
        
        if (smtpPassword != null && !smtpPassword.isEmpty()) {
            mailSender.setPassword(smtpPassword);
        }
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth != null ? smtpAuth : "true");
        props.put("mail.smtp.starttls.enable", smtpStartTls != null ? smtpStartTls : "true");
        
        // Mail gönder
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        String fromEmail = smtpFrom != null && !smtpFrom.isEmpty() ? smtpFrom : smtpUsername;
        if (fromEmail != null && !fromEmail.isEmpty()) {
            helper.setFrom(fromEmail);
        }
        
        helper.setTo(emailRequest.getTo());
        helper.setSubject(emailRequest.getSubject());
        helper.setText(emailRequest.getBody(), true); // HTML olarak gönder
        
        mailSender.send(message);
        
        log.info("Email gönderildi: {} -> {}", fromEmail, emailRequest.getTo());
    }
}

