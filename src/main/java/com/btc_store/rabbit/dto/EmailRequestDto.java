package com.btc_store.rabbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String to;
    private String subject;
    private String body;
    private String siteCode;
}
