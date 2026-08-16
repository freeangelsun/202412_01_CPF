package com.cpf.messaging.api;

/** Provider acknowledgement/result를 CPF 공통 의미로 노출합니다. */
public record CpfBrokerBridgeResult(boolean accepted, String transport, String destination, String messageId, String transactionId, String detail) { }
