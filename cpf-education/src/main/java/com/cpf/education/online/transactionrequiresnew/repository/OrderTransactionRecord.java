package com.cpf.education.online.transactionrequiresnew.repository;
/** OrderTransactionRecord는 Outer REQUIRED와 독립 REQUIRES_NEW 경계를 서로 다른 Service Bean으로 보여주는 Transaction Golden Path입니다. */
public record OrderTransactionRecord(String id,String type,String value) { }
