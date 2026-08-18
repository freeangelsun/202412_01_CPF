package com.cpf.education.online.fixedlength.dto;
import java.util.Map;
/** AccountInquiryRequest는 고정길이 전문 Public API와 기관 Outcome Adapter를 분리하는 Fixed-Length Golden Path입니다. */
public record AccountInquiryRequest(Map<String,Object> fields){ public AccountInquiryRequest{fields=fields==null?Map.of():Map.copyOf(fields);} }
