package com.cpf.education.online.externalrest.dto;
import java.util.Map;
/** 기관 응답을 typed DTO로 표현합니다. resultCode는 기관 계약 코드입니다. */
public record InstitutionInquiryResponse(String resultCode, String status, Map<String,Object> detail) { }
