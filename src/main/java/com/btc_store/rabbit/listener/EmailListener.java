package com.btc_store.rabbit.listener;

import com.btc_store.rabbit.config.RabbitMQConfig;
import com.btc_store.rabbit.dto.EmailRequestDto;
import com.btc_store.rabbit.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailListener {
    
    private final EmailService emailService;
    
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailEvent(Map<String, Object> event) {
        log.info("Email Event alındı: {}", event);
        
        try {
            String subject = (String) event.get("subject");
            String body = (String) event.get("body");
            List<String> recipients = (List<String>) event.get("recipients");
            String siteCode = (String) event.get("siteCode");
            
            // Tüm alıcılara mail gönder
            if (recipients != null && !recipients.isEmpty()) {
                for (String email : recipients) {
                    EmailRequestDto emailRequest = EmailRequestDto.builder()
                            .to(email)
                            .subject(subject)
                            .body(body)
                            .siteCode(siteCode) // Site code'u ekle
                            .build();
                    
                    emailService.sendEmail(emailRequest);
                }
                
                log.info("Email Event işlendi: {} alıcıya mail gönderildi", recipients.size());
            }
        } catch (Exception e) {
            log.error("Email Event işlenirken hata oluştu: {}", e.getMessage(), e);
        }
    }
}
