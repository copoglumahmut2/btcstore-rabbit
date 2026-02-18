package com.btc_store.rabbit.service;

import com.btc_store.rabbit.dto.EmailRequestDto;

public interface EmailService {
    void sendEmail(EmailRequestDto emailRequest);
}
