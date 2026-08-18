package com.cpf.education.online.transaction.requiresnew.dto;
/** OrderProcessingCommand는 Outer REQUIRED와 독립 REQUIRES_NEW 경계를 서로 다른 Service Bean으로 보여주는 Transaction Golden Path입니다. */
public record OrderProcessingCommand(String orderId,String auditId,boolean failOuter) { }
